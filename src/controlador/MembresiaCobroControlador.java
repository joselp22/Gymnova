package controlador;

import dao.MembresiaCobroDAO;
import dao.MembresiaDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.Cliente;
import modelo.Membresia;
import modelo.MembresiaCobroResumen;
import modelo.MetodoPago;
import modelo.TipoMembresia;
import utilidades.SesionUsuario;

/**
 * Fachada de aplicacion para la nueva pantalla de venta/renovacion de
 * membresias. Centraliza permisos y evita que la vista cree membresias sin
 * su movimiento financiero asociado.
 */
public class MembresiaCobroControlador {

    private final ClienteControlador clienteControlador = new ClienteControlador();
    private final TipoMembresiaControlador tipoControlador = new TipoMembresiaControlador();
    private final MetodoPagoControlador metodoControlador = new MetodoPagoControlador();
    private final MembresiaDAO membresiaDAO = new MembresiaDAO();
    private final MembresiaCobroDAO cobroDAO = new MembresiaCobroDAO();
    private final FlujoRecepcionControlador flujo = new FlujoRecepcionControlador();
    private final AutorizacionControlador autorizacion = new AutorizacionControlador();
    private String mensaje = "";

    public String getMensaje() { return mensaje; }

    public List<Cliente> listarClientesActivos(String criterio) {
        List<Cliente> lista = clienteControlador.listar(criterio == null ? "" : criterio);
        List<Cliente> activos = new ArrayList<>();
        for (Cliente cliente : lista) {
            if (cliente != null && cliente.isEstadoCliente() && cliente.isEstado()) {
                activos.add(cliente);
            }
        }
        mensaje = clienteControlador.getMensaje();
        return activos;
    }

    public List<TipoMembresia> listarPlanesActivos() {
        List<TipoMembresia> resultado = new ArrayList<>();
        for (TipoMembresia plan : tipoControlador.listar("")) {
            if (plan != null && plan.isEstadoTipo()) {
                resultado.add(plan);
            }
        }
        mensaje = tipoControlador.getMensaje();
        return resultado;
    }

    public List<MetodoPago> listarMetodosActivos() {
        List<MetodoPago> resultado = new ArrayList<>();
        for (MetodoPago metodo : metodoControlador.listar("")) {
            if (metodo != null && metodo.isEstadoMetodo()) {
                resultado.add(metodo);
            }
        }
        mensaje = metodoControlador.getMensaje();
        return resultado;
    }

    public Membresia obtenerUltimaMembresia(Long idCliente) {
        mensaje = "";
        try {
            return membresiaDAO.buscarUltimaPorCliente(idCliente);
        } catch (SQLException ex) {
            mensaje = "No se pudo consultar la membresia actual: " + ex.getMessage();
            return null;
        }
    }

    public List<MembresiaCobroResumen> listarHistorial(Long idCliente) {
        mensaje = "";
        try {
            return cobroDAO.listarPorCliente(idCliente);
        } catch (SQLException ex) {
            mensaje = "No se pudo consultar el historial de cobros: " + ex.getMessage();
            return new ArrayList<>();
        }
    }

    public LocalDate fechaInicioSugerida(Membresia ultima) {
        LocalDate hoy = LocalDate.now();
        if (ultima == null || ultima.getFechaFin() == null) {
            return hoy;
        }
        String estado = ultima.getEstadoMembresia() == null
                ? "" : ultima.getEstadoMembresia().trim().toUpperCase();
        boolean vigente = !ultima.getFechaFin().isBefore(hoy)
                && ("ACTIVA".equals(estado) || "CONGELADA".equals(estado)
                || "PENDIENTE".equals(estado));
        return vigente ? ultima.getFechaFin().plusDays(1) : hoy;
    }

    public boolean esRenovacionProgramada(Membresia ultima) {
        if (ultima == null || ultima.getFechaFin() == null) {
            return false;
        }
        String estado = ultima.getEstadoMembresia() == null
                ? "" : ultima.getEstadoMembresia().trim().toUpperCase();
        return !ultima.getFechaFin().isBefore(LocalDate.now())
                && ("ACTIVA".equals(estado) || "CONGELADA".equals(estado)
                || "PENDIENTE".equals(estado));
    }

    public Long cobrar(Long idCliente, TipoMembresia plan, LocalDate inicio,
            String observaciones, MetodoPago metodo, String referencia) {
        mensaje = "";
        if (!autorizacion.tienePermiso("MEMBRESIAS", "CREAR")) {
            mensaje = autorizacion.getMensaje();
            return null;
        }
        if (idCliente == null || plan == null || plan.getIdTipoMembresia() == null
                || inicio == null || metodo == null || metodo.getIdMetodoPago() == null) {
            mensaje = "Seleccione cliente, plan, fecha de inicio y metodo de pago.";
            return null;
        }
        String ref = referencia == null ? "" : referencia.trim();
        if (metodo.isRequiereReferencia() && ref.isBlank()) {
            mensaje = "El metodo '" + metodo.getNombreMetodo()
                    + "' requiere una referencia de transaccion.";
            return null;
        }
        if (plan.getPrecioBase() == null || plan.getPrecioBase().signum() < 0) {
            mensaje = "El plan seleccionado no tiene un precio valido.";
            return null;
        }

        Membresia ultima;
        try {
            ultima = membresiaDAO.buscarUltimaPorCliente(idCliente);
        } catch (SQLException ex) {
            mensaje = "No se pudo verificar el historial del cliente: " + ex.getMessage();
            return null;
        }
        Long id;
        if (ultima == null) {
            id = flujo.crearMembresiaConPago(idCliente, plan.getIdTipoMembresia(),
                    inicio, observaciones,
                    metodo.getIdMetodoPago().intValue(), ref.isBlank() ? null : ref);
        } else {
            id = flujo.renovarMembresiaConPago(ultima.getIdMembresia(),
                    plan.getIdTipoMembresia(), inicio, observaciones,
                    metodo.getIdMetodoPago().intValue(), ref.isBlank() ? null : ref);
        }
        mensaje = flujo.getMensaje();
        return id;
    }

    public boolean congelar(Long idMembresia, LocalDate inicio,
            LocalDate fin, String motivo) {
        mensaje = "";
        if (!autorizacion.tienePermiso("MEMBRESIAS", "MODIFICAR")) {
            mensaje = autorizacion.getMensaje();
            return false;
        }
        if (motivo == null || motivo.isBlank()) {
            mensaje = "Ingrese el motivo de la congelacion.";
            return false;
        }
        boolean ok = flujo.congelarMembresia(idMembresia, inicio, fin, motivo.trim());
        mensaje = flujo.getMensaje();
        return ok;
    }

    public boolean reactivar(Long idMembresia) {
        mensaje = "";
        if (!autorizacion.tienePermiso("MEMBRESIAS", "MODIFICAR")) {
            mensaje = autorizacion.getMensaje();
            return false;
        }
        boolean ok = flujo.reactivarMembresia(idMembresia);
        mensaje = flujo.getMensaje();
        return ok;
    }

    public boolean cancelar(Long idMembresia, String motivo) {
        mensaje = "";
        if (!autorizacion.tienePermiso("MEMBRESIAS", "DESACTIVAR")) {
            mensaje = autorizacion.getMensaje();
            return false;
        }
        boolean ok = flujo.cancelarMembresia(idMembresia, motivo);
        mensaje = flujo.getMensaje();
        return ok;
    }

    public boolean actualizarPrecioPlan(TipoMembresia plan,
            BigDecimal nuevoPrecio, String descripcion) {
        mensaje = "";
        if (!esAdministrador()) {
            mensaje = "Solo el Administrador puede modificar el precio de los planes.";
            return false;
        }
        if (!autorizacion.tienePermiso("MEMBRESIAS", "MODIFICAR")) {
            mensaje = autorizacion.getMensaje();
            return false;
        }
        if (plan == null || nuevoPrecio == null || nuevoPrecio.signum() < 0) {
            mensaje = "Seleccione un plan e ingrese un precio valido.";
            return false;
        }
        TipoMembresia copia = new TipoMembresia();
        copia.setIdTipoMembresia(plan.getIdTipoMembresia());
        copia.setNombre(plan.getNombre());
        copia.setDescripcion(descripcion == null ? null : descripcion.trim());
        copia.setDuracionDias(plan.getDuracionDias());
        copia.setPrecioBase(nuevoPrecio);
        copia.setLimiteAccesos(plan.getLimiteAccesos());
        copia.setAccesoIlimitado(plan.isAccesoIlimitado());
        copia.setEstadoTipo(plan.isEstadoTipo());
        boolean ok = tipoControlador.modificar(copia);
        mensaje = tipoControlador.getMensaje();
        return ok;
    }

    public boolean esAdministrador() {
        return SesionUsuario.haySesionActiva()
                && "ADMINISTRADOR".equalsIgnoreCase(
                        SesionUsuario.getUsuarioActual().getNombreRol());
    }
}
