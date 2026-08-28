package dao;

import conexion.ConexionPostgreSQL;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO de consultas para dashboard y reportes.
 *
 * @author Usuario
 */
public class DashboardDAO {

    public Integer contarClientesActivos() throws SQLException {

        String sql = "SELECT COUNT(*) FROM cliente WHERE estado_cliente = TRUE";

        return consultarEntero(
                sql
        );
    }

    public Integer contarMembresiasActivas() throws SQLException {

        String sql = "SELECT COUNT(*) FROM membresia WHERE estado_membresia = 'ACTIVA'";

        return consultarEntero(
                sql
        );
    }

    public BigDecimal obtenerPagosDelMes() throws SQLException {

        String sql = "SELECT COALESCE(SUM(monto_pago), 0) "
                + "FROM pago "
                + "WHERE DATE_TRUNC('month', fecha_hora_pago) = DATE_TRUNC('month', CURRENT_DATE) "
                + "AND estado_pago = 'CONFIRMADO'";

        return consultarDecimal(
                sql
        );
    }

    public Integer contarRutinasAsignadas() throws SQLException {

        String sql = "SELECT COUNT(*) FROM asignacion_rutina WHERE estado_asignacion = 'ACTIVA'";

        return consultarEntero(
                sql
        );
    }

    public List<Map<String, Object>> listarUltimosClientesRegistrados(
            Integer limite
    ) throws SQLException {

        if (limite == null
                || limite <= 0) {
            limite = 10;
        }

        String sql = "SELECT c.codigo_cliente, p.cedula, p.nombres, p.apellidos, c.fecha_registro "
                + "FROM cliente c "
                + "INNER JOIN persona p ON p.id_persona = c.id_persona "
                + "ORDER BY c.fecha_registro DESC, c.id_persona DESC "
                + "LIMIT ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    limite
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                return convertirLista(
                        resultado
                );
            }
        }
    }

    public List<Map<String, Object>> listarMembresiasProximasAVencer(
            Integer dias
    ) throws SQLException {

        if (dias == null
                || dias <= 0) {
            dias = 15;
        }

        String sql = "SELECT m.numero_membresia, p.nombres, p.apellidos, m.fecha_fin, m.estado_membresia "
                + "FROM membresia m "
                + "INNER JOIN cliente c ON c.id_persona = m.id_cliente "
                + "INNER JOIN persona p ON p.id_persona = c.id_persona "
                + "WHERE m.fecha_fin BETWEEN CURRENT_DATE AND CURRENT_DATE + (? * INTERVAL '1 day') "
                + "ORDER BY m.fecha_fin ASC";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    dias
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                return convertirLista(
                        resultado
                );
            }
        }
    }

    public List<Map<String, Object>> listarIngresosPendientes() throws SQLException {

        String sql = "SELECT f.numero_factura, p.nombres, p.apellidos, f.fecha_emision, f.estado_factura "
                + "FROM factura f "
                + "INNER JOIN cliente c ON c.id_persona = f.id_cliente "
                + "INNER JOIN persona p ON p.id_persona = c.id_persona "
                + "WHERE f.estado_factura IN ('PENDIENTE', 'EMITIDA') "
                + "ORDER BY f.fecha_emision ASC";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql);
            ResultSet resultado = sentencia.executeQuery()
        ) {

            return convertirLista(
                    resultado
            );
        }
    }

    public List<Map<String, Object>> obtenerActividadSemanal() throws SQLException {

        String sql = "SELECT fecha_asistencia, COUNT(*) AS total_asistencias "
                + "FROM asistencia "
                + "WHERE fecha_asistencia >= CURRENT_DATE - INTERVAL '7 day' "
                + "GROUP BY fecha_asistencia "
                + "ORDER BY fecha_asistencia";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql);
            ResultSet resultado = sentencia.executeQuery()
        ) {

            return convertirLista(
                    resultado
            );
        }
    }

    public List<Map<String, Object>> obtenerActividadSemanalPorRol(
            String nombreRol,
            Long idPersona
    ) throws SQLException {

        String rol = nombreRol == null
                ? "" : nombreRol.trim().toUpperCase();
        String tablaConteo;
        boolean requierePersona = false;

        switch (rol) {
            case "CLIENTE" -> {
                tablaConteo = "SELECT fecha_registro AS fecha, COUNT(*) AS total "
                        + "FROM progreso_rutina WHERE id_cliente = ? "
                        + "GROUP BY fecha_registro";
                requierePersona = true;
            }
            case "ENTRENADOR" -> {
                tablaConteo = "SELECT pr.fecha_registro AS fecha, COUNT(*) AS total "
                        + "FROM progreso_rutina pr "
                        + "JOIN rutina r ON r.id_rutina = pr.id_rutina "
                        + "WHERE r.id_entrenador = ? GROUP BY pr.fecha_registro";
                requierePersona = true;
            }
            case "NUTRICIONISTA" -> {
                tablaConteo = "SELECT fecha_creacion AS fecha, COUNT(*) AS total "
                        + "FROM plan_nutricional WHERE id_nutricionista = ? "
                        + "GROUP BY fecha_creacion";
                requierePersona = true;
            }
            default -> tablaConteo
                    = "SELECT fecha_asistencia AS fecha, COUNT(*) AS total "
                    + "FROM asistencia GROUP BY fecha_asistencia";
        }

        String sql = "WITH dias AS ("
                + " SELECT generate_series(CURRENT_DATE - 6, CURRENT_DATE, "
                + " INTERVAL '1 day')::date AS fecha), conteo AS ("
                + tablaConteo + ") "
                + "SELECT d.fecha, COALESCE(c.total, 0)::int AS total "
                + "FROM dias d LEFT JOIN conteo c ON c.fecha = d.fecha "
                + "ORDER BY d.fecha";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (requierePersona) {
                sentencia.setObject(1, idPersona);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                return convertirLista(resultado);
            }
        }
    }

    public Map<String, Object> obtenerResumenGeneral() throws SQLException {

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("clientesActivos", contarClientesActivos());
        resumen.put("membresiasActivas", contarMembresiasActivas());
        resumen.put("pagosDelMes", obtenerPagosDelMes());
        resumen.put("rutinasAsignadas", contarRutinasAsignadas());

        return resumen;
    }

    /**
     * Obtiene los indicadores que corresponden al usuario autenticado. Las
     * consultas usan exclusivamente las tablas existentes del modelo GYMNOVA.
     */
    public Map<String, Object> obtenerResumenPorRol(
            String nombreRol,
            Long idPersona
    ) throws SQLException {

        Map<String, Object> resumen = new HashMap<>();
        String rol = nombreRol == null
                ? ""
                : nombreRol.trim().toUpperCase();

        try (Connection conexion = ConexionPostgreSQL.getConexion()) {

            switch (rol) {
                case "RECEPCIONISTA" -> {
                    resumen.put("principal1", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM cliente "
                            + "WHERE fecha_registro = CURRENT_DATE"
                    ));
                    resumen.put("principal2", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM reserva "
                            + "WHERE fecha_hora_reserva::date = CURRENT_DATE "
                            + "AND estado_reserva <> 'CANCELADA'"
                    ));
                    resumen.put("principal3", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM asistencia "
                            + "WHERE fecha_asistencia = CURRENT_DATE "
                            + "AND estado_acceso <> 'ANULADO'"
                    ));
                    resumen.put("principal4", consultarDecimal(
                            conexion,
                            "SELECT COALESCE(SUM(monto_pago), 0) FROM pago "
                            + "WHERE fecha_hora_pago::date = CURRENT_DATE "
                            + "AND estado_pago = 'CONFIRMADO'"
                    ));
                }
                case "ENTRENADOR" -> {
                    resumen.put("principal1", consultarEntero(
                            conexion,
                            "SELECT COUNT(DISTINCT ar.id_cliente) "
                            + "FROM asignacion_rutina ar "
                            + "JOIN rutina r ON r.id_rutina = ar.id_rutina "
                            + "WHERE r.id_entrenador = ? "
                            + "AND ar.estado_asignacion = 'ACTIVA'",
                            idPersona
                    ));
                    resumen.put("principal2", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM rutina "
                            + "WHERE id_entrenador = ? "
                            + "AND estado_rutina = 'ACTIVA'",
                            idPersona
                    ));
                    resumen.put("principal3", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM evaluacion_fisica "
                            + "WHERE id_entrenador = ? "
                            + "AND estado_evaluacion = 'REGISTRADA'",
                            idPersona
                    ));
                    resumen.put("principal4", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM asignacion_rutina ar "
                            + "JOIN rutina r ON r.id_rutina = ar.id_rutina "
                            + "WHERE r.id_entrenador = ? "
                            + "AND CURRENT_DATE BETWEEN ar.fecha_inicio "
                            + "AND COALESCE(ar.fecha_fin, CURRENT_DATE) "
                            + "AND ar.estado_asignacion = 'ACTIVA'",
                            idPersona
                    ));
                }
                case "NUTRICIONISTA" -> {
                    resumen.put("principal1", consultarEntero(
                            conexion,
                            "SELECT COUNT(DISTINCT id_cliente) "
                            + "FROM plan_nutricional "
                            + "WHERE id_nutricionista = ? "
                            + "AND estado_plan = 'ACTIVO'",
                            idPersona
                    ));
                    resumen.put("principal2", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM plan_nutricional "
                            + "WHERE id_nutricionista = ? "
                            + "AND estado_plan = 'ACTIVO'",
                            idPersona
                    ));
                    resumen.put("principal3", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM plan_nutricional "
                            + "WHERE id_nutricionista = ? "
                            + "AND estado_plan = 'ACTIVO' "
                            + "AND fecha_fin BETWEEN CURRENT_DATE "
                            + "AND CURRENT_DATE + INTERVAL '14 day'",
                            idPersona
                    ));
                    resumen.put("principal4", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM recomendacion re "
                            + "JOIN evaluacion_fisica ev "
                            + "ON ev.id_evaluacion = re.id_evaluacion "
                            + "JOIN plan_nutricional pn "
                            + "ON pn.id_cliente = ev.id_cliente "
                            + "WHERE pn.id_nutricionista = ? "
                            + "AND re.estado_recomendacion = 'ACTIVA'",
                            idPersona
                    ));
                }
                case "CLIENTE" -> {
                    resumen.put("principal1", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM membresia "
                            + "WHERE id_cliente = ? "
                            + "AND estado_membresia = 'ACTIVA'",
                            idPersona
                    ));
                    resumen.put("principal2", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM asignacion_rutina "
                            + "WHERE id_cliente = ? "
                            + "AND estado_asignacion = 'ACTIVA'",
                            idPersona
                    ));
                    resumen.put("principal3", consultarDecimal(
                            conexion,
                            "SELECT COALESCE((SELECT peso_corporal "
                            + "FROM progreso_rutina WHERE id_cliente = ? "
                            + "ORDER BY fecha_registro DESC, id_progreso DESC "
                            + "LIMIT 1), 0)",
                            idPersona
                    ));
                    resumen.put("principal4", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM plan_nutricional "
                            + "WHERE id_cliente = ? AND estado_plan = 'ACTIVO'",
                            idPersona
                    ));
                    resumen.put("reservas", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM reserva "
                            + "WHERE id_cliente = ? AND estado_reserva = 'ACTIVA'",
                            idPersona
                    ));
                    resumen.put("asistencias", consultarEntero(
                            conexion,
                            "SELECT COUNT(*) FROM asistencia "
                            + "WHERE id_cliente = ? "
                            + "AND DATE_TRUNC('month', fecha_asistencia) "
                            + "= DATE_TRUNC('month', CURRENT_DATE) "
                            + "AND estado_acceso = 'COMPLETADO'",
                            idPersona
                    ));
                }
                default -> {
                    resumen.put("principal1", contarClientesActivos());
                    resumen.put("principal2", contarMembresiasActivas());
                    resumen.put("principal3", obtenerPagosDelMes());
                    resumen.put("principal4", contarRutinasAsignadas());
                }
            }
        }

        return resumen;
    }

    public List<Map<String, Object>> listarActividadPorRol(
            String nombreRol,
            Long idPersona,
            Integer limite
    ) throws SQLException {

        int maximo = limite == null || limite <= 0 ? 6 : limite;
        String rol = nombreRol == null
                ? ""
                : nombreRol.trim().toUpperCase();

        String sql;
        boolean usaPersona = false;

        switch (rol) {
            case "ENTRENADOR" -> {
                sql = "SELECT p.nombres || ' ' || p.apellidos AS nombre, "
                        + "r.nombre_rutina AS detalle, ar.estado_asignacion AS estado, "
                        + "ar.fecha_inicio AS fecha, c.codigo_cliente AS referencia "
                        + "FROM asignacion_rutina ar "
                        + "JOIN rutina r ON r.id_rutina = ar.id_rutina "
                        + "JOIN cliente c ON c.id_persona = ar.id_cliente "
                        + "JOIN persona p ON p.id_persona = c.id_persona "
                        + "WHERE r.id_entrenador = ? "
                        + "ORDER BY ar.fecha_inicio DESC LIMIT ?";
                usaPersona = true;
            }
            case "NUTRICIONISTA" -> {
                sql = "SELECT p.nombres || ' ' || p.apellidos AS nombre, "
                        + "pn.nombre_plan AS detalle, pn.estado_plan AS estado, "
                        + "pn.fecha_inicio AS fecha, c.codigo_cliente AS referencia "
                        + "FROM plan_nutricional pn "
                        + "JOIN cliente c ON c.id_persona = pn.id_cliente "
                        + "JOIN persona p ON p.id_persona = c.id_persona "
                        + "WHERE pn.id_nutricionista = ? "
                        + "ORDER BY pn.fecha_creacion DESC LIMIT ?";
                usaPersona = true;
            }
            case "CLIENTE" -> {
                sql = "SELECT r.nombre_rutina AS nombre, "
                        + "COALESCE(r.descripcion, 'Rutina asignada') AS detalle, "
                        + "ar.estado_asignacion AS estado, ar.fecha_inicio AS fecha, "
                        + "COALESCE(r.nivel, '-') AS referencia "
                        + "FROM asignacion_rutina ar "
                        + "JOIN rutina r ON r.id_rutina = ar.id_rutina "
                        + "WHERE ar.id_cliente = ? "
                        + "ORDER BY ar.fecha_inicio DESC LIMIT ?";
                usaPersona = true;
            }
            case "RECEPCIONISTA" -> sql
                    = "SELECT p.nombres || ' ' || p.apellidos AS nombre, "
                    + "p.telefono AS detalle, "
                    + "CASE WHEN c.estado_cliente THEN 'NUEVO' ELSE 'INACTIVO' END AS estado, "
                    + "c.fecha_registro AS fecha, c.codigo_cliente AS referencia "
                    + "FROM cliente c JOIN persona p ON p.id_persona=c.id_persona "
                    + "ORDER BY c.fecha_registro DESC, c.id_persona DESC LIMIT ?";
            default -> sql
                    = "SELECT p.nombres || ' ' || p.apellidos AS nombre, "
                    + "p.cedula AS detalle, "
                    + "CASE WHEN c.estado_cliente THEN 'ACTIVO' ELSE 'INACTIVO' END AS estado, "
                    + "c.fecha_registro AS fecha, c.codigo_cliente AS referencia "
                    + "FROM cliente c JOIN persona p ON p.id_persona = c.id_persona "
                    + "ORDER BY c.fecha_registro DESC, c.id_persona DESC LIMIT ?";
        }

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            int posicion = 1;
            if (usaPersona) {
                if (idPersona == null) {
                    return new ArrayList<>();
                }
                sentencia.setLong(posicion++, idPersona);
            }
            sentencia.setInt(posicion, maximo);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return convertirLista(resultado);
            }
        }
    }

    private Integer consultarEntero(
            String sql
    ) throws SQLException {

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql);
            ResultSet resultado = sentencia.executeQuery()
        ) {

            if (resultado.next()) {
                return resultado.getInt(1);
            }
        }

        return 0;
    }

    private BigDecimal consultarDecimal(
            String sql
    ) throws SQLException {

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql);
            ResultSet resultado = sentencia.executeQuery()
        ) {

            if (resultado.next()) {
                return resultado.getBigDecimal(1);
            }
        }

        return BigDecimal.ZERO;
    }

    private Integer consultarEntero(
            Connection conexion,
            String sql,
            Object... parametros
    ) throws SQLException {

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignarParametros(sentencia, parametros);
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? resultado.getInt(1) : 0;
            }
        }
    }

    private BigDecimal consultarDecimal(
            Connection conexion,
            String sql,
            Object... parametros
    ) throws SQLException {

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            asignarParametros(sentencia, parametros);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    BigDecimal valor = resultado.getBigDecimal(1);
                    return valor == null ? BigDecimal.ZERO : valor;
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private void asignarParametros(
            PreparedStatement sentencia,
            Object... parametros
    ) throws SQLException {

        for (int i = 0; i < parametros.length; i++) {
            sentencia.setObject(i + 1, parametros[i]);
        }
    }

    private List<Map<String, Object>> convertirLista(
            ResultSet resultado
    ) throws SQLException {

        List<Map<String, Object>> lista = new ArrayList<>();
        int columnas = resultado.getMetaData().getColumnCount();

        while (resultado.next()) {

            Map<String, Object> fila = new HashMap<>();

            for (int i = 1; i <= columnas; i++) {
                fila.put(
                        resultado.getMetaData().getColumnLabel(i),
                        resultado.getObject(i)
                );
            }

            lista.add(
                    fila
            );
        }

        return lista;
    }
}
