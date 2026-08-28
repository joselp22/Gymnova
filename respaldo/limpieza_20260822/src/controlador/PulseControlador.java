package controlador;

import dao.PulseDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.AlertaPulse;

/** Fachada de aplicacion para el indicador preventivo GYMNOVA Pulse. */
public class PulseControlador {

    private final PulseDAO dao = new PulseDAO();
    private String mensaje = "";

    public List<AlertaPulse> listar() {
        try {
            mensaje = "Indicadores Pulse actualizados.";
            return dao.listar();
        } catch (SQLException ex) {
            mensaje = "No fue posible calcular Pulse: " + ex.getMessage();
            return new ArrayList<>();
        }
    }

    public String getMensaje() { return mensaje; }
}
