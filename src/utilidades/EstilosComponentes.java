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

    private EstilosComponentes() {
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
