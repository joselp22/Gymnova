package controlador;

import dao.MetodoPagoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.MetodoPago;
import utilidades.SesionUsuario;

/**
 * Controlador para metodo_pago.
 *
 * @author Usuario
 */
public class MetodoPagoControlador {

    private final MetodoPagoDAO metodoPagoDAO;
    private String mensaje;

    public MetodoPagoControlador() {

        metodoPagoDAO = new MetodoPagoDAO();
        mensaje = "";
    }


    public boolean registrar(
            MetodoPago metodoPago
    ) {

        mensaje = "";

        try {
            validarMetodoPago(
                    metodoPago
            );

            boolean guardado = metodoPagoDAO.guardar(
                    metodoPago
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
            MetodoPago metodoPago
    ) {

        mensaje = "";

        try {

            validarMetodoPago(
                    metodoPago
            );

            MetodoPago guardado = metodoPagoDAO.buscar(
                    metodoPago.getIdMetodoPago()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = metodoPagoDAO.modificar(
                    metodoPago
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
            Long idMetodoPago
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idMetodoPago
            );

            boolean desactivado = metodoPagoDAO.desactivar(
                    idMetodoPago
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
            Long idMetodoPago
    ) throws SQLException {

        validarClavePrimaria(
                idMetodoPago
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = metodoPagoDAO.eliminarDefinitivamente(
                    idMetodoPago
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
    public MetodoPago buscar(
            Long idMetodoPago
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idMetodoPago
            );

            return metodoPagoDAO.buscar(
                    idMetodoPago
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
    public List<MetodoPago> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return metodoPagoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarMetodoPago(
            MetodoPago metodoPago
    ) {

        if (metodoPago == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idMetodoPago
    ) {

        if (idMetodoPago == null) {
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
