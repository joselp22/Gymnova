import javax.swing.JPanel;
import modelo.Usuario;
import utilidades.SesionUsuario;
import vista.PnlAcceso;
import vista.PnlClientes;
import vista.PnlConfiguracion;
import vista.PnlFinanzas;
import vista.PnlInicio;
import vista.PnlInicioAdministrador;
import vista.PnlInicioCliente;
import vista.PnlInicioEntrenador;
import vista.PnlInicioNutricionista;
import vista.PnlInicioRecepcionista;
import vista.PnlMembresiasCobro;
import vista.PnlModuloAplicacion;
import vista.PnlNutricion;
import vista.PnlPersonal;
import vista.PnlPersonas;
import vista.PnlReportes;
import vista.PnlRutinas;
import vista.PnlSalud;
import vista.PnlSeguridad;

/** Prueba de humo, de solo lectura, para construir todas las vistas. */
public class PruebaConstruccionVistas {

    public static void main(String[] args) throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setIdRol(1);
        usuario.setNombreUsuario("PRUEBA");
        usuario.setNombreRol("Administrador");
        usuario.setEstadoUsuario(true);
        SesionUsuario.iniciarSesion(usuario);

        final JPanel[][] vistas = new JPanel[1][];
        javax.swing.SwingUtilities.invokeAndWait(() -> vistas[0] = new JPanel[]{
            new PnlInicio(), new PnlPersonas(), new PnlClientes(),
            new PnlPersonal(), new PnlSeguridad(), new PnlMembresiasCobro(),
            new PnlAcceso(), new PnlRutinas(), new PnlSalud(),
            new PnlNutricion(), new PnlFinanzas(),
            new PnlReportes(), new PnlConfiguracion()
        });

        System.out.println("VISTAS_CONSTRUIDAS=" + vistas[0].length);
        String[] roles = {"Administrador", "Recepcionista", "Entrenador",
            "Nutricionista", "Cliente"};
        usuario.setIdPersona(1L);
        usuario.setNombreRol(roles[0]);
        javax.swing.SwingUtilities.invokeAndWait(PnlInicioAdministrador::new);
        usuario.setNombreRol(roles[1]);
        javax.swing.SwingUtilities.invokeAndWait(PnlInicioRecepcionista::new);
        usuario.setNombreRol(roles[2]);
        javax.swing.SwingUtilities.invokeAndWait(PnlInicioEntrenador::new);
        usuario.setNombreRol(roles[3]);
        javax.swing.SwingUtilities.invokeAndWait(PnlInicioNutricionista::new);
        usuario.setNombreRol(roles[4]);
        javax.swing.SwingUtilities.invokeAndWait(PnlInicioCliente::new);
        System.out.println("DASHBOARDS_POR_ROL=" + roles.length);

        String[][] experiencias = {
            {"Cliente", "CLIENTES", "MEMBRESIAS", "ACCESO", "RUTINAS", "SALUD", "NUTRICION", "REPORTES"},
            {"Entrenador", "CLIENTES", "RUTINAS", "SALUD", "REPORTES"},
            {"Nutricionista", "CLIENTES", "NUTRICION", "SALUD", "REPORTES"},
            {"Recepcionista", "MEMBRESIAS", "ACCESO", "FINANZAS", "REPORTES"}
        };
        int construidas = 0;
        for (String[] experiencia : experiencias) {
            usuario.setNombreRol(experiencia[0]);
            for (int indice = 1; indice < experiencia.length; indice++) {
                String modulo = experiencia[indice];
                javax.swing.SwingUtilities.invokeAndWait(
                        () -> new PnlModuloAplicacion(modulo, () -> { })
                );
                construidas++;
            }
        }
        System.out.println("EXPERIENCIAS_POR_ROL=" + construidas);
        SesionUsuario.cerrarSesion();
    }
}
