/*
 * GYMNOVA - Formulario visual de clientes.
 */
package vista;
import controlador.AutorizacionControlador;
import controlador.ClienteControlador;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.Persona;
import modelo.AlertaPulse;

public class PnlClientes extends javax.swing.JPanel {

    private final ClienteControlador controlador;
private final AutorizacionControlador autorizacionControlador;
private final List<Cliente> clientesCargados;
private DefaultTableModel modeloTabla;
private Long idPersonaSeleccionada;
private final Map<Long, AlertaPulse> indicadoresPulse = new HashMap<>();

    public PnlClientes() {
        initComponents();

        utilidades.CalendarioSelector.vincularFecha(txtFechaRegistro);

        controlador = new ClienteControlador();
autorizacionControlador = new AutorizacionControlador();
clientesCargados = new ArrayList<>();

        configurarTabla();
        configurarEstilos();
        configurarComboPersonas();

        btnBuscarClientes.addActionListener(
                evento -> cargarClientes()
        );

        tblClientes.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                seleccionarClienteTabla();
            }
        });

        refrescarDatos();
        limpiarFormulario();
    }

    private void configurarComboPersonas() {
        cboPersona.setRenderer(new RenderizadorPersona());
        cboPersona.addActionListener(evento -> actualizarCedulaPersona());
        txtCodigoCliente.setEditable(false);
        txtCodigoCliente.setToolTipText(
                "No se escribe: PostgreSQL asigna automaticamente CL00001, CL00002, etc."
        );
        txtCedulaPersona.setEditable(false);
        txtCedulaPersona.setBackground(new Color(248, 250, 252));
    }

    private void actualizarCedulaPersona() {
        Object seleccionado = cboPersona.getSelectedItem();

        if (seleccionado instanceof Persona) {
            Persona persona = (Persona) seleccionado;
            txtCedulaPersona.setText(persona.getCedula());
            utilidades.AvatarPerfil.mostrar(lblFotoPerfil, persona);
        } else {
            txtCedulaPersona.setText("");
            utilidades.AvatarPerfil.mostrar(
                    lblFotoPerfil, null, null);
        }
    }

    private void configurarTabla() {

        String[] columnas = {
            "ID",
            "C\u00f3digo",
            "C\u00e9dula",
            "Cliente",
            "Registro",
            "Peso inicial",
            "Peso meta",
            "Estado",
            "GYMNOVA Pulse"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tblClientes.setModel(modeloTabla);
        tblClientes.setRowHeight(40);
        tblClientes.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tblClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblClientes.setAutoCreateRowSorter(true);
        tblClientes.setFillsViewportHeight(true);
        tblClientes.setShowGrid(false);
        tblClientes.setIntercellSpacing(new Dimension(0, 0));
        tblClientes.setSelectionBackground(new Color(220, 238, 255));
        tblClientes.setSelectionForeground(new Color(23, 42, 67));
        tblClientes.getTableHeader().setPreferredSize(new Dimension(0, 42));
        tblClientes.getTableHeader().setReorderingAllowed(false);
        tblClientes.getTableHeader().setDefaultRenderer(
                new RenderizadorEncabezado()
        );
        tblClientes.setDefaultRenderer(Object.class, new RenderizadorFila());
        tblClientes.getColumnModel().getColumn(7).setCellRenderer(
                new RenderizadorEstado()
        );
        tblClientes.getColumnModel().getColumn(8).setCellRenderer(
                new RenderizadorPulse()
        );
        tblClientes.setToolTipText(
                "GYMNOVA Pulse explica el riesgo usando asistencia, membresia y rutina."
        );

        int[] anchos = {45, 90, 95, 170, 90, 85, 85, 75, 125};
        for (int i = 0; i < anchos.length; i++) {
            tblClientes.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(anchos[i]);
        }
    }

    private void aplicarPermisosFormulario() {

    if (idPersonaSeleccionada == null) {

        btnGuardar.setEnabled(
                tienePermiso(
                        "CLIENTES",
                        "CREAR"
                )
        );

        btnModificar.setEnabled(false);
        btnDesactivar.setEnabled(false);
        btnEliminar.setEnabled(false);

    } else {

        btnGuardar.setEnabled(false);

        btnModificar.setEnabled(
                tienePermiso(
                        "CLIENTES",
                        "MODIFICAR"
                )
        );

        btnDesactivar.setEnabled(
                tienePermiso(
                        "CLIENTES",
                        "DESACTIVAR"
                )
        );

        btnEliminar.setEnabled(
                puedeEliminarDefinitivamente()
        );
    }
}

private boolean tienePermiso(
        String modulo,
        String accion
) {

    return autorizacionControlador
            .tienePermiso(
                    modulo,
                    accion
            );
}

private boolean verificarPermisoOperacion(
        String modulo,
        String accion
) {

    boolean permitido
            = autorizacionControlador
                    .tienePermiso(
                            modulo,
                            accion
                    );

    if (permitido) {
        return true;
    }

    JOptionPane.showMessageDialog(
            this,
            autorizacionControlador.getMensaje(),
            "Acceso denegado",
            JOptionPane.WARNING_MESSAGE
    );

    return false;
}

    private void configurarEstilos() {

        utilidades.EstilosComponentes.aplicarComboRedondeado(cboPersona);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtCodigoCliente);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtFechaRegistro);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtPesoInicial);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtPesoMeta);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtCedulaPersona);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtObservaciones);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtBuscarClientes);

        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnGuardar,
                new Color(8, 124, 255),
                new Color(54, 207, 255),
                Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnModificar,
                new Color(109, 40, 217),
                new Color(168, 85, 247),
                Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnDesactivar,
                new Color(255, 214, 0),
                new Color(255, 232, 82),
                new Color(41, 31, 0)
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnEliminar,
                new Color(255, 23, 68),
                new Color(255, 91, 110),
                Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnLimpiar,
                new Color(241, 245, 249),
                new Color(226, 232, 240),
                new Color(52, 74, 100)
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnBuscarClientes,
                new Color(8, 124, 255),
                new Color(54, 207, 255),
                Color.WHITE
        );

        txtObservaciones.setFont(new Font("SansSerif", Font.PLAIN, 12));
        txtObservaciones.setForeground(new Color(23, 42, 67));
        btnDesactivar.setText("\u23FB  DESACTIVAR");
        btnDesactivar.setToolTipText(
                "Conserva el registro y deja al cliente inactivo"
        );
        btnEliminar.setText("\u2715  ELIMINAR DEFINITIVAMENTE");
        btnEliminar.setToolTipText(
                "Elimina el registro de la base de datos. Solo Administrador."
        );
    }

    private void configurarEventos() {

        btnGuardar.addActionListener(evento -> guardarCliente());
        btnModificar.addActionListener(evento -> modificarCliente());
        btnDesactivar.addActionListener(evento -> desactivarCliente());
        btnEliminar.addActionListener(evento -> eliminarCliente());
        btnLimpiar.addActionListener(evento -> limpiarFormulario());
        btnBuscarClientes.addActionListener(evento -> cargarClientes());
        txtBuscarClientes.addActionListener(evento -> cargarClientes());

        tblClientes.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                seleccionarClienteTabla();
            }
        });

        // Clic derecho sobre la tabla: reactivar el cliente seleccionado.
        // El boton "Desactivar" ya cubre la accion contraria; esto expone
        // la reactivacion sin agregar botones al layout autogenerado.
        javax.swing.JPopupMenu menu = new javax.swing.JPopupMenu();
        javax.swing.JMenuItem itemReactivar
                = new javax.swing.JMenuItem("Reactivar perfil del cliente");
        itemReactivar.addActionListener(evento -> reactivarCliente());
        menu.add(itemReactivar);
        tblClientes.setComponentPopupMenu(menu);
        tblClientes.setToolTipText(
                "Clic derecho: reactivar el perfil del cliente seleccionado.");
    }

    private void reactivarCliente() {
        if (idPersonaSeleccionada == null) {
            mostrarAdvertencia("Seleccione un cliente de la tabla.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "Desea reactivar el perfil de este cliente?",
                "Reactivar cliente", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        boolean ok = controlador.reactivar(idPersonaSeleccionada);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Cliente reactivado correctamente.",
                    "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
            cargarClientes();
        } else {
            mostrarAdvertencia(controlador.getMensaje());
        }
    }

    public final void refrescarDatos() {
        cargarClientes();

        if (idPersonaSeleccionada == null) {
            cargarPersonasDisponibles(null);
        }
    }

    private void cargarClientes() {
        indicadoresPulse.clear();
        for (AlertaPulse alerta : new controlador.PulseControlador().listar()) {
            indicadoresPulse.put(alerta.getIdCliente(), alerta);
        }
        String rol = utilidades.SesionUsuario.haySesionActiva()
                ? utilidades.SesionUsuario.getUsuarioActual().getNombreRol()
                : "";
        Long idPersona = utilidades.SesionUsuario.haySesionActiva()
                ? utilidades.SesionUsuario.getUsuarioActual().getIdPersona()
                : null;
        List<Cliente> encontrados;
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            Cliente propio = controlador.buscar(idPersona);
            encontrados = propio == null
                    ? new ArrayList<>() : java.util.List.of(propio);
        } else if ("ENTRENADOR".equalsIgnoreCase(rol)) {
            encontrados = controlador.listarPorEntrenador(
                    idPersona, txtBuscarClientes.getText());
        } else if ("NUTRICIONISTA".equalsIgnoreCase(rol)) {
            encontrados = controlador.listarPorNutricionista(
                    idPersona, txtBuscarClientes.getText());
        } else {
            encontrados = controlador.listar(txtBuscarClientes.getText());
        }

        clientesCargados.clear();
        clientesCargados.addAll(encontrados);
        modeloTabla.setRowCount(0);

        for (Cliente cliente : clientesCargados) {
            modeloTabla.addRow(new Object[]{
                cliente.getIdPersona(),
                cliente.getCodigoCliente(),
                cliente.getCedula(),
                cliente.getNombreCompleto(),
                cliente.getFechaRegistro(),
                cliente.getPesoInicial(),
                cliente.getPesoMeta(),
                cliente.isEstadoCliente() ? "Activo" : "Inactivo",
                indicadoresPulse.getOrDefault(
                        cliente.getIdPersona(), pulseNoDisponible())
            });
        }

        int cantidad = clientesCargados.size();
        lblCantidadClientes.setText(
                cantidad == 1
                ? "1 cliente encontrado"
                : cantidad + " clientes encontrados"
        );
    }

    private AlertaPulse pulseNoDisponible() {
        AlertaPulse alerta = new AlertaPulse();
        alerta.setNivel("SIN DATOS");
        alerta.setPuntuacion(0);
        return alerta;
    }

    private static class RenderizadorPulse extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla,
                Object valor, boolean seleccionado, boolean foco,
                int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionado,
                    foco, fila, columna);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(getFont().deriveFont(Font.BOLD));
            if (valor instanceof AlertaPulse alerta) {
                setText(alerta.toString());
                setToolTipText(alerta.getExplicacion());
                if (!seleccionado) {
                    setForeground(switch (alerta.getNivel()) {
                        case "CRITICO" -> new Color(220, 38, 38);
                        case "ATENCION" -> new Color(234, 88, 12);
                        default -> new Color(22, 163, 74);
                    });
                }
            }
            return this;
        }
    }

    private void cargarPersonasDisponibles(Persona personaActual) {

        DefaultComboBoxModel<Persona> modelo = new DefaultComboBoxModel<>();
        modelo.addElement(null);

        if (personaActual != null) {
            modelo.addElement(personaActual);
        }

        List<Persona> disponibles = controlador.listarPersonasDisponibles();

        for (Persona persona : disponibles) {
            if (personaActual == null
                    || !persona.getIdPersona().equals(
                            personaActual.getIdPersona()
                    )) {
                modelo.addElement(persona);
            }
        }

        cboPersona.setModel(modelo);
    }

    private void seleccionarClienteTabla() {

        int filaVista = tblClientes.getSelectedRow();

        if (filaVista < 0) {
            return;
        }

        int filaModelo = tblClientes.convertRowIndexToModel(filaVista);

        if (filaModelo < 0 || filaModelo >= clientesCargados.size()) {
            return;
        }

        cargarClienteFormulario(clientesCargados.get(filaModelo));
    }

    private void cargarClienteFormulario(
        Cliente cliente
) {

    idPersonaSeleccionada = cliente.getIdPersona();

    // Establece el cliente en atencion para los flujos guiados de
    // Recepcion, Entrenador y Nutricionista. El Administrador no lo usa.
    if (utilidades.SesionUsuario.haySesionActiva()) {
        String rol = utilidades.NavegacionRol.normalizarRol(
                utilidades.SesionUsuario.getUsuarioActual().getNombreRol());
        if (java.util.Set.of("RECEPCIONISTA", "ENTRENADOR",
                "NUTRICIONISTA").contains(rol)) {
            utilidades.ClienteEnAtencion.seleccionar(cliente);
            java.awt.Window ventana = javax.swing.SwingUtilities
                    .getWindowAncestor(this);
            if (ventana instanceof FrmPrincipal frame) {
                frame.refrescarBannerCliente();
            }
        }
    }

    cargarPersonasDisponibles(
            cliente
    );

    cboPersona.setSelectedItem(
            cliente
    );

    cboPersona.setEnabled(false);

    txtCodigoCliente.setText(
            cliente.getCodigoCliente()
    );

    txtFechaRegistro.setText(
            cliente.getFechaRegistro().toString()
    );

    txtPesoInicial.setText(
            cliente.getPesoInicial().toPlainString()
    );

    txtPesoMeta.setText(
            cliente.getPesoMeta().toPlainString()
    );

    txtCedulaPersona.setText(
            cliente.getCedula()
    );

    txtObservaciones.setText(
            cliente.getObservaciones() == null
            ? ""
            : cliente.getObservaciones()
    );

    chkEstadoCliente.setSelected(
            cliente.isEstadoCliente()
    );

    utilidades.AvatarPerfil.mostrar(
            lblFotoPerfil,
            cliente
    );

    btnGuardar.setEnabled(false);

    btnModificar.setEnabled(
            tienePermiso(
                    "CLIENTES",
                    "MODIFICAR"
            )
    );

    btnDesactivar.setEnabled(
            cliente.isEstadoCliente()
            && tienePermiso(
                    "CLIENTES",
                    "DESACTIVAR"
            )
    );

    btnEliminar.setEnabled(
            puedeEliminarDefinitivamente()
    );
}

    private boolean puedeEliminarDefinitivamente() {

    return tienePermiso(
            "CLIENTES",
            "ELIMINAR"
    )
            && utilidades.SesionUsuario.haySesionActiva()
            && "Administrador".equalsIgnoreCase(
                    utilidades.SesionUsuario
                            .getUsuarioActual()
                            .getNombreRol()
            );
}

    private Cliente obtenerClienteFormulario() {

        Persona persona = (Persona) cboPersona.getSelectedItem();

        if (persona == null) {
            throw new IllegalArgumentException("Seleccione una persona.");
        }

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(txtFechaRegistro.getText().trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "La fecha debe utilizar el formato AAAA-MM-DD."
            );
        }

        BigDecimal pesoInicial = convertirPeso(
                txtPesoInicial.getText(),
                "El peso inicial"
        );
        BigDecimal pesoMeta = convertirPeso(
                txtPesoMeta.getText(),
                "El peso meta"
        );

        Cliente cliente = new Cliente();
        cliente.setIdPersona(
                idPersonaSeleccionada == null
                ? persona.getIdPersona()
                : idPersonaSeleccionada
        );
        cliente.setCodigoCliente(txtCodigoCliente.getText());
        cliente.setFechaRegistro(fecha);
        cliente.setPesoInicial(pesoInicial);
        cliente.setPesoMeta(pesoMeta);
        cliente.setObservaciones(txtObservaciones.getText());
        cliente.setEstadoCliente(chkEstadoCliente.isSelected());

        return cliente;
    }

    private BigDecimal convertirPeso(String texto, String nombre) {
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    nombre + " debe ser un n\u00famero v\u00e1lido."
            );
        }
    }

    private void guardarCliente() {

    if (!verificarPermisoOperacion(
            "CLIENTES",
            "CREAR"
    )) {
        return;
    }

    try {
        Cliente cliente = obtenerClienteFormulario();
        boolean guardado = controlador.registrar(cliente);
        mostrarResultado(guardado);

        if (guardado) {
            cargarClientes();
            limpiarFormulario();
        }
    } catch (IllegalArgumentException e) {
        mostrarAdvertencia(e.getMessage());
    }
}

    private void modificarCliente() {

    if (!verificarPermisoOperacion(
            "CLIENTES",
            "MODIFICAR"
    )) {
        return;
    }

    if (idPersonaSeleccionada == null) {
        mostrarAdvertencia("Seleccione un cliente de la tabla.");
        return;
    }

    try {
        Cliente cliente = obtenerClienteFormulario();
        boolean modificado = controlador.modificar(cliente);
        mostrarResultado(modificado);

        if (modificado) {
            cargarClientes();
            limpiarFormulario();
        }
    } catch (IllegalArgumentException e) {
        mostrarAdvertencia(e.getMessage());
    }
}

    private void desactivarCliente() {

    if (!verificarPermisoOperacion(
            "CLIENTES",
            "DESACTIVAR"
    )) {
        return;
    }

    if (idPersonaSeleccionada == null) {
        mostrarAdvertencia("Seleccione un cliente de la tabla.");
        return;
    }

    int respuesta = JOptionPane.showConfirmDialog(
            this,
            "\u00bfDesea desactivar el cliente seleccionado?\n"
            + "El registro se conservar\u00e1 en la base de datos.",
            "Confirmar desactivaci\u00f3n",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
    );

    if (respuesta != JOptionPane.YES_OPTION) {
        return;
    }

    boolean desactivado = controlador.desactivar(idPersonaSeleccionada);
    mostrarResultado(desactivado);

    if (desactivado) {
        cargarClientes();
        limpiarFormulario();
    }
}

    private void eliminarCliente() {
        
        if (!verificarPermisoOperacion(
        "CLIENTES",
        "ELIMINAR"
)) {
    return;
}

        if (idPersonaSeleccionada == null) {
            mostrarAdvertencia("Seleccione un cliente de la tabla.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "Esta accion eliminara el cliente definitivamente.\n"
                + "La persona permanecera registrada. Desea continuar?",
                "Confirmar eliminacion definitiva",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        String codigo = txtCodigoCliente.getText().trim();
        String confirmacion = JOptionPane.showInputDialog(
                this,
                "Para confirmar, escriba el codigo: " + codigo,
                "Segunda confirmacion",
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == null || !codigo.equalsIgnoreCase(confirmacion.trim())) {
            mostrarAdvertencia(
                    "El codigo no coincide. No se elimino ningun registro."
            );
            return;
        }

        try {
            controlador.eliminarDefinitivamente(idPersonaSeleccionada);

            JOptionPane.showMessageDialog(
                    this,
                    "El cliente fue eliminado definitivamente.\n"
                    + "La persona sigue disponible en el sistema.",
                    "Eliminacion exitosa",
                    JOptionPane.INFORMATION_MESSAGE
            );

            cargarClientes();
            limpiarFormulario();

        } catch (SecurityException
                | IllegalArgumentException
                | IllegalStateException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "No se puede eliminar",
                    JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo eliminar el cliente.\nDetalle: "
                    + e.getMessage(),
                    "Error de PostgreSQL",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiarFormulario() {

    idPersonaSeleccionada = null;

    cargarPersonasDisponibles(null);

    cboPersona.setSelectedIndex(0);
    cboPersona.setEnabled(true);

        txtCodigoCliente.setText("");
        txtCodigoCliente.setEditable(false);
        txtCodigoCliente.setToolTipText(
                "El sistema asigna el codigo al guardar el cliente."
        );
    txtFechaRegistro.setText(LocalDate.now().toString());
    txtPesoInicial.setText("");
    txtPesoMeta.setText("");
    txtCedulaPersona.setText("");
    txtObservaciones.setText("");

    chkEstadoCliente.setSelected(true);

    utilidades.AvatarPerfil.mostrar(
            lblFotoPerfil,
            null,
            null
    );

    tblClientes.clearSelection();

    aplicarPermisosFormulario();
}

    private void mostrarResultado(boolean correcto) {

        JOptionPane.showMessageDialog(
                this,
                controlador.getMensaje(),
                correcto ? "GYMNOVA" : "No se complet\u00f3 la operaci\u00f3n",
                correcto
                ? JOptionPane.INFORMATION_MESSAGE
                : JOptionPane.WARNING_MESSAGE
        );
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje,
                "Revise los datos",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private static class RenderizadorPersona extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                javax.swing.JList<?> lista,
                Object valor,
                int indice,
                boolean seleccionado,
                boolean tieneFoco
        ) {
            super.getListCellRendererComponent(
                    lista,
                    valor,
                    indice,
                    seleccionado,
                    tieneFoco
            );

            setText(valor == null ? "Seleccione..." : valor.toString());
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return this;
        }
    }

    private static class RenderizadorEncabezado
            extends DefaultTableCellRenderer {

        RenderizadorEncabezado() {
            setOpaque(true);
            setBackground(new Color(10, 47, 92));
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tabla,
                Object valor,
                boolean seleccionada,
                boolean tieneFoco,
                int fila,
                int columna
        ) {
            super.getTableCellRendererComponent(
                    tabla,
                    valor,
                    seleccionada,
                    tieneFoco,
                    fila,
                    columna
            );
            setBackground(new Color(10, 47, 92));
            setForeground(Color.WHITE);
            setHorizontalAlignment(
                    columna == 0 || columna >= 4
                    ? SwingConstants.CENTER
                    : SwingConstants.LEFT
            );
            return this;
        }
    }

    private static class RenderizadorFila extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable tabla,
                Object valor,
                boolean seleccionada,
                boolean tieneFoco,
                int fila,
                int columna
        ) {
            super.getTableCellRendererComponent(
                    tabla,
                    valor,
                    seleccionada,
                    tieneFoco,
                    fila,
                    columna
            );

            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setBackground(
                    seleccionada
                    ? new Color(220, 238, 255)
                    : fila % 2 == 0
                        ? Color.WHITE
                        : new Color(247, 251, 255)
            );
            setForeground(new Color(52, 74, 100));
            setHorizontalAlignment(
                    columna == 0 || columna >= 4
                    ? SwingConstants.CENTER
                    : SwingConstants.LEFT
            );
            return this;
        }
    }

    private static class RenderizadorEstado extends DefaultTableCellRenderer {

        private Color fondoFila;
        private Color fondoEstado;

        RenderizadorEstado() {
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("SansSerif", Font.BOLD, 11));
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tabla,
                Object valor,
                boolean seleccionada,
                boolean tieneFoco,
                int fila,
                int columna
        ) {
            super.getTableCellRendererComponent(
                    tabla,
                    valor,
                    seleccionada,
                    tieneFoco,
                    fila,
                    columna
            );

            boolean activo = "Activo".equalsIgnoreCase(String.valueOf(valor));
            setText(activo ? "ACTIVO" : "INACTIVO");
            fondoFila = seleccionada
                    ? new Color(220, 238, 255)
                    : fila % 2 == 0
                        ? Color.WHITE
                        : new Color(247, 251, 255);
            fondoEstado = activo
                    ? new Color(0, 230, 118)
                    : new Color(255, 23, 68);
            setForeground(
                    activo
                    ? new Color(6, 60, 36)
                    : Color.WHITE
            );
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return this;
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            g2.setColor(fondoFila);
            g2.fillRect(0, 0, getWidth(), getHeight());

            int ancho = Math.min(
                    getWidth() - 12,
                    Math.max(
                            68,
                            getFontMetrics(getFont()).stringWidth(getText()) + 24
                    )
            );
            int alto = 24;
            int x = (getWidth() - ancho) / 2;
            int y = (getHeight() - alto) / 2;

            g2.setColor(fondoEstado);
            g2.fillRoundRect(x, y, ancho, alto, 18, 18);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBaseClientes = new javax.swing.JPanel();
        pnlEncabezadoClientes = new javax.swing.JPanel();
        lblTituloClientes = new javax.swing.JLabel();
        lblFotoPerfil = new javax.swing.JLabel();
        pnlFormularioCliente = new javax.swing.JPanel();
        lblTituloFormulario = new javax.swing.JLabel();
        lblCamposObligatorios = new javax.swing.JLabel();
        lblCodigoCliente = new javax.swing.JLabel();
        txtCodigoCliente = new javax.swing.JTextField();
        lblFechaRegistro = new javax.swing.JLabel();
        txtFechaRegistro = new javax.swing.JTextField();
        lblPesoInicial = new javax.swing.JLabel();
        txtPesoInicial = new javax.swing.JTextField();
        lblPesoMeta = new javax.swing.JLabel();
        txtPesoMeta = new javax.swing.JTextField();
        cboPersona = new javax.swing.JComboBox<>();
        lblPersona = new javax.swing.JLabel();
        lblCedulaPersona = new javax.swing.JLabel();
        txtCedulaPersona = new javax.swing.JTextField();
        lblObservaciones = new javax.swing.JLabel();
        txtObservaciones = new javax.swing.JTextField();
        chkEstadoCliente = new javax.swing.JCheckBox();
        btnGuardar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnDesactivar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        pnlTablaClientes = new javax.swing.JPanel();
        txtBuscarClientes = new javax.swing.JTextField();
        btnBuscarClientes = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        lblBuscarClientes = new javax.swing.JLabel();
        lblCantidadClientes = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(1050, 650));
        setPreferredSize(new java.awt.Dimension(1150, 720));

        pnlBaseClientes.setBackground(new java.awt.Color(234, 242, 251));

        pnlEncabezadoClientes.setBackground(new java.awt.Color(255, 255, 255));
        pnlEncabezadoClientes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlEncabezadoClientes.setPreferredSize(new java.awt.Dimension(1000, 70));

        lblTituloClientes.setFont(new java.awt.Font("SansSerif", 1, 25)); // NOI18N
        lblTituloClientes.setForeground(new java.awt.Color(23, 42, 67));
        lblTituloClientes.setText("Registro y seguimiento de clientes");
        lblTituloClientes.setPreferredSize(new java.awt.Dimension(350, 35));

        lblFotoPerfil.setForeground(new java.awt.Color(8, 124, 255));
        lblFotoPerfil.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFotoPerfil.setText("CLIENTES");
        lblFotoPerfil.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(8, 124, 255), 1, true));
        lblFotoPerfil.setPreferredSize(new java.awt.Dimension(58, 58));

        javax.swing.GroupLayout pnlEncabezadoClientesLayout = new javax.swing.GroupLayout(pnlEncabezadoClientes);
        pnlEncabezadoClientes.setLayout(pnlEncabezadoClientesLayout);
        pnlEncabezadoClientesLayout.setHorizontalGroup(
            pnlEncabezadoClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoClientesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblFotoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlEncabezadoClientesLayout.setVerticalGroup(
            pnlEncabezadoClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoClientesLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlEncabezadoClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTituloClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFotoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlFormularioCliente.setBackground(new java.awt.Color(255, 255, 255));
        pnlFormularioCliente.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlFormularioCliente.setPreferredSize(new java.awt.Dimension(1000, 285));

        lblTituloFormulario.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblTituloFormulario.setForeground(new java.awt.Color(23, 42, 67));
        lblTituloFormulario.setText("Datos del cliente");
        lblTituloFormulario.setPreferredSize(new java.awt.Dimension(300, 30));

        lblCamposObligatorios.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblCamposObligatorios.setForeground(new java.awt.Color(107, 127, 153));
        lblCamposObligatorios.setText("Los campos con * son obligatorios");

        lblCodigoCliente.setText("Código del cliente *");

        txtCodigoCliente.setPreferredSize(new java.awt.Dimension(300, 32));

        lblFechaRegistro.setText("Fecha de registro * (AAAA-MM-DD)");

        txtFechaRegistro.setPreferredSize(new java.awt.Dimension(300, 32));

        lblPesoInicial.setText("Peso inicial (kg) *");

        txtPesoInicial.setPreferredSize(new java.awt.Dimension(300, 32));

        lblPesoMeta.setText("Peso meta (kg) *");

        txtPesoMeta.setToolTipText("Ejemplo: 70.50");
        txtPesoMeta.setPreferredSize(new java.awt.Dimension(300, 32));

        cboPersona.setPreferredSize(new java.awt.Dimension(300, 32));

        lblPersona.setText("Persona registrada *");

        lblCedulaPersona.setText("Cédula de la persona");

        txtCedulaPersona.setEditable(false);
        txtCedulaPersona.setPreferredSize(new java.awt.Dimension(300, 32));

        lblObservaciones.setText("Observaciones");

        txtObservaciones.setPreferredSize(new java.awt.Dimension(300, 32));

        chkEstadoCliente.setBackground(new java.awt.Color(255, 255, 255));
        chkEstadoCliente.setForeground(new java.awt.Color(23, 42, 67));
        chkEstadoCliente.setSelected(true);
        chkEstadoCliente.setText("Cliente activo");

        btnGuardar.setBackground(new java.awt.Color(8, 124, 255));
        btnGuardar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar cliente");
        btnGuardar.setPreferredSize(new java.awt.Dimension(375, 38));
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnModificar.setBackground(new java.awt.Color(157, 78, 221));
        btnModificar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnModificar.setForeground(new java.awt.Color(255, 255, 255));
        btnModificar.setText("Modificar");
        btnModificar.setEnabled(false);
        btnModificar.setPreferredSize(new java.awt.Dimension(180, 36));
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });

        btnLimpiar.setBackground(new java.awt.Color(226, 232, 240));
        btnLimpiar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(52, 74, 100));
        btnLimpiar.setText("Limpiar formulario");
        btnLimpiar.setPreferredSize(new java.awt.Dimension(375, 36));
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnDesactivar.setBackground(new java.awt.Color(255, 214, 0));
        btnDesactivar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnDesactivar.setForeground(new java.awt.Color(41, 31, 0));
        btnDesactivar.setText("Desactivar");
        btnDesactivar.setEnabled(false);
        btnDesactivar.setPreferredSize(new java.awt.Dimension(180, 36));
        btnDesactivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDesactivarActionPerformed(evt);
            }
        });

        btnEliminar.setBackground(new java.awt.Color(255, 23, 68));
        btnEliminar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Eliminar definitivamente");
        btnEliminar.setEnabled(false);
        btnEliminar.setPreferredSize(new java.awt.Dimension(375, 36));
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFormularioClienteLayout = new javax.swing.GroupLayout(pnlFormularioCliente);
        pnlFormularioCliente.setLayout(pnlFormularioClienteLayout);
        pnlFormularioClienteLayout.setHorizontalGroup(
            pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormularioClienteLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCamposObligatorios)
                    .addGroup(pnlFormularioClienteLayout.createSequentialGroup()
                        .addGroup(pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCodigoCliente)
                            .addComponent(txtCodigoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPersona)
                            .addComponent(cboPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFechaRegistro)
                            .addComponent(txtFechaRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCedulaPersona)
                            .addComponent(txtCedulaPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPesoInicial)
                            .addComponent(txtPesoInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblObservaciones)
                            .addComponent(txtObservaciones, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPesoMeta)
                            .addComponent(txtPesoMeta, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkEstadoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlFormularioClienteLayout.createSequentialGroup()
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnDesactivar, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        pnlFormularioClienteLayout.setVerticalGroup(
            pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormularioClienteLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblTituloFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblCamposObligatorios)
                .addGap(13, 13, 13)
                .addGroup(pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCodigoCliente)
                    .addComponent(lblFechaRegistro)
                    .addComponent(lblPesoInicial)
                    .addComponent(lblPesoMeta))
                .addGap(5, 5, 5)
                .addGroup(pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCodigoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFechaRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPesoInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPesoMeta, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPersona)
                    .addComponent(lblCedulaPersona)
                    .addComponent(lblObservaciones))
                .addGap(5, 5, 5)
                .addGroup(pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCedulaPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtObservaciones, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkEstadoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(pnlFormularioClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDesactivar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pnlTablaClientes.setBackground(new java.awt.Color(255, 255, 255));
        pnlTablaClientes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlTablaClientes.setPreferredSize(new java.awt.Dimension(1000, 400));

        txtBuscarClientes.setToolTipText("Buscar por código, cédula, nombre, apellido o correo");
        txtBuscarClientes.setPreferredSize(new java.awt.Dimension(400, 38));
        txtBuscarClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarClientesActionPerformed(evt);
            }
        });

        btnBuscarClientes.setBackground(new java.awt.Color(8, 124, 255));
        btnBuscarClientes.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnBuscarClientes.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscarClientes.setText("Buscar");
        btnBuscarClientes.setPreferredSize(new java.awt.Dimension(100, 38));

        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"ID", "Código", "Cédula", "Cliente", "Registro", "Peso inicial", "Peso meta", "Estado"}
        ));
        jScrollPane1.setViewportView(tblClientes);

        lblBuscarClientes.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblBuscarClientes.setForeground(new java.awt.Color(107, 127, 153));
        lblBuscarClientes.setText("Buscar por código, cédula, nombres, apellidos o correo");
        lblBuscarClientes.setPreferredSize(new java.awt.Dimension(500, 20));

        lblCantidadClientes.setText("0 registros encontrados");

        javax.swing.GroupLayout pnlTablaClientesLayout = new javax.swing.GroupLayout(pnlTablaClientes);
        pnlTablaClientes.setLayout(pnlTablaClientesLayout);
        pnlTablaClientesLayout.setHorizontalGroup(
            pnlTablaClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTablaClientesLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlTablaClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBuscarClientes)
                    .addGroup(pnlTablaClientesLayout.createSequentialGroup()
                        .addComponent(txtBuscarClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                        .addGap(12, 12, 12)
                        .addComponent(btnBuscarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblCantidadClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1090, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlTablaClientesLayout.setVerticalGroup(
            pnlTablaClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTablaClientesLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblBuscarClientes)
                .addGap(6, 6, 6)
                .addGroup(pnlTablaClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantidadClientes))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );

        javax.swing.GroupLayout pnlBaseClientesLayout = new javax.swing.GroupLayout(pnlBaseClientes);
        pnlBaseClientes.setLayout(pnlBaseClientesLayout);
        pnlBaseClientesLayout.setHorizontalGroup(
            pnlBaseClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlEncabezadoClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 1175, Short.MAX_VALUE)
            .addGroup(pnlBaseClientesLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlBaseClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFormularioCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTablaClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlBaseClientesLayout.setVerticalGroup(
            pnlBaseClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBaseClientesLayout.createSequentialGroup()
                .addComponent(pnlEncabezadoClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(pnlFormularioCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(pnlTablaClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBaseClientes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBaseClientes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarCliente();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void txtBuscarClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarClientesActionPerformed
        cargarClientes();
    }//GEN-LAST:event_txtBuscarClientesActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarFormulario();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        modificarCliente();
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnDesactivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDesactivarActionPerformed
        desactivarCliente();
    }//GEN-LAST:event_btnDesactivarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminarCliente();
    }//GEN-LAST:event_btnEliminarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarClientes;
    private javax.swing.JButton btnDesactivar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JComboBox<modelo.Persona> cboPersona;
    private javax.swing.JCheckBox chkEstadoCliente;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBuscarClientes;
    private javax.swing.JLabel lblCamposObligatorios;
    private javax.swing.JLabel lblCantidadClientes;
    private javax.swing.JLabel lblCedulaPersona;
    private javax.swing.JLabel lblCodigoCliente;
    private javax.swing.JLabel lblFechaRegistro;
    private javax.swing.JLabel lblFotoPerfil;
    private javax.swing.JLabel lblObservaciones;
    private javax.swing.JLabel lblPersona;
    private javax.swing.JLabel lblPesoInicial;
    private javax.swing.JLabel lblPesoMeta;
    private javax.swing.JLabel lblTituloClientes;
    private javax.swing.JLabel lblTituloFormulario;
    private javax.swing.JPanel pnlBaseClientes;
    private javax.swing.JPanel pnlEncabezadoClientes;
    private javax.swing.JPanel pnlFormularioCliente;
    private javax.swing.JPanel pnlTablaClientes;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTextField txtBuscarClientes;
    private javax.swing.JTextField txtCedulaPersona;
    private javax.swing.JTextField txtCodigoCliente;
    private javax.swing.JTextField txtFechaRegistro;
    private javax.swing.JTextField txtObservaciones;
    private javax.swing.JTextField txtPesoInicial;
    private javax.swing.JTextField txtPesoMeta;
    // End of variables declaration//GEN-END:variables
}
