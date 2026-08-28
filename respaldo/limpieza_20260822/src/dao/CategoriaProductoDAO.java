package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.CategoriaProducto;

/**
 * DAO para la tabla categoria_producto.
 *
 * @author Usuario
 */
public class CategoriaProductoDAO {

    public boolean guardar(
            CategoriaProducto categoriaProducto
    ) throws SQLException {

        String sql = "INSERT INTO categoria_producto (nombre_categoria, descripcion, requiere_caducidad, estado_categoria) VALUES (?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    categoriaProducto.getNombreCategoria()
            );

            sentencia.setString(
                    2,
                    categoriaProducto.getDescripcion()
            );

            sentencia.setBoolean(
                    3,
                    categoriaProducto.isRequiereCaducidad()
            );

            sentencia.setBoolean(
                    4,
                    categoriaProducto.isEstadoCategoria()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            CategoriaProducto categoriaProducto
    ) throws SQLException {

        String sql = "UPDATE categoria_producto SET nombre_categoria = ?, descripcion = ?, requiere_caducidad = ?, estado_categoria = ? WHERE id_categoria_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    categoriaProducto.getNombreCategoria()
            );

            sentencia.setString(
                    2,
                    categoriaProducto.getDescripcion()
            );

            sentencia.setBoolean(
                    3,
                    categoriaProducto.isRequiereCaducidad()
            );

            sentencia.setBoolean(
                    4,
                    categoriaProducto.isEstadoCategoria()
            );

            sentencia.setLong(
                    5,
                    categoriaProducto.getIdCategoriaProducto()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public CategoriaProducto buscar(
            Long idCategoriaProducto
    ) throws SQLException {

        String sql = "SELECT id_categoria_producto, nombre_categoria, descripcion, requiere_caducidad, estado_categoria FROM categoria_producto WHERE id_categoria_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idCategoriaProducto);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirCategoriaProducto(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<CategoriaProducto> listar(
            String criterio
    ) throws SQLException {

        List<CategoriaProducto> lista = new ArrayList<>();
        String sql = "SELECT id_categoria_producto, nombre_categoria, descripcion, requiere_caducidad, estado_categoria FROM categoria_producto WHERE nombre_categoria ILIKE ? OR descripcion ILIKE ? ORDER BY id_categoria_producto";

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
                            convertirCategoriaProducto(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idCategoriaProducto
    ) throws SQLException {

        String sql = "UPDATE categoria_producto SET estado_categoria = FALSE WHERE id_categoria_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idCategoriaProducto);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idCategoriaProducto
    ) throws SQLException {

        String sql = "DELETE FROM categoria_producto WHERE id_categoria_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idCategoriaProducto);

            return sentencia.executeUpdate() > 0;
        }
    }



    private CategoriaProducto convertirCategoriaProducto(
            ResultSet resultado
    ) throws SQLException {

        CategoriaProducto categoriaProducto = new CategoriaProducto();

        categoriaProducto.setIdCategoriaProducto(
                resultado.getLong("id_categoria_producto")
        );

        categoriaProducto.setNombreCategoria(
                resultado.getString("nombre_categoria")
        );

        categoriaProducto.setDescripcion(
                resultado.getString("descripcion")
        );

        categoriaProducto.setRequiereCaducidad(
                resultado.getBoolean("requiere_caducidad")
        );

        categoriaProducto.setEstadoCategoria(
                resultado.getBoolean("estado_categoria")
        );

        return categoriaProducto;
    }
}
