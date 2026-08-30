package vista;

import controlador.MembresiaCobroControlador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.Membresia;
import modelo.MembresiaCobroResumen;
import modelo.MetodoPago;
import modelo.TipoMembresia;
import utilidades.CalendarioSelector;
import utilidades.EstilosComponentes;

/**
 * Nueva vista de Membresias. Sustituye al formulario generico anterior y hace
 * visible en la misma pantalla el plan, la vigencia, el metodo de pago y el
 * total que se registra automaticamente en Finanzas.
 */
public class PnlMembresiasCobro extends JPanel {

    private static final Color FONDO = new Color(234, 242, 251);
    private static final Color TARJETA = Color.WHITE;
    private static final Color BORDE = new Color(212, 225, 239);
    private static final Color AZUL = new Color(8, 124, 255);
    private static final Color VERDE = new Color(22, 163, 74);
    private static final Color NARANJA = new Color(234, 137, 26);
    private static final Color ROJO = new Color(231, 76, 92);
    private static final Color TEXTO = new Color(23, 42, 67);
    private static final Color TEXTO_SUAVE = new Color(90, 110, 135);
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final NumberFormat DINERO = NumberFormat.getCurrencyInstance(Locale.US);
    private static final BigDecimal IVA = new BigDecimal(
            System.getProperty("gymnova.iva", "15.00"));

    private final MembresiaCobroControlador controlador =
            new MembresiaCobroControlador();

    private final JTextField txtBuscarCliente = new JTextField();
    private final JComboBox<Cliente> cboCliente = new JComboBox<>();
    private final JButton btnBuscarCliente = new JButton("Buscar");

    private final JLabel lblActualNumero = valor("Sin membresia");
    private final JLabel lblActualPlan = valor("-");
    private final JLabel lblActualVigencia = valor("-");
    private final JLabel lblActualEstado = valor("SIN MEMBRESIA");

    private final JComboBox<TipoMembresia> cboPlan = new JComboBox<>();
    private final JLabel lblDuracion = new JLabel("-");
    private final JTextField txtFechaInicio = new JTextField(LocalDate.now().toString());
    private final JLabel lblFechaFin = new JLabel("-");
    private final JTextArea txtObservaciones = new JTextArea(3, 28);

    private final JComboBox<MetodoPago> cboMetodo = new JComboBox<>();
    private final JTextField txtReferencia = new JTextField();
    private final JLabel lblReferenciaObligatoria = new JLabel("Referencia opcional");

    private final JLabel lblSubtotal = monto("$0.00");
    private final JLabel lblIva = monto("$0.00");
    private final JLabel lblTotal = montoGrande("$0.00");
    private final JButton btnCobrar = new JButton("Cobrar y activar membresia");
    private final JButton btnEditarPlan = new JButton("Editar precio del plan");
    private final JButton btnActualizar = new JButton("Actualizar");

    private final JTable tblHistorial = new JTable();
    private final JLabel lblCantidadHistorial = new JLabel("0 registros");
    private final JButton btnCongelar = new JButton("Congelar seleccionada");
    private final JButton btnReactivar = new JButton("Reactivar seleccionada");
    private final JButton btnCancelar = new JButton("Cancelar seleccionada");

    private List<TipoMembresia> planes = new ArrayList<>();
    private List<MembresiaCobroResumen> historial = new ArrayList<>();
    private Membresia ultimaMembresia;

    public PnlMembresiasCobro() {
        CalendarioSelector.vincularFecha(txtFechaInicio);
        construirVista();
        configurarComponentes();
        configurarEventos();
        cargarCatalogos();
        cargarClientes("");
        refrescarDatos();
    }

    public final void refrescarDatos() {
        // Antes de mostrar el historial, sincroniza estados:
        //   * PENDIENTE cuya fecha_inicio ya llegó -> ACTIVA.
        //   * ACTIVA con fecha_fin expirada -> VENCIDA.
        // Así el usuario ve el estado real al abrir/recargar la vista.
        utilidades.VerificadorMembresias.ejecutar();

        Cliente actual = clienteSeleccionado();
        if (actual == null && cboCliente.getItemCount() > 0) {
            cboCliente.setSelectedIndex(0);
            actual = clienteSeleccionado();
        }
        cargarClienteSeleccionado(actual);
    }

    private void construirVista() {
        setLayout(new BorderLayout(12, 12));
        setBackground(FONDO);
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        add(construirEncabezado(), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(12, 12));
        cuerpo.setOpaque(false);

        JPanel superior = new JPanel(new GridLayout(1, 2, 12, 0));
        superior.setOpaque(false);
        superior.add(construirClienteActual());
        superior.add(construirCobro());
        cuerpo.add(superior, BorderLayout.NORTH);
        cuerpo.add(construirHistorial(), BorderLayout.CENTER);

        add(cuerpo, BorderLayout.CENTER);
    }

    private JPanel construirEncabezado() {
        JPanel p = tarjeta(new BorderLayout(10, 0));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new javax.swing.BoxLayout(textos,
                javax.swing.BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Cobro y renovacion de mensualidades");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setForeground(TEXTO);
        JLabel sub = new JLabel(
                "Selecciona al cliente, el plan y el metodo de pago. El cobro genera membresia, factura, pago y comprobante en una sola operacion.");
        sub.setForeground(TEXTO_SUAVE);
        textos.add(titulo);
        textos.add(javax.swing.Box.createVerticalStrut(4));
        textos.add(sub);
        p.add(textos, BorderLayout.CENTER);

        JLabel modulo = new JLabel("MEMBRESIAS", SwingConstants.CENTER);
        modulo.setForeground(AZUL);
        modulo.setBorder(BorderFactory.createLineBorder(AZUL));
        modulo.setPreferredSize(new Dimension(120, 45));
        p.add(modulo, BorderLayout.EAST);
        return p;
    }

    private JPanel construirClienteActual() {
        JPanel tarjeta = tarjeta(new BorderLayout(8, 8));
        JLabel titulo = tituloSeccion("1. Cliente y membresia actual");
        tarjeta.add(titulo, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setOpaque(false);
        int y = 0;
        agregarCampoAncho(contenido, y++, "Buscar cliente", txtBuscarCliente,
                btnBuscarCliente);
        agregarCampoCompleto(contenido, y++, "Cliente *", cboCliente);

        JPanel estado = new JPanel(new GridLayout(4, 2, 8, 8));
        estado.setOpaque(false);
        estado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        estado.add(etiqueta("Membresia")); estado.add(lblActualNumero);
        estado.add(etiqueta("Plan actual")); estado.add(lblActualPlan);
        estado.add(etiqueta("Vigencia")); estado.add(lblActualVigencia);
        estado.add(etiqueta("Estado")); estado.add(lblActualEstado);
        GridBagConstraints c = gbc(0, y);
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        contenido.add(estado, c);

        tarjeta.add(contenido, BorderLayout.CENTER);
        return tarjeta;
    }

    private JPanel construirCobro() {
        JPanel tarjeta = tarjeta(new BorderLayout(8, 8));
        tarjeta.add(tituloSeccion("2. Cobro / renovacion"), BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        int y = 0;
        agregarDos(formulario, y++, "Plan *", cboPlan,
                "Duracion", lblDuracion);
        agregarDos(formulario, y++, "Inicio de nueva vigencia *", txtFechaInicio,
                "Fecha de fin", lblFechaFin);
        agregarDos(formulario, y++, "Metodo de pago *", cboMetodo,
                "Referencia", txtReferencia);
        GridBagConstraints nota = gbc(2, y++);
        nota.gridwidth = 2;
        lblReferenciaObligatoria.setForeground(TEXTO_SUAVE);
        lblReferenciaObligatoria.setFont(new Font("SansSerif", Font.ITALIC, 11));
        formulario.add(lblReferenciaObligatoria, nota);

        GridBagConstraints lo = gbc(0, y);
        formulario.add(etiqueta("Observaciones"), lo);
        GridBagConstraints co = gbc(1, y++);
        co.gridwidth = 3;
        co.fill = GridBagConstraints.BOTH;
        co.weightx = 1;
        co.weighty = 1;
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setPreferredSize(new Dimension(300, 58));
        formulario.add(scrollObs, co);

        tarjeta.add(formulario, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout(10, 8));
        sur.setOpaque(false);
        JPanel totales = new JPanel(new GridLayout(1, 3, 8, 0));
        totales.setOpaque(false);
        totales.add(tarjetaMonto("Subtotal", lblSubtotal));
        totales.add(tarjetaMonto("IVA " + IVA.stripTrailingZeros().toPlainString() + "%", lblIva));
        totales.add(tarjetaMonto("TOTAL A COBRAR", lblTotal));
        sur.add(totales, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        estiloClaro(btnEditarPlan);
        estiloPrimario(btnCobrar);
        acciones.add(btnEditarPlan);
        acciones.add(btnCobrar);
        sur.add(acciones, BorderLayout.SOUTH);
        tarjeta.add(sur, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel construirHistorial() {
        JPanel tarjeta = tarjeta(new BorderLayout(8, 8));
        JPanel cab = new JPanel(new BorderLayout());
        cab.setOpaque(false);
        cab.add(tituloSeccion("Historial de mensualidades y cobros"), BorderLayout.WEST);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        estiloClaro(btnActualizar);
        derecha.add(btnActualizar);
        derecha.add(lblCantidadHistorial);
        cab.add(derecha, BorderLayout.EAST);
        tarjeta.add(cab, BorderLayout.NORTH);

        DefaultTableModel modelo = new DefaultTableModel(new Object[]{
            "Membresia", "Plan", "Inicio", "Fin", "Estado", "Subtotal",
            "Factura", "Pago", "Metodo", "Fecha de cobro", "Total cobrado"}, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
        tblHistorial.setModel(modelo);
        estilizarTabla(tblHistorial);
        tblHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblHistorial.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {100, 120, 95, 95, 100, 95, 100, 100, 120, 150, 110};
        for (int i = 0; i < anchos.length; i++) {
            tblHistorial.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        tarjeta.add(new JScrollPane(tblHistorial), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        estiloAdvertencia(btnCongelar);
        estiloClaro(btnReactivar);
        estiloPeligro(btnCancelar);
        acciones.add(btnCongelar);
        acciones.add(btnReactivar);
        acciones.add(btnCancelar);
        JLabel nota = new JLabel(
                "  Los cobros confirmados aparecen automaticamente en Finanzas > Dashboard.");
        nota.setForeground(TEXTO_SUAVE);
        acciones.add(nota);
        tarjeta.add(acciones, BorderLayout.SOUTH);
        return tarjeta;
    }

    private void configurarComponentes() {
        EstilosComponentes.aplicarCampoSimple(txtBuscarCliente);
        EstilosComponentes.aplicarCampoSimple(txtFechaInicio);
        EstilosComponentes.aplicarCampoSimple(txtReferencia);
        EstilosComponentes.aplicarComboRedondeado(cboCliente);
        EstilosComponentes.aplicarComboRedondeado(cboPlan);
        EstilosComponentes.aplicarComboRedondeado(cboMetodo);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        btnEditarPlan.setVisible(controlador.esAdministrador());

        cboCliente.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index,
                        isSelected, cellHasFocus);
                if (value instanceof Cliente c) {
                    setText(c.getCodigoCliente() + " - " + c.getNombreCompleto());
                }
                return this;
            }
        });
    }

    private void configurarEventos() {
        btnBuscarCliente.addActionListener(e -> cargarClientes(txtBuscarCliente.getText()));
        txtBuscarCliente.addActionListener(e -> cargarClientes(txtBuscarCliente.getText()));
        cboCliente.addActionListener(e -> cargarClienteSeleccionado(clienteSeleccionado()));
        cboPlan.addActionListener(e -> actualizarCalculo());
        cboMetodo.addActionListener(e -> actualizarMetodo());
        txtFechaInicio.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarCalculo(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarCalculo(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarCalculo(); }
        });
        btnCobrar.addActionListener(e -> cobrar());
        btnActualizar.addActionListener(e -> {
            cargarCatalogos();
            cargarClienteSeleccionado(clienteSeleccionado());
        });
        btnEditarPlan.addActionListener(e -> editarPrecioPlan());
        btnCongelar.addActionListener(e -> congelarSeleccionada());
        btnReactivar.addActionListener(e -> reactivarSeleccionada());
        btnCancelar.addActionListener(e -> cancelarSeleccionada());
    }

    private void cargarCatalogos() {
        TipoMembresia planPrevio = planSeleccionado();
        Long idPlanPrevio = planPrevio == null ? null : planPrevio.getIdTipoMembresia();
        planes = controlador.listarPlanesActivos();
        DefaultComboBoxModel<TipoMembresia> mp = new DefaultComboBoxModel<>();
        for (TipoMembresia plan : planes) mp.addElement(plan);
        cboPlan.setModel(mp);
        if (idPlanPrevio != null) seleccionarPlan(idPlanPrevio);

        MetodoPago previo = metodoSeleccionado();
        Long idMetodo = previo == null ? null : previo.getIdMetodoPago();
        DefaultComboBoxModel<MetodoPago> mm = new DefaultComboBoxModel<>();
        for (MetodoPago metodo : controlador.listarMetodosActivos()) mm.addElement(metodo);
        cboMetodo.setModel(mm);
        if (idMetodo != null) {
            for (int i = 0; i < cboMetodo.getItemCount(); i++) {
                if (idMetodo.equals(cboMetodo.getItemAt(i).getIdMetodoPago())) {
                    cboMetodo.setSelectedIndex(i); break;
                }
            }
        }
        actualizarMetodo();
        actualizarCalculo();
    }

    private void cargarClientes(String criterio) {
        Cliente previo = clienteSeleccionado();
        Long idPrevio = previo == null ? null : previo.getIdPersona();
        DefaultComboBoxModel<Cliente> modelo = new DefaultComboBoxModel<>();
        for (Cliente cliente : controlador.listarClientesActivos(criterio)) {
            modelo.addElement(cliente);
        }
        cboCliente.setModel(modelo);
        if (idPrevio != null) {
            for (int i = 0; i < cboCliente.getItemCount(); i++) {
                if (idPrevio.equals(cboCliente.getItemAt(i).getIdPersona())) {
                    cboCliente.setSelectedIndex(i);
                    return;
                }
            }
        }
        if (modelo.getSize() > 0) cboCliente.setSelectedIndex(0);
        cargarClienteSeleccionado(clienteSeleccionado());
    }

    private void cargarClienteSeleccionado(Cliente cliente) {
        if (cliente == null || cliente.getIdPersona() == null) {
            ultimaMembresia = null;
            mostrarActual(null);
            cargarHistorial(null);
            btnCobrar.setEnabled(false);
            return;
        }
        ultimaMembresia = controlador.obtenerUltimaMembresia(cliente.getIdPersona());
        mostrarActual(ultimaMembresia);
        LocalDate inicio = controlador.fechaInicioSugerida(ultimaMembresia);
        txtFechaInicio.setText(inicio.toString());
        txtFechaInicio.setEnabled(!controlador.esRenovacionProgramada(ultimaMembresia));
        txtFechaInicio.setToolTipText(txtFechaInicio.isEnabled()
                ? "Selecciona la fecha de inicio"
                : "La renovacion comienza automaticamente al terminar la vigencia actual");
        if (ultimaMembresia != null) {
            seleccionarPlan(ultimaMembresia.getIdTipoMembresia());
        }
        btnCobrar.setText(ultimaMembresia == null
                ? "Cobrar y activar membresia"
                : "Cobrar renovacion");
        btnCobrar.setEnabled(true);
        cargarHistorial(cliente.getIdPersona());
        actualizarCalculo();
    }

    private void mostrarActual(Membresia m) {
        if (m == null) {
            lblActualNumero.setText("Sin membresia");
            lblActualPlan.setText("-");
            lblActualVigencia.setText("-");
            lblActualEstado.setText("SIN MEMBRESIA");
            lblActualEstado.setForeground(TEXTO_SUAVE);
            return;
        }
        lblActualNumero.setText(seguro(m.getNumeroMembresia()));
        TipoMembresia plan = buscarPlan(m.getIdTipoMembresia());
        lblActualPlan.setText(plan == null ? "Plan #" + m.getIdTipoMembresia() : plan.getNombre());
        lblActualVigencia.setText(seguro(m.getFechaInicio()) + "  →  " + seguro(m.getFechaFin()));
        lblActualEstado.setText(seguro(m.getEstadoMembresia()));
        String estado = m.getEstadoMembresia() == null ? "" : m.getEstadoMembresia().toUpperCase();
        lblActualEstado.setForeground("ACTIVA".equals(estado) ? VERDE
                : "PENDIENTE".equals(estado) ? NARANJA : ROJO);
    }

    private void cargarHistorial(Long idCliente) {
        historial = idCliente == null ? new ArrayList<>() : controlador.listarHistorial(idCliente);
        DefaultTableModel m = (DefaultTableModel) tblHistorial.getModel();
        m.setRowCount(0);
        for (MembresiaCobroResumen fila : historial) {
            String estado = etiquetaEstado(fila);
            m.addRow(new Object[]{
                fila.getNumeroMembresia(), fila.getNombrePlan(), fila.getFechaInicio(),
                fila.getFechaFin(), estado, dinero(fila.getCostoFinal()),
                seguro(fila.getNumeroFactura()), seguro(fila.getCodigoPago()),
                seguro(fila.getMetodoPago()),
                fila.getFechaHoraPago() == null ? "" : FECHA_HORA.format(fila.getFechaHoraPago()),
                dinero(fila.getTotalCobrado())
            });
        }
        lblCantidadHistorial.setText(historial.size() + " registro(s)");
    }

    /**
     * Ajusta la etiqueta visible del estado de membresía:
     *   - Si en la BD está PENDIENTE y la fecha_inicio aún no llega, se
     *     muestra "PROGRAMADA" para dejar claro que el cobro ya se
     *     realizó y solo espera a que su vigencia comience.
     *   - En cualquier otro caso se conserva el estado literal de la BD.
     */
    private String etiquetaEstado(MembresiaCobroResumen fila) {
        String estado = fila.getEstadoMembresia();
        if ("PENDIENTE".equalsIgnoreCase(estado)
                && fila.getFechaInicio() != null
                && fila.getFechaInicio().isAfter(LocalDate.now())) {
            return "PROGRAMADA";
        }
        return estado;
    }

    private void actualizarCalculo() {
        TipoMembresia plan = planSeleccionado();
        if (plan == null) {
            lblDuracion.setText("-");
            lblFechaFin.setText("-");
            lblSubtotal.setText("$0.00"); lblIva.setText("$0.00"); lblTotal.setText("$0.00");
            return;
        }
        lblDuracion.setText(plan.getDuracionDias() + " dias");
        LocalDate inicio = leerFecha(txtFechaInicio.getText(), LocalDate.now());
        lblFechaFin.setText(inicio.plusDays(Math.max(0, plan.getDuracionDias() - 1L)).toString());
        BigDecimal base = plan.getPrecioBase() == null ? BigDecimal.ZERO : plan.getPrecioBase();
        BigDecimal iva = base.multiply(IVA).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(iva).setScale(2, RoundingMode.HALF_UP);
        lblSubtotal.setText(dinero(base));
        lblIva.setText(dinero(iva));
        lblTotal.setText(dinero(total));
    }

    private void actualizarMetodo() {
        MetodoPago metodo = metodoSeleccionado();
        boolean requiere = metodo != null && metodo.isRequiereReferencia();
        lblReferenciaObligatoria.setText(requiere
                ? "Referencia obligatoria para " + metodo.getNombreMetodo()
                : "Referencia opcional");
    }

    private void cobrar() {
        Cliente cliente = clienteSeleccionado();
        TipoMembresia plan = planSeleccionado();
        MetodoPago metodo = metodoSeleccionado();
        LocalDate inicio = leerFecha(txtFechaInicio.getText(), null);
        if (inicio == null) {
            advertencia("Seleccione una fecha de inicio valida.");
            return;
        }
        if (cliente == null || plan == null || metodo == null) {
            advertencia("Seleccione cliente, plan y metodo de pago.");
            return;
        }
        BigDecimal base = plan.getPrecioBase() == null ? BigDecimal.ZERO : plan.getPrecioBase();
        BigDecimal iva = base.multiply(IVA).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal total = base.add(iva).setScale(2, RoundingMode.HALF_UP);
        String operacion = ultimaMembresia == null ? "NUEVA MEMBRESIA" : "RENOVACION";
        String mensaje = "Cliente: " + cliente.getNombreCompleto()
                + "\nOperacion: " + operacion
                + "\nPlan: " + plan.getNombre()
                + "\nVigencia: " + inicio + " a " + lblFechaFin.getText()
                + "\nMetodo: " + metodo.getNombreMetodo()
                + "\nSubtotal: " + dinero(base)
                + "\nIVA: " + dinero(iva)
                + "\nTOTAL A COBRAR: " + dinero(total)
                + "\n\n¿Confirmar el cobro?";
        int opcion = JOptionPane.showConfirmDialog(this, mensaje,
                "Confirmar cobro de mensualidad", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) return;

        Long id = controlador.cobrar(cliente.getIdPersona(), plan, inicio,
                txtObservaciones.getText(), metodo, txtReferencia.getText());
        if (id == null) {
            advertencia(controlador.getMensaje());
            return;
        }
        JOptionPane.showMessageDialog(this,
                controlador.getMensaje() + "\n\nEl ingreso ya esta reflejado en Finanzas > Dashboard.",
                "Cobro registrado", JOptionPane.INFORMATION_MESSAGE);
        txtReferencia.setText("");
        txtObservaciones.setText("");
        cargarCatalogos();
        cargarClienteSeleccionado(cliente);
    }

    private void editarPrecioPlan() {
        TipoMembresia plan = planSeleccionado();
        if (plan == null) { advertencia("Seleccione un plan."); return; }
        JTextField precio = new JTextField(plan.getPrecioBase() == null
                ? "0.00" : plan.getPrecioBase().toPlainString());
        JTextArea descripcion = new JTextArea(plan.getDescripcion() == null
                ? "" : plan.getDescripcion(), 3, 24);
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Plan: " + plan.getNombre()));
        p.add(new JLabel("Nuevo precio base:")); p.add(precio);
        p.add(new JLabel("Descripcion:")); p.add(new JScrollPane(descripcion));
        int op = JOptionPane.showConfirmDialog(this, p, "Editar precio del plan",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return;
        BigDecimal nuevo;
        try { nuevo = new BigDecimal(precio.getText().trim()); }
        catch (RuntimeException ex) { advertencia("Ingrese un precio numerico valido."); return; }
        if (!controlador.actualizarPrecioPlan(plan, nuevo, descripcion.getText())) {
            advertencia(controlador.getMensaje()); return;
        }
        JOptionPane.showMessageDialog(this, controlador.getMensaje(), "GYMNOVA",
                JOptionPane.INFORMATION_MESSAGE);
        cargarCatalogos();
    }

    private void congelarSeleccionada() {
        MembresiaCobroResumen fila = filaSeleccionada();
        if (fila == null) { advertencia("Seleccione una membresia del historial."); return; }
        if (!"ACTIVA".equalsIgnoreCase(fila.getEstadoMembresia())) {
            advertencia("Solo puede congelar una membresia ACTIVA."); return;
        }
        JTextField inicio = new JTextField(LocalDate.now().toString());
        JTextField fin = new JTextField(LocalDate.now().plusDays(7).toString());
        CalendarioSelector.vincularFecha(inicio); CalendarioSelector.vincularFecha(fin);
        JTextField motivo = new JTextField();
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Membresia: " + fila.getNumeroMembresia()));
        p.add(new JLabel("Inicio:")); p.add(inicio);
        p.add(new JLabel("Fin:")); p.add(fin);
        p.add(new JLabel("Motivo:")); p.add(motivo);
        int op = JOptionPane.showConfirmDialog(this, p, "Congelar membresia",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (op != JOptionPane.OK_OPTION) return;
        LocalDate fi = leerFecha(inicio.getText(), null);
        LocalDate ff = leerFecha(fin.getText(), null);
        if (!controlador.congelar(fila.getIdMembresia(), fi, ff, motivo.getText())) {
            advertencia(controlador.getMensaje()); return;
        }
        JOptionPane.showMessageDialog(this, "Membresia congelada.", "GYMNOVA",
                JOptionPane.INFORMATION_MESSAGE);
        cargarClienteSeleccionado(clienteSeleccionado());
    }

    private void reactivarSeleccionada() {
        MembresiaCobroResumen fila = filaSeleccionada();
        if (fila == null) { advertencia("Seleccione una membresia del historial."); return; }
        if (!"CONGELADA".equalsIgnoreCase(fila.getEstadoMembresia())) {
            advertencia("Solo puede reactivar una membresia CONGELADA."); return;
        }
        if (!controlador.reactivar(fila.getIdMembresia())) {
            advertencia(controlador.getMensaje()); return;
        }
        JOptionPane.showMessageDialog(this, "Membresia reactivada.", "GYMNOVA",
                JOptionPane.INFORMATION_MESSAGE);
        cargarClienteSeleccionado(clienteSeleccionado());
    }

    private void cancelarSeleccionada() {
        MembresiaCobroResumen fila = filaSeleccionada();
        if (fila == null) { advertencia("Seleccione una membresia del historial."); return; }
        String motivo = JOptionPane.showInputDialog(this,
                "Motivo de cancelacion de " + fila.getNumeroMembresia() + ":",
                "Cancelar membresia", JOptionPane.WARNING_MESSAGE);
        if (motivo == null) return;
        if (!controlador.cancelar(fila.getIdMembresia(), motivo)) {
            advertencia(controlador.getMensaje()); return;
        }
        JOptionPane.showMessageDialog(this, "Membresia cancelada.", "GYMNOVA",
                JOptionPane.INFORMATION_MESSAGE);
        cargarClienteSeleccionado(clienteSeleccionado());
    }

    private MembresiaCobroResumen filaSeleccionada() {
        int fila = tblHistorial.getSelectedRow();
        return fila < 0 || fila >= historial.size() ? null : historial.get(fila);
    }

    private Cliente clienteSeleccionado() {
        Object o = cboCliente.getSelectedItem();
        return o instanceof Cliente c ? c : null;
    }
    private TipoMembresia planSeleccionado() {
        Object o = cboPlan.getSelectedItem();
        return o instanceof TipoMembresia p ? p : null;
    }
    private MetodoPago metodoSeleccionado() {
        Object o = cboMetodo.getSelectedItem();
        return o instanceof MetodoPago m ? m : null;
    }

    private TipoMembresia buscarPlan(Long id) {
        if (id == null) return null;
        for (TipoMembresia plan : planes) {
            if (id.equals(plan.getIdTipoMembresia())) return plan;
        }
        return null;
    }
    private void seleccionarPlan(Long id) {
        if (id == null) return;
        for (int i = 0; i < cboPlan.getItemCount(); i++) {
            if (id.equals(cboPlan.getItemAt(i).getIdTipoMembresia())) {
                cboPlan.setSelectedIndex(i); return;
            }
        }
    }

    private LocalDate leerFecha(String texto, LocalDate porDefecto) {
        try { return LocalDate.parse(texto == null ? "" : texto.trim()); }
        catch (RuntimeException ex) { return porDefecto; }
    }

    private JPanel tarjeta(java.awt.LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(TARJETA);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        return p;
    }
    private JPanel tarjetaMonto(String titulo, JLabel valor) {
        JPanel p = new JPanel();
        p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS));
        p.setBackground(new Color(247, 250, 253));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel t = new JLabel(titulo); t.setForeground(TEXTO_SUAVE);
        t.setFont(new Font("SansSerif", Font.PLAIN, 11));
        t.setAlignmentX(Component.LEFT_ALIGNMENT); valor.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t); p.add(javax.swing.Box.createVerticalStrut(4)); p.add(valor);
        return p;
    }
    private JLabel tituloSeccion(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("SansSerif", Font.BOLD, 16));
        l.setForeground(TEXTO); return l;
    }
    private JLabel etiqueta(String t) {
        JLabel l = new JLabel(t); l.setForeground(TEXTO); return l;
    }
    private static JLabel valor(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(TEXTO); return l;
    }
    private JLabel monto(String t) {
        JLabel l = new JLabel(t); l.setFont(new Font("SansSerif", Font.BOLD, 17));
        l.setForeground(TEXTO); return l;
    }
    private JLabel montoGrande(String t) {
        JLabel l = monto(t); l.setFont(new Font("SansSerif", Font.BOLD, 21));
        l.setForeground(VERDE); return l;
    }
    private GridBagConstraints gbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx=x; c.gridy=y; c.insets=new Insets(4,4,4,4);
        c.anchor=GridBagConstraints.WEST; return c;
    }
    private void agregarCampoAncho(JPanel p,int y,String texto,Component campo,Component boton) {
        GridBagConstraints l=gbc(0,y); p.add(etiqueta(texto),l);
        GridBagConstraints c=gbc(1,y); c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;p.add(campo,c);
        GridBagConstraints b=gbc(2,y);p.add(boton,b);
    }
    private void agregarCampoCompleto(JPanel p,int y,String texto,Component campo) {
        GridBagConstraints l=gbc(0,y);p.add(etiqueta(texto),l);
        GridBagConstraints c=gbc(1,y);c.gridwidth=2;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;p.add(campo,c);
    }
    private void agregarDos(JPanel p,int y,String l1,Component c1,String l2,Component c2) {
        GridBagConstraints a=gbc(0,y);p.add(etiqueta(l1),a);
        GridBagConstraints b=gbc(1,y);b.weightx=1;b.fill=GridBagConstraints.HORIZONTAL;p.add(c1,b);
        GridBagConstraints c=gbc(2,y);p.add(etiqueta(l2),c);
        GridBagConstraints d=gbc(3,y);d.weightx=1;d.fill=GridBagConstraints.HORIZONTAL;p.add(c2,d);
    }
    private void estilizarTabla(JTable t) {
        t.setRowHeight(29); t.setShowGrid(false); t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(220,238,255)); t.setSelectionForeground(TEXTO);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setPreferredSize(new Dimension(0,34));
        DefaultTableCellRenderer h=new DefaultTableCellRenderer();h.setOpaque(true);
        h.setBackground(new Color(10,58,108));h.setForeground(Color.WHITE);
        h.setHorizontalAlignment(SwingConstants.CENTER);h.setFont(new Font("SansSerif",Font.PLAIN,12));
        t.getTableHeader().setDefaultRenderer(h);
    }
    private void estiloPrimario(JButton b){
        EstilosComponentes.aplicarBotonPrimario(b); b.setPreferredSize(new Dimension(205,36));
    }
    private void estiloClaro(JButton b){b.setBackground(new Color(242,247,252));b.setForeground(TEXTO);b.setFocusPainted(false);}
    private void estiloAdvertencia(JButton b){b.setBackground(new Color(255,232,183));b.setForeground(new Color(120,72,0));b.setFocusPainted(false);}
    private void estiloPeligro(JButton b){b.setBackground(ROJO);b.setForeground(Color.WHITE);b.setFocusPainted(false);}
    private void advertencia(String m){JOptionPane.showMessageDialog(this,m==null||m.isBlank()?"No se pudo completar la operacion.":m,"GYMNOVA",JOptionPane.WARNING_MESSAGE);}
    private String dinero(BigDecimal v){return DINERO.format(v==null?BigDecimal.ZERO:v);}
    private String seguro(Object o){return o==null?"":String.valueOf(o);}
}
