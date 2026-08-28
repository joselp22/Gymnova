package vista;

import controlador.NutricionistaWorkspaceControlador;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.IndicadorSalud;
import modelo.MedicionCorporal;
import modelo.Recomendacion;
import modelo.ResultadoIndicador;
import utilidades.ClienteEnAtencion;

/** Seguimiento profesional del cliente seleccionado por el Nutricionista. */
public class PnlSeguimientoNutricionista extends JPanel {

    private final NutricionistaWorkspaceControlador controlador
            = new NutricionistaWorkspaceControlador();

    private final JLabel lblCliente = new JLabel();
    private final JTabbedPane tabs = new JTabbedPane();

    private final JTextField txtPeso = NutricionistaUI.campo();
    private final JTextField txtAltura = NutricionistaUI.campo();
    private final JTextField txtGrasa = NutricionistaUI.campo();
    private final JTextField txtCintura = NutricionistaUI.campo();
    private final JTextArea txtObsMedicion = NutricionistaUI.area(3);
    private final JTable tblMediciones = new JTable();

    private final JComboBox<IndicadorSalud> cboIndicador = new JComboBox<>();
    private final JTextField txtValorIndicador = NutricionistaUI.campo();
    private final JTextField txtClasificacion = NutricionistaUI.campo();
    private final JTextArea txtObsIndicador = NutricionistaUI.area(3);
    private final JLabel lblRango = new JLabel("Rango de referencia: —");
    private final JTable tblResultados = new JTable();

    private final JComboBox<String> cboTipoRec = new JComboBox<>(new String[]{
        "NUTRICIONAL", "HABITO", "HIDRATACION", "GENERAL"
    });
    private final JComboBox<String> cboPrioridad = new JComboBox<>(new String[]{
        "BAJA", "MEDIA", "ALTA"
    });
    private final JTextField txtTituloRec = NutricionistaUI.campo();
    private final JTextField txtInicioRec = NutricionistaUI.campo();
    private final JTextField txtFinRec = NutricionistaUI.campo();
    private final JTextArea txtDescripcionRec = NutricionistaUI.area(4);
    private final JTable tblRecomendaciones = new JTable();

    private final JTable tblEvolucionMedidas = new JTable();
    private final JTable tblEvolucionIndicadores = new JTable();

    public PnlSeguimientoNutricionista() {
        construir();
        eventos();
        refrescarDatos();
    }

    private void construir() {
        setLayout(new BorderLayout());
        setBackground(NutricionistaUI.FONDO);
        JPanel pagina = NutricionistaUI.pagina();
        pagina.add(NutricionistaUI.encabezado(
                "Seguimiento nutricional",
                "Registre mediciones, indicadores y recomendaciones del cliente en atención."),
                BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        JPanel banner = NutricionistaUI.bannerCliente();
        lblCliente.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        lblCliente.setForeground(NutricionistaUI.TEXTO);
        banner.add(lblCliente, BorderLayout.CENTER);
        centro.add(banner, BorderLayout.NORTH);

        tabs.addTab("Mediciones", crearMediciones());
        tabs.addTab("Indicadores", crearIndicadores());
        tabs.addTab("Recomendaciones", crearRecomendaciones());
        tabs.addTab("Evolución", crearEvolucion());
        centro.add(tabs, BorderLayout.CENTER);
        pagina.add(centro, BorderLayout.CENTER);
        add(pagina, BorderLayout.CENTER);
    }

    private JPanel crearMediciones() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(NutricionistaUI.FONDO);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel form = NutricionistaUI.tarjeta();
        form.setLayout(new BorderLayout(0, 10));
        form.add(NutricionistaUI.tituloSeccion("Nueva medición"), BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(2, 4, 10, 8));
        campos.setOpaque(false);
        agregarCampo(campos, "Peso (kg)", txtPeso);
        agregarCampo(campos, "Altura (m)", txtAltura);
        agregarCampo(campos, "Grasa corporal (%)", txtGrasa);
        agregarCampo(campos, "Cintura (cm)", txtCintura);
        form.add(campos, BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout(10, 0));
        sur.setOpaque(false);
        sur.add(new JScrollPane(txtObsMedicion), BorderLayout.CENTER);
        JButton btn = NutricionistaUI.boton("Registrar medición");
        sur.add(btn, BorderLayout.EAST);
        btn.addActionListener(e -> registrarMedicion());
        form.add(sur, BorderLayout.SOUTH);
        p.add(form, BorderLayout.NORTH);

        NutricionistaUI.tabla(tblMediciones);
        p.add(NutricionistaUI.scrollTabla(tblMediciones), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearIndicadores() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(NutricionistaUI.FONDO);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel form = NutricionistaUI.tarjeta();
        form.setLayout(new BorderLayout(0, 10));
        form.add(NutricionistaUI.tituloSeccion("Registrar indicador"), BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(1, 4, 10, 0));
        campos.setOpaque(false);
        agregarCampo(campos, "Indicador", cboIndicador);
        agregarCampo(campos, "Valor", txtValorIndicador);
        agregarCampo(campos, "Clasificación", txtClasificacion);
        JPanel rango = new JPanel(new BorderLayout());
        rango.setOpaque(false);
        rango.add(NutricionistaUI.etiqueta("Referencia"), BorderLayout.NORTH);
        lblRango.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        rango.add(lblRango, BorderLayout.CENTER);
        campos.add(rango);
        form.add(campos, BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout(10, 0));
        sur.setOpaque(false);
        sur.add(new JScrollPane(txtObsIndicador), BorderLayout.CENTER);
        JButton btn = NutricionistaUI.boton("Registrar resultado");
        btn.addActionListener(e -> registrarIndicador());
        sur.add(btn, BorderLayout.EAST);
        form.add(sur, BorderLayout.SOUTH);
        p.add(form, BorderLayout.NORTH);

        NutricionistaUI.tabla(tblResultados);
        p.add(NutricionistaUI.scrollTabla(tblResultados), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearRecomendaciones() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(NutricionistaUI.FONDO);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel form = NutricionistaUI.tarjeta();
        form.setLayout(new BorderLayout(0, 10));
        form.add(NutricionistaUI.tituloSeccion("Nueva recomendación"), BorderLayout.NORTH);
        JPanel campos = new JPanel(new GridLayout(1, 5, 10, 0));
        campos.setOpaque(false);
        agregarCampo(campos, "Tipo", cboTipoRec);
        agregarCampo(campos, "Prioridad", cboPrioridad);
        agregarCampo(campos, "Título", txtTituloRec);
        agregarCampo(campos, "Inicio (AAAA-MM-DD)", txtInicioRec);
        agregarCampo(campos, "Fin (AAAA-MM-DD)", txtFinRec);
        form.add(campos, BorderLayout.CENTER);
        JPanel sur = new JPanel(new BorderLayout(10, 0));
        sur.setOpaque(false);
        sur.add(new JScrollPane(txtDescripcionRec), BorderLayout.CENTER);
        JButton btn = NutricionistaUI.boton("Guardar recomendación");
        btn.addActionListener(e -> registrarRecomendacion());
        sur.add(btn, BorderLayout.EAST);
        form.add(sur, BorderLayout.SOUTH);
        p.add(form, BorderLayout.NORTH);

        NutricionistaUI.tabla(tblRecomendaciones);
        p.add(NutricionistaUI.scrollTabla(tblRecomendaciones), BorderLayout.CENTER);
        return p;
    }

    private JPanel crearEvolucion() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 12));
        p.setBackground(NutricionistaUI.FONDO);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));
        JPanel medidas = NutricionistaUI.tarjeta();
        medidas.setLayout(new BorderLayout(0, 8));
        medidas.add(NutricionistaUI.tituloSeccion("Evolución de medidas"), BorderLayout.NORTH);
        NutricionistaUI.tabla(tblEvolucionMedidas);
        medidas.add(NutricionistaUI.scrollTabla(tblEvolucionMedidas), BorderLayout.CENTER);
        JPanel indicadores = NutricionistaUI.tarjeta();
        indicadores.setLayout(new BorderLayout(0, 8));
        indicadores.add(NutricionistaUI.tituloSeccion("Evolución de indicadores"), BorderLayout.NORTH);
        NutricionistaUI.tabla(tblEvolucionIndicadores);
        indicadores.add(NutricionistaUI.scrollTabla(tblEvolucionIndicadores), BorderLayout.CENTER);
        p.add(medidas);
        p.add(indicadores);
        return p;
    }

    private void eventos() {
        cboIndicador.addActionListener(e -> actualizarRango());
        tabs.addChangeListener(e -> refrescarDatos());
    }

    public void refrescarDatos() {
        if (!ClienteEnAtencion.hay()) {
            lblCliente.setText("Ningún cliente seleccionado. Vaya a Mis clientes y seleccione uno para atención.");
        } else {
            lblCliente.setText("Cliente en atención: " + ClienteEnAtencion.descripcion());
        }
        cargarIndicadores();
        cargarMediciones();
        cargarResultados();
        cargarRecomendaciones();
    }

    private void cargarIndicadores() {
        IndicadorSalud seleccionado = (IndicadorSalud) cboIndicador.getSelectedItem();
        List<IndicadorSalud> lista = controlador.listarIndicadoresActivos();
        DefaultComboBoxModel<IndicadorSalud> m = new DefaultComboBoxModel<>();
        for (IndicadorSalud i : lista) m.addElement(i);
        cboIndicador.setModel(m);
        cboIndicador.setRenderer((ListCellRenderer<? super IndicadorSalud>) (list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel(value == null ? "" : value.getNombreIndicador());
            l.setOpaque(true);
            l.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            l.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            l.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 6, 4, 6));
            return l;
        });
        if (seleccionado != null) {
            for (int i = 0; i < m.getSize(); i++) {
                if (m.getElementAt(i).getIdIndicador().equals(seleccionado.getIdIndicador())) {
                    cboIndicador.setSelectedIndex(i); break;
                }
            }
        }
        actualizarRango();
    }

    private void actualizarRango() {
        IndicadorSalud i = (IndicadorSalud) cboIndicador.getSelectedItem();
        if (i == null) {
            lblRango.setText("Rango de referencia: —");
        } else {
            lblRango.setText(valor(i.getValorMinimoReferencia()) + " - "
                    + valor(i.getValorMaximoReferencia()) + " " + valor(i.getUnidadMedida()));
        }
    }

    private void registrarMedicion() {
        try {
            boolean ok = controlador.registrarMedicion(
                    decimalOpcional(txtPeso.getText(), "Peso"),
                    decimalOpcional(txtAltura.getText(), "Altura"),
                    decimalOpcional(txtGrasa.getText(), "Grasa corporal"),
                    decimalOpcional(txtCintura.getText(), "Cintura"),
                    texto(txtObsMedicion.getText()));
            mensaje(ok);
            if (ok) {
                txtPeso.setText(""); txtAltura.setText(""); txtGrasa.setText("");
                txtCintura.setText(""); txtObsMedicion.setText("");
                cargarMediciones();
            }
        } catch (IllegalArgumentException ex) {
            advertencia(ex.getMessage());
        }
    }

    private void registrarIndicador() {
        try {
            IndicadorSalud i = (IndicadorSalud) cboIndicador.getSelectedItem();
            if (i == null) throw new IllegalArgumentException("Seleccione un indicador.");
            Long id = controlador.registrarResultado(i.getIdIndicador(),
                    decimalRequerido(txtValorIndicador.getText(), "Valor"),
                    requerido(txtClasificacion.getText(), "Ingrese una clasificación."),
                    texto(txtObsIndicador.getText()));
            mensaje(id != null);
            if (id != null) {
                txtValorIndicador.setText(""); txtClasificacion.setText(""); txtObsIndicador.setText("");
                cargarResultados();
            }
        } catch (IllegalArgumentException ex) {
            advertencia(ex.getMessage());
        }
    }

    private void registrarRecomendacion() {
        try {
            boolean ok = controlador.registrarRecomendacion(
                    String.valueOf(cboTipoRec.getSelectedItem()),
                    requerido(txtTituloRec.getText(), "Ingrese un título."),
                    requerido(txtDescripcionRec.getText(), "Ingrese la descripción."),
                    String.valueOf(cboPrioridad.getSelectedItem()),
                    fechaOpcional(txtInicioRec.getText()),
                    fechaOpcional(txtFinRec.getText()));
            mensaje(ok);
            if (ok) {
                txtTituloRec.setText(""); txtDescripcionRec.setText("");
                txtInicioRec.setText(""); txtFinRec.setText("");
                cargarRecomendaciones();
            }
        } catch (IllegalArgumentException ex) {
            advertencia(ex.getMessage());
        }
    }

    private void cargarMediciones() {
        List<MedicionCorporal> lista = controlador.listarMedicionesClienteActual();
        DefaultTableModel m = modelo(new String[]{"Evaluación", "Peso", "Altura", "Grasa %", "Cintura", "Observaciones"});
        for (MedicionCorporal x : lista) m.addRow(new Object[]{
            x.getIdEvaluacion(), x.getPesoKg(), x.getAlturaM(), x.getPorcentajeGrasa(), x.getCinturaCm(), x.getObservaciones()
        });
        tblMediciones.setModel(m);
        tblEvolucionMedidas.setModel(m);
    }

    private void cargarResultados() {
        List<ResultadoIndicador> lista = controlador.listarResultadosClienteActual();
        DefaultTableModel m = modelo(new String[]{"Fecha", "Indicador", "Valor", "Clasificación", "Fuera de rango", "Observaciones"});
        for (ResultadoIndicador r : lista) m.addRow(new Object[]{
            r.getFechaRegistro(), controlador.nombreIndicador(r.getIdIndicador()),
            r.getValorObtenido(), r.getClasificacion(), r.isFueraDeRango() ? "SÍ" : "NO", r.getObservaciones()
        });
        tblResultados.setModel(m);
        tblEvolucionIndicadores.setModel(m);
    }

    private void cargarRecomendaciones() {
        List<Recomendacion> lista = controlador.listarRecomendacionesClienteActual();
        DefaultTableModel m = modelo(new String[]{"Fecha", "Tipo", "Título", "Prioridad", "Inicio", "Fin", "Estado"});
        for (Recomendacion r : lista) m.addRow(new Object[]{
            r.getFechaRecomendacion(), r.getTipoRecomendacion(), r.getTitulo(),
            r.getPrioridad(), r.getFechaInicio(), r.getFechaFin(), r.getEstadoRecomendacion()
        });
        tblRecomendaciones.setModel(m);
    }

    private void agregarCampo(JPanel panel, String etiqueta, java.awt.Component campo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(NutricionistaUI.etiqueta(etiqueta), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        panel.add(p);
    }

    private BigDecimal decimalRequerido(String s, String nombre) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(nombre + " es obligatorio.");
        try { return new BigDecimal(s.trim().replace(',', '.')); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException(nombre + " debe ser numérico."); }
    }

    private BigDecimal decimalOpcional(String s, String nombre) {
        if (s == null || s.isBlank()) return null;
        BigDecimal v = decimalRequerido(s, nombre);
        if (v.signum() < 0) throw new IllegalArgumentException(nombre + " no puede ser negativo.");
        return v;
    }

    private String requerido(String s, String mensaje) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(mensaje);
        return s.trim();
    }

    private String texto(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    private LocalDate fechaOpcional(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); }
        catch (java.time.format.DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha debe tener formato AAAA-MM-DD.");
        }
    }

    private String valor(Object o) { return o == null ? "—" : o.toString(); }

    private void mensaje(boolean ok) {
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
