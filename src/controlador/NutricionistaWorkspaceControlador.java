package controlador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Alimento;
import modelo.Cliente;
import modelo.EvaluacionFisica;
import modelo.IncluyeAlimento;
import modelo.IndicadorSalud;
import modelo.MedicionCorporal;
import modelo.Nutricionista;
import modelo.PlanNutricional;
import modelo.Recomendacion;
import modelo.ResultadoIndicador;
import modelo.Usuario;
import utilidades.ClienteEnAtencion;
import utilidades.SeguridadClave;
import utilidades.SesionUsuario;

/**
 * Fachada del espacio de trabajo del Nutricionista.
 *
 * Centraliza el alcance del rol para que las vistas no conozcan detalles de
 * DAO ni puedan operar sobre clientes/profesionales que no corresponden.
 */
public class NutricionistaWorkspaceControlador {

    private final ClienteControlador clienteControlador = new ClienteControlador();
    private final PlanNutricionalControlador planControlador = new PlanNutricionalControlador();
    private final IncluyeAlimentoControlador incluyeControlador = new IncluyeAlimentoControlador();
    private final AlimentoControlador alimentoControlador = new AlimentoControlador();
    private final IndicadorSaludControlador indicadorControlador = new IndicadorSaludControlador();
    private final ResultadoIndicadorControlador resultadoControlador = new ResultadoIndicadorControlador();
    private final RecomendacionControlador recomendacionControlador = new RecomendacionControlador();
    private final MedicionCorporalControlador medicionControlador = new MedicionCorporalControlador();
    private final EvaluacionFisicaControlador evaluacionControlador = new EvaluacionFisicaControlador();
    private final NutricionistaControlador nutricionistaControlador = new NutricionistaControlador();
    private final FlujoNutricionistaControlador flujoNutricionista = new FlujoNutricionistaControlador();
    private final UsuarioControlador usuarioControlador = new UsuarioControlador();

    private String mensaje = "";

    public Long idNutricionistaActual() {
        if (!SesionUsuario.haySesionActiva()) {
            throw new IllegalStateException("No existe una sesion activa.");
        }
        Usuario usuario = SesionUsuario.getUsuarioActual();
        if (!"NUTRICIONISTA".equalsIgnoreCase(usuario.getNombreRol())) {
            throw new SecurityException("La operacion requiere el rol Nutricionista.");
        }
        if (usuario.getIdPersona() == null) {
            throw new IllegalStateException("La cuenta no tiene un profesional vinculado.");
        }
        return usuario.getIdPersona();
    }

    public Nutricionista perfilActual() {
        mensaje = "";
        Nutricionista n = nutricionistaControlador.buscar(idNutricionistaActual());
        if (n == null) {
            mensaje = nutricionistaControlador.getMensaje().isBlank()
                    ? "No se encontro el perfil profesional del nutricionista."
                    : nutricionistaControlador.getMensaje();
        }
        return n;
    }

    public List<Cliente> listarMisClientes(String criterio) {
        mensaje = "";
        List<Cliente> lista = clienteControlador.listarPorNutricionista(
                idNutricionistaActual(), criterio == null ? "" : criterio);
        if (lista.isEmpty() && !clienteControlador.getMensaje().isBlank()) {
            mensaje = clienteControlador.getMensaje();
        }
        return lista;
    }

    public boolean seleccionarCliente(Cliente cliente) {
        mensaje = "";
        if (cliente == null || cliente.getIdPersona() == null) {
            mensaje = "Seleccione un cliente valido.";
            return false;
        }
        boolean permitido = listarMisClientes("").stream()
                .anyMatch(c -> c.getIdPersona().equals(cliente.getIdPersona()));
        if (!permitido) {
            mensaje = "El cliente no pertenece al alcance del nutricionista actual.";
            return false;
        }
        ClienteEnAtencion.seleccionar(cliente);
        mensaje = "Cliente seleccionado: " + cliente.getNombreCompleto() + ".";
        return true;
    }

    public Cliente clienteActual() {
        return ClienteEnAtencion.actual();
    }

    public boolean hayClienteActual() {
        return ClienteEnAtencion.hay();
    }

    public List<PlanNutricional> listarPlanesClienteActual() {
        if (!hayClienteActual()) {
            mensaje = "Seleccione primero un cliente desde Mis clientes.";
            return new ArrayList<>();
        }
        List<PlanNutricional> planes = planControlador.listarPorIdCliente(
                ClienteEnAtencion.idPersona());
        planes.removeIf(p -> p.getIdNutricionista() == null
                || !p.getIdNutricionista().equals(idNutricionistaActual()));
        planes.sort(Comparator.comparing(PlanNutricional::getFechaInicio,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return planes;
    }

    public List<PlanNutricional> listarPlanesPropios() {
        List<PlanNutricional> planes = planControlador.listarPorIdNutricionista(
                idNutricionistaActual());
        planes.sort(Comparator.comparing(PlanNutricional::getFechaInicio,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return planes;
    }

    public PlanNutricional planActivo(Long idCliente) {
        if (idCliente == null) {
            return null;
        }
        return planControlador.listarPorIdCliente(idCliente).stream()
                .filter(p -> idNutricionistaActual().equals(p.getIdNutricionista()))
                .filter(p -> "ACTIVO".equalsIgnoreCase(p.getEstadoPlan()))
                .findFirst().orElse(null);
    }

    public PlanNutricional planActivoClienteActual() {
        return listarPlanesClienteActual().stream()
                .filter(p -> "ACTIVO".equalsIgnoreCase(p.getEstadoPlan()))
                .findFirst().orElse(null);
    }

    public Long crearPlan(String nombre, LocalDate inicio, LocalDate fin,
            Integer calorias, BigDecimal proteinas, BigDecimal carbohidratos,
            String restricciones) {
        mensaje = "";
        if (!hayClienteActual()) {
            mensaje = "Seleccione primero un cliente desde Mis clientes.";
            return null;
        }
        if (planActivoClienteActual() != null) {
            mensaje = "El cliente ya posee un plan nutricional activo. Finalicelo antes de crear otro.";
            return null;
        }
        Long id = flujoNutricionista.crearPlanNutricional(
                ClienteEnAtencion.idPersona(), idNutricionistaActual(),
                nombre, inicio, fin, calorias, proteinas, carbohidratos,
                restricciones);
        mensaje = id != null ? "Plan nutricional creado correctamente."
                : flujoNutricionista.getMensaje();
        return id;
    }

    public boolean modificarPlan(PlanNutricional plan) {
        mensaje = "";
        if (plan == null || plan.getIdPlanNutricional() == null) {
            mensaje = "Seleccione un plan para modificar.";
            return false;
        }
        if (!idNutricionistaActual().equals(plan.getIdNutricionista())) {
            mensaje = "Solo puede modificar planes creados por su perfil profesional.";
            return false;
        }
        if (!hayClienteActual() || !ClienteEnAtencion.idPersona().equals(plan.getIdCliente())) {
            mensaje = "El plan no corresponde al cliente actualmente seleccionado.";
            return false;
        }
        boolean ok = planControlador.modificar(plan);
        mensaje = ok ? "Plan nutricional modificado correctamente." : planControlador.getMensaje();
        return ok;
    }

    public boolean finalizarPlan(Long idPlan) {
        mensaje = "";
        PlanNutricional activo = planActivoClienteActual();
        if (activo == null || idPlan == null || !idPlan.equals(activo.getIdPlanNutricional())) {
            mensaje = "Seleccione el plan activo del cliente para finalizarlo.";
            return false;
        }
        boolean ok = flujoNutricionista.finalizarPlan(idPlan);
        mensaje = ok ? "Plan nutricional finalizado correctamente."
                : flujoNutricionista.getMensaje();
        return ok;
    }

    public List<Alimento> listarAlimentos(String criterio) {
        return alimentoControlador.listar(criterio == null ? "" : criterio);
    }

    public List<IncluyeAlimento> listarComidasPlanActivo() {
        PlanNutricional plan = planActivoClienteActual();
        if (plan == null) {
            mensaje = "El cliente no tiene un plan activo.";
            return new ArrayList<>();
        }
        return incluyeControlador.listarPorIdPlanNutricional(plan.getIdPlanNutricional());
    }

    public Long agregarComida(String dia, String tipoComida, LocalTime hora,
            BigDecimal cantidad, String unidad, Integer orden,
            Long idAlimento, String indicaciones) {
        mensaje = "";
        PlanNutricional plan = planActivoClienteActual();
        if (plan == null) {
            mensaje = "El cliente necesita un plan activo antes de agregar alimentos.";
            return null;
        }
        if (idAlimento == null || idAlimento > Integer.MAX_VALUE) {
            mensaje = "Seleccione un alimento valido.";
            return null;
        }
        Long id = flujoNutricionista.agregarComidaAlPlan(
                plan.getIdPlanNutricional(), dia, tipoComida, hora,
                cantidad, unidad, orden, idAlimento.intValue(), indicaciones);
        mensaje = id != null ? "Alimento agregado al plan correctamente."
                : flujoNutricionista.getMensaje();
        return id;
    }

    public boolean modificarComida(IncluyeAlimento detalle) {
        mensaje = "";
        PlanNutricional plan = planActivoClienteActual();
        if (detalle == null || plan == null
                || !plan.getIdPlanNutricional().equals(detalle.getIdPlanNutricional())) {
            mensaje = "Seleccione un detalle del plan activo.";
            return false;
        }
        boolean ok = incluyeControlador.modificar(detalle);
        mensaje = ok ? "Detalle de alimentacion modificado correctamente."
                : incluyeControlador.getMensaje();
        return ok;
    }

    public List<IndicadorSalud> listarIndicadoresActivos() {
        List<IndicadorSalud> indicadores = indicadorControlador.listar("");
        indicadores.removeIf(i -> !i.isEstadoIndicador());
        return indicadores;
    }

    public EvaluacionFisica evaluacionActualCliente() {
        if (!hayClienteActual()) {
            return null;
        }
        List<EvaluacionFisica> lista = evaluacionControlador.listarPorIdCliente(
                ClienteEnAtencion.idPersona());
        return lista.stream()
                .filter(e -> !"ANULADA".equalsIgnoreCase(e.getEstadoEvaluacion()))
                .max(Comparator.comparing(EvaluacionFisica::getFechaEvaluacion,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
    }

    private EvaluacionFisica exigirEvaluacion() {
        if (!hayClienteActual()) {
            mensaje = "Seleccione primero un cliente desde Mis clientes.";
            return null;
        }
        EvaluacionFisica evaluacion = evaluacionActualCliente();
        if (evaluacion == null) {
            mensaje = "El cliente no posee una evaluacion fisica vigente. El Entrenador debe registrar una evaluacion antes de guardar mediciones, indicadores o recomendaciones.";
        }
        return evaluacion;
    }

    public boolean registrarMedicion(BigDecimal peso, BigDecimal altura,
            BigDecimal grasa, BigDecimal cintura, String observaciones) {
        mensaje = "";
        EvaluacionFisica evaluacion = exigirEvaluacion();
        if (evaluacion == null) {
            return false;
        }
        MedicionCorporal m = new MedicionCorporal();
        m.setIdEvaluacion(evaluacion.getIdEvaluacion());
        m.setPesoKg(peso);
        m.setAlturaM(altura);
        m.setPorcentajeGrasa(grasa);
        m.setCinturaCm(cintura);
        m.setObservaciones(observaciones);
        boolean ok = medicionControlador.registrar(m);
        mensaje = ok ? "Medicion corporal registrada correctamente."
                : medicionControlador.getMensaje();
        return ok;
    }

    public List<MedicionCorporal> listarMedicionesClienteActual() {
        List<MedicionCorporal> salida = new ArrayList<>();
        if (!hayClienteActual()) {
            return salida;
        }
        for (EvaluacionFisica e : evaluacionesClienteActual()) {
            salida.addAll(medicionControlador.listarPorIdEvaluacion(e.getIdEvaluacion()));
        }
        return salida;
    }

    public Long registrarResultado(Long idIndicador, BigDecimal valor,
            String clasificacion, String observaciones) {
        mensaje = "";
        EvaluacionFisica evaluacion = exigirEvaluacion();
        if (evaluacion == null) {
            return null;
        }
        if (idIndicador == null || idIndicador > Integer.MAX_VALUE) {
            mensaje = "Seleccione un indicador valido.";
            return null;
        }
        Long id = flujoNutricionista.registrarResultadoIndicador(
                evaluacion.getIdEvaluacion(), idIndicador.intValue(), valor,
                clasificacion, false, observaciones);
        mensaje = id != null ? "Resultado registrado correctamente."
                : flujoNutricionista.getMensaje();
        return id;
    }

    public List<ResultadoIndicador> listarResultadosClienteActual() {
        List<ResultadoIndicador> salida = new ArrayList<>();
        for (EvaluacionFisica e : evaluacionesClienteActual()) {
            salida.addAll(resultadoControlador.listarPorIdEvaluacion(e.getIdEvaluacion()));
        }
        salida.sort(Comparator.comparing(ResultadoIndicador::getFechaRegistro,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return salida;
    }

    public boolean registrarRecomendacion(String tipo, String titulo,
            String descripcion, String prioridad, LocalDate inicio,
            LocalDate fin) {
        mensaje = "";
        EvaluacionFisica evaluacion = exigirEvaluacion();
        if (evaluacion == null) {
            return false;
        }
        Recomendacion r = new Recomendacion();
        r.setIdEvaluacion(evaluacion.getIdEvaluacion());
        r.setFechaRecomendacion(LocalDate.now());
        r.setTipoRecomendacion(tipo);
        r.setTitulo(titulo);
        r.setDescripcion(descripcion);
        r.setPrioridad(prioridad);
        r.setFechaInicio(inicio);
        r.setFechaFin(fin);
        r.setEstadoRecomendacion("ACTIVA");
        boolean ok = recomendacionControlador.registrar(r);
        mensaje = ok ? "Recomendacion registrada correctamente."
                : recomendacionControlador.getMensaje();
        return ok;
    }

    public List<Recomendacion> listarRecomendacionesClienteActual() {
        List<Recomendacion> salida = new ArrayList<>();
        for (EvaluacionFisica e : evaluacionesClienteActual()) {
            salida.addAll(recomendacionControlador.listarPorIdEvaluacion(e.getIdEvaluacion()));
        }
        salida.sort(Comparator.comparing(Recomendacion::getFechaRecomendacion,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return salida;
    }

    private List<EvaluacionFisica> evaluacionesClienteActual() {
        if (!hayClienteActual()) {
            return new ArrayList<>();
        }
        return evaluacionControlador.listarPorIdCliente(ClienteEnAtencion.idPersona());
    }

    public ResumenDashboard resumenDashboard() {
        List<Cliente> clientes = listarMisClientes("");
        List<PlanNutricional> planes = listarPlanesPropios();
        long activos = planes.stream().filter(p -> "ACTIVO".equalsIgnoreCase(p.getEstadoPlan())).count();
        long finalizados = planes.stream().filter(p -> "FINALIZADO".equalsIgnoreCase(p.getEstadoPlan())).count();
        int seguimientos = 0;
        int recomendaciones = 0;
        for (Cliente cliente : clientes) {
            Cliente anterior = ClienteEnAtencion.actual();
            ClienteEnAtencion.seleccionar(cliente);
            seguimientos += listarResultadosClienteActual().size();
            recomendaciones += listarRecomendacionesClienteActual().size();
            ClienteEnAtencion.seleccionar(anterior);
        }
        return new ResumenDashboard(clientes.size(), (int) activos,
                (int) finalizados, seguimientos, recomendaciones);
    }


    public List<ResultadoReporte> listarResultadosPropios() {
        List<ResultadoReporte> salida = new ArrayList<>();
        Cliente anterior = ClienteEnAtencion.actual();
        try {
            for (Cliente c : listarMisClientes("")) {
                ClienteEnAtencion.seleccionar(c);
                for (ResultadoIndicador r : listarResultadosClienteActual()) {
                    salida.add(new ResultadoReporte(c.getCodigoCliente(), c.getNombreCompleto(),
                            r.getFechaRegistro(), nombreIndicador(r.getIdIndicador()),
                            r.getValorObtenido(), r.getClasificacion(), r.isFueraDeRango()));
                }
            }
        } finally {
            ClienteEnAtencion.seleccionar(anterior);
        }
        salida.sort(Comparator.comparing(ResultadoReporte::fecha,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return salida;
    }

    public List<RecomendacionReporte> listarRecomendacionesPropias() {
        List<RecomendacionReporte> salida = new ArrayList<>();
        Cliente anterior = ClienteEnAtencion.actual();
        try {
            for (Cliente c : listarMisClientes("")) {
                ClienteEnAtencion.seleccionar(c);
                for (Recomendacion r : listarRecomendacionesClienteActual()) {
                    salida.add(new RecomendacionReporte(c.getCodigoCliente(), c.getNombreCompleto(),
                            r.getFechaRecomendacion(), r.getTipoRecomendacion(), r.getTitulo(),
                            r.getPrioridad(), r.getEstadoRecomendacion()));
                }
            }
        } finally {
            ClienteEnAtencion.seleccionar(anterior);
        }
        salida.sort(Comparator.comparing(RecomendacionReporte::fecha,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return salida;
    }

    public Map<String, Integer> resumenReportes() {
        ResumenDashboard r = resumenDashboard();
        Map<String, Integer> datos = new LinkedHashMap<>();
        datos.put("Clientes en alcance", r.clientes());
        datos.put("Planes activos", r.planesActivos());
        datos.put("Planes finalizados", r.planesFinalizados());
        datos.put("Resultados registrados", r.resultados());
        datos.put("Recomendaciones", r.recomendaciones());
        return datos;
    }

    public String nombreCliente(Long idCliente) {
        Cliente c = clienteControlador.buscar(idCliente);
        return c == null ? "Cliente " + idCliente : c.getNombreCompleto();
    }

    public String nombreAlimento(Long idAlimento) {
        Alimento a = alimentoControlador.buscar(idAlimento);
        return a == null ? "Alimento " + idAlimento : a.getNombreAlimento();
    }

    /**
     * Devuelve el id del alimento cuyo nombre coincide, o crea uno nuevo si
     * no existe. Sirve para que el panel de Alimentación permita al
     * nutricionista escribir el nombre libremente en vez de elegir del
     * combo. Los campos nutricionales quedan vacíos (se pueden completar
     * después desde el catálogo).
     */
    public Long obtenerOCrearAlimento(String nombre) {
        mensaje = "";
        if (nombre == null || nombre.isBlank()) {
            mensaje = "Escriba el nombre del alimento.";
            return null;
        }
        try {
            dao.AlimentoDAO dao = new dao.AlimentoDAO();
            modelo.Alimento existente = dao.buscarPorNombre(nombre.trim());
            if (existente != null) {
                return existente.getIdAlimento();
            }
            modelo.Alimento nuevo = new modelo.Alimento();
            nuevo.setNombreAlimento(nombre.trim());
            if (dao.guardar(nuevo) && nuevo.getIdAlimento() != null) {
                return nuevo.getIdAlimento();
            }
            mensaje = "No se pudo registrar el nuevo alimento.";
            return null;
        } catch (java.sql.SQLException ex) {
            mensaje = "No se pudo registrar el alimento: " + ex.getMessage();
            return null;
        }
    }

    public String nombreIndicador(Long idIndicador) {
        IndicadorSalud i = indicadorControlador.buscar(idIndicador);
        return i == null ? "Indicador " + idIndicador : i.getNombreIndicador();
    }

    public boolean cambiarClave(char[] actual, char[] nueva, char[] confirmacion) {
        mensaje = "";
        try {
            Usuario sesion = SesionUsuario.getUsuarioActual();
            if (actual == null || !SeguridadClave.verificar(actual, sesion.getClaveHash())) {
                mensaje = "La contrasena actual no es correcta.";
                return false;
            }
            if (nueva == null || confirmacion == null
                    || !java.util.Arrays.equals(nueva, confirmacion)) {
                mensaje = "La nueva contrasena y su confirmacion no coinciden.";
                return false;
            }
            boolean ok = usuarioControlador.cambiarClave(sesion.getIdUsuario(), nueva);
            if (!ok) {
                mensaje = usuarioControlador.getMensaje();
                return false;
            }
            Usuario actualizado = usuarioControlador.buscar(sesion.getIdUsuario());
            if (actualizado != null) {
                SesionUsuario.iniciarSesion(actualizado);
            }
            mensaje = "Contrasena actualizada correctamente.";
            return true;
        } finally {
            if (actual != null) java.util.Arrays.fill(actual, '\0');
            if (nueva != null) java.util.Arrays.fill(nueva, '\0');
            if (confirmacion != null) java.util.Arrays.fill(confirmacion, '\0');
        }
    }

    public String getMensaje() {
        return mensaje == null ? "" : mensaje;
    }


    public record ResultadoReporte(String codigoCliente, String cliente,
            LocalDate fecha, String indicador, BigDecimal valor,
            String clasificacion, boolean fueraRango) {
    }

    public record RecomendacionReporte(String codigoCliente, String cliente,
            LocalDate fecha, String tipo, String titulo, String prioridad,
            String estado) {
    }

    public record ResumenDashboard(int clientes, int planesActivos,
            int planesFinalizados, int resultados, int recomendaciones) {
    }
}
