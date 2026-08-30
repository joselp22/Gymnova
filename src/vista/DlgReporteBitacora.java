package vista;

import conexion.ConexionPostgreSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import utilidades.CalendarioSelector;

/** Reporte de auditoría con filtros por usuario / módulo / rango de fechas. */
public final class DlgReporteBitacora extends JDialog {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color AZUL = new Color(8, 124, 255);

    private final JTextField txtDesde = new JTextField(10);
    private final JTextField txtHasta = new JTextField(10);
    private final JComboBox<String> cboUsuario = new JComboBox<>();
    private final JComboBox<String> cboModulo = new JComboBox<>();
    private final JTable tabla = new JTable();
    private final JLabel lblResumen = new JLabel(" ");

    public DlgReporteBitacora(Frame padre) {
        super(padre, "Bitácora del sistema", true);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(FONDO);
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(construirEncabezado(), BorderLayout.NORTH);
        JScrollPane sc = new JScrollPane(tabla);
        sc.setBorder(BorderFactory.createLineBorder(BORDE));
        add(sc, BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);

        LocalDate hoy = LocalDate.now();
        txtDesde.setText(hoy.minusDays(7).toString());
        txtHasta.setText(hoy.toString());
        CalendarioSelector.vincularFecha(txtDesde);
        CalendarioSelector.vincularFecha(txtHasta);

        tabla.setRowHeight(26);
        tabla.getTableHeader().setBackground(new Color(10, 58, 108));
        tabla.getTableHeader().setForeground(Color.WHITE);

        setSize(new Dimension(1050, 620));
        setLocationRelativeTo(padre);

        cargarCombos();
        cargarDatos();
    }

    private JPanel construirEncabezado() {
        JPanel enc = new JPanel(new BorderLayout(0, 8));
        enc.setBackground(TARJETA);
        enc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JLabel titulo = new JLabel("GYMNOVA · Bitácora del sistema");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(TITULO);
        enc.add(titulo, BorderLayout.NORTH);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.add(new JLabel("Desde:"));
        filtros.add(txtDesde);
        filtros.add(new JLabel("Hasta:"));
        filtros.add(txtHasta);
        filtros.add(new JLabel("Usuario:"));
        filtros.add(cboUsuario);
        filtros.add(new JLabel("Módulo:"));
        filtros.add(cboModulo);
        JButton btn = boton("Aplicar filtros");
        btn.addActionListener(e -> cargarDatos());
        filtros.add(btn);
        enc.add(filtros, BorderLayout.CENTER);
        return enc;
    }

    private JPanel construirPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setBackground(TARJETA);
        pie.setBorder(BorderFactory.createLineBorder(BORDE));
        lblResumen.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        lblResumen.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblResumen.setForeground(TITULO);
        pie.add(lblResumen, BorderLayout.WEST);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        botones.setOpaque(false);
        JButton imp = boton("Imprimir");
        imp.addActionListener(e -> imprimir());
        JButton c = new JButton("Cerrar");
        c.addActionListener(e -> dispose());
        botones.add(imp);
        botones.add(c);
        pie.add(botones, BorderLayout.EAST);
        return pie;
    }

    private void cargarCombos() {
        DefaultComboBoxModel<String> u = new DefaultComboBoxModel<>();
        u.addElement("TODOS");
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
        m.addElement("TODOS");
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT DISTINCT us.nombre_usuario FROM bitacora b "
                    + "JOIN usuario us ON us.id_usuario = b.id_usuario "
                    + "ORDER BY 1");
                 ResultSet r = s.executeQuery()) {
                while (r.next()) u.addElement(r.getString(1));
            }
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT DISTINCT modulo FROM bitacora ORDER BY 1");
                 ResultSet r = s.executeQuery()) {
                while (r.next()) m.addElement(r.getString(1));
            }
        } catch (SQLException ex) {
            // combos quedan con solo "TODOS"; no bloqueamos la vista.
        }
        cboUsuario.setModel(u);
        cboModulo.setModel(m);
    }

    private void cargarDatos() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(txtDesde.getText().trim());
            hasta = LocalDate.parse(txtHasta.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Fechas inválidas. Use AAAA-MM-DD.",
                    "GYMNOVA", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (hasta.isBefore(desde)) {
            JOptionPane.showMessageDialog(this,
                    "La fecha hasta no puede ser anterior a desde.",
                    "GYMNOVA", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String usuario = String.valueOf(cboUsuario.getSelectedItem());
        String modulo = String.valueOf(cboModulo.getSelectedItem());

        StringBuilder sql = new StringBuilder(
                "SELECT b.fecha_hora, us.nombre_usuario, b.modulo, "
                + "b.accion_realizada, b.descripcion, b.resultado, "
                + "b.direccion_ip "
                + "FROM bitacora b "
                + "JOIN usuario us ON us.id_usuario = b.id_usuario "
                + "WHERE b.fecha_hora::date BETWEEN ? AND ? ");
        if (!"TODOS".equals(usuario)) {
            sql.append("AND us.nombre_usuario = ? ");
        }
        if (!"TODOS".equals(modulo)) {
            sql.append("AND b.modulo = ? ");
        }
        sql.append("ORDER BY b.fecha_hora DESC LIMIT 500");

        DefaultTableModel m = new DefaultTableModel(new Object[]{
            "Fecha/hora", "Usuario", "Módulo", "Acción",
            "Descripción", "Resultado", "IP"}, 0);
        int total = 0;
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql.toString())) {
            int idx = 1;
            s.setDate(idx++, Date.valueOf(desde));
            s.setDate(idx++, Date.valueOf(hasta));
            if (!"TODOS".equals(usuario)) s.setString(idx++, usuario);
            if (!"TODOS".equals(modulo)) s.setString(idx++, modulo);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    Timestamp t = r.getTimestamp(1);
                    m.addRow(new Object[]{t, r.getString(2), r.getString(3),
                        r.getString(4), r.getString(5), r.getString(6),
                        r.getString(7)});
                    total++;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar la bitácora: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tabla.setModel(m);
        lblResumen.setText(String.format(
                "Registros mostrados: %d (máx. 500 por consulta)", total));
    }

    private void imprimir() {
        if (tabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "No hay registros para imprimir con esos filtros.",
                    "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            tabla.print(JTable.PrintMode.FIT_WIDTH,
                    new MessageFormat("GYMNOVA · Bitácora ("
                            + txtDesde.getText().trim() + " a "
                            + txtHasta.getText().trim() + ")"),
                    new MessageFormat("Página {0}"));
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al imprimir: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
        }
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
