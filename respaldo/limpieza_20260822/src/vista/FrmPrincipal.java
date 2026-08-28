/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;
import controlador.AutorizacionControlador;
/**
 *
 * @author Usuario
 */
public class FrmPrincipal extends javax.swing.JFrame {
    
    private PnlPersonas panelPersonas;
    private PnlClientes panelClientes;
    private PnlPersonal panelPersonal;
    private PnlSeguridad panelSeguridad;
    private PnlMembresias panelMembresias;
    private PnlAcceso panelAcceso;
    private PnlRutinas panelRutinas;
    private PnlSalud panelSalud;
    private PnlNutricion panelNutricion;
    private PnlFinanzas panelFinanzas;
    private PnlInventario panelInventario;
    private PnlReportes panelReportes;
    private PnlConfiguracion panelConfiguracion;
    private javax.swing.JPanel panelInicio;
    private final java.util.Map<String, PnlModuloAplicacion> panelesExperiencia
            = new java.util.HashMap<>();
    private final String rolVentana;

    private AutorizacionControlador autorizacionControlador;
    private javax.swing.JButton[] botonesMenu;

    /**
     * Creates new form FrmPrincipal
     */
    public FrmPrincipal() {

    initComponents();

    rolVentana = utilidades.NavegacionRol.normalizarRol(
            utilidades.SesionUsuario.getUsuarioActual().getNombreRol()
    );

    autorizacionControlador = new AutorizacionControlador();

    setDefaultCloseOperation(
            javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE
    );

    addWindowListener(
            new java.awt.event.WindowAdapter() {

                @Override
                public void windowClosing(
                        java.awt.event.WindowEvent evento
                ) {
                    volverAlLogin();
                }
            }
    );

    setIconImage(
            new javax.swing.ImageIcon(
                    getClass().getResource(
                            "/recursos/gymnova_logo_gn_32.png"
                    )
            ).getImage()
    );

    configurarNavegacion();
    cargarDatosSesion();
    configurarMenuSegunRol();
    aplicarPermisosMenu();
    mostrarInicio();

    btnPersonas.addActionListener(
            evento -> mostrarPersonas()
    );

    btnClientes.addActionListener(
            evento -> mostrarClientes()
    );

    setMinimumSize(new java.awt.Dimension(1180, 700));
    setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
    setLocationRelativeTo(null);
}
    private void volverAlLogin() {

    int respuesta
            = javax.swing.JOptionPane.showConfirmDialog(
                    this,
                    "Desea volver al inicio de sesion?",
                    "Cerrar sesion",
                    javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE
            );

    if (respuesta
            != javax.swing.JOptionPane.YES_OPTION) {
        return;
    }

    utilidades.SesionUsuario.cerrarSesion();

    FrmLogin login = new FrmLogin();
    login.setLocationRelativeTo(null);
    login.setVisible(true);

    dispose();
}
    
    private void cargarDatosSesion() {

    modelo.Usuario usuario
            = utilidades.SesionUsuario
                    .getUsuarioActual();

    lblUsuarioSesion.setText(
            usuario.getNombreUsuario()
    );

    lblRolSesion.setText(
            usuario.getNombreRol()
    );

    cargarAvatarSesion(usuario);
}

private void cargarAvatarSesion(modelo.Usuario usuario) {
    javax.swing.JLabel temporal = new javax.swing.JLabel();
    temporal.setSize(48, 48);
    modelo.Persona persona = null;
    if (usuario.getIdPersona() != null) {
        try {
            persona = new controlador.PersonaControlador()
                    .buscarPorId(usuario.getIdPersona()).orElse(null);
        } catch (java.sql.SQLException ex) {
            // Se conserva el avatar neutro si PostgreSQL no responde.
        }
    }
    utilidades.AvatarPerfil.mostrar(temporal, persona);
    if (temporal.getIcon() instanceof javax.swing.ImageIcon icono) {
        java.awt.Image imagen = icono.getImage().getScaledInstance(
                32, 32, java.awt.Image.SCALE_SMOOTH
        );
        lblUsuarioSesion.setIcon(new javax.swing.ImageIcon(imagen));
    }
    lblUsuarioSesion.setHorizontalTextPosition(
            javax.swing.SwingConstants.RIGHT
    );
    lblUsuarioSesion.setIconTextGap(8);
    lblUsuarioSesion.setToolTipText(
            usuario.getNombreUsuario() + " - " + usuario.getNombreRol()
    );
}

   private void aplicarPermisosMenu() {

    btnInicio.setEnabled(true);
    btnCerrarSesion.setEnabled(true);

    btnPersonas.setEnabled(
            tienePermiso(
                    "PERSONAS",
                    "VER"
            )
    );

    btnClientes.setEnabled(
            tienePermiso(
                    "CLIENTES",
                    "VER"
            )
    );

    btnPersonal.setEnabled(
            tieneAlMenosUnPermiso(
                    new String[][]{
                        {"EMPLEADOS", "VER"},
                        {"ENTRENADORES", "VER"},
                        {"NUTRICIONISTAS", "VER"}
                    }
            )
    );

    btnSeguridad.setEnabled(
            tieneAlMenosUnPermiso(
                    new String[][]{
                        {"ROLES", "VER"},
                        {"PERMISOS", "VER"}
                    }
            )
    );

    btnMembresias.setEnabled(
            tienePermiso(
                    "MEMBRESIAS",
                    "VER"
            )
    );

    btnAcceso.setEnabled(
            tienePermiso(
                    "ACCESO",
                    "VER"
            )
    );

    btnRutinas.setEnabled(
            tienePermiso(
                    "RUTINAS",
                    "VER"
            )
    );

    btnSalud.setEnabled(
            tienePermiso(
                    "SALUD",
                    "VER"
            )
    );

    btnNutricion.setEnabled(
            tienePermiso(
                    "NUTRICION",
                    "VER"
            )
    );

    btnFinanzas.setEnabled(
            tienePermiso(
                    "FINANZAS",
                    "VER"
            )
    );

    btnInventario.setEnabled(
            tienePermiso(
                    "INVENTARIO",
                    "VER"
            )
    );

    btnReportes.setEnabled(
            tienePermiso(
                    "REPORTES",
                    "VER"
            )
    );

    btnConfiguracion.setEnabled(
            tienePermiso(
                    "CONFIGURACION",
                    "VER"
            )
    );
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

private boolean tieneAlMenosUnPermiso(
        String[][] permisos
) {

    for (String[] permiso : permisos) {

        if (tienePermiso(
                permiso[0],
                permiso[1]
        )) {

            return true;
        }
    }

    return false;
}

private boolean verificarPermisoVista(
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

    javax.swing.JOptionPane.showMessageDialog(
            this,
            autorizacionControlador.getMensaje(),
            "Acceso denegado",
            javax.swing.JOptionPane.WARNING_MESSAGE
    );

    return false;
}

private boolean verificarAlMenosUnPermisoVista(
        String[][] permisos
) {

    if (tieneAlMenosUnPermiso(permisos)) {
        return true;
    }

    javax.swing.JOptionPane.showMessageDialog(
            this,
            autorizacionControlador.getMensaje(),
            "Acceso denegado",
            javax.swing.JOptionPane.WARNING_MESSAGE
    );

    return false;
}

private void configurarMenuSegunRol() {

    String rol = rolVentana;

    if ("CLIENTE".equals(rol)) {
        btnClientes.setText("Mi perfil");
        btnMembresias.setText("Mi membresía");
        btnAcceso.setText("Mis reservas");
        btnRutinas.setText("Mi rutina");
        btnSalud.setText("Mi progreso");
        btnNutricion.setText("Mi nutrición");
        btnReportes.setText("Mi historial");
        mostrarSoloRol(btnInicio, btnClientes, btnMembresias, btnAcceso,
                btnRutinas, btnSalud, btnNutricion, btnReportes,
                btnConfiguracion);
    } else if ("ENTRENADOR".equals(rol)) {
        btnClientes.setText("Mis clientes");
        btnRutinas.setText("Rutinas");
        btnSalud.setText("Evaluaciones");
        btnReportes.setText("Mis reportes");
        mostrarSoloRol(btnInicio, btnClientes, btnRutinas, btnSalud,
                btnReportes, btnConfiguracion);
    } else if ("NUTRICIONISTA".equals(rol)) {
        btnClientes.setText("Mis clientes");
        btnSalud.setText("Seguimiento");
        btnNutricion.setText("Nutrición");
        btnReportes.setText("Mis reportes");
        mostrarSoloRol(btnInicio, btnClientes, btnSalud, btnNutricion,
                btnReportes, btnConfiguracion);
    } else if ("RECEPCIONISTA".equals(rol)) {
        mostrarSoloRol(btnInicio, btnPersonas, btnClientes, btnMembresias,
                btnAcceso, btnFinanzas, btnReportes, btnConfiguracion);
    } else if ("ADMINISTRADOR".equals(rol)) {
        mostrarSoloRol(btnInicio, btnPersonas, btnClientes, btnPersonal,
                btnSeguridad, btnMembresias, btnAcceso, btnRutinas,
                btnSalud, btnNutricion, btnFinanzas, btnInventario,
                btnReportes, btnConfiguracion);
    } else {
        mostrarSoloRol(btnInicio);
    }
}

private void mostrarSoloRol(javax.swing.JButton... visibles) {
    java.util.Set<javax.swing.JButton> permitidos
            = new java.util.HashSet<>(java.util.Arrays.asList(visibles));
    javax.swing.JButton[] todos = {
        btnInicio, btnPersonas, btnClientes, btnPersonal, btnSeguridad,
        btnMembresias, btnAcceso, btnRutinas, btnSalud, btnNutricion,
        btnFinanzas, btnInventario, btnReportes, btnConfiguracion
    };
    for (javax.swing.JButton boton : todos) {
        boton.setVisible(permitidos.contains(boton));
    }
}
    
    private void configurarNavegacion() {

        botonesMenu = new javax.swing.JButton[] {
            btnInicio,
            btnPersonas,
            btnClientes,
            btnPersonal,
            btnSeguridad,
            btnMembresias,
            btnAcceso,
            btnRutinas,
            btnSalud,
            btnNutricion,
            btnFinanzas,
            btnInventario,
            btnReportes,
            btnConfiguracion
        };

        btnPersonal.addActionListener(
                evento -> mostrarPersonal()
        );

        btnSeguridad.addActionListener(
                evento -> mostrarSeguridad()
        );

        btnMembresias.addActionListener(
                evento -> mostrarMembresias()
        );

        btnAcceso.addActionListener(
                evento -> mostrarAcceso()
        );

        btnRutinas.addActionListener(
                evento -> mostrarRutinas()
        );

        btnSalud.addActionListener(
                evento -> mostrarSalud()
        );

        btnNutricion.addActionListener(
                evento -> mostrarNutricion()
        );

        btnFinanzas.addActionListener(
                evento -> mostrarFinanzas()
        );

        btnInventario.addActionListener(
                evento -> mostrarInventario()
        );

        btnReportes.addActionListener(
                evento -> mostrarReportes()
        );

        btnConfiguracion.addActionListener(
                evento -> mostrarConfiguracion()
        );

    }

    private void mostrarModuloVacio(
            String titulo,
            javax.swing.JButton botonActivo
    ) {
        pnlContenido.removeAll();
        lblTituloSeccion.setText(titulo);
        marcarBotonActivo(botonActivo);
        pnlContenido.revalidate();
        pnlContenido.repaint();
    }

    private void mostrarVista(
        javax.swing.JPanel vista,
        String titulo,
        javax.swing.JButton botonActivo
) {
    pnlContenido.removeAll();

    utilidades.AyudasContextuales.aplicar(vista);

    pnlContenido.setLayout(
            new java.awt.BorderLayout()
    );

    pnlContenido.add(
            vista,
            java.awt.BorderLayout.CENTER
    );

    lblTituloSeccion.setText(titulo);

    marcarBotonActivo(botonActivo);

    pnlContenido.revalidate();
    pnlContenido.repaint();
}
    private void marcarBotonActivo(
        javax.swing.JButton botonActivo
) {
    java.awt.Color colorNormal
            = new java.awt.Color(7, 26, 53);

    java.awt.Color colorActivo
            = new java.awt.Color(8, 124, 255);

    if (botonesMenu == null) {
        return;
    }

    for (javax.swing.JButton boton : botonesMenu) {
        boton.setBackground(colorNormal);
    }

    botonActivo.setBackground(colorActivo);
}
    
    private void mostrarInicio() {

    if (panelInicio == null) {
        panelInicio = crearPanelInicioSegunRol();
    } else {
        refrescarPanelInicio();
    }

    mostrarVista(
            panelInicio,
            "Dashboard",
            btnInicio
    );
}

private javax.swing.JPanel crearPanelInicioSegunRol() {
    return switch (rolVentana) {
        case "RECEPCIONISTA" -> new PnlInicioRecepcionista();
        case "ENTRENADOR" -> new PnlInicioEntrenador();
        case "NUTRICIONISTA" -> new PnlInicioNutricionista();
        case "CLIENTE" -> new PnlInicioCliente();
        default -> new PnlInicioAdministrador();
    };
}

private void refrescarPanelInicio() {
    if (panelInicio instanceof PnlInicioAdministrador panel) {
        panel.refrescarDatos();
    } else if (panelInicio instanceof PnlInicioRecepcionista panel) {
        panel.refrescarDatos();
    } else if (panelInicio instanceof PnlInicioEntrenador panel) {
        panel.refrescarDatos();
    } else if (panelInicio instanceof PnlInicioNutricionista panel) {
        panel.refrescarDatos();
    } else if (panelInicio instanceof PnlInicioCliente panel) {
        panel.refrescarDatos();
    }
}

private boolean esAdministrador() {
    return utilidades.NavegacionRol.usaGestionAdministrativa(rolVentana);
}

private boolean verificarRutaRol(String modulo) {
    String rolSesion = utilidades.NavegacionRol.normalizarRol(
            utilidades.SesionUsuario.getUsuarioActual().getNombreRol()
    );
    boolean mismaSesion = rolVentana.equals(rolSesion);
    boolean permitido = utilidades.NavegacionRol.puedeAbrir(rolVentana, modulo);
    if (mismaSesion && permitido) {
        return true;
    }
    javax.swing.JOptionPane.showMessageDialog(
            this,
            "Esta pantalla no pertenece al rol " + rolVentana + ".",
            "Navegacion no permitida",
            javax.swing.JOptionPane.WARNING_MESSAGE
    );
    return false;
}

private void mostrarExperiencia(
        String modulo,
        String titulo,
        javax.swing.JButton boton,
        Runnable abrirGestion
) {
    PnlModuloAplicacion panel = panelesExperiencia.get(modulo);
    if (panel == null) {
        Runnable accionDisponible = puedeGestionarModulo(modulo)
                ? abrirGestion : null;
        panel = new PnlModuloAplicacion(modulo, accionDisponible);
        panelesExperiencia.put(modulo, panel);
    } else {
        panel.refrescarDatos();
    }
    mostrarVista(panel, titulo, boton);
}

private boolean puedeGestionarModulo(String modulo) {
    String rol = rolVentana;
    String clave = rol + "|" + modulo;
    if ("ADMINISTRADOR".equals(rol)) {
        return true;
    }
    return switch (clave) {
        case "RECEPCIONISTA|CLIENTES", "RECEPCIONISTA|MEMBRESIAS",
                "RECEPCIONISTA|ACCESO", "RECEPCIONISTA|FINANZAS",
                "RECEPCIONISTA|REPORTES",
                "ENTRENADOR|RUTINAS", "ENTRENADOR|SALUD",
                "ENTRENADOR|REPORTES",
                "NUTRICIONISTA|NUTRICION", "NUTRICIONISTA|SALUD",
                "NUTRICIONISTA|REPORTES",
                "CLIENTE|ACCESO", "CLIENTE|RUTINAS",
                "CLIENTE|REPORTES", "CLIENTE|CONFIGURACION",
                "ENTRENADOR|CONFIGURACION",
                "NUTRICIONISTA|CONFIGURACION",
                "RECEPCIONISTA|CONFIGURACION" -> true;
        default -> false;
    };
}
    
    private void mostrarPersonas() {

    if (!verificarRutaRol("PERSONAS")) {
        return;
    }

    if (!verificarPermisoVista(
            "PERSONAS",
            "VER"
    )) {
        return;
    }

    if (panelPersonas == null) {
        panelPersonas = new PnlPersonas();
    }

    mostrarVista(
            panelPersonas,
            "Gestión de personas",
            btnPersonas
    );
}

    private void mostrarClientes() {

    if (!verificarRutaRol("CLIENTES")) {
        return;
    }

    if (!verificarPermisoVista(
            "CLIENTES",
            "VER"
    )) {
        return;
    }

    if (esAdministrador()) {
        mostrarGestionClientes();
        return;
    }

    mostrarExperiencia(
            "CLIENTES", btnClientes.getText(), btnClientes,
            this::mostrarGestionClientes
    );
}

private void mostrarGestionClientes() {
    if (panelClientes == null) {
        panelClientes = new PnlClientes();
    } else {
        panelClientes.refrescarDatos();
    }

    mostrarVista(
            panelClientes,
            "Gestión de clientes",
            btnClientes
    );
}

    private void mostrarPersonal() {

    if (!verificarRutaRol("PERSONAL")) {
        return;
    }

    if (!verificarAlMenosUnPermisoVista(
            new String[][]{
                {"EMPLEADOS", "VER"},
                {"ENTRENADORES", "VER"},
                {"NUTRICIONISTAS", "VER"}
            }
    )) {
        return;
    }

    if (panelPersonal == null) {
        panelPersonal = new PnlPersonal();
    } else {
        panelPersonal.refrescarDatos();
    }

    mostrarVista(
            panelPersonal,
            "Gestion de personal",
            btnPersonal
    );
}


    private void mostrarSeguridad() {
        if (!verificarRutaRol("SEGURIDAD")) {
            return;
        }
        if (!verificarAlMenosUnPermisoVista(
                new String[][]{
                    {"ROLES", "VER"},
                    {"PERMISOS", "VER"}
                }
        )) {
            return;
        }
        if (panelSeguridad == null) {
            panelSeguridad = new PnlSeguridad();
        } else {
            panelSeguridad.refrescarDatos();
        }
        mostrarVista(panelSeguridad, "Seguridad", btnSeguridad);
    }
    private void mostrarMembresias() {
        if (!verificarRutaRol("MEMBRESIAS")) {
            return;
        }
        if (!verificarPermisoVista("MEMBRESIAS", "VER")) {
            return;
        }
        if (esAdministrador()) {
            mostrarGestionMembresias();
            return;
        }
        mostrarExperiencia(
                "MEMBRESIAS", btnMembresias.getText(), btnMembresias,
                this::mostrarGestionMembresias
        );
    }
    private void mostrarGestionMembresias() {
        if (panelMembresias == null) {
            panelMembresias = new PnlMembresias();
        } else {
            panelMembresias.refrescarDatos();
        }
        mostrarVista(panelMembresias, "Membresias", btnMembresias);
    }
    private void mostrarAcceso() {
        if (!verificarRutaRol("ACCESO")) {
            return;
        }
        if (!verificarPermisoVista("ACCESO", "VER")) {
            return;
        }
        if (esAdministrador()) {
            mostrarGestionAcceso();
            return;
        }
        mostrarExperiencia(
                "ACCESO", btnAcceso.getText(), btnAcceso,
                this::mostrarGestionAcceso
        );
    }
    private void mostrarGestionAcceso() {
        if (panelAcceso == null) {
            panelAcceso = new PnlAcceso();
        } else {
            panelAcceso.refrescarDatos();
        }
        mostrarVista(panelAcceso, "Acceso", btnAcceso);
    }
    private void mostrarRutinas() {
        if (!verificarRutaRol("RUTINAS")) {
            return;
        }
        if (!verificarPermisoVista("RUTINAS", "VER")) {
            return;
        }
        if (esAdministrador()) {
            mostrarGestionRutinas();
            return;
        }
        mostrarExperiencia(
                "RUTINAS", btnRutinas.getText(), btnRutinas,
                this::mostrarGestionRutinas
        );
    }
    private void mostrarGestionRutinas() {
        if (panelRutinas == null) {
            panelRutinas = new PnlRutinas();
        } else {
            panelRutinas.refrescarDatos();
        }
        mostrarVista(panelRutinas, "Rutinas", btnRutinas);
    }
    private void mostrarSalud() {
        if (!verificarRutaRol("SALUD")) {
            return;
        }
        if (!verificarPermisoVista("SALUD", "VER")) {
            return;
        }
        if (esAdministrador()) {
            mostrarGestionSalud();
            return;
        }
        mostrarExperiencia(
                "SALUD", btnSalud.getText(), btnSalud,
                this::mostrarGestionSalud
        );
    }
    private void mostrarGestionSalud() {
        if (panelSalud == null) {
            panelSalud = new PnlSalud();
        } else {
            panelSalud.refrescarDatos();
        }
        mostrarVista(panelSalud, "Salud", btnSalud);
    }
    private void mostrarNutricion() {
        if (!verificarRutaRol("NUTRICION")) {
            return;
        }
        if (!verificarPermisoVista("NUTRICION", "VER")) {
            return;
        }
        if (esAdministrador()) {
            mostrarGestionNutricion();
            return;
        }
        mostrarExperiencia(
                "NUTRICION", btnNutricion.getText(), btnNutricion,
                this::mostrarGestionNutricion
        );
    }
    private void mostrarGestionNutricion() {
        if (panelNutricion == null) {
            panelNutricion = new PnlNutricion();
        } else {
            panelNutricion.refrescarDatos();
        }
        mostrarVista(panelNutricion, "Nutricion", btnNutricion);
    }
    private void mostrarFinanzas() {
        if (!verificarRutaRol("FINANZAS")) {
            return;
        }
        if (!verificarPermisoVista("FINANZAS", "VER")) {
            return;
        }
        if (esAdministrador()) {
            mostrarGestionFinanzas();
            return;
        }
        mostrarExperiencia(
                "FINANZAS", btnFinanzas.getText(), btnFinanzas,
                this::mostrarGestionFinanzas
        );
    }
    private void mostrarGestionFinanzas() {
        if (panelFinanzas == null) {
            panelFinanzas = new PnlFinanzas();
        } else {
            panelFinanzas.refrescarDatos();
        }
        mostrarVista(panelFinanzas, "Finanzas", btnFinanzas);
    }
    private void mostrarInventario() {
        if (!verificarRutaRol("INVENTARIO")) {
            return;
        }
        if (!verificarPermisoVista("INVENTARIO", "VER")) {
            return;
        }
        if (panelInventario == null) {
            panelInventario = new PnlInventario();
        } else {
            panelInventario.refrescarDatos();
        }
        mostrarVista(panelInventario, "Inventario", btnInventario);
    }
    private void mostrarReportes() {
        if (!verificarRutaRol("REPORTES")) {
            return;
        }
        if (!verificarPermisoVista("REPORTES", "VER")) {
            return;
        }
        if (esAdministrador()) {
            mostrarGestionReportes();
            return;
        }
        mostrarExperiencia(
                "REPORTES", btnReportes.getText(), btnReportes,
                this::mostrarGestionReportes
        );
    }
    private void mostrarGestionReportes() {
        if (panelReportes == null) {
            panelReportes = new PnlReportes();
        } else {
            panelReportes.refrescarDatos();
        }
        mostrarVista(panelReportes, "Reportes", btnReportes);
    }
    private void mostrarConfiguracion() {
        if (!verificarRutaRol("CONFIGURACION")) {
            return;
        }
        if (!verificarPermisoVista("CONFIGURACION", "VER")) {
            return;
        }
        if (esAdministrador()) {
            mostrarGestionConfiguracion();
            return;
        }
        mostrarExperiencia(
                "CONFIGURACION", btnConfiguracion.getText(), btnConfiguracion,
                this::mostrarGestionConfiguracion
        );
    }
    private void mostrarGestionConfiguracion() {
        if (panelConfiguracion == null) {
            panelConfiguracion = new PnlConfiguracion(this::cargarDatosSesion);
        } else {
            panelConfiguracion.refrescarDatos();
        }
        mostrarVista(panelConfiguracion, "Configuracion", btnConfiguracion);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBase = new javax.swing.JPanel();
        pnlMenu = new javax.swing.JPanel();
        lblLogoMenu = new javax.swing.JLabel();
        lblNombreMenu = new javax.swing.JLabel();
        lblSubtituloMenu = new javax.swing.JLabel();
        btnInicio = new javax.swing.JButton();
        btnPersonas = new javax.swing.JButton();
        btnClientes = new javax.swing.JButton();
        btnPersonal = new javax.swing.JButton();
        btnSeguridad = new javax.swing.JButton();
        btnMembresias = new javax.swing.JButton();
        btnAcceso = new javax.swing.JButton();
        btnRutinas = new javax.swing.JButton();
        btnSalud = new javax.swing.JButton();
        btnNutricion = new javax.swing.JButton();
        btnFinanzas = new javax.swing.JButton();
        btnInventario = new javax.swing.JButton();
        btnReportes = new javax.swing.JButton();
        btnConfiguracion = new javax.swing.JButton();
        btnCerrarSesion = new javax.swing.JButton();
        pnlEncabezado = new javax.swing.JPanel();
        lblTituloSeccion = new javax.swing.JLabel();
        lblUsuarioSesion = new javax.swing.JLabel();
        lblRolSesion = new javax.swing.JLabel();
        pnlContenido = new javax.swing.JPanel();
        lblBienvenida = new javax.swing.JLabel();
        lblResumenModulos = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("GYMNOVA - Sistema integrado de gimnasio");
        setMinimumSize(new java.awt.Dimension(1000, 620));
        setPreferredSize(new java.awt.Dimension(1180, 700));

        pnlBase.setBackground(new java.awt.Color(234, 242, 251));

        pnlMenu.setBackground(new java.awt.Color(7, 26, 53));
        pnlMenu.setPreferredSize(new java.awt.Dimension(230, 620));

        lblLogoMenu.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogoMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/recursos/gymnova_logo_gn_72.png"))); // NOI18N
        lblLogoMenu.setPreferredSize(new java.awt.Dimension(90, 80));

        lblNombreMenu.setFont(new java.awt.Font("SansSerif", 1, 22)); // NOI18N
        lblNombreMenu.setForeground(new java.awt.Color(255, 255, 255));
        lblNombreMenu.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblNombreMenu.setText("GYMNOVA");
        lblNombreMenu.setPreferredSize(new java.awt.Dimension(190, 35));

        lblSubtituloMenu.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblSubtituloMenu.setForeground(new java.awt.Color(157, 184, 216));
        lblSubtituloMenu.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSubtituloMenu.setText("Gestión deportiva");
        lblSubtituloMenu.setPreferredSize(new java.awt.Dimension(190, 22));

        btnInicio.setBackground(new java.awt.Color(8, 124, 255));
        btnInicio.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnInicio.setForeground(new java.awt.Color(255, 255, 255));
        btnInicio.setText("⌂  Inicio");
        btnInicio.setBorderPainted(false);
        btnInicio.setFocusPainted(false);
        btnInicio.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnInicio.setPreferredSize(new java.awt.Dimension(214, 34));
        btnInicio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInicioActionPerformed(evt);
            }
        });

        btnPersonas.setBackground(new java.awt.Color(10, 42, 84));
        btnPersonas.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnPersonas.setForeground(new java.awt.Color(255, 255, 255));
        btnPersonas.setText("◉  Personas");
        btnPersonas.setBorderPainted(false);
        btnPersonas.setFocusPainted(false);
        btnPersonas.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnPersonas.setPreferredSize(new java.awt.Dimension(214, 34));

        btnClientes.setBackground(new java.awt.Color(10, 42, 84));
        btnClientes.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnClientes.setForeground(new java.awt.Color(255, 255, 255));
        btnClientes.setText("♙  Clientes");
        btnClientes.setBorderPainted(false);
        btnClientes.setFocusPainted(false);
        btnClientes.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnClientes.setPreferredSize(new java.awt.Dimension(214, 34));

        btnPersonal.setBackground(new java.awt.Color(10, 42, 84));
        btnPersonal.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnPersonal.setForeground(new java.awt.Color(255, 255, 255));
        btnPersonal.setText("⚙  Personal");
        btnPersonal.setBorderPainted(false);
        btnPersonal.setFocusPainted(false);
        btnPersonal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnPersonal.setPreferredSize(new java.awt.Dimension(214, 34));

        btnSeguridad.setBackground(new java.awt.Color(10, 42, 84));
        btnSeguridad.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnSeguridad.setForeground(new java.awt.Color(255, 255, 255));
        btnSeguridad.setText("◇  Seguridad");
        btnSeguridad.setBorderPainted(false);
        btnSeguridad.setFocusPainted(false);
        btnSeguridad.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnSeguridad.setPreferredSize(new java.awt.Dimension(214, 34));

        btnMembresias.setBackground(new java.awt.Color(10, 42, 84));
        btnMembresias.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnMembresias.setForeground(new java.awt.Color(255, 255, 255));
        btnMembresias.setText("▤  Membresias");
        btnMembresias.setBorderPainted(false);
        btnMembresias.setFocusPainted(false);
        btnMembresias.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnMembresias.setPreferredSize(new java.awt.Dimension(214, 34));

        btnAcceso.setBackground(new java.awt.Color(10, 42, 84));
        btnAcceso.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnAcceso.setForeground(new java.awt.Color(255, 255, 255));
        btnAcceso.setText("▣  Acceso");
        btnAcceso.setBorderPainted(false);
        btnAcceso.setFocusPainted(false);
        btnAcceso.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnAcceso.setPreferredSize(new java.awt.Dimension(214, 34));

        btnRutinas.setBackground(new java.awt.Color(10, 42, 84));
        btnRutinas.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnRutinas.setForeground(new java.awt.Color(255, 255, 255));
        btnRutinas.setText("☷  Rutinas");
        btnRutinas.setBorderPainted(false);
        btnRutinas.setFocusPainted(false);
        btnRutinas.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnRutinas.setPreferredSize(new java.awt.Dimension(214, 34));

        btnSalud.setBackground(new java.awt.Color(10, 42, 84));
        btnSalud.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnSalud.setForeground(new java.awt.Color(255, 255, 255));
        btnSalud.setText("♡  Salud");
        btnSalud.setBorderPainted(false);
        btnSalud.setFocusPainted(false);
        btnSalud.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnSalud.setPreferredSize(new java.awt.Dimension(214, 34));

        btnNutricion.setBackground(new java.awt.Color(10, 42, 84));
        btnNutricion.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnNutricion.setForeground(new java.awt.Color(255, 255, 255));
        btnNutricion.setText("○  Nutricion");
        btnNutricion.setBorderPainted(false);
        btnNutricion.setFocusPainted(false);
        btnNutricion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNutricion.setPreferredSize(new java.awt.Dimension(214, 34));

        btnFinanzas.setBackground(new java.awt.Color(10, 42, 84));
        btnFinanzas.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnFinanzas.setForeground(new java.awt.Color(255, 255, 255));
        btnFinanzas.setText("$  Finanzas");
        btnFinanzas.setBorderPainted(false);
        btnFinanzas.setFocusPainted(false);
        btnFinanzas.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnFinanzas.setPreferredSize(new java.awt.Dimension(214, 34));

        btnInventario.setBackground(new java.awt.Color(10, 42, 84));
        btnInventario.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnInventario.setForeground(new java.awt.Color(255, 255, 255));
        btnInventario.setText("▫  Inventario");
        btnInventario.setBorderPainted(false);
        btnInventario.setFocusPainted(false);
        btnInventario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnInventario.setPreferredSize(new java.awt.Dimension(214, 34));

        btnReportes.setBackground(new java.awt.Color(10, 42, 84));
        btnReportes.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnReportes.setForeground(new java.awt.Color(255, 255, 255));
        btnReportes.setText("▥  Reportes");
        btnReportes.setBorderPainted(false);
        btnReportes.setFocusPainted(false);
        btnReportes.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnReportes.setPreferredSize(new java.awt.Dimension(214, 34));

        btnConfiguracion.setBackground(new java.awt.Color(10, 42, 84));
        btnConfiguracion.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnConfiguracion.setForeground(new java.awt.Color(255, 255, 255));
        btnConfiguracion.setText("⚙  Configuracion");
        btnConfiguracion.setBorderPainted(false);
        btnConfiguracion.setFocusPainted(false);
        btnConfiguracion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnConfiguracion.setPreferredSize(new java.awt.Dimension(214, 34));

        btnCerrarSesion.setBackground(new java.awt.Color(10, 42, 84));
        btnCerrarSesion.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        btnCerrarSesion.setForeground(new java.awt.Color(255, 91, 110));
        btnCerrarSesion.setText("↪  Cerrar sesión");
        btnCerrarSesion.setBorderPainted(false);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnCerrarSesion.setPreferredSize(new java.awt.Dimension(214, 34));
        btnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarSesionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlMenuLayout = new javax.swing.GroupLayout(pnlMenu);
        pnlMenu.setLayout(pnlMenuLayout);
        pnlMenuLayout.setHorizontalGroup(
            pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMenuLayout.createSequentialGroup()
                        .addComponent(btnCerrarSesion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(pnlMenuLayout.createSequentialGroup()
                        .addGroup(pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNombreMenu, javax.swing.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
                            .addComponent(lblSubtituloMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnlMenuLayout.createSequentialGroup()
                                .addGroup(pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnSeguridad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnMembresias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnAcceso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnRutinas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnSalud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnNutricion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnFinanzas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnInventario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnReportes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnConfiguracion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMenuLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblLogoMenu, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(74, 74, 74))
        );
        pnlMenuLayout.setVerticalGroup(
            pnlMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMenuLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblLogoMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(lblNombreMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblSubtituloMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnInicio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnPersonas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnSeguridad, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnMembresias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnAcceso, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnRutinas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnSalud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnNutricion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnFinanzas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnInventario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnReportes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(btnConfiguracion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCerrarSesion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        pnlEncabezado.setBackground(new java.awt.Color(9, 29, 59));
        pnlEncabezado.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 124, 255)));
        pnlEncabezado.setPreferredSize(new java.awt.Dimension(850, 75));

        lblTituloSeccion.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        lblTituloSeccion.setForeground(new java.awt.Color(255, 255, 255));
        lblTituloSeccion.setText("Panel principal");
        lblTituloSeccion.setPreferredSize(new java.awt.Dimension(300, 35));

        lblUsuarioSesion.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        lblUsuarioSesion.setForeground(new java.awt.Color(255, 255, 255));
        lblUsuarioSesion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblUsuarioSesion.setText("Usuario");
        lblUsuarioSesion.setPreferredSize(new java.awt.Dimension(180, 40));

        lblRolSesion.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblRolSesion.setForeground(new java.awt.Color(54, 207, 255));
        lblRolSesion.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRolSesion.setText("Rol");

        javax.swing.GroupLayout pnlEncabezadoLayout = new javax.swing.GroupLayout(pnlEncabezado);
        pnlEncabezado.setLayout(pnlEncabezadoLayout);
        pnlEncabezadoLayout.setHorizontalGroup(
            pnlEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloSeccion, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 294, Short.MAX_VALUE)
                .addGroup(pnlEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEncabezadoLayout.createSequentialGroup()
                        .addComponent(lblUsuarioSesion, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(50, 50, 50))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlEncabezadoLayout.createSequentialGroup()
                        .addComponent(lblRolSesion, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30))))
        );
        pnlEncabezadoLayout.setVerticalGroup(
            pnlEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoLayout.createSequentialGroup()
                .addGroup(pnlEncabezadoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEncabezadoLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(lblTituloSeccion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlEncabezadoLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(lblUsuarioSesion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblRolSesion)))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        pnlContenido.setBackground(new java.awt.Color(234, 242, 251));
        pnlContenido.setPreferredSize(new java.awt.Dimension(850, 545));

        lblBienvenida.setFont(new java.awt.Font("SansSerif", 1, 30)); // NOI18N
        lblBienvenida.setForeground(new java.awt.Color(23, 42, 67));
        lblBienvenida.setText("Bienvenido a GYMNOVA");
        lblBienvenida.setPreferredSize(new java.awt.Dimension(600, 45));

        lblResumenModulos.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblResumenModulos.setForeground(new java.awt.Color(8, 124, 255));
        lblResumenModulos.setText("Personas  •  Clientes");

        javax.swing.GroupLayout pnlContenidoLayout = new javax.swing.GroupLayout(pnlContenido);
        pnlContenido.setLayout(pnlContenidoLayout);
        pnlContenidoLayout.setHorizontalGroup(
            pnlContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContenidoLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(pnlContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBienvenida, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblResumenModulos, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(465, Short.MAX_VALUE))
        );
        pnlContenidoLayout.setVerticalGroup(
            pnlContenidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlContenidoLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(lblBienvenida, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblResumenModulos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlBaseLayout = new javax.swing.GroupLayout(pnlBase);
        pnlBase.setLayout(pnlBaseLayout);
        pnlBaseLayout.setHorizontalGroup(
            pnlBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBaseLayout.createSequentialGroup()
                .addComponent(pnlMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(pnlBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlEncabezado, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlContenido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        pnlBaseLayout.setVerticalGroup(
            pnlBaseLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMenu, javax.swing.GroupLayout.DEFAULT_SIZE, 766, Short.MAX_VALUE)
            .addGroup(pnlBaseLayout.createSequentialGroup()
                .addComponent(pnlEncabezado, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(pnlContenido, javax.swing.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBase, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBase, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesionActionPerformed
        // TODO add your handling code here:
        
        volverAlLogin();
    }//GEN-LAST:event_btnCerrarSesionActionPerformed

    private void btnInicioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInicioActionPerformed
        // TODO add your handling code here:
        
       
    mostrarInicio();

    }//GEN-LAST:event_btnInicioActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(
            () -> {
                FrmLogin login = new FrmLogin();
                login.setLocationRelativeTo(null);
                login.setVisible(true);
            }
    );
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAcceso;
    private javax.swing.JButton btnCerrarSesion;
    private javax.swing.JButton btnClientes;
    private javax.swing.JButton btnConfiguracion;
    private javax.swing.JButton btnFinanzas;
    private javax.swing.JButton btnInicio;
    private javax.swing.JButton btnInventario;
    private javax.swing.JButton btnMembresias;
    private javax.swing.JButton btnNutricion;
    private javax.swing.JButton btnPersonal;
    private javax.swing.JButton btnPersonas;
    private javax.swing.JButton btnReportes;
    private javax.swing.JButton btnRutinas;
    private javax.swing.JButton btnSalud;
    private javax.swing.JButton btnSeguridad;
    private javax.swing.JLabel lblBienvenida;
    private javax.swing.JLabel lblLogoMenu;
    private javax.swing.JLabel lblNombreMenu;
    private javax.swing.JLabel lblResumenModulos;
    private javax.swing.JLabel lblRolSesion;
    private javax.swing.JLabel lblSubtituloMenu;
    private javax.swing.JLabel lblTituloSeccion;
    private javax.swing.JLabel lblUsuarioSesion;
    private javax.swing.JPanel pnlBase;
    private javax.swing.JPanel pnlContenido;
    private javax.swing.JPanel pnlEncabezado;
    private javax.swing.JPanel pnlMenu;
    // End of variables declaration//GEN-END:variables
}
