package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.AlimentoAlergeno;

/**
 * DAO para la tabla alimento_alergeno.
 *
 * @author Usuario
 */
public class AlimentoAlergenoDAO {

    public boolean guardar(
            AlimentoAlergeno alimentoAlergeno
    ) throws SQLException {

        String sql = "INSERT INTO alimento_alergeno (alergeno, id_alimento) VALUES (?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    alimentoAlergeno.getAlergeno()
            );

            sentencia.setLong(
                    2,
                    alimentoAlergeno.getIdAlimento()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public AlimentoAlergeno buscar(
            String alergeno,
            Long idAlimento
    ) throws SQLException {

        String sql = "SELECT alergeno, id_alimento FROM alimento_alergeno WHERE alergeno = ? AND id_alimento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, alergeno);
            sentencia.setLong(2, idAlimento);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirAlimentoAlergeno(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<AlimentoAlergeno> listar(
            String criterio
    ) throws SQLException {

        List<AlimentoAlergeno> lista = new ArrayList<>();
        String sql = "SELECT alergeno, id_alimento FROM alimento_alergeno WHERE alergeno ILIKE ? ORDER BY alergeno, id_alimento";

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
                            convertirAlimentoAlergeno(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            String alergeno,
            Long idAlimento
    ) throws SQLException {

        String sql = "DELETE FROM alimento_alergeno WHERE alergeno = ? AND id_alimento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, alergeno);
            sentencia.setLong(2, idAlimento);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<AlimentoAlergeno> listarPorIdAlimento(
            Long idAlimento
    ) throws SQLException {

        List<AlimentoAlergeno> lista = new ArrayList<>();
        String sql = "SELECT alergeno, id_alimento FROM alimento_alergeno WHERE id_alimento = ? ORDER BY alergeno, id_alimento";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idAlimento
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirAlimentoAlergeno(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }


    public boolean modificar(
            AlimentoAlergeno alimentoAlergeno
    ) throws SQLException {

        return false;
    }

    private AlimentoAlergeno convertirAlimentoAlergeno(
            ResultSet resultado
    ) throws SQLException {

        AlimentoAlergeno alimentoAlergeno = new AlimentoAlergeno();

        alimentoAlergeno.setAlergeno(
                resultado.getString("alergeno")
        );

        alimentoAlergeno.setIdAlimento(
                resultado.getLong("id_alimento")
        );

        return alimentoAlergeno;
    }
}
