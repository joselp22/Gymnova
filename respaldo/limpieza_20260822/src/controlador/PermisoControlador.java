/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.PermisoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Permiso;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class PermisoControlador {

    private final PermisoDAO permisoDAO;
    private String mensaje;

    public PermisoControlador() {

        permisoDAO = new PermisoDAO();
        mensaje = "";
    }

    public boolean registrar(
            Permiso permiso
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    permiso
            );

            validarPermiso(
                    permiso
            );

            boolean existeNombre
                    = permisoDAO.existeNombre(
                            permiso.getNombrePermiso()
                    );

            if (existeNombre) {

                mensaje
                        = "Ya existe un permiso "
                        + "con ese nombre.";

                return false;
            }

            boolean existeModuloAccion
                    = permisoDAO.existeModuloAccion(
                            permiso.getModulo(),
                            permiso.getAccion()
                    );

            if (existeModuloAccion) {

                mensaje
                        = "Ya existe un permiso "
                        + "para ese modulo y accion.";

                return false;
            }

            boolean guardado
                    = permisoDAO.guardar(
                            permiso
                    );

            if (guardado) {

                mensaje
                        = "Permiso registrado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo registrar "
                    + "el permiso.";

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
            Permiso permiso
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    permiso
            );

            validarPermiso(
                    permiso
            );

            if (permiso.getIdPermiso() == null) {

                mensaje
                        = "Seleccione un permiso "
                        + "para modificar.";

                return false;
            }

            Permiso permisoGuardado
                    = permisoDAO.buscar(
                            permiso.getIdPermiso()
                    );

            if (permisoGuardado == null) {

                mensaje
                        = "El permiso seleccionado "
                        + "ya no existe.";

                return false;
            }

            boolean nombreUsado
                    = permisoDAO.existeNombreEnOtroRegistro(
                            permiso.getIdPermiso(),
                            permiso.getNombrePermiso()
                    );

            if (nombreUsado) {

                mensaje
                        = "Ya existe otro permiso "
                        + "con ese nombre.";

                return false;
            }

            boolean moduloAccionUsado
                    = permisoDAO.existeModuloAccionEnOtroRegistro(
                            permiso.getIdPermiso(),
                            permiso.getModulo(),
                            permiso.getAccion()
                    );

            if (moduloAccionUsado) {

                mensaje
                        = "Ya existe otro permiso "
                        + "para ese modulo y accion.";

                return false;
            }

            boolean modificado
                    = permisoDAO.modificar(
                            permiso
                    );

            if (modificado) {

                mensaje
                        = "Permiso modificado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "el permiso.";

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
            Integer idPermiso
    ) {

        mensaje = "";

        if (idPermiso == null) {

            mensaje
                    = "Seleccione un permiso "
                    + "para desactivar.";

            return false;
        }

        try {

            Permiso permiso
                    = permisoDAO.buscar(
                            idPermiso
                    );

            if (permiso == null) {

                mensaje
                        = "El permiso seleccionado "
                        + "no existe.";

                return false;
            }

            if (!permiso.isEstadoPermiso()) {

                mensaje
                        = "El permiso ya se encuentra "
                        + "inactivo.";

                return false;
            }

            boolean desactivado
                    = permisoDAO.desactivar(
                            idPermiso
                    );

            if (desactivado) {

                mensaje
                        = "Permiso desactivado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desactivar "
                    + "el permiso.";

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
            Integer idPermiso
    ) throws SQLException {

        if (idPermiso == null) {

            throw new IllegalArgumentException(
                    "Seleccione un permiso para eliminar."
            );
        }

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede eliminar "
                    + "permisos definitivamente."
            );
        }

        try {

            boolean eliminado
                    = permisoDAO.eliminarDefinitivamente(
                            idPermiso
                    );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "El permiso ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque el permiso tiene roles "
                        + "relacionados. Puede desactivarlo.",
                        e
                );
            }

            throw e;
        }
    }

    public Permiso buscar(
            Integer idPermiso
    ) {

        mensaje = "";

        if (idPermiso == null) {
            return null;
        }

        try {

            return permisoDAO.buscar(
                    idPermiso
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "el permiso.";

            return null;
        }
    }

    public List<Permiso> listar(
            String criterio
    ) {

        mensaje = "";

        try {

            return permisoDAO.listar(
                    criterio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los permisos.";

            return new ArrayList<>();
        }
    }

    public List<Permiso> listarActivos() {

        mensaje = "";

        try {

            return permisoDAO.listarActivos();

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los permisos activos.";

            return new ArrayList<>();
        }
    }

    private void validarPermiso(
            Permiso permiso
    ) {

        if (permiso == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "del permiso."
            );
        }

        if (permiso.getNombrePermiso() == null
                || permiso.getNombrePermiso().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre del permiso "
                    + "es obligatorio."
            );
        }

        if (permiso.getNombrePermiso().length() < 3
                || permiso.getNombrePermiso().length() > 100) {

            throw new IllegalArgumentException(
                    "El nombre del permiso debe contener "
                    + "entre 3 y 100 caracteres."
            );
        }

        if (permiso.getModulo() == null
                || permiso.getModulo().isBlank()) {

            throw new IllegalArgumentException(
                    "El modulo del permiso "
                    + "es obligatorio."
            );
        }

        if (permiso.getModulo().length() < 3
                || permiso.getModulo().length() > 80) {

            throw new IllegalArgumentException(
                    "El modulo debe contener "
                    + "entre 3 y 80 caracteres."
            );
        }

        if (permiso.getAccion() == null
                || permiso.getAccion().isBlank()) {

            throw new IllegalArgumentException(
                    "La accion del permiso "
                    + "es obligatoria."
            );
        }

        if (permiso.getAccion().length() < 3
                || permiso.getAccion().length() > 80) {

            throw new IllegalArgumentException(
                    "La accion debe contener "
                    + "entre 3 y 80 caracteres."
            );
        }

        if (permiso.getDescripcion() != null
                && permiso.getDescripcion().length() > 500) {

            throw new IllegalArgumentException(
                    "La descripcion no debe superar "
                    + "los 500 caracteres."
            );
        }
    }

    private void normalizarDatos(
            Permiso permiso
    ) {

        if (permiso == null) {
            return;
        }

        if (permiso.getNombrePermiso() != null) {

            permiso.setNombrePermiso(
                    permiso
                            .getNombrePermiso()
                            .trim()
            );
        }

        if (permiso.getModulo() != null) {

            permiso.setModulo(
                    permiso
                            .getModulo()
                            .trim()
                            .toUpperCase()
            );
        }

        if (permiso.getAccion() != null) {

            permiso.setAccion(
                    permiso
                            .getAccion()
                            .trim()
                            .toUpperCase()
            );
        }

        if (permiso.getDescripcion() != null) {

            String descripcion
                    = permiso
                            .getDescripcion()
                            .trim();

            if (descripcion.isEmpty()) {

                descripcion = null;
            }

            permiso.setDescripcion(
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

            return "Ya existe un permiso "
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
