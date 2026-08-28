package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Compra;

/**
 * DAO para la tabla compra.
 *
 * @author Usuario
 */
public class CompraDAO {

    public boolean guardar(
            Compra compra
    ) throws SQLException {

        String sql = "INSERT INTO compra (numero_compra, numero_factura_proveedor, fecha_compra, fecha_recepcion, tipo_pago, estado_compra, id_proveedor, id_empleado) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    compra.getNumeroCompra()
            );

            sentencia.setString(
                    2,
                    compra.getNumeroFacturaProveedor()
            );

            if (compra.getFechaCompra() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            compra.getFechaCompra()
                    )
            );
        }

            if (compra.getFechaRecepcion() == null) {
            sentencia.setDate(
                    4,
                    null
            );
        } else {
            sentencia.setDate(
                    4,
                    Date.valueOf(
                            compra.getFechaRecepcion()
                    )
            );
        }

            sentencia.setString(
                    5,
                    compra.getTipoPago()
            );

            sentencia.setString(
                    6,
                    compra.getEstadoCompra()
            );

            sentencia.setLong(
                    7,
                    compra.getIdProveedor()
            );

            sentencia.setLong(
                    8,
                    compra.getIdEmpleado()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Compra compra
    ) throws SQLException {

        String sql = "UPDATE compra SET numero_compra = ?, numero_factura_proveedor = ?, fecha_compra = ?, fecha_recepcion = ?, tipo_pago = ?, estado_compra = ?, id_proveedor = ?, id_empleado = ? WHERE id_compra = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    compra.getNumeroCompra()
            );

            sentencia.setString(
                    2,
                    compra.getNumeroFacturaProveedor()
            );

            if (compra.getFechaCompra() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            compra.getFechaCompra()
                    )
            );
        }

            if (compra.getFechaRecepcion() == null) {
            sentencia.setDate(
                    4,
                    null
            );
        } else {
            sentencia.setDate(
                    4,
                    Date.valueOf(
                            compra.getFechaRecepcion()
                    )
            );
        }

            sentencia.setString(
                    5,
                    compra.getTipoPago()
            );

            sentencia.setString(
                    6,
                    compra.getEstadoCompra()
            );

            sentencia.setLong(
                    7,
                    compra.getIdProveedor()
            );

            sentencia.setLong(
                    8,
                    compra.getIdEmpleado()
            );

            sentencia.setLong(
                    9,
                    compra.getIdCompra()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Compra buscar(
            Long idCompra
    ) throws SQLException {

        String sql = "SELECT id_compra, numero_compra, numero_factura_proveedor, fecha_compra, fecha_recepcion, tipo_pago, estado_compra, id_proveedor, id_empleado FROM compra WHERE id_compra = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idCompra);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirCompra(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Compra> listar(
            String criterio
    ) throws SQLException {

        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT id_compra, numero_compra, numero_factura_proveedor, fecha_compra, fecha_recepcion, tipo_pago, estado_compra, id_proveedor, id_empleado FROM compra WHERE numero_compra ILIKE ? OR numero_factura_proveedor ILIKE ? OR tipo_pago ILIKE ? OR estado_compra ILIKE ? ORDER BY id_compra";

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

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirCompra(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idCompra
    ) throws SQLException {

        String sql = "UPDATE compra SET estado_compra = 'ANULADA' WHERE id_compra = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idCompra);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idCompra
    ) throws SQLException {

        String sql = "DELETE FROM compra WHERE id_compra = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idCompra);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Compra> listarPorIdProveedor(
            Long idProveedor
    ) throws SQLException {

        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT id_compra, numero_compra, numero_factura_proveedor, fecha_compra, fecha_recepcion, tipo_pago, estado_compra, id_proveedor, id_empleado FROM compra WHERE id_proveedor = ? ORDER BY id_compra";

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
                            convertirCompra(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<Compra> listarPorIdEmpleado(
            Long idEmpleado
    ) throws SQLException {

        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT id_compra, numero_compra, numero_factura_proveedor, fecha_compra, fecha_recepcion, tipo_pago, estado_compra, id_proveedor, id_empleado FROM compra WHERE id_empleado = ? ORDER BY id_compra";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEmpleado
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirCompra(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private Compra convertirCompra(
            ResultSet resultado
    ) throws SQLException {

        Compra compra = new Compra();

        compra.setIdCompra(
                resultado.getLong("id_compra")
        );

        compra.setNumeroCompra(
                resultado.getString("numero_compra")
        );

        compra.setNumeroFacturaProveedor(
                resultado.getString("numero_factura_proveedor")
        );

        compra.setFechaCompra(
                resultado.getDate("fecha_compra") == null
                ? null
                : resultado.getDate("fecha_compra").toLocalDate()
        );

        compra.setFechaRecepcion(
                resultado.getDate("fecha_recepcion") == null
                ? null
                : resultado.getDate("fecha_recepcion").toLocalDate()
        );

        compra.setTipoPago(
                resultado.getString("tipo_pago")
        );

        compra.setEstadoCompra(
                resultado.getString("estado_compra")
        );

        compra.setIdProveedor(
                resultado.getLong("id_proveedor")
        );

        compra.setIdEmpleado(
                resultado.getLong("id_empleado")
        );

        return compra;
    }
}
