/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.JComboBox;
/**
 *
 * @author Usuario
 */

public final class EstilosComponentes {

    private static final Color AZUL
            = new Color(8, 124, 255);

    private static final Color AZUL_PRESIONADO
            = new Color(5, 88, 202);

    private static final Color BORDE
            = new Color(160, 182, 207);

    private static final Color ICONO
            = new Color(107, 127, 153);

    /** Color base del fondo azul claro que usan las vistas principales. */
    public static final Color FONDO_AZUL_CLARO = new Color(234, 242, 251);

    /** Color base del encabezado azul oscuro de las tablas. */
    public static final Color ENCABEZADO_TABLA_AZUL = new Color(10, 58, 108);

    /** Color de selección de fila (mismo tono que el resto del sistema). */
    public static final Color SELECCION_TABLA = new Color(220, 238, 255);

    private EstilosComponentes() {
    }

    /**
     * Centra el contenido de todas las columnas de la tabla y aplica el
     * estilo azul consistente con el resto de la aplicación:
     *   * encabezado azul oscuro con texto blanco centrado,
     *   * filas más altas,
     *   * sin líneas de cuadrícula,
     *   * selección con el mismo tono azul claro.
     *
     * Idempotente: se puede llamar varias veces sobre la misma tabla y
     * sobre modelos que se reemplazan (para eso hay que volver a llamar
     * después de sustituir el TableModel).
     */
    public static void centrarTabla(javax.swing.JTable tabla) {
        if (tabla == null) {
            return;
        }
        // Renderer con tooltip: cuando el ancho de la columna es menor que
        // el texto (nombres largos, etc.), se muestra el valor completo al
        // pasar el puntero por encima, evitando que quede oculto.
        javax.swing.table.DefaultTableCellRenderer centro
                = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    javax.swing.JTable t, Object valor,
                    boolean sel, boolean foco, int fila, int col) {
                java.awt.Component c = super.getTableCellRendererComponent(
                        t, valor, sel, foco, fila, col);
                setToolTipText(valor == null ? null : valor.toString());
                return c;
            }
        };
        centro.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        // setDefaultRenderer sobrevive a cambios de modelo (setModel), así
        // los paneles que recargan sus datos mantienen el centrado.
        tabla.setDefaultRenderer(Object.class, centro);
        tabla.setDefaultRenderer(String.class, centro);
        tabla.setDefaultRenderer(Number.class, centro);
        tabla.setDefaultRenderer(Integer.class, centro);
        tabla.setDefaultRenderer(Long.class, centro);
        tabla.setDefaultRenderer(java.math.BigDecimal.class, centro);
        tabla.setDefaultRenderer(java.time.LocalDate.class, centro);
        tabla.setDefaultRenderer(java.time.LocalDateTime.class, centro);
        // También aplicamos a las columnas ya existentes por si alguna
        // ya tenía un renderer específico que no queremos conservar.
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            javax.swing.table.TableCellRenderer actual = tabla
                    .getColumnModel().getColumn(i).getCellRenderer();
            if (actual == null
                    || actual instanceof javax.swing.table.DefaultTableCellRenderer) {
                tabla.getColumnModel().getColumn(i).setCellRenderer(centro);
            }
        }
        tabla.setRowHeight(Math.max(tabla.getRowHeight(), 30));
        tabla.setShowGrid(false);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionBackground(SELECCION_TABLA);
        tabla.setSelectionForeground(new Color(23, 42, 67));

        javax.swing.table.JTableHeader cabecera = tabla.getTableHeader();
        if (cabecera != null) {
            cabecera.setPreferredSize(new java.awt.Dimension(
                    cabecera.getPreferredSize().width, 40));
            cabecera.setReorderingAllowed(false);
            javax.swing.table.DefaultTableCellRenderer estilo
                    = new javax.swing.table.DefaultTableCellRenderer();
            estilo.setOpaque(true);
            estilo.setBackground(ENCABEZADO_TABLA_AZUL);
            estilo.setForeground(Color.WHITE);
            estilo.setFont(new java.awt.Font("SansSerif",
                    java.awt.Font.BOLD, 12));
            estilo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            cabecera.setDefaultRenderer(estilo);
        }
    }

    /**
     * Aplica a un contenedor el fondo azul claro estándar del sistema.
     * Útil para forzar el mismo look en vistas que quedaron con el gris
     * del Look and Feel.
     */
    public static void aplicarFondoAzul(JComponent panel) {
        if (panel == null) {
            return;
        }
        panel.setBackground(FONDO_AZUL_CLARO);
        panel.setOpaque(true);
    }

    /**
     * Recorre recursivamente el contenedor y aplica el tema azul y el
     * centrado de tablas a todos los componentes que encuentre. Se usa
     * desde FrmPrincipal cada vez que se muestra una vista para
     * uniformar el look sin tocar cada .form individualmente.
     */
    public static void uniformarLookAzul(java.awt.Component raiz) {
        if (raiz == null) {
            return;
        }
        if (raiz instanceof javax.swing.JTable tabla) {
            centrarTabla(tabla);
        }
        if (raiz instanceof javax.swing.JScrollPane sp) {
            sp.setBorder(sp.getBorder() == null
                    ? javax.swing.BorderFactory.createLineBorder(
                            new Color(212, 225, 239))
                    : sp.getBorder());
            if (sp.getViewport() != null
                    && sp.getViewport().getView() instanceof javax.swing.JTable) {
                sp.getViewport().setBackground(Color.WHITE);
            }
        }
        // Solo repintamos el fondo azul si el componente sigue con el
        // color por defecto del look (gris claro del Nimbus/system). Así
        // no aplastamos paneles que ya definieron sus propios colores.
        if (raiz instanceof javax.swing.JPanel panel) {
            Color actual = panel.getBackground();
            if (esColorPorDefecto(actual)) {
                panel.setBackground(FONDO_AZUL_CLARO);
            }
        }
        if (raiz instanceof java.awt.Container c) {
            for (java.awt.Component hijo : c.getComponents()) {
                uniformarLookAzul(hijo);
            }
        }
    }

    private static boolean esColorPorDefecto(Color c) {
        if (c == null) {
            return true;
        }
        // Gris típico de Metal/Nimbus/System sin tocar.
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        return (r == g && g == b) // grises puros
                || (Math.abs(r - g) < 10 && Math.abs(g - b) < 10
                    && r >= 200 && r <= 245); // grises casi-neutros
    }

    public static void aplicarCampoUsuario(
            JTextField campo
    ) {
        aplicarCampo(campo, TipoIcono.USUARIO);
    }

    public static void aplicarCampoContrasena(
            JPasswordField campo
    ) {
        aplicarCampo(campo, TipoIcono.CANDADO);
    }
    
    public static void aplicarCampoSimple(
        JTextField campo
) {
    aplicarCampo(campo, null);
}

public static void aplicarComboRedondeado(
        JComboBox<?> combo
) {
    combo.setBorder(
            new BordeCampoRedondeado(null)
    );

    combo.setBackground(Color.WHITE);
}

    private static void aplicarCampo(
            JTextField campo,
            TipoIcono tipoIcono
    ) {
        campo.setBorder(
                new BordeCampoRedondeado(tipoIcono)
        );

        campo.setBackground(Color.WHITE);
        campo.setForeground(new Color(23, 42, 67));
        campo.setCaretColor(AZUL);

        campo.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                campo.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.repaint();
            }
        });
    }

    public static void aplicarBotonPrimario(
            JButton boton
    ) {
        boton.setUI(new BotonRedondeadoUI());
        boton.setBackground(AZUL);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setOpaque(false);
    }

    private enum TipoIcono {
        USUARIO,
        CANDADO
    }

    private static class BordeCampoRedondeado
            extends AbstractBorder {

        private final TipoIcono tipoIcono;

        BordeCampoRedondeado(TipoIcono tipoIcono) {
            this.tipoIcono = tipoIcono;
        }

        @Override
public Insets getBorderInsets(Component componente) {

    int espacioIzquierdo
            = tipoIcono == null ? 14 : 46;

    return new Insets(
            8,
            espacioIzquierdo,
            8,
            14
    );
}

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(
                Component componente,
                Graphics graphics,
                int x,
                int y,
                int ancho,
                int alto
        ) {
            Graphics2D g2
                    = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color colorBorde = componente.hasFocus()
                    ? AZUL
                    : BORDE;

            g2.setColor(colorBorde);
            g2.setStroke(new BasicStroke(
                    componente.hasFocus() ? 2f : 1.4f
            ));

            g2.drawRoundRect(
                    x + 1,
                    y + 1,
                    ancho - 3,
                    alto - 3,
                    24,
                    24
            );

            g2.setColor(ICONO);
            g2.setStroke(new BasicStroke(1.8f));

            int centroY = y + (alto / 2);

            if (tipoIcono == TipoIcono.USUARIO) {

            dibujarUsuario(
                    g2,
                    x + 15,
                    centroY
            );

        } else if (tipoIcono == TipoIcono.CANDADO) {

            dibujarCandado(
                    g2,
                    x + 15,
                    centroY
            );
        }

            g2.dispose();
        }

        private void dibujarUsuario(
                Graphics2D g2,
                int x,
                int centroY
        ) {
            g2.drawOval(
                    x + 5,
                    centroY - 12,
                    10,
                    10
            );

            g2.drawArc(
                    x,
                    centroY,
                    20,
                    15,
                    0,
                    180
            );
        }

        private void dibujarCandado(
                Graphics2D g2,
                int x,
                int centroY
        ) {
            g2.drawArc(
                    x + 4,
                    centroY - 12,
                    12,
                    14,
                    0,
                    180
            );

            g2.drawRoundRect(
                    x + 1,
                    centroY - 4,
                    18,
                    15,
                    4,
                    4
            );

            g2.fillOval(
                    x + 9,
                    centroY + 1,
                    3,
                    5
            );
        }
    }
    
    public static void aplicarBotonAccion(
        JButton boton,
        Color color
) {
    boton.setUI(new BotonRedondeadoUI());
    boton.setBackground(color);
    boton.setForeground(Color.WHITE);
    boton.setFocusPainted(false);
    boton.setContentAreaFilled(false);
    boton.setBorderPainted(false);
    boton.setOpaque(false);
}

    private static class BotonRedondeadoUI
            extends BasicButtonUI {

        @Override
        public void paint(
                Graphics graphics,
                JComponent componente
        ) {
            AbstractButton boton
                    = (AbstractButton) componente;

            Graphics2D g2
                    = (Graphics2D) graphics.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color color = boton.getModel().isPressed()
            ? boton.getBackground().darker()
            : boton.getBackground();

            g2.setColor(color);

            g2.fillRoundRect(
                    0,
                    0,
                    componente.getWidth(),
                    componente.getHeight(),
                    26,
                    26
            );

            g2.dispose();

            super.paint(graphics, componente);
        }
    }
    
    public static void aplicarBotonContorno(
        JButton boton,
        Color color
) {
    boton.setUI(new BotonContornoUI(color));
    boton.setBackground(Color.WHITE);
    boton.setForeground(color);
    boton.setFocusPainted(false);
    boton.setContentAreaFilled(false);
    boton.setBorderPainted(false);
    boton.setOpaque(false);
    boton.setRolloverEnabled(true);
}

private static class BotonContornoUI
        extends BasicButtonUI {

    private final Color colorBorde;

    BotonContornoUI(Color colorBorde) {
        this.colorBorde = colorBorde;
    }

    @Override
    public void paint(
            Graphics graphics,
            JComponent componente
    ) {
        AbstractButton boton
                = (AbstractButton) componente;

        Graphics2D g2
                = (Graphics2D) graphics.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        Color colorActual = boton.isEnabled()
                ? colorBorde
                : new Color(148, 163, 184);

        if (boton.getModel().isPressed()) {

            g2.setColor(new Color(
                    colorActual.getRed(),
                    colorActual.getGreen(),
                    colorActual.getBlue(),
                    45
            ));

            g2.fillRoundRect(
                    1,
                    1,
                    componente.getWidth() - 3,
                    componente.getHeight() - 3,
                    26,
                    26
            );

        } else if (boton.getModel().isRollover()) {

            g2.setColor(new Color(
                    colorActual.getRed(),
                    colorActual.getGreen(),
                    colorActual.getBlue(),
                    20
            ));

            g2.fillRoundRect(
                    1,
                    1,
                    componente.getWidth() - 3,
                    componente.getHeight() - 3,
                    26,
                    26
            );
        }

        g2.setColor(colorActual);
        g2.setStroke(new BasicStroke(1.8f));

        g2.drawRoundRect(
                1,
                1,
                componente.getWidth() - 3,
                componente.getHeight() - 3,
                26,
                26
        );

        g2.dispose();

        super.paint(graphics, componente);
    }
}

public static void aplicarBotonPremium(
        JButton boton,
        Color colorInicial,
        Color colorFinal,
        Color colorTexto
) {
    boton.setUI(
            new BotonPremiumUI(
                    colorInicial,
                    colorFinal
            )
    );

    boton.setBackground(colorInicial);
    boton.setForeground(colorTexto);

    boton.setFont(
            new java.awt.Font(
                    "SansSerif",
                    java.awt.Font.BOLD,
                    12
            )
    );

    boton.setFocusPainted(false);
    boton.setContentAreaFilled(false);
    boton.setBorderPainted(false);
    boton.setOpaque(false);
    boton.setRolloverEnabled(true);

    boton.setCursor(
            new java.awt.Cursor(
                    java.awt.Cursor.HAND_CURSOR
            )
    );

    boton.setBorder(
            javax.swing.BorderFactory
                    .createEmptyBorder(
                            0,
                            14,
                            4,
                            14
                    )
    );
}

private static class BotonPremiumUI
        extends BasicButtonUI {

    private final Color colorInicial;
    private final Color colorFinal;

    BotonPremiumUI(
            Color colorInicial,
            Color colorFinal
    ) {
        this.colorInicial = colorInicial;
        this.colorFinal = colorFinal;
    }

    @Override
    public void paint(
            Graphics graphics,
            JComponent componente
    ) {
        AbstractButton boton
                = (AbstractButton) componente;

        Graphics2D g2
                = (Graphics2D) graphics.create();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int ancho = componente.getWidth();
        int alto = componente.getHeight() - 4;

        Color inicio = colorInicial;
        Color fin = colorFinal;

        if (!boton.isEnabled()) {

            inicio = new Color(148, 163, 184);
            fin = new Color(100, 116, 139);

        } else if (boton.getModel().isPressed()) {

            inicio = colorInicial.darker();
            fin = colorFinal.darker();

        } else if (boton.getModel().isRollover()) {

            inicio = aclarar(colorInicial, 18);
            fin = aclarar(colorFinal, 18);
        }

        // Sombra inferior
        g2.setColor(
                new Color(
                        15,
                        23,
                        42,
                        boton.isEnabled() ? 55 : 20
                )
        );

        g2.fillRoundRect(
                2,
                4,
                ancho - 4,
                alto,
                24,
                24
        );

        // Degradado principal
        g2.setPaint(
                new java.awt.GradientPaint(
                        0,
                        0,
                        inicio,
                        ancho,
                        alto,
                        fin
                )
        );

        g2.fillRoundRect(
                1,
                0,
                ancho - 3,
                alto,
                24,
                24
        );

        // Brillo al pasar el cursor
        if (boton.getModel().isRollover()
                && boton.isEnabled()) {

            g2.setColor(
                    new Color(255, 255, 255, 30)
            );

            g2.fillRoundRect(
                    1,
                    0,
                    ancho - 3,
                    alto / 2,
                    24,
                    24
            );
        }

        g2.dispose();

        super.paint(graphics, componente);
    }

    private Color aclarar(
            Color color,
            int cantidad
    ) {
        return new Color(
                Math.min(
                        255,
                        color.getRed() + cantidad
                ),
                Math.min(
                        255,
                        color.getGreen() + cantidad
                ),
                Math.min(
                        255,
                        color.getBlue() + cantidad
                )
        );
    }
}

}
