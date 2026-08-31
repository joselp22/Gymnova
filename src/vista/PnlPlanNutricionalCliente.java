package vista;

import conexion.ConexionPostgreSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import utilidades.CalendarioSelector;
import utilidades.SesionUsuario;

/**
 * Vista del cliente para consultar su plan alimenticio del día.
 *
 * El plan nutricional creado por el nutricionista es una plantilla
 * semanal (LUNES, MARTES, ...). La fecha elegida se convierte al día de
 * semana correspondiente y se muestran, como tarjetas, todos los
 * alimentos que el cliente debe consumir ese día, agrupados por tipo de
 * comida (DESAYUNO, ALMUERZO, ...) y ordenados por hora / orden.
 */
public final class PnlPlanNutricionalCliente extends JPanel {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color TEXTO_SECUNDARIO = new Color(90, 110, 135);
    private static final Color AZUL = new Color(8, 124, 255);
    private static final Color VERDE_SUAVE = new Color(236, 253, 245);
    private static final Color VERDE = new Color(34, 139, 94);
    private static final Color GRIS_SUAVE = new Color(248, 250, 252);
    private static final Color GRIS = new Color(100, 116, 139);
    private static final Color CINTA = new Color(10, 58, 108);

    private final JLabel lblTitulo = new JLabel("Mi plan alimenticio");
    private final JLabel lblSubtitulo = new JLabel(" ");
    private final JLabel lblNutricionista = new JLabel(" ");
    private final JLabel lblDia = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblResumen = new JLabel(" ");
    private final JTextField txtFechaConsulta = new JTextField(10);
    private final JPanel pnlTarjetas = new JPanel(new GridBagLayout());
    private final JScrollPane scrollTarjetas = new JScrollPane(pnlTarjetas);

    private boolean refrescando;

    public PnlPlanNutricionalCliente() {
        txtFechaConsulta.setText(LocalDate.now().toString());
        CalendarioSelector.vincularFecha(txtFechaConsulta);
        txtFechaConsulta.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refrescarDesdeCampo();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refrescarDesdeCampo();
            }
        });

        setLayout(new BorderLayout(0, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCuerpo(), BorderLayout.CENTER);
        refrescarDatos();
    }

    private JPanel construirEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout(12, 0));
        encabezado.setBackground(TARJETA);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(TITULO);
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitulo.setForeground(TEXTO_SECUNDARIO);
        lblNutricionista.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblNutricionista.setForeground(TEXTO_SECUNDARIO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(lblSubtitulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(lblNutricionista);
        encabezado.add(textos, BorderLayout.WEST);

        JButton btnAnterior = new JButton("‹");
        JButton btnHoy = new JButton("Hoy");
        JButton btnSiguiente = new JButton("›");
        JButton btnActualizar = botonPrincipal("Actualizar");
        btnAnterior.addActionListener(e -> moverFecha(-1));
        btnSiguiente.addActionListener(e -> moverFecha(1));
        btnHoy.addActionListener(e -> establecerFecha(LocalDate.now()));
        btnActualizar.addActionListener(e -> refrescarDatos());

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        selector.setOpaque(false);
        selector.add(new JLabel("Fecha:"));
        selector.add(btnAnterior);
        selector.add(txtFechaConsulta);
        selector.add(btnSiguiente);
        selector.add(btnHoy);
        selector.add(btnActualizar);
        encabezado.add(selector, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel construirCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.setOpaque(false);
        superior.add(construirDiaSeleccionado(), BorderLayout.NORTH);
        superior.add(construirResumen(), BorderLayout.SOUTH);
        cuerpo.add(superior, BorderLayout.NORTH);

        pnlTarjetas.setBackground(FONDO);
        pnlTarjetas.setBorder(BorderFactory.createEmptyBorder(2, 2, 16, 2));
        scrollTarjetas.setBorder(null);
        scrollTarjetas.getViewport().setBackground(FONDO);
        scrollTarjetas.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTarjetas.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTarjetas.getVerticalScrollBar().setUnitIncrement(18);
        cuerpo.add(scrollTarjetas, BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel construirDiaSeleccionado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CINTA);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        lblDia.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblDia.setForeground(Color.WHITE);
        panel.add(lblDia, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirResumen() {
        JPanel resumen = new JPanel(new BorderLayout(12, 6));
        resumen.setBackground(TARJETA);
        resumen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        lblResumen.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblResumen.setForeground(TITULO);
        resumen.add(lblResumen, BorderLayout.CENTER);
        return resumen;
    }

    public void refrescarDatos() {
        if (refrescando || !SesionUsuario.haySesionActiva()) {
            return;
        }
        Long idCliente = SesionUsuario.getUsuarioActual().getIdPersona();
        if (idCliente == null) {
            lblSubtitulo.setText("La cuenta no tiene un perfil de cliente.");
            mostrarMensajeCentral(
                    "No se pudo identificar el perfil del cliente.");
            return;
        }

        refrescando = true;
        try {
            LocalDate fecha = fechaSeleccionada();
            String dia = nombreDia(fecha).toUpperCase(new Locale("es", "ES"));
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                    "dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            String fechaBonita = capitalizar(fecha.format(fmt));
            lblDia.setText(dia + "  ·  " + fechaBonita);
            lblSubtitulo.setText(
                    "Alimentos que debes consumir según tu nutricionista.");

            PlanCliente plan = cargarPlanActivo(idCliente);
            limpiarTarjetas();

            if (plan == null) {
                lblTitulo.setText("Mi plan alimenticio");
                lblNutricionista.setText(" ");
                lblResumen.setText("Aún no tienes un plan nutricional "
                        + "activo asignado.");
                mostrarMensajeCentral("Tu nutricionista todavía no te ha "
                        + "creado un plan nutricional activo.");
                return;
            }

            lblTitulo.setText("Plan: " + valor(plan.nombrePlan));
            lblNutricionista.setText(
                    (plan.nutricionista == null
                            || plan.nutricionista.isBlank()
                            ? "Nutricionista no especificado"
                            : "Nutricionista: " + plan.nutricionista)
                    + "  ·  Vigencia: "
                    + valor(plan.fechaInicio) + " → "
                    + (plan.fechaFin == null ? "sin fin" : plan.fechaFin));

            List<Comida> comidas = cargarComidasDelDia(
                    plan.idPlanNutricional, diaCorto(fecha));
            if (comidas.isEmpty()) {
                lblResumen.setText("No hay alimentos programados para "
                        + capitalizar(dia.toLowerCase(new Locale("es", "ES")))
                        + ".");
                mostrarMensajeCentral(
                        "Tu plan no tiene alimentos programados para "
                        + dia + ". Consulta con tu nutricionista.");
                return;
            }
            agregarTarjetas(comidas);
            String objetivos = construirObjetivos(plan);
            lblResumen.setText(comidas.size()
                    + (comidas.size() == 1 ? " alimento programado"
                            : " alimentos programados")
                    + " para hoy" + (objetivos.isBlank() ? "" : "  ·  "
                            + objetivos));
            pnlTarjetas.revalidate();
            pnlTarjetas.repaint();
            javax.swing.SwingUtilities.invokeLater(() ->
                    scrollTarjetas.getVerticalScrollBar().setValue(0));
        } finally {
            refrescando = false;
        }
    }

    private String construirObjetivos(PlanCliente p) {
        List<String> partes = new ArrayList<>();
        if (p.calorias != null) partes.add(p.calorias + " kcal");
        if (p.proteinas != null) partes.add(p.proteinas + " g proteína");
        if (p.carbos != null) partes.add(p.carbos + " g carbos");
        return String.join(" · ", partes);
    }

    private void agregarTarjetas(List<Comida> comidas) {
        // Agrupadas por tipo de comida, en orden natural del día.
        List<String> orden = List.of("DESAYUNO", "MEDIA_MANANA", "ALMUERZO",
                "MERIENDA", "CENA", "SNACK");
        comidas.sort(Comparator.<Comida>comparingInt(
                c -> {
                    int i = orden.indexOf(
                            c.tipoComida == null ? "" : c.tipoComida);
                    return i < 0 ? 99 : i;
                })
                .thenComparing(c -> c.hora == null
                        ? LocalTime.MAX : c.hora)
                .thenComparingInt(c -> c.orden == null ? 0 : c.orden));

        final int columnas = 3;
        for (int i = 0; i < comidas.size(); i++) {
            Comida c = comidas.get(i);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i % columnas;
            gbc.gridy = i / columnas;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(6, 6, 6, 6);
            pnlTarjetas.add(construirTarjeta(c), gbc);
        }
        GridBagConstraints rellenoHorizontal = new GridBagConstraints();
        rellenoHorizontal.gridx = columnas;
        rellenoHorizontal.gridy = 0;
        rellenoHorizontal.weightx = 1.0;
        rellenoHorizontal.fill = GridBagConstraints.HORIZONTAL;
        pnlTarjetas.add(Box.createHorizontalGlue(), rellenoHorizontal);

        GridBagConstraints rellenoVertical = new GridBagConstraints();
        rellenoVertical.gridx = 0;
        rellenoVertical.gridy = (comidas.size() + columnas - 1) / columnas;
        rellenoVertical.gridwidth = columnas + 1;
        rellenoVertical.weighty = 1.0;
        rellenoVertical.fill = GridBagConstraints.VERTICAL;
        pnlTarjetas.add(Box.createVerticalGlue(), rellenoVertical);
    }

    private JPanel construirTarjeta(Comida c) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 10));
        tarjeta.setBackground(TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1),
                BorderFactory.createEmptyBorder(16, 16, 14, 16)));
        tarjeta.setPreferredSize(new Dimension(285, 240));
        tarjeta.setMinimumSize(new Dimension(245, 220));

        String comida = c.tipoComida == null ? "COMIDA"
                : c.tipoComida.replace('_', ' ');
        JLabel tipo = new JLabel(comida);
        tipo.setFont(new Font("SansSerif", Font.BOLD, 11));
        tipo.setForeground(AZUL);

        JLabel nombre = new JLabel(htmlCentrado(valor(c.alimento)));
        nombre.setHorizontalAlignment(SwingConstants.CENTER);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 18));
        nombre.setForeground(TITULO);

        JPanel arriba = new JPanel();
        arriba.setOpaque(false);
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        tipo.setAlignmentX(Component.CENTER_ALIGNMENT);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        arriba.add(tipo);
        arriba.add(Box.createVerticalStrut(7));
        arriba.add(nombre);
        tarjeta.add(arriba, BorderLayout.NORTH);

        JPanel datos = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        datos.setOpaque(false);
        datos.add(datoDestacado("CANTIDAD",
                (c.cantidad == null ? "-" : c.cantidad.toPlainString())
                + (c.unidad == null || c.unidad.isBlank()
                        ? "" : " " + c.unidad)));
        datos.add(datoDestacado("HORA",
                c.hora == null ? "-" : c.hora.toString()));
        tarjeta.add(datos, BorderLayout.CENTER);

        String extra = construirDetalleOpcional(c);
        JLabel lblExtra = new JLabel(htmlCentrado(extra),
                SwingConstants.CENTER);
        lblExtra.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblExtra.setForeground(TEXTO_SECUNDARIO);
        tarjeta.add(lblExtra, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel datoDestacado(String titulo, String valor) {
        JPanel panel = new JPanel();
        panel.setBackground(GRIS_SUAVE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lTitulo = new JLabel(titulo);
        lTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lTitulo.setFont(new Font("SansSerif", Font.BOLD, 10));
        lTitulo.setForeground(GRIS);

        JLabel lValor = new JLabel(
                valor == null || valor.isBlank() ? "-" : valor);
        lValor.setAlignmentX(Component.CENTER_ALIGNMENT);
        lValor.setFont(new Font("SansSerif", Font.BOLD, 16));
        lValor.setForeground(TITULO);

        panel.add(lTitulo);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lValor);
        return panel;
    }

    private String construirDetalleOpcional(Comida c) {
        List<String> partes = new ArrayList<>();
        if (c.categoria != null && !c.categoria.isBlank()) {
            partes.add("Categoría: " + c.categoria);
        }
        if (c.orden != null) partes.add("Orden: " + c.orden);
        if (c.indicaciones != null && !c.indicaciones.isBlank()) {
            partes.add(c.indicaciones);
        }
        return partes.isEmpty() ? " " : String.join("  ·  ", partes);
    }

    private void mostrarMensajeCentral(String mensaje) {
        limpiarTarjetas();
        JLabel lbl = new JLabel(mensaje, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 15));
        lbl.setForeground(TEXTO_SECUNDARIO);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(30, 20, 30, 20)));
        panel.add(lbl, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        pnlTarjetas.add(panel, gbc);
        pnlTarjetas.revalidate();
        pnlTarjetas.repaint();
    }

    private void limpiarTarjetas() {
        pnlTarjetas.removeAll();
    }

    // ---------- Acceso a datos (consulta directa a la BD) ----------

    private PlanCliente cargarPlanActivo(long idCliente) {
        // Prioriza planes en estado ACTIVO y luego los más recientes por
        // fecha_inicio, para mostrar siempre lo que el nutricionista dejó
        // vigente para este cliente.
        String sql = "SELECT p.id_plan_nutricional, p.nombre_plan, "
                + "p.fecha_inicio, p.fecha_fin, p.calorias_objetivo, "
                + "p.proteinas_objetivo_g, p.carbohidratos_objetivo_g, "
                + "p.estado_plan, "
                + "TRIM(pn.nombres || ' ' || pn.apellidos) AS nutricionista "
                + "FROM plan_nutricional p "
                + "LEFT JOIN persona pn ON pn.id_persona = p.id_nutricionista "
                + "WHERE p.id_cliente = ? "
                + "ORDER BY (CASE WHEN p.estado_plan = 'ACTIVO' THEN 0 "
                + "ELSE 1 END), p.fecha_inicio DESC, "
                + "p.id_plan_nutricional DESC "
                + "LIMIT 1";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idCliente);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) return null;
                PlanCliente p = new PlanCliente();
                p.idPlanNutricional = r.getLong("id_plan_nutricional");
                p.nombrePlan = r.getString("nombre_plan");
                java.sql.Date fi = r.getDate("fecha_inicio");
                java.sql.Date ff = r.getDate("fecha_fin");
                p.fechaInicio = fi == null ? null : fi.toLocalDate();
                p.fechaFin = ff == null ? null : ff.toLocalDate();
                int cal = r.getInt("calorias_objetivo");
                p.calorias = r.wasNull() ? null : cal;
                p.proteinas = r.getBigDecimal("proteinas_objetivo_g");
                p.carbos = r.getBigDecimal("carbohidratos_objetivo_g");
                p.estado = r.getString("estado_plan");
                p.nutricionista = r.getString("nutricionista");
                return p;
            }
        } catch (SQLException ex) {
            return null;
        }
    }

    private List<Comida> cargarComidasDelDia(long idPlan, String diaSemana) {
        List<Comida> lista = new ArrayList<>();
        String sql = "SELECT i.dia_semana, i.tipo_comida, i.hora_consumo, "
                + "i.cantidad, i.unidad_medida, i.orden_comida, "
                + "i.indicaciones, i.estado_detalle, "
                + "a.nombre_alimento, a.categoria "
                + "FROM incluye_alimento i "
                + "JOIN alimento a ON a.id_alimento = i.id_alimento "
                + "WHERE i.id_plan_nutricional = ? "
                + "AND UPPER(i.dia_semana) = ? "
                + "AND (i.estado_detalle IS NULL "
                + "     OR UPPER(i.estado_detalle) NOT IN "
                + "        ('CANCELADO','SUSPENDIDO')) "
                + "ORDER BY i.hora_consumo NULLS LAST, i.orden_comida";
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idPlan);
            s.setString(2, diaSemana);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    Comida x = new Comida();
                    x.tipoComida = r.getString("tipo_comida");
                    Time h = r.getTime("hora_consumo");
                    x.hora = h == null ? null : h.toLocalTime();
                    x.cantidad = r.getBigDecimal("cantidad");
                    x.unidad = r.getString("unidad_medida");
                    int o = r.getInt("orden_comida");
                    x.orden = r.wasNull() ? null : o;
                    x.indicaciones = r.getString("indicaciones");
                    x.alimento = r.getString("nombre_alimento");
                    x.categoria = r.getString("categoria");
                    lista.add(x);
                }
            }
        } catch (SQLException ex) {
            // Retornamos vacío; el UI mostrará el mensaje central.
        }
        return lista;
    }

    // ---------- Utilidades ----------

    private LocalDate fechaSeleccionada() {
        try {
            return LocalDate.parse(txtFechaConsulta.getText().trim());
        } catch (RuntimeException ex) {
            return LocalDate.now();
        }
    }

    private void refrescarDesdeCampo() {
        if (!refrescando && txtFechaConsulta.getText().trim()
                .matches("\\d{4}-\\d{2}-\\d{2}")) {
            refrescarDatos();
        }
    }

    private void moverFecha(int dias) {
        establecerFecha(fechaSeleccionada().plusDays(dias));
    }

    private void establecerFecha(LocalDate fecha) {
        refrescando = true;
        try {
            txtFechaConsulta.setText(fecha.toString());
        } finally {
            refrescando = false;
        }
        refrescarDatos();
    }

    private JButton botonPrincipal(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(AZUL);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private String nombreDia(LocalDate fecha) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE",
                new Locale("es", "ES"));
        return capitalizar(fecha.format(fmt));
    }

    /**
     * Convierte una fecha al identificador de día usado en la BD: LUNES,
     * MARTES, MIERCOLES (sin tilde), JUEVES, VIERNES, SABADO (sin tilde),
     * DOMINGO. Igual a la convención de {@link vista.PnlNutricion}.
     */
    private String diaCorto(LocalDate fecha) {
        return switch (fecha.getDayOfWeek()) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }

    private String capitalizar(String valor) {
        if (valor == null || valor.isBlank()) return "";
        return Character.toUpperCase(valor.charAt(0)) + valor.substring(1);
    }

    private String valor(Object o) { return o == null ? "" : o.toString(); }

    private String htmlCentrado(String texto) {
        if (texto == null) {
            return "<html><div style='text-align:center;'>Alimento</div></html>";
        }
        String seguro = texto.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;");
        return "<html><div style='text-align:center;width:210px;'>"
                + seguro + "</div></html>";
    }

    /** Registro de plan vigente del cliente. */
    private static final class PlanCliente {
        Long idPlanNutricional;
        String nombrePlan;
        LocalDate fechaInicio;
        LocalDate fechaFin;
        Integer calorias;
        BigDecimal proteinas;
        BigDecimal carbos;
        String estado;
        String nutricionista;
    }

    /** Alimento del día ya resuelto (join incluye_alimento + alimento). */
    private static final class Comida {
        String tipoComida;
        LocalTime hora;
        BigDecimal cantidad;
        String unidad;
        Integer orden;
        String indicaciones;
        String alimento;
        String categoria;
    }
}
