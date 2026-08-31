package vista;

import controlador.AsistenciaRecepcionControlador;
import controlador.AsistenciaRecepcionControlador.ClienteAsistencia;
import controlador.AsistenciaRecepcionControlador.RegistroAsistencia;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Pestaña operativa de Recepción para entrada/salida manual de clientes. */
public class PnlAsistenciaRecepcion extends JPanel {
    private static final DateTimeFormatter F_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private final AsistenciaRecepcionControlador controlador = new AsistenciaRecepcionControlador();
    private final JComboBox<ClienteAsistencia> cboCliente = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(10);
    private final JTextField txtHoraEntrada = new JTextField(7);
    private final JTextField txtHoraSalida = new JTextField(7);
    private final JButton btnAhoraEntrada = new JButton("Ahora");
    private final JButton btnAhoraSalida = new JButton("Ahora");
    private final JButton btnEntrada = new JButton("Registrar entrada");
    private final JButton btnSalida = new JButton("Registrar salida");
    private final JButton btnActualizar = new JButton("Actualizar historial");
    private final JLabel lblEstado = new JLabel("Seleccione un cliente y registre su llegada o salida.");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Código", "Cédula", "Cliente", "Fecha", "Entrada", "Salida", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modelo);

    public PnlAsistenciaRecepcion() {
        construir();
        cargarClientes();
        establecerAhora();
        cargarHistorial();
    }

    /** Recarga clientes e historial al volver a abrir el módulo desde el menú. */
    public final void refrescarDatos() {
        cargarClientes();
        if (txtFecha.getText() == null || txtFecha.getText().isBlank()) {
            establecerAhora();
        }
        cargarHistorial();
    }

    private void construir() {
        setLayout(new BorderLayout(0, 14));
        setBackground(new Color(234, 242, 251));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(Color.WHITE);
        cab.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212,225,239)),
                BorderFactory.createEmptyBorder(16,18,16,18)));
        JLabel t = new JLabel("Registro manual de asistencia");
        t.setFont(new Font("SansSerif", Font.BOLD, 22));
        t.setForeground(new Color(10,58,108));
        JLabel s = new JLabel("Registro manual. Preparado para integración externa por QR, cédula o biométrico.");
        s.setForeground(new Color(72,94,120));
        cab.add(t, BorderLayout.NORTH); cab.add(s, BorderLayout.SOUTH);
        add(cab, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0,14));
        centro.setOpaque(false);
        centro.add(formulario(), BorderLayout.NORTH);

        tabla.setRowHeight(34); tabla.setFillsViewportHeight(true); tabla.setShowGrid(false);
        tabla.setSelectionBackground(new Color(220,238,255));
        tabla.setSelectionForeground(new Color(23,42,67));
        DefaultTableCellRenderer h = new DefaultTableCellRenderer();
        h.setOpaque(true); h.setBackground(new Color(10,58,108)); h.setForeground(Color.WHITE);
        h.setHorizontalAlignment(SwingConstants.CENTER); h.setFont(new Font("SansSerif", Font.BOLD, 12));
        tabla.getTableHeader().setDefaultRenderer(h); tabla.getTableHeader().setPreferredSize(new Dimension(0,38));
        JScrollPane sp = new JScrollPane(tabla);
        sp.setBorder(BorderFactory.createTitledBorder("Asistencias de la fecha seleccionada"));
        centro.add(sp, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        btnAhoraEntrada.addActionListener(e -> txtHoraEntrada.setText(LocalTime.now().format(F_HORA)));
        btnAhoraSalida.addActionListener(e -> txtHoraSalida.setText(LocalTime.now().format(F_HORA)));
        btnEntrada.addActionListener(e -> registrarEntrada());
        btnSalida.addActionListener(e -> registrarSalida());
        btnActualizar.addActionListener(e -> cargarHistorial());
        txtFecha.addActionListener(e -> cargarHistorial());

        utilidades.EstilosComponentes.aplicarComboRedondeado(cboCliente);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtFecha);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtHoraEntrada);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtHoraSalida);
        utilidades.EstilosComponentes.aplicarBotonPremium(btnEntrada,new Color(34,139,34),new Color(56,170,70),Color.WHITE);
        utilidades.EstilosComponentes.aplicarBotonPremium(btnSalida,new Color(215,100,40),new Color(235,130,65),Color.WHITE);
        utilidades.EstilosComponentes.aplicarBotonPremium(btnActualizar,new Color(8,124,255),new Color(54,207,255),Color.WHITE);
    }

    private JPanel formulario() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(212,225,239)),
                BorderFactory.createEmptyBorder(14,16,14,16)));
        GridBagConstraints g = new GridBagConstraints(); g.insets = new Insets(5,6,5,6); g.anchor=GridBagConstraints.WEST;
        g.gridx=0;g.gridy=0;p.add(new JLabel("Cliente *"),g);
        g.gridx=1;g.gridwidth=5;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;cboCliente.setPreferredSize(new Dimension(520,30));p.add(cboCliente,g);
        g.weightx=0;g.fill=GridBagConstraints.NONE;g.gridwidth=1;
        g.gridy=1;g.gridx=0;p.add(new JLabel("Fecha (AAAA-MM-DD) *"),g);g.gridx=1;p.add(txtFecha,g);
        g.gridx=2;p.add(new JLabel("Hora entrada (HH:mm) *"),g);g.gridx=3;p.add(txtHoraEntrada,g);g.gridx=4;p.add(btnAhoraEntrada,g);
        g.gridx=5;p.add(btnEntrada,g);
        g.gridy=2;g.gridx=2;p.add(new JLabel("Hora salida (HH:mm) *"),g);g.gridx=3;p.add(txtHoraSalida,g);g.gridx=4;p.add(btnAhoraSalida,g);g.gridx=5;p.add(btnSalida,g);
        g.gridy=3;g.gridx=0;g.gridwidth=5;g.fill=GridBagConstraints.HORIZONTAL;lblEstado.setForeground(new Color(72,94,120));p.add(lblEstado,g);
        g.gridx=5;g.gridwidth=1;g.fill=GridBagConstraints.NONE;p.add(btnActualizar,g);
        return p;
    }

    private void establecerAhora() {
        txtFecha.setText(LocalDate.now().toString());
        String hora = LocalTime.now().format(F_HORA);
        txtHoraEntrada.setText(hora); txtHoraSalida.setText(hora);
    }

    private void cargarClientes() {
        cboCliente.removeAllItems();
        for (ClienteAsistencia c : controlador.listarClientesActivos()) cboCliente.addItem(c);
        if (cboCliente.getItemCount()==0 && !controlador.getMensaje().isBlank())
            lblEstado.setText(controlador.getMensaje());
    }

    private LocalDate fecha() { return LocalDate.parse(txtFecha.getText().trim()); }
    private LocalTime hora(String texto) { return LocalTime.parse(texto.trim(), F_HORA); }
    private ClienteAsistencia cliente() { return (ClienteAsistencia)cboCliente.getSelectedItem(); }

    private void registrarEntrada() {
        try {
            ClienteAsistencia c=cliente();
            if(c==null) throw new IllegalArgumentException("Seleccione un cliente.");
            boolean ok=controlador.registrarEntrada(c.id(),fecha(),hora(txtHoraEntrada.getText()));
            mostrarResultado(ok); if(ok) cargarHistorial();
        } catch(Exception ex){ avisoFormato(ex); }
    }
    private void registrarSalida() {
        try {
            ClienteAsistencia c=cliente();
            if(c==null) throw new IllegalArgumentException("Seleccione un cliente.");
            boolean ok=controlador.registrarSalida(c.id(),fecha(),hora(txtHoraSalida.getText()));
            mostrarResultado(ok); if(ok) cargarHistorial();
        } catch(Exception ex){ avisoFormato(ex); }
    }
    private void mostrarResultado(boolean ok) {
        lblEstado.setText(controlador.getMensaje());
        JOptionPane.showMessageDialog(this,controlador.getMensaje(),ok?"Asistencia":"No se pudo registrar",
                ok?JOptionPane.INFORMATION_MESSAGE:JOptionPane.WARNING_MESSAGE);
    }
    private void avisoFormato(Exception ex) {
        String m = ex instanceof java.time.format.DateTimeParseException
                ? "Revise los formatos: fecha AAAA-MM-DD y hora HH:mm." : ex.getMessage();
        JOptionPane.showMessageDialog(this,m,"Datos inválidos",JOptionPane.WARNING_MESSAGE);
    }
    private void cargarHistorial() {
        try {
            LocalDate f=fecha(); modelo.setRowCount(0);
            for(RegistroAsistencia r:controlador.listarPorFecha(f)) {
                modelo.addRow(new Object[]{r.id(),r.codigo(),r.cedula(),r.cliente(),r.fecha(),
                    r.entrada()==null?"-":r.entrada().format(F_HORA),
                    r.salida()==null?"PENDIENTE":r.salida().format(F_HORA),r.estado()});
            }
            lblEstado.setText(modelo.getRowCount()+" registro(s) para "+f+".");
        } catch(Exception ex) { avisoFormato(ex); }
    }
}
