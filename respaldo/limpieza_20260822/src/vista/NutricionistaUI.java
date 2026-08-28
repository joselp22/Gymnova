package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;

/** Utilidades visuales compartidas por las pantallas del Nutricionista. */
final class NutricionistaUI {

    static final Color FONDO = new Color(234, 242, 251);
    static final Color BLANCO = Color.WHITE;
    static final Color AZUL = new Color(8, 124, 255);
    static final Color AZUL_OSCURO = new Color(10, 58, 108);
    static final Color TEXTO = new Color(23, 42, 67);
    static final Color TEXTO_SECUNDARIO = new Color(107, 127, 153);
    static final Color BORDE = new Color(212, 225, 239);
    static final Color VERDE = new Color(34, 139, 94);
    static final Color MORADO = new Color(109, 40, 217);
    static final Color ROJO = new Color(190, 55, 65);

    private NutricionistaUI() {
    }

    static JPanel pagina() {
        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setBackground(FONDO);
        p.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        return p;
    }

    static JPanel tarjeta() {
        JPanel p = new JPanel();
        p.setBackground(BLANCO);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        return p;
    }

    static JPanel encabezado(String titulo, String subtitulo) {
        JPanel p = tarjeta();
        p.setLayout(new BorderLayout(12, 3));
        JLabel t = new JLabel(titulo);
        t.setFont(new Font("SansSerif", Font.BOLD, 25));
        t.setForeground(TEXTO);
        JLabel s = new JLabel(subtitulo == null ? "" : subtitulo);
        s.setFont(new Font("SansSerif", Font.PLAIN, 12));
        s.setForeground(TEXTO_SECUNDARIO);
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new javax.swing.BoxLayout(textos, javax.swing.BoxLayout.Y_AXIS));
        textos.add(t);
        textos.add(javax.swing.Box.createVerticalStrut(4));
        textos.add(s);
        p.add(textos, BorderLayout.CENTER);
        JLabel badge = new JLabel("NUTRICIONISTA", JLabel.CENTER);
        badge.setForeground(AZUL);
        badge.setBorder(BorderFactory.createLineBorder(AZUL));
        badge.setPreferredSize(new Dimension(125, 42));
        p.add(badge, BorderLayout.EAST);
        return p;
    }

    static JPanel bannerCliente() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(new Color(242, 248, 255));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(183, 214, 246)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return p;
    }

    static JLabel tituloSeccion(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setForeground(TEXTO);
        return l;
    }

    static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(TEXTO);
        return l;
    }

    static JTextField campo() {
        JTextField t = new JTextField();
        t.setPreferredSize(new Dimension(180, 36));
        return t;
    }

    static JTextArea area(int filas) {
        JTextArea a = new JTextArea(filas, 20);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return a;
    }

    static JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(AZUL);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(150, 38));
        return b;
    }

    static JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(new Color(235, 241, 248));
        b.setForeground(TEXTO);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(150, 38));
        return b;
    }

    static void tabla(JTable tabla) {
        tabla.setRowHeight(36);
        tabla.setShowGrid(false);
        tabla.setFillsViewportHeight(true);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionBackground(new Color(220, 238, 255));
        tabla.setSelectionForeground(TEXTO);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 38));
        DefaultTableCellRenderer h = new DefaultTableCellRenderer();
        h.setOpaque(true);
        h.setBackground(AZUL_OSCURO);
        h.setForeground(Color.WHITE);
        h.setHorizontalAlignment(JLabel.CENTER);
        h.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.getTableHeader().setDefaultRenderer(h);
    }

    static JScrollPane scrollTabla(JTable tabla) {
        JScrollPane s = new JScrollPane(tabla);
        s.setBorder(BorderFactory.createLineBorder(BORDE));
        return s;
    }

    static JPanel filaBotones(Component... componentes) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);
        for (Component c : componentes) p.add(c);
        return p;
    }
}
