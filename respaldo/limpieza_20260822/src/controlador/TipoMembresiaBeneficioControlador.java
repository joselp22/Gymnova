package controlador;

import dao.TipoMembresiaBeneficioDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.TipoMembresiaBeneficio;
import utilidades.SesionUsuario;

/**
 * Controlador para tipo_membresia_beneficio.
 *
 * @author Usuario
 */
public class TipoMembresiaBeneficioControlador {

    private final TipoMembresiaBeneficioDAO tipoMembresiaBeneficioDAO;
    private String mensaje;

    public TipoMembresiaBeneficioControlador() {

        tipoMembresiaBeneficioDAO = new TipoMembresiaBeneficioDAO();
        mensaje = "";
    }


    public boolean registrar(
            TipoMembresiaBeneficio tipoMembresiaBeneficio
    ) {

        mensaje = "";

        try {
            validarTipoMembresiaBeneficio(
                    tipoMembresiaBeneficio
            );

            boolean guardado = tipoMembresiaBeneficioDAO.guardar(
                    tipoMembresiaBeneficio
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
            TipoMembresiaBeneficio tipoMembresiaBeneficio
    ) {

        mensaje = "";

        try {

            validarTipoMembresiaBeneficio(
                    tipoMembresiaBeneficio
            );

            TipoMembresiaBeneficio guardado = tipoMembresiaBeneficioDAO.buscar(
                    tipoMembresiaBeneficio.getBeneficio(), tipoMembresiaBeneficio.getIdTipoMembresia()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = tipoMembresiaBeneficioDAO.modificar(
                    tipoMembresiaBeneficio
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
            String beneficio,
            Long idTipoMembresia
    ) throws SQLException {

        validarClavePrimaria(
                beneficio,
                            idTipoMembresia
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = tipoMembresiaBeneficioDAO.eliminarDefinitivamente(
                    beneficio,
                            idTipoMembresia
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
    public TipoMembresiaBeneficio buscar(
            String beneficio,
            Long idTipoMembresia
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    beneficio,
                            idTipoMembresia
            );

            return tipoMembresiaBeneficioDAO.buscar(
                    beneficio,
                            idTipoMembresia
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
    public List<TipoMembresiaBeneficio> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return tipoMembresiaBeneficioDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<TipoMembresiaBeneficio> listarPorIdTipoMembresia(
            Long idTipoMembresia
    ) {

        mensaje = "";

        if (idTipoMembresia == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return tipoMembresiaBeneficioDAO.listarPorIdTipoMembresia(
                    idTipoMembresia
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarTipoMembresiaBeneficio(
            TipoMembresiaBeneficio tipoMembresiaBeneficio
    ) {

        if (tipoMembresiaBeneficio == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            String beneficio,
            Long idTipoMembresia
    ) {

        if (beneficio == null || beneficio.isBlank()) {
            throw new IllegalArgumentException(
                    "Seleccione un registro."
            );
        }
        if (idTipoMembresia == null) {
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
