package controlador;

import dao.CatalogoRelacionDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.OpcionRelacion;
import utilidades.SesionUsuario;

public class CatalogoRelacionControlador {

    private final CatalogoRelacionDAO dao = new CatalogoRelacionDAO();
    private String mensaje = "";

    public boolean esRelacion(String campo) {
        return dao.esRelacion(campo);
    }

    public List<OpcionRelacion> listar(String campo) {
        mensaje = "";
        try {
            return dao.listar(campo);
        } catch (SQLException ex) {
            mensaje = "No fue posible cargar " + campo + ": " + ex.getMessage();
            return new ArrayList<>();
        }
    }

    public List<OpcionRelacion> listarParaSesion(String campo) {
        if (!SesionUsuario.haySesionActiva()) {
            return listar(campo);
        }
        mensaje = "";
        try {
            return dao.listarParaSesion(
                    campo,
                    SesionUsuario.getUsuarioActual().getNombreRol(),
                    SesionUsuario.getUsuarioActual().getIdPersona());
        } catch (SQLException ex) {
            mensaje = "No fue posible cargar " + campo + ": " + ex.getMessage();
            return new ArrayList<>();
        }
    }

    public String getMensaje() {
        return mensaje;
    }
}
