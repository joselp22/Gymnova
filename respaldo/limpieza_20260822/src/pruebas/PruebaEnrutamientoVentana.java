package pruebas;

import controlador.LoginControlador;
import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;
import modelo.Usuario;
import utilidades.SesionUsuario;
import vista.FrmPrincipal;

/** Prueba integrada de la vista central que abre cada opcion administrativa. */
public final class PruebaEnrutamientoVentana {

    private PruebaEnrutamientoVentana() {
    }

    public static void main(String[] args) throws Exception {
        LoginControlador login = new LoginControlador();
        Usuario usuario = login.iniciarSesion(
                "admin", "Gymnova2026*".toCharArray()
        );
        if (usuario == null || !"Administrador".equalsIgnoreCase(usuario.getNombreRol())) {
            throw new AssertionError("No se pudo iniciar la sesion administrativa");
        }
        SesionUsuario.iniciarSesion(usuario);

        final FrmPrincipal[] ventana = new FrmPrincipal[1];
        javax.swing.SwingUtilities.invokeAndWait(
                () -> ventana[0] = new FrmPrincipal()
        );

        Map<String, String> rutas = new LinkedHashMap<>();
        rutas.put("mostrarPersonas", "PnlPersonas");
        rutas.put("mostrarClientes", "PnlClientes");
        rutas.put("mostrarPersonal", "PnlPersonal");
        rutas.put("mostrarSeguridad", "PnlSeguridad");
        rutas.put("mostrarMembresias", "PnlMembresias");
        rutas.put("mostrarAcceso", "PnlAcceso");
        rutas.put("mostrarRutinas", "PnlRutinas");
        rutas.put("mostrarSalud", "PnlSalud");
        rutas.put("mostrarNutricion", "PnlNutricion");
        rutas.put("mostrarFinanzas", "PnlFinanzas");
        rutas.put("mostrarInventario", "PnlInventario");
        rutas.put("mostrarReportes", "PnlReportes");
        rutas.put("mostrarConfiguracion", "PnlConfiguracion");

        Field campoContenido = FrmPrincipal.class.getDeclaredField("pnlContenido");
        campoContenido.setAccessible(true);
        JPanel contenido = (JPanel) campoContenido.get(ventana[0]);

        for (Map.Entry<String, String> ruta : rutas.entrySet()) {
            Method metodo = FrmPrincipal.class.getDeclaredMethod(ruta.getKey());
            metodo.setAccessible(true);
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                try {
                    metodo.invoke(ventana[0]);
                } catch (ReflectiveOperationException ex) {
                    throw new RuntimeException(ex);
                }
            });
            Component panel = contenido.getComponentCount() == 0
                    ? null : contenido.getComponent(0);
            String actual = panel == null ? "VACIO" : panel.getClass().getSimpleName();
            if (!ruta.getValue().equals(actual)) {
                throw new AssertionError(ruta.getKey() + " esperaba "
                        + ruta.getValue() + " pero abrio " + actual);
            }
            System.out.println("RUTA_ADMIN_OK=" + ruta.getKey() + "|" + actual);
        }

        javax.swing.SwingUtilities.invokeAndWait(ventana[0]::dispose);
        SesionUsuario.cerrarSesion();
        System.out.println("ENRUTAMIENTO_ADMIN_OK=" + rutas.size());
        System.exit(0);
    }
}
