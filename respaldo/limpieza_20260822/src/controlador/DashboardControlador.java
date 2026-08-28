package controlador;

import dao.DashboardDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador de consultas para dashboard y reportes.
 *
 * @author Usuario
 */
public class DashboardControlador {

    private final DashboardDAO dashboardDAO;
    private String mensaje;

    public DashboardControlador() {

        dashboardDAO = new DashboardDAO();
        mensaje = "";
    }

    public Integer contarClientesActivos() {

        mensaje = "";

        try {
            return dashboardDAO.contarClientesActivos();
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return 0;
        }
    }

    public Integer contarMembresiasActivas() {

        mensaje = "";

        try {
            return dashboardDAO.contarMembresiasActivas();
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return 0;
        }
    }

    public BigDecimal obtenerPagosDelMes() {

        mensaje = "";

        try {
            return dashboardDAO.obtenerPagosDelMes();
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return BigDecimal.ZERO;
        }
    }

    public Integer contarRutinasAsignadas() {

        mensaje = "";

        try {
            return dashboardDAO.contarRutinasAsignadas();
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return 0;
        }
    }

    public List<Map<String, Object>> listarUltimosClientesRegistrados(
            Integer limite
    ) {

        mensaje = "";

        try {
            return dashboardDAO.listarUltimosClientesRegistrados(
                    limite
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> listarMembresiasProximasAVencer(
            Integer dias
    ) {

        mensaje = "";

        try {
            return dashboardDAO.listarMembresiasProximasAVencer(
                    dias
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> listarIngresosPendientes() {

        mensaje = "";

        try {
            return dashboardDAO.listarIngresosPendientes();
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> obtenerActividadSemanal() {

        mensaje = "";

        try {
            return dashboardDAO.obtenerActividadSemanal();
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> obtenerActividadSemanalPorRol(
            String nombreRol,
            Long idPersona
    ) {
        mensaje = "";
        try {
            return dashboardDAO.obtenerActividadSemanalPorRol(
                    nombreRol, idPersona);
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    public Map<String, Object> obtenerResumenGeneral() {

        mensaje = "";

        try {
            return dashboardDAO.obtenerResumenGeneral();
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new HashMap<>();
        }
    }

    public Map<String, Object> obtenerResumenPorRol(
            String nombreRol,
            Long idPersona
    ) {

        mensaje = "";

        try {
            return dashboardDAO.obtenerResumenPorRol(
                    nombreRol,
                    idPersona
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new HashMap<>();
        }
    }

    public List<Map<String, Object>> listarActividadPorRol(
            String nombreRol,
            Long idPersona,
            Integer limite
    ) {

        mensaje = "";

        try {
            return dashboardDAO.listarActividadPorRol(
                    nombreRol,
                    idPersona,
                    limite
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    private String traducirErrorBaseDatos(
            SQLException e
    ) {

        return "Error de base de datos: " + e.getMessage();
    }

    public String getMensaje() {
        return mensaje;
    }
}
