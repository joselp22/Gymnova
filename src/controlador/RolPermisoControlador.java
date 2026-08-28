/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.PermisoDAO;
import dao.RolDAO;
import dao.RolPermisoDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.Permiso;
import modelo.Rol;
import modelo.RolPermiso;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class RolPermisoControlador {

    private final RolPermisoDAO rolPermisoDAO;
    private final RolDAO rolDAO;
    private final PermisoDAO permisoDAO;
    private String mensaje;

    public RolPermisoControlador() {

        rolPermisoDAO = new RolPermisoDAO();
        rolDAO = new RolDAO();
        permisoDAO = new PermisoDAO();
        mensaje = "";
    }

    public boolean registrar(
            RolPermiso rolPermiso
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    rolPermiso
            );

            validarRolPermiso(
                    rolPermiso
            );

            Rol rol
                    = rolDAO.buscar(
                            rolPermiso.getIdRol()
                    );

            if (rol == null) {

                mensaje
                        = "El rol seleccionado "
                        + "no existe.";

                return false;
            }

            if (!rol.isEstadoRol()) {

                mensaje
                        = "El rol seleccionado "
                        + "se encuentra inactivo.";

                return false;
            }

            Permiso permiso
                    = permisoDAO.buscar(
                            rolPermiso.getIdPermiso()
                    );

            if (permiso == null) {

                mensaje
                        = "El permiso seleccionado "
                        + "no existe.";

                return false;
            }

            if (!permiso.isEstadoPermiso()) {

                mensaje
                        = "El permiso seleccionado "
                        + "se encuentra inactivo.";

                return false;
            }

            boolean existe
                    = rolPermisoDAO.existe(
                            rolPermiso.getIdRol(),
                            rolPermiso.getIdPermiso()
                    );

            if (existe) {

                mensaje
                        = "El rol ya tiene asignado "
                        + "ese permiso.";

                return false;
            }

            boolean guardado
                    = rolPermisoDAO.guardar(
                            rolPermiso
                    );

            if (guardado) {

                mensaje
                        = "Permiso asignado al rol "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo asignar "
                    + "el permiso al rol.";

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
            RolPermiso rolPermiso
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    rolPermiso
            );

            validarRolPermiso(
                    rolPermiso
            );

            RolPermiso guardado
                    = rolPermisoDAO.buscar(
                            rolPermiso.getIdRol(),
                            rolPermiso.getIdPermiso()
                    );

            if (guardado == null) {

                mensaje
                        = "La asignacion seleccionada "
                        + "ya no existe.";

                return false;
            }

            boolean modificado
                    = rolPermisoDAO.modificar(
                            rolPermiso
                    );

            if (modificado) {

                mensaje
                        = "Asignacion modificada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "la asignacion.";

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
            Integer idRol,
            Integer idPermiso
    ) {

        mensaje = "";

        if (idRol == null) {

            mensaje
                    = "Seleccione un rol.";

            return false;
        }

        if (idPermiso == null) {

            mensaje
                    = "Seleccione un permiso.";

            return false;
        }

        try {

            RolPermiso asignacion
                    = rolPermisoDAO.buscar(
                            idRol,
                            idPermiso
                    );

            if (asignacion == null) {

                mensaje
                        = "La asignacion seleccionada "
                        + "no existe.";

                return false;
            }

            if ("INACTIVA".equalsIgnoreCase(
                    asignacion.getEstadoAsignacion())) {

                mensaje
                        = "La asignacion ya se encuentra "
                        + "inactiva.";

                return false;
            }

            boolean desactivado
                    = rolPermisoDAO.desactivar(
                            idRol,
                            idPermiso
                    );

            if (desactivado) {

                mensaje
                        = "Asignacion desactivada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desactivar "
                    + "la asignacion.";

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
            Integer idRol,
            Integer idPermiso
    ) throws SQLException {

        if (idRol == null) {

            throw new IllegalArgumentException(
                    "Seleccione un rol."
            );
        }

        if (idPermiso == null) {

            throw new IllegalArgumentException(
                    "Seleccione un permiso."
            );
        }

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede eliminar "
                    + "asignaciones definitivamente."
            );
        }

        try {

            boolean eliminado
                    = rolPermisoDAO
                            .eliminarDefinitivamente(
                                    idRol,
                                    idPermiso
                            );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "La asignacion ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque existen registros relacionados. "
                        + "Puede desactivar la asignacion.",
                        e
                );
            }

            throw e;
        }
    }

    public RolPermiso buscar(
            Integer idRol,
            Integer idPermiso
    ) {

        mensaje = "";

        if (idRol == null
                || idPermiso == null) {

            return null;
        }

        try {

            return rolPermisoDAO.buscar(
                    idRol,
                    idPermiso
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "la asignacion.";

            return null;
        }
    }

    public List<RolPermiso> listarPorRol(
            Integer idRol
    ) {

        mensaje = "";

        if (idRol == null) {

            mensaje
                    = "Seleccione un rol.";

            return new ArrayList<>();
        }

        try {

            return rolPermisoDAO.listarPorRol(
                    idRol
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los permisos del rol.";

            return new ArrayList<>();
        }
    }

    public List<RolPermiso> listarPorPermiso(
            Integer idPermiso
    ) {

        mensaje = "";

        if (idPermiso == null) {

            mensaje
                    = "Seleccione un permiso.";

            return new ArrayList<>();
        }

        try {

            return rolPermisoDAO.listarPorPermiso(
                    idPermiso
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los roles del permiso.";

            return new ArrayList<>();
        }
    }

    public List<RolPermiso> listarActivosPorRol(
            Integer idRol
    ) {

        mensaje = "";

        if (idRol == null) {

            mensaje
                    = "Seleccione un rol.";

            return new ArrayList<>();
        }

        try {

            return rolPermisoDAO
                    .listarActivosPorRol(
                            idRol
                    );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los permisos activos del rol.";

            return new ArrayList<>();
        }
    }

    private void validarRolPermiso(
            RolPermiso rolPermiso
    ) {

        if (rolPermiso == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "de la asignacion."
            );
        }

        if (rolPermiso.getIdRol() == null) {

            throw new IllegalArgumentException(
                    "Seleccione un rol."
            );
        }

        if (rolPermiso.getIdPermiso() == null) {

            throw new IllegalArgumentException(
                    "Seleccione un permiso."
            );
        }

        validarFechaAsignacion(
                rolPermiso.getFechaAsignacion()
        );

        validarEstadoAsignacion(
                rolPermiso.getEstadoAsignacion()
        );
    }

    private void validarFechaAsignacion(
            LocalDate fechaAsignacion
    ) {

        if (fechaAsignacion == null) {

            throw new IllegalArgumentException(
                    "La fecha de asignacion "
                    + "es obligatoria."
            );
        }

        if (fechaAsignacion.isAfter(
                LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha de asignacion "
                    + "no puede ser futura."
            );
        }
    }

    private void validarEstadoAsignacion(
            String estadoAsignacion
    ) {

        if (estadoAsignacion == null
                || estadoAsignacion.isBlank()) {

            throw new IllegalArgumentException(
                    "El estado de la asignacion "
                    + "es obligatorio."
            );
        }

        if (!"ACTIVA".equalsIgnoreCase(
                estadoAsignacion)
                && !"INACTIVA".equalsIgnoreCase(
                        estadoAsignacion)) {

            throw new IllegalArgumentException(
                    "El estado de la asignacion debe ser "
                    + "ACTIVA o INACTIVA."
            );
        }
    }

    private void normalizarDatos(
            RolPermiso rolPermiso
    ) {

        if (rolPermiso == null) {
            return;
        }

        if (rolPermiso.getEstadoAsignacion() != null) {

            rolPermiso.setEstadoAsignacion(
                    rolPermiso
                            .getEstadoAsignacion()
                            .trim()
                            .toUpperCase()
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

            return "El rol ya tiene asignado "
                    + "ese permiso.";
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
