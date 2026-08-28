package controlador;

import dao.ProgresoRutinaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.ProgresoRutina;
import utilidades.SesionUsuario;

/**
 * Controlador para progreso_rutina.
 *
 * @author Usuario
 */
public class ProgresoRutinaControlador {

    private final ProgresoRutinaDAO progresoRutinaDAO;
    private String mensaje;

    public ProgresoRutinaControlador() {

        progresoRutinaDAO = new ProgresoRutinaDAO();
        mensaje = "";
    }


    public boolean registrar(
            ProgresoRutina progresoRutina
    ) {

        mensaje = "";

        try {
            validarProgresoRutina(
                    progresoRutina
            );

            boolean guardado = progresoRutinaDAO.guardar(
                    progresoRutina
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
            ProgresoRutina progresoRutina
    ) {

        mensaje = "";

        try {

            validarProgresoRutina(
                    progresoRutina
            );

            ProgresoRutina guardado = progresoRutinaDAO.buscar(
                    progresoRutina.getIdProgreso()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = progresoRutinaDAO.modificar(
                    progresoRutina
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
            Long idProgreso
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idProgreso
            );

            boolean desactivado = progresoRutinaDAO.desactivar(
                    idProgreso
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
            Long idProgreso
    ) throws SQLException {

        validarClavePrimaria(
                idProgreso
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = progresoRutinaDAO.eliminarDefinitivamente(
                    idProgreso
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
    public ProgresoRutina buscar(
            Long idProgreso
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idProgreso
            );

            return progresoRutinaDAO.buscar(
                    idProgreso
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
    public List<ProgresoRutina> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return progresoRutinaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ProgresoRutina> listarPorIdCliente(
            Long idCliente
    ) {

        mensaje = "";

        if (idCliente == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return progresoRutinaDAO.listarPorIdCliente(
                    idCliente
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ProgresoRutina> listarPorIdRutina(
            Long idRutina
    ) {

        mensaje = "";

        if (idRutina == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return progresoRutinaDAO.listarPorIdRutina(
                    idRutina
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarProgresoRutina(
            ProgresoRutina progresoRutina
    ) {

        if (progresoRutina == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }

        if (progresoRutina.getFechaRegistro() == null
                || progresoRutina.getFechaRegistro().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("La fecha del progreso no puede estar vacia ni ser futura.");
        }
        if (progresoRutina.getSesionesPlanificadas() == null
                || progresoRutina.getSesionesPlanificadas() < 0
                || progresoRutina.getSesionesCompletadas() == null
                || progresoRutina.getSesionesCompletadas() < 0
                || progresoRutina.getSesionesCompletadas() > progresoRutina.getSesionesPlanificadas()) {
            throw new IllegalArgumentException("Las sesiones completadas deben estar entre cero y las planificadas.");
        }
        if (progresoRutina.getPesoCorporal() != null
                && progresoRutina.getPesoCorporal().signum() <= 0) {
            throw new IllegalArgumentException("El peso corporal debe ser mayor que cero.");
        }
        if (progresoRutina.getNivelEsfuerzo() != null
                && !progresoRutina.getNivelEsfuerzo().isBlank()
                && !java.util.Set.of("BAJO", "MEDIO", "ALTO").contains(texto(progresoRutina.getNivelEsfuerzo()))) {
            throw new IllegalArgumentException("Seleccione un nivel de esfuerzo valido.");
        }
        if (!java.util.Set.of("REGISTRADO", "REVISADO", "ANULADO")
                .contains(texto(progresoRutina.getEstadoProgreso()))) {
            throw new IllegalArgumentException("Seleccione un estado de progreso valido.");
        }
        if (progresoRutina.getIdCliente() == null || progresoRutina.getIdRutina() == null) {
            throw new IllegalArgumentException("Seleccione el cliente y la rutina.");
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void validarClavePrimaria(
            Long idProgreso
    ) {

        if (idProgreso == null) {
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
