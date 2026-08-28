package pruebas;

import controlador.AutorizacionControlador;
import controlador.LoginControlador;
import controlador.RolControlador;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Set;
import javax.swing.JComboBox;
import modelo.Rol;
import modelo.Usuario;
import utilidades.SesionUsuario;
import vista.PnlSeguridad;

/** Pruebas adversariales del modulo Seguridad y del deny-by-default. */
public final class PruebaSeguridadIntegral {

    private PruebaSeguridadIntegral() {
    }

    public static void main(String[] args) throws Exception {
        LoginControlador login = new LoginControlador();
        Usuario admin = login.iniciarSesion(
                "admin", "Gymnova2026*".toCharArray());
        if (admin == null) {
            throw new AssertionError("No fue posible autenticar al administrador");
        }
        SesionUsuario.iniciarSesion(admin);

        Set<String> esperados = Set.of("ADMINISTRADOR", "RECEPCIONISTA",
                "ENTRENADOR", "NUTRICIONISTA", "CLIENTE");
        Set<String> actuales = new RolControlador().listar("").stream()
                .filter(Rol::isEstadoRol)
                .map(Rol::getNombreRol)
                .map(nombre -> nombre.toUpperCase(Locale.ROOT))
                .filter(esperados::contains)
                .collect(java.util.stream.Collectors.toSet());
        if (!actuales.equals(esperados)) {
            throw new AssertionError("Roles visibles incorrectos: " + actuales);
        }

        final PnlSeguridad[] panel = new PnlSeguridad[1];
        javax.swing.SwingUtilities.invokeAndWait(
                () -> panel[0] = new PnlSeguridad());
        Field campoSelector = PnlSeguridad.class
                .getDeclaredField("cboTipoContrato");
        campoSelector.setAccessible(true);
        @SuppressWarnings("unchecked")
        JComboBox<String> selector = (JComboBox<String>) campoSelector.get(panel[0]);
        for (int indice = 0; indice < selector.getItemCount(); indice++) {
            final int actual = indice;
            javax.swing.SwingUtilities.invokeAndWait(
                    () -> selector.setSelectedIndex(actual));
            if (selector.getSelectedIndex() != indice) {
                throw new AssertionError(
                        "El submodulo se regreso solo al indice 0");
            }
        }

        AutorizacionControlador autorizacion = new AutorizacionControlador();
        Usuario falso = new Usuario();
        falso.setIdUsuario(999999L);
        falso.setIdRol(admin.getIdRol());
        falso.setNombreRol("ROL_DESCONOCIDO");
        falso.setEstadoUsuario(true);
        SesionUsuario.iniciarSesion(falso);
        if (autorizacion.tienePermiso("SEGURIDAD", "ELIMINAR")) {
            throw new AssertionError("Un rol desconocido obtuvo privilegios Admin");
        }

        SesionUsuario.iniciarSesion(admin);
        if (!autorizacion.tienePermiso("ROLES", "VER")) {
            throw new AssertionError("El administrador real perdio sus permisos");
        }
        SesionUsuario.cerrarSesion();
        System.out.println("SEGURIDAD_INTEGRAL_OK=5_ROLES+5_SUBMODULOS+DENY_DEFAULT");
    }
}
