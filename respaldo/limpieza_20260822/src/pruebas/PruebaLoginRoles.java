package pruebas;

import controlador.LoginControlador;
import modelo.Usuario;

/** Comprueba autenticacion real de las cinco cuentas de demostracion. */
public class PruebaLoginRoles {
    public static void main(String[] args) throws Exception {
        String[] usuarios={"admin","RECEPCION","ENTRENADOR","NUTRICION","cliente"};
        String[] roles={"Administrador","Recepcionista","Entrenador","Nutricionista","Cliente"};
        for(int i=0;i<usuarios.length;i++){
            LoginControlador c=new LoginControlador();
            Usuario u=c.iniciarSesion(usuarios[i],"Gymnova2026*".toCharArray());
            if(u==null||!roles[i].equalsIgnoreCase(u.getNombreRol()))
                throw new IllegalStateException("Fallo de autenticacion: "+usuarios[i]);
            System.out.println("LOGIN_OK="+usuarios[i]+"|"+u.getNombreRol());
        }
    }
}
