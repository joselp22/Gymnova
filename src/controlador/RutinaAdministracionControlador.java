package controlador;

import dao.RutinaAdministracionDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.RutinaDetalleAdministrador;
import modelo.RutinaResumenAdministrador;
import utilidades.NavegacionRol;
import utilidades.SesionUsuario;

/**
 * Controlador de consulta administrativa de rutinas.
 * No crea ni modifica rutinas: esa responsabilidad pertenece al Entrenador.
 */
public class RutinaAdministracionControlador {

    private final RutinaAdministracionDAO dao = new RutinaAdministracionDAO();
    private String mensaje = "";

    public List<RutinaResumenAdministrador> listar(String criterio) {
        mensaje = "";
        if (!esAdministrador()) {
            mensaje = "Solo el Administrador puede consultar el consolidado de rutinas.";
            return new ArrayList<>();
        }
        try {
            return dao.listar(criterio);
        } catch (SQLException e) {
            mensaje = "No se pudieron consultar las rutinas: " + e.getMessage();
            return new ArrayList<>();
        }
    }

    public List<RutinaDetalleAdministrador> listarDetalle(Long idRutina) {
        mensaje = "";
        if (!esAdministrador()) {
            mensaje = "Solo el Administrador puede consultar el detalle de las rutinas.";
            return new ArrayList<>();
        }
        if (idRutina == null) {
            mensaje = "Seleccione una rutina.";
            return new ArrayList<>();
        }
        try {
            return dao.listarDetalle(idRutina);
        } catch (SQLException e) {
            mensaje = "No se pudo consultar el detalle: " + e.getMessage();
            return new ArrayList<>();
        }
    }

    public String getMensaje() {
        return mensaje;
    }

    private boolean esAdministrador() {
        return SesionUsuario.haySesionActiva()
                && "ADMINISTRADOR".equals(
                        NavegacionRol.normalizarRol(
                                SesionUsuario.getUsuarioActual().getNombreRol()
                        )
                );
    }
}
