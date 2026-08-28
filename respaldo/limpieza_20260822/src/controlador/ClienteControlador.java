/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.ClienteDAO;
import dao.PersonaDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.Cliente;
import modelo.Persona;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class ClienteControlador {

    private final ClienteDAO clienteDAO;
    private final PersonaDAO personaDAO;
    private String mensaje;

    public ClienteControlador() {

        clienteDAO = new ClienteDAO();
        personaDAO = new PersonaDAO();
        mensaje = "";
    }

    // REGISTRAR
    public boolean registrar(
            Cliente cliente
    ) {

        mensaje = "";

        try {

            if (cliente != null) {

                cliente.setCodigoCliente(
                        null
                );
            }

            normalizarDatos(cliente);
            validarClienteParaRegistro(cliente);

            Cliente clienteExistente
                    = clienteDAO.buscar(
                            cliente.getIdPersona()
                    );

            if (clienteExistente != null) {

                mensaje
                        = "La persona seleccionada "
                        + "ya se encuentra registrada "
                        + "como cliente.";

                return false;
            }

            boolean guardado
                    = clienteDAO.guardar(
                            cliente
                    );

            if (guardado) {

                mensaje
                        = "Cliente registrado correctamente. "
                        + "Codigo asignado: "
                        + cliente.getCodigoCliente()
                        + ".";

                return true;
            }

            mensaje
                    = "No se pudo registrar "
                    + "el cliente.";

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

    // MODIFICAR
    public boolean modificar(
            Cliente cliente
    ) {

        mensaje = "";

        try {

            normalizarDatos(cliente);
            validarCliente(cliente);

            Cliente clienteGuardado
                    = clienteDAO.buscar(
                            cliente.getIdPersona()
                    );

            if (clienteGuardado == null) {

                mensaje
                        = "Seleccione un cliente "
                        + "registrado para modificar.";

                return false;
            }

            if (!clienteGuardado
                    .getCodigoCliente()
                    .equalsIgnoreCase(
                            cliente.getCodigoCliente()
                    )) {

                mensaje
                        = "El codigo del cliente "
                        + "no se puede modificar.";

                return false;
            }

            cliente.setCodigoCliente(
                    clienteGuardado.getCodigoCliente()
            );

            boolean modificado
                    = clienteDAO.modificar(
                            cliente
                    );

            if (modificado) {

                mensaje
                        = "Cliente modificado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "el cliente.";

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

    // DESACTIVAR
    public boolean desactivar(
            Long idPersona
    ) {

        mensaje = "";

        if (idPersona == null) {

            mensaje
                    = "Seleccione un cliente "
                    + "para desactivar.";

            return false;
        }

        try {

            Cliente cliente
                    = clienteDAO.buscar(
                            idPersona
                    );

            if (cliente == null) {

                mensaje
                        = "El cliente seleccionado "
                        + "no existe.";

                return false;
            }

            if (!cliente.isEstadoCliente()) {

                mensaje
                        = "El cliente ya se encuentra "
                        + "inactivo.";

                return false;
            }

            boolean desactivado
                    = clienteDAO.desactivar(
                            idPersona
                    );

            if (desactivado) {

                mensaje
                        = "Cliente desactivado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo desactivar "
                    + "el cliente.";

            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    // ELIMINAR DEFINITIVAMENTE
    public void eliminarDefinitivamente(
            Long idPersona
    ) throws SQLException {

        if (idPersona == null) {

            throw new IllegalArgumentException(
                    "Seleccione un cliente para eliminar."
            );
        }

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede "
                    + "eliminar clientes definitivamente."
            );
        }

        try {

            boolean eliminado
                    = clienteDAO.eliminarDefinitivamente(
                            idPersona
                    );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "El cliente ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque el cliente tiene registros relacionados.",
                        e
                );
            }

            throw e;
        }
    }

    // LISTAR CLIENTES
    public List<Cliente> listar(
            String criterio
    ) {

        mensaje = "";

        try {

            return clienteDAO.listar(
                    criterio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los clientes.";

            return new ArrayList<>();
        }
    }

    public List<Cliente> listarPorEntrenador(Long idEntrenador, String criterio) {
        mensaje = "";
        try {
            return clienteDAO.listarPorEntrenador(idEntrenador, criterio);
        } catch (SQLException e) {
            mensaje = "No se pudieron cargar los clientes asignados.";
            return new ArrayList<>();
        }
    }

    public List<Cliente> listarPorNutricionista(Long idNutricionista, String criterio) {
        mensaje = "";
        try {
            return clienteDAO.listarPorNutricionista(idNutricionista, criterio);
        } catch (SQLException e) {
            mensaje = "No se pudieron cargar los clientes asignados.";
            return new ArrayList<>();
        }
    }

    // BUSCAR CLIENTE
    public Cliente buscar(
            Long idPersona
    ) {

        mensaje = "";

        if (idPersona == null) {
            return null;
        }

        try {

            return clienteDAO.buscar(
                    idPersona
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "el cliente.";

            return null;
        }
    }

    // PERSONAS QUE TODAVÍA NO SON CLIENTES
    public List<Persona> listarPersonasDisponibles() {

        mensaje = "";

        try {

            return personaDAO
                    .listarDisponiblesParaCliente();

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "las personas disponibles.";

            return new ArrayList<>();
        }
    }

    // VALIDACIONES
    private void validarClienteParaRegistro(
            Cliente cliente
    ) {

        validarDatosClienteBase(
                cliente
        );
    }

    private void validarCliente(
            Cliente cliente
    ) {

        validarDatosClienteBase(
                cliente
        );

        validarCodigoCliente(
                cliente.getCodigoCliente()
        );
    }

    private void validarDatosClienteBase(
            Cliente cliente
    ) {

        if (cliente == null) {

            throw new IllegalArgumentException(
                    "No se recibieron "
                    + "los datos del cliente."
            );
        }

        if (cliente.getIdPersona() == null) {

            throw new IllegalArgumentException(
                    "Seleccione una persona."
            );
        }

        LocalDate fecha
                = cliente.getFechaRegistro();

        if (fecha == null) {

            throw new IllegalArgumentException(
                    "La fecha de registro "
                    + "es obligatoria."
            );
        }

        if (fecha.isAfter(
                LocalDate.now())) {

            throw new IllegalArgumentException(
                    "La fecha de registro "
                    + "no puede ser futura."
            );
        }

        validarPeso(
                cliente.getPesoInicial(),
                "El peso inicial"
        );

        validarPeso(
                cliente.getPesoMeta(),
                "El peso meta"
        );

        String observaciones
                = cliente.getObservaciones();

        if (observaciones != null
                && observaciones.length() > 500) {

            throw new IllegalArgumentException(
                    "Las observaciones no pueden "
                    + "superar 500 caracteres."
            );
        }
    }

    private void validarCodigoCliente(
            String codigo
    ) {

        if (codigo == null
                || codigo.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "El codigo del cliente "
                    + "es obligatorio."
            );
        }

        codigo = codigo.trim();

        if (!codigo.matches(
                "CL[0-9]{5}")) {

            throw new IllegalArgumentException(
                    "El codigo del cliente debe tener "
                    + "el formato CL00001."
            );
        }
    }

    private void validarPeso(
            BigDecimal peso,
            String nombreCampo
    ) {

        if (peso == null) {

            throw new IllegalArgumentException(
                    nombreCampo
                    + " es obligatorio."
            );
        }

        if (peso.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    nombreCampo
                    + " debe ser mayor que cero."
            );
        }

        BigDecimal limite
                = new BigDecimal("999.99");

        if (peso.compareTo(limite) > 0) {

            throw new IllegalArgumentException(
                    nombreCampo
                    + " no puede superar 999.99 kg."
            );
        }
    }

    // NORMALIZAR DATOS
    private void normalizarDatos(
            Cliente cliente
    ) {

        if (cliente == null) {
            return;
        }

        if (cliente.getCodigoCliente() != null) {

            cliente.setCodigoCliente(
                    cliente
                            .getCodigoCliente()
                            .trim()
                            .toUpperCase()
            );
        }

        String observaciones
                = cliente.getObservaciones();

        if (observaciones != null) {

            observaciones
                    = observaciones.trim();

            if (observaciones.isEmpty()) {

                observaciones = null;
            }
        }

        cliente.setObservaciones(
                observaciones
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

    private String traducirErrorBaseDatos(
            SQLException e
    ) {

        if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

            return "No se puede realizar la operacion "
                    + "porque existen registros relacionados.";
        }

        if ("23505".equals(e.getSQLState())) {

            return "Ya existe un cliente "
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
