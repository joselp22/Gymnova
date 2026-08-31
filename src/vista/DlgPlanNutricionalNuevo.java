package vista;

import controlador.NutricionistaWorkspaceControlador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.IncluyeAlimento;
import modelo.PlanNutricional;
import utilidades.ClienteEnAtencion;

/**
 * Diálogo para agregar alimentos al plan nutricional ACTIVO del cliente
 * en atención.
 *
 * El encabezado del plan (nombre, fechas, objetivos, restricciones) ya se
 * crea desde la pestaña "Plan nutricional" con el botón "Crear plan".
 * Esta ventana se abre después, cuando el plan ya existe, y sirve
 * exclusivamente para cargar los alimentos que el cliente debe
 * consumir (día, tipo de comida, cantidad, unidad, hora, orden e
 * indicaciones).
 */
public final class DlgPlanNutricionalNuevo extends JDialog {

    private final NutricionistaWorkspaceControlador controlador
            = new NutricionistaWorkspaceControlador();

    private final Cliente clienteFijo;
    private final PlanNutricional planActivo;
    private final JLabel lblCabecera = new JLabel();

    // Detalle de alimentos (en memoria hasta pulsar Guardar)
    private final List<Comida> comidas = new ArrayList<>();
    private final DefaultTableModel modeloComidas = new DefaultTableModel(
            new Object[]{"Día", "Comida", "Hora", "Alimento", "Cantidad",
                "Unidad", "Orden", "Indicaciones"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tblComidas = new JTable(modeloComidas);

    // Campos del formulario
    private final JComboBox<String> cboDia = new JComboBox<>(new String[]{
        "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO",
        "DOMINGO"});
    private final JComboBox<String> cboTipoComida = new JComboBox<>(
            new String[]{"DESAYUNO", "MEDIA_MANANA", "ALMUERZO", "MERIENDA",
                "CENA", "SNACK"});
    private final JTextField txtAlimento = new JTextField();
    private final JTextField txtCantidad = new JTextField();
    private final JTextField txtUnidad = new JTextField();
    private final JSpinner spnHora = crearSpinnerHora();
    private final JTextField txtOrden = new JTextField();
    private final JTextArea txtIndicaciones = new JTextArea(2, 20);

    private final JButton btnAgregar = boton("Agregar alimento",
            new Color(8, 124, 255));
    private final JButton btnQuitar = boton("Quitar seleccionado",
            new Color(190, 55, 65));
    private final JButton btnGuardar = boton("Guardar alimentos",
            new Color(34, 139, 94));
    private final JButton btnCancelar = boton("Cerrar",
            new Color(235, 241, 248));

    public DlgPlanNutricionalNuevo(Frame padre) {
        super(padre, "Agregar alimentos al plan", true);
        this.clienteFijo = ClienteEnAtencion.actual();
        this.planActivo = controlador.planActivoClienteActual();

        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(new Color(234, 242, 251));
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(14, 14, 14, 14));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirPanelAlimentos(), BorderLayout.CENTER);
        add(construirPie(), BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> agregarComida());
        btnQuitar.addActionListener(e -> quitarComida());
        btnGuardar.addActionListener(e -> guardarAlimentos());
        btnCancelar.addActionListener(e -> dispose());

        setSize(new Dimension(1020, 620));
        setLocationRelativeTo(padre);

        // Cargar alimentos que ya tenga el plan (para no duplicarlos).
        cargarAlimentosExistentes();

        // Validación: si no hay plan activo, no tiene sentido continuar.
        if (clienteFijo == null || planActivo == null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(padre,
                        clienteFijo == null
                        ? "Selecciona primero un cliente desde \"Mis "
                                + "clientes\"."
                        : "El cliente no tiene un plan nutricional activo. "
                                + "Créalo primero con el botón \"Crear "
                                + "plan\" y vuelve a abrir esta ventana.",
                        "Nutrición", JOptionPane.WARNING_MESSAGE);
                dispose();
            });
        }
    }

    // ---------- Construcción visual ----------

    private JPanel construirEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout(10, 8));
        encabezado.setBackground(Color.WHITE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));

        JLabel titulo = new JLabel(
                "GYMNOVA · Alimentos del plan nutricional");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(new Color(23, 42, 67));

        String textoCabecera;
        if (clienteFijo == null) {
            textoCabecera = "Ningún cliente en atención.";
        } else if (planActivo == null) {
            textoCabecera = "Cliente: " + clienteFijo.getNombreCompleto()
                    + "  ·  No tiene un plan activo.";
        } else {
            textoCabecera = "Cliente: " + clienteFijo.getNombreCompleto()
                    + "  ·  Plan: " + valor(planActivo.getNombrePlan())
                    + "  (" + valor(planActivo.getCodigoPlan()) + ")";
        }
        lblCabecera.setText(textoCabecera);
        lblCabecera.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblCabecera.setForeground(new Color(23, 42, 67));

        JLabel sub = new JLabel(
                "Agregue uno por uno los alimentos que el cliente debe "
                + "consumir. Al guardar, quedan asociados al plan activo.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(new Color(107, 127, 153));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(4));
        textos.add(lblCabecera);
        textos.add(Box.createVerticalStrut(3));
        textos.add(sub);
        encabezado.add(textos, BorderLayout.CENTER);
        return encabezado;
    }

    private JPanel construirPanelAlimentos() {
        JPanel panel = tarjeta();
        panel.setLayout(new BorderLayout(0, 8));

        // ------- Cabecera (título + ayuda) -------
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.add(tituloSeccion("Detalle de alimentos"), BorderLayout.WEST);
        JLabel ayuda = new JLabel(
                "  · agregue uno por uno los alimentos del plan");
        ayuda.setForeground(new Color(107, 127, 153));
        cab.add(ayuda, BorderLayout.CENTER);

        // ------- Formulario en dos filas con 4 columnas -------
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        formulario.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        Insets ins = new Insets(4, 6, 4, 6);
        Dimension pref = new Dimension(180, 28);
        cboDia.setPreferredSize(pref);
        cboTipoComida.setPreferredSize(pref);
        txtAlimento.setPreferredSize(pref);
        txtCantidad.setPreferredSize(pref);
        txtUnidad.setPreferredSize(pref);
        spnHora.setPreferredSize(pref);
        txtOrden.setPreferredSize(pref);

        celda(formulario, "Día *", cboDia, 0, 0, ins);
        celda(formulario, "Tipo comida *", cboTipoComida, 1, 0, ins);
        celda(formulario, "Alimento *", txtAlimento, 2, 0, ins);
        celda(formulario, "Cantidad *", txtCantidad, 3, 0, ins);
        celda(formulario, "Unidad *", txtUnidad, 0, 1, ins);
        celda(formulario, "Hora", spnHora, 1, 1, ins);
        celda(formulario, "Orden *", txtOrden, 2, 1, ins);

        txtIndicaciones.setLineWrap(true);
        txtIndicaciones.setWrapStyleWord(true);
        JScrollPane spInd = new JScrollPane(txtIndicaciones);
        spInd.setPreferredSize(new Dimension(200, 60));
        JPanel obs = new JPanel(new BorderLayout(0, 2));
        obs.setOpaque(false);
        JLabel lInd = new JLabel("Indicaciones");
        lInd.setForeground(new Color(23, 42, 67));
        obs.add(lInd, BorderLayout.NORTH);
        obs.add(spInd, BorderLayout.CENTER);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 3; g.gridy = 1;
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1;
        g.insets = ins;
        formulario.add(obs, g);

        // ------- Botones del formulario -------
        JPanel botonesForm = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botonesForm.setOpaque(false);
        botonesForm.add(btnAgregar);
        botonesForm.add(btnQuitar);

        // ------- NORTH: cabecera + formulario + botones -------
        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        cab.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        formulario.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        botonesForm.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        norte.add(cab);
        norte.add(Box.createVerticalStrut(4));
        norte.add(formulario);
        norte.add(Box.createVerticalStrut(4));
        norte.add(botonesForm);
        panel.add(norte, BorderLayout.NORTH);

        // ------- CENTER: tabla con lo agregado -------
        tblComidas.setRowHeight(28);
        tblComidas.setFillsViewportHeight(true);
        tblComidas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblComidas.getTableHeader().setBackground(new Color(10, 58, 108));
        tblComidas.getTableHeader().setForeground(Color.WHITE);
        tblComidas.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12));
        tblComidas.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(tblComidas);
        scroll.setBorder(BorderFactory.createLineBorder(
                new Color(212, 225, 239)));
        scroll.setPreferredSize(new Dimension(0, 200));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel construirPie() {
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pie.setBackground(Color.WHITE);
        pie.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        pie.add(btnGuardar);
        pie.add(btnCancelar);
        return pie;
    }

    // ---------- Datos ----------

    /** Muestra los alimentos que ya tiene registrados el plan activo. */
    private void cargarAlimentosExistentes() {
        if (planActivo == null) return;
        List<IncluyeAlimento> existentes
                = controlador.listarComidasPlanActivo();
        for (IncluyeAlimento x : existentes) {
            String nombre = controlador.nombreAlimento(x.getIdAlimento());
            modeloComidas.addRow(new Object[]{x.getDiaSemana(),
                x.getTipoComida(),
                x.getHoraConsumo() == null ? "" : x.getHoraConsumo(),
                nombre, x.getCantidad(), x.getUnidadMedida(),
                x.getOrdenComida(), x.getIndicaciones()});
        }
        // Estos ya están guardados en la BD: los mostramos en la tabla
        // pero no los volvemos a insertar. La lista "comidas" solo
        // acumulará lo NUEVO que agregue el nutricionista en esta sesión.
    }

    private void agregarComida() {
        try {
            String alimento = requerido(txtAlimento.getText(),
                    "Escriba el nombre del alimento.");
            BigDecimal cantidad = decimalPositivo(txtCantidad.getText(),
                    "Cantidad");
            String unidad = requerido(txtUnidad.getText(),
                    "Ingrese la unidad de medida.");
            Integer orden = enteroPositivo(txtOrden.getText(), "Orden");
            LocalTime hora = horaSpinner();
            String dia = (String) cboDia.getSelectedItem();

            // Validación: el mismo alimento no puede repetirse el mismo
            // día a la misma hora (ni entre los que ya estaban guardados
            // en el plan ni entre los agregados en esta sesión).
            if (existeAlimentoEnMismoHorario(dia, alimento, hora)) {
                throw new IllegalArgumentException(
                        "Ya existe \"" + alimento + "\" para el día " + dia
                        + (hora == null ? " sin hora asignada"
                                : " a las " + hora)
                        + ". No se puede repetir el mismo alimento a la "
                        + "misma hora.");
            }

            Comida c = new Comida();
            c.dia = dia;
            c.tipoComida = (String) cboTipoComida.getSelectedItem();
            c.hora = hora;
            c.alimento = alimento;
            c.cantidad = cantidad;
            c.unidad = unidad;
            c.orden = orden;
            c.indicaciones = txtIndicaciones.getText() == null
                    ? "" : txtIndicaciones.getText().trim();
            comidas.add(c);
            modeloComidas.addRow(new Object[]{c.dia, c.tipoComida,
                c.hora == null ? "" : c.hora.toString(), c.alimento,
                c.cantidad, c.unidad, c.orden, c.indicaciones});
            limpiarFormularioComida();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Alimento", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Devuelve true si en la tabla actual (alimentos ya guardados en el
     * plan + los recién agregados en esta sesión) ya existe una fila
     * con el mismo día, mismo alimento (comparación insensible a
     * mayúsculas/espacios) y misma hora.
     */
    private boolean existeAlimentoEnMismoHorario(String dia, String alimento,
            LocalTime hora) {
        if (alimento == null) return false;
        String nombreNuevo = alimento.trim().toLowerCase();
        String horaNueva = hora == null ? "" : hora.toString();
        String diaNuevo = dia == null ? "" : dia.trim().toUpperCase();
        for (int i = 0; i < modeloComidas.getRowCount(); i++) {
            String diaFila = valor(modeloComidas.getValueAt(i, 0))
                    .trim().toUpperCase();
            String horaFila = valor(modeloComidas.getValueAt(i, 2)).trim();
            String alimentoFila = valor(modeloComidas.getValueAt(i, 3))
                    .trim().toLowerCase();
            if (diaFila.equals(diaNuevo)
                    && alimentoFila.equals(nombreNuevo)
                    && horaFila.equals(horaNueva)) {
                return true;
            }
        }
        return false;
    }

    private void quitarComida() {
        int fila = tblComidas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un alimento de la lista.",
                    "Alimento", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // Las filas que ya estaban guardadas al abrir el diálogo se dejan
        // como referencia y no se pueden quitar aquí (para modificarlas se
        // usa la gestión avanzada del plan). Aquí solo se puede quitar lo
        // recién agregado en esta sesión, que corresponde a las últimas
        // filas de la tabla.
        int filasGuardadas = modeloComidas.getRowCount() - comidas.size();
        if (fila < filasGuardadas) {
            JOptionPane.showMessageDialog(this,
                    "Ese alimento ya está guardado en el plan y no se "
                    + "puede quitar desde aquí.",
                    "Alimento", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int idxLocal = fila - filasGuardadas;
        comidas.remove(idxLocal);
        modeloComidas.removeRow(fila);
    }

    private void guardarAlimentos() {
        if (clienteFijo == null || planActivo == null) {
            JOptionPane.showMessageDialog(this,
                    "No hay un plan activo para guardar alimentos.",
                    "Nutrición", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (comidas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Agregue al menos un alimento antes de guardar.",
                    "Nutrición", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int agregados = 0;
        List<String> errores = new ArrayList<>();
        for (Comida c : comidas) {
            Long idAlimento = controlador.obtenerOCrearAlimento(c.alimento);
            if (idAlimento == null) {
                errores.add("• " + c.alimento + ": "
                        + controlador.getMensaje());
                continue;
            }
            Long id = controlador.agregarComida(c.dia, c.tipoComida,
                    c.hora, c.cantidad, c.unidad, c.orden, idAlimento,
                    c.indicaciones == null || c.indicaciones.isBlank()
                            ? null : c.indicaciones);
            if (id == null) {
                errores.add("• " + c.alimento + ": "
                        + controlador.getMensaje());
            } else {
                agregados++;
            }
        }

        StringBuilder resumen = new StringBuilder();
        resumen.append("Alimentos guardados: ").append(agregados)
                .append(" de ").append(comidas.size())
                .append(" en el plan \"")
                .append(valor(planActivo.getNombrePlan())).append("\".");
        if (!errores.isEmpty()) {
            resumen.append("\n\nCon problemas:\n");
            for (String e : errores) resumen.append(e).append('\n');
        }
        JOptionPane.showMessageDialog(this, resumen.toString(),
                "Alimentos del plan",
                errores.isEmpty()
                        ? JOptionPane.INFORMATION_MESSAGE
                        : JOptionPane.WARNING_MESSAGE);

        // Si todo salió bien, cerramos; si hubo errores, dejamos abierto
        // para que el usuario los revise.
        if (errores.isEmpty()) {
            dispose();
        } else {
            // Vaciamos la lista de "recién agregados" que sí se guardaron:
            // como no distinguimos cuáles fallaron, el usuario debe revisar
            // el resumen. Mantenemos la tabla como está para no confundir.
        }
    }

    // ---------- Utilidades ----------

    private void limpiarFormularioComida() {
        txtAlimento.setText("");
        txtCantidad.setText("");
        txtUnidad.setText("");
        txtOrden.setText("");
        txtIndicaciones.setText("");
    }

    private String requerido(String s, String msg) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException(msg);
        }
        return s.trim();
    }

    private Integer enteroPositivo(String s, String etiqueta) {
        try {
            int v = Integer.parseInt(requerido(s,
                    etiqueta + " es obligatorio."));
            if (v <= 0) {
                throw new IllegalArgumentException(
                        etiqueta + " debe ser mayor que cero.");
            }
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    etiqueta + " debe ser un número entero.");
        }
    }

    private BigDecimal decimalPositivo(String s, String etiqueta) {
        try {
            BigDecimal v = new BigDecimal(requerido(s,
                    etiqueta + " es obligatorio.").replace(',', '.'));
            if (v.signum() <= 0) {
                throw new IllegalArgumentException(
                        etiqueta + " debe ser mayor que cero.");
            }
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    etiqueta + " debe ser numérico.");
        }
    }

    private LocalTime horaSpinner() {
        Object v = spnHora.getValue();
        if (!(v instanceof java.util.Date d)) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        return LocalTime.of(cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE));
    }

    private static JSpinner crearSpinnerHora() {
        JSpinner s = new JSpinner(new SpinnerDateModel());
        s.setEditor(new JSpinner.DateEditor(s, "HH:mm"));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        s.setValue(cal.getTime());
        return s;
    }

    private static JPanel etiquetaCampo(String etiqueta,
            java.awt.Component campo) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        JLabel l = new JLabel(etiqueta);
        l.setForeground(new Color(23, 42, 67));
        p.add(l, BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        return p;
    }

    private static void celda(JPanel panel, String etiqueta,
            java.awt.Component campo, int x, int y, Insets ins) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x; g.gridy = y;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = ins;
        panel.add(etiquetaCampo(etiqueta, campo), g);
    }

    private static JPanel tarjeta() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return p;
    }

    private static JLabel tituloSeccion(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 16));
        l.setForeground(new Color(23, 42, 67));
        return l;
    }

    private static JButton boton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setBackground(fondo);
        b.setForeground(fondo.equals(new Color(235, 241, 248))
                ? new Color(23, 42, 67) : Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return b;
    }

    private static String valor(Object o) {
        return o == null ? "" : o.toString();
    }

    /** Comida del detalle mientras aún no se guarda en la BD. */
    private static final class Comida {
        String dia;
        String tipoComida;
        LocalTime hora;
        String alimento;
        BigDecimal cantidad;
        String unidad;
        Integer orden;
        String indicaciones;
    }
}
