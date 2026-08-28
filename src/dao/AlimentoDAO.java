package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Alimento;

/**
 * DAO para la tabla alimento.
 *
 * @author Usuario
 */
public class AlimentoDAO {

    public boolean guardar(
            Alimento alimento
    ) throws SQLException {

        // Ahora devuelve además el id generado en el objeto para que el
        // flujo de nutrición pueda encadenar la creación de la comida.
        String sql = "INSERT INTO alimento (nombre_alimento, categoria, descripcion, porcion_referencia_g, proteinas_g, carbohidratos_g, fibra_g) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_alimento";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, alimento.getNombreAlimento());
            sentencia.setString(2, alimento.getCategoria());
            sentencia.setString(3, alimento.getDescripcion());
            sentencia.setBigDecimal(4, alimento.getPorcionReferenciaG());
            sentencia.setBigDecimal(5, alimento.getProteinasG());
            sentencia.setBigDecimal(6, alimento.getCarbohidratosG());
            sentencia.setBigDecimal(7, alimento.getFibraG());

            try (ResultSet r = sentencia.executeQuery()) {
                if (r.next()) {
                    alimento.setIdAlimento(r.getLong(1));
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * Busca un alimento por nombre exacto (respetando el UNIQUE de la
     * tabla). Devuelve null si no existe.
     */
    public Alimento buscarPorNombre(String nombre) throws SQLException {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        String sql = "SELECT id_alimento, nombre_alimento, categoria, "
                + "descripcion, porcion_referencia_g, proteinas_g, "
                + "carbohidratos_g, fibra_g "
                + "FROM alimento WHERE LOWER(TRIM(nombre_alimento)) = LOWER(?)";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, nombre.trim());
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    Alimento a = new Alimento();
                    a.setIdAlimento(r.getLong("id_alimento"));
                    a.setNombreAlimento(r.getString("nombre_alimento"));
                    a.setCategoria(r.getString("categoria"));
                    a.setDescripcion(r.getString("descripcion"));
                    a.setPorcionReferenciaG(r.getBigDecimal("porcion_referencia_g"));
                    a.setProteinasG(r.getBigDecimal("proteinas_g"));
                    a.setCarbohidratosG(r.getBigDecimal("carbohidratos_g"));
                    a.setFibraG(r.getBigDecimal("fibra_g"));
                    return a;
                }
            }
        }
        return null;
    }

    public boolean modificar(
            Alimento alimento
    ) throws SQLException {

        String sql = "UPDATE alimento SET nombre_alimento = ?, categoria = ?, descripcion = ?, porcion_referencia_g = ?, proteinas_g = ?, carbohidratos_g = ?, fibra_g = ? WHERE id_alimento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    alimento.getNombreAlimento()
            );

            sentencia.setString(
                    2,
                    alimento.getCategoria()
            );

            sentencia.setString(
                    3,
                    alimento.getDescripcion()
            );

            sentencia.setBigDecimal(
                    4,
                    alimento.getPorcionReferenciaG()
            );

            sentencia.setBigDecimal(
                    5,
                    alimento.getProteinasG()
            );

            sentencia.setBigDecimal(
                    6,
                    alimento.getCarbohidratosG()
            );

            sentencia.setBigDecimal(
                    7,
                    alimento.getFibraG()
            );

            sentencia.setLong(
                    8,
                    alimento.getIdAlimento()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Alimento buscar(
            Long idAlimento
    ) throws SQLException {

        String sql = "SELECT id_alimento, nombre_alimento, categoria, descripcion, porcion_referencia_g, proteinas_g, carbohidratos_g, fibra_g FROM alimento WHERE id_alimento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idAlimento);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirAlimento(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Alimento> listar(
            String criterio
    ) throws SQLException {

        List<Alimento> lista = new ArrayList<>();
        String sql = "SELECT id_alimento, nombre_alimento, categoria, descripcion, porcion_referencia_g, proteinas_g, carbohidratos_g, fibra_g FROM alimento WHERE nombre_alimento ILIKE ? OR categoria ILIKE ? OR descripcion ILIKE ? ORDER BY id_alimento";

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
                            convertirAlimento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idAlimento
    ) throws SQLException {

        String sql = "DELETE FROM alimento WHERE id_alimento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idAlimento);

            return sentencia.executeUpdate() > 0;
        }
    }

    private Alimento convertirAlimento(
            ResultSet resultado
    ) throws SQLException {

        Alimento alimento = new Alimento();

        alimento.setIdAlimento(
                resultado.getLong("id_alimento")
        );

        alimento.setNombreAlimento(
                resultado.getString("nombre_alimento")
        );

        alimento.setCategoria(
                resultado.getString("categoria")
        );

        alimento.setDescripcion(
                resultado.getString("descripcion")
        );

        alimento.setPorcionReferenciaG(
                resultado.getBigDecimal("porcion_referencia_g")
        );

        alimento.setProteinasG(
                resultado.getBigDecimal("proteinas_g")
        );

        alimento.setCarbohidratosG(
                resultado.getBigDecimal("carbohidratos_g")
        );

        alimento.setFibraG(
                resultado.getBigDecimal("fibra_g")
        );

        return alimento;
    }
}
