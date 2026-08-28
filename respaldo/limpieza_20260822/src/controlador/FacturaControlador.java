package controlador;

import dao.FacturaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Factura;
import utilidades.SesionUsuario;

/**
 * Controlador para factura.
 *
 * @author Usuario
 */
public class FacturaControlador {

    private final FacturaDAO facturaDAO;
    private String mensaje;

    public FacturaControlador() {

        facturaDAO = new FacturaDAO();
        mensaje = "";
    }


    public boolean registrar(
            Factura factura
    ) {

        mensaje = "";

        try {
            if (factura != null) {
                factura.setNumeroFactura(null);
            }
            validarFactura(
                    factura
            );

            boolean guardado = facturaDAO.guardar(
                    factura
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
            Factura factura
    ) {

        mensaje = "";

        try {

            validarFactura(
                    factura
            );

            Factura guardado = facturaDAO.buscar(
                    factura.getIdFactura()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            if (!java.util.Objects.equals(
                    guardado.getNumeroFactura(),
                    factura.getNumeroFactura())) {
                mensaje = "El numero de factura no se puede modificar.";
                return false;
            }
            factura.setNumeroFactura(guardado.getNumeroFactura());

            boolean modificado = facturaDAO.modificar(
                    factura
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
            Long idFactura
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idFactura
            );

            boolean desactivado = facturaDAO.desactivar(
                    idFactura
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
            Long idFactura
    ) throws SQLException {

        validarClavePrimaria(
                idFactura
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = facturaDAO.eliminarDefinitivamente(
                    idFactura
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
    public Factura buscar(
            Long idFactura
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idFactura
            );

            return facturaDAO.buscar(
                    idFactura
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
    public List<Factura> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return facturaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Factura> listarPorIdCliente(
            Long idCliente
    ) {

        mensaje = "";

        if (idCliente == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return facturaDAO.listarPorIdCliente(
                    idCliente
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarFactura(
            Factura factura
    ) {

        if (factura == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
        if (factura.getFechaEmision() == null) {
            throw new IllegalArgumentException("La fecha de emision es obligatoria.");
        }
        if (factura.getIdCliente() == null) {
            throw new IllegalArgumentException("Seleccione el cliente de la factura.");
        }
        if (factura.getTotalDescuento() == null
                || factura.getTotalDescuento().signum() < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo.");
        }
        if (factura.getImpuesto() == null || factura.getImpuesto().signum() < 0) {
            throw new IllegalArgumentException("El impuesto no puede ser negativo.");
        }
        if (factura.getEstadoFactura() == null
                || factura.getEstadoFactura().isBlank()) {
            throw new IllegalArgumentException("El estado de la factura es obligatorio.");
        }
    }

    private void validarClavePrimaria(
            Long idFactura
    ) {

        if (idFactura == null) {
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
