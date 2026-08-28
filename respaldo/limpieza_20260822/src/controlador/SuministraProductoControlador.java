package controlador;

import dao.SuministraProductoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.SuministraProducto;
import utilidades.SesionUsuario;

/**
 * Controlador para suministra_producto.
 *
 * @author Usuario
 */
public class SuministraProductoControlador {

    private final SuministraProductoDAO suministraProductoDAO;
    private String mensaje;

    public SuministraProductoControlador() {

        suministraProductoDAO = new SuministraProductoDAO();
        mensaje = "";
    }


    public boolean registrar(
            SuministraProducto suministraProducto
    ) {

        mensaje = "";

        try {
            validarSuministraProducto(
                    suministraProducto
            );

            boolean guardado = suministraProductoDAO.guardar(
                    suministraProducto
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
            SuministraProducto suministraProducto
    ) {

        mensaje = "";

        try {

            validarSuministraProducto(
                    suministraProducto
            );

            SuministraProducto guardado = suministraProductoDAO.buscar(
                    suministraProducto.getIdProveedor(), suministraProducto.getIdProducto()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = suministraProductoDAO.modificar(
                    suministraProducto
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
            Long idProveedor,
            Long idProducto
    ) throws SQLException {

        validarClavePrimaria(
                idProveedor,
                            idProducto
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = suministraProductoDAO.eliminarDefinitivamente(
                    idProveedor,
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
    public SuministraProducto buscar(
            Long idProveedor,
            Long idProducto
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idProveedor,
                            idProducto
            );

            return suministraProductoDAO.buscar(
                    idProveedor,
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
    public List<SuministraProducto> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return suministraProductoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<SuministraProducto> listarPorIdProveedor(
            Long idProveedor
    ) {

        mensaje = "";

        if (idProveedor == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return suministraProductoDAO.listarPorIdProveedor(
                    idProveedor
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<SuministraProducto> listarPorIdProducto(
            Long idProducto
    ) {

        mensaje = "";

        if (idProducto == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return suministraProductoDAO.listarPorIdProducto(
                    idProducto
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarSuministraProducto(
            SuministraProducto suministraProducto
    ) {

        if (suministraProducto == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idProveedor,
            Long idProducto
    ) {

        if (idProveedor == null) {
            throw new IllegalArgumentException(
                    "Seleccione un registro."
            );
        }
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
