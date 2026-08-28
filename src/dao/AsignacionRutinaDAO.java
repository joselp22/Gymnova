package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.AsignacionRutina;

/**
 * DAO para la tabla asignacion_rutina.
 *
 * @author Usuario
 */
public class AsignacionRutinaDAO {

    public boolean guardar(
            AsignacionRutina asignacionRutina
    ) throws SQLException {

        String sql = "INSERT INTO asignacion_rutina (fecha_asignacion, fecha_inicio, fecha_fin, estado_asignacion, motivo_finalizacion, observaciones, id_cliente, id_rutina) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (asignacionRutina.getFechaAsignacion() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            asignacionRutina.getFechaAsignacion()
                    )
            );
        }

            if (asignacionRutina.getFechaInicio() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            asignacionRutina.getFechaInicio()
                    )
            );
        }

            if (asignacionRutina.getFechaFin() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            asignacionRutina.getFechaFin()
                    )
            );
        }

            sentencia.setString(
                    4,
                    asignacionRutina.getEstadoAsignacion()
            );

            sentencia.setString(
                    5,
                    asignacionRutina.getMotivoFinalizacion()
            );

            sentencia.setString(
                    6,
                    asignacionRutina.getObservaciones()
            );

            sentencia.setLong(
                    7,
                    asignacionRutina.getIdCliente()
            );

            sentencia.setLong(
                    8,
                    asignacionRutina.getIdRutina()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            AsignacionRutina asignacionRutina
    ) throws SQLException {

        String sql = "UPDATE asignacion_rutina SET fecha_asignacion = ?, fecha_inicio = ?, fecha_fin = ?, estado_asignacion = ?, motivo_finalizacion = ?, observaciones = ?, id_cliente = ?, id_rutina = ? WHERE id_asignacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (asignacionRutina.getFechaAsignacion() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            asignacionRutina.getFechaAsignacion()
                    )
            );
        }

            if (asignacionRutina.getFechaInicio() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            asignacionRutina.getFechaInicio()
                    )
            );
        }

            if (asignacionRutina.getFechaFin() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            asignacionRutina.getFechaFin()
                    )
            );
        }

            sentencia.setString(
                    4,
                    asignacionRutina.getEstadoAsignacion()
            );

            sentencia.setString(
                    5,
                    asignacionRutina.getMotivoFinalizacion()
            );

            sentencia.setString(
                    6,
                    asignacionRutina.getObservaciones()
            );

            sentencia.setLong(
                    7,
                    asignacionRutina.getIdCliente()
            );

            sentencia.setLong(
                    8,
                    asignacionRutina.getIdRutina()
            );

            sentencia.setLong(
                    9,
                    asignacionRutina.getIdAsignacion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public AsignacionRutina buscar(
            Long idAsignacion
    ) throws SQLException {

        String sql = "SELECT id_asignacion, fecha_asignacion, fecha_inicio, fecha_fin, estado_asignacion, motivo_finalizacion, observaciones, id_cliente, id_rutina FROM asignacion_rutina WHERE id_asignacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idAsignacion);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirAsignacionRutina(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<AsignacionRutina> listar(
            String criterio
    ) throws SQLException {

        List<AsignacionRutina> lista = new ArrayList<>();
        String sql = "SELECT id_asignacion, fecha_asignacion, fecha_inicio, fecha_fin, estado_asignacion, motivo_finalizacion, observaciones, id_cliente, id_rutina FROM asignacion_rutina WHERE estado_asignacion ILIKE ? OR motivo_finalizacion ILIKE ? OR observaciones ILIKE ? ORDER BY id_asignacion";

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

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirAsignacionRutina(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idAsignacion
    ) throws SQLException {

        String sql = "UPDATE asignacion_rutina SET estado_asignacion = 'CANCELADA' WHERE id_asignacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idAsignacion);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idAsignacion
    ) throws SQLException {

        String sql = "DELETE FROM asignacion_rutina WHERE id_asignacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idAsignacion);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<AsignacionRutina> listarPorIdCliente(
            Long idCliente
    ) throws SQLException {

        List<AsignacionRutina> lista = new ArrayList<>();
        String sql = "SELECT id_asignacion, fecha_asignacion, fecha_inicio, fecha_fin, estado_asignacion, motivo_finalizacion, observaciones, id_cliente, id_rutina FROM asignacion_rutina WHERE id_cliente = ? ORDER BY id_asignacion";

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
                            convertirAsignacionRutina(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<AsignacionRutina> listarPorIdRutina(
            Long idRutina
    ) throws SQLException {

        List<AsignacionRutina> lista = new ArrayList<>();
        String sql = "SELECT id_asignacion, fecha_asignacion, fecha_inicio, fecha_fin, estado_asignacion, motivo_finalizacion, observaciones, id_cliente, id_rutina FROM asignacion_rutina WHERE id_rutina = ? ORDER BY id_asignacion";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idRutina
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirAsignacionRutina(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private AsignacionRutina convertirAsignacionRutina(
            ResultSet resultado
    ) throws SQLException {

        AsignacionRutina asignacionRutina = new AsignacionRutina();

        asignacionRutina.setIdAsignacion(
                resultado.getLong("id_asignacion")
        );

        asignacionRutina.setFechaAsignacion(
                resultado.getDate("fecha_asignacion") == null
                ? null
                : resultado.getDate("fecha_asignacion").toLocalDate()
        );

        asignacionRutina.setFechaInicio(
                resultado.getDate("fecha_inicio") == null
                ? null
                : resultado.getDate("fecha_inicio").toLocalDate()
        );

        asignacionRutina.setFechaFin(
                resultado.getDate("fecha_fin") == null
                ? null
                : resultado.getDate("fecha_fin").toLocalDate()
        );

        asignacionRutina.setEstadoAsignacion(
                resultado.getString("estado_asignacion")
        );

        asignacionRutina.setMotivoFinalizacion(
                resultado.getString("motivo_finalizacion")
        );

        asignacionRutina.setObservaciones(
                resultado.getString("observaciones")
        );

        asignacionRutina.setIdCliente(
                resultado.getLong("id_cliente")
        );

        asignacionRutina.setIdRutina(
                resultado.getLong("id_rutina")
        );

        return asignacionRutina;
    }
}
