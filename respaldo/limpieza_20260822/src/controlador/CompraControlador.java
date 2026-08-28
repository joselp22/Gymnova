package controlador;

import dao.CompraDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Compra;
import utilidades.SesionUsuario;

/**
 * Controlador para compra.
 *
 * @author Usuario
 */
public class CompraControlador {

    private final CompraDAO compraDAO;
    private String mensaje;

    public CompraControlador() {

        compraDAO = new CompraDAO();
        mensaje = "";
    }


    public boolean registrar(
            Compra compra
    ) {

        mensaje = "";

        try {
            if (compra != null) {
                compra.setNumeroCompra(null);
            }
            validarCompra(
                    compra
            );

            boolean guardado = compraDAO.guardar(
                    compra
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
            Compra compra
    ) {

        mensaje = "";

        try {

            validarCompra(
                    compra
            );

            Compra guardado = compraDAO.buscar(
                    compra.getIdCompra()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            if (!java.util.Objects.equals(
                    guardado.getNumeroCompra(), compra.getNumeroCompra())) {
                mensaje = "El numero de compra no se puede modificar.";
                return false;
            }
            compra.setNumeroCompra(guardado.getNumeroCompra());

            boolean modificado = compraDAO.modificar(
                    compra
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
            Long idCompra
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idCompra
            );

            boolean desactivado = compraDAO.desactivar(
                    idCompra
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
            Long idCompra
    ) throws SQLException {

        validarClavePrimaria(
                idCompra
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = compraDAO.eliminarDefinitivamente(
                    idCompra
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
    public Compra buscar(
            Long idCompra
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idCompra
            );

            return compraDAO.buscar(
                    idCompra
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
    public List<Compra> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return compraDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Compra> listarPorIdProveedor(
            Long idProveedor
    ) {

        mensaje = "";

        if (idProveedor == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return compraDAO.listarPorIdProveedor(
                    idProveedor
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Compra> listarPorIdEmpleado(
            Long idEmpleado
    ) {

        mensaje = "";

        if (idEmpleado == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return compraDAO.listarPorIdEmpleado(
                    idEmpleado
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarCompra(
            Compra compra
    ) {

        if (compra == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idCompra
    ) {

        if (idCompra == null) {
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
