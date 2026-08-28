package controlador;

import dao.RequiereEquipoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.RequiereEquipo;
import utilidades.SesionUsuario;

/**
 * Controlador para requiere_equipo.
 *
 * @author Usuario
 */
public class RequiereEquipoControlador {

    private final RequiereEquipoDAO requiereEquipoDAO;
    private String mensaje;

    public RequiereEquipoControlador() {

        requiereEquipoDAO = new RequiereEquipoDAO();
        mensaje = "";
    }


    public boolean registrar(
            RequiereEquipo requiereEquipo
    ) {

        mensaje = "";

        try {
            validarRequiereEquipo(
                    requiereEquipo
            );

            boolean guardado = requiereEquipoDAO.guardar(
                    requiereEquipo
            );

            if (guardado) {
                mensaje = "Registro guardado correctamente.";
                return true;
            }

            mensaje = "No se pudo guardar el registro.";
            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje = traducirErrorBaseDatos(
                    e
            );
            return false;
        }
    }
    public boolean modificar(
            RequiereEquipo requiereEquipo
    ) {

        mensaje = "";

        try {

            validarRequiereEquipo(
                    requiereEquipo
            );

            RequiereEquipo guardado = requiereEquipoDAO.buscar(
                    requiereEquipo.getIdEjercicio(), requiereEquipo.getIdTipoEquipo()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = requiereEquipoDAO.modificar(
                    requiereEquipo
            );

            if (modificado) {
                mensaje = "Registro modificado correctamente.";
                return true;
            }

            mensaje = "No se pudo modificar el registro.";
            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje = traducirErrorBaseDatos(
                    e
            );
            return false;
        }
    }
    public void eliminarDefinitivamente(
            Long idEjercicio,
            Long idTipoEquipo
    ) throws SQLException {

        validarClavePrimaria(
                idEjercicio,
                            idTipoEquipo
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = requiereEquipoDAO.eliminarDefinitivamente(
                    idEjercicio,
                            idTipoEquipo
            );

            if (!eliminado) {
                throw new IllegalArgumentException(
                        "El registro ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {
                throw new IllegalStateException(
                        "No se puede eliminar porque existen registros relacionados. Puede desactivar el registro.",
                        e
                );
            }

            throw e;
        }
    }
    public RequiereEquipo buscar(
            Long idEjercicio,
            Long idTipoEquipo
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idEjercicio,
                            idTipoEquipo
            );

            return requiereEquipoDAO.buscar(
                    idEjercicio,
                            idTipoEquipo
            );

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return null;

        } catch (SQLException e) {

            mensaje = traducirErrorBaseDatos(
                    e
            );
            return null;
        }
    }
    public List<RequiereEquipo> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return requiereEquipoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<RequiereEquipo> listarPorIdEjercicio(
            Long idEjercicio
    ) {

        mensaje = "";

        if (idEjercicio == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return requiereEquipoDAO.listarPorIdEjercicio(
                    idEjercicio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<RequiereEquipo> listarPorIdTipoEquipo(
            Long idTipoEquipo
    ) {

        mensaje = "";

        if (idTipoEquipo == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return requiereEquipoDAO.listarPorIdTipoEquipo(
                    idTipoEquipo
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarRequiereEquipo(
            RequiereEquipo requiereEquipo
    ) {

        if (requiereEquipo == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idEjercicio,
            Long idTipoEquipo
    ) {

        if (idEjercicio == null) {
            throw new IllegalArgumentException(
                    "Seleccione un registro."
            );
        }
        if (idTipoEquipo == null) {
            throw new IllegalArgumentException(
                    "Seleccione un registro."
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
            return "No se puede realizar la operacion porque existen registros relacionados.";
        }

        if ("23505".equals(e.getSQLState())) {
            return "Ya existe un registro con esos datos unicos.";
        }

        if ("23514".equals(e.getSQLState())) {
            return "Los datos no cumplen las reglas de validacion de la base de datos.";
        }

        return "Error de base de datos: " + e.getMessage();
    }

    public String getMensaje() {
        return mensaje;
    }
}
