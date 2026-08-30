package vista;

import conexion.ConexionPostgreSQL;
import controlador.ClienteControlador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import utilidades.SesionUsuario;

/**
 * Reportes del entrenador enfocados en sus clientes asignados.
 * Muestra:
 *   - Resumen general del grupo (KPI cards).
 *   - Historial de peso por cliente (medicion_corporal).
 *   - Cumplimiento de rutinas (progreso_rutina).
 *   - Semáforo de progreso (Cumple / Justo / Riesgo).
 *
 * Ofrece impresión de cada tabla y descarga de un reporte consolidado
 * en HTML con nombre "Reporte entrenador <fecha>.html".
 */
public final class PnlReportesEntrenador extends JPanel {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color TEXTO = new Color(90, 110, 135);
    private static final Color AZUL = new Color(8, 124, 255);
    private static final Color VERDE = new Color(34, 197, 94);
    private static final Color AMARILLO = new Color(234, 179, 8);
    private static final Color ROJO = new Color(239, 68, 68);

    private final ClienteControlador clienteControlador = new ClienteControlador();

    private final JLabel lblTotal = kpiValor();
    private final JLabel lblConRutina = kpiValor();
    private final JLabel lblCumpleAlto = kpiValor();
    private final JLabel lblRiesgo = kpiValor();

    private final JComboBox<Cliente> cboCliente = new JComboBox<>();
    private final JLabel lblCliInicial = kpiValor();
    private final JLabel lblCliMeta = kpiValor();
    private final JLabel lblCliActual = kpiValor();
    private final JLabel lblCliCumpl = kpiValor();
    private final JLabel lblSemaforo = new JLabel(" ",
            SwingConstants.CENTER);

    private final JTable tblResumenClientes = new JTable();
    private final JTable tblPeso = new JTable();
    private final JTable tblRutinas = new JTable();
    private final JTable tblCumplimientoDia = new JTable();

    public PnlReportesEntrenador() {
        setLayout(new BorderLayout(0, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);

        cboCliente.addActionListener(e -> refrescarClienteDetalle());
        cargarClientes();
        cargarResumenGrupal();
        cargarTablaClientes();
        refrescarClienteDetalle();
    }

    // ---- Layout ---------------------------------------------------------

    private JPanel construirEncabezado() {
        JPanel enc = new JPanel(new BorderLayout(0, 10));
        enc.setBackground(TARJETA);
        enc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel titulo = new JLabel("Reportes de mis clientes");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(TITULO);

        JLabel sub = new JLabel(
                "Peso, rutinas realizadas y cumplimiento de cada cliente asignado.");
        sub.setForeground(TEXTO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(sub);
        enc.add(textos, BorderLayout.NORTH);

        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setOpaque(false);
        kpis.add(tarjeta("Clientes asignados", lblTotal));
        kpis.add(tarjeta("Con rutina activa", lblConRutina));
        kpis.add(tarjeta("Cumplimiento alto (≥70%)", lblCumpleAlto));
        kpis.add(tarjeta("En riesgo (<30%)", lblRiesgo));
        enc.add(kpis, BorderLayout.CENTER);
        return enc;
    }

    private JTabbedPane construirCentro() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Resumen por cliente", construirTabResumen());
        tabs.addTab("Detalle de cliente", construirTabDetalle());
        return tabs;
    }

    /**
     * Pestaña "Resumen por cliente": arriba la tabla clásica de KPIs,
     * abajo un mapa de cumplimiento con 7 columnas (Lun..Dom) coloreadas
     * en verde/amarillo/gris según el % completado por día.
     */
    private JPanel construirTabResumen() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.add(wrap(tblResumenClientes), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout(0, 6));
        abajo.setOpaque(false);
        JLabel tit = new JLabel(
                "Cumplimiento por día (últimos 7 días)");
        tit.setFont(new Font("SansSerif", Font.BOLD, 13));
        tit.setForeground(TITULO);
        tit.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));
        abajo.add(tit, BorderLayout.NORTH);
        abajo.add(wrap(tblCumplimientoDia), BorderLayout.CENTER);

        JLabel leyenda = new JLabel(
                "Verde = 100 %   ·   Amarillo = parcial   ·   Gris = 0 %");
        leyenda.setFont(new Font("SansSerif", Font.PLAIN, 11));
        leyenda.setForeground(TEXTO);
        leyenda.setBorder(BorderFactory.createEmptyBorder(2, 4, 4, 4));
        abajo.add(leyenda, BorderLayout.SOUTH);
        abajo.setPreferredSize(new Dimension(0, 240));
        p.add(abajo, BorderLayout.SOUTH);
        return p;
    }

    private JPanel construirTabDetalle() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);

        JPanel sel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        sel.setBackground(TARJETA);
        sel.setBorder(BorderFactory.createLineBorder(BORDE));
        sel.add(new JLabel("Cliente:"));
        cboCliente.setPreferredSize(new Dimension(300, 26));
        sel.add(cboCliente);
        top.add(sel, BorderLayout.NORTH);

        JPanel kpis = new JPanel(new GridLayout(1, 5, 10, 0));
        kpis.setOpaque(false);
        kpis.add(tarjeta("Peso inicial", lblCliInicial));
        kpis.add(tarjeta("Peso meta", lblCliMeta));
        kpis.add(tarjeta("Peso actual", lblCliActual));
        kpis.add(tarjeta("Cumplimiento semanal", lblCliCumpl));
        kpis.add(tarjetaSemaforo(lblSemaforo));
        top.add(kpis, BorderLayout.CENTER);
        p.add(top, BorderLayout.NORTH);

        JTabbedPane sub = new JTabbedPane();
        sub.addTab("Historial de peso", wrap(tblPeso));
        sub.addTab("Rutinas realizadas", wrap(tblRutinas));
        p.add(sub, BorderLayout.CENTER);
        return p;
    }

    private JScrollPane wrap(JTable t) {
        t.setRowHeight(28);
        t.getTableHeader().setBackground(new Color(10, 58, 108));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setReorderingAllowed(false);
        return new JScrollPane(t);
    }

    private JPanel construirPie() {
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pie.setBackground(TARJETA);
        pie.setBorder(BorderFactory.createLineBorder(BORDE));
        JButton btnImp = boton("Imprimir tablas visibles", AZUL);
        btnImp.addActionListener(e -> imprimirTablas());
        JButton btnDesc = boton("⬇ Descargar reporte", VERDE);
        btnDesc.addActionListener(e -> descargarConsolidado());
        pie.add(btnImp);
        pie.add(btnDesc);
        return pie;
    }

    // ---- Carga de datos --------------------------------------------------

    private Long idEntrenadorSesion() {
        if (!SesionUsuario.haySesionActiva()) return null;
        return SesionUsuario.getUsuarioActual().getIdPersona();
    }

    private void cargarClientes() {
        Long id = idEntrenadorSesion();
        DefaultComboBoxModel<Cliente> m = new DefaultComboBoxModel<>();
        if (id != null) {
            for (Cliente c : clienteControlador
                    .listarPorEntrenador(id, "")) {
                m.addElement(c);
            }
        }
        cboCliente.setModel(m);
    }

    private void cargarResumenGrupal() {
        Long id = idEntrenadorSesion();
        if (id == null) return;
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            int total = queryInt(c,
                    "SELECT COUNT(DISTINCT c.id_persona) FROM cliente c "
                    + "JOIN asignacion_rutina ar ON ar.id_cliente = c.id_persona "
                    + "JOIN rutina r ON r.id_rutina = ar.id_rutina "
                    + "WHERE r.id_entrenador = ?", id);
            int conRutina = queryInt(c,
                    "SELECT COUNT(DISTINCT c.id_persona) FROM cliente c "
                    + "JOIN asignacion_rutina ar ON ar.id_cliente = c.id_persona "
                    + "JOIN rutina r ON r.id_rutina = ar.id_rutina "
                    + "WHERE r.id_entrenador = ? "
                    + "AND ar.estado_asignacion = 'ACTIVA'", id);
            lblTotal.setText(String.valueOf(total));
            lblConRutina.setText(String.valueOf(conRutina));

            int cumpleAlto = 0;
            int riesgo = 0;
            for (Cliente cl : clienteControlador
                    .listarPorEntrenador(id, "")) {
                double pct = cumplimientoSemanal(c, cl.getIdPersona());
                if (pct >= 70) cumpleAlto++;
                if (pct < 30) riesgo++;
            }
            lblCumpleAlto.setText(String.valueOf(cumpleAlto));
            lblRiesgo.setText(String.valueOf(riesgo));
        } catch (SQLException ex) {
            // No bloqueamos la vista si algún KPI falla.
        }
    }

    private void cargarTablaClientes() {
        Long id = idEntrenadorSesion();
        DefaultTableModel m = new DefaultTableModel(new Object[]{
            "Código", "Cliente", "Peso inicial", "Peso meta",
            "Peso actual", "% avance", "Cumplimiento sem.",
            "Semáforo"}, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        if (id == null) { tblResumenClientes.setModel(m); return; }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            for (Cliente cl : clienteControlador
                    .listarPorEntrenador(id, "")) {
                Double actual = ultimoPeso(c, cl.getIdPersona());
                BigDecimal pi = cl.getPesoInicial();
                BigDecimal pm = cl.getPesoMeta();
                String avance = calcAvance(pi, pm, actual);
                double pctCumpl = cumplimientoSemanal(c,
                        cl.getIdPersona());
                m.addRow(new Object[]{
                    cl.getCodigoCliente(),
                    cl.getNombreCompleto(),
                    pi == null ? "—" : formatoKg(pi.doubleValue()),
                    pm == null ? "—" : formatoKg(pm.doubleValue()),
                    actual == null ? "—" : formatoKg(actual),
                    avance,
                    String.format("%.0f %%", pctCumpl),
                    semaforoTexto(pctCumpl)
                });
            }
        } catch (SQLException ex) {
            // Solo se pierden filas parciales, sigue viable.
        }
        tblResumenClientes.setModel(m);
        aplicarRendererSemaforo(tblResumenClientes);
    }

    /**
     * Pinta la última columna ("Semáforo") con fondo verde/amarillo/rojo
     * según el texto: "Cumple", "Justo" o "En riesgo". Sin emojis, para
     * que se vea igual en cualquier sistema/fuente.
     */
    private void aplicarRendererSemaforo(JTable tabla) {
        int col = tabla.getColumnCount() - 1;
        if (col < 0) return;
        javax.swing.table.TableCellRenderer r =
                new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foco,
                    int fila, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(
                        t, v, sel, foco, fila, c);
                String texto = v == null ? "" : v.toString().trim();
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setOpaque(true);
                l.setFont(new Font("SansSerif", Font.BOLD, 12));
                Color bg;
                if (texto.equalsIgnoreCase("Cumple")) {
                    bg = VERDE;
                } else if (texto.equalsIgnoreCase("Justo")) {
                    bg = AMARILLO;
                } else if (texto.equalsIgnoreCase("En riesgo")) {
                    bg = ROJO;
                } else {
                    bg = Color.LIGHT_GRAY;
                }
                l.setBackground(bg);
                l.setForeground(Color.WHITE);
                l.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return l;
            }
        };
        tabla.getColumnModel().getColumn(col).setCellRenderer(r);
        tabla.getColumnModel().getColumn(col).setPreferredWidth(110);
    }

    private void refrescarClienteDetalle() {
        Object o = cboCliente.getSelectedItem();
        if (!(o instanceof Cliente cl)) {
            lblCliInicial.setText("—");
            lblCliMeta.setText("—");
            lblCliActual.setText("—");
            lblCliCumpl.setText("—");
            lblSemaforo.setText("Sin cliente");
            lblSemaforo.setForeground(TEXTO);
            tblPeso.setModel(new DefaultTableModel());
            tblRutinas.setModel(new DefaultTableModel());
            return;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            Double actual = ultimoPeso(c, cl.getIdPersona());
            BigDecimal pi = cl.getPesoInicial();
            BigDecimal pm = cl.getPesoMeta();
            lblCliInicial.setText(pi == null ? "—"
                    : formatoKg(pi.doubleValue()));
            lblCliMeta.setText(pm == null ? "—"
                    : formatoKg(pm.doubleValue()));
            lblCliActual.setText(actual == null ? "—"
                    : formatoKg(actual));
            double pct = cumplimientoSemanal(c, cl.getIdPersona());
            lblCliCumpl.setText(String.format("%.0f %%", pct));
            aplicarSemaforo(pct);

            DefaultTableModel pesoM = new DefaultTableModel(new Object[]{
                "Fecha", "Peso (kg)", "Tipo evaluación",
                "Observaciones"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT ef.fecha_evaluacion, mc.peso_kg, "
                    + "ef.tipo_evaluacion, mc.observaciones "
                    + "FROM evaluacion_fisica ef "
                    + "JOIN medicion_corporal mc "
                    + "  ON mc.id_evaluacion = ef.id_evaluacion "
                    + "WHERE ef.id_cliente = ? AND mc.peso_kg IS NOT NULL "
                    + "ORDER BY ef.fecha_evaluacion DESC")) {
                s.setLong(1, cl.getIdPersona());
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        pesoM.addRow(new Object[]{r.getDate(1),
                            r.getBigDecimal(2), r.getString(3),
                            r.getString(4)});
                    }
                }
            }
            tblPeso.setModel(pesoM);

            DefaultTableModel rutM = new DefaultTableModel(new Object[]{
                "Fecha", "Rutina", "Sesiones planificadas",
                "Sesiones completadas", "% del día", "Estado"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT pr.fecha_registro, r.nombre_rutina, "
                    + "pr.sesiones_planificadas, "
                    + "pr.sesiones_completadas, pr.estado_progreso "
                    + "FROM progreso_rutina pr "
                    + "JOIN rutina r ON r.id_rutina = pr.id_rutina "
                    + "WHERE pr.id_cliente = ? "
                    + "ORDER BY pr.fecha_registro DESC LIMIT 60")) {
                s.setLong(1, cl.getIdPersona());
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        int plan = r.getInt(3);
                        int hech = r.getInt(4);
                        String p = plan == 0 ? "—"
                                : String.format("%.0f %%",
                                        100.0 * hech / plan);
                        rutM.addRow(new Object[]{r.getDate(1),
                            r.getString(2), plan, hech, p,
                            r.getString(5)});
                    }
                }
            }
            tblRutinas.setModel(rutM);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el detalle: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ---- Consultas concretas --------------------------------------------

    private Double ultimoPeso(Connection c, Long idCliente)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT mc.peso_kg FROM evaluacion_fisica ef "
                + "JOIN medicion_corporal mc "
                + "  ON mc.id_evaluacion = ef.id_evaluacion "
                + "WHERE ef.id_cliente = ? AND mc.peso_kg IS NOT NULL "
                + "ORDER BY ef.fecha_evaluacion DESC, ef.id_evaluacion DESC "
                + "LIMIT 1")) {
            s.setLong(1, idCliente);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    BigDecimal bd = r.getBigDecimal(1);
                    return bd == null ? null : bd.doubleValue();
                }
            }
        }
        return null;
    }

    /**
     * % de cumplimiento semanal: sesiones_completadas / sesiones_planificadas
     * agregado en la última semana. Si no hay planificación, devuelve 0.
     */
    private double cumplimientoSemanal(Connection c, Long idCliente)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT COALESCE(SUM(sesiones_planificadas), 0), "
                + "COALESCE(SUM(sesiones_completadas), 0) "
                + "FROM progreso_rutina "
                + "WHERE id_cliente = ? "
                + "AND fecha_registro >= CURRENT_DATE - INTERVAL '7 day'")) {
            s.setLong(1, idCliente);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                int plan = r.getInt(1);
                int hech = r.getInt(2);
                return plan == 0 ? 0.0 : 100.0 * hech / plan;
            }
        }
    }

    // ---- Helpers UI ------------------------------------------------------

    private String calcAvance(BigDecimal pi, BigDecimal pm, Double actual) {
        if (pi == null || pm == null || actual == null) return "—";
        double diff = pi.doubleValue() - pm.doubleValue();
        if (Math.abs(diff) < 0.001) return "—";
        double av = pi.doubleValue() - actual;
        double pct = Math.max(0, Math.min(100, (av / diff) * 100));
        return String.format("%.0f %%", pct);
    }

    private String semaforoTexto(double pct) {
        // Sin emojis: la celda se colorea de fondo con un renderer.
        if (pct >= 70) return "Cumple";
        if (pct >= 30) return "Justo";
        return "En riesgo";
    }

    private void aplicarSemaforo(double pct) {
        if (pct >= 70) {
            lblSemaforo.setText("Cumple");
            lblSemaforo.setForeground(VERDE);
        } else if (pct >= 30) {
            lblSemaforo.setText("Justo");
            lblSemaforo.setForeground(AMARILLO);
        } else {
            lblSemaforo.setText("En riesgo");
            lblSemaforo.setForeground(ROJO);
        }
    }

    private String formatoKg(double v) {
        return String.format(Locale.US, "%.2f kg", v);
    }

    // ---- Refresh público -------------------------------------------------

    public void refrescarDatos() {
        cargarClientes();
        cargarResumenGrupal();
        cargarTablaClientes();
        cargarCumplimientoPorDia();
        refrescarClienteDetalle();
    }

    /**
     * Rellena la tabla de cumplimiento por día: una fila por cliente y
     * 7 columnas (Lunes..Domingo). Cada celda es un objeto {@code int[]}
     * = [completadas, planificadas] que un renderer traduce a un color
     * de barra (verde/amarillo/gris) con texto "X/Y" centrado.
     */
    private void cargarCumplimientoPorDia() {
        Long idEntrenador = idEntrenadorSesion();
        String[] cols = {"Cliente", "Lun", "Mar", "Mié", "Jue", "Vie",
                "Sáb", "Dom"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
            @Override public Class<?> getColumnClass(int c) {
                return c == 0 ? String.class : int[].class;
            }
        };
        tblCumplimientoDia.setModel(m);
        if (idEntrenador == null) return;

        try (Connection c = ConexionPostgreSQL.getConexion()) {
            for (Cliente cl : clienteControlador
                    .listarPorEntrenador(idEntrenador, "")) {
                int[][] dias = new int[7][2];
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT EXTRACT(ISODOW FROM fecha_registro)::int AS d, "
                        + "COALESCE(SUM(sesiones_planificadas), 0), "
                        + "COALESCE(SUM(sesiones_completadas), 0) "
                        + "FROM progreso_rutina "
                        + "WHERE id_cliente = ? "
                        + "AND fecha_registro >= CURRENT_DATE - INTERVAL '6 day' "
                        + "GROUP BY d")) {
                    s.setLong(1, cl.getIdPersona());
                    try (ResultSet r = s.executeQuery()) {
                        while (r.next()) {
                            int d = r.getInt(1) - 1; // 0..6
                            if (d >= 0 && d <= 6) {
                                dias[d][1] = r.getInt(2);
                                dias[d][0] = r.getInt(3);
                            }
                        }
                    }
                }
                Object[] fila = new Object[8];
                fila[0] = cl.getNombreCompleto();
                for (int i = 0; i < 7; i++) fila[i + 1] = dias[i];
                m.addRow(fila);
            }
        } catch (SQLException ex) {
            // No bloqueamos la vista si algún cliente falla.
        }

        // Renderer coloreado en las columnas de día.
        javax.swing.table.TableCellRenderer renderer =
                new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc,
                    int fila, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(
                        t, "", sel, foc, fila, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(new Font("SansSerif", Font.BOLD, 11));
                l.setForeground(Color.WHITE);
                l.setOpaque(true);
                if (v instanceof int[] arr && arr.length >= 2) {
                    int hech = arr[0];
                    int plan = arr[1];
                    if (plan == 0) {
                        l.setBackground(new Color(226, 232, 240));
                        l.setForeground(new Color(120, 130, 145));
                        l.setText("—");
                    } else if (hech >= plan) {
                        l.setBackground(VERDE);
                        l.setText(hech + "/" + plan);
                    } else if (hech == 0) {
                        l.setBackground(new Color(200, 208, 220));
                        l.setForeground(new Color(90, 100, 115));
                        l.setText("0/" + plan);
                    } else {
                        l.setBackground(AMARILLO);
                        l.setText(hech + "/" + plan);
                    }
                } else {
                    l.setBackground(sel
                            ? new Color(220, 238, 255) : Color.WHITE);
                    l.setForeground(TITULO);
                    l.setText(v == null ? "" : v.toString());
                    l.setHorizontalAlignment(SwingConstants.LEFT);
                }
                return l;
            }
        };
        for (int i = 1; i <= 7; i++) {
            tblCumplimientoDia.getColumnModel().getColumn(i)
                    .setCellRenderer(renderer);
            tblCumplimientoDia.getColumnModel().getColumn(i)
                    .setPreferredWidth(65);
        }
        tblCumplimientoDia.getColumnModel().getColumn(0)
                .setPreferredWidth(220);
        tblCumplimientoDia.setRowHeight(30);
    }

    // ---- Acciones --------------------------------------------------------

    private void imprimirTablas() {
        JTable[] tablas = {tblResumenClientes, tblPeso, tblRutinas};
        String[] nombres = {"Resumen por cliente", "Historial de peso",
                "Rutinas realizadas"};
        try {
            for (int i = 0; i < tablas.length; i++) {
                if (tablas[i].getRowCount() == 0) continue;
                tablas[i].print(JTable.PrintMode.FIT_WIDTH,
                        new MessageFormat("GYMNOVA · " + nombres[i]
                                + " · " + LocalDate.now()),
                        new MessageFormat("Página {0}"));
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al imprimir: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void descargarConsolidado() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar reporte del entrenador");
        fc.setSelectedFile(new File(
                "Reporte entrenador " + LocalDate.now() + ".html"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Archivos HTML (*.html)", "html"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File destino = fc.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".html")) {
            destino = new File(destino.getParentFile(),
                    destino.getName() + ".html");
        }
        try {
            Files.writeString(destino.toPath(), generarHtml(),
                    StandardCharsets.UTF_8);
        } catch (IOException | SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "Reporte guardado:\n" + destino.getAbsolutePath()
                + "\n\n¿Deseas abrirlo ahora?",
                "GYMNOVA", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        if (op == JOptionPane.YES_OPTION
                && java.awt.Desktop.isDesktopSupported()) {
            try {
                java.awt.Desktop.getDesktop().browse(destino.toURI());
            } catch (IOException | UnsupportedOperationException ignored) {
                // Nada; ya lo puede abrir manualmente.
            }
        }
    }

    private String generarHtml() throws SQLException {
        Long id = idEntrenadorSesion();
        String entrenador = SesionUsuario.haySesionActiva()
                ? SesionUsuario.getUsuarioActual().getNombreUsuario()
                : "Entrenador";
        StringBuilder h = new StringBuilder();
        h.append("<!DOCTYPE html><html lang=\"es\"><head>")
         .append("<meta charset=\"UTF-8\"/>")
         .append("<title>Reporte entrenador ").append(LocalDate.now())
         .append("</title><style>")
         .append(css()).append("</style></head><body>");
        h.append("<header><h1>GYMNOVA</h1>")
         .append("<p class=\"subtitulo\">Reporte del entrenador ")
         .append(escapar(entrenador)).append("</p>")
         .append("<p class=\"generado\">Generado el ")
         .append(LocalDate.now()).append("</p></header>");

        try (Connection c = ConexionPostgreSQL.getConexion()) {
            h.append("<section><h2>Resumen por cliente</h2>");
            h.append("<table><thead><tr><th>Código</th><th>Cliente</th>")
             .append("<th>Peso inicial</th><th>Peso meta</th>")
             .append("<th>Peso actual</th><th>% avance</th>")
             .append("<th>Cumplimiento semanal</th><th>Estado</th>")
             .append("</tr></thead><tbody>");
            if (id != null) {
                for (Cliente cl : clienteControlador
                        .listarPorEntrenador(id, "")) {
                    Double actual = ultimoPeso(c, cl.getIdPersona());
                    BigDecimal pi = cl.getPesoInicial();
                    BigDecimal pm = cl.getPesoMeta();
                    double pct = cumplimientoSemanal(c, cl.getIdPersona());
                    h.append("<tr>")
                     .append("<td>").append(escapar(cl.getCodigoCliente()))
                     .append("</td>")
                     .append("<td>").append(escapar(cl.getNombreCompleto()))
                     .append("</td>")
                     .append("<td>").append(pi == null ? "—"
                             : formatoKg(pi.doubleValue())).append("</td>")
                     .append("<td>").append(pm == null ? "—"
                             : formatoKg(pm.doubleValue())).append("</td>")
                     .append("<td>").append(actual == null ? "—"
                             : formatoKg(actual)).append("</td>")
                     .append("<td>").append(calcAvance(pi, pm, actual))
                     .append("</td>")
                     .append("<td>").append(String.format("%.0f %%", pct))
                     .append("</td>")
                     .append("<td>").append(semaforoTexto(pct)).append("</td>")
                     .append("</tr>");
                }
            }
            h.append("</tbody></table></section>");

            if (id != null) {
                for (Cliente cl : clienteControlador
                        .listarPorEntrenador(id, "")) {
                    h.append("<section><h2>Detalle · ")
                     .append(escapar(cl.getNombreCompleto()))
                     .append(" (").append(escapar(cl.getCodigoCliente()))
                     .append(")</h2>");
                    h.append("<h3>Historial de peso</h3>");
                    tablaSql(h, c,
                            "SELECT ef.fecha_evaluacion AS \"Fecha\", "
                            + "mc.peso_kg AS \"Peso (kg)\", "
                            + "ef.tipo_evaluacion AS \"Tipo\", "
                            + "mc.observaciones AS \"Observaciones\" "
                            + "FROM evaluacion_fisica ef "
                            + "JOIN medicion_corporal mc "
                            + "  ON mc.id_evaluacion = ef.id_evaluacion "
                            + "WHERE ef.id_cliente = ? "
                            + "AND mc.peso_kg IS NOT NULL "
                            + "ORDER BY ef.fecha_evaluacion DESC",
                            cl.getIdPersona());
                    h.append("<h3>Rutinas realizadas (últimas 60)</h3>");
                    tablaSql(h, c,
                            "SELECT pr.fecha_registro AS \"Fecha\", "
                            + "r.nombre_rutina AS \"Rutina\", "
                            + "pr.sesiones_planificadas AS \"Planif.\", "
                            + "pr.sesiones_completadas AS \"Hechas\", "
                            + "pr.estado_progreso AS \"Estado\" "
                            + "FROM progreso_rutina pr "
                            + "JOIN rutina r ON r.id_rutina = pr.id_rutina "
                            + "WHERE pr.id_cliente = ? "
                            + "ORDER BY pr.fecha_registro DESC LIMIT 60",
                            cl.getIdPersona());
                    h.append("</section>");
                }
            }
        }
        h.append("<footer>GYMNOVA · Generado ").append(LocalDate.now())
         .append("</footer></body></html>");
        return h.toString();
    }

    private void tablaSql(StringBuilder h, Connection c, String sql,
            Long param) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, param);
            try (ResultSet r = s.executeQuery()) {
                java.sql.ResultSetMetaData md = r.getMetaData();
                int cols = md.getColumnCount();
                h.append("<table><thead><tr>");
                for (int i = 1; i <= cols; i++) {
                    h.append("<th>").append(escapar(md.getColumnLabel(i)))
                     .append("</th>");
                }
                h.append("</tr></thead><tbody>");
                int filas = 0;
                while (r.next()) {
                    h.append("<tr>");
                    for (int i = 1; i <= cols; i++) {
                        Object v = r.getObject(i);
                        h.append("<td>")
                         .append(escapar(v == null ? "" : v.toString()))
                         .append("</td>");
                    }
                    h.append("</tr>");
                    filas++;
                }
                if (filas == 0) {
                    h.append("<tr><td colspan=\"").append(cols)
                     .append("\" class=\"vacio\">Sin datos</td></tr>");
                }
                h.append("</tbody></table>");
            }
        }
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String css() {
        return "*{box-sizing:border-box;font-family:Segoe UI,Arial,sans-serif;}"
                + "body{margin:0;background:#eaf2fb;color:#172a43;}"
                + "header{background:#0a3a6c;color:#fff;padding:24px 32px;}"
                + "header h1{margin:0;font-size:28px;letter-spacing:1px;}"
                + "header .subtitulo{margin:6px 0 0;opacity:.85;}"
                + "header .generado{margin:2px 0 0;opacity:.65;font-size:12px;}"
                + "section{background:#fff;margin:16px 24px;padding:20px 24px;"
                + "border-radius:8px;border:1px solid #d4e1ef;}"
                + "section h2{color:#0a3a6c;border-bottom:2px solid #087cff;"
                + "padding-bottom:6px;margin:0 0 12px;}"
                + "section h3{color:#172a43;margin:16px 0 6px;font-size:14px;}"
                + "table{width:100%;border-collapse:collapse;font-size:12px;"
                + "margin-top:6px;}"
                + "thead{background:#0a3a6c;color:#fff;}"
                + "th{padding:8px 10px;text-align:center;font-weight:600;}"
                + "td{padding:6px 10px;border-bottom:1px solid #eef2f7;"
                + "text-align:center;}"
                + ".vacio{color:#8a99b1;font-style:italic;}"
                + "footer{color:#5a6e87;font-size:11px;padding:16px 24px;"
                + "text-align:center;}";
    }

    // ---- Utilería SQL ----------------------------------------------------

    private static int queryInt(Connection c, String sql, Long param)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, param);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    // ---- Helpers UI atómicos --------------------------------------------

    private JPanel tarjeta(String titulo, JLabel valor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(TARJETA);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(TEXTO);
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(valor);
        return p;
    }

    private JPanel tarjetaSemaforo(JLabel etiqueta) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(TARJETA);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JLabel t = new JLabel("Estado del cliente");
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(TEXTO);
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 18));
        p.add(etiqueta);
        return p;
    }

    private JLabel kpiValor() {
        JLabel l = new JLabel("—", SwingConstants.LEFT);
        l.setFont(new Font("SansSerif", Font.BOLD, 20));
        l.setForeground(AZUL);
        return l;
    }

    private JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return b;
    }
}
