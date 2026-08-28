package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Provincia;

/**
 * DAO para la tabla provincia.
 *
 * @author Usuario
 */
public class ProvinciaDAO {

    public boolean guardar(
            Provincia provincia
    ) throws SQLException {

        String sql = "INSERT INTO provincia (nombre, estado) VALUES (?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    provincia.getNombre()
            );

            sentencia.setBoolean(
                    2,
                    provincia.isEstado()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Provincia provincia
    ) throws SQLException {

        String sql = "UPDATE provincia SET nombre = ?, estado = ? WHERE id_provincia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    provincia.getNombre()
            );

            sentencia.setBoolean(
                    2,
                    provincia.isEstado()
            );

            sentencia.setInt(
                    3,
                    provincia.getIdProvincia()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Provincia buscar(
            Integer idProvincia
    ) throws SQLException {

        String sql = "SELECT id_provincia, nombre, estado FROM provincia WHERE id_provincia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(1, idProvincia);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirProvincia(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Provincia> listar(
            String criterio
    ) throws SQLException {

        List<Provincia> lista = new ArrayList<>();
        String sql = "SELECT id_provincia, nombre, estado FROM provincia WHERE nombre ILIKE ? OR CAST(estado AS TEXT) ILIKE ? ORDER BY estado DESC, nombre";

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
                            convertirProvincia(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Integer idProvincia
    ) throws SQLException {

        String sql = "UPDATE provincia SET estado = FALSE WHERE id_provincia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(1, idProvincia);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Integer idProvincia
    ) throws SQLException {

        String sql = "DELETE FROM provincia WHERE id_provincia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(1, idProvincia);

            return sentencia.executeUpdate() > 0;
        }
    }

    private Provincia convertirProvincia(
            ResultSet resultado
    ) throws SQLException {

        Provincia provincia = new Provincia();

        provincia.setIdProvincia(
                resultado.getInt("id_provincia")
        );

        provincia.setNombre(
                resultado.getString("nombre")
        );

        provincia.setEstado(
                resultado.getBoolean("estado")
        );

        return provincia;
    }
}
