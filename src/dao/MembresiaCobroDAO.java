package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import modelo.MembresiaCobroResumen;

/** Consultas de lectura para la nueva vista de cobro/renovacion. */
public class MembresiaCobroDAO {

    public List<MembresiaCobroResumen> listarPorCliente(Long idCliente)
            throws SQLException {
        List<MembresiaCobroResumen> filas = new ArrayList<>();
        if (idCliente == null) {
            return filas;
        }

        String sql = "SELECT m.id_membresia, m.numero_membresia, tm.nombre AS plan, "
                + "m.fecha_inicio, m.fecha_fin, m.estado_membresia, m.costo_final, "
                + "f.numero_factura, p.codigo_pago, p.fecha_hora_pago, "
                + "mp.nombre_metodo, p.referencia_transaccion, p.monto_pago "
                + "FROM membresia m "
                + "JOIN tipo_membresia tm ON tm.id_tipo_membresia = m.id_tipo_membresia "
                + "LEFT JOIN LATERAL (SELECT ff.id_factura, ff.numero_factura "
                + " FROM detalle_factura d JOIN factura ff ON ff.id_factura = d.id_factura "
                + " WHERE d.tipo_concepto = 'MEMBRESIA' "
                + " AND d.codigo_referencia = m.numero_membresia "
                + " ORDER BY ff.id_factura DESC LIMIT 1) f ON TRUE "
                + "LEFT JOIN LATERAL (SELECT pp.codigo_pago, pp.fecha_hora_pago, "
                + " pp.referencia_transaccion, pp.monto_pago, pp.id_metodo_pago "
                + " FROM pago pp WHERE pp.id_factura = f.id_factura "
                + " AND pp.estado_pago = 'CONFIRMADO' "
                + " ORDER BY pp.fecha_hora_pago DESC, pp.id_pago DESC LIMIT 1) p ON TRUE "
                + "LEFT JOIN metodo_pago mp ON mp.id_metodo_pago = p.id_metodo_pago "
                + "WHERE m.id_cliente = ? "
                + "ORDER BY m.fecha_inicio DESC, m.id_membresia DESC";

        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idCliente);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    MembresiaCobroResumen fila = new MembresiaCobroResumen();
                    fila.setIdMembresia(r.getLong("id_membresia"));
                    fila.setNumeroMembresia(r.getString("numero_membresia"));
                    fila.setNombrePlan(r.getString("plan"));
                    Date inicio = r.getDate("fecha_inicio");
                    Date fin = r.getDate("fecha_fin");
                    fila.setFechaInicio(inicio == null ? null : inicio.toLocalDate());
                    fila.setFechaFin(fin == null ? null : fin.toLocalDate());
                    fila.setEstadoMembresia(r.getString("estado_membresia"));
                    fila.setCostoFinal(r.getBigDecimal("costo_final"));
                    fila.setNumeroFactura(r.getString("numero_factura"));
                    fila.setCodigoPago(r.getString("codigo_pago"));
                    Timestamp pago = r.getTimestamp("fecha_hora_pago");
                    fila.setFechaHoraPago(pago == null ? null : pago.toLocalDateTime());
                    fila.setMetodoPago(r.getString("nombre_metodo"));
                    fila.setReferenciaTransaccion(r.getString("referencia_transaccion"));
                    fila.setTotalCobrado(r.getBigDecimal("monto_pago"));
                    filas.add(fila);
                }
            }
        }
        return filas;
    }
}
