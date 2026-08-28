package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import modelo.Reserva;
import utilidades.GeneradorCodigos;

/**
 * DAO para la tabla reserva.
 *
 * @author Usuario
 */
public class ReservaDAO {

    private static final String PREFIJO_CODIGO
            = GeneradorCodigos.PREFIJO_RESERVA;
    private static final long CLAVE_BLOQUEO_CODIGO
            = GeneradorCodigos.crearClaveBloqueoAdvisory(
                    "reserva.codigo_reserva"
            );

    public boolean guardar(
            Reserva reserva
    ) throws SQLException {

        Connection conexion = null;
        boolean autoCommitOriginal = true;

        try {
            conexion = ConexionPostgreSQL.getConexion();
            autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            bloquearGeneracionCodigo(
                    conexion
            );

            reserva.setCodigoReserva(
                    generarSiguienteCodigo(
                            conexion
                    )
            );

            boolean guardado = insertar(
                    conexion,
                    reserva
            );

            conexion.commit();
            return guardado;

        } catch (SQLException | RuntimeException e) {

            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }

            throw e;

        } finally {

            if (conexion != null) {
                try {
                    conexion.setAutoCommit(
                            autoCommitOriginal
                    );
                } finally {
                    conexion.close();
                }
            }
        }
    }

    private boolean insertar(
            Connection conexion,
            Reserva reserva
    ) throws SQLException {

        String sql = "INSERT INTO reserva (codigo_reserva, fecha_hora_reserva, estado_reserva, fecha_hora_cancelacion, motivo_cancelacion, asistencia_confirmada, observaciones, id_cliente, id_clase) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    reserva.getCodigoReserva()
            );

            if (reserva.getFechaHoraReserva() == null) {
            sentencia.setTimestamp(
                    2,
                    null
            );
        } else {
            sentencia.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            reserva.getFechaHoraReserva()
                    )
            );
        }

            sentencia.setString(
                    3,
                    reserva.getEstadoReserva()
            );

            if (reserva.getFechaHoraCancelacion() == null) {
            sentencia.setTimestamp(
                    4,
                    null
            );
        } else {
            sentencia.setTimestamp(
                    4,
                    Timestamp.valueOf(
                            reserva.getFechaHoraCancelacion()
                    )
            );
        }

            sentencia.setString(
                    5,
                    reserva.getMotivoCancelacion()
            );

            sentencia.setBoolean(
                    6,
                    reserva.isAsistenciaConfirmada()
            );

            sentencia.setString(
                    7,
                    reserva.getObservaciones()
            );

            sentencia.setLong(
                    8,
                    reserva.getIdCliente()
            );

            sentencia.setLong(
                    9,
                    reserva.getIdClase()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Reserva reserva
    ) throws SQLException {

        String sql = "UPDATE reserva SET fecha_hora_reserva = ?, estado_reserva = ?, fecha_hora_cancelacion = ?, motivo_cancelacion = ?, asistencia_confirmada = ?, observaciones = ?, id_cliente = ?, id_clase = ? WHERE id_reserva = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (reserva.getFechaHoraReserva() == null) {
            sentencia.setTimestamp(
                    1,
                    null
            );
        } else {
            sentencia.setTimestamp(
                    1,
                    Timestamp.valueOf(
                            reserva.getFechaHoraReserva()
                    )
            );
        }

            sentencia.setString(
                    2,
                    reserva.getEstadoReserva()
            );

            if (reserva.getFechaHoraCancelacion() == null) {
            sentencia.setTimestamp(
                    3,
                    null
            );
        } else {
            sentencia.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            reserva.getFechaHoraCancelacion()
                    )
            );
        }

            sentencia.setString(
                    4,
                    reserva.getMotivoCancelacion()
            );

            sentencia.setBoolean(
                    5,
                    reserva.isAsistenciaConfirmada()
            );

            sentencia.setString(
                    6,
                    reserva.getObservaciones()
            );

            sentencia.setLong(
                    7,
                    reserva.getIdCliente()
            );

            sentencia.setLong(
                    8,
                    reserva.getIdClase()
            );

            sentencia.setLong(
                    9,
                    reserva.getIdReserva()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Reserva buscar(
            Long idReserva
    ) throws SQLException {

        String sql = "SELECT id_reserva, codigo_reserva, fecha_hora_reserva, estado_reserva, fecha_hora_cancelacion, motivo_cancelacion, asistencia_confirmada, observaciones, id_cliente, id_clase FROM reserva WHERE id_reserva = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idReserva);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirReserva(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Reserva> listar(
            String criterio
    ) throws SQLException {

        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT id_reserva, codigo_reserva, fecha_hora_reserva, estado_reserva, fecha_hora_cancelacion, motivo_cancelacion, asistencia_confirmada, observaciones, id_cliente, id_clase FROM reserva WHERE codigo_reserva ILIKE ? OR estado_reserva ILIKE ? OR motivo_cancelacion ILIKE ? OR observaciones ILIKE ? ORDER BY id_reserva";

        if (criterio == null) {
            criterio = "";
        }

        String filtro = "%" + criterio.trim() + "%";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, filtro);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);
            sentencia.setString(4, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirReserva(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idReserva
    ) throws SQLException {

        String sql = "UPDATE reserva SET estado_reserva = 'CANCELADA', "
                + "motivo_cancelacion = COALESCE(motivo_cancelacion, 'Cancelada desde gestion') "
                + "WHERE id_reserva = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idReserva);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idReserva
    ) throws SQLException {

        String sql = "DELETE FROM reserva WHERE id_reserva = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idReserva);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Reserva> listarPorIdCliente(
            Long idCliente
    ) throws SQLException {

        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT id_reserva, codigo_reserva, fecha_hora_reserva, estado_reserva, fecha_hora_cancelacion, motivo_cancelacion, asistencia_confirmada, observaciones, id_cliente, id_clase FROM reserva WHERE id_cliente = ? ORDER BY id_reserva";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idCliente
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirReserva(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<Reserva> listarPorIdClase(
            Long idClase
    ) throws SQLException {

        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT id_reserva, codigo_reserva, fecha_hora_reserva, estado_reserva, fecha_hora_cancelacion, motivo_cancelacion, asistencia_confirmada, observaciones, id_cliente, id_clase FROM reserva WHERE id_clase = ? ORDER BY id_reserva";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idClase
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirReserva(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private void bloquearGeneracionCodigo(
            Connection conexion
    ) throws SQLException {

        String sql = "SELECT pg_advisory_xact_lock(?)";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    CLAVE_BLOQUEO_CODIGO
            );

            sentencia.execute();
        }
    }

    private String generarSiguienteCodigo(
            Connection conexion
    ) throws SQLException {

        String sql = "SELECT codigo_reserva FROM reserva "
                + "WHERE codigo_reserva ~ ? "
                + "ORDER BY CAST(SUBSTRING(codigo_reserva FROM 3) AS INTEGER) DESC "
                + "LIMIT 1";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    GeneradorCodigos.crearPatronPostgreSQL(
                            PREFIJO_CODIGO
                    )
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return GeneradorCodigos.generarSiguienteCodigo(
                            PREFIJO_CODIGO,
                            resultado.getString(
                                    "codigo_reserva"
                            )
                    );
                }
            }
        }

        return GeneradorCodigos.construirCodigo(
                PREFIJO_CODIGO,
                GeneradorCodigos.VALOR_INICIAL
        );
    }

    private Reserva convertirReserva(
            ResultSet resultado
    ) throws SQLException {

        Reserva reserva = new Reserva();

        reserva.setIdReserva(
                resultado.getLong("id_reserva")
        );

        reserva.setCodigoReserva(
                resultado.getString("codigo_reserva")
        );

        reserva.setFechaHoraReserva(
                resultado.getTimestamp("fecha_hora_reserva") == null
                ? null
                : resultado.getTimestamp("fecha_hora_reserva").toLocalDateTime()
        );

        reserva.setEstadoReserva(
                resultado.getString("estado_reserva")
        );

        reserva.setFechaHoraCancelacion(
                resultado.getTimestamp("fecha_hora_cancelacion") == null
                ? null
                : resultado.getTimestamp("fecha_hora_cancelacion").toLocalDateTime()
        );

        reserva.setMotivoCancelacion(
                resultado.getString("motivo_cancelacion")
        );

        reserva.setAsistenciaConfirmada(
                resultado.getBoolean("asistencia_confirmada")
        );

        reserva.setObservaciones(
                resultado.getString("observaciones")
        );

        reserva.setIdCliente(
                resultado.getLong("id_cliente")
        );

        reserva.setIdClase(
                resultado.getLong("id_clase")
        );

        return reserva;
    }
}
