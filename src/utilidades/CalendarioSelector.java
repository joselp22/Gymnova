package utilidades;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Componente reutilizable de calendario para GYMNOVA.
 *
 * Permite convertir los JTextField existentes del proyecto en selectores
 * visuales de fecha o fecha/hora sin tener que rehacer los formularios de
 * NetBeans. El valor escrito mantiene formatos compatibles con la logica
 * existente: yyyy-MM-dd y yyyy-MM-dd HH:mm.
 */
public final class CalendarioSelector {

    private static final String PROP_LISTENER =
            "gymnova.calendario.listener";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private CalendarioSelector() {
    }

    public static void vincularFecha(JTextField campo) {
        vincular(campo, false);
    }

    public static void vincularFechaHora(JTextField campo) {
        vincular(campo, true);
    }

    /** Quita el comportamiento calendario para campos reutilizados como texto. */
    public static void desvincular(JTextField campo) {
        desvincular(campo, true);
    }

    public static void desvincular(JTextField campo, boolean editable) {
        Object previo = campo.getClientProperty(PROP_LISTENER);
        if (previo instanceof MouseAdapter adapter) {
            campo.removeMouseListener(adapter);
        }
        campo.putClientProperty(PROP_LISTENER, null);
        campo.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        campo.setToolTipText(null);
        campo.setEditable(editable);
    }

    private static void vincular(JTextField campo, boolean conHora) {
        Object previo = campo.getClientProperty(PROP_LISTENER);
        if (previo instanceof MouseAdapter adapter) {
            campo.removeMouseListener(adapter);
        }

        campo.setEditable(false);
        campo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        campo.setBackground(Color.WHITE);
        campo.setToolTipText(conHora
                ? "Haz clic para seleccionar fecha y hora en el calendario"
                : "Haz clic para seleccionar una fecha en el calendario");

        MouseAdapter listener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!campo.isEnabled()) {
                    return;
                }
                if (conHora) {
                    LocalDateTime inicial = parseFechaHora(campo.getText());
                    LocalDateTime seleccion = seleccionarFechaHora(campo, inicial);
                    if (seleccion != null) {
                        campo.setText(seleccion.format(FORMATO_FECHA_HORA));
                    }
                } else {
                    LocalDate inicial = parseFecha(campo.getText());
                    LocalDate seleccion = seleccionarFecha(campo, inicial);
                    if (seleccion != null) {
                        campo.setText(seleccion.format(FORMATO_FECHA));
                    }
                }
            }
        };
        campo.addMouseListener(listener);
        campo.putClientProperty(PROP_LISTENER, listener);
    }

    public static LocalDate seleccionarFecha(Component padre, LocalDate inicial) {
        DialogoCalendario dlg = new DialogoCalendario(
                padre, inicial == null ? LocalDate.now() : inicial, null);
        dlg.setVisible(true);
        return dlg.aceptado ? dlg.fechaSeleccionada : null;
    }

    public static LocalDateTime seleccionarFechaHora(
            Component padre, LocalDateTime inicial) {
        LocalDateTime base = inicial == null
                ? LocalDateTime.now().withSecond(0).withNano(0)
                : inicial.withSecond(0).withNano(0);
        DialogoCalendario dlg = new DialogoCalendario(
                padre, base.toLocalDate(), base.toLocalTime());
        dlg.setVisible(true);
        if (!dlg.aceptado) {
            return null;
        }
        return LocalDateTime.of(dlg.fechaSeleccionada, dlg.horaSeleccionada());
    }

    private static LocalDate parseFecha(String texto) {
        try {
            return texto == null || texto.isBlank()
                    ? LocalDate.now()
                    : LocalDate.parse(texto.trim(), FORMATO_FECHA);
        } catch (RuntimeException ex) {
            return LocalDate.now();
        }
    }

    private static LocalDateTime parseFechaHora(String texto) {
        try {
            return texto == null || texto.isBlank()
                    ? LocalDateTime.now().withSecond(0).withNano(0)
                    : LocalDateTime.parse(texto.trim().replace('T', ' '),
                            FORMATO_FECHA_HORA);
        } catch (RuntimeException ex) {
            return LocalDateTime.now().withSecond(0).withNano(0);
        }
    }

    private static final class DialogoCalendario extends JDialog {

        private static final Locale ES = new Locale("es", "ES");
        private static final String[] DIAS = {
            "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"
        };

        private final JComboBox<String> cboMes = new JComboBox<>();
        private final JSpinner spnAnio = new JSpinner(
                new SpinnerNumberModel(LocalDate.now().getYear(), 1900, 2200, 1));
        private final JPanel pnlDias = new JPanel(new GridLayout(7, 7, 3, 3));
        private final JLabel lblSeleccion = new JLabel(" ");
        private final JSpinner spnHora = new JSpinner(
                new SpinnerNumberModel(8, 0, 23, 1));
        private final JSpinner spnMinuto = new JSpinner(
                new SpinnerNumberModel(0, 0, 59, 1));
        private final boolean conHora;

        private LocalDate fechaSeleccionada;
        private YearMonth mesVisible;
        private boolean aceptado;

        DialogoCalendario(Component padre, LocalDate fecha, LocalTime hora) {
            super(ventana(padre), "Seleccionar fecha",
                    ModalityType.APPLICATION_MODAL);
            fechaSeleccionada = fecha;
            mesVisible = YearMonth.from(fecha);
            conHora = hora != null;
            if (hora != null) {
                spnHora.setValue(hora.getHour());
                spnMinuto.setValue(hora.getMinute());
                setTitle("Seleccionar fecha y hora");
            }
            construir();
        }

        private void construir() {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLayout(new BorderLayout(8, 8));
            ((JPanel) getContentPane()).setBorder(
                    BorderFactory.createEmptyBorder(12, 12, 12, 12));

            for (Month mes : Month.values()) {
                String nombre = mes.getDisplayName(TextStyle.FULL, ES);
                cboMes.addItem(capitalizar(nombre));
            }
            cboMes.setSelectedIndex(mesVisible.getMonthValue() - 1);
            spnAnio.setValue(mesVisible.getYear());

            JButton anterior = new JButton("‹");
            JButton siguiente = new JButton("›");
            anterior.setFont(anterior.getFont().deriveFont(Font.BOLD, 20f));
            siguiente.setFont(siguiente.getFont().deriveFont(Font.BOLD, 20f));
            anterior.addActionListener(e -> cambiarMes(-1));
            siguiente.addActionListener(e -> cambiarMes(1));
            cboMes.addActionListener(e -> cambiarDesdeCabecera());
            spnAnio.addChangeListener(e -> cambiarDesdeCabecera());

            JPanel cabecera = new JPanel(new BorderLayout(6, 0));
            cabecera.add(anterior, BorderLayout.WEST);
            JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            centro.add(cboMes);
            centro.add(spnAnio);
            cabecera.add(centro, BorderLayout.CENTER);
            cabecera.add(siguiente, BorderLayout.EAST);
            add(cabecera, BorderLayout.NORTH);

            add(pnlDias, BorderLayout.CENTER);

            JPanel sur = new JPanel(new BorderLayout(6, 6));
            lblSeleccion.setHorizontalAlignment(SwingConstants.CENTER);
            lblSeleccion.setFont(lblSeleccion.getFont().deriveFont(Font.BOLD));
            sur.add(lblSeleccion, BorderLayout.NORTH);

            if (conHora) {
                JPanel hora = new JPanel(new FlowLayout(FlowLayout.CENTER));
                hora.add(new JLabel("Hora:"));
                hora.add(spnHora);
                hora.add(new JLabel(":"));
                hora.add(spnMinuto);
                sur.add(hora, BorderLayout.CENTER);
            }

            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton hoy = new JButton("Hoy");
            JButton cancelar = new JButton("Cancelar");
            JButton aceptar = new JButton("Aceptar");
            hoy.addActionListener(e -> {
                fechaSeleccionada = LocalDate.now();
                mesVisible = YearMonth.from(fechaSeleccionada);
                sincronizarCabecera();
                renderCalendario();
            });
            cancelar.addActionListener(e -> dispose());
            aceptar.addActionListener(e -> {
                aceptado = true;
                dispose();
            });
            acciones.add(hoy);
            acciones.add(cancelar);
            acciones.add(aceptar);
            sur.add(acciones, BorderLayout.SOUTH);
            add(sur, BorderLayout.SOUTH);

            renderCalendario();
            setMinimumSize(new Dimension(430, conHora ? 500 : 455));
            pack();
            setSize(430, conHora ? 500 : 455);
            setLocationRelativeTo(getOwner());
        }

        private void cambiarMes(int cantidad) {
            mesVisible = mesVisible.plusMonths(cantidad);
            sincronizarCabecera();
            renderCalendario();
        }

        private void cambiarDesdeCabecera() {
            if (cboMes.getSelectedIndex() < 0) {
                return;
            }
            int anio = (Integer) spnAnio.getValue();
            mesVisible = YearMonth.of(anio, cboMes.getSelectedIndex() + 1);
            renderCalendario();
        }

        private void sincronizarCabecera() {
            cboMes.setSelectedIndex(mesVisible.getMonthValue() - 1);
            spnAnio.setValue(mesVisible.getYear());
        }

        private void renderCalendario() {
            pnlDias.removeAll();
            for (String dia : DIAS) {
                JLabel l = new JLabel(dia, SwingConstants.CENTER);
                l.setFont(l.getFont().deriveFont(Font.BOLD));
                l.setForeground(new Color(52, 74, 100));
                pnlDias.add(l);
            }

            LocalDate primero = mesVisible.atDay(1);
            int huecos = primero.getDayOfWeek().getValue() - 1;
            for (int i = 0; i < huecos; i++) {
                pnlDias.add(new JLabel(""));
            }

            for (int d = 1; d <= mesVisible.lengthOfMonth(); d++) {
                LocalDate fecha = mesVisible.atDay(d);
                JButton boton = new JButton(String.valueOf(d));
                boton.setFocusPainted(false);
                boton.setMargin(new java.awt.Insets(3, 3, 3, 3));
                if (fecha.equals(fechaSeleccionada)) {
                    boton.setBackground(new Color(8, 124, 255));
                    boton.setForeground(Color.WHITE);
                } else if (fecha.equals(LocalDate.now())) {
                    boton.setBorder(BorderFactory.createLineBorder(
                            new Color(8, 124, 255), 2));
                }
                boton.addActionListener(e -> {
                    fechaSeleccionada = fecha;
                    actualizarTextoSeleccion();
                    renderCalendario();
                });
                boton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() >= 2) {
                            fechaSeleccionada = fecha;
                            aceptado = true;
                            dispose();
                        }
                    }
                });
                pnlDias.add(boton);
            }

            int ocupados = 7 + huecos + mesVisible.lengthOfMonth();
            while (ocupados++ < 49) {
                pnlDias.add(new JLabel(""));
            }
            actualizarTextoSeleccion();
            pnlDias.revalidate();
            pnlDias.repaint();
        }

        private void actualizarTextoSeleccion() {
            DateTimeFormatter f = DateTimeFormatter.ofPattern(
                    "EEEE, dd 'de' MMMM 'de' yyyy", ES);
            lblSeleccion.setText(capitalizar(fechaSeleccionada.format(f)));
        }

        private LocalTime horaSeleccionada() {
            return LocalTime.of((Integer) spnHora.getValue(),
                    (Integer) spnMinuto.getValue());
        }

        private static Window ventana(Component c) {
            return c instanceof Window w ? w : SwingUtilities.getWindowAncestor(c);
        }

        private static String capitalizar(String texto) {
            if (texto == null || texto.isBlank()) {
                return "";
            }
            return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
        }
    }
}
