package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Congelacion;

/**
 * DAO para la tabla congelacion.
 *
 * @author Usuario
 */
public class CongelacionDAO {

    public boolean guardar(
            Congelacion congelacion
    ) throws SQLException {

        String sql = "INSERT INTO congelacion (fecha_solicitud, fecha_inicio, fecha_fin, motivo, observaciones, estado_congelacion, id_membresia) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (congelacion.getFechaSolicitud() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            congelacion.getFechaSolicitud()
                    )
            );
        }

            if (congelacion.getFechaInicio() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            congelacion.getFechaInicio()
                    )
            );
        }

            if (congelacion.getFechaFin() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            congelacion.getFechaFin()
                    )
            );
        }

            sentencia.setString(
                    4,
                    congelacion.getMotivo()
            );

            sentencia.setString(
                    5,
                    congelacion.getObservaciones()
            );

            sentencia.setString(
                    6,
                    congelacion.getEstadoCongelacion()
            );

            sentencia.setLong(
                    7,
                    congelacion.getIdMembresia()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Congelacion congelacion
    ) throws SQLException {

        String sql = "UPDATE congelacion SET fecha_solicitud = ?, fecha_inicio = ?, fecha_fin = ?, motivo = ?, observaciones = ?, estado_congelacion = ?, id_membresia = ? WHERE id_congelacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (congelacion.getFechaSolicitud() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            congelacion.getFechaSolicitud()
                    )
            );
        }

            if (congelacion.getFechaInicio() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            congelacion.getFechaInicio()
                    )
            );
        }

            if (congelacion.getFechaFin() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            congelacion.getFechaFin()
                    )
            );
        }

            sentencia.setString(
                    4,
                    congelacion.getMotivo()
            );

            sentencia.setString(
                    5,
                    congelacion.getObservaciones()
            );

            sentencia.setString(
                    6,
                    congelacion.getEstadoCongelacion()
            );

            sentencia.setLong(
                    7,
                    congelacion.getIdMembresia()
            );

            sentencia.setLong(
                    8,
                    congelacion.getIdCongelacion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Congelacion buscar(
            Long idCongelacion
    ) throws SQLException {

        String sql = "SELECT id_congelacion, fecha_solicitud, fecha_inicio, fecha_fin, motivo, observaciones, estado_congelacion, id_membresia FROM congelacion WHERE id_congelacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idCongelacion);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirCongelacion(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Congelacion> listar(
            String criterio
    ) throws SQLException {

        List<Congelacion> lista = new ArrayList<>();
        String sql = "SELECT id_congelacion, fecha_solicitud, fecha_inicio, fecha_fin, motivo, observaciones, estado_congelacion, id_membresia FROM congelacion WHERE motivo ILIKE ? OR observaciones ILIKE ? OR estado_congelacion ILIKE ? ORDER BY id_congelacion";

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
                            convertirCongelacion(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idCongelacion
    ) throws SQLException {

        String sql = "UPDATE congelacion SET estado_congelacion = 'CANCELADA' WHERE id_congelacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idCongelacion);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idCongelacion
    ) throws SQLException {

        String sql = "DELETE FROM congelacion WHERE id_congelacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idCongelacion);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Congelacion> listarPorIdMembresia(
            Long idMembresia
    ) throws SQLException {

        List<Congelacion> lista = new ArrayList<>();
        String sql = "SELECT id_congelacion, fecha_solicitud, fecha_inicio, fecha_fin, motivo, observaciones, estado_congelacion, id_membresia FROM congelacion WHERE id_membresia = ? ORDER BY id_congelacion";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idMembresia
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirCongelacion(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private Congelacion convertirCongelacion(
            ResultSet resultado
    ) throws SQLException {

        Congelacion congelacion = new Congelacion();

        congelacion.setIdCongelacion(
                resultado.getLong("id_congelacion")
        );

        congelacion.setFechaSolicitud(
                resultado.getDate("fecha_solicitud") == null
                ? null
                : resultado.getDate("fecha_solicitud").toLocalDate()
        );

        congelacion.setFechaInicio(
                resultado.getDate("fecha_inicio") == null
                ? null
                : resultado.getDate("fecha_inicio").toLocalDate()
        );

        congelacion.setFechaFin(
                resultado.getDate("fecha_fin") == null
                ? null
                : resultado.getDate("fecha_fin").toLocalDate()
        );

        congelacion.setMotivo(
                resultado.getString("motivo")
        );

        congelacion.setObservaciones(
                resultado.getString("observaciones")
        );

        congelacion.setEstadoCongelacion(
                resultado.getString("estado_congelacion")
        );

        congelacion.setIdMembresia(
                resultado.getLong("id_membresia")
        );

        return congelacion;
    }
}
