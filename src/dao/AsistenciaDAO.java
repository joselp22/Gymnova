package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import modelo.Asistencia;

/**
 * DAO para la tabla asistencia.
 *
 * @author Usuario
 */
public class AsistenciaDAO {

    public boolean guardar(
            Asistencia asistencia
    ) throws SQLException {

        String sql = "INSERT INTO asistencia (fecha_asistencia, hora_entrada, hora_salida, tipo_acceso, metodo_registro, estado_acceso, observaciones, id_cliente) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (asistencia.getFechaAsistencia() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            asistencia.getFechaAsistencia()
                    )
            );
        }

            if (asistencia.getHoraEntrada() == null) {
            sentencia.setTime(
                    2,
                    null
            );
        } else {
            sentencia.setTime(
                    2,
                    Time.valueOf(
                            asistencia.getHoraEntrada()
                    )
            );
        }

            if (asistencia.getHoraSalida() == null) {
            sentencia.setTime(
                    3,
                    null
            );
        } else {
            sentencia.setTime(
                    3,
                    Time.valueOf(
                            asistencia.getHoraSalida()
                    )
            );
        }

            sentencia.setString(
                    4,
                    asistencia.getTipoAcceso()
            );

            sentencia.setString(
                    5,
                    asistencia.getMetodoRegistro()
            );

            sentencia.setString(
                    6,
                    asistencia.getEstadoAcceso()
            );

            sentencia.setString(
                    7,
                    asistencia.getObservaciones()
            );

            sentencia.setLong(
                    8,
                    asistencia.getIdCliente()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Asistencia asistencia
    ) throws SQLException {

        String sql = "UPDATE asistencia SET fecha_asistencia = ?, hora_entrada = ?, hora_salida = ?, tipo_acceso = ?, metodo_registro = ?, estado_acceso = ?, observaciones = ?, id_cliente = ? WHERE id_asistencia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (asistencia.getFechaAsistencia() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            asistencia.getFechaAsistencia()
                    )
            );
        }

            if (asistencia.getHoraEntrada() == null) {
            sentencia.setTime(
                    2,
                    null
            );
        } else {
            sentencia.setTime(
                    2,
                    Time.valueOf(
                            asistencia.getHoraEntrada()
                    )
            );
        }

            if (asistencia.getHoraSalida() == null) {
            sentencia.setTime(
                    3,
                    null
            );
        } else {
            sentencia.setTime(
                    3,
                    Time.valueOf(
                            asistencia.getHoraSalida()
                    )
            );
        }

            sentencia.setString(
                    4,
                    asistencia.getTipoAcceso()
            );

            sentencia.setString(
                    5,
                    asistencia.getMetodoRegistro()
            );

            sentencia.setString(
                    6,
                    asistencia.getEstadoAcceso()
            );

            sentencia.setString(
                    7,
                    asistencia.getObservaciones()
            );

            sentencia.setLong(
                    8,
                    asistencia.getIdCliente()
            );

            sentencia.setLong(
                    9,
                    asistencia.getIdAsistencia()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Asistencia buscar(
            Long idAsistencia
    ) throws SQLException {

        String sql = "SELECT id_asistencia, fecha_asistencia, hora_entrada, hora_salida, tipo_acceso, metodo_registro, estado_acceso, observaciones, id_cliente FROM asistencia WHERE id_asistencia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idAsistencia);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirAsistencia(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Asistencia> listar(
            String criterio
    ) throws SQLException {

        List<Asistencia> lista = new ArrayList<>();
        String sql = "SELECT id_asistencia, fecha_asistencia, hora_entrada, hora_salida, tipo_acceso, metodo_registro, estado_acceso, observaciones, id_cliente FROM asistencia WHERE tipo_acceso ILIKE ? OR metodo_registro ILIKE ? OR estado_acceso ILIKE ? OR observaciones ILIKE ? ORDER BY id_asistencia";

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
                            convertirAsistencia(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idAsistencia
    ) throws SQLException {

        String sql = "UPDATE asistencia SET estado_acceso = 'ANULADO' WHERE id_asistencia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idAsistencia);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idAsistencia
    ) throws SQLException {

        String sql = "DELETE FROM asistencia WHERE id_asistencia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idAsistencia);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Asistencia> listarPorIdCliente(
            Long idCliente
    ) throws SQLException {

        List<Asistencia> lista = new ArrayList<>();
        String sql = "SELECT id_asistencia, fecha_asistencia, hora_entrada, hora_salida, tipo_acceso, metodo_registro, estado_acceso, observaciones, id_cliente FROM asistencia WHERE id_cliente = ? ORDER BY id_asistencia";

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
                            convertirAsistencia(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private Asistencia convertirAsistencia(
            ResultSet resultado
    ) throws SQLException {

        Asistencia asistencia = new Asistencia();

        asistencia.setIdAsistencia(
                resultado.getLong("id_asistencia")
        );

        asistencia.setFechaAsistencia(
                resultado.getDate("fecha_asistencia") == null
                ? null
                : resultado.getDate("fecha_asistencia").toLocalDate()
        );

        asistencia.setHoraEntrada(
                resultado.getTime("hora_entrada") == null
                ? null
                : resultado.getTime("hora_entrada").toLocalTime()
        );

        asistencia.setHoraSalida(
                resultado.getTime("hora_salida") == null
                ? null
                : resultado.getTime("hora_salida").toLocalTime()
        );

        asistencia.setTipoAcceso(
                resultado.getString("tipo_acceso")
        );

        asistencia.setMetodoRegistro(
                resultado.getString("metodo_registro")
        );

        asistencia.setEstadoAcceso(
                resultado.getString("estado_acceso")
        );

        asistencia.setObservaciones(
                resultado.getString("observaciones")
        );

        asistencia.setIdCliente(
                resultado.getLong("id_cliente")
        );

        return asistencia;
    }
}
