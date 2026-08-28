package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.GrupoMuscular;

/**
 * DAO para la tabla grupo_muscular.
 *
 * @author Usuario
 */
public class GrupoMuscularDAO {

    public boolean guardar(
            GrupoMuscular grupoMuscular
    ) throws SQLException {

        String sql = "INSERT INTO grupo_muscular (nombre_grupo, zona_corporal, descripcion, estado_grupo) VALUES (?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    grupoMuscular.getNombreGrupo()
            );

            sentencia.setString(
                    2,
                    grupoMuscular.getZonaCorporal()
            );

            sentencia.setString(
                    3,
                    grupoMuscular.getDescripcion()
            );

            sentencia.setBoolean(
                    4,
                    grupoMuscular.isEstadoGrupo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            GrupoMuscular grupoMuscular
    ) throws SQLException {

        String sql = "UPDATE grupo_muscular SET nombre_grupo = ?, zona_corporal = ?, descripcion = ?, estado_grupo = ? WHERE id_grupo_muscular = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    grupoMuscular.getNombreGrupo()
            );

            sentencia.setString(
                    2,
                    grupoMuscular.getZonaCorporal()
            );

            sentencia.setString(
                    3,
                    grupoMuscular.getDescripcion()
            );

            sentencia.setBoolean(
                    4,
                    grupoMuscular.isEstadoGrupo()
            );

            sentencia.setLong(
                    5,
                    grupoMuscular.getIdGrupoMuscular()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public GrupoMuscular buscar(
            Long idGrupoMuscular
    ) throws SQLException {

        String sql = "SELECT id_grupo_muscular, nombre_grupo, zona_corporal, descripcion, estado_grupo FROM grupo_muscular WHERE id_grupo_muscular = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idGrupoMuscular);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirGrupoMuscular(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<GrupoMuscular> listar(
            String criterio
    ) throws SQLException {

        List<GrupoMuscular> lista = new ArrayList<>();
        String sql = "SELECT id_grupo_muscular, nombre_grupo, zona_corporal, descripcion, estado_grupo FROM grupo_muscular WHERE nombre_grupo ILIKE ? OR zona_corporal ILIKE ? OR descripcion ILIKE ? ORDER BY id_grupo_muscular";

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
                            convertirGrupoMuscular(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idGrupoMuscular
    ) throws SQLException {

        String sql = "UPDATE grupo_muscular SET estado_grupo = FALSE WHERE id_grupo_muscular = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idGrupoMuscular);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idGrupoMuscular
    ) throws SQLException {

        String sql = "DELETE FROM grupo_muscular WHERE id_grupo_muscular = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idGrupoMuscular);

            return sentencia.executeUpdate() > 0;
        }
    }



    private GrupoMuscular convertirGrupoMuscular(
            ResultSet resultado
    ) throws SQLException {

        GrupoMuscular grupoMuscular = new GrupoMuscular();

        grupoMuscular.setIdGrupoMuscular(
                resultado.getLong("id_grupo_muscular")
        );

        grupoMuscular.setNombreGrupo(
                resultado.getString("nombre_grupo")
        );

        grupoMuscular.setZonaCorporal(
                resultado.getString("zona_corporal")
        );

        grupoMuscular.setDescripcion(
                resultado.getString("descripcion")
        );

        grupoMuscular.setEstadoGrupo(
                resultado.getBoolean("estado_grupo")
        );

        return grupoMuscular;
    }
}
