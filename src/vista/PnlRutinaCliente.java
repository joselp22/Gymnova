package vista;

import controlador.RutinaProgresoControlador;
import controlador.RutinaProgresoControlador.LineaEjercicio;
import controlador.RutinaProgresoControlador.RutinaDelDia;
import controlador.RutinaProgresoControlador.RutinaAsignadaCliente;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import utilidades.CalendarioSelector;
import utilidades.SesionUsuario;

/**
 * Vista del cliente para ejecutar su rutina por fecha real.
 *
 * La rutina creada por el entrenador sigue siendo una plantilla semanal
 * (LUNES, MARTES, ...). La fecha seleccionada se transforma al dia de semana
 * correspondiente y se muestran solamente las actividades de ese dia.
 * Cada actividad se presenta como una tarjeta y puede marcarse como realizada.
 */
public final class PnlRutinaCliente extends JPanel {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color TITULO = new Color(23, 42, 67);
    private static final Color TEXTO_SECUNDARIO = new Color(90, 110, 135);
    private static final Color AZUL = new Color(8, 124, 255);
    private static final Color VERDE = new Color(34, 197, 94);
    private static final Color VERDE_SUAVE = new Color(236, 253, 245);
    private static final Color GRIS_SUAVE = new Color(248, 250, 252);
    private static final Color GRIS = new Color(100, 116, 139);

    private final RutinaProgresoControlador controlador
            = new RutinaProgresoControlador();

    private final JLabel lblTitulo = new JLabel("Mi rutina");
    private final JLabel lblSubtitulo = new JLabel(" ");
    private final JLabel lblEntrenador = new JLabel(" ");
    private final JLabel lblDia = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel lblResumen = new JLabel(" ");
    private final JComboBox<RutinaAsignadaCliente> cboRutina = new JComboBox<>();
    private final JTextField txtFechaConsulta = new JTextField(10);
    private final JProgressBar barra = new JProgressBar(0, 100);
    private final JPanel pnlTarjetas = new JPanel(new GridBagLayout());
    private final JScrollPane scrollTarjetas = new JScrollPane(pnlTarjetas);

    private boolean refrescando;
    private boolean fechaInicialAjustada;
    private boolean cargandoRutinas;

    public PnlRutinaCliente() {
        txtFechaConsulta.setText(LocalDate.now().toString());
        CalendarioSelector.vincularFecha(txtFechaConsulta);
        txtFechaConsulta.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refrescarDesdeCampo();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                // Se espera el insert posterior al seleccionar otra fecha.
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refrescarDesdeCampo();
            }
        });

        cboRutina.addActionListener(e -> {
            if (!cargandoRutinas) {
                fechaInicialAjustada = false;
                refrescarDatos();
            }
        });

        setLayout(new BorderLayout(0, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirCuerpo(), BorderLayout.CENTER);
        refrescarDatos();
    }

    private JPanel construirEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout(12, 0));
        encabezado.setBackground(TARJETA);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(TITULO);
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitulo.setForeground(TEXTO_SECUNDARIO);
        lblEntrenador.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEntrenador.setForeground(TEXTO_SECUNDARIO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(lblTitulo);
        textos.add(Box.createVerticalStrut(3));
        textos.add(lblSubtitulo);
        textos.add(Box.createVerticalStrut(2));
        textos.add(lblEntrenador);
        encabezado.add(textos, BorderLayout.WEST);

        JButton btnAnterior = new JButton("‹");
        JButton btnHoy = new JButton("Hoy");
        JButton btnSiguiente = new JButton("›");
        JButton btnProxima = new JButton("Próxima actividad");
        JButton btnActualizar = botonPrincipal("Actualizar");
        btnAnterior.addActionListener(e -> moverFecha(-1));
        btnSiguiente.addActionListener(e -> moverFecha(1));
        btnHoy.addActionListener(e -> establecerFecha(LocalDate.now()));
        btnProxima.addActionListener(e -> irProximaActividad());
        btnActualizar.addActionListener(e -> refrescarDatos());

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        selector.setOpaque(false);
        selector.add(new JLabel("Rutina:"));
        cboRutina.setPreferredSize(new Dimension(220, 26));
        selector.add(cboRutina);
        selector.add(Box.createHorizontalStrut(8));
        selector.add(new JLabel("Fecha:"));
        selector.add(btnAnterior);
        selector.add(txtFechaConsulta);
        selector.add(btnSiguiente);
        selector.add(btnHoy);
        selector.add(btnProxima);
        selector.add(btnActualizar);
        encabezado.add(selector, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel construirCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.setOpaque(false);
        superior.add(construirDiaSeleccionado(), BorderLayout.NORTH);
        superior.add(construirResumen(), BorderLayout.SOUTH);
        cuerpo.add(superior, BorderLayout.NORTH);

        pnlTarjetas.setBackground(FONDO);
        pnlTarjetas.setBorder(BorderFactory.createEmptyBorder(2, 2, 16, 2));

        scrollTarjetas.setBorder(null);
        scrollTarjetas.getViewport().setBackground(FONDO);
        scrollTarjetas.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollTarjetas.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTarjetas.getVerticalScrollBar().setUnitIncrement(18);
        cuerpo.add(scrollTarjetas, BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel construirDiaSeleccionado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(10, 58, 108));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        lblDia.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblDia.setForeground(Color.WHITE);
        panel.add(lblDia, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirResumen() {
        JPanel resumen = new JPanel(new BorderLayout(12, 6));
        resumen.setBackground(TARJETA);
        resumen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));
        lblResumen.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblResumen.setForeground(TITULO);
        barra.setStringPainted(true);
        barra.setPreferredSize(new Dimension(0, 24));
        resumen.add(lblResumen, BorderLayout.NORTH);
        resumen.add(barra, BorderLayout.CENTER);
        return resumen;
    }

    private void cargarRutinasCliente(long idCliente) {
        Long seleccionAnterior = null;
        Object actual = cboRutina.getSelectedItem();
        if (actual instanceof RutinaAsignadaCliente r) seleccionAnterior = r.idAsignacion;
        java.util.List<RutinaAsignadaCliente> rutinas = controlador.listarRutinasActivasCliente(idCliente);
        cargandoRutinas = true;
        try {
            DefaultComboBoxModel<RutinaAsignadaCliente> m = new DefaultComboBoxModel<>();
            RutinaAsignadaCliente volver = null;
            for (RutinaAsignadaCliente r : rutinas) {
                m.addElement(r);
                if (seleccionAnterior != null && seleccionAnterior == r.idAsignacion) volver = r;
            }
            cboRutina.setModel(m);
            if (volver != null) cboRutina.setSelectedItem(volver);
        } finally {
            cargandoRutinas = false;
        }
    }

    public void refrescarDatos() {
        if (refrescando || !SesionUsuario.haySesionActiva()) {
            return;
        }
        Long idCliente = SesionUsuario.getUsuarioActual().getIdPersona();
        if (idCliente == null) {
            lblSubtitulo.setText("La cuenta no tiene un perfil de cliente.");
            mostrarMensajeCentral("No se pudo identificar el perfil del cliente.");
            return;
        }

        cargarRutinasCliente(idCliente);
        RutinaAsignadaCliente seleccionada = (RutinaAsignadaCliente) cboRutina.getSelectedItem();
        if (seleccionada == null) {
            lblTitulo.setText("Mi rutina");
            lblSubtitulo.setText("No tienes rutinas activas asignadas.");
            lblEntrenador.setText(" ");
            limpiarTarjetas();
            lblResumen.setText("Pide a tu entrenador que te asigne una rutina.");
            barra.setValue(0);
            barra.setString("0 %");
            mostrarMensajeCentral("No hay rutinas activas disponibles.");
            return;
        }

        /*
         * Al entrar por primera vez, si hoy es día de descanso o la rutina
         * comienza próximamente, mostramos automáticamente la siguiente fecha
         * que sí tiene actividades. Así el cliente no interpreta una pantalla
         * vacía como que la asignación falló.
         */
        if (!fechaInicialAjustada) {
            fechaInicialAjustada = true;
            LocalDate actual = fechaSeleccionada();
            RutinaDelDia hoy = controlador.obtenerRutinaAsignadaEnFecha(
                    idCliente, seleccionada.idAsignacion, actual);
            if (hoy.idRutina == null || hoy.ejercicios.isEmpty()) {
                LocalDate siguiente = controlador.buscarProximaFechaConActividad(
                        idCliente, seleccionada.idAsignacion, actual.minusDays(1));
                if (siguiente != null) {
                    txtFechaConsulta.setText(siguiente.toString());
                }
            }
        }

        refrescando = true;
        try {
            LocalDate fecha = fechaSeleccionada();
            String dia = nombreDia(fecha);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                    "dd 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
            String fechaBonita = capitalizar(fecha.format(fmt));
            lblDia.setText(dia.toUpperCase() + "  ·  " + fechaBonita);
            lblSubtitulo.setText("Las actividades cambian automáticamente según la fecha seleccionada.");

            RutinaDelDia rutina = controlador.obtenerRutinaAsignadaEnFecha(
                    idCliente, seleccionada.idAsignacion, fecha);
            limpiarTarjetas();

            if (rutina.idRutina == null) {
                lblTitulo.setText("Mi rutina");
                lblEntrenador.setText(" ");
                lblResumen.setText("No tienes una rutina asignada para esta fecha.");
                barra.setValue(0);
                barra.setString("0 %");
                mostrarMensajeCentral(
                        "No hay una rutina activa dentro del período de esta fecha.");
                return;
            }

            lblTitulo.setText("Rutina: " + rutina.nombreRutina
                    + " (" + rutina.nivel + ")");
            String entrenador = rutina.entrenador == null
                    || rutina.entrenador.isBlank()
                    ? "Entrenador no especificado"
                    : "Entrenador: " + rutina.entrenador;
            String diasProgramados = rutina.diasPlanificados.isEmpty()
                    ? "sin días configurados"
                    : String.join(", ", rutina.diasPlanificados);
            String estado = rutina.estadoAsignacion == null
                    ? "" : " · Estado: " + rutina.estadoAsignacion;
            lblEntrenador.setText(entrenador
                    + " · Días: " + diasProgramados + estado);

            List<LineaEjercicio> ejercicios = rutina.ejercicios;
            if (ejercicios.isEmpty()) {
                lblResumen.setText("Sin actividades para " + dia
                        + ". Días programados: " + diasProgramados + ".");
                barra.setValue(0);
                barra.setString("Día sin actividades");
                mostrarMensajeCentral(
                        "No hay ejercicios para " + dia.toUpperCase()
                        + ". Usa el calendario o «Próxima actividad». "
                        + "Días configurados: " + diasProgramados + ".");
                return;
            }

            if (rutina.advertenciaProgreso != null
                    && !rutina.advertenciaProgreso.isBlank()) {
                lblSubtitulo.setText(rutina.advertenciaProgreso);
            }

            boolean futura = fecha.isAfter(LocalDate.now());
            agregarTarjetas(ejercicios, idCliente, seleccionada.idAsignacion,
                    fecha, futura, rutina.puedeRegistrar, rutina.estadoAsignacion);
            recalcularBarra(ejercicios, futura, rutina.estadoAsignacion);
            pnlTarjetas.revalidate();
            pnlTarjetas.repaint();
            javax.swing.SwingUtilities.invokeLater(() ->
                    scrollTarjetas.getVerticalScrollBar().setValue(0));
        } finally {
            refrescando = false;
        }
    }

    private void agregarTarjetas(List<LineaEjercicio> ejercicios,
            long idCliente, long idAsignacion, LocalDate fecha, boolean futura,
            boolean puedeRegistrar, String estadoAsignacion) {
        final int columnas = 3;
        for (int i = 0; i < ejercicios.size(); i++) {
            LineaEjercicio ejercicio = ejercicios.get(i);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i % columnas;
            gbc.gridy = i / columnas;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets = new Insets(6, 6, 6, 6);
            pnlTarjetas.add(
                    construirTarjeta(ejercicio, idCliente, idAsignacion, fecha, futura,
                            puedeRegistrar, estadoAsignacion), gbc);
        }

        // El relleno conserva el aspecto de tarjeta/cuadrado en pantallas amplias.
        GridBagConstraints rellenoHorizontal = new GridBagConstraints();
        rellenoHorizontal.gridx = columnas;
        rellenoHorizontal.gridy = 0;
        rellenoHorizontal.weightx = 1.0;
        rellenoHorizontal.fill = GridBagConstraints.HORIZONTAL;
        pnlTarjetas.add(Box.createHorizontalGlue(), rellenoHorizontal);

        GridBagConstraints rellenoVertical = new GridBagConstraints();
        rellenoVertical.gridx = 0;
        rellenoVertical.gridy = (ejercicios.size() + columnas - 1) / columnas;
        rellenoVertical.gridwidth = columnas + 1;
        rellenoVertical.weighty = 1.0;
        rellenoVertical.fill = GridBagConstraints.VERTICAL;
        pnlTarjetas.add(Box.createVerticalGlue(), rellenoVertical);
    }

    private JPanel construirTarjeta(LineaEjercicio ejercicio,
            long idCliente, long idAsignacion, LocalDate fecha, boolean futura,
            boolean puedeRegistrar, String estadoAsignacion) {
        boolean realizada = ejercicio.hecho;
        JPanel tarjeta = new JPanel(new BorderLayout(0, 10));
        tarjeta.setBackground(realizada ? VERDE_SUAVE : TARJETA);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(realizada ? VERDE : BORDE, realizada ? 2 : 1),
                BorderFactory.createEmptyBorder(16, 16, 14, 16)));
        tarjeta.setPreferredSize(new Dimension(285, 220));
        tarjeta.setMinimumSize(new Dimension(245, 205));

        JLabel orden = new JLabel("ACTIVIDAD " + ejercicio.orden);
        orden.setFont(new Font("SansSerif", Font.BOLD, 11));
        orden.setForeground(realizada ? new Color(22, 101, 52) : GRIS);

        JLabel nombre = new JLabel(htmlCentrado(ejercicio.nombreEjercicio));
        nombre.setHorizontalAlignment(SwingConstants.CENTER);
        nombre.setFont(new Font("SansSerif", Font.BOLD, 18));
        nombre.setForeground(TITULO);

        JPanel arriba = new JPanel();
        arriba.setOpaque(false);
        arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
        orden.setAlignmentX(Component.CENTER_ALIGNMENT);
        nombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        arriba.add(orden);
        arriba.add(Box.createVerticalStrut(7));
        arriba.add(nombre);
        tarjeta.add(arriba, BorderLayout.NORTH);

        JPanel datos = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        datos.setOpaque(false);
        datos.add(datoDestacado("SERIES", String.valueOf(ejercicio.series)));
        datos.add(datoDestacado("REPETICIONES", ejercicio.repeticiones));
        tarjeta.add(datos, BorderLayout.CENTER);

        String extra = construirDetalleOpcional(ejercicio);
        JLabel lblExtra = new JLabel(extra, SwingConstants.CENTER);
        lblExtra.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblExtra.setForeground(TEXTO_SECUNDARIO);

        JButton btnAccion;
        if (!puedeRegistrar) {
            String texto = futura || "PROGRAMADA".equals(estadoAsignacion)
                    ? "Programada"
                    : "PAUSADA".equals(estadoAsignacion)
                    ? "Rutina pausada"
                    : "Histórico";
            btnAccion = new JButton(texto);
            btnAccion.setEnabled(false);
        } else if (realizada) {
            btnAccion = new JButton("✓ HECHO");
            btnAccion.setToolTipText("Haz clic si necesitas desmarcar esta actividad.");
            btnAccion.addActionListener(e -> desmarcarActividad(
                    ejercicio.idRutinaEjercicio, idCliente, idAsignacion, fecha));
        } else {
            btnAccion = botonPrincipal("Marcar como hecho");
            btnAccion.addActionListener(e -> marcarActividad(
                    ejercicio.idRutinaEjercicio, idCliente, idAsignacion, fecha));
        }
        btnAccion.setPreferredSize(new Dimension(0, 38));

        JPanel abajo = new JPanel(new BorderLayout(0, 7));
        abajo.setOpaque(false);
        abajo.add(lblExtra, BorderLayout.NORTH);
        abajo.add(btnAccion, BorderLayout.SOUTH);
        tarjeta.add(abajo, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel datoDestacado(String titulo, String valor) {
        JPanel panel = new JPanel();
        panel.setBackground(GRIS_SUAVE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel lTitulo = new JLabel(titulo);
        lTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lTitulo.setFont(new Font("SansSerif", Font.BOLD, 10));
        lTitulo.setForeground(GRIS);

        JLabel lValor = new JLabel(valor == null || valor.isBlank() ? "-" : valor);
        lValor.setAlignmentX(Component.CENTER_ALIGNMENT);
        lValor.setFont(new Font("SansSerif", Font.BOLD, 20));
        lValor.setForeground(TITULO);

        panel.add(lTitulo);
        panel.add(Box.createVerticalStrut(4));
        panel.add(lValor);
        return panel;
    }

    private String construirDetalleOpcional(LineaEjercicio e) {
        java.util.List<String> partes = new java.util.ArrayList<>();
        if (e.pesoSugerido != null && !e.pesoSugerido.isBlank()) {
            partes.add("Peso: " + e.pesoSugerido);
        }
        if (e.duracionMinutos != null) {
            partes.add("Duración: " + e.duracionMinutos + " min");
        }
        if (e.descansoSegundos != null) {
            partes.add("Descanso: " + e.descansoSegundos + " s");
        }
        return partes.isEmpty() ? " " : String.join("  ·  ", partes);
    }

    private void marcarActividad(long idRutinaEjercicio,
            long idCliente, long idAsignacion, LocalDate fecha) {
        if (fecha.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this,
                    "Esta actividad corresponde a una fecha futura.",
                    "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        boolean ok = controlador.marcarHecho(
                idRutinaEjercicio, idCliente, idAsignacion, fecha);
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    controlador.getMensaje(), "GYMNOVA",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        refrescarDatos();
    }

    private void desmarcarActividad(long idRutinaEjercicio,
            long idCliente, long idAsignacion, LocalDate fecha) {
        int opcion = JOptionPane.showConfirmDialog(
                this,
                "La actividad ya está marcada como hecha.\n"
                + "¿Deseas volver a dejarla pendiente?",
                "Corregir registro",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        boolean ok = controlador.desmarcarHecho(
                idRutinaEjercicio, idCliente, idAsignacion, fecha);
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    controlador.getMensaje(), "GYMNOVA",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        refrescarDatos();
    }

    private void recalcularBarra(List<LineaEjercicio> ejercicios,
            boolean futura, String estadoAsignacion) {
        int total = ejercicios.size();
        int realizados = 0;
        for (LineaEjercicio e : ejercicios) {
            if (e.hecho) {
                realizados++;
            }
        }
        int porcentaje = total == 0 ? 0
                : (int) Math.round(100.0 * realizados / total);
        barra.setValue(porcentaje);
        barra.setString(porcentaje + " %  (" + realizados + " / " + total + ")");
        if (futura || "PROGRAMADA".equals(estadoAsignacion)) {
            lblResumen.setText("Rutina programada: " + total
                    + " actividades para esta fecha.");
        } else if ("PAUSADA".equals(estadoAsignacion)) {
            lblResumen.setText("Rutina pausada. Puedes consultar las actividades, "
                    + "pero no registrar progreso.");
        } else if ("FINALIZADA".equals(estadoAsignacion)) {
            lblResumen.setText("Historial de rutina finalizada.");
        } else if (realizados == total) {
            lblResumen.setText("Rutina del día completada. Todas las actividades están hechas.");
        } else {
            lblResumen.setText("Progreso del día: " + realizados + " de "
                    + total + " actividades hechas.");
        }
    }

    private void mostrarMensajeCentral(String mensaje) {
        limpiarTarjetas();
        JLabel lbl = new JLabel(mensaje, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 15));
        lbl.setForeground(TEXTO_SECUNDARIO);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(30, 20, 30, 20)));
        panel.add(lbl, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        pnlTarjetas.add(panel, gbc);
        pnlTarjetas.revalidate();
        pnlTarjetas.repaint();
    }

    private void limpiarTarjetas() {
        pnlTarjetas.removeAll();
    }

    private LocalDate fechaSeleccionada() {
        try {
            return LocalDate.parse(txtFechaConsulta.getText().trim());
        } catch (RuntimeException ex) {
            return LocalDate.now();
        }
    }

    private void refrescarDesdeCampo() {
        if (!refrescando && txtFechaConsulta.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
            refrescarDatos();
        }
    }


    private void irProximaActividad() {
        if (!SesionUsuario.haySesionActiva()
                || SesionUsuario.getUsuarioActual().getIdPersona() == null) {
            return;
        }
        long idCliente = SesionUsuario.getUsuarioActual().getIdPersona();
        RutinaAsignadaCliente seleccionada = (RutinaAsignadaCliente) cboRutina.getSelectedItem();
        if (seleccionada == null) {
            JOptionPane.showMessageDialog(this, "No tienes una rutina seleccionada.",
                    "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LocalDate siguiente = controlador.buscarProximaFechaConActividad(
                idCliente, seleccionada.idAsignacion, fechaSeleccionada());
        if (siguiente == null) {
            JOptionPane.showMessageDialog(this,
                    "No encontré otra actividad programada en los próximos 60 días "
                    + "dentro de la asignación activa.",
                    "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        establecerFecha(siguiente);
    }

    private void moverFecha(int dias) {
        establecerFecha(fechaSeleccionada().plusDays(dias));
    }

    private void establecerFecha(LocalDate fecha) {
        refrescando = true;
        try {
            txtFechaConsulta.setText(fecha.toString());
        } finally {
            refrescando = false;
        }
        refrescarDatos();
    }

    private JButton botonPrincipal(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(AZUL);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return btn;
    }

    private String nombreDia(LocalDate fecha) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern(
                "EEEE", new Locale("es", "ES"));
        return capitalizar(fecha.format(formato));
    }

    private String capitalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        return Character.toUpperCase(valor.charAt(0)) + valor.substring(1);
    }

    private String htmlCentrado(String texto) {
        if (texto == null) {
            return "<html><div style='text-align:center;'>Actividad</div></html>";
        }
        String seguro = texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        return "<html><div style='text-align:center;width:210px;'>"
                + seguro + "</div></html>";
    }
}
