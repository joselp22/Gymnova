package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.EjercicioRecursoMultimedia;

/**
 * DAO para la tabla ejercicio_recurso_multimedia.
 *
 * @author Usuario
 */
public class EjercicioRecursoMultimediaDAO {

    public boolean guardar(
            EjercicioRecursoMultimedia ejercicioRecursoMultimedia
    ) throws SQLException {

        String sql = "INSERT INTO ejercicio_recurso_multimedia (recurso_multimedia, id_ejercicio) VALUES (?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    ejercicioRecursoMultimedia.getRecursoMultimedia()
            );

            sentencia.setLong(
                    2,
                    ejercicioRecursoMultimedia.getIdEjercicio()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public EjercicioRecursoMultimedia buscar(
            String recursoMultimedia,
            Long idEjercicio
    ) throws SQLException {

        String sql = "SELECT recurso_multimedia, id_ejercicio FROM ejercicio_recurso_multimedia WHERE recurso_multimedia = ? AND id_ejercicio = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, recursoMultimedia);
            sentencia.setLong(2, idEjercicio);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirEjercicioRecursoMultimedia(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<EjercicioRecursoMultimedia> listar(
            String criterio
    ) throws SQLException {

        List<EjercicioRecursoMultimedia> lista = new ArrayList<>();
        String sql = "SELECT recurso_multimedia, id_ejercicio FROM ejercicio_recurso_multimedia WHERE recurso_multimedia ILIKE ? ORDER BY recurso_multimedia, id_ejercicio";

        if (criterio == null) {
            criterio = "";
        }

        String filtro = "%" + criterio.trim() + "%";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirEjercicioRecursoMultimedia(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            String recursoMultimedia,
            Long idEjercicio
    ) throws SQLException {

        String sql = "DELETE FROM ejercicio_recurso_multimedia WHERE recurso_multimedia = ? AND id_ejercicio = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, recursoMultimedia);
            sentencia.setLong(2, idEjercicio);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<EjercicioRecursoMultimedia> listarPorIdEjercicio(
            Long idEjercicio
    ) throws SQLException {

        List<EjercicioRecursoMultimedia> lista = new ArrayList<>();
        String sql = "SELECT recurso_multimedia, id_ejercicio FROM ejercicio_recurso_multimedia WHERE id_ejercicio = ? ORDER BY recurso_multimedia, id_ejercicio";

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
                            convertirEjercicioRecursoMultimedia(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }


    public boolean modificar(
            EjercicioRecursoMultimedia ejercicioRecursoMultimedia
    ) throws SQLException {

        return false;
    }

    private EjercicioRecursoMultimedia convertirEjercicioRecursoMultimedia(
            ResultSet resultado
    ) throws SQLException {

        EjercicioRecursoMultimedia ejercicioRecursoMultimedia = new EjercicioRecursoMultimedia();

        ejercicioRecursoMultimedia.setRecursoMultimedia(
                resultado.getString("recurso_multimedia")
        );

        ejercicioRecursoMultimedia.setIdEjercicio(
                resultado.getLong("id_ejercicio")
        );

        return ejercicioRecursoMultimedia;
    }
}
