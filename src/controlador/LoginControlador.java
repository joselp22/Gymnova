/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.ClienteDAO;
import dao.EntrenadorDAO;
import dao.MembresiaDAO;
import dao.UsuarioDAO;
import modelo.Entrenador;
import modelo.Usuario;
import utilidades.Auditoria;
import utilidades.NavegacionRol;
import utilidades.SeguridadClave;
import utilidades.SesionUsuario;
import utilidades.VerificadorMembresias;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

/**
 *
 * @author Usuario
 */
public class LoginControlador {

    private final UsuarioDAO usuarioDAO;
    private final EntrenadorDAO entrenadorDAO;
    private final MembresiaDAO membresiaDAO;
    private final ClienteDAO clienteDAO;

    public LoginControlador() {

        usuarioDAO = new UsuarioDAO();
        entrenadorDAO = new EntrenadorDAO();
        membresiaDAO = new MembresiaDAO();
        clienteDAO = new ClienteDAO();
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

            validarPerfilProfesional(usuario);
            validarMembresiaCliente(usuario);

            usuarioDAO.registrarAccesoCorrecto(
                    usuario.getIdUsuario()
            );

            usuario.setIntentosFallidos(
                    0
            );

            SesionUsuario.iniciarSesion(
                    usuario
            );

            Auditoria.exito("SESION", "INICIO",
                    "Inicio de sesion de " + usuario.getNombreUsuario()
                    + " (rol " + usuario.getNombreRol() + ")");

            return usuario;

        } finally {

            Arrays.fill(
                    contrasena,
                    '\0'
            );
        }
    }

    private void validarPerfilProfesional(Usuario usuario)
            throws SQLException {

        String rol = NavegacionRol.normalizarRol(
                usuario.getNombreRol()
        );

        if (!"ENTRENADOR".equals(rol)) {
            return;
        }

        if (usuario.getIdPersona() == null) {
            throw new IllegalStateException(
                    "La cuenta Entrenador no tiene una persona vinculada. "
                    + "Solicite al Administrador corregir la cuenta."
            );
        }

        Entrenador entrenador = entrenadorDAO.buscar(
                usuario.getIdPersona()
        );

        if (entrenador == null) {
            throw new IllegalStateException(
                    "La cuenta tiene rol Entrenador, pero la persona vinculada "
                    + "no está registrada en Personal como Entrenador. "
                    + "El Administrador debe completar el perfil antes de iniciar sesión."
            );
        }

        if (!entrenador.isEstadoEntrenador()) {
            throw new IllegalStateException(
                    "El perfil de Entrenador está inactivo. "
                    + "Solicite al Administrador su reactivación."
            );
        }
    }

    /**
     * Filtro de acceso para el rol Cliente. Se ejecutan tres chequeos:
     *   1) Perfil de cliente activo (estado_cliente = TRUE). El admin
     *      puede desactivarlo manualmente y eso bloquea el login sin
     *      tocar la membresia.
     *   2) Membresias vencidas se re-marcan como VENCIDA (barrido de
     *      seguridad; el barrido principal corre al arrancar la app).
     *   3) Debe existir al menos una membresia en estado ACTIVA cuya
     *      fecha_fin todavia no expiro.
     */
    private void validarMembresiaCliente(Usuario usuario) throws SQLException {

        String rol = NavegacionRol.normalizarRol(
                usuario.getNombreRol()
        );

        if (!"CLIENTE".equals(rol)) {
            return;
        }

        if (usuario.getIdPersona() == null) {
            throw new IllegalStateException(
                    "La cuenta Cliente no tiene una persona vinculada. "
                    + "Solicite al Administrador corregir la cuenta."
            );
        }

        if (!clienteDAO.estaActivo(usuario.getIdPersona())) {
            throw new IllegalStateException(
                    "Tu perfil de cliente esta desactivado. "
                    + "Comunicate con Administracion para reactivarlo."
            );
        }

        VerificadorMembresias.ejecutar();

        if (!membresiaDAO.tieneMembresiaActiva(usuario.getIdPersona())) {
            // Cualquier caso en el que el cliente ya no tenga una
            // membresia ACTIVA vigente (vencida, cancelada, congelada,
            // pendiente de cobro o inexistente) se reporta como deuda
            // pendiente para que se acerque a Recepcion.
            throw new IllegalStateException(
                    "Tienes una deuda pendiente. "
                    + "Acude a Recepcion para regularizar tu membresia "
                    + "antes de iniciar sesion."
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

            Auditoria.registrarPorUsuario(usuario.getIdUsuario(),
                    "SESION", "BLOQUEO",
                    "Usuario " + usuario.getNombreUsuario()
                    + " bloqueado tras 3 intentos fallidos.",
                    "USUARIO_BLOQUEADO");

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
