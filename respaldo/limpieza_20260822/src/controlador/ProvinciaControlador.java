package controlador;

import dao.ProvinciaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Provincia;
import utilidades.SesionUsuario;

/**
 * Controlador para provincia.
 *
 * @author Usuario
 */
public class ProvinciaControlador {

    private final ProvinciaDAO provinciaDAO;
    private String mensaje;

    public ProvinciaControlador() {

        provinciaDAO = new ProvinciaDAO();
        mensaje = "";
    }


    public boolean registrar(
            Provincia provincia
    ) {

        mensaje = "";

        try {
            validarProvincia(
                    provincia
            );

            boolean guardado = provinciaDAO.guardar(
                    provincia
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
            Provincia provincia
    ) {

        mensaje = "";

        try {

            validarProvincia(
                    provincia
            );

            Provincia guardada = provinciaDAO.buscar(
                    provincia.getIdProvincia()
            );

            if (guardada == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = provinciaDAO.modificar(
                    provincia
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
            Integer idProvincia
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idProvincia
            );

            boolean desactivado = provinciaDAO.desactivar(
                    idProvincia
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
            Integer idProvincia
    ) throws SQLException {

        validarClavePrimaria(
                idProvincia
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = provinciaDAO.eliminarDefinitivamente(
                    idProvincia
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
    public Provincia buscar(
            Integer idProvincia
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idProvincia
            );

            return provinciaDAO.buscar(
                    idProvincia
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
    public List<Provincia> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return provinciaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarProvincia(
            Provincia provincia
    ) {

        if (provincia == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Integer idProvincia
    ) {

        if (idProvincia == null) {
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
