package controlador;

import dao.ClaseGrupalGestionDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.ClaseGrupal;
import modelo.ClaseGrupalResumen;
import modelo.ParticipanteClaseGrupal;
import utilidades.NavegacionRol;
import utilidades.SesionUsuario;

/**
 * Controlador del nuevo flujo de clases grupales por rol.
 */
public class ClaseGrupalGestionControlador {

    private final ClaseGrupalGestionDAO dao = new ClaseGrupalGestionDAO();
    private final ClaseGrupalControlador claseControlador = new ClaseGrupalControlador();
    private final FlujoRecepcionControlador flujo = new FlujoRecepcionControlador();
    private String mensaje = "";

    public boolean crearClase(ClaseGrupal clase) {
        mensaje = "";
        if (!esRol("ADMINISTRADOR")) {
            mensaje = "Solo el Administrador puede crear clases grupales.";
            return false;
        }
        boolean ok = claseControlador.registrar(clase);
        mensaje = claseControlador.getMensaje();
        return ok;
    }

    public boolean actualizarClase(ClaseGrupal clase) {
        mensaje = "";
        if (!esRol("ADMINISTRADOR")) {
            mensaje = "Solo el Administrador puede actualizar clases grupales.";
            return false;
        }
        try {
            if (clase == null || clase.getIdClase() == null) {
                mensaje = "Seleccione una clase existente.";
                return false;
            }
            int ocupados = dao.contarReservasActivas(clase.getIdClase());
            if (clase.getCupoMaximo() != null && clase.getCupoMaximo() < ocupados) {
                mensaje = "El cupo no puede ser menor a los " + ocupados
                        + " participantes que ya están inscritos.";
                return false;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo validar el cupo actual: " + ex.getMessage();
            return false;
        }
        boolean ok = claseControlador.modificar(clase);
        mensaje = claseControlador.getMensaje();
        return ok;
    }

    public boolean desactivarClase(Long idClase) {
        mensaje = "";
        if (!esRol("ADMINISTRADOR")) {
            mensaje = "Solo el Administrador puede desactivar clases grupales.";
            return false;
        }
        boolean ok = claseControlador.desactivar(idClase);
        mensaje = claseControlador.getMensaje();
        return ok;
    }

    public List<ClaseGrupalResumen> listarAdministracion(String criterio) {
        mensaje = "";
        try {
            return dao.listarAdministracion(criterio);
        } catch (SQLException ex) {
            mensaje = "No se pudieron consultar las clases: " + ex.getMessage();
            return new ArrayList<>();
        }
    }

    public List<ClaseGrupalResumen> listarMisClasesEntrenador() {
        mensaje = "";
        Long id = idPersonaSesion("ENTRENADOR");
        if (id == null) return new ArrayList<>();
        try {
            return dao.listarPorEntrenador(id);
        } catch (SQLException ex) {
            mensaje = "No se pudieron consultar tus clases: " + ex.getMessage();
            return new ArrayList<>();
        }
    }

    public List<ParticipanteClaseGrupal> listarParticipantesEntrenador(Long idClase) {
        mensaje = "";
        Long idEntrenador = idPersonaSesion("ENTRENADOR");
        if (idEntrenador == null || idClase == null) return new ArrayList<>();
        try {
            if (!dao.clasePerteneceAEntrenador(idClase, idEntrenador)) {
                mensaje = "No puedes consultar participantes de una clase asignada a otro entrenador.";
                return new ArrayList<>();
            }
            return dao.listarParticipantes(idClase);
        } catch (SQLException ex) {
            mensaje = "No se pudieron consultar los participantes: " + ex.getMessage();
            return new ArrayList<>();
        }
    }

    public List<ClaseGrupalResumen> listarClasesCliente() {
        mensaje = "";
        Long idCliente = idPersonaSesion("CLIENTE");
        if (idCliente == null) return new ArrayList<>();
        try {
            return dao.listarParaCliente(idCliente);
        } catch (SQLException ex) {
            mensaje = "No se pudieron consultar las clases disponibles: " + ex.getMessage();
            return new ArrayList<>();
        }
    }

    /**
     * Asigna una clase a un cliente concreto (desde el panel de gestión del
     * administrador). Delega en el flujo de recepción existente para respetar
     * validaciones de cupo, fecha futura, clase activa y unicidad de reservas
     * activas por (cliente, clase).
     *
     * @return id de la reserva creada, o {@code null} si falló; el motivo
     *         queda disponible con {@link #getMensaje()}.
     */
    public Long reservarParaCliente(Long idCliente, Integer idClase,
            String observaciones) {
        mensaje = "";
        if (idCliente == null || idClase == null) {
            mensaje = "Cliente o clase no seleccionada.";
            return null;
        }
        String obs = observaciones == null || observaciones.isBlank()
                ? "Asignación manual desde gestión de clases"
                : observaciones;
        Long idReserva = flujo.registrarReserva(
                idCliente, idClase, null, obs);
        mensaje = flujo.getMensaje();
        return idReserva;
    }

    public boolean unirseAClase(Long idClase) {
        mensaje = "";
        Long idCliente = idPersonaSesion("CLIENTE");
        if (idCliente == null || idClase == null) {
            if (mensaje.isBlank()) mensaje = "No se pudo identificar la clase o el cliente.";
            return false;
        }
        Long idReserva = flujo.registrarReserva(
                idCliente, idClase.intValue(), null, "Inscripción desde panel del cliente");
        mensaje = flujo.getMensaje();
        if (idReserva != null) {
            mensaje = "Te uniste correctamente a la clase.";
            return true;
        }
        return false;
    }

    public boolean salirDeClase(Long idReserva) {
        mensaje = "";
        Long idCliente = idPersonaSesion("CLIENTE");
        if (idCliente == null || idReserva == null) return false;
        try {
            if (!dao.reservaPerteneceACliente(idReserva, idCliente)) {
                mensaje = "La inscripción seleccionada no pertenece a tu cuenta o ya fue cancelada.";
                return false;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo validar la inscripción: " + ex.getMessage();
            return false;
        }
        boolean ok = flujo.cancelarReserva(idReserva, "Cancelada por el cliente");
        mensaje = ok ? "Saliste correctamente de la clase." : flujo.getMensaje();
        return ok;
    }

    public List<ParticipanteClaseGrupal> listarParticipantesAdministracion(Long idClase) {
        mensaje = "";
        if (!esRol("ADMINISTRADOR") || idClase == null) return new ArrayList<>();
        try {
            return dao.listarParticipantes(idClase);
        } catch (SQLException ex) {
            mensaje = "No se pudieron consultar los participantes: " + ex.getMessage();
            return new ArrayList<>();
        }
    }

    private Long idPersonaSesion(String rolEsperado) {
        if (!SesionUsuario.haySesionActiva()
                || SesionUsuario.getUsuarioActual() == null
                || SesionUsuario.getUsuarioActual().getIdPersona() == null) {
            mensaje = "No existe una sesión válida.";
            return null;
        }
        String rol = NavegacionRol.normalizarRol(
                SesionUsuario.getUsuarioActual().getNombreRol());
        if (!rolEsperado.equals(rol)) {
            mensaje = "Esta operación no está disponible para tu rol.";
            return null;
        }
        return SesionUsuario.getUsuarioActual().getIdPersona();
    }

    private boolean esRol(String esperado) {
        return SesionUsuario.haySesionActiva()
                && SesionUsuario.getUsuarioActual() != null
                && esperado.equals(NavegacionRol.normalizarRol(
                        SesionUsuario.getUsuarioActual().getNombreRol()));
    }

    public String getMensaje() {
        return mensaje == null ? "" : mensaje;
    }
}
