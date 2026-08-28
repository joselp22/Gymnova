package controlador;

import dao.DescuentoDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.Descuento;
import utilidades.SesionUsuario;

/**
 * Controlador para descuento.
 *
 * @author Usuario
 */
public class DescuentoControlador {

    private final DescuentoDAO descuentoDAO;
    private String mensaje;

    public DescuentoControlador() {

        descuentoDAO = new DescuentoDAO();
        mensaje = "";
    }


    public boolean registrar(
            Descuento descuento
    ) {

        mensaje = "";

        try {
            descuento.setCodigoDescuento(null);
            validarDescuento(
                    descuento
            );

            boolean guardado = descuentoDAO.guardar(
                    descuento
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
            Descuento descuento
    ) {

        mensaje = "";

        try {

            validarDescuento(
                    descuento
            );

            Descuento guardado = descuentoDAO.buscar(
                    descuento.getIdDescuento()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            descuento.setCodigoDescuento(
                    guardado.getCodigoDescuento()
            );

            boolean modificado = descuentoDAO.modificar(
                    descuento
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
    public boolean finalizarVigencia(
            Long idDescuento,
            LocalDate fechaFin
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idDescuento
            );

            if (fechaFin == null) {
                throw new IllegalArgumentException(
                        "La fecha de finalizacion es obligatoria."
                );
            }

            boolean finalizado = descuentoDAO.finalizarVigencia(
                    idDescuento,
                    fechaFin
            );

            if (finalizado) {
                mensaje = "Vigencia finalizada correctamente.";
                return true;
            }

            mensaje = "No se pudo finalizar la vigencia.";
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
            Long idDescuento
    ) throws SQLException {

        validarClavePrimaria(
                idDescuento
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = descuentoDAO.eliminarDefinitivamente(
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
                        "No se puede eliminar porque existen registros relacionados. Finalice la vigencia del descuento.",
                        e
                );
            }

            throw e;
        }
    }
    public Descuento buscar(
            Long idDescuento
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idDescuento
            );

            return descuentoDAO.buscar(
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
    public List<Descuento> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return descuentoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarDescuento(
            Descuento descuento
    ) {

        if (descuento == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idDescuento
    ) {

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
