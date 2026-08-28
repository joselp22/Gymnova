package controlador;

import dao.AsignacionRutinaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.AsignacionRutina;
import utilidades.SesionUsuario;

/**
 * Controlador para asignacion_rutina.
 *
 * @author Usuario
 */
public class AsignacionRutinaControlador {

    private final AsignacionRutinaDAO asignacionRutinaDAO;
    private String mensaje;

    public AsignacionRutinaControlador() {

        asignacionRutinaDAO = new AsignacionRutinaDAO();
        mensaje = "";
    }


    public boolean registrar(
            AsignacionRutina asignacionRutina
    ) {

        mensaje = "";

        try {
            validarAsignacionRutina(
                    asignacionRutina
            );

            boolean guardado = asignacionRutinaDAO.guardar(
                    asignacionRutina
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
            AsignacionRutina asignacionRutina
    ) {

        mensaje = "";

        try {

            validarAsignacionRutina(
                    asignacionRutina
            );

            AsignacionRutina guardado = asignacionRutinaDAO.buscar(
                    asignacionRutina.getIdAsignacion()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = asignacionRutinaDAO.modificar(
                    asignacionRutina
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
            Long idAsignacion
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idAsignacion
            );

            boolean desactivado = asignacionRutinaDAO.desactivar(
                    idAsignacion
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
            Long idAsignacion
    ) throws SQLException {

        validarClavePrimaria(
                idAsignacion
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = asignacionRutinaDAO.eliminarDefinitivamente(
                    idAsignacion
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
    public AsignacionRutina buscar(
            Long idAsignacion
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idAsignacion
            );

            return asignacionRutinaDAO.buscar(
                    idAsignacion
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
    public List<AsignacionRutina> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return asignacionRutinaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<AsignacionRutina> listarPorIdCliente(
            Long idCliente
    ) {

        mensaje = "";

        if (idCliente == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return asignacionRutinaDAO.listarPorIdCliente(
                    idCliente
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<AsignacionRutina> listarPorIdRutina(
            Long idRutina
    ) {

        mensaje = "";

        if (idRutina == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return asignacionRutinaDAO.listarPorIdRutina(
                    idRutina
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarAsignacionRutina(
            AsignacionRutina asignacionRutina
    ) {

        if (asignacionRutina == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }

        if (asignacionRutina.getFechaAsignacion() == null
                || asignacionRutina.getFechaInicio() == null) {
            throw new IllegalArgumentException("Ingrese las fechas de asignacion e inicio.");
        }
        if (asignacionRutina.getFechaAsignacion().isAfter(asignacionRutina.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha de asignacion no puede ser posterior al inicio.");
        }
        if (asignacionRutina.getFechaFin() != null
                && asignacionRutina.getFechaFin().isBefore(asignacionRutina.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior al inicio.");
        }
        String estado = texto(asignacionRutina.getEstadoAsignacion());
        if (!java.util.Set.of("PROGRAMADA", "ACTIVA", "FINALIZADA", "PAUSADA", "CANCELADA").contains(estado)) {
            throw new IllegalArgumentException("Seleccione un estado de asignacion valido.");
        }
        if ("FINALIZADA".equals(estado) && texto(asignacionRutina.getMotivoFinalizacion()).isEmpty()) {
            throw new IllegalArgumentException("Indique el motivo de finalizacion.");
        }
        if (asignacionRutina.getIdCliente() == null || asignacionRutina.getIdRutina() == null) {
            throw new IllegalArgumentException("Seleccione el cliente y la rutina.");
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void validarClavePrimaria(
            Long idAsignacion
    ) {

        if (idAsignacion == null) {
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
