package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.ClaseGrupal;

/**
 * DAO para la tabla clase_grupal.
 *
 * @author Usuario
 */
public class ClaseGrupalDAO {

    public boolean guardar(
            ClaseGrupal claseGrupal
    ) throws SQLException {

        String sql = "INSERT INTO clase_grupal (nombre_clase, descripcion, nivel, duracion_base_minutos, intensidad, estado_clase) VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    claseGrupal.getNombreClase()
            );

            sentencia.setString(
                    2,
                    claseGrupal.getDescripcion()
            );

            sentencia.setString(
                    3,
                    claseGrupal.getNivel()
            );

            sentencia.setInt(
                    4,
                    claseGrupal.getDuracionBaseMinutos()
            );

            sentencia.setString(
                    5,
                    claseGrupal.getIntensidad()
            );

            sentencia.setBoolean(
                    6,
                    claseGrupal.isEstadoClase()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            ClaseGrupal claseGrupal
    ) throws SQLException {

        String sql = "UPDATE clase_grupal SET nombre_clase = ?, descripcion = ?, nivel = ?, duracion_base_minutos = ?, intensidad = ?, estado_clase = ? WHERE id_clase = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    claseGrupal.getNombreClase()
            );

            sentencia.setString(
                    2,
                    claseGrupal.getDescripcion()
            );

            sentencia.setString(
                    3,
                    claseGrupal.getNivel()
            );

            sentencia.setInt(
                    4,
                    claseGrupal.getDuracionBaseMinutos()
            );

            sentencia.setString(
                    5,
                    claseGrupal.getIntensidad()
            );

            sentencia.setBoolean(
                    6,
                    claseGrupal.isEstadoClase()
            );

            sentencia.setLong(
                    7,
                    claseGrupal.getIdClase()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public ClaseGrupal buscar(
            Long idClase
    ) throws SQLException {

        String sql = "SELECT id_clase, nombre_clase, descripcion, nivel, duracion_base_minutos, intensidad, estado_clase FROM clase_grupal WHERE id_clase = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idClase);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirClaseGrupal(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<ClaseGrupal> listar(
            String criterio
    ) throws SQLException {

        List<ClaseGrupal> lista = new ArrayList<>();
        String sql = "SELECT id_clase, nombre_clase, descripcion, nivel, duracion_base_minutos, intensidad, estado_clase FROM clase_grupal WHERE nombre_clase ILIKE ? OR descripcion ILIKE ? OR nivel ILIKE ? OR intensidad ILIKE ? ORDER BY id_clase";

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
                            convertirClaseGrupal(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idClase
    ) throws SQLException {

        String sql = "UPDATE clase_grupal SET estado_clase = FALSE WHERE id_clase = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idClase);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idClase
    ) throws SQLException {

        String sql = "DELETE FROM clase_grupal WHERE id_clase = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idClase);

            return sentencia.executeUpdate() > 0;
        }
    }



    private ClaseGrupal convertirClaseGrupal(
            ResultSet resultado
    ) throws SQLException {

        ClaseGrupal claseGrupal = new ClaseGrupal();

        claseGrupal.setIdClase(
                resultado.getLong("id_clase")
        );

        claseGrupal.setNombreClase(
                resultado.getString("nombre_clase")
        );

        claseGrupal.setDescripcion(
                resultado.getString("descripcion")
        );

        claseGrupal.setNivel(
                resultado.getString("nivel")
        );

        claseGrupal.setDuracionBaseMinutos(
                resultado.getInt("duracion_base_minutos")
        );

        claseGrupal.setIntensidad(
                resultado.getString("intensidad")
        );

        claseGrupal.setEstadoClase(
                resultado.getBoolean("estado_clase")
        );

        return claseGrupal;
    }
}
