package controlador;

import dao.AlimentoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Alimento;
import utilidades.SesionUsuario;

/**
 * Controlador para alimento.
 *
 * @author Usuario
 */
public class AlimentoControlador {

    private final AlimentoDAO alimentoDAO;
    private String mensaje;

    public AlimentoControlador() {

        alimentoDAO = new AlimentoDAO();
        mensaje = "";
    }


    public boolean registrar(
            Alimento alimento
    ) {

        mensaje = "";

        try {
            validarAlimento(
                    alimento
            );

            boolean guardado = alimentoDAO.guardar(
                    alimento
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
            Alimento alimento
    ) {

        mensaje = "";

        try {

            validarAlimento(
                    alimento
            );

            Alimento guardado = alimentoDAO.buscar(
                    alimento.getIdAlimento()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = alimentoDAO.modificar(
                    alimento
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
            Long idAlimento
    ) throws SQLException {

        validarClavePrimaria(
                idAlimento
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = alimentoDAO.eliminarDefinitivamente(
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
                        "No se puede eliminar porque existen registros relacionados.",
                        e
                );
            }

            throw e;
        }
    }
    public Alimento buscar(
            Long idAlimento
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idAlimento
            );

            return alimentoDAO.buscar(
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
    public List<Alimento> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return alimentoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarAlimento(
            Alimento alimento
    ) {

        if (alimento == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idAlimento
    ) {

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
