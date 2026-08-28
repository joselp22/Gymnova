package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.RequiereEquipo;

/**
 * DAO para la tabla requiere_equipo.
 *
 * @author Usuario
 */
public class RequiereEquipoDAO {

    public boolean guardar(
            RequiereEquipo requiereEquipo
    ) throws SQLException {

        String sql = "INSERT INTO requiere_equipo (cantidad_requerida, es_obligatorio, alternativa_sin_equipo, observaciones, id_ejercicio, id_tipo_equipo) VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    requiereEquipo.getCantidadRequerida()
            );

            sentencia.setBoolean(
                    2,
                    requiereEquipo.isEsObligatorio()
            );

            sentencia.setString(
                    3,
                    requiereEquipo.getAlternativaSinEquipo()
            );

            sentencia.setString(
                    4,
                    requiereEquipo.getObservaciones()
            );

            sentencia.setLong(
                    5,
                    requiereEquipo.getIdEjercicio()
            );

            sentencia.setLong(
                    6,
                    requiereEquipo.getIdTipoEquipo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            RequiereEquipo requiereEquipo
    ) throws SQLException {

        String sql = "UPDATE requiere_equipo SET cantidad_requerida = ?, es_obligatorio = ?, alternativa_sin_equipo = ?, observaciones = ? WHERE id_ejercicio = ? AND id_tipo_equipo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    requiereEquipo.getCantidadRequerida()
            );

            sentencia.setBoolean(
                    2,
                    requiereEquipo.isEsObligatorio()
            );

            sentencia.setString(
                    3,
                    requiereEquipo.getAlternativaSinEquipo()
            );

            sentencia.setString(
                    4,
                    requiereEquipo.getObservaciones()
            );

            sentencia.setLong(
                    5,
                    requiereEquipo.getIdEjercicio()
            );

            sentencia.setLong(
                    6,
                    requiereEquipo.getIdTipoEquipo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public RequiereEquipo buscar(
            Long idEjercicio,
            Long idTipoEquipo
    ) throws SQLException {

        String sql = "SELECT cantidad_requerida, es_obligatorio, alternativa_sin_equipo, observaciones, id_ejercicio, id_tipo_equipo FROM requiere_equipo WHERE id_ejercicio = ? AND id_tipo_equipo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idEjercicio);
            sentencia.setLong(2, idTipoEquipo);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirRequiereEquipo(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<RequiereEquipo> listar(
            String criterio
    ) throws SQLException {

        List<RequiereEquipo> lista = new ArrayList<>();
        String sql = "SELECT cantidad_requerida, es_obligatorio, alternativa_sin_equipo, observaciones, id_ejercicio, id_tipo_equipo FROM requiere_equipo WHERE alternativa_sin_equipo ILIKE ? OR observaciones ILIKE ? ORDER BY id_ejercicio, id_tipo_equipo";

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

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirRequiereEquipo(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idEjercicio,
            Long idTipoEquipo
    ) throws SQLException {

        String sql = "DELETE FROM requiere_equipo WHERE id_ejercicio = ? AND id_tipo_equipo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idEjercicio);
            sentencia.setLong(2, idTipoEquipo);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<RequiereEquipo> listarPorIdEjercicio(
            Long idEjercicio
    ) throws SQLException {

        List<RequiereEquipo> lista = new ArrayList<>();
        String sql = "SELECT cantidad_requerida, es_obligatorio, alternativa_sin_equipo, observaciones, id_ejercicio, id_tipo_equipo FROM requiere_equipo WHERE id_ejercicio = ? ORDER BY id_ejercicio, id_tipo_equipo";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEjercicio
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirRequiereEquipo(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<RequiereEquipo> listarPorIdTipoEquipo(
            Long idTipoEquipo
    ) throws SQLException {

        List<RequiereEquipo> lista = new ArrayList<>();
        String sql = "SELECT cantidad_requerida, es_obligatorio, alternativa_sin_equipo, observaciones, id_ejercicio, id_tipo_equipo FROM requiere_equipo WHERE id_tipo_equipo = ? ORDER BY id_ejercicio, id_tipo_equipo";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idTipoEquipo
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirRequiereEquipo(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private RequiereEquipo convertirRequiereEquipo(
            ResultSet resultado
    ) throws SQLException {

        RequiereEquipo requiereEquipo = new RequiereEquipo();

        requiereEquipo.setCantidadRequerida(
                resultado.getInt("cantidad_requerida")
        );

        requiereEquipo.setEsObligatorio(
                resultado.getBoolean("es_obligatorio")
        );

        requiereEquipo.setAlternativaSinEquipo(
                resultado.getString("alternativa_sin_equipo")
        );

        requiereEquipo.setObservaciones(
                resultado.getString("observaciones")
        );

        requiereEquipo.setIdEjercicio(
                resultado.getLong("id_ejercicio")
        );

        requiereEquipo.setIdTipoEquipo(
                resultado.getLong("id_tipo_equipo")
        );

        return requiereEquipo;
    }
}
