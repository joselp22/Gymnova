package pruebas;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import modelo.Usuario;
import utilidades.SesionUsuario;
import vista.PnlAcceso;
import vista.PnlClientes;
import vista.PnlConfiguracion;
import vista.PnlFinanzas;
import vista.PnlMembresiasCobro;
import vista.PnlModuloAplicacion;
import vista.PnlNutricion;
import vista.PnlPersonal;
import vista.PnlPersonas;
import vista.PnlReportes;
import vista.PnlRutinas;
import vista.PnlSalud;
import vista.PnlSeguridad;

/** Detecta botones visibles que aparentan funcionar pero no tienen acción. */
public final class PruebaControlesVisibles {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        List<String> errores = new ArrayList<>();
        final int[] total = {0};

        Usuario administrador = usuario("Administrador", 1L);
        SesionUsuario.iniciarSesion(administrador);
        JPanel[] gestion = {
            new PnlPersonas(), new PnlClientes(), new PnlPersonal(),
            new PnlSeguridad(), new PnlMembresiasCobro(), new PnlAcceso(),
            new PnlRutinas(), new PnlSalud(), new PnlNutricion(),
            new PnlFinanzas(), new PnlReportes(),
            new PnlConfiguracion()
        };
        for (JPanel panel : gestion) {
            revisar(panel, panel.getClass().getSimpleName(), errores, total);
        }

        Object[][] roles = {
            {"Administrador", 1L}, {"Recepcionista", 1L},
            {"Entrenador", 4L}, {"Nutricionista", 6L},
            {"Cliente", 2L}
        };
        String[] modulos = {
            "CLIENTES", "MEMBRESIAS", "ACCESO", "RUTINAS", "SALUD",
            "NUTRICION", "FINANZAS", "REPORTES", "CONFIGURACION"
        };
        // INVENTARIO fue retirado del alcance del sistema.
        for (Object[] dato : roles) {
            SesionUsuario.iniciarSesion(usuario((String) dato[0], (Long) dato[1]));
            for (String modulo : modulos) {
                PnlModuloAplicacion panel = new PnlModuloAplicacion(modulo, null);
                revisar(panel, dato[0] + "|" + modulo, errores, total);
            }
        }
        SesionUsuario.cerrarSesion();

        if (!errores.isEmpty()) {
            throw new IllegalStateException(
                    "Botones visibles sin accion: " + String.join(", ", errores)
            );
        }
        System.out.println("CONTROLES_VISIBLES_OK=" + total[0]);
        System.exit(0);
    }

    private static Usuario usuario(String rol, Long idPersona) {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario("PRUEBA_" + rol.toUpperCase());
        usuario.setNombreRol(rol);
        usuario.setIdPersona(idPersona);
        usuario.setEstadoUsuario(true);
        return usuario;
    }

    private static void revisar(
            Container contenedor,
            String ruta,
            List<String> errores,
            int[] total
    ) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JButton boton && boton.isVisible()) {
                total[0]++;
                if (boton.isEnabled()
                        && boton.getText() != null
                        && !boton.getText().isBlank()
                        && boton.getActionListeners().length == 0
                        && boton.getAction() == null) {
                    errores.add(ruta + ":" + boton.getText());
                }
            }
            if (componente instanceof Container hijo) {
                revisar(hijo, ruta, errores, total);
            }
        }
    }

    private PruebaControlesVisibles() {
    }
}
