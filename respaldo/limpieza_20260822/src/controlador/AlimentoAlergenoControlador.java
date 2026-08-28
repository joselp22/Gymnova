package controlador;

import dao.AlimentoAlergenoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.AlimentoAlergeno;
import utilidades.SesionUsuario;

/**
 * Controlador para alimento_alergeno.
 *
 * @author Usuario
 */
public class AlimentoAlergenoControlador {

    private final AlimentoAlergenoDAO alimentoAlergenoDAO;
    private String mensaje;

    public AlimentoAlergenoControlador() {

        alimentoAlergenoDAO = new AlimentoAlergenoDAO();
        mensaje = "";
    }


    public boolean registrar(
            AlimentoAlergeno alimentoAlergeno
    ) {

        mensaje = "";

        try {
            validarAlimentoAlergeno(
                    alimentoAlergeno
            );

            boolean guardado = alimentoAlergenoDAO.guardar(
                    alimentoAlergeno
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
            AlimentoAlergeno alimentoAlergeno
    ) {

        mensaje = "";

        try {

            validarAlimentoAlergeno(
                    alimentoAlergeno
            );

            AlimentoAlergeno guardado = alimentoAlergenoDAO.buscar(
                    alimentoAlergeno.getAlergeno(), alimentoAlergeno.getIdAlimento()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = alimentoAlergenoDAO.modificar(
                    alimentoAlergeno
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
            String alergeno,
            Long idAlimento
    ) throws SQLException {

        validarClavePrimaria(
                alergeno,
                            idAlimento
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = alimentoAlergenoDAO.eliminarDefinitivamente(
                    alergeno,
                            idAlimento
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
    public AlimentoAlergeno buscar(
            String alergeno,
            Long idAlimento
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    alergeno,
                            idAlimento
            );

            return alimentoAlergenoDAO.buscar(
                    alergeno,
                            idAlimento
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
    public List<AlimentoAlergeno> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return alimentoAlergenoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<AlimentoAlergeno> listarPorIdAlimento(
            Long idAlimento
    ) {

        mensaje = "";

        if (idAlimento == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return alimentoAlergenoDAO.listarPorIdAlimento(
                    idAlimento
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarAlimentoAlergeno(
            AlimentoAlergeno alimentoAlergeno
    ) {

        if (alimentoAlergeno == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            String alergeno,
            Long idAlimento
    ) {

        if (alergeno == null || alergeno.isBlank()) {
            throw new IllegalArgumentException(
                    "Seleccione un registro."
            );
        }
        if (idAlimento == null) {
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
