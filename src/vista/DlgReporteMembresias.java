package vista;

import conexion.ConexionPostgreSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import utilidades.CalendarioSelector;

/**
 * Reporte imprimible de mensualidades (tabla membresia + tipo + cliente).
 *
 * Filtro por rango de fechas de inicio, opcionalmente por estado. Muestra
 * la lista con costos y totales, y ofrece un botón de imprimir que envía
 * la tabla a la impresora del sistema con encabezado y numeración.
 */
public final class DlgReporteMembresias extends JDialog {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color AZUL = new Color(8, 124, 255);

    private final JTextField txtDesde = new JTextField(10);
    private final JTextField txtHasta = new JTextField(10);
    private final JComboBox<String> cboEstado = new JComboBox<>(new String[]{
        "TODOS", "ACTIVA", "VENCIDA", "CONGELADA", "CANCELADA", "PENDIENTE"});
    private final JLabel lblResumen = new JLabel(" ");
    private final JTable tabla = new JTable();

    public DlgReporteMembresias(Frame padre) {
        super(padre, "Reporte de mensualidades", true);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(FONDO);
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);

        LocalDate hoy = LocalDate.now();
        txtDesde.setText(hoy.withDayOfMonth(1).toString());
        txtHasta.setText(hoy.withDayOfMonth(hoy.lengthOfMonth()).toString());
        CalendarioSelector.vincularFecha(txtDesde);
        CalendarioSelector.vincularFecha(txtHasta);

        setSize(new Dimension(950, 620));
        setLocationRelativeTo(padre);
        cargarDatos();
    }

    private JPanel construirEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout(10, 8));
        encabezado.setBackground(TARJETA);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel titulo = new JLabel("GYMNOVA · Reporte de mensualidades");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(TITULO);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.add(new JLabel("Desde:"));
        filtros.add(txtDesde);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(txtHasta);
        filtros.add(new JLabel("Estado:"));
        filtros.add(cboEstado);

        JButton btnFiltrar = botonPrimario("Aplicar filtros");
        btnFiltrar.addActionListener(e -> cargarDatos());
        filtros.add(btnFiltrar);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(filtros);
        encabezado.add(textos, BorderLayout.CENTER);
        return encabezado;
    }

    private JScrollPane construirTabla() {
        tabla.setModel(nuevoModelo());
        tabla.setRowHeight(28);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setBackground(new Color(10, 58, 108));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12));
        tabla.getTableHeader().setReorderingAllowed(false);

        // Alinea la columna de costo a la derecha.
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(6).setCellRenderer(derecha);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel construirPie() {
        JPanel pie = new JPanel(new BorderLayout(10, 8));
        pie.setBackground(TARJETA);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));

        lblResumen.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblResumen.setForeground(TITULO);
        pie.add(lblResumen, BorderLayout.WEST);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        JButton btnImprimir = botonPrimario("Imprimir reporte");
        btnImprimir.addActionListener(e -> imprimir());
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        botones.add(btnImprimir);
        botones.add(btnCerrar);
        pie.add(botones, BorderLayout.EAST);
        return pie;
    }

    private DefaultTableModel nuevoModelo() {
        return new DefaultTableModel(
                new Object[][]{},
                new String[]{"N° membresía", "Código cliente", "Cliente",
                    "Tipo", "Fecha inicio", "Fecha fin", "Costo",
                    "Estado"}) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    private void cargarDatos() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(txtDesde.getText().trim());
            hasta = LocalDate.parse(txtHasta.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Use fechas válidas con formato AAAA-MM-DD.",
                    "Filtros", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (hasta.isBefore(desde)) {
            JOptionPane.showMessageDialog(this,
                    "La fecha hasta no puede ser anterior a desde.",
                    "Filtros", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String estado = String.valueOf(cboEstado.getSelectedItem());
        String sql = "SELECT m.numero_membresia, c.codigo_cliente, "
                + "TRIM(p.nombres || ' ' || p.apellidos) AS cliente, "
                + "tm.nombre AS tipo, m.fecha_inicio, m.fecha_fin, "
                + "m.costo_final, m.estado_membresia "
                + "FROM membresia m "
                + "JOIN cliente c ON c.id_persona = m.id_cliente "
                + "JOIN persona p ON p.id_persona = m.id_cliente "
                + "JOIN tipo_membresia tm ON tm.id_tipo_membresia "
                + "     = m.id_tipo_membresia "
                + "WHERE m.fecha_inicio BETWEEN ? AND ? "
                + (estado.equals("TODOS")
                        ? "" : "  AND m.estado_membresia = ? ")
                + "ORDER BY m.fecha_inicio DESC, m.id_membresia DESC";

        DefaultTableModel modelo = nuevoModelo();
        BigDecimal totalCosto = BigDecimal.ZERO;
        int totalRegistros = 0;
        int activas = 0;
        DateTimeFormatter fechaFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setDate(1, Date.valueOf(desde));
            s.setDate(2, Date.valueOf(hasta));
            if (!estado.equals("TODOS")) {
                s.setString(3, estado);
            }
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    Date ini = r.getDate(5);
                    Date fin = r.getDate(6);
                    BigDecimal costo = r.getBigDecimal(7);
                    if (costo == null) {
                        costo = BigDecimal.ZERO;
                    }
                    totalCosto = totalCosto.add(costo);
                    String est = r.getString(8);
                    if ("ACTIVA".equals(est)) {
                        activas++;
                    }
                    modelo.addRow(new Object[]{
                        r.getString(1),
                        r.getString(2),
                        r.getString(3),
                        r.getString(4),
                        ini == null ? "" : ini.toLocalDate().format(fechaFmt),
                        fin == null ? "" : fin.toLocalDate().format(fechaFmt),
                        formatearMoneda(costo),
                        est
                    });
                    totalRegistros++;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el reporte: " + ex.getMessage(),
                    "Base de datos", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tabla.setModel(modelo);
        DefaultTableCellRenderer der = new DefaultTableCellRenderer();
        der.setHorizontalAlignment(SwingConstants.RIGHT);
        tabla.getColumnModel().getColumn(6).setCellRenderer(der);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(180);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(90);

        lblResumen.setText(String.format(Locale.US,
                "Registros: %d   ·   Activas: %d   ·   Total facturado: %s",
                totalRegistros, activas, formatearMoneda(totalCosto)));
    }

    private void imprimir() {
        if (tabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No hay membresías para imprimir con los filtros actuales.",
                    "Imprimir", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            String cabecera = String.format(Locale.US,
                    "GYMNOVA — Reporte de mensualidades (%s a %s) · %s",
                    txtDesde.getText().trim(),
                    txtHasta.getText().trim(),
                    LocalDate.now().toString());
            boolean impreso = tabla.print(
                    JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat(cabecera.replace("'", "''")),
                    new MessageFormat("Página {0}"));
            if (impreso) {
                JOptionPane.showMessageDialog(this,
                        "Reporte enviado a la impresora.",
                        "Imprimir", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al imprimir: " + ex.getMessage(),
                    "Imprimir", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatearMoneda(BigDecimal valor) {
        if (valor == null) {
            return "$ 0.00";
        }
        return String.format(Locale.US, "$ %,.2f", valor);
    }

    private JButton botonPrimario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(AZUL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return b;
    }
}
