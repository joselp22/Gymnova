package vista;

import conexion.ConexionPostgreSQL;
import controlador.ClienteControlador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import modelo.Cliente;
import utilidades.CalendarioSelector;
import utilidades.SesionUsuario;

/**
 * Vista del entrenador para hacer seguimiento del peso corporal de sus
 * clientes contra el peso meta. Permite elegir cliente, registrar una
 * nueva medición y ver la evolución del peso en una gráfica de línea con
 * las referencias de peso inicial y peso meta.
 *
 * Cada registro genera una fila en evaluacion_fisica (tipo
 * "SEGUIMIENTO_PESO") y otra en medicion_corporal con el peso capturado.
 */
public final class PnlProgresoPesoEntrenador extends JPanel {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color TEXTO = new Color(90, 110, 135);
    private static final Color AZUL = new Color(8, 124, 255);
    private static final Color VERDE = new Color(34, 197, 94);
    private static final Color VERDE_LINEA = new Color(16, 185, 129);
    private static final Color AMARILLO = new Color(234, 179, 8);
    private static final Color ROJO = new Color(239, 68, 68);
    private static final Color GRIS = new Color(160, 174, 192);

    private final ClienteControlador clienteControlador = new ClienteControlador();

    private final JComboBox<Cliente> cboCliente = new JComboBox<>();
    private final JLabel lblPesoInicial = valor("—");
    private final JLabel lblPesoMeta = valor("—");
    private final JLabel lblPesoActual = valor("—");
    private final JLabel lblAvance = valor("—");
    private final JLabel lblTendencia = new JLabel(" ", SwingConstants.LEFT);

    private final JTextField txtPeso = new JTextField();
    private final JTextField txtFecha = new JTextField(LocalDate.now().toString());
    private final JTextArea txtObservaciones = new JTextArea(2, 20);
    private final JButton btnRegistrar = botonPrimario("Registrar peso");
    private final JButton btnActualizar = botonSecundario("Actualizar");

    private final GraficoPeso grafico = new GraficoPeso();

    /** Punto de la gráfica: fecha + peso. */
    private static final class Punto {
        final LocalDate fecha;
        final double peso;
        Punto(LocalDate f, double p) { fecha = f; peso = p; }
    }

    private final List<Punto> historia = new ArrayList<>();
    private Double pesoInicial;
    private Double pesoMeta;

    public PnlProgresoPesoEntrenador() {
        setLayout(new BorderLayout(0, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        CalendarioSelector.vincularFecha(txtFecha);

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCuerpo(), BorderLayout.CENTER);

        cboCliente.addActionListener(e -> refrescarCliente());
        btnRegistrar.addActionListener(e -> registrarPeso());
        btnActualizar.addActionListener(e -> {
            cargarClientes();
            refrescarCliente();
        });

        cargarClientes();
        refrescarCliente();
    }

    // ---- Layout ----------------------------------------------------------

    private JPanel construirEncabezado() {
        JPanel enc = new JPanel(new BorderLayout(10, 8));
        enc.setBackground(TARJETA);
        enc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel titulo = new JLabel("Progreso de peso del cliente");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(TITULO);

        JLabel sub = new JLabel(
                "Selecciona un cliente, registra un nuevo peso y observa su evolución hacia el peso meta.");
        sub.setForeground(TEXTO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(sub);
        enc.add(textos, BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(new JLabel("Cliente:"));
        cboCliente.setPreferredSize(new Dimension(280, 26));
        derecha.add(cboCliente);
        derecha.add(btnActualizar);
        enc.add(derecha, BorderLayout.EAST);
        return enc;
    }

    private JPanel construirCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);
        cuerpo.add(construirKPIs(), BorderLayout.NORTH);
        cuerpo.add(construirCentro(), BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel construirKPIs() {
        JPanel fila = new JPanel(new java.awt.GridLayout(1, 4, 12, 0));
        fila.setOpaque(false);
        fila.add(tarjetaKPI("PESO INICIAL", lblPesoInicial));
        fila.add(tarjetaKPI("PESO META", lblPesoMeta));
        fila.add(tarjetaKPI("PESO ACTUAL", lblPesoActual));
        fila.add(tarjetaKPI("% AVANCE HACIA META", lblAvance));
        return fila;
    }

    private JPanel construirCentro() {
        JPanel centro = new JPanel(new BorderLayout(12, 0));
        centro.setOpaque(false);
        centro.add(construirGrafica(), BorderLayout.CENTER);
        centro.add(construirFormulario(), BorderLayout.EAST);
        return centro;
    }

    private JPanel construirGrafica() {
        JPanel c = new JPanel(new BorderLayout(0, 8));
        c.setBackground(TARJETA);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JPanel superior = new JPanel(new BorderLayout());
        superior.setOpaque(false);
        JLabel tit = new JLabel("Evolución de peso");
        tit.setFont(new Font("SansSerif", Font.BOLD, 15));
        tit.setForeground(TITULO);
        superior.add(tit, BorderLayout.WEST);
        lblTendencia.setFont(new Font("SansSerif", Font.BOLD, 13));
        superior.add(lblTendencia, BorderLayout.EAST);
        c.add(superior, BorderLayout.NORTH);

        grafico.setBackground(Color.WHITE);
        c.add(grafico, BorderLayout.CENTER);
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leyenda.setOpaque(false);
        leyenda.add(punto("Bajó (mejor)", VERDE));
        leyenda.add(punto("Igual", AMARILLO));
        leyenda.add(punto("Subió", ROJO));
        leyenda.add(punto("Primer registro", AZUL));
        leyenda.add(punto("Meta", VERDE_LINEA));
        leyenda.add(punto("Inicial", GRIS));
        c.add(leyenda, BorderLayout.SOUTH);
        return c;
    }

    private JPanel construirFormulario() {
        JPanel c = new JPanel(new BorderLayout(0, 10));
        c.setPreferredSize(new Dimension(240, 0));
        c.setBackground(TARJETA);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JLabel tit = new JLabel("Registrar nuevo peso");
        tit.setFont(new Font("SansSerif", Font.BOLD, 14));
        tit.setForeground(TITULO);

        // Compactamos los campos: 28px de alto, tipografía pequeña,
        // padding interno reducido para un look limpio.
        Dimension altoCampo = new Dimension(0, 28);
        Font fuente = new Font("SansSerif", Font.PLAIN, 12);

        txtPeso.setPreferredSize(altoCampo);
        txtPeso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        txtPeso.setFont(fuente);
        txtPeso.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        txtFecha.setPreferredSize(altoCampo);
        txtFecha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        txtFecha.setFont(fuente);
        txtFecha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        txtObservaciones.setFont(fuente);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        JScrollPane sc = new JScrollPane(txtObservaciones);
        sc.setPreferredSize(new Dimension(0, 55));
        sc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        sc.setBorder(BorderFactory.createLineBorder(BORDE, 1));

        btnRegistrar.setPreferredSize(new Dimension(0, 32));
        btnRegistrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btnRegistrar.setFont(new Font("SansSerif", Font.BOLD, 12));

        JPanel campos = new JPanel();
        campos.setOpaque(false);
        campos.setLayout(new BoxLayout(campos, BoxLayout.Y_AXIS));
        campos.add(etiqueta("Peso (kg) *"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(txtPeso);
        campos.add(Box.createVerticalStrut(10));
        campos.add(etiqueta("Fecha *"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(txtFecha);
        campos.add(Box.createVerticalStrut(10));
        campos.add(etiqueta("Observaciones"));
        campos.add(Box.createVerticalStrut(3));
        campos.add(sc);
        campos.add(Box.createVerticalStrut(14));
        campos.add(btnRegistrar);
        campos.add(Box.createVerticalGlue());

        c.add(tit, BorderLayout.NORTH);
        c.add(campos, BorderLayout.CENTER);
        return c;
    }

    // ---- Datos ----------------------------------------------------------

    private void cargarClientes() {
        DefaultComboBoxModel<Cliente> m = new DefaultComboBoxModel<>();
        Long idEntrenador = idEntrenadorSesion();
        if (idEntrenador == null) {
            cboCliente.setModel(m);
            return;
        }
        for (Cliente c : clienteControlador.listarPorEntrenador(idEntrenador, "")) {
            m.addElement(c);
        }
        cboCliente.setModel(m);
    }

    private Long idEntrenadorSesion() {
        if (!SesionUsuario.haySesionActiva()) return null;
        return SesionUsuario.getUsuarioActual().getIdPersona();
    }

    private void refrescarCliente() {
        Cliente c = clienteSeleccionado();
        historia.clear();
        pesoInicial = null;
        pesoMeta = null;
        if (c == null) {
            lblPesoInicial.setText("—");
            lblPesoMeta.setText("—");
            lblPesoActual.setText("—");
            lblAvance.setText("—");
            lblTendencia.setText(" ");
            grafico.repaint();
            btnRegistrar.setEnabled(false);
            return;
        }
        btnRegistrar.setEnabled(true);
        if (c.getPesoInicial() != null) pesoInicial = c.getPesoInicial().doubleValue();
        if (c.getPesoMeta() != null)    pesoMeta    = c.getPesoMeta().doubleValue();
        cargarHistoria(c.getIdPersona());
        actualizarKPIs();
        grafico.repaint();
    }

    private Cliente clienteSeleccionado() {
        Object o = cboCliente.getSelectedItem();
        return o instanceof Cliente c ? c : null;
    }

    private void cargarHistoria(Long idCliente) {
        historia.clear();
        String sql = "SELECT ef.fecha_evaluacion, mc.peso_kg "
                + "  FROM evaluacion_fisica ef "
                + "  JOIN medicion_corporal mc ON mc.id_evaluacion = ef.id_evaluacion "
                + " WHERE ef.id_cliente = ? "
                + "   AND mc.peso_kg IS NOT NULL "
                + " ORDER BY ef.fecha_evaluacion, ef.id_evaluacion";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idCliente);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    Date f = r.getDate(1);
                    BigDecimal p = r.getBigDecimal(2);
                    if (f != null && p != null) {
                        historia.add(new Punto(f.toLocalDate(), p.doubleValue()));
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar la historia de peso: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarKPIs() {
        lblPesoInicial.setText(pesoInicial == null ? "—"
                : String.format("%.2f kg", pesoInicial));
        lblPesoMeta.setText(pesoMeta == null ? "—"
                : String.format("%.2f kg", pesoMeta));

        Double actual = historia.isEmpty() ? null
                : historia.get(historia.size() - 1).peso;
        lblPesoActual.setText(actual == null ? "—"
                : String.format("%.2f kg", actual));

        if (pesoInicial != null && pesoMeta != null && actual != null
                && Math.abs(pesoInicial - pesoMeta) > 0.001) {
            double avanzado = pesoInicial - actual;
            double objetivo = pesoInicial - pesoMeta;
            double pct = Math.max(0.0, Math.min(100.0,
                    (avanzado / objetivo) * 100.0));
            lblAvance.setText(String.format("%.0f %%", pct));
        } else {
            lblAvance.setText("—");
        }

        // Tendencia: comparamos las dos últimas mediciones respecto a la meta.
        if (historia.size() >= 2 && pesoMeta != null) {
            double actualDist = Math.abs(actual - pesoMeta);
            double anteriorDist = Math.abs(
                    historia.get(historia.size() - 2).peso - pesoMeta);
            double diff = anteriorDist - actualDist;   // >0 se acercó a la meta
            if (Math.abs(diff) < 0.1) {
                lblTendencia.setText("Estancado");
                lblTendencia.setForeground(GRIS);
            } else if (diff > 0) {
                lblTendencia.setText("↗ Progresando");
                lblTendencia.setForeground(VERDE);
            } else {
                lblTendencia.setText("↘ Retrocediendo");
                lblTendencia.setForeground(ROJO);
            }
        } else if (historia.size() == 1) {
            lblTendencia.setText("Primer registro");
            lblTendencia.setForeground(AZUL);
        } else {
            lblTendencia.setText(" ");
        }
    }

    private void registrarPeso() {
        Cliente c = clienteSeleccionado();
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un cliente.",
                    "GYMNOVA", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double peso;
        try {
            peso = Double.parseDouble(txtPeso.getText().trim().replace(',', '.'));
            if (peso <= 0 || peso > 500) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Ingresa un peso válido (entre 0 y 500 kg).",
                    "GYMNOVA", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(txtFecha.getText().trim());
            if (fecha.isAfter(LocalDate.now())) {
                JOptionPane.showMessageDialog(this,
                        "La fecha no puede ser futura.",
                        "GYMNOVA", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Formato de fecha inválido. Usa AAAA-MM-DD.",
                    "GYMNOVA", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Long idEntrenador = idEntrenadorSesion();
        if (idEntrenador == null) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo identificar el entrenador de la sesión.",
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection cn = ConexionPostgreSQL.getConexion()) {
            cn.setAutoCommit(false);
            try {
                String codigo = siguienteCodigoEV(cn);
                Long idEvaluacion;
                try (PreparedStatement s = cn.prepareStatement(
                        "INSERT INTO evaluacion_fisica ("
                        + "codigo_evaluacion, fecha_evaluacion, tipo_evaluacion, "
                        + "motivo, estado_evaluacion, id_cliente, id_entrenador) "
                        + "VALUES (?, ?, 'SEGUIMIENTO_PESO', "
                        + "'Registro de peso desde Evaluaciones', 'REGISTRADA', ?, ?) "
                        + "RETURNING id_evaluacion")) {
                    s.setString(1, codigo);
                    s.setDate(2, Date.valueOf(fecha));
                    s.setLong(3, c.getIdPersona());
                    s.setLong(4, idEntrenador);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        idEvaluacion = r.getLong(1);
                    }
                }
                try (PreparedStatement s = cn.prepareStatement(
                        "INSERT INTO medicion_corporal ("
                        + "peso_kg, observaciones, id_evaluacion) "
                        + "VALUES (?, ?, ?)")) {
                    s.setBigDecimal(1, BigDecimal.valueOf(peso));
                    String obs = txtObservaciones.getText().trim();
                    s.setString(2, obs.isEmpty() ? null : obs);
                    s.setLong(3, idEvaluacion);
                    s.executeUpdate();
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo registrar el peso: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
            return;
        }

        txtPeso.setText("");
        txtObservaciones.setText("");
        refrescarCliente();
        JOptionPane.showMessageDialog(this,
                "Peso registrado correctamente.",
                "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
    }

    private String siguienteCodigoEV(Connection c) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT COALESCE(MAX(CAST(SUBSTRING(codigo_evaluacion FROM 3) "
                + "AS INTEGER)), 0) + 1 FROM evaluacion_fisica")) {
            try (ResultSet r = s.executeQuery()) {
                r.next();
                return String.format("EV%05d", r.getInt(1));
            }
        }
    }

    // Este método existe para poder invocar refresh desde FrmPrincipal.
    public void refrescarDatos() {
        cargarClientes();
        refrescarCliente();
    }

    // ---- Helpers UI ------------------------------------------------------

    private JPanel tarjetaKPI(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(TARJETA);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(TEXTO);
        valor.setFont(new Font("SansSerif", Font.BOLD, 22));
        valor.setForeground(AZUL);
        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    private JLabel valor(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(AZUL);
        return l;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(TEXTO);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JPanel punto(String texto, Color c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(c);
        dot.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel l = new JLabel(texto);
        l.setForeground(TEXTO);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        p.add(dot);
        p.add(l);
        return p;
    }

    private JButton botonPrimario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(AZUL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return b;
    }

    private JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        return b;
    }

    // ---- Gráfica personalizada ------------------------------------------

    private final class GraficoPeso extends JPanel {

        private static final int MARGEN_IZQ = 55;
        private static final int MARGEN_DER = 20;
        private static final int MARGEN_ARR = 15;
        private static final int MARGEN_ABJ = 40;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(600, 300);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            int cw = w - MARGEN_IZQ - MARGEN_DER;
            int ch = h - MARGEN_ARR - MARGEN_ABJ;

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);

            if (historia.isEmpty() && pesoInicial == null && pesoMeta == null) {
                g2.setColor(TEXTO);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
                String msg = "Sin datos aún. Registra un peso para comenzar.";
                g2.drawString(msg, w / 2
                        - g2.getFontMetrics().stringWidth(msg) / 2, h / 2);
                g2.dispose();
                return;
            }

            double minY = Double.POSITIVE_INFINITY;
            double maxY = Double.NEGATIVE_INFINITY;
            for (Punto p : historia) {
                minY = Math.min(minY, p.peso);
                maxY = Math.max(maxY, p.peso);
            }
            if (pesoInicial != null) { minY = Math.min(minY, pesoInicial); maxY = Math.max(maxY, pesoInicial); }
            if (pesoMeta != null)    { minY = Math.min(minY, pesoMeta);    maxY = Math.max(maxY, pesoMeta); }
            if (!Double.isFinite(minY)) { minY = 0; maxY = 100; }
            double pad = Math.max(1.0, (maxY - minY) * 0.15);
            minY -= pad; maxY += pad;

            // Rejilla + eje Y
            g2.setColor(new Color(226, 232, 240));
            for (int i = 0; i <= 4; i++) {
                int y = MARGEN_ARR + (ch * i) / 4;
                g2.drawLine(MARGEN_IZQ, y, w - MARGEN_DER, y);
            }
            g2.setColor(TEXTO);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i <= 4; i++) {
                int y = MARGEN_ARR + (ch * i) / 4;
                double valor = maxY - ((maxY - minY) * i) / 4;
                g2.drawString(String.format("%.1f", valor), 5, y + 4);
            }

            // Líneas horizontales de referencia
            if (pesoInicial != null) {
                int y = yPara(pesoInicial, minY, maxY, ch);
                dibujarLineaPunteada(g2, MARGEN_IZQ, y, w - MARGEN_DER, y, GRIS);
                g2.setColor(GRIS);
                g2.drawString("Inicial " + String.format("%.1f", pesoInicial),
                        w - MARGEN_DER - 100, y - 4);
            }
            if (pesoMeta != null) {
                int y = yPara(pesoMeta, minY, maxY, ch);
                dibujarLineaPunteada(g2, MARGEN_IZQ, y, w - MARGEN_DER, y,
                        VERDE_LINEA);
                g2.setColor(VERDE_LINEA);
                g2.drawString("Meta " + String.format("%.1f", pesoMeta),
                        w - MARGEN_DER - 80, y - 4);
            }

            // Historial en barras con semáforo:
            //   verde  = peso menor que el anterior (bajó = mejor si busca bajar)
            //   amarillo = peso igual (variación < 0.1 kg)
            //   rojo    = peso mayor que el anterior (subió)
            //   azul    = primer registro (sin referencia)
            if (historia.size() >= 1) {
                int n = historia.size();
                // Ancho de barra dinámico: no menos de 20, no más de 55.
                int slot = cw / n;
                int anchoBarra = Math.max(20, Math.min(55, slot - 8));
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");
                int y0 = yPara(minY, minY, maxY, ch);

                for (int i = 0; i < n; i++) {
                    int cx = MARGEN_IZQ + slot * i + slot / 2;
                    int xBarra = cx - anchoBarra / 2;
                    int yBarra = yPara(historia.get(i).peso, minY, maxY, ch);
                    int alto = Math.max(2, y0 - yBarra);

                    Color color;
                    if (i == 0) {
                        color = AZUL;
                    } else {
                        double diff = historia.get(i).peso
                                - historia.get(i - 1).peso;
                        if (Math.abs(diff) < 0.1) {
                            color = AMARILLO;
                        } else if (diff < 0) {
                            color = VERDE;
                        } else {
                            color = ROJO;
                        }
                    }
                    // Sombra ligera + barra + borde.
                    g2.setColor(new Color(0, 0, 0, 15));
                    g2.fillRoundRect(xBarra + 2, yBarra + 2,
                            anchoBarra, alto, 6, 6);
                    g2.setColor(color);
                    g2.fillRoundRect(xBarra, yBarra,
                            anchoBarra, alto, 6, 6);
                    g2.setColor(color.darker());
                    g2.drawRoundRect(xBarra, yBarra,
                            anchoBarra, alto, 6, 6);

                    // Valor arriba de cada barra.
                    g2.setColor(TITULO);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    String v = String.format("%.1f", historia.get(i).peso);
                    g2.drawString(v,
                            cx - g2.getFontMetrics().stringWidth(v) / 2,
                            yBarra - 4);

                    // Etiqueta de fecha bajo la barra (rotarla si hay muchas).
                    g2.setColor(TEXTO);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    String etiqueta = historia.get(i).fecha.format(fmt);
                    if (n <= 6) {
                        g2.drawString(etiqueta,
                                cx - g2.getFontMetrics().stringWidth(etiqueta) / 2,
                                h - 12);
                    } else {
                        // Rotar 45° para no encimarse.
                        java.awt.geom.AffineTransform prev = g2.getTransform();
                        g2.rotate(-Math.PI / 4, cx, h - 22);
                        g2.drawString(etiqueta, cx - 25, h - 18);
                        g2.setTransform(prev);
                    }
                }
            }
            g2.dispose();
        }

        private int[] indices0aN(int n) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = i;
            return a;
        }

        private int yPara(double valor, double minY, double maxY, int ch) {
            double t = (valor - minY) / (maxY - minY);
            return (int) Math.round(MARGEN_ARR + ch * (1 - t));
        }

        private void dibujarLineaPunteada(Graphics2D g2, int x1, int y1,
                int x2, int y2, Color color) {
            java.awt.Stroke prev = g2.getStroke();
            g2.setStroke(new java.awt.BasicStroke(1.5f,
                    java.awt.BasicStroke.CAP_BUTT,
                    java.awt.BasicStroke.JOIN_MITER, 10f,
                    new float[]{6f, 6f}, 0f));
            g2.setColor(color);
            g2.draw(new Line2D.Float(x1, y1, x2, y2));
            g2.setStroke(prev);
        }
    }
}
