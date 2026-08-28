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
    private PlanNutricional planSeleccionado;

    // Alimentación
    private final JComboBox<String> cboDia = new JComboBox<>(new String[]{
        "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"
    });
    private final JComboBox<String> cboComida = new JComboBox<>(new String[]{
        "DESAYUNO", "MEDIA_MANANA", "ALMUERZO", "MERIENDA", "CENA", "OTRO"
    });
    private final JComboBox<Alimento> cboAlimento = new JComboBox<>();
    private final JTextField txtCantidad = NutricionistaUI.campo();
    private final JTextField txtUnidad = NutricionistaUI.campo();
    private final JTextField txtHora = NutricionistaUI.campo();
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

    public PnlNutricion() {
        construir();
        eventos();
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
        tabs.addTab("Alimentación", crearAlimentacion());
        tabs.addTab("Indicadores", crearIndicadoresConsulta());
        tabs.addTab("Recomendaciones", crearRecomendacionesConsulta());
        tabs.addTab("Evolución", crearEvolucion());
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
        form.setLayout(new BorderLayout(0, 10));
        form.add(NutricionistaUI.tituloSeccion("Alimentación del plan activo"), BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(2, 4, 10, 8));
        campos.setOpaque(false);
        agregarCampo(campos, "Día *", cboDia);
        agregarCampo(campos, "Tipo de comida *", cboComida);
        agregarCampo(campos, "Alimento *", cboAlimento);
        agregarCampo(campos, "Cantidad *", txtCantidad);
        agregarCampo(campos, "Unidad *", txtUnidad);
        agregarCampo(campos, "Hora (HH:MM)", txtHora);
        agregarCampo(campos, "Orden *", txtOrden);
        JPanel obs = new JPanel(new BorderLayout(0, 4));
        obs.setOpaque(false);
        obs.add(NutricionistaUI.etiqueta("Indicaciones"), BorderLayout.NORTH);
        obs.add(new JScrollPane(txtIndicaciones), BorderLayout.CENTER);
        campos.add(obs);
        form.add(campos, BorderLayout.CENTER);
        form.add(NutricionistaUI.filaBotones(btnAgregarComida, btnModificarComida), BorderLayout.SOUTH);
        p.add(form, BorderLayout.NORTH);

        NutricionistaUI.tabla(tblComidas);
        p.add(NutricionistaUI.scrollTabla(tblComidas), BorderLayout.CENTER);
        return p;
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

    private JPanel crearEvolucion() {
        JPanel p = panelPestana();
        JPanel aviso = NutricionistaUI.tarjeta();
        aviso.setLayout(new BorderLayout());
        aviso.add(NutricionistaUI.tituloSeccion("Evolución de indicadores"), BorderLayout.WEST);
        p.add(aviso, BorderLayout.NORTH);
        NutricionistaUI.tabla(tblEvolucion);
        p.add(NutricionistaUI.scrollTabla(tblEvolucion), BorderLayout.CENTER);
        return p;
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
        cargarAlimentos();
        cargarComidas();
        cargarConsultas();
    }

    private void habilitarTrabajo(boolean hay) {
        btnCrearPlan.setEnabled(hay);
        btnModificarPlan.setEnabled(false);
        btnFinalizarPlan.setEnabled(false);
        btnAgregarComida.setEnabled(hay && controlador.planActivoClienteActual() != null);
        btnModificarComida.setEnabled(false);
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

    private void cargarAlimentos() {
        Alimento actual = (Alimento) cboAlimento.getSelectedItem();
        DefaultComboBoxModel<Alimento> m = new DefaultComboBoxModel<>();
        for (Alimento a : controlador.listarAlimentos("")) m.addElement(a);
        cboAlimento.setModel(m);
        if (actual != null) {
            for (int i = 0; i < m.getSize(); i++) {
                if (m.getElementAt(i).getIdAlimento().equals(actual.getIdAlimento())) {
                    cboAlimento.setSelectedIndex(i); break;
                }
            }
        }
    }

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
        seleccionarAlimento(comidaSeleccionada.getIdAlimento());
        txtCantidad.setText(valor(comidaSeleccionada.getCantidad()));
        txtUnidad.setText(valor(comidaSeleccionada.getUnidadMedida()));
        txtHora.setText(valor(comidaSeleccionada.getHoraConsumo()));
        txtOrden.setText(valor(comidaSeleccionada.getOrdenComida()));
        txtIndicaciones.setText(valor(comidaSeleccionada.getIndicaciones()));
        btnModificarComida.setEnabled(true);
    }

    private void agregarComida() {
        try {
            Alimento a = (Alimento) cboAlimento.getSelectedItem();
            if (a == null) throw new IllegalArgumentException("Seleccione un alimento.");
            Long id = controlador.agregarComida(
                    String.valueOf(cboDia.getSelectedItem()),
                    String.valueOf(cboComida.getSelectedItem()),
                    horaOpcional(txtHora.getText()),
                    decimalRequerido(txtCantidad.getText(), "Cantidad"),
                    requerido(txtUnidad.getText(), "Ingrese la unidad de medida."),
                    enteroRequerido(txtOrden.getText(), "Orden"),
                    a.getIdAlimento(), texto(txtIndicaciones.getText()));
            informar(id != null);
            if (id != null) { limpiarComida(); cargarComidas(); }
        } catch (IllegalArgumentException ex) { advertencia(ex.getMessage()); }
    }

    private void modificarComida() {
        if (comidaSeleccionada == null) return;
        try {
            Alimento a = (Alimento) cboAlimento.getSelectedItem();
            if (a == null) throw new IllegalArgumentException("Seleccione un alimento.");
            comidaSeleccionada.setDiaSemana(String.valueOf(cboDia.getSelectedItem()));
            comidaSeleccionada.setTipoComida(String.valueOf(cboComida.getSelectedItem()));
            comidaSeleccionada.setIdAlimento(a.getIdAlimento());
            comidaSeleccionada.setCantidad(decimalRequerido(txtCantidad.getText(), "Cantidad"));
            comidaSeleccionada.setUnidadMedida(requerido(txtUnidad.getText(), "Ingrese la unidad de medida."));
            comidaSeleccionada.setHoraConsumo(horaOpcional(txtHora.getText()));
            comidaSeleccionada.setOrdenComida(enteroRequerido(txtOrden.getText(), "Orden"));
            comidaSeleccionada.setIndicaciones(texto(txtIndicaciones.getText()));
            boolean ok = controlador.modificarComida(comidaSeleccionada);
            informar(ok);
            if (ok) { limpiarComida(); cargarComidas(); }
        } catch (IllegalArgumentException ex) { advertencia(ex.getMessage()); }
    }

    private void cargarConsultas() {
        DefaultTableModel i = modelo(new String[]{"Fecha", "Indicador", "Valor", "Clasificación", "Fuera de rango"});
        List<ResultadoIndicador> resultados = controlador.listarResultadosClienteActual();
        for (ResultadoIndicador r : resultados) i.addRow(new Object[]{
            r.getFechaRegistro(), controlador.nombreIndicador(r.getIdIndicador()),
            r.getValorObtenido(), r.getClasificacion(), r.isFueraDeRango() ? "SÍ" : "NO"
        });
        tblIndicadores.setModel(i);
        tblEvolucion.setModel(i);

        DefaultTableModel rec = modelo(new String[]{"Fecha", "Tipo", "Título", "Prioridad", "Estado"});
        for (Recomendacion r : controlador.listarRecomendacionesClienteActual()) rec.addRow(new Object[]{
            r.getFechaRecomendacion(), r.getTipoRecomendacion(), r.getTitulo(), r.getPrioridad(), r.getEstadoRecomendacion()
        });
        tblRecomendaciones.setModel(rec);
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
        txtCantidad.setText(""); txtUnidad.setText(""); txtHora.setText("");
        txtOrden.setText(""); txtIndicaciones.setText(""); tblComidas.clearSelection();
    }

    private void seleccionarAlimento(Long id) {
        for (int i = 0; i < cboAlimento.getItemCount(); i++) {
            Alimento a = cboAlimento.getItemAt(i);
            if (a != null && a.getIdAlimento().equals(id)) { cboAlimento.setSelectedIndex(i); return; }
        }
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
