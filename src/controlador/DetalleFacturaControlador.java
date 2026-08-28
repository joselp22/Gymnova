package controlador;

import dao.DetalleFacturaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.DetalleFactura;
import utilidades.SesionUsuario;

/**
 * Controlador para detalle_factura.
 *
 * @author Usuario
 */
public class DetalleFacturaControlador {

    private final DetalleFacturaDAO detalleFacturaDAO;
    private String mensaje;

    public DetalleFacturaControlador() {

        detalleFacturaDAO = new DetalleFacturaDAO();
        mensaje = "";
    }


    public boolean registrar(
            DetalleFactura detalleFactura
    ) {

        mensaje = "";

        try {
            validarDetalleFactura(
                    detalleFactura
            );

            boolean guardado = detalleFacturaDAO.guardar(
                    detalleFactura
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
            DetalleFactura detalleFactura
    ) {

        mensaje = "";

        try {

            validarDetalleFactura(
                    detalleFactura
            );

            DetalleFactura guardado = detalleFacturaDAO.buscar(
                    detalleFactura.getIdDetalleFactura()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = detalleFacturaDAO.modificar(
                    detalleFactura
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
            Long idDetalleFactura
    ) throws SQLException {

        validarClavePrimaria(
                idDetalleFactura
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = detalleFacturaDAO.eliminarDefinitivamente(
                    idDetalleFactura
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
    public DetalleFactura buscar(
            Long idDetalleFactura
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idDetalleFactura
            );

            return detalleFacturaDAO.buscar(
                    idDetalleFactura
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
    public List<DetalleFactura> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return detalleFacturaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<DetalleFactura> listarPorIdFactura(
            Long idFactura
    ) {

        mensaje = "";

        if (idFactura == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return detalleFacturaDAO.listarPorIdFactura(
                    idFactura
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarDetalleFactura(
            DetalleFactura detalleFactura
    ) {

        if (detalleFactura == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idDetalleFactura
    ) {

        if (idDetalleFactura == null) {
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
