/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.PersonaDAO;
import dao.RolDAO;
import dao.UsuarioDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import modelo.Persona;
import modelo.Rol;
import modelo.Usuario;
import utilidades.SeguridadClave;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class UsuarioControlador {

    private final UsuarioDAO usuarioDAO;
    private final RolDAO rolDAO;
    private final PersonaDAO personaDAO;
    private String mensaje;

    public UsuarioControlador() {

        usuarioDAO = new UsuarioDAO();
        rolDAO = new RolDAO();
        personaDAO = new PersonaDAO();
        mensaje = "";
    }

    public boolean registrar(
            Usuario usuario,
            char[] contrasena
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    usuario
            );

            validarUsuarioParaRegistro(
                    usuario
            );

            validarContrasena(
                    contrasena
            );

            validarRolExistente(
                    usuario.getIdRol()
            );

            validarPersonaExistente(
                    usuario.getIdPersona()
            );

            boolean existeNombre
                    = usuarioDAO.existeNombre(
                            usuario.getNombreUsuario()
                    );

            if (existeNombre) {

                mensaje
                        = "Ya existe un usuario "
                        + "con ese nombre.";

                return false;
            }

            if (usuario.getIdPersona() != null
                    && usuarioDAO.personaTieneUsuario(
                            usuario.getIdPersona()
                    )) {

                mensaje
                        = "La persona seleccionada ya tiene "
                        + "un usuario asignado.";

                return false;
            }

            usuario.setClaveHash(
                    SeguridadClave.generarHash(
                            contrasena
                    )
            );

            usuario.setBloqueado(false);
            usuario.setIntentosFallidos(0);
            usuario.setEstadoUsuario(true);

            boolean guardado
                    = usuarioDAO.guardar(
                            usuario
                    );

            if (guardado) {

                mensaje
                        = "Usuario registrado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo registrar "
                    + "el usuario.";

            return false;

        } catch (IllegalArgumentException
                | IllegalStateException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;

        } finally {

            limpiarContrasena(
                    contrasena
            );
        }
    }

    public boolean modificar(
            Usuario usuario
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    usuario
            );

            validarUsuarioParaModificacion(
                    usuario
            );

            Usuario usuarioGuardado
                    = usuarioDAO.buscar(
                            usuario.getIdUsuario()
                    );

            if (usuarioGuardado == null) {

                mensaje
                        = "El usuario seleccionado "
                        + "ya no existe.";

                return false;
            }

            validarRolExistente(
                    usuario.getIdRol()
            );

            validarPersonaExistente(
                    usuario.getIdPersona()
            );

            boolean nombreUsado
                    = usuarioDAO.existeNombreEnOtroRegistro(
                            usuario.getIdUsuario(),
                            usuario.getNombreUsuario()
                    );

            if (nombreUsado) {

                mensaje
                        = "Ya existe otro usuario "
                        + "con ese nombre.";

                return false;
            }

            if (usuario.getIdPersona() != null
                    && usuarioDAO.personaTieneUsuarioEnOtroRegistro(
                            usuario.getIdUsuario(),
                            usuario.getIdPersona()
                    )) {

                mensaje
                        = "La persona seleccionada ya tiene "
                        + "otro usuario asignado.";

                return false;
            }

            usuario.setClaveHash(
                    usuarioGuardado.getClaveHash()
            );

            boolean modificado
                    = usuarioDAO.modificar(
                            usuario
                    );

            if (modificado) {

                mensaje
                        = "Usuario modificado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "el usuario.";

            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    public boolean cambiarClave(
            Long idUsuario,
            char[] nuevaContrasena
    ) {

        mensaje = "";

        try {

            if (idUsuario == null) {

                mensaje
                        = "Seleccione un usuario.";

                return false;
            }

            validarContrasena(
                    nuevaContrasena
            );

            Usuario usuario
                    = usuarioDAO.buscar(
                            idUsuario
                    );

            if (usuario == null) {

                mensaje
                        = "El usuario seleccionado "
                        + "no existe.";

                return false;
            }

            String hash
                    = SeguridadClave.generarHash(
                            nuevaContrasena
                    );

            boolean actualizado
                    = usuarioDAO.actualizarClave(
                            idUsuario,
                            hash
                    );

            if (actualizado) {

                mensaje
                        = "Clave actualizada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo actualizar "
                    + "la clave.";

            return false;

        } catch (IllegalArgumentException
                | IllegalStateException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;

        } finally {

            limpiarContrasena(
                    nuevaContrasena
            );
        }
    }

    public boolean desactivar(
            Long idUsuario
    ) {

        mensaje = "";

        if (idUsuario == null) {

            mensaje
                    = "Seleccione un usuario.";

            return false;
        }

        if (esUsuarioActual(
                idUsuario
        )) {

            mensaje
                    = "No puede desactivar "
                    + "su propio usuario.";

            return false;
        }

        try {

            Usuario usuario
                    = usuarioDAO.buscar(
                            idUsuario
                    );

            if (usuario == null) {

                mensaje
                        = "El usuario seleccionado "
                        + "no existe.";

                return false;
            }

            if (!usuario.isEstadoUsuario()) {

                mensaje
                        = "El usuario ya se encuentra "
                        + "inactivo.";

                return false;
            }

            boolean desactivado
                    = usuarioDAO.desactivar(
                            idUsuario
                    );

            if (desactivado) {

                mensaje
                        = "Usuario desactivado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desactivar "
                    + "el usuario.";

            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    public boolean activar(
            Long idUsuario
    ) {

        mensaje = "";

        if (idUsuario == null) {

            mensaje
                    = "Seleccione un usuario.";

            return false;
        }

        try {

            boolean activado
                    = usuarioDAO.activar(
                            idUsuario
                    );

            if (activado) {

                mensaje
                        = "Usuario activado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo activar "
                    + "el usuario.";

            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    public boolean bloquear(
            Long idUsuario
    ) {

        mensaje = "";

        if (idUsuario == null) {

            mensaje
                    = "Seleccione un usuario.";

            return false;
        }

        if (esUsuarioActual(
                idUsuario
        )) {

            mensaje
                    = "No puede bloquear "
                    + "su propio usuario.";

            return false;
        }

        try {

            boolean bloqueado
                    = usuarioDAO.bloquear(
                            idUsuario
                    );

            if (bloqueado) {

                mensaje
                        = "Usuario bloqueado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo bloquear "
                    + "el usuario.";

            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    public boolean desbloquear(
            Long idUsuario
    ) {

        mensaje = "";

        if (idUsuario == null) {

            mensaje
                    = "Seleccione un usuario.";

            return false;
        }

        try {

            boolean desbloqueado
                    = usuarioDAO.desbloquear(
                            idUsuario
                    );

            if (desbloqueado) {

                mensaje
                        = "Usuario desbloqueado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desbloquear "
                    + "el usuario.";

            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    public void eliminarDefinitivamente(
            Long idUsuario
    ) throws SQLException {

        if (idUsuario == null) {

            throw new IllegalArgumentException(
                    "Seleccione un usuario para eliminar."
            );
        }

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede eliminar "
                    + "usuarios definitivamente."
            );
        }

        if (esUsuarioActual(
                idUsuario
        )) {

            throw new SecurityException(
                    "No puede eliminar su propio usuario."
            );
        }

        try {

            boolean eliminado
                    = usuarioDAO.eliminarDefinitivamente(
                            idUsuario
                    );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "El usuario ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque el usuario tiene registros "
                        + "historicos relacionados. "
                        + "Puede desactivarlo.",
                        e
                );
            }

            throw e;
        }
    }

    public Usuario buscar(
            Long idUsuario
    ) {

        mensaje = "";

        if (idUsuario == null) {
            return null;
        }

        try {

            return usuarioDAO.buscar(
                    idUsuario
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "el usuario.";

            return null;
        }
    }

    public Optional<Usuario> buscarPorNombre(
            String nombreUsuario
    ) {

        mensaje = "";

        try {

            return usuarioDAO.buscarPorNombre(
                    nombreUsuario
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "el usuario.";

            return Optional.empty();
        }
    }

    public List<Usuario> listar(
            String criterio
    ) {

        mensaje = "";

        try {

            return usuarioDAO.listar(
                    criterio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los usuarios.";

            return new ArrayList<>();
        }
    }

    private void validarUsuarioParaRegistro(
            Usuario usuario
    ) {

        validarUsuarioComun(
                usuario
        );
    }

    private void validarUsuarioParaModificacion(
            Usuario usuario
    ) {

        validarUsuarioComun(
                usuario
        );

        if (usuario.getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "Seleccione un usuario para modificar."
            );
        }

        if (usuario.getIntentosFallidos() < 0) {

            throw new IllegalArgumentException(
                    "Los intentos fallidos no pueden ser negativos."
            );
        }
    }

    private void validarUsuarioComun(
            Usuario usuario
    ) {

        if (usuario == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos del usuario."
            );
        }

        if (usuario.getIdRol() == null) {

            throw new IllegalArgumentException(
                    "Seleccione un rol."
            );
        }

        if (usuario.getNombreUsuario() == null
                || usuario.getNombreUsuario().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio."
            );
        }

        if (!usuario.getNombreUsuario()
                .matches("^[A-Za-z0-9._-]{4,40}$")) {

            throw new IllegalArgumentException(
                    "El nombre de usuario debe tener entre 4 y 40 "
                    + "caracteres y solo puede usar letras, numeros, "
                    + "punto, guion o guion bajo."
            );
        }
    }

    private void validarContrasena(
            char[] contrasena
    ) {

        if (contrasena == null
                || contrasena.length == 0) {

            throw new IllegalArgumentException(
                    "Ingrese la clave del usuario."
            );
        }

        if (contrasena.length < 8
                || contrasena.length > 64) {

            throw new IllegalArgumentException(
                    "La clave debe tener entre 8 y 64 caracteres."
            );
        }

        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneNumero = false;
        boolean tieneEspecial = false;

        for (char caracter : contrasena) {

            if (Character.isUpperCase(
                    caracter
            )) {

                tieneMayuscula = true;

            } else if (Character.isLowerCase(
                    caracter
            )) {

                tieneMinuscula = true;

            } else if (Character.isDigit(
                    caracter
            )) {

                tieneNumero = true;

            } else {

                tieneEspecial = true;
            }
        }

        if (!tieneMayuscula
                || !tieneMinuscula
                || !tieneNumero
                || !tieneEspecial) {

            throw new IllegalArgumentException(
                    "La clave debe incluir mayusculas, minusculas, "
                    + "numeros y un caracter especial."
            );
        }
    }

    private void validarRolExistente(
            Integer idRol
    ) throws SQLException {

        Rol rol
                = rolDAO.buscar(
                        idRol
                );

        if (rol == null) {

            throw new IllegalArgumentException(
                    "El rol seleccionado no existe."
            );
        }

        if (!rol.isEstadoRol()) {

            throw new IllegalArgumentException(
                    "El rol seleccionado se encuentra inactivo."
            );
        }
    }

    private void validarPersonaExistente(
            Long idPersona
    ) throws SQLException {

        if (idPersona == null) {
            return;
        }

        Optional<Persona> persona
                = personaDAO.buscarPorId(
                        idPersona
                );

        if (persona.isEmpty()) {

            throw new IllegalArgumentException(
                    "La persona seleccionada no existe."
            );
        }
    }

    private void normalizarDatos(
            Usuario usuario
    ) {

        if (usuario == null) {
            return;
        }

        if (usuario.getNombreUsuario() != null) {

            usuario.setNombreUsuario(
                    usuario.getNombreUsuario()
                            .trim()
                            .toLowerCase()
            );
        }
    }

    private void limpiarContrasena(
            char[] contrasena
    ) {

        if (contrasena != null) {

            Arrays.fill(
                    contrasena,
                    '\0'
            );
        }
    }

    private boolean esAdministrador() {

        return SesionUsuario.haySesionActiva()
                && "Administrador".equalsIgnoreCase(
                        SesionUsuario
                                .getUsuarioActual()
                                .getNombreRol()
                );
    }

    private boolean esUsuarioActual(
            Long idUsuario
    ) {

        return idUsuario != null
                && SesionUsuario.haySesionActiva()
                && SesionUsuario
                        .getUsuarioActual()
                        .getIdUsuario()
                        .equals(
                                idUsuario
                        );
    }

    private String traducirErrorBaseDatos(
            SQLException e
    ) {

        if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

            return "No se puede realizar la operacion "
                    + "porque existen registros relacionados.";
        }

        if ("23505".equals(e.getSQLState())) {

            return "Ya existe un usuario "
                    + "con esos datos.";
        }

        if ("23514".equals(e.getSQLState())) {

            return "Los datos no cumplen las reglas "
                    + "de validacion de la base de datos.";
        }

        return "Error de base de datos: "
                + e.getMessage();
    }

    public String getMensaje() {
        return mensaje;
    }
}
