package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import modelo.Pago;
import utilidades.GeneradorCodigos;

/**
 * DAO para la tabla pago.
 *
 * @author Usuario
 */
public class PagoDAO {

    private static final String PREFIJO_CODIGO
            = GeneradorCodigos.PREFIJO_PAGO;
    private static final long CLAVE_BLOQUEO_CODIGO
            = GeneradorCodigos.crearClaveBloqueoAdvisory(
                    "pago.codigo_pago"
            );

    public boolean guardar(
            Pago pago
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

            pago.setCodigoPago(
                    generarSiguienteCodigo(
                            conexion
                    )
            );

            boolean guardado = insertar(
                    conexion,
                    pago
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
            Pago pago
    ) throws SQLException {

        String sql = "INSERT INTO pago (monto_recibido, monto_pago, codigo_pago, fecha_hora_pago, estado_pago, referencia_transaccion, id_factura, id_metodo_pago) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setBigDecimal(
                    1,
                    pago.getMontoRecibido()
            );

            sentencia.setBigDecimal(
                    2,
                    pago.getMontoPago()
            );

            sentencia.setString(
                    3,
                    pago.getCodigoPago()
            );

            if (pago.getFechaHoraPago() == null) {
            sentencia.setTimestamp(
                    4,
                    null
            );
        } else {
            sentencia.setTimestamp(
                    4,
                    Timestamp.valueOf(
                            pago.getFechaHoraPago()
                    )
            );
        }

            sentencia.setString(
                    5,
                    pago.getEstadoPago()
            );

            sentencia.setString(
                    6,
                    pago.getReferenciaTransaccion()
            );

            sentencia.setLong(
                    7,
                    pago.getIdFactura()
            );

            sentencia.setLong(
                    8,
                    pago.getIdMetodoPago()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Pago pago
    ) throws SQLException {

        String sql = "UPDATE pago SET monto_recibido = ?, monto_pago = ?, fecha_hora_pago = ?, estado_pago = ?, referencia_transaccion = ?, id_factura = ?, id_metodo_pago = ? WHERE id_pago = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setBigDecimal(
                    1,
                    pago.getMontoRecibido()
            );

            sentencia.setBigDecimal(
                    2,
                    pago.getMontoPago()
            );

            if (pago.getFechaHoraPago() == null) {
            sentencia.setTimestamp(
                    3,
                    null
            );
        } else {
            sentencia.setTimestamp(
                    3,
                    Timestamp.valueOf(
                            pago.getFechaHoraPago()
                    )
            );
        }

            sentencia.setString(
                    4,
                    pago.getEstadoPago()
            );

            sentencia.setString(
                    5,
                    pago.getReferenciaTransaccion()
            );

            sentencia.setLong(
                    6,
                    pago.getIdFactura()
            );

            sentencia.setLong(
                    7,
                    pago.getIdMetodoPago()
            );

            sentencia.setLong(
                    8,
                    pago.getIdPago()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Pago buscar(
            Long idPago
    ) throws SQLException {

        String sql = "SELECT id_pago, monto_recibido, monto_pago, codigo_pago, fecha_hora_pago, estado_pago, referencia_transaccion, id_factura, id_metodo_pago FROM pago WHERE id_pago = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idPago);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirPago(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Pago> listar(
            String criterio
    ) throws SQLException {

        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT id_pago, monto_recibido, monto_pago, codigo_pago, fecha_hora_pago, estado_pago, referencia_transaccion, id_factura, id_metodo_pago FROM pago WHERE codigo_pago ILIKE ? OR estado_pago ILIKE ? OR referencia_transaccion ILIKE ? ORDER BY id_pago";

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
                            convertirPago(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idPago
    ) throws SQLException {

        String sql = "UPDATE pago SET estado_pago = 'ANULADO' WHERE id_pago = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idPago);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idPago
    ) throws SQLException {

        String sql = "DELETE FROM pago WHERE id_pago = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idPago);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Pago> listarPorIdFactura(
            Long idFactura
    ) throws SQLException {

        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT id_pago, monto_recibido, monto_pago, codigo_pago, fecha_hora_pago, estado_pago, referencia_transaccion, id_factura, id_metodo_pago FROM pago WHERE id_factura = ? ORDER BY id_pago";

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
                            convertirPago(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<Pago> listarPorIdMetodoPago(
            Long idMetodoPago
    ) throws SQLException {

        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT id_pago, monto_recibido, monto_pago, codigo_pago, fecha_hora_pago, estado_pago, referencia_transaccion, id_factura, id_metodo_pago FROM pago WHERE id_metodo_pago = ? ORDER BY id_pago";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idMetodoPago
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirPago(
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

        String sql = "SELECT codigo_pago FROM pago "
                + "WHERE codigo_pago ~ ? "
                + "ORDER BY CAST(SUBSTRING(codigo_pago FROM 3) AS INTEGER) DESC "
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
                                    "codigo_pago"
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

    private Pago convertirPago(
            ResultSet resultado
    ) throws SQLException {

        Pago pago = new Pago();

        pago.setIdPago(
                resultado.getLong("id_pago")
        );

        pago.setMontoRecibido(
                resultado.getBigDecimal("monto_recibido")
        );

        pago.setMontoPago(
                resultado.getBigDecimal("monto_pago")
        );

        pago.setCodigoPago(
                resultado.getString("codigo_pago")
        );

        pago.setFechaHoraPago(
                resultado.getTimestamp("fecha_hora_pago") == null
                ? null
                : resultado.getTimestamp("fecha_hora_pago").toLocalDateTime()
        );

        pago.setEstadoPago(
                resultado.getString("estado_pago")
        );

        pago.setReferenciaTransaccion(
                resultado.getString("referencia_transaccion")
        );

        pago.setIdFactura(
                resultado.getLong("id_factura")
        );

        pago.setIdMetodoPago(
                resultado.getLong("id_metodo_pago")
        );

        return pago;
    }
}
