package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.SuministraProducto;

/**
 * DAO para la tabla suministra_producto.
 *
 * @author Usuario
 */
public class SuministraProductoDAO {

    public boolean guardar(
            SuministraProducto suministraProducto
    ) throws SQLException {

        String sql = "INSERT INTO suministra_producto (codigo_producto_proveedor, costo_referencia, tiempo_entrega_dias, cantidad_minima_pedido, fecha_actualizacion, id_proveedor, id_producto) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    suministraProducto.getCodigoProductoProveedor()
            );

            sentencia.setBigDecimal(
                    2,
                    suministraProducto.getCostoReferencia()
            );

            sentencia.setInt(
                    3,
                    suministraProducto.getTiempoEntregaDias()
            );

            sentencia.setInt(
                    4,
                    suministraProducto.getCantidadMinimaPedido()
            );

            if (suministraProducto.getFechaActualizacion() == null) {
            sentencia.setDate(
                    5,
                    null
            );
        } else {
            sentencia.setDate(
                    5,
                    Date.valueOf(
                            suministraProducto.getFechaActualizacion()
                    )
            );
        }

            sentencia.setLong(
                    6,
                    suministraProducto.getIdProveedor()
            );

            sentencia.setLong(
                    7,
                    suministraProducto.getIdProducto()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            SuministraProducto suministraProducto
    ) throws SQLException {

        String sql = "UPDATE suministra_producto SET codigo_producto_proveedor = ?, costo_referencia = ?, tiempo_entrega_dias = ?, cantidad_minima_pedido = ?, fecha_actualizacion = ? WHERE id_proveedor = ? AND id_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    suministraProducto.getCodigoProductoProveedor()
            );

            sentencia.setBigDecimal(
                    2,
                    suministraProducto.getCostoReferencia()
            );

            sentencia.setInt(
                    3,
                    suministraProducto.getTiempoEntregaDias()
            );

            sentencia.setInt(
                    4,
                    suministraProducto.getCantidadMinimaPedido()
            );

            if (suministraProducto.getFechaActualizacion() == null) {
            sentencia.setDate(
                    5,
                    null
            );
        } else {
            sentencia.setDate(
                    5,
                    Date.valueOf(
                            suministraProducto.getFechaActualizacion()
                    )
            );
        }

            sentencia.setLong(
                    6,
                    suministraProducto.getIdProveedor()
            );

            sentencia.setLong(
                    7,
                    suministraProducto.getIdProducto()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public SuministraProducto buscar(
            Long idProveedor,
            Long idProducto
    ) throws SQLException {

        String sql = "SELECT codigo_producto_proveedor, costo_referencia, tiempo_entrega_dias, cantidad_minima_pedido, fecha_actualizacion, id_proveedor, id_producto FROM suministra_producto WHERE id_proveedor = ? AND id_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idProveedor);
            sentencia.setLong(2, idProducto);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirSuministraProducto(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<SuministraProducto> listar(
            String criterio
    ) throws SQLException {

        List<SuministraProducto> lista = new ArrayList<>();
        String sql = "SELECT codigo_producto_proveedor, costo_referencia, tiempo_entrega_dias, cantidad_minima_pedido, fecha_actualizacion, id_proveedor, id_producto FROM suministra_producto WHERE codigo_producto_proveedor ILIKE ? ORDER BY id_proveedor, id_producto";

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
                            convertirSuministraProducto(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idProveedor,
            Long idProducto
    ) throws SQLException {

        String sql = "DELETE FROM suministra_producto WHERE id_proveedor = ? AND id_producto = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idProveedor);
            sentencia.setLong(2, idProducto);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<SuministraProducto> listarPorIdProveedor(
            Long idProveedor
    ) throws SQLException {

        List<SuministraProducto> lista = new ArrayList<>();
        String sql = "SELECT codigo_producto_proveedor, costo_referencia, tiempo_entrega_dias, cantidad_minima_pedido, fecha_actualizacion, id_proveedor, id_producto FROM suministra_producto WHERE id_proveedor = ? ORDER BY id_proveedor, id_producto";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idProveedor
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirSuministraProducto(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<SuministraProducto> listarPorIdProducto(
            Long idProducto
    ) throws SQLException {

        List<SuministraProducto> lista = new ArrayList<>();
        String sql = "SELECT codigo_producto_proveedor, costo_referencia, tiempo_entrega_dias, cantidad_minima_pedido, fecha_actualizacion, id_proveedor, id_producto FROM suministra_producto WHERE id_producto = ? ORDER BY id_proveedor, id_producto";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idProducto
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirSuministraProducto(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private SuministraProducto convertirSuministraProducto(
            ResultSet resultado
    ) throws SQLException {

        SuministraProducto suministraProducto = new SuministraProducto();

        suministraProducto.setCodigoProductoProveedor(
                resultado.getString("codigo_producto_proveedor")
        );

        suministraProducto.setCostoReferencia(
                resultado.getBigDecimal("costo_referencia")
        );

        suministraProducto.setTiempoEntregaDias(
                resultado.getInt("tiempo_entrega_dias")
        );

        suministraProducto.setCantidadMinimaPedido(
                resultado.getInt("cantidad_minima_pedido")
        );

        suministraProducto.setFechaActualizacion(
                resultado.getDate("fecha_actualizacion") == null
                ? null
                : resultado.getDate("fecha_actualizacion").toLocalDate()
        );

        suministraProducto.setIdProveedor(
                resultado.getLong("id_proveedor")
        );

        suministraProducto.setIdProducto(
                resultado.getLong("id_producto")
        );

        return suministraProducto;
    }
}
