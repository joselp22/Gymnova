package utilidades;

import conexion.ConexionPostgreSQL;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Ensambla un reporte consolidado (HTML) con toda la información
 * disponible: KPIs generales, membresías, financiero, ocupación,
 * desempeño profesional y bitácora reciente. Se guarda en el destino
 * elegido por el usuario con nombre "Reporte YYYY-MM-DD.html".
 *
 * Se optó por HTML porque no requiere librerías externas, abre en
 * cualquier navegador y desde el navegador se puede imprimir o
 * "Guardar como PDF" directamente.
 */
public final class GeneradorReporteConsolidado {

    private GeneradorReporteConsolidado() {
    }

    /** Nombre sugerido para el archivo: "Reporte YYYY-MM-DD.html". */
    public static String nombreSugerido() {
        return "Reporte " + LocalDate.now() + ".html";
    }

    /** Genera el HTML consolidado y lo guarda en {@code destino}. */
    public static void generarYGuardar(Path destino) throws IOException,
            SQLException {
        String html = generarHtml();
        Files.writeString(destino, html, StandardCharsets.UTF_8);
    }

    private static String generarHtml() throws SQLException {
        StringBuilder h = new StringBuilder();
        DateTimeFormatter fmt = DateTimeFormatter
                .ofPattern("dd 'de' MMMM 'de' yyyy, HH:mm",
                        new Locale("es", "ES"));
        String generado = LocalDateTime.now().format(fmt);

        h.append("<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n")
         .append("<meta charset=\"UTF-8\"/>\n")
         .append("<title>Reporte GYMNOVA ").append(LocalDate.now())
         .append("</title>\n")
         .append("<style>\n").append(css()).append("</style>\n")
         .append("</head>\n<body>\n");

        h.append("<header><h1>GYMNOVA</h1>")
         .append("<p class=\"subtitulo\">Reporte administrativo consolidado</p>")
         .append("<p class=\"generado\">Generado el ").append(generado)
         .append("</p></header>\n");

        try (Connection c = ConexionPostgreSQL.getConexion()) {
            seccionResumenGeneral(h, c);
            seccionMembresias(h, c);
            seccionFinanciero(h, c);
            seccionClientes(h, c);
            seccionOcupacion(h, c);
            seccionDesempeno(h, c);
            seccionBitacora(h, c);
        }

        h.append("<footer>Reporte generado automáticamente por GYMNOVA · ")
         .append(generado).append("</footer>\n")
         .append("</body>\n</html>\n");
        return h.toString();
    }

    // ---- Secciones -------------------------------------------------------

    private static void seccionResumenGeneral(StringBuilder h, Connection c)
            throws SQLException {
        h.append("<section><h2>1. Resumen general</h2>\n");
        int clientes = queryInt(c,
                "SELECT COUNT(*) FROM cliente WHERE estado_cliente = TRUE");
        int membresias = queryInt(c,
                "SELECT COUNT(*) FROM membresia WHERE estado_membresia='ACTIVA'");
        BigDecimal pagosMes = queryDec(c,
                "SELECT COALESCE(SUM(monto_pago),0) FROM pago "
                + "WHERE DATE_TRUNC('month', fecha_hora_pago) = "
                + "  DATE_TRUNC('month', CURRENT_DATE) "
                + "AND estado_pago='CONFIRMADO'");
        int rutinas = queryInt(c,
                "SELECT COUNT(*) FROM asignacion_rutina "
                + "WHERE estado_asignacion='ACTIVA'");
        h.append("<div class=\"kpis\">")
         .append(kpi("Clientes activos", clientes))
         .append(kpi("Membresías activas", membresias))
         .append(kpi("Pagos del mes", dinero(pagosMes)))
         .append(kpi("Rutinas asignadas", rutinas))
         .append("</div></section>\n");
    }

    private static void seccionMembresias(StringBuilder h, Connection c)
            throws SQLException {
        h.append("<section><h2>2. Membresías</h2>\n");
        h.append("<h3>Por plan (activas)</h3>\n");
        tablaSql(h, c,
                "SELECT tm.nombre AS \"Plan\", "
                + "COUNT(*)::int AS \"Activas\", "
                + "COALESCE(SUM(m.costo_final),0) AS \"Facturación\" "
                + "FROM membresia m JOIN tipo_membresia tm "
                + "  ON tm.id_tipo_membresia = m.id_tipo_membresia "
                + "WHERE m.estado_membresia='ACTIVA' "
                + "GROUP BY tm.nombre ORDER BY 2 DESC");

        h.append("<h3>Por estado</h3>\n");
        tablaSql(h, c,
                "SELECT estado_membresia AS \"Estado\", "
                + "COUNT(*)::int AS \"Cantidad\", "
                + "COALESCE(SUM(costo_final),0) AS \"Facturación\" "
                + "FROM membresia GROUP BY estado_membresia ORDER BY 2 DESC");

        h.append("<h3>Próximas a vencer (30 días)</h3>\n");
        tablaSql(h, c,
                "SELECT m.numero_membresia AS \"Membresía\", "
                + "TRIM(p.nombres || ' ' || p.apellidos) AS \"Cliente\", "
                + "tm.nombre AS \"Plan\", m.fecha_fin AS \"Vence\", "
                + "(m.fecha_fin - CURRENT_DATE)::int AS \"Días\" "
                + "FROM membresia m "
                + "JOIN persona p ON p.id_persona = m.id_cliente "
                + "JOIN tipo_membresia tm "
                + "  ON tm.id_tipo_membresia = m.id_tipo_membresia "
                + "WHERE m.estado_membresia='ACTIVA' "
                + "AND m.fecha_fin BETWEEN CURRENT_DATE "
                + "  AND CURRENT_DATE + INTERVAL '30 day' "
                + "ORDER BY m.fecha_fin");

        h.append("<h3>Congelaciones activas</h3>\n");
        tablaSql(h, c,
                "SELECT m.numero_membresia AS \"Membresía\", "
                + "TRIM(p.nombres || ' ' || p.apellidos) AS \"Cliente\", "
                + "cg.motivo AS \"Motivo\", "
                + "cg.fecha_inicio AS \"Inicio\", "
                + "cg.fecha_fin AS \"Fin\" "
                + "FROM congelacion cg "
                + "JOIN membresia m ON m.id_membresia = cg.id_membresia "
                + "JOIN persona p ON p.id_persona = m.id_cliente "
                + "WHERE cg.estado_congelacion='ACTIVA' "
                + "ORDER BY cg.fecha_inicio");
        h.append("</section>\n");
    }

    private static void seccionFinanciero(StringBuilder h, Connection c)
            throws SQLException {
        h.append("<section><h2>3. Financiero</h2>\n");
        h.append("<h3>Ingresos día a día del mes actual</h3>\n");
        tablaSql(h, c,
                "SELECT (fecha_hora_pago::date) AS \"Fecha\", "
                + "SUM(monto_pago) AS \"Ingresos\", "
                + "COUNT(*)::int AS \"Pagos\" "
                + "FROM pago "
                + "WHERE DATE_TRUNC('month', fecha_hora_pago) = "
                + "  DATE_TRUNC('month', CURRENT_DATE) "
                + "AND estado_pago='CONFIRMADO' "
                + "GROUP BY 1 ORDER BY 1");

        h.append("<h3>Distribución por método de pago</h3>\n");
        tablaSql(h, c,
                "SELECT mp.nombre_metodo AS \"Método\", "
                + "COUNT(*)::int AS \"Pagos\", "
                + "SUM(pg.monto_pago) AS \"Monto\" "
                + "FROM pago pg "
                + "JOIN metodo_pago mp "
                + "  ON mp.id_metodo_pago = pg.id_metodo_pago "
                + "WHERE pg.estado_pago='CONFIRMADO' "
                + "GROUP BY mp.nombre_metodo ORDER BY 3 DESC");

        h.append("<h3>Facturas pendientes de cobro</h3>\n");
        tablaSql(h, c,
                "SELECT f.numero_factura AS \"Nº\", "
                + "TRIM(p.nombres || ' ' || p.apellidos) AS \"Cliente\", "
                + "f.fecha_emision AS \"Emisión\", "
                + "f.estado_factura AS \"Estado\", "
                + "(CURRENT_DATE - f.fecha_emision)::int AS \"Días\" "
                + "FROM factura f "
                + "JOIN persona p ON p.id_persona = f.id_cliente "
                + "WHERE f.estado_factura IN ('EMITIDA','PENDIENTE') "
                + "ORDER BY f.fecha_emision");
        h.append("</section>\n");
    }

    private static void seccionClientes(StringBuilder h, Connection c)
            throws SQLException {
        h.append("<section><h2>4. Panorama de clientes</h2>\n");
        h.append("<h3>Distribución por sexo (activos)</h3>\n");
        tablaSql(h, c,
                "SELECT p.sexo AS \"Sexo\", COUNT(*)::int AS \"Clientes\" "
                + "FROM cliente cli "
                + "JOIN persona p ON p.id_persona = cli.id_persona "
                + "WHERE cli.estado_cliente "
                + "GROUP BY p.sexo ORDER BY 2 DESC");

        h.append("<h3>Top 10 asistencias del mes</h3>\n");
        tablaSql(h, c,
                "SELECT cli.codigo_cliente AS \"Código\", "
                + "TRIM(p.nombres || ' ' || p.apellidos) AS \"Cliente\", "
                + "COUNT(*)::int AS \"Asistencias\" "
                + "FROM asistencia a "
                + "JOIN cliente cli ON cli.id_persona = a.id_cliente "
                + "JOIN persona p ON p.id_persona = a.id_cliente "
                + "WHERE a.fecha_asistencia >= "
                + "  DATE_TRUNC('month', CURRENT_DATE) "
                + "GROUP BY cli.codigo_cliente, p.nombres, p.apellidos "
                + "ORDER BY 3 DESC LIMIT 10");

        h.append("<h3>Bajas recientes (últimos 30 registros)</h3>\n");
        tablaSql(h, c,
                "SELECT cli.codigo_cliente AS \"Código\", "
                + "TRIM(p.nombres || ' ' || p.apellidos) AS \"Cliente\", "
                + "p.cedula AS \"Cédula\", "
                + "cli.fecha_registro AS \"Registro\", "
                + "cli.observaciones AS \"Observaciones\" "
                + "FROM cliente cli "
                + "JOIN persona p ON p.id_persona = cli.id_persona "
                + "WHERE cli.estado_cliente = FALSE "
                + "ORDER BY cli.fecha_registro DESC LIMIT 30");
        h.append("</section>\n");
    }

    private static void seccionOcupacion(StringBuilder h, Connection c)
            throws SQLException {
        h.append("<section><h2>5. Ocupación y clases</h2>\n");
        h.append("<h3>Asistencias por día de la semana (últimos 30 días)</h3>\n");
        tablaSql(h, c,
                "SELECT CASE EXTRACT(ISODOW FROM fecha_asistencia)::int "
                + "  WHEN 1 THEN 'Lunes' WHEN 2 THEN 'Martes' "
                + "  WHEN 3 THEN 'Miércoles' WHEN 4 THEN 'Jueves' "
                + "  WHEN 5 THEN 'Viernes' WHEN 6 THEN 'Sábado' "
                + "  WHEN 7 THEN 'Domingo' END AS \"Día\", "
                + "COUNT(*)::int AS \"Asistencias\" "
                + "FROM asistencia "
                + "WHERE fecha_asistencia >= CURRENT_DATE - INTERVAL '30 day' "
                + "GROUP BY EXTRACT(ISODOW FROM fecha_asistencia) "
                + "ORDER BY EXTRACT(ISODOW FROM fecha_asistencia)");

        h.append("<h3>Horarios más concurridos</h3>\n");
        tablaSql(h, c,
                "SELECT (EXTRACT(HOUR FROM hora_entrada)::int || ':00') AS \"Hora\", "
                + "COUNT(*)::int AS \"Ingresos\" "
                + "FROM asistencia "
                + "WHERE fecha_asistencia >= CURRENT_DATE - INTERVAL '30 day' "
                + "GROUP BY 1 ORDER BY 2 DESC LIMIT 12");

        h.append("<h3>Uso de cupo por clase grupal</h3>\n");
        tablaSql(h, c,
                "SELECT cg.nombre_clase AS \"Clase\", "
                + "cg.fecha_hora AS \"Fecha\", "
                + "cg.cupo_maximo AS \"Cupo\", "
                + "(SELECT COUNT(*)::int FROM reserva r "
                + "  WHERE r.id_clase = cg.id_clase "
                + "  AND r.estado_reserva='ACTIVA') AS \"Inscritos\" "
                + "FROM clase_grupal cg "
                + "WHERE cg.estado_clase = TRUE "
                + "ORDER BY cg.fecha_hora DESC LIMIT 40");
        h.append("</section>\n");
    }

    private static void seccionDesempeno(StringBuilder h, Connection c)
            throws SQLException {
        h.append("<section><h2>6. Desempeño profesional</h2>\n");
        h.append("<h3>Entrenadores</h3>\n");
        tablaSql(h, c,
                "SELECT TRIM(p.nombres || ' ' || p.apellidos) AS \"Entrenador\", "
                + "(SELECT COUNT(*)::int FROM rutina r "
                + "  WHERE r.id_entrenador = e.id_persona) AS \"Rutinas\", "
                + "(SELECT COUNT(DISTINCT ar.id_cliente)::int "
                + "  FROM asignacion_rutina ar JOIN rutina r "
                + "    ON r.id_rutina = ar.id_rutina "
                + "  WHERE r.id_entrenador = e.id_persona) AS \"Clientes\", "
                + "(SELECT COUNT(*)::int FROM evaluacion_fisica ef "
                + "  WHERE ef.id_entrenador = e.id_persona) AS \"Evaluaciones\", "
                + "(SELECT COUNT(*)::int FROM clase_grupal cg "
                + "  WHERE cg.id_entrenador = e.id_persona) AS \"Clases\" "
                + "FROM entrenador e "
                + "JOIN persona p ON p.id_persona = e.id_persona "
                + "WHERE e.estado_entrenador ORDER BY 1");

        h.append("<h3>Nutricionistas</h3>\n");
        tablaSql(h, c,
                "SELECT TRIM(p.nombres || ' ' || p.apellidos) AS \"Nutricionista\", "
                + "(SELECT COUNT(*)::int FROM plan_nutricional pn "
                + "  WHERE pn.id_nutricionista = n.id_persona "
                + "  AND pn.estado_plan='ACTIVO') AS \"Planes activos\", "
                + "(SELECT COUNT(DISTINCT pn.id_cliente)::int "
                + "  FROM plan_nutricional pn "
                + "  WHERE pn.id_nutricionista = n.id_persona) AS \"Clientes con plan\" "
                + "FROM nutricionista n "
                + "JOIN persona p ON p.id_persona = n.id_persona "
                + "ORDER BY 1");
        h.append("</section>\n");
    }

    private static void seccionBitacora(StringBuilder h, Connection c)
            throws SQLException {
        h.append("<section><h2>7. Bitácora (últimos 7 días)</h2>\n");
        tablaSql(h, c,
                "SELECT b.fecha_hora AS \"Fecha/hora\", "
                + "us.nombre_usuario AS \"Usuario\", "
                + "b.modulo AS \"Módulo\", "
                + "b.accion_realizada AS \"Acción\", "
                + "b.resultado AS \"Resultado\" "
                + "FROM bitacora b "
                + "JOIN usuario us ON us.id_usuario = b.id_usuario "
                + "WHERE b.fecha_hora >= CURRENT_DATE - INTERVAL '7 day' "
                + "ORDER BY b.fecha_hora DESC LIMIT 200");
        h.append("</section>\n");
    }

    // ---- Helpers ---------------------------------------------------------

    private static int queryInt(Connection c, String sql) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            r.next();
            return r.getInt(1);
        }
    }

    private static BigDecimal queryDec(Connection c, String sql)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            r.next();
            BigDecimal v = r.getBigDecimal(1);
            return v == null ? BigDecimal.ZERO : v;
        }
    }

    private static void tablaSql(StringBuilder h, Connection c, String sql)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            ResultSetMetaData md = r.getMetaData();
            int cols = md.getColumnCount();
            h.append("<table>\n<thead><tr>");
            for (int i = 1; i <= cols; i++) {
                h.append("<th>").append(escapar(md.getColumnLabel(i)))
                 .append("</th>");
            }
            h.append("</tr></thead>\n<tbody>");
            int filas = 0;
            while (r.next()) {
                h.append("<tr>");
                for (int i = 1; i <= cols; i++) {
                    Object v = r.getObject(i);
                    String txt;
                    if (v == null) {
                        txt = "";
                    } else if (v instanceof BigDecimal bd) {
                        txt = dinero(bd);
                    } else {
                        txt = v.toString();
                    }
                    h.append("<td>").append(escapar(txt)).append("</td>");
                }
                h.append("</tr>\n");
                filas++;
            }
            if (filas == 0) {
                h.append("<tr><td colspan=\"").append(cols)
                 .append("\" class=\"vacio\">Sin datos</td></tr>");
            }
            h.append("</tbody>\n</table>\n");
        }
    }

    private static String kpi(String titulo, Object valor) {
        return "<div class=\"kpi\"><span class=\"t\">" + escapar(titulo)
                + "</span><span class=\"v\">" + escapar(String.valueOf(valor))
                + "</span></div>";
    }

    private static String dinero(BigDecimal v) {
        return String.format(Locale.US, "$ %,.2f",
                v == null ? BigDecimal.ZERO : v);
    }

    private static String escapar(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String css() {
        return String.join("\n", List.of(
                "*{box-sizing:border-box;font-family:Segoe UI,Arial,sans-serif;}",
                "body{margin:0;background:#eaf2fb;color:#172a43;}",
                "header{background:#0a3a6c;color:#fff;padding:24px 32px;}",
                "header h1{margin:0;font-size:28px;letter-spacing:1px;}",
                "header .subtitulo{margin:6px 0 0;opacity:.85;}",
                "header .generado{margin:2px 0 0;opacity:.65;font-size:12px;}",
                "section{background:#fff;margin:16px 24px;padding:20px 24px;",
                "  border-radius:8px;border:1px solid #d4e1ef;",
                "  box-shadow:0 1px 2px rgba(0,0,0,.03);}",
                "section h2{color:#0a3a6c;border-bottom:2px solid #087cff;",
                "  padding-bottom:6px;margin:0 0 12px;}",
                "section h3{color:#172a43;margin:16px 0 6px;font-size:14px;}",
                ".kpis{display:grid;grid-template-columns:repeat(4,1fr);",
                "  gap:12px;margin-top:8px;}",
                ".kpi{background:#f0f6ff;border:1px solid #d4e1ef;",
                "  border-radius:6px;padding:12px 16px;display:flex;",
                "  flex-direction:column;}",
                ".kpi .t{color:#5a6e87;font-size:11px;text-transform:uppercase;",
                "  letter-spacing:.5px;}",
                ".kpi .v{color:#087cff;font-size:22px;font-weight:700;",
                "  margin-top:4px;}",
                "table{width:100%;border-collapse:collapse;font-size:12px;",
                "  margin-top:6px;}",
                "thead{background:#0a3a6c;color:#fff;}",
                "th{padding:8px 10px;text-align:center;font-weight:600;}",
                "td{padding:6px 10px;border-bottom:1px solid #eef2f7;",
                "  text-align:center;}",
                "tbody tr:hover{background:#f4f9ff;}",
                ".vacio{color:#8a99b1;font-style:italic;}",
                "footer{color:#5a6e87;font-size:11px;padding:16px 24px 30px;",
                "  text-align:center;}",
                "@media print{header{background:#fff !important;color:#0a3a6c;}",
                "  thead{background:#eef2f7 !important;color:#0a3a6c;}}"
        ));
    }
}
