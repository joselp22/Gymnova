package vista;

import conexion.ConexionPostgreSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Reporte de ocupación: asistencias por día de la semana, uso de cupo
 * en clases grupales, y horarios con mayor concurrencia.
 */
public final class DlgReporteOcupacion extends JDialog {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color AZUL = new Color(8, 124, 255);

    private final JLabel lblTotalMes = kpi();
    private final JLabel lblPromedioDia = kpi();
    private final JLabel lblClasesActivas = kpi();
    private final JLabel lblReservasCanceladas = kpi();

    private final JTable tblDiaSemana = new JTable();
    private final JTable tblHorario = new JTable();
    private final JTable tblClases = new JTable();

    public DlgReporteOcupacion(Frame padre) {
        super(padre, "Reporte de ocupación y clases", true);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(FONDO);
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(14, 14, 14, 14));
        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCentro(), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);
        setSize(new Dimension(1000, 640));
        setLocationRelativeTo(padre);
        cargarDatos();
    }

    private JPanel construirEncabezado() {
        JPanel enc = new JPanel(new BorderLayout(0, 6));
        enc.setBackground(TARJETA);
        enc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JLabel titulo = new JLabel("GYMNOVA · Ocupación y clases");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(TITULO);
        enc.add(titulo, BorderLayout.NORTH);

        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setOpaque(false);
        kpis.add(tarjetaKPI("Asistencias mes", lblTotalMes));
        kpis.add(tarjetaKPI("Promedio diario", lblPromedioDia));
        kpis.add(tarjetaKPI("Clases activas", lblClasesActivas));
        kpis.add(tarjetaKPI("Reservas canceladas", lblReservasCanceladas));
        enc.add(kpis, BorderLayout.CENTER);
        return enc;
    }

    private JTabbedPane construirCentro() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Asistencias por día de la semana", wrap(tblDiaSemana));
        tabs.addTab("Horarios con más ingresos", wrap(tblHorario));
        tabs.addTab("Uso de cupo por clase grupal", wrap(tblClases));
        return tabs;
    }

    private JScrollPane wrap(JTable t) {
        t.setRowHeight(26);
        t.getTableHeader().setBackground(new Color(10, 58, 108));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setReorderingAllowed(false);
        return new JScrollPane(t);
    }

    private JPanel construirPie() {
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pie.setBackground(TARJETA);
        pie.setBorder(BorderFactory.createLineBorder(BORDE));
        JButton imp = boton("Imprimir todo");
        imp.addActionListener(e -> imprimirTodo());
        JButton c = new JButton("Cerrar");
        c.addActionListener(e -> dispose());
        pie.add(imp);
        pie.add(c);
        return pie;
    }

    private void cargarDatos() {
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            int totalMes = queryInt(c,
                    "SELECT COUNT(*) FROM asistencia "
                    + "WHERE DATE_TRUNC('month', fecha_asistencia) = "
                    + "  DATE_TRUNC('month', CURRENT_DATE)");
            lblTotalMes.setText(String.valueOf(totalMes));
            int diasDelMes = queryInt(c,
                    "SELECT EXTRACT(DAY FROM CURRENT_DATE)::int");
            lblPromedioDia.setText(String.format("%.1f",
                    diasDelMes == 0 ? 0.0 : (double) totalMes / diasDelMes));
            lblClasesActivas.setText(String.valueOf(queryInt(c,
                    "SELECT COUNT(*) FROM clase_grupal "
                    + "WHERE estado_clase = TRUE")));
            lblReservasCanceladas.setText(String.valueOf(queryInt(c,
                    "SELECT COUNT(*) FROM reserva "
                    + "WHERE estado_reserva = 'CANCELADA' "
                    + "AND DATE_TRUNC('month', fecha_hora_cancelacion) = "
                    + "  DATE_TRUNC('month', CURRENT_DATE)")));

            DefaultTableModel dia = new DefaultTableModel(
                    new Object[]{"Día de la semana",
                        "Asistencias últimos 30 días",
                        "Barra"}, 0);
            String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves",
                    "Viernes", "Sábado", "Domingo"};
            int max = 1;
            int[] valores = new int[7];
            for (int i = 0; i < 7; i++) {
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT COUNT(*) FROM asistencia "
                        + "WHERE fecha_asistencia >= "
                        + "  CURRENT_DATE - INTERVAL '30 day' "
                        + "AND EXTRACT(ISODOW FROM fecha_asistencia)::int = ?")) {
                    s.setInt(1, i + 1);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        valores[i] = r.getInt(1);
                        if (valores[i] > max) max = valores[i];
                    }
                }
            }
            for (int i = 0; i < 7; i++) {
                int len = valores[i] * 20 / max;
                dia.addRow(new Object[]{dias[i], valores[i],
                    "█".repeat(Math.max(1, len))});
            }
            tblDiaSemana.setModel(dia);

            DefaultTableModel hor = new DefaultTableModel(
                    new Object[]{"Franja horaria", "Ingresos"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT EXTRACT(HOUR FROM hora_entrada)::int || ':00', "
                    + "COUNT(*) FROM asistencia "
                    + "WHERE fecha_asistencia >= "
                    + "  CURRENT_DATE - INTERVAL '30 day' "
                    + "GROUP BY 1 ORDER BY 2 DESC")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        hor.addRow(new Object[]{r.getString(1), r.getInt(2)});
                    }
                }
            }
            tblHorario.setModel(hor);

            DefaultTableModel cla = new DefaultTableModel(
                    new Object[]{"Clase", "Fecha", "Cupo",
                        "Inscritos", "% ocupación", "Canceladas"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT cg.nombre_clase, cg.fecha_hora, cg.cupo_maximo, "
                    + "(SELECT COUNT(*) FROM reserva r "
                    + "  WHERE r.id_clase = cg.id_clase "
                    + "  AND r.estado_reserva = 'ACTIVA'), "
                    + "(SELECT COUNT(*) FROM reserva r "
                    + "  WHERE r.id_clase = cg.id_clase "
                    + "  AND r.estado_reserva = 'CANCELADA') "
                    + "FROM clase_grupal cg "
                    + "WHERE cg.estado_clase = TRUE "
                    + "ORDER BY cg.fecha_hora DESC LIMIT 40")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        int cupo = r.getInt(3);
                        int ins = r.getInt(4);
                        String pct = cupo == 0 ? "0 %"
                                : String.format("%.0f %%",
                                        100.0 * ins / cupo);
                        cla.addRow(new Object[]{r.getString(1),
                            r.getTimestamp(2), cupo, ins, pct, r.getInt(5)});
                    }
                }
            }
            tblClases.setModel(cla);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el reporte: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static int queryInt(Connection c, String sql) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            r.next();
            return r.getInt(1);
        }
    }

    private void imprimirTodo() {
        JTable[] tablas = {tblDiaSemana, tblHorario, tblClases};
        String[] nombres = {"Asistencias por día", "Horarios más concurridos",
                "Uso de cupo en clases"};
        try {
            for (int i = 0; i < tablas.length; i++) {
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

    private JPanel tarjetaKPI(String titulo, JLabel valor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(TARJETA);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("SansSerif", Font.BOLD, 11));
        t.setForeground(new Color(90, 110, 135));
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(valor);
        return p;
    }

    private JLabel kpi() {
        JLabel l = new JLabel("—", SwingConstants.LEFT);
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(AZUL);
        return l;
    }

    private JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(AZUL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return b;
    }
}
