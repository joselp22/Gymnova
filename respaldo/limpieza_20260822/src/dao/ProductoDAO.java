package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Producto;
import utilidades.GeneradorCodigos;

/**
 * DAO para la tabla producto.
 *
 * @author Usuario
 */
public class ProductoDAO {

    private static final String PREFIJO_CODIGO
            = GeneradorCodigos.PREFIJO_PRODUCTO;
    private static final long CLAVE_BLOQUEO_CODIGO
            = GeneradorCodigos.crearClaveBloqueoAdvisory(
                    "producto.codigo_producto"
            );

    public boolean guardar(
            Producto producto
    ) throws SQLException {

        Connection conexion = null;
        boolean autoCommitOriginal = true;

        try {
            conexion = ConexionPostgreSQL.getConexion();
            autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            bloquearGeneracionCodigo(
                    conexion
            );

            producto.setCodigoProducto(
                    generarSiguienteCodigo(
                            conexion
                    )
            );

            boolean guardado = insertar(
                    conexion,
                    producto
            );

            conexion.commit();
            return guardado;

        } catch (SQLException | RuntimeException e) {

            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }

            throw e;

        } finally {

            if (conexion != null) {
                try {
                    conexion.setAutoCommit(
                            autoCommitOriginal
                    );
                } finally {
                    conexion.close();
                }
            }
        }
    }

    private boolean insertar(
            Connection conexion,
            Producto producto
    ) throws SQLException {

        String sql = "INSERT INTO producto (codigo_producto, codigo_barras, nombre_producto, marca, unidad_medida, precio_compra, precio_venta, margen_ganancia, imagen_producto, stock_actual, stock_minimo, estado_producto, id_categoria_producto) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    producto.getCodigoProducto()
            );

            sentencia.setString(
                    2,
                    producto.getCodigoBarras()
            );

            sentencia.setString(
                    3,
                    producto.getNombreProducto()
            );

            sentencia.setString(
                    4,
                    producto.getMarca()
            );

            sentencia.setString(
                    5,
                    producto.getUnidadMedida()
            );

            sentencia.setBigDecimal(
                    6,
                    producto.getPrecioCompra()
            );

            sentencia.setBigDecimal(
                    7,
                    producto.getPrecioVenta()
            );

            sentencia.setBigDecimal(
                    8,
                    producto.getMargenGanancia()
            );

            sentencia.setBytes(
                    9,
                    producto.getImagenProducto()
            );

            sentencia.setInt(
                    10,
                    producto.getStockActual()
            );

            sentencia.setInt(
                    11,
                    producto.getStockMinimo()
            );

            sentencia.setBoolean(
                    12,
                    producto.isEstadoProducto()
            );

            sentencia.setLong(
                    13,
                    producto.getIdCategoriaProducto()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Producto producto
    ) throws SQLException {

        String sql = "UPDATE producto SET codigo_barras = ?, nombre_producto = ?, marca = ?, unidad_medida = ?, precio_compra = ?, precio_venta = ?, margen_ganancia = ?, imagen_producto = ?, stock_actual = ?, stock_minimo = ?, estado_producto = ?, id_categoria_producto = ? WHERE id_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    producto.getCodigoBarras()
            );

            sentencia.setString(
                    2,
                    producto.getNombreProducto()
            );

            sentencia.setString(
                    3,
                    producto.getMarca()
            );

            sentencia.setString(
                    4,
                    producto.getUnidadMedida()
            );

            sentencia.setBigDecimal(
                    5,
                    producto.getPrecioCompra()
            );

            sentencia.setBigDecimal(
                    6,
                    producto.getPrecioVenta()
            );

            sentencia.setBigDecimal(
                    7,
                    producto.getMargenGanancia()
            );

            sentencia.setBytes(
                    8,
                    producto.getImagenProducto()
            );

            sentencia.setInt(
                    9,
                    producto.getStockActual()
            );

            sentencia.setInt(
                    10,
                    producto.getStockMinimo()
            );

            sentencia.setBoolean(
                    11,
                    producto.isEstadoProducto()
            );

            sentencia.setLong(
                    12,
                    producto.getIdCategoriaProducto()
            );

            sentencia.setLong(
                    13,
                    producto.getIdProducto()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Producto buscar(
            Long idProducto
    ) throws SQLException {

        String sql = "SELECT codigo_producto, id_producto, codigo_barras, nombre_producto, marca, unidad_medida, precio_compra, precio_venta, margen_ganancia, imagen_producto, stock_actual, stock_minimo, estado_producto, id_categoria_producto FROM producto WHERE id_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idProducto);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirProducto(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Producto> listar(
            String criterio
    ) throws SQLException {

        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT codigo_producto, id_producto, codigo_barras, nombre_producto, marca, unidad_medida, precio_compra, precio_venta, margen_ganancia, imagen_producto, stock_actual, stock_minimo, estado_producto, id_categoria_producto FROM producto WHERE codigo_producto ILIKE ? OR codigo_barras ILIKE ? OR nombre_producto ILIKE ? OR marca ILIKE ? OR unidad_medida ILIKE ? ORDER BY id_producto";

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
            sentencia.setString(5, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirProducto(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idProducto
    ) throws SQLException {

        String sql = "UPDATE producto SET estado_producto = FALSE WHERE id_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idProducto);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idProducto
    ) throws SQLException {

        String sql = "DELETE FROM producto WHERE id_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idProducto);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Producto> listarPorIdCategoriaProducto(
            Long idCategoriaProducto
    ) throws SQLException {

        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT codigo_producto, id_producto, codigo_barras, nombre_producto, marca, unidad_medida, precio_compra, precio_venta, margen_ganancia, imagen_producto, stock_actual, stock_minimo, estado_producto, id_categoria_producto FROM producto WHERE id_categoria_producto = ? ORDER BY id_producto";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idCategoriaProducto
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirProducto(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private void bloquearGeneracionCodigo(
            Connection conexion
    ) throws SQLException {

        String sql = "SELECT pg_advisory_xact_lock(?)";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    CLAVE_BLOQUEO_CODIGO
            );

            sentencia.execute();
        }
    }

    private String generarSiguienteCodigo(
            Connection conexion
    ) throws SQLException {

        String sql = "SELECT codigo_producto FROM producto "
                + "WHERE codigo_producto ~ ? "
                + "ORDER BY CAST(SUBSTRING(codigo_producto FROM 3) AS INTEGER) DESC "
                + "LIMIT 1";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    GeneradorCodigos.crearPatronPostgreSQL(
                            PREFIJO_CODIGO
                    )
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return GeneradorCodigos.generarSiguienteCodigo(
                            PREFIJO_CODIGO,
                            resultado.getString(
                                    "codigo_producto"
                            )
                    );
                }
            }
        }

        return GeneradorCodigos.construirCodigo(
                PREFIJO_CODIGO,
                GeneradorCodigos.VALOR_INICIAL
        );
    }

    private Producto convertirProducto(
            ResultSet resultado
    ) throws SQLException {

        Producto producto = new Producto();

        producto.setCodigoProducto(
                resultado.getString("codigo_producto")
        );

        producto.setIdProducto(
                resultado.getLong("id_producto")
        );

        producto.setCodigoBarras(
                resultado.getString("codigo_barras")
        );

        producto.setNombreProducto(
                resultado.getString("nombre_producto")
        );

        producto.setMarca(
                resultado.getString("marca")
        );

        producto.setUnidadMedida(
                resultado.getString("unidad_medida")
        );

        producto.setPrecioCompra(
                resultado.getBigDecimal("precio_compra")
        );

        producto.setPrecioVenta(
                resultado.getBigDecimal("precio_venta")
        );

        producto.setMargenGanancia(
                resultado.getBigDecimal("margen_ganancia")
        );

        producto.setImagenProducto(
                resultado.getBytes("imagen_producto")
        );

        producto.setStockActual(
                resultado.getInt("stock_actual")
        );

        producto.setStockMinimo(
                resultado.getInt("stock_minimo")
        );

        producto.setEstadoProducto(
                resultado.getBoolean("estado_producto")
        );

        producto.setIdCategoriaProducto(
                resultado.getLong("id_categoria_producto")
        );

        return producto;
    }
}
