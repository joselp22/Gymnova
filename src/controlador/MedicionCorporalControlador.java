package controlador;

import dao.MedicionCorporalDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.MedicionCorporal;
import utilidades.SesionUsuario;

/**
 * Controlador para medicion_corporal.
 *
 * @author Usuario
 */
public class MedicionCorporalControlador {

    private final MedicionCorporalDAO medicionCorporalDAO;
    private String mensaje;

    public MedicionCorporalControlador() {

        medicionCorporalDAO = new MedicionCorporalDAO();
        mensaje = "";
    }


    public boolean registrar(
            MedicionCorporal medicionCorporal
    ) {

        mensaje = "";

        try {
            validarMedicionCorporal(
                    medicionCorporal
            );

            boolean guardado = medicionCorporalDAO.guardar(
                    medicionCorporal
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
            MedicionCorporal medicionCorporal
    ) {

        mensaje = "";

        try {

            validarMedicionCorporal(
                    medicionCorporal
            );

            MedicionCorporal guardado = medicionCorporalDAO.buscar(
                    medicionCorporal.getIdMedicion()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = medicionCorporalDAO.modificar(
                    medicionCorporal
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
            Long idMedicion
    ) throws SQLException {

        validarClavePrimaria(
                idMedicion
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = medicionCorporalDAO.eliminarDefinitivamente(
                    idMedicion
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
    public MedicionCorporal buscar(
            Long idMedicion
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idMedicion
            );

            return medicionCorporalDAO.buscar(
                    idMedicion
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
    public List<MedicionCorporal> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return medicionCorporalDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<MedicionCorporal> listarPorIdEvaluacion(
            Long idEvaluacion
    ) {

        mensaje = "";

        if (idEvaluacion == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return medicionCorporalDAO.listarPorIdEvaluacion(
                    idEvaluacion
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarMedicionCorporal(
            MedicionCorporal medicionCorporal
    ) {

        if (medicionCorporal == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idMedicion
    ) {

        if (idMedicion == null) {
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
