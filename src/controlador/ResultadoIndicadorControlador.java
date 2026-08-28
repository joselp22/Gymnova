package controlador;

import dao.ResultadoIndicadorDAO;
import dao.IndicadorSaludDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.ResultadoIndicador;
import modelo.IndicadorSalud;
import utilidades.SesionUsuario;

/**
 * Controlador para resultado_indicador.
 *
 * @author Usuario
 */
public class ResultadoIndicadorControlador {

    private final ResultadoIndicadorDAO resultadoIndicadorDAO;
    private String mensaje;

    public ResultadoIndicadorControlador() {

        resultadoIndicadorDAO = new ResultadoIndicadorDAO();
        mensaje = "";
    }


    public boolean registrar(
            ResultadoIndicador resultadoIndicador
    ) {

        mensaje = "";

        try {
            validarResultadoIndicador(
                    resultadoIndicador
            );
            calcularFueraDeRango(resultadoIndicador);

            boolean guardado = resultadoIndicadorDAO.guardar(
                    resultadoIndicador
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
            ResultadoIndicador resultadoIndicador
    ) {

        mensaje = "";

        try {

            validarResultadoIndicador(
                    resultadoIndicador
            );
            calcularFueraDeRango(resultadoIndicador);

            ResultadoIndicador guardado = resultadoIndicadorDAO.buscar(
                    resultadoIndicador.getIdResultado()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = resultadoIndicadorDAO.modificar(
                    resultadoIndicador
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
            Long idResultado
    ) throws SQLException {

        validarClavePrimaria(
                idResultado
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = resultadoIndicadorDAO.eliminarDefinitivamente(
                    idResultado
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
    public ResultadoIndicador buscar(
            Long idResultado
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idResultado
            );

            return resultadoIndicadorDAO.buscar(
                    idResultado
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
    public List<ResultadoIndicador> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return resultadoIndicadorDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ResultadoIndicador> listarPorIdEvaluacion(
            Long idEvaluacion
    ) {

        mensaje = "";

        if (idEvaluacion == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return resultadoIndicadorDAO.listarPorIdEvaluacion(
                    idEvaluacion
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ResultadoIndicador> listarPorIdIndicador(
            Long idIndicador
    ) {

        mensaje = "";

        if (idIndicador == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return resultadoIndicadorDAO.listarPorIdIndicador(
                    idIndicador
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarResultadoIndicador(
            ResultadoIndicador resultadoIndicador
    ) {

        if (resultadoIndicador == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
        if (resultadoIndicador.getValorObtenido() == null) {
            throw new IllegalArgumentException(
                    "Ingrese el valor obtenido."
            );
        }
        if (resultadoIndicador.getFechaRegistro() == null) {
            throw new IllegalArgumentException(
                    "Ingrese la fecha de registro."
            );
        }
        if (resultadoIndicador.getIdEvaluacion() == null
                || resultadoIndicador.getIdIndicador() == null) {
            throw new IllegalArgumentException(
                    "Seleccione la evaluacion y el indicador."
            );
        }
    }

    private void calcularFueraDeRango(
            ResultadoIndicador resultadoIndicador
    ) throws SQLException {

        IndicadorSalud indicador = new IndicadorSaludDAO().buscar(
                resultadoIndicador.getIdIndicador()
        );

        if (indicador == null) {
            throw new IllegalArgumentException(
                    "El indicador de salud seleccionado no existe."
            );
        }

        boolean fuera = false;

        if (indicador.getValorMinimoReferencia() != null
                && resultadoIndicador.getValorObtenido().compareTo(
                        indicador.getValorMinimoReferencia()) < 0) {
            fuera = true;
        }

        if (indicador.getValorMaximoReferencia() != null
                && resultadoIndicador.getValorObtenido().compareTo(
                        indicador.getValorMaximoReferencia()) > 0) {
            fuera = true;
        }

        resultadoIndicador.setFueraDeRango(fuera);
    }

    private void validarClavePrimaria(
            Long idResultado
    ) {

        if (idResultado == null) {
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
