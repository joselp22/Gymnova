/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.EntrenadorDAO;
import dao.EntrenadorEspecialidadDAO;
import dao.EspecialidadDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.Entrenador;
import modelo.EntrenadorEspecialidad;
import modelo.Especialidad;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class EntrenadorEspecialidadControlador {

    private final EntrenadorEspecialidadDAO entrenadorEspecialidadDAO;
    private final EntrenadorDAO entrenadorDAO;
    private final EspecialidadDAO especialidadDAO;
    private String mensaje;

    public EntrenadorEspecialidadControlador() {

        entrenadorEspecialidadDAO
                = new EntrenadorEspecialidadDAO();

        entrenadorDAO = new EntrenadorDAO();
        especialidadDAO = new EspecialidadDAO();
        mensaje = "";
    }

    public boolean registrar(
            EntrenadorEspecialidad entrenadorEspecialidad
    ) {

        mensaje = "";

        try {

            normalizarDatos(entrenadorEspecialidad);
            validarEntrenadorEspecialidad(entrenadorEspecialidad);

            Entrenador entrenador
                    = entrenadorDAO.buscar(
                            entrenadorEspecialidad
                                    .getIdEntrenador()
                    );

            if (entrenador == null) {

                mensaje
                        = "El entrenador seleccionado "
                        + "no existe.";

                return false;
            }

            if (!entrenador.isEstadoEntrenador()) {

                mensaje
                        = "El entrenador seleccionado "
                        + "se encuentra inactivo.";

                return false;
            }

            Especialidad especialidad
                    = especialidadDAO.buscar(
                            entrenadorEspecialidad
                                    .getIdEspecialidad()
                    );

            if (especialidad == null) {

                mensaje
                        = "La especialidad seleccionada "
                        + "no existe.";

                return false;
            }

            if (!especialidad.isEstadoEspecialidad()) {

                mensaje
                        = "La especialidad seleccionada "
                        + "se encuentra inactiva.";

                return false;
            }

            boolean existe
                    = entrenadorEspecialidadDAO.existe(
                            entrenadorEspecialidad.getIdEntrenador(),
                            entrenadorEspecialidad.getIdEspecialidad()
                    );

            if (existe) {

                mensaje
                        = "El entrenador ya tiene asignada "
                        + "esa especialidad.";

                return false;
            }

            boolean guardado
                    = entrenadorEspecialidadDAO.guardar(
                            entrenadorEspecialidad
                    );

            if (guardado) {

                mensaje
                        = "Especialidad asignada "
                        + "correctamente al entrenador.";

                return true;
            }

            mensaje
                    = "No se pudo asignar "
                    + "la especialidad al entrenador.";

            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(e);

            return false;
        }
    }

    public boolean modificar(
            EntrenadorEspecialidad entrenadorEspecialidad
    ) {

        mensaje = "";

        try {

            normalizarDatos(entrenadorEspecialidad);
            validarEntrenadorEspecialidad(entrenadorEspecialidad);

            EntrenadorEspecialidad guardada
                    = entrenadorEspecialidadDAO.buscar(
                            entrenadorEspecialidad.getIdEntrenador(),
                            entrenadorEspecialidad.getIdEspecialidad()
                    );

            if (guardada == null) {

                mensaje
                        = "La asignacion seleccionada "
                        + "ya no existe.";

                return false;
            }

            boolean modificado
                    = entrenadorEspecialidadDAO.modificar(
                            entrenadorEspecialidad
                    );

            if (modificado) {

                mensaje
                        = "Asignacion modificada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "la asignacion.";

            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(e);

            return false;
        }
    }

    public boolean desactivar(
            Long idEntrenador,
            Integer idEspecialidad
    ) {

        mensaje = "";

        if (idEntrenador == null) {

            mensaje
                    = "Seleccione un entrenador.";

            return false;
        }

        if (idEspecialidad == null) {

            mensaje
                    = "Seleccione una especialidad.";

            return false;
        }

        try {

            EntrenadorEspecialidad asignacion
                    = entrenadorEspecialidadDAO.buscar(
                            idEntrenador,
                            idEspecialidad
                    );

            if (asignacion == null) {

                mensaje
                        = "La asignacion seleccionada "
                        + "no existe.";

                return false;
            }

            if ("INACTIVA".equalsIgnoreCase(
                    asignacion.getEstadoAsignacion())) {

                mensaje
                        = "La asignacion ya se encuentra "
                        + "inactiva.";

                return false;
            }

            boolean desactivado
                    = entrenadorEspecialidadDAO.desactivar(
                            idEntrenador,
                            idEspecialidad
                    );

            if (desactivado) {

                mensaje
                        = "Asignacion desactivada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desactivar "
                    + "la asignacion.";

            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(e);

            return false;
        }
    }

    public void eliminarDefinitivamente(
            Long idEntrenador,
            Integer idEspecialidad
    ) throws SQLException {

        if (idEntrenador == null) {

            throw new IllegalArgumentException(
                    "Seleccione un entrenador."
            );
        }

        if (idEspecialidad == null) {

            throw new IllegalArgumentException(
                    "Seleccione una especialidad."
            );
        }

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede eliminar "
                    + "asignaciones definitivamente."
            );
        }

        try {

            boolean eliminado
                    = entrenadorEspecialidadDAO
                            .eliminarDefinitivamente(
                                    idEntrenador,
                                    idEspecialidad
                            );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "La asignacion ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque existen registros relacionados. "
                        + "Puede desactivar la asignacion.",
                        e
                );
            }

            throw e;
        }
    }

    public EntrenadorEspecialidad buscar(
            Long idEntrenador,
            Integer idEspecialidad
    ) {

        mensaje = "";

        if (idEntrenador == null
                || idEspecialidad == null) {
            return null;
        }

        try {

            return entrenadorEspecialidadDAO.buscar(
                    idEntrenador,
                    idEspecialidad
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "la asignacion.";

            return null;
        }
    }

    public List<EntrenadorEspecialidad> listarPorEntrenador(
            Long idEntrenador
    ) {

        mensaje = "";

        if (idEntrenador == null) {

            mensaje
                    = "Seleccione un entrenador.";

            return new ArrayList<>();
        }

        try {

            return entrenadorEspecialidadDAO
                    .listarPorEntrenador(
                            idEntrenador
                    );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "las especialidades del entrenador.";

            return new ArrayList<>();
        }
    }

    public List<EntrenadorEspecialidad> listarPorEspecialidad(
            Integer idEspecialidad
    ) {

        mensaje = "";

        if (idEspecialidad == null) {

            mensaje
                    = "Seleccione una especialidad.";

            return new ArrayList<>();
        }

        try {

            return entrenadorEspecialidadDAO
                    .listarPorEspecialidad(
                            idEspecialidad
                    );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los entrenadores de la especialidad.";

            return new ArrayList<>();
        }
    }

    public List<EntrenadorEspecialidad>
            listarActivasPorEntrenador(
                    Long idEntrenador
            ) {

        mensaje = "";

        if (idEntrenador == null) {

            mensaje
                    = "Seleccione un entrenador.";

            return new ArrayList<>();
        }

        try {

            return entrenadorEspecialidadDAO
                    .listarActivasPorEntrenador(
                            idEntrenador
                    );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "las especialidades activas.";

            return new ArrayList<>();
        }
    }

    private void validarEntrenadorEspecialidad(
            EntrenadorEspecialidad entrenadorEspecialidad
    ) {

        if (entrenadorEspecialidad == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "de la asignacion."
            );
        }

        if (entrenadorEspecialidad.getIdEntrenador() == null) {

            throw new IllegalArgumentException(
                    "Seleccione un entrenador."
            );
        }

        if (entrenadorEspecialidad.getIdEspecialidad() == null) {

            throw new IllegalArgumentException(
                    "Seleccione una especialidad."
            );
        }

        validarFechaAsignacion(
                entrenadorEspecialidad.getFechaAsignacion()
        );

        validarNivelDominio(
                entrenadorEspecialidad.getNivelDominio()
        );

        validarEstadoAsignacion(
                entrenadorEspecialidad.getEstadoAsignacion()
        );
    }

    private void validarFechaAsignacion(
            LocalDate fechaAsignacion
    ) {

        if (fechaAsignacion == null) {

            throw new IllegalArgumentException(
                    "La fecha de asignacion "
                    + "es obligatoria."
            );
        }

        if (fechaAsignacion.isAfter(
                LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha de asignacion "
                    + "no puede ser futura."
            );
        }
    }

    private void validarNivelDominio(
            String nivelDominio
    ) {

        if (nivelDominio == null
                || nivelDominio.isBlank()) {
            return;
        }

        if (nivelDominio.length() < 3
                || nivelDominio.length() > 80) {

            throw new IllegalArgumentException(
                    "El nivel de dominio debe contener "
                    + "entre 3 y 80 caracteres."
            );
        }
    }

    private void validarEstadoAsignacion(
            String estadoAsignacion
    ) {

        if (estadoAsignacion == null
                || estadoAsignacion.isBlank()) {

            throw new IllegalArgumentException(
                    "El estado de la asignacion "
                    + "es obligatorio."
            );
        }

        if (!"ACTIVA".equalsIgnoreCase(estadoAsignacion)
                && !"INACTIVA".equalsIgnoreCase(estadoAsignacion)) {

            throw new IllegalArgumentException(
                    "El estado de la asignacion debe ser "
                    + "ACTIVA o INACTIVA."
            );
        }
    }

    private void normalizarDatos(
            EntrenadorEspecialidad entrenadorEspecialidad
    ) {

        if (entrenadorEspecialidad == null) {
            return;
        }

        if (entrenadorEspecialidad.getNivelDominio() != null) {

            entrenadorEspecialidad.setNivelDominio(
                    entrenadorEspecialidad
                            .getNivelDominio()
                            .trim()
            );
        }

        if (entrenadorEspecialidad.getEstadoAsignacion() != null) {

            entrenadorEspecialidad.setEstadoAsignacion(
                    entrenadorEspecialidad
                            .getEstadoAsignacion()
                            .trim()
                            .toUpperCase()
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

            return "No se puede realizar la operacion "
                    + "porque existen registros relacionados.";
        }

        if ("23505".equals(e.getSQLState())) {

            return "El entrenador ya tiene asignada "
                    + "esa especialidad.";
        }

        if ("23514".equals(e.getSQLState())) {

            return "Los datos no cumplen las reglas "
                    + "de validacion de la base de datos.";
        }

        return "Error de base de datos: "
                + e.getMessage();
    }

    public String getMensaje() {
        return mensaje;
    }
}
