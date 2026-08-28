package utilidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/** Dibuja una serie semanal real dentro del espacio creado en NetBeans Design. */
public final class GraficoActividad {

    private GraficoActividad() {
    }

    public static void mostrar(
            JLabel etiqueta,
            List<Map<String, Object>> datos
    ) {
        List<Integer> valores = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            valores.add(0);
        }

        if (datos != null) {
            for (int i = 0; i < Math.min(7, datos.size()); i++) {
                Object valor = datos.get(i).get("total");
                if (valor instanceof Number numero) {
                    valores.set(i, numero.intValue());
                }
            }
        }

        int ancho = Math.max(360, etiqueta.getWidth());
        int alto = Math.max(90, etiqueta.getHeight());
        etiqueta.setText("");
        etiqueta.setIcon(new ImageIcon(dibujar(valores, ancho, alto)));
        etiqueta.setToolTipText("Actividad real registrada durante los ultimos siete dias");
    }

    private static BufferedImage dibujar(
            List<Integer> valores,
            int ancho,
            int alto
    ) {
        BufferedImage imagen = new BufferedImage(
                ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagen.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int izquierda = 26;
        int derecha = 20;
        int arriba = 18;
        int abajo = 22;
        int maximo = Math.max(1, valores.stream()
                .mapToInt(Integer::intValue).max().orElse(1));

        g2.setColor(new Color(38, 70, 108));
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 3; i++) {
            int y = arriba + i * (alto - arriba - abajo) / 2;
            g2.drawLine(izquierda, y, ancho - derecha, y);
        }

        int[] xs = new int[7];
        int[] ys = new int[7];
        for (int i = 0; i < 7; i++) {
            xs[i] = izquierda
                    + i * (ancho - izquierda - derecha) / 6;
            ys[i] = alto - abajo
                    - (int) Math.round((alto - arriba - abajo)
                    * (valores.get(i) / (double) maximo));
        }

        g2.setColor(new Color(8, 124, 255, 45));
        int[] areaX = new int[9];
        int[] areaY = new int[9];
        areaX[0] = xs[0];
        areaY[0] = alto - abajo;
        for (int i = 0; i < 7; i++) {
            areaX[i + 1] = xs[i];
            areaY[i + 1] = ys[i];
        }
        areaX[8] = xs[6];
        areaY[8] = alto - abajo;
        g2.fillPolygon(areaX, areaY, 9);

        g2.setColor(new Color(49, 197, 244));
        g2.setStroke(new BasicStroke(3f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < 6; i++) {
            g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }

        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        for (int i = 0; i < 7; i++) {
            g2.setColor(new Color(8, 124, 255));
            g2.fillOval(xs[i] - 5, ys[i] - 5, 10, 10);
            g2.setColor(Color.WHITE);
            g2.fillOval(xs[i] - 2, ys[i] - 2, 4, 4);
            String texto = String.valueOf(valores.get(i));
            int textoAncho = g2.getFontMetrics().stringWidth(texto);
            g2.drawString(texto, xs[i] - textoAncho / 2,
                    Math.max(11, ys[i] - 8));
        }
        g2.dispose();
        return imagen;
    }
}
