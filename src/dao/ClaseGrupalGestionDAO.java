package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import modelo.ClaseGrupalResumen;
import modelo.ParticipanteClaseGrupal;

/** Consultas especializadas para el nuevo flujo de clases grupales. */
public class ClaseGrupalGestionDAO {

    private static final String SELECT_RESUMEN =
            "SELECT cg.id_clase, cg.nombre_clase, cg.descripcion, cg.nivel, "
            + "cg.duracion_base_minutos, cg.intensidad, cg.id_entrenador, "
            + "cg.cupo_maximo, cg.fecha_hora, cg.estado_clase, "
            + "TRIM(COALESCE(p.nombres,'') || ' ' || COALESCE(p.apellidos,'')) AS entrenador, "
            + "COUNT(r.id_reserva) FILTER (WHERE r.estado_reserva='ACTIVA')::int AS reservados "
            + "FROM clase_grupal cg "
            + "JOIN entrenador en ON en.id_persona=cg.id_entrenador "
            + "JOIN persona p ON p.id_persona=en.id_persona "
            + "LEFT JOIN reserva r ON r.id_clase=cg.id_clase ";

    private static final String GROUP_RESUMEN =
            " GROUP BY cg.id_clase, cg.nombre_clase, cg.descripcion, cg.nivel, "
            + "cg.duracion_base_minutos, cg.intensidad, cg.id_entrenador, "
            + "cg.cupo_maximo, cg.fecha_hora, cg.estado_clase, p.nombres, p.apellidos ";

    public List<ClaseGrupalResumen> listarAdministracion(String criterio) throws SQLException {
        List<ClaseGrupalResumen> lista = new ArrayList<>();
        String filtro = criterio == null ? "" : criterio.trim();
        String sql = SELECT_RESUMEN
                + "WHERE (?='' OR cg.nombre_clase ILIKE ? OR p.nombres ILIKE ? "
                + "OR p.apellidos ILIKE ? OR cg.nivel ILIKE ? OR cg.intensidad ILIKE ?) "
                + GROUP_RESUMEN
                + "ORDER BY cg.fecha_hora DESC, cg.id_clase DESC";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            String like = "%" + filtro + "%";
            s.setString(1, filtro);
            for (int i = 2; i <= 6; i++) s.setString(i, like);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) lista.add(convertirResumen(r));
            }
        }
        return lista;
    }

    public List<ClaseGrupalResumen> listarPorEntrenador(Long idEntrenador) throws SQLException {
        List<ClaseGrupalResumen> lista = new ArrayList<>();
        String sql = SELECT_RESUMEN
                + "WHERE cg.id_entrenador=? "
                + GROUP_RESUMEN
                + "ORDER BY CASE WHEN cg.fecha_hora >= CURRENT_TIMESTAMP THEN 0 ELSE 1 END, "
                + "cg.fecha_hora, cg.id_clase";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idEntrenador);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) lista.add(convertirResumen(r));
            }
        }
        return lista;
    }

    public List<ClaseGrupalResumen> listarParaCliente(Long idCliente) throws SQLException {
        List<ClaseGrupalResumen> lista = new ArrayList<>();
        String sql = "SELECT cg.id_clase, cg.nombre_clase, cg.descripcion, cg.nivel, "
                + "cg.duracion_base_minutos, cg.intensidad, cg.id_entrenador, "
                + "cg.cupo_maximo, cg.fecha_hora, cg.estado_clase, "
                + "TRIM(COALESCE(p.nombres,'') || ' ' || COALESCE(p.apellidos,'')) AS entrenador, "
                + "(SELECT COUNT(*)::int FROM reserva ra WHERE ra.id_clase=cg.id_clase "
                + "AND ra.estado_reserva='ACTIVA') AS reservados, "
                + "rc.id_reserva AS id_reserva_cliente, rc.estado_reserva AS estado_reserva_cliente "
                + "FROM clase_grupal cg "
                + "JOIN entrenador en ON en.id_persona=cg.id_entrenador "
                + "JOIN persona p ON p.id_persona=en.id_persona "
                + "LEFT JOIN reserva rc ON rc.id_clase=cg.id_clase "
                + "AND rc.id_cliente=? AND rc.estado_reserva='ACTIVA' "
                + "WHERE cg.estado_clase=TRUE AND cg.fecha_hora > CURRENT_TIMESTAMP "
                + "ORDER BY cg.fecha_hora, cg.nombre_clase";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idCliente);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    ClaseGrupalResumen item = convertirResumen(r);
                    long idReserva = r.getLong("id_reserva_cliente");
                    item.setIdReservaCliente(r.wasNull() ? null : idReserva);
                    item.setEstadoReservaCliente(r.getString("estado_reserva_cliente"));
                    lista.add(item);
                }
            }
        }
        return lista;
    }

    public List<ParticipanteClaseGrupal> listarParticipantes(Long idClase) throws SQLException {
        List<ParticipanteClaseGrupal> lista = new ArrayList<>();
        String sql = "SELECT r.id_reserva, r.id_cliente, c.codigo_cliente, "
                + "TRIM(COALESCE(p.nombres,'') || ' ' || COALESCE(p.apellidos,'')) AS cliente, "
                + "r.fecha_hora_reserva, r.estado_reserva "
                + "FROM reserva r "
                + "JOIN cliente c ON c.id_persona=r.id_cliente "
                + "JOIN persona p ON p.id_persona=c.id_persona "
                + "WHERE r.id_clase=? AND r.estado_reserva='ACTIVA' "
                + "ORDER BY p.apellidos, p.nombres";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idClase);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    ParticipanteClaseGrupal p = new ParticipanteClaseGrupal();
                    p.setIdReserva(r.getLong("id_reserva"));
                    p.setIdCliente(r.getLong("id_cliente"));
                    p.setCodigoCliente(r.getString("codigo_cliente"));
                    p.setNombreCompleto(r.getString("cliente"));
                    Timestamp t = r.getTimestamp("fecha_hora_reserva");
                    p.setFechaReserva(t == null ? null : t.toLocalDateTime());
                    p.setEstadoReserva(r.getString("estado_reserva"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    public int contarReservasActivas(Long idClase) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reserva WHERE id_clase=? AND estado_reserva='ACTIVA'";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idClase);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }


    public boolean clasePerteneceAEntrenador(Long idClase, Long idEntrenador) throws SQLException {
        String sql = "SELECT 1 FROM clase_grupal WHERE id_clase=? AND id_entrenador=?";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setLong(1, idClase);
            st.setLong(2, idEntrenador);
            try (ResultSet r = st.executeQuery()) {
                return r.next();
            }
        }
    }

    public boolean reservaPerteneceACliente(Long idReserva, Long idCliente) throws SQLException {
        String sql = "SELECT 1 FROM reserva WHERE id_reserva=? AND id_cliente=? AND estado_reserva='ACTIVA'";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idReserva);
            s.setLong(2, idCliente);
            try (ResultSet r = s.executeQuery()) {
                return r.next();
            }
        }
    }

    private ClaseGrupalResumen convertirResumen(ResultSet r) throws SQLException {
        ClaseGrupalResumen item = new ClaseGrupalResumen();
        item.setIdClase(r.getLong("id_clase"));
        item.setNombreClase(r.getString("nombre_clase"));
        item.setDescripcion(r.getString("descripcion"));
        item.setNivel(r.getString("nivel"));
        item.setDuracionMinutos((Integer) r.getObject("duracion_base_minutos"));
        item.setIntensidad(r.getString("intensidad"));
        item.setIdEntrenador((Long) r.getObject("id_entrenador"));
        item.setCupoMaximo((Integer) r.getObject("cupo_maximo"));
        item.setReservados((Integer) r.getObject("reservados"));
        Timestamp fecha = r.getTimestamp("fecha_hora");
        item.setFechaHora(fecha == null ? null : fecha.toLocalDateTime());
        item.setEstadoClase(r.getBoolean("estado_clase"));
        item.setNombreEntrenador(r.getString("entrenador"));
        return item;
    }
}
