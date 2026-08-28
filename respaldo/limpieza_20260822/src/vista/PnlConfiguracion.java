/* GYMNOVA - Vista administrativa: CONFIGURACION. */
package vista;

import controlador.UsuarioControlador;
import controlador.PersonaControlador;
import java.nio.file.Files;
import java.io.InputStream;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Usuario;
import utilidades.SesionUsuario;

public class PnlConfiguracion extends javax.swing.JPanel {

    private final UsuarioControlador usuarioControlador;
    private final PersonaControlador personaControlador;
    private final Runnable alActualizarPerfil;
    private Usuario usuarioActual;

    public PnlConfiguracion() {
        this(() -> { });
    }

    public PnlConfiguracion(Runnable alActualizarPerfil) {
        initComponents();
        usuarioControlador = new UsuarioControlador();
        personaControlador = new PersonaControlador();
        this.alActualizarPerfil = alActualizarPerfil == null
                ? () -> { } : alActualizarPerfil;
        cboTipoContrato.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Datos de contacto", "Alias de usuario",
                    "Avatar", "Contraseña"}));
        configurarEstilos();
        configurarTabla();
        btnBuscarPersonal.addActionListener(evento -> buscarRegistros());
        cboTipoContrato.addActionListener(evento -> configurarSeccion());
        txtCodigoEmpleado.setEditable(false);
        txtCedulaPersona.setEditable(false);
        btnEliminar.setVisible(false);
        refrescarDatos();
    }

    public void refrescarDatos() {
        if (!SesionUsuario.haySesionActiva()) {
            return;
        }
        usuarioActual = usuarioControlador.buscar(
                SesionUsuario.getUsuarioActual().getIdUsuario()
        );
        if (usuarioActual == null) {
            usuarioActual = SesionUsuario.getUsuarioActual();
        }
        cargarPersonaVinculada();
        configurarSeccion();
        cargarTablaUsuario();
    }

    private void cargarPersonaVinculada() {
        cboPersona.removeAllItems();
        if (usuarioActual == null || usuarioActual.getIdPersona() == null) {
            return;
        }
        try {
            personaControlador.buscarPorId(usuarioActual.getIdPersona())
                    .ifPresent(cboPersona::addItem);
        } catch (Exception ex) {
            mostrarAdvertencia(
                    "No fue posible cargar la persona vinculada: "
                    + ex.getMessage());
        }
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
        cargarTablaUsuario();
    }

    private void guardarRegistro() {
        guardarConfiguracion();
    }

    private void modificarRegistro() {
        guardarConfiguracion();
    }

    private void desactivarRegistro() {
        limpiarFormulario();
        JOptionPane.showMessageDialog(
                this,
                "Las preferencias locales volvieron a sus valores visuales predeterminados.",
                "Configuración",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void eliminarRegistro() {
        // Configuración no elimina información histórica ni estructural.
    }

    private void limpiarFormulario() {
        txtCodigoEmpleado.setText("");
        txtFechaIngreso.setText("");
        cboTipoContrato.setSelectedIndex(0);
        txtSalario.setText("");
        cboPersona.setSelectedIndex(-1);
        txtCedulaPersona.setText("");
        txtTurno.setText("");
        chkEstadoEmpleado.setSelected(true);
        tblPersonal.clearSelection();
    }

    private boolean esAdministrador() {
        return utilidades.SesionUsuario.haySesionActiva()
                && "Administrador".equalsIgnoreCase(
                        utilidades.SesionUsuario.getUsuarioActual().getNombreRol()
                );
    }

    private void configurarSeccion() {
        if (usuarioActual == null) {
            return;
        }

        String seccion = String.valueOf(cboTipoContrato.getSelectedItem());
        modelo.Persona persona = (modelo.Persona) cboPersona.getSelectedItem();
        txtCodigoEmpleado.setText(String.valueOf(usuarioActual.getIdUsuario()));
        txtFechaIngreso.setText(String.valueOf(usuarioActual.getFechaCreacion()));
        txtSalario.setText("Datos de contacto".equalsIgnoreCase(seccion)
                && persona != null ? persona.getCorreo()
                : usuarioActual.getNombreUsuario());
        txtCedulaPersona.setText(usuarioActual.getNombreRol());
        txtTurno.setText("Datos de contacto".equalsIgnoreCase(seccion)
                && persona != null ? persona.getTelefono() : "");
        chkEstadoEmpleado.setSelected(usuarioActual.isEstadoUsuario());

        lblCodigoEmpleado.setText("ID de mi cuenta");
        lblFechaIngreso.setText("Cuenta creada el");
        lblSalario.setText("Datos de contacto".equalsIgnoreCase(seccion)
                ? "Correo electrónico" : "Alias de acceso");
        lblPersona.setText("Persona vinculada (no editable)");
        lblCedulaPersona.setText("Rol asignado (no editable)");
        lblTurno.setText("Datos de contacto".equalsIgnoreCase(seccion)
                ? "Teléfono" : "Nueva contraseña segura");
        lblTituloFormulario.setText("Mi cuenta: " + seccion);

        boolean contacto = "Datos de contacto".equalsIgnoreCase(seccion);
        boolean alias = "Alias de usuario".equalsIgnoreCase(seccion);
        boolean avatar = "Avatar".equalsIgnoreCase(seccion);
        boolean clave = "Contraseña".equalsIgnoreCase(seccion);
        txtSalario.setEditable(alias || contacto);
        txtFechaIngreso.setEditable(clave);
        txtFechaIngreso.setEchoChar(clave ? '\u2022' : (char) 0);
        txtTurno.setEchoChar(clave ? '\u2022' : (char) 0);
        lblFechaIngreso.setText(clave
                ? "Confirmar nueva contraseña"
                : "Cuenta creada el");
        if (clave) {
            txtFechaIngreso.setText("");
        }
        txtTurno.setEnabled(clave || contacto);
        txtTurno.setEditable(clave || contacto);
        if (!clave && !contacto) {
            txtTurno.setText("");
        }
        btnGuardar.setText(contacto ? "Guardar datos de contacto"
                : alias ? "Guardar nuevo alias"
                : avatar ? "Seleccionar y guardar avatar"
                : "Cambiar contraseña");
        txtSalario.setToolTipText(contacto
                ? "Ingrese su correo electronico de contacto."
                : alias ? "Ingrese el alias que aparecera en su sesion."
                : "Este dato no se utiliza en la opcion seleccionada.");
        txtTurno.setToolTipText(contacto
                ? "Ingrese su numero de telefono de contacto."
                : clave ? "Ingrese una contraseña segura de al menos 8 caracteres."
                : "Este dato no se utiliza en la opcion seleccionada.");
        txtFechaIngreso.setToolTipText(clave
                ? "Repita exactamente la nueva contraseña."
                : "Fecha automatica de creacion de la cuenta; no es editable.");
        cboPersona.setToolTipText(
                "Persona vinculada a la cuenta actual; no puede cambiarse aqui.");
        btnModificar.setVisible(false);
        btnDesactivar.setVisible(false);
        btnLimpiar.setText("Restablecer campos");
    }

    private void guardarConfiguracion() {
        if (usuarioActual == null) {
            return;
        }

        String seccion = String.valueOf(cboTipoContrato.getSelectedItem());
        boolean correcto = true;

        if ("Datos de contacto".equalsIgnoreCase(seccion)) {
            correcto = guardarDatosContacto();
        } else if ("Contraseña".equalsIgnoreCase(seccion)) {
            char[] nueva = txtTurno.getPassword();
            char[] confirmacion = txtFechaIngreso.getPassword();
            try {
                if (nueva.length == 0 || confirmacion.length == 0) {
                    mostrarAdvertencia(
                            "Ingrese y confirme la nueva contraseña."
                    );
                    return;
                }
                if (!Arrays.equals(nueva, confirmacion)) {
                    mostrarAdvertencia(
                            "La contraseña y su confirmación no coinciden."
                    );
                    return;
                }
                correcto = usuarioControlador.cambiarClave(
                        usuarioActual.getIdUsuario(), nueva
                );
            } finally {
                Arrays.fill(nueva, '\0');
                Arrays.fill(confirmacion, '\0');
            }
        } else if ("Alias de usuario".equalsIgnoreCase(seccion)) {
            usuarioActual.setNombreUsuario(txtSalario.getText().trim());
            correcto = usuarioControlador.modificar(usuarioActual);
            if (correcto) {
                SesionUsuario.getUsuarioActual().setNombreUsuario(
                        usuarioActual.getNombreUsuario()
                );
            }
        } else if ("Avatar".equalsIgnoreCase(seccion)) {
            correcto = cambiarAvatar();
        }

        if (correcto) {
            JOptionPane.showMessageDialog(
                    this,
                    "Configuración guardada correctamente.",
                    "GYMNOVA",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refrescarDatos();
            alActualizarPerfil.run();
        } else {
            mostrarAdvertencia(usuarioControlador.getMensaje());
        }
    }

    private boolean guardarDatosContacto() {
        if (usuarioActual.getIdPersona() == null) {
            mostrarAdvertencia("Esta cuenta no tiene una persona vinculada.");
            return false;
        }
        try {
            modelo.Persona persona = personaControlador.buscarPorId(
                    usuarioActual.getIdPersona()).orElse(null);
            if (persona == null) {
                mostrarAdvertencia("No fue posible localizar el perfil vinculado.");
                return false;
            }
            persona.setCorreo(txtSalario.getText().trim());
            persona.setTelefono(new String(txtTurno.getPassword()).trim());
            personaControlador.modificar(persona);
            return true;
        } catch (IllegalArgumentException ex) {
            mostrarAdvertencia(ex.getMessage());
            return false;
        } catch (Exception ex) {
            mostrarAdvertencia(
                    "No fue posible actualizar los datos de contacto: "
                    + ex.getMessage());
            return false;
        }
    }

    private boolean cambiarAvatar() {
        if (usuarioActual.getIdPersona() == null) {
            mostrarAdvertencia("Esta cuenta no tiene una persona vinculada.");
            return false;
        }
        Object[] opciones = {
            "Avatar hombre", "Avatar mujer", "Avatar neutro",
            "Elegir archivo", "Cancelar"
        };
        int opcion = JOptionPane.showOptionDialog(
                this,
                "Seleccione el avatar que se mostrará en todo GYMNOVA.",
                "Biblioteca de avatares",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                opciones,
                opciones[0]
        );

        if (opcion < 0 || opcion == 4) {
            return false;
        }

        if (opcion <= 2) {
            String recurso = switch (opcion) {
                case 0 -> "/recursos/avatar_hombre.png";
                case 1 -> "/recursos/avatar_mujer.png";
                default -> "/recursos/avatar_neutro.png";
            };
            try (InputStream entrada
                    = PnlConfiguracion.class.getResourceAsStream(recurso)) {
                if (entrada == null) {
                    mostrarAdvertencia("No fue posible cargar el avatar.");
                    return false;
                }
                return guardarAvatar(entrada.readAllBytes());
            } catch (Exception ex) {
                mostrarAdvertencia(
                        "No fue posible guardar el avatar: " + ex.getMessage()
                );
                return false;
            }
        }

        javax.swing.JFileChooser selector = new javax.swing.JFileChooser();
        selector.setDialogTitle("Seleccione una imagen PNG o JPG para el avatar");
        selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imagenes PNG y JPG", "png", "jpg", "jpeg"));
        if (selector.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) {
            return false;
        }
        try {
            byte[] datos = Files.readAllBytes(selector.getSelectedFile().toPath());
            if (datos.length > 2_000_000) {
                mostrarAdvertencia("La imagen no debe superar 2 MB.");
                return false;
            }
            if (javax.imageio.ImageIO.read(
                    new java.io.ByteArrayInputStream(datos)) == null) {
                mostrarAdvertencia("El archivo seleccionado no es una imagen valida.");
                return false;
            }
            return guardarAvatar(datos);
        } catch (Exception ex) {
            mostrarAdvertencia("No fue posible guardar el avatar: " + ex.getMessage());
            return false;
        }
    }

    private boolean guardarAvatar(byte[] datos) throws Exception {
        modelo.Persona persona = personaControlador.buscarPorId(
                usuarioActual.getIdPersona()).orElse(null);
        if (persona == null) {
            mostrarAdvertencia("No fue posible localizar el perfil vinculado.");
            return false;
        }
        persona.setFotoPerfil(datos);
        personaControlador.modificar(persona);
        return true;
    }

    private void cargarTablaUsuario() {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"ID", "Usuario", "Rol", "Último acceso", "Estado"},
                0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        if (usuarioActual != null) {
            modelo.addRow(new Object[]{
                usuarioActual.getIdUsuario(),
                usuarioActual.getNombreUsuario(),
                usuarioActual.getNombreRol(),
                usuarioActual.getUltimoAcceso(),
                usuarioActual.isEstadoUsuario() ? "ACTIVO" : "INACTIVO"
            });
        }
        tblPersonal.setModel(modelo);
        lblCantidadPersonal.setText(
                usuarioActual == null
                        ? "0 registros"
                        : "1 usuario de sesión"
        );
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje == null || mensaje.isBlank()
                        ? "No fue posible guardar la configuración."
                        : mensaje,
                "Atención",
                JOptionPane.WARNING_MESSAGE
        );
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBaseConfiguracion = new javax.swing.JPanel();
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

        pnlBaseConfiguracion.setBackground(new java.awt.Color(234, 242, 251));

        pnlEncabezadoPersonal.setBackground(new java.awt.Color(255, 255, 255));
        pnlEncabezadoPersonal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlEncabezadoPersonal.setPreferredSize(new java.awt.Dimension(1000, 70));

        lblTituloPersonal.setFont(new java.awt.Font("SansSerif", 1, 25)); // NOI18N
        lblTituloPersonal.setForeground(new java.awt.Color(23, 42, 67));
        lblTituloPersonal.setText("Configuracion general del sistema");
        lblTituloPersonal.setPreferredSize(new java.awt.Dimension(350, 35));

        lblModuloPersonal.setForeground(new java.awt.Color(8, 124, 255));
        lblModuloPersonal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblModuloPersonal.setText("CONFIGURACION");
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
        lblTituloFormulario.setText("Configuración de mi cuenta");
        lblTituloFormulario.setPreferredSize(new java.awt.Dimension(300, 30));

        lblCamposObligatorios.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblCamposObligatorios.setForeground(new java.awt.Color(107, 127, 153));
        lblCamposObligatorios.setText("Los campos con * son obligatorios");

        lblCodigoEmpleado.setText("Clave de configuracion *");

        txtCodigoEmpleado.setPreferredSize(new java.awt.Dimension(300, 32));

        lblFechaIngreso.setText("Fecha de actualizacion *");

        txtFechaIngreso.setPreferredSize(new java.awt.Dimension(300, 32));

        lblTipoContrato.setText("Seccion *");

        cboTipoContrato.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Datos del gimnasio", "Parametros generales", "Notificaciones", "Seguridad", "Respaldos", "Preferencias" }));

        cboTipoContrato.setPreferredSize(new java.awt.Dimension(300, 32));

        lblSalario.setText("Valor *");

        txtSalario.setToolTipText("Dato editable de la cuenta seleccionada.");
        txtSalario.setPreferredSize(new java.awt.Dimension(300, 32));

        cboPersona.setPreferredSize(new java.awt.Dimension(300, 32));

        lblPersona.setText("Usuario responsable *");

        lblCedulaPersona.setText("Rol / alcance *");

        txtCedulaPersona.setEditable(false);
        txtCedulaPersona.setPreferredSize(new java.awt.Dimension(300, 32));

        lblTurno.setText("Descripcion");

        txtTurno.setPreferredSize(new java.awt.Dimension(300, 32));

        chkEstadoEmpleado.setBackground(new java.awt.Color(255, 255, 255));
        chkEstadoEmpleado.setForeground(new java.awt.Color(23, 42, 67));
        chkEstadoEmpleado.setSelected(true);
        chkEstadoEmpleado.setText("Configuracion activa");

        btnGuardar.setBackground(new java.awt.Color(8, 124, 255));
        btnGuardar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Guardar configuracion");
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
        btnEliminar.setText("Eliminar");
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
        lblBuscarPersonal.setText("Buscar configuraciones...");
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

        javax.swing.GroupLayout pnlBaseConfiguracionLayout = new javax.swing.GroupLayout(pnlBaseConfiguracion);
        pnlBaseConfiguracion.setLayout(pnlBaseConfiguracionLayout);
        pnlBaseConfiguracionLayout.setHorizontalGroup(
            pnlBaseConfiguracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlEncabezadoPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, 1175, Short.MAX_VALUE)
            .addGroup(pnlBaseConfiguracionLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlBaseConfiguracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFormularioPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTablaPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlBaseConfiguracionLayout.setVerticalGroup(
            pnlBaseConfiguracionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBaseConfiguracionLayout.createSequentialGroup()
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
            .addComponent(pnlBaseConfiguracion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBaseConfiguracion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
    private javax.swing.JPanel pnlBaseConfiguracion;
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
