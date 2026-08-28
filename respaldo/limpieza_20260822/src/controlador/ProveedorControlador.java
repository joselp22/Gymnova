package controlador;

import dao.ProveedorDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Proveedor;
import utilidades.SesionUsuario;

/**
 * Controlador para proveedor.
 *
 * @author Usuario
 */
public class ProveedorControlador {

    private final ProveedorDAO proveedorDAO;
    private String mensaje;

    public ProveedorControlador() {

        proveedorDAO = new ProveedorDAO();
        mensaje = "";
    }


    public boolean registrar(
            Proveedor proveedor
    ) {

        mensaje = "";

        try {
            validarProveedor(
                    proveedor
            );

            boolean guardado = proveedorDAO.guardar(
                    proveedor
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
            Proveedor proveedor
    ) {

        mensaje = "";

        try {

            validarProveedor(
                    proveedor
            );

            Proveedor guardado = proveedorDAO.buscar(
                    proveedor.getIdProveedor()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = proveedorDAO.modificar(
                    proveedor
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
            Long idProveedor
    ) throws SQLException {

        validarClavePrimaria(
                idProveedor
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = proveedorDAO.eliminarDefinitivamente(
                    idProveedor
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
    public Proveedor buscar(
            Long idProveedor
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idProveedor
            );

            return proveedorDAO.buscar(
                    idProveedor
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
    public List<Proveedor> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return proveedorDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarProveedor(
            Proveedor proveedor
    ) {

        if (proveedor == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idProveedor
    ) {

        if (idProveedor == null) {
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
