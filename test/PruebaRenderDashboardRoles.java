import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import modelo.Usuario;
import utilidades.SesionUsuario;
import vista.PnlInicioAdministrador;
import vista.PnlInicioCliente;
import vista.PnlInicioEntrenador;
import vista.PnlInicioNutricionista;
import vista.PnlInicioRecepcionista;
import vista.PnlModuloAplicacion;

/** Renderiza los cinco formularios para la revision visual de la entrega. */
public class PruebaRenderDashboardRoles {

    public static void main(String[] args) throws Exception {
        File salida = new File(args.length == 0 ? "capturas_roles" : args[0]);
        salida.mkdirs();
        renderizar(salida, "administrador", "Administrador", 1L,
                PnlInicioAdministrador::new);
        renderizar(salida, "recepcionista", "Recepcionista", 1L,
                PnlInicioRecepcionista::new);
        renderizar(salida, "entrenador", "Entrenador", 4L,
                PnlInicioEntrenador::new);
        renderizar(salida, "nutricionista", "Nutricionista", 6L,
                PnlInicioNutricionista::new);
        renderizar(salida, "cliente", "Cliente", 2L,
                PnlInicioCliente::new);
        renderizar(salida, "cliente_mi_rutina", "Cliente", 2L,
                () -> new PnlModuloAplicacion("RUTINAS", () -> { }));
        renderizar(salida, "entrenador_planificador", "Entrenador", 4L,
                () -> new PnlModuloAplicacion("RUTINAS", () -> { }));
        renderizar(salida, "nutricionista_seguimiento", "Nutricionista", 6L,
                () -> new PnlModuloAplicacion("NUTRICION", () -> { }));
        renderizar(salida, "cliente_progreso", "Cliente", 2L,
                () -> new PnlModuloAplicacion("SALUD", () -> { }));
        renderizar(salida, "cliente_reservas", "Cliente", 2L,
                () -> new PnlModuloAplicacion("ACCESO", () -> { }));
        renderizar(salida, "cliente_membresia", "Cliente", 2L,
                () -> new PnlModuloAplicacion("MEMBRESIAS", () -> { }));
        renderizar(salida, "entrenador_clientes", "Entrenador", 4L,
                () -> new PnlModuloAplicacion("CLIENTES", () -> { }));
        renderizar(salida, "recepcion_finanzas", "Recepcionista", 3L,
                () -> new PnlModuloAplicacion("FINANZAS", () -> { }));
        renderizar(salida, "cliente_reportes", "Cliente", 2L,
                () -> new PnlModuloAplicacion("REPORTES", () -> { }));
        renderizar(salida, "cliente_configuracion", "Cliente", 2L,
                () -> new PnlModuloAplicacion("CONFIGURACION", () -> { }));
        SesionUsuario.cerrarSesion();
        System.out.println("CAPTURAS_ROLES_Y_MODULOS=15");
    }

    private static void renderizar(
            File salida,
            String archivo,
            String rol,
            Long idPersona,
            CreadorPanel creador
    ) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setIdPersona(idPersona);
        usuario.setIdRol(1);
        usuario.setNombreUsuario(rol.toUpperCase());
        usuario.setNombreRol(rol);
        usuario.setEstadoUsuario(true);
        SesionUsuario.iniciarSesion(usuario);

        JPanel[] referencia = new JPanel[1];
        javax.swing.SwingUtilities.invokeAndWait(() -> {
            JPanel panel = creador.crear();
            panel.setSize(1280, 720);
            distribuir(panel);
            referencia[0] = panel;
        });

        BufferedImage imagen = new BufferedImage(
                1280, 720, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graficos = imagen.createGraphics();
        referencia[0].printAll(graficos);
        graficos.dispose();
        ImageIO.write(imagen, "png", new File(salida, archivo + ".png"));
    }

    private static void distribuir(java.awt.Container contenedor) {
        contenedor.doLayout();
        for (java.awt.Component componente : contenedor.getComponents()) {
            if (componente instanceof java.awt.Container hijo) {
                distribuir(hijo);
            }
        }
    }

    private interface CreadorPanel {
        JPanel crear();
    }
}
