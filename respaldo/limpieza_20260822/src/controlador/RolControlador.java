/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.RolDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Rol;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class RolControlador {

    private final RolDAO rolDAO;
    private String mensaje;

    public RolControlador() {

        rolDAO = new RolDAO();
        mensaje = "";
    }

    public boolean registrar(
            Rol rol
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    rol
            );

            validarRol(
                    rol
            );

            boolean existeNombre
                    = rolDAO.existeNombre(
                            rol.getNombreRol()
                    );

            if (existeNombre) {

                mensaje
                        = "Ya existe un rol "
                        + "con ese nombre.";

                return false;
            }

            boolean guardado
                    = rolDAO.guardar(
                            rol
                    );

            if (guardado) {

                mensaje
                        = "Rol registrado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo registrar "
                    + "el rol.";

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

    public boolean modificar(
            Rol rol
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    rol
            );

            validarRol(
                    rol
            );

            if (rol.getIdRol() == null) {

                mensaje
                        = "Seleccione un rol "
                        + "para modificar.";

                return false;
            }

            Rol rolGuardado
                    = rolDAO.buscar(
                            rol.getIdRol()
                    );

            if (rolGuardado == null) {

                mensaje
                        = "El rol seleccionado "
                        + "ya no existe.";

                return false;
            }

            boolean nombreUsado
                    = rolDAO.existeNombreEnOtroRegistro(
                            rol.getIdRol(),
                            rol.getNombreRol()
                    );

            if (nombreUsado) {

                mensaje
                        = "Ya existe otro rol "
                        + "con ese nombre.";

                return false;
            }

            boolean modificado
                    = rolDAO.modificar(
                            rol
                    );

            if (modificado) {

                mensaje
                        = "Rol modificado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "el rol.";

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

    public boolean desactivar(
            Integer idRol
    ) {

        mensaje = "";

        if (idRol == null) {

            mensaje
                    = "Seleccione un rol "
                    + "para desactivar.";

            return false;
        }

        try {

            Rol rol
                    = rolDAO.buscar(
                            idRol
                    );

            if (rol == null) {

                mensaje
                        = "El rol seleccionado "
                        + "no existe.";

                return false;
            }

            if (!rol.isEstadoRol()) {

                mensaje
                        = "El rol ya se encuentra "
                        + "inactivo.";

                return false;
            }

            boolean desactivado
                    = rolDAO.desactivar(
                            idRol
                    );

            if (desactivado) {

                mensaje
                        = "Rol desactivado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desactivar "
                    + "el rol.";

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
            Integer idRol
    ) throws SQLException {

        if (idRol == null) {

            throw new IllegalArgumentException(
                    "Seleccione un rol para eliminar."
            );
        }

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede eliminar "
                    + "roles definitivamente."
            );
        }

        try {

            boolean eliminado
                    = rolDAO.eliminarDefinitivamente(
                            idRol
                    );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "El rol ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque el rol tiene usuarios "
                        + "o permisos relacionados. "
                        + "Puede desactivarlo.",
                        e
                );
            }

            throw e;
        }
    }

    public Rol buscar(
            Integer idRol
    ) {

        mensaje = "";

        if (idRol == null) {
            return null;
        }

        try {

            return rolDAO.buscar(
                    idRol
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "el rol.";

            return null;
        }
    }

    public List<Rol> listar(
            String criterio
    ) {

        mensaje = "";

        try {

            return rolDAO.listar(
                    criterio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los roles.";

            return new ArrayList<>();
        }
    }

    public List<Rol> listarActivos() {

        mensaje = "";

        try {

            return rolDAO.listarActivos();

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los roles activos.";

            return new ArrayList<>();
        }
    }

    private void validarRol(
            Rol rol
    ) {

        if (rol == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "del rol."
            );
        }

        if (rol.getNombreRol() == null
                || rol.getNombreRol().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre del rol "
                    + "es obligatorio."
            );
        }

        if (rol.getNombreRol().length() < 3
                || rol.getNombreRol().length() > 50) {

            throw new IllegalArgumentException(
                    "El nombre del rol debe contener "
                    + "entre 3 y 50 caracteres."
            );
        }

        if (rol.getDescripcion() != null
                && rol.getDescripcion().length() > 255) {

            throw new IllegalArgumentException(
                    "La descripcion no debe superar "
                    + "los 255 caracteres."
            );
        }
    }

    private void normalizarDatos(
            Rol rol
    ) {

        if (rol == null) {
            return;
        }

        if (rol.getNombreRol() != null) {

            rol.setNombreRol(
                    rol.getNombreRol()
                            .trim()
            );
        }

        if (rol.getDescripcion() != null) {

            String descripcion
                    = rol.getDescripcion()
                            .trim();

            if (descripcion.isEmpty()) {

                descripcion = null;
            }

            rol.setDescripcion(
                    descripcion
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

    private String traducirErrorBaseDatos(
            SQLException e
    ) {

        if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

            return "No se puede realizar la operacion "
                    + "porque existen registros relacionados.";
        }

        if ("23505".equals(e.getSQLState())) {

            return "Ya existe un rol "
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
