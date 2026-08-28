package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.RutinaDetalleAdministrador;

/**
 * Ventana compacta y reutilizable para consultar el detalle de una rutina.
 * No realiza consultas ni modificaciones: recibe el detalle ya cargado por
 * el controlador de la vista que la invoca.
 */
public final class DlgDetalleRutina extends JDialog {

    private final JTable tblDetalle = new JTable();

    private DlgDetalleRutina(
            Window owner,
            Long idRutina,
            String nombreRutina,
            String entrenador,
            String nivel,
            Integer semanas,
            String estado,
            List<RutinaDetalleAdministrador> detalle
    ) {
        super(owner, "Detalle de rutina", ModalityType.APPLICATION_MODAL);
        construirVista(idRutina, nombreRutina, entrenador,
                nivel, semanas, estado, detalle);
    }

    public static void mostrar(
            Component parent,
            Long idRutina,
            String nombreRutina,
            String entrenador,
            String nivel,
            Integer semanas,
            String estado,
            List<RutinaDetalleAdministrador> detalle
    ) {
        Window owner = parent == null
                ? null
                : SwingUtilities.getWindowAncestor(parent);

        DlgDetalleRutina dialogo = new DlgDetalleRutina(
                owner,
                idRutina,
                nombreRutina,
                entrenador,
                nivel,
                semanas,
                estado,
                detalle
        );
        dialogo.setLocationRelativeTo(parent);
        dialogo.setVisible(true);
    }

    private void construirVista(
            Long idRutina,
            String nombreRutina,
            String entrenador,
            String nivel,
            Integer semanas,
            String estado,
            List<RutinaDetalleAdministrador> detalle
    ) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(820, 430));
        setPreferredSize(new Dimension(930, 500));
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new java.awt.Color(234, 242, 251));

        JPanel cabecera = new JPanel();
        cabecera.setBackground(java.awt.Color.WHITE);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        cabecera.setLayout(new javax.swing.BoxLayout(
                cabecera, javax.swing.BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel(
                "Rutina N° " + idRutina + " - " + valor(nombreRutina));
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(new java.awt.Color(23, 42, 67));

        JLabel datos = new JLabel(
                "Entrenador: " + valor(entrenador)
                + "   |   Nivel: " + valor(nivel)
                + "   |   Semanas: " + (semanas == null ? "-" : semanas)
                + "   |   Estado: " + valor(estado));
        datos.setForeground(new java.awt.Color(90, 110, 135));

        JLabel cantidad = new JLabel(
                (detalle == null ? 0 : detalle.size()) + " ejercicio(s) en la rutina");
        cantidad.setForeground(new java.awt.Color(8, 124, 255));

        cabecera.add(titulo);
        cabecera.add(javax.swing.Box.createVerticalStrut(5));
        cabecera.add(datos);
        cabecera.add(javax.swing.Box.createVerticalStrut(3));
        cabecera.add(cantidad);

        configurarTabla();
        cargarDetalle(detalle);

        JScrollPane scroll = new JScrollPane(tblDetalle);
        scroll.setBorder(BorderFactory.createLineBorder(
                new java.awt.Color(212, 225, 239)));

        JPanel pie = new JPanel(new java.awt.FlowLayout(
                java.awt.FlowLayout.RIGHT, 8, 0));
        pie.setOpaque(false);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setPreferredSize(new Dimension(110, 34));
        btnCerrar.addActionListener(e -> dispose());
        pie.add(btnCerrar);

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenido.add(cabecera, BorderLayout.NORTH);
        contenido.add(scroll, BorderLayout.CENTER);
        contenido.add(pie, BorderLayout.SOUTH);

        add(contenido, BorderLayout.CENTER);
        pack();
    }

    private void configurarTabla() {
        tblDetalle.setModel(new DefaultTableModel(
                new Object[]{
                    "Día",
                    "Orden",
                    "Ejercicio",
                    "Tipo",
                    "Nivel",
                    "Series",
                    "Repeticiones",
                    "Peso",
                    "Duración",
                    "Descanso"
                }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        tblDetalle.setRowHeight(34);
        tblDetalle.setShowGrid(false);
        tblDetalle.setFillsViewportHeight(true);
        tblDetalle.setSelectionBackground(new java.awt.Color(220, 238, 255));
        tblDetalle.setSelectionForeground(new java.awt.Color(23, 42, 67));
        tblDetalle.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tblDetalle.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer encabezado = new DefaultTableCellRenderer();
        encabezado.setOpaque(true);
        encabezado.setBackground(new java.awt.Color(10, 58, 108));
        encabezado.setForeground(java.awt.Color.WHITE);
        encabezado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        encabezado.setHorizontalAlignment(SwingConstants.CENTER);
        tblDetalle.getTableHeader().setDefaultRenderer(encabezado);
    }

    private void cargarDetalle(List<RutinaDetalleAdministrador> detalle) {
        DefaultTableModel modelo = (DefaultTableModel) tblDetalle.getModel();
        modelo.setRowCount(0);

        if (detalle == null || detalle.isEmpty()) {
            modelo.addRow(new Object[]{
                "-", "-", "Sin ejercicios registrados", "-", "-",
                "-", "-", "-", "-", "-"
            });
            return;
        }

        for (RutinaDetalleAdministrador item : detalle) {
            modelo.addRow(new Object[]{
                item.getDiaSemana(),
                item.getOrden(),
                item.getNombreEjercicio(),
                item.getTipoEjercicio(),
                item.getNivelDificultad(),
                item.getSeries(),
                item.getRepeticiones(),
                item.getPesoSugerido(),
                item.getDuracionMinutos(),
                item.getDescansoSegundos()
            });
        }
    }

    private String valor(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }
}
