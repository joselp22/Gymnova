/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package vista;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import controlador.PersonaControlador;
import controlador.AutorizacionControlador;
import modelo.Persona;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Usuario
 */
public class PnlPersonas extends javax.swing.JPanel {
    
private final PersonaControlador controlador;
private final AutorizacionControlador autorizacionControlador;
private DefaultTableModel modeloTabla;
private List<Persona> personasCargadas;
private Long idPersonaSeleccionada;

    /**
     * Creates new form PnlPersonas
     */
    public PnlPersonas() {
    initComponents();

    utilidades.CalendarioSelector.vincularFecha(txtFechaNacimiento);

    controlador = new PersonaControlador();
autorizacionControlador = new AutorizacionControlador();
personasCargadas = new ArrayList<>();

    configurarEstilos();
    configurarTabla();

    btnBuscarPersonas.addActionListener(
            evento -> cargarPersonas()
    );

    cargarPersonas();
    limpiarFormulario();

    cboSexo.addActionListener(evento ->
            utilidades.AvatarPerfil.mostrar(
                    lblFotoPerfil,
                    null,
                    cboSexo.getSelectedItem() == null
                    ? null : cboSexo.getSelectedItem().toString()
            )
    );

}
    private void aplicarEstiloTabla() {

    tblPersonas.setRowHeight(40);

    tblPersonas.setFont(
            new java.awt.Font(
                    "SansSerif",
                    java.awt.Font.PLAIN,
                    12
            )
    );

    tblPersonas.setShowGrid(false);
    tblPersonas.setShowHorizontalLines(false);
    tblPersonas.setShowVerticalLines(false);

    tblPersonas.setIntercellSpacing(
            new java.awt.Dimension(0, 0)
    );

    tblPersonas.setSelectionBackground(
            new java.awt.Color(220, 238, 255)
    );

    tblPersonas.setSelectionForeground(
            new java.awt.Color(30, 41, 59)
    );

    tblPersonas.getTableHeader()
            .setPreferredSize(
                    new java.awt.Dimension(0, 42)
            );

    tblPersonas.getTableHeader()
            .setReorderingAllowed(false);

    tblPersonas.getTableHeader()
            .setDefaultRenderer(
                    new RenderizadorEncabezado()
            );

    tblPersonas.setDefaultRenderer(
            Object.class,
            new RenderizadorFilas()
    );

    tblPersonas.getColumnModel()
            .getColumn(8)
            .setCellRenderer(
                    new RenderizadorEstado()
            );

    jScrollPane1.setBorder(
            javax.swing.BorderFactory.createLineBorder(
                    new java.awt.Color(212, 225, 239),
                    1
            )
    );

    jScrollPane1.getViewport().setBackground(
            java.awt.Color.WHITE
    );
}
    
    private static class RenderizadorEncabezado
        extends DefaultTableCellRenderer {

    RenderizadorEncabezado() {

        setOpaque(true);

        setBackground(
                new java.awt.Color(10, 47, 92)
        );

        setForeground(java.awt.Color.WHITE);

        setFont(
                new java.awt.Font(
                        "SansSerif",
                        java.awt.Font.BOLD,
                        12
                )
        );

        setBorder(
                javax.swing.BorderFactory.createEmptyBorder(
                        0,
                        10,
                        0,
                        10
                )
        );
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

        setBackground(
                new java.awt.Color(10, 47, 92)
        );

        setForeground(java.awt.Color.WHITE);

        if (columna == 0
                || columna == 4
                || columna == 5
                || columna == 8) {

            setHorizontalAlignment(
                    SwingConstants.CENTER
            );

        } else {

            setHorizontalAlignment(
                    SwingConstants.LEFT
            );
        }

        return this;
    }
}
    
    private static class RenderizadorFilas
        extends DefaultTableCellRenderer {

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

        setBorder(
                javax.swing.BorderFactory.createEmptyBorder(
                        0,
                        10,
                        0,
                        10
                )
        );

        if (seleccionada) {

            setBackground(
                    new java.awt.Color(220, 238, 255)
            );

            setForeground(
                    new java.awt.Color(30, 41, 59)
            );

        } else {

            setBackground(
                    fila % 2 == 0
                    ? java.awt.Color.WHITE
                    : new java.awt.Color(247, 251, 255)
            );

            setForeground(
                    new java.awt.Color(52, 74, 100)
            );
        }

        if (columna == 0
                || columna == 4
                || columna == 5
                || columna == 6) {

            setHorizontalAlignment(
                    SwingConstants.CENTER
            );

        } else {

            setHorizontalAlignment(
                    SwingConstants.LEFT
            );
        }

        return this;
    }
}
    private static class RenderizadorEstado
        extends DefaultTableCellRenderer {

    private java.awt.Color fondoFila;
    private java.awt.Color fondoEstado;

    RenderizadorEstado() {

        setHorizontalAlignment(
                SwingConstants.CENTER
        );

        setFont(
                new java.awt.Font(
                        "SansSerif",
                        java.awt.Font.BOLD,
                        11
                )
        );

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

        boolean activo
                = "Activo".equalsIgnoreCase(
                        String.valueOf(valor)
                );

        setText(
                activo
                ? "ACTIVO"
                : "INACTIVO"
        );

        fondoFila = seleccionada
                ? new java.awt.Color(220, 238, 255)
                : fila % 2 == 0
                    ? java.awt.Color.WHITE
                    : new java.awt.Color(247, 251, 255);

        if (activo) {

            fondoEstado
                    = new java.awt.Color(0, 230, 118);

            setForeground(
                    new java.awt.Color(6, 60, 36)
            );

        } else {

            fondoEstado
                    = new java.awt.Color(255, 23, 68);

            setForeground(
                    java.awt.Color.WHITE
            );
        }

        setBorder(
                javax.swing.BorderFactory.createEmptyBorder(
                        0,
                        8,
                        0,
                        8
                )
        );

        setOpaque(false);

        return this;
    }

    @Override
    protected void paintComponent(
            Graphics graphics
    ) {
        Graphics2D g2
                = (Graphics2D) graphics.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setColor(fondoFila);

        g2.fillRect(
                0,
                0,
                getWidth(),
                getHeight()
        );

        int anchoEtiqueta = Math.min(
                getWidth() - 12,
                Math.max(
                        68,
                        getFontMetrics(getFont())
                                .stringWidth(getText()) + 24
                )
        );

        int altoEtiqueta = 24;

        int x = (
                getWidth() - anchoEtiqueta
        ) / 2;

        int y = (
                getHeight() - altoEtiqueta
        ) / 2;

        g2.setColor(fondoEstado);

        g2.fillRoundRect(
                x,
                y,
                anchoEtiqueta,
                altoEtiqueta,
                18,
                18
        );

        g2.dispose();

        super.paintComponent(graphics);
    }
}

   private void aplicarPermisosFormulario() {

    if (idPersonaSeleccionada == null) {

        btnGuardar.setEnabled(
                tienePermiso(
                        "PERSONAS",
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
                        "PERSONAS",
                        "MODIFICAR"
                )
        );

        btnDesactivar.setEnabled(
                tienePermiso(
                        "PERSONAS",
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
    
    private void cargarPersonaFormulario(
        Persona persona
) {

    idPersonaSeleccionada
            = persona.getIdPersona();

    txtCedula.setText(
            persona.getCedula()
    );
    // La cedula identifica a la persona y no se modifica una vez registrada.
    txtCedula.setEditable(false);

    txtNombres.setText(
            persona.getNombres()
    );

    txtApellidos.setText(
            persona.getApellidos()
    );

    txtFechaNacimiento.setText(
            persona.getFechaNacimiento().toString()
    );

    cboSexo.setSelectedItem(
            persona.getSexo()
    );

    txtTelefono.setText(
            persona.getTelefono()
    );

    txtCorreo.setText(
            persona.getCorreo()
    );

    chkEstadoPersona.setSelected(
            persona.isEstado()
    );

    utilidades.AvatarPerfil.mostrar(
            lblFotoPerfil,
            persona
    );

    btnGuardar.setEnabled(false);

    btnModificar.setEnabled(
            tienePermiso(
                    "PERSONAS",
                    "MODIFICAR"
            )
    );

    btnDesactivar.setEnabled(
            persona.isEstado()
            && tienePermiso(
                    "PERSONAS",
                    "DESACTIVAR"
            )
    );

    btnEliminar.setEnabled(
            puedeEliminarDefinitivamente()
    );
}

    private boolean puedeEliminarDefinitivamente() {

    return tienePermiso(
            "PERSONAS",
            "ELIMINAR"
    )
            && utilidades.SesionUsuario.haySesionActiva()
            && "Administrador".equalsIgnoreCase(
                    utilidades.SesionUsuario
                            .getUsuarioActual()
                            .getNombreRol()
            );
}
    
    private void seleccionarPersonaTabla() {

    int filaVista = tblPersonas.getSelectedRow();

    if (filaVista < 0) {
        return;
    }

    int filaModelo
            = tblPersonas.convertRowIndexToModel(
                    filaVista
            );

    if (filaModelo < 0
            || filaModelo >= personasCargadas.size()) {
        return;
    }

    Persona persona
            = personasCargadas.get(filaModelo);

    cargarPersonaFormulario(persona);
}
    
    private Persona obtenerPersonaFormulario() {

    if (cboSexo.getSelectedIndex() == 0) {
        throw new IllegalArgumentException(
                "Seleccione el sexo."
        );
    }

    java.time.LocalDate fecha;

    try {
        fecha = java.time.LocalDate.parse(
                txtFechaNacimiento
                        .getText()
                        .trim()
        );

    } catch (java.time.format
            .DateTimeParseException e) {

        throw new IllegalArgumentException(
                "La fecha debe utilizar el formato "
                + "AAAA-MM-DD."
        );
    }

    Persona persona = new Persona();

    persona.setIdPersona(
            idPersonaSeleccionada
    );

    persona.setCedula(
            txtCedula.getText()
    );

    persona.setNombres(
            txtNombres.getText()
    );

    persona.setApellidos(
            txtApellidos.getText()
    );

    persona.setFechaNacimiento(fecha);

    persona.setSexo(
            cboSexo.getSelectedItem().toString()
    );

    persona.setTelefono(
            txtTelefono.getText()
    );

    persona.setCorreo(
            txtCorreo.getText()
    );

    persona.setEstado(
            chkEstadoPersona.isSelected()
    );

    persona.setFotoPerfil(null);

    return persona;
}
    
    private void limpiarFormulario() {

    idPersonaSeleccionada = null;

    txtCedula.setText("");
    txtCedula.setEditable(true);
    txtNombres.setText("");
    txtApellidos.setText("");
    txtFechaNacimiento.setText("");
    cboSexo.setSelectedIndex(0);

    utilidades.AvatarPerfil.mostrar(
            lblFotoPerfil,
            null,
            null
    );
    txtTelefono.setText("");
    txtCorreo.setText("");

    chkEstadoPersona.setSelected(true);

    tblPersonas.clearSelection();

    aplicarPermisosFormulario();

    txtCedula.requestFocus();
}
    
    private void configurarTabla() {

    String[] columnas = {
    "ID",
    "Cédula",
    "Nombres",
    "Apellidos",
    "Nacimiento",
    "Sexo",
    "Teléfono",
    "Correo",
    "Estado"
};

    modeloTabla = new DefaultTableModel(
            columnas,
            0
    ) {
        @Override
        public boolean isCellEditable(
                int fila,
                int columna
        ) {
            return false;
        }
    };

    tblPersonas.setModel(modeloTabla);

    tblPersonas.setRowHeight(30);
    
    tblPersonas.setFillsViewportHeight(true);

    tblPersonas.setBackground(
            java.awt.Color.WHITE
    );

    tblPersonas.getParent().setBackground(
            java.awt.Color.WHITE
    );

    tblPersonas.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
    );

    tblPersonas.setAutoCreateRowSorter(true);

    tblPersonas.setGridColor(
            new java.awt.Color(212, 225, 239)
    );

    tblPersonas.setSelectionBackground(
            new java.awt.Color(220, 238, 255)
    );

    tblPersonas.setSelectionForeground(
            new java.awt.Color(31, 41, 55)
    );

    tblPersonas.getTableHeader().setFont(
            new java.awt.Font(
                    "SansSerif",
                    java.awt.Font.BOLD,
                    12
            )
    );

    tblPersonas.getTableHeader().setBackground(
            new java.awt.Color(10, 47, 92)
    );

    tblPersonas.getTableHeader().setForeground(
            java.awt.Color.WHITE
    );

    tblPersonas.getColumnModel()
        .getColumn(0)
        .setPreferredWidth(45);

    tblPersonas.getColumnModel()
            .getColumn(1)
            .setPreferredWidth(95);

    tblPersonas.getColumnModel()
            .getColumn(2)
            .setPreferredWidth(125);

    tblPersonas.getColumnModel()
            .getColumn(3)
            .setPreferredWidth(125);

    tblPersonas.getColumnModel()
            .getColumn(4)
            .setPreferredWidth(100);

    tblPersonas.getColumnModel()
            .getColumn(5)
            .setPreferredWidth(75);

    tblPersonas.getColumnModel()
            .getColumn(6)
            .setPreferredWidth(100);

    tblPersonas.getColumnModel()
            .getColumn(7)
            .setPreferredWidth(170);

    tblPersonas.getColumnModel()
            .getColumn(8)
            .setPreferredWidth(80);
            
            aplicarEstiloTabla();
        
    tblPersonas.getSelectionModel()
        .addListSelectionListener(evento -> {

            if (!evento.getValueIsAdjusting()) {
                seleccionarPersonaTabla();
            }
        });
}
    
    private void cargarPersonas() {

    try {
        String criterio
                = txtBuscarPersonas.getText();

        personasCargadas
                = controlador.listar(criterio);

        modeloTabla.setRowCount(0);

        for (Persona persona : personasCargadas) {

            modeloTabla.addRow(
                    new Object[]{
                        persona.getIdPersona(),
                        persona.getCedula(),
                        persona.getNombres(),
                        persona.getApellidos(),
                        persona.getFechaNacimiento(),
                        persona.getSexo(),
                        persona.getTelefono(),
                        persona.getCorreo(),
                        persona.isEstado()
                            ? "Activo"
                            : "Inactivo"
                    }
            );
        }
        
        int cantidad = personasCargadas.size();

        lblCantidadPersonas.setText(
                cantidad == 1
                ? "1 persona encontrada"
                : cantidad + " personas encontradas"
);

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar las personas.\n"
                + "Detalle: "
                + e.getMessage(),
                "Error de PostgreSQL",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    
   private void configurarEstilos() {

    // Campos del formulario
    utilidades.EstilosComponentes
            .aplicarCampoSimple(txtCedula);

    utilidades.EstilosComponentes
            .aplicarCampoSimple(txtNombres);

    utilidades.EstilosComponentes
            .aplicarCampoSimple(txtApellidos);

    utilidades.EstilosComponentes
            .aplicarCampoSimple(txtFechaNacimiento);

    utilidades.EstilosComponentes
            .aplicarComboRedondeado(cboSexo);

    utilidades.EstilosComponentes
            .aplicarCampoSimple(txtTelefono);

    utilidades.EstilosComponentes
            .aplicarCampoSimple(txtCorreo);

    utilidades.EstilosComponentes
            .aplicarCampoSimple(txtBuscarPersonas);

    // Textos e iconos
    btnGuardar.setText(
            "\u2713  GUARDAR PERSONA"
    );

    btnModificar.setText(
            "\u270E  MODIFICAR"
    );

    btnDesactivar.setText(
            "\u23FB  DESACTIVAR"
    );

    btnLimpiar.setText(
            "\u21BB  LIMPIAR FORMULARIO"
    );

    btnEliminar.setText(
            "\u2715  ELIMINAR DEFINITIVAMENTE"
    );

    btnEliminar.setToolTipText(
            "Elimina el registro de la base de datos. Solo Administrador."
    );

    btnBuscarPersonas.setText(
            "\u2315  BUSCAR"
    );

    // Guardar: azul electrico
    utilidades.EstilosComponentes
            .aplicarBotonPremium(
                    btnGuardar,
                    new java.awt.Color(8, 124, 255),
                    new java.awt.Color(54, 207, 255),
                    java.awt.Color.WHITE
            );

    // Modificar: violeta electrico
    utilidades.EstilosComponentes
            .aplicarBotonPremium(
                    btnModificar,
                    new java.awt.Color(109, 40, 217),
                    new java.awt.Color(168, 85, 247),
                    java.awt.Color.WHITE
            );

    // Desactivar: amarillo electrico
    utilidades.EstilosComponentes
            .aplicarBotonPremium(
                    btnDesactivar,
                    new java.awt.Color(255, 143, 0),
                    new java.awt.Color(255, 214, 0),
                    new java.awt.Color(41, 31, 0)
            );

    // Limpiar: gris claro
    utilidades.EstilosComponentes
            .aplicarBotonPremium(
                    btnLimpiar,
                    new java.awt.Color(241, 245, 249),
                    new java.awt.Color(226, 232, 240),
                    new java.awt.Color(51, 65, 85)
            );

    // Eliminar definitivamente: rojo electrico
    utilidades.EstilosComponentes
            .aplicarBotonPremium(
                    btnEliminar,
                    new java.awt.Color(213, 0, 50),
                    new java.awt.Color(255, 23, 68),
                    java.awt.Color.WHITE
            );

    // Buscar: azul electrico
    utilidades.EstilosComponentes
            .aplicarBotonPremium(
                    btnBuscarPersonas,
                    new java.awt.Color(8, 124, 255),
                    new java.awt.Color(54, 207, 255),
                    java.awt.Color.WHITE
            );

    // Tamaños uniformes
    btnGuardar.setPreferredSize(
            new java.awt.Dimension(375, 46)
    );

    btnModificar.setPreferredSize(
            new java.awt.Dimension(180, 42)
    );

    btnDesactivar.setPreferredSize(
            new java.awt.Dimension(180, 42)
    );

    btnLimpiar.setPreferredSize(
            new java.awt.Dimension(375, 42)
    );

    btnEliminar.setPreferredSize(
            new java.awt.Dimension(375, 42)
    );
}

    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBasePersonas = new javax.swing.JPanel();
        pnlEncabezadoPersonas = new javax.swing.JPanel();
        lblTituloPersonas = new javax.swing.JLabel();
        lblFotoPerfil = new javax.swing.JLabel();
        pnlFormularioPersona = new javax.swing.JPanel();
        lblTituloFormulario = new javax.swing.JLabel();
        lblCamposObligatorios = new javax.swing.JLabel();
        lblCedula = new javax.swing.JLabel();
        txtCedula = new javax.swing.JTextField();
        lblNombres = new javax.swing.JLabel();
        txtNombres = new javax.swing.JTextField();
        lblApellidos = new javax.swing.JLabel();
        txtApellidos = new javax.swing.JTextField();
        lblFechaNacimiento = new javax.swing.JLabel();
        txtFechaNacimiento = new javax.swing.JTextField();
        cboSexo = new javax.swing.JComboBox<>();
        lblSexo = new javax.swing.JLabel();
        lblTelefono = new javax.swing.JLabel();
        txtTelefono = new javax.swing.JTextField();
        lblCorreo = new javax.swing.JLabel();
        txtCorreo = new javax.swing.JTextField();
        chkEstadoPersona = new javax.swing.JCheckBox();
        btnGuardar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnDesactivar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        pnlTablaPersonas = new javax.swing.JPanel();
        txtBuscarPersonas = new javax.swing.JTextField();
        btnBuscarPersonas = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPersonas = new javax.swing.JTable();
        lblBuscarPersonas = new javax.swing.JLabel();
        lblCantidadPersonas = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(1050, 650));
        setPreferredSize(new java.awt.Dimension(1150, 720));

        pnlBasePersonas.setBackground(new java.awt.Color(234, 242, 251));

        pnlEncabezadoPersonas.setBackground(new java.awt.Color(255, 255, 255));
        pnlEncabezadoPersonas.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlEncabezadoPersonas.setPreferredSize(new java.awt.Dimension(1000, 70));

        lblTituloPersonas.setFont(new java.awt.Font("SansSerif", 1, 25)); // NOI18N
        lblTituloPersonas.setForeground(new java.awt.Color(23, 42, 67));
        lblTituloPersonas.setText("Directorio de personas");
        lblTituloPersonas.setPreferredSize(new java.awt.Dimension(350, 35));

        lblFotoPerfil.setForeground(new java.awt.Color(8, 124, 255));
        lblFotoPerfil.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblFotoPerfil.setText("PERSONAS");
        lblFotoPerfil.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(8, 124, 255), 1, true));
        lblFotoPerfil.setPreferredSize(new java.awt.Dimension(58, 58));

        javax.swing.GroupLayout pnlEncabezadoPersonasLayout = new javax.swing.GroupLayout(pnlEncabezadoPersonas);
        pnlEncabezadoPersonas.setLayout(pnlEncabezadoPersonasLayout);
        pnlEncabezadoPersonasLayout.setHorizontalGroup(
            pnlEncabezadoPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoPersonasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 629, Short.MAX_VALUE)
                .addComponent(lblFotoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlEncabezadoPersonasLayout.setVerticalGroup(
            pnlEncabezadoPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoPersonasLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlEncabezadoPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTituloPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFotoPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlFormularioPersona.setBackground(new java.awt.Color(255, 255, 255));
        pnlFormularioPersona.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlFormularioPersona.setPreferredSize(new java.awt.Dimension(1000, 285));

        lblTituloFormulario.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblTituloFormulario.setForeground(new java.awt.Color(31, 41, 55));
        lblTituloFormulario.setText("Datos de la persona");
        lblTituloFormulario.setPreferredSize(new java.awt.Dimension(300, 30));

        lblCamposObligatorios.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblCamposObligatorios.setForeground(new java.awt.Color(107, 114, 128));
        lblCamposObligatorios.setText("Los campos con * son obligatorios");

        lblCedula.setText("Cédula *");

        txtCedula.setPreferredSize(new java.awt.Dimension(300, 32));
        txtCedula.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCedulaActionPerformed(evt);
            }
        });

        lblNombres.setText("Nombres *");

        txtNombres.setPreferredSize(new java.awt.Dimension(300, 32));

        lblApellidos.setText("Apellidos *");

        txtApellidos.setPreferredSize(new java.awt.Dimension(300, 32));

        lblFechaNacimiento.setText("Fecha de nacimiento *");

        txtFechaNacimiento.setToolTipText("Formato AAAA-MM-DD");
        txtFechaNacimiento.setPreferredSize(new java.awt.Dimension(300, 32));

        cboSexo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Seleccione...", "Masculino", "Femenino", "Otro" }));
        cboSexo.setPreferredSize(new java.awt.Dimension(300, 32));

        lblSexo.setText("Sexo *");

        lblTelefono.setText("Teléfono *");

        txtTelefono.setPreferredSize(new java.awt.Dimension(300, 32));

        lblCorreo.setText("Correo electrónico *");

        txtCorreo.setPreferredSize(new java.awt.Dimension(300, 32));

        chkEstadoPersona.setBackground(new java.awt.Color(255, 255, 255));
        chkEstadoPersona.setForeground(new java.awt.Color(31, 41, 55));
        chkEstadoPersona.setSelected(true);
        chkEstadoPersona.setText("Persona activa");

        btnGuardar.setBackground(new java.awt.Color(8, 124, 255));
        btnGuardar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar persona");
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

        javax.swing.GroupLayout pnlFormularioPersonaLayout = new javax.swing.GroupLayout(pnlFormularioPersona);
        pnlFormularioPersona.setLayout(pnlFormularioPersonaLayout);
        pnlFormularioPersonaLayout.setHorizontalGroup(
            pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormularioPersonaLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCamposObligatorios)
                    .addGroup(pnlFormularioPersonaLayout.createSequentialGroup()
                        .addGroup(pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCedula)
                            .addComponent(txtCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSexo)
                            .addComponent(cboSexo, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNombres)
                            .addComponent(txtNombres, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTelefono)
                            .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblApellidos)
                            .addComponent(txtApellidos, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCorreo)
                            .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFechaNacimiento)
                            .addComponent(txtFechaNacimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkEstadoPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlFormularioPersonaLayout.createSequentialGroup()
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
        pnlFormularioPersonaLayout.setVerticalGroup(
            pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormularioPersonaLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblTituloFormulario, javax.swing.GroupLayout.DEFAULT_SIZE, 24, javax.swing.GroupLayout.DEFAULT_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblCamposObligatorios)
                .addGap(13, 13, 13)
                .addGroup(pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCedula)
                    .addComponent(lblNombres)
                    .addComponent(lblApellidos)
                    .addComponent(lblFechaNacimiento))
                .addGap(5, 5, 5)
                .addGroup(pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNombres, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtApellidos, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFechaNacimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSexo)
                    .addComponent(lblTelefono)
                    .addComponent(lblCorreo))
                .addGap(5, 5, 5)
                .addGroup(pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboSexo, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkEstadoPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(pnlFormularioPersonaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDesactivar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pnlTablaPersonas.setBackground(new java.awt.Color(255, 255, 255));
        pnlTablaPersonas.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlTablaPersonas.setPreferredSize(new java.awt.Dimension(1000, 400));

        txtBuscarPersonas.setToolTipText("Buscar por cédula, nombre, apellido o correo");
        txtBuscarPersonas.setPreferredSize(new java.awt.Dimension(400, 38));
        txtBuscarPersonas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarPersonasActionPerformed(evt);
            }
        });

        btnBuscarPersonas.setBackground(new java.awt.Color(8, 124, 255));
        btnBuscarPersonas.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnBuscarPersonas.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscarPersonas.setText("Buscar");
        btnBuscarPersonas.setPreferredSize(new java.awt.Dimension(100, 38));

        tblPersonas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Cédula", "Nombres", "Apellidos", "Nacimiento", "Sexo", "Teléfono", "Correo", "Estado"
            }
        ));
        jScrollPane1.setViewportView(tblPersonas);

        lblBuscarPersonas.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblBuscarPersonas.setForeground(new java.awt.Color(107, 114, 128));
        lblBuscarPersonas.setText("Buscar por cédula, nombres, apellidos o correo");
        lblBuscarPersonas.setPreferredSize(new java.awt.Dimension(450, 20));

        lblCantidadPersonas.setText("0 registros encontrados");

        javax.swing.GroupLayout pnlTablaPersonasLayout = new javax.swing.GroupLayout(pnlTablaPersonas);
        pnlTablaPersonas.setLayout(pnlTablaPersonasLayout);
        pnlTablaPersonasLayout.setHorizontalGroup(
            pnlTablaPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTablaPersonasLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlTablaPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBuscarPersonas, javax.swing.GroupLayout.DEFAULT_SIZE, 246, javax.swing.GroupLayout.DEFAULT_SIZE)
                    .addGroup(pnlTablaPersonasLayout.createSequentialGroup()
                        .addComponent(txtBuscarPersonas, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                        .addGap(12, 12, 12)
                        .addComponent(btnBuscarPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblCantidadPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1090, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlTablaPersonasLayout.setVerticalGroup(
            pnlTablaPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTablaPersonasLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblBuscarPersonas, javax.swing.GroupLayout.DEFAULT_SIZE, 16, javax.swing.GroupLayout.DEFAULT_SIZE)
                .addGap(6, 6, 6)
                .addGroup(pnlTablaPersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscarPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantidadPersonas))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );

        javax.swing.GroupLayout pnlBasePersonasLayout = new javax.swing.GroupLayout(pnlBasePersonas);
        pnlBasePersonas.setLayout(pnlBasePersonasLayout);
        pnlBasePersonasLayout.setHorizontalGroup(
            pnlBasePersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlEncabezadoPersonas, javax.swing.GroupLayout.DEFAULT_SIZE, 1150, Short.MAX_VALUE)
            .addGroup(pnlBasePersonasLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlBasePersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFormularioPersona, javax.swing.GroupLayout.DEFAULT_SIZE, 1114, Short.MAX_VALUE)
                    .addComponent(pnlTablaPersonas, javax.swing.GroupLayout.DEFAULT_SIZE, 1114, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlBasePersonasLayout.setVerticalGroup(
            pnlBasePersonasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBasePersonasLayout.createSequentialGroup()
                .addComponent(pnlEncabezadoPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(pnlFormularioPersona, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(pnlTablaPersonas, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBasePersonas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBasePersonas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        // TODO add your handling code here:
        

    if (!verificarPermisoOperacion(
            "PERSONAS",
            "CREAR"
    )) {
        return;
    }

    try {
        Persona persona = obtenerPersonaFormulario();

        controlador.registrar(persona);

        JOptionPane.showMessageDialog(
                this,
                "La persona fue registrada correctamente.",
                "Registro exitoso",
                JOptionPane.INFORMATION_MESSAGE
        );

        cargarPersonas();
        limpiarFormulario();

    } catch (IllegalArgumentException e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Datos no validos",
                JOptionPane.WARNING_MESSAGE
        );

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "No se pudo registrar la persona.\n"
                + "Detalle: "
                + e.getMessage(),
                "Error de PostgreSQL",
                JOptionPane.ERROR_MESSAGE
        );
    }




    

    }//GEN-LAST:event_btnGuardarActionPerformed

    private void txtBuscarPersonasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarPersonasActionPerformed
        // TODO add your handling code here:
        
        cargarPersonas();
    }//GEN-LAST:event_txtBuscarPersonasActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        // TODO add your handling code here:
    
    limpiarFormulario();

    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        // TODO add your handling code here:
        
        if (!verificarPermisoOperacion(
            "PERSONAS",
            "MODIFICAR"
    )) {
        return;
    }

    if (idPersonaSeleccionada == null) {

        JOptionPane.showMessageDialog(
                this,
                "Seleccione una persona de la tabla.",
                "Seleccion requerida",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    try {
        Persona persona
                = obtenerPersonaFormulario();

        // Defensa adicional: aunque el campo ya esta bloqueado en la vista,
        // siempre se conserva la cedula almacenada en PostgreSQL.
        controlador.buscarPorId(idPersonaSeleccionada)
                .ifPresent(actual -> persona.setCedula(actual.getCedula()));

        controlador.modificar(persona);

        JOptionPane.showMessageDialog(
                this,
                "La persona fue modificada correctamente.",
                "Modificacion exitosa",
                JOptionPane.INFORMATION_MESSAGE
        );

        cargarPersonas();
        limpiarFormulario();

    } catch (IllegalArgumentException e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Datos no validos",
                JOptionPane.WARNING_MESSAGE
        );

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "No se pudo modificar la persona.\n"
                + "Detalle: "
                + e.getMessage(),
                "Error de PostgreSQL",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnDesactivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDesactivarActionPerformed
        // TODO add your handling code here:
        
        if (!verificarPermisoOperacion(
            "PERSONAS",
            "DESACTIVAR"
    )) {
        return;
    }

    if (idPersonaSeleccionada == null) {

        JOptionPane.showMessageDialog(
                this,
                "Seleccione una persona de la tabla.",
                "Seleccion requerida",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    int respuesta
            = JOptionPane.showConfirmDialog(
                    this,
                    "Desea desactivar la persona seleccionada?\n"
                    + "El registro no sera eliminado.",
                    "Confirmar desactivacion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

    if (respuesta != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        controlador.desactivar(
                idPersonaSeleccionada
        );

        JOptionPane.showMessageDialog(
                this,
                "La persona fue desactivada correctamente.",
                "Desactivacion exitosa",
                JOptionPane.INFORMATION_MESSAGE
        );

        cargarPersonas();
        limpiarFormulario();

    } catch (SQLException e) {

        JOptionPane.showMessageDialog(
                this,
                "No se pudo desactivar la persona.\n"
                + "Detalle: "
                + e.getMessage(),
                "Error de PostgreSQL",
                JOptionPane.ERROR_MESSAGE
        );
    }

    }//GEN-LAST:event_btnDesactivarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        if (!verificarPermisoOperacion(
            "PERSONAS",
            "ELIMINAR"
    )) {
        return;
    }

    if (!puedeEliminarDefinitivamente()) {

        JOptionPane.showMessageDialog(
                this,
                "Solo el Administrador puede eliminar "
                + "personas definitivamente.",
                "Acceso denegado",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    if (idPersonaSeleccionada == null) {
        JOptionPane.showMessageDialog(
                this,
                "Seleccione una persona de la tabla.",
                "Seleccion requerida",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    int respuesta = JOptionPane.showConfirmDialog(
            this,
            "Esta accion eliminara la persona definitivamente.\n"
            + "No se puede deshacer. Desea continuar?",
            "Confirmar eliminacion definitiva",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE
    );

    if (respuesta != JOptionPane.YES_OPTION) {
        return;
    }

    String cedula = txtCedula.getText().trim();

    String confirmacion = JOptionPane.showInputDialog(
            this,
            "Para confirmar, escriba la cedula: " + cedula,
            "Segunda confirmacion",
            JOptionPane.WARNING_MESSAGE
    );

    if (confirmacion == null
            || !cedula.equals(confirmacion.trim())) {

        JOptionPane.showMessageDialog(
                this,
                "La cedula no coincide. "
                + "No se elimino ningun registro.",
                "Eliminacion cancelada",
                JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    try {
        controlador.eliminarDefinitivamente(
                idPersonaSeleccionada
        );

        JOptionPane.showMessageDialog(
                this,
                "La persona fue eliminada definitivamente.",
                "Eliminacion exitosa",
                JOptionPane.INFORMATION_MESSAGE
        );

        cargarPersonas();
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
                "No se pudo eliminar la persona.\nDetalle: "
                + e.getMessage(),
                "Error de PostgreSQL",
                JOptionPane.ERROR_MESSAGE
        );
    }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void txtCedulaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCedulaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCedulaActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarPersonas;
    private javax.swing.JButton btnDesactivar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JComboBox<String> cboSexo;
    private javax.swing.JCheckBox chkEstadoPersona;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblApellidos;
    private javax.swing.JLabel lblBuscarPersonas;
    private javax.swing.JLabel lblCamposObligatorios;
    private javax.swing.JLabel lblCantidadPersonas;
    private javax.swing.JLabel lblCedula;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblFechaNacimiento;
    private javax.swing.JLabel lblFotoPerfil;
    private javax.swing.JLabel lblNombres;
    private javax.swing.JLabel lblSexo;
    private javax.swing.JLabel lblTelefono;
    private javax.swing.JLabel lblTituloFormulario;
    private javax.swing.JLabel lblTituloPersonas;
    private javax.swing.JPanel pnlBasePersonas;
    private javax.swing.JPanel pnlEncabezadoPersonas;
    private javax.swing.JPanel pnlFormularioPersona;
    private javax.swing.JPanel pnlTablaPersonas;
    private javax.swing.JTable tblPersonas;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JTextField txtBuscarPersonas;
    private javax.swing.JTextField txtCedula;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtFechaNacimiento;
    private javax.swing.JTextField txtNombres;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
