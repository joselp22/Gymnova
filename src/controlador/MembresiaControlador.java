package controlador;

import dao.MembresiaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Membresia;
import utilidades.SesionUsuario;

/**
 * Controlador para membresia.
 *
 * @author Usuario
 */
public class MembresiaControlador {

    private final MembresiaDAO membresiaDAO;
    private String mensaje;

    public MembresiaControlador() {

        membresiaDAO = new MembresiaDAO();
        mensaje = "";
    }


    public boolean registrar(
            Membresia membresia
    ) {

        mensaje = "";

        try {
            if (membresia != null) {
                membresia.setNumeroMembresia(null);
            }
            validarMembresia(
                    membresia
            );

            boolean guardado = membresiaDAO.guardar(
                    membresia
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
            Membresia membresia
    ) {

        mensaje = "";

        try {

            validarMembresia(
                    membresia
            );

            Membresia guardado = membresiaDAO.buscar(
                    membresia.getIdMembresia()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            if (!java.util.Objects.equals(
                    guardado.getNumeroMembresia(),
                    membresia.getNumeroMembresia())) {
                mensaje = "El numero de membresia no se puede modificar.";
                return false;
            }
            membresia.setNumeroMembresia(guardado.getNumeroMembresia());

            boolean modificado = membresiaDAO.modificar(
                    membresia
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
            Long idMembresia
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idMembresia
            );

            boolean desactivado = membresiaDAO.desactivar(
                    idMembresia
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
            Long idMembresia
    ) throws SQLException {

        validarClavePrimaria(
                idMembresia
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = membresiaDAO.eliminarDefinitivamente(
                    idMembresia
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
    public Membresia buscar(
            Long idMembresia
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idMembresia
            );

            return membresiaDAO.buscar(
                    idMembresia
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
    public List<Membresia> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return membresiaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Membresia> listarPorIdCliente(
            Long idCliente
    ) {

        mensaje = "";

        if (idCliente == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return membresiaDAO.listarPorIdCliente(
                    idCliente
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Membresia> listarPorIdTipoMembresia(
            Long idTipoMembresia
    ) {

        mensaje = "";

        if (idTipoMembresia == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return membresiaDAO.listarPorIdTipoMembresia(
                    idTipoMembresia
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarMembresia(
            Membresia membresia
    ) {

        if (membresia == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }

        if (membresia.getFechaInicio() == null
                || membresia.getFechaFin() == null) {
            throw new IllegalArgumentException("Ingrese las fechas de inicio y fin.");
        }
        if (membresia.getFechaFin().isBefore(membresia.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la fecha inicial.");
        }
        if (membresia.getCostoFinal() == null
                || membresia.getCostoFinal().signum() < 0) {
            throw new IllegalArgumentException("El costo final debe ser un valor valido mayor o igual a cero.");
        }
        if (!java.util.Set.of("ACTIVA", "VENCIDA", "CONGELADA", "CANCELADA", "PENDIENTE")
                .contains(texto(membresia.getEstadoMembresia()))) {
            throw new IllegalArgumentException("Seleccione un estado de membresia valido.");
        }
        if (membresia.getIdCliente() == null || membresia.getIdTipoMembresia() == null) {
            throw new IllegalArgumentException("Seleccione el cliente y el plan de membresia.");
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void validarClavePrimaria(
            Long idMembresia
    ) {

        if (idMembresia == null) {
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
