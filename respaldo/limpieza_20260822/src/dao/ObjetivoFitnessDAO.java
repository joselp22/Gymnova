package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.ObjetivoFitness;

/**
 * DAO para la tabla objetivo_fitness.
 *
 * @author Usuario
 */
public class ObjetivoFitnessDAO {

    public boolean guardar(
            ObjetivoFitness objetivoFitness
    ) throws SQLException {

        String sql = "INSERT INTO objetivo_fitness (nombre_objetivo, descripcion, categoria, estado_objetivo) VALUES (?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    objetivoFitness.getNombreObjetivo()
            );

            sentencia.setString(
                    2,
                    objetivoFitness.getDescripcion()
            );

            sentencia.setString(
                    3,
                    objetivoFitness.getCategoria()
            );

            sentencia.setBoolean(
                    4,
                    objetivoFitness.isEstadoObjetivo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            ObjetivoFitness objetivoFitness
    ) throws SQLException {

        String sql = "UPDATE objetivo_fitness SET nombre_objetivo = ?, descripcion = ?, categoria = ?, estado_objetivo = ? WHERE id_objetivo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    objetivoFitness.getNombreObjetivo()
            );

            sentencia.setString(
                    2,
                    objetivoFitness.getDescripcion()
            );

            sentencia.setString(
                    3,
                    objetivoFitness.getCategoria()
            );

            sentencia.setBoolean(
                    4,
                    objetivoFitness.isEstadoObjetivo()
            );

            sentencia.setLong(
                    5,
                    objetivoFitness.getIdObjetivo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public ObjetivoFitness buscar(
            Long idObjetivo
    ) throws SQLException {

        String sql = "SELECT id_objetivo, nombre_objetivo, descripcion, categoria, estado_objetivo FROM objetivo_fitness WHERE id_objetivo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idObjetivo);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirObjetivoFitness(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<ObjetivoFitness> listar(
            String criterio
    ) throws SQLException {

        List<ObjetivoFitness> lista = new ArrayList<>();
        String sql = "SELECT id_objetivo, nombre_objetivo, descripcion, categoria, estado_objetivo FROM objetivo_fitness WHERE nombre_objetivo ILIKE ? OR descripcion ILIKE ? OR categoria ILIKE ? ORDER BY id_objetivo";

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
                            convertirObjetivoFitness(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idObjetivo
    ) throws SQLException {

        String sql = "UPDATE objetivo_fitness SET estado_objetivo = FALSE WHERE id_objetivo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idObjetivo);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idObjetivo
    ) throws SQLException {

        String sql = "DELETE FROM objetivo_fitness WHERE id_objetivo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idObjetivo);

            return sentencia.executeUpdate() > 0;
        }
    }



    private ObjetivoFitness convertirObjetivoFitness(
            ResultSet resultado
    ) throws SQLException {

        ObjetivoFitness objetivoFitness = new ObjetivoFitness();

        objetivoFitness.setIdObjetivo(
                resultado.getLong("id_objetivo")
        );

        objetivoFitness.setNombreObjetivo(
                resultado.getString("nombre_objetivo")
        );

        objetivoFitness.setDescripcion(
                resultado.getString("descripcion")
        );

        objetivoFitness.setCategoria(
                resultado.getString("categoria")
        );

        objetivoFitness.setEstadoObjetivo(
                resultado.getBoolean("estado_objetivo")
        );

        return objetivoFitness;
    }
}
