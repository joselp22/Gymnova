package controlador;

import dao.AplicaDescuentoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.AplicaDescuento;
import utilidades.SesionUsuario;

/**
 * Controlador para aplica_descuento.
 *
 * @author Usuario
 */
public class AplicaDescuentoControlador {

    private final AplicaDescuentoDAO aplicaDescuentoDAO;
    private String mensaje;

    public AplicaDescuentoControlador() {

        aplicaDescuentoDAO = new AplicaDescuentoDAO();
        mensaje = "";
    }


    public boolean registrar(
            AplicaDescuento aplicaDescuento
    ) {

        mensaje = "";

        try {
            validarAplicaDescuento(
                    aplicaDescuento
            );

            boolean guardado = aplicaDescuentoDAO.guardar(
                    aplicaDescuento
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
            AplicaDescuento aplicaDescuento
    ) {

        mensaje = "";

        try {

            validarAplicaDescuento(
                    aplicaDescuento
            );

            AplicaDescuento guardado = aplicaDescuentoDAO.buscar(
                    aplicaDescuento.getIdFactura(), aplicaDescuento.getIdDescuento()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = aplicaDescuentoDAO.modificar(
                    aplicaDescuento
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
            Long idFactura,
            Long idDescuento
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idFactura,
                            idDescuento
            );

            boolean desactivado = aplicaDescuentoDAO.desactivar(
                    idFactura,
                            idDescuento
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
            Long idFactura,
            Long idDescuento
    ) throws SQLException {

        validarClavePrimaria(
                idFactura,
                            idDescuento
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = aplicaDescuentoDAO.eliminarDefinitivamente(
                    idFactura,
                            idDescuento
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
    public AplicaDescuento buscar(
            Long idFactura,
            Long idDescuento
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idFactura,
                            idDescuento
            );

            return aplicaDescuentoDAO.buscar(
                    idFactura,
                            idDescuento
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
    public List<AplicaDescuento> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return aplicaDescuentoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<AplicaDescuento> listarPorIdFactura(
            Long idFactura
    ) {

        mensaje = "";

        if (idFactura == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return aplicaDescuentoDAO.listarPorIdFactura(
                    idFactura
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<AplicaDescuento> listarPorIdDescuento(
            Long idDescuento
    ) {

        mensaje = "";

        if (idDescuento == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return aplicaDescuentoDAO.listarPorIdDescuento(
                    idDescuento
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarAplicaDescuento(
            AplicaDescuento aplicaDescuento
    ) {

        if (aplicaDescuento == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idFactura,
            Long idDescuento
    ) {

        if (idFactura == null) {
            throw new IllegalArgumentException(
                    "Seleccione un registro."
            );
        }
        if (idDescuento == null) {
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
