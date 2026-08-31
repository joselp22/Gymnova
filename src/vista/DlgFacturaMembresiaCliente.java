package vista;

import conexion.ConexionPostgreSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.DetalleFactura;
import modelo.Factura;
import modelo.FacturaDocumento;
import modelo.Persona;
import utilidades.DocumentoFacturaPDF;

/**
 * Factura de membresía por cliente. Permite elegir un cliente del listado
 * de clientes activos, seleccionar una de sus membresías registradas y
 * generar un PDF de factura. El archivo se guarda con el nombre
 * "Factura_<NombreCliente>_<yyyy-MM-dd>.pdf".
 */
public final class DlgFacturaMembresiaCliente extends JDialog {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color AZUL = new Color(8, 124, 255);

    private final JComboBox<ItemCliente> cboClientes = new JComboBox<>();
    private final JTable tblMembresias = new JTable();
    private final JLabel lblResumen = new JLabel(" ");
    private final JButton btnGenerar = botonPrimario("Generar factura PDF");

    public DlgFacturaMembresiaCliente(Frame padre) {
        super(padre, "Factura de membresía por cliente", true);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(FONDO);
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);

        cargarClientes();
        cboClientes.addActionListener(e -> cargarMembresiasCliente());
        btnGenerar.setEnabled(false);
        tblMembresias.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnGenerar.setEnabled(tblMembresias.getSelectedRow() >= 0);
            }
        });

        setSize(new Dimension(900, 560));
        setLocationRelativeTo(padre);
    }

    private JPanel construirEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout(10, 8));
        encabezado.setBackground(TARJETA);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel titulo = new JLabel(
                "GYMNOVA · Factura de membresía por cliente");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(TITULO);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.add(new JLabel("Cliente:"));
        cboClientes.setPreferredSize(new Dimension(360, 30));
        filtros.add(cboClientes);

        JButton btnRefrescar = new JButton("Actualizar");
        btnRefrescar.addActionListener(e -> {
            cargarClientes();
            cargarMembresiasCliente();
        });
        filtros.add(btnRefrescar);

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
        tblMembresias.setModel(nuevoModelo());
        tblMembresias.setRowHeight(28);
        tblMembresias.setFillsViewportHeight(true);
        tblMembresias.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        tblMembresias.getTableHeader().setBackground(new Color(10, 58, 108));
        tblMembresias.getTableHeader().setForeground(Color.WHITE);
        tblMembresias.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12));
        tblMembresias.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);

        JScrollPane scroll = new JScrollPane(tblMembresias);
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
        btnGenerar.addActionListener(e -> generarFactura());
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        botones.add(btnGenerar);
        botones.add(btnCerrar);
        pie.add(botones, BorderLayout.EAST);
        return pie;
    }

    private DefaultTableModel nuevoModelo() {
        return new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "N° membresía", "Tipo", "Fecha inicio",
                    "Fecha fin", "Costo", "Estado"}) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    private void cargarClientes() {
        cboClientes.removeAllItems();
        String sql = "SELECT c.id_persona, c.codigo_cliente, p.cedula, "
                + "TRIM(p.nombres || ' ' || p.apellidos) AS cliente "
                + "FROM cliente c "
                + "JOIN persona p ON p.id_persona = c.id_persona "
                + "WHERE c.estado_cliente = TRUE "
                + "ORDER BY p.apellidos, p.nombres";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            while (r.next()) {
                cboClientes.addItem(new ItemCliente(
                        r.getLong("id_persona"),
                        r.getString("codigo_cliente"),
                        r.getString("cedula"),
                        r.getString("cliente")));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No fue posible cargar los clientes: " + ex.getMessage(),
                    "Base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarMembresiasCliente() {
        DefaultTableModel modelo = nuevoModelo();
        tblMembresias.setModel(modelo);
        btnGenerar.setEnabled(false);
        lblResumen.setText(" ");

        ItemCliente sel = (ItemCliente) cboClientes.getSelectedItem();
        if (sel == null) return;

        String sql = "SELECT m.id_membresia, m.numero_membresia, "
                + "tm.nombre AS tipo, m.fecha_inicio, m.fecha_fin, "
                + "m.costo_final, m.estado_membresia "
                + "FROM membresia m "
                + "JOIN tipo_membresia tm "
                + "  ON tm.id_tipo_membresia = m.id_tipo_membresia "
                + "WHERE m.id_cliente = ? "
                + "ORDER BY m.fecha_inicio DESC, m.id_membresia DESC";
        int total = 0;
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, sel.idPersona);
            try (ResultSet r = s.executeQuery()) {
                DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                while (r.next()) {
                    java.sql.Date fi = r.getDate("fecha_inicio");
                    java.sql.Date ff = r.getDate("fecha_fin");
                    BigDecimal costo = r.getBigDecimal("costo_final");
                    if (costo == null) costo = BigDecimal.ZERO;
                    modelo.addRow(new Object[]{
                        r.getLong("id_membresia"),
                        r.getString("numero_membresia"),
                        r.getString("tipo"),
                        fi == null ? "" : fi.toLocalDate().format(f),
                        ff == null ? "" : ff.toLocalDate().format(f),
                        "$ " + costo,
                        r.getString("estado_membresia")});
                    total++;
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No fue posible cargar las membresías: " + ex.getMessage(),
                    "Base de datos", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableCellRenderer der = new DefaultTableCellRenderer();
        der.setHorizontalAlignment(SwingConstants.RIGHT);
        tblMembresias.getColumnModel().getColumn(5).setCellRenderer(der);
        tblMembresias.getColumnModel().getColumn(0).setMinWidth(0);
        tblMembresias.getColumnModel().getColumn(0).setMaxWidth(0);
        tblMembresias.getColumnModel().getColumn(0).setWidth(0);

        lblResumen.setText("Cliente: " + sel.nombre
                + "  ·  " + total + " membresía(s) registrada(s)");
    }

    private void generarFactura() {
        int fila = tblMembresias.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione una membresía en la tabla.",
                    "Factura", JOptionPane.WARNING_MESSAGE);
            return;
        }
        ItemCliente sel = (ItemCliente) cboClientes.getSelectedItem();
        if (sel == null) return;

        Long idMembresia = (Long) tblMembresias.getModel().getValueAt(fila, 0);
        String numeroMembresia = String.valueOf(
                tblMembresias.getModel().getValueAt(fila, 1));
        String tipo = String.valueOf(
                tblMembresias.getModel().getValueAt(fila, 2));
        String estado = String.valueOf(
                tblMembresias.getModel().getValueAt(fila, 6));

        // Recuperar datos completos del cliente y de la membresía para el PDF.
        Persona cliente = cargarClientePersona(sel.idPersona);
        if (cliente == null) {
            JOptionPane.showMessageDialog(this,
                    "No fue posible cargar los datos del cliente.",
                    "Factura", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DatosMembresia datos = cargarDatosMembresia(idMembresia);
        if (datos == null) {
            JOptionPane.showMessageDialog(this,
                    "No fue posible cargar la membresía seleccionada.",
                    "Factura", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate hoy = LocalDate.now();
        String nombreArchivo = "Factura_" + sanear(cliente.getNombreCompleto())
                + "_" + hoy + ".pdf";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar factura de membresía");
        chooser.setSelectedFile(new File(nombreArchivo));
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Archivos PDF (*.pdf)", "pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = chooser.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".pdf")) {
            destino = new File(destino.getParentFile(),
                    destino.getName() + ".pdf");
        }

        // Armar el documento de factura en memoria a partir de la membresía.
        Factura factura = new Factura();
        factura.setNumeroFactura("MEM-" + numeroMembresia + "-" + hoy);
        factura.setFechaEmision(hoy);
        factura.setEstadoFactura(estado);
        factura.setTotalDescuento(BigDecimal.ZERO);
        factura.setImpuesto(BigDecimal.ZERO);
        factura.setIdCliente(cliente.getIdPersona());

        DetalleFactura detalle = new DetalleFactura();
        detalle.setTipoConcepto("Membresía " + tipo);
        detalle.setCodigoReferencia(numeroMembresia);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(datos.costo == null
                ? BigDecimal.ZERO : datos.costo);
        detalle.setPorcentajeImpuesto(BigDecimal.ZERO);

        List<DetalleFactura> detalles = new ArrayList<>();
        detalles.add(detalle);

        FacturaDocumento doc = new FacturaDocumento();
        doc.setFactura(factura);
        doc.setCliente(cliente);
        doc.setDetalles(detalles);
        doc.setPagos(new ArrayList<>());

        try {
            DocumentoFacturaPDF.generar(doc, Path.of(destino.getAbsolutePath()));
        } catch (IOException | RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "No fue posible generar el PDF: " + ex.getMessage(),
                    "Factura", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int op = JOptionPane.showConfirmDialog(this,
                "Factura guardada en:\n" + destino.getAbsolutePath()
                        + "\n\n¿Deseas abrirla ahora?",
                "Factura generada", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        if (op == JOptionPane.YES_OPTION
                && java.awt.Desktop.isDesktopSupported()) {
            try {
                java.awt.Desktop.getDesktop().open(destino);
            } catch (IOException | UnsupportedOperationException ex) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo abrir el archivo automáticamente. "
                        + "Ábralo manualmente desde la ubicación indicada.",
                        "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private Persona cargarClientePersona(Long idPersona) {
        String sql = "SELECT id_persona, cedula, nombres, apellidos, "
                + "fecha_nacimiento, sexo, telefono, correo, estado "
                + "FROM persona WHERE id_persona = ?";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idPersona);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    Persona p = new Persona();
                    p.setIdPersona(r.getLong("id_persona"));
                    p.setCedula(r.getString("cedula"));
                    p.setNombres(r.getString("nombres"));
                    p.setApellidos(r.getString("apellidos"));
                    java.sql.Date fn = r.getDate("fecha_nacimiento");
                    if (fn != null) p.setFechaNacimiento(fn.toLocalDate());
                    p.setSexo(r.getString("sexo"));
                    p.setTelefono(r.getString("telefono"));
                    p.setCorreo(r.getString("correo"));
                    p.setEstado(r.getBoolean("estado"));
                    return p;
                }
            }
        } catch (SQLException ex) {
            // Se reporta al invocador con retorno null.
        }
        return null;
    }

    private DatosMembresia cargarDatosMembresia(Long idMembresia) {
        String sql = "SELECT costo_final FROM membresia WHERE id_membresia = ?";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idMembresia);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    DatosMembresia d = new DatosMembresia();
                    d.costo = r.getBigDecimal("costo_final");
                    return d;
                }
            }
        } catch (SQLException ex) {
            // Retorna null si no puede leer.
        }
        return null;
    }

    private static String sanear(String texto) {
        if (texto == null) return "Cliente";
        String limpio = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9 _-]", "")
                .trim()
                .replaceAll("\\s+", "_");
        return limpio.isEmpty() ? "Cliente" : limpio;
    }

    private static JButton botonPrimario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(AZUL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return b;
    }

    /** Ítem del combo de clientes: guarda ID y mostrar amigable. */
    private static final class ItemCliente {
        final Long idPersona;
        final String codigo;
        final String cedula;
        final String nombre;

        ItemCliente(Long idPersona, String codigo, String cedula,
                String nombre) {
            this.idPersona = idPersona;
            this.codigo = codigo == null ? "" : codigo;
            this.cedula = cedula == null ? "" : cedula;
            this.nombre = nombre == null ? "" : nombre;
        }

        @Override
        public String toString() {
            return (codigo.isBlank() ? "" : codigo + "  ·  ")
                    + cedula + "  ·  " + nombre;
        }
    }

    private static final class DatosMembresia {
        BigDecimal costo;
    }
}
