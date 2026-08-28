package pruebas;

import javax.swing.JPanel;
import modelo.Usuario;
import utilidades.SesionUsuario;
import vista.PnlModuloAplicacion;

/** Construye cada experiencia de rol sin abrir ventanas ni modificar datos. */
public class PruebaVistasRoles {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless","true");
        Object[][] roles={{"Administrador",null},{"Recepcionista",1L},{"Entrenador",4L},{"Nutricionista",6L},{"Cliente",2L}};
        String[] modulos={"CLIENTES","MEMBRESIAS","ACCESO","RUTINAS","SALUD","NUTRICION","FINANZAS","REPORTES","CONFIGURACION"};
        int total=0;
        for(Object[] dato:roles){
            Usuario u=new Usuario();u.setNombreUsuario("PRUEBA_"+dato[0]);u.setNombreRol((String)dato[0]);u.setIdPersona((Long)dato[1]);u.setEstadoUsuario(true);
            SesionUsuario.iniciarSesion(u);
            for(String modulo:modulos){
                final JPanel[] vista=new JPanel[1];
                javax.swing.SwingUtilities.invokeAndWait(()->vista[0]=new PnlModuloAplicacion(modulo,null));
                if(vista[0].getComponentCount()==0) throw new IllegalStateException(dato[0]+" / "+modulo+" vacio");
                total++;
            }
        }
        SesionUsuario.cerrarSesion();
        System.out.println("VISTAS_ROL_CONSTRUIDAS_OK="+total);
        System.exit(0);
    }
}
