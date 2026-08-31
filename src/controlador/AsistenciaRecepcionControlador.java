package controlador;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import utilidades.Auditoria;

/** Lógica específica del registro manual de asistencia desde Recepción. */
public class AsistenciaRecepcionControlador {

    private String mensaje = "";

    public String getMensaje() { return mensaje; }

    public List<ClienteAsistencia> listarClientesActivos() {
        List<ClienteAsistencia> lista = new ArrayList<>();
        String sql = "SELECT c.id_persona, c.codigo_cliente, p.cedula, "
                + "TRIM(p.nombres || ' ' || p.apellidos) cliente "
                + "FROM cliente c JOIN persona p ON p.id_persona=c.id_persona "
                + "WHERE c.estado_cliente=TRUE ORDER BY p.apellidos,p.nombres";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            while (r.next()) {
                lista.add(new ClienteAsistencia(r.getLong("id_persona"),
                        r.getString("codigo_cliente"), r.getString("cedula"),
                        r.getString("cliente")));
            }
        } catch (SQLException ex) {
            mensaje = "No se pudieron cargar los clientes: " + ex.getMessage();
        }
        return lista;
    }

    public boolean registrarEntrada(Long idCliente, LocalDate fecha, LocalTime hora) {
        return registrarEntrada(idCliente, fecha, hora, "MANUAL");
    }

    public boolean registrarEntrada(Long idCliente, LocalDate fecha, LocalTime hora,
            String metodoRegistro) {
        mensaje = "";
        String metodo = normalizarMetodo(metodoRegistro);
        if (idCliente == null || fecha == null || hora == null) {
            mensaje = "Seleccione un cliente, una fecha y una hora de entrada.";
            return false;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            if (tieneEntradaAbierta(c, idCliente)) {
                mensaje = "El cliente ya tiene una entrada pendiente de salida.";
                return false;
            }
            String sql = "INSERT INTO asistencia (fecha_asistencia,hora_entrada,tipo_acceso,"
                    + "metodo_registro,estado_acceso,id_cliente) "
                    + "VALUES (?,?,'MEMBRESIA',?,'REGISTRADO',?)";
            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setDate(1, Date.valueOf(fecha));
                s.setTime(2, Time.valueOf(hora));
                s.setString(3, metodo);
                s.setLong(4, idCliente);
                boolean ok = s.executeUpdate() > 0;
                if (ok) {
                    mensaje = "Entrada registrada correctamente a las "
                            + hora.withSecond(0).withNano(0) + " (" + metodo + ").";
                    Auditoria.exito("ACCESO", "ENTRADA_" + metodo,
                            "Cliente " + idCliente + " " + fecha + " " + hora);
                }
                return ok;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo registrar la entrada: " + ex.getMessage();
            Auditoria.fallo("ACCESO", "ENTRADA_" + metodo, mensaje, ex.getMessage());
            return false;
        }
    }

    public boolean registrarSalida(Long idCliente, LocalDate fecha, LocalTime hora) {
        mensaje = "";
        if (idCliente == null || fecha == null || hora == null) {
            mensaje = "Seleccione un cliente, una fecha y una hora de salida.";
            return false;
        }
        String sqlBuscar = "SELECT id_asistencia,fecha_asistencia,hora_entrada FROM asistencia "
                + "WHERE id_cliente=? AND estado_acceso='REGISTRADO' AND hora_salida IS NULL "
                + "ORDER BY fecha_asistencia DESC,hora_entrada DESC LIMIT 1";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement b = c.prepareStatement(sqlBuscar)) {
            b.setLong(1, idCliente);
            Long id = null;
            LocalDate fechaEntrada = null;
            LocalTime horaEntrada = null;
            try (ResultSet r = b.executeQuery()) {
                if (r.next()) {
                    id = r.getLong("id_asistencia");
                    fechaEntrada = r.getDate("fecha_asistencia").toLocalDate();
                    horaEntrada = r.getTime("hora_entrada").toLocalTime();
                }
            }
            if (id == null) {
                mensaje = "El cliente no tiene una entrada pendiente de salida.";
                return false;
            }
            if (fecha.isBefore(fechaEntrada) || (fecha.equals(fechaEntrada) && hora.isBefore(horaEntrada))) {
                mensaje = "La hora de salida no puede ser anterior a la hora de entrada (" + horaEntrada + ").";
                return false;
            }
            try (PreparedStatement s = c.prepareStatement(
                    "UPDATE asistencia SET hora_salida=?, estado_acceso='COMPLETADO' WHERE id_asistencia=?")) {
                s.setTime(1, Time.valueOf(hora));
                s.setLong(2, id);
                boolean ok = s.executeUpdate() > 0;
                if (ok) {
                    mensaje = "Salida registrada correctamente a las " + hora.withSecond(0).withNano(0) + ".";
                    Auditoria.exito("ACCESO", "SALIDA_MANUAL", "Cliente " + idCliente + " asistencia " + id);
                }
                return ok;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo registrar la salida: " + ex.getMessage();
            Auditoria.fallo("ACCESO", "SALIDA_MANUAL", mensaje, ex.getMessage());
            return false;
        }
    }

    /**
     * Punto de integración para lectores QR, cédula o biométricos. El equipo o
     * su SDK debe entregar el código/cedula del cliente a este método. Si el
     * cliente está fuera registra entrada; si tiene una entrada abierta, salida.
     */
    public boolean registrarEventoAutomatico(String identificadorCliente, String metodoRegistro) {
        mensaje = "";
        String metodo = normalizarMetodoAutomatico(metodoRegistro);
        String identificador = identificadorCliente == null ? "" : identificadorCliente.trim();
        if (identificador.isBlank()) {
            mensaje = "El dispositivo no proporcionó un identificador de cliente.";
            return false;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            Long idCliente = buscarClienteActivo(c, identificador);
            if (idCliente == null) {
                mensaje = "No existe un cliente activo asociado al identificador recibido.";
                return false;
            }
            if (tieneEntradaAbierta(c, idCliente)) {
                return registrarSalida(idCliente, LocalDate.now(), LocalTime.now().withSecond(0).withNano(0));
            }
            return registrarEntrada(idCliente, LocalDate.now(),
                    LocalTime.now().withSecond(0).withNano(0), metodo);
        } catch (SQLException ex) {
            mensaje = "No se pudo procesar el evento del dispositivo: " + ex.getMessage();
            return false;
        }
    }

    private Long buscarClienteActivo(Connection c, String identificador) throws SQLException {
        String sql = "SELECT c.id_persona FROM cliente c "
                + "JOIN persona p ON p.id_persona=c.id_persona "
                + "WHERE c.estado_cliente=TRUE AND (p.cedula=? OR c.codigo_cliente=?) LIMIT 1";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, identificador);
            s.setString(2, identificador);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? r.getLong(1) : null;
            }
        }
    }

    private String normalizarMetodo(String metodo) {
        String valor = metodo == null ? "MANUAL" : metodo.trim().toUpperCase();
        if (!java.util.Set.of("MANUAL", "QR", "CEDULA", "BIOMETRICO").contains(valor)) {
            throw new IllegalArgumentException("Método de registro no permitido: " + valor);
        }
        return valor;
    }

    private String normalizarMetodoAutomatico(String metodo) {
        String valor = normalizarMetodo(metodo);
        if ("MANUAL".equals(valor)) {
            throw new IllegalArgumentException(
                    "La integración automática debe usar QR, CEDULA o BIOMETRICO.");
        }
        return valor;
    }

    private boolean tieneEntradaAbierta(Connection c, Long idCliente) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT 1 FROM asistencia WHERE id_cliente=? AND estado_acceso='REGISTRADO' "
                + "AND hora_salida IS NULL LIMIT 1")) {
            s.setLong(1, idCliente);
            try (ResultSet r = s.executeQuery()) { return r.next(); }
        }
    }

    public List<RegistroAsistencia> listarPorFecha(LocalDate fecha) {
        List<RegistroAsistencia> lista = new ArrayList<>();
        String sql = "SELECT a.id_asistencia,c.codigo_cliente,p.cedula,"
                + "TRIM(p.nombres||' '||p.apellidos) cliente,a.fecha_asistencia,"
                + "a.hora_entrada,a.hora_salida,a.estado_acceso "
                + "FROM asistencia a JOIN cliente c ON c.id_persona=a.id_cliente "
                + "JOIN persona p ON p.id_persona=c.id_persona WHERE a.fecha_asistencia=? "
                + "ORDER BY a.hora_entrada DESC";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setDate(1, Date.valueOf(fecha));
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    lista.add(new RegistroAsistencia(r.getLong("id_asistencia"),
                            r.getString("codigo_cliente"), r.getString("cedula"),
                            r.getString("cliente"), r.getDate("fecha_asistencia").toLocalDate(),
                            r.getTime("hora_entrada") == null ? null : r.getTime("hora_entrada").toLocalTime(),
                            r.getTime("hora_salida") == null ? null : r.getTime("hora_salida").toLocalTime(),
                            r.getString("estado_acceso")));
                }
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo cargar el historial: " + ex.getMessage();
        }
        return lista;
    }

    public record ClienteAsistencia(Long id, String codigo, String cedula, String nombre) {
        @Override public String toString() { return nombre + "  |  " + cedula + "  |  " + codigo; }
    }
    public record RegistroAsistencia(Long id, String codigo, String cedula, String cliente,
            LocalDate fecha, LocalTime entrada, LocalTime salida, String estado) { }
}
