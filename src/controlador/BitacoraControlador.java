/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.BitacoraDAO;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import modelo.Bitacora;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class BitacoraControlador {

    private final BitacoraDAO bitacoraDAO;
    private String mensaje;

    public BitacoraControlador() {

        bitacoraDAO = new BitacoraDAO();
        mensaje = "";
    }

    public boolean registrar(
            Bitacora bitacora
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    bitacora
            );

            completarDatosSesion(
                    bitacora
            );

            validarBitacora(
                    bitacora
            );

            boolean guardado
                    = bitacoraDAO.registrar(
                            bitacora
                    );

            if (guardado) {

                mensaje
                        = "Registro de bitacora guardado "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo guardar "
                    + "el registro de bitacora.";

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

    public boolean registrarEvento(
            String modulo,
            String accionRealizada,
            String descripcion,
            String resultado
    ) {

        Bitacora bitacora
                = new Bitacora();

        bitacora.setModulo(
                modulo
        );

        bitacora.setAccionRealizada(
                accionRealizada
        );

        bitacora.setDescripcion(
                descripcion
        );

        bitacora.setResultado(
                resultado
        );

        return registrar(
                bitacora
        );
    }

    public Bitacora buscar(
            Long idBitacora
    ) {

        mensaje = "";

        if (idBitacora == null) {
            return null;
        }

        try {

            return bitacoraDAO.buscar(
                    idBitacora
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "el registro de bitacora.";

            return null;
        }
    }

    public List<Bitacora> listar(
            String criterio
    ) {

        mensaje = "";

        try {

            return bitacoraDAO.listar(
                    criterio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los registros de bitacora.";

            return new ArrayList<>();
        }
    }

    public List<Bitacora> listarPorUsuario(
            Long idUsuario
    ) {

        mensaje = "";

        if (idUsuario == null) {

            mensaje
                    = "Seleccione un usuario.";

            return new ArrayList<>();
        }

        try {

            return bitacoraDAO.listarPorUsuario(
                    idUsuario
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los registros del usuario.";

            return new ArrayList<>();
        }
    }

    public List<Bitacora> listarPorModulo(
            String modulo
    ) {

        mensaje = "";

        if (modulo == null
                || modulo.isBlank()) {

            mensaje
                    = "Ingrese el modulo.";

            return new ArrayList<>();
        }

        try {

            return bitacoraDAO.listarPorModulo(
                    modulo.trim().toUpperCase()
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los registros del modulo.";

            return new ArrayList<>();
        }
    }

    public List<Bitacora> listarPorFechas(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) {

        mensaje = "";

        if (fechaInicio == null
                || fechaFin == null) {

            mensaje
                    = "Ingrese el rango de fechas.";

            return new ArrayList<>();
        }

        if (fechaFin.isBefore(
                fechaInicio
        )) {

            mensaje
                    = "La fecha final no puede ser menor "
                    + "que la fecha inicial.";

            return new ArrayList<>();
        }

        try {

            return bitacoraDAO.listarPorFechas(
                    fechaInicio,
                    fechaFin
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los registros por fecha.";

            return new ArrayList<>();
        }
    }

    private void validarBitacora(
            Bitacora bitacora
    ) {

        if (bitacora == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos de bitacora."
            );
        }

        if (bitacora.getFechaHora() == null) {

            throw new IllegalArgumentException(
                    "La fecha y hora son obligatorias."
            );
        }

        if (bitacora.getFechaHora().isAfter(
                LocalDateTime.now().plusMinutes(
                        1
                )
        )) {

            throw new IllegalArgumentException(
                    "La fecha de bitacora no puede ser futura."
            );
        }

        validarTextoObligatorio(
                bitacora.getAccionRealizada(),
                "La accion realizada es obligatoria.",
                150
        );

        validarTextoObligatorio(
                bitacora.getModulo(),
                "El modulo es obligatorio.",
                150
        );

        validarTextoOpcional(
                bitacora.getDireccionIp(),
                "La direccion IP no debe superar 45 caracteres.",
                45
        );

        validarTextoOpcional(
                bitacora.getDescripcion(),
                "La descripcion no debe superar 1000 caracteres.",
                1000
        );

        validarTextoOpcional(
                bitacora.getResultado(),
                "El resultado no debe superar 1000 caracteres.",
                1000
        );

        if (bitacora.getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "No existe un usuario para registrar la bitacora."
            );
        }
    }

    private void validarTextoObligatorio(
            String texto,
            String mensajeError,
            int maximo
    ) {

        if (texto == null
                || texto.isBlank()) {

            throw new IllegalArgumentException(
                    mensajeError
            );
        }

        if (texto.length() > maximo) {

            throw new IllegalArgumentException(
                    mensajeError
            );
        }
    }

    private void validarTextoOpcional(
            String texto,
            String mensajeError,
            int maximo
    ) {

        if (texto != null
                && texto.length() > maximo) {

            throw new IllegalArgumentException(
                    mensajeError
            );
        }
    }

    private void normalizarDatos(
            Bitacora bitacora
    ) {

        if (bitacora == null) {
            return;
        }

        if (bitacora.getFechaHora() == null) {

            bitacora.setFechaHora(
                    LocalDateTime.now()
            );
        }

        if (bitacora.getModulo() != null) {

            bitacora.setModulo(
                    bitacora.getModulo()
                            .trim()
                            .toUpperCase()
            );
        }

        if (bitacora.getAccionRealizada() != null) {

            bitacora.setAccionRealizada(
                    bitacora.getAccionRealizada()
                            .trim()
                            .toUpperCase()
            );
        }

        bitacora.setDireccionIp(
                limpiarTextoOpcional(
                        bitacora.getDireccionIp()
                )
        );

        bitacora.setDescripcion(
                limpiarTextoOpcional(
                        bitacora.getDescripcion()
                )
        );

        bitacora.setResultado(
                limpiarTextoOpcional(
                        bitacora.getResultado()
                )
        );
    }

    private void completarDatosSesion(
            Bitacora bitacora
    ) {

        if (bitacora == null) {
            return;
        }

        if (bitacora.getIdUsuario() == null
                && SesionUsuario.haySesionActiva()) {

            bitacora.setIdUsuario(
                    SesionUsuario
                            .getUsuarioActual()
                            .getIdUsuario()
            );
        }

        if (bitacora.getDireccionIp() == null) {

            bitacora.setDireccionIp(
                    obtenerDireccionIp()
            );
        }
    }

    private String limpiarTextoOpcional(
            String texto
    ) {

        if (texto == null
                || texto.isBlank()) {

            return null;
        }

        return texto.trim();
    }

    private String obtenerDireccionIp() {

        try {

            return InetAddress
                    .getLocalHost()
                    .getHostAddress();

        } catch (UnknownHostException e) {

            return null;
        }
    }

    private String traducirErrorBaseDatos(
            SQLException e
    ) {

        if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

            return "No se puede registrar la bitacora "
                    + "porque el usuario no existe.";
        }

        if ("23505".equals(e.getSQLState())) {

            return "Ya existe un registro de bitacora "
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
