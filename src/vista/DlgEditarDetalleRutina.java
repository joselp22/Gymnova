package vista;

import controlador.FlujoEntrenadorControlador;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import modelo.ContieneEjercicio;
import modelo.Ejercicio;

/**
 * Editor del detalle de una rutina propia del entrenador.
 * Los cambios se mantienen localmente y se guardan en una sola transaccion.
 */
public final class DlgEditarDetalleRutina extends JDialog {

    private static final String[] DIAS = {
        "LUNES", "MARTES", "MIERCOLES", "JUEVES",
        "VIERNES", "SABADO"
    };

    private final Long idEntrenador;
    private final Long idRutina;
    private final String nombreRutina;
    private final FlujoEntrenadorControlador flujo;
    private final List<Ejercicio> actividades;
    private final List<ItemDetalle> items = new ArrayList<>();

    private final JComboBox<String> cboDia = new JComboBox<>(DIAS);
    private final JComboBox<Ejercicio> cboEjercicio = new JComboBox<>();
    private final JTextField txtOrden = new JTextField("1", 6);
    private final JTextField txtSeries = new JTextField("3", 6);
    private final JTextField txtRepeticiones = new JTextField("10", 8);
    private final JTextField txtPeso = new JTextField(8);
    private final JTextField txtDuracion = new JTextField(8);
    private final JTextField txtDescanso = new JTextField("60", 8);
    private final JTable tblDetalle = new JTable();

    private int indiceSeleccionado = -1;
    private boolean guardado;

    private DlgEditarDetalleRutina(
            Window owner,
            Long idEntrenador,
            Long idRutina,
            String nombreRutina,
            List<ContieneEjercicio> detalleActual,
            List<Ejercicio> actividades,
            FlujoEntrenadorControlador flujo
    ) {
        super(owner, "Editar detalle de rutina", ModalityType.APPLICATION_MODAL);
        this.idEntrenador = idEntrenador;
        this.idRutina = idRutina;
        this.nombreRutina = nombreRutina;
        this.actividades = actividades == null ? new ArrayList<>() : actividades;
        this.flujo = flujo;

        cargarActividades();
        cargarDetalleActual(detalleActual);
        construirVista();
        refrescarTabla();
    }

    public static boolean mostrar(
            Component parent,
            Long idEntrenador,
            Long idRutina,
            String nombreRutina,
            List<ContieneEjercicio> detalleActual,
            List<Ejercicio> actividades,
            FlujoEntrenadorControlador flujo
    ) {
        Window owner = parent == null
                ? null : SwingUtilities.getWindowAncestor(parent);

        DlgEditarDetalleRutina dlg = new DlgEditarDetalleRutina(
                owner, idEntrenador, idRutina, nombreRutina,
                detalleActual, actividades, flujo);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        return dlg.guardado;
    }

    private void construirVista() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(980, 600));
        setPreferredSize(new Dimension(1080, 650));
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new java.awt.Color(234, 242, 251));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(java.awt.Color.WHITE);
        cabecera.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JLabel titulo = new JLabel(
                "Editar detalle - Rutina N° " + idRutina + " - " + nombreRutina);
        titulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 19));
        titulo.setForeground(new java.awt.Color(23, 42, 67));
        cabecera.add(titulo, BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(java.awt.Color.WHITE);
        formulario.setBorder(BorderFactory.createTitledBorder(
                "Actividad del detalle"));

        agregarCampo(formulario, 0, "Día *", cboDia,
                "Actividad *", cboEjercicio);
        agregarCampo(formulario, 1, "Orden *", txtOrden,
                "Series *", txtSeries);
        agregarCampo(formulario, 2, "Repeticiones *", txtRepeticiones,
                "Peso sugerido", txtPeso);
        agregarCampo(formulario, 3, "Duración (min)", txtDuracion,
                "Descanso (seg)", txtDescanso);

        JButton btnAgregar = botonPrincipal("Agregar actividad");
        JButton btnActualizar = new JButton("Actualizar seleccionada");
        JButton btnQuitar = new JButton("Quitar seleccionada");
        JButton btnLimpiar = new JButton("Limpiar campos");

        JPanel accionesFormulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accionesFormulario.setOpaque(false);
        accionesFormulario.add(btnAgregar);
        accionesFormulario.add(btnActualizar);
        accionesFormulario.add(btnQuitar);
        accionesFormulario.add(btnLimpiar);

        JPanel norteFormulario = new JPanel(new BorderLayout());
        norteFormulario.setOpaque(false);
        norteFormulario.add(formulario, BorderLayout.CENTER);
        norteFormulario.add(accionesFormulario, BorderLayout.SOUTH);

        configurarTabla();
        JScrollPane scroll = new JScrollPane(tblDetalle);
        scroll.setBorder(BorderFactory.createTitledBorder(
                "Detalle actual de la rutina"));

        JButton btnGuardar = botonPrincipal("Guardar detalle de rutina");
        JButton btnCerrar = new JButton("Cerrar");
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pie.setOpaque(false);
        pie.add(btnGuardar);
        pie.add(btnCerrar);

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contenido.add(cabecera, BorderLayout.NORTH);
        contenido.add(norteFormulario, BorderLayout.CENTER);

        JPanel centroSur = new JPanel(new BorderLayout(8, 8));
        centroSur.setOpaque(false);
        centroSur.add(scroll, BorderLayout.CENTER);
        centroSur.add(pie, BorderLayout.SOUTH);
        contenido.add(centroSur, BorderLayout.SOUTH);

        // El scroll necesita una altura razonable dentro del BorderLayout.
        scroll.setPreferredSize(new Dimension(950, 270));

        add(contenido, BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> agregarItem());
        btnActualizar.addActionListener(e -> actualizarItem());
        btnQuitar.addActionListener(e -> quitarItem());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnGuardar.addActionListener(e -> guardarDetalle());
        btnCerrar.addActionListener(e -> dispose());

        // Al cambiar de día, sugerimos el siguiente orden libre del día
        // seleccionado; así se pueden encadenar varias actividades sin
        // chocar con el orden 1 por defecto.
        cboDia.addActionListener(e -> {
            if (indiceSeleccionado < 0) {
                txtOrden.setText(String.valueOf(
                        siguienteOrden(String.valueOf(cboDia.getSelectedItem()))));
            }
        });

        tblDetalle.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        pack();
        utilidades.EstilosComponentes.uniformarLookAzul(getContentPane());
    }

    private void cargarActividades() {
        DefaultComboBoxModel<Ejercicio> modelo = new DefaultComboBoxModel<>();
        for (Ejercicio e : actividades) {
            modelo.addElement(e);
        }
        cboEjercicio.setModel(modelo);
    }

    private void cargarDetalleActual(List<ContieneEjercicio> detalleActual) {
        if (detalleActual == null) {
            return;
        }
        for (ContieneEjercicio d : detalleActual) {
            Ejercicio ejercicio = buscarEjercicio(d.getIdEjercicio());
            if (ejercicio == null) {
                ejercicio = new Ejercicio();
                ejercicio.setIdEjercicio(d.getIdEjercicio());
                ejercicio.setNombreEjercicio("Actividad #" + d.getIdEjercicio());
            }
            items.add(new ItemDetalle(
                    d.getIdRutinaEjercicio(),
                    d.getDiaSemana(),
                    valorEntero(d.getOrden(), 1),
                    ejercicio,
                    valorEntero(d.getSeries(), 1),
                    d.getRepeticiones(),
                    d.getPesoSugerido(),
                    d.getDuracionMinutos(),
                    d.getDescansoSegundos()));
        }
    }

    private void configurarTabla() {
        tblDetalle.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Día", "Orden", "Actividad", "Series",
                    "Repeticiones", "Peso", "Duración", "Descanso"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tblDetalle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDetalle.setRowHeight(30);
        tblDetalle.setFillsViewportHeight(true);
    }

    private void refrescarTabla() {
        items.sort(Comparator
                .comparingInt((ItemDetalle i) -> indiceDia(i.dia))
                .thenComparingInt(i -> i.orden));

        DefaultTableModel modelo = (DefaultTableModel) tblDetalle.getModel();
        modelo.setRowCount(0);
        for (ItemDetalle i : items) {
            modelo.addRow(new Object[]{
                i.dia,
                i.orden,
                i.ejercicio,
                i.series,
                i.repeticiones,
                i.peso,
                i.duracion,
                i.descanso
            });
        }
        indiceSeleccionado = -1;
        tblDetalle.clearSelection();
    }

    private void agregarItem() {
        try {
            ItemDetalle nuevo = itemDesdeCampos(null);
            if (existeOrden(nuevo.dia, nuevo.orden, -1)) {
                error("Ya existe una actividad con ese orden en " + nuevo.dia + ".");
                return;
            }
            items.add(nuevo);
            refrescarTabla();
            limpiarCampos();
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void actualizarItem() {
        int fila = tblDetalle.getSelectedRow();
        if (fila < 0 || indiceSeleccionado < 0 || indiceSeleccionado >= items.size()) {
            error("Selecciona una actividad del detalle para actualizarla.");
            return;
        }
        try {
            Long idDetalle = items.get(indiceSeleccionado).idRutinaEjercicio;
            ItemDetalle actualizado = itemDesdeCampos(idDetalle);
            if (existeOrden(actualizado.dia, actualizado.orden, indiceSeleccionado)) {
                error("Ya existe otra actividad con ese orden en " + actualizado.dia + ".");
                return;
            }
            items.set(indiceSeleccionado, actualizado);
            refrescarTabla();
            limpiarCampos();
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void quitarItem() {
        if (indiceSeleccionado < 0 || indiceSeleccionado >= items.size()) {
            error("Selecciona una actividad del detalle para quitarla.");
            return;
        }
        items.remove(indiceSeleccionado);
        refrescarTabla();
        limpiarCampos();
    }

    private void cargarSeleccion() {
        int fila = tblDetalle.getSelectedRow();
        if (fila < 0 || fila >= items.size()) {
            indiceSeleccionado = -1;
            return;
        }
        // La tabla se construye en el mismo orden que items.
        indiceSeleccionado = fila;
        ItemDetalle i = items.get(fila);
        cboDia.setSelectedItem(i.dia);
        seleccionarEjercicio(i.ejercicio.getIdEjercicio());
        txtOrden.setText(String.valueOf(i.orden));
        txtSeries.setText(String.valueOf(i.series));
        txtRepeticiones.setText(i.repeticiones == null ? "" : i.repeticiones);
        txtPeso.setText(i.peso == null ? "" : i.peso.toPlainString());
        txtDuracion.setText(i.duracion == null ? "" : String.valueOf(i.duracion));
        txtDescanso.setText(i.descanso == null ? "" : String.valueOf(i.descanso));
    }

    private void guardarDetalle() {
        if (items.isEmpty()) {
            error("La rutina debe conservar al menos una actividad.");
            return;
        }

        List<FlujoEntrenadorControlador.EjercicioDia> nuevos = new ArrayList<>();
        for (ItemDetalle i : items) {
            FlujoEntrenadorControlador.EjercicioDia d =
                    new FlujoEntrenadorControlador.EjercicioDia(
                            i.dia, i.orden, i.series, i.repeticiones,
                            i.descanso, i.ejercicio.getIdEjercicio().intValue());
            d.idRutinaEjercicio = i.idRutinaEjercicio;
            d.pesoSugerido = i.peso;
            d.duracionMinutos = i.duracion;
            nuevos.add(d);
        }

        if (!flujo.actualizarDetalleRutina(idEntrenador, idRutina, nuevos)) {
            error(flujo.getMensaje());
            return;
        }

        guardado = true;
        JOptionPane.showMessageDialog(this,
                "Detalle de la rutina actualizado correctamente.",
                "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private ItemDetalle itemDesdeCampos(Long idRutinaEjercicio) {
        Ejercicio ejercicio = (Ejercicio) cboEjercicio.getSelectedItem();
        if (ejercicio == null || ejercicio.getIdEjercicio() == null) {
            throw new IllegalArgumentException("Selecciona una actividad válida.");
        }

        String dia = String.valueOf(cboDia.getSelectedItem());
        int orden = enteroPositivo(txtOrden, "orden");
        int series = enteroPositivo(txtSeries, "series");
        String repeticiones = txtRepeticiones.getText().trim();
        if (repeticiones.isEmpty()) {
            throw new IllegalArgumentException("Ingresa las repeticiones.");
        }

        BigDecimal peso = decimalOpcional(txtPeso, "peso");
        Integer duracion = duracionOpcional(txtDuracion);
        Integer descanso = enteroOpcional(txtDescanso, "descanso");

        return new ItemDetalle(idRutinaEjercicio, dia, orden, ejercicio,
                series, repeticiones, peso, duracion, descanso);
    }

    private boolean existeOrden(String dia, int orden, int indiceIgnorado) {
        for (int i = 0; i < items.size(); i++) {
            if (i == indiceIgnorado) {
                continue;
            }
            ItemDetalle item = items.get(i);
            if (item.dia.equalsIgnoreCase(dia) && item.orden == orden) {
                return true;
            }
        }
        return false;
    }

    /** Próximo orden libre para el día indicado (1 si aún no hay). */
    private int siguienteOrden(String dia) {
        int max = 0;
        if (dia != null) {
            for (ItemDetalle i : items) {
                if (i.dia != null && i.dia.equalsIgnoreCase(dia)) {
                    max = Math.max(max, i.orden);
                }
            }
        }
        return max + 1;
    }

    private Ejercicio buscarEjercicio(Long id) {
        if (id == null) {
            return null;
        }
        for (Ejercicio e : actividades) {
            if (id.equals(e.getIdEjercicio())) {
                return e;
            }
        }
        return null;
    }

    private void seleccionarEjercicio(Long id) {
        for (int i = 0; i < cboEjercicio.getItemCount(); i++) {
            Ejercicio e = cboEjercicio.getItemAt(i);
            if (e != null && id != null && id.equals(e.getIdEjercicio())) {
                cboEjercicio.setSelectedIndex(i);
                return;
            }
        }
    }

    private void limpiarCampos() {
        indiceSeleccionado = -1;
        if (cboEjercicio.getItemCount() > 0) {
            cboEjercicio.setSelectedIndex(0);
        }
        // Conservamos el día visible; el orden se ajusta al siguiente
        // libre para ese día para poder añadir varias actividades.
        txtOrden.setText(String.valueOf(
                siguienteOrden(String.valueOf(cboDia.getSelectedItem()))));
        txtSeries.setText("3");
        txtRepeticiones.setText("10");
        txtPeso.setText("");
        txtDuracion.setText("");
        txtDescanso.setText("60");
        tblDetalle.clearSelection();
    }

    private int enteroPositivo(JTextField campo, String nombre) {
        try {
            int valor = Integer.parseInt(campo.getText().trim());
            if (valor <= 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El campo " + nombre + " debe ser un entero mayor a cero.");
        }
    }

    private Integer duracionOpcional(JTextField campo) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            int valor = Integer.parseInt(texto);
            if (valor < 0) {
                throw new NumberFormatException();
            }
            return valor == 0 ? null : valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "La duración debe estar vacía o ser un entero mayor a cero.");
        }
    }

    private Integer enteroOpcional(JTextField campo, String nombre) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            int valor = Integer.parseInt(texto);
            if (valor < 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El campo " + nombre + " debe ser un entero válido.");
        }
    }

    private BigDecimal decimalOpcional(JTextField campo, String nombre) {
        String texto = campo.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            return null;
        }
        try {
            BigDecimal valor = new BigDecimal(texto);
            if (valor.signum() < 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El campo " + nombre + " debe ser un número válido.");
        }
    }

    private void agregarCampo(
            JPanel panel, int fila,
            String etiqueta1, Component campo1,
            String etiqueta2, Component campo2
    ) {
        GridBagConstraints c = gbc(0, fila);
        panel.add(new JLabel(etiqueta1), c);
        c = gbc(1, fila);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(campo1, c);
        c = gbc(2, fila);
        panel.add(new JLabel(etiqueta2), c);
        c = gbc(3, fila);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(campo2, c);
    }

    private GridBagConstraints gbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;
        return c;
    }

    private JButton botonPrincipal(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(new java.awt.Color(8, 124, 255));
        b.setForeground(java.awt.Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private void error(String texto) {
        JOptionPane.showMessageDialog(this, texto,
                "GYMNOVA", JOptionPane.WARNING_MESSAGE);
    }

    private int indiceDia(String dia) {
        for (int i = 0; i < DIAS.length; i++) {
            if (DIAS[i].equalsIgnoreCase(dia)) {
                return i;
            }
        }
        return DIAS.length;
    }

    private int valorEntero(Integer valor, int defecto) {
        return valor == null || valor <= 0 ? defecto : valor;
    }

    private static final class ItemDetalle {
        private final Long idRutinaEjercicio;
        private final String dia;
        private final int orden;
        private final Ejercicio ejercicio;
        private final int series;
        private final String repeticiones;
        private final BigDecimal peso;
        private final Integer duracion;
        private final Integer descanso;

        ItemDetalle(Long idRutinaEjercicio, String dia, int orden,
                Ejercicio ejercicio, int series, String repeticiones,
                BigDecimal peso, Integer duracion, Integer descanso) {
            this.idRutinaEjercicio = idRutinaEjercicio;
            this.dia = dia;
            this.orden = orden;
            this.ejercicio = ejercicio;
            this.series = series;
            this.repeticiones = repeticiones;
            this.peso = peso;
            this.duracion = duracion;
            this.descanso = descanso;
        }
    }
}
