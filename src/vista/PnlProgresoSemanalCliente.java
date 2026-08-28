package vista;

import controlador.RutinaProgresoControlador;
import controlador.RutinaProgresoControlador.LineaEjercicio;
import controlador.RutinaProgresoControlador.RutinaDelDia;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import utilidades.SesionUsuario;

/**
 * Vista dedicada del cliente: "Mi progreso" — resumen semanal
 * de cumplimiento de la rutina activa.
 *
 * Muestra una tarjeta por cada día laborable de la semana
 * (LUNES a SABADO) con el número de actividades planificadas,
 * cuántas ha marcado hechas y una barra de porcentaje. Arriba se
 * ofrece el total agregado de la semana.
 */
public final class PnlProgresoSemanalCliente extends JPanel {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color TEXTO_SECUNDARIO = new Color(90, 110, 135);
    private static final Color VERDE = new Color(34, 197, 94);
    private static final Color AZUL = new Color(8, 124, 255);
    private static final Color GRIS_SUAVE = new Color(248, 250, 252);

    private static final String[] DIAS_ES = {
        "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO"
    };

    private final RutinaProgresoControlador controlador =
            new RutinaProgresoControlador();

    private final JLabel lblEncabezadoSemana = new JLabel(" ");
    private final JLabel lblResumenSemana = new JLabel(" ");
    private final JProgressBar barraSemana = new JProgressBar(0, 100);
    private final JPanel pnlDias = new JPanel(new GridLayout(2, 3, 12, 12));

    public PnlProgresoSemanalCliente() {
        setLayout(new BorderLayout(0, 14));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(construirEncabezado(), BorderLayout.NORTH);

        pnlDias.setOpaque(false);
        JScrollPane scroll = new JScrollPane(pnlDias);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);

        refrescarDatos();
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel titulo = new JLabel("Mi progreso semanal");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        titulo.setForeground(TITULO);

        lblEncabezadoSemana.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEncabezadoSemana.setForeground(TEXTO_SECUNDARIO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(lblEncabezadoSemana);
        panel.add(textos, BorderLayout.NORTH);

        lblResumenSemana.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblResumenSemana.setForeground(TITULO);

        barraSemana.setStringPainted(true);
        barraSemana.setPreferredSize(new Dimension(0, 26));
        barraSemana.setForeground(VERDE);

        JPanel resumen = new JPanel(new BorderLayout(0, 6));
        resumen.setOpaque(false);
        resumen.add(lblResumenSemana, BorderLayout.NORTH);
        resumen.add(barraSemana, BorderLayout.CENTER);
        panel.add(resumen, BorderLayout.CENTER);

        return panel;
    }

    /** Recalcula todo (barra global y las 6 tarjetas de día). */
    public void refrescarDatos() {
        if (!SesionUsuario.haySesionActiva()
                || SesionUsuario.getUsuarioActual().getIdPersona() == null) {
            lblEncabezadoSemana.setText(
                    "No se pudo identificar el perfil del cliente.");
            pnlDias.removeAll();
            barraSemana.setValue(0);
            barraSemana.setString("0 %");
            lblResumenSemana.setText("Sin datos disponibles.");
            revalidate();
            repaint();
            return;
        }
        long idCliente = SesionUsuario.getUsuarioActual().getIdPersona();

        LocalDate lunes = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate sabado = lunes.plusDays(5);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                "dd 'de' MMMM", new Locale("es", "ES"));
        lblEncabezadoSemana.setText(
                "Semana del " + capitalizar(lunes.format(fmt))
                + " al " + capitalizar(sabado.format(fmt)) + ".");

        pnlDias.removeAll();
        int totalSemana = 0;
        int hechosSemana = 0;

        for (int i = 0; i < DIAS_ES.length; i++) {
            LocalDate fecha = lunes.plusDays(i);
            RutinaDelDia rutina =
                    controlador.obtenerRutinaEnFecha(idCliente, fecha);
            int total = rutina.ejercicios == null
                    ? 0 : rutina.ejercicios.size();
            int hechos = 0;
            if (rutina.ejercicios != null) {
                for (LineaEjercicio e : rutina.ejercicios) {
                    if (e.hecho) {
                        hechos++;
                    }
                }
            }
            totalSemana += total;
            hechosSemana += hechos;
            pnlDias.add(construirTarjetaDia(
                    DIAS_ES[i], fecha, rutina, total, hechos));
        }

        int porcentajeSemana = totalSemana == 0
                ? 0 : (int) Math.round(100.0 * hechosSemana / totalSemana);
        barraSemana.setValue(porcentajeSemana);
        barraSemana.setString(porcentajeSemana + " %  ("
                + hechosSemana + " / " + totalSemana + ")");
        lblResumenSemana.setText(
                totalSemana == 0
                        ? "Aún no tienes actividades planificadas para esta semana."
                        : "Progreso general: " + hechosSemana
                        + " de " + totalSemana + " actividades hechas.");

        revalidate();
        repaint();
    }

    private JPanel construirTarjetaDia(String dia, LocalDate fecha,
            RutinaDelDia rutina, int total, int hechos) {

        boolean esHoy = fecha.equals(LocalDate.now());
        Color colorBarra = total == 0 ? new Color(148, 163, 184)
                : (hechos == total ? VERDE : AZUL);

        JPanel tarjeta = new JPanel(new BorderLayout(0, 8));
        tarjeta.setBackground(TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(esHoy ? AZUL : BORDE,
                        esHoy ? 2 : 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel lblDia = new JLabel(dia + (esHoy ? "  ·  HOY" : ""));
        lblDia.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDia.setForeground(esHoy ? AZUL : TITULO);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                "dd MMM yyyy", new Locale("es", "ES"));
        JLabel lblFecha = new JLabel(capitalizar(fecha.format(fmt)));
        lblFecha.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblFecha.setForeground(TEXTO_SECUNDARIO);

        JPanel arriba = new JPanel();
        arriba.setOpaque(false);
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        arriba.add(lblDia);
        arriba.add(Box.createVerticalStrut(2));
        arriba.add(lblFecha);
        tarjeta.add(arriba, BorderLayout.NORTH);

        String detalle;
        if (rutina.idRutina == null) {
            detalle = "Sin rutina asignada para esta fecha.";
        } else if (total == 0) {
            detalle = "Día de descanso · " + rutina.nombreRutina;
        } else if (hechos == total) {
            detalle = "¡Completado! · " + rutina.nombreRutina;
        } else {
            detalle = hechos + " de " + total + " actividades · "
                    + rutina.nombreRutina;
        }
        JLabel lblDetalle = new JLabel(
                "<html><div style='width:170px;'>" + escapar(detalle)
                + "</div></html>");
        lblDetalle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblDetalle.setForeground(TEXTO_SECUNDARIO);

        int porcentaje = total == 0 ? 0
                : (int) Math.round(100.0 * hechos / total);
        JProgressBar barra = new JProgressBar(0, 100);
        barra.setValue(porcentaje);
        barra.setStringPainted(true);
        barra.setString(porcentaje + " %");
        barra.setForeground(colorBarra);
        barra.setPreferredSize(new Dimension(0, 22));
        barra.setBackground(GRIS_SUAVE);

        JPanel abajo = new JPanel(new BorderLayout(0, 6));
        abajo.setOpaque(false);
        abajo.add(lblDetalle, BorderLayout.NORTH);
        abajo.add(barra, BorderLayout.SOUTH);
        tarjeta.add(abajo, BorderLayout.CENTER);
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);
        return tarjeta;
    }

    private String capitalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        return Character.toUpperCase(valor.charAt(0)) + valor.substring(1);
    }

    private String escapar(String t) {
        if (t == null) {
            return "";
        }
        return t.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

}
