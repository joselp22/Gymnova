package vista;

import conexion.ConexionPostgreSQL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Reporte de desempeño profesional del gimnasio: métricas por entrenador
 * y por nutricionista para evaluar carga y resultados.
 */
public final class DlgReporteDesempeno extends JDialog {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color AZUL = new Color(8, 124, 255);

    private final JTable tblEntrenadores = new JTable();
    private final JTable tblNutricionistas = new JTable();

    public DlgReporteDesempeno(Frame padre) {
        super(padre, "Desempeño profesional", true);
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(FONDO);
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel enc = new JPanel(new BorderLayout());
        enc.setBackground(TARJETA);
        enc.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        JLabel titulo = new JLabel("GYMNOVA · Desempeño profesional");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(TITULO);
        enc.add(titulo, BorderLayout.WEST);
        add(enc, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Entrenadores", wrap(tblEntrenadores));
        tabs.addTab("Nutricionistas", wrap(tblNutricionistas));
        add(tabs, BorderLayout.CENTER);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        pie.setBackground(TARJETA);
        pie.setBorder(BorderFactory.createLineBorder(BORDE));
        JButton imp = boton("Imprimir todo");
        imp.addActionListener(e -> imprimirTodo());
        JButton c = new JButton("Cerrar");
        c.addActionListener(e -> dispose());
        pie.add(imp);
        pie.add(c);
        add(pie, BorderLayout.SOUTH);

        setSize(new Dimension(1000, 620));
        setLocationRelativeTo(padre);
        cargarDatos();
    }

    private JScrollPane wrap(JTable t) {
        t.setRowHeight(26);
        t.getTableHeader().setBackground(new Color(10, 58, 108));
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setReorderingAllowed(false);
        return new JScrollPane(t);
    }

    private void cargarDatos() {
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            DefaultTableModel ent = new DefaultTableModel(
                    new Object[]{"Entrenador", "Rutinas creadas",
                        "Clientes atendidos", "Sesiones registradas",
                        "Evaluaciones", "Clases dictadas"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT TRIM(p.nombres || ' ' || p.apellidos), "
                    + "(SELECT COUNT(*) FROM rutina r "
                    + "  WHERE r.id_entrenador = e.id_persona), "
                    + "(SELECT COUNT(DISTINCT ar.id_cliente) "
                    + "  FROM asignacion_rutina ar "
                    + "  JOIN rutina r ON r.id_rutina = ar.id_rutina "
                    + "  WHERE r.id_entrenador = e.id_persona), "
                    + "(SELECT COUNT(*) FROM progreso_rutina pr "
                    + "  JOIN rutina r ON r.id_rutina = pr.id_rutina "
                    + "  WHERE r.id_entrenador = e.id_persona), "
                    + "(SELECT COUNT(*) FROM evaluacion_fisica ef "
                    + "  WHERE ef.id_entrenador = e.id_persona), "
                    + "(SELECT COUNT(*) FROM clase_grupal cg "
                    + "  WHERE cg.id_entrenador = e.id_persona) "
                    + "FROM entrenador e "
                    + "JOIN persona p ON p.id_persona = e.id_persona "
                    + "WHERE e.estado_entrenador ORDER BY 1")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        ent.addRow(new Object[]{r.getString(1), r.getInt(2),
                            r.getInt(3), r.getInt(4), r.getInt(5),
                            r.getInt(6)});
                    }
                }
            }
            tblEntrenadores.setModel(ent);

            DefaultTableModel nut = new DefaultTableModel(
                    new Object[]{"Nutricionista", "Planes activos",
                        "Planes finalizados", "Clientes con plan",
                        "Comidas registradas"}, 0);
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT TRIM(p.nombres || ' ' || p.apellidos), "
                    + "(SELECT COUNT(*) FROM plan_nutricional pn "
                    + "  WHERE pn.id_nutricionista = n.id_persona "
                    + "  AND pn.estado_plan = 'ACTIVO'), "
                    + "(SELECT COUNT(*) FROM plan_nutricional pn "
                    + "  WHERE pn.id_nutricionista = n.id_persona "
                    + "  AND pn.estado_plan = 'FINALIZADO'), "
                    + "(SELECT COUNT(DISTINCT pn.id_cliente) "
                    + "  FROM plan_nutricional pn "
                    + "  WHERE pn.id_nutricionista = n.id_persona), "
                    + "(SELECT COUNT(*) FROM incluye_alimento ia "
                    + "  JOIN plan_nutricional pn "
                    + "    ON pn.id_plan_nutricional = ia.id_plan_nutricional "
                    + "  WHERE pn.id_nutricionista = n.id_persona) "
                    + "FROM nutricionista n "
                    + "JOIN persona p ON p.id_persona = n.id_persona "
                    + "ORDER BY 1")) {
                try (ResultSet r = s.executeQuery()) {
                    while (r.next()) {
                        nut.addRow(new Object[]{r.getString(1), r.getInt(2),
                            r.getInt(3), r.getInt(4), r.getInt(5)});
                    }
                }
            }
            tblNutricionistas.setModel(nut);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar el reporte: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void imprimirTodo() {
        JTable[] tablas = {tblEntrenadores, tblNutricionistas};
        String[] nombres = {"Desempeño de entrenadores",
                "Desempeño de nutricionistas"};
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

    private JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(AZUL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return b;
    }
}
