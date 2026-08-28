/*
 * GYMNOVA - Vista de gestion del personal.
 * La distribucion visual se mantiene en PnlPersonal.form.
 */
package vista;

import controlador.AutorizacionControlador;
import controlador.EmpleadoControlador;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.Empleado;
import modelo.Persona;
import utilidades.GestorConsultaModulo;

public class PnlPersonal extends javax.swing.JPanel {

    private final EmpleadoControlador controlador;
    private final AutorizacionControlador autorizacionControlador;
    private final List<Empleado> empleadosCargados;
    private DefaultTableModel modeloTabla;
    private Long idPersonaSeleccionada;
    private final javax.swing.JComboBox<String> selectorSubmodulo;
    private final GestorConsultaModulo gestorComplementario;
    private String submoduloActual;

    public PnlPersonal() {
        initComponents();

        utilidades.CalendarioSelector.vincularFecha(txtFechaIngreso);

        controlador = new EmpleadoControlador();
        autorizacionControlador = new AutorizacionControlador();
        empleadosCargados = new ArrayList<>();
        selectorSubmodulo = new javax.swing.JComboBox<>(
                GestorConsultaModulo.controladoresPara("PERSONAL")
                        .keySet().toArray(String[]::new)
        );
        gestorComplementario = new GestorConsultaModulo(
                this, selectorSubmodulo, tblPersonal, txtBuscarPersonal,
                lblCantidadPersonal, "PERSONAL",
                GestorConsultaModulo.controladoresPara("PERSONAL")
        );
        submoduloActual = "Empleados";

        configurarTabla();
        configurarComboPersonas();
        configurarCampos();
        configurarEstilos();
        configurarSelectorSubmodulo();

        btnBuscarPersonal.addActionListener(evento -> cargarPersonal());
        tblPersonal.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                if (esModoEmpleados()) {
                    seleccionarEmpleadoTabla();
                }
            }
        });

        refrescarDatos();
        limpiarFormulario();
    }

    private void configurarSelectorSubmodulo() {
        lblModuloPersonal.setText("PERSONAL  ▾");
        lblModuloPersonal.setToolTipText(
                "Seleccione empleados, entrenadores, nutricionistas o certificaciones"
        );
        lblModuloPersonal.setCursor(
                new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
        );
        lblModuloPersonal.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evento) {
                seleccionarSubmodulo();
            }
        });
    }

    private void seleccionarSubmodulo() {
        Object opcion = JOptionPane.showInputDialog(
                this,
                "Seleccione el registro de personal que desea administrar:",
                "Modulo Personal",
                JOptionPane.PLAIN_MESSAGE,
                null,
                utilidades.GestorConsultaModulo.controladoresPara("PERSONAL")
                        .keySet().toArray(),
                submoduloActual
        );
        if (opcion == null) {
            return;
        }
        submoduloActual = opcion.toString();
        selectorSubmodulo.setSelectedItem(submoduloActual);
        lblTituloFormulario.setText("Datos de " + submoduloActual.toLowerCase());
        lblTituloPersonal.setText("Gestion de personal - " + submoduloActual);
        habilitarFormularioEmpleado(esModoEmpleados());
        limpiarFormulario();
        refrescarDatos();
    }

    private void habilitarFormularioEmpleado(boolean habilitar) {
        txtFechaIngreso.setEnabled(habilitar);
        cboTipoContrato.setEnabled(habilitar);
        txtSalario.setEnabled(habilitar);
        cboPersona.setEnabled(habilitar);
        txtTurno.setEnabled(habilitar);
        chkEstadoEmpleado.setEnabled(habilitar);
    }

    private boolean esModoEmpleados() {
        return "Empleados".equals(submoduloActual);
    }

    private void configurarCampos() {
        txtCodigoEmpleado.setEditable(false);
        txtCedulaPersona.setEditable(false);
        txtCodigoEmpleado.setBackground(new Color(248, 250, 252));
        txtCedulaPersona.setBackground(new Color(248, 250, 252));
        cboTipoContrato.setToolTipText(
                "Tiempo completo, Medio tiempo, Temporal o Servicios profesionales"
        );
        txtTurno.setToolTipText("Ejemplo: 08:00 - 17:00");
    }

    private void configurarEstilos() {
        utilidades.EstilosComponentes.aplicarComboRedondeado(cboPersona);
        utilidades.EstilosComponentes.aplicarComboRedondeado(cboTipoContrato);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtCodigoEmpleado);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtFechaIngreso);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtSalario);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtCedulaPersona);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtTurno);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtBuscarPersonal);

        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnGuardar, new Color(8, 124, 255),
                new Color(54, 207, 255), Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnModificar, new Color(109, 40, 217),
                new Color(168, 85, 247), Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnDesactivar, new Color(255, 214, 0),
                new Color(255, 232, 82), new Color(41, 31, 0)
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnEliminar, new Color(255, 23, 68),
                new Color(255, 91, 110), Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnLimpiar, new Color(241, 245, 249),
                new Color(226, 232, 240), new Color(52, 74, 100)
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnBuscarPersonal, new Color(8, 124, 255),
                new Color(54, 207, 255), Color.WHITE
        );

        btnDesactivar.setText("\u23FB  DESACTIVAR");
        btnEliminar.setText("\u2715  ELIMINAR DEFINITIVAMENTE");
    }

    private void configurarComboPersonas() {
        cboPersona.setRenderer(new RenderizadorPersona());
        cboPersona.addActionListener(evento -> actualizarCedulaPersona());
    }

    private void actualizarCedulaPersona() {
        Object seleccionado = cboPersona.getSelectedItem();
        if (seleccionado instanceof Persona) {
            txtCedulaPersona.setText(((Persona) seleccionado).getCedula());
        } else {
            txtCedulaPersona.setText("");
        }
    }

    private void configurarTabla() {
        String[] columnas = {
            "ID", "Codigo", "Cedula", "Empleado", "Ingreso",
            "Contrato", "Horario", "Salario", "Estado"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tblPersonal.setModel(modeloTabla);
        tblPersonal.setRowHeight(40);
        tblPersonal.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tblPersonal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblPersonal.setAutoCreateRowSorter(true);
        tblPersonal.setFillsViewportHeight(true);
        tblPersonal.setShowGrid(false);
        tblPersonal.setIntercellSpacing(new Dimension(0, 0));
        tblPersonal.setSelectionBackground(new Color(220, 238, 255));
        tblPersonal.setSelectionForeground(new Color(23, 42, 67));
        tblPersonal.getTableHeader().setPreferredSize(new Dimension(0, 42));
        tblPersonal.getTableHeader().setReorderingAllowed(false);
        tblPersonal.getTableHeader().setDefaultRenderer(new RenderizadorEncabezado());
        tblPersonal.setDefaultRenderer(Object.class, new RenderizadorFila());
        tblPersonal.getColumnModel().getColumn(8).setCellRenderer(new RenderizadorEstado());

        int[] anchos = {45, 85, 95, 170, 95, 140, 120, 90, 80};
        for (int i = 0; i < anchos.length; i++) {
            tblPersonal.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    public final void refrescarDatos() {
        if (esModoEmpleados()) {
            cargarPersonasDisponibles();
            cargarPersonal();
        } else {
            gestorComplementario.cargar();
        }
    }

    private void cargarPersonasDisponibles() {
        DefaultComboBoxModel<Persona> modelo = new DefaultComboBoxModel<>();
        modelo.addElement(null);
        for (Persona persona : controlador.listarPersonasDisponibles()) {
            modelo.addElement(persona);
        }
        cboPersona.setModel(modelo);
    }

    private void cargarPersonal() {
        if (!esModoEmpleados()) {
            gestorComplementario.cargar();
            return;
        }
        String criterio = txtBuscarPersonal.getText().trim();
        empleadosCargados.clear();
        empleadosCargados.addAll(controlador.listar(criterio));
        modeloTabla.setRowCount(0);

        for (Empleado empleado : empleadosCargados) {
            modeloTabla.addRow(new Object[]{
                empleado.getIdPersona(),
                empleado.getCodigoEmpleado(),
                empleado.getCedula(),
                empleado.getNombreCompleto(),
                empleado.getFechaIngreso(),
                empleado.getTipoContrato(),
                empleado.getTurno(),
                empleado.getSalario(),
                empleado.isEstadoEmpleado() ? "ACTIVO" : "INACTIVO"
            });
        }

        int cantidad = empleadosCargados.size();
        lblCantidadPersonal.setText(
                cantidad + (cantidad == 1 ? " empleado encontrado" : " empleados encontrados")
        );
    }

    private void seleccionarEmpleadoTabla() {
        int filaVista = tblPersonal.getSelectedRow();
        if (filaVista < 0) {
            return;
        }

        int filaModelo = tblPersonal.convertRowIndexToModel(filaVista);
        Empleado empleado = empleadosCargados.get(filaModelo);
        idPersonaSeleccionada = empleado.getIdPersona();

        txtCodigoEmpleado.setText(empleado.getCodigoEmpleado());
        txtFechaIngreso.setText(String.valueOf(empleado.getFechaIngreso()));
        cboTipoContrato.setSelectedItem(empleado.getTipoContrato());
        txtSalario.setText(String.valueOf(empleado.getSalario()));
        txtTurno.setText(empleado.getTurno());
        chkEstadoEmpleado.setSelected(empleado.isEstadoEmpleado());

        seleccionarPersona(empleado);
        cboPersona.setEnabled(false);
        aplicarPermisosFormulario();
    }

    private void seleccionarPersona(Empleado empleado) {
        DefaultComboBoxModel<Persona> modelo
                = (DefaultComboBoxModel<Persona>) cboPersona.getModel();
        Persona encontrada = null;

        for (int i = 0; i < modelo.getSize(); i++) {
            Persona persona = modelo.getElementAt(i);
            if (persona != null && persona.getIdPersona().equals(empleado.getIdPersona())) {
                encontrada = persona;
                break;
            }
        }

        if (encontrada == null) {
            modelo.addElement(empleado);
            encontrada = empleado;
        }
        cboPersona.setSelectedItem(encontrada);
    }

    private Empleado obtenerEmpleadoFormulario() {
        Object seleccionado = cboPersona.getSelectedItem();
        if (!(seleccionado instanceof Persona)) {
            throw new IllegalArgumentException("Seleccione una persona registrada.");
        }

        String[] horario = txtTurno.getText().trim().split("\\s*-\\s*");
        if (horario.length != 2) {
            throw new IllegalArgumentException("Ingrese el horario como 08:00 - 17:00.");
        }

        Persona persona = (Persona) seleccionado;
        Empleado empleado = new Empleado();
        empleado.setIdPersona(persona.getIdPersona());
        empleado.setCodigoEmpleado(txtCodigoEmpleado.getText().trim());
        empleado.setFechaIngreso(LocalDate.parse(txtFechaIngreso.getText().trim()));
        empleado.setTipoContrato(String.valueOf(cboTipoContrato.getSelectedItem()));
        empleado.setSalario(new BigDecimal(txtSalario.getText().trim()));
        empleado.setHoraInicio(LocalTime.parse(horario[0]));
        empleado.setHoraFin(LocalTime.parse(horario[1]));
        empleado.setEstadoEmpleado(chkEstadoEmpleado.isSelected());
        return empleado;
    }

    private void guardarEmpleado() {
        try {
            Empleado empleado = obtenerEmpleadoFormulario();
            empleado.setCodigoEmpleado(null);
            if (controlador.registrar(empleado)) {
                mostrarInformacion(controlador.getMensaje());
                limpiarFormulario();
                refrescarDatos();
            } else {
                mostrarAdvertencia(controlador.getMensaje());
            }
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            mostrarAdvertencia(mensajeEntrada(ex));
        }
    }

    private void modificarEmpleado() {
        if (idPersonaSeleccionada == null) {
            mostrarAdvertencia("Seleccione un empleado de la tabla.");
            return;
        }

        try {
            Empleado empleado = obtenerEmpleadoFormulario();
            empleado.setIdPersona(idPersonaSeleccionada);
            if (controlador.modificar(empleado)) {
                mostrarInformacion(controlador.getMensaje());
                limpiarFormulario();
                refrescarDatos();
            } else {
                mostrarAdvertencia(controlador.getMensaje());
            }
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            mostrarAdvertencia(mensajeEntrada(ex));
        }
    }

    private void desactivarEmpleado() {
        if (idPersonaSeleccionada == null) {
            mostrarAdvertencia("Seleccione un empleado de la tabla.");
            return;
        }
        if (confirmar("Desea desactivar al empleado seleccionado?")) {
            if (controlador.desactivar(idPersonaSeleccionada)) {
                mostrarInformacion(controlador.getMensaje());
                limpiarFormulario();
                refrescarDatos();
            } else {
                mostrarAdvertencia(controlador.getMensaje());
            }
        }
    }

    private void eliminarEmpleado() {
        if (idPersonaSeleccionada == null) {
            mostrarAdvertencia("Seleccione un empleado de la tabla.");
            return;
        }
        if (!confirmar("Esta accion es definitiva. Desea continuar?")) {
            return;
        }

        try {
            controlador.eliminarDefinitivamente(idPersonaSeleccionada);
            mostrarInformacion("Empleado eliminado definitivamente.");
            limpiarFormulario();
            refrescarDatos();
        } catch (SQLException | IllegalArgumentException | SecurityException ex) {
            mostrarAdvertencia(ex.getMessage());
        }
    }

    private void limpiarFormulario() {
        idPersonaSeleccionada = null;
        txtCodigoEmpleado.setText("");
        txtCodigoEmpleado.setEditable(false);
        txtCodigoEmpleado.setToolTipText(
                "El sistema asigna el codigo al guardar el empleado."
        );
        txtFechaIngreso.setText(LocalDate.now().toString());
        cboTipoContrato.setSelectedIndex(0);
        txtSalario.setText("");
        txtTurno.setText("08:00 - 17:00");
        chkEstadoEmpleado.setSelected(true);
        tblPersonal.clearSelection();
        cboPersona.setEnabled(esModoEmpleados());
        if (esModoEmpleados()) {
            cargarPersonasDisponibles();
        }
        aplicarPermisosFormulario();
    }

    private void aplicarPermisosFormulario() {
        if (!esModoEmpleados()) {
            btnGuardar.setEnabled(tienePermiso("PERSONAL", "CREAR"));
            btnModificar.setEnabled(tienePermiso("PERSONAL", "MODIFICAR"));
            btnDesactivar.setEnabled(tienePermiso("PERSONAL", "DESACTIVAR"));
            btnEliminar.setEnabled(puedeEliminarDefinitivamente());
            return;
        }
        boolean nuevo = idPersonaSeleccionada == null;
        btnGuardar.setEnabled(nuevo && tienePermiso("PERSONAL", "CREAR"));
        btnModificar.setEnabled(!nuevo && tienePermiso("PERSONAL", "MODIFICAR"));
        btnDesactivar.setEnabled(!nuevo && tienePermiso("PERSONAL", "DESACTIVAR"));
        btnEliminar.setEnabled(!nuevo && puedeEliminarDefinitivamente());
    }

    private boolean tienePermiso(String modulo, String accion) {
        return autorizacionControlador.tienePermiso(modulo, accion);
    }

    private boolean puedeEliminarDefinitivamente() {
        return utilidades.SesionUsuario.haySesionActiva()
                && "Administrador".equalsIgnoreCase(
                        utilidades.SesionUsuario.getUsuarioActual().getNombreRol()
                );
    }

    private boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(
                this, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }

    private void mostrarInformacion(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "GYMNOVA", JOptionPane.WARNING_MESSAGE);
    }

    private String mensajeEntrada(Exception ex) {
        if (ex instanceof DateTimeParseException) {
            return "Revise la fecha y el horario. Use AAAA-MM-DD y HH:mm.";
        }
        if (ex instanceof NumberFormatException) {
            return "El salario debe ser un numero valido.";
        }
        return ex.getMessage();
    }

    private static class RenderizadorPersona extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                javax.swing.JList<?> lista, Object valor, int indice,
                boolean seleccionado, boolean foco
        ) {
            super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
            if (valor instanceof Persona) {
                Persona persona = (Persona) valor;
                setText(persona.getCedula() + " - " + persona.getNombreCompleto());
            } else {
                setText("Seleccione una persona...");
            }
            return this;
        }
    }

    private static class RenderizadorEncabezado extends DefaultTableCellRenderer {
        RenderizadorEncabezado() {
            setOpaque(true);
            setBackground(new Color(8, 124, 255));
            setForeground(Color.WHITE);
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable tabla, Object valor, boolean seleccionado,
                boolean foco, int fila, int columna
        ) {
            super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
            return this;
        }
    }

    private static class RenderizadorFila extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable tabla, Object valor, boolean seleccionado,
                boolean foco, int fila, int columna
        ) {
            super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
            if (!seleccionado) {
                setBackground(fila % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                setForeground(new Color(23, 42, 67));
            }
            setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return this;
        }
    }

    private static class RenderizadorEstado extends RenderizadorFila {
        @Override
        public Component getTableCellRendererComponent(
                JTable tabla, Object valor, boolean seleccionado,
                boolean foco, int fila, int columna
        ) {
            super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!seleccionado) {
                boolean activo = "ACTIVO".equals(String.valueOf(valor));
                setForeground(activo ? new Color(16, 140, 76) : new Color(220, 38, 38));
                setFont(getFont().deriveFont(Font.BOLD));
            }
            return this;
        }
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBasePersonal = new javax.swing.JPanel();
        pnlEncabezadoPersonal = new javax.swing.JPanel();
        lblTituloPersonal = new javax.swing.JLabel();
        lblModuloPersonal = new javax.swing.JLabel();
        pnlFormularioPersonal = new javax.swing.JPanel();
        lblTituloFormulario = new javax.swing.JLabel();
        lblCamposObligatorios = new javax.swing.JLabel();
        lblCodigoEmpleado = new javax.swing.JLabel();
        txtCodigoEmpleado = new javax.swing.JTextField();
        lblFechaIngreso = new javax.swing.JLabel();
        txtFechaIngreso = new javax.swing.JTextField();
        lblTipoContrato = new javax.swing.JLabel();
        cboTipoContrato = new javax.swing.JComboBox<>();
        lblSalario = new javax.swing.JLabel();
        txtSalario = new javax.swing.JTextField();
        cboPersona = new javax.swing.JComboBox<>();
        lblPersona = new javax.swing.JLabel();
        lblCedulaPersona = new javax.swing.JLabel();
        txtCedulaPersona = new javax.swing.JTextField();
        lblTurno = new javax.swing.JLabel();
        txtTurno = new javax.swing.JTextField();
        chkEstadoEmpleado = new javax.swing.JCheckBox();
        btnGuardar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnDesactivar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        pnlTablaPersonal = new javax.swing.JPanel();
        txtBuscarPersonal = new javax.swing.JTextField();
        btnBuscarPersonal = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPersonal = new javax.swing.JTable();
        lblBuscarPersonal = new javax.swing.JLabel();
        lblCantidadPersonal = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(1050, 650));
        setPreferredSize(new java.awt.Dimension(1150, 720));

        pnlBasePersonal.setBackground(new java.awt.Color(234, 242, 251));

        pnlEncabezadoPersonal.setBackground(new java.awt.Color(255, 255, 255));
        pnlEncabezadoPersonal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlEncabezadoPersonal.setPreferredSize(new java.awt.Dimension(1000, 70));

        lblTituloPersonal.setFont(new java.awt.Font("SansSerif", 1, 25)); // NOI18N
        lblTituloPersonal.setForeground(new java.awt.Color(23, 42, 67));
        lblTituloPersonal.setText("Gestion integral del personal");
        lblTituloPersonal.setPreferredSize(new java.awt.Dimension(350, 35));

        lblModuloPersonal.setForeground(new java.awt.Color(8, 124, 255));
        lblModuloPersonal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblModuloPersonal.setText("PERSONAL");
        lblModuloPersonal.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(8, 124, 255), 1, true));
        lblModuloPersonal.setPreferredSize(new java.awt.Dimension(58, 58));

        javax.swing.GroupLayout pnlEncabezadoPersonalLayout = new javax.swing.GroupLayout(pnlEncabezadoPersonal);
        pnlEncabezadoPersonal.setLayout(pnlEncabezadoPersonalLayout);
        pnlEncabezadoPersonalLayout.setHorizontalGroup(
            pnlEncabezadoPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoPersonalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblModuloPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlEncabezadoPersonalLayout.setVerticalGroup(
            pnlEncabezadoPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoPersonalLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlEncabezadoPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTituloPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblModuloPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlFormularioPersonal.setBackground(new java.awt.Color(255, 255, 255));
        pnlFormularioPersonal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlFormularioPersonal.setPreferredSize(new java.awt.Dimension(1000, 285));

        lblTituloFormulario.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblTituloFormulario.setForeground(new java.awt.Color(23, 42, 67));
        lblTituloFormulario.setText("Datos del empleado");
        lblTituloFormulario.setPreferredSize(new java.awt.Dimension(300, 30));

        lblCamposObligatorios.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblCamposObligatorios.setForeground(new java.awt.Color(107, 127, 153));
        lblCamposObligatorios.setText("Los campos con * son obligatorios");

        lblCodigoEmpleado.setText("Código del empleado");

        txtCodigoEmpleado.setPreferredSize(new java.awt.Dimension(300, 32));

        lblFechaIngreso.setText("Fecha de ingreso * (AAAA-MM-DD)");

        txtFechaIngreso.setPreferredSize(new java.awt.Dimension(300, 32));

        lblTipoContrato.setText("Tipo de contrato *");

        cboTipoContrato.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione...", "Tiempo completo", "Medio tiempo", "Temporal", "Servicios profesionales" }));

        cboTipoContrato.setPreferredSize(new java.awt.Dimension(300, 32));

        lblSalario.setText("Salario *");

        txtSalario.setToolTipText("Ejemplo: 70.50");
        txtSalario.setPreferredSize(new java.awt.Dimension(300, 32));

        cboPersona.setPreferredSize(new java.awt.Dimension(300, 32));

        lblPersona.setText("Persona registrada *");

        lblCedulaPersona.setText("Cédula de la persona");

        txtCedulaPersona.setEditable(false);
        txtCedulaPersona.setPreferredSize(new java.awt.Dimension(300, 32));

        lblTurno.setText("Horario * (HH:mm - HH:mm)");

        txtTurno.setPreferredSize(new java.awt.Dimension(300, 32));

        chkEstadoEmpleado.setBackground(new java.awt.Color(255, 255, 255));
        chkEstadoEmpleado.setForeground(new java.awt.Color(23, 42, 67));
        chkEstadoEmpleado.setSelected(true);
        chkEstadoEmpleado.setText("Empleado activo");

        btnGuardar.setBackground(new java.awt.Color(8, 124, 255));
        btnGuardar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar empleado");
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

        javax.swing.GroupLayout pnlFormularioPersonalLayout = new javax.swing.GroupLayout(pnlFormularioPersonal);
        pnlFormularioPersonal.setLayout(pnlFormularioPersonalLayout);
        pnlFormularioPersonalLayout.setHorizontalGroup(
            pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormularioPersonalLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCamposObligatorios)
                    .addGroup(pnlFormularioPersonalLayout.createSequentialGroup()
                        .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCodigoEmpleado)
                            .addComponent(txtCodigoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPersona)
                            .addComponent(cboPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFechaIngreso)
                            .addComponent(txtFechaIngreso, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCedulaPersona)
                            .addComponent(txtCedulaPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTipoContrato)
                            .addComponent(cboTipoContrato, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTurno)
                            .addComponent(txtTurno, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSalario)
                            .addComponent(txtSalario, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkEstadoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlFormularioPersonalLayout.createSequentialGroup()
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
        pnlFormularioPersonalLayout.setVerticalGroup(
            pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormularioPersonalLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblTituloFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblCamposObligatorios)
                .addGap(13, 13, 13)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCodigoEmpleado)
                    .addComponent(lblFechaIngreso)
                    .addComponent(lblTipoContrato)
                    .addComponent(lblSalario))
                .addGap(5, 5, 5)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCodigoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFechaIngreso, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTipoContrato, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSalario, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPersona)
                    .addComponent(lblCedulaPersona)
                    .addComponent(lblTurno))
                .addGap(5, 5, 5)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCedulaPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTurno, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkEstadoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDesactivar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pnlTablaPersonal.setBackground(new java.awt.Color(255, 255, 255));
        pnlTablaPersonal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlTablaPersonal.setPreferredSize(new java.awt.Dimension(1000, 400));

        txtBuscarPersonal.setToolTipText("Buscar por código, cédula, nombre, apellido o correo");
        txtBuscarPersonal.setPreferredSize(new java.awt.Dimension(400, 38));
        txtBuscarPersonal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarPersonalActionPerformed(evt);
            }
        });

        btnBuscarPersonal.setBackground(new java.awt.Color(8, 124, 255));
        btnBuscarPersonal.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnBuscarPersonal.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscarPersonal.setText("Buscar");
        btnBuscarPersonal.setPreferredSize(new java.awt.Dimension(100, 38));

        tblPersonal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"ID", "Código", "Cédula", "Empleado", "Ingreso", "Contrato", "Horario", "Salario", "Estado"}
        ));
        jScrollPane1.setViewportView(tblPersonal);

        lblBuscarPersonal.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblBuscarPersonal.setForeground(new java.awt.Color(107, 127, 153));
        lblBuscarPersonal.setText("Buscar por código, cédula, nombres, apellidos o correo");
        lblBuscarPersonal.setPreferredSize(new java.awt.Dimension(500, 20));

        lblCantidadPersonal.setText("0 empleados encontrados");

        javax.swing.GroupLayout pnlTablaPersonalLayout = new javax.swing.GroupLayout(pnlTablaPersonal);
        pnlTablaPersonal.setLayout(pnlTablaPersonalLayout);
        pnlTablaPersonalLayout.setHorizontalGroup(
            pnlTablaPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTablaPersonalLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlTablaPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBuscarPersonal)
                    .addGroup(pnlTablaPersonalLayout.createSequentialGroup()
                        .addComponent(txtBuscarPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                        .addGap(12, 12, 12)
                        .addComponent(btnBuscarPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblCantidadPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1090, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlTablaPersonalLayout.setVerticalGroup(
            pnlTablaPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTablaPersonalLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblBuscarPersonal)
                .addGap(6, 6, 6)
                .addGroup(pnlTablaPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscarPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantidadPersonal))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );

        javax.swing.GroupLayout pnlBasePersonalLayout = new javax.swing.GroupLayout(pnlBasePersonal);
        pnlBasePersonal.setLayout(pnlBasePersonalLayout);
        pnlBasePersonalLayout.setHorizontalGroup(
            pnlBasePersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlEncabezadoPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, 1175, Short.MAX_VALUE)
            .addGroup(pnlBasePersonalLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlBasePersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFormularioPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTablaPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlBasePersonalLayout.setVerticalGroup(
            pnlBasePersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBasePersonalLayout.createSequentialGroup()
                .addComponent(pnlEncabezadoPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(pnlFormularioPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(pnlTablaPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBasePersonal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBasePersonal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        if (esModoEmpleados()) {
            guardarEmpleado();
        } else if (gestorComplementario.nuevo()) {
            limpiarFormulario();
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void txtBuscarPersonalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarPersonalActionPerformed
        cargarPersonal();
    }//GEN-LAST:event_txtBuscarPersonalActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarFormulario();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        if (esModoEmpleados()) {
            modificarEmpleado();
        } else if (gestorComplementario.modificar()) {
            limpiarFormulario();
        }
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnDesactivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDesactivarActionPerformed
        if (esModoEmpleados()) {
            desactivarEmpleado();
        } else if (gestorComplementario.desactivar()) {
            limpiarFormulario();
        }
    }//GEN-LAST:event_btnDesactivarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        if (esModoEmpleados()) {
            eliminarEmpleado();
        } else if (gestorComplementario.eliminar()) {
            limpiarFormulario();
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarPersonal;
    private javax.swing.JButton btnDesactivar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JComboBox<modelo.Persona> cboPersona;
    private javax.swing.JCheckBox chkEstadoEmpleado;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBuscarPersonal;
    private javax.swing.JLabel lblCamposObligatorios;
    private javax.swing.JLabel lblCantidadPersonal;
    private javax.swing.JLabel lblCedulaPersona;
    private javax.swing.JLabel lblCodigoEmpleado;
    private javax.swing.JLabel lblFechaIngreso;
    private javax.swing.JLabel lblModuloPersonal;
    private javax.swing.JLabel lblTurno;
    private javax.swing.JLabel lblPersona;
    private javax.swing.JLabel lblTipoContrato;
    private javax.swing.JLabel lblSalario;
    private javax.swing.JLabel lblTituloPersonal;
    private javax.swing.JLabel lblTituloFormulario;
    private javax.swing.JPanel pnlBasePersonal;
    private javax.swing.JPanel pnlEncabezadoPersonal;
    private javax.swing.JPanel pnlFormularioPersonal;
    private javax.swing.JPanel pnlTablaPersonal;
    private javax.swing.JTable tblPersonal;
    private javax.swing.JTextField txtBuscarPersonal;
    private javax.swing.JTextField txtCedulaPersona;
    private javax.swing.JTextField txtCodigoEmpleado;
    private javax.swing.JTextField txtFechaIngreso;
    private javax.swing.JTextField txtTurno;
    private javax.swing.JComboBox<String> cboTipoContrato;
    private javax.swing.JTextField txtSalario;
    // End of variables declaration//GEN-END:variables
}
