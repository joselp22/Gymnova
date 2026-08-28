package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Persistencia del cumplimiento diario por ejercicio sin crear una tabla
 * adicional. El detalle de ejercicios realizados se almacena dentro de
 * progreso_rutina.detalle_ejercicios (JSONB), manteniendo un único resumen
 * diario por cliente/rutina siempre que sea posible.
 */
public class ProgresoEjercicioDAO {

    private static final String[] DIAS_SEMANA_ES = {
        "LUNES", "MARTES", "MIERCOLES", "JUEVES",
        "VIERNES", "SABADO", "DOMINGO"
    };

    public boolean marcarHecho(long idRutinaEjercicio, long idCliente,
            LocalDate fecha) throws SQLException {
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                DatosEjercicio datos = validarEjercicioDelDia(
                        c, idRutinaEjercicio, idCliente, fecha);
                if (datos == null) {
                    c.rollback();
                    return false;
                }

                bloquearProgreso(c, idCliente, datos.idRutina, fecha);
                long idProgreso = obtenerOCrearProgreso(
                        c, idCliente, datos.idRutina, fecha,
                        datos.totalPlanificados);

                String sql = "UPDATE progreso_rutina "
                        + "SET detalle_ejercicios = "
                        + "    COALESCE(detalle_ejercicios, '{}'::jsonb) "
                        + "    || jsonb_build_object(?, "
                        + "       to_char(CURRENT_TIMESTAMP, 'YYYY-MM-DD\"T\"HH24:MI:SS')), "
                        + "    sesiones_planificadas = ?, "
                        + "    fecha_hora_actualizacion = CURRENT_TIMESTAMP "
                        + "WHERE id_progreso = ?";
                try (PreparedStatement s = c.prepareStatement(sql)) {
                    s.setString(1, String.valueOf(idRutinaEjercicio));
                    s.setInt(2, datos.totalPlanificados);
                    s.setLong(3, idProgreso);
                    s.executeUpdate();
                }
                sincronizarCantidadCompletada(c, idProgreso);
                c.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    public boolean desmarcarHecho(long idRutinaEjercicio, long idCliente,
            LocalDate fecha) throws SQLException {
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            boolean autoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                DatosEjercicio datos = validarEjercicioDelDia(
                        c, idRutinaEjercicio, idCliente, fecha);
                if (datos == null) {
                    c.rollback();
                    return false;
                }

                bloquearProgreso(c, idCliente, datos.idRutina, fecha);
                Long idProgreso = buscarProgresoDelDia(
                        c, idCliente, datos.idRutina, fecha);
                if (idProgreso == null) {
                    c.rollback();
                    return false;
                }

                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE progreso_rutina "
                        + "SET detalle_ejercicios = "
                        + "    COALESCE(detalle_ejercicios, '{}'::jsonb) - ?, "
                        + "    sesiones_planificadas = ?, "
                        + "    fecha_hora_actualizacion = CURRENT_TIMESTAMP "
                        + "WHERE id_progreso = ?")) {
                    s.setString(1, String.valueOf(idRutinaEjercicio));
                    s.setInt(2, datos.totalPlanificados);
                    s.setLong(3, idProgreso);
                    s.executeUpdate();
                }
                sincronizarCantidadCompletada(c, idProgreso);
                c.commit();
                return true;
            } catch (SQLException | RuntimeException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(autoCommit);
            }
        }
    }

    public Set<Long> listarHechosEnFecha(long idCliente, long idRutina,
            LocalDate fecha) throws SQLException {
        String sql = "SELECT jsonb_object_keys(COALESCE(detalle_ejercicios, '{}'::jsonb)) "
                + "FROM progreso_rutina "
                + "WHERE id_cliente = ? AND id_rutina = ? "
                + "AND fecha_registro = ? "
                + "AND estado_progreso <> 'ANULADO'";
        Set<Long> resultado = new HashSet<>();
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idCliente);
            s.setLong(2, idRutina);
            s.setDate(3, Date.valueOf(fecha));
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    String valor = r.getString(1);
                    try {
                        resultado.add(Long.parseLong(valor));
                    } catch (NumberFormatException ignorado) {
                        // El JSON pertenece a datos de aplicación; una clave
                        // no numérica no debe impedir cargar el resto.
                    }
                }
            }
        }
        return resultado;
    }

    public int contarHechosEnRango(long idCliente, LocalDate desde,
            LocalDate hasta) throws SQLException {
        /*
         * PostgreSQL 17 no tiene jsonb_object_length (existe desde PG 18).
         * Se reemplaza por una subconsulta escalar sobre jsonb_object_keys,
         * que ya es la función usada en listarHechosEnFecha y garantiza
         * compatibilidad con la versión que corre la base.
         */
        String sql = "SELECT COALESCE(SUM(( "
                + "  SELECT COUNT(*)::int FROM jsonb_object_keys( "
                + "    COALESCE(detalle_ejercicios, '{}'::jsonb)) "
                + ")), 0) "
                + "FROM progreso_rutina "
                + "WHERE id_cliente = ? "
                + "AND fecha_registro BETWEEN ? AND ? "
                + "AND estado_progreso <> 'ANULADO'";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idCliente);
            s.setDate(2, Date.valueOf(desde));
            s.setDate(3, Date.valueOf(hasta));
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? r.getInt(1) : 0;
            }
        }
    }

    private DatosEjercicio validarEjercicioDelDia(Connection c,
            long idRutinaEjercicio, long idCliente, LocalDate fecha)
            throws SQLException {
        String dia = diaSemana(fecha);
        String sql = "SELECT ce.id_rutina, "
                + "       (SELECT COUNT(*) FROM contiene_ejercicio ce2 "
                + "         WHERE ce2.id_rutina = ce.id_rutina "
                + "           AND ce2.dia_semana = ce.dia_semana) AS total "
                + "FROM contiene_ejercicio ce "
                + "JOIN asignacion_rutina ar ON ar.id_rutina = ce.id_rutina "
                + "WHERE ce.id_rutina_ejercicio = ? "
                + "AND ce.dia_semana = ? "
                + "AND ar.id_cliente = ? "
                + "AND ar.estado_asignacion = 'ACTIVA' "
                + "AND ar.fecha_inicio <= ? "
                + "AND (ar.fecha_fin IS NULL OR ar.fecha_fin >= ?)";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idRutinaEjercicio);
            s.setString(2, dia);
            s.setLong(3, idCliente);
            s.setDate(4, Date.valueOf(fecha));
            s.setDate(5, Date.valueOf(fecha));
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    return new DatosEjercicio(r.getLong(1), r.getInt(2));
                }
            }
        }
        return null;
    }

    private void bloquearProgreso(Connection c, long idCliente,
            long idRutina, LocalDate fecha) throws SQLException {
        String clave = "GYMNOVA-PROGRESO-" + idCliente + "-"
                + idRutina + "-" + fecha;
        try (PreparedStatement s = c.prepareStatement(
                "SELECT pg_advisory_xact_lock(hashtext(?))")) {
            s.setString(1, clave);
            s.executeQuery();
        }
    }

    private long obtenerOCrearProgreso(Connection c, long idCliente,
            long idRutina, LocalDate fecha, int planificados)
            throws SQLException {
        Long existente = buscarProgresoDelDia(c, idCliente, idRutina, fecha);
        if (existente != null) {
            return existente;
        }
        String sql = "INSERT INTO progreso_rutina "
                + "(fecha_registro, sesiones_planificadas, sesiones_completadas, "
                + "peso_corporal, nivel_esfuerzo, estado_progreso, id_cliente, "
                + "id_rutina, detalle_ejercicios, fecha_hora_actualizacion) "
                + "VALUES (?, ?, 0, NULL, NULL, 'REGISTRADO', ?, ?, "
                + "'{}'::jsonb, CURRENT_TIMESTAMP) RETURNING id_progreso";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setDate(1, Date.valueOf(fecha));
            s.setInt(2, planificados);
            s.setLong(3, idCliente);
            s.setLong(4, idRutina);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    return r.getLong(1);
                }
            }
        }
        throw new SQLException("No fue posible crear el progreso diario.");
    }

    private Long buscarProgresoDelDia(Connection c, long idCliente,
            long idRutina, LocalDate fecha) throws SQLException {
        String sql = "SELECT id_progreso FROM progreso_rutina "
                + "WHERE id_cliente = ? AND id_rutina = ? "
                + "AND fecha_registro = ? AND estado_progreso <> 'ANULADO' "
                + "ORDER BY id_progreso DESC LIMIT 1 FOR UPDATE";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idCliente);
            s.setLong(2, idRutina);
            s.setDate(3, Date.valueOf(fecha));
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? r.getLong(1) : null;
            }
        }
    }

    private void sincronizarCantidadCompletada(Connection c, long idProgreso)
            throws SQLException {
        // Igual que en contarHechosEnRango: PG 17 no expone
        // jsonb_object_length, así que contamos claves con
        // jsonb_object_keys en subconsulta escalar.
        try (PreparedStatement s = c.prepareStatement(
                "UPDATE progreso_rutina SET sesiones_completadas = "
                + "LEAST(sesiones_planificadas, ( "
                + "  SELECT COUNT(*)::int FROM jsonb_object_keys( "
                + "    COALESCE(detalle_ejercicios, '{}'::jsonb)) "
                + ")) "
                + "WHERE id_progreso = ?")) {
            s.setLong(1, idProgreso);
            s.executeUpdate();
        }
    }

    private static String diaSemana(LocalDate fecha) {
        return DIAS_SEMANA_ES[fecha.getDayOfWeek().getValue() - 1];
    }

    private static final class DatosEjercicio {
        private final long idRutina;
        private final int totalPlanificados;

        private DatosEjercicio(long idRutina, int totalPlanificados) {
            this.idRutina = idRutina;
            this.totalPlanificados = totalPlanificados;
        }
    }
}
