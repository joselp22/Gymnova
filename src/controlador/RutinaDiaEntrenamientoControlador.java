package controlador;

import dao.RutinaDiaEntrenamientoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.RutinaDiaEntrenamiento;
import utilidades.SesionUsuario;

/**
 * Controlador para rutina_dia_entrenamiento.
 *
 * @author Usuario
 */
public class RutinaDiaEntrenamientoControlador {

    private final RutinaDiaEntrenamientoDAO rutinaDiaEntrenamientoDAO;
    private String mensaje;

    public RutinaDiaEntrenamientoControlador() {

        rutinaDiaEntrenamientoDAO = new RutinaDiaEntrenamientoDAO();
        mensaje = "";
    }


    public boolean registrar(
            RutinaDiaEntrenamiento rutinaDiaEntrenamiento
    ) {

        mensaje = "";

        try {
            validarRutinaDiaEntrenamiento(
                    rutinaDiaEntrenamiento
            );

            boolean guardado = rutinaDiaEntrenamientoDAO.guardar(
                    rutinaDiaEntrenamiento
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
            RutinaDiaEntrenamiento rutinaDiaEntrenamiento
    ) {

        mensaje = "";

        try {

            validarRutinaDiaEntrenamiento(
                    rutinaDiaEntrenamiento
            );

            RutinaDiaEntrenamiento guardado = rutinaDiaEntrenamientoDAO.buscar(
                    rutinaDiaEntrenamiento.getDiaEntrenamiento(), rutinaDiaEntrenamiento.getIdRutina()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = rutinaDiaEntrenamientoDAO.modificar(
                    rutinaDiaEntrenamiento
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
    public void eliminarDefinitivamente(
            String diaEntrenamiento,
            Long idRutina
    ) throws SQLException {

        validarClavePrimaria(
                diaEntrenamiento,
                            idRutina
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = rutinaDiaEntrenamientoDAO.eliminarDefinitivamente(
                    diaEntrenamiento,
                            idRutina
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
    public RutinaDiaEntrenamiento buscar(
            String diaEntrenamiento,
            Long idRutina
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    diaEntrenamiento,
                            idRutina
            );

            return rutinaDiaEntrenamientoDAO.buscar(
                    diaEntrenamiento,
                            idRutina
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
    public List<RutinaDiaEntrenamiento> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return rutinaDiaEntrenamientoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<RutinaDiaEntrenamiento> listarPorIdRutina(
            Long idRutina
    ) {

        mensaje = "";

        if (idRutina == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return rutinaDiaEntrenamientoDAO.listarPorIdRutina(
                    idRutina
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarRutinaDiaEntrenamiento(
            RutinaDiaEntrenamiento rutinaDiaEntrenamiento
    ) {

        if (rutinaDiaEntrenamiento == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            String diaEntrenamiento,
            Long idRutina
    ) {

        if (diaEntrenamiento == null || diaEntrenamiento.isBlank()) {
            throw new IllegalArgumentException(
                    "Seleccione un registro."
            );
        }
        if (idRutina == null) {
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
