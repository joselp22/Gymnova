package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.TipoMembresiaBeneficio;

/**
 * DAO para la tabla tipo_membresia_beneficio.
 *
 * @author Usuario
 */
public class TipoMembresiaBeneficioDAO {

    public boolean guardar(
            TipoMembresiaBeneficio tipoMembresiaBeneficio
    ) throws SQLException {

        String sql = "INSERT INTO tipo_membresia_beneficio (beneficio, id_tipo_membresia) VALUES (?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    tipoMembresiaBeneficio.getBeneficio()
            );

            sentencia.setLong(
                    2,
                    tipoMembresiaBeneficio.getIdTipoMembresia()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public TipoMembresiaBeneficio buscar(
            String beneficio,
            Long idTipoMembresia
    ) throws SQLException {

        String sql = "SELECT beneficio, id_tipo_membresia FROM tipo_membresia_beneficio WHERE beneficio = ? AND id_tipo_membresia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, beneficio);
            sentencia.setLong(2, idTipoMembresia);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirTipoMembresiaBeneficio(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<TipoMembresiaBeneficio> listar(
            String criterio
    ) throws SQLException {

        List<TipoMembresiaBeneficio> lista = new ArrayList<>();
        String sql = "SELECT beneficio, id_tipo_membresia FROM tipo_membresia_beneficio WHERE beneficio ILIKE ? ORDER BY beneficio, id_tipo_membresia";

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
                            convertirTipoMembresiaBeneficio(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            String beneficio,
            Long idTipoMembresia
    ) throws SQLException {

        String sql = "DELETE FROM tipo_membresia_beneficio WHERE beneficio = ? AND id_tipo_membresia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, beneficio);
            sentencia.setLong(2, idTipoMembresia);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<TipoMembresiaBeneficio> listarPorIdTipoMembresia(
            Long idTipoMembresia
    ) throws SQLException {

        List<TipoMembresiaBeneficio> lista = new ArrayList<>();
        String sql = "SELECT beneficio, id_tipo_membresia FROM tipo_membresia_beneficio WHERE id_tipo_membresia = ? ORDER BY beneficio, id_tipo_membresia";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idTipoMembresia
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirTipoMembresiaBeneficio(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }


    public boolean modificar(
            TipoMembresiaBeneficio tipoMembresiaBeneficio
    ) throws SQLException {

        return false;
    }

    private TipoMembresiaBeneficio convertirTipoMembresiaBeneficio(
            ResultSet resultado
    ) throws SQLException {

        TipoMembresiaBeneficio tipoMembresiaBeneficio = new TipoMembresiaBeneficio();

        tipoMembresiaBeneficio.setBeneficio(
                resultado.getString("beneficio")
        );

        tipoMembresiaBeneficio.setIdTipoMembresia(
                resultado.getLong("id_tipo_membresia")
        );

        return tipoMembresiaBeneficio;
    }
}
