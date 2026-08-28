package controlador;

import dao.TipoEquipoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.TipoEquipo;
import utilidades.SesionUsuario;

/**
 * Controlador para tipo_equipo.
 *
 * @author Usuario
 */
public class TipoEquipoControlador {

    private final TipoEquipoDAO tipoEquipoDAO;
    private String mensaje;

    public TipoEquipoControlador() {

        tipoEquipoDAO = new TipoEquipoDAO();
        mensaje = "";
    }


    public boolean registrar(
            TipoEquipo tipoEquipo
    ) {

        mensaje = "";

        try {
            validarTipoEquipo(
                    tipoEquipo
            );

            boolean guardado = tipoEquipoDAO.guardar(
                    tipoEquipo
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
            TipoEquipo tipoEquipo
    ) {

        mensaje = "";

        try {

            validarTipoEquipo(
                    tipoEquipo
            );

            TipoEquipo guardado = tipoEquipoDAO.buscar(
                    tipoEquipo.getIdTipoEquipo()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = tipoEquipoDAO.modificar(
                    tipoEquipo
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
            Long idTipoEquipo
    ) throws SQLException {

        validarClavePrimaria(
                idTipoEquipo
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = tipoEquipoDAO.eliminarDefinitivamente(
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
                        "No se puede eliminar porque existen registros relacionados.",
                        e
                );
            }

            throw e;
        }
    }
    public TipoEquipo buscar(
            Long idTipoEquipo
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idTipoEquipo
            );

            return tipoEquipoDAO.buscar(
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
    public List<TipoEquipo> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return tipoEquipoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarTipoEquipo(
            TipoEquipo tipoEquipo
    ) {

        if (tipoEquipo == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idTipoEquipo
    ) {

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
