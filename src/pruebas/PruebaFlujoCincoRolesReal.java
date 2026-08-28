package pruebas;

import conexion.ConexionPostgreSQL;
import controlador.AsignacionRutinaControlador;
import controlador.ComprobanteControlador;
import controlador.FacturaControlador;
import controlador.IncluyeAlimentoControlador;
import controlador.LoginControlador;
import controlador.MembresiaControlador;
import controlador.PagoControlador;
import controlador.PlanNutricionalControlador;
import controlador.ProgresoRutinaControlador;
import controlador.ResumenModuloControlador;
import controlador.RutinaControlador;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import modelo.AsignacionRutina;
import modelo.Comprobante;
import modelo.Factura;
import modelo.IncluyeAlimento;
import modelo.Membresia;
import modelo.Pago;
import modelo.PlanNutricional;
import modelo.ProgresoRutina;
import modelo.Rutina;
import utilidades.SesionUsuario;

/**
 * Prueba de integración del mismo flujo de negocio visto por los cinco roles.
 * Debe ejecutarse únicamente contra una copia de auditoría de la base.
 */
public final class PruebaFlujoCincoRolesReal {

    private static final String CLAVE = "Gymnova123*";
    private static final Long CLIENTE = 2L;
    private static final Long ENTRENADOR = 4L;
    private static final Long NUTRICIONISTA = 6L;

    private PruebaFlujoCincoRolesReal() {
    }

    public static void main(String[] args) throws Exception {

        exigirBaseDeAuditoria();

        long marca = System.currentTimeMillis() % 100000;

        iniciarSesion("RECEPCION", "Recepcionista");
        Long membresia = registrarMembresia(marca);
        Long factura = registrarFactura();
        Long pago = registrarPago(factura, marca);
        registrarComprobante(pago, marca);

        iniciarSesion("jose", "Administrador");
        comprobarAdministracion(membresia, factura, pago);

        iniciarSesion("cliente", "Cliente");
        comprobarPortalClienteFinanciero(factura, pago);

        iniciarSesion("ENTRENADOR", "Entrenador");
        Long rutina = registrarRutina(marca);
        registrarAsignacion(rutina);
        registrarProgreso(rutina);

        iniciarSesion("jose", "Administrador");
        comprobarProgresoAdministrador(rutina);

        iniciarSesion("cliente", "Cliente");
        comprobarRutinaCliente(rutina);

        iniciarSesion("NUTRICION", "Nutricionista");
        Long plan = registrarPlanNutricional(marca);
        registrarComida(plan);

        iniciarSesion("jose", "Administrador");
        comprobarPlanAdministrador(plan);

        iniciarSesion("cliente", "Cliente");
        comprobarNutricionCliente(plan);

        SesionUsuario.cerrarSesion();
        System.out.println(
                "FLUJO_CINCO_ROLES_OK="
                + "RECEPCION>ADMIN>CLIENTE>ENTRENADOR>ADMIN>CLIENTE>"
                + "NUTRICIONISTA>ADMIN>CLIENTE"
        );
    }

    private static void exigirBaseDeAuditoria() {

        String url = System.getProperty("gymnova.db.url", "");

        if (!url.toLowerCase().contains("auditoria")) {
            throw new IllegalStateException(
                    "Esta prueba solo puede ejecutarse contra una base de auditoria."
            );
        }
    }

    private static void iniciarSesion(String usuario, String rolEsperado)
            throws Exception {

        SesionUsuario.cerrarSesion();
        // 'jose' es la unica cuenta administrativa real y conserva su propia
        // contrasena de produccion. El resto usa la clave demo Gymnova123*.
        String clave = "jose".equalsIgnoreCase(usuario) ? "Admin123*" : CLAVE;
        new LoginControlador().iniciarSesion(usuario, clave.toCharArray());
        exigir(rolEsperado.equalsIgnoreCase(
                SesionUsuario.getUsuarioActual().getNombreRol()
        ), "El usuario " + usuario + " no ingreso con el rol esperado.");
    }

    private static Long registrarMembresia(long marca) throws Exception {

        Membresia modelo = new Membresia();
        modelo.setNumeroMembresia(null);
        modelo.setFechaInicio(LocalDate.now());
        modelo.setFechaFin(LocalDate.now().plusDays(30));
        modelo.setCostoFinal(new BigDecimal("45.00"));
        modelo.setEstadoMembresia("ACTIVA");
        modelo.setObservaciones("Auditoria integral " + marca);
        modelo.setIdCliente(CLIENTE);
        modelo.setIdTipoMembresia(2L);

        MembresiaControlador controlador = new MembresiaControlador();
        exigir(controlador.registrar(modelo), controlador.getMensaje());

        return id("SELECT MAX(id_membresia) FROM membresia "
                + "WHERE id_cliente=? AND observaciones=?",
                CLIENTE, modelo.getObservaciones());
    }

    private static Long registrarFactura() throws Exception {

        Factura modelo = new Factura();
        modelo.setFechaEmision(LocalDate.now());
        modelo.setTotalDescuento(BigDecimal.ZERO);
        modelo.setImpuesto(new BigDecimal("5.40"));
        modelo.setEstadoFactura("EMITIDA");
        modelo.setIdCliente(CLIENTE);

        FacturaControlador controlador = new FacturaControlador();
        exigir(controlador.registrar(modelo), controlador.getMensaje());
        return id("SELECT MAX(id_factura) FROM factura WHERE id_cliente=?",
                CLIENTE);
    }

    private static Long registrarPago(Long factura, long marca) throws Exception {

        Pago modelo = new Pago();
        modelo.setMontoRecibido(new BigDecimal("50.00"));
        modelo.setMontoPago(new BigDecimal("45.00"));
        modelo.setFechaHoraPago(LocalDateTime.now());
        modelo.setEstadoPago("CONFIRMADO");
        modelo.setReferenciaTransaccion("AUD-" + marca);
        modelo.setIdFactura(factura);
        modelo.setIdMetodoPago(1L);

        PagoControlador controlador = new PagoControlador();
        exigir(controlador.registrar(modelo), controlador.getMensaje());
        return id("SELECT MAX(id_pago) FROM pago WHERE id_factura=?", factura);
    }

    private static void registrarComprobante(Long pago, long marca) {

        Comprobante modelo = new Comprobante();
        modelo.setNumeroComprobante("COMP-AUD-" + marca);
        modelo.setTipoComprobante("RECIBO DE PAGO");
        modelo.setFechaEmision(LocalDate.now());
        modelo.setFormatoArchivo("PDF");
        modelo.setRutaArchivo("auditoria/comprobante-" + marca + ".pdf");
        modelo.setCorreoEnvio("monica@gymnova.test");
        modelo.setFechaEnvio(LocalDate.now());
        modelo.setEstadoComprobante("ENVIADO");
        modelo.setIdPago(pago);

        ComprobanteControlador controlador = new ComprobanteControlador();
        exigir(controlador.registrar(modelo), controlador.getMensaje());
    }

    private static void comprobarAdministracion(
            Long membresia, Long factura, Long pago
    ) {

        exigir(new MembresiaControlador().buscar(membresia) != null,
                "Admin no encontro la membresia creada por Recepcion.");
        exigir(new FacturaControlador().buscar(factura) != null,
                "Admin no encontro la factura creada por Recepcion.");
        exigir(new PagoControlador().buscar(pago) != null,
                "Admin no encontro el pago creado por Recepcion.");
        exigir(!new ResumenModuloControlador().movimientos(20).isEmpty(),
                "Finanzas de Admin no muestra movimientos reales.");
    }

    private static void comprobarPortalClienteFinanciero(
            Long factura, Long pago
    ) {

        Map<String, Object> membresia
                = new ResumenModuloControlador().membresia(CLIENTE);
        exigir(!membresia.isEmpty(),
                "El cliente no ve la membresia creada por Recepcion.");
        exigir(!new FacturaControlador().listarPorIdCliente(CLIENTE).isEmpty(),
                "El cliente no ve su factura.");
        exigir(!new PagoControlador().listarPorIdFactura(factura).isEmpty(),
                "El cliente no ve su pago.");
        exigir(!new ComprobanteControlador().listarPorIdPago(pago).isEmpty(),
                "El cliente no ve su comprobante.");
    }

    private static Long registrarRutina(long marca) throws Exception {

        // La tabla objetivo_fitness fue eliminada; la rutina ya no requiere
        // objetivo asignado.
        Rutina modelo = new Rutina();
        modelo.setNombreRutina("Rutina auditoria " + marca);
        modelo.setDescripcion("Plan compartido entre Entrenador, Admin y Cliente");
        modelo.setNivel("INTERMEDIO");
        modelo.setDuracionSemanas(4);
        modelo.setFechaCreacion(LocalDate.now());
        modelo.setEstadoRutina("ACTIVA");
        modelo.setIdEntrenador(ENTRENADOR);

        RutinaControlador controlador = new RutinaControlador();
        exigir(controlador.registrar(modelo), controlador.getMensaje());
        return id("SELECT MAX(id_rutina) FROM rutina WHERE nombre_rutina=?",
                modelo.getNombreRutina());
    }

    private static void registrarAsignacion(Long rutina) {

        AsignacionRutina modelo = new AsignacionRutina();
        modelo.setFechaAsignacion(LocalDate.now());
        modelo.setFechaInicio(LocalDate.now());
        modelo.setFechaFin(LocalDate.now().plusWeeks(4));
        modelo.setEstadoAsignacion("ACTIVA");
        modelo.setObservaciones("Asignacion integral de auditoria");
        modelo.setIdCliente(CLIENTE);
        modelo.setIdRutina(rutina);

        AsignacionRutinaControlador controlador
                = new AsignacionRutinaControlador();
        exigir(controlador.registrar(modelo), controlador.getMensaje());
    }

    private static void registrarProgreso(Long rutina) {

        ProgresoRutina modelo = new ProgresoRutina();
        modelo.setFechaRegistro(LocalDate.now());
        modelo.setSesionesPlanificadas(4);
        modelo.setSesionesCompletadas(2);
        modelo.setPesoCorporal(new BigDecimal("61.50"));
        modelo.setNivelEsfuerzo("MEDIO");
        modelo.setEstadoProgreso("REGISTRADO");
        modelo.setIdCliente(CLIENTE);
        modelo.setIdRutina(rutina);

        ProgresoRutinaControlador controlador
                = new ProgresoRutinaControlador();
        exigir(controlador.registrar(modelo), controlador.getMensaje());
    }

    private static void comprobarProgresoAdministrador(Long rutina) {

        exigir(!new ProgresoRutinaControlador()
                .listarPorIdCliente(CLIENTE).isEmpty(),
                "Admin no ve el progreso registrado por Entrenador.");
        exigir(!new AsignacionRutinaControlador()
                .listarPorIdRutina(rutina).isEmpty(),
                "Admin no ve la asignacion creada por Entrenador.");
    }

    private static void comprobarRutinaCliente(Long rutina) {

        ResumenModuloControlador resumen = new ResumenModuloControlador();
        Map<String, Object> visible = resumen.rutina("Cliente", CLIENTE);
        exigir(rutina.equals(numeroLargo(visible.get("id_rutina"))),
                "El Cliente no ve la rutina que le asigno el Entrenador.");
        List<Map<String, Object>> actividad
                = resumen.actividad("Cliente", CLIENTE);
        exigir(actividad.size() == 7,
                "La grafica del Cliente no contiene los siete dias reales.");
    }

    private static Long registrarPlanNutricional(long marca) throws Exception {

        PlanNutricional modelo = new PlanNutricional();
        modelo.setNombrePlan("Plan auditoria " + marca);
        modelo.setFechaCreacion(LocalDate.now());
        modelo.setFechaInicio(LocalDate.now());
        modelo.setFechaFin(LocalDate.now().plusMonths(1));
        modelo.setCaloriasObjetivo(2100);
        modelo.setProteinasObjetivoG(new BigDecimal("120.00"));
        modelo.setCarbohidratosObjetivoG(new BigDecimal("240.00"));
        modelo.setRestriccionesGenerales("Sin restricciones de auditoria");
        modelo.setEstadoPlan("ACTIVO");
        modelo.setIdCliente(CLIENTE);
        modelo.setIdNutricionista(NUTRICIONISTA);

        PlanNutricionalControlador controlador
                = new PlanNutricionalControlador();
        exigir(controlador.registrar(modelo), controlador.getMensaje());
        return id("SELECT MAX(id_plan_nutricional) FROM plan_nutricional "
                + "WHERE nombre_plan=?", modelo.getNombrePlan());
    }

    private static void registrarComida(Long plan) throws Exception {

        Long alimento = id("SELECT MIN(id_alimento) FROM alimento");
        IncluyeAlimento modelo = new IncluyeAlimento();
        modelo.setDiaSemana("LUNES");
        modelo.setTipoComida("DESAYUNO");
        modelo.setHoraConsumo(LocalTime.of(7, 30));
        modelo.setCantidad(new java.math.BigDecimal("1.00"));
        modelo.setUnidadMedida("PORCION");
        modelo.setOrdenComida(1);
        modelo.setIndicaciones("Comida compartida de auditoria");
        modelo.setEstadoDetalle("ACTIVO");
        modelo.setIdPlanNutricional(plan);
        modelo.setIdAlimento(alimento);

        IncluyeAlimentoControlador controlador
                = new IncluyeAlimentoControlador();
        exigir(controlador.registrar(modelo), controlador.getMensaje());
    }

    private static void comprobarPlanAdministrador(Long plan) {

        exigir(new PlanNutricionalControlador().buscar(plan) != null,
                "Admin no ve el plan creado por Nutricionista.");
        exigir(!new IncluyeAlimentoControlador()
                .listarPorIdPlanNutricional(plan).isEmpty(),
                "Admin no ve las comidas del plan nutricional.");
    }

    private static void comprobarNutricionCliente(Long plan) {

        ResumenModuloControlador resumen = new ResumenModuloControlador();
        Map<String, Object> visible = resumen.plan(CLIENTE);
        exigir(plan.equals(numeroLargo(consultar(
                "SELECT id_plan_nutricional FROM plan_nutricional "
                + "WHERE codigo_plan=?",
                visible.get("codigo_plan")
        ))), "El Cliente no ve el plan creado por Nutricionista.");
        exigir(!resumen.comidas(CLIENTE, 20).isEmpty(),
                "El Cliente no ve las comidas de su plan.");
        exigir(!new PlanNutricionalControlador()
                .listarPorIdNutricionista(NUTRICIONISTA).isEmpty(),
                "Nutricion no conserva la relacion con su profesional.");
    }

    private static Long id(String sql, Object... parametros) throws Exception {

        Object valor = consultar(sql, parametros);

        if (valor == null) {
            throw new IllegalStateException("La consulta no devolvio un identificador.");
        }

        return ((Number) valor).longValue();
    }

    private static Object consultar(String sql, Object... parametros) {

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                sentencia.setObject(i + 1, parametros[i]);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() ? resultado.getObject(1) : null;
            }
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "No fue posible comprobar el flujo en PostgreSQL.", ex
            );
        }
    }

    private static Long numeroLargo(Object valor) {
        return valor instanceof Number ? ((Number) valor).longValue() : null;
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalStateException(mensaje);
        }
    }
}
