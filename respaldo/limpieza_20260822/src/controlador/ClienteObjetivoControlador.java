package controlador;

import dao.ClienteObjetivoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.ClienteObjetivo;
import utilidades.SesionUsuario;

/**
 * Controlador para cliente_objetivo.
 *
 * @author Usuario
 */
public class ClienteObjetivoControlador {

    private final ClienteObjetivoDAO clienteObjetivoDAO;
    private String mensaje;

    public ClienteObjetivoControlador() {

        clienteObjetivoDAO = new ClienteObjetivoDAO();
        mensaje = "";
    }


    public boolean registrar(
            ClienteObjetivo clienteObjetivo
    ) {

        mensaje = "";

        try {
            validarClienteObjetivo(
                    clienteObjetivo
            );

            boolean guardado = clienteObjetivoDAO.guardar(
                    clienteObjetivo
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
            ClienteObjetivo clienteObjetivo
    ) {

        mensaje = "";

        try {

            validarClienteObjetivo(
                    clienteObjetivo
            );

            ClienteObjetivo guardado = clienteObjetivoDAO.buscar(
                    clienteObjetivo.getIdClienteObjetivo()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = clienteObjetivoDAO.modificar(
                    clienteObjetivo
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
            Long idClienteObjetivo
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idClienteObjetivo
            );

            boolean desactivado = clienteObjetivoDAO.desactivar(
                    idClienteObjetivo
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
            Long idClienteObjetivo
    ) throws SQLException {

        validarClavePrimaria(
                idClienteObjetivo
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = clienteObjetivoDAO.eliminarDefinitivamente(
                    idClienteObjetivo
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
    public ClienteObjetivo buscar(
            Long idClienteObjetivo
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idClienteObjetivo
            );

            return clienteObjetivoDAO.buscar(
                    idClienteObjetivo
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
    public List<ClienteObjetivo> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return clienteObjetivoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ClienteObjetivo> listarPorIdCliente(
            Long idCliente
    ) {

        mensaje = "";

        if (idCliente == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return clienteObjetivoDAO.listarPorIdCliente(
                    idCliente
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ClienteObjetivo> listarPorIdObjetivo(
            Long idObjetivo
    ) {

        mensaje = "";

        if (idObjetivo == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return clienteObjetivoDAO.listarPorIdObjetivo(
                    idObjetivo
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarClienteObjetivo(
            ClienteObjetivo clienteObjetivo
    ) {

        if (clienteObjetivo == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idClienteObjetivo
    ) {

        if (idClienteObjetivo == null) {
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
