/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.UsuarioDAO;
import modelo.Usuario;
import utilidades.SeguridadClave;
import utilidades.SesionUsuario;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

/**
 *
 * @author Usuario
 */
public class LoginControlador {

    private final UsuarioDAO usuarioDAO;

    public LoginControlador() {

        usuarioDAO = new UsuarioDAO();
    }

    public Usuario iniciarSesion(
            String nombreUsuario,
            char[] contrasena
    ) throws SQLException {

        validarCampos(
                nombreUsuario,
                contrasena
        );

        try {

            Optional<Usuario> resultado
                    = usuarioDAO.buscarPorNombre(
                            nombreUsuario
                    );

            if (resultado.isEmpty()) {

                throw new IllegalArgumentException(
                        "El usuario o la contraseña son incorrectos."
                );
            }

            Usuario usuario
                    = resultado.get();

            if (!usuario.isEstadoUsuario()) {

                throw new IllegalStateException(
                        "El usuario o su rol se encuentran inactivos."
                );
            }

            if (usuario.isBloqueado()) {

                throw new IllegalStateException(
                        "El usuario esta bloqueado. "
                        + "Solicite ayuda al administrador."
                );
            }

            boolean contrasenaCorrecta
                    = SeguridadClave.verificar(
                            contrasena,
                            usuario.getClaveHash()
                    );

            if (!contrasenaCorrecta) {

                procesarContrasenaIncorrecta(
                        usuario
                );
            }

            usuarioDAO.registrarAccesoCorrecto(
                    usuario.getIdUsuario()
            );

            usuario.setIntentosFallidos(
                    0
            );

            SesionUsuario.iniciarSesion(
                    usuario
            );

            return usuario;

        } finally {

            Arrays.fill(
                    contrasena,
                    '\0'
            );
        }
    }

    private void validarCampos(
            String nombreUsuario,
            char[] contrasena
    ) {

        if (nombreUsuario == null
                || nombreUsuario.isBlank()) {

            throw new IllegalArgumentException(
                    "Ingrese el nombre de usuario."
            );
        }

        if (contrasena == null
                || contrasena.length == 0) {

            throw new IllegalArgumentException(
                    "Ingrese la contraseña."
            );
        }
    }

    private void procesarContrasenaIncorrecta(
            Usuario usuario
    ) throws SQLException {

        usuarioDAO.registrarIntentoFallido(
                usuario.getIdUsuario()
        );

        int intentosRealizados
                = usuario.getIntentosFallidos()
                + 1;

        int intentosRestantes
                = 3 - intentosRealizados;

        if (intentosRestantes <= 0) {

            throw new IllegalStateException(
                    "La contraseña es incorrecta. "
                    + "El usuario ha sido bloqueado."
            );
        }

        throw new IllegalArgumentException(
                "El usuario o la contraseña son incorrectos. "
                + "Intentos restantes: "
                + intentosRestantes
        );
    }
}
