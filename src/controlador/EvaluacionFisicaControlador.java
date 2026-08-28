package controlador;

import dao.EvaluacionFisicaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.EvaluacionFisica;
import utilidades.SesionUsuario;

/**
 * Controlador para evaluacion_fisica.
 *
 * @author Usuario
 */
public class EvaluacionFisicaControlador {

    private final EvaluacionFisicaDAO evaluacionFisicaDAO;
    private String mensaje;

    public EvaluacionFisicaControlador() {

        evaluacionFisicaDAO = new EvaluacionFisicaDAO();
        mensaje = "";
    }


    public boolean registrar(
            EvaluacionFisica evaluacionFisica
    ) {

        mensaje = "";

        try {
            if (evaluacionFisica != null) {
                evaluacionFisica.setCodigoEvaluacion(
                        null
                );
            }

            validarEvaluacionFisica(
                    evaluacionFisica
            );

            boolean guardado = evaluacionFisicaDAO.guardar(
                    evaluacionFisica
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
            EvaluacionFisica evaluacionFisica
    ) {

        mensaje = "";

        try {

            validarEvaluacionFisica(
                    evaluacionFisica
            );

            EvaluacionFisica guardado = evaluacionFisicaDAO.buscar(
                    evaluacionFisica.getIdEvaluacion()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            evaluacionFisica.setCodigoEvaluacion(guardado.getCodigoEvaluacion());
            boolean modificado = evaluacionFisicaDAO.modificar(
                    evaluacionFisica
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
            Long idEvaluacion
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idEvaluacion
            );

            boolean desactivado = evaluacionFisicaDAO.desactivar(
                    idEvaluacion
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
            Long idEvaluacion
    ) throws SQLException {

        validarClavePrimaria(
                idEvaluacion
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = evaluacionFisicaDAO.eliminarDefinitivamente(
                    idEvaluacion
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
    public EvaluacionFisica buscar(
            Long idEvaluacion
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idEvaluacion
            );

            return evaluacionFisicaDAO.buscar(
                    idEvaluacion
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
    public List<EvaluacionFisica> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return evaluacionFisicaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<EvaluacionFisica> listarPorIdCliente(
            Long idCliente
    ) {

        mensaje = "";

        if (idCliente == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return evaluacionFisicaDAO.listarPorIdCliente(
                    idCliente
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<EvaluacionFisica> listarPorIdEntrenador(
            Long idEntrenador
    ) {

        mensaje = "";

        if (idEntrenador == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return evaluacionFisicaDAO.listarPorIdEntrenador(
                    idEntrenador
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarEvaluacionFisica(
            EvaluacionFisica evaluacionFisica
    ) {

        if (evaluacionFisica == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idEvaluacion
    ) {

        if (idEvaluacion == null) {
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
