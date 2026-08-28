package vista;

import controlador.KpiFinanzasControlador;
import controlador.KpiFinanzasControlador.CobroMembresiaReciente;
import controlador.KpiFinanzasControlador.DesglosePlan;
import controlador.KpiFinanzasControlador.Kpis;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/** Dashboard financiero alimentado por pagos CONFIRMADOS. */
public final class PnlDashboardFinanzas extends JPanel {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TEXTO_TITULO = new Color(52, 74, 100);
    private static final Color TEXTO_VALOR = new Color(23, 42, 67);
    private static final Color AZUL = new Color(8, 124, 255);
    private static final Color VERDE = new Color(34, 197, 94);
    private static final Color NARANJA = new Color(234, 88, 12);
    private static final Color VIOLETA = new Color(109, 40, 217);
    private static final Color TURQUESA = new Color(13, 148, 136);

    private static final NumberFormat DINERO
            = NumberFormat.getCurrencyInstance(new Locale("es", "EC"));
    private static final DateTimeFormatter FECHA_HORA
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final KpiFinanzasControlador controlador
            = new KpiFinanzasControlador();

    private final JLabel valMembresiasActivas = crearValor("0");
    private final JLabel valIngresosMes = crearValor("$0.00");
    private final JLabel valIngresosMembresias = crearValor("$0.00");
    private final JLabel subIngresosMembresias = crearSubtitulo("0 cobros este mes");
    private final JLabel valIngresosAno = crearValor("$0.00");
    private final JLabel valFacturasPendientes = crearValor("0");
    private final JLabel subFacturasPendientes = crearSubtitulo("$0.00 por cobrar");

    private final DefaultTableModel modeloDesglose = crearModeloDesglose();
    private final JTable tblDesglose = new JTable(modeloDesglose);
    private final DefaultTableModel modeloCobros = crearModeloCobros();
    private final JTable tblCobros = new JTable(modeloCobros);
    private final JLabel lblEstado = new JLabel(" ");

    public PnlDashboardFinanzas() {
        setLayout(new BorderLayout(0, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCuerpo(), BorderLayout.CENTER);
        refrescarDatos();
    }

    private JPanel construirEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(TARJETA);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        JLabel titulo = new JLabel("Dashboard financiero");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(TEXTO_VALOR);
        encabezado.add(titulo, BorderLayout.WEST);
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setBackground(AZUL);
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnActualizar.addActionListener(evento -> refrescarDatos());
        encabezado.add(btnActualizar, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel construirCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);

        JPanel tarjetas = new JPanel(new GridLayout(1, 5, 10, 10));
        tarjetas.setOpaque(false);
        tarjetas.add(tarjeta("Membresias activas", valMembresiasActivas,
                crearSubtitulo("Vigentes hoy"), AZUL));
        tarjetas.add(tarjeta("Ingresos del mes", valIngresosMes,
                crearSubtitulo("Todos los pagos"), VERDE));
        tarjetas.add(tarjeta("Mensualidades cobradas", valIngresosMembresias,
                subIngresosMembresias, TURQUESA));
        tarjetas.add(tarjeta("Ingresos del anio", valIngresosAno,
                crearSubtitulo("Acumulado " + java.time.Year.now()), VIOLETA));
        tarjetas.add(tarjeta("Facturas por cobrar", valFacturasPendientes,
                subFacturasPendientes, NARANJA));
        cuerpo.add(tarjetas, BorderLayout.NORTH);

        JPanel tablas = new JPanel(new GridLayout(2, 1, 0, 12));
        tablas.setOpaque(false);
        tablas.add(construirTablaDesglose());
        tablas.add(construirTablaCobros());
        cuerpo.add(tablas, BorderLayout.CENTER);

        lblEstado.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblEstado.setForeground(new Color(120, 128, 145));
        cuerpo.add(lblEstado, BorderLayout.SOUTH);
        return cuerpo;
    }

    private JPanel tarjeta(String tituloTexto, JLabel valor,
            JLabel subtitulo, Color acento) {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new javax.swing.BoxLayout(tarjeta,
                javax.swing.BoxLayout.Y_AXIS));
        tarjeta.setBackground(TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, acento),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDE),
                        BorderFactory.createEmptyBorder(12, 14, 12, 14))));
        JLabel titulo = new JLabel(tituloTexto);
        titulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titulo.setForeground(TEXTO_TITULO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        valor.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(titulo);
        tarjeta.add(javax.swing.Box.createVerticalStrut(7));
        tarjeta.add(valor);
        tarjeta.add(javax.swing.Box.createVerticalStrut(4));
        tarjeta.add(subtitulo);
        return tarjeta;
    }

    private JLabel crearValor(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(TEXTO_VALOR);
        return label;
    }

    private JLabel crearSubtitulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(new Color(120, 128, 145));
        return label;
    }

    private DefaultTableModel crearModeloDesglose() {
        return new DefaultTableModel(
                new Object[]{"Plan", "Membresias activas", "Ingresos del mes"}, 0) {
            @Override public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private DefaultTableModel crearModeloCobros() {
        return new DefaultTableModel(
                new Object[]{"Fecha", "Cliente", "Plan", "Membresia", "Metodo", "Total"}, 0) {
            @Override public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private JPanel construirTablaDesglose() {
        JPanel contenedor = contenedorTabla("Desglose por tipo de plan");
        configurarTabla(tblDesglose);
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        TableColumn colActivas = tblDesglose.getColumnModel().getColumn(1);
        colActivas.setCellRenderer(derecha);
        TableColumn colIngresos = tblDesglose.getColumnModel().getColumn(2);
        colIngresos.setCellRenderer(derecha);
        JScrollPane scroll = new JScrollPane(tblDesglose);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        contenedor.add(scroll, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel construirTablaCobros() {
        JPanel contenedor = contenedorTabla("Ultimos cobros de membresias y renovaciones");
        configurarTabla(tblCobros);
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tblCobros.getColumnModel().getColumn(5).setCellRenderer(derecha);
        tblCobros.getColumnModel().getColumn(0).setPreferredWidth(130);
        tblCobros.getColumnModel().getColumn(1).setPreferredWidth(220);
        JScrollPane scroll = new JScrollPane(tblCobros);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE));
        contenedor.add(scroll, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel contenedorTabla(String textoTitulo) {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(TARJETA);
        contenedor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel titulo = new JLabel(textoTitulo);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setForeground(TEXTO_VALOR);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
        contenedor.add(titulo, BorderLayout.NORTH);
        return contenedor;
    }

    private void configurarTabla(JTable tabla) {
        tabla.setRowHeight(30);
        tabla.setShowGrid(false);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionBackground(new Color(220, 238, 255));
        tabla.setSelectionForeground(TEXTO_VALOR);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 34));
        DefaultTableCellRenderer encabezado = new DefaultTableCellRenderer();
        encabezado.setOpaque(true);
        encabezado.setBackground(new Color(10, 58, 108));
        encabezado.setForeground(Color.WHITE);
        encabezado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        encabezado.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getTableHeader().setDefaultRenderer(encabezado);
    }

    public void refrescarDatos() {
        Kpis kpis = controlador.obtenerKpis();
        valMembresiasActivas.setText(String.valueOf(kpis.membresiasActivas));
        valIngresosMes.setText(formatoDinero(kpis.ingresosMes));
        valIngresosMembresias.setText(formatoDinero(kpis.ingresosMembresiasMes));
        subIngresosMembresias.setText(
                kpis.cobrosMembresiasMes + " cobros este mes");
        valIngresosAno.setText(formatoDinero(kpis.ingresosAno));
        valFacturasPendientes.setText(String.valueOf(kpis.facturasPendientes));
        subFacturasPendientes.setText(
                formatoDinero(kpis.montoPorCobrar) + " por cobrar");

        modeloDesglose.setRowCount(0);
        List<DesglosePlan> filas = controlador.obtenerDesglosePorPlan();
        for (DesglosePlan fila : filas) {
            modeloDesglose.addRow(new Object[]{
                fila.nombrePlan,
                fila.activas,
                formatoDinero(fila.ingresosMes)
            });
        }

        modeloCobros.setRowCount(0);
        List<CobroMembresiaReciente> cobros
                = controlador.listarUltimosCobrosMembresia(12);
        for (CobroMembresiaReciente cobro : cobros) {
            modeloCobros.addRow(new Object[]{
                cobro.fechaHora == null ? "" : FECHA_HORA.format(cobro.fechaHora),
                cobro.cliente,
                cobro.plan,
                cobro.numeroMembresia,
                cobro.metodoPago,
                formatoDinero(cobro.total)
            });
        }

        String mensajeError = controlador.getMensaje();
        lblEstado.setText(mensajeError == null || mensajeError.isBlank()
                ? "Actualizado " + java.time.LocalDateTime.now()
                        .withNano(0).toString().replace('T', ' ')
                : mensajeError);
    }

    private static String formatoDinero(BigDecimal valor) {
        BigDecimal seguro = valor == null ? BigDecimal.ZERO : valor;
        try {
            return DINERO.format(seguro);
        } catch (RuntimeException ex) {
            return "$" + seguro.toPlainString();
        }
    }
}
