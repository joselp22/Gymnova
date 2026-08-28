/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.EspecialidadDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Especialidad;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class EspecialidadControlador {

    private final EspecialidadDAO especialidadDAO;
    private String mensaje;

    public EspecialidadControlador() {

        especialidadDAO = new EspecialidadDAO();
        mensaje = "";
    }

    public boolean registrar(
            Especialidad especialidad
    ) {

        mensaje = "";

        try {

            normalizarDatos(especialidad);
            validarEspecialidad(especialidad);

            boolean existeNombre
                    = especialidadDAO.existeNombre(
                            especialidad.getNombreEspecialidad()
                    );

            if (existeNombre) {

                mensaje
                        = "Ya existe una especialidad "
                        + "con ese nombre.";

                return false;
            }

            boolean guardado
                    = especialidadDAO.guardar(
                            especialidad
                    );

            if (guardado) {

                mensaje
                        = "Especialidad registrada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo registrar "
                    + "la especialidad.";

            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    public boolean modificar(
            Especialidad especialidad
    ) {

        mensaje = "";

        try {

            normalizarDatos(especialidad);
            validarEspecialidad(especialidad);

            if (especialidad.getIdEspecialidad() == null) {

                mensaje
                        = "Seleccione una especialidad "
                        + "para modificar.";

                return false;
            }

            Especialidad guardada
                    = especialidadDAO.buscar(
                            especialidad.getIdEspecialidad()
                    );

            if (guardada == null) {

                mensaje
                        = "La especialidad seleccionada "
                        + "ya no existe.";

                return false;
            }

            boolean nombreUsado
                    = especialidadDAO
                            .existeNombreEnOtroRegistro(
                                    especialidad.getIdEspecialidad(),
                                    especialidad.getNombreEspecialidad()
                            );

            if (nombreUsado) {

                mensaje
                        = "Ya existe otra especialidad "
                        + "con ese nombre.";

                return false;
            }

            boolean modificado
                    = especialidadDAO.modificar(
                            especialidad
                    );

            if (modificado) {

                mensaje
                        = "Especialidad modificada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "la especialidad.";

            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    public boolean desactivar(
            Integer idEspecialidad
    ) {

        mensaje = "";

        if (idEspecialidad == null) {

            mensaje
                    = "Seleccione una especialidad "
                    + "para desactivar.";

            return false;
        }

        try {

            Especialidad especialidad
                    = especialidadDAO.buscar(
                            idEspecialidad
                    );

            if (especialidad == null) {

                mensaje
                        = "La especialidad seleccionada "
                        + "no existe.";

                return false;
            }

            if (!especialidad.isEstadoEspecialidad()) {

                mensaje
                        = "La especialidad ya se encuentra "
                        + "inactiva.";

                return false;
            }

            boolean desactivado
                    = especialidadDAO.desactivar(
                            idEspecialidad
                    );

            if (desactivado) {

                mensaje
                        = "Especialidad desactivada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desactivar "
                    + "la especialidad.";

            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    public void eliminarDefinitivamente(
            Integer idEspecialidad
    ) throws SQLException {

        if (idEspecialidad == null) {

            throw new IllegalArgumentException(
                    "Seleccione una especialidad para eliminar."
            );
        }

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede eliminar "
                    + "especialidades definitivamente."
            );
        }

        try {

            boolean eliminado
                    = especialidadDAO
                            .eliminarDefinitivamente(
                                    idEspecialidad
                            );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "La especialidad ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque la especialidad tiene registros "
                        + "relacionados. Puede desactivarla.",
                        e
                );
            }

            throw e;
        }
    }

    public Especialidad buscar(
            Integer idEspecialidad
    ) {

        mensaje = "";

        if (idEspecialidad == null) {
            return null;
        }

        try {

            return especialidadDAO.buscar(
                    idEspecialidad
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "la especialidad.";

            return null;
        }
    }

    public List<Especialidad> listar(
            String criterio
    ) {

        mensaje = "";

        try {

            return especialidadDAO.listar(
                    criterio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "las especialidades.";

            return new ArrayList<>();
        }
    }

    public List<Especialidad> listarActivas() {

        mensaje = "";

        try {

            return especialidadDAO
                    .listarActivas();

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "las especialidades activas.";

            return new ArrayList<>();
        }
    }

    private void validarEspecialidad(
            Especialidad especialidad
    ) {

        if (especialidad == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "de la especialidad."
            );
        }

        if (especialidad.getNombreEspecialidad() == null
                || especialidad.getNombreEspecialidad().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre de la especialidad "
                    + "es obligatorio."
            );
        }

        if (especialidad.getNombreEspecialidad().length() < 3
                || especialidad.getNombreEspecialidad().length() > 100) {

            throw new IllegalArgumentException(
                    "El nombre de la especialidad debe contener "
                    + "entre 3 y 100 caracteres."
            );
        }

        if (especialidad.getDescripcion() != null
                && especialidad.getDescripcion().length() > 500) {

            throw new IllegalArgumentException(
                    "La descripcion no debe superar "
                    + "los 500 caracteres."
            );
        }

        if (especialidad.getCategoria() != null
                && especialidad.getCategoria().length() > 80) {

            throw new IllegalArgumentException(
                    "La categoria no debe superar "
                    + "los 80 caracteres."
            );
        }
    }

    private void normalizarDatos(
            Especialidad especialidad
    ) {

        if (especialidad == null) {
            return;
        }

        if (especialidad.getNombreEspecialidad() != null) {

            especialidad.setNombreEspecialidad(
                    especialidad
                            .getNombreEspecialidad()
                            .trim()
            );
        }

        if (especialidad.getDescripcion() != null) {

            especialidad.setDescripcion(
                    especialidad
                            .getDescripcion()
                            .trim()
            );
        }

        if (especialidad.getCategoria() != null) {

            especialidad.setCategoria(
                    especialidad
                            .getCategoria()
                            .trim()
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

            return "Ya existe una especialidad "
                    + "con esos datos.";
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
