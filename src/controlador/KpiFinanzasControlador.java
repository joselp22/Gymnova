package controlador;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas agregadas para el dashboard financiero.
 * Todas son de solo lectura y se alimentan de pagos CONFIRMADOS.
 */
public class KpiFinanzasControlador {

    public static final class Kpis {
        public long membresiasActivas;
        public BigDecimal ingresosMes = BigDecimal.ZERO;
        public BigDecimal ingresosAno = BigDecimal.ZERO;
        public BigDecimal ingresosMembresiasMes = BigDecimal.ZERO;
        public long cobrosMembresiasMes;
        public long facturasPendientes;
        public BigDecimal montoPorCobrar = BigDecimal.ZERO;
    }

    public static final class DesglosePlan {
        public final String nombrePlan;
        public final long activas;
        public final BigDecimal ingresosMes;

        public DesglosePlan(String nombrePlan, long activas,
                BigDecimal ingresosMes) {
            this.nombrePlan = nombrePlan;
            this.activas = activas;
            this.ingresosMes = ingresosMes == null
                    ? BigDecimal.ZERO : ingresosMes;
        }
    }

    public static final class CobroMembresiaReciente {
        public final LocalDateTime fechaHora;
        public final String cliente;
        public final String plan;
        public final String numeroMembresia;
        public final String metodoPago;
        public final BigDecimal total;

        public CobroMembresiaReciente(LocalDateTime fechaHora,
                String cliente, String plan, String numeroMembresia,
                String metodoPago, BigDecimal total) {
            this.fechaHora = fechaHora;
            this.cliente = cliente;
            this.plan = plan;
            this.numeroMembresia = numeroMembresia;
            this.metodoPago = metodoPago;
            this.total = total == null ? BigDecimal.ZERO : total;
        }
    }

    private String mensaje = "";

    public String getMensaje() {
        return mensaje;
    }

    public Kpis obtenerKpis() {
        mensaje = "";
        Kpis kpis = new Kpis();
        try (Connection c = ConexionPostgreSQL.getConexion()) {

            try (PreparedStatement s = c.prepareStatement(
                    "SELECT COUNT(*) FROM membresia "
                    + "WHERE estado_membresia = 'ACTIVA' "
                    + "AND CURRENT_DATE BETWEEN fecha_inicio AND fecha_fin");
                 ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    kpis.membresiasActivas = r.getLong(1);
                }
            }

            try (PreparedStatement s = c.prepareStatement(
                    "SELECT COALESCE(SUM(monto_pago), 0) FROM pago "
                    + "WHERE estado_pago = 'CONFIRMADO' "
                    + "AND date_trunc('month', fecha_hora_pago) "
                    + "= date_trunc('month', CURRENT_DATE)");
                 ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    kpis.ingresosMes = r.getBigDecimal(1);
                }
            }

            try (PreparedStatement s = c.prepareStatement(
                    "SELECT COALESCE(SUM(monto_pago), 0) FROM pago "
                    + "WHERE estado_pago = 'CONFIRMADO' "
                    + "AND date_trunc('year', fecha_hora_pago) "
                    + "= date_trunc('year', CURRENT_DATE)");
                 ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    kpis.ingresosAno = r.getBigDecimal(1);
                }
            }

            // Ingreso especificamente originado por membresias/renovaciones.
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT COUNT(*), COALESCE(SUM(p.monto_pago), 0) "
                    + "FROM pago p "
                    + "WHERE p.estado_pago = 'CONFIRMADO' "
                    + "AND date_trunc('month', p.fecha_hora_pago) "
                    + "= date_trunc('month', CURRENT_DATE) "
                    + "AND EXISTS (SELECT 1 FROM detalle_factura d "
                    + "WHERE d.id_factura = p.id_factura "
                    + "AND d.tipo_concepto = 'MEMBRESIA')");
                 ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    kpis.cobrosMembresiasMes = r.getLong(1);
                    kpis.ingresosMembresiasMes = r.getBigDecimal(2);
                }
            }

            try (PreparedStatement s = c.prepareStatement(
                    "WITH totales AS ("
                    + " SELECT f.id_factura, "
                    + " COALESCE(SUM(d.cantidad * d.precio_unitario "
                    + " * (1 + d.porcentaje_impuesto/100.0)), 0) "
                    + " - COALESCE(f.total_descuento, 0) AS total, "
                    + " COALESCE((SELECT SUM(p.monto_pago) FROM pago p "
                    + " WHERE p.id_factura = f.id_factura "
                    + " AND p.estado_pago = 'CONFIRMADO'), 0) AS pagado "
                    + " FROM factura f LEFT JOIN detalle_factura d "
                    + " ON d.id_factura = f.id_factura "
                    + " WHERE f.estado_factura IN ('EMITIDA','PENDIENTE') "
                    + " GROUP BY f.id_factura, f.total_descuento"
                    + ") SELECT COUNT(*) FILTER (WHERE total - pagado > 0), "
                    + "COALESCE(SUM(GREATEST(total - pagado, 0)), 0) "
                    + "FROM totales");
                 ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    kpis.facturasPendientes = r.getLong(1);
                    kpis.montoPorCobrar = r.getBigDecimal(2);
                }
            }

        } catch (SQLException ex) {
            mensaje = "No se pudieron calcular los KPIs: " + ex.getMessage();
        }
        return kpis;
    }

    public List<DesglosePlan> obtenerDesglosePorPlan() {
        mensaje = "";
        List<DesglosePlan> filas = new ArrayList<>();
        String sql
                = "SELECT tm.nombre, "
                + "COALESCE(SUM(CASE WHEN m.estado_membresia = 'ACTIVA' "
                + "AND CURRENT_DATE BETWEEN m.fecha_inicio AND m.fecha_fin "
                + "THEN 1 ELSE 0 END), 0) AS activas, "
                + "COALESCE((SELECT SUM(p.monto_pago) FROM pago p "
                + "JOIN detalle_factura d ON d.id_factura = p.id_factura "
                + "JOIN membresia mm ON mm.numero_membresia = d.codigo_referencia "
                + "AND d.tipo_concepto = 'MEMBRESIA' "
                + "WHERE p.estado_pago = 'CONFIRMADO' "
                + "AND date_trunc('month', p.fecha_hora_pago) "
                + "= date_trunc('month', CURRENT_DATE) "
                + "AND mm.id_tipo_membresia = tm.id_tipo_membresia), 0) "
                + "AS ingresos_mes "
                + "FROM tipo_membresia tm "
                + "LEFT JOIN membresia m ON m.id_tipo_membresia = tm.id_tipo_membresia "
                + "WHERE tm.estado_tipo = TRUE "
                + "GROUP BY tm.id_tipo_membresia, tm.nombre "
                + "ORDER BY tm.duracion_dias";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            while (r.next()) {
                filas.add(new DesglosePlan(
                        r.getString("nombre"),
                        r.getLong("activas"),
                        r.getBigDecimal("ingresos_mes")
                ));
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo cargar el desglose por plan: "
                    + ex.getMessage();
        }
        return filas;
    }

    public List<CobroMembresiaReciente> listarUltimosCobrosMembresia(int limite) {
        mensaje = "";
        List<CobroMembresiaReciente> filas = new ArrayList<>();
        int max = limite <= 0 ? 10 : Math.min(limite, 50);
        String sql = "SELECT p.fecha_hora_pago, "
                + "trim(per.nombres || ' ' || per.apellidos) AS cliente, "
                + "tm.nombre AS plan, m.numero_membresia, "
                + "mp.nombre_metodo, p.monto_pago "
                + "FROM pago p "
                + "JOIN factura f ON f.id_factura = p.id_factura "
                + "JOIN detalle_factura d ON d.id_factura = f.id_factura "
                + "AND d.tipo_concepto = 'MEMBRESIA' "
                + "JOIN membresia m ON m.numero_membresia = d.codigo_referencia "
                + "JOIN tipo_membresia tm "
                + "ON tm.id_tipo_membresia = m.id_tipo_membresia "
                + "JOIN persona per ON per.id_persona = f.id_cliente "
                + "JOIN metodo_pago mp ON mp.id_metodo_pago = p.id_metodo_pago "
                + "WHERE p.estado_pago = 'CONFIRMADO' "
                + "ORDER BY p.fecha_hora_pago DESC, p.id_pago DESC LIMIT ?";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, max);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    java.sql.Timestamp ts = r.getTimestamp("fecha_hora_pago");
                    filas.add(new CobroMembresiaReciente(
                            ts == null ? null : ts.toLocalDateTime(),
                            r.getString("cliente"),
                            r.getString("plan"),
                            r.getString("numero_membresia"),
                            r.getString("nombre_metodo"),
                            r.getBigDecimal("monto_pago")
                    ));
                }
            }
        } catch (SQLException ex) {
            mensaje = "No se pudieron cargar los cobros recientes: "
                    + ex.getMessage();
        }
        return filas;
    }
}
