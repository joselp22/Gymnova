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
import modelo.Factura;
import utilidades.GeneradorCodigos;

/**
 * DAO para la tabla factura.
 *
 * @author Usuario
 */
public class FacturaDAO {

    private static final long CLAVE_BLOQUEO_CODIGO
            = GeneradorCodigos.crearClaveBloqueoAdvisory(
                    "factura.numero_factura");

    public boolean guardar(
            Factura factura
    ) throws SQLException {

        String sql = "INSERT INTO factura (numero_factura, fecha_emision, total_descuento, impuesto, estado_factura, id_cliente) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionPostgreSQL.getConexion()) {
            boolean autoCommit = conexion.getAutoCommit();
            conexion.setAutoCommit(false);
            try (PreparedStatement bloqueo = conexion.prepareStatement(
                    "SELECT pg_advisory_xact_lock(?)")) {
                bloqueo.setLong(1, CLAVE_BLOQUEO_CODIGO);
                bloqueo.execute();
            }
            factura.setNumeroFactura(generarSiguienteNumero(conexion));

            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(
                    1,
                    factura.getNumeroFactura()
            );

            if (factura.getFechaEmision() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            factura.getFechaEmision()
                    )
            );
        }

            sentencia.setBigDecimal(
                    3,
                    factura.getTotalDescuento()
            );

            sentencia.setBigDecimal(
                    4,
                    factura.getImpuesto()
            );

            sentencia.setString(
                    5,
                    factura.getEstadoFactura()
            );

            sentencia.setLong(
                    6,
                    factura.getIdCliente()
            );

                boolean guardado = sentencia.executeUpdate() > 0;
                conexion.commit();
                conexion.setAutoCommit(autoCommit);
                return guardado;
            } catch (SQLException | RuntimeException ex) {
                conexion.rollback();
                throw ex;
            }
        }
    }

    private String generarSiguienteNumero(Connection conexion)
            throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(numero_factura "
                + "FROM 5) AS INTEGER)),0)+1 FROM factura "
                + "WHERE numero_factura ~ '^FAC-[0-9]{5}$'";
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            int numero = rs.getInt(1);
            if (numero > 99999) {
                throw new SQLException("Se agotaron los numeros de factura.");
            }
            return "FAC-" + String.format("%05d", numero);
        }
    }

    public boolean modificar(
            Factura factura
    ) throws SQLException {

        String sql = "UPDATE factura SET numero_factura = ?, fecha_emision = ?, total_descuento = ?, impuesto = ?, estado_factura = ?, id_cliente = ? WHERE id_factura = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    factura.getNumeroFactura()
            );

            if (factura.getFechaEmision() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            factura.getFechaEmision()
                    )
            );
        }

            sentencia.setBigDecimal(
                    3,
                    factura.getTotalDescuento()
            );

            sentencia.setBigDecimal(
                    4,
                    factura.getImpuesto()
            );

            sentencia.setString(
                    5,
                    factura.getEstadoFactura()
            );

            sentencia.setLong(
                    6,
                    factura.getIdCliente()
            );

            sentencia.setLong(
                    7,
                    factura.getIdFactura()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Factura buscar(
            Long idFactura
    ) throws SQLException {

        String sql = "SELECT id_factura, numero_factura, fecha_emision, total_descuento, impuesto, estado_factura, id_cliente FROM factura WHERE id_factura = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idFactura);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirFactura(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Factura> listar(
            String criterio
    ) throws SQLException {

        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT id_factura, numero_factura, fecha_emision, total_descuento, impuesto, estado_factura, id_cliente FROM factura WHERE numero_factura ILIKE ? OR CAST(total_descuento AS TEXT) ILIKE ? OR estado_factura ILIKE ? ORDER BY id_factura";

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
                            convertirFactura(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idFactura
    ) throws SQLException {

        String sql = "UPDATE factura SET estado_factura = 'ANULADA' WHERE id_factura = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idFactura);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idFactura
    ) throws SQLException {

        String sql = "DELETE FROM factura WHERE id_factura = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idFactura);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Factura> listarPorIdCliente(
            Long idCliente
    ) throws SQLException {

        List<Factura> lista = new ArrayList<>();
        String sql = "SELECT id_factura, numero_factura, fecha_emision, total_descuento, impuesto, estado_factura, id_cliente FROM factura WHERE id_cliente = ? ORDER BY id_factura";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idCliente
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirFactura(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private Factura convertirFactura(
            ResultSet resultado
    ) throws SQLException {

        Factura factura = new Factura();

        factura.setIdFactura(
                resultado.getLong("id_factura")
        );

        factura.setNumeroFactura(
                resultado.getString("numero_factura")
        );

        factura.setFechaEmision(
                resultado.getDate("fecha_emision") == null
                ? null
                : resultado.getDate("fecha_emision").toLocalDate()
        );

        factura.setTotalDescuento(
                resultado.getBigDecimal("total_descuento")
        );

        factura.setImpuesto(
                resultado.getBigDecimal("impuesto")
        );

        factura.setEstadoFactura(
                resultado.getString("estado_factura")
        );

        factura.setIdCliente(
                resultado.getLong("id_cliente")
        );

        return factura;
    }
}
