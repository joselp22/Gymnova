package controlador;

import dao.EjercicioRecursoMultimediaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.EjercicioRecursoMultimedia;
import utilidades.SesionUsuario;

/**
 * Controlador para ejercicio_recurso_multimedia.
 *
 * @author Usuario
 */
public class EjercicioRecursoMultimediaControlador {

    private final EjercicioRecursoMultimediaDAO ejercicioRecursoMultimediaDAO;
    private String mensaje;

    public EjercicioRecursoMultimediaControlador() {

        ejercicioRecursoMultimediaDAO = new EjercicioRecursoMultimediaDAO();
        mensaje = "";
    }


    public boolean registrar(
            EjercicioRecursoMultimedia ejercicioRecursoMultimedia
    ) {

        mensaje = "";

        try {
            validarEjercicioRecursoMultimedia(
                    ejercicioRecursoMultimedia
            );

            boolean guardado = ejercicioRecursoMultimediaDAO.guardar(
                    ejercicioRecursoMultimedia
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
            EjercicioRecursoMultimedia ejercicioRecursoMultimedia
    ) {

        mensaje = "";

        try {

            validarEjercicioRecursoMultimedia(
                    ejercicioRecursoMultimedia
            );

            EjercicioRecursoMultimedia guardado = ejercicioRecursoMultimediaDAO.buscar(
                    ejercicioRecursoMultimedia.getRecursoMultimedia(), ejercicioRecursoMultimedia.getIdEjercicio()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = ejercicioRecursoMultimediaDAO.modificar(
                    ejercicioRecursoMultimedia
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
            String recursoMultimedia,
            Long idEjercicio
    ) throws SQLException {

        validarClavePrimaria(
                recursoMultimedia,
                            idEjercicio
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = ejercicioRecursoMultimediaDAO.eliminarDefinitivamente(
                    recursoMultimedia,
                            idEjercicio
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
    public EjercicioRecursoMultimedia buscar(
            String recursoMultimedia,
            Long idEjercicio
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    recursoMultimedia,
                            idEjercicio
            );

            return ejercicioRecursoMultimediaDAO.buscar(
                    recursoMultimedia,
                            idEjercicio
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
    public List<EjercicioRecursoMultimedia> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return ejercicioRecursoMultimediaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<EjercicioRecursoMultimedia> listarPorIdEjercicio(
            Long idEjercicio
    ) {

        mensaje = "";

        if (idEjercicio == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return ejercicioRecursoMultimediaDAO.listarPorIdEjercicio(
                    idEjercicio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarEjercicioRecursoMultimedia(
            EjercicioRecursoMultimedia ejercicioRecursoMultimedia
    ) {

        if (ejercicioRecursoMultimedia == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            String recursoMultimedia,
            Long idEjercicio
    ) {

        if (recursoMultimedia == null || recursoMultimedia.isBlank()) {
            throw new IllegalArgumentException(
                    "Seleccione un registro."
            );
        }
        if (idEjercicio == null) {
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
