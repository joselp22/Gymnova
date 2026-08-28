package utilidades;

import dao.BitacoraDAO;
import java.time.LocalDateTime;
import modelo.Bitacora;
import modelo.Usuario;

/**
 * Escribe eventos en la tabla bitacora. Nunca lanza excepciones al llamador:
 * las auditorias no deben bloquear la operacion de negocio.
 */
public final class Auditoria {

    private static final BitacoraDAO DAO = new BitacoraDAO();

    private Auditoria() {
    }

    public static void registrar(String modulo, String accion,
            String descripcion, String resultado) {
        Long idUsuario = idUsuarioSesion();
        if (idUsuario == null) {
            return; // Sin sesion no hay a quien atribuir el evento.
        }
        try {
            Bitacora bitacora = new Bitacora();
            bitacora.setFechaHora(LocalDateTime.now());
            bitacora.setModulo(modulo == null ? "SISTEMA" : modulo);
            bitacora.setAccionRealizada(accion == null ? "OPERACION" : accion);
            bitacora.setDescripcion(descripcion);
            bitacora.setResultado(resultado == null ? "EXITOSO" : resultado);
            bitacora.setDireccionIp(direccionLocal());
            bitacora.setIdUsuario(idUsuario);
            DAO.registrar(bitacora);
        } catch (Exception ignorado) {
            // Auditoria silenciosa: no debe interrumpir el flujo principal.
        }
    }

    /** Registra un evento vinculado a un usuario concreto (util para login). */
    public static void registrarPorUsuario(Long idUsuario, String modulo,
            String accion, String descripcion, String resultado) {
        if (idUsuario == null) {
            return;
        }
        try {
            Bitacora bitacora = new Bitacora();
            bitacora.setFechaHora(LocalDateTime.now());
            bitacora.setModulo(modulo == null ? "SISTEMA" : modulo);
            bitacora.setAccionRealizada(accion == null ? "OPERACION" : accion);
            bitacora.setDescripcion(descripcion);
            bitacora.setResultado(resultado == null ? "EXITOSO" : resultado);
            bitacora.setDireccionIp(direccionLocal());
            bitacora.setIdUsuario(idUsuario);
            DAO.registrar(bitacora);
        } catch (Exception ignorado) {
        }
    }

    public static void exito(String modulo, String accion, String descripcion) {
        registrar(modulo, accion, descripcion, "EXITOSO");
    }

    public static void fallo(String modulo, String accion,
            String descripcion, String error) {
        registrar(modulo, accion, descripcion,
                "FALLIDO: " + (error == null ? "sin detalle" : error));
    }

    private static Long idUsuarioSesion() {
        if (!SesionUsuario.haySesionActiva()) {
            return null;
        }
        Usuario usuario = SesionUsuario.getUsuarioActual();
        return usuario.getIdUsuario() == null ? null : usuario.getIdUsuario();
    }

    private static String direccionLocal() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            return "127.0.0.1";
        }
    }
}
