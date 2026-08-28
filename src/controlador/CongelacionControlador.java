package controlador;

import dao.CongelacionDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Congelacion;
import utilidades.SesionUsuario;

/**
 * Controlador para congelacion.
 *
 * @author Usuario
 */
public class CongelacionControlador {

    private final CongelacionDAO congelacionDAO;
    private String mensaje;

    public CongelacionControlador() {

        congelacionDAO = new CongelacionDAO();
        mensaje = "";
    }


    public boolean registrar(
            Congelacion congelacion
    ) {

        mensaje = "";

        try {
            validarCongelacion(
                    congelacion
            );

            boolean guardado = congelacionDAO.guardar(
                    congelacion
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
            Congelacion congelacion
    ) {

        mensaje = "";

        try {

            validarCongelacion(
                    congelacion
            );

            Congelacion guardado = congelacionDAO.buscar(
                    congelacion.getIdCongelacion()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = congelacionDAO.modificar(
                    congelacion
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
    public boolean desactivar(
            Long idCongelacion
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idCongelacion
            );

            boolean desactivado = congelacionDAO.desactivar(
                    idCongelacion
            );

            if (desactivado) {
                mensaje = "Registro desactivado correctamente.";
                return true;
            }

            mensaje = "No se pudo desactivar el registro.";
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
            Long idCongelacion
    ) throws SQLException {

        validarClavePrimaria(
                idCongelacion
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = congelacionDAO.eliminarDefinitivamente(
                    idCongelacion
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
    public Congelacion buscar(
            Long idCongelacion
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idCongelacion
            );

            return congelacionDAO.buscar(
                    idCongelacion
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
    public List<Congelacion> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return congelacionDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Congelacion> listarPorIdMembresia(
            Long idMembresia
    ) {

        mensaje = "";

        if (idMembresia == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return congelacionDAO.listarPorIdMembresia(
                    idMembresia
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarCongelacion(
            Congelacion congelacion
    ) {

        if (congelacion == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idCongelacion
    ) {

        if (idCongelacion == null) {
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
