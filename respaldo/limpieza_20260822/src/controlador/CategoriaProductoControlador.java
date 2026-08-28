package controlador;

import dao.CategoriaProductoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.CategoriaProducto;
import utilidades.SesionUsuario;

/**
 * Controlador para categoria_producto.
 *
 * @author Usuario
 */
public class CategoriaProductoControlador {

    private final CategoriaProductoDAO categoriaProductoDAO;
    private String mensaje;

    public CategoriaProductoControlador() {

        categoriaProductoDAO = new CategoriaProductoDAO();
        mensaje = "";
    }


    public boolean registrar(
            CategoriaProducto categoriaProducto
    ) {

        mensaje = "";

        try {
            validarCategoriaProducto(
                    categoriaProducto
            );

            boolean guardado = categoriaProductoDAO.guardar(
                    categoriaProducto
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
            CategoriaProducto categoriaProducto
    ) {

        mensaje = "";

        try {

            validarCategoriaProducto(
                    categoriaProducto
            );

            CategoriaProducto guardado = categoriaProductoDAO.buscar(
                    categoriaProducto.getIdCategoriaProducto()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = categoriaProductoDAO.modificar(
                    categoriaProducto
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
            Long idCategoriaProducto
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idCategoriaProducto
            );

            boolean desactivado = categoriaProductoDAO.desactivar(
                    idCategoriaProducto
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
            Long idCategoriaProducto
    ) throws SQLException {

        validarClavePrimaria(
                idCategoriaProducto
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = categoriaProductoDAO.eliminarDefinitivamente(
                    idCategoriaProducto
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
    public CategoriaProducto buscar(
            Long idCategoriaProducto
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idCategoriaProducto
            );

            return categoriaProductoDAO.buscar(
                    idCategoriaProducto
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
    public List<CategoriaProducto> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return categoriaProductoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarCategoriaProducto(
            CategoriaProducto categoriaProducto
    ) {

        if (categoriaProducto == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idCategoriaProducto
    ) {

        if (idCategoriaProducto == null) {
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
