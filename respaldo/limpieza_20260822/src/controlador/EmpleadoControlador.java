/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.EmpleadoDAO;
import dao.PersonaDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import modelo.Empleado;
import modelo.Persona;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class EmpleadoControlador {

    private final EmpleadoDAO empleadoDAO;
    private final PersonaDAO personaDAO;
    private String mensaje;

    public EmpleadoControlador() {

        empleadoDAO = new EmpleadoDAO();
        personaDAO = new PersonaDAO();
        mensaje = "";
    }

    public boolean registrar(
            Empleado empleado
    ) {

        mensaje = "";

        try {

            if (empleado != null) {

                empleado.setCodigoEmpleado(
                        null
                );
            }

            normalizarDatos(empleado);
            validarEmpleadoParaRegistro(empleado);

            Empleado existente
                    = empleadoDAO.buscar(
                            empleado.getIdPersona()
                    );

            if (existente != null) {

                mensaje
                        = "La persona seleccionada "
                        + "ya se encuentra registrada "
                        + "como empleado.";

                return false;
            }

            boolean guardado
                    = empleadoDAO.guardar(
                            empleado
                    );

            if (guardado) {

                mensaje
                        = "Empleado registrado correctamente. "
                        + "Codigo asignado: "
                        + empleado.getCodigoEmpleado()
                        + ".";

                return true;
            }

            mensaje
                    = "No se pudo registrar "
                    + "el empleado.";

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
            Empleado empleado
    ) {

        mensaje = "";

        try {

            normalizarDatos(empleado);
            validarEmpleado(empleado);

            Empleado guardado
                    = empleadoDAO.buscar(
                            empleado.getIdPersona()
                    );

            if (guardado == null) {

                mensaje
                        = "Seleccione un empleado "
                        + "registrado para modificar.";

                return false;
            }

            if (!guardado
                    .getCodigoEmpleado()
                    .equalsIgnoreCase(
                            empleado.getCodigoEmpleado()
                    )) {

                mensaje
                        = "El codigo del empleado "
                        + "no se puede modificar.";

                return false;
            }

            empleado.setCodigoEmpleado(
                    guardado.getCodigoEmpleado()
            );

            boolean modificado
                    = empleadoDAO.modificar(
                            empleado
                    );

            if (modificado) {

                mensaje
                        = "Empleado modificado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "el empleado.";

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
            Long idPersona
    ) {

        mensaje = "";

        if (idPersona == null) {

            mensaje
                    = "Seleccione un empleado "
                    + "para desactivar.";

            return false;
        }

        try {

            Empleado empleado
                    = empleadoDAO.buscar(
                            idPersona
                    );

            if (empleado == null) {

                mensaje
                        = "El empleado seleccionado "
                        + "no existe.";

                return false;
            }

            if (!empleado.isEstadoEmpleado()) {

                mensaje
                        = "El empleado ya se encuentra "
                        + "inactivo.";

                return false;
            }

            boolean desactivado
                    = empleadoDAO.desactivar(
                            idPersona
                    );

            if (desactivado) {

                mensaje
                        = "Empleado desactivado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desactivar "
                    + "el empleado.";

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
            Long idPersona
    ) throws SQLException {

        if (idPersona == null) {

            throw new IllegalArgumentException(
                    "Seleccione un empleado para eliminar."
            );
        }

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede eliminar "
                    + "empleados definitivamente."
            );
        }

        try {

            boolean eliminado
                    = empleadoDAO
                            .eliminarDefinitivamente(
                                    idPersona
                            );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "El empleado ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque el empleado tiene registros "
                        + "relacionados.",
                        e
                );
            }

            throw e;
        }
    }

    public List<Empleado> listar(
            String criterio
    ) {

        mensaje = "";

        try {

            return empleadoDAO.listar(
                    criterio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los empleados.";

            return new ArrayList<>();
        }
    }

    public Empleado buscar(
            Long idPersona
    ) {

        mensaje = "";

        if (idPersona == null) {
            return null;
        }

        try {

            return empleadoDAO.buscar(
                    idPersona
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "el empleado.";

            return null;
        }
    }

    public List<Persona> listarPersonasDisponibles() {

        mensaje = "";

        try {

            return personaDAO
                    .listarDisponiblesParaEmpleado();

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "las personas disponibles.";

            return new ArrayList<>();
        }
    }

    private void validarEmpleadoParaRegistro(
            Empleado empleado
    ) {

        validarDatosEmpleadoBase(
                empleado
        );
    }

    private void validarEmpleado(
            Empleado empleado
    ) {

        validarDatosEmpleadoBase(
                empleado
        );

        validarCodigo(
                empleado.getCodigoEmpleado()
        );
    }

    private void validarDatosEmpleadoBase(
            Empleado empleado
    ) {

        if (empleado == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "del empleado."
            );
        }

        if (empleado.getIdPersona() == null) {

            throw new IllegalArgumentException(
                    "Seleccione una persona."
            );
        }

        validarFechaIngreso(
                empleado.getFechaIngreso()
        );

        validarTipoContrato(
                empleado.getTipoContrato()
        );

        validarSalario(
                empleado.getSalario()
        );

        validarTurno(
                empleado.getHoraInicio(),
                empleado.getHoraFin()
        );
    }

    private void validarCodigo(
            String codigo
    ) {

        if (codigo == null
                || codigo.isBlank()) {

            throw new IllegalArgumentException(
                    "El codigo del empleado "
                    + "es obligatorio."
            );
        }

        if (!codigo.matches(
                "EM[0-9]{5}")) {

            throw new IllegalArgumentException(
                    "El codigo del empleado debe tener "
                    + "el formato EM00001."
            );
        }
    }

    private void validarFechaIngreso(
            LocalDate fechaIngreso
    ) {

        if (fechaIngreso == null) {

            throw new IllegalArgumentException(
                    "La fecha de ingreso "
                    + "es obligatoria."
            );
        }

        if (fechaIngreso.isAfter(
                LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha de ingreso "
                    + "no puede ser futura."
            );
        }
    }

    private void validarTipoContrato(
            String tipoContrato
    ) {

        if (tipoContrato == null
                || tipoContrato.isBlank()) {

            throw new IllegalArgumentException(
                    "Seleccione el tipo de contrato."
            );
        }

        boolean contratoValido
                = tipoContrato.equals(
                        "Tiempo completo"
                )
                || tipoContrato.equals(
                        "Medio tiempo"
                )
                || tipoContrato.equals(
                        "Temporal"
                )
                || tipoContrato.equals(
                        "Servicios profesionales"
                );

        if (!contratoValido) {

            throw new IllegalArgumentException(
                    "El tipo de contrato seleccionado "
                    + "no es valido."
            );
        }
    }

    private void validarSalario(
            BigDecimal salario
    ) {

        if (salario == null) {

            throw new IllegalArgumentException(
                    "El salario es obligatorio."
            );
        }

        if (salario.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El salario debe ser "
                    + "mayor que cero."
            );
        }

        BigDecimal limite
                = new BigDecimal(
                        "99999999.99"
                );

        if (salario.compareTo(
                limite) > 0) {

            throw new IllegalArgumentException(
                    "El salario supera "
                    + "el valor permitido."
            );
        }

        if (salario.scale() > 2) {

            throw new IllegalArgumentException(
                    "El salario solamente puede "
                    + "tener dos decimales."
            );
        }
    }

    private void validarTurno(
            LocalTime horaInicio,
            LocalTime horaFin
    ) {

        if (horaInicio == null) {

            throw new IllegalArgumentException(
                    "La hora de inicio "
                    + "es obligatoria."
            );
        }

        if (horaFin == null) {

            throw new IllegalArgumentException(
                    "La hora de finalizacion "
                    + "es obligatoria."
            );
        }

        if (horaInicio.equals(horaFin)) {

            throw new IllegalArgumentException(
                    "La hora de inicio y la hora "
                    + "de finalizacion deben ser diferentes."
            );
        }
    }

    private void normalizarDatos(
            Empleado empleado
    ) {

        if (empleado == null) {
            return;
        }

        String codigo
                = empleado.getCodigoEmpleado();

        if (codigo != null) {

            empleado.setCodigoEmpleado(
                    codigo.trim().toUpperCase()
            );
        }

        String contrato
                = empleado.getTipoContrato();

        if (contrato != null) {

            empleado.setTipoContrato(
                    contrato.trim()
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

            return "Ya existe un empleado "
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
