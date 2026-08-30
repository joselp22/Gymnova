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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
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
 * Reporte imprimible del panorama de clientes activos:
 * KPIs, segmentación por sexo/edad/antigüedad, ranking de asistencias
 * del mes y bajas recientes. Se ejecuta en tiempo real contra la BD.
 */
public final class DlgReporteClientes extends JDialog {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color AZUL = new Color(8, 124, 255);

    private final JLabel lblTotal = kpi();
    private final JLabel lblActivos = kpi();
    private final JLabel lblInactivos = kpi();
    private final JLabel lblNuevosMes = kpi();
    private final JTable tblSegmentacion = new JTable();
    private final JTable tblRanking = new JTable();
    private final JTable tblBajas = new JTable();

    public DlgReporteClientes(Frame padre) {
        super(padre, "Panorama de clientes activos", true);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(FONDO);
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);

        setSize(new Dimension(980, 640));
        setLocationRelativeTo(padre);
        cargarDatos();
    }

    private JPanel construirEncabezado() {
        JPanel enc = new JPanel(new BorderLayout(0, 6));
        enc.setBackground(TARJETA);
        enc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JLabel titulo = new JLabel("GYMNOVA · Panorama de clientes");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(TITULO);
        enc.add(titulo, BorderLayout.NORTH);

        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setOpaque(false);
        kpis.add(tarjetaKPI("Total clientes", lblTotal));
        kpis.add(tarjetaKPI("Activos", lblActivos));
        kpis.add(tarjetaKPI("Inactivos", lblInactivos));
        kpis.add(tarjetaKPI("Nuevos este mes", lblNuevosMes));
        enc.add(kpis, BorderLayout.CENTER);
        return enc;
    }

    private JTabbedPane construirCentro() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Segmentación", wrap(tblSegmentacion));
        tabs.addTab("Top asistencias del mes", wrap(tblRanking));
        tabs.addTab("Bajas últimos 30 días", wrap(tblBajas));
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
            int total = queryInt(c,
                    "SELECT COUNT(*) FROM cliente");
            int activos = queryInt(c,
                    "SELECT COUNT(*) FROM cliente WHERE estado_cliente = TRUE");
            int inactivos = total - activos;
            int nuevos = queryInt(c,
                    "SELECT COUNT(*) FROM cliente "
                    + "WHERE fecha_registro >= DATE_TRUNC('month', CURRENT_DATE)");
            lblTotal.setText(String.valueOf(total));
            lblActivos.setText(String.valueOf(activos));
            lblInactivos.setText(String.valueOf(inactivos));
            lblNuevosMes.setText(String.valueOf(nuevos));

            DefaultTableModel seg = new DefaultTableModel(
                    new Object[]{"Segmento", "Categoría",
                        "Clientes", "% del total"}, 0);
            cargarSegmentoSexo(c, seg, activos);
            cargarSegmentoEdad(c, seg, activos);
            cargarSegmentoAntiguedad(c, seg, activos);
            tblSegmentacion.setModel(seg);

            DefaultTableModel rank = new DefaultTableModel(
                    new Object[]{"#", "Código", "Cliente",
                        "Asistencias del mes"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT cli.codigo_cliente, "
                    + "TRIM(p.nombres || ' ' || p.apellidos), COUNT(*) "
                    + "FROM asistencia a "
                    + "JOIN cliente cli ON cli.id_persona = a.id_cliente "
                    + "JOIN persona p ON p.id_persona = a.id_cliente "
                    + "WHERE a.fecha_asistencia >= "
                    + "  DATE_TRUNC('month', CURRENT_DATE) "
                    + "GROUP BY cli.codigo_cliente, p.nombres, p.apellidos "
                    + "ORDER BY 3 DESC LIMIT 10")) {
                try (ResultSet r = s.executeQuery()) {
                    int i = 1;
                    while (r.next()) {
                        rank.addRow(new Object[]{i++,
                            r.getString(1), r.getString(2), r.getInt(3)});
                    }
                }
            }
            tblRanking.setModel(rank);

            DefaultTableModel baj = new DefaultTableModel(
                    new Object[]{"Código", "Cliente", "Cédula",
                        "Fecha registro", "Observaciones"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT cli.codigo_cliente, "
                    + "TRIM(p.nombres || ' ' || p.apellidos), "
                    + "p.cedula, cli.fecha_registro, cli.observaciones "
                    + "FROM cliente cli JOIN persona p ON p.id_persona = cli.id_persona "
                    + "WHERE cli.estado_cliente = FALSE "
                    + "ORDER BY cli.fecha_registro DESC LIMIT 30")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        baj.addRow(new Object[]{
                            r.getString(1), r.getString(2), r.getString(3),
                            r.getDate(4), r.getString(5)});
                    }
                }
            }
            tblBajas.setModel(baj);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el reporte: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarSegmentoSexo(Connection c,
            DefaultTableModel m, int activos) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT p.sexo, COUNT(*) FROM cliente cli "
                + "JOIN persona p ON p.id_persona = cli.id_persona "
                + "WHERE cli.estado_cliente GROUP BY p.sexo ORDER BY 2 DESC")) {
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    m.addRow(new Object[]{"Sexo", r.getString(1),
                        r.getInt(2), pct(r.getInt(2), activos)});
                }
            }
        }
    }

    private void cargarSegmentoEdad(Connection c,
            DefaultTableModel m, int activos) throws SQLException {
        String[] rangos = {"18-25", "26-35", "36-45", "46-55", "55+"};
        int[][] limites = {{18, 25}, {26, 35}, {36, 45}, {46, 55}, {56, 200}};
        for (int i = 0; i < rangos.length; i++) {
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT COUNT(*) FROM cliente cli "
                    + "JOIN persona p ON p.id_persona = cli.id_persona "
                    + "WHERE cli.estado_cliente "
                    + "AND EXTRACT(YEAR FROM AGE(p.fecha_nacimiento)) "
                    + "  BETWEEN ? AND ?")) {
                s.setInt(1, limites[i][0]);
                s.setInt(2, limites[i][1]);
                try (ResultSet r = s.executeQuery()) {
                    r.next();
                    m.addRow(new Object[]{"Edad", rangos[i] + " años",
                        r.getInt(1), pct(r.getInt(1), activos)});
                }
            }
        }
    }

    private void cargarSegmentoAntiguedad(Connection c,
            DefaultTableModel m, int activos) throws SQLException {
        String[] rangos = {"< 1 mes", "1-3 meses", "3-6 meses", "6-12 meses",
                           "+1 año"};
        String[] where = {
                "cli.fecha_registro > CURRENT_DATE - INTERVAL '1 month'",
                "cli.fecha_registro <= CURRENT_DATE - INTERVAL '1 month' "
                    + "AND cli.fecha_registro > CURRENT_DATE - INTERVAL '3 month'",
                "cli.fecha_registro <= CURRENT_DATE - INTERVAL '3 month' "
                    + "AND cli.fecha_registro > CURRENT_DATE - INTERVAL '6 month'",
                "cli.fecha_registro <= CURRENT_DATE - INTERVAL '6 month' "
                    + "AND cli.fecha_registro > CURRENT_DATE - INTERVAL '1 year'",
                "cli.fecha_registro <= CURRENT_DATE - INTERVAL '1 year'"};
        for (int i = 0; i < rangos.length; i++) {
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT COUNT(*) FROM cliente cli "
                    + "WHERE cli.estado_cliente AND " + where[i])) {
                try (ResultSet r = s.executeQuery()) {
                    r.next();
                    m.addRow(new Object[]{"Antigüedad", rangos[i],
                        r.getInt(1), pct(r.getInt(1), activos)});
                }
            }
        }
    }

    private static String pct(int val, int total) {
        return total == 0 ? "0 %"
                : String.format("%.1f %%", 100.0 * val / total);
    }

    private static int queryInt(Connection c, String sql) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            r.next();
            return r.getInt(1);
        }
    }

    private void imprimirTodo() {
        JTable[] tablas = {tblSegmentacion, tblRanking, tblBajas};
        String[] nombres = {"Segmentación", "Top asistencias", "Bajas"};
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
