package vista;

import conexion.ConexionPostgreSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Reporte financiero imprimible: ingresos día a día del mes, distribución
 * por método de pago, facturas pendientes de cobro y comparación mes
 * actual vs mes anterior.
 */
public final class DlgReporteFinanciero extends JDialog {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color AZUL = new Color(8, 124, 255);

    private final JLabel lblMesActual = kpi();
    private final JLabel lblMesAnterior = kpi();
    private final JLabel lblVariacion = kpi();
    private final JLabel lblPendiente = kpi();

    private final JTable tblDiario = new JTable();
    private final JTable tblMetodos = new JTable();
    private final JTable tblPendientes = new JTable();
    private final JTable tblComprobantes = new JTable();

    public DlgReporteFinanciero(Frame padre) {
        super(padre, "Reporte financiero", true);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(FONDO);
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);

        setSize(new Dimension(1000, 640));
        setLocationRelativeTo(padre);
        cargarDatos();
    }

    private JPanel construirEncabezado() {
        JPanel enc = new JPanel(new BorderLayout(0, 6));
        enc.setBackground(TARJETA);
        enc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JLabel titulo = new JLabel("GYMNOVA · Reporte financiero");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(TITULO);
        enc.add(titulo, BorderLayout.NORTH);

        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setOpaque(false);
        kpis.add(tarjetaKPI("Ingresos mes actual", lblMesActual));
        kpis.add(tarjetaKPI("Ingresos mes anterior", lblMesAnterior));
        kpis.add(tarjetaKPI("Variación", lblVariacion));
        kpis.add(tarjetaKPI("Facturas pendientes", lblPendiente));
        enc.add(kpis, BorderLayout.CENTER);
        return enc;
    }

    private JTabbedPane construirCentro() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ingresos día a día (mes actual)", wrap(tblDiario));
        tabs.addTab("Método de pago", wrap(tblMetodos));
        tabs.addTab("Facturas pendientes de cobro", wrap(tblPendientes));
        tabs.addTab("Comprobantes emitidos vs enviados", wrap(tblComprobantes));
        return tabs;
    }

    private JScrollPane wrap(JTable t) {
        t.setRowHeight(26);
        t.getTableHeader().setBackground(new Color(10, 58, 108));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setReorderingAllowed(false);
        return new JScrollPane(t);
    }

    private JPanel construirPie() {
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pie.setBackground(TARJETA);
        pie.setBorder(BorderFactory.createLineBorder(BORDE));
        JButton btnImp = boton("Imprimir todo");
        btnImp.addActionListener(e -> imprimirTodo());
        JButton btnClose = new JButton("Cerrar");
        btnClose.addActionListener(e -> dispose());
        pie.add(btnImp);
        pie.add(btnClose);
        return pie;
    }

    private void cargarDatos() {
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            BigDecimal actual = queryDec(c,
                    "SELECT COALESCE(SUM(monto_pago), 0) FROM pago "
                    + "WHERE DATE_TRUNC('month', fecha_hora_pago) = "
                    + "  DATE_TRUNC('month', CURRENT_DATE) "
                    + "AND estado_pago = 'CONFIRMADO'");
            BigDecimal anterior = queryDec(c,
                    "SELECT COALESCE(SUM(monto_pago), 0) FROM pago "
                    + "WHERE DATE_TRUNC('month', fecha_hora_pago) = "
                    + "  DATE_TRUNC('month', CURRENT_DATE - INTERVAL '1 month') "
                    + "AND estado_pago = 'CONFIRMADO'");
            int pendientes = queryInt(c,
                    "SELECT COUNT(*) FROM factura "
                    + "WHERE estado_factura IN ('EMITIDA','PENDIENTE')");
            lblMesActual.setText(dinero(actual));
            lblMesAnterior.setText(dinero(anterior));
            if (anterior.signum() == 0) {
                lblVariacion.setText(actual.signum() > 0 ? "+100 %" : "0 %");
            } else {
                BigDecimal diff = actual.subtract(anterior);
                double pct = diff.doubleValue()
                        / anterior.doubleValue() * 100.0;
                lblVariacion.setText(String.format(Locale.US,
                        "%+.1f %%", pct));
            }
            lblPendiente.setText(String.valueOf(pendientes));

            DefaultTableModel dia = new DefaultTableModel(
                    new Object[]{"Fecha", "Ingresos", "Nº pagos"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT fecha_hora_pago::date, SUM(monto_pago), COUNT(*) "
                    + "FROM pago "
                    + "WHERE DATE_TRUNC('month', fecha_hora_pago) = "
                    + "  DATE_TRUNC('month', CURRENT_DATE) "
                    + "AND estado_pago = 'CONFIRMADO' "
                    + "GROUP BY 1 ORDER BY 1")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        dia.addRow(new Object[]{r.getDate(1),
                            dinero(r.getBigDecimal(2)), r.getInt(3)});
                    }
                }
            }
            tblDiario.setModel(dia);

            DefaultTableModel met = new DefaultTableModel(
                    new Object[]{"Método", "Pagos", "Monto", "% del total"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT mp.nombre_metodo, COUNT(*), SUM(pg.monto_pago) "
                    + "FROM pago pg JOIN metodo_pago mp "
                    + "  ON mp.id_metodo_pago = pg.id_metodo_pago "
                    + "WHERE pg.estado_pago = 'CONFIRMADO' "
                    + "GROUP BY mp.nombre_metodo ORDER BY 3 DESC")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        BigDecimal m = r.getBigDecimal(3);
                        double pct = actual.signum() == 0 ? 0
                                : m.doubleValue() / totalGlobal(c).doubleValue()
                                        * 100.0;
                        met.addRow(new Object[]{r.getString(1),
                            r.getInt(2), dinero(m),
                            String.format("%.1f %%", pct)});
                    }
                }
            }
            tblMetodos.setModel(met);

            DefaultTableModel pen = new DefaultTableModel(
                    new Object[]{"Nº factura", "Cliente", "Emisión",
                        "Estado", "Días vencida"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT f.numero_factura, "
                    + "TRIM(p.nombres || ' ' || p.apellidos), "
                    + "f.fecha_emision, f.estado_factura, "
                    + "CURRENT_DATE - f.fecha_emision "
                    + "FROM factura f "
                    + "JOIN cliente cli ON cli.id_persona = f.id_cliente "
                    + "JOIN persona p ON p.id_persona = f.id_cliente "
                    + "WHERE f.estado_factura IN ('EMITIDA','PENDIENTE') "
                    + "ORDER BY f.fecha_emision")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        pen.addRow(new Object[]{r.getString(1), r.getString(2),
                            r.getDate(3), r.getString(4), r.getInt(5)});
                    }
                }
            }
            tblPendientes.setModel(pen);

            DefaultTableModel com = new DefaultTableModel(
                    new Object[]{"Estado", "Cantidad"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT estado_comprobante, COUNT(*) "
                    + "FROM comprobante GROUP BY estado_comprobante "
                    + "ORDER BY 1")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        com.addRow(new Object[]{r.getString(1), r.getInt(2)});
                    }
                }
            }
            tblComprobantes.setModel(com);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el reporte: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BigDecimal totalGlobal(Connection c) throws SQLException {
        return queryDec(c,
                "SELECT COALESCE(SUM(monto_pago), 0) FROM pago "
                + "WHERE estado_pago = 'CONFIRMADO'");
    }

    private static int queryInt(Connection c, String sql) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            r.next();
            return r.getInt(1);
        }
    }

    private static BigDecimal queryDec(Connection c, String sql)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            r.next();
            BigDecimal v = r.getBigDecimal(1);
            return v == null ? BigDecimal.ZERO : v;
        }
    }

    private static String dinero(BigDecimal v) {
        return String.format(Locale.US, "$ %,.2f",
                v == null ? BigDecimal.ZERO : v);
    }

    private void imprimirTodo() {
        JTable[] tablas = {tblDiario, tblMetodos, tblPendientes,
                tblComprobantes};
        String[] nombres = {"Ingresos diarios", "Métodos de pago",
                "Facturas pendientes", "Comprobantes"};
        try {
            for (int i = 0; i < tablas.length; i++) {
                tablas[i].print(JTable.PrintMode.FIT_WIDTH,
                        new MessageFormat("GYMNOVA · " + nombres[i]
                                + " · " + LocalDate.now()),
                        new MessageFormat("Página {0}"));
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al imprimir: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel tarjetaKPI(String titulo, JLabel valor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(TARJETA);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(new Color(90, 110, 135));
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(valor);
        return p;
    }

    private JLabel kpi() {
        JLabel l = new JLabel("—", SwingConstants.LEFT);
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(AZUL);
        return l;
    }

    private JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(AZUL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return b;
    }
}
