package controlador;

import dao.ReservaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Reserva;
import utilidades.SesionUsuario;

/**
 * Controlador para reserva.
 *
 * @author Usuario
 */
public class ReservaControlador {

    private final ReservaDAO reservaDAO;
    private String mensaje;

    public ReservaControlador() {

        reservaDAO = new ReservaDAO();
        mensaje = "";
    }


    public boolean registrar(
            Reserva reserva
    ) {

        mensaje = "";

        try {
            if (reserva != null) {
                reserva.setCodigoReserva(
                        null
                );
            }

            validarReserva(
                    reserva
            );

            boolean guardado = reservaDAO.guardar(
                    reserva
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
            Reserva reserva
    ) {

        mensaje = "";

        try {

            validarReserva(
                    reserva
            );

            Reserva guardado = reservaDAO.buscar(
                    reserva.getIdReserva()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            reserva.setCodigoReserva(guardado.getCodigoReserva());
            boolean modificado = reservaDAO.modificar(
                    reserva
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
            Long idReserva
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idReserva
            );

            boolean desactivado = reservaDAO.desactivar(
                    idReserva
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
            Long idReserva
    ) throws SQLException {

        validarClavePrimaria(
                idReserva
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = reservaDAO.eliminarDefinitivamente(
                    idReserva
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
    public Reserva buscar(
            Long idReserva
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idReserva
            );

            return reservaDAO.buscar(
                    idReserva
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
    public List<Reserva> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return reservaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Reserva> listarPorIdCliente(
            Long idCliente
    ) {

        mensaje = "";

        if (idCliente == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return reservaDAO.listarPorIdCliente(
                    idCliente
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Reserva> listarPorIdClase(
            Long idClase
    ) {

        mensaje = "";

        if (idClase == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return reservaDAO.listarPorIdClase(
                    idClase
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarReserva(
            Reserva reserva
    ) {

        if (reserva == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idReserva
    ) {

        if (idReserva == null) {
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
