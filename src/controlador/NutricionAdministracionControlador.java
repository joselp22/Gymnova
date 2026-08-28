package controlador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import modelo.Alimento;
import modelo.Cliente;
import modelo.EvaluacionFisica;
import modelo.IncluyeAlimento;
import modelo.IndicadorSalud;
import modelo.Nutricionista;
import modelo.PlanNutricional;
import modelo.Recomendacion;
import modelo.ResultadoIndicador;

/**
 * Fachada de lectura y mantenimiento para la vista administrativa de
 * Nutricion. El Administrador supervisa el trabajo profesional y solamente
 * mantiene los catalogos globales de alimentos e indicadores de salud.
 */
public class NutricionAdministracionControlador {

    public static final String VISTA_CLIENTES = "Clientes";
    public static final String VISTA_PLANES = "Planes nutricionales";
    public static final String VISTA_COMIDAS = "Comidas de los planes";
    public static final String VISTA_RESULTADOS = "Resultados de indicadores";
    public static final String VISTA_RECOMENDACIONES = "Recomendaciones";
    public static final String VISTA_NUTRICIONISTAS = "Nutricionistas";

    private final ClienteControlador clienteControlador = new ClienteControlador();
    private final NutricionistaControlador nutricionistaControlador
            = new NutricionistaControlador();
    private final PlanNutricionalControlador planControlador
            = new PlanNutricionalControlador();
    private final IncluyeAlimentoControlador incluyeControlador
            = new IncluyeAlimentoControlador();
    private final ResultadoIndicadorControlador resultadoControlador
            = new ResultadoIndicadorControlador();
    private final RecomendacionControlador recomendacionControlador
            = new RecomendacionControlador();
    private final EvaluacionFisicaControlador evaluacionControlador
            = new EvaluacionFisicaControlador();
    private final AlimentoControlador alimentoControlador
            = new AlimentoControlador();
    private final IndicadorSaludControlador indicadorControlador
            = new IndicadorSaludControlador();

    private String mensaje = "";

    public Resumen obtenerResumen() {
        mensaje = "";

        List<Cliente> clientes = clienteControlador.listar("");
        List<PlanNutricional> planes = planControlador.listar("");
        List<Nutricionista> nutricionistas = nutricionistaControlador.listar("");
        List<Alimento> alimentos = alimentoControlador.listar("");
        List<IndicadorSalud> indicadores = indicadorControlador.listar("");

        long clientesActivos = clientes.stream()
                .filter(Cliente::isEstadoCliente)
                .count();

        long planesActivos = planes.stream()
                .filter(plan -> "ACTIVO".equalsIgnoreCase(plan.getEstadoPlan()))
                .count();

        long nutricionistasActivos = nutricionistas.stream()
                .filter(Nutricionista::isEstadoEmpleado)
                .count();

        long indicadoresActivos = indicadores.stream()
                .filter(IndicadorSalud::isEstadoIndicador)
                .count();

        return new Resumen(
                clientesActivos,
                planesActivos,
                nutricionistasActivos,
                alimentos.size(),
                indicadoresActivos
        );
    }

    public String[] columnasSupervision(String vista) {
        return switch (normalizar(vista)) {
            case "CLIENTES" -> new String[]{
                "ID", "Codigo", "Cedula", "Cliente", "Registro",
                "Peso inicial", "Peso meta", "Estado"
            };
            case "PLANES NUTRICIONALES" -> new String[]{
                "ID", "Codigo", "Plan", "Cliente", "Nutricionista",
                "Inicio", "Fin", "Calorias", "Estado"
            };
            case "COMIDAS DE LOS PLANES" -> new String[]{
                "ID", "Plan", "Alimento", "Dia", "Comida",
                "Cantidad", "Unidad", "Estado"
            };
            case "RESULTADOS DE INDICADORES" -> new String[]{
                "ID", "Cliente", "Indicador", "Valor", "Clasificacion",
                "Fecha", "Fuera de rango"
            };
            case "RECOMENDACIONES" -> new String[]{
                "ID", "Cliente", "Tipo", "Titulo", "Prioridad",
                "Inicio", "Fin", "Estado"
            };
            case "NUTRICIONISTAS" -> new String[]{
                "ID", "Codigo", "Cedula", "Nutricionista", "Licencia",
                "Estado licencia", "Inicio profesion", "Estado"
            };
            default -> new String[]{"Informacion"};
        };
    }

    public List<Object[]> listarSupervision(String vista, String criterio) {
        mensaje = "";
        List<Object[]> filas = switch (normalizar(vista)) {
            case "CLIENTES" -> filasClientes();
            case "PLANES NUTRICIONALES" -> filasPlanes();
            case "COMIDAS DE LOS PLANES" -> filasComidas();
            case "RESULTADOS DE INDICADORES" -> filasResultados();
            case "RECOMENDACIONES" -> filasRecomendaciones();
            case "NUTRICIONISTAS" -> filasNutricionistas();
            default -> new ArrayList<>();
        };
        return filtrar(filas, criterio);
    }

    public List<Alimento> listarAlimentos(String criterio) {
        mensaje = "";
        List<Alimento> lista = alimentoControlador.listar(criterio);
        if (!alimentoControlador.getMensaje().isBlank()) {
            mensaje = alimentoControlador.getMensaje();
        }
        return lista;
    }

    public List<IndicadorSalud> listarIndicadores(String criterio) {
        mensaje = "";
        List<IndicadorSalud> lista = indicadorControlador.listar(criterio);
        if (!indicadorControlador.getMensaje().isBlank()) {
            mensaje = indicadorControlador.getMensaje();
        }
        return lista;
    }

    public boolean guardarAlimento(Alimento alimento, boolean modificacion) {
        mensaje = "";
        boolean ok = modificacion
                ? alimentoControlador.modificar(alimento)
                : alimentoControlador.registrar(alimento);
        mensaje = alimentoControlador.getMensaje();
        return ok;
    }

    public boolean eliminarAlimento(Long idAlimento) {
        mensaje = "";
        try {
            alimentoControlador.eliminarDefinitivamente(idAlimento);
            mensaje = "Alimento eliminado correctamente.";
            return true;
        } catch (Exception ex) {
            mensaje = mensajeExcepcion(ex);
            return false;
        }
    }

    public boolean guardarIndicador(IndicadorSalud indicador, boolean modificacion) {
        mensaje = "";
        boolean ok = modificacion
                ? indicadorControlador.modificar(indicador)
                : indicadorControlador.registrar(indicador);
        mensaje = indicadorControlador.getMensaje();
        return ok;
    }

    public boolean desactivarIndicador(Long idIndicador) {
        mensaje = "";
        boolean ok = indicadorControlador.desactivar(idIndicador);
        mensaje = indicadorControlador.getMensaje();
        return ok;
    }

    public boolean eliminarIndicador(Long idIndicador) {
        mensaje = "";
        try {
            indicadorControlador.eliminarDefinitivamente(idIndicador);
            mensaje = "Indicador eliminado correctamente.";
            return true;
        } catch (Exception ex) {
            mensaje = mensajeExcepcion(ex);
            return false;
        }
    }

    private List<Object[]> filasClientes() {
        List<Object[]> filas = new ArrayList<>();
        for (Cliente cliente : clienteControlador.listar("")) {
            filas.add(new Object[]{
                cliente.getIdPersona(),
                cliente.getCodigoCliente(),
                cliente.getCedula(),
                cliente.getNombreCompleto(),
                cliente.getFechaRegistro(),
                cliente.getPesoInicial(),
                cliente.getPesoMeta(),
                cliente.isEstadoCliente() ? "ACTIVO" : "INACTIVO"
            });
        }
        return filas;
    }

    private List<Object[]> filasPlanes() {
        List<Object[]> filas = new ArrayList<>();
        for (PlanNutricional plan : planControlador.listar("")) {
            filas.add(new Object[]{
                plan.getIdPlanNutricional(),
                plan.getCodigoPlan(),
                plan.getNombrePlan(),
                nombreCliente(plan.getIdCliente()),
                nombreNutricionista(plan.getIdNutricionista()),
                plan.getFechaInicio(),
                plan.getFechaFin(),
                plan.getCaloriasObjetivo(),
                plan.getEstadoPlan()
            });
        }
        return filas;
    }

    private List<Object[]> filasComidas() {
        List<Object[]> filas = new ArrayList<>();
        for (IncluyeAlimento detalle : incluyeControlador.listar("")) {
            PlanNutricional plan = planControlador.buscar(
                    detalle.getIdPlanNutricional());
            Alimento alimento = alimentoControlador.buscar(detalle.getIdAlimento());
            filas.add(new Object[]{
                detalle.getIdDetallePlan(),
                plan == null ? detalle.getIdPlanNutricional() : plan.getNombrePlan(),
                alimento == null ? detalle.getIdAlimento() : alimento.getNombreAlimento(),
                detalle.getDiaSemana(),
                detalle.getTipoComida(),
                detalle.getCantidad(),
                detalle.getUnidadMedida(),
                detalle.getEstadoDetalle()
            });
        }
        return filas;
    }

    private List<Object[]> filasResultados() {
        List<Object[]> filas = new ArrayList<>();
        for (ResultadoIndicador resultado : resultadoControlador.listar("")) {
            EvaluacionFisica evaluacion = evaluacionControlador.buscar(
                    resultado.getIdEvaluacion());
            IndicadorSalud indicador = indicadorControlador.buscar(
                    resultado.getIdIndicador());
            filas.add(new Object[]{
                resultado.getIdResultado(),
                evaluacion == null
                        ? "Evaluacion #" + resultado.getIdEvaluacion()
                        : nombreCliente(evaluacion.getIdCliente()),
                indicador == null
                        ? resultado.getIdIndicador()
                        : indicador.getNombreIndicador(),
                resultado.getValorObtenido(),
                resultado.getClasificacion(),
                resultado.getFechaRegistro(),
                resultado.isFueraDeRango() ? "SI" : "NO"
            });
        }
        return filas;
    }

    private List<Object[]> filasRecomendaciones() {
        List<Object[]> filas = new ArrayList<>();
        for (Recomendacion recomendacion : recomendacionControlador.listar("")) {
            EvaluacionFisica evaluacion = evaluacionControlador.buscar(
                    recomendacion.getIdEvaluacion());
            filas.add(new Object[]{
                recomendacion.getIdRecomendacion(),
                evaluacion == null
                        ? "Evaluacion #" + recomendacion.getIdEvaluacion()
                        : nombreCliente(evaluacion.getIdCliente()),
                recomendacion.getTipoRecomendacion(),
                recomendacion.getTitulo(),
                recomendacion.getPrioridad(),
                recomendacion.getFechaInicio(),
                recomendacion.getFechaFin(),
                recomendacion.getEstadoRecomendacion()
            });
        }
        return filas;
    }

    private List<Object[]> filasNutricionistas() {
        List<Object[]> filas = new ArrayList<>();
        for (Nutricionista nutricionista : nutricionistaControlador.listar("")) {
            filas.add(new Object[]{
                nutricionista.getIdPersona(),
                nutricionista.getCodigoEmpleado(),
                nutricionista.getCedula(),
                nutricionista.getNombreCompleto(),
                nutricionista.getNumeroLicencia(),
                nutricionista.getEstadoLicencia(),
                nutricionista.getFechaInicioProfesion(),
                nutricionista.isEstadoEmpleado() ? "ACTIVO" : "INACTIVO"
            });
        }
        return filas;
    }

    private String nombreCliente(Long idCliente) {
        Cliente cliente = clienteControlador.buscar(idCliente);
        return cliente == null
                ? "Cliente #" + idCliente
                : cliente.getNombreCompleto();
    }

    private String nombreNutricionista(Long idNutricionista) {
        Nutricionista nutricionista = nutricionistaControlador.buscar(idNutricionista);
        return nutricionista == null
                ? "Nutricionista #" + idNutricionista
                : nutricionista.getNombreCompleto();
    }

    private List<Object[]> filtrar(List<Object[]> filas, String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return filas;
        }
        String buscado = criterio.trim().toLowerCase(Locale.ROOT);
        return filas.stream()
                .filter(fila -> Arrays.stream(fila)
                .anyMatch(valor -> valor != null
                && valor.toString().toLowerCase(Locale.ROOT).contains(buscado)))
                .toList();
    }

    private String normalizar(String texto) {
        return texto == null
                ? ""
                : texto.trim().toUpperCase(Locale.ROOT);
    }

    private String mensajeExcepcion(Throwable error) {
        Throwable causa = error;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        String detalle = causa.getMessage();
        return detalle == null || detalle.isBlank()
                ? "No fue posible completar la operacion."
                : detalle;
    }

    public String getMensaje() {
        return mensaje;
    }

    public record Resumen(
            long clientesActivos,
            long planesActivos,
            long nutricionistasActivos,
            long alimentos,
            long indicadoresActivos
    ) {
    }
}
