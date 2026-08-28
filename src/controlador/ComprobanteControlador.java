package controlador;

import dao.ComprobanteDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Comprobante;
import utilidades.SesionUsuario;

/**
 * Controlador para comprobante.
 *
 * @author Usuario
 */
public class ComprobanteControlador {

    private final ComprobanteDAO comprobanteDAO;
    private String mensaje;

    public ComprobanteControlador() {

        comprobanteDAO = new ComprobanteDAO();
        mensaje = "";
    }


    public boolean registrar(
            Comprobante comprobante
    ) {

        mensaje = "";

        try {
            if (comprobante != null) {
                comprobante.setNumeroComprobante(null);
            }
            validarComprobante(
                    comprobante
            );

            boolean guardado = comprobanteDAO.guardar(
                    comprobante
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
            Comprobante comprobante
    ) {

        mensaje = "";

        try {

            validarComprobante(
                    comprobante
            );

            Comprobante guardado = comprobanteDAO.buscar(
                    comprobante.getIdComprobante()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            if (!java.util.Objects.equals(
                    guardado.getNumeroComprobante(),
                    comprobante.getNumeroComprobante())) {
                mensaje = "El numero de comprobante no se puede modificar.";
                return false;
            }
            comprobante.setNumeroComprobante(
                    guardado.getNumeroComprobante());

            boolean modificado = comprobanteDAO.modificar(
                    comprobante
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
            Long idComprobante
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idComprobante
            );

            boolean desactivado = comprobanteDAO.desactivar(
                    idComprobante
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
            Long idComprobante
    ) throws SQLException {

        validarClavePrimaria(
                idComprobante
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = comprobanteDAO.eliminarDefinitivamente(
                    idComprobante
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
    public Comprobante buscar(
            Long idComprobante
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idComprobante
            );

            return comprobanteDAO.buscar(
                    idComprobante
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
    public List<Comprobante> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return comprobanteDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Comprobante> listarPorIdPago(
            Long idPago
    ) {

        mensaje = "";

        if (idPago == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return comprobanteDAO.listarPorIdPago(
                    idPago
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarComprobante(
            Comprobante comprobante
    ) {

        if (comprobante == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }

        if (texto(comprobante.getTipoComprobante()).isEmpty()) {
            throw new IllegalArgumentException("Seleccione el tipo de comprobante.");
        }
        if (comprobante.getFechaEmision() == null
                || comprobante.getFechaEmision().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de emision no puede estar vacia ni ser futura.");
        }
        if (comprobante.getFechaEnvio() != null
                && comprobante.getFechaEnvio().isBefore(comprobante.getFechaEmision())) {
            throw new IllegalArgumentException("La fecha de envio no puede ser anterior a la emision.");
        }
        if (comprobante.getCorreoEnvio() != null
                && !comprobante.getCorreoEnvio().isBlank()
                && !comprobante.getCorreoEnvio().matches("(?i)^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$")) {
            throw new IllegalArgumentException("Ingrese un correo de envio valido.");
        }
        if (!java.util.Set.of("GENERADO", "ENVIADO", "ANULADO", "ERROR")
                .contains(texto(comprobante.getEstadoComprobante()))) {
            throw new IllegalArgumentException("Seleccione un estado de comprobante valido.");
        }
        if (comprobante.getIdPago() == null) {
            throw new IllegalArgumentException("Seleccione el pago relacionado.");
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void validarClavePrimaria(
            Long idComprobante
    ) {

        if (idComprobante == null) {
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
