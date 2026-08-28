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
import modelo.DetalleCompra;

/**
 * DAO para la tabla detalle_compra.
 *
 * @author Usuario
 */
public class DetalleCompraDAO {

    public boolean guardar(
            DetalleCompra detalleCompra
    ) throws SQLException {

        String sql = "INSERT INTO detalle_compra (cantidad, costo_unitario, porcentaje_descuento, porcentaje_impuesto, fecha_elaboracion, fecha_vencimiento, id_compra, id_producto) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    detalleCompra.getCantidad()
            );

            sentencia.setBigDecimal(
                    2,
                    detalleCompra.getCostoUnitario()
            );

            sentencia.setBigDecimal(
                    3,
                    detalleCompra.getPorcentajeDescuento()
            );

            sentencia.setBigDecimal(
                    4,
                    detalleCompra.getPorcentajeImpuesto()
            );

            if (detalleCompra.getFechaElaboracion() == null) {
            sentencia.setDate(
                    5,
                    null
            );
        } else {
            sentencia.setDate(
                    5,
                    Date.valueOf(
                            detalleCompra.getFechaElaboracion()
                    )
            );
        }

            if (detalleCompra.getFechaVencimiento() == null) {
            sentencia.setDate(
                    6,
                    null
            );
        } else {
            sentencia.setDate(
                    6,
                    Date.valueOf(
                            detalleCompra.getFechaVencimiento()
                    )
            );
        }

            sentencia.setLong(
                    7,
                    detalleCompra.getIdCompra()
            );

            sentencia.setLong(
                    8,
                    detalleCompra.getIdProducto()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            DetalleCompra detalleCompra
    ) throws SQLException {

        String sql = "UPDATE detalle_compra SET cantidad = ?, costo_unitario = ?, porcentaje_descuento = ?, porcentaje_impuesto = ?, fecha_elaboracion = ?, fecha_vencimiento = ?, id_compra = ?, id_producto = ? WHERE id_detalle_compra = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    detalleCompra.getCantidad()
            );

            sentencia.setBigDecimal(
                    2,
                    detalleCompra.getCostoUnitario()
            );

            sentencia.setBigDecimal(
                    3,
                    detalleCompra.getPorcentajeDescuento()
            );

            sentencia.setBigDecimal(
                    4,
                    detalleCompra.getPorcentajeImpuesto()
            );

            if (detalleCompra.getFechaElaboracion() == null) {
            sentencia.setDate(
                    5,
                    null
            );
        } else {
            sentencia.setDate(
                    5,
                    Date.valueOf(
                            detalleCompra.getFechaElaboracion()
                    )
            );
        }

            if (detalleCompra.getFechaVencimiento() == null) {
            sentencia.setDate(
                    6,
                    null
            );
        } else {
            sentencia.setDate(
                    6,
                    Date.valueOf(
                            detalleCompra.getFechaVencimiento()
                    )
            );
        }

            sentencia.setLong(
                    7,
                    detalleCompra.getIdCompra()
            );

            sentencia.setLong(
                    8,
                    detalleCompra.getIdProducto()
            );

            sentencia.setLong(
                    9,
                    detalleCompra.getIdDetalleCompra()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public DetalleCompra buscar(
            Long idDetalleCompra
    ) throws SQLException {

        String sql = "SELECT id_detalle_compra, cantidad, costo_unitario, porcentaje_descuento, porcentaje_impuesto, fecha_elaboracion, fecha_vencimiento, id_compra, id_producto FROM detalle_compra WHERE id_detalle_compra = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idDetalleCompra);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirDetalleCompra(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<DetalleCompra> listar(
            String criterio
    ) throws SQLException {

        List<DetalleCompra> lista = new ArrayList<>();
        String sql = "SELECT id_detalle_compra, cantidad, costo_unitario, porcentaje_descuento, porcentaje_impuesto, fecha_elaboracion, fecha_vencimiento, id_compra, id_producto FROM detalle_compra ORDER BY id_detalle_compra";



        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirDetalleCompra(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idDetalleCompra
    ) throws SQLException {

        String sql = "DELETE FROM detalle_compra WHERE id_detalle_compra = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idDetalleCompra);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<DetalleCompra> listarPorIdCompra(
            Long idCompra
    ) throws SQLException {

        List<DetalleCompra> lista = new ArrayList<>();
        String sql = "SELECT id_detalle_compra, cantidad, costo_unitario, porcentaje_descuento, porcentaje_impuesto, fecha_elaboracion, fecha_vencimiento, id_compra, id_producto FROM detalle_compra WHERE id_compra = ? ORDER BY id_detalle_compra";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idCompra
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirDetalleCompra(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<DetalleCompra> listarPorIdProducto(
            Long idProducto
    ) throws SQLException {

        List<DetalleCompra> lista = new ArrayList<>();
        String sql = "SELECT id_detalle_compra, cantidad, costo_unitario, porcentaje_descuento, porcentaje_impuesto, fecha_elaboracion, fecha_vencimiento, id_compra, id_producto FROM detalle_compra WHERE id_producto = ? ORDER BY id_detalle_compra";

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
                            convertirDetalleCompra(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private DetalleCompra convertirDetalleCompra(
            ResultSet resultado
    ) throws SQLException {

        DetalleCompra detalleCompra = new DetalleCompra();

        detalleCompra.setIdDetalleCompra(
                resultado.getLong("id_detalle_compra")
        );

        detalleCompra.setCantidad(
                resultado.getInt("cantidad")
        );

        detalleCompra.setCostoUnitario(
                resultado.getBigDecimal("costo_unitario")
        );

        detalleCompra.setPorcentajeDescuento(
                resultado.getBigDecimal("porcentaje_descuento")
        );

        detalleCompra.setPorcentajeImpuesto(
                resultado.getBigDecimal("porcentaje_impuesto")
        );

        detalleCompra.setFechaElaboracion(
                resultado.getDate("fecha_elaboracion") == null
                ? null
                : resultado.getDate("fecha_elaboracion").toLocalDate()
        );

        detalleCompra.setFechaVencimiento(
                resultado.getDate("fecha_vencimiento") == null
                ? null
                : resultado.getDate("fecha_vencimiento").toLocalDate()
        );

        detalleCompra.setIdCompra(
                resultado.getLong("id_compra")
        );

        detalleCompra.setIdProducto(
                resultado.getLong("id_producto")
        );

        return detalleCompra;
    }
}
