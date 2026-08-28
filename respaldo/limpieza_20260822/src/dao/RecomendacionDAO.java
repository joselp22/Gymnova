package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Recomendacion;

/**
 * DAO para la tabla recomendacion.
 *
 * @author Usuario
 */
public class RecomendacionDAO {

    public boolean guardar(
            Recomendacion recomendacion
    ) throws SQLException {

        String sql = "INSERT INTO recomendacion (fecha_recomendacion, tipo_recomendacion, titulo, descripcion, prioridad, fecha_inicio, fecha_fin, estado_recomendacion, id_evaluacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (recomendacion.getFechaRecomendacion() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            recomendacion.getFechaRecomendacion()
                    )
            );
        }

            sentencia.setString(
                    2,
                    recomendacion.getTipoRecomendacion()
            );

            sentencia.setString(
                    3,
                    recomendacion.getTitulo()
            );

            sentencia.setString(
                    4,
                    recomendacion.getDescripcion()
            );

            sentencia.setString(
                    5,
                    recomendacion.getPrioridad()
            );

            if (recomendacion.getFechaInicio() == null) {
            sentencia.setDate(
                    6,
                    null
            );
        } else {
            sentencia.setDate(
                    6,
                    Date.valueOf(
                            recomendacion.getFechaInicio()
                    )
            );
        }

            if (recomendacion.getFechaFin() == null) {
            sentencia.setDate(
                    7,
                    null
            );
        } else {
            sentencia.setDate(
                    7,
                    Date.valueOf(
                            recomendacion.getFechaFin()
                    )
            );
        }

            sentencia.setString(
                    8,
                    recomendacion.getEstadoRecomendacion()
            );

            sentencia.setLong(
                    9,
                    recomendacion.getIdEvaluacion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Recomendacion recomendacion
    ) throws SQLException {

        String sql = "UPDATE recomendacion SET fecha_recomendacion = ?, tipo_recomendacion = ?, titulo = ?, descripcion = ?, prioridad = ?, fecha_inicio = ?, fecha_fin = ?, estado_recomendacion = ?, id_evaluacion = ? WHERE id_recomendacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (recomendacion.getFechaRecomendacion() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            recomendacion.getFechaRecomendacion()
                    )
            );
        }

            sentencia.setString(
                    2,
                    recomendacion.getTipoRecomendacion()
            );

            sentencia.setString(
                    3,
                    recomendacion.getTitulo()
            );

            sentencia.setString(
                    4,
                    recomendacion.getDescripcion()
            );

            sentencia.setString(
                    5,
                    recomendacion.getPrioridad()
            );

            if (recomendacion.getFechaInicio() == null) {
            sentencia.setDate(
                    6,
                    null
            );
        } else {
            sentencia.setDate(
                    6,
                    Date.valueOf(
                            recomendacion.getFechaInicio()
                    )
            );
        }

            if (recomendacion.getFechaFin() == null) {
            sentencia.setDate(
                    7,
                    null
            );
        } else {
            sentencia.setDate(
                    7,
                    Date.valueOf(
                            recomendacion.getFechaFin()
                    )
            );
        }

            sentencia.setString(
                    8,
                    recomendacion.getEstadoRecomendacion()
            );

            sentencia.setLong(
                    9,
                    recomendacion.getIdEvaluacion()
            );

            sentencia.setLong(
                    10,
                    recomendacion.getIdRecomendacion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Recomendacion buscar(
            Long idRecomendacion
    ) throws SQLException {

        String sql = "SELECT id_recomendacion, fecha_recomendacion, tipo_recomendacion, titulo, descripcion, prioridad, fecha_inicio, fecha_fin, estado_recomendacion, id_evaluacion FROM recomendacion WHERE id_recomendacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idRecomendacion);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirRecomendacion(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Recomendacion> listar(
            String criterio
    ) throws SQLException {

        List<Recomendacion> lista = new ArrayList<>();
        String sql = "SELECT id_recomendacion, fecha_recomendacion, tipo_recomendacion, titulo, descripcion, prioridad, fecha_inicio, fecha_fin, estado_recomendacion, id_evaluacion FROM recomendacion WHERE tipo_recomendacion ILIKE ? OR titulo ILIKE ? OR descripcion ILIKE ? OR prioridad ILIKE ? OR estado_recomendacion ILIKE ? ORDER BY id_recomendacion";

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
            sentencia.setString(5, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirRecomendacion(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idRecomendacion
    ) throws SQLException {

        String sql = "UPDATE recomendacion SET estado_recomendacion = 'CANCELADA' WHERE id_recomendacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idRecomendacion);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idRecomendacion
    ) throws SQLException {

        String sql = "DELETE FROM recomendacion WHERE id_recomendacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idRecomendacion);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Recomendacion> listarPorIdEvaluacion(
            Long idEvaluacion
    ) throws SQLException {

        List<Recomendacion> lista = new ArrayList<>();
        String sql = "SELECT id_recomendacion, fecha_recomendacion, tipo_recomendacion, titulo, descripcion, prioridad, fecha_inicio, fecha_fin, estado_recomendacion, id_evaluacion FROM recomendacion WHERE id_evaluacion = ? ORDER BY id_recomendacion";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEvaluacion
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirRecomendacion(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private Recomendacion convertirRecomendacion(
            ResultSet resultado
    ) throws SQLException {

        Recomendacion recomendacion = new Recomendacion();

        recomendacion.setIdRecomendacion(
                resultado.getLong("id_recomendacion")
        );

        recomendacion.setFechaRecomendacion(
                resultado.getDate("fecha_recomendacion") == null
                ? null
                : resultado.getDate("fecha_recomendacion").toLocalDate()
        );

        recomendacion.setTipoRecomendacion(
                resultado.getString("tipo_recomendacion")
        );

        recomendacion.setTitulo(
                resultado.getString("titulo")
        );

        recomendacion.setDescripcion(
                resultado.getString("descripcion")
        );

        recomendacion.setPrioridad(
                resultado.getString("prioridad")
        );

        recomendacion.setFechaInicio(
                resultado.getDate("fecha_inicio") == null
                ? null
                : resultado.getDate("fecha_inicio").toLocalDate()
        );

        recomendacion.setFechaFin(
                resultado.getDate("fecha_fin") == null
                ? null
                : resultado.getDate("fecha_fin").toLocalDate()
        );

        recomendacion.setEstadoRecomendacion(
                resultado.getString("estado_recomendacion")
        );

        recomendacion.setIdEvaluacion(
                resultado.getLong("id_evaluacion")
        );

        return recomendacion;
    }
}
