/* GYMNOVA - Vista administrativa: SEGURIDAD. */
package vista;

import controlador.AutorizacionControlador;
import controlador.BitacoraControlador;
import controlador.PermisoControlador;
import controlador.PersonaControlador;
import controlador.RolControlador;
import controlador.RolPermisoControlador;
import controlador.UsuarioControlador;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Bitacora;
import modelo.Permiso;
import modelo.Persona;
import modelo.Rol;
import modelo.RolPermiso;
import modelo.Usuario;

public class PnlSeguridad extends javax.swing.JPanel {

    private final UsuarioControlador usuarioControlador;
    private final RolControlador rolControlador;
    private final PermisoControlador permisoControlador;
    private final RolPermisoControlador rolPermisoControlador;
    private final BitacoraControlador bitacoraControlador;
    private final PersonaControlador personaControlador;
    private final AutorizacionControlador autorizacionControlador;
    private List<?> registrosActuales;
    private Object registroSeleccionado;

    public PnlSeguridad() {
        initComponents();
        usuarioControlador = new UsuarioControlador();
        rolControlador = new RolControlador();
        permisoControlador = new PermisoControlador();
        rolPermisoControlador = new RolPermisoControlador();
        bitacoraControlador = new BitacoraControlador();
        personaControlador = new PersonaControlador();
        autorizacionControlador = new AutorizacionControlador();
        registrosActuales = new ArrayList<>();
        configurarAlcanceRol();
        configurarEstilos();
        configurarTabla();
        btnBuscarPersonal.addActionListener(evento -> buscarRegistros());
        cboTipoContrato.addActionListener(
                evento -> cambiarSubmodulo()
        );
        txtSalario.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evento) {
                if (!txtSalario.isEditable() && txtSalario.isEnabled()) {
                    seleccionarRolPorNombre();
                }
            }
        });
        txtCedulaPersona.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evento) {
                if ("ROL - PERMISO".equals(submoduloActual())
                        && txtCedulaPersona.isEnabled()) {
                    seleccionarPermisoPorNombre();
                }
            }
        });
        tblPersonal.getSelectionModel().addListSelectionListener(
                evento -> {
                    if (!evento.getValueIsAdjusting()) {
                        seleccionarRegistro();
                    }
                }
        );
        txtCodigoEmpleado.setEditable(false);
        btnEliminar.setVisible(esAdministrador());
        cargarPersonas();
        cambiarSubmodulo();
    }

    private void configurarAlcanceRol() {
        if (utilidades.SesionUsuario.haySesionActiva()
                && "RECEPCIONISTA".equalsIgnoreCase(
                        utilidades.SesionUsuario.getUsuarioActual().getNombreRol())) {
            cboTipoContrato.setModel(new javax.swing.DefaultComboBoxModel<>(
                    new String[]{"Cuentas de usuario", "Roles del sistema",
                        "Bitacora de auditoria"}));
            lblTituloPersonal.setText("Acceso y seguridad del personal");
            lblModuloPersonal.setText("ACCESO");
        }
    }

    public void refrescarDatos() {
        buscarRegistros();
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
                btnGuardar, new java.awt.Color(8, 124, 255),
                new java.awt.Color(54, 207, 255), java.awt.Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnModificar, new java.awt.Color(109, 40, 217),
                new java.awt.Color(168, 85, 247), java.awt.Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnDesactivar, new java.awt.Color(255, 214, 0),
                new java.awt.Color(255, 232, 82), new java.awt.Color(41, 31, 0)
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnEliminar, new java.awt.Color(255, 23, 68),
                new java.awt.Color(255, 91, 110), java.awt.Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnLimpiar, new java.awt.Color(241, 245, 249),
                new java.awt.Color(226, 232, 240), new java.awt.Color(52, 74, 100)
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnBuscarPersonal, new java.awt.Color(8, 124, 255),
                new java.awt.Color(54, 207, 255), java.awt.Color.WHITE
        );
    }

    private void configurarTabla() {
        tblPersonal.setRowHeight(40);
        tblPersonal.setShowGrid(false);
        tblPersonal.setFillsViewportHeight(true);
        tblPersonal.setSelectionBackground(new java.awt.Color(220, 238, 255));
        tblPersonal.setSelectionForeground(new java.awt.Color(23, 42, 67));
        tblPersonal.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 42));
        tblPersonal.getTableHeader().setReorderingAllowed(false);
        javax.swing.table.DefaultTableCellRenderer encabezado
                = new javax.swing.table.DefaultTableCellRenderer();
        encabezado.setOpaque(true);
        encabezado.setBackground(new java.awt.Color(10, 58, 108));
        encabezado.setForeground(java.awt.Color.WHITE);
        encabezado.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        encabezado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tblPersonal.getTableHeader().setDefaultRenderer(encabezado);
    }

    private void buscarRegistros() {

        String criterio = txtBuscarPersonal.getText().trim();

        try {
            registrosActuales = switch (submoduloActual()) {
                case "ROLES" -> rolControlador.listar(criterio);
                case "PERMISOS" -> permisoControlador.listar(criterio);
                case "ROL - PERMISO" -> listarAsignaciones();
                case "BITACORA" -> bitacoraControlador.listar(criterio);
                default -> usuarioControlador.listar(criterio);
            };

            cargarTabla();
        } catch (RuntimeException ex) {
            mostrarError("No fue posible consultar los registros: "
                    + ex.getMessage());
        }
    }

    private void guardarRegistro() {

        if (!verificarPermiso("CREAR")) {
            return;
        }

        try {
            boolean correcto;

            switch (submoduloActual()) {
                case "ROLES" -> correcto = registrarRol();
                case "PERMISOS" -> correcto = registrarPermiso();
                case "ROL - PERMISO" -> correcto = registrarRolPermiso();
                case "BITACORA" -> {
                    mostrarAdvertencia("La bitácora es de solo consulta.");
                    return;
                }
                default -> correcto = registrarUsuario();
            }

            mostrarResultado(correcto, "Registro guardado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarError("No fue posible guardar el registro: "
                    + mensajeSeguro(ex));
        }
    }

    private void modificarRegistro() {

        if (registroSeleccionado == null) {
            mostrarAdvertencia("Seleccione un registro de la tabla.");
            return;
        }

        if (!verificarPermiso("MODIFICAR")) {
            return;
        }

        try {
            boolean correcto;

            switch (submoduloActual()) {
                case "ROLES" -> correcto = modificarRol();
                case "PERMISOS" -> correcto = modificarPermiso();
                case "ROL - PERMISO" -> correcto = modificarRolPermiso();
                case "BITACORA" -> {
                    mostrarAdvertencia("La bitácora no se modifica.");
                    return;
                }
                default -> correcto = modificarUsuario();
            }

            mostrarResultado(correcto, "Registro modificado correctamente.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            mostrarAdvertencia(ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarError("No fue posible modificar el registro: "
                    + mensajeSeguro(ex));
        }
    }

    private void desactivarRegistro() {

        if (registroSeleccionado == null) {
            mostrarAdvertencia("Seleccione un registro de la tabla.");
            return;
        }

        if (!verificarPermiso("DESACTIVAR")) {
            return;
        }

        boolean correcto;

        switch (submoduloActual()) {
            case "ROLES" -> correcto = rolControlador.desactivar(
                    ((Rol) registroSeleccionado).getIdRol()
            );
            case "PERMISOS" -> correcto = permisoControlador.desactivar(
                    ((Permiso) registroSeleccionado).getIdPermiso()
            );
            case "ROL - PERMISO" -> {
                RolPermiso asignacion = (RolPermiso) registroSeleccionado;
                correcto = rolPermisoControlador.desactivar(
                        asignacion.getIdRol(),
                        asignacion.getIdPermiso()
                );
            }
            case "BITACORA" -> {
                mostrarAdvertencia("La bitácora no se desactiva.");
                return;
            }
            default -> {
                Usuario usuario = (Usuario) registroSeleccionado;
                correcto = gestionarEstadoUsuario(usuario);
            }
        }

        mostrarResultado(correcto, "Estado actualizado correctamente.");
    }

    private void eliminarRegistro() {

        if (!esAdministrador()) {
            mostrarAdvertencia(
                    "Solo el administrador puede eliminar definitivamente."
            );
            return;
        }

        if (registroSeleccionado == null) {
            mostrarAdvertencia("Seleccione un registro de la tabla.");
            return;
        }

        if (!verificarPermiso("ELIMINAR")
                || JOptionPane.showConfirmDialog(
                        this,
                        "¿Eliminar definitivamente el registro seleccionado?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                ) != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            switch (submoduloActual()) {
                case "ROLES" -> rolControlador.eliminarDefinitivamente(
                        ((Rol) registroSeleccionado).getIdRol()
                );
                case "PERMISOS" -> permisoControlador.eliminarDefinitivamente(
                        ((Permiso) registroSeleccionado).getIdPermiso()
                );
                case "ROL - PERMISO" -> {
                    RolPermiso asignacion = (RolPermiso) registroSeleccionado;
                    rolPermisoControlador.eliminarDefinitivamente(
                            asignacion.getIdRol(),
                            asignacion.getIdPermiso()
                    );
                }
                case "BITACORA" -> {
                    mostrarAdvertencia("La bitácora conserva el historial.");
                    return;
                }
                default -> usuarioControlador.eliminarDefinitivamente(
                        ((Usuario) registroSeleccionado).getIdUsuario()
                );
            }

            mostrarResultado(true, "Registro eliminado correctamente.");
        } catch (SQLException ex) {
            mostrarError(
                    "No se puede eliminar porque el registro está relacionado "
                    + "con información histórica."
            );
        }
    }

    private void limpiarFormulario() {
        txtCodigoEmpleado.setText("");
        txtFechaIngreso.setText("");
        txtSalario.setText("");
        if (cboPersona.getItemCount() > 0) {
            cboPersona.setSelectedIndex(0);
        }
        txtCedulaPersona.setText("");
        txtTurno.setText("");
        chkEstadoEmpleado.setSelected(true);
        tblPersonal.clearSelection();
        registroSeleccionado = null;
    }

    private void cambiarSubmodulo() {

        limpiarFormulario();
        boolean lectura = "BITACORA".equals(submoduloActual());
        boolean usuarios = "USUARIOS".equals(submoduloActual());
        boolean asignacion = "ROL - PERMISO".equals(submoduloActual());

        txtFechaIngreso.setEnabled(!lectura);
        txtSalario.setEnabled(!lectura);
        cboPersona.setEnabled(!lectura);
        txtCedulaPersona.setEnabled(!lectura);
        txtTurno.setEnabled(!lectura);
        chkEstadoEmpleado.setEnabled(!lectura);
        btnGuardar.setEnabled(!lectura);
        btnModificar.setEnabled(!lectura);
        btnDesactivar.setEnabled(!lectura);
        btnEliminar.setEnabled(!lectura && esAdministrador());
        txtSalario.setEditable(!usuarios && !asignacion && !lectura);
        txtCedulaPersona.setEditable(!asignacion && !lectura);
        txtFechaIngreso.setEchoChar(usuarios ? '\u2022' : (char) 0);
        txtTurno.setEchoChar(usuarios ? '\u2022' : (char) 0);
        cargarRelacionSegunSubmodulo();

        switch (submoduloActual()) {
            case "ROLES" -> configurarEtiquetas(
                    "Identificador", "Fecha", "Nombre del rol *",
                    "No aplica", "Descripción", "No aplica"
            );
            case "PERMISOS" -> configurarEtiquetas(
                    "Identificador", "Módulo *", "Acción *",
                    "No aplica", "Nombre del permiso *", "Descripción"
            );
            case "ROL - PERMISO" -> configurarEtiquetas(
                    "Asignación", "Fecha de asignación *", "Rol *",
                    "No aplica", "Permiso *", "Estado"
            );
            case "BITACORA" -> configurarEtiquetas(
                    "ID", "Fecha y hora", "Acción", "Usuario",
                    "Módulo", "Resultado"
            );
            default -> configurarEtiquetas(
                    "Identificador", "Confirmar contraseña *", "Rol *",
                    "Persona vinculada *", "Nombre de usuario *", "Contraseña *"
            );
        }

        txtSalario.setToolTipText(usuarios || asignacion
                ? "Haga clic para seleccionar el rol por su nombre."
                : "Ingrese el valor correspondiente al submódulo.");
        txtCedulaPersona.setToolTipText(asignacion
                ? "Haga clic para seleccionar el permiso por su nombre."
                : "Ingrese el valor solicitado.");
        txtCodigoEmpleado.setToolTipText(
                "Identificador automatico y protegido.");

        buscarRegistros();
    }

    private void configurarEtiquetas(
            String codigo,
            String fecha,
            String valor,
            String persona,
            String referencia,
            String detalle
    ) {
        lblCodigoEmpleado.setText(codigo);
        lblFechaIngreso.setText(fecha);
        lblSalario.setText(valor);
        lblPersona.setText(persona);
        lblCedulaPersona.setText(referencia);
        lblTurno.setText(detalle);
        lblTituloFormulario.setText("Datos de "
                + cboTipoContrato.getSelectedItem().toString().toLowerCase());
    }

    private void cargarPersonas() {
        cboPersona.removeAllItems();
        cboPersona.addItem(null);
        try {
            for (Persona persona : personaControlador.listar("")) {
                cboPersona.addItem(persona);
            }
        } catch (SQLException ex) {
            mostrarError("No fue posible cargar las personas: "
                    + ex.getMessage());
        }
    }

    private List<RolPermiso> listarAsignaciones() {
        List<RolPermiso> asignaciones = new ArrayList<>();
        for (Rol rol : rolControlador.listar("")) {
            asignaciones.addAll(
                    rolPermisoControlador.listarPorRol(rol.getIdRol())
            );
        }
        return asignaciones;
    }

    private void cargarTabla() {

        String[] columnas = columnasSubmodulo();
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        for (Object registro : registrosActuales) {
            modelo.addRow(filaRegistro(registro));
        }

        tblPersonal.setModel(modelo);
        lblCantidadPersonal.setText(
                registrosActuales.size() + " registros encontrados"
        );
    }

    private String[] columnasSubmodulo() {
        return switch (submoduloActual()) {
            case "ROLES" -> new String[]{"ID", "Rol", "Descripción", "Estado"};
            case "PERMISOS" -> new String[]{
                "ID", "Permiso", "Módulo", "Acción", "Estado"
            };
            case "ROL - PERMISO" -> new String[]{
                "Rol", "Permiso", "Fecha", "Estado"
            };
            case "BITACORA" -> new String[]{
                "ID", "Fecha", "Usuario", "Módulo", "Acción", "Resultado"
            };
            default -> new String[]{
                "ID", "Usuario", "Rol", "Persona", "Bloqueado", "Estado"
            };
        };
    }

    private Object[] filaRegistro(Object registro) {
        if (registro instanceof Usuario usuario) {
            return new Object[]{
                usuario.getIdUsuario(), usuario.getNombreUsuario(),
                usuario.getNombreRol(), usuario.getIdPersona(),
                usuario.isBloqueado() ? "SÍ" : "NO",
                usuario.isEstadoUsuario() ? "ACTIVO" : "INACTIVO"
            };
        }
        if (registro instanceof Rol rol) {
            return new Object[]{
                rol.getIdRol(), rol.getNombreRol(), rol.getDescripcion(),
                rol.isEstadoRol() ? "ACTIVO" : "INACTIVO"
            };
        }
        if (registro instanceof Permiso permiso) {
            return new Object[]{
                permiso.getIdPermiso(), permiso.getNombrePermiso(),
                permiso.getModulo(), permiso.getAccion(),
                permiso.isEstadoPermiso() ? "ACTIVO" : "INACTIVO"
            };
        }
        if (registro instanceof RolPermiso asignacion) {
            return new Object[]{
                asignacion.getIdRol(), asignacion.getIdPermiso(),
                asignacion.getFechaAsignacion(), asignacion.getEstadoAsignacion()
            };
        }
        Bitacora bitacora = (Bitacora) registro;
        return new Object[]{
            bitacora.getIdBitacora(), bitacora.getFechaHora(),
            bitacora.getNombreUsuario(), bitacora.getModulo(),
            bitacora.getAccionRealizada(), bitacora.getResultado()
        };
    }

    private void seleccionarRegistro() {
        int fila = tblPersonal.getSelectedRow();
        if (fila < 0 || fila >= registrosActuales.size()) {
            return;
        }

        registroSeleccionado = registrosActuales.get(fila);
        cargarRegistroFormulario(registroSeleccionado);
    }

    private void cargarRegistroFormulario(Object registro) {
        txtTurno.setText("");

        if (registro instanceof Usuario usuario) {
            txtCodigoEmpleado.setText(String.valueOf(usuario.getIdUsuario()));
            txtFechaIngreso.setText(valor(usuario.getFechaCreacion()));
            txtSalario.setText(nombreRol(usuario.getIdRol()));
            seleccionarPersona(usuario.getIdPersona());
            txtCedulaPersona.setText(usuario.getNombreUsuario());
            txtFechaIngreso.setText("");
            txtTurno.setText("");
            chkEstadoEmpleado.setSelected(usuario.isEstadoUsuario());
        } else if (registro instanceof Rol rol) {
            txtCodigoEmpleado.setText(String.valueOf(rol.getIdRol()));
            txtSalario.setText(rol.getNombreRol());
            txtCedulaPersona.setText(valor(rol.getDescripcion()));
            chkEstadoEmpleado.setSelected(rol.isEstadoRol());
        } else if (registro instanceof Permiso permiso) {
            txtCodigoEmpleado.setText(String.valueOf(permiso.getIdPermiso()));
            txtFechaIngreso.setText(permiso.getModulo());
            txtSalario.setText(permiso.getAccion());
            txtCedulaPersona.setText(permiso.getNombrePermiso());
            txtTurno.setText(valor(permiso.getDescripcion()));
            chkEstadoEmpleado.setSelected(permiso.isEstadoPermiso());
        } else if (registro instanceof RolPermiso asignacion) {
            txtFechaIngreso.setText(valor(asignacion.getFechaAsignacion()));
            txtSalario.setText(nombreRol(asignacion.getIdRol()));
            txtCedulaPersona.setText(nombrePermiso(asignacion.getIdPermiso()));
            txtTurno.setText(asignacion.getEstadoAsignacion());
            chkEstadoEmpleado.setSelected(
                    "ACTIVA".equalsIgnoreCase(asignacion.getEstadoAsignacion())
            );
        } else if (registro instanceof Bitacora bitacora) {
            txtCodigoEmpleado.setText(String.valueOf(bitacora.getIdBitacora()));
            txtFechaIngreso.setText(valor(bitacora.getFechaHora()));
            txtSalario.setText(valor(bitacora.getAccionRealizada()));
            txtCedulaPersona.setText(valor(bitacora.getModulo()));
            txtTurno.setText(valor(bitacora.getResultado()));
        }
    }

    private boolean registrarUsuario() {
        Persona persona = personaSeleccionada();
        if (persona == null) {
            mostrarAdvertencia("Seleccione una persona vinculada.");
            return false;
        }
        char[] clave = txtTurno.getPassword();
        char[] confirmacion = txtFechaIngreso.getPassword();
        validarConfirmacionClave(clave, confirmacion, true);
        Usuario usuario = new Usuario();
        usuario.setIdPersona(persona.getIdPersona());
        usuario.setIdRol(resolverRol(txtSalario.getText()));
        usuario.setNombreUsuario(txtCedulaPersona.getText().trim());
        usuario.setEstadoUsuario(chkEstadoEmpleado.isSelected());
        try {
            return usuarioControlador.registrar(usuario, clave);
        } finally {
            java.util.Arrays.fill(confirmacion, '\0');
        }
    }

    private boolean modificarUsuario() {
        Usuario usuario = (Usuario) registroSeleccionado;
        Persona persona = personaSeleccionada();
        usuario.setIdPersona(persona == null ? null : persona.getIdPersona());
        usuario.setIdRol(resolverRol(txtSalario.getText()));
        usuario.setNombreUsuario(txtCedulaPersona.getText().trim());
        usuario.setEstadoUsuario(chkEstadoEmpleado.isSelected());
        char[] clave = txtTurno.getPassword();
        char[] confirmacion = txtFechaIngreso.getPassword();
        boolean cambiarClave = clave.length > 0 || confirmacion.length > 0;
        if (cambiarClave) {
            validarConfirmacionClave(clave, confirmacion, false);
        }
        boolean correcto = usuarioControlador.modificar(usuario);
        if (correcto && cambiarClave) {
            correcto = usuarioControlador.cambiarClave(
                    usuario.getIdUsuario(), clave
            );
        }
        java.util.Arrays.fill(confirmacion, '\0');
        return correcto;
    }

    private boolean registrarRol() {
        Rol rol = new Rol();
        rol.setNombreRol(txtSalario.getText().trim());
        rol.setDescripcion(txtCedulaPersona.getText().trim());
        rol.setEstadoRol(chkEstadoEmpleado.isSelected());
        return rolControlador.registrar(rol);
    }

    private boolean modificarRol() {
        Rol rol = (Rol) registroSeleccionado;
        rol.setNombreRol(txtSalario.getText().trim());
        rol.setDescripcion(txtCedulaPersona.getText().trim());
        rol.setEstadoRol(chkEstadoEmpleado.isSelected());
        return rolControlador.modificar(rol);
    }

    private boolean registrarPermiso() {
        Permiso permiso = new Permiso();
        completarPermiso(permiso);
        return permisoControlador.registrar(permiso);
    }

    private boolean modificarPermiso() {
        Permiso permiso = (Permiso) registroSeleccionado;
        completarPermiso(permiso);
        return permisoControlador.modificar(permiso);
    }

    private void completarPermiso(Permiso permiso) {
        permiso.setModulo(txtFechaIngreso.getText().trim().toUpperCase());
        permiso.setAccion(txtSalario.getText().trim().toUpperCase());
        permiso.setNombrePermiso(txtCedulaPersona.getText().trim());
        permiso.setDescripcion(txtTurno.getText().trim());
        permiso.setEstadoPermiso(chkEstadoEmpleado.isSelected());
    }

    private boolean registrarRolPermiso() {
        RolPermiso asignacion = new RolPermiso();
        completarRolPermiso(asignacion);
        return rolPermisoControlador.registrar(asignacion);
    }

    private boolean modificarRolPermiso() {
        RolPermiso asignacion = (RolPermiso) registroSeleccionado;
        completarRolPermiso(asignacion);
        return rolPermisoControlador.modificar(asignacion);
    }

    private void completarRolPermiso(RolPermiso asignacion) {
        asignacion.setIdRol(resolverRol(txtSalario.getText()));
        asignacion.setIdPermiso(resolverPermiso(txtCedulaPersona.getText()));
        asignacion.setFechaAsignacion(
                txtFechaIngreso.getText().isBlank()
                        ? LocalDate.now()
                        : LocalDate.parse(txtFechaIngreso.getText().trim())
        );
        asignacion.setEstadoAsignacion(
                chkEstadoEmpleado.isSelected() ? "ACTIVA" : "INACTIVA"
        );
    }

    private boolean gestionarEstadoUsuario(Usuario usuario) {
        Object[] opciones = {
            "Activar", "Desactivar", "Bloquear", "Desbloquear", "Cancelar"
        };
        int opcion = JOptionPane.showOptionDialog(
                this,
                "Seleccione la acción para el usuario.",
                "Estado del usuario",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[4]
        );

        return switch (opcion) {
            case 0 -> usuarioControlador.activar(usuario.getIdUsuario());
            case 1 -> usuarioControlador.desactivar(usuario.getIdUsuario());
            case 2 -> usuarioControlador.bloquear(usuario.getIdUsuario());
            case 3 -> usuarioControlador.desbloquear(usuario.getIdUsuario());
            default -> false;
        };
    }

    private boolean verificarPermiso(String accion) {
        String modulo = switch (submoduloActual()) {
            case "ROLES" -> "ROLES";
            case "PERMISOS", "ROL - PERMISO" -> "PERMISOS";
            case "BITACORA" -> "BITACORA";
            default -> "USUARIOS";
        };

        if (autorizacionControlador.tienePermiso(modulo, accion)) {
            return true;
        }

        mostrarAdvertencia(autorizacionControlador.getMensaje());
        return false;
    }

    private String submoduloActual() {
        Object seleccionado = cboTipoContrato.getSelectedItem();
        if (seleccionado == null) {
            return "USUARIOS";
        }
        return switch (seleccionado.toString().trim().toUpperCase()) {
            case "CUENTAS DE USUARIO" -> "USUARIOS";
            case "ROLES DEL SISTEMA" -> "ROLES";
            case "PERMISOS DEL SISTEMA" -> "PERMISOS";
            case "ASIGNAR PERMISOS A ROL" -> "ROL - PERMISO";
            case "BITACORA DE AUDITORIA" -> "BITACORA";
            default -> seleccionado.toString().trim().toUpperCase();
        };
    }

    private Integer resolverRol(String texto) {
        String valorRol = texto == null ? "" : texto.trim();
        for (Rol rol : rolControlador.listar("")) {
            if (rol.isEstadoRol()
                    && rol.getNombreRol().equalsIgnoreCase(valorRol)
                    && esNombreRolSistema(rol.getNombreRol())) {
                return rol.getIdRol();
            }
        }
        throw new IllegalArgumentException(
                "Rol invalido. Use Administrador, Recepcionista, Entrenador, "
                + "Nutricionista o Cliente."
        );
    }

    private Integer resolverPermiso(String texto) {
        String valorPermiso = texto == null ? "" : texto.trim();
        for (Permiso permiso : permisoControlador.listar("")) {
            if (permiso.isEstadoPermiso()
                    && permiso.getNombrePermiso().equalsIgnoreCase(valorPermiso)) {
                return permiso.getIdPermiso();
            }
        }
        throw new IllegalArgumentException(
                "Permiso inválido. Selecciónelo por su nombre."
        );
    }

    private void seleccionarRolPorNombre() {
        java.util.List<Rol> roles = rolesSistemaActivos();
        if (roles.isEmpty()) {
            mostrarAdvertencia("No existen roles activos configurados.");
            return;
        }
        Rol seleccionado = (Rol) JOptionPane.showInputDialog(
                this, "Seleccione el rol que tendrá la cuenta:",
                "Rol del sistema", JOptionPane.QUESTION_MESSAGE,
                null, roles.toArray(), roles.get(0)
        );
        if (seleccionado != null) {
            txtSalario.setText(seleccionado.getNombreRol());
        }
    }

    private void seleccionarPermisoPorNombre() {
        java.util.List<Permiso> permisos = permisoControlador.listar("").stream()
                .filter(Permiso::isEstadoPermiso)
                .toList();
        if (permisos.isEmpty()) {
            mostrarAdvertencia("No existen permisos activos configurados.");
            return;
        }
        Permiso seleccionado = (Permiso) JOptionPane.showInputDialog(
                this, "Seleccione el permiso que se asignará al rol:",
                "Permiso del sistema", JOptionPane.QUESTION_MESSAGE,
                null, permisos.toArray(), permisos.get(0)
        );
        if (seleccionado != null) {
            txtCedulaPersona.setText(seleccionado.getNombrePermiso());
        }
    }

    private java.util.List<Rol> rolesSistemaActivos() {
        return rolControlador.listar("").stream()
                .filter(Rol::isEstadoRol)
                .filter(rol -> esNombreRolSistema(rol.getNombreRol()))
                .toList();
    }

    private boolean esNombreRolSistema(String nombre) {
        if (nombre == null) {
            return false;
        }
        return java.util.Set.of("ADMINISTRADOR", "RECEPCIONISTA",
                "ENTRENADOR", "NUTRICIONISTA", "CLIENTE")
                .contains(nombre.trim().toUpperCase(java.util.Locale.ROOT));
    }

    private String nombreRol(Integer idRol) {
        return rolControlador.listar("").stream()
                .filter(rol -> java.util.Objects.equals(rol.getIdRol(), idRol))
                .map(Rol::getNombreRol).findFirst().orElse("");
    }

    private String nombrePermiso(Integer idPermiso) {
        return permisoControlador.listar("").stream()
                .filter(permiso -> java.util.Objects.equals(
                        permiso.getIdPermiso(), idPermiso))
                .map(Permiso::getNombrePermiso).findFirst().orElse("");
    }

    private void cargarRelacionSegunSubmodulo() {
        cboPersona.removeAllItems();
        if ("USUARIOS".equals(submoduloActual())) {
            cargarPersonas();
            cboPersona.setEnabled(true);
        } else {
            cboPersona.setEnabled(false);
        }
    }

    private void validarConfirmacionClave(
            char[] clave, char[] confirmacion, boolean obligatoria) {
        if (obligatoria && clave.length == 0) {
            throw new IllegalArgumentException("Ingrese una contraseña.");
        }
        if (!java.util.Arrays.equals(clave, confirmacion)) {
            java.util.Arrays.fill(clave, '\0');
            java.util.Arrays.fill(confirmacion, '\0');
            throw new IllegalArgumentException(
                    "La contraseña y su confirmación no coinciden."
            );
        }
    }

    private String mensajeSeguro(Throwable error) {
        return error.getMessage() == null || error.getMessage().isBlank()
                ? "Error inesperado controlado por GYMNOVA."
                : error.getMessage();
    }

    private Persona personaSeleccionada() {
        Object seleccionado = cboPersona.getSelectedItem();
        return seleccionado instanceof Persona persona ? persona : null;
    }

    private void seleccionarPersona(Long idPersona) {
        for (int i = 0; i < cboPersona.getItemCount(); i++) {
            Object item = cboPersona.getItemAt(i);
            if (item instanceof Persona persona
                    && java.util.Objects.equals(
                            persona.getIdPersona(), idPersona
                    )) {
                cboPersona.setSelectedIndex(i);
                return;
            }
        }
        cboPersona.setSelectedIndex(0);
    }

    private Integer entero(String texto, String campo) {
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    campo + " debe contener un número entero."
            );
        }
    }

    private String valor(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private void mostrarResultado(boolean correcto, String mensajeCorrecto) {
        if (correcto) {
            JOptionPane.showMessageDialog(
                    this, mensajeCorrecto, "GYMNOVA",
                    JOptionPane.INFORMATION_MESSAGE
            );
            limpiarFormulario();
            buscarRegistros();
        } else {
            mostrarError(mensajeControladorActual());
        }
    }

    private String mensajeControladorActual() {
        return switch (submoduloActual()) {
            case "ROLES" -> rolControlador.getMensaje();
            case "PERMISOS" -> permisoControlador.getMensaje();
            case "ROL - PERMISO" -> rolPermisoControlador.getMensaje();
            case "BITACORA" -> bitacoraControlador.getMensaje();
            default -> usuarioControlador.getMensaje();
        };
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(
                this, mensaje, "Atención", JOptionPane.WARNING_MESSAGE
        );
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje == null || mensaje.isBlank()
                        ? "No fue posible completar la operación."
                        : mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private boolean esAdministrador() {
        return utilidades.SesionUsuario.haySesionActiva()
                && "Administrador".equalsIgnoreCase(
                        utilidades.SesionUsuario.getUsuarioActual().getNombreRol()
                );
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBaseSeguridad = new javax.swing.JPanel();
        pnlEncabezadoPersonal = new javax.swing.JPanel();
        lblTituloPersonal = new javax.swing.JLabel();
        lblModuloPersonal = new javax.swing.JLabel();
        pnlFormularioPersonal = new javax.swing.JPanel();
        lblTituloFormulario = new javax.swing.JLabel();
        lblCamposObligatorios = new javax.swing.JLabel();
        lblCodigoEmpleado = new javax.swing.JLabel();
        txtCodigoEmpleado = new javax.swing.JTextField();
        lblFechaIngreso = new javax.swing.JLabel();
        txtFechaIngreso = new javax.swing.JPasswordField();
        lblTipoContrato = new javax.swing.JLabel();
        cboTipoContrato = new javax.swing.JComboBox<>();
        lblSalario = new javax.swing.JLabel();
        txtSalario = new javax.swing.JTextField();
        cboPersona = new javax.swing.JComboBox<>();
        lblPersona = new javax.swing.JLabel();
        lblCedulaPersona = new javax.swing.JLabel();
        txtCedulaPersona = new javax.swing.JTextField();
        lblTurno = new javax.swing.JLabel();
        txtTurno = new javax.swing.JPasswordField();
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

        pnlBaseSeguridad.setBackground(new java.awt.Color(234, 242, 251));

        pnlEncabezadoPersonal.setBackground(new java.awt.Color(255, 255, 255));
        pnlEncabezadoPersonal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlEncabezadoPersonal.setPreferredSize(new java.awt.Dimension(1000, 70));

        lblTituloPersonal.setFont(new java.awt.Font("SansSerif", 1, 25)); // NOI18N
        lblTituloPersonal.setForeground(new java.awt.Color(23, 42, 67));
        lblTituloPersonal.setText("Administracion de seguridad");
        lblTituloPersonal.setPreferredSize(new java.awt.Dimension(350, 35));

        lblModuloPersonal.setForeground(new java.awt.Color(8, 124, 255));
        lblModuloPersonal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblModuloPersonal.setText("SEGURIDAD");
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
        lblTituloFormulario.setText("Configuración de seguridad");
        lblTituloFormulario.setPreferredSize(new java.awt.Dimension(300, 30));

        lblCamposObligatorios.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblCamposObligatorios.setForeground(new java.awt.Color(107, 127, 153));
        lblCamposObligatorios.setText("Los campos con * son obligatorios");

        lblCodigoEmpleado.setText("Identificador / usuario *");

        txtCodigoEmpleado.setPreferredSize(new java.awt.Dimension(300, 32));

        lblFechaIngreso.setText("Fecha de registro *");

        txtFechaIngreso.setPreferredSize(new java.awt.Dimension(300, 32));

        lblTipoContrato.setText("Submodulo *");

        cboTipoContrato.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cuentas de usuario", "Roles del sistema", "Permisos del sistema", "Asignar permisos a rol", "Bitacora de auditoria" }));

        cboTipoContrato.setPreferredSize(new java.awt.Dimension(300, 32));

        lblSalario.setText("Rol asignado *");

        txtSalario.setToolTipText("Escriba el nombre del rol, por ejemplo Entrenador");
        txtSalario.setPreferredSize(new java.awt.Dimension(300, 32));

        cboPersona.setPreferredSize(new java.awt.Dimension(300, 32));

        lblPersona.setText("Persona vinculada *");

        lblCedulaPersona.setText("Correo / referencia");

        txtCedulaPersona.setEditable(false);
        txtCedulaPersona.setPreferredSize(new java.awt.Dimension(300, 32));

        lblTurno.setText("Descripcion o permiso");

        txtTurno.setPreferredSize(new java.awt.Dimension(300, 32));

        chkEstadoEmpleado.setBackground(new java.awt.Color(255, 255, 255));
        chkEstadoEmpleado.setForeground(new java.awt.Color(23, 42, 67));
        chkEstadoEmpleado.setSelected(true);
        chkEstadoEmpleado.setText("Registro activo");

        btnGuardar.setBackground(new java.awt.Color(8, 124, 255));
        btnGuardar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar registro");
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
        lblBuscarPersonal.setText("Buscar usuarios, roles o permisos...");
        lblBuscarPersonal.setPreferredSize(new java.awt.Dimension(500, 20));

        lblCantidadPersonal.setText("0 registros encontrados");

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

        javax.swing.GroupLayout pnlBaseSeguridadLayout = new javax.swing.GroupLayout(pnlBaseSeguridad);
        pnlBaseSeguridad.setLayout(pnlBaseSeguridadLayout);
        pnlBaseSeguridadLayout.setHorizontalGroup(
            pnlBaseSeguridadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlEncabezadoPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, 1175, Short.MAX_VALUE)
            .addGroup(pnlBaseSeguridadLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlBaseSeguridadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFormularioPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTablaPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlBaseSeguridadLayout.setVerticalGroup(
            pnlBaseSeguridadLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBaseSeguridadLayout.createSequentialGroup()
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
            .addComponent(pnlBaseSeguridad, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBaseSeguridad, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarRegistro();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void txtBuscarPersonalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarPersonalActionPerformed
        buscarRegistros();
    }//GEN-LAST:event_txtBuscarPersonalActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarFormulario();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        modificarRegistro();
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnDesactivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDesactivarActionPerformed
        desactivarRegistro();
    }//GEN-LAST:event_btnDesactivarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminarRegistro();
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
    private javax.swing.JPanel pnlBaseSeguridad;
    private javax.swing.JPanel pnlEncabezadoPersonal;
    private javax.swing.JPanel pnlFormularioPersonal;
    private javax.swing.JPanel pnlTablaPersonal;
    private javax.swing.JTable tblPersonal;
    private javax.swing.JTextField txtBuscarPersonal;
    private javax.swing.JTextField txtCedulaPersona;
    private javax.swing.JTextField txtCodigoEmpleado;
    private javax.swing.JPasswordField txtFechaIngreso;
    private javax.swing.JPasswordField txtTurno;
    private javax.swing.JComboBox<String> cboTipoContrato;
    private javax.swing.JTextField txtSalario;
    // End of variables declaration//GEN-END:variables
}
