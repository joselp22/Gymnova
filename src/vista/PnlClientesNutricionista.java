package vista;

import controlador.NutricionistaWorkspaceControlador;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.PlanNutricional;
import utilidades.ClienteEnAtencion;

/** Pantalla de selección y consulta de clientes del Nutricionista. */
public class PnlClientesNutricionista extends JPanel {

    private final NutricionistaWorkspaceControlador controlador
            = new NutricionistaWorkspaceControlador();
    private final Runnable alSeleccionarCliente;

    private final JTextField txtBuscar = NutricionistaUI.campo();
    private final JTable tabla = new JTable();
    private final JLabel lblCantidad = new JLabel("0 clientes");
    private final JLabel lblNombre = new JLabel("Seleccione un cliente");
    private final JLabel lblCodigo = new JLabel("Código: —");
    private final JLabel lblCedula = new JLabel("Cédula: —");
    private final JLabel lblContacto = new JLabel("Contacto: —");
    private final JLabel lblPeso = new JLabel("Peso inicial / meta: —");
    private final JLabel lblPlan = new JLabel("Plan activo: —");
    private final JLabel lblAtencion = new JLabel();
    private final JButton btnAtender = NutricionistaUI.boton("Atender cliente");

    private List<Cliente> clientes = new ArrayList<>();
    private Cliente seleccionado;

    public PnlClientesNutricionista() {
        this(null);
    }

    public PnlClientesNutricionista(Runnable alSeleccionarCliente) {
        this.alSeleccionarCliente = alSeleccionarCliente;
        construir();
        eventos();
        refrescarDatos();
    }

    private void construir() {
        setLayout(new BorderLayout());
        setBackground(NutricionistaUI.FONDO);
        JPanel pagina = NutricionistaUI.pagina();
        pagina.add(NutricionistaUI.encabezado(
                "Mis clientes",
                "Consulte sus clientes y seleccione con quién va a trabajar."),
                BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);

        JPanel buscador = NutricionistaUI.tarjeta();
        buscador.setLayout(new BorderLayout(10, 0));
        JPanel izquierda = new JPanel(new BorderLayout(8, 0));
        izquierda.setOpaque(false);
        txtBuscar.setToolTipText("Buscar por código, cédula, nombre, apellido o correo");
        izquierda.add(txtBuscar, BorderLayout.CENTER);
        JButton btnBuscar = NutricionistaUI.boton("Buscar");
        izquierda.add(btnBuscar, BorderLayout.EAST);
        buscador.add(izquierda, BorderLayout.CENTER);
        buscador.add(lblCantidad, BorderLayout.EAST);
        btnBuscar.addActionListener(e -> cargarTabla());
        txtBuscar.addActionListener(e -> cargarTabla());
        cuerpo.add(buscador, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 14, 0));
        centro.setOpaque(false);

        JPanel listado = NutricionistaUI.tarjeta();
        listado.setLayout(new BorderLayout(0, 10));
        listado.add(NutricionistaUI.tituloSeccion("Clientes disponibles"), BorderLayout.NORTH);
        NutricionistaUI.tabla(tabla);
        listado.add(NutricionistaUI.scrollTabla(tabla), BorderLayout.CENTER);
        centro.add(listado);

        JPanel detalle = NutricionistaUI.tarjeta();
        detalle.setLayout(new BorderLayout(0, 14));
        detalle.add(NutricionistaUI.tituloSeccion("Ficha rápida"), BorderLayout.NORTH);
        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new javax.swing.BoxLayout(datos, javax.swing.BoxLayout.Y_AXIS));
        lblNombre.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 19));
        lblNombre.setForeground(NutricionistaUI.TEXTO);
        lblAtencion.setForeground(NutricionistaUI.AZUL);
        datos.add(lblNombre);
        datos.add(javax.swing.Box.createVerticalStrut(12));
        datos.add(lblCodigo);
        datos.add(javax.swing.Box.createVerticalStrut(8));
        datos.add(lblCedula);
        datos.add(javax.swing.Box.createVerticalStrut(8));
        datos.add(lblContacto);
        datos.add(javax.swing.Box.createVerticalStrut(8));
        datos.add(lblPeso);
        datos.add(javax.swing.Box.createVerticalStrut(8));
        datos.add(lblPlan);
        datos.add(javax.swing.Box.createVerticalStrut(8));
        datos.add(lblAtencion);
        detalle.add(datos, BorderLayout.CENTER);

        btnAtender.setEnabled(false);
        JButton btnLimpiar = NutricionistaUI.botonSecundario("Limpiar selección");
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(btnAtender);
        acciones.add(btnLimpiar);
        detalle.add(acciones, BorderLayout.SOUTH);
        btnLimpiar.addActionListener(e -> limpiarSeleccion());
        centro.add(detalle);

        cuerpo.add(centro, BorderLayout.CENTER);
        pagina.add(cuerpo, BorderLayout.CENTER);
        add(pagina, BorderLayout.CENTER);
    }

    private void eventos() {
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });
        btnAtender.addActionListener(e -> seleccionarParaAtencion());
    }

    public void refrescarDatos() {
        cargarTabla();
        actualizarAtencion();
    }

    private void cargarTabla() {
        clientes = controlador.listarMisClientes(txtBuscar.getText());
        DefaultTableModel m = modelo(new String[]{
            "Código", "Cédula", "Cliente", "Teléfono", "Peso inicial", "Peso meta", "Estado"
        });
        for (Cliente c : clientes) {
            m.addRow(new Object[]{
                c.getCodigoCliente(), c.getCedula(), c.getNombreCompleto(),
                c.getTelefono(), c.getPesoInicial(), c.getPesoMeta(),
                c.isEstadoCliente() ? "ACTIVO" : "INACTIVO"
            });
        }
        tabla.setModel(m);
        lblCantidad.setText(clientes.size() + (clientes.size() == 1 ? " cliente" : " clientes"));
        seleccionado = null;
        btnAtender.setEnabled(false);
        mostrarDetalle(null);
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        int modelo = tabla.convertRowIndexToModel(fila);
        if (modelo < 0 || modelo >= clientes.size()) return;
        seleccionado = clientes.get(modelo);
        btnAtender.setEnabled(seleccionado.isEstadoCliente());
        mostrarDetalle(seleccionado);
    }

    private void mostrarDetalle(Cliente c) {
        if (c == null) {
            lblNombre.setText("Seleccione un cliente");
            lblCodigo.setText("Código: —");
            lblCedula.setText("Cédula: —");
            lblContacto.setText("Contacto: —");
            lblPeso.setText("Peso inicial / meta: —");
            lblPlan.setText("Plan activo: —");
            actualizarAtencion();
            return;
        }
        lblNombre.setText(c.getNombreCompleto());
        lblCodigo.setText("Código: " + c.getCodigoCliente());
        lblCedula.setText("Cédula: " + c.getCedula());
        lblContacto.setText("Contacto: " + valor(c.getTelefono()) + " · " + valor(c.getCorreo()));
        lblPeso.setText("Peso inicial / meta: " + valor(c.getPesoInicial()) + " kg / "
                + valor(c.getPesoMeta()) + " kg");
        PlanNutricional plan = controlador.planActivo(c.getIdPersona());
        lblPlan.setText(plan == null ? "Plan activo: ninguno"
                : "Plan activo: " + plan.getNombrePlan() + " · " + plan.getEstadoPlan());
        actualizarAtencion();
    }

    private void seleccionarParaAtencion() {
        if (!controlador.seleccionarCliente(seleccionado)) {
            javax.swing.JOptionPane.showMessageDialog(this, controlador.getMensaje(),
                    "GYMNOVA", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        actualizarAtencion();
        javax.swing.JOptionPane.showMessageDialog(this, controlador.getMensaje(),
                "GYMNOVA", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        if (alSeleccionarCliente != null) alSeleccionarCliente.run();
    }

    private void actualizarAtencion() {
        if (!ClienteEnAtencion.hay()) {
            lblAtencion.setText("Cliente en atención: ninguno");
        } else {
            lblAtencion.setText("Cliente en atención: " + ClienteEnAtencion.descripcion());
        }
    }

    private void limpiarSeleccion() {
        tabla.clearSelection();
        seleccionado = null;
        btnAtender.setEnabled(false);
        mostrarDetalle(null);
    }

    private String valor(Object o) {
        return o == null || o.toString().isBlank() ? "—" : o.toString();
    }

    private DefaultTableModel modelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }
}
