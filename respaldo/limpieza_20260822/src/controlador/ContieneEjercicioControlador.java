package controlador;

import dao.ContieneEjercicioDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.ContieneEjercicio;
import utilidades.SesionUsuario;

/**
 * Controlador para contiene_ejercicio.
 *
 * @author Usuario
 */
public class ContieneEjercicioControlador {

    private final ContieneEjercicioDAO contieneEjercicioDAO;
    private String mensaje;

    public ContieneEjercicioControlador() {

        contieneEjercicioDAO = new ContieneEjercicioDAO();
        mensaje = "";
    }


    public boolean registrar(
            ContieneEjercicio contieneEjercicio
    ) {

        mensaje = "";

        try {
            validarContieneEjercicio(
                    contieneEjercicio
            );

            boolean guardado = contieneEjercicioDAO.guardar(
                    contieneEjercicio
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
            ContieneEjercicio contieneEjercicio
    ) {

        mensaje = "";

        try {

            validarContieneEjercicio(
                    contieneEjercicio
            );

            ContieneEjercicio guardado = contieneEjercicioDAO.buscar(
                    contieneEjercicio.getIdRutinaEjercicio()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = contieneEjercicioDAO.modificar(
                    contieneEjercicio
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
            Long idRutinaEjercicio
    ) throws SQLException {

        validarClavePrimaria(
                idRutinaEjercicio
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = contieneEjercicioDAO.eliminarDefinitivamente(
                    idRutinaEjercicio
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
    public ContieneEjercicio buscar(
            Long idRutinaEjercicio
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idRutinaEjercicio
            );

            return contieneEjercicioDAO.buscar(
                    idRutinaEjercicio
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
    public List<ContieneEjercicio> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return contieneEjercicioDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ContieneEjercicio> listarPorIdRutina(
            Long idRutina
    ) {

        mensaje = "";

        if (idRutina == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return contieneEjercicioDAO.listarPorIdRutina(
                    idRutina
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ContieneEjercicio> listarPorIdEjercicio(
            Long idEjercicio
    ) {

        mensaje = "";

        if (idEjercicio == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return contieneEjercicioDAO.listarPorIdEjercicio(
                    idEjercicio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarContieneEjercicio(
            ContieneEjercicio contieneEjercicio
    ) {

        if (contieneEjercicio == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idRutinaEjercicio
    ) {

        if (idRutinaEjercicio == null) {
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
