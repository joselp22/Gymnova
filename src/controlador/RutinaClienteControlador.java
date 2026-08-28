package controlador;

import dao.RutinaClienteDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RutinaClienteControlador {

    private final RutinaClienteDAO rutinaClienteDAO;
    private String mensaje;

    public RutinaClienteControlador() {
        rutinaClienteDAO = new RutinaClienteDAO();
        mensaje = "";
    }

    public List<Map<String, Object>> listarEjercicios(
            Long idCliente,
            LocalDate fecha
    ) {
        mensaje = "";
        if (idCliente == null) {
            mensaje = "El usuario no tiene una persona asociada.";
            return new ArrayList<>();
        }
        try {
            return rutinaClienteDAO.listarEjercicios(idCliente, fecha);
        } catch (SQLException ex) {
            mensaje = "No fue posible consultar la rutina: " + ex.getMessage();
            return new ArrayList<>();
        }
    }

    public String getMensaje() {
        return mensaje;
    }
}
