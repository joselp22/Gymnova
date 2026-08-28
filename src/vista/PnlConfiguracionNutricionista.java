package vista;

import controlador.NutricionistaWorkspaceControlador;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import modelo.Nutricionista;
import modelo.Usuario;
import utilidades.SesionUsuario;

/** Configuración personal y de seguridad del Nutricionista. */
public class PnlConfiguracionNutricionista extends JPanel {

    private final NutricionistaWorkspaceControlador controlador
            = new NutricionistaWorkspaceControlador();

    private final JTextField txtNombre = campoLectura();
    private final JTextField txtCedula = campoLectura();
    private final JTextField txtCorreo = campoLectura();
    private final JTextField txtTelefono = campoLectura();
    private final JTextField txtLicencia = campoLectura();
    private final JTextField txtEstadoLicencia = campoLectura();
    private final JTextField txtUsuario = campoLectura();
    private final JTextField txtRol = campoLectura();

    private final JPasswordField txtActual = new JPasswordField();
    private final JPasswordField txtNueva = new JPasswordField();
    private final JPasswordField txtConfirmar = new JPasswordField();

    public PnlConfiguracionNutricionista() {
        construir();
        refrescarDatos();
    }

    private void construir() {
        setLayout(new BorderLayout());
        setBackground(NutricionistaUI.FONDO);
        JPanel pagina = NutricionistaUI.pagina();
        pagina.add(NutricionistaUI.encabezado(
                "Mi configuración",
                "Consulte su perfil profesional y administre la seguridad de su cuenta."),
                BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(1, 2, 14, 0));
        centro.setOpaque(false);

        JPanel perfil = NutricionistaUI.tarjeta();
        perfil.setLayout(new BorderLayout(0, 14));
        perfil.add(NutricionistaUI.tituloSeccion("Perfil profesional"), BorderLayout.NORTH);
        JPanel camposPerfil = new JPanel(new GridLayout(4, 2, 10, 10));
        camposPerfil.setOpaque(false);
        agregarCampo(camposPerfil, "Nombre", txtNombre);
        agregarCampo(camposPerfil, "Cédula", txtCedula);
        agregarCampo(camposPerfil, "Correo", txtCorreo);
        agregarCampo(camposPerfil, "Teléfono", txtTelefono);
        agregarCampo(camposPerfil, "Licencia", txtLicencia);
        agregarCampo(camposPerfil, "Estado licencia", txtEstadoLicencia);
        agregarCampo(camposPerfil, "Usuario", txtUsuario);
        agregarCampo(camposPerfil, "Rol", txtRol);
        perfil.add(camposPerfil, BorderLayout.CENTER);
        JLabel nota = new JLabel("Los datos profesionales son administrados desde Personal por el Administrador.");
        nota.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        perfil.add(nota, BorderLayout.SOUTH);
        centro.add(perfil);

        JPanel seguridad = NutricionistaUI.tarjeta();
        seguridad.setLayout(new BorderLayout(0, 14));
        seguridad.add(NutricionistaUI.tituloSeccion("Seguridad de la cuenta"), BorderLayout.NORTH);
        JPanel camposClave = new JPanel(new GridLayout(3, 1, 0, 10));
        camposClave.setOpaque(false);
        agregarCampo(camposClave, "Contraseña actual", txtActual);
        agregarCampo(camposClave, "Nueva contraseña", txtNueva);
        agregarCampo(camposClave, "Confirmar nueva contraseña", txtConfirmar);
        seguridad.add(camposClave, BorderLayout.CENTER);
        JButton btnCambiar = NutricionistaUI.boton("Cambiar contraseña");
        btnCambiar.addActionListener(e -> cambiarClave());
        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.add(btnCambiar, BorderLayout.WEST);
        JLabel ayuda = new JLabel("Use una contraseña segura según las reglas de GYMNOVA.");
        ayuda.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        sur.add(ayuda, BorderLayout.CENTER);
        seguridad.add(sur, BorderLayout.SOUTH);
        centro.add(seguridad);

        pagina.add(centro, BorderLayout.CENTER);
        add(pagina, BorderLayout.CENTER);
    }

    public void refrescarDatos() {
        Usuario u = SesionUsuario.getUsuarioActual();
        txtUsuario.setText(valor(u.getNombreUsuario()));
        txtRol.setText(valor(u.getNombreRol()));
        Nutricionista n = controlador.perfilActual();
        if (n != null) {
            txtNombre.setText(n.getNombreCompleto());
            txtCedula.setText(valor(n.getCedula()));
            txtCorreo.setText(valor(n.getCorreo()));
            txtTelefono.setText(valor(n.getTelefono()));
            txtLicencia.setText(valor(n.getNumeroLicencia()));
            txtEstadoLicencia.setText(valor(n.getEstadoLicencia()));
        }
    }

    private void cambiarClave() {
        char[] actual = txtActual.getPassword();
        char[] nueva = txtNueva.getPassword();
        char[] confirmar = txtConfirmar.getPassword();
        try {
            boolean ok = controlador.cambiarClave(actual, nueva, confirmar);
            javax.swing.JOptionPane.showMessageDialog(this,
                    controlador.getMensaje(), "GYMNOVA",
                    ok ? javax.swing.JOptionPane.INFORMATION_MESSAGE : javax.swing.JOptionPane.WARNING_MESSAGE);
            if (ok) {
                txtActual.setText("");
                txtNueva.setText("");
                txtConfirmar.setText("");
                refrescarDatos();
            }
        } finally {
            Arrays.fill(actual, '\0');
            Arrays.fill(nueva, '\0');
            Arrays.fill(confirmar, '\0');
        }
    }

    private void agregarCampo(JPanel panel, String etiqueta, java.awt.Component campo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(NutricionistaUI.etiqueta(etiqueta), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        panel.add(p);
    }

    private static JTextField campoLectura() {
        JTextField t = NutricionistaUI.campo();
        t.setEditable(false);
        t.setBackground(new java.awt.Color(244, 247, 250));
        return t;
    }

    private String valor(Object o) {
        return o == null || o.toString().isBlank() ? "—" : o.toString();
    }
}
