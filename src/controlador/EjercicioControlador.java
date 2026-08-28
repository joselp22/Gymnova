package controlador;

import dao.EjercicioDAO;
import dao.EntrenadorDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Ejercicio;
import modelo.Entrenador;
import utilidades.NavegacionRol;
import utilidades.SesionUsuario;

/**
 * Controlador para ejercicio/actividad.
 */
public class EjercicioControlador {

    private final EjercicioDAO ejercicioDAO;
    private final EntrenadorDAO entrenadorDAO;
    private String mensaje;

    public EjercicioControlador() {
        ejercicioDAO = new EjercicioDAO();
        entrenadorDAO = new EntrenadorDAO();
        mensaje = "";
    }

    public boolean registrar(Ejercicio ejercicio) {

        mensaje = "";

        try {
            validarEjercicio(ejercicio);

            if (esEntrenador()) {
                Long idEntrenador = SesionUsuario
                        .getUsuarioActual()
                        .getIdPersona();

                validarPerfilEntrenadorActivo(idEntrenador);
                ejercicio.setIdEntrenador(idEntrenador);
            }

            boolean guardado = ejercicioDAO.guardar(ejercicio);

            if (guardado) {
                mensaje = "Actividad guardada correctamente.";
                return true;
            }

            mensaje = "No se pudo guardar la actividad.";
            return false;

        } catch (IllegalArgumentException e) {
            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return false;
        }
    }

    public boolean modificar(Ejercicio ejercicio) {

        mensaje = "";

        try {
            validarEjercicio(ejercicio);
            validarClavePrimaria(ejercicio.getIdEjercicio());

            Ejercicio guardado = ejercicioDAO.buscar(
                    ejercicio.getIdEjercicio()
            );

            if (guardado == null) {
                mensaje = "Seleccione una actividad existente.";
                return false;
            }

            if (esEntrenador()) {
                Long idEntrenador = SesionUsuario
                        .getUsuarioActual().getIdPersona();

                validarPerfilEntrenadorActivo(idEntrenador);

                if (guardado.getIdEntrenador() == null
                        || !idEntrenador.equals(
                                guardado.getIdEntrenador())) {
                    mensaje = "Solo puedes modificar actividades creadas por ti.";
                    return false;
                }

                ejercicio.setIdEntrenador(idEntrenador);
            } else if (ejercicio.getIdEntrenador() == null) {
                // Al modificar desde administración no se pierde el propietario.
                ejercicio.setIdEntrenador(guardado.getIdEntrenador());
            }

            boolean modificado = ejercicioDAO.modificar(ejercicio);

            if (modificado) {
                mensaje = "Actividad modificada correctamente.";
                return true;
            }

            mensaje = "No se pudo modificar la actividad.";
            return false;

        } catch (IllegalArgumentException e) {
            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return false;
        }
    }

    public void eliminarDefinitivamente(Long idEjercicio)
            throws SQLException {

        validarClavePrimaria(idEjercicio);

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {
            boolean eliminado =
                    ejercicioDAO.eliminarDefinitivamente(idEjercicio);

            if (!eliminado) {
                throw new IllegalArgumentException(
                        "La actividad ya no existe."
                );
            }

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState())) {
                throw new IllegalStateException(
                        "No se puede eliminar porque la actividad ya forma parte de una rutina.",
                        e
                );
            }

            throw e;
        }
    }

    public Ejercicio buscar(Long idEjercicio) {

        mensaje = "";

        try {
            validarClavePrimaria(idEjercicio);
            return ejercicioDAO.buscar(idEjercicio);

        } catch (IllegalArgumentException e) {
            mensaje = e.getMessage();
            return null;

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return null;
        }
    }

    public List<Ejercicio> listar(String criterio) {

        mensaje = "";

        try {
            return ejercicioDAO.listar(criterio);
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    public List<Ejercicio> listarDisponiblesParaEntrenador(
            Long idEntrenador,
            String criterio
    ) {

        mensaje = "";

        if (idEntrenador == null) {
            mensaje = "No se pudo identificar al entrenador.";
            return new ArrayList<>();
        }

        try {
            return ejercicioDAO.listarDisponiblesParaEntrenador(
                    idEntrenador,
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    public List<Ejercicio> listarPropiosEntrenador(
            Long idEntrenador,
            String criterio
    ) {

        mensaje = "";

        if (idEntrenador == null) {
            mensaje = "No se pudo identificar al entrenador.";
            return new ArrayList<>();
        }

        try {
            return ejercicioDAO.listarPropiosEntrenador(
                    idEntrenador,
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    private void validarEjercicio(Ejercicio ejercicio) {

        if (ejercicio == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos de la actividad."
            );
        }

        if (ejercicio.getNombreEjercicio() == null
                || ejercicio.getNombreEjercicio().isBlank()) {
            throw new IllegalArgumentException(
                    "Ingrese el nombre de la actividad."
            );
        }

        if (ejercicio.getTipoEjercicio() == null
                || ejercicio.getTipoEjercicio().isBlank()) {
            throw new IllegalArgumentException(
                    "Ingrese el tipo de actividad."
            );
        }

        if (ejercicio.getNivelDificultad() == null
                || ejercicio.getNivelDificultad().isBlank()) {
            throw new IllegalArgumentException(
                    "Seleccione el nivel de dificultad."
            );
        }
    }

    private void validarClavePrimaria(Long idEjercicio) {
        if (idEjercicio == null) {
            throw new IllegalArgumentException(
                    "Seleccione una actividad."
            );
        }
    }

    private void validarPerfilEntrenadorActivo(Long idPersona)
            throws SQLException {

        if (idPersona == null) {
            throw new IllegalArgumentException(
                    "La sesión no tiene una persona vinculada al entrenador."
            );
        }

        Entrenador entrenador = entrenadorDAO.buscar(idPersona);

        if (entrenador == null) {
            throw new IllegalArgumentException(
                    "Tu cuenta tiene rol Entrenador, pero la persona vinculada "
                    + "no está registrada en Personal como Entrenador. "
                    + "Solicita al Administrador completar primero tu perfil profesional."
            );
        }

        if (!entrenador.isEstadoEntrenador()) {
            throw new IllegalArgumentException(
                    "Tu perfil de Entrenador está inactivo."
            );
        }
    }

    private boolean esAdministrador() {
        return SesionUsuario.haySesionActiva()
                && "ADMINISTRADOR".equals(
                        NavegacionRol.normalizarRol(
                                SesionUsuario.getUsuarioActual().getNombreRol()
                        )
                );
    }

    private boolean esEntrenador() {
        return SesionUsuario.haySesionActiva()
                && "ENTRENADOR".equals(
                        NavegacionRol.normalizarRol(
                                SesionUsuario.getUsuarioActual().getNombreRol()
                        )
                );
    }

    private String traducirErrorBaseDatos(SQLException e) {

        if ("23503".equals(e.getSQLState())) {
            String detalle = e.getMessage() == null ? "" : e.getMessage();
            if (detalle.contains("fk_ejercicio_entrenador")
                    || detalle.contains("id_entrenador")) {
                return "No se pudo guardar la actividad porque el usuario no tiene "
                        + "un perfil de Entrenador válido asociado.";
            }
            return "No se pudo realizar la operación por una relación inválida.";
        }

        if ("23001".equals(e.getSQLState())) {
            return "No se puede realizar la operación porque existen registros relacionados.";
        }

        if ("23505".equals(e.getSQLState())) {
            return "Ya existe una actividad con ese nombre.";
        }

        if ("23514".equals(e.getSQLState())) {
            return "Los datos de la actividad no cumplen las reglas de validación.";
        }

        if ("42703".equals(e.getSQLState())) {
            return "La base de datos aún no tiene la migración de actividades por entrenador.";
        }

        return "Error de base de datos: " + e.getMessage();
    }

    public String getMensaje() {
        return mensaje;
    }
}
