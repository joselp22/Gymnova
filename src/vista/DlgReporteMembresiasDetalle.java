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
 * Reporte administrativo extendido de membresías: KPIs por estado,
 * distribución por plan con facturación, congelaciones activas y
 * renovaciones próximas (7 / 15 / 30 días).
 */
public final class DlgReporteMembresiasDetalle extends JDialog {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color AZUL = new Color(8, 124, 255);

    private final JLabel lblActivas = kpi();
    private final JLabel lblVencidas = kpi();
    private final JLabel lblFacturado = kpi();
    private final JLabel lblProximas30 = kpi();

    private final JTable tblPorPlan = new JTable();
    private final JTable tblCongelaciones = new JTable();
    private final JTable tblProximas = new JTable();
    private final JTable tblEstados = new JTable();

    public DlgReporteMembresiasDetalle(Frame padre) {
        super(padre, "Detalle administrativo de membresías", true);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(FONDO);
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(14, 14, 14, 14));
        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);
        setSize(new Dimension(1020, 640));
        setLocationRelativeTo(padre);
        cargarDatos();
    }

    private JPanel construirEncabezado() {
        JPanel enc = new JPanel(new BorderLayout(0, 6));
        enc.setBackground(TARJETA);
        enc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JLabel titulo = new JLabel(
                "GYMNOVA · Detalle administrativo de membresías");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(TITULO);
        enc.add(titulo, BorderLayout.NORTH);

        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setOpaque(false);
        kpis.add(tarjetaKPI("Activas", lblActivas));
        kpis.add(tarjetaKPI("Vencidas", lblVencidas));
        kpis.add(tarjetaKPI("Facturado total", lblFacturado));
        kpis.add(tarjetaKPI("Vencen próximos 30 días", lblProximas30));
        enc.add(kpis, BorderLayout.CENTER);
        return enc;
    }

    private JTabbedPane construirCentro() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Por plan", wrap(tblPorPlan));
        tabs.addTab("Estados", wrap(tblEstados));
        tabs.addTab("Congelaciones activas", wrap(tblCongelaciones));
        tabs.addTab("Próximas a vencer", wrap(tblProximas));
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
        JButton imp = boton("Imprimir todo");
        imp.addActionListener(e -> imprimirTodo());
        JButton c = new JButton("Cerrar");
        c.addActionListener(e -> dispose());
        pie.add(imp);
        pie.add(c);
        return pie;
    }

    private void cargarDatos() {
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            lblActivas.setText(String.valueOf(queryInt(c,
                    "SELECT COUNT(*) FROM membresia "
                    + "WHERE estado_membresia = 'ACTIVA'")));
            lblVencidas.setText(String.valueOf(queryInt(c,
                    "SELECT COUNT(*) FROM membresia "
                    + "WHERE estado_membresia = 'VENCIDA'")));
            BigDecimal fac = queryDec(c,
                    "SELECT COALESCE(SUM(costo_final), 0) FROM membresia");
            lblFacturado.setText(dinero(fac));
            lblProximas30.setText(String.valueOf(queryInt(c,
                    "SELECT COUNT(*) FROM membresia "
                    + "WHERE estado_membresia = 'ACTIVA' "
                    + "AND fecha_fin BETWEEN CURRENT_DATE "
                    + "  AND CURRENT_DATE + INTERVAL '30 day'")));

            DefaultTableModel plan = new DefaultTableModel(
                    new Object[]{"Plan", "Activas", "Facturación",
                        "% del total"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT tm.nombre, COUNT(*), COALESCE(SUM(m.costo_final),0), "
                    + "ROUND(100.0 * COUNT(*) / NULLIF("
                    + " (SELECT COUNT(*) FROM membresia "
                    + "   WHERE estado_membresia = 'ACTIVA'), 0), 1) "
                    + "FROM membresia m "
                    + "JOIN tipo_membresia tm "
                    + "  ON tm.id_tipo_membresia = m.id_tipo_membresia "
                    + "WHERE m.estado_membresia = 'ACTIVA' "
                    + "GROUP BY tm.nombre ORDER BY 2 DESC")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        plan.addRow(new Object[]{r.getString(1), r.getInt(2),
                            dinero(r.getBigDecimal(3)),
                            (r.getBigDecimal(4) == null ? "0"
                                : r.getBigDecimal(4)) + " %"});
                    }
                }
            }
            tblPorPlan.setModel(plan);

            DefaultTableModel est = new DefaultTableModel(
                    new Object[]{"Estado", "Cantidad", "Facturación"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT estado_membresia, COUNT(*), COALESCE(SUM(costo_final),0) "
                    + "FROM membresia GROUP BY estado_membresia "
                    + "ORDER BY 2 DESC")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        est.addRow(new Object[]{r.getString(1), r.getInt(2),
                            dinero(r.getBigDecimal(3))});
                    }
                }
            }
            tblEstados.setModel(est);

            DefaultTableModel con = new DefaultTableModel(
                    new Object[]{"Membresía", "Cliente", "Motivo",
                        "Inicio", "Fin", "Días"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT m.numero_membresia, "
                    + "TRIM(p.nombres || ' ' || p.apellidos), "
                    + "cg.motivo, cg.fecha_inicio, cg.fecha_fin, "
                    + "(cg.fecha_fin - cg.fecha_inicio) "
                    + "FROM congelacion cg "
                    + "JOIN membresia m ON m.id_membresia = cg.id_membresia "
                    + "JOIN persona p ON p.id_persona = m.id_cliente "
                    + "WHERE cg.estado_congelacion = 'ACTIVA' "
                    + "ORDER BY cg.fecha_inicio")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        con.addRow(new Object[]{r.getString(1), r.getString(2),
                            r.getString(3), r.getDate(4), r.getDate(5),
                            r.getInt(6)});
                    }
                }
            }
            tblCongelaciones.setModel(con);

            DefaultTableModel prx = new DefaultTableModel(
                    new Object[]{"Nº membresía", "Cliente", "Plan",
                        "Fecha fin", "Días restantes"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT m.numero_membresia, "
                    + "TRIM(p.nombres || ' ' || p.apellidos), tm.nombre, "
                    + "m.fecha_fin, (m.fecha_fin - CURRENT_DATE) "
                    + "FROM membresia m "
                    + "JOIN persona p ON p.id_persona = m.id_cliente "
                    + "JOIN tipo_membresia tm "
                    + "  ON tm.id_tipo_membresia = m.id_tipo_membresia "
                    + "WHERE m.estado_membresia = 'ACTIVA' "
                    + "AND m.fecha_fin BETWEEN CURRENT_DATE "
                    + "  AND CURRENT_DATE + INTERVAL '30 day' "
                    + "ORDER BY m.fecha_fin")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        prx.addRow(new Object[]{r.getString(1), r.getString(2),
                            r.getString(3), r.getDate(4), r.getInt(5)});
                    }
                }
            }
            tblProximas.setModel(prx);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el reporte: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
        }
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
        JTable[] tablas = {tblPorPlan, tblEstados, tblCongelaciones,
                tblProximas};
        String[] nombres = {"Membresías por plan", "Estados de membresía",
                "Congelaciones", "Próximas a vencer"};
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
