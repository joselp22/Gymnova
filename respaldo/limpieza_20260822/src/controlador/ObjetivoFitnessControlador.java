package controlador;

import dao.ObjetivoFitnessDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.ObjetivoFitness;
import utilidades.SesionUsuario;

/**
 * Controlador para objetivo_fitness.
 *
 * @author Usuario
 */
public class ObjetivoFitnessControlador {

    private final ObjetivoFitnessDAO objetivoFitnessDAO;
    private String mensaje;

    public ObjetivoFitnessControlador() {

        objetivoFitnessDAO = new ObjetivoFitnessDAO();
        mensaje = "";
    }


    public boolean registrar(
            ObjetivoFitness objetivoFitness
    ) {

        mensaje = "";

        try {
            validarObjetivoFitness(
                    objetivoFitness
            );

            boolean guardado = objetivoFitnessDAO.guardar(
                    objetivoFitness
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
            ObjetivoFitness objetivoFitness
    ) {

        mensaje = "";

        try {

            validarObjetivoFitness(
                    objetivoFitness
            );

            ObjetivoFitness guardado = objetivoFitnessDAO.buscar(
                    objetivoFitness.getIdObjetivo()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = objetivoFitnessDAO.modificar(
                    objetivoFitness
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
            Long idObjetivo
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idObjetivo
            );

            boolean desactivado = objetivoFitnessDAO.desactivar(
                    idObjetivo
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
            Long idObjetivo
    ) throws SQLException {

        validarClavePrimaria(
                idObjetivo
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = objetivoFitnessDAO.eliminarDefinitivamente(
                    idObjetivo
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
    public ObjetivoFitness buscar(
            Long idObjetivo
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idObjetivo
            );

            return objetivoFitnessDAO.buscar(
                    idObjetivo
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
    public List<ObjetivoFitness> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return objetivoFitnessDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarObjetivoFitness(
            ObjetivoFitness objetivoFitness
    ) {

        if (objetivoFitness == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idObjetivo
    ) {

        if (idObjetivo == null) {
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
