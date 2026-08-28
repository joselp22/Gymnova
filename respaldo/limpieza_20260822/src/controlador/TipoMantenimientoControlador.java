package controlador;

import dao.TipoMantenimientoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.TipoMantenimiento;
import utilidades.SesionUsuario;

/**
 * Controlador para tipo_mantenimiento.
 *
 * @author Usuario
 */
public class TipoMantenimientoControlador {

    private final TipoMantenimientoDAO tipoMantenimientoDAO;
    private String mensaje;

    public TipoMantenimientoControlador() {

        tipoMantenimientoDAO = new TipoMantenimientoDAO();
        mensaje = "";
    }


    public boolean registrar(
            TipoMantenimiento tipoMantenimiento
    ) {

        mensaje = "";

        try {
            validarTipoMantenimiento(
                    tipoMantenimiento
            );

            boolean guardado = tipoMantenimientoDAO.guardar(
                    tipoMantenimiento
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
            TipoMantenimiento tipoMantenimiento
    ) {

        mensaje = "";

        try {

            validarTipoMantenimiento(
                    tipoMantenimiento
            );

            TipoMantenimiento guardado = tipoMantenimientoDAO.buscar(
                    tipoMantenimiento.getIdTipoMantenimiento()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = tipoMantenimientoDAO.modificar(
                    tipoMantenimiento
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
            Long idTipoMantenimiento
    ) throws SQLException {

        validarClavePrimaria(
                idTipoMantenimiento
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = tipoMantenimientoDAO.eliminarDefinitivamente(
                    idTipoMantenimiento
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
                        "No se puede eliminar porque existen registros relacionados.",
                        e
                );
            }

            throw e;
        }
    }
    public TipoMantenimiento buscar(
            Long idTipoMantenimiento
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idTipoMantenimiento
            );

            return tipoMantenimientoDAO.buscar(
                    idTipoMantenimiento
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
    public List<TipoMantenimiento> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return tipoMantenimientoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarTipoMantenimiento(
            TipoMantenimiento tipoMantenimiento
    ) {

        if (tipoMantenimiento == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idTipoMantenimiento
    ) {

        if (idTipoMantenimiento == null) {
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
