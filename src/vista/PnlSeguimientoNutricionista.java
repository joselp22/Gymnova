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

    // Evolución de peso (mismo formato que la vista del entrenador):
    // KPIs + tabla con la historia real del cliente en atención.
    private final JLabel lblEvoPesoInicial = new JLabel("—");
    private final JLabel lblEvoPesoMeta = new JLabel("—");
    private final JLabel lblEvoPesoActual = new JLabel("—");
    private final JLabel lblEvoAvance = new JLabel("—");
    private final JLabel lblEvoTendencia = new JLabel(" ");
    private final JTable tblEvolucionPeso = new JTable();

    public PnlSeguimientoNutricionista() {
        construir();
        eventos();
        // Calendario para las fechas de inicio/fin de la recomendación.
        utilidades.CalendarioSelector.vincularFecha(txtInicioRec);
        utilidades.CalendarioSelector.vincularFecha(txtFinRec);
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

    /**
     * Pestaña "Evolución": muestra la evolución de peso del cliente en
     * atención con el mismo formato que la vista del entrenador
     * (PnlProgresoPesoEntrenador) - KPIs con peso inicial/meta/actual y
     * % de avance más una tabla con la historia real de mediciones.
     * Solo lectura: la carga de nuevas mediciones sigue haciéndose desde
     * la pestaña "Mediciones".
     */
    private JPanel crearEvolucion() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(NutricionistaUI.FONDO);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Cabecera
        JPanel aviso = NutricionistaUI.tarjeta();
        aviso.setLayout(new BorderLayout());
        aviso.add(NutricionistaUI.tituloSeccion(
                "Evolución de peso del cliente"), BorderLayout.WEST);
        JLabel ayuda = new JLabel(
                "Registra nuevas mediciones en la pestaña \"Mediciones\".");
        ayuda.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        aviso.add(ayuda, BorderLayout.EAST);

        // KPIs
        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setOpaque(false);
        kpis.add(tarjetaKPIEvolucion("PESO INICIAL", lblEvoPesoInicial));
        kpis.add(tarjetaKPIEvolucion("PESO META", lblEvoPesoMeta));
        kpis.add(tarjetaKPIEvolucion("PESO ACTUAL", lblEvoPesoActual));
        kpis.add(tarjetaKPIEvolucion("% AVANCE HACIA META", lblEvoAvance));

        // Tabla con la historia de mediciones
        NutricionistaUI.tabla(tblEvolucionPeso);
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
        tarjetaTabla.add(NutricionistaUI.scrollTabla(tblEvolucionPeso),
                BorderLayout.CENTER);

        JPanel norte = new JPanel(new BorderLayout(0, 12));
        norte.setOpaque(false);
        norte.add(aviso, BorderLayout.NORTH);
        norte.add(kpis, BorderLayout.CENTER);
        p.add(norte, BorderLayout.NORTH);
        p.add(tarjetaTabla, BorderLayout.CENTER);
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

    private void cargarEvolucionPeso() {
        DefaultTableModel m = modelo(new String[]{
            "Fecha", "Peso (kg)", "Cambio vs. anterior",
            "Distancia a la meta", "Observaciones"});
        lblEvoPesoInicial.setText("—");
        lblEvoPesoMeta.setText("—");
        lblEvoPesoActual.setText("—");
        lblEvoAvance.setText("—");
        lblEvoTendencia.setText(" ");
        tblEvolucionPeso.setModel(m);

        modelo.Cliente cliente = ClienteEnAtencion.actual();
        if (cliente == null || cliente.getIdPersona() == null) {
            return;
        }
        Double pesoInicial = cliente.getPesoInicial() == null
                ? null : cliente.getPesoInicial().doubleValue();
        Double pesoMeta = cliente.getPesoMeta() == null
                ? null : cliente.getPesoMeta().doubleValue();

        java.util.List<Object[]> filas = new java.util.ArrayList<>();
        String sql = "SELECT ef.fecha_evaluacion, mc.peso_kg, "
                + "       mc.observaciones "
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
                        String.format(java.util.Locale.US, "%.2f", peso),
                        cambio, distMeta, obs == null ? "" : obs});
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

        // Más reciente arriba
        for (int idx = filas.size() - 1; idx >= 0; idx--) {
            m.addRow(filas.get(idx));
        }

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
                lblEvoTendencia.setForeground(
                        new java.awt.Color(160, 174, 192));
            } else if (diff > 0) {
                lblEvoTendencia.setText("↗ Progresando");
                lblEvoTendencia.setForeground(NutricionistaUI.VERDE);
            } else {
                lblEvoTendencia.setText("↘ Retrocediendo");
                lblEvoTendencia.setForeground(NutricionistaUI.ROJO);
            }
        }
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
        cargarEvolucionPeso();
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
