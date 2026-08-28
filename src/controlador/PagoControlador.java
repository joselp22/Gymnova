package controlador;

import dao.PagoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Pago;
import utilidades.SesionUsuario;

/**
 * Controlador para pago.
 *
 * @author Usuario
 */
public class PagoControlador {

    private final PagoDAO pagoDAO;
    private String mensaje;

    public PagoControlador() {

        pagoDAO = new PagoDAO();
        mensaje = "";
    }


    public boolean registrar(
            Pago pago
    ) {

        mensaje = "";

        try {
            if (pago != null) {
                pago.setCodigoPago(
                        null
                );
            }

            validarPago(
                    pago
            );

            boolean guardado = pagoDAO.guardar(
                    pago
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
            Pago pago
    ) {

        mensaje = "";

        try {

            validarPago(
                    pago
            );

            Pago guardado = pagoDAO.buscar(
                    pago.getIdPago()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            if ("CONFIRMADO".equalsIgnoreCase(guardado.getEstadoPago())) {
                mensaje = "Un pago CONFIRMADO no se puede modificar.";
                return false;
            }

            pago.setCodigoPago(guardado.getCodigoPago());
            boolean modificado = pagoDAO.modificar(
                    pago
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
            Long idPago
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idPago
            );

            Pago guardado = pagoDAO.buscar(idPago);
            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }
            if ("CONFIRMADO".equalsIgnoreCase(guardado.getEstadoPago())) {
                mensaje = "Un pago CONFIRMADO no se puede anular ni desactivar.";
                return false;
            }

            boolean desactivado = pagoDAO.desactivar(
                    idPago
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
            Long idPago
    ) throws SQLException {

        validarClavePrimaria(
                idPago
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = pagoDAO.eliminarDefinitivamente(
                    idPago
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
    public Pago buscar(
            Long idPago
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idPago
            );

            return pagoDAO.buscar(
                    idPago
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
    public List<Pago> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return pagoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Pago> listarPorIdFactura(
            Long idFactura
    ) {

        mensaje = "";

        if (idFactura == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return pagoDAO.listarPorIdFactura(
                    idFactura
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Pago> listarPorIdMetodoPago(
            Long idMetodoPago
    ) {

        mensaje = "";

        if (idMetodoPago == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return pagoDAO.listarPorIdMetodoPago(
                    idMetodoPago
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarPago(
            Pago pago
    ) {

        if (pago == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }

        if (pago.getMontoPago() == null || pago.getMontoPago().signum() < 0
                || pago.getMontoRecibido() == null || pago.getMontoRecibido().signum() < 0) {
            throw new IllegalArgumentException("Los montos deben ser valores validos mayores o iguales a cero.");
        }
        if (pago.getMontoRecibido().compareTo(pago.getMontoPago()) < 0) {
            throw new IllegalArgumentException("El monto recibido no puede ser menor que el monto del pago.");
        }
        if (pago.getFechaHoraPago() == null) {
            throw new IllegalArgumentException("Ingrese la fecha y hora del pago.");
        }
        if (!java.util.Set.of("REGISTRADO", "CONFIRMADO", "ANULADO", "RECHAZADO", "DEVUELTO")
                .contains(texto(pago.getEstadoPago()))) {
            throw new IllegalArgumentException("Seleccione un estado de pago valido.");
        }
        if (pago.getIdFactura() == null || pago.getIdMetodoPago() == null) {
            throw new IllegalArgumentException("Seleccione la factura y el metodo de pago.");
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void validarClavePrimaria(
            Long idPago
    ) {

        if (idPago == null) {
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
