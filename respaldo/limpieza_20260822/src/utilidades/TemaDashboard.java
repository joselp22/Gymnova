package utilidades;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.border.LineBorder;

/**
 * Paletas visuales de los cinco paneles de inicio. La estructura de cada
 * pantalla permanece en su archivo .form; esta clase centraliza solamente
 * los colores que cambian segun el rol.
 */
public final class TemaDashboard {

    public enum RolVisual {
        ADMINISTRADOR,
        RECEPCIONISTA,
        ENTRENADOR,
        NUTRICIONISTA,
        CLIENTE
    }

    private TemaDashboard() {
    }

    public static void aplicar(JComponent raiz, RolVisual rol) {
        Paleta paleta = paleta(rol);
        aplicarRecursivo(raiz, paleta, true);
    }

    private static void aplicarRecursivo(
            Component componente,
            Paleta paleta,
            boolean raiz
    ) {
        if (componente instanceof JComponent visual) {
            Color actual = visual.getBackground();

            if (raiz) {
                visual.setBackground(paleta.fondo());
            } else if (esTarjeta(actual)) {
                visual.setBackground(paleta.tarjeta());
                if (!(visual instanceof JTable)
                        && !(visual instanceof JViewport)) {
                    visual.setBorder(new LineBorder(paleta.borde(), 1));
                }
            } else if (esIcono(actual)) {
                visual.setBackground(paleta.icono());
            }

            Color primerPlano = visual.getForeground();
            if (esAzulElectrico(primerPlano)) {
                visual.setForeground(paleta.acento());
            } else if (esTextoPrincipal(primerPlano)) {
                visual.setForeground(paleta.textoPrincipal());
            } else if (esTextoSecundario(primerPlano)) {
                visual.setForeground(paleta.textoSecundario());
            }
        }

        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                aplicarRecursivo(hijo, paleta, false);
            }
        }
    }

    private static boolean esTarjeta(Color color) {
        return color != null && color.equals(new Color(7, 21, 43));
    }

    private static boolean esIcono(Color color) {
        return color != null && color.equals(new Color(9, 38, 76));
    }

    private static boolean esAzulElectrico(Color color) {
        return color != null && color.equals(new Color(8, 124, 255));
    }

    private static boolean esTextoPrincipal(Color color) {
        return color != null && (color.equals(Color.WHITE)
                || color.equals(new Color(205, 220, 239)));
    }

    private static boolean esTextoSecundario(Color color) {
        return color != null && (color.equals(new Color(220, 235, 255))
                || color.equals(new Color(169, 187, 211)));
    }

    private static Paleta paleta(RolVisual rol) {
        return switch (rol) {
            case RECEPCIONISTA -> new Paleta(
                    new Color(4, 18, 36), new Color(8, 36, 70),
                    new Color(0, 181, 255), new Color(4, 65, 110),
                    new Color(0, 209, 255), Color.WHITE,
                    new Color(220, 235, 255));
            case ENTRENADOR -> new Paleta(
                    new Color(244, 247, 252), Color.WHITE,
                    new Color(210, 222, 239), new Color(231, 240, 255),
                    new Color(21, 137, 255), new Color(13, 31, 60),
                    new Color(64, 83, 108));
            case NUTRICIONISTA -> new Paleta(
                    new Color(247, 251, 249), Color.WHITE,
                    new Color(205, 225, 219), new Color(229, 247, 243),
                    new Color(0, 158, 199), new Color(13, 42, 50),
                    new Color(70, 94, 100));
            case CLIENTE -> new Paleta(
                    new Color(2, 7, 20), new Color(6, 26, 55),
                    new Color(0, 108, 255), new Color(7, 54, 104),
                    new Color(0, 153, 255), Color.WHITE,
                    new Color(220, 235, 255));
            default -> new Paleta(
                    new Color(3, 10, 24), new Color(7, 21, 43),
                    new Color(23, 52, 87), new Color(9, 38, 76),
                    new Color(8, 124, 255), Color.WHITE,
                    new Color(220, 235, 255));
        };
    }

    private record Paleta(
            Color fondo,
            Color tarjeta,
            Color borde,
            Color icono,
            Color acento,
            Color textoPrincipal,
            Color textoSecundario
    ) {
    }
}
