package utilidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import modelo.Persona;

/**
 * Presenta la fotografia guardada de una persona o un avatar ilustrado
 * predeterminado segun su sexo.
 */
public final class AvatarPerfil {

    private static final String AVATAR_HOMBRE = "/recursos/avatar_hombre.png";
    private static final String AVATAR_MUJER = "/recursos/avatar_mujer.png";
    private static final String AVATAR_NEUTRO = "/recursos/avatar_neutro.png";

    private AvatarPerfil() {
    }

    public static void mostrar(JLabel etiqueta, Persona persona) {
        String sexo = persona == null ? null : persona.getSexo();
        byte[] fotografia = persona == null ? null : persona.getFotoPerfil();
        mostrar(etiqueta, fotografia, sexo);
    }

    public static void mostrar(JLabel etiqueta, byte[] fotografia, String sexo) {
        int ancho = etiqueta.getWidth() > 8 ? etiqueta.getWidth() : 110;
        int alto = etiqueta.getHeight() > 8 ? etiqueta.getHeight() : 58;
        int lado = Math.max(48, Math.min(ancho, alto));

        BufferedImage origen = leerFotografia(fotografia);
        if (origen == null) {
            origen = leerRecurso(recursoPorSexo(sexo));
        }

        if (origen == null) {
            etiqueta.setIcon(null);
            etiqueta.setText("SIN FOTO");
            return;
        }

        etiqueta.setText("");
        etiqueta.setIcon(new ImageIcon(crearCircular(origen, lado)));
        etiqueta.setToolTipText("Avatar de perfil");
    }

    private static BufferedImage leerFotografia(byte[] datos) {
        if (datos == null || datos.length == 0) {
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(datos));
        } catch (IOException ex) {
            return null;
        }
    }

    private static BufferedImage leerRecurso(String ruta) {
        try {
            return ImageIO.read(AvatarPerfil.class.getResource(ruta));
        } catch (IOException | IllegalArgumentException ex) {
            return null;
        }
    }

    private static String recursoPorSexo(String sexo) {
        if (sexo == null) {
            return AVATAR_NEUTRO;
        }
        String valor = sexo.trim().toUpperCase();
        if (valor.startsWith("M")) {
            return AVATAR_HOMBRE;
        }
        if (valor.startsWith("F")) {
            return AVATAR_MUJER;
        }
        return AVATAR_NEUTRO;
    }

    private static BufferedImage crearCircular(BufferedImage origen, int lado) {
        BufferedImage salida = new BufferedImage(
                lado, lado, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = salida.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Double(2, 2, lado - 4, lado - 4));

        double escala = Math.max(
                (double) lado / origen.getWidth(),
                (double) lado / origen.getHeight());
        int ancho = (int) Math.ceil(origen.getWidth() * escala);
        int alto = (int) Math.ceil(origen.getHeight() * escala);
        int x = (lado - ancho) / 2;
        int y = (lado - alto) / 2;
        Image escalada = origen.getScaledInstance(
                ancho, alto, Image.SCALE_SMOOTH);
        g2.drawImage(escalada, x, y, null);

        g2.setClip(null);
        g2.setColor(new Color(8, 124, 255));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(2, 2, lado - 5, lado - 5);
        g2.dispose();
        return salida;
    }
}
