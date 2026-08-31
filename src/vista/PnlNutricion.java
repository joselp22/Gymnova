package vista;

import controlador.NutricionistaWorkspaceControlador;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelo.Alimento;
import modelo.IncluyeAlimento;
import modelo.PlanNutricional;
import modelo.Recomendacion;
import modelo.ResultadoIndicador;
import utilidades.ClienteEnAtencion;

/**
 * Espacio profesional de Nutrición.
 *
 * El cliente se selecciona previamente desde "Mis clientes". Esta pantalla
 * se enfoca en plan nutricional y alimentación; indicadores, recomendaciones
 * y evolución se muestran como consulta para conservar una visión completa.
 */
public class PnlNutricion extends JPanel {

    private final NutricionistaWorkspaceControlador controlador
            = new NutricionistaWorkspaceControlador();

    private final JLabel lblCliente = new JLabel();
    private final JTabbedPane tabs = new JTabbedPane();

    // Plan
    private final JTextField txtNombrePlan = NutricionistaUI.campo();
    private final JTextField txtFechaInicio = NutricionistaUI.campo();
    private final JTextField txtFechaFin = NutricionistaUI.campo();
    private final JTextField txtCalorias = NutricionistaUI.campo();
    private final JTextField txtProteinas = NutricionistaUI.campo();
    private final JTextField txtCarbohidratos = NutricionistaUI.campo();
    private final JTextArea txtRestricciones = NutricionistaUI.area(3);
    private final JLabel lblEstadoPlan = new JLabel("Estado: —");
    private final JTable tblPlanes = new JTable();
    private final JButton btnCrearPlan = NutricionistaUI.boton("Crear plan");
    private final JButton btnModificarPlan = NutricionistaUI.botonSecundario("Modificar plan");
    private final JButton btnFinalizarPlan = NutricionistaUI.botonSecundario("Finalizar plan");
    private final JButton btnNuevoPlanCompleto = NutricionistaUI.boton("Agregar alimentos");
    private PlanNutricional planSeleccionado;

    // Alimentación
    private final JComboBox<String> cboDia = new JComboBox<>(new String[]{
        "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"
    });
    private final JComboBox<String> cboComida = new JComboBox<>(new String[]{
        // Debe coincidir con ck_incluye_alimento_tipo_comida en la BD.
        // "OTRO" no está permitido; se usa "SNACK".
        "DESAYUNO", "MEDIA_MANANA", "ALMUERZO", "MERIENDA", "CENA", "SNACK"
    });
    private final JTextField txtNombreAlimento = NutricionistaUI.campo();
    private final JTextField txtCantidad = NutricionistaUI.campo();
    private final JTextField txtUnidad = NutricionistaUI.campo();
    private final javax.swing.JSpinner spnHora = crearSpinnerHora();
    private final JTextField txtOrden = NutricionistaUI.campo();
    private final JTextArea txtIndicaciones = NutricionistaUI.area(3);
    private final JTable tblComidas = new JTable();
    private final JButton btnAgregarComida = NutricionistaUI.boton("Agregar alimento");
    private final JButton btnModificarComida = NutricionistaUI.botonSecundario("Modificar detalle");
    private List<IncluyeAlimento> comidas = new ArrayList<>();
    private IncluyeAlimento comidaSeleccionada;

    // Consulta
    private final JTable tblIndicadores = new JTable();
    private final JTable tblRecomendaciones = new JTable();
    private final JTable tblEvolucion = new JTable();

    // Evolución de peso (misma lógica que usa el entrenador, pero
    // apuntando al cliente en atención y en modo solo lectura).
    private final JLabel lblEvoPesoInicial = new JLabel("—");
    private final JLabel lblEvoPesoMeta = new JLabel("—");
    private final JLabel lblEvoPesoActual = new JLabel("—");
    private final JLabel lblEvoAvance = new JLabel("—");
    private final JLabel lblEvoTendencia = new JLabel(" ");

    public PnlNutricion() {
        construir();
        eventos();
        // Selector de calendario para los campos de fecha del plan
        // y para la hora de consumo dentro de la pestaña Alimentación.
        utilidades.CalendarioSelector.vincularFecha(txtFechaInicio);
        utilidades.CalendarioSelector.vincularFecha(txtFechaFin);
        refrescarDatos();
    }

    private void construir() {
        setLayout(new BorderLayout());
        setBackground(NutricionistaUI.FONDO);
        JPanel pagina = NutricionistaUI.pagina();
        pagina.add(NutricionistaUI.encabezado(
                "Gestión nutricional",
                "Planifique la alimentación del cliente seleccionado y consulte su evolución."),
                BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        JPanel banner = NutricionistaUI.bannerCliente();
        lblCliente.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        lblCliente.setForeground(NutricionistaUI.TEXTO);
        banner.add(lblCliente, BorderLayout.CENTER);
        centro.add(banner, BorderLayout.NORTH);

        tabs.addTab("Plan nutricional", crearPlan());
        // Las pestañas "Alimentación", "Indicadores", "Recomendaciones" y
        // "Evolución" se retiraron de aquí. Los alimentos se cargan desde
        // el diálogo del plan. La consulta de recomendaciones y la
        // evolución de peso del cliente ahora viven en el menú
        // "Seguimiento".
        centro.add(tabs, BorderLayout.CENTER);
        pagina.add(centro, BorderLayout.CENTER);
        add(pagina, BorderLayout.CENTER);
    }

    private JPanel crearPlan() {
        JPanel p = panelPestana();

        JPanel form = NutricionistaUI.tarjeta();
        form.setLayout(new BorderLayout(0, 12));
        JPanel titulo = new JPanel(new BorderLayout());
        titulo.setOpaque(false);
        titulo.add(NutricionistaUI.tituloSeccion("Plan del cliente"), BorderLayout.WEST);
        lblEstadoPlan.setForeground(NutricionistaUI.AZUL_OSCURO);
        titulo.add(lblEstadoPlan, BorderLayout.EAST);
        form.add(titulo, BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(2, 3, 10, 8));
        campos.setOpaque(false);
        agregarCampo(campos, "Nombre del plan *", txtNombrePlan);
        agregarCampo(campos, "Fecha inicio *", txtFechaInicio);
        agregarCampo(campos, "Fecha fin", txtFechaFin);
        agregarCampo(campos, "Calorías objetivo", txtCalorias);
        agregarCampo(campos, "Proteínas objetivo (g)", txtProteinas);
        agregarCampo(campos, "Carbohidratos objetivo (g)", txtCarbohidratos);
        form.add(campos, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout(10, 0));
        sur.setOpaque(false);
        JPanel restr = new JPanel(new BorderLayout(0, 4));
        restr.setOpaque(false);
        restr.add(NutricionistaUI.etiqueta("Restricciones generales"), BorderLayout.NORTH);
        restr.add(new JScrollPane(txtRestricciones), BorderLayout.CENTER);
        sur.add(restr, BorderLayout.CENTER);
        JPanel botones = new JPanel();
        botones.setOpaque(false);
        botones.setLayout(new javax.swing.BoxLayout(botones, javax.swing.BoxLayout.Y_AXIS));
        botones.add(btnCrearPlan);
        botones.add(javax.swing.Box.createVerticalStrut(6));
        botones.add(btnModificarPlan);
        botones.add(javax.swing.Box.createVerticalStrut(6));
        botones.add(btnFinalizarPlan);
        botones.add(javax.swing.Box.createVerticalStrut(6));
        // Abre una ventana dedicada para cargar los alimentos que debe
        // consumir el cliente dentro del plan activo. El encabezado del
        // plan (nombre, fechas, objetivos) se gestiona arriba con
        // "Crear plan" y "Modificar plan".
        btnNuevoPlanCompleto.setToolTipText(
                "Agregar los alimentos que van dentro del plan activo.");
        botones.add(btnNuevoPlanCompleto);
        sur.add(botones, BorderLayout.EAST);
        form.add(sur, BorderLayout.SOUTH);
        p.add(form, BorderLayout.NORTH);

        NutricionistaUI.tabla(tblPlanes);
        p.add(NutricionistaUI.scrollTabla(tblPlanes), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearAlimentacion() {
        JPanel p = panelPestana();
        JPanel form = NutricionistaUI.tarjeta();
        form.setLayout(new BorderLayout(0, 8));
        form.add(NutricionistaUI.tituloSeccion("Alimentación del plan activo"),
                BorderLayout.NORTH);

        // Layout compacto con GridBagLayout: 4 columnas de campos, labels
        // pequeños encima de cada control, filas ajustadas. Indicaciones va
        // ocupando toda la fila inferior con altura reducida.
        JPanel campos = new JPanel(new java.awt.GridBagLayout());
        campos.setOpaque(false);
        java.awt.Insets ins = new java.awt.Insets(4, 6, 4, 6);

        celda(campos, "Día",         cboDia,             0, 0, ins);
        celda(campos, "Tipo comida", cboComida,          1, 0, ins);
        celda(campos, "Alimento",    txtNombreAlimento,  2, 0, ins);
        celda(campos, "Cantidad",    txtCantidad,        3, 0, ins);

        celda(campos, "Unidad",      txtUnidad,          0, 1, ins);
        celda(campos, "Hora",        spnHora,            1, 1, ins);
        celda(campos, "Orden",       txtOrden,           2, 1, ins);

        // Indicaciones ocupando 2 columnas para no cortar el texto.
        java.awt.GridBagConstraints gbcInd = new java.awt.GridBagConstraints();
        gbcInd.gridx = 3;   gbcInd.gridy = 1;
        gbcInd.gridwidth = 1;
        gbcInd.fill = java.awt.GridBagConstraints.BOTH;
        gbcInd.weightx = 1;
        gbcInd.insets = ins;
        JPanel obs = new JPanel(new BorderLayout(0, 2));
        obs.setOpaque(false);
        obs.add(etiquetaCompacta("Indicaciones"), BorderLayout.NORTH);
        JScrollPane sc = new JScrollPane(txtIndicaciones);
        sc.setPreferredSize(new java.awt.Dimension(200, 44));
        obs.add(sc, BorderLayout.CENTER);
        campos.add(obs, gbcInd);

        form.add(campos, BorderLayout.CENTER);
        form.add(NutricionistaUI.filaBotones(btnAgregarComida, btnModificarComida),
                BorderLayout.SOUTH);
        p.add(form, BorderLayout.NORTH);

        NutricionistaUI.tabla(tblComidas);
        p.add(NutricionistaUI.scrollTabla(tblComidas), BorderLayout.CENTER);
        return p;
    }

    /** Añade una celda "label pequeño arriba + campo" en el grid. */
    private void celda(JPanel panel, String etiqueta, java.awt.Component campo,
            int x, int y, java.awt.Insets ins) {
        java.awt.GridBagConstraints g = new java.awt.GridBagConstraints();
        g.gridx = x; g.gridy = y;
        g.fill = java.awt.GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = ins;
        JPanel wrap = new JPanel(new BorderLayout(0, 2));
        wrap.setOpaque(false);
        wrap.add(etiquetaCompacta(etiqueta), BorderLayout.NORTH);
        wrap.add(campo, BorderLayout.CENTER);
        panel.add(wrap, g);
    }

    private JLabel etiquetaCompacta(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 11));
        l.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        return l;
    }

    /** Selector tipo reloj (spinner de tiempo HH:mm) para la hora de consumo. */
    private static javax.swing.JSpinner crearSpinnerHora() {
        javax.swing.SpinnerDateModel modelo =
                new javax.swing.SpinnerDateModel();
        javax.swing.JSpinner s = new javax.swing.JSpinner(modelo);
        s.setEditor(new javax.swing.JSpinner.DateEditor(s, "HH:mm"));
        // Arranca en 08:00 para que no aparezca la hora del sistema.
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 8);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        s.setValue(cal.getTime());
        return s;
    }

    private JPanel crearIndicadoresConsulta() {
        JPanel p = panelPestana();
        JPanel aviso = NutricionistaUI.tarjeta();
        aviso.setLayout(new BorderLayout());
        aviso.add(NutricionistaUI.tituloSeccion("Indicadores del cliente"), BorderLayout.WEST);
        JLabel l = new JLabel("Los resultados se registran desde Seguimiento.");
        l.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        aviso.add(l, BorderLayout.EAST);
        p.add(aviso, BorderLayout.NORTH);
        NutricionistaUI.tabla(tblIndicadores);
        p.add(NutricionistaUI.scrollTabla(tblIndicadores), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearRecomendacionesConsulta() {
        JPanel p = panelPestana();
        JPanel aviso = NutricionistaUI.tarjeta();
        aviso.setLayout(new BorderLayout());
        aviso.add(NutricionistaUI.tituloSeccion("Recomendaciones"), BorderLayout.WEST);
        JLabel l = new JLabel("Cree nuevas recomendaciones desde Seguimiento.");
        l.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        aviso.add(l, BorderLayout.EAST);
        p.add(aviso, BorderLayout.NORTH);
        NutricionistaUI.tabla(tblRecomendaciones);
        p.add(NutricionistaUI.scrollTabla(tblRecomendaciones), BorderLayout.CENTER);
        return p;
    }

    /**
     * Pestaña "Evolución": replica el seguimiento de peso corporal del
     * entrenador, pero para el cliente actualmente seleccionado por el
     * nutricionista. Solo lectura: muestra KPIs (peso inicial / meta /
     * actual / % avance) y la tabla histórica de mediciones.
     */
    private JPanel crearEvolucion() {
        JPanel p = panelPestana();

        // Cabecera
        JPanel aviso = NutricionistaUI.tarjeta();
        aviso.setLayout(new BorderLayout());
        aviso.add(NutricionistaUI.tituloSeccion(
                "Evolución de peso del cliente"), BorderLayout.WEST);
        JLabel ayuda = new JLabel(
                "Registrado por el entrenador desde \"Evaluaciones\".");
        ayuda.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        aviso.add(ayuda, BorderLayout.EAST);
        p.add(aviso, BorderLayout.NORTH);

        // KPIs
        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setOpaque(false);
        kpis.add(tarjetaKPIEvolucion("PESO INICIAL", lblEvoPesoInicial));
        kpis.add(tarjetaKPIEvolucion("PESO META", lblEvoPesoMeta));
        kpis.add(tarjetaKPIEvolucion("PESO ACTUAL", lblEvoPesoActual));
        kpis.add(tarjetaKPIEvolucion("% AVANCE HACIA META", lblEvoAvance));

        // Tabla con historia de mediciones
        NutricionistaUI.tabla(tblEvolucion);
        JPanel tarjetaTabla = NutricionistaUI.tarjeta();
        tarjetaTabla.setLayout(new BorderLayout(0, 8));
        JPanel cabTabla = new JPanel(new BorderLayout());
        cabTabla.setOpaque(false);
        cabTabla.add(NutricionistaUI.tituloSeccion(
                "Historial de mediciones"), BorderLayout.WEST);
        lblEvoTendencia.setFont(
                new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        cabTabla.add(lblEvoTendencia, BorderLayout.EAST);
        tarjetaTabla.add(cabTabla, BorderLayout.NORTH);
        tarjetaTabla.add(NutricionistaUI.scrollTabla(tblEvolucion),
                BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(kpis, BorderLayout.NORTH);
        centro.add(tarjetaTabla, BorderLayout.CENTER);
        p.add(centro, BorderLayout.CENTER);
        return p;
    }

    private JPanel tarjetaKPIEvolucion(String titulo, JLabel valor) {
        JPanel t = NutricionistaUI.tarjeta();
        t.setLayout(new BorderLayout(0, 6));
        valor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        valor.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        valor.setForeground(NutricionistaUI.TEXTO);
        JLabel l = new JLabel(titulo, javax.swing.SwingConstants.CENTER);
        l.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        l.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
        t.add(valor, BorderLayout.CENTER);
        t.add(l, BorderLayout.SOUTH);
        return t;
    }

    private JPanel panelPestana() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(NutricionistaUI.FONDO);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        return p;
    }

    private void eventos() {
        btnCrearPlan.addActionListener(e -> crearPlanNuevo());
        btnModificarPlan.addActionListener(e -> modificarPlan());
        btnFinalizarPlan.addActionListener(e -> finalizarPlan());
        btnNuevoPlanCompleto.addActionListener(e -> abrirDialogoNuevoPlan());
        tblPlanes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarPlan();
        });
        btnAgregarComida.addActionListener(e -> agregarComida());
        btnModificarComida.addActionListener(e -> modificarComida());
        tblComidas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarComida();
        });
        tabs.addChangeListener(e -> refrescarDatos());
    }

    public void refrescarDatos() {
        boolean hay = ClienteEnAtencion.hay();
        lblCliente.setText(hay
                ? "Cliente en atención: " + ClienteEnAtencion.descripcion()
                : "Ningún cliente seleccionado. Seleccione uno desde Mis clientes.");
        habilitarTrabajo(hay);
        cargarPlanes();
        cargarComidas();
        cargarConsultas();
    }

    private void habilitarTrabajo(boolean hay) {
        btnCrearPlan.setEnabled(hay);
        btnModificarPlan.setEnabled(false);
        btnFinalizarPlan.setEnabled(false);
        boolean hayPlanActivo = hay
                && controlador.planActivoClienteActual() != null;
        btnAgregarComida.setEnabled(hayPlanActivo);
        btnModificarComida.setEnabled(false);
        // El botón "Agregar alimentos" solo tiene sentido si ya existe
        // un plan activo al cual asociar los alimentos.
        btnNuevoPlanCompleto.setEnabled(hayPlanActivo);
    }

    private void cargarPlanes() {
        List<PlanNutricional> lista = controlador.listarPlanesClienteActual();
        DefaultTableModel m = modelo(new String[]{
            "Código", "Plan", "Inicio", "Fin", "Calorías", "Proteínas", "Carbohidratos", "Estado"
        });
        for (PlanNutricional x : lista) m.addRow(new Object[]{
            x.getCodigoPlan(), x.getNombrePlan(), x.getFechaInicio(), x.getFechaFin(),
            x.getCaloriasObjetivo(), x.getProteinasObjetivoG(), x.getCarbohidratosObjetivoG(), x.getEstadoPlan()
        });
        tblPlanes.setModel(m);
        planSeleccionado = null;
        PlanNutricional activo = controlador.planActivoClienteActual();
        lblEstadoPlan.setText(activo == null ? "Estado: sin plan activo"
                : "Estado: ACTIVO · " + activo.getNombrePlan());
        btnCrearPlan.setEnabled(ClienteEnAtencion.hay() && activo == null);
        btnModificarPlan.setEnabled(false);
        btnFinalizarPlan.setEnabled(false);
        btnNuevoPlanCompleto.setEnabled(activo != null);
    }

    private void seleccionarPlan() {
        int f = tblPlanes.getSelectedRow();
        if (f < 0) return;
        int m = tblPlanes.convertRowIndexToModel(f);
        List<PlanNutricional> lista = controlador.listarPlanesClienteActual();
        if (m >= lista.size()) return;
        planSeleccionado = lista.get(m);
        txtNombrePlan.setText(valor(planSeleccionado.getNombrePlan()));
        txtFechaInicio.setText(valor(planSeleccionado.getFechaInicio()));
        txtFechaFin.setText(valor(planSeleccionado.getFechaFin()));
        txtCalorias.setText(valor(planSeleccionado.getCaloriasObjetivo()));
        txtProteinas.setText(valor(planSeleccionado.getProteinasObjetivoG()));
        txtCarbohidratos.setText(valor(planSeleccionado.getCarbohidratosObjetivoG()));
        txtRestricciones.setText(valor(planSeleccionado.getRestriccionesGenerales()));
        boolean activo = "ACTIVO".equalsIgnoreCase(planSeleccionado.getEstadoPlan());
        btnModificarPlan.setEnabled(activo);
        btnFinalizarPlan.setEnabled(activo);
    }

    private void crearPlanNuevo() {
        try {
            Long id = controlador.crearPlan(
                    requerido(txtNombrePlan.getText(), "Ingrese el nombre del plan."),
                    fechaRequerida(txtFechaInicio.getText(), "Fecha de inicio"),
                    fechaOpcional(txtFechaFin.getText()),
                    enteroOpcional(txtCalorias.getText(), "Calorías"),
                    decimalOpcional(txtProteinas.getText(), "Proteínas"),
                    decimalOpcional(txtCarbohidratos.getText(), "Carbohidratos"),
                    texto(txtRestricciones.getText()));
            informar(id != null);
            if (id != null) { limpiarPlan(); refrescarDatos(); }
        } catch (IllegalArgumentException ex) { advertencia(ex.getMessage()); }
    }

    private void modificarPlan() {
        if (planSeleccionado == null) return;
        try {
            planSeleccionado.setNombrePlan(requerido(txtNombrePlan.getText(), "Ingrese el nombre del plan."));
            planSeleccionado.setFechaInicio(fechaRequerida(txtFechaInicio.getText(), "Fecha de inicio"));
            planSeleccionado.setFechaFin(fechaOpcional(txtFechaFin.getText()));
            planSeleccionado.setCaloriasObjetivo(enteroOpcional(txtCalorias.getText(), "Calorías"));
            planSeleccionado.setProteinasObjetivoG(decimalOpcional(txtProteinas.getText(), "Proteínas"));
            planSeleccionado.setCarbohidratosObjetivoG(decimalOpcional(txtCarbohidratos.getText(), "Carbohidratos"));
            planSeleccionado.setRestriccionesGenerales(texto(txtRestricciones.getText()));
            boolean ok = controlador.modificarPlan(planSeleccionado);
            informar(ok);
            if (ok) { limpiarPlan(); refrescarDatos(); }
        } catch (IllegalArgumentException ex) { advertencia(ex.getMessage()); }
    }

    private void finalizarPlan() {
        if (planSeleccionado == null) return;
        int op = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Finalizar el plan activo seleccionado?", "Finalizar plan",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (op != javax.swing.JOptionPane.YES_OPTION) return;
        boolean ok = controlador.finalizarPlan(planSeleccionado.getIdPlanNutricional());
        informar(ok);
        if (ok) { limpiarPlan(); refrescarDatos(); }
    }

    // El combo de alimentos se reemplazó por un campo de texto libre
    // (txtNombreAlimento). Cuando el nutricionista guarda la comida, el
    // controlador busca el alimento por nombre y lo crea si no existe
    // (obtenerOCrearAlimento). Por eso ya no hay carga de catálogo aquí.

    private void cargarComidas() {
        comidas = controlador.listarComidasPlanActivo();
        DefaultTableModel m = modelo(new String[]{
            "Día", "Comida", "Hora", "Alimento", "Cantidad", "Unidad", "Orden", "Indicaciones"
        });
        for (IncluyeAlimento x : comidas) m.addRow(new Object[]{
            x.getDiaSemana(), x.getTipoComida(), x.getHoraConsumo(),
            controlador.nombreAlimento(x.getIdAlimento()), x.getCantidad(),
            x.getUnidadMedida(), x.getOrdenComida(), x.getIndicaciones()
        });
        tblComidas.setModel(m);
        comidaSeleccionada = null;
        btnModificarComida.setEnabled(false);
        btnAgregarComida.setEnabled(ClienteEnAtencion.hay() && controlador.planActivoClienteActual() != null);
    }

    private void seleccionarComida() {
        int f = tblComidas.getSelectedRow();
        if (f < 0) return;
        int m = tblComidas.convertRowIndexToModel(f);
        if (m >= comidas.size()) return;
        comidaSeleccionada = comidas.get(m);
        cboDia.setSelectedItem(comidaSeleccionada.getDiaSemana());
        cboComida.setSelectedItem(comidaSeleccionada.getTipoComida());
        txtNombreAlimento.setText(controlador.nombreAlimento(
                comidaSeleccionada.getIdAlimento()));
        txtCantidad.setText(valor(comidaSeleccionada.getCantidad()));
        txtUnidad.setText(valor(comidaSeleccionada.getUnidadMedida()));
        establecerHoraSpinner(comidaSeleccionada.getHoraConsumo());
        txtOrden.setText(valor(comidaSeleccionada.getOrdenComida()));
        txtIndicaciones.setText(valor(comidaSeleccionada.getIndicaciones()));
        btnModificarComida.setEnabled(true);
    }

    private void agregarComida() {
        try {
            String nombre = requerido(txtNombreAlimento.getText(),
                    "Escriba el nombre del alimento.");
            Long idAlimento = controlador.obtenerOCrearAlimento(nombre);
            if (idAlimento == null) {
                throw new IllegalArgumentException(controlador.getMensaje().isBlank()
                        ? "No se pudo registrar el alimento."
                        : controlador.getMensaje());
            }
            Long id = controlador.agregarComida(
                    String.valueOf(cboDia.getSelectedItem()),
                    String.valueOf(cboComida.getSelectedItem()),
                    horaSpinner(),
                    decimalRequerido(txtCantidad.getText(), "Cantidad"),
                    requerido(txtUnidad.getText(), "Ingrese la unidad de medida."),
                    enteroRequerido(txtOrden.getText(), "Orden"),
                    idAlimento, texto(txtIndicaciones.getText()));
            informar(id != null);
            if (id != null) { limpiarComida(); cargarComidas(); }
        } catch (IllegalArgumentException ex) { advertencia(ex.getMessage()); }
    }

    private void modificarComida() {
        if (comidaSeleccionada == null) return;
        try {
            String nombre = requerido(txtNombreAlimento.getText(),
                    "Escriba el nombre del alimento.");
            Long idAlimento = controlador.obtenerOCrearAlimento(nombre);
            if (idAlimento == null) {
                throw new IllegalArgumentException(controlador.getMensaje().isBlank()
                        ? "No se pudo registrar el alimento."
                        : controlador.getMensaje());
            }
            comidaSeleccionada.setDiaSemana(String.valueOf(cboDia.getSelectedItem()));
            comidaSeleccionada.setTipoComida(String.valueOf(cboComida.getSelectedItem()));
            comidaSeleccionada.setIdAlimento(idAlimento);
            comidaSeleccionada.setCantidad(decimalRequerido(txtCantidad.getText(), "Cantidad"));
            comidaSeleccionada.setUnidadMedida(requerido(txtUnidad.getText(), "Ingrese la unidad de medida."));
            comidaSeleccionada.setHoraConsumo(horaSpinner());
            comidaSeleccionada.setOrdenComida(enteroRequerido(txtOrden.getText(), "Orden"));
            comidaSeleccionada.setIndicaciones(texto(txtIndicaciones.getText()));
            boolean ok = controlador.modificarComida(comidaSeleccionada);
            informar(ok);
            if (ok) { limpiarComida(); cargarComidas(); }
        } catch (IllegalArgumentException ex) { advertencia(ex.getMessage()); }
    }

    /** Lee la hora seleccionada en el spinner-reloj como LocalTime. */
    private LocalTime horaSpinner() {
        Object v = spnHora.getValue();
        if (!(v instanceof java.util.Date d)) {
            return null;
        }
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(d);
        return LocalTime.of(
                cal.get(java.util.Calendar.HOUR_OF_DAY),
                cal.get(java.util.Calendar.MINUTE));
    }

    /** Pone el spinner-reloj en la hora indicada (o 08:00 si es null). */
    private void establecerHoraSpinner(LocalTime hora) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        LocalTime h = hora == null ? LocalTime.of(8, 0) : hora;
        cal.set(java.util.Calendar.HOUR_OF_DAY, h.getHour());
        cal.set(java.util.Calendar.MINUTE, h.getMinute());
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        spnHora.setValue(cal.getTime());
    }

    private void abrirDialogoNuevoPlan() {
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        java.awt.Frame padre = w instanceof java.awt.Frame f ? f : null;
        DlgPlanNutricionalNuevo dlg = new DlgPlanNutricionalNuevo(padre);
        dlg.setVisible(true);
        // Al cerrarse el diálogo, refrescamos por si se creó el plan.
        refrescarDatos();
    }

    private void cargarConsultas() {
        // La tabla de indicadores ya no se muestra, pero se conserva su
        // llenado por compatibilidad con el resto de la clase.
        DefaultTableModel i = modelo(new String[]{"Fecha", "Indicador",
            "Valor", "Clasificación", "Fuera de rango"});
        List<ResultadoIndicador> resultados
                = controlador.listarResultadosClienteActual();
        for (ResultadoIndicador r : resultados) i.addRow(new Object[]{
            r.getFechaRegistro(),
            controlador.nombreIndicador(r.getIdIndicador()),
            r.getValorObtenido(), r.getClasificacion(),
            r.isFueraDeRango() ? "SÍ" : "NO"
        });
        tblIndicadores.setModel(i);

        DefaultTableModel rec = modelo(new String[]{"Fecha", "Tipo",
            "Título", "Prioridad", "Estado"});
        for (Recomendacion r
                : controlador.listarRecomendacionesClienteActual()) {
            rec.addRow(new Object[]{
                r.getFechaRecomendacion(), r.getTipoRecomendacion(),
                r.getTitulo(), r.getPrioridad(),
                r.getEstadoRecomendacion()});
        }
        tblRecomendaciones.setModel(rec);

        cargarEvolucionPeso();
    }

    /**
     * Rellena la tabla y los KPIs de la pestaña "Evolución" con la
     * historia de peso del cliente en atención. Consulta las tablas
     * evaluacion_fisica + medicion_corporal, igual que la vista del
     * entrenador. Si no hay cliente seleccionado o no hay historial,
     * deja el bloque en su estado vacío ("—").
     */
    private void cargarEvolucionPeso() {
        DefaultTableModel m = modelo(new String[]{
            "Fecha", "Peso (kg)", "Cambio vs. anterior",
            "Distancia a la meta", "Observaciones"});
        // Reset KPIs
        lblEvoPesoInicial.setText("—");
        lblEvoPesoMeta.setText("—");
        lblEvoPesoActual.setText("—");
        lblEvoAvance.setText("—");
        lblEvoTendencia.setText(" ");
        tblEvolucion.setModel(m);

        modelo.Cliente cliente = ClienteEnAtencion.actual();
        if (cliente == null || cliente.getIdPersona() == null) {
            return;
        }
        Double pesoInicial = cliente.getPesoInicial() == null
                ? null : cliente.getPesoInicial().doubleValue();
        Double pesoMeta = cliente.getPesoMeta() == null
                ? null : cliente.getPesoMeta().doubleValue();

        // Consulta: fecha + peso + observaciones de cada medición.
        List<Object[]> filas = new java.util.ArrayList<>();
        String sql = "SELECT ef.fecha_evaluacion, mc.peso_kg, mc.observaciones "
                + "FROM evaluacion_fisica ef "
                + "JOIN medicion_corporal mc "
                + "  ON mc.id_evaluacion = ef.id_evaluacion "
                + "WHERE ef.id_cliente = ? "
                + "  AND mc.peso_kg IS NOT NULL "
                + "ORDER BY ef.fecha_evaluacion, ef.id_evaluacion";
        try (java.sql.Connection c
                     = conexion.ConexionPostgreSQL.getConexion();
             java.sql.PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, cliente.getIdPersona());
            try (java.sql.ResultSet r = s.executeQuery()) {
                Double anterior = null;
                while (r.next()) {
                    java.sql.Date f = r.getDate(1);
                    java.math.BigDecimal p = r.getBigDecimal(2);
                    String obs = r.getString(3);
                    if (f == null || p == null) continue;
                    double peso = p.doubleValue();
                    String cambio = anterior == null ? "—"
                            : String.format(java.util.Locale.US,
                                    "%+.2f kg", peso - anterior);
                    String distMeta = pesoMeta == null ? "—"
                            : String.format(java.util.Locale.US,
                                    "%.2f kg", peso - pesoMeta);
                    filas.add(new Object[]{f.toLocalDate().toString(),
                        String.format(java.util.Locale.US,
                                "%.2f", peso),
                        cambio, distMeta,
                        obs == null ? "" : obs});
                    anterior = peso;
                }
            }
        } catch (java.sql.SQLException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No fue posible cargar la historia de peso: "
                    + ex.getMessage(),
                    "GYMNOVA",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Muestra la historia más reciente arriba.
        for (int idx = filas.size() - 1; idx >= 0; idx--) {
            m.addRow(filas.get(idx));
        }

        // KPIs
        lblEvoPesoInicial.setText(pesoInicial == null ? "—"
                : String.format(java.util.Locale.US, "%.2f kg",
                        pesoInicial));
        lblEvoPesoMeta.setText(pesoMeta == null ? "—"
                : String.format(java.util.Locale.US, "%.2f kg", pesoMeta));

        Double actual = filas.isEmpty() ? null
                : Double.parseDouble(String.valueOf(
                        filas.get(filas.size() - 1)[1]));
        lblEvoPesoActual.setText(actual == null ? "—"
                : String.format(java.util.Locale.US, "%.2f kg", actual));

        if (pesoInicial != null && pesoMeta != null && actual != null
                && Math.abs(pesoInicial - pesoMeta) > 0.001) {
            double avanzado = pesoInicial - actual;
            double objetivo = pesoInicial - pesoMeta;
            double pct = Math.max(0.0, Math.min(100.0,
                    (avanzado / objetivo) * 100.0));
            lblEvoAvance.setText(String.format(java.util.Locale.US,
                    "%.0f %%", pct));
        }

        if (filas.size() >= 2 && pesoMeta != null && actual != null) {
            double anteriorPeso = Double.parseDouble(String.valueOf(
                    filas.get(filas.size() - 2)[1]));
            double distActual = Math.abs(actual - pesoMeta);
            double distAnterior = Math.abs(anteriorPeso - pesoMeta);
            double diff = distAnterior - distActual;
            if (Math.abs(diff) < 0.1) {
                lblEvoTendencia.setText("Estancado");
                lblEvoTendencia.setForeground(new java.awt.Color(160, 174, 192));
            } else if (diff > 0) {
                lblEvoTendencia.setText("↗ Progresando");
                lblEvoTendencia.setForeground(NutricionistaUI.VERDE);
            } else {
                lblEvoTendencia.setText("↘ Retrocediendo");
                lblEvoTendencia.setForeground(NutricionistaUI.ROJO);
            }
        }
    }

    private void agregarCampo(JPanel panel, String etiqueta, java.awt.Component campo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(NutricionistaUI.etiqueta(etiqueta), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        panel.add(p);
    }

    private void limpiarPlan() {
        planSeleccionado = null;
        txtNombrePlan.setText(""); txtFechaInicio.setText(""); txtFechaFin.setText("");
        txtCalorias.setText(""); txtProteinas.setText(""); txtCarbohidratos.setText("");
        txtRestricciones.setText(""); tblPlanes.clearSelection();
    }

    private void limpiarComida() {
        comidaSeleccionada = null;
        txtNombreAlimento.setText("");
        txtCantidad.setText(""); txtUnidad.setText("");
        establecerHoraSpinner(null);
        txtOrden.setText(""); txtIndicaciones.setText(""); tblComidas.clearSelection();
    }

    private String requerido(String s, String msg) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(msg);
        return s.trim();
    }
    private String texto(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private LocalDate fechaRequerida(String s, String nombre) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(nombre + " es obligatoria.");
        return fechaOpcional(s);
    }
    private LocalDate fechaOpcional(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); }
        catch (java.time.format.DateTimeParseException ex) { throw new IllegalArgumentException("La fecha debe tener formato AAAA-MM-DD."); }
    }
    private LocalTime horaOpcional(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalTime.parse(s.trim()); }
        catch (java.time.format.DateTimeParseException ex) { throw new IllegalArgumentException("La hora debe tener formato HH:MM."); }
    }
    private Integer enteroOpcional(String s, String nombre) {
        if (s == null || s.isBlank()) return null;
        return enteroRequerido(s, nombre);
    }
    private Integer enteroRequerido(String s, String nombre) {
        try {
            int v = Integer.parseInt(requerido(s, nombre + " es obligatorio."));
            if (v <= 0) throw new IllegalArgumentException(nombre + " debe ser mayor que cero.");
            return v;
        } catch (NumberFormatException ex) { throw new IllegalArgumentException(nombre + " debe ser un número entero."); }
    }
    private BigDecimal decimalOpcional(String s, String nombre) {
        if (s == null || s.isBlank()) return null;
        return decimalRequerido(s, nombre);
    }
    private BigDecimal decimalRequerido(String s, String nombre) {
        try {
            BigDecimal v = new BigDecimal(requerido(s, nombre + " es obligatorio.").replace(',', '.'));
            if (v.signum() < 0) throw new IllegalArgumentException(nombre + " no puede ser negativo.");
            return v;
        } catch (NumberFormatException ex) { throw new IllegalArgumentException(nombre + " debe ser numérico."); }
    }
    private String valor(Object o) { return o == null ? "" : o.toString(); }

    private void informar(boolean ok) {
        javax.swing.JOptionPane.showMessageDialog(this,
                controlador.getMensaje().isBlank() ? (ok ? "Operación completada." : "No fue posible completar la operación.") : controlador.getMensaje(),
                "GYMNOVA", ok ? javax.swing.JOptionPane.INFORMATION_MESSAGE : javax.swing.JOptionPane.WARNING_MESSAGE);
    }
    private void advertencia(String s) {
        javax.swing.JOptionPane.showMessageDialog(this, s, "GYMNOVA", javax.swing.JOptionPane.WARNING_MESSAGE);
    }
    private DefaultTableModel modelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }
}
