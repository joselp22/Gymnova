package controlador;

import dao.ProductoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Producto;
import utilidades.SesionUsuario;

/**
 * Controlador para producto.
 *
 * @author Usuario
 */
public class ProductoControlador {

    private final ProductoDAO productoDAO;
    private String mensaje;

    public ProductoControlador() {

        productoDAO = new ProductoDAO();
        mensaje = "";
    }


    public boolean registrar(
            Producto producto
    ) {

        mensaje = "";

        try {
            if (producto != null) {
                producto.setCodigoProducto(
                        null
                );
            }

            validarProducto(
                    producto
            );

            boolean guardado = productoDAO.guardar(
                    producto
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
            Producto producto
    ) {

        mensaje = "";

        try {

            validarProducto(
                    producto
            );

            Producto guardado = productoDAO.buscar(
                    producto.getIdProducto()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            producto.setCodigoProducto(guardado.getCodigoProducto());
            boolean modificado = productoDAO.modificar(
                    producto
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
            Long idProducto
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idProducto
            );

            boolean desactivado = productoDAO.desactivar(
                    idProducto
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
            Long idProducto
    ) throws SQLException {

        validarClavePrimaria(
                idProducto
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = productoDAO.eliminarDefinitivamente(
                    idProducto
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
    public Producto buscar(
            Long idProducto
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idProducto
            );

            return productoDAO.buscar(
                    idProducto
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
    public List<Producto> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return productoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Producto> listarPorIdCategoriaProducto(
            Long idCategoriaProducto
    ) {

        mensaje = "";

        if (idCategoriaProducto == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return productoDAO.listarPorIdCategoriaProducto(
                    idCategoriaProducto
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarProducto(
            Producto producto
    ) {

        if (producto == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idProducto
    ) {

        if (idProducto == null) {
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
