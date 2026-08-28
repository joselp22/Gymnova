package vista;

import controlador.NutricionAdministracionControlador;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.Alimento;
import modelo.IndicadorSalud;

/**
 * Vista exclusiva del Administrador para Nutricion.
 *
 * El Administrador:
 * - supervisa clientes, planes, comidas, resultados, recomendaciones y
 *   nutricionistas en modo solo lectura;
 * - administra los catalogos globales de alimentos e indicadores de salud.
 *
 * Las operaciones profesionales de nutricion se realizan desde
 * {@link PnlNutricion} con el rol Nutricionista.
 */
public class PnlNutricionAdministrador extends JPanel {

    private final NutricionAdministracionControlador controlador
            = new NutricionAdministracionControlador();

    private final JLabel lblClientes = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblPlanes = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblNutricionistas = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblAlimentos = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblIndicadores = new JLabel("0", SwingConstants.CENTER);

    private final JComboBox<String> cboSupervision = new JComboBox<>(new String[]{
        NutricionAdministracionControlador.VISTA_PLANES,
        NutricionAdministracionControlador.VISTA_CLIENTES,
        NutricionAdministracionControlador.VISTA_COMIDAS,
        NutricionAdministracionControlador.VISTA_RESULTADOS,
        NutricionAdministracionControlador.VISTA_RECOMENDACIONES,
        NutricionAdministracionControlador.VISTA_NUTRICIONISTAS
    });
    private final JTextField txtBuscarSupervision = new JTextField();
    private final JButton btnBuscarSupervision = new JButton("Buscar");
    private final JButton btnActualizarSupervision = new JButton("Actualizar");
    private final JLabel lblCantidadSupervision = new JLabel("0 registros");
    private final JTable tblSupervision = new JTable();

    private final JTextField txtBuscarAlimento = new JTextField();
    private final JTable tblAlimentos = new JTable();
    private final JLabel lblCantidadAlimentos = new JLabel("0 alimentos");
    private final JTextField txtNombreAlimento = new JTextField();
    private final JTextField txtCategoriaAlimento = new JTextField();
    private final JTextField txtPorcionAlimento = new JTextField();
    private final JTextField txtProteinasAlimento = new JTextField();
    private final JTextField txtCarbohidratosAlimento = new JTextField();
    private final JTextField txtFibraAlimento = new JTextField();
    private final JTextArea txtDescripcionAlimento = new JTextArea(4, 20);
    private final JButton btnGuardarAlimento = new JButton("Guardar");
    private final JButton btnModificarAlimento = new JButton("Modificar");
    private final JButton btnEliminarAlimento = new JButton("Eliminar");
    private final JButton btnLimpiarAlimento = new JButton("Limpiar");
    private Alimento alimentoSeleccionado;

    private final JTextField txtBuscarIndicador = new JTextField();
    private final JTable tblIndicadores = new JTable();
    private final JLabel lblCantidadIndicadores = new JLabel("0 indicadores");
    private final JTextField txtNombreIndicador = new JTextField();
    private final JTextField txtUnidadIndicador = new JTextField();
    private final JTextField txtMinimoIndicador = new JTextField();
    private final JTextField txtMaximoIndicador = new JTextField();
    private final JComboBox<String> cboCategoriaIndicador = new JComboBox<>(new String[]{
        "GENERAL", "CORPORAL", "CARDIOVASCULAR", "NUTRICIONAL"
    });
    private final JTextArea txtDescripcionIndicador = new JTextArea(4, 20);
    private final JCheckBox chkIndicadorActivo = new JCheckBox("Indicador activo", true);
    private final JButton btnGuardarIndicador = new JButton("Guardar");
    private final JButton btnModificarIndicador = new JButton("Modificar");
    private final JButton btnDesactivarIndicador = new JButton("Desactivar");
    private final JButton btnEliminarIndicador = new JButton("Eliminar");
    private final JButton btnLimpiarIndicador = new JButton("Limpiar");
    private IndicadorSalud indicadorSeleccionado;

    public PnlNutricionAdministrador() {
        construirVista();
        configurarEventos();
        refrescarDatos();
    }

    public final void refrescarDatos() {
        cargarResumen();
        cargarSupervision();
        cargarAlimentos();
        cargarIndicadores();
    }

    private void construirVista() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new java.awt.Color(234, 242, 251));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel norte = new JPanel();
        norte.setOpaque(false);
        norte.setLayout(new BoxLayout(norte, BoxLayout.Y_AXIS));
        norte.add(crearEncabezado());
        norte.add(javax.swing.Box.createVerticalStrut(12));
        norte.add(crearResumen());
        add(norte, BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("SansSerif", Font.BOLD, 13));
        pestanas.addTab("Supervision", crearPanelSupervision());
        pestanas.addTab("Catalogo de alimentos", crearPanelAlimentos());
        pestanas.addTab("Indicadores de salud", crearPanelIndicadores());
        add(pestanas, BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(java.awt.Color.WHITE);
        encabezado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Nutricion - supervision administrativa");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setForeground(new java.awt.Color(23, 42, 67));

        JLabel subtitulo = new JLabel(
                "Supervise la atencion nutricional y administre los catalogos globales del sistema.");
        subtitulo.setForeground(new java.awt.Color(90, 110, 135));

        textos.add(titulo);
        textos.add(javax.swing.Box.createVerticalStrut(4));
        textos.add(subtitulo);

        JLabel modulo = new JLabel("NUTRICION", SwingConstants.CENTER);
        modulo.setForeground(new java.awt.Color(8, 124, 255));
        modulo.setBorder(BorderFactory.createLineBorder(
                new java.awt.Color(8, 124, 255)));
        modulo.setPreferredSize(new Dimension(110, 45));

        encabezado.add(textos, BorderLayout.CENTER);
        encabezado.add(modulo, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearResumen() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 10, 0));
        panel.setOpaque(false);
        panel.add(crearTarjeta("Clientes activos", lblClientes));
        panel.add(crearTarjeta("Planes activos", lblPlanes));
        panel.add(crearTarjeta("Nutricionistas", lblNutricionistas));
        panel.add(crearTarjeta("Alimentos", lblAlimentos));
        panel.add(crearTarjeta("Indicadores activos", lblIndicadores));
        return panel;
    }

    private JPanel crearTarjeta(String titulo, JLabel valor) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 6));
        tarjeta.setBackground(java.awt.Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel etiqueta = new JLabel(titulo, SwingConstants.CENTER);
        etiqueta.setForeground(new java.awt.Color(90, 110, 135));
        valor.setFont(new Font("SansSerif", Font.BOLD, 24));
        valor.setForeground(new java.awt.Color(23, 42, 67));

        tarjeta.add(valor, BorderLayout.CENTER);
        tarjeta.add(etiqueta, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearPanelSupervision() {
        JPanel panel = panelBlanco();
        panel.setLayout(new BorderLayout(10, 10));

        JPanel superior = new JPanel(new BorderLayout(10, 8));
        superior.setOpaque(false);

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        selector.setOpaque(false);
        selector.add(new JLabel("Vista:"));
        cboSupervision.setPreferredSize(new Dimension(250, 36));
        utilidades.EstilosComponentes.aplicarComboRedondeado(cboSupervision);
        selector.add(cboSupervision);
        selector.add(lblCantidadSupervision);

        txtBuscarSupervision.setToolTipText(
                "Buscar dentro de la informacion actualmente seleccionada");
        utilidades.EstilosComponentes.aplicarCampoSimple(txtBuscarSupervision);

        estilizarBotonPrincipal(btnBuscarSupervision);
        estilizarBotonSecundario(btnActualizarSupervision);

        JPanel buscador = new JPanel(new BorderLayout(8, 0));
        buscador.setOpaque(false);
        buscador.add(txtBuscarSupervision, BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(btnBuscarSupervision);
        botones.add(btnActualizarSupervision);
        buscador.add(botones, BorderLayout.EAST);

        superior.add(selector, BorderLayout.NORTH);
        superior.add(buscador, BorderLayout.CENTER);

        configurarTabla(tblSupervision);
        panel.add(superior, BorderLayout.NORTH);
        panel.add(new JScrollPane(tblSupervision), BorderLayout.CENTER);

        JLabel ayuda = new JLabel(
                "Vista de solo lectura: la creacion y edicion de planes, resultados y recomendaciones corresponde al Nutricionista.");
        ayuda.setForeground(new java.awt.Color(90, 110, 135));
        panel.add(ayuda, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelAlimentos() {
        JPanel panel = panelBlanco();
        panel.setLayout(new BorderLayout(10, 10));

        JPanel formulario = crearFormularioAlimento();
        JPanel listado = new JPanel(new BorderLayout(8, 8));
        listado.setOpaque(false);

        JPanel buscador = new JPanel(new BorderLayout(8, 0));
        buscador.setOpaque(false);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtBuscarAlimento);
        txtBuscarAlimento.setToolTipText("Buscar alimento por nombre o categoria");
        JButton btnBuscar = new JButton("Buscar");
        estilizarBotonPrincipal(btnBuscar);
        btnBuscar.addActionListener(e -> cargarAlimentos());
        txtBuscarAlimento.addActionListener(e -> cargarAlimentos());
        buscador.add(txtBuscarAlimento, BorderLayout.CENTER);
        buscador.add(btnBuscar, BorderLayout.EAST);

        JPanel cabeceraListado = new JPanel(new BorderLayout());
        cabeceraListado.setOpaque(false);
        cabeceraListado.add(buscador, BorderLayout.CENTER);
        cabeceraListado.add(lblCantidadAlimentos, BorderLayout.SOUTH);

        configurarTabla(tblAlimentos);
        listado.add(cabeceraListado, BorderLayout.NORTH);
        listado.add(new JScrollPane(tblAlimentos), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, formulario, listado);
        split.setResizeWeight(0.34);
        split.setDividerLocation(380);
        split.setBorder(null);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioAlimento() {
        JPanel formulario = new JPanel();
        formulario.setBackground(java.awt.Color.WHITE);
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Mantenimiento de alimentos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(new java.awt.Color(23, 42, 67));
        formulario.add(titulo);
        formulario.add(javax.swing.Box.createVerticalStrut(10));

        formulario.add(campo("Nombre *", txtNombreAlimento));
        formulario.add(campo("Categoria", txtCategoriaAlimento));
        formulario.add(campo("Porcion referencia (g)", txtPorcionAlimento));
        formulario.add(campo("Proteinas (g)", txtProteinasAlimento));
        formulario.add(campo("Carbohidratos (g)", txtCarbohidratosAlimento));
        formulario.add(campo("Fibra (g)", txtFibraAlimento));

        JLabel lblDescripcion = new JLabel("Descripcion");
        formulario.add(lblDescripcion);
        txtDescripcionAlimento.setLineWrap(true);
        txtDescripcionAlimento.setWrapStyleWord(true);
        formulario.add(new JScrollPane(txtDescripcionAlimento));
        formulario.add(javax.swing.Box.createVerticalStrut(10));

        estilizarBotonPrincipal(btnGuardarAlimento);
        estilizarBotonModificar(btnModificarAlimento);
        estilizarBotonPeligro(btnEliminarAlimento);
        estilizarBotonSecundario(btnLimpiarAlimento);

        JPanel acciones = new JPanel(new GridLayout(2, 2, 8, 8));
        acciones.setOpaque(false);
        acciones.add(btnGuardarAlimento);
        acciones.add(btnModificarAlimento);
        acciones.add(btnLimpiarAlimento);
        acciones.add(btnEliminarAlimento);
        formulario.add(acciones);

        btnModificarAlimento.setEnabled(false);
        btnEliminarAlimento.setEnabled(false);
        return formulario;
    }

    private JPanel crearPanelIndicadores() {
        JPanel panel = panelBlanco();
        panel.setLayout(new BorderLayout(10, 10));

        JPanel formulario = crearFormularioIndicador();
        JPanel listado = new JPanel(new BorderLayout(8, 8));
        listado.setOpaque(false);

        JPanel buscador = new JPanel(new BorderLayout(8, 0));
        buscador.setOpaque(false);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtBuscarIndicador);
        txtBuscarIndicador.setToolTipText("Buscar indicador por nombre o categoria");
        JButton btnBuscar = new JButton("Buscar");
        estilizarBotonPrincipal(btnBuscar);
        btnBuscar.addActionListener(e -> cargarIndicadores());
        txtBuscarIndicador.addActionListener(e -> cargarIndicadores());
        buscador.add(txtBuscarIndicador, BorderLayout.CENTER);
        buscador.add(btnBuscar, BorderLayout.EAST);

        JPanel cabeceraListado = new JPanel(new BorderLayout());
        cabeceraListado.setOpaque(false);
        cabeceraListado.add(buscador, BorderLayout.CENTER);
        cabeceraListado.add(lblCantidadIndicadores, BorderLayout.SOUTH);

        configurarTabla(tblIndicadores);
        listado.add(cabeceraListado, BorderLayout.NORTH);
        listado.add(new JScrollPane(tblIndicadores), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, formulario, listado);
        split.setResizeWeight(0.34);
        split.setDividerLocation(380);
        split.setBorder(null);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearFormularioIndicador() {
        JPanel formulario = new JPanel();
        formulario.setBackground(java.awt.Color.WHITE);
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Mantenimiento de indicadores");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(new java.awt.Color(23, 42, 67));
        formulario.add(titulo);
        formulario.add(javax.swing.Box.createVerticalStrut(10));

        formulario.add(campo("Nombre *", txtNombreIndicador));
        formulario.add(campo("Unidad de medida *", txtUnidadIndicador));
        formulario.add(campo("Valor minimo *", txtMinimoIndicador));
        formulario.add(campo("Valor maximo *", txtMaximoIndicador));

        JPanel categoria = new JPanel(new BorderLayout(0, 3));
        categoria.setOpaque(false);
        categoria.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        categoria.add(new JLabel("Categoria"), BorderLayout.NORTH);
        utilidades.EstilosComponentes.aplicarComboRedondeado(cboCategoriaIndicador);
        categoria.add(cboCategoriaIndicador, BorderLayout.CENTER);
        formulario.add(categoria);
        formulario.add(javax.swing.Box.createVerticalStrut(6));

        formulario.add(chkIndicadorActivo);
        formulario.add(javax.swing.Box.createVerticalStrut(6));
        formulario.add(new JLabel("Descripcion"));
        txtDescripcionIndicador.setLineWrap(true);
        txtDescripcionIndicador.setWrapStyleWord(true);
        formulario.add(new JScrollPane(txtDescripcionIndicador));
        formulario.add(javax.swing.Box.createVerticalStrut(10));

        estilizarBotonPrincipal(btnGuardarIndicador);
        estilizarBotonModificar(btnModificarIndicador);
        estilizarBotonAdvertencia(btnDesactivarIndicador);
        estilizarBotonPeligro(btnEliminarIndicador);
        estilizarBotonSecundario(btnLimpiarIndicador);

        JPanel acciones = new JPanel(new GridLayout(3, 2, 8, 8));
        acciones.setOpaque(false);
        acciones.add(btnGuardarIndicador);
        acciones.add(btnModificarIndicador);
        acciones.add(btnDesactivarIndicador);
        acciones.add(btnLimpiarIndicador);
        acciones.add(btnEliminarIndicador);
        formulario.add(acciones);

        btnModificarIndicador.setEnabled(false);
        btnDesactivarIndicador.setEnabled(false);
        btnEliminarIndicador.setEnabled(false);
        return formulario;
    }

    private JPanel campo(String etiqueta, JTextField campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        panel.add(new JLabel(etiqueta), BorderLayout.NORTH);
        utilidades.EstilosComponentes.aplicarCampoSimple(campo);
        panel.add(campo, BorderLayout.CENTER);
        return panel;
    }

    private JPanel panelBlanco() {
        JPanel panel = new JPanel();
        panel.setBackground(java.awt.Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        return panel;
    }

    private void configurarEventos() {
        btnBuscarSupervision.addActionListener(e -> cargarSupervision());
        btnActualizarSupervision.addActionListener(e -> {
            txtBuscarSupervision.setText("");
            cargarSupervision();
            cargarResumen();
        });
        txtBuscarSupervision.addActionListener(e -> cargarSupervision());
        cboSupervision.addActionListener(e -> cargarSupervision());

        tblAlimentos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarAlimentoSeleccionado();
            }
        });
        btnGuardarAlimento.addActionListener(e -> guardarAlimento(false));
        btnModificarAlimento.addActionListener(e -> guardarAlimento(true));
        btnEliminarAlimento.addActionListener(e -> eliminarAlimento());
        btnLimpiarAlimento.addActionListener(e -> limpiarAlimento());

        tblIndicadores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarIndicadorSeleccionado();
            }
        });
        btnGuardarIndicador.addActionListener(e -> guardarIndicador(false));
        btnModificarIndicador.addActionListener(e -> guardarIndicador(true));
        btnDesactivarIndicador.addActionListener(e -> desactivarIndicador());
        btnEliminarIndicador.addActionListener(e -> eliminarIndicador());
        btnLimpiarIndicador.addActionListener(e -> limpiarIndicador());
    }

    private void cargarResumen() {
        NutricionAdministracionControlador.Resumen resumen
                = controlador.obtenerResumen();
        lblClientes.setText(String.valueOf(resumen.clientesActivos()));
        lblPlanes.setText(String.valueOf(resumen.planesActivos()));
        lblNutricionistas.setText(String.valueOf(resumen.nutricionistasActivos()));
        lblAlimentos.setText(String.valueOf(resumen.alimentos()));
        lblIndicadores.setText(String.valueOf(resumen.indicadoresActivos()));
    }

    private void cargarSupervision() {
        String vista = String.valueOf(cboSupervision.getSelectedItem());
        String[] columnas = controlador.columnasSupervision(vista);
        DefaultTableModel modelo = modeloNoEditable(columnas);
        List<Object[]> filas = controlador.listarSupervision(
                vista, txtBuscarSupervision.getText());
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
        tblSupervision.setModel(modelo);
        lblCantidadSupervision.setText(filas.size() + " registro(s)");
        ajustarAnchos(tblSupervision);
        mostrarMensajeSiError();
    }

    private void cargarAlimentos() {
        List<Alimento> alimentos = controlador.listarAlimentos(
                txtBuscarAlimento.getText());
        DefaultTableModel modelo = modeloNoEditable(new String[]{
            "ID", "Alimento", "Categoria", "Porcion (g)", "Proteinas",
            "Carbohidratos", "Fibra", "Descripcion"
        });
        for (Alimento alimento : alimentos) {
            modelo.addRow(new Object[]{
                alimento.getIdAlimento(), alimento.getNombreAlimento(),
                alimento.getCategoria(), alimento.getPorcionReferenciaG(),
                alimento.getProteinasG(), alimento.getCarbohidratosG(),
                alimento.getFibraG(), alimento.getDescripcion()
            });
        }
        tblAlimentos.setModel(modelo);
        tblAlimentos.putClientProperty("registros", alimentos);
        lblCantidadAlimentos.setText(alimentos.size() + " alimento(s)");
        ajustarAnchos(tblAlimentos);
        mostrarMensajeSiError();
    }

    @SuppressWarnings("unchecked")
    private void cargarAlimentoSeleccionado() {
        int filaVista = tblAlimentos.getSelectedRow();
        Object valor = tblAlimentos.getClientProperty("registros");
        if (filaVista < 0 || !(valor instanceof List<?> lista)) {
            return;
        }
        int fila = tblAlimentos.convertRowIndexToModel(filaVista);
        if (fila < 0 || fila >= lista.size() || !(lista.get(fila) instanceof Alimento a)) {
            return;
        }
        alimentoSeleccionado = a;
        txtNombreAlimento.setText(valor(a.getNombreAlimento()));
        txtCategoriaAlimento.setText(valor(a.getCategoria()));
        txtPorcionAlimento.setText(valor(a.getPorcionReferenciaG()));
        txtProteinasAlimento.setText(valor(a.getProteinasG()));
        txtCarbohidratosAlimento.setText(valor(a.getCarbohidratosG()));
        txtFibraAlimento.setText(valor(a.getFibraG()));
        txtDescripcionAlimento.setText(valor(a.getDescripcion()));
        btnModificarAlimento.setEnabled(true);
        btnEliminarAlimento.setEnabled(true);
    }

    private void guardarAlimento(boolean modificacion) {
        try {
            Alimento alimento = modificacion && alimentoSeleccionado != null
                    ? alimentoSeleccionado : new Alimento();
            alimento.setNombreAlimento(requerido(
                    txtNombreAlimento.getText(), "Ingrese el nombre del alimento."));
            alimento.setCategoria(opcional(txtCategoriaAlimento.getText()));
            alimento.setPorcionReferenciaG(decimalOpcional(
                    txtPorcionAlimento.getText(), "Porcion"));
            alimento.setProteinasG(decimalOpcional(
                    txtProteinasAlimento.getText(), "Proteinas"));
            alimento.setCarbohidratosG(decimalOpcional(
                    txtCarbohidratosAlimento.getText(), "Carbohidratos"));
            alimento.setFibraG(decimalOpcional(
                    txtFibraAlimento.getText(), "Fibra"));
            alimento.setDescripcion(opcional(txtDescripcionAlimento.getText()));

            boolean ok = controlador.guardarAlimento(alimento, modificacion);
            informar(ok);
            if (ok) {
                limpiarAlimento();
                cargarAlimentos();
                cargarResumen();
            }
        } catch (IllegalArgumentException ex) {
            advertencia(ex.getMessage());
        }
    }

    private void eliminarAlimento() {
        if (alimentoSeleccionado == null) {
            advertencia("Seleccione un alimento.");
            return;
        }
        if (!confirmar("¿Eliminar definitivamente el alimento seleccionado?")) {
            return;
        }
        boolean ok = controlador.eliminarAlimento(
                alimentoSeleccionado.getIdAlimento());
        informar(ok);
        if (ok) {
            limpiarAlimento();
            cargarAlimentos();
            cargarResumen();
        }
    }

    private void limpiarAlimento() {
        alimentoSeleccionado = null;
        tblAlimentos.clearSelection();
        txtNombreAlimento.setText("");
        txtCategoriaAlimento.setText("");
        txtPorcionAlimento.setText("");
        txtProteinasAlimento.setText("");
        txtCarbohidratosAlimento.setText("");
        txtFibraAlimento.setText("");
        txtDescripcionAlimento.setText("");
        btnModificarAlimento.setEnabled(false);
        btnEliminarAlimento.setEnabled(false);
    }

    private void cargarIndicadores() {
        List<IndicadorSalud> indicadores = controlador.listarIndicadores(
                txtBuscarIndicador.getText());
        DefaultTableModel modelo = modeloNoEditable(new String[]{
            "ID", "Indicador", "Unidad", "Minimo", "Maximo",
            "Categoria", "Estado", "Descripcion"
        });
        for (IndicadorSalud indicador : indicadores) {
            modelo.addRow(new Object[]{
                indicador.getIdIndicador(), indicador.getNombreIndicador(),
                indicador.getUnidadMedida(), indicador.getValorMinimoReferencia(),
                indicador.getValorMaximoReferencia(), indicador.getCategoria(),
                indicador.isEstadoIndicador() ? "ACTIVO" : "INACTIVO",
                indicador.getDescripcion()
            });
        }
        tblIndicadores.setModel(modelo);
        tblIndicadores.putClientProperty("registros", indicadores);
        lblCantidadIndicadores.setText(indicadores.size() + " indicador(es)");
        ajustarAnchos(tblIndicadores);
        mostrarMensajeSiError();
    }

    private void cargarIndicadorSeleccionado() {
        int filaVista = tblIndicadores.getSelectedRow();
        Object valor = tblIndicadores.getClientProperty("registros");
        if (filaVista < 0 || !(valor instanceof List<?> lista)) {
            return;
        }
        int fila = tblIndicadores.convertRowIndexToModel(filaVista);
        if (fila < 0 || fila >= lista.size()
                || !(lista.get(fila) instanceof IndicadorSalud i)) {
            return;
        }
        indicadorSeleccionado = i;
        txtNombreIndicador.setText(valor(i.getNombreIndicador()));
        txtUnidadIndicador.setText(valor(i.getUnidadMedida()));
        txtMinimoIndicador.setText(valor(i.getValorMinimoReferencia()));
        txtMaximoIndicador.setText(valor(i.getValorMaximoReferencia()));
        seleccionarTexto(cboCategoriaIndicador, i.getCategoria());
        txtDescripcionIndicador.setText(valor(i.getDescripcion()));
        chkIndicadorActivo.setSelected(i.isEstadoIndicador());
        btnModificarIndicador.setEnabled(true);
        btnDesactivarIndicador.setEnabled(i.isEstadoIndicador());
        btnEliminarIndicador.setEnabled(true);
    }

    private void guardarIndicador(boolean modificacion) {
        try {
            IndicadorSalud indicador = modificacion && indicadorSeleccionado != null
                    ? indicadorSeleccionado : new IndicadorSalud();
            indicador.setNombreIndicador(requerido(
                    txtNombreIndicador.getText(), "Ingrese el nombre del indicador."));
            indicador.setUnidadMedida(requerido(
                    txtUnidadIndicador.getText(), "Ingrese la unidad de medida."));
            indicador.setValorMinimoReferencia(decimalRequerido(
                    txtMinimoIndicador.getText(), "Valor minimo"));
            indicador.setValorMaximoReferencia(decimalRequerido(
                    txtMaximoIndicador.getText(), "Valor maximo"));
            indicador.setCategoria(String.valueOf(cboCategoriaIndicador.getSelectedItem()));
            indicador.setDescripcion(opcional(txtDescripcionIndicador.getText()));
            indicador.setEstadoIndicador(chkIndicadorActivo.isSelected());

            boolean ok = controlador.guardarIndicador(indicador, modificacion);
            informar(ok);
            if (ok) {
                limpiarIndicador();
                cargarIndicadores();
                cargarResumen();
            }
        } catch (IllegalArgumentException ex) {
            advertencia(ex.getMessage());
        }
    }

    private void desactivarIndicador() {
        if (indicadorSeleccionado == null) {
            advertencia("Seleccione un indicador.");
            return;
        }
        if (!confirmar("¿Desactivar el indicador seleccionado?")) {
            return;
        }
        boolean ok = controlador.desactivarIndicador(
                indicadorSeleccionado.getIdIndicador());
        informar(ok);
        if (ok) {
            limpiarIndicador();
            cargarIndicadores();
            cargarResumen();
        }
    }

    private void eliminarIndicador() {
        if (indicadorSeleccionado == null) {
            advertencia("Seleccione un indicador.");
            return;
        }
        if (!confirmar("¿Eliminar definitivamente el indicador seleccionado?")) {
            return;
        }
        boolean ok = controlador.eliminarIndicador(
                indicadorSeleccionado.getIdIndicador());
        informar(ok);
        if (ok) {
            limpiarIndicador();
            cargarIndicadores();
            cargarResumen();
        }
    }

    private void limpiarIndicador() {
        indicadorSeleccionado = null;
        tblIndicadores.clearSelection();
        txtNombreIndicador.setText("");
        txtUnidadIndicador.setText("");
        txtMinimoIndicador.setText("");
        txtMaximoIndicador.setText("");
        cboCategoriaIndicador.setSelectedIndex(0);
        txtDescripcionIndicador.setText("");
        chkIndicadorActivo.setSelected(true);
        btnModificarIndicador.setEnabled(false);
        btnDesactivarIndicador.setEnabled(false);
        btnEliminarIndicador.setEnabled(false);
    }

    private void configurarTabla(JTable tabla) {
        tabla.setRowHeight(38);
        tabla.setShowGrid(false);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionBackground(new java.awt.Color(220, 238, 255));
        tabla.setSelectionForeground(new java.awt.Color(23, 42, 67));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 40));
        tabla.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer encabezado = new DefaultTableCellRenderer();
        encabezado.setOpaque(true);
        encabezado.setBackground(new java.awt.Color(10, 58, 108));
        encabezado.setForeground(java.awt.Color.WHITE);
        encabezado.setFont(new Font("SansSerif", Font.PLAIN, 12));
        encabezado.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.getTableHeader().setDefaultRenderer(encabezado);
    }

    private DefaultTableModel modeloNoEditable(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void ajustarAnchos(JTable tabla) {
        if (tabla.getColumnCount() == 0) {
            return;
        }
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            int ancho = i == 0 ? 75 : 145;
            if (i == tabla.getColumnCount() - 1) {
                ancho = 190;
            }
            tabla.getColumnModel().getColumn(i).setPreferredWidth(ancho);
        }
    }

    private void estilizarBotonPrincipal(JButton boton) {
        utilidades.EstilosComponentes.aplicarBotonPremium(
                boton, new java.awt.Color(8, 124, 255),
                new java.awt.Color(54, 207, 255), java.awt.Color.WHITE);
    }

    private void estilizarBotonModificar(JButton boton) {
        utilidades.EstilosComponentes.aplicarBotonPremium(
                boton, new java.awt.Color(109, 40, 217),
                new java.awt.Color(168, 85, 247), java.awt.Color.WHITE);
    }

    private void estilizarBotonAdvertencia(JButton boton) {
        utilidades.EstilosComponentes.aplicarBotonPremium(
                boton, new java.awt.Color(255, 214, 0),
                new java.awt.Color(255, 232, 82), new java.awt.Color(41, 31, 0));
    }

    private void estilizarBotonPeligro(JButton boton) {
        utilidades.EstilosComponentes.aplicarBotonPremium(
                boton, new java.awt.Color(255, 23, 68),
                new java.awt.Color(255, 91, 110), java.awt.Color.WHITE);
    }

    private void estilizarBotonSecundario(JButton boton) {
        utilidades.EstilosComponentes.aplicarBotonPremium(
                boton, new java.awt.Color(241, 245, 249),
                new java.awt.Color(226, 232, 240), new java.awt.Color(52, 74, 100));
    }

    private String requerido(String texto, String mensaje) {
        String valor = texto == null ? "" : texto.trim();
        if (valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
    }

    private String opcional(String texto) {
        String valor = texto == null ? "" : texto.trim();
        return valor.isBlank() ? null : valor;
    }

    private BigDecimal decimalRequerido(String texto, String nombre) {
        try {
            return new BigDecimal(requerido(
                    texto, nombre + " es obligatorio.").replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(nombre + " debe ser numerico.");
        }
    }

    private BigDecimal decimalOpcional(String texto, String nombre) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        BigDecimal valor = decimalRequerido(texto, nombre);
        if (valor.signum() < 0) {
            throw new IllegalArgumentException(nombre + " no puede ser negativo.");
        }
        return valor;
    }

    private void seleccionarTexto(JComboBox<String> combo, String valor) {
        if (valor == null) {
            return;
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (valor.equalsIgnoreCase(combo.getItemAt(i))) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String valor(Object objeto) {
        return objeto == null ? "" : objeto.toString();
    }

    private boolean confirmar(String mensaje) {
        return JOptionPane.showConfirmDialog(
                this, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void informar(boolean correcto) {
        if (correcto) {
            JOptionPane.showMessageDialog(
                    this,
                    controlador.getMensaje().isBlank()
                            ? "Operacion completada correctamente."
                            : controlador.getMensaje(),
                    "GYMNOVA",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            advertencia(controlador.getMensaje());
        }
    }

    private void mostrarMensajeSiError() {
        if (!controlador.getMensaje().isBlank()) {
            advertencia(controlador.getMensaje());
        }
    }

    private void advertencia(String mensaje) {
        JOptionPane.showMessageDialog(
                this,
                mensaje == null || mensaje.isBlank()
                        ? "No fue posible completar la operacion."
                        : mensaje,
                "GYMNOVA",
                JOptionPane.WARNING_MESSAGE);
    }
}
