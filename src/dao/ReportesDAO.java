package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.ReporteResultado;

/** Consultas de solo lectura para los reportes administrativos. */
public class ReportesDAO {

    public Map<String, String> cargarKpis() throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM cliente WHERE estado_cliente=TRUE) clientes, "
                + "(SELECT COUNT(*) FROM membresia WHERE estado_membresia='ACTIVA') membresias, "
                + "(SELECT COALESCE(SUM(monto_pago),0) FROM pago WHERE estado_pago='CONFIRMADO' "
                + " AND DATE_TRUNC('month',fecha_hora_pago)=DATE_TRUNC('month',CURRENT_DATE)) ingresos, "
                + "(SELECT COUNT(*) FROM asistencia WHERE fecha_asistencia>=DATE_TRUNC('month',CURRENT_DATE)) asistencias, "
                + "(SELECT COUNT(*) FROM asignacion_rutina WHERE estado_asignacion='ACTIVA') rutinas";
        LinkedHashMap<String, String> r = new LinkedHashMap<>();
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                r.put("Clientes activos", rs.getString("clientes"));
                r.put("Membresías activas", rs.getString("membresias"));
                r.put("Ingresos del mes", dinero(rs.getBigDecimal("ingresos")));
                r.put("Asistencias del mes", rs.getString("asistencias"));
                r.put("Rutinas activas", rs.getString("rutinas"));
            }
        }
        return r;
    }

    public ReporteResultado ingresos(LocalDate desde, LocalDate hasta, String filtro) throws SQLException {
        String like = normalizarFiltro(filtro);
        String sql = "SELECT pg.fecha_hora_pago::date AS \"Fecha\", "
                + "TRIM(pe.nombres||' '||pe.apellidos) AS \"Cliente\", "
                + "f.numero_factura AS \"Factura\", mp.nombre_metodo AS \"Método\", "
                + "pg.codigo_pago AS \"Código pago\", pg.monto_pago AS \"Valor\" "
                + "FROM pago pg JOIN factura f ON f.id_factura=pg.id_factura "
                + "JOIN persona pe ON pe.id_persona=f.id_cliente "
                + "JOIN metodo_pago mp ON mp.id_metodo_pago=pg.id_metodo_pago "
                + "WHERE pg.estado_pago='CONFIRMADO' AND pg.fecha_hora_pago::date BETWEEN ? AND ? "
                + "AND (?='' OR pe.nombres ILIKE ? OR pe.apellidos ILIKE ? OR f.numero_factura ILIKE ? OR mp.nombre_metodo ILIKE ?) "
                + "ORDER BY pg.fecha_hora_pago DESC";
        ReporteResultado base = ejecutar("Ingresos y facturación", sql,
                desde, hasta, filtroVacio(filtro), like, like, like, like);
        BigDecimal total = BigDecimal.ZERO;
        for (Object[] fila : base.getFilas()) {
            if (fila[5] instanceof BigDecimal b) total = total.add(b);
        }
        LinkedHashMap<String,String> resumen = new LinkedHashMap<>();
        resumen.put("Ingresos totales", dinero(total));
        resumen.put("Pagos confirmados", String.valueOf(base.getFilas().size()));
        java.util.Set<Object> facturas = new java.util.HashSet<>();
        for (Object[] fila : base.getFilas()) if (fila[2] != null) facturas.add(fila[2]);
        resumen.put("Facturas", String.valueOf(facturas.size()));
        resumen.put("Promedio por pago", base.getFilas().isEmpty() ? "$ 0.00" : dinero(total.divide(BigDecimal.valueOf(base.getFilas().size()), 2, RoundingMode.HALF_UP)));
        resumen.put("Período", desde + " a " + hasta);
        return copiar(base, resumen);
    }

    public ReporteResultado clientes(LocalDate desde, LocalDate hasta, String filtro) throws SQLException {
        String like = normalizarFiltro(filtro);
        String sql = "SELECT p.cedula AS \"Cédula\", c.codigo_cliente AS \"Código\", "
                + "TRIM(p.nombres||' '||p.apellidos) AS \"Cliente\", p.telefono AS \"Teléfono\", "
                + "p.correo AS \"Correo\", c.fecha_registro AS \"Registro\", "
                + "CASE WHEN c.estado_cliente THEN 'ACTIVO' ELSE 'INACTIVO' END AS \"Estado\" "
                + "FROM cliente c JOIN persona p ON p.id_persona=c.id_persona "
                + "WHERE c.fecha_registro BETWEEN ? AND ? "
                + "AND (?='' OR p.cedula ILIKE ? OR p.nombres ILIKE ? OR p.apellidos ILIKE ? OR c.codigo_cliente ILIKE ?) "
                + "ORDER BY c.fecha_registro DESC, p.apellidos";
        ReporteResultado base = ejecutar("Clientes", sql, desde, hasta,
                filtroVacio(filtro), like, like, like, like);
        int activos=0, inactivos=0;
        for(Object[] f: base.getFilas()) { if("ACTIVO".equals(f[6])) activos++; else inactivos++; }
        LinkedHashMap<String,String> r=new LinkedHashMap<>();
        r.put("Registrados en período", String.valueOf(base.getFilas().size()));
        r.put("Activos", String.valueOf(activos));
        r.put("Inactivos", String.valueOf(inactivos));
        r.put("Período", desde+" a "+hasta);
        return copiar(base,r);
    }

    public ReporteResultado membresiasPorVencer(int dias, String filtro) throws SQLException {
        String like=normalizarFiltro(filtro);
        String sql="SELECT m.numero_membresia AS \"Membresía\", TRIM(p.nombres||' '||p.apellidos) AS \"Cliente\", "
                +"tm.nombre AS \"Tipo\", m.fecha_inicio AS \"Inicio\", m.fecha_fin AS \"Fin\", "
                +"(m.fecha_fin-CURRENT_DATE)::int AS \"Días restantes\", "
                +"CASE WHEN m.fecha_fin<CURRENT_DATE THEN 'VENCIDA' WHEN m.fecha_fin<=CURRENT_DATE+? THEN 'POR VENCER' ELSE m.estado_membresia END AS \"Estado\" "
                +"FROM membresia m JOIN persona p ON p.id_persona=m.id_cliente JOIN tipo_membresia tm ON tm.id_tipo_membresia=m.id_tipo_membresia "
                +"WHERE m.fecha_fin<=CURRENT_DATE+? AND (?='' OR p.nombres ILIKE ? OR p.apellidos ILIKE ? OR m.numero_membresia ILIKE ?) "
                +"ORDER BY m.fecha_fin";
        ReporteResultado base=ejecutar("Membresías próximas a vencer",sql,dias,dias,filtroVacio(filtro),like,like,like);
        LinkedHashMap<String,String> r=new LinkedHashMap<>(); r.put("Registros",String.valueOf(base.getFilas().size())); r.put("Horizonte",dias+" días");
        return copiar(base,r);
    }

    public ReporteResultado asistencia(LocalDate desde, LocalDate hasta, String filtro) throws SQLException {
        String like=normalizarFiltro(filtro);
        String sql="SELECT a.fecha_asistencia AS \"Fecha\", TRIM(p.nombres||' '||p.apellidos) AS \"Cliente\", "
                +"a.hora_entrada AS \"Entrada\", a.hora_salida AS \"Salida\", a.tipo_acceso AS \"Tipo acceso\", a.estado_acceso AS \"Estado\" "
                +"FROM asistencia a JOIN persona p ON p.id_persona=a.id_cliente WHERE a.fecha_asistencia BETWEEN ? AND ? "
                +"AND (?='' OR p.nombres ILIKE ? OR p.apellidos ILIKE ? OR a.tipo_acceso ILIKE ?) ORDER BY a.fecha_asistencia DESC,a.hora_entrada DESC";
        ReporteResultado base=ejecutar("Asistencia",sql,desde,hasta,filtroVacio(filtro),like,like,like);
        LinkedHashMap<String,String> r=new LinkedHashMap<>(); r.put("Asistencias",String.valueOf(base.getFilas().size())); r.put("Período",desde+" a "+hasta);
        return copiar(base,r);
    }

    public ReporteResultado clases(LocalDate desde, LocalDate hasta, String filtro) throws SQLException {
        String like=normalizarFiltro(filtro);
        String sql="SELECT cg.nombre_clase AS \"Clase\", TRIM(p.nombres||' '||p.apellidos) AS \"Entrenador\", cg.fecha_hora AS \"Fecha/hora\", "
                +"cg.cupo_maximo AS \"Cupo\", COUNT(r.id_reserva)::int AS \"Reservas\", "
                +"COUNT(r.id_reserva) FILTER (WHERE r.asistencia_confirmada=TRUE)::int AS \"Asistencias confirmadas\", "
                +"CASE WHEN cg.cupo_maximo>0 THEN ROUND(100.0*COUNT(r.id_reserva) FILTER (WHERE r.estado_reserva='ACTIVA')/cg.cupo_maximo,1) ELSE 0 END AS \"Ocupación %\" "
                +"FROM clase_grupal cg JOIN persona p ON p.id_persona=cg.id_entrenador LEFT JOIN reserva r ON r.id_clase=cg.id_clase "
                +"WHERE cg.fecha_hora::date BETWEEN ? AND ? AND (?='' OR cg.nombre_clase ILIKE ? OR p.nombres ILIKE ? OR p.apellidos ILIKE ?) "
                +"GROUP BY cg.id_clase,cg.nombre_clase,p.nombres,p.apellidos,cg.fecha_hora,cg.cupo_maximo ORDER BY cg.fecha_hora DESC";
        ReporteResultado base=ejecutar("Clases grupales y reservas",sql,desde,hasta,filtroVacio(filtro),like,like,like);
        LinkedHashMap<String,String> r=new LinkedHashMap<>(); r.put("Clases",String.valueOf(base.getFilas().size())); r.put("Período",desde+" a "+hasta);
        return copiar(base,r);
    }

    public ReporteResultado rutinas(LocalDate desde, LocalDate hasta, String filtro) throws SQLException {
        String like=normalizarFiltro(filtro);
        String sql="SELECT TRIM(pc.nombres||' '||pc.apellidos) AS \"Cliente\", ru.nombre_rutina AS \"Rutina\", "
                +"TRIM(pe.nombres||' '||pe.apellidos) AS \"Entrenador\", ar.fecha_inicio AS \"Inicio\", ar.fecha_fin AS \"Fin\", ar.estado_asignacion AS \"Estado\" "
                +"FROM asignacion_rutina ar JOIN rutina ru ON ru.id_rutina=ar.id_rutina JOIN persona pc ON pc.id_persona=ar.id_cliente "
                +"LEFT JOIN persona pe ON pe.id_persona=ru.id_entrenador WHERE ar.fecha_inicio<=? AND COALESCE(ar.fecha_fin,?)>=? "
                +"AND (?='' OR pc.nombres ILIKE ? OR pc.apellidos ILIKE ? OR ru.nombre_rutina ILIKE ? OR ar.estado_asignacion ILIKE ?) "
                +"ORDER BY ar.fecha_inicio DESC,pc.apellidos";
        ReporteResultado base=ejecutar("Rutinas asignadas",sql,hasta,hasta,desde,filtroVacio(filtro),like,like,like,like);
        LinkedHashMap<String,String> r=new LinkedHashMap<>(); r.put("Asignaciones",String.valueOf(base.getFilas().size())); r.put("Período",desde+" a "+hasta); r.put("Nota","Admite múltiples rutinas por cliente");
        return copiar(base,r);
    }

    public ReporteResultado progreso(LocalDate desde, LocalDate hasta, String filtro) throws SQLException {
        String like=normalizarFiltro(filtro);
        String sql="SELECT pr.fecha_registro AS \"Fecha\", TRIM(p.nombres||' '||p.apellidos) AS \"Cliente\", r.nombre_rutina AS \"Rutina\", "
                +"pr.sesiones_planificadas AS \"Planificadas\", pr.sesiones_completadas AS \"Completadas\", "
                +"CASE WHEN pr.sesiones_planificadas>0 THEN ROUND(100.0*pr.sesiones_completadas/pr.sesiones_planificadas,1) ELSE 0 END AS \"Cumplimiento %\", "
                +"pr.nivel_esfuerzo AS \"Esfuerzo\", pr.estado_progreso AS \"Estado\" "
                +"FROM progreso_rutina pr JOIN persona p ON p.id_persona=pr.id_cliente JOIN rutina r ON r.id_rutina=pr.id_rutina "
                +"WHERE pr.fecha_registro BETWEEN ? AND ? AND (?='' OR p.nombres ILIKE ? OR p.apellidos ILIKE ? OR r.nombre_rutina ILIKE ?) "
                +"ORDER BY pr.fecha_registro DESC";
        ReporteResultado base=ejecutar("Progreso de entrenamiento",sql,desde,hasta,filtroVacio(filtro),like,like,like);
        LinkedHashMap<String,String> r=new LinkedHashMap<>(); r.put("Registros de progreso",String.valueOf(base.getFilas().size())); r.put("Período",desde+" a "+hasta);
        return copiar(base,r);
    }

    public ReporteResultado progresoFisico(LocalDate desde, LocalDate hasta, String filtro) throws SQLException {
        String like=normalizarFiltro(filtro);
        String sql="SELECT ef.fecha_evaluacion AS \"Fecha\", TRIM(p.nombres||' '||p.apellidos) AS \"Cliente\", ef.codigo_evaluacion AS \"Evaluación\", "
                +"mc.peso_kg AS \"Peso kg\", mc.altura_m AS \"Altura m\", mc.porcentaje_grasa AS \"Grasa %\", mc.cintura_cm AS \"Cintura cm\", "
                +"COALESCE((SELECT pn.nombre_plan FROM plan_nutricional pn WHERE pn.id_cliente=ef.id_cliente AND pn.estado_plan='ACTIVO' ORDER BY pn.fecha_creacion DESC LIMIT 1),'Sin plan activo') AS \"Plan nutricional\" "
                +"FROM evaluacion_fisica ef JOIN persona p ON p.id_persona=ef.id_cliente LEFT JOIN medicion_corporal mc ON mc.id_evaluacion=ef.id_evaluacion "
                +"WHERE ef.fecha_evaluacion BETWEEN ? AND ? AND (?='' OR p.cedula ILIKE ? OR p.nombres ILIKE ? OR p.apellidos ILIKE ?) "
                +"ORDER BY ef.fecha_evaluacion DESC,p.apellidos";
        ReporteResultado base=ejecutar("Progreso físico / nutricional",sql,desde,hasta,filtroVacio(filtro),like,like,like);
        LinkedHashMap<String,String> r=new LinkedHashMap<>(); r.put("Evaluaciones",String.valueOf(base.getFilas().size())); r.put("Período",desde+" a "+hasta);
        return copiar(base,r);
    }

    private ReporteResultado ejecutar(String titulo,String sql,Object... params) throws SQLException {
        List<String> columnas=new ArrayList<>(); List<Object[]> filas=new ArrayList<>();
        try(Connection c=ConexionPostgreSQL.getConexion(); PreparedStatement ps=c.prepareStatement(sql)){
            for(int i=0;i<params.length;i++) set(ps,i+1,params[i]);
            try(ResultSet rs=ps.executeQuery()){
                ResultSetMetaData md=rs.getMetaData(); for(int i=1;i<=md.getColumnCount();i++) columnas.add(md.getColumnLabel(i));
                while(rs.next()){ Object[] f=new Object[md.getColumnCount()]; for(int i=0;i<f.length;i++) f[i]=rs.getObject(i+1); filas.add(f); }
            }
        }
        return new ReporteResultado(titulo,columnas,filas,new LinkedHashMap<>());
    }

    private void set(PreparedStatement ps,int i,Object v)throws SQLException{
        if(v instanceof LocalDate d) ps.setDate(i,Date.valueOf(d)); else if(v instanceof Integer n) ps.setInt(i,n); else ps.setObject(i,v);
    }
    private ReporteResultado copiar(ReporteResultado b,Map<String,String> r){return new ReporteResultado(b.getTitulo(),b.getColumnas(),b.getFilas(),r);}
    private String normalizarFiltro(String f){return "%"+(f==null?"":f.trim())+"%";}
    private String filtroVacio(String f){return f==null?"":f.trim();}
    private String dinero(BigDecimal b){return "$ "+(b==null?BigDecimal.ZERO:b).setScale(2,RoundingMode.HALF_UP);}
}
