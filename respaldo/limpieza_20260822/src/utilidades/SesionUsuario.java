/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilidades;

import modelo.Usuario;
/**
 *
 * @author Usuario
 */

public final class SesionUsuario {

    private static Usuario usuarioActual;

    private SesionUsuario() {
        // Evita crear objetos de esta clase.
    }

    public static void iniciarSesion(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "No se puede iniciar una sesión sin usuario."
            );
        }

        usuarioActual = usuario;
    }

    public static Usuario getUsuarioActual() {

        if (usuarioActual == null) {
            throw new IllegalStateException(
                    "No existe una sesión activa."
            );
        }

        return usuarioActual;
    }

    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    public static void cerrarSesion() {
        usuarioActual = null;
    }
}
