/* GYMNOVA - Vista administrativa: REPORTES. */
package vista;

import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import utilidades.GestorConsultaModulo;

public class PnlReportes extends javax.swing.JPanel {

    private final GestorConsultaModulo gestorConsulta;

    public PnlReportes() {
        initComponents();
        instalarBarraReportesEspecificos();

        utilidades.CalendarioSelector.vincularFecha(txtFechaIngreso);
        utilidades.CalendarioSelector.vincularFecha(txtCedulaPersona);
        gestorConsulta = new GestorConsultaModulo(
                this, cboTipoContrato, tblPersonal, txtBuscarPersonal,
                lblCantidadPersonal, "REPORTES",
                GestorConsultaModulo.controladoresPara("REPORTES")
        );
        configurarEstilos();
        configurarTabla();
        btnBuscarPersonal.addActionListener(evento -> buscarRegistros());
        cboTipoContrato.addActionListener(evento -> refrescarDatos());
        txtCodigoEmpleado.setEditable(false);
        txtCedulaPersona.setEditable(false);
        btnEliminar.setVisible(true);
        btnGuardar.setEnabled(true);
        btnModificar.setEnabled(true);
        btnDesactivar.setEnabled(true);
        btnEliminar.setEnabled(true);
        ((javax.swing.JComboBox) cboPersona).setModel(
                new javax.swing.DefaultComboBoxModel<>(new String[]{
                    "CSV", "Impresión directa"
                }));
        txtFechaIngreso.setText(LocalDate.now().withDayOfMonth(1).toString());
        txtCedulaPersona.setText(LocalDate.now().toString());
        txtSalario.setVisible(false);
        lblSalario.setVisible(false);
        lblPersona.setText("Formato de salida *");
        txtTurno.setToolTipText(
                "Opcional: texto que debe aparecer en alguna columna."
        );
        refrescarDatos();
    }

    public void refrescarDatos() {
        gestorConsulta.cargar();
        aplicarFiltrosReales(false);
    }

    private void configurarEstilos() {
        utilidades.EstilosComponentes.aplicarComboRedondeado(cboPersona);
        utilidades.EstilosComponentes.aplicarComboRedondeado(cboTipoContrato);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtCodigoEmpleado);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtFechaIngreso);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtSalario);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtCedulaPersona);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtTurno);
        utilidades.EstilosComponentes.aplicarCampoSimple(txtBuscarPersonal);

        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnGuardar, new java.awt.Color(8, 124, 255),
                new java.awt.Color(54, 207, 255), java.awt.Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnModificar, new java.awt.Color(109, 40, 217),
                new java.awt.Color(168, 85, 247), java.awt.Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnDesactivar, new java.awt.Color(255, 214, 0),
                new java.awt.Color(255, 232, 82), new java.awt.Color(41, 31, 0)
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnEliminar, new java.awt.Color(255, 23, 68),
                new java.awt.Color(255, 91, 110), java.awt.Color.WHITE
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnLimpiar, new java.awt.Color(241, 245, 249),
                new java.awt.Color(226, 232, 240), new java.awt.Color(52, 74, 100)
        );
        utilidades.EstilosComponentes.aplicarBotonPremium(
                btnBuscarPersonal, new java.awt.Color(8, 124, 255),
                new java.awt.Color(54, 207, 255), java.awt.Color.WHITE
        );
    }

    private void configurarTabla() {
        tblPersonal.setRowHeight(40);
        tblPersonal.setShowGrid(false);
        tblPersonal.setFillsViewportHeight(true);
        tblPersonal.setSelectionBackground(new java.awt.Color(220, 238, 255));
        tblPersonal.setSelectionForeground(new java.awt.Color(23, 42, 67));
        tblPersonal.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 42));
        tblPersonal.getTableHeader().setReorderingAllowed(false);
        javax.swing.table.DefaultTableCellRenderer encabezado
                = new javax.swing.table.DefaultTableCellRenderer();
        encabezado.setOpaque(true);
        encabezado.setBackground(new java.awt.Color(10, 58, 108));
        encabezado.setForeground(java.awt.Color.WHITE);
        encabezado.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        encabezado.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        tblPersonal.getTableHeader().setDefaultRenderer(encabezado);
    }

    private void buscarRegistros() {
        gestorConsulta.cargar();
        aplicarFiltrosReales(true);
    }

    private void guardarRegistro() {
        gestorConsulta.cargar();
        aplicarFiltrosReales(true);
        btnModificar.setEnabled(true);
        btnDesactivar.setEnabled(tblPersonal.getRowCount() > 0);
        btnEliminar.setEnabled(tblPersonal.getRowCount() > 0);
        JOptionPane.showMessageDialog(
                this,
                "Reporte generado con " + tblPersonal.getRowCount()
                + " registros reales.",
                "Reportes",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void modificarRegistro() {
        gestorConsulta.cargar();
        aplicarFiltrosReales(true);
        JOptionPane.showMessageDialog(
                this,
                "Vista previa actualizada con los filtros seleccionados.",
                "Reportes",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void desactivarRegistro() {
        if ("Impresión directa".equals(cboPersona.getSelectedItem())) {
            imprimirReporte();
        } else {
            exportarCsv();
        }
    }

    private void eliminarRegistro() {
        imprimirReporte();
    }

    private void limpiarFormulario() {
        txtCodigoEmpleado.setText("");
        txtFechaIngreso.setText(
                LocalDate.now().withDayOfMonth(1).toString()
        );
        cboTipoContrato.setSelectedIndex(0);
        txtSalario.setText("");
        cboPersona.setSelectedIndex(0);
        txtCedulaPersona.setText(LocalDate.now().toString());
        txtTurno.setText("");
        chkEstadoEmpleado.setSelected(true);
        tblPersonal.clearSelection();
        btnModificar.setEnabled(false);
        btnDesactivar.setEnabled(false);
        btnEliminar.setEnabled(false);
        refrescarDatos();
    }

    private void aplicarFiltrosReales(boolean mostrarError) {
        try {
            LocalDate desde = LocalDate.parse(txtFechaIngreso.getText().trim());
            LocalDate hasta = LocalDate.parse(txtCedulaPersona.getText().trim());
            if (hasta.isBefore(desde)) {
                throw new IllegalArgumentException(
                        "La fecha hasta no puede ser anterior a la fecha desde."
                );
            }

            String adicional = txtTurno.getText() == null
                    ? "" : txtTurno.getText().trim().toLowerCase();
            TableRowSorter<TableModel> ordenador
                    = new TableRowSorter<>(tblPersonal.getModel());
            ordenador.setRowFilter(new RowFilter<>() {
                @Override
                public boolean include(Entry<? extends TableModel,
                        ? extends Integer> entrada) {
                    boolean contieneFecha = false;
                    boolean fechaEnRango = false;
                    boolean coincideTexto = adicional.isBlank();

                    for (int columna = 0;
                            columna < entrada.getValueCount(); columna++) {
                        Object valor = entrada.getValue(columna);
                        LocalDate fecha = convertirFecha(valor);
                        if (fecha != null) {
                            contieneFecha = true;
                            if (!fecha.isBefore(desde)
                                    && !fecha.isAfter(hasta)) {
                                fechaEnRango = true;
                            }
                        }
                        if (!coincideTexto && valor != null
                                && valor.toString().toLowerCase()
                                        .contains(adicional)) {
                            coincideTexto = true;
                        }
                    }
                    return (!contieneFecha || fechaEnRango) && coincideTexto;
                }
            });
            tblPersonal.setRowSorter(ordenador);
            lblCantidadPersonal.setText(
                    tblPersonal.getRowCount()
                    + (tblPersonal.getRowCount() == 1
                            ? " registro filtrado" : " registros filtrados")
            );
        } catch (DateTimeParseException | IllegalArgumentException ex) {
            if (mostrarError) {
                JOptionPane.showMessageDialog(
                        this,
                        ex instanceof DateTimeParseException
                                ? "Use fechas válidas con formato AAAA-MM-DD."
                                : ex.getMessage(),
                        "Filtros del reporte",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    private LocalDate convertirFecha(Object valor) {
        if (valor instanceof LocalDate fecha) {
            return fecha;
        }
        if (valor instanceof LocalDateTime fechaHora) {
            return fechaHora.toLocalDate();
        }
        if (valor instanceof java.sql.Date fechaSql) {
            return fechaSql.toLocalDate();
        }
        if (valor instanceof java.sql.Timestamp marca) {
            return marca.toLocalDateTime().toLocalDate();
        }
        return null;
    }

    private boolean esAdministrador() {
        return utilidades.SesionUsuario.haySesionActiva()
                && "Administrador".equalsIgnoreCase(
                        utilidades.SesionUsuario.getUsuarioActual().getNombreRol()
                );
    }

    private void exportarCsv() {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File(
                "reporte_" + cboTipoContrato.getSelectedItem() + ".csv"
        ));

        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".csv")) {
            destino = new File(destino.getParentFile(),
                    destino.getName() + ".csv");
        }

        try {
            Files.writeString(
                    destino.toPath(),
                    construirCsv(tblPersonal),
                    StandardCharsets.UTF_8
            );
            JOptionPane.showMessageDialog(
                    this,
                    "Reporte exportado en:\n" + destino.getAbsolutePath(),
                    "Reportes",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible exportar: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String construirCsv(javax.swing.JTable tabla) {
        StringBuilder csv = new StringBuilder();
        for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
            if (columna > 0) {
                csv.append(';');
            }
            csv.append(escaparCsv(tabla.getColumnName(columna)));
        }
        csv.append(System.lineSeparator());

        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            for (int columna = 0; columna < tabla.getColumnCount(); columna++) {
                if (columna > 0) {
                    csv.append(';');
                }
                csv.append(escaparCsv(tabla.getValueAt(fila, columna)));
            }
            csv.append(System.lineSeparator());
        }
        return csv.toString();
    }

    private String escaparCsv(Object valor) {
        String texto = valor == null ? "" : valor.toString();
        return '"' + texto.replace("\"", "\"\"") + '"';
    }

    /**
     * Añade en la parte superior del panel una barra con accesos directos a
     * los reportes imprimibles específicos (mensualidades, y futuros).
     * Reemplaza el layout generado por el .form por un BorderLayout para
     * poder situar la barra sin tocar la sección administrativa existente.
     */
    private void instalarBarraReportesEspecificos() {
        removeAll();
        setLayout(new java.awt.BorderLayout(0, 8));

        // Barra superior con dos filas: título + fila de botones que se
        // adapta con FlowLayout (LEFT). Cada botón abre un diálogo
        // dedicado. Todos usan el mismo azul y padding para verse iguales.
        javax.swing.JPanel barra = new javax.swing.JPanel();
        barra.setLayout(new javax.swing.BoxLayout(
                barra, javax.swing.BoxLayout.Y_AXIS));
        barra.setBackground(new java.awt.Color(234, 242, 251));
        barra.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                6, 8, 6, 8));

        javax.swing.JLabel titulo = new javax.swing.JLabel(
                "Reportes especializados:");
        titulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
        titulo.setForeground(new java.awt.Color(23, 42, 67));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        barra.add(titulo);
        barra.add(javax.swing.Box.createVerticalStrut(4));

        javax.swing.JPanel fila = new javax.swing.JPanel(
                new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 4));
        fila.setBackground(new java.awt.Color(234, 242, 251));
        fila.setAlignmentX(LEFT_ALIGNMENT);

        fila.add(botonReporte("Mensualidades",
                e -> abrir(new DlgReporteMembresias(ventana()))));
        fila.add(botonReporte("Membresías (detalle)",
                e -> abrir(new DlgReporteMembresiasDetalle(ventana()))));
        fila.add(botonReporte("Panorama de clientes",
                e -> abrir(new DlgReporteClientes(ventana()))));
        fila.add(botonReporte("Financiero",
                e -> abrir(new DlgReporteFinanciero(ventana()))));
        fila.add(botonReporte("Ocupación / clases",
                e -> abrir(new DlgReporteOcupacion(ventana()))));
        fila.add(botonReporte("Desempeño profesional",
                e -> abrir(new DlgReporteDesempeno(ventana()))));
        fila.add(botonReporte("Bitácora",
                e -> abrir(new DlgReporteBitacora(ventana()))));

        // Botón especial: consolida TODO en un archivo HTML y lo descarga
        // con el nombre "Reporte YYYY-MM-DD.html" al destino que elija.
        javax.swing.JButton btnConsolidado = new javax.swing.JButton(
                "⬇ Descargar reporte consolidado");
        btnConsolidado.setBackground(new java.awt.Color(34, 197, 94));
        btnConsolidado.setForeground(java.awt.Color.WHITE);
        btnConsolidado.setFocusPainted(false);
        btnConsolidado.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                6, 14, 6, 14));
        btnConsolidado.addActionListener(e -> descargarConsolidado());
        fila.add(btnConsolidado);

        barra.add(fila);

        add(barra, java.awt.BorderLayout.NORTH);
        add(pnlBaseReportes, java.awt.BorderLayout.CENTER);
    }

    private java.awt.Frame ventana() {
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        return w instanceof java.awt.Frame f ? f : null;
    }

    private javax.swing.JButton botonReporte(String etiqueta,
            java.awt.event.ActionListener accion) {
        javax.swing.JButton b = new javax.swing.JButton(etiqueta);
        b.setBackground(new java.awt.Color(8, 124, 255));
        b.setForeground(java.awt.Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(javax.swing.BorderFactory.createEmptyBorder(
                6, 12, 6, 12));
        b.addActionListener(accion);
        return b;
    }

    private void abrir(javax.swing.JDialog dlg) {
        dlg.setVisible(true);
    }

    /**
     * Genera un archivo HTML consolidado con la información de todos
     * los reportes especializados (resumen, membresías, financiero,
     * clientes, ocupación, desempeño, bitácora) y lo guarda con el
     * nombre "Reporte YYYY-MM-DD.html". Al terminar ofrece abrirlo en
     * el navegador para revisar / imprimir / exportar como PDF.
     */
    private void descargarConsolidado() {
        String nombreDefecto = utilidades.GeneradorReporteConsolidado
                .nombreSugerido();
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar reporte consolidado");
        selector.setSelectedFile(new File(nombreDefecto));
        selector.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "Archivos HTML (*.html)", "html"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = selector.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".html")) {
            destino = new File(destino.getParentFile(),
                    destino.getName() + ".html");
        }
        try {
            utilidades.GeneradorReporteConsolidado.generarYGuardar(
                    destino.toPath());
        } catch (IOException | java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo generar el reporte: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int op = JOptionPane.showConfirmDialog(this,
                "Reporte guardado en:\n" + destino.getAbsolutePath()
                        + "\n\n¿Deseas abrirlo ahora en el navegador?",
                "Reporte consolidado", JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        if (op == JOptionPane.YES_OPTION
                && java.awt.Desktop.isDesktopSupported()) {
            try {
                java.awt.Desktop.getDesktop().browse(destino.toURI());
            } catch (IOException | UnsupportedOperationException ex) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo abrir el archivo automáticamente. "
                        + "Ábrelo manualmente desde la ubicación indicada.",
                        "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void imprimirReporte() {
        try {
            tblPersonal.print(
                    javax.swing.JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat(
                            "GYMNOVA - " + cboTipoContrato.getSelectedItem()
                    ),
                    new java.text.MessageFormat("Página {0}")
            );
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No fue posible imprimir: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBaseReportes = new javax.swing.JPanel();
        pnlEncabezadoPersonal = new javax.swing.JPanel();
        lblTituloPersonal = new javax.swing.JLabel();
        lblModuloPersonal = new javax.swing.JLabel();
        pnlFormularioPersonal = new javax.swing.JPanel();
        lblTituloFormulario = new javax.swing.JLabel();
        lblCamposObligatorios = new javax.swing.JLabel();
        lblCodigoEmpleado = new javax.swing.JLabel();
        txtCodigoEmpleado = new javax.swing.JTextField();
        lblFechaIngreso = new javax.swing.JLabel();
        txtFechaIngreso = new javax.swing.JTextField();
        lblTipoContrato = new javax.swing.JLabel();
        cboTipoContrato = new javax.swing.JComboBox<>();
        lblSalario = new javax.swing.JLabel();
        txtSalario = new javax.swing.JTextField();
        cboPersona = new javax.swing.JComboBox<>();
        lblPersona = new javax.swing.JLabel();
        lblCedulaPersona = new javax.swing.JLabel();
        txtCedulaPersona = new javax.swing.JTextField();
        lblTurno = new javax.swing.JLabel();
        txtTurno = new javax.swing.JTextField();
        chkEstadoEmpleado = new javax.swing.JCheckBox();
        btnGuardar = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnLimpiar = new javax.swing.JButton();
        btnDesactivar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        pnlTablaPersonal = new javax.swing.JPanel();
        txtBuscarPersonal = new javax.swing.JTextField();
        btnBuscarPersonal = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPersonal = new javax.swing.JTable();
        lblBuscarPersonal = new javax.swing.JLabel();
        lblCantidadPersonal = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(1050, 650));
        setPreferredSize(new java.awt.Dimension(1150, 720));

        pnlBaseReportes.setBackground(new java.awt.Color(234, 242, 251));

        pnlEncabezadoPersonal.setBackground(new java.awt.Color(255, 255, 255));
        pnlEncabezadoPersonal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlEncabezadoPersonal.setPreferredSize(new java.awt.Dimension(1000, 70));

        lblTituloPersonal.setFont(new java.awt.Font("SansSerif", 1, 25)); // NOI18N
        lblTituloPersonal.setForeground(new java.awt.Color(23, 42, 67));
        lblTituloPersonal.setText("Reportes administrativos");
        lblTituloPersonal.setPreferredSize(new java.awt.Dimension(350, 35));

        lblModuloPersonal.setForeground(new java.awt.Color(8, 124, 255));
        lblModuloPersonal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblModuloPersonal.setText("REPORTES");
        lblModuloPersonal.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(8, 124, 255), 1, true));
        lblModuloPersonal.setPreferredSize(new java.awt.Dimension(58, 58));

        javax.swing.GroupLayout pnlEncabezadoPersonalLayout = new javax.swing.GroupLayout(pnlEncabezadoPersonal);
        pnlEncabezadoPersonal.setLayout(pnlEncabezadoPersonalLayout);
        pnlEncabezadoPersonalLayout.setHorizontalGroup(
            pnlEncabezadoPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoPersonalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTituloPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblModuloPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlEncabezadoPersonalLayout.setVerticalGroup(
            pnlEncabezadoPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEncabezadoPersonalLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlEncabezadoPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTituloPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblModuloPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlFormularioPersonal.setBackground(new java.awt.Color(255, 255, 255));
        pnlFormularioPersonal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlFormularioPersonal.setPreferredSize(new java.awt.Dimension(1000, 285));

        lblTituloFormulario.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        lblTituloFormulario.setForeground(new java.awt.Color(23, 42, 67));
        lblTituloFormulario.setText("Filtros del reporte");
        lblTituloFormulario.setPreferredSize(new java.awt.Dimension(300, 30));

        lblCamposObligatorios.setFont(new java.awt.Font("SansSerif", 0, 11)); // NOI18N
        lblCamposObligatorios.setForeground(new java.awt.Color(107, 127, 153));
        lblCamposObligatorios.setText("Los campos con * son obligatorios");

        lblCodigoEmpleado.setText("Numero de reporte");

        txtCodigoEmpleado.setPreferredSize(new java.awt.Dimension(300, 32));

        lblFechaIngreso.setText("Fecha desde *");

        txtFechaIngreso.setPreferredSize(new java.awt.Dimension(300, 32));

        lblTipoContrato.setText("Tipo de reporte *");

        cboTipoContrato.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Clientes", "Membresias", "Accesos", "Rutinas", "Salud", "Nutricion", "Finanzas", "Personal", "Auditoria" }));

        cboTipoContrato.setPreferredSize(new java.awt.Dimension(300, 32));

        lblSalario.setText("Formato de salida *");

        txtSalario.setToolTipText("Campo reservado para opciones internas del reporte.");
        txtSalario.setPreferredSize(new java.awt.Dimension(300, 32));

        cboPersona.setPreferredSize(new java.awt.Dimension(300, 32));

        lblPersona.setText("Modulo / entidad *");

        lblCedulaPersona.setText("Fecha hasta *");

        txtCedulaPersona.setEditable(false);
        txtCedulaPersona.setPreferredSize(new java.awt.Dimension(300, 32));

        lblTurno.setText("Filtro adicional");

        txtTurno.setPreferredSize(new java.awt.Dimension(300, 32));

        chkEstadoEmpleado.setBackground(new java.awt.Color(255, 255, 255));
        chkEstadoEmpleado.setForeground(new java.awt.Color(23, 42, 67));
        chkEstadoEmpleado.setSelected(true);
        chkEstadoEmpleado.setText("Incluir inactivos");

        btnGuardar.setBackground(new java.awt.Color(8, 124, 255));
        btnGuardar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setText("Generar reporte");
        btnGuardar.setPreferredSize(new java.awt.Dimension(375, 38));
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnModificar.setBackground(new java.awt.Color(157, 78, 221));
        btnModificar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnModificar.setForeground(new java.awt.Color(255, 255, 255));
        btnModificar.setText("Vista previa");
        btnModificar.setEnabled(false);
        btnModificar.setPreferredSize(new java.awt.Dimension(180, 36));
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });

        btnLimpiar.setBackground(new java.awt.Color(226, 232, 240));
        btnLimpiar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnLimpiar.setForeground(new java.awt.Color(52, 74, 100));
        btnLimpiar.setText("Limpiar formulario");
        btnLimpiar.setPreferredSize(new java.awt.Dimension(375, 36));
        btnLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpiarActionPerformed(evt);
            }
        });

        btnDesactivar.setBackground(new java.awt.Color(255, 214, 0));
        btnDesactivar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnDesactivar.setForeground(new java.awt.Color(41, 31, 0));
        btnDesactivar.setText("Exportar");
        btnDesactivar.setEnabled(false);
        btnDesactivar.setPreferredSize(new java.awt.Dimension(180, 36));
        btnDesactivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDesactivarActionPerformed(evt);
            }
        });

        btnEliminar.setBackground(new java.awt.Color(255, 23, 68));
        btnEliminar.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setText("Imprimir");
        btnEliminar.setEnabled(false);
        btnEliminar.setPreferredSize(new java.awt.Dimension(375, 36));
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFormularioPersonalLayout = new javax.swing.GroupLayout(pnlFormularioPersonal);
        pnlFormularioPersonal.setLayout(pnlFormularioPersonalLayout);
        pnlFormularioPersonalLayout.setHorizontalGroup(
            pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormularioPersonalLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCamposObligatorios)
                    .addGroup(pnlFormularioPersonalLayout.createSequentialGroup()
                        .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCodigoEmpleado)
                            .addComponent(txtCodigoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPersona)
                            .addComponent(cboPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFechaIngreso)
                            .addComponent(txtFechaIngreso, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCedulaPersona)
                            .addComponent(txtCedulaPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTipoContrato)
                            .addComponent(cboTipoContrato, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTurno)
                            .addComponent(txtTurno, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSalario)
                            .addComponent(txtSalario, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(chkEstadoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlFormularioPersonalLayout.createSequentialGroup()
                        .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnDesactivar, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        pnlFormularioPersonalLayout.setVerticalGroup(
            pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormularioPersonalLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblTituloFormulario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblCamposObligatorios)
                .addGap(13, 13, 13)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCodigoEmpleado)
                    .addComponent(lblFechaIngreso)
                    .addComponent(lblTipoContrato)
                    .addComponent(lblSalario))
                .addGap(5, 5, 5)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCodigoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFechaIngreso, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTipoContrato, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSalario, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPersona)
                    .addComponent(lblCedulaPersona)
                    .addComponent(lblTurno))
                .addGap(5, 5, 5)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCedulaPersona, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTurno, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkEstadoEmpleado, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(17, 17, 17)
                .addGroup(pnlFormularioPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnModificar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDesactivar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimpiar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pnlTablaPersonal.setBackground(new java.awt.Color(255, 255, 255));
        pnlTablaPersonal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(212, 225, 239)));
        pnlTablaPersonal.setPreferredSize(new java.awt.Dimension(1000, 400));

        txtBuscarPersonal.setToolTipText("Buscar por código, cédula, nombre, apellido o correo");
        txtBuscarPersonal.setPreferredSize(new java.awt.Dimension(400, 38));
        txtBuscarPersonal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarPersonalActionPerformed(evt);
            }
        });

        btnBuscarPersonal.setBackground(new java.awt.Color(8, 124, 255));
        btnBuscarPersonal.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        btnBuscarPersonal.setForeground(new java.awt.Color(255, 255, 255));
        btnBuscarPersonal.setText("Buscar");
        btnBuscarPersonal.setPreferredSize(new java.awt.Dimension(100, 38));

        tblPersonal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"ID", "Código", "Cédula", "Empleado", "Ingreso", "Contrato", "Horario", "Salario", "Estado"}
        ));
        jScrollPane1.setViewportView(tblPersonal);

        lblBuscarPersonal.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblBuscarPersonal.setForeground(new java.awt.Color(107, 127, 153));
        lblBuscarPersonal.setText("Buscar o filtrar informacion...");
        lblBuscarPersonal.setPreferredSize(new java.awt.Dimension(500, 20));

        lblCantidadPersonal.setText("0 registros encontrados");

        javax.swing.GroupLayout pnlTablaPersonalLayout = new javax.swing.GroupLayout(pnlTablaPersonal);
        pnlTablaPersonal.setLayout(pnlTablaPersonalLayout);
        pnlTablaPersonalLayout.setHorizontalGroup(
            pnlTablaPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTablaPersonalLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlTablaPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBuscarPersonal)
                    .addGroup(pnlTablaPersonalLayout.createSequentialGroup()
                        .addComponent(txtBuscarPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, 760, Short.MAX_VALUE)
                        .addGap(12, 12, 12)
                        .addComponent(btnBuscarPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblCantidadPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1090, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlTablaPersonalLayout.setVerticalGroup(
            pnlTablaPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTablaPersonalLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblBuscarPersonal)
                .addGap(6, 6, 6)
                .addGroup(pnlTablaPersonalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscarPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantidadPersonal))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addGap(16, 16, 16))
        );

        javax.swing.GroupLayout pnlBaseReportesLayout = new javax.swing.GroupLayout(pnlBaseReportes);
        pnlBaseReportes.setLayout(pnlBaseReportesLayout);
        pnlBaseReportesLayout.setHorizontalGroup(
            pnlBaseReportesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlEncabezadoPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, 1175, Short.MAX_VALUE)
            .addGroup(pnlBaseReportesLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlBaseReportesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlFormularioPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTablaPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlBaseReportesLayout.setVerticalGroup(
            pnlBaseReportesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBaseReportesLayout.createSequentialGroup()
                .addComponent(pnlEncabezadoPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(pnlFormularioPersonal, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(pnlTablaPersonal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBaseReportes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBaseReportes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        guardarRegistro();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void txtBuscarPersonalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBuscarPersonalActionPerformed
        buscarRegistros();
    }//GEN-LAST:event_txtBuscarPersonalActionPerformed

    private void btnLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpiarActionPerformed
        limpiarFormulario();
    }//GEN-LAST:event_btnLimpiarActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        modificarRegistro();
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnDesactivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDesactivarActionPerformed
        desactivarRegistro();
    }//GEN-LAST:event_btnDesactivarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        eliminarRegistro();
    }//GEN-LAST:event_btnEliminarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscarPersonal;
    private javax.swing.JButton btnDesactivar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnLimpiar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JComboBox<modelo.Persona> cboPersona;
    private javax.swing.JCheckBox chkEstadoEmpleado;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblBuscarPersonal;
    private javax.swing.JLabel lblCamposObligatorios;
    private javax.swing.JLabel lblCantidadPersonal;
    private javax.swing.JLabel lblCedulaPersona;
    private javax.swing.JLabel lblCodigoEmpleado;
    private javax.swing.JLabel lblFechaIngreso;
    private javax.swing.JLabel lblModuloPersonal;
    private javax.swing.JLabel lblTurno;
    private javax.swing.JLabel lblPersona;
    private javax.swing.JLabel lblTipoContrato;
    private javax.swing.JLabel lblSalario;
    private javax.swing.JLabel lblTituloPersonal;
    private javax.swing.JLabel lblTituloFormulario;
    private javax.swing.JPanel pnlBaseReportes;
    private javax.swing.JPanel pnlEncabezadoPersonal;
    private javax.swing.JPanel pnlFormularioPersonal;
    private javax.swing.JPanel pnlTablaPersonal;
    private javax.swing.JTable tblPersonal;
    private javax.swing.JTextField txtBuscarPersonal;
    private javax.swing.JTextField txtCedulaPersona;
    private javax.swing.JTextField txtCodigoEmpleado;
    private javax.swing.JTextField txtFechaIngreso;
    private javax.swing.JTextField txtTurno;
    private javax.swing.JComboBox<String> cboTipoContrato;
    private javax.swing.JTextField txtSalario;
    // End of variables declaration//GEN-END:variables
}
