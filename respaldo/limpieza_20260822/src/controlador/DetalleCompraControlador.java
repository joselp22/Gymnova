package controlador;

import dao.DetalleCompraDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.DetalleCompra;
import utilidades.SesionUsuario;

/**
 * Controlador para detalle_compra.
 *
 * @author Usuario
 */
public class DetalleCompraControlador {

    private final DetalleCompraDAO detalleCompraDAO;
    private String mensaje;

    public DetalleCompraControlador() {

        detalleCompraDAO = new DetalleCompraDAO();
        mensaje = "";
    }


    public boolean registrar(
            DetalleCompra detalleCompra
    ) {

        mensaje = "";

        try {
            validarDetalleCompra(
                    detalleCompra
            );

            boolean guardado = detalleCompraDAO.guardar(
                    detalleCompra
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
            DetalleCompra detalleCompra
    ) {

        mensaje = "";

        try {

            validarDetalleCompra(
                    detalleCompra
            );

            DetalleCompra guardado = detalleCompraDAO.buscar(
                    detalleCompra.getIdDetalleCompra()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = detalleCompraDAO.modificar(
                    detalleCompra
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
            Long idDetalleCompra
    ) throws SQLException {

        validarClavePrimaria(
                idDetalleCompra
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = detalleCompraDAO.eliminarDefinitivamente(
                    idDetalleCompra
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
    public DetalleCompra buscar(
            Long idDetalleCompra
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idDetalleCompra
            );

            return detalleCompraDAO.buscar(
                    idDetalleCompra
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
    public List<DetalleCompra> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return detalleCompraDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<DetalleCompra> listarPorIdCompra(
            Long idCompra
    ) {

        mensaje = "";

        if (idCompra == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return detalleCompraDAO.listarPorIdCompra(
                    idCompra
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<DetalleCompra> listarPorIdProducto(
            Long idProducto
    ) {

        mensaje = "";

        if (idProducto == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return detalleCompraDAO.listarPorIdProducto(
                    idProducto
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarDetalleCompra(
            DetalleCompra detalleCompra
    ) {

        if (detalleCompra == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idDetalleCompra
    ) {

        if (idDetalleCompra == null) {
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
