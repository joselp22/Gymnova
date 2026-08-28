package controlador;

import dao.ClaseGrupalDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.ClaseGrupal;
import utilidades.SesionUsuario;

/**
 * Controlador para clase_grupal.
 *
 * @author Usuario
 */
public class ClaseGrupalControlador {

    private final ClaseGrupalDAO claseGrupalDAO;
    private String mensaje;

    public ClaseGrupalControlador() {

        claseGrupalDAO = new ClaseGrupalDAO();
        mensaje = "";
    }


    public boolean registrar(
            ClaseGrupal claseGrupal
    ) {

        mensaje = "";

        try {
            validarClaseGrupal(
                    claseGrupal
            );

            boolean guardado = claseGrupalDAO.guardar(
                    claseGrupal
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
            ClaseGrupal claseGrupal
    ) {

        mensaje = "";

        try {

            validarClaseGrupal(
                    claseGrupal
            );

            ClaseGrupal guardado = claseGrupalDAO.buscar(
                    claseGrupal.getIdClase()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = claseGrupalDAO.modificar(
                    claseGrupal
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
            Long idClase
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idClase
            );

            boolean desactivado = claseGrupalDAO.desactivar(
                    idClase
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
            Long idClase
    ) throws SQLException {

        validarClavePrimaria(
                idClase
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = claseGrupalDAO.eliminarDefinitivamente(
                    idClase
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
    public ClaseGrupal buscar(
            Long idClase
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idClase
            );

            return claseGrupalDAO.buscar(
                    idClase
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
    public List<ClaseGrupal> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return claseGrupalDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarClaseGrupal(
            ClaseGrupal claseGrupal
    ) {

        if (claseGrupal == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }

        if (claseGrupal.getNombreClase() == null
                || claseGrupal.getNombreClase().isBlank()) {
            throw new IllegalArgumentException(
                    "Ingrese el nombre de la clase."
            );
        }

        if (claseGrupal.getDuracionBaseMinutos() == null
                || claseGrupal.getDuracionBaseMinutos() <= 0) {
            throw new IllegalArgumentException(
                    "La duracion debe ser mayor que cero."
            );
        }

        if (claseGrupal.getIdEntrenador() == null) {
            throw new IllegalArgumentException(
                    "Seleccione el entrenador responsable."
            );
        }

        if (claseGrupal.getCupoMaximo() == null
                || claseGrupal.getCupoMaximo() <= 0) {
            throw new IllegalArgumentException(
                    "El cupo maximo debe ser mayor que cero."
            );
        }

        if (claseGrupal.getFechaHora() == null) {
            throw new IllegalArgumentException(
                    "Ingrese la fecha y hora de la clase."
            );
        }
    }

    private void validarClavePrimaria(
            Long idClase
    ) {

        if (idClase == null) {
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
