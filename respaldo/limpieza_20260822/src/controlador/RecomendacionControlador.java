package controlador;

import dao.RecomendacionDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Recomendacion;
import utilidades.SesionUsuario;

/**
 * Controlador para recomendacion.
 *
 * @author Usuario
 */
public class RecomendacionControlador {

    private final RecomendacionDAO recomendacionDAO;
    private String mensaje;

    public RecomendacionControlador() {

        recomendacionDAO = new RecomendacionDAO();
        mensaje = "";
    }


    public boolean registrar(
            Recomendacion recomendacion
    ) {

        mensaje = "";

        try {
            validarRecomendacion(
                    recomendacion
            );

            boolean guardado = recomendacionDAO.guardar(
                    recomendacion
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
            Recomendacion recomendacion
    ) {

        mensaje = "";

        try {

            validarRecomendacion(
                    recomendacion
            );

            Recomendacion guardado = recomendacionDAO.buscar(
                    recomendacion.getIdRecomendacion()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = recomendacionDAO.modificar(
                    recomendacion
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
            Long idRecomendacion
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idRecomendacion
            );

            boolean desactivado = recomendacionDAO.desactivar(
                    idRecomendacion
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
            Long idRecomendacion
    ) throws SQLException {

        validarClavePrimaria(
                idRecomendacion
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = recomendacionDAO.eliminarDefinitivamente(
                    idRecomendacion
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
    public Recomendacion buscar(
            Long idRecomendacion
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idRecomendacion
            );

            return recomendacionDAO.buscar(
                    idRecomendacion
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
    public List<Recomendacion> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return recomendacionDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Recomendacion> listarPorIdEvaluacion(
            Long idEvaluacion
    ) {

        mensaje = "";

        if (idEvaluacion == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return recomendacionDAO.listarPorIdEvaluacion(
                    idEvaluacion
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarRecomendacion(
            Recomendacion recomendacion
    ) {

        if (recomendacion == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idRecomendacion
    ) {

        if (idRecomendacion == null) {
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
