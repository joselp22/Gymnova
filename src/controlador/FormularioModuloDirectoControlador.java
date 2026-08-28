package controlador;

import java.awt.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import modelo.Alimento;
import modelo.AsignacionRutina;
import modelo.Asistencia;
import modelo.ClaseGrupal;
import modelo.Cliente;
import modelo.Comprobante;
import modelo.ContieneEjercicio;
import modelo.DetalleFactura;
import modelo.Ejercicio;
import modelo.EvaluacionFisica;
import modelo.Factura;
import modelo.IncluyeAlimento;
import modelo.IndicadorSalud;
import modelo.MedicionCorporal;
import modelo.MetodoPago;
import modelo.Nutricionista;
import modelo.OpcionRelacion;
import modelo.Pago;
import modelo.PlanNutricional;
import modelo.ProgresoRutina;
import modelo.Recomendacion;
import modelo.Reserva;
import modelo.ResultadoIndicador;
import modelo.Rutina;
import modelo.RutinaDiaEntrenamiento;
import utilidades.CalendarioSelector;
import utilidades.ClienteEnAtencion;
import utilidades.EstilosComponentes;
import utilidades.GestorConsultaModulo;

/**
 * Controla los formularios CRUD que ya existen dentro de los paneles
 * Acceso, Rutinas, Salud, Nutricion y Finanzas.
 *
 * Su objetivo es evitar PnlEditorRegistro/JOptionPane como editor CRUD:
 * Guardar y Modificar usan los controles visibles del propio panel.
 */
public class FormularioModuloDirectoControlador {

    private final Component padre;
    private final String modulo;
    private final GestorConsultaModulo gestorConsulta;
    private final JComboBox<String> cboProceso;
    private final JTable tabla;

    private final JLabel lblReferencia;
    private final JLabel lblFecha;
    private final JLabel lblNumero;
    private final JLabel lblPersona;
    private final JLabel lblSegundo;
    private final JLabel lblTexto;

    private final JTextField txtCodigoOriginal;
    private final JTextField txtFecha;
    private final JTextField txtNumero;
    private final JTextField txtSegundo;
    private final JTextField txtTexto;
    private final JComboBox<?> cboPersona;
    private final JCheckBox chkEstado;
    private final JComboBox<Object> cboReferencia = new JComboBox<>();

    private final CatalogoRelacionControlador catalogoRelacion
            = new CatalogoRelacionControlador();

    private final FlujoRecepcionControlador flujoRecepcion
            = new FlujoRecepcionControlador();
    private final FlujoEntrenadorControlador flujoEntrenador
            = new FlujoEntrenadorControlador();
    private final FlujoNutricionistaControlador flujoNutricionista
            = new FlujoNutricionistaControlador();

    private final ReservaControlador reservaControlador = new ReservaControlador();
    private final AsistenciaControlador asistenciaControlador = new AsistenciaControlador();
    private final ClaseGrupalControlador claseControlador = new ClaseGrupalControlador();

    private final RutinaControlador rutinaControlador = new RutinaControlador();
    private final AsignacionRutinaControlador asignacionControlador
            = new AsignacionRutinaControlador();
    private final EjercicioControlador ejercicioControlador = new EjercicioControlador();
    private final RutinaDiaEntrenamientoControlador diaRutinaControlador
            = new RutinaDiaEntrenamientoControlador();
    private final ProgresoRutinaControlador progresoControlador
            = new ProgresoRutinaControlador();
    private final ContieneEjercicioControlador contieneControlador
            = new ContieneEjercicioControlador();

    private final EvaluacionFisicaControlador evaluacionControlador
            = new EvaluacionFisicaControlador();
    private final MedicionCorporalControlador medicionControlador
            = new MedicionCorporalControlador();
    private final IndicadorSaludControlador indicadorControlador
            = new IndicadorSaludControlador();
    private final ResultadoIndicadorControlador resultadoControlador
            = new ResultadoIndicadorControlador();
    private final RecomendacionControlador recomendacionControlador
            = new RecomendacionControlador();

    private final PlanNutricionalControlador planControlador
            = new PlanNutricionalControlador();
    private final AlimentoControlador alimentoControlador = new AlimentoControlador();
    private final IncluyeAlimentoControlador incluyeControlador
            = new IncluyeAlimentoControlador();

    private final PagoControlador pagoControlador = new PagoControlador();
    private final FacturaControlador facturaControlador = new FacturaControlador();
    private final DetalleFacturaControlador detalleFacturaControlador
            = new DetalleFacturaControlador();
    private final MetodoPagoControlador metodoPagoControlador
            = new MetodoPagoControlador();
    private final ComprobanteControlador comprobanteControlador
            = new ComprobanteControlador();

    private boolean configurando;

    public FormularioModuloDirectoControlador(
            Component padre,
            String modulo,
            GestorConsultaModulo gestorConsulta,
            JPanel panelFormulario,
            JComboBox<String> cboProceso,
            JTable tabla,
            JLabel lblReferencia,
            JTextField txtCodigoOriginal,
            JLabel lblFecha,
            JTextField txtFecha,
            JLabel lblNumero,
            JTextField txtNumero,
            JLabel lblPersona,
            JComboBox<?> cboPersona,
            JLabel lblSegundo,
            JTextField txtSegundo,
            JLabel lblTexto,
            JTextField txtTexto,
            JCheckBox chkEstado
    ) {
        this.padre = padre;
        this.modulo = modulo == null ? "" : modulo.trim().toUpperCase();
        this.gestorConsulta = gestorConsulta;
        this.cboProceso = cboProceso;
        this.tabla = tabla;
        this.lblReferencia = lblReferencia;
        this.txtCodigoOriginal = txtCodigoOriginal;
        this.lblFecha = lblFecha;
        this.txtFecha = txtFecha;
        this.lblNumero = lblNumero;
        this.txtNumero = txtNumero;
        this.lblPersona = lblPersona;
        this.cboPersona = cboPersona;
        this.lblSegundo = lblSegundo;
        this.txtSegundo = txtSegundo;
        this.lblTexto = lblTexto;
        this.txtTexto = txtTexto;
        this.chkEstado = chkEstado;

        reemplazarCodigoPorReferencia(panelFormulario);
        EstilosComponentes.aplicarComboRedondeado(cboReferencia);

        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && !configurando) {
                cargarSeleccionado();
            }
        });

        cboPersona.addActionListener(evento -> {
            if (!configurando && usaClienteEnCombo()) {
                sincronizarClienteEnAtencion();
            }
        });
    }

    private void reemplazarCodigoPorReferencia(JPanel panelFormulario) {
        if (panelFormulario.getLayout() instanceof GroupLayout layout) {
            layout.replace(txtCodigoOriginal, cboReferencia);
        }
        cboReferencia.setPreferredSize(txtCodigoOriginal.getPreferredSize());
        cboReferencia.setMinimumSize(txtCodigoOriginal.getMinimumSize());
    }

    public void configurarFormulario() {
        configurando = true;
        try {
            limpiarCamposBase();
            habilitarTodos();
            switch (modulo) {
                case "ACCESO" -> configurarAcceso();
                case "RUTINAS" -> configurarRutinas();
                case "SALUD" -> configurarSalud();
                case "NUTRICION" -> configurarNutricion();
                case "FINANZAS" -> configurarFinanzas();
                default -> {
                }
            }
            configurarCalendarioCampoFecha();
        } finally {
            configurando = false;
        }
    }

    private void configurarCalendarioCampoFecha() {
        String etiqueta = lblFecha.getText() == null
                ? "" : lblFecha.getText().trim().toLowerCase();
        boolean fechaHora = etiqueta.contains("fecha y hora")
                || etiqueta.contains("momento");
        boolean fecha = fechaHora
                || etiqueta.contains("fecha")
                || etiqueta.contains("inicio de profesion");

        if (fechaHora) {
            CalendarioSelector.vincularFechaHora(txtFecha);
        } else if (fecha) {
            CalendarioSelector.vincularFecha(txtFecha);
        } else {
            boolean editable = txtFecha.isEditable();
            CalendarioSelector.desvincular(txtFecha, editable);
        }
    }

    public void refrescarAuxiliares() {
        // No limpia datos escritos. Solo repuebla las relaciones conservando id.
        configurando = true;
        try {
            String proceso = procesoActual();
            switch (modulo + "|" + proceso) {
                case "ACCESO|Reservas" -> {
                    recargarRelacion(cboReferencia, "idClase");
                    recargarRelacion(cboPersona, "idCliente");
                }
                case "ACCESO|Asistencias" -> recargarRelacion(cboPersona, "idCliente");
                case "ACCESO|Clases grupales" -> recargarRelacion(cboPersona, "idEntrenador");

                case "RUTINAS|Plantillas de rutina" -> recargarRelacion(cboPersona, "idEntrenador");
                case "RUTINAS|Asignar rutina a cliente" -> {
                    recargarRelacion(cboReferencia, "idRutina");
                    recargarRelacion(cboPersona, "idCliente");
                }
                case "RUTINAS|Calendario semanal" -> recargarRelacion(cboReferencia, "idRutina");
                case "RUTINAS|Registrar progreso del cliente" -> {
                    recargarRelacion(cboReferencia, "idRutina");
                    recargarRelacion(cboPersona, "idCliente");
                }
                case "RUTINAS|Ejercicios que componen la rutina" -> {
                    recargarRelacion(cboReferencia, "idRutina");
                    recargarRelacion(cboPersona, "idEjercicio");
                }

                case "SALUD|Evaluar cliente" -> {
                    recargarRelacion(cboReferencia, "idEntrenador");
                    recargarRelacion(cboPersona, "idCliente");
                }
                case "SALUD|Registrar peso y medidas" -> recargarRelacion(cboReferencia, "idEvaluacion");
                case "SALUD|Resultados de indicadores" -> {
                    recargarRelacion(cboReferencia, "idEvaluacion");
                    recargarRelacion(cboPersona, "idIndicador");
                }
                case "SALUD|Recomendaciones profesionales" -> recargarRelacion(cboReferencia, "idEvaluacion");

                case "NUTRICION|Planes asignados a clientes" -> {
                    recargarRelacion(cboReferencia, "idNutricionista");
                    recargarRelacion(cboPersona, "idCliente");
                }
                case "NUTRICION|Comidas y porciones del plan" -> {
                    recargarRelacion(cboReferencia, "idPlanNutricional");
                    recargarRelacion(cboPersona, "idAlimento");
                }
                case "NUTRICION|Resultados de indicadores" -> {
                    recargarRelacion(cboReferencia, "idEvaluacion");
                    recargarRelacion(cboPersona, "idIndicador");
                }
                case "NUTRICION|Recomendaciones profesionales" ->
                    recargarRelacion(cboReferencia, "idEvaluacion");

                case "FINANZAS|Pagos" -> {
                    recargarRelacion(cboReferencia, "idFactura");
                    recargarRelacion(cboPersona, "idMetodoPago");
                }
                case "FINANZAS|Facturas" -> recargarRelacion(cboPersona, "idCliente");
                case "FINANZAS|Detalle de factura" -> recargarRelacion(cboReferencia, "idFactura");
                case "FINANZAS|Comprobantes" -> recargarRelacion(cboReferencia, "idPago");
                default -> {
                }
            }
        } finally {
            configurando = false;
        }
    }

    public boolean guardar() {
        if (!gestorConsulta.verificarPermiso("CREAR")) {
            return false;
        }
        try {
            ResultadoOperacion resultado = switch (modulo) {
                case "ACCESO" -> guardarAcceso(null);
                case "RUTINAS" -> guardarRutinas(null);
                case "SALUD" -> guardarSalud(null);
                case "NUTRICION" -> guardarNutricion(null);
                case "FINANZAS" -> guardarFinanzas(null);
                default -> new ResultadoOperacion(false, "Modulo no compatible.");
            };
            informar(resultado, "Registro guardado correctamente.");
            if (resultado.correcto()) {
                gestorConsulta.cargar();
                limpiar();
            }
            return resultado.correcto();
        } catch (IllegalArgumentException ex) {
            advertencia(ex.getMessage());
            return false;
        }
    }

    public boolean modificar() {
        if (!gestorConsulta.verificarPermiso("MODIFICAR")) {
            return false;
        }
        Object seleccionado = gestorConsulta.getSeleccionado();
        if (seleccionado == null) {
            advertencia("Seleccione un registro de la tabla.");
            return false;
        }
        try {
            ResultadoOperacion resultado = switch (modulo) {
                case "ACCESO" -> guardarAcceso(seleccionado);
                case "RUTINAS" -> guardarRutinas(seleccionado);
                case "SALUD" -> guardarSalud(seleccionado);
                case "NUTRICION" -> guardarNutricion(seleccionado);
                case "FINANZAS" -> guardarFinanzas(seleccionado);
                default -> new ResultadoOperacion(false, "Modulo no compatible.");
            };
            informar(resultado, "Registro modificado correctamente.");
            if (resultado.correcto()) {
                gestorConsulta.cargar();
                limpiar();
            }
            return resultado.correcto();
        } catch (IllegalArgumentException ex) {
            advertencia(ex.getMessage());
            return false;
        }
    }

    /**
     * Cambia los campos visibles entre modo edicion y modo consulta.
     * La vista llama a este metodo despues de configurar cada proceso.
     */
    public void establecerSoloLectura(boolean soloLectura) {
        cboReferencia.setEnabled(!soloLectura);
        txtFecha.setEnabled(!soloLectura);
        txtNumero.setEnabled(!soloLectura);
        cboPersona.setEnabled(!soloLectura);
        txtSegundo.setEnabled(!soloLectura);
        txtTexto.setEnabled(!soloLectura);
        chkEstado.setEnabled(!soloLectura);
    }

    /** Finaliza el plan seleccionado usando el flujo propio del Nutricionista. */
    public boolean finalizarPlanSeleccionado() {
        if (!gestorConsulta.verificarPermiso("MODIFICAR")) {
            return false;
        }

        Object seleccionado = gestorConsulta.getSeleccionado();
        if (!(seleccionado instanceof PlanNutricional plan)
                || plan.getIdPlanNutricional() == null) {
            advertencia("Seleccione un plan nutricional de la tabla.");
            return false;
        }

        if ("FINALIZADO".equalsIgnoreCase(plan.getEstadoPlan())) {
            advertencia("El plan seleccionado ya se encuentra finalizado.");
            return false;
        }

        int opcion = JOptionPane.showConfirmDialog(
                padre,
                "¿Finalizar el plan nutricional seleccionado?",
                "Finalizar plan",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (opcion != JOptionPane.YES_OPTION) {
            return false;
        }

        boolean correcto = flujoNutricionista.finalizarPlan(
                plan.getIdPlanNutricional());

        if (correcto) {
            JOptionPane.showMessageDialog(
                    padre,
                    "Plan nutricional finalizado correctamente.",
                    "GYMNOVA",
                    JOptionPane.INFORMATION_MESSAGE
            );
            gestorConsulta.cargar();
            limpiar();
        } else {
            advertencia(flujoNutricionista.getMensaje());
        }

        return correcto;
    }

    public void limpiar() {
        tabla.clearSelection();
        configurarFormulario();
    }

    // ---------------------------------------------------------------------
    // ACCESO
    // ---------------------------------------------------------------------

    private void configurarAcceso() {
        switch (procesoActual()) {
            case "Reservas" -> {
                labels("Clase grupal *", "Momento de reserva", "Estado",
                        "Cliente *", "Motivo de cancelacion", "Observaciones");
                cargarRelacion(cboReferencia, "idClase", null);
                cargarRelacion(cboPersona, "idCliente", null);
                txtFecha.setText(LocalDateTime.now().withSecond(0).withNano(0).toString());
                txtFecha.setEditable(false);
                txtNumero.setText("ACTIVA");
                txtNumero.setEditable(false);
                chkEstado.setText("Reserva activa");
                chkEstado.setSelected(true);
            }
            case "Asistencias" -> {
                labels("Operacion *", "Fecha", "Metodo de registro *",
                        "Cliente *", "Tipo de acceso", "Observaciones");
                cargarOpcionesReferencia("Registrar entrada", "Registrar salida");
                cargarRelacion(cboPersona, "idCliente", null);
                txtFecha.setText(LocalDate.now().toString());
                txtFecha.setEditable(false);
                txtNumero.setText("MANUAL");
                txtSegundo.setText("MEMBRESIA");
                chkEstado.setText("Acceso registrado");
                chkEstado.setSelected(true);
                chkEstado.setEnabled(false);
            }
            case "Clases grupales" -> {
                labels("Tipo de clase grupal *", "Fecha y hora *", "Cupo maximo *",
                        "Entrenador responsable *", "Duracion (minutos) *", "Descripcion");
                cargarTextoReferencia(null);
                cargarRelacion(cboPersona, "idEntrenador", null);
                txtFecha.setText(LocalDateTime.now().plusDays(1)
                        .withSecond(0).withNano(0).toString());
                txtNumero.setText("30");
                txtSegundo.setText("60");
                chkEstado.setText("Clase activa");
                chkEstado.setSelected(true);
            }
            default -> {
            }
        }
    }

    private ResultadoOperacion guardarAcceso(Object existente) {
        return switch (procesoActual()) {
            case "Reservas" -> guardarReserva(existente);
            case "Asistencias" -> guardarAsistencia(existente);
            case "Clases grupales" -> guardarClase(existente);
            default -> new ResultadoOperacion(false, "Seleccione un proceso de acceso valido.");
        };
    }

    private ResultadoOperacion guardarReserva(Object existente) {
        Long idCliente = idSeleccionado(cboPersona, "Seleccione un cliente.");
        Long idClase = idSeleccionado(cboReferencia, "Seleccione una clase grupal.");
        if (existente == null) {
            Long id = flujoRecepcion.registrarReserva(
                    idCliente, idClase.intValue(), LocalDateTime.now(), textoOpcional(txtTexto.getText()));
            return new ResultadoOperacion(id != null, flujoRecepcion.getMensaje());
        }
        if (!(existente instanceof Reserva reserva)) {
            return tipoIncorrecto("reserva");
        }
        reserva.setIdCliente(idCliente);
        reserva.setIdClase(idClase);
        reserva.setObservaciones(textoOpcional(txtTexto.getText()));
        if (chkEstado.isSelected()) {
            reserva.setEstadoReserva("ACTIVA");
            reserva.setFechaHoraCancelacion(null);
            reserva.setMotivoCancelacion(null);
        } else {
            reserva.setEstadoReserva("CANCELADA");
            reserva.setFechaHoraCancelacion(LocalDateTime.now());
            reserva.setMotivoCancelacion(textoOpcional(txtSegundo.getText()));
        }
        boolean ok = reservaControlador.modificar(reserva);
        return new ResultadoOperacion(ok, reservaControlador.getMensaje());
    }

    private ResultadoOperacion guardarAsistencia(Object existente) {
        Long idCliente = idSeleccionado(cboPersona, "Seleccione un cliente.");
        String operacion = valorReferencia();
        if (existente == null) {
            if ("Registrar salida".equalsIgnoreCase(operacion)) {
                boolean ok = flujoRecepcion.registrarSalida(idCliente);
                return new ResultadoOperacion(ok, flujoRecepcion.getMensaje());
            }
            Long id = flujoRecepcion.registrarEntrada(idCliente,
                    requerido(txtNumero.getText(), "Ingrese el metodo de registro."));
            return new ResultadoOperacion(id != null, flujoRecepcion.getMensaje());
        }
        if (!(existente instanceof Asistencia asistencia)) {
            return tipoIncorrecto("asistencia");
        }
        asistencia.setIdCliente(idCliente);
        asistencia.setFechaAsistencia(fechaFlexible(txtFecha.getText(), "Fecha"));
        asistencia.setTipoAcceso(requerido(txtSegundo.getText(), "Ingrese el tipo de acceso."));
        asistencia.setMetodoRegistro(requerido(txtNumero.getText(), "Ingrese el metodo de registro."));
        asistencia.setEstadoAcceso(chkEstado.isSelected() ? "REGISTRADO" : "COMPLETADO");
        asistencia.setObservaciones(textoOpcional(txtTexto.getText()));
        if (asistencia.getHoraEntrada() == null) {
            asistencia.setHoraEntrada(LocalTime.now().withSecond(0).withNano(0));
        }
        boolean ok = asistenciaControlador.modificar(asistencia);
        return new ResultadoOperacion(ok, asistenciaControlador.getMensaje());
    }

    private ResultadoOperacion guardarClase(Object existente) {
        ClaseGrupal clase = existente instanceof ClaseGrupal c ? c : new ClaseGrupal();
        clase.setNombreClase(requerido(valorReferencia(), "Ingrese el nombre de la clase."));
        clase.setFechaHora(fechaHora(txtFecha.getText(), "Fecha y hora"));
        clase.setCupoMaximo(enteroPositivo(txtNumero.getText(), "Cupo maximo"));
        clase.setIdEntrenador(idSeleccionado(cboPersona, "Seleccione un entrenador."));
        clase.setDuracionBaseMinutos(enteroPositivo(txtSegundo.getText(), "Duracion"));
        clase.setDescripcion(textoOpcional(txtTexto.getText()));
        if (clase.getNivel() == null || clase.getNivel().isBlank()) {
            clase.setNivel("GENERAL");
        }
        if (clase.getIntensidad() == null || clase.getIntensidad().isBlank()) {
            clase.setIntensidad("MEDIA");
        }
        clase.setEstadoClase(chkEstado.isSelected());
        boolean ok = existente == null
                ? claseControlador.registrar(clase)
                : claseControlador.modificar(clase);
        return new ResultadoOperacion(ok, claseControlador.getMensaje());
    }

    // ---------------------------------------------------------------------
    // RUTINAS
    // ---------------------------------------------------------------------

    private void configurarRutinas() {
        switch (procesoActual()) {
            case "Plantillas de rutina" -> {
                labels("Nombre de rutina *", "Fecha de creacion *", "Duracion (semanas) *",
                        "Entrenador *", "Nivel *", "Descripcion / objetivo");
                cargarTextoReferencia(null);
                cargarRelacion(cboPersona, "idEntrenador", null);
                txtFecha.setText(LocalDate.now().toString());
                txtNumero.setText("4");
                txtSegundo.setText("PRINCIPIANTE");
                chkEstado.setText("Rutina activa");
                chkEstado.setSelected(true);
            }
            case "Asignar rutina a cliente" -> {
                labels("Rutina *", "Fecha de inicio *", "Fecha de finalizacion",
                        "Cliente *", "Estado", "Observaciones");
                cargarRelacion(cboReferencia, "idRutina", null);
                cargarRelacion(cboPersona, "idCliente", null);
                txtFecha.setText(LocalDate.now().toString());
                txtSegundo.setText("ACTIVA");
                txtSegundo.setEditable(false);
                chkEstado.setText("Asignacion activa");
                chkEstado.setSelected(true);
            }
            case "Catalogo de ejercicios" -> {
                labels("Nombre del ejercicio *", "Tipo de ejercicio", "Nivel de dificultad",
                        "", "Instrucciones", "Descripcion");
                cargarTextoReferencia(null);
                deshabilitarComboPersona();
                chkEstado.setText("Catalogo activo");
                chkEstado.setSelected(true);
                chkEstado.setEnabled(false);
            }
            case "Calendario semanal" -> {
                labels("Rutina *", "Dia de entrenamiento *", "", "", "", "");
                cargarRelacion(cboReferencia, "idRutina", null);
                deshabilitarComboPersona();
                deshabilitar(txtNumero, txtSegundo, txtTexto, chkEstado);
            }
            case "Registrar progreso del cliente" -> {
                labels("Rutina *", "Fecha de registro *", "Sesiones completadas / planificadas *",
                        "Cliente *", "Peso corporal", "Nivel de esfuerzo");
                cargarRelacion(cboReferencia, "idRutina", null);
                cargarRelacion(cboPersona, "idCliente", null);
                txtFecha.setText(LocalDate.now().toString());
                txtNumero.setText("0/0");
                txtTexto.setText("MEDIO");
                chkEstado.setText("Progreso registrado");
                chkEstado.setSelected(true);
            }
            case "Ejercicios que componen la rutina" -> {
                labels("Rutina *", "Dia de la semana *", "Series *",
                        "Ejercicio *", "Repeticiones *", "Orden *");
                cargarRelacion(cboReferencia, "idRutina", null);
                cargarRelacion(cboPersona, "idEjercicio", null);
                txtNumero.setText("3");
                txtSegundo.setText("10");
                txtTexto.setText("1");
                chkEstado.setText("Detalle activo");
                chkEstado.setSelected(true);
                chkEstado.setEnabled(false);
            }
            default -> {
            }
        }
    }

    private ResultadoOperacion guardarRutinas(Object existente) {
        return switch (procesoActual()) {
            case "Plantillas de rutina" -> guardarRutina(existente);
            case "Asignar rutina a cliente" -> guardarAsignacion(existente);
            case "Catalogo de ejercicios" -> guardarEjercicio(existente);
            case "Calendario semanal" -> guardarDiaRutina(existente);
            case "Registrar progreso del cliente" -> guardarProgreso(existente);
            case "Ejercicios que componen la rutina" -> guardarContieneEjercicio(existente);
            default -> new ResultadoOperacion(false, "Seleccione un proceso de rutina valido.");
        };
    }

    private ResultadoOperacion guardarRutina(Object existente) {
        Rutina rutina = existente instanceof Rutina r ? r : new Rutina();
        rutina.setNombreRutina(requerido(valorReferencia(), "Ingrese el nombre de la rutina."));
        rutina.setFechaCreacion(fechaFlexible(txtFecha.getText(), "Fecha de creacion"));
        rutina.setDuracionSemanas(enteroPositivo(txtNumero.getText(), "Duracion"));
        rutina.setIdEntrenador(idSeleccionado(cboPersona, "Seleccione un entrenador."));
        rutina.setNivel(requerido(txtSegundo.getText(), "Ingrese el nivel.").toUpperCase());
        rutina.setDescripcion(textoOpcional(txtTexto.getText()));
        rutina.setEstadoRutina(chkEstado.isSelected() ? "ACTIVA" : "INACTIVA");
        boolean ok = existente == null
                ? rutinaControlador.registrar(rutina)
                : rutinaControlador.modificar(rutina);
        return new ResultadoOperacion(ok, rutinaControlador.getMensaje());
    }

    private ResultadoOperacion guardarAsignacion(Object existente) {
        Long idRutina = idSeleccionado(cboReferencia, "Seleccione una rutina.");
        Long idCliente = idSeleccionado(cboPersona, "Seleccione un cliente.");
        LocalDate inicio = fechaFlexible(txtFecha.getText(), "Fecha de inicio");
        LocalDate fin = fechaOpcional(txtNumero.getText(), "Fecha de finalizacion");
        if (existente == null) {
            Long id = flujoEntrenador.asignarRutina(
                    idCliente, idRutina, inicio, fin, textoOpcional(txtTexto.getText()));
            return new ResultadoOperacion(id != null, flujoEntrenador.getMensaje());
        }
        if (!(existente instanceof AsignacionRutina asignacion)) {
            return tipoIncorrecto("asignacion de rutina");
        }
        asignacion.setIdRutina(idRutina);
        asignacion.setIdCliente(idCliente);
        asignacion.setFechaInicio(inicio);
        asignacion.setFechaFin(fin);
        if (asignacion.getFechaAsignacion() == null) {
            asignacion.setFechaAsignacion(LocalDate.now());
        }
        asignacion.setEstadoAsignacion(chkEstado.isSelected() ? "ACTIVA" : "PAUSADA");
        asignacion.setObservaciones(textoOpcional(txtTexto.getText()));
        boolean ok = asignacionControlador.modificar(asignacion);
        return new ResultadoOperacion(ok, asignacionControlador.getMensaje());
    }

    private ResultadoOperacion guardarEjercicio(Object existente) {
        Ejercicio ejercicio = existente instanceof Ejercicio e ? e : new Ejercicio();
        ejercicio.setNombreEjercicio(requerido(valorReferencia(), "Ingrese el nombre del ejercicio."));
        ejercicio.setTipoEjercicio(textoOpcional(txtFecha.getText()));
        ejercicio.setNivelDificultad(textoOpcional(txtNumero.getText()));
        ejercicio.setInstrucciones(textoOpcional(txtSegundo.getText()));
        ejercicio.setDescripcion(textoOpcional(txtTexto.getText()));
        boolean ok = existente == null
                ? ejercicioControlador.registrar(ejercicio)
                : ejercicioControlador.modificar(ejercicio);
        return new ResultadoOperacion(ok, ejercicioControlador.getMensaje());
    }

    private ResultadoOperacion guardarDiaRutina(Object existente) {
        RutinaDiaEntrenamiento dia = existente instanceof RutinaDiaEntrenamiento d
                ? d : new RutinaDiaEntrenamiento();
        dia.setIdRutina(idSeleccionado(cboReferencia, "Seleccione una rutina."));
        dia.setDiaEntrenamiento(requerido(txtFecha.getText(), "Ingrese el dia de entrenamiento.").toUpperCase());
        boolean ok = existente == null
                ? diaRutinaControlador.registrar(dia)
                : diaRutinaControlador.modificar(dia);
        return new ResultadoOperacion(ok, diaRutinaControlador.getMensaje());
    }

    private ResultadoOperacion guardarProgreso(Object existente) {
        ProgresoRutina progreso = existente instanceof ProgresoRutina p ? p : new ProgresoRutina();
        progreso.setIdRutina(idSeleccionado(cboReferencia, "Seleccione una rutina."));
        progreso.setIdCliente(idSeleccionado(cboPersona, "Seleccione un cliente."));
        progreso.setFechaRegistro(fechaFlexible(txtFecha.getText(), "Fecha de registro"));
        int[] sesiones = sesiones(txtNumero.getText());
        progreso.setSesionesCompletadas(sesiones[0]);
        progreso.setSesionesPlanificadas(sesiones[1]);
        progreso.setPesoCorporal(decimalOpcional(txtSegundo.getText(), "Peso corporal"));
        progreso.setNivelEsfuerzo(textoOpcional(txtTexto.getText()) == null
                ? null : txtTexto.getText().trim().toUpperCase());
        progreso.setEstadoProgreso(chkEstado.isSelected() ? "REGISTRADO" : "ANULADO");
        boolean ok = existente == null
                ? progresoControlador.registrar(progreso)
                : progresoControlador.modificar(progreso);
        return new ResultadoOperacion(ok, progresoControlador.getMensaje());
    }

    private ResultadoOperacion guardarContieneEjercicio(Object existente) {
        ContieneEjercicio detalle = existente instanceof ContieneEjercicio d
                ? d : new ContieneEjercicio();
        detalle.setIdRutina(idSeleccionado(cboReferencia, "Seleccione una rutina."));
        detalle.setIdEjercicio(idSeleccionado(cboPersona, "Seleccione un ejercicio."));
        detalle.setDiaSemana(requerido(txtFecha.getText(), "Ingrese el dia de la semana.").toUpperCase());
        detalle.setSeries(enteroPositivo(txtNumero.getText(), "Series"));
        detalle.setRepeticiones(requerido(txtSegundo.getText(), "Ingrese las repeticiones."));
        detalle.setOrden(enteroPositivo(txtTexto.getText(), "Orden"));
        boolean ok = existente == null
                ? contieneControlador.registrar(detalle)
                : contieneControlador.modificar(detalle);
        return new ResultadoOperacion(ok, contieneControlador.getMensaje());
    }

    // ---------------------------------------------------------------------
    // SALUD
    // ---------------------------------------------------------------------

    private void configurarSalud() {
        switch (procesoActual()) {
            case "Evaluar cliente" -> {
                labels("Entrenador responsable *", "Fecha de evaluacion *", "Tipo de evaluacion *",
                        "Cliente *", "Nivel de riesgo", "Motivo / observacion");
                cargarRelacion(cboReferencia, "idEntrenador", null);
                cargarRelacion(cboPersona, "idCliente", null);
                txtFecha.setText(LocalDate.now().toString());
                txtNumero.setText("GENERAL");
                txtSegundo.setText("BAJO");
                chkEstado.setText("Evaluacion registrada");
                chkEstado.setSelected(true);
            }
            case "Registrar peso y medidas" -> {
                labels("Evaluacion *", "Peso (kg)", "Altura (m)", "", "Porcentaje de grasa", "Observaciones");
                cargarRelacion(cboReferencia, "idEvaluacion", null);
                deshabilitarComboPersona();
                chkEstado.setText("Medicion activa");
                chkEstado.setSelected(true);
                chkEstado.setEnabled(false);
            }
            case "Catalogo de indicadores" -> {
                labels("Nombre del indicador *", "Unidad de medida", "Valor minimo",
                        "Categoria", "Valor maximo", "Descripcion");
                cargarTextoReferencia(null);
                cargarOpcionesPersona("GENERAL", "CORPORAL", "CARDIOVASCULAR", "NUTRICIONAL");
                chkEstado.setText("Indicador activo");
                chkEstado.setSelected(true);
            }
            case "Resultados de indicadores" -> {
                labels("Evaluacion *", "Fecha de registro *", "Valor obtenido *",
                        "Indicador *", "Clasificacion", "Observaciones");
                cargarRelacion(cboReferencia, "idEvaluacion", null);
                cargarRelacion(cboPersona, "idIndicador", null);
                txtFecha.setText(LocalDate.now().toString());
                chkEstado.setText("Fuera de rango");
                chkEstado.setSelected(false);
                chkEstado.setEnabled(false);
            }
            case "Recomendaciones profesionales" -> {
                labels("Evaluacion *", "Fecha de inicio", "Tipo de recomendacion *",
                        "Prioridad *", "Fecha de finalizacion", "Descripcion *");
                cargarRelacion(cboReferencia, "idEvaluacion", null);
                cargarOpcionesPersona("BAJA", "MEDIA", "ALTA");
                txtFecha.setText(LocalDate.now().toString());
                txtNumero.setText("GENERAL");
                chkEstado.setText("Recomendacion activa");
                chkEstado.setSelected(true);
            }
            default -> {
            }
        }
    }

    private ResultadoOperacion guardarSalud(Object existente) {
        return switch (procesoActual()) {
            case "Evaluar cliente" -> guardarEvaluacion(existente);
            case "Registrar peso y medidas" -> guardarMedicion(existente);
            case "Catalogo de indicadores" -> guardarIndicador(existente);
            case "Resultados de indicadores" -> guardarResultado(existente);
            case "Recomendaciones profesionales" -> guardarRecomendacion(existente);
            default -> new ResultadoOperacion(false, "Seleccione un proceso de salud valido.");
        };
    }

    private ResultadoOperacion guardarEvaluacion(Object existente) {
        EvaluacionFisica evaluacion = existente instanceof EvaluacionFisica e
                ? e : new EvaluacionFisica();
        evaluacion.setIdEntrenador(idSeleccionado(cboReferencia, "Seleccione el entrenador."));
        evaluacion.setIdCliente(idSeleccionado(cboPersona, "Seleccione el cliente."));
        evaluacion.setFechaEvaluacion(fechaFlexible(txtFecha.getText(), "Fecha de evaluacion"));
        evaluacion.setTipoEvaluacion(requerido(txtNumero.getText(), "Ingrese el tipo de evaluacion."));
        evaluacion.setNivelRiesgo(textoOpcional(txtSegundo.getText()));
        evaluacion.setMotivo(textoOpcional(txtTexto.getText()));
        if (evaluacion.getCondicionGeneral() == null) {
            evaluacion.setCondicionGeneral("ESTABLE");
        }
        evaluacion.setEstadoEvaluacion(chkEstado.isSelected() ? "REGISTRADA" : "ANULADA");
        boolean ok = existente == null
                ? evaluacionControlador.registrar(evaluacion)
                : evaluacionControlador.modificar(evaluacion);
        return new ResultadoOperacion(ok, evaluacionControlador.getMensaje());
    }

    private ResultadoOperacion guardarMedicion(Object existente) {
        MedicionCorporal medicion = existente instanceof MedicionCorporal m
                ? m : new MedicionCorporal();
        medicion.setIdEvaluacion(idSeleccionado(cboReferencia, "Seleccione una evaluacion."));
        medicion.setPesoKg(decimalOpcional(txtFecha.getText(), "Peso"));
        medicion.setAlturaM(decimalOpcional(txtNumero.getText(), "Altura"));
        medicion.setPorcentajeGrasa(decimalOpcional(txtSegundo.getText(), "Porcentaje de grasa"));
        medicion.setObservaciones(textoOpcional(txtTexto.getText()));
        boolean ok = existente == null
                ? medicionControlador.registrar(medicion)
                : medicionControlador.modificar(medicion);
        return new ResultadoOperacion(ok, medicionControlador.getMensaje());
    }

    private ResultadoOperacion guardarIndicador(Object existente) {
        IndicadorSalud indicador = existente instanceof IndicadorSalud i
                ? i : new IndicadorSalud();
        indicador.setNombreIndicador(requerido(valorReferencia(), "Ingrese el nombre del indicador."));
        indicador.setUnidadMedida(textoOpcional(txtFecha.getText()));
        indicador.setValorMinimoReferencia(decimalOpcional(txtNumero.getText(), "Valor minimo"));
        indicador.setCategoria(valorCombo(cboPersona));
        indicador.setValorMaximoReferencia(decimalOpcional(txtSegundo.getText(), "Valor maximo"));
        indicador.setDescripcion(textoOpcional(txtTexto.getText()));
        indicador.setEstadoIndicador(chkEstado.isSelected());
        boolean ok = existente == null
                ? indicadorControlador.registrar(indicador)
                : indicadorControlador.modificar(indicador);
        return new ResultadoOperacion(ok, indicadorControlador.getMensaje());
    }

    private ResultadoOperacion guardarResultado(Object existente) {
        ResultadoIndicador resultado = existente instanceof ResultadoIndicador r
                ? r : new ResultadoIndicador();
        resultado.setIdEvaluacion(idSeleccionado(cboReferencia, "Seleccione una evaluacion."));
        resultado.setIdIndicador(idSeleccionado(cboPersona, "Seleccione un indicador."));
        resultado.setFechaRegistro(fechaFlexible(txtFecha.getText(), "Fecha de registro"));
        resultado.setValorObtenido(decimalRequerido(txtNumero.getText(), "Valor obtenido"));
        resultado.setClasificacion(textoOpcional(txtSegundo.getText()));
        resultado.setObservaciones(textoOpcional(txtTexto.getText()));
        boolean ok = existente == null
                ? resultadoControlador.registrar(resultado)
                : resultadoControlador.modificar(resultado);
        return new ResultadoOperacion(ok, resultadoControlador.getMensaje());
    }

    private ResultadoOperacion guardarRecomendacion(Object existente) {
        Recomendacion recomendacion = existente instanceof Recomendacion r
                ? r : new Recomendacion();
        recomendacion.setIdEvaluacion(idSeleccionado(cboReferencia, "Seleccione una evaluacion."));
        recomendacion.setFechaRecomendacion(LocalDate.now());
        recomendacion.setFechaInicio(fechaOpcional(txtFecha.getText(), "Fecha de inicio"));
        recomendacion.setTipoRecomendacion(requerido(txtNumero.getText(), "Ingrese el tipo de recomendacion."));
        recomendacion.setPrioridad(valorCombo(cboPersona) == null ? "MEDIA" : valorCombo(cboPersona));
        recomendacion.setFechaFin(fechaOpcional(txtSegundo.getText(), "Fecha de finalizacion"));
        String descripcion = requerido(txtTexto.getText(), "Ingrese la descripcion.");
        recomendacion.setDescripcion(descripcion);
        if (recomendacion.getTitulo() == null || recomendacion.getTitulo().isBlank()) {
            recomendacion.setTitulo(descripcion.length() > 120
                    ? descripcion.substring(0, 120) : descripcion);
        }
        recomendacion.setEstadoRecomendacion(chkEstado.isSelected() ? "ACTIVA" : "CANCELADA");
        boolean ok = existente == null
                ? recomendacionControlador.registrar(recomendacion)
                : recomendacionControlador.modificar(recomendacion);
        return new ResultadoOperacion(ok, recomendacionControlador.getMensaje());
    }

    // ---------------------------------------------------------------------
    // NUTRICION
    // ---------------------------------------------------------------------

    private void configurarNutricion() {
        switch (procesoActual()) {
            case "Clientes" -> {
                labels("Codigo de cliente", "Fecha de registro", "Peso inicial (kg)",
                        "", "Peso meta (kg)", "Observaciones");
                cargarTextoReferencia(null);
                deshabilitarComboPersona();
                chkEstado.setText("Cliente activo");
                chkEstado.setSelected(true);
            }
            case "Planes asignados a clientes" -> {
                labels("Nutricionista *", "Fecha de inicio *", "Calorias objetivo",
                        "Cliente *", "Nombre del plan *", "Restricciones generales");
                cargarRelacion(cboReferencia, "idNutricionista", null);
                cargarRelacion(cboPersona, "idCliente", null);
                txtFecha.setText(LocalDate.now().toString());
                txtNumero.setText("2000");
                chkEstado.setText("Plan activo");
                chkEstado.setSelected(true);
            }
            case "Catalogo de alimentos" -> {
                labels("Nombre del alimento *", "Categoria", "Porcion referencia (g)",
                        "", "Proteinas (g)", "Descripcion");
                cargarTextoReferencia(null);
                deshabilitarComboPersona();
                chkEstado.setText("Catalogo de alimentos");
                chkEstado.setSelected(true);
                chkEstado.setEnabled(false);
            }
            case "Catalogo de indicadores" -> {
                labels("Nombre del indicador *", "Unidad de medida *", "Valor minimo *",
                        "Categoria", "Valor maximo *", "Descripcion");
                cargarTextoReferencia(null);
                cargarOpcionesPersona(
                        "GENERAL", "CORPORAL", "CARDIOVASCULAR", "NUTRICIONAL");
                chkEstado.setText("Indicador activo");
                chkEstado.setSelected(true);
            }
            case "Comidas y porciones del plan" -> {
                labels("Plan nutricional *", "Dia de la semana *", "Cantidad *",
                        "Alimento *", "Tipo de comida *", "Unidad / indicaciones");
                cargarRelacion(cboReferencia, "idPlanNutricional", null);
                cargarRelacion(cboPersona, "idAlimento", null);
                txtFecha.setText("LUNES");
                txtNumero.setText("100");
                txtSegundo.setText("ALMUERZO");
                txtTexto.setText("g");
                chkEstado.setText("Detalle activo");
                chkEstado.setSelected(true);
            }
            case "Resultados de indicadores" -> {
                labels("Evaluacion *", "Fecha de registro *", "Valor obtenido *",
                        "Indicador *", "Clasificacion", "Observaciones");
                cargarRelacion(cboReferencia, "idEvaluacion", null);
                cargarRelacion(cboPersona, "idIndicador", null);
                txtFecha.setText(LocalDate.now().toString());
                chkEstado.setText("Fuera de rango");
                chkEstado.setSelected(false);
                chkEstado.setEnabled(false);
            }
            case "Recomendaciones profesionales" -> {
                labels("Evaluacion *", "Fecha de inicio", "Tipo de recomendacion *",
                        "Prioridad *", "Fecha de finalizacion", "Descripcion *");
                cargarRelacion(cboReferencia, "idEvaluacion", null);
                cargarOpcionesPersona("BAJA", "MEDIA", "ALTA");
                txtFecha.setText(LocalDate.now().toString());
                txtNumero.setText("NUTRICIONAL");
                chkEstado.setText("Recomendacion activa");
                chkEstado.setSelected(true);
            }
            case "Nutricionistas" -> {
                labels("Nutricionista", "Inicio de profesion", "Numero de licencia",
                        "Estado de licencia", "Codigo de empleado", "");
                cargarTextoReferencia(null);
                cargarOpcionesPersona("ACTIVA", "SUSPENDIDA", "VENCIDA");
                chkEstado.setText("Empleado activo");
                chkEstado.setSelected(true);
            }
            default -> {
            }
        }
    }

    private ResultadoOperacion guardarNutricion(Object existente) {
        return switch (procesoActual()) {
            case "Planes asignados a clientes" -> guardarPlanNutricional(existente);
            case "Catalogo de alimentos" -> guardarAlimento(existente);
            case "Catalogo de indicadores" -> guardarIndicador(existente);
            case "Comidas y porciones del plan" -> guardarIncluyeAlimento(existente);
            case "Resultados de indicadores" -> guardarResultado(existente);
            case "Recomendaciones profesionales" -> guardarRecomendacion(existente);
            default -> new ResultadoOperacion(false,
                    "La opcion seleccionada es solo de consulta.");
        };
    }

    private ResultadoOperacion guardarPlanNutricional(Object existente) {
        PlanNutricional plan = existente instanceof PlanNutricional p ? p : new PlanNutricional();
        plan.setIdNutricionista(idSeleccionado(cboReferencia, "Seleccione un nutricionista."));
        plan.setIdCliente(idSeleccionado(cboPersona, "Seleccione un cliente."));
        plan.setFechaInicio(fechaFlexible(txtFecha.getText(), "Fecha de inicio"));
        if (plan.getFechaCreacion() == null) {
            plan.setFechaCreacion(LocalDate.now());
        }
        plan.setCaloriasObjetivo(enteroOpcionalPositivo(txtNumero.getText(), "Calorias"));
        plan.setNombrePlan(requerido(txtSegundo.getText(), "Ingrese el nombre del plan."));
        plan.setRestriccionesGenerales(textoOpcional(txtTexto.getText()));
        if (plan.getProteinasObjetivoG() == null) {
            plan.setProteinasObjetivoG(BigDecimal.ZERO);
        }
        if (plan.getCarbohidratosObjetivoG() == null) {
            plan.setCarbohidratosObjetivoG(BigDecimal.ZERO);
        }
        plan.setEstadoPlan(chkEstado.isSelected() ? "ACTIVO" : "SUSPENDIDO");
        boolean ok = existente == null
                ? planControlador.registrar(plan)
                : planControlador.modificar(plan);
        return new ResultadoOperacion(ok, planControlador.getMensaje());
    }

    private ResultadoOperacion guardarAlimento(Object existente) {
        Alimento alimento = existente instanceof Alimento a ? a : new Alimento();
        alimento.setNombreAlimento(requerido(valorReferencia(), "Ingrese el nombre del alimento."));
        alimento.setCategoria(textoOpcional(txtFecha.getText()));
        alimento.setPorcionReferenciaG(decimalOpcional(txtNumero.getText(), "Porcion"));
        alimento.setProteinasG(decimalOpcional(txtSegundo.getText(), "Proteinas"));
        alimento.setDescripcion(textoOpcional(txtTexto.getText()));
        if (alimento.getCarbohidratosG() == null) {
            alimento.setCarbohidratosG(BigDecimal.ZERO);
        }
        if (alimento.getFibraG() == null) {
            alimento.setFibraG(BigDecimal.ZERO);
        }
        boolean ok = existente == null
                ? alimentoControlador.registrar(alimento)
                : alimentoControlador.modificar(alimento);
        return new ResultadoOperacion(ok, alimentoControlador.getMensaje());
    }

    private ResultadoOperacion guardarIncluyeAlimento(Object existente) {
        IncluyeAlimento detalle = existente instanceof IncluyeAlimento i
                ? i : new IncluyeAlimento();
        detalle.setIdPlanNutricional(idSeleccionado(cboReferencia, "Seleccione un plan nutricional."));
        detalle.setIdAlimento(idSeleccionado(cboPersona, "Seleccione un alimento."));
        detalle.setDiaSemana(requerido(txtFecha.getText(), "Ingrese el dia de la semana.").toUpperCase());
        detalle.setCantidad(decimalRequerido(txtNumero.getText(), "Cantidad"));
        detalle.setTipoComida(requerido(txtSegundo.getText(), "Ingrese el tipo de comida.").toUpperCase());
        String[] unidadIndicacion = partirUnidadIndicacion(txtTexto.getText());
        detalle.setUnidadMedida(unidadIndicacion[0]);
        detalle.setIndicaciones(unidadIndicacion[1]);
        if (detalle.getOrdenComida() == null || detalle.getOrdenComida() <= 0) {
            detalle.setOrdenComida(1);
        }
        detalle.setEstadoDetalle(chkEstado.isSelected() ? "ACTIVO" : "SUSPENDIDO");
        boolean ok = existente == null
                ? incluyeControlador.registrar(detalle)
                : incluyeControlador.modificar(detalle);
        return new ResultadoOperacion(ok, incluyeControlador.getMensaje());
    }

    // ---------------------------------------------------------------------
    // FINANZAS
    // ---------------------------------------------------------------------

    private void configurarFinanzas() {
        switch (procesoActual()) {
            case "Pagos" -> {
                labels("Factura *", "Fecha y hora de pago *", "Monto pagado *",
                        "Metodo de pago *", "Monto recibido *", "Referencia de transaccion");
                cargarRelacion(cboReferencia, "idFactura", null);
                cargarRelacion(cboPersona, "idMetodoPago", null);
                txtFecha.setText(LocalDateTime.now().withSecond(0).withNano(0).toString());
                chkEstado.setText("Pago confirmado");
                chkEstado.setSelected(true);
            }
            case "Facturas" -> {
                labels("Estado de factura *", "Fecha de emision *", "Impuesto *",
                        "Cliente *", "Descuento total *", "Observacion");
                cargarOpcionesReferencia("EMITIDA", "PENDIENTE", "ANULADA");
                cargarRelacion(cboPersona, "idCliente", null);
                txtFecha.setText(LocalDate.now().toString());
                txtNumero.setText("0");
                txtSegundo.setText("0");
                chkEstado.setText("Factura activa");
                chkEstado.setSelected(true);
            }
            case "Detalle de factura" -> {
                labels("Factura *", "Tipo de concepto *", "Precio unitario *",
                        "Tipo", "Cantidad *", "Codigo referencia / impuesto %");
                cargarRelacion(cboReferencia, "idFactura", null);
                cargarOpcionesPersona("MEMBRESIA", "SERVICIO", "OTRO");
                txtNumero.setText("0");
                txtSegundo.setText("1");
                txtTexto.setText("|15");
                chkEstado.setText("Detalle activo");
                chkEstado.setSelected(true);
                chkEstado.setEnabled(false);
            }
            case "Metodos de pago" -> {
                labels("Nombre del metodo *", "Descripcion", "Comision % *",
                        "Requiere referencia", "Permite cuotas", "");
                cargarTextoReferencia(null);
                cargarOpcionesPersona("NO", "SI");
                txtNumero.setText("0");
                txtSegundo.setText("NO");
                txtTexto.setEnabled(false);
                chkEstado.setText("Metodo activo");
                chkEstado.setSelected(true);
            }
            case "Comprobantes" -> {
                labels("Pago *", "Fecha de emision *", "Tipo de comprobante *",
                        "Formato", "Correo de envio", "Ruta de archivo");
                cargarRelacion(cboReferencia, "idPago", null);
                cargarOpcionesPersona("PDF", "IMPRESO");
                txtFecha.setText(LocalDate.now().toString());
                txtNumero.setText("PAGO");
                chkEstado.setText("Comprobante generado");
                chkEstado.setSelected(true);
            }
            default -> {
            }
        }
    }

    private ResultadoOperacion guardarFinanzas(Object existente) {
        return switch (procesoActual()) {
            case "Pagos" -> guardarPago(existente);
            case "Facturas" -> guardarFactura(existente);
            case "Detalle de factura" -> guardarDetalleFactura(existente);
            case "Metodos de pago" -> guardarMetodoPago(existente);
            case "Comprobantes" -> guardarComprobante(existente);
            default -> new ResultadoOperacion(false, "Seleccione un proceso financiero valido.");
        };
    }

    private ResultadoOperacion guardarPago(Object existente) {
        Pago pago = existente instanceof Pago p ? p : new Pago();
        pago.setIdFactura(idSeleccionado(cboReferencia, "Seleccione una factura."));
        pago.setIdMetodoPago(idSeleccionado(cboPersona, "Seleccione un metodo de pago."));
        pago.setFechaHoraPago(fechaHora(txtFecha.getText(), "Fecha y hora de pago"));
        pago.setMontoPago(decimalRequerido(txtNumero.getText(), "Monto pagado"));
        pago.setMontoRecibido(decimalRequerido(txtSegundo.getText(), "Monto recibido"));
        pago.setReferenciaTransaccion(textoOpcional(txtTexto.getText()));
        pago.setEstadoPago(chkEstado.isSelected() ? "CONFIRMADO" : "REGISTRADO");
        if (existente == null && chkEstado.isSelected()) {
            Long id = flujoRecepcion.registrarPago(
                    pago.getIdFactura(), pago.getMontoPago(),
                    pago.getIdMetodoPago().intValue(), pago.getReferenciaTransaccion());
            return new ResultadoOperacion(id != null, flujoRecepcion.getMensaje());
        }
        boolean ok = existente == null
                ? pagoControlador.registrar(pago)
                : pagoControlador.modificar(pago);
        return new ResultadoOperacion(ok, pagoControlador.getMensaje());
    }

    private ResultadoOperacion guardarFactura(Object existente) {
        Factura factura = existente instanceof Factura f ? f : new Factura();
        factura.setEstadoFactura(requerido(valorReferencia(), "Seleccione el estado de la factura.").toUpperCase());
        factura.setFechaEmision(fechaFlexible(txtFecha.getText(), "Fecha de emision"));
        factura.setImpuesto(decimalNoNegativo(txtNumero.getText(), "Impuesto"));
        factura.setIdCliente(idSeleccionado(cboPersona, "Seleccione un cliente."));
        factura.setTotalDescuento(decimalNoNegativo(txtSegundo.getText(), "Descuento"));
        boolean ok = existente == null
                ? facturaControlador.registrar(factura)
                : facturaControlador.modificar(factura);
        return new ResultadoOperacion(ok, facturaControlador.getMensaje());
    }

    private ResultadoOperacion guardarDetalleFactura(Object existente) {
        DetalleFactura detalle = existente instanceof DetalleFactura d ? d : new DetalleFactura();
        detalle.setIdFactura(idSeleccionado(cboReferencia, "Seleccione una factura."));
        String tipo = valorCombo(cboPersona);
        detalle.setTipoConcepto(tipo == null ? requerido(txtFecha.getText(), "Ingrese el tipo de concepto.") : tipo);
        if (txtFecha.getText() != null && !txtFecha.getText().isBlank()) {
            detalle.setTipoConcepto(txtFecha.getText().trim());
        }
        detalle.setPrecioUnitario(decimalRequerido(txtNumero.getText(), "Precio unitario"));
        detalle.setCantidad(enteroPositivo(txtSegundo.getText(), "Cantidad"));
        String[] codigoImpuesto = partirCodigoImpuesto(txtTexto.getText());
        detalle.setCodigoReferencia(codigoImpuesto[0]);
        detalle.setPorcentajeImpuesto(new BigDecimal(codigoImpuesto[1]));
        boolean ok = existente == null
                ? detalleFacturaControlador.registrar(detalle)
                : detalleFacturaControlador.modificar(detalle);
        return new ResultadoOperacion(ok, detalleFacturaControlador.getMensaje());
    }

    private ResultadoOperacion guardarMetodoPago(Object existente) {
        MetodoPago metodo = existente instanceof MetodoPago m ? m : new MetodoPago();
        metodo.setNombreMetodo(requerido(valorReferencia(), "Ingrese el nombre del metodo."));
        metodo.setDescripcion(textoOpcional(txtFecha.getText()));
        metodo.setPorcentajeComision(decimalNoNegativo(txtNumero.getText(), "Comision"));
        metodo.setRequiereReferencia("SI".equalsIgnoreCase(valorCombo(cboPersona)));
        metodo.setPermiteCuotas("SI".equalsIgnoreCase(txtSegundo.getText().trim()));
        metodo.setEstadoMetodo(chkEstado.isSelected());
        boolean ok = existente == null
                ? metodoPagoControlador.registrar(metodo)
                : metodoPagoControlador.modificar(metodo);
        return new ResultadoOperacion(ok, metodoPagoControlador.getMensaje());
    }

    private ResultadoOperacion guardarComprobante(Object existente) {
        Comprobante comprobante = existente instanceof Comprobante c ? c : new Comprobante();
        comprobante.setIdPago(idSeleccionado(cboReferencia, "Seleccione un pago."));
        comprobante.setFechaEmision(fechaFlexible(txtFecha.getText(), "Fecha de emision"));
        comprobante.setTipoComprobante(requerido(txtNumero.getText(), "Ingrese el tipo de comprobante."));
        comprobante.setFormatoArchivo(valorCombo(cboPersona));
        comprobante.setCorreoEnvio(textoOpcional(txtSegundo.getText()));
        comprobante.setRutaArchivo(textoOpcional(txtTexto.getText()));
        comprobante.setEstadoComprobante(chkEstado.isSelected() ? "GENERADO" : "ANULADO");
        boolean ok = existente == null
                ? comprobanteControlador.registrar(comprobante)
                : comprobanteControlador.modificar(comprobante);
        return new ResultadoOperacion(ok, comprobanteControlador.getMensaje());
    }

    // ---------------------------------------------------------------------
    // CARGA DE SELECCION
    // ---------------------------------------------------------------------

    private void cargarSeleccionado() {
        Object registro = gestorConsulta.getSeleccionado();
        if (registro == null) {
            return;
        }
        configurando = true;
        try {
            switch (modulo) {
                case "ACCESO" -> cargarAcceso(registro);
                case "RUTINAS" -> cargarRutinas(registro);
                case "SALUD" -> cargarSalud(registro);
                case "NUTRICION" -> cargarNutricion(registro);
                case "FINANZAS" -> cargarFinanzas(registro);
                default -> {
                }
            }
        } finally {
            configurando = false;
        }
    }

    private void cargarAcceso(Object registro) {
        if (registro instanceof Reserva r) {
            seleccionarId(cboReferencia, r.getIdClase());
            seleccionarId(cboPersona, r.getIdCliente());
            txtFecha.setText(valor(r.getFechaHoraReserva()));
            txtNumero.setText(valor(r.getEstadoReserva()));
            txtSegundo.setText(valor(r.getMotivoCancelacion()));
            txtTexto.setText(valor(r.getObservaciones()));
            chkEstado.setSelected("ACTIVA".equalsIgnoreCase(r.getEstadoReserva()));
        } else if (registro instanceof Asistencia a) {
            seleccionarTextoReferencia(a.getHoraSalida() == null ? "Registrar entrada" : "Registrar salida");
            seleccionarId(cboPersona, a.getIdCliente());
            txtFecha.setText(valor(a.getFechaAsistencia()));
            txtNumero.setText(valor(a.getMetodoRegistro()));
            txtSegundo.setText(valor(a.getTipoAcceso()));
            txtTexto.setText(valor(a.getObservaciones()));
            chkEstado.setSelected(a.getHoraSalida() == null);
        } else if (registro instanceof ClaseGrupal c) {
            setTextoReferencia(c.getNombreClase());
            seleccionarId(cboPersona, c.getIdEntrenador());
            txtFecha.setText(valor(c.getFechaHora()));
            txtNumero.setText(valor(c.getCupoMaximo()));
            txtSegundo.setText(valor(c.getDuracionBaseMinutos()));
            txtTexto.setText(valor(c.getDescripcion()));
            chkEstado.setSelected(c.isEstadoClase());
        }
    }

    private void cargarRutinas(Object registro) {
        if (registro instanceof Rutina r) {
            setTextoReferencia(r.getNombreRutina());
            seleccionarId(cboPersona, r.getIdEntrenador());
            txtFecha.setText(valor(r.getFechaCreacion()));
            txtNumero.setText(valor(r.getDuracionSemanas()));
            txtSegundo.setText(valor(r.getNivel()));
            txtTexto.setText(valor(r.getDescripcion()));
            chkEstado.setSelected("ACTIVA".equalsIgnoreCase(r.getEstadoRutina()));
        } else if (registro instanceof AsignacionRutina a) {
            seleccionarId(cboReferencia, a.getIdRutina());
            seleccionarId(cboPersona, a.getIdCliente());
            txtFecha.setText(valor(a.getFechaInicio()));
            txtNumero.setText(valor(a.getFechaFin()));
            txtSegundo.setText(valor(a.getEstadoAsignacion()));
            txtTexto.setText(valor(a.getObservaciones()));
            chkEstado.setSelected("ACTIVA".equalsIgnoreCase(a.getEstadoAsignacion()));
        } else if (registro instanceof Ejercicio e) {
            setTextoReferencia(e.getNombreEjercicio());
            txtFecha.setText(valor(e.getTipoEjercicio()));
            txtNumero.setText(valor(e.getNivelDificultad()));
            txtSegundo.setText(valor(e.getInstrucciones()));
            txtTexto.setText(valor(e.getDescripcion()));
        } else if (registro instanceof RutinaDiaEntrenamiento d) {
            seleccionarId(cboReferencia, d.getIdRutina());
            txtFecha.setText(valor(d.getDiaEntrenamiento()));
        } else if (registro instanceof ProgresoRutina p) {
            seleccionarId(cboReferencia, p.getIdRutina());
            seleccionarId(cboPersona, p.getIdCliente());
            txtFecha.setText(valor(p.getFechaRegistro()));
            txtNumero.setText(valor(p.getSesionesCompletadas()) + "/" + valor(p.getSesionesPlanificadas()));
            txtSegundo.setText(valor(p.getPesoCorporal()));
            txtTexto.setText(valor(p.getNivelEsfuerzo()));
            chkEstado.setSelected(!"ANULADO".equalsIgnoreCase(p.getEstadoProgreso()));
        } else if (registro instanceof ContieneEjercicio d) {
            seleccionarId(cboReferencia, d.getIdRutina());
            seleccionarId(cboPersona, d.getIdEjercicio());
            txtFecha.setText(valor(d.getDiaSemana()));
            txtNumero.setText(valor(d.getSeries()));
            txtSegundo.setText(valor(d.getRepeticiones()));
            txtTexto.setText(valor(d.getOrden()));
        }
    }

    private void cargarSalud(Object registro) {
        if (registro instanceof EvaluacionFisica e) {
            seleccionarId(cboReferencia, e.getIdEntrenador());
            seleccionarId(cboPersona, e.getIdCliente());
            txtFecha.setText(valor(e.getFechaEvaluacion()));
            txtNumero.setText(valor(e.getTipoEvaluacion()));
            txtSegundo.setText(valor(e.getNivelRiesgo()));
            txtTexto.setText(valor(e.getMotivo()));
            chkEstado.setSelected(!"ANULADA".equalsIgnoreCase(e.getEstadoEvaluacion()));
        } else if (registro instanceof MedicionCorporal m) {
            seleccionarId(cboReferencia, m.getIdEvaluacion());
            txtFecha.setText(valor(m.getPesoKg()));
            txtNumero.setText(valor(m.getAlturaM()));
            txtSegundo.setText(valor(m.getPorcentajeGrasa()));
            txtTexto.setText(valor(m.getObservaciones()));
        } else if (registro instanceof IndicadorSalud i) {
            setTextoReferencia(i.getNombreIndicador());
            txtFecha.setText(valor(i.getUnidadMedida()));
            txtNumero.setText(valor(i.getValorMinimoReferencia()));
            seleccionarTextoCombo(cboPersona, i.getCategoria());
            txtSegundo.setText(valor(i.getValorMaximoReferencia()));
            txtTexto.setText(valor(i.getDescripcion()));
            chkEstado.setSelected(i.isEstadoIndicador());
        } else if (registro instanceof ResultadoIndicador r) {
            seleccionarId(cboReferencia, r.getIdEvaluacion());
            seleccionarId(cboPersona, r.getIdIndicador());
            txtFecha.setText(valor(r.getFechaRegistro()));
            txtNumero.setText(valor(r.getValorObtenido()));
            txtSegundo.setText(valor(r.getClasificacion()));
            txtTexto.setText(valor(r.getObservaciones()));
            chkEstado.setSelected(r.isFueraDeRango());
        } else if (registro instanceof Recomendacion r) {
            seleccionarId(cboReferencia, r.getIdEvaluacion());
            txtFecha.setText(valor(r.getFechaInicio()));
            txtNumero.setText(valor(r.getTipoRecomendacion()));
            seleccionarTextoCombo(cboPersona, r.getPrioridad());
            txtSegundo.setText(valor(r.getFechaFin()));
            txtTexto.setText(valor(r.getDescripcion()));
            chkEstado.setSelected("ACTIVA".equalsIgnoreCase(r.getEstadoRecomendacion()));
        }
    }

    private void cargarNutricion(Object registro) {
        if (registro instanceof Cliente c) {
            setTextoReferencia(c.getCodigoCliente());
            txtFecha.setText(valor(c.getFechaRegistro()));
            txtNumero.setText(valor(c.getPesoInicial()));
            txtSegundo.setText(valor(c.getPesoMeta()));
            txtTexto.setText(valor(c.getObservaciones()));
            chkEstado.setSelected(c.isEstadoCliente());
        } else if (registro instanceof PlanNutricional p) {
            seleccionarId(cboReferencia, p.getIdNutricionista());
            seleccionarId(cboPersona, p.getIdCliente());
            txtFecha.setText(valor(p.getFechaInicio()));
            txtNumero.setText(valor(p.getCaloriasObjetivo()));
            txtSegundo.setText(valor(p.getNombrePlan()));
            txtTexto.setText(valor(p.getRestriccionesGenerales()));
            chkEstado.setSelected("ACTIVO".equalsIgnoreCase(p.getEstadoPlan()));
        } else if (registro instanceof Alimento a) {
            setTextoReferencia(a.getNombreAlimento());
            txtFecha.setText(valor(a.getCategoria()));
            txtNumero.setText(valor(a.getPorcionReferenciaG()));
            txtSegundo.setText(valor(a.getProteinasG()));
            txtTexto.setText(valor(a.getDescripcion()));
            chkEstado.setSelected(true);
        } else if (registro instanceof IndicadorSalud i) {
            setTextoReferencia(i.getNombreIndicador());
            txtFecha.setText(valor(i.getUnidadMedida()));
            txtNumero.setText(valor(i.getValorMinimoReferencia()));
            seleccionarTextoCombo(cboPersona, i.getCategoria());
            txtSegundo.setText(valor(i.getValorMaximoReferencia()));
            txtTexto.setText(valor(i.getDescripcion()));
            chkEstado.setSelected(i.isEstadoIndicador());
        } else if (registro instanceof IncluyeAlimento i) {
            seleccionarId(cboReferencia, i.getIdPlanNutricional());
            seleccionarId(cboPersona, i.getIdAlimento());
            txtFecha.setText(valor(i.getDiaSemana()));
            txtNumero.setText(valor(i.getCantidad()));
            txtSegundo.setText(valor(i.getTipoComida()));
            String unidad = valor(i.getUnidadMedida());
            String indicacion = valor(i.getIndicaciones());
            txtTexto.setText(indicacion.isBlank() ? unidad : unidad + " | " + indicacion);
            chkEstado.setSelected("ACTIVO".equalsIgnoreCase(i.getEstadoDetalle()));
        } else if (registro instanceof ResultadoIndicador r) {
            seleccionarId(cboReferencia, r.getIdEvaluacion());
            seleccionarId(cboPersona, r.getIdIndicador());
            txtFecha.setText(valor(r.getFechaRegistro()));
            txtNumero.setText(valor(r.getValorObtenido()));
            txtSegundo.setText(valor(r.getClasificacion()));
            txtTexto.setText(valor(r.getObservaciones()));
            chkEstado.setSelected(r.isFueraDeRango());
        } else if (registro instanceof Recomendacion r) {
            seleccionarId(cboReferencia, r.getIdEvaluacion());
            txtFecha.setText(valor(r.getFechaInicio()));
            txtNumero.setText(valor(r.getTipoRecomendacion()));
            seleccionarTextoCombo(cboPersona, r.getPrioridad());
            txtSegundo.setText(valor(r.getFechaFin()));
            txtTexto.setText(valor(r.getDescripcion()));
            chkEstado.setSelected("ACTIVA".equalsIgnoreCase(r.getEstadoRecomendacion()));
        } else if (registro instanceof Nutricionista n) {
            setTextoReferencia(n.getNombreCompleto());
            txtFecha.setText(valor(n.getFechaInicioProfesion()));
            txtNumero.setText(valor(n.getNumeroLicencia()));
            seleccionarTextoCombo(cboPersona, n.getEstadoLicencia());
            txtSegundo.setText(valor(n.getCodigoEmpleado()));
            chkEstado.setSelected(n.isEstadoEmpleado());
        }
    }

    private void cargarFinanzas(Object registro) {
        if (registro instanceof Pago p) {
            seleccionarId(cboReferencia, p.getIdFactura());
            seleccionarId(cboPersona, p.getIdMetodoPago());
            txtFecha.setText(valor(p.getFechaHoraPago()));
            txtNumero.setText(valor(p.getMontoPago()));
            txtSegundo.setText(valor(p.getMontoRecibido()));
            txtTexto.setText(valor(p.getReferenciaTransaccion()));
            chkEstado.setSelected("CONFIRMADO".equalsIgnoreCase(p.getEstadoPago()));
        } else if (registro instanceof Factura f) {
            seleccionarTextoReferencia(f.getEstadoFactura());
            seleccionarId(cboPersona, f.getIdCliente());
            txtFecha.setText(valor(f.getFechaEmision()));
            txtNumero.setText(valor(f.getImpuesto()));
            txtSegundo.setText(valor(f.getTotalDescuento()));
            chkEstado.setSelected(!"ANULADA".equalsIgnoreCase(f.getEstadoFactura()));
        } else if (registro instanceof DetalleFactura d) {
            seleccionarId(cboReferencia, d.getIdFactura());
            txtFecha.setText(valor(d.getTipoConcepto()));
            txtNumero.setText(valor(d.getPrecioUnitario()));
            txtSegundo.setText(valor(d.getCantidad()));
            txtTexto.setText(valor(d.getCodigoReferencia()) + "|" + valor(d.getPorcentajeImpuesto()));
        } else if (registro instanceof MetodoPago m) {
            setTextoReferencia(m.getNombreMetodo());
            txtFecha.setText(valor(m.getDescripcion()));
            txtNumero.setText(valor(m.getPorcentajeComision()));
            seleccionarTextoCombo(cboPersona, m.isRequiereReferencia() ? "SI" : "NO");
            txtSegundo.setText(m.isPermiteCuotas() ? "SI" : "NO");
            chkEstado.setSelected(m.isEstadoMetodo());
        } else if (registro instanceof Comprobante c) {
            seleccionarId(cboReferencia, c.getIdPago());
            txtFecha.setText(valor(c.getFechaEmision()));
            txtNumero.setText(valor(c.getTipoComprobante()));
            seleccionarTextoCombo(cboPersona, c.getFormatoArchivo());
            txtSegundo.setText(valor(c.getCorreoEnvio()));
            txtTexto.setText(valor(c.getRutaArchivo()));
            chkEstado.setSelected(!"ANULADO".equalsIgnoreCase(c.getEstadoComprobante()));
        }
    }

    // ---------------------------------------------------------------------
    // COMPONENTES Y PARSEO
    // ---------------------------------------------------------------------

    private void labels(String referencia, String fecha, String numero,
            String persona, String segundo, String texto) {
        lblReferencia.setText(referencia);
        lblFecha.setText(fecha);
        lblNumero.setText(numero);
        lblPersona.setText(persona);
        lblSegundo.setText(segundo);
        lblTexto.setText(texto);
    }

    private void limpiarCamposBase() {
        cboReferencia.removeAllItems();
        cboReferencia.setEditable(false);
        txtFecha.setText("");
        txtNumero.setText("");
        txtSegundo.setText("");
        txtTexto.setText("");
        limpiarCombo(cboPersona);
        chkEstado.setSelected(true);
    }

    private void habilitarTodos() {
        cboReferencia.setEnabled(true);
        txtFecha.setEnabled(true);
        txtFecha.setEditable(true);
        txtNumero.setEnabled(true);
        txtNumero.setEditable(true);
        cboPersona.setEnabled(true);
        txtSegundo.setEnabled(true);
        txtSegundo.setEditable(true);
        txtTexto.setEnabled(true);
        txtTexto.setEditable(true);
        chkEstado.setEnabled(true);
    }

    private void deshabilitar(Object... componentes) {
        for (Object componente : componentes) {
            if (componente instanceof JTextField txt) {
                txt.setText("");
                txt.setEnabled(false);
            } else if (componente instanceof JCheckBox chk) {
                chk.setEnabled(false);
            }
        }
    }

    private void deshabilitarComboPersona() {
        limpiarCombo(cboPersona);
        cboPersona.setEnabled(false);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void cargarRelacion(JComboBox<?> combo, String campo, Object idSeleccionado) {
        JComboBox raw = combo;
        DefaultComboBoxModel modelo = new DefaultComboBoxModel();
        modelo.addElement(null);
        List<OpcionRelacion> opciones = catalogoRelacion.listarParaSesion(campo);
        for (OpcionRelacion opcion : opciones) {
            modelo.addElement(opcion);
        }
        raw.setModel(modelo);
        if (idSeleccionado != null) {
            seleccionarId(combo, idSeleccionado);
        }
    }

    private void recargarRelacion(JComboBox<?> combo, String campo) {
        Object id = idActual(combo);
        cargarRelacion(combo, campo, id);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void cargarOpcionesPersona(String... opciones) {
        JComboBox raw = cboPersona;
        DefaultComboBoxModel modelo = new DefaultComboBoxModel();
        for (String opcion : opciones) {
            modelo.addElement(opcion);
        }
        raw.setModel(modelo);
        if (opciones.length > 0) {
            raw.setSelectedIndex(0);
        }
    }

    private void cargarOpcionesReferencia(String... opciones) {
        DefaultComboBoxModel<Object> modelo = new DefaultComboBoxModel<>();
        for (String opcion : opciones) {
            modelo.addElement(opcion);
        }
        cboReferencia.setModel(modelo);
        cboReferencia.setEditable(false);
        if (opciones.length > 0) {
            cboReferencia.setSelectedIndex(0);
        }
    }

    private void cargarTextoReferencia(String texto) {
        cboReferencia.setModel(new DefaultComboBoxModel<>());
        cboReferencia.setEditable(true);
        cboReferencia.getEditor().setItem(texto == null ? "" : texto);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void limpiarCombo(JComboBox<?> combo) {
        ((JComboBox) combo).setModel(new DefaultComboBoxModel());
    }

    private Object idActual(JComboBox<?> combo) {
        Object seleccionado = combo.getSelectedItem();
        return seleccionado instanceof OpcionRelacion op ? op.getId() : null;
    }

    private Long idSeleccionado(JComboBox<?> combo, String mensaje) {
        Object seleccionado = combo.getSelectedItem();
        if (!(seleccionado instanceof OpcionRelacion opcion)
                || !(opcion.getId() instanceof Number numero)) {
            throw new IllegalArgumentException(mensaje);
        }
        return numero.longValue();
    }

    private void seleccionarId(JComboBox<?> combo, Object id) {
        if (id == null) {
            return;
        }
        long esperado = id instanceof Number n ? n.longValue() : Long.parseLong(id.toString());
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof OpcionRelacion opcion
                    && opcion.getId() instanceof Number n
                    && n.longValue() == esperado) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarTextoCombo(JComboBox<?> combo, String valor) {
        if (valor == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item != null && valor.equalsIgnoreCase(item.toString())) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void seleccionarTextoReferencia(String valor) {
        if (valor == null) {
            return;
        }
        for (int i = 0; i < cboReferencia.getItemCount(); i++) {
            Object item = cboReferencia.getItemAt(i);
            if (item != null && valor.equalsIgnoreCase(item.toString())) {
                cboReferencia.setSelectedIndex(i);
                return;
            }
        }
    }

    private void setTextoReferencia(String valor) {
        if (cboReferencia.isEditable()) {
            cboReferencia.getEditor().setItem(valor == null ? "" : valor);
        } else {
            seleccionarTextoReferencia(valor);
        }
    }

    private String valorReferencia() {
        Object valor = cboReferencia.isEditable()
                ? cboReferencia.getEditor().getItem()
                : cboReferencia.getSelectedItem();
        return valor == null ? "" : valor.toString().trim();
    }

    private String valorCombo(JComboBox<?> combo) {
        Object valor = combo.getSelectedItem();
        return valor == null ? null : valor.toString().trim();
    }

    private String procesoActual() {
        Object item = cboProceso.getSelectedItem();
        return item == null ? "" : item.toString();
    }

    private boolean usaClienteEnCombo() {
        String clave = modulo + "|" + procesoActual();
        return clave.equals("ACCESO|Reservas")
                || clave.equals("ACCESO|Asistencias")
                || clave.equals("RUTINAS|Asignar rutina a cliente")
                || clave.equals("RUTINAS|Registrar progreso del cliente")
                || clave.equals("SALUD|Evaluar cliente")
                || clave.equals("NUTRICION|Planes asignados a clientes")
                || clave.equals("FINANZAS|Facturas");
    }

    private void sincronizarClienteEnAtencion() {
        try {
            Object seleccionado = cboPersona.getSelectedItem();
            if (seleccionado instanceof OpcionRelacion opcion
                    && opcion.getId() instanceof Number n) {
                modelo.Cliente cliente = new ClienteControlador().buscar(n.longValue());
                if (cliente != null) {
                    ClienteEnAtencion.seleccionar(cliente);
                }
            }
        } catch (RuntimeException ex) {
            // El selector sigue siendo util aunque el banner no pueda actualizarse.
        }
    }

    private String requerido(String texto, String mensaje) {
        String valor = texto == null ? "" : texto.trim();
        if (valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
    }

    private String textoOpcional(String texto) {
        String valor = texto == null ? "" : texto.trim();
        return valor.isBlank() ? null : valor;
    }

    private LocalDate fechaFlexible(String texto, String nombre) {
        try {
            return LocalDate.parse(requerido(texto, nombre + " es obligatoria."));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(nombre + " debe tener formato AAAA-MM-DD.");
        }
    }

    private LocalDate fechaOpcional(String texto, String nombre) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return fechaFlexible(texto, nombre);
    }

    private LocalDateTime fechaHora(String texto, String nombre) {
        try {
            return LocalDateTime.parse(requerido(texto, nombre + " es obligatoria.")
                    .replace(' ', 'T'));
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(nombre + " debe tener formato AAAA-MM-DDTHH:MM.");
        }
    }

    private Integer enteroPositivo(String texto, String nombre) {
        try {
            int valor = Integer.parseInt(requerido(texto, nombre + " es obligatorio."));
            if (valor <= 0) {
                throw new IllegalArgumentException(nombre + " debe ser mayor a cero.");
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(nombre + " debe ser un numero entero.");
        }
    }

    private Integer enteroOpcionalPositivo(String texto, String nombre) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return enteroPositivo(texto, nombre);
    }

    private BigDecimal decimalRequerido(String texto, String nombre) {
        try {
            return new BigDecimal(requerido(texto, nombre + " es obligatorio.").replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(nombre + " debe ser numerico.");
        }
    }

    private BigDecimal decimalOpcional(String texto, String nombre) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return decimalRequerido(texto, nombre);
    }

    private BigDecimal decimalNoNegativo(String texto, String nombre) {
        BigDecimal valor = decimalRequerido(texto, nombre);
        if (valor.signum() < 0) {
            throw new IllegalArgumentException(nombre + " no puede ser negativo.");
        }
        return valor;
    }

    private int[] sesiones(String texto) {
        String valor = requerido(texto, "Ingrese sesiones como completadas/planificadas, por ejemplo 3/5.");
        String[] partes = valor.split("/");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Use el formato completadas/planificadas, por ejemplo 3/5.");
        }
        try {
            int completadas = Integer.parseInt(partes[0].trim());
            int planificadas = Integer.parseInt(partes[1].trim());
            if (completadas < 0 || planificadas < 0 || completadas > planificadas) {
                throw new IllegalArgumentException("Las sesiones completadas deben estar entre 0 y las planificadas.");
            }
            return new int[]{completadas, planificadas};
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Use numeros enteros en sesiones, por ejemplo 3/5.");
        }
    }

    private String[] partirUnidadIndicacion(String texto) {
        String valor = requerido(texto, "Ingrese la unidad de medida, por ejemplo g.");
        String[] partes = valor.split("\\|", 2);
        return new String[]{partes[0].trim(), partes.length > 1
            ? textoOpcional(partes[1]) : null};
    }

    private String[] partirCodigoImpuesto(String texto) {
        String valor = texto == null ? "" : texto.trim();
        String[] partes = valor.split("\\|", 2);
        String codigo = partes.length > 0 ? textoOpcional(partes[0]) : null;
        String impuesto = partes.length > 1 && !partes[1].isBlank()
                ? partes[1].trim().replace(',', '.') : "0";
        try {
            BigDecimal valorImpuesto = new BigDecimal(impuesto);
            if (valorImpuesto.signum() < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El impuesto debe ser numerico y no negativo.");
        }
        return new String[]{codigo, impuesto};
    }

    private String valor(Object objeto) {
        return objeto == null ? "" : objeto.toString();
    }

    private ResultadoOperacion tipoIncorrecto(String esperado) {
        return new ResultadoOperacion(false,
                "El registro seleccionado no corresponde a " + esperado + ".");
    }

    private void informar(ResultadoOperacion resultado, String exitoPredeterminado) {
        if (resultado.correcto()) {
            JOptionPane.showMessageDialog(padre,
                    resultado.mensaje() == null || resultado.mensaje().isBlank()
                            ? exitoPredeterminado : resultado.mensaje(),
                    "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
        } else {
            advertencia(resultado.mensaje());
        }
    }

    private void advertencia(String mensaje) {
        JOptionPane.showMessageDialog(padre,
                mensaje == null || mensaje.isBlank()
                        ? "No fue posible completar la operacion." : mensaje,
                "GYMNOVA", JOptionPane.WARNING_MESSAGE);
    }

    private record ResultadoOperacion(boolean correcto, String mensaje) {
    }
}
