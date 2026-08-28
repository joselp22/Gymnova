/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.EmpleadoDAO;
import dao.EntrenadorDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.Empleado;
import modelo.Entrenador;
import utilidades.SesionUsuario;
/**
 *
 * @author Usuario
 */

public class EntrenadorControlador {

    private final EntrenadorDAO entrenadorDAO;
    private final EmpleadoDAO empleadoDAO;
    private String mensaje;

    public EntrenadorControlador() {

        entrenadorDAO = new EntrenadorDAO();
        empleadoDAO = new EmpleadoDAO();
        mensaje = "";
    }

    public boolean registrar(
            Entrenador entrenador
    ) {

        mensaje = "";

        try {

            normalizarDatos(entrenador);
            validarEntrenador(entrenador);

            Empleado empleado
                    = empleadoDAO.buscar(
                            entrenador.getIdPersona()
                    );

            if (empleado == null) {

                mensaje
                        = "La persona seleccionada "
                        + "no esta registrada como empleado.";

                return false;
            }

            if (!empleado.isEstado()
                    || !empleado.isEstadoEmpleado()) {

                mensaje
                        = "El empleado seleccionado "
                        + "se encuentra inactivo.";

                return false;
            }

            Entrenador existente
                    = entrenadorDAO.buscar(
                            entrenador.getIdPersona()
                    );

            if (existente != null) {

                mensaje
                        = "El empleado seleccionado "
                        + "ya esta registrado como entrenador.";

                return false;
            }

            boolean guardado
                    = entrenadorDAO.guardar(
                            entrenador
                    );

            if (guardado) {

                mensaje
                        = "Entrenador registrado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo registrar "
                    + "el entrenador.";

            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = "Error al registrar el entrenador: "
                    + e.getMessage();

            return false;
        }
    }

    public boolean modificar(
            Entrenador entrenador
    ) {

        mensaje = "";

        try {

            normalizarDatos(entrenador);
            validarEntrenador(entrenador);

            Entrenador guardado
                    = entrenadorDAO.buscar(
                            entrenador.getIdPersona()
                    );

            if (guardado == null) {

                mensaje
                        = "Seleccione un entrenador "
                        + "registrado para modificar.";

                return false;
            }

            boolean modificado
                    = entrenadorDAO.modificar(
                            entrenador
                    );

            if (modificado) {

                mensaje
                        = "Entrenador modificado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "el entrenador.";

            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = "Error al modificar el entrenador: "
                    + e.getMessage();

            return false;
        }
    }

    public boolean desactivar(
            Long idPersona
    ) {

        mensaje = "";

        if (idPersona == null) {

            mensaje
                    = "Seleccione un entrenador "
                    + "para desactivar.";

            return false;
        }

        try {

            Entrenador entrenador
                    = entrenadorDAO.buscar(idPersona);

            if (entrenador == null) {

                mensaje
                        = "El entrenador seleccionado "
                        + "no existe.";

                return false;
            }

            if (!entrenador.isEstadoEntrenador()) {

                mensaje
                        = "El entrenador ya se encuentra "
                        + "inactivo.";

                return false;
            }

            boolean desactivado
                    = entrenadorDAO.desactivar(
                            idPersona
                    );

            if (desactivado) {

                mensaje
                        = "Entrenador desactivado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desactivar "
                    + "el entrenador.";

            return false;

        } catch (SQLException e) {

            mensaje
                    = "Error al desactivar el entrenador: "
                    + e.getMessage();

            return false;
        }
    }

    public void eliminarDefinitivamente(
            Long idPersona
    ) throws SQLException {

        if (idPersona == null) {

            throw new IllegalArgumentException(
                    "Seleccione un entrenador para eliminar."
            );
        }

        boolean esAdministrador
                = SesionUsuario.haySesionActiva()
                && "Administrador".equalsIgnoreCase(
                        SesionUsuario
                                .getUsuarioActual()
                                .getNombreRol()
                );

        if (!esAdministrador) {

            throw new SecurityException(
                    "Solo el Administrador puede eliminar "
                    + "entrenadores definitivamente."
            );
        }

        try {

            boolean eliminado
                    = entrenadorDAO
                            .eliminarDefinitivamente(
                                    idPersona
                            );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "El entrenador ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque el entrenador tiene registros "
                        + "relacionados.",
                        e
                );
            }

            throw e;
        }
    }

    public List<Entrenador> listar(
            String criterio
    ) {

        mensaje = "";

        try {

            return entrenadorDAO.listar(
                    criterio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los entrenadores.";

            return new ArrayList<>();
        }
    }

    public Entrenador buscar(
            Long idPersona
    ) {

        mensaje = "";

        if (idPersona == null) {
            return null;
        }

        try {

            return entrenadorDAO.buscar(
                    idPersona
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "el entrenador.";

            return null;
        }
    }

    public List<Empleado>
            listarEmpleadosDisponibles() {

        mensaje = "";

        try {

            return entrenadorDAO
                    .listarEmpleadosDisponibles();

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los empleados disponibles.";

            return new ArrayList<>();
        }
    }

    private void validarEntrenador(
        Entrenador entrenador
) {

    if (entrenador == null) {

        throw new IllegalArgumentException(
                "No se recibieron los datos "
                + "del entrenador."
        );
    }

    if (entrenador.getIdPersona() == null) {

        throw new IllegalArgumentException(
                "Seleccione un empleado."
        );
    }

    validarFechaInicio(
            entrenador.getFechaInicioProfesion()
    );

    validarNivel(
            entrenador.getNivelEntrenador()
    );
}

    private void validarFechaInicio(
            LocalDate fechaInicio
    ) {

        if (fechaInicio == null) {

            throw new IllegalArgumentException(
                    "La fecha de inicio profesional "
                    + "es obligatoria."
            );
        }

        if (fechaInicio.isAfter(
                LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha de inicio profesional "
                    + "no puede ser futura."
            );
        }
    }

    private void validarNivel(
        String nivel
) {

    if (nivel == null
            || nivel.isBlank()) {

        throw new IllegalArgumentException(
                "El nivel del entrenador "
                + "es obligatorio."
        );
    }

    if (nivel.length() < 3
            || nivel.length() > 80) {

        throw new IllegalArgumentException(
                "El nivel debe contener "
                + "entre 3 y 80 caracteres."
        );
    }
}

    private void normalizarDatos(
        Entrenador entrenador
) {

    if (entrenador == null) {
        return;
    }

    String nivel
            = entrenador.getNivelEntrenador();

    if (nivel != null) {

        entrenador.setNivelEntrenador(
                nivel.trim()
        );
    }
}

    public String getMensaje() {
        return mensaje;
    }
}
