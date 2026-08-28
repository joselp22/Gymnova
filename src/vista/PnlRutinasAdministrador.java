package vista;

import controlador.RutinaAdministracionControlador;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import modelo.RutinaDetalleAdministrador;
import modelo.RutinaResumenAdministrador;

/**
 * Vista administrativa de solo lectura para las rutinas creadas por los
 * entrenadores. La creación, edición y asignación se realizan exclusivamente
 * desde el Planificador del Entrenador.
 */
public class PnlRutinasAdministrador extends JPanel {

    private final RutinaAdministracionControlador controlador;

    private final JTextField txtBuscar = new JTextField();
    private final JButton btnBuscar = new JButton("Buscar");
    private final JButton btnActualizar = new JButton("Actualizar");
    private final JTable tblRutinas = new JTable();
    private final JLabel lblCantidad = new JLabel("0 rutinas");

    public PnlRutinasAdministrador() {
        controlador = new RutinaAdministracionControlador();
        construirVista();
        configurarEventos();
        refrescarDatos();
    }

    public final void refrescarDatos() {
        cargarRutinas();
    }

    private void construirVista() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new java.awt.Color(234, 242, 251));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(crearEncabezado(), BorderLayout.NORTH);

        configurarTablaRutinas();

        JPanel panelRutinas = new JPanel(new BorderLayout(8, 8));
        panelRutinas.setBackground(java.awt.Color.WHITE);
        panelRutinas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        panelRutinas.add(crearBarraBusqueda(), BorderLayout.NORTH);
        panelRutinas.add(new JScrollPane(tblRutinas), BorderLayout.CENTER);

        JLabel ayuda = new JLabel(
                "Use el botón Ver detalle para abrir los ejercicios de la rutina seleccionada.");
        ayuda.setForeground(new java.awt.Color(90, 110, 135));
        panelRutinas.add(ayuda, BorderLayout.SOUTH);

        add(panelRutinas, BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(java.awt.Color.WHITE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new javax.swing.BoxLayout(
                textos, javax.swing.BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Rutinas creadas por entrenadores");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setForeground(new java.awt.Color(23, 42, 67));

        JLabel subtitulo = new JLabel(
                "Vista administrativa de supervisión. Las rutinas se crean y editan desde el Planificador del Entrenador.");
        subtitulo.setForeground(new java.awt.Color(90, 110, 135));

        textos.add(titulo);
        textos.add(javax.swing.Box.createVerticalStrut(4));
        textos.add(subtitulo);

        JLabel modulo = new JLabel("RUTINAS", SwingConstants.CENTER);
        modulo.setForeground(new java.awt.Color(8, 124, 255));
        modulo.setBorder(BorderFactory.createLineBorder(
                new java.awt.Color(8, 124, 255)));
        modulo.setPreferredSize(new Dimension(110, 45));

        encabezado.add(textos, BorderLayout.CENTER);
        encabezado.add(modulo, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearBarraBusqueda() {
        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setOpaque(false);

        txtBuscar.setToolTipText(
                "Buscar por número de rutina, nombre, entrenador, nivel o estado");
        txtBuscar.setPreferredSize(new Dimension(420, 38));
        utilidades.EstilosComponentes.aplicarCampoSimple(txtBuscar);

        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnBuscar,
                new java.awt.Color(8, 124, 255),
                new java.awt.Color(54, 207, 255),
                java.awt.Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnActualizar,
                new java.awt.Color(241, 245, 249),
                new java.awt.Color(226, 232, 240),
                new java.awt.Color(52, 74, 100)
        );

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnBuscar);
        acciones.add(btnActualizar);
        acciones.add(lblCantidad);

        barra.add(txtBuscar, BorderLayout.CENTER);
        barra.add(acciones, BorderLayout.EAST);
        return barra;
    }

    private void configurarTablaRutinas() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{
                    "N° Rutina",
                    "Rutina",
                    "Entrenador",
                    "Nivel",
                    "Semanas",
                    "Estado",
                    "Ejercicios",
                    "Detalle"
                }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        tblRutinas.setModel(modelo);
        estilizarTabla(tblRutinas);
        tblRutinas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tblRutinas.getColumnModel().getColumn(0).setPreferredWidth(80);
        tblRutinas.getColumnModel().getColumn(1).setPreferredWidth(220);
        tblRutinas.getColumnModel().getColumn(2).setPreferredWidth(210);
        tblRutinas.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblRutinas.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblRutinas.getColumnModel().getColumn(5).setPreferredWidth(100);
        tblRutinas.getColumnModel().getColumn(6).setPreferredWidth(85);
        tblRutinas.getColumnModel().getColumn(7).setPreferredWidth(120);

        tblRutinas.getColumnModel().getColumn(7)
                .setCellRenderer(new BotonDetalleRenderer());
        tblRutinas.getColumnModel().getColumn(7)
                .setCellEditor(new BotonDetalleEditor());
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setRowHeight(38);
        tabla.setShowGrid(false);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionBackground(new java.awt.Color(220, 238, 255));
        tabla.setSelectionForeground(new java.awt.Color(23, 42, 67));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tabla.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer encabezado = new DefaultTableCellRenderer();
        encabezado.setOpaque(true);
        encabezado.setBackground(new java.awt.Color(10, 58, 108));
        encabezado.setForeground(java.awt.Color.WHITE);
        encabezado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        encabezado.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getTableHeader().setDefaultRenderer(encabezado);
    }

    private void configurarEventos() {
        btnBuscar.addActionListener(e -> cargarRutinas());
        btnActualizar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarRutinas();
        });
        txtBuscar.addActionListener(e -> cargarRutinas());
    }

    private void cargarRutinas() {
        List<RutinaResumenAdministrador> rutinas = controlador.listar(
                txtBuscar.getText().trim());

        DefaultTableModel modelo = (DefaultTableModel) tblRutinas.getModel();
        modelo.setRowCount(0);

        for (RutinaResumenAdministrador rutina : rutinas) {
            modelo.addRow(new Object[]{
                rutina.getIdRutina(),
                rutina.getNombreRutina(),
                rutina.getNombreEntrenador(),
                rutina.getNivel(),
                rutina.getDuracionSemanas(),
                rutina.getEstadoRutina(),
                rutina.getTotalEjercicios(),
                "Ver detalle"
            });
        }

        lblCantidad.setText(rutinas.size() + " rutina(s)");

        if (rutinas.isEmpty() && !controlador.getMensaje().isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    controlador.getMensaje(),
                    "GYMNOVA",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void mostrarDetalleFila(int filaVista) {
        if (filaVista < 0 || filaVista >= tblRutinas.getRowCount()) {
            return;
        }

        int fila = tblRutinas.convertRowIndexToModel(filaVista);
        Object valorId = tblRutinas.getModel().getValueAt(fila, 0);
        Long idRutina = valorId instanceof Number numero
                ? numero.longValue()
                : Long.valueOf(String.valueOf(valorId));

        String nombreRutina = String.valueOf(
                tblRutinas.getModel().getValueAt(fila, 1));
        String entrenador = String.valueOf(
                tblRutinas.getModel().getValueAt(fila, 2));
        String nivel = String.valueOf(
                tblRutinas.getModel().getValueAt(fila, 3));

        Integer semanas = null;
        Object valorSemanas = tblRutinas.getModel().getValueAt(fila, 4);
        if (valorSemanas instanceof Number numero) {
            semanas = numero.intValue();
        }

        String estado = String.valueOf(
                tblRutinas.getModel().getValueAt(fila, 5));

        List<RutinaDetalleAdministrador> detalle =
                controlador.listarDetalle(idRutina);

        if (!controlador.getMensaje().isBlank()) {
            JOptionPane.showMessageDialog(
                    this, controlador.getMensaje(), "GYMNOVA",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        tblRutinas.setRowSelectionInterval(filaVista, filaVista);
        DlgDetalleRutina.mostrar(
                this, idRutina, nombreRutina, entrenador,
                nivel, semanas, estado, detalle);
    }

    private class BotonDetalleRenderer extends JButton
            implements TableCellRenderer {

        BotonDetalleRenderer() {
            setText("Ver detalle");
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            setText("Ver detalle");
            return this;
        }
    }

    private class BotonDetalleEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final JButton boton = new JButton("Ver detalle");
        private int fila;

        BotonDetalleEditor() {
            boton.setFocusPainted(false);
            boton.addActionListener((ActionEvent e) -> {
                fireEditingStopped();
                mostrarDetalleFila(fila);
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            fila = row;
            return boton;
        }

        @Override
        public Object getCellEditorValue() {
            return "Ver detalle";
        }
    }
}
