package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.DetalleFactura;

/**
 * DAO para la tabla detalle_factura.
 *
 * @author Usuario
 */
public class DetalleFacturaDAO {

    public boolean guardar(
            DetalleFactura detalleFactura
    ) throws SQLException {

        String sql = "INSERT INTO detalle_factura (tipo_concepto, codigo_referencia, cantidad, precio_unitario, porcentaje_impuesto, id_factura) VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    detalleFactura.getTipoConcepto()
            );

            sentencia.setString(
                    2,
                    detalleFactura.getCodigoReferencia()
            );

            sentencia.setInt(
                    3,
                    detalleFactura.getCantidad()
            );

            sentencia.setBigDecimal(
                    4,
                    detalleFactura.getPrecioUnitario()
            );

            sentencia.setBigDecimal(
                    5,
                    detalleFactura.getPorcentajeImpuesto()
            );

            sentencia.setLong(
                    6,
                    detalleFactura.getIdFactura()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            DetalleFactura detalleFactura
    ) throws SQLException {

        String sql = "UPDATE detalle_factura SET tipo_concepto = ?, codigo_referencia = ?, cantidad = ?, precio_unitario = ?, porcentaje_impuesto = ?, id_factura = ? WHERE id_detalle_factura = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    detalleFactura.getTipoConcepto()
            );

            sentencia.setString(
                    2,
                    detalleFactura.getCodigoReferencia()
            );

            sentencia.setInt(
                    3,
                    detalleFactura.getCantidad()
            );

            sentencia.setBigDecimal(
                    4,
                    detalleFactura.getPrecioUnitario()
            );

            sentencia.setBigDecimal(
                    5,
                    detalleFactura.getPorcentajeImpuesto()
            );

            sentencia.setLong(
                    6,
                    detalleFactura.getIdFactura()
            );

            sentencia.setLong(
                    7,
                    detalleFactura.getIdDetalleFactura()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public DetalleFactura buscar(
            Long idDetalleFactura
    ) throws SQLException {

        String sql = "SELECT id_detalle_factura, tipo_concepto, codigo_referencia, cantidad, precio_unitario, porcentaje_impuesto, id_factura FROM detalle_factura WHERE id_detalle_factura = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idDetalleFactura);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirDetalleFactura(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<DetalleFactura> listar(
            String criterio
    ) throws SQLException {

        List<DetalleFactura> lista = new ArrayList<>();
        String sql = "SELECT id_detalle_factura, tipo_concepto, codigo_referencia, cantidad, precio_unitario, porcentaje_impuesto, id_factura FROM detalle_factura WHERE tipo_concepto ILIKE ? OR codigo_referencia ILIKE ? ORDER BY id_detalle_factura";

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
                            convertirDetalleFactura(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idDetalleFactura
    ) throws SQLException {

        String sql = "DELETE FROM detalle_factura WHERE id_detalle_factura = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idDetalleFactura);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<DetalleFactura> listarPorIdFactura(
            Long idFactura
    ) throws SQLException {

        List<DetalleFactura> lista = new ArrayList<>();
        String sql = "SELECT id_detalle_factura, tipo_concepto, codigo_referencia, cantidad, precio_unitario, porcentaje_impuesto, id_factura FROM detalle_factura WHERE id_factura = ? ORDER BY id_detalle_factura";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idFactura
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirDetalleFactura(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private DetalleFactura convertirDetalleFactura(
            ResultSet resultado
    ) throws SQLException {

        DetalleFactura detalleFactura = new DetalleFactura();

        detalleFactura.setIdDetalleFactura(
                resultado.getLong("id_detalle_factura")
        );

        detalleFactura.setTipoConcepto(
                resultado.getString("tipo_concepto")
        );

        detalleFactura.setCodigoReferencia(
                resultado.getString("codigo_referencia")
        );

        detalleFactura.setCantidad(
                resultado.getInt("cantidad")
        );

        detalleFactura.setPrecioUnitario(
                resultado.getBigDecimal("precio_unitario")
        );

        detalleFactura.setPorcentajeImpuesto(
                resultado.getBigDecimal("porcentaje_impuesto")
        );

        detalleFactura.setIdFactura(
                resultado.getLong("id_factura")
        );

        return detalleFactura;
    }
}
