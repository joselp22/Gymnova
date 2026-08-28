package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consultas de solo lectura para las portadas funcionales de los modulos. */
public class ResumenModuloDAO {

    public Map<String, Object> perfilCliente(Long idCliente) throws SQLException {
        return una("SELECT c.codigo_cliente,p.cedula,p.nombres,p.apellidos,p.telefono,p.correo,"
                + "c.peso_inicial,c.peso_meta,c.estado_cliente "
                + "FROM cliente c JOIN persona p ON p.id_persona=c.id_persona "
                + "WHERE c.id_persona=?", idCliente);
    }

    public Map<String, Object> saludCliente(Long idCliente) throws SQLException {
        return una("SELECT mc.peso_kg,mc.altura_m,mc.porcentaje_grasa,"
                + "CASE WHEN mc.altura_m>0 THEN ROUND(mc.peso_kg/(mc.altura_m*mc.altura_m),2) END imc,"
                + "ef.fecha_evaluacion,ef.proxima_evaluacion,ef.condicion_general "
                + "FROM evaluacion_fisica ef JOIN medicion_corporal mc ON mc.id_evaluacion=ef.id_evaluacion "
                + "WHERE ef.id_cliente=? ORDER BY ef.fecha_evaluacion DESC,ef.id_evaluacion DESC LIMIT 1", idCliente);
    }

    public List<Map<String, Object>> historialSalud(Long idCliente, int limite) throws SQLException {
        return varias("SELECT ef.fecha_evaluacion,ef.tipo_evaluacion,ef.estado_evaluacion,mc.peso_kg "
                + "FROM evaluacion_fisica ef LEFT JOIN medicion_corporal mc ON mc.id_evaluacion=ef.id_evaluacion "
                + "WHERE ef.id_cliente=? ORDER BY ef.fecha_evaluacion DESC LIMIT ?", idCliente, limite);
    }

    public Map<String, Object> planNutricional(Long idCliente) throws SQLException {
        return una("SELECT codigo_plan,nombre_plan,fecha_inicio,fecha_fin,calorias_objetivo,"
                + "proteinas_objetivo_g,carbohidratos_objetivo_g,restricciones_generales,estado_plan "
                + "FROM plan_nutricional WHERE id_cliente=? "
                + "ORDER BY (estado_plan='ACTIVO') DESC,fecha_inicio DESC,id_plan_nutricional DESC LIMIT 1", idCliente);
    }

    public List<Map<String, Object>> comidasCliente(Long idCliente, int limite) throws SQLException {
        return varias("SELECT ipa.hora_consumo,ipa.tipo_comida,a.nombre_alimento,ipa.cantidad,ipa.unidad_medida "
                + "FROM incluye_alimento ipa JOIN alimento a ON a.id_alimento=ipa.id_alimento "
                + "JOIN plan_nutricional pn ON pn.id_plan_nutricional=ipa.id_plan_nutricional "
                + "WHERE pn.id_cliente=? AND pn.estado_plan='ACTIVO' AND ipa.estado_detalle='ACTIVO' "
                + "ORDER BY ipa.dia_semana,ipa.orden_comida LIMIT ?", idCliente, limite);
    }

    public Map<String, Object> membresiaCliente(Long idCliente) throws SQLException {
        return una("SELECT m.numero_membresia,tm.nombre plan,tm.descripcion,tm.duracion_dias,tm.precio_base,"
                + "m.fecha_inicio,m.fecha_fin,m.estado_membresia,"
                + "GREATEST(0,(m.fecha_fin-CURRENT_DATE)) dias_restantes "
                + "FROM membresia m JOIN tipo_membresia tm ON tm.id_tipo_membresia=m.id_tipo_membresia "
                + "WHERE m.id_cliente=? ORDER BY (m.estado_membresia='ACTIVA') DESC,m.fecha_fin DESC LIMIT 1", idCliente);
    }

    public List<Map<String, Object>> beneficiosMembresia(Long idCliente) throws SQLException {
        return varias("SELECT tmb.beneficio FROM membresia m JOIN tipo_membresia_beneficio tmb "
                + "ON tmb.id_tipo_membresia=m.id_tipo_membresia WHERE m.id_cliente=? "
                + "ORDER BY (m.estado_membresia='ACTIVA') DESC,m.fecha_fin DESC LIMIT 5", idCliente);
    }

    public List<Map<String, Object>> reservasCliente(Long idCliente, int limite) throws SQLException {
        return varias("SELECT r.codigo_reserva,r.fecha_hora_reserva,r.estado_reserva,h.dia_semana,"
                + "h.hora_inicio,h.hora_fin,cg.nombre_clase "
                + "FROM reserva r JOIN horario h ON h.id_horario=r.id_horario "
                + "LEFT JOIN clase_especial ce ON ce.id_horario=h.id_horario "
                + "LEFT JOIN clase_grupal cg ON cg.id_clase=ce.id_clase "
                + "WHERE r.id_cliente=? ORDER BY r.fecha_hora_reserva DESC LIMIT ?", idCliente, limite);
    }

    public Map<String, Object> accesoCliente(Long idCliente) throws SQLException {
        return una("SELECT c.codigo_cliente,COALESCE(m.estado_membresia,'SIN MEMBRESIA') estado_membresia,"
                + "(SELECT MAX(a.fecha_asistencia::text||' '||a.hora_entrada::text) FROM asistencia a "
                + "WHERE a.id_cliente=c.id_persona) ultimo_ingreso "
                + "FROM cliente c LEFT JOIN LATERAL (SELECT estado_membresia FROM membresia "
                + "WHERE id_cliente=c.id_persona ORDER BY (estado_membresia='ACTIVA') DESC,fecha_fin DESC LIMIT 1) m ON true "
                + "WHERE c.id_persona=?", idCliente);
    }

    public List<Map<String, Object>> clientesPorRol(String rol, Long idPersona, int limite) throws SQLException {
        String normal = rol == null ? "" : rol.trim().toUpperCase();
        if ("ENTRENADOR".equals(normal)) {
            return varias("SELECT DISTINCT c.id_persona id_cliente,c.codigo_cliente,p.nombres||' '||p.apellidos nombre,"
                    + "r.nombre_rutina detalle,ar.estado_asignacion estado,"
                    + "COALESCE(ROUND(100.0*pr.sesiones_completadas/NULLIF(pr.sesiones_planificadas,0)),0) progreso "
                    + "FROM asignacion_rutina ar JOIN rutina r ON r.id_rutina=ar.id_rutina "
                    + "JOIN cliente c ON c.id_persona=ar.id_cliente JOIN persona p ON p.id_persona=c.id_persona "
                    + "LEFT JOIN LATERAL (SELECT sesiones_completadas,sesiones_planificadas FROM progreso_rutina "
                    + "WHERE id_cliente=c.id_persona AND id_rutina=r.id_rutina ORDER BY fecha_registro DESC LIMIT 1) pr ON true "
                    + "WHERE r.id_entrenador=? ORDER BY c.codigo_cliente LIMIT ?", idPersona, limite);
        }
        if ("NUTRICIONISTA".equals(normal)) {
            return varias("SELECT c.id_persona id_cliente,c.codigo_cliente,p.nombres||' '||p.apellidos nombre,"
                    + "pn.nombre_plan detalle,pn.estado_plan estado,0 progreso "
                    + "FROM plan_nutricional pn JOIN cliente c ON c.id_persona=pn.id_cliente "
                    + "JOIN persona p ON p.id_persona=c.id_persona WHERE pn.id_nutricionista=? "
                    + "ORDER BY pn.fecha_creacion DESC LIMIT ?", idPersona, limite);
        }
        return varias("SELECT c.id_persona id_cliente,c.codigo_cliente,p.nombres||' '||p.apellidos nombre,"
                + "COALESCE(tm.nombre,'Sin membresia') detalle,"
                + "CASE WHEN c.estado_cliente THEN 'ACTIVO' ELSE 'INACTIVO' END estado,0 progreso "
                + "FROM cliente c JOIN persona p ON p.id_persona=c.id_persona "
                + "LEFT JOIN LATERAL (SELECT id_tipo_membresia FROM membresia WHERE id_cliente=c.id_persona "
                + "ORDER BY (estado_membresia='ACTIVA') DESC,fecha_fin DESC LIMIT 1) m ON true "
                + "LEFT JOIN tipo_membresia tm ON tm.id_tipo_membresia=m.id_tipo_membresia "
                + "ORDER BY c.fecha_registro DESC LIMIT ?", limite);
    }

    public List<Map<String, Object>> reservasOperacion(int limite)
            throws SQLException {
        return varias("SELECT r.id_cliente,c.codigo_cliente,"
                + "p.nombres||' '||p.apellidos cliente,"
                + "r.fecha_hora_reserva,r.estado_reserva,h.dia_semana,"
                + "h.hora_inicio,h.hora_fin,COALESCE(cg.nombre_clase,'Acceso general') nombre_clase "
                + "FROM reserva r JOIN cliente c ON c.id_persona=r.id_cliente "
                + "JOIN persona p ON p.id_persona=c.id_persona "
                + "JOIN horario h ON h.id_horario=r.id_horario "
                + "LEFT JOIN clase_especial ce ON ce.id_horario=h.id_horario "
                + "LEFT JOIN clase_grupal cg ON cg.id_clase=ce.id_clase "
                + "ORDER BY r.fecha_hora_reserva DESC LIMIT ?", limite);
    }

    public List<Map<String, Object>> membresiasOperacion(int limite)
            throws SQLException {
        return varias("SELECT m.id_cliente,c.codigo_cliente,"
                + "p.nombres||' '||p.apellidos cliente,m.numero_membresia,"
                + "tm.nombre plan,m.fecha_inicio,m.fecha_fin,m.costo_final,"
                + "m.estado_membresia,GREATEST(0,m.fecha_fin-CURRENT_DATE) dias_restantes "
                + "FROM membresia m JOIN cliente c ON c.id_persona=m.id_cliente "
                + "JOIN persona p ON p.id_persona=c.id_persona "
                + "JOIN tipo_membresia tm ON tm.id_tipo_membresia=m.id_tipo_membresia "
                + "ORDER BY (m.estado_membresia='ACTIVA') DESC,m.fecha_fin,m.id_membresia DESC "
                + "LIMIT ?", limite);
    }

    public Map<String,Object> rutinaVisible(String rol,Long idPersona) throws SQLException {
        String normal=rol==null?"":rol.trim().toUpperCase();
        if("CLIENTE".equals(normal))
            return una("SELECT r.id_rutina,r.nombre_rutina,r.nivel,r.duracion_semanas,r.estado_rutina "
                    + "FROM asignacion_rutina ar JOIN rutina r ON r.id_rutina=ar.id_rutina "
                    + "WHERE ar.id_cliente=? ORDER BY (ar.estado_asignacion='ACTIVA') DESC,ar.fecha_inicio DESC LIMIT 1",idPersona);
        if("ENTRENADOR".equals(normal))
            return una("SELECT id_rutina,nombre_rutina,nivel,duracion_semanas,estado_rutina FROM rutina "
                    + "WHERE id_entrenador=? ORDER BY (estado_rutina='ACTIVA') DESC,fecha_creacion DESC LIMIT 1",idPersona);
        return una("SELECT id_rutina,nombre_rutina,nivel,duracion_semanas,estado_rutina FROM rutina "
                + "ORDER BY (estado_rutina='ACTIVA') DESC,fecha_creacion DESC LIMIT 1");
    }

    public List<Map<String,Object>> ejerciciosRutina(Long idRutina,int limite) throws SQLException {
        if(idRutina==null) return new ArrayList<>();
        return varias("SELECT ce.dia_semana,e.nombre_ejercicio,ce.series,ce.repeticiones,ce.descanso_segundos "
                + "FROM contiene_ejercicio ce JOIN ejercicio e ON e.id_ejercicio=ce.id_ejercicio "
                + "WHERE ce.id_rutina=? ORDER BY ce.dia_semana,ce.orden LIMIT ?",idRutina,limite);
    }

    public Map<String, Object> resumenFinanciero() throws SQLException {
        return una("SELECT COALESCE(SUM(monto_pago) FILTER (WHERE fecha_hora_pago::date=CURRENT_DATE "
                + "AND estado_pago='CONFIRMADO'),0) caja_hoy,"
                + "COUNT(*) FILTER (WHERE fecha_hora_pago::date=CURRENT_DATE AND estado_pago='CONFIRMADO') cobros_hoy,"
                + "(SELECT COUNT(*) FROM factura "
                + "WHERE estado_factura IN ('PENDIENTE','EMITIDA')) pendientes,"
                + "(SELECT COUNT(*) FROM comprobante "
                + "WHERE estado_comprobante<>'ANULADO') comprobantes FROM pago");
    }

    public List<Map<String, Object>> movimientosFinancieros(int limite) throws SQLException {
        return varias("SELECT pg.fecha_hora_pago,COALESCE(f.numero_factura,'Sin factura') referencia,"
                + "pg.monto_pago,pg.estado_pago FROM pago pg LEFT JOIN factura f ON f.id_factura=pg.id_factura "
                + "ORDER BY pg.fecha_hora_pago DESC LIMIT ?", limite);
    }

    public List<Map<String, Object>> actividadSemanal(String rol, Long idPersona) throws SQLException {
        String normal = rol == null ? "" : rol.trim().toUpperCase();
        String conteo;
        List<Object> parametros = new ArrayList<>();
        if ("CLIENTE".equals(normal)) {
            conteo = "SELECT fecha_registro fecha,COUNT(*) total FROM progreso_rutina WHERE id_cliente=? GROUP BY fecha_registro";
            parametros.add(idPersona);
        } else if ("ENTRENADOR".equals(normal)) {
            conteo = "SELECT pr.fecha_registro fecha,COUNT(*) total FROM progreso_rutina pr JOIN rutina r ON r.id_rutina=pr.id_rutina WHERE r.id_entrenador=? GROUP BY pr.fecha_registro";
            parametros.add(idPersona);
        } else if ("NUTRICIONISTA".equals(normal)) {
            conteo = "SELECT fecha_creacion fecha,COUNT(*) total FROM plan_nutricional WHERE id_nutricionista=? GROUP BY fecha_creacion";
            parametros.add(idPersona);
        } else {
            conteo = "SELECT fecha_asistencia fecha,COUNT(*) total FROM asistencia GROUP BY fecha_asistencia";
        }
        return varias("WITH dias AS (SELECT generate_series(CURRENT_DATE-6,CURRENT_DATE,INTERVAL '1 day')::date fecha),"
                + "c AS (" + conteo + ") SELECT d.fecha,COALESCE(c.total,0)::int total FROM dias d LEFT JOIN c USING(fecha) ORDER BY d.fecha",
                parametros.toArray());
    }

    private Map<String, Object> una(String sql, Object... parametros) throws SQLException {
        List<Map<String, Object>> filas = varias(sql, parametros);
        return filas.isEmpty() ? new LinkedHashMap<>() : filas.get(0);
    }

    private List<Map<String, Object>> varias(String sql, Object... parametros) throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<>();
        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) sentencia.setObject(i + 1, parametros[i]);
            try (ResultSet resultado = sentencia.executeQuery()) {
                ResultSetMetaData meta = resultado.getMetaData();
                while (resultado.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++)
                        fila.put(meta.getColumnLabel(i).toLowerCase(), resultado.getObject(i));
                    filas.add(fila);
                }
            }
        }
        return filas;
    }
}
