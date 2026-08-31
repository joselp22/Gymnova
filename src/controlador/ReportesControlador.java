package controlador;

import dao.ReportesDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import modelo.ReporteResultado;

/** Coordinación y validaciones del módulo administrativo de reportes. */
public class ReportesControlador {
    public static final String[] TIPOS = {
        "Ingresos y facturación", "Clientes", "Membresías próximas a vencer",
        "Asistencia", "Clases grupales y reservas", "Rutinas asignadas",
        "Progreso de entrenamiento", "Progreso físico / nutricional"
    };
    private final ReportesDAO dao = new ReportesDAO();

    public Map<String,String> cargarKpis() throws SQLException { return dao.cargarKpis(); }

    public ReporteResultado generar(String tipo, LocalDate desde, LocalDate hasta,
            String filtro, int dias) throws SQLException {
        if (tipo == null || tipo.isBlank()) throw new IllegalArgumentException("Seleccione un tipo de reporte.");
        if (!"Membresías próximas a vencer".equals(tipo)) {
            if (desde == null || hasta == null) throw new IllegalArgumentException("Ingrese las fechas del reporte.");
            if (hasta.isBefore(desde)) throw new IllegalArgumentException("La fecha hasta no puede ser anterior a la fecha desde.");
        }
        return switch (tipo) {
            case "Ingresos y facturación" -> dao.ingresos(desde,hasta,filtro);
            case "Clientes" -> dao.clientes(desde,hasta,filtro);
            case "Membresías próximas a vencer" -> dao.membresiasPorVencer(dias,filtro);
            case "Asistencia" -> dao.asistencia(desde,hasta,filtro);
            case "Clases grupales y reservas" -> dao.clases(desde,hasta,filtro);
            case "Rutinas asignadas" -> dao.rutinas(desde,hasta,filtro);
            case "Progreso de entrenamiento" -> dao.progreso(desde,hasta,filtro);
            case "Progreso físico / nutricional" -> dao.progresoFisico(desde,hasta,filtro);
            default -> throw new IllegalArgumentException("Tipo de reporte no reconocido.");
        };
    }
}
