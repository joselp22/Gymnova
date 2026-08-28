package vista;

import controlador.ClienteControlador;
import controlador.ContieneEjercicioControlador;
import controlador.EjercicioControlador;
import controlador.FlujoEntrenadorControlador;
import controlador.RutinaControlador;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import modelo.Cliente;
import modelo.ContieneEjercicio;
import modelo.Ejercicio;
import modelo.Rutina;
import modelo.RutinaDetalleAdministrador;
import utilidades.SesionUsuario;

/**
 * Vista operativa exclusiva del rol ENTRENADOR.
 *
 * Flujo:
 * 1) Crear actividades propias.
 * 2) Armar una rutina usando actividades como detalle.
 * 3) Guardar la rutina (rutina + rutina_dia_entrenamiento
 *    + contiene_ejercicio).
 * 4) Asignar la rutina a un cliente.
 *
 * Esta vista NO reutiliza el panel administrativo PnlRutinas.
 */
public class PnlRutinasEntrenador extends JPanel {

    private final Long idEntrenador;

    private final EjercicioControlador ejercicioControlador;
    private final RutinaControlador rutinaControlador;
    private final ContieneEjercicioControlador detalleControlador;
    private final ClienteControlador clienteControlador;
    private final FlujoEntrenadorControlador flujoEntrenador;

    private final JTabbedPane tabs = new JTabbedPane();

    // Actividades
    private final JTextField txtActividadNombre = new JTextField(24);
    private final JComboBox<String> cboActividadTipo = new JComboBox<>(
            new String[]{"FUERZA", "CARDIO", "MOVILIDAD", "FUNCIONAL", "OTRO"});
    private final JComboBox<String> cboActividadNivel = new JComboBox<>(
            new String[]{"PRINCIPIANTE", "INTERMEDIO", "AVANZADO"});
    private final JTextArea txtActividadDescripcion = new JTextArea(3, 28);
    private final JTextArea txtActividadInstrucciones = new JTextArea(4, 28);
    private final JTable tblActividades = new JTable();
    private Long idActividadSeleccionada;

    // Constructor de rutina
    private final JTextField txtRutinaNombre = new JTextField(22);
    private final JComboBox<String> cboRutinaNivel = new JComboBox<>(
            new String[]{"PRINCIPIANTE", "INTERMEDIO", "AVANZADO"});
    private final JTextField txtRutinaSemanas = new JTextField("4", 6);
    private final JTextArea txtRutinaDescripcion = new JTextArea(3, 28);

    private final JComboBox<String> cboDia = new JComboBox<>(
            new String[]{"LUNES", "MARTES", "MIERCOLES", "JUEVES",
                "VIERNES", "SABADO"});
    private final JComboBox<Ejercicio> cboActividadRutina = new JComboBox<>();
    private final JTextField txtOrden = new JTextField("1", 5);
    private final JTextField txtSeries = new JTextField("3", 5);
    private final JTextField txtRepeticiones = new JTextField("10", 7);
    private final JTextField txtPeso = new JTextField("", 7);
    private final JTextField txtDuracion = new JTextField("", 7);
    private final JTextField txtDescanso = new JTextField("60", 7);
    private final JTable tblDetalleTemporal = new JTable();
    private final JTable tblRutinas = new JTable();

    // Mis rutinas / edición
    private Long idRutinaSeleccionada;
    private final JTextField txtEditarRutinaNombre = new JTextField(22);
    private final JComboBox<String> cboEditarRutinaNivel = new JComboBox<>(
            new String[]{"PRINCIPIANTE", "INTERMEDIO", "AVANZADO"});
    private final JTextField txtEditarRutinaSemanas = new JTextField(6);
    private final JTextArea txtEditarRutinaDescripcion = new JTextArea(3, 28);
    private final JLabel lblRutinaSeleccionada = new JLabel(
            "Selecciona una rutina de la tabla para editarla.");

    // Asignación
    private final JComboBox<Cliente> cboCliente = new JComboBox<>();
    private final JComboBox<Rutina> cboRutinaAsignar = new JComboBox<>();
    private final JTextArea txtObservacionesAsignacion = new JTextArea(3, 28);
    private final JLabel lblDiasRutinaAsignar = new JLabel(
            "Días con actividades: selecciona una rutina.");
    private final JTable tblAsignaciones = new JTable();

    private final List<DetalleTemporal> detalleTemporal = new ArrayList<>();

    public PnlRutinasEntrenador() {

        if (!SesionUsuario.haySesionActiva()
                || SesionUsuario.getUsuarioActual().getIdPersona() == null) {
            throw new IllegalStateException(
                    "No existe una sesión de entrenador válida.");
        }

        idEntrenador = SesionUsuario.getUsuarioActual().getIdPersona();

        ejercicioControlador = new EjercicioControlador();
        rutinaControlador = new RutinaControlador();
        detalleControlador = new ContieneEjercicioControlador();
        clienteControlador = new ClienteControlador();
        flujoEntrenador = new FlujoEntrenadorControlador();

        cboRutinaAsignar.addActionListener(e -> actualizarResumenDiasAsignacion());

        // Al cambiar el día en el formulario de detalle, sugerimos el
        // siguiente orden libre para ese día para que se puedan agregar
        // varias actividades sin conflicto con el orden 1 por defecto.
        cboDia.addActionListener(e -> {
            Object sel = cboDia.getSelectedItem();
            if (sel != null) {
                txtOrden.setText(String.valueOf(siguienteOrden(sel.toString())));
            }
        });

        construirVista();
        configurarEventos();
        refrescarDatos();
    }

    public final void refrescarDatos() {
        cargarMisActividades();
        cargarActividadesDisponibles();
        cargarRutinas();
        cargarClientes();
        cargarAsignaciones();
    }

    private void construirVista() {

        setLayout(new BorderLayout(12, 12));
        setBackground(new java.awt.Color(234, 242, 251));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(java.awt.Color.WHITE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel titulo = new JLabel("Planificador del entrenador");
        titulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
        titulo.setForeground(new java.awt.Color(23, 42, 67));

        JLabel subtitulo = new JLabel(
                "Crea actividades, construye el detalle de tus rutinas y asígnalas a tus clientes.");
        subtitulo.setForeground(new java.awt.Color(90, 110, 135));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new javax.swing.BoxLayout(
                textos, javax.swing.BoxLayout.Y_AXIS));
        textos.add(titulo);
        textos.add(javax.swing.Box.createVerticalStrut(4));
        textos.add(subtitulo);

        encabezado.add(textos, BorderLayout.CENTER);
        add(encabezado, BorderLayout.NORTH);

        tabs.addTab("1. Mis actividades", construirTabActividades());
        tabs.addTab("2. Mis rutinas", construirTabMisRutinas());
        tabs.addTab("3. Asignar rutina", construirTabAsignacion());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel construirTabActividades() {

        JPanel raiz = panelBlanco(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);

        int y = 0;
        agregarCampo(formulario, y++, "Nombre *", txtActividadNombre,
                "Tipo *", cboActividadTipo);
        agregarCampo(formulario, y++, "Nivel *", cboActividadNivel,
                "Descripción", new JScrollPane(txtActividadDescripcion));

        GridBagConstraints c = gbc(0, y);
        c.gridwidth = 1;
        formulario.add(new JLabel("Instrucciones"), c);
        c = gbc(1, y);
        c.gridwidth = 3;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        formulario.add(new JScrollPane(txtActividadInstrucciones), c);

        JButton btnNueva = boton("Guardar actividad");
        JButton btnModificar = botonSecundario("Modificar seleccionada");
        JButton btnLimpiar = botonSecundario("Limpiar");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.setOpaque(false);
        acciones.add(btnNueva);
        acciones.add(btnModificar);
        acciones.add(btnLimpiar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(formulario, BorderLayout.CENTER);
        norte.add(acciones, BorderLayout.SOUTH);

        configurarTabla(tblActividades,
                new String[]{"ID", "Actividad", "Tipo", "Nivel", "Descripción"});
        JScrollPane scroll = new JScrollPane(tblActividades);
        scroll.setBorder(BorderFactory.createTitledBorder("Mis actividades creadas"));

        raiz.add(norte, BorderLayout.NORTH);
        raiz.add(scroll, BorderLayout.CENTER);

        btnNueva.addActionListener(e -> guardarActividad());
        btnModificar.addActionListener(e -> modificarActividad());
        btnLimpiar.addActionListener(e -> limpiarActividad());

        tblActividades.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarActividadSeleccionada();
            }
        });

        return raiz;
    }

    private JPanel construirTabRutinas() {

        JPanel raiz = panelBlanco(new BorderLayout(10, 10));

        JPanel cabecera = new JPanel(new GridBagLayout());
        cabecera.setOpaque(false);
        cabecera.setBorder(BorderFactory.createTitledBorder(
                "Datos de la nueva rutina"));

        int y = 0;
        agregarCampo(cabecera, y++, "Nombre de rutina *", txtRutinaNombre,
                "Nivel *", cboRutinaNivel);
        agregarCampo(cabecera, y++, "Duración (semanas) *", txtRutinaSemanas,
                "Descripción", new JScrollPane(txtRutinaDescripcion));

        JPanel detalle = new JPanel(new GridBagLayout());
        detalle.setOpaque(false);
        detalle.setBorder(BorderFactory.createTitledBorder(
                "Actividades que formarán la rutina"));

        int r = 0;
        agregarCampo(detalle, r++, "Día *", cboDia,
                "Actividad *", cboActividadRutina);
        agregarCampo(detalle, r++, "Orden *", txtOrden,
                "Series *", txtSeries);
        agregarCampo(detalle, r++, "Repeticiones *", txtRepeticiones,
                "Peso sugerido", txtPeso);
        agregarCampo(detalle, r++, "Duración (min)", txtDuracion,
                "Descanso (seg)", txtDescanso);

        JButton btnAgregar = boton("Agregar actividad");
        JButton btnQuitar = botonSecundario("Quitar seleccionada");
        JButton btnGuardarRutina = boton("Guardar rutina completa");
        JButton btnNuevaRutina = botonSecundario("Limpiar constructor");

        JPanel accionesDetalle = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accionesDetalle.setOpaque(false);
        accionesDetalle.add(btnAgregar);
        accionesDetalle.add(btnQuitar);
        accionesDetalle.add(btnGuardarRutina);
        accionesDetalle.add(btnNuevaRutina);

        configurarTabla(tblDetalleTemporal,
                new String[]{"Día", "Orden", "Actividad", "Series",
                    "Repeticiones", "Peso", "Duración", "Descanso"});
        JScrollPane scrollTemporal = new JScrollPane(tblDetalleTemporal);
        scrollTemporal.setBorder(BorderFactory.createTitledBorder(
                "Detalle temporal de la nueva rutina"));

        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.setOpaque(false);
        centro.add(detalle, BorderLayout.NORTH);
        centro.add(scrollTemporal, BorderLayout.CENTER);
        centro.add(accionesDetalle, BorderLayout.SOUTH);

        raiz.add(cabecera, BorderLayout.NORTH);
        raiz.add(centro, BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> agregarDetalleTemporal());
        btnQuitar.addActionListener(e -> quitarDetalleTemporal());
        btnGuardarRutina.addActionListener(e -> guardarRutinaCompleta());
        btnNuevaRutina.addActionListener(e -> limpiarRutina());

        return raiz;
    }

    private JPanel construirTabMisRutinas() {

        JPanel raiz = panelBlanco(new BorderLayout(10, 10));

        JLabel ayuda = new JLabel(
                "<html>Gestiona tus rutinas desde esta pantalla. "
                + "Crear y editar abre una ventana con los datos generales "
                + "y el detalle de actividades.</html>");
        ayuda.setForeground(new java.awt.Color(90, 110, 135));

        JButton btnCrear = boton("Crear rutina");
        JButton btnEditar = boton("Editar rutina");
        JButton btnActualizar = botonSecundario("Actualizar lista");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.setOpaque(false);
        acciones.add(btnCrear);
        acciones.add(btnEditar);
        acciones.add(btnActualizar);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(ayuda, BorderLayout.CENTER);
        norte.add(acciones, BorderLayout.SOUTH);

        configurarTablaRutinasGuardadas();
        JScrollPane scroll = new JScrollPane(tblRutinas);
        scroll.setBorder(BorderFactory.createTitledBorder(
                "Rutinas creadas por mí"));

        raiz.add(norte, BorderLayout.NORTH);
        raiz.add(scroll, BorderLayout.CENTER);

        tblRutinas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarRutinaSeleccionadaParaEdicion();
            }
        });

        btnCrear.addActionListener(e -> crearRutinaDesdeMisRutinas());
        btnEditar.addActionListener(e -> editarRutinaDesdeMisRutinas());
        btnActualizar.addActionListener(e -> {
            cargarRutinas();
            limpiarEdicionRutina();
        });

        return raiz;
    }

    private JPanel construirTabAsignacion() {

        JPanel raiz = panelBlanco(new BorderLayout(10, 10));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        int y = 0;
        agregarCampo(formulario, y++, "Cliente *", cboCliente,
                "Rutina *", cboRutinaAsignar);

        GridBagConstraints c = gbc(0, y);
        formulario.add(new JLabel("Observaciones"), c);
        c = gbc(1, y);
        c.gridwidth = 3;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        formulario.add(new JScrollPane(txtObservacionesAsignacion), c);

        JButton btnAsignar = boton("Asignar rutina al cliente");
        JButton btnActualizar = botonSecundario("Actualizar");
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.setOpaque(false);
        acciones.add(btnAsignar);
        acciones.add(btnActualizar);

        JLabel ayuda = new JLabel(
                "<html>La asignación es directa: Cliente ↔ Rutina. No requiere fecha de inicio ni fin. "
                + "Un cliente puede tener varias rutinas activas y escoger cuál seguir desde Mi rutina.</html>");
        ayuda.setForeground(new java.awt.Color(90, 110, 135));
        lblDiasRutinaAsignar.setForeground(new java.awt.Color(20, 70, 120));

        JPanel cabecera = new JPanel(new BorderLayout(4, 6));
        cabecera.setOpaque(false);
        cabecera.add(formulario, BorderLayout.NORTH);
        JPanel info = new JPanel(new BorderLayout(4, 4));
        info.setOpaque(false);
        info.add(lblDiasRutinaAsignar, BorderLayout.NORTH);
        info.add(ayuda, BorderLayout.CENTER);
        cabecera.add(info, BorderLayout.CENTER);
        cabecera.add(acciones, BorderLayout.SOUTH);

        tblAsignaciones.setModel(new DefaultTableModel(
                new Object[]{"ID", "Cliente", "Rutina", "Días", "Estado"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        tblAsignaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollAsignaciones = new JScrollPane(tblAsignaciones);
        scrollAsignaciones.setBorder(BorderFactory.createTitledBorder(
                "Rutinas asignadas a clientes"));

        raiz.add(cabecera, BorderLayout.NORTH);
        raiz.add(scrollAsignaciones, BorderLayout.CENTER);

        btnAsignar.addActionListener(e -> asignarRutina());
        btnActualizar.addActionListener(e -> refrescarDatos());

        return raiz;
    }

    private void configurarEventos() {
        // Los eventos específicos se registran al construir cada pestaña.
    }

    private void guardarActividad() {

        Ejercicio actividad = actividadDesdeFormulario();
        if (actividad == null) {
            return;
        }

        actividad.setIdEntrenador(idEntrenador);

        if (ejercicioControlador.registrar(actividad)) {
            mensaje("Actividad creada correctamente.");
            limpiarActividad();
            cargarMisActividades();
            cargarActividadesDisponibles();
        } else {
            error(ejercicioControlador.getMensaje());
        }
    }

    private void modificarActividad() {

        if (idActividadSeleccionada == null) {
            error("Selecciona una actividad creada por ti.");
            return;
        }

        Ejercicio actividad = actividadDesdeFormulario();
        if (actividad == null) {
            return;
        }

        actividad.setIdEjercicio(idActividadSeleccionada);
        actividad.setIdEntrenador(idEntrenador);

        if (ejercicioControlador.modificar(actividad)) {
            mensaje("Actividad modificada.");
            limpiarActividad();
            cargarMisActividades();
            cargarActividadesDisponibles();
        } else {
            error(ejercicioControlador.getMensaje());
        }
    }

    private Ejercicio actividadDesdeFormulario() {

        String nombre = txtActividadNombre.getText().trim();
        if (nombre.isEmpty()) {
            error("Ingresa el nombre de la actividad.");
            return null;
        }

        Ejercicio actividad = new Ejercicio();
        actividad.setNombreEjercicio(nombre);
        actividad.setTipoEjercicio(
                String.valueOf(cboActividadTipo.getSelectedItem()));
        actividad.setNivelDificultad(
                String.valueOf(cboActividadNivel.getSelectedItem()));
        actividad.setDescripcion(txtActividadDescripcion.getText().trim());
        actividad.setInstrucciones(txtActividadInstrucciones.getText().trim());
        return actividad;
    }

    private void cargarMisActividades() {

        List<Ejercicio> lista = ejercicioControlador
                .listarPropiosEntrenador(idEntrenador, "");

        DefaultTableModel m = modelo(tblActividades);
        m.setRowCount(0);

        for (Ejercicio e : lista) {
            m.addRow(new Object[]{
                e.getIdEjercicio(),
                e.getNombreEjercicio(),
                e.getTipoEjercicio(),
                e.getNivelDificultad(),
                e.getDescripcion()
            });
        }
    }

    private void cargarActividadesDisponibles() {

        List<Ejercicio> lista = ejercicioControlador
                .listarDisponiblesParaEntrenador(idEntrenador, "");

        DefaultComboBoxModel<Ejercicio> m = new DefaultComboBoxModel<>();
        for (Ejercicio e : lista) {
            m.addElement(e);
        }
        cboActividadRutina.setModel(m);
    }

    private void cargarActividadSeleccionada() {

        int fila = tblActividades.getSelectedRow();
        if (fila < 0) {
            return;
        }

        Long id = numeroLong(tblActividades.getValueAt(fila, 0));
        Ejercicio e = ejercicioControlador.buscar(id);
        if (e == null) {
            return;
        }

        idActividadSeleccionada = e.getIdEjercicio();
        txtActividadNombre.setText(e.getNombreEjercicio());
        cboActividadTipo.setSelectedItem(e.getTipoEjercicio());
        cboActividadNivel.setSelectedItem(e.getNivelDificultad());
        txtActividadDescripcion.setText(e.getDescripcion());
        txtActividadInstrucciones.setText(e.getInstrucciones());
    }

    private void limpiarActividad() {
        idActividadSeleccionada = null;
        txtActividadNombre.setText("");
        cboActividadTipo.setSelectedIndex(0);
        cboActividadNivel.setSelectedIndex(0);
        txtActividadDescripcion.setText("");
        txtActividadInstrucciones.setText("");
        tblActividades.clearSelection();
    }

    private void agregarDetalleTemporal() {

        Ejercicio actividad = (Ejercicio) cboActividadRutina.getSelectedItem();
        if (actividad == null) {
            error("Primero crea o selecciona una actividad.");
            return;
        }

        try {
            String dia = String.valueOf(cboDia.getSelectedItem());
            int orden = enteroObligatorio(txtOrden, "orden");
            int series = enteroObligatorio(txtSeries, "series");
            String repeticiones = txtRepeticiones.getText().trim();

            if (repeticiones.isEmpty()) {
                throw new IllegalArgumentException(
                        "Ingresa las repeticiones.");
            }

            BigDecimal peso = decimalOpcional(txtPeso);
            Integer duracion = enteroOpcional(txtDuracion);
            Integer descanso = enteroOpcional(txtDescanso);

            for (DetalleTemporal d : detalleTemporal) {
                if (d.dia.equals(dia) && d.orden == orden) {
                    error("Ya existe una actividad con ese orden en " + dia + ".");
                    return;
                }
            }

            FlujoEntrenadorControlador.EjercicioDia item
                    = new FlujoEntrenadorControlador.EjercicioDia(
                            dia, orden, series, repeticiones,
                            descanso, actividad.getIdEjercicio().intValue());

            item.pesoSugerido = peso;
            item.duracionMinutos = duracion;

            detalleTemporal.add(new DetalleTemporal(
                    item, actividad.getNombreEjercicio()));

            refrescarDetalleTemporal();

            txtOrden.setText(
                    String.valueOf(siguienteOrden(dia)));
            txtPeso.setText("");
            txtDuracion.setText("");

        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void quitarDetalleTemporal() {
        int fila = tblDetalleTemporal.getSelectedRow();
        if (fila < 0) {
            error("Selecciona una actividad del detalle.");
            return;
        }
        detalleTemporal.remove(fila);
        refrescarDetalleTemporal();
    }

    private int siguienteOrden(String dia) {
        int max = 0;
        for (DetalleTemporal d : detalleTemporal) {
            if (d.dia.equals(dia)) {
                max = Math.max(max, d.orden);
            }
        }
        return max + 1;
    }

    private void refrescarDetalleTemporal() {

        DefaultTableModel m = modelo(tblDetalleTemporal);
        m.setRowCount(0);

        for (DetalleTemporal d : detalleTemporal) {
            m.addRow(new Object[]{
                d.dia,
                d.orden,
                d.nombreActividad,
                d.series,
                d.repeticiones,
                d.pesoSugerido,
                d.duracionMinutos,
                d.descansoSegundos
            });
        }
    }

    private void guardarRutinaCompleta() {

        String nombre = txtRutinaNombre.getText().trim();
        if (nombre.isEmpty()) {
            error("Ingresa el nombre de la rutina.");
            return;
        }

        if (detalleTemporal.isEmpty()) {
            error("Agrega al menos una actividad al detalle de la rutina.");
            return;
        }

        try {
            int semanas = enteroObligatorio(
                    txtRutinaSemanas, "duración en semanas");

            List<FlujoEntrenadorControlador.EjercicioDia> ejercicios
                    = new ArrayList<>();

            for (DetalleTemporal d : detalleTemporal) {
                ejercicios.add(d.flujo);
            }

            Long idRutina = flujoEntrenador.crearRutinaCompleta(
                    idEntrenador,
                    nombre,
                    txtRutinaDescripcion.getText().trim(),
                    String.valueOf(cboRutinaNivel.getSelectedItem()),
                    semanas,
                    ejercicios
            );

            if (idRutina == null) {
                error(flujoEntrenador.getMensaje());
                return;
            }

            mensaje("Rutina guardada con " + detalleTemporal.size()
                    + " actividades. ID: " + idRutina);
            limpiarRutina();
            cargarRutinas();

        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void limpiarRutina() {
        txtRutinaNombre.setText("");
        cboRutinaNivel.setSelectedIndex(0);
        txtRutinaSemanas.setText("4");
        txtRutinaDescripcion.setText("");
        detalleTemporal.clear();
        refrescarDetalleTemporal();
        txtOrden.setText("1");
        txtSeries.setText("3");
        txtRepeticiones.setText("10");
        txtPeso.setText("");
        txtDuracion.setText("");
        txtDescanso.setText("60");
    }

    private void cargarRutinas() {

        List<Rutina> rutinas =
                rutinaControlador.listarPorIdEntrenador(idEntrenador);

        DefaultTableModel m = modelo(tblRutinas);
        m.setRowCount(0);

        DefaultComboBoxModel<Rutina> combo = new DefaultComboBoxModel<>();

        for (Rutina r : rutinas) {
            int cantidadEjercicios = detalleControlador
                    .listarPorIdRutina(r.getIdRutina()).size();

            m.addRow(new Object[]{
                r.getIdRutina(),
                r.getNombreRutina(),
                r.getNivel(),
                r.getDuracionSemanas(),
                r.getEstadoRutina(),
                cantidadEjercicios,
                "Ver detalle"
            });

            if ("ACTIVA".equalsIgnoreCase(r.getEstadoRutina())) {
                combo.addElement(r);
            }
        }

        cboRutinaAsignar.setModel(combo);
    }

    private void crearRutinaDesdeMisRutinas() {

        List<Ejercicio> actividades = new ArrayList<>(
                ejercicioControlador.listarDisponiblesParaEntrenador(
                        idEntrenador, ""));

        if (actividades.isEmpty()) {
            error("Primero crea al menos una actividad en la pestaña Mis actividades.");
            return;
        }

        boolean guardado = DlgRutinaEntrenador.mostrarCrear(
                this, idEntrenador, actividades, flujoEntrenador);

        if (guardado) {
            cargarRutinas();
        }
    }

    private void editarRutinaDesdeMisRutinas() {

        if (idRutinaSeleccionada == null) {
            error("Selecciona una rutina de la tabla para editarla.");
            return;
        }

        Rutina rutina = rutinaControlador.buscar(idRutinaSeleccionada);
        if (rutina == null) {
            error("La rutina seleccionada ya no existe.");
            limpiarEdicionRutina();
            return;
        }
        if (!idEntrenador.equals(rutina.getIdEntrenador())) {
            error("Solo puedes editar rutinas creadas por ti.");
            return;
        }

        List<ContieneEjercicio> detalle =
                detalleControlador.listarPorIdRutina(idRutinaSeleccionada);
        List<Ejercicio> actividades = new ArrayList<>(
                ejercicioControlador.listarDisponiblesParaEntrenador(
                        idEntrenador, ""));

        // Mantiene visibles las actividades ya usadas aunque hayan dejado de
        // estar disponibles en el catálogo actual.
        for (ContieneEjercicio d : detalle) {
            boolean existe = false;
            for (Ejercicio e : actividades) {
                if (e.getIdEjercicio() != null
                        && e.getIdEjercicio().equals(d.getIdEjercicio())) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                Ejercicio usada = ejercicioControlador.buscar(d.getIdEjercicio());
                if (usada != null) {
                    actividades.add(usada);
                }
            }
        }

        boolean guardado = DlgRutinaEntrenador.mostrarEditar(
                this, idEntrenador, rutina, detalle,
                actividades, flujoEntrenador);

        if (guardado) {
            Long idEditada = idRutinaSeleccionada;
            cargarRutinas();
            for (int i = 0; i < tblRutinas.getRowCount(); i++) {
                Long id = numeroLong(tblRutinas.getValueAt(i, 0));
                if (idEditada.equals(id)) {
                    tblRutinas.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void cargarRutinaSeleccionadaParaEdicion() {

        int filaVista = tblRutinas.getSelectedRow();
        if (filaVista < 0) {
            return;
        }

        int fila = tblRutinas.convertRowIndexToModel(filaVista);
        Long idRutina = numeroLong(tblRutinas.getModel().getValueAt(fila, 0));
        Rutina rutina = rutinaControlador.buscar(idRutina);

        if (rutina == null) {
            error(rutinaControlador.getMensaje());
            limpiarEdicionRutina();
            return;
        }

        if (!idEntrenador.equals(rutina.getIdEntrenador())) {
            error("Solo puedes editar rutinas creadas por ti.");
            limpiarEdicionRutina();
            return;
        }

        idRutinaSeleccionada = rutina.getIdRutina();
        lblRutinaSeleccionada.setText(
                "Editando rutina N° " + rutina.getIdRutina()
                + " - " + rutina.getNombreRutina());
        txtEditarRutinaNombre.setText(rutina.getNombreRutina());
        cboEditarRutinaNivel.setSelectedItem(rutina.getNivel());
        txtEditarRutinaSemanas.setText(
                String.valueOf(rutina.getDuracionSemanas()));
        txtEditarRutinaDescripcion.setText(
                rutina.getDescripcion() == null ? "" : rutina.getDescripcion());
    }

    private void guardarCambiosRutina() {

        if (idRutinaSeleccionada == null) {
            error("Selecciona una rutina de la tabla para editarla.");
            return;
        }

        String nombre = txtEditarRutinaNombre.getText().trim();
        if (nombre.isEmpty()) {
            error("Ingresa el nombre de la rutina.");
            return;
        }

        try {
            int semanas = enteroObligatorio(
                    txtEditarRutinaSemanas, "duración en semanas");

            Rutina guardada = rutinaControlador.buscar(idRutinaSeleccionada);
            if (guardada == null) {
                error("La rutina seleccionada ya no existe.");
                limpiarEdicionRutina();
                return;
            }

            if (!idEntrenador.equals(guardada.getIdEntrenador())) {
                error("Solo puedes editar rutinas creadas por ti.");
                limpiarEdicionRutina();
                return;
            }

            guardada.setNombreRutina(nombre);
            guardada.setNivel(String.valueOf(
                    cboEditarRutinaNivel.getSelectedItem()));
            guardada.setDuracionSemanas(semanas);
            guardada.setDescripcion(
                    txtEditarRutinaDescripcion.getText().trim());

            if (!rutinaControlador.modificar(guardada)) {
                error(rutinaControlador.getMensaje());
                return;
            }

            mensaje("Rutina modificada correctamente.");
            cargarRutinas();
            limpiarEdicionRutina();

        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private void limpiarEdicionRutina() {
        idRutinaSeleccionada = null;
        lblRutinaSeleccionada.setText(
                "Selecciona una rutina de la tabla para editarla.");
        txtEditarRutinaNombre.setText("");
        cboEditarRutinaNivel.setSelectedIndex(0);
        txtEditarRutinaSemanas.setText("");
        txtEditarRutinaDescripcion.setText("");
        tblRutinas.clearSelection();
    }

    private void editarDetalleRutinaSeleccionada() {

        if (idRutinaSeleccionada == null) {
            error("Selecciona una rutina de la tabla para editar su detalle.");
            return;
        }

        Rutina rutina = rutinaControlador.buscar(idRutinaSeleccionada);
        if (rutina == null) {
            error("La rutina seleccionada ya no existe.");
            limpiarEdicionRutina();
            return;
        }
        if (!idEntrenador.equals(rutina.getIdEntrenador())) {
            error("Solo puedes modificar el detalle de rutinas creadas por ti.");
            return;
        }

        List<ContieneEjercicio> detalle =
                detalleControlador.listarPorIdRutina(idRutinaSeleccionada);
        List<Ejercicio> actividades = new ArrayList<>(
                ejercicioControlador.listarDisponiblesParaEntrenador(
                        idEntrenador, ""));

        // Conserva en el combo cualquier actividad ya usada aunque haya dejado
        // de aparecer en el catalogo disponible.
        for (ContieneEjercicio d : detalle) {
            boolean existe = false;
            for (Ejercicio e : actividades) {
                if (e.getIdEjercicio() != null
                        && e.getIdEjercicio().equals(d.getIdEjercicio())) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                Ejercicio usada = ejercicioControlador.buscar(d.getIdEjercicio());
                if (usada != null) {
                    actividades.add(usada);
                }
            }
        }

        boolean actualizado = DlgEditarDetalleRutina.mostrar(
                this,
                idEntrenador,
                rutina.getIdRutina(),
                rutina.getNombreRutina(),
                detalle,
                actividades,
                flujoEntrenador);

        if (actualizado) {
            cargarRutinas();
            // Mantener seleccionada la rutina editada si sigue visible.
            for (int i = 0; i < tblRutinas.getRowCount(); i++) {
                Long id = numeroLong(tblRutinas.getValueAt(i, 0));
                if (idRutinaSeleccionada.equals(id)) {
                    tblRutinas.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void mostrarDetalleRutinaFila(int filaVista) {

        if (filaVista < 0 || filaVista >= tblRutinas.getRowCount()) {
            return;
        }

        int fila = tblRutinas.convertRowIndexToModel(filaVista);
        Long idRutina = numeroLong(tblRutinas.getModel().getValueAt(fila, 0));
        String nombre = String.valueOf(tblRutinas.getModel().getValueAt(fila, 1));
        String nivel = String.valueOf(tblRutinas.getModel().getValueAt(fila, 2));

        Integer semanas = null;
        Object valorSemanas = tblRutinas.getModel().getValueAt(fila, 3);
        if (valorSemanas instanceof Number numero) {
            semanas = numero.intValue();
        }

        String estado = String.valueOf(tblRutinas.getModel().getValueAt(fila, 4));

        List<ContieneEjercicio> detalle =
                detalleControlador.listarPorIdRutina(idRutina);
        List<RutinaDetalleAdministrador> detalleVista = new ArrayList<>();

        for (ContieneEjercicio d : detalle) {
            Ejercicio e = ejercicioControlador.buscar(d.getIdEjercicio());

            RutinaDetalleAdministrador item = new RutinaDetalleAdministrador();
            item.setDiaSemana(d.getDiaSemana());
            item.setOrden(d.getOrden());
            item.setNombreEjercicio(e == null
                    ? "Actividad #" + d.getIdEjercicio()
                    : e.getNombreEjercicio());
            item.setTipoEjercicio(e == null ? "-" : e.getTipoEjercicio());
            item.setNivelDificultad(e == null ? "-" : e.getNivelDificultad());
            item.setSeries(d.getSeries());
            item.setRepeticiones(d.getRepeticiones());
            item.setPesoSugerido(d.getPesoSugerido());
            item.setDuracionMinutos(d.getDuracionMinutos());
            item.setDescansoSegundos(d.getDescansoSegundos());
            detalleVista.add(item);
        }

        tblRutinas.setRowSelectionInterval(filaVista, filaVista);

        String entrenador = SesionUsuario.getUsuarioActual().getNombreUsuario();
        DlgDetalleRutina.mostrar(
                this, idRutina, nombre, entrenador, nivel,
                semanas, estado, detalleVista);
    }

    private void cargarClientes() {

        List<Cliente> clientes =
                clienteControlador.listarPorEntrenador(idEntrenador, "");

        DefaultComboBoxModel<Cliente> m = new DefaultComboBoxModel<>();

        for (Cliente c : clientes) {
            if (c.isEstado() && c.isEstadoCliente()) {
                m.addElement(c);
            }
        }

        cboCliente.setModel(m);
    }

    private java.util.List<String> diasRutinaSeleccionada() {
        Object item = cboRutinaAsignar.getSelectedItem();
        if (!(item instanceof Rutina rutina) || rutina.getIdRutina() == null) {
            return java.util.Collections.emptyList();
        }
        java.util.Set<String> dias = new java.util.LinkedHashSet<>();
        for (ContieneEjercicio d : detalleControlador
                .listarPorIdRutina(rutina.getIdRutina())) {
            if (d.getDiaSemana() != null && !d.getDiaSemana().isBlank()) {
                dias.add(d.getDiaSemana().trim().toUpperCase());
            }
        }
        java.util.List<String> orden = java.util.Arrays.asList(
                "LUNES", "MARTES", "MIERCOLES", "JUEVES",
                "VIERNES", "SABADO", "DOMINGO");
        java.util.List<String> salida = new java.util.ArrayList<>(dias);
        salida.sort(java.util.Comparator.comparingInt(d -> {
            int i = orden.indexOf(d);
            return i < 0 ? 99 : i;
        }));
        return salida;
    }

    private void actualizarResumenDiasAsignacion() {
        if (detalleControlador == null) {
            return;
        }
        java.util.List<String> dias = diasRutinaSeleccionada();
        lblDiasRutinaAsignar.setText(dias.isEmpty()
                ? "Días con actividades: la rutina no tiene detalle guardado."
                : "Días con actividades: " + String.join(", ", dias));
    }

    private String diaRutina(LocalDate fecha) {
        String[] dias = {"LUNES", "MARTES", "MIERCOLES", "JUEVES",
            "VIERNES", "SABADO", "DOMINGO"};
        return dias[fecha.getDayOfWeek().getValue() - 1];
    }

    private void asignarRutina() {

        Cliente cliente = (Cliente) cboCliente.getSelectedItem();
        Rutina rutina = (Rutina) cboRutinaAsignar.getSelectedItem();

        if (cliente == null || rutina == null) {
            error("Selecciona un cliente y una rutina.");
            return;
        }
        if (!idEntrenador.equals(rutina.getIdEntrenador())) {
            error("Solo puedes asignar rutinas creadas por ti.");
            return;
        }
        java.util.List<String> dias = diasRutinaSeleccionada();
        if (dias.isEmpty()) {
            error("La rutina seleccionada no tiene actividades guardadas. "
                    + "Agrega al menos una actividad antes de asignarla.");
            return;
        }

        Long idAsignacion = flujoEntrenador.asignarRutina(
                cliente.getIdPersona(),
                rutina.getIdRutina(),
                txtObservacionesAsignacion.getText().trim());
        if (idAsignacion == null) {
            error(flujoEntrenador.getMensaje());
            return;
        }

        mensaje(flujoEntrenador.getMensaje());
        txtObservacionesAsignacion.setText("");
        cargarAsignaciones();
    }

    private void cargarAsignaciones() {
        DefaultTableModel m = modelo(tblAsignaciones);
        m.setRowCount(0);
        for (FlujoEntrenadorControlador.AsignacionRutinaVista a
                : flujoEntrenador.listarAsignacionesEntrenador(idEntrenador)) {
            m.addRow(new Object[]{
                a.idAsignacion, a.cliente, a.rutina, a.dias, a.estado
            });
        }
    }

    private JPanel panelBlanco(java.awt.LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        return p;
    }

    private void agregarCampo(
            JPanel panel,
            int fila,
            String etiqueta1,
            java.awt.Component campo1,
            String etiqueta2,
            java.awt.Component campo2
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

    private JButton boton(String texto) {
        JButton b = new JButton(texto);
        b.setBackground(new java.awt.Color(8, 124, 255));
        b.setForeground(java.awt.Color.WHITE);
        b.setFocusPainted(false);
        return b;
    }

    private JButton botonSecundario(String texto) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        return b;
    }

    private void configurarTablaRutinasGuardadas() {
        tblRutinas.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Rutina", "Nivel", "Semanas", "Estado",
                    "Ejercicios", "Detalle"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        });
        tblRutinas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRutinas.setRowHeight(32);
        tblRutinas.getColumnModel().getColumn(6)
                .setCellRenderer(new BotonDetalleRenderer());
        tblRutinas.getColumnModel().getColumn(6)
                .setCellEditor(new BotonDetalleEditor());
    }

    private class BotonDetalleRenderer extends JButton
            implements TableCellRenderer {

        BotonDetalleRenderer() {
            setText("Ver detalle");
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            setText("Ver detalle");
            return this;
        }
    }

    private class BotonDetalleEditor extends AbstractCellEditor
            implements TableCellEditor {

        private final JButton boton = new JButton("Ver detalle");
        private int fila;

        BotonDetalleEditor() {
            boton.setFocusPainted(false);
            boton.addActionListener((ActionEvent e) -> {
                fireEditingStopped();
                mostrarDetalleRutinaFila(fila);
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, Object value, boolean isSelected,
                int row, int column) {
            fila = row;
            return boton;
        }

        @Override
        public Object getCellEditorValue() {
            return "Ver detalle";
        }
    }

    private void configurarTabla(JTable tabla, String[] columnas) {
        tabla.setModel(new DefaultTableModel(new Object[][]{}, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);
    }

    private DefaultTableModel modelo(JTable tabla) {
        return (DefaultTableModel) tabla.getModel();
    }

    private int enteroObligatorio(JTextField campo, String nombre) {
        try {
            int valor = Integer.parseInt(campo.getText().trim());
            if (valor <= 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El valor de " + nombre + " debe ser un entero mayor a cero.");
        }
    }

    private Integer enteroOpcional(JTextField campo) {
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
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Revisa los valores numéricos del detalle.");
        }
    }

    private BigDecimal decimalOpcional(JTextField campo) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            BigDecimal valor = new BigDecimal(texto);
            if (valor.signum() < 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El peso sugerido debe ser un número válido.");
        }
    }

    private Long numeroLong(Object valor) {
        if (valor instanceof Number n) {
            return n.longValue();
        }
        return Long.valueOf(String.valueOf(valor));
    }

    private void mensaje(String texto) {
        JOptionPane.showMessageDialog(
                this, texto, "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String texto) {
        JOptionPane.showMessageDialog(
                this,
                texto == null || texto.isBlank() ? "No se pudo realizar la operación." : texto,
                "GYMNOVA",
                JOptionPane.WARNING_MESSAGE);
    }

    private static class DetalleTemporal {

        final FlujoEntrenadorControlador.EjercicioDia flujo;
        final String dia;
        final int orden;
        final int series;
        final String repeticiones;
        final BigDecimal pesoSugerido;
        final Integer duracionMinutos;
        final Integer descansoSegundos;
        final String nombreActividad;

        DetalleTemporal(
                FlujoEntrenadorControlador.EjercicioDia flujo,
                String nombreActividad
        ) {
            this.flujo = flujo;
            this.dia = flujo.diaSemana;
            this.orden = flujo.orden;
            this.series = flujo.series;
            this.repeticiones = flujo.repeticiones;
            this.pesoSugerido = flujo.pesoSugerido;
            this.duracionMinutos = flujo.duracionMinutos;
            this.descansoSegundos = flujo.descansoSegundos;
            this.nombreActividad = nombreActividad;
        }
    }
}
