package controlador;

import dao.PlanNutricionalDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.PlanNutricional;
import utilidades.SesionUsuario;

/**
 * Controlador para plan_nutricional.
 *
 * @author Usuario
 */
public class PlanNutricionalControlador {

    private final PlanNutricionalDAO planNutricionalDAO;
    private String mensaje;

    public PlanNutricionalControlador() {

        planNutricionalDAO = new PlanNutricionalDAO();
        mensaje = "";
    }


    public boolean registrar(
            PlanNutricional planNutricional
    ) {

        mensaje = "";

        try {
            if (planNutricional != null) {
                planNutricional.setCodigoPlan(
                        null
                );
            }

            validarPlanNutricional(
                    planNutricional
            );

            boolean guardado = planNutricionalDAO.guardar(
                    planNutricional
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
            PlanNutricional planNutricional
    ) {

        mensaje = "";

        try {

            validarPlanNutricional(
                    planNutricional
            );

            PlanNutricional guardado = planNutricionalDAO.buscar(
                    planNutricional.getIdPlanNutricional()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            planNutricional.setCodigoPlan(guardado.getCodigoPlan());
            boolean modificado = planNutricionalDAO.modificar(
                    planNutricional
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
            Long idPlanNutricional
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idPlanNutricional
            );

            boolean desactivado = planNutricionalDAO.desactivar(
                    idPlanNutricional
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
            Long idPlanNutricional
    ) throws SQLException {

        validarClavePrimaria(
                idPlanNutricional
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = planNutricionalDAO.eliminarDefinitivamente(
                    idPlanNutricional
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
    public PlanNutricional buscar(
            Long idPlanNutricional
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idPlanNutricional
            );

            return planNutricionalDAO.buscar(
                    idPlanNutricional
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
    public List<PlanNutricional> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return planNutricionalDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<PlanNutricional> listarPorIdCliente(
            Long idCliente
    ) {

        mensaje = "";

        if (idCliente == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return planNutricionalDAO.listarPorIdCliente(
                    idCliente
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<PlanNutricional> listarPorIdNutricionista(
            Long idNutricionista
    ) {

        mensaje = "";

        if (idNutricionista == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return planNutricionalDAO.listarPorIdNutricionista(
                    idNutricionista
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarPlanNutricional(
            PlanNutricional planNutricional
    ) {

        if (planNutricional == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }

        if (texto(planNutricional.getNombrePlan()).isEmpty()) {
            throw new IllegalArgumentException("Ingrese el nombre del plan nutricional.");
        }
        if (planNutricional.getFechaCreacion() == null
                || planNutricional.getFechaCreacion().isAfter(java.time.LocalDate.now())
                || planNutricional.getFechaInicio() == null) {
            throw new IllegalArgumentException("Ingrese fechas validas de creacion e inicio.");
        }
        if (planNutricional.getFechaFin() != null
                && planNutricional.getFechaFin().isBefore(planNutricional.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha final no puede ser anterior al inicio.");
        }
        if (planNutricional.getCaloriasObjetivo() != null && planNutricional.getCaloriasObjetivo() <= 0) {
            throw new IllegalArgumentException("Las calorias objetivo deben ser mayores que cero.");
        }
        if ((planNutricional.getProteinasObjetivoG() != null && planNutricional.getProteinasObjetivoG().signum() < 0)
                || (planNutricional.getCarbohidratosObjetivoG() != null && planNutricional.getCarbohidratosObjetivoG().signum() < 0)) {
            throw new IllegalArgumentException("Los macronutrientes no pueden tener valores negativos.");
        }
        if (!java.util.Set.of("ACTIVO", "FINALIZADO", "SUSPENDIDO", "CANCELADO")
                .contains(texto(planNutricional.getEstadoPlan()))) {
            throw new IllegalArgumentException("Seleccione un estado de plan valido.");
        }
        if (planNutricional.getIdCliente() == null || planNutricional.getIdNutricionista() == null) {
            throw new IllegalArgumentException("Seleccione el cliente y el nutricionista.");
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void validarClavePrimaria(
            Long idPlanNutricional
    ) {

        if (idPlanNutricional == null) {
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
