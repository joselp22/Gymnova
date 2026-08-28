/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.EmpleadoDAO;
import dao.NutricionistaDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.Empleado;
import modelo.Nutricionista;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class NutricionistaControlador {

    private final NutricionistaDAO nutricionistaDAO;
    private final EmpleadoDAO empleadoDAO;
    private String mensaje;

    public NutricionistaControlador() {

        nutricionistaDAO = new NutricionistaDAO();
        empleadoDAO = new EmpleadoDAO();
        mensaje = "";
    }

    public boolean registrar(
            Nutricionista nutricionista
    ) {

        mensaje = "";

        try {

            normalizarDatos(nutricionista);
            validarNutricionista(nutricionista);

            Empleado empleado
                    = empleadoDAO.buscar(
                            nutricionista.getIdPersona()
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

            Nutricionista existente
                    = nutricionistaDAO.buscar(
                            nutricionista.getIdPersona()
                    );

            if (existente != null) {

                mensaje
                        = "El empleado seleccionado "
                        + "ya esta registrado como nutricionista.";

                return false;
            }

            boolean guardado
                    = nutricionistaDAO.guardar(
                            nutricionista
                    );

            if (guardado) {

                mensaje
                        = "Nutricionista registrado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo registrar "
                    + "el nutricionista.";

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
            Nutricionista nutricionista
    ) {

        mensaje = "";

        try {

            normalizarDatos(nutricionista);
            validarNutricionista(nutricionista);

            Nutricionista guardado
                    = nutricionistaDAO.buscar(
                            nutricionista.getIdPersona()
                    );

            if (guardado == null) {

                mensaje
                        = "Seleccione un nutricionista "
                        + "registrado para modificar.";

                return false;
            }

            boolean modificado
                    = nutricionistaDAO.modificar(
                            nutricionista
                    );

            if (modificado) {

                mensaje
                        = "Nutricionista modificado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "el nutricionista.";

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
            Long idNutricionista
    ) {

        mensaje = "";

        if (idNutricionista == null) {

            mensaje
                    = "Seleccione un nutricionista "
                    + "para desactivar.";

            return false;
        }

        try {

            Nutricionista nutricionista
                    = nutricionistaDAO.buscar(
                            idNutricionista
                    );

            if (nutricionista == null) {

                mensaje
                        = "El nutricionista seleccionado "
                        + "no existe.";

                return false;
            }

            if ("INACTIVA".equalsIgnoreCase(
                    nutricionista.getEstadoLicencia())) {

                mensaje
                        = "El nutricionista ya se encuentra "
                        + "inactivo.";

                return false;
            }

            boolean desactivado
                    = nutricionistaDAO.desactivar(
                            idNutricionista
                    );

            if (desactivado) {

                mensaje
                        = "Nutricionista desactivado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desactivar "
                    + "el nutricionista.";

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
            Long idNutricionista
    ) throws SQLException {

        if (idNutricionista == null) {

            throw new IllegalArgumentException(
                    "Seleccione un nutricionista para eliminar."
            );
        }

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede eliminar "
                    + "nutricionistas definitivamente."
            );
        }

        try {

            boolean eliminado
                    = nutricionistaDAO
                            .eliminarDefinitivamente(
                                    idNutricionista
                            );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "El nutricionista ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque el nutricionista tiene registros "
                        + "relacionados.",
                        e
                );
            }

            throw e;
        }
    }

    public List<Nutricionista> listar(
            String criterio
    ) {

        mensaje = "";

        try {

            return nutricionistaDAO.listar(
                    criterio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los nutricionistas.";

            return new ArrayList<>();
        }
    }

    public Nutricionista buscar(
            Long idNutricionista
    ) {

        mensaje = "";

        if (idNutricionista == null) {
            return null;
        }

        try {

            return nutricionistaDAO.buscar(
                    idNutricionista
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "el nutricionista.";

            return null;
        }
    }

    public List<Empleado> listarEmpleadosDisponibles() {

        mensaje = "";

        try {

            return nutricionistaDAO
                    .listarEmpleadosDisponibles();

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los empleados disponibles.";

            return new ArrayList<>();
        }
    }

    private void validarNutricionista(
            Nutricionista nutricionista
    ) {

        if (nutricionista == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "del nutricionista."
            );
        }

        if (nutricionista.getIdPersona() == null) {

            throw new IllegalArgumentException(
                    "Seleccione un empleado."
            );
        }

        validarNumeroLicencia(
                nutricionista.getNumeroLicencia()
        );

        validarFechaInicio(
                nutricionista.getFechaInicioProfesion()
        );

        validarEstadoLicencia(
                nutricionista.getEstadoLicencia()
        );
    }

    private void validarNumeroLicencia(
            String numeroLicencia
    ) {

        if (numeroLicencia == null
                || numeroLicencia.isBlank()) {

            throw new IllegalArgumentException(
                    "El numero de licencia "
                    + "es obligatorio."
            );
        }

        if (numeroLicencia.length() < 3
                || numeroLicencia.length() > 50) {

            throw new IllegalArgumentException(
                    "El numero de licencia debe contener "
                    + "entre 3 y 50 caracteres."
            );
        }
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

    private void validarEstadoLicencia(
            String estadoLicencia
    ) {

        if (estadoLicencia == null
                || estadoLicencia.isBlank()) {

            throw new IllegalArgumentException(
                    "El estado de la licencia "
                    + "es obligatorio."
            );
        }

        if (!"ACTIVA".equalsIgnoreCase(estadoLicencia)
                && !"INACTIVA".equalsIgnoreCase(estadoLicencia)
                && !"SUSPENDIDA".equalsIgnoreCase(estadoLicencia)) {

            throw new IllegalArgumentException(
                    "El estado de la licencia debe ser "
                    + "ACTIVA, INACTIVA o SUSPENDIDA."
            );
        }
    }

    private void normalizarDatos(
            Nutricionista nutricionista
    ) {

        if (nutricionista == null) {
            return;
        }

        if (nutricionista.getNumeroLicencia() != null) {

            nutricionista.setNumeroLicencia(
                    nutricionista
                            .getNumeroLicencia()
                            .trim()
            );
        }

        if (nutricionista.getEstadoLicencia() != null) {

            nutricionista.setEstadoLicencia(
                    nutricionista
                            .getEstadoLicencia()
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

            return "Ya existe un nutricionista "
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
