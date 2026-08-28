/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.PersonaDAO;
import modelo.Persona;
import utilidades.SesionUsuario;
import utilidades.Validaciones;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Usuario
 */
public class PersonaControlador {

    private final PersonaDAO personaDAO;

    public PersonaControlador() {
        personaDAO = new PersonaDAO();
    }

    public Persona registrar(
            Persona persona
    ) throws SQLException {

        validarPersona(persona);

        if (personaDAO.existeCedula(
                persona.getCedula(),
                null
        )) {

            throw new IllegalArgumentException(
                    "Ya existe una persona "
                    + "con esa cedula."
            );
        }

        try {

            return personaDAO.guardar(
                    persona
            );

        } catch (SQLException e) {

            traducirErrorBaseDatos(e);
            throw e;
        }
    }

    public void modificar(
            Persona persona
    ) throws SQLException {

        if (persona == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "de la persona."
            );
        }

        if (persona.getIdPersona() == null) {

            throw new IllegalArgumentException(
                    "Seleccione una persona "
                    + "para modificar."
            );
        }

        Optional<Persona> personaGuardada
                = personaDAO.buscarPorId(
                        persona.getIdPersona()
                );

        if (personaGuardada.isEmpty()) {

            throw new IllegalArgumentException(
                    "La persona seleccionada ya no existe."
            );
        }

        String cedulaOriginal
                = personaGuardada
                        .get()
                        .getCedula();

        if (persona.getCedula() == null
                || !cedulaOriginal.equals(
                        persona.getCedula().trim()
                )) {

            throw new IllegalArgumentException(
                    "La cedula no se puede modificar "
                    + "despues del registro."
            );
        }

        persona.setCedula(
                cedulaOriginal
        );

        validarPersona(persona);

        try {

            personaDAO.modificar(
                    persona
            );

        } catch (SQLException e) {

            traducirErrorBaseDatos(e);
            throw e;
        }
    }

    public List<Persona> listar(
            String criterio
    ) throws SQLException {

        return personaDAO.listar(
                criterio
        );
    }

    public Optional<Persona> buscarPorId(
            Long idPersona
    ) throws SQLException {

        if (idPersona == null) {
            return Optional.empty();
        }

        return personaDAO.buscarPorId(
                idPersona
        );
    }

    public void desactivar(
            Long idPersona
    ) throws SQLException {

        if (idPersona == null) {

            throw new IllegalArgumentException(
                    "Seleccione una persona "
                    + "para desactivar."
            );
        }

        Optional<Persona> persona
                = personaDAO.buscarPorId(
                        idPersona
                );

        if (persona.isEmpty()) {

            throw new IllegalArgumentException(
                    "La persona seleccionada no existe."
            );
        }

        if (!persona.get().isEstado()) {

            throw new IllegalArgumentException(
                    "La persona ya se encuentra inactiva."
            );
        }

        try {

            personaDAO.desactivar(
                    idPersona
            );

        } catch (SQLException e) {

            traducirErrorBaseDatos(e);
            throw e;
        }
    }

    public void eliminarDefinitivamente(
            Long idPersona
    ) throws SQLException {

        if (idPersona == null) {

            throw new IllegalArgumentException(
                    "Seleccione una persona para eliminar."
            );
        }

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede "
                    + "eliminar personas definitivamente."
            );
        }

        try {

            boolean eliminado
                    = personaDAO.eliminarDefinitivamente(
                            idPersona
                    );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "La persona ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque la persona tiene registros "
                        + "relacionados como cliente, empleado "
                        + "o usuario.",
                        e
                );
            }

            throw e;
        }
    }

    public List<Persona> listarDisponiblesParaCliente()
            throws SQLException {

        return personaDAO
                .listarDisponiblesParaCliente();
    }

    public List<Persona> listarDisponiblesParaEmpleado()
            throws SQLException {

        return personaDAO
                .listarDisponiblesParaEmpleado();
    }

    private void validarPersona(
            Persona persona
    ) {

        if (persona == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "de la persona."
            );
        }

        Validaciones.validarCedulaEcuatoriana(
                persona.getCedula()
        );

        Validaciones.validarNombre(
                persona.getNombres(),
                "Los nombres"
        );

        Validaciones.validarNombre(
                persona.getApellidos(),
                "Los apellidos"
        );

        Validaciones.validarFechaNacimiento(
                persona.getFechaNacimiento()
        );

        Validaciones.validarSexo(
                persona.getSexo()
        );

        Validaciones.validarTelefono(
                persona.getTelefono()
        );

        Validaciones.validarCorreo(
                persona.getCorreo()
        );

        if (persona.getFotoPerfil() != null
                && persona.getFotoPerfil().length
                > 2_000_000) {

            throw new IllegalArgumentException(
                    "La fotografia no puede superar 2 MB."
            );
        }

        normalizarDatos(
                persona
        );
    }

    private void normalizarDatos(
            Persona persona
    ) {

        persona.setCedula(
                persona
                        .getCedula()
                        .trim()
        );

        persona.setNombres(
                normalizarNombre(
                        persona.getNombres()
                )
        );

        persona.setApellidos(
                normalizarNombre(
                        persona.getApellidos()
                )
        );

        persona.setTelefono(
                persona
                        .getTelefono()
                        .trim()
        );

        persona.setCorreo(
                persona
                        .getCorreo()
                        .trim()
                        .toLowerCase()
        );
    }

    private String normalizarNombre(
            String texto
    ) {

        return texto
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private boolean esAdministrador() {

        return SesionUsuario.haySesionActiva()
                && "Administrador".equalsIgnoreCase(
                        SesionUsuario
                                .getUsuarioActual()
                                .getNombreRol()
                );
    }

    private void traducirErrorBaseDatos(
            SQLException error
    ) throws SQLException {

        if (("23503".equals(error.getSQLState())
                    || "23001".equals(error.getSQLState()))) {

            throw new IllegalStateException(
                    "No se puede realizar la operacion "
                    + "porque existen registros relacionados.",
                    error
            );
        }

        if ("23514".equals(
                error.getSQLState())) {

            throw new IllegalArgumentException(
                    "Los datos no cumplen las reglas "
                    + "de validacion de la base de datos.",
                    error
            );
        }

        if (!"23505".equals(
                error.getSQLState())) {

            throw error;
        }

        String detalle
                = error.getMessage();

        if (detalle != null
                && detalle.contains(
                        "uq_persona_correo"
                )) {

            throw new IllegalArgumentException(
                    "Ya existe una persona "
                    + "con ese correo.",
                    error
            );
        }

        if (detalle != null
                && detalle.contains(
                        "uq_persona_cedula"
                )) {

            throw new IllegalArgumentException(
                    "Ya existe una persona "
                    + "con esa cedula.",
                    error
            );
        }

        throw new IllegalArgumentException(
                "Ya existe un registro "
                + "con esos datos.",
                error
        );
    }
}
