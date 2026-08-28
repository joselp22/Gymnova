package vista;

import controlador.DashboardControlador;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import modelo.Usuario;
import utilidades.SesionUsuario;

/**
 * Dashboard principal de GYMNOVA. La estructura se administra desde Design.
 */
public class PnlInicioEntrenador extends javax.swing.JPanel {

    private final DashboardControlador dashboardControlador;

    public PnlInicioEntrenador() {
        initComponents();
        dashboardControlador = new DashboardControlador();
        configurarTabla();
        utilidades.TemaDashboard.aplicar(this,
                utilidades.TemaDashboard.RolVisual.ENTRENADOR);
        refrescarDatos();
    }

    public final void refrescarDatos() {

        if (!SesionUsuario.haySesionActiva()) {
            return;
        }

        Usuario usuario = SesionUsuario.getUsuarioActual();
        String rol = normalizarRol(usuario.getNombreRol());
        Long idPersona = usuario.getIdPersona();

        configurarContenidoRol(
                rol,
                usuario.getNombreUsuario()
        );

        Map<String, Object> resumen
                = dashboardControlador.obtenerResumenPorRol(
                        rol,
                        idPersona
                );

        mostrarValoresRol(rol, resumen);
        cargarActividadRol(rol, idPersona);

        utilidades.GraficoActividad.mostrar(
                lblGraficoResumen,
                dashboardControlador.obtenerActividadSemanalPorRol(
                        rol, idPersona)
        );

        if (!dashboardControlador.getMensaje().isBlank()) {
            lblResumenIngresos.setText(
                    "No fue posible actualizar todos los indicadores"
            );
        }
    }

    private void configurarContenidoRol(
            String rol,
            String nombreUsuario
    ) {

        lblBienvenida.setText(
                "¡Bienvenido, " + nombreUsuario + "!"
        );

        switch (rol) {
            case "RECEPCIONISTA" -> {
                lblSubtitulo.setText(
                        "Resumen de recepción y atención de hoy"
                );
                lblTituloClientes.setText("Clientes registrados hoy");
                lblTituloMembresias.setText("Reservas del día");
                lblTituloPagos.setText("Asistencias del día");
                lblTituloRutinas.setText("Pagos registrados hoy");
                lblTituloUltimos.setText("Membresías próximas a vencer");
                lblTituloResumen.setText("Acciones rápidas");
                lblTituloGrafico.setText("Actividad de recepción");
            }
            case "ENTRENADOR" -> {
                lblSubtitulo.setText(
                        "Panel del entrenador: actividad y clientes"
                );
                lblTituloClientes.setText("Clientes asignados");
                lblTituloMembresias.setText("Rutinas activas");
                lblTituloPagos.setText("Evaluaciones pendientes");
                lblTituloRutinas.setText("Sesiones en curso");
                lblTituloUltimos.setText("Rutinas y clientes asignados");
                lblTituloResumen.setText("Acciones del entrenador");
                lblTituloGrafico.setText("Progreso semanal");
            }
            case "NUTRICIONISTA" -> {
                lblSubtitulo.setText(
                        "Seguimiento nutricional de tus clientes"
                );
                lblTituloClientes.setText("Clientes asignados");
                lblTituloMembresias.setText("Planes activos");
                lblTituloPagos.setText("Revisiones pendientes");
                lblTituloRutinas.setText("Recomendaciones activas");
                lblTituloUltimos.setText("Planes nutricionales recientes");
                lblTituloResumen.setText("Acciones de nutrición");
                lblTituloGrafico.setText("Seguimiento nutricional");
            }
            case "CLIENTE" -> {
                lblSubtitulo.setText(
                        "Mi rutina, membresía y progreso"
                );
                lblTituloClientes.setText("Mi membresía activa");
                lblTituloMembresias.setText("Mi rutina activa");
                lblTituloPagos.setText("Mi peso actual");
                lblTituloRutinas.setText("Mi plan nutricional");
                lblTituloUltimos.setText("Mi rutina y actividad reciente");
                lblTituloResumen.setText("Mi acceso rápido");
                lblTituloGrafico.setText("Mi progreso");
            }
            default -> {
                lblSubtitulo.setText(
                        "Aquí tienes el resumen general del gimnasio"
                );
                lblTituloClientes.setText("Clientes activos");
                lblTituloMembresias.setText("Membresías activas");
                lblTituloPagos.setText("Pagos del mes");
                lblTituloRutinas.setText("Rutinas asignadas");
                lblTituloUltimos.setText("Últimos clientes registrados");
                lblTituloResumen.setText("Resumen administrativo");
                lblTituloGrafico.setText("Actividad semanal");
            }
        }
    }

    private void mostrarValoresRol(
            String rol,
            Map<String, Object> resumen
    ) {

        lblClientesActivos.setText(
                formatearNumero(resumen.get("principal1"))
        );
        lblMembresiasActivas.setText(
                formatearNumero(resumen.get("principal2"))
        );

        Object tercerValor = resumen.get("principal3");
        Object cuartoValor = resumen.get("principal4");

        if ("ADMINISTRADOR".equals(rol)
                || "RECEPCIONISTA".equals(rol)) {
            if ("ADMINISTRADOR".equals(rol)) {
                lblPagosMes.setText(formatearMoneda(tercerValor));
                lblRutinasAsignadas.setText(
                        formatearNumero(cuartoValor)
                );
            } else {
                lblPagosMes.setText(formatearNumero(tercerValor));
                lblRutinasAsignadas.setText(formatearMoneda(cuartoValor));
            }
        } else if ("CLIENTE".equals(rol)) {
            lblPagosMes.setText(formatearDecimal(tercerValor) + " kg");
            lblRutinasAsignadas.setText(formatearNumero(cuartoValor));
        } else {
            lblPagosMes.setText(formatearNumero(tercerValor));
            lblRutinasAsignadas.setText(formatearNumero(cuartoValor));
        }

        configurarResumenRol(rol, resumen);
    }

    private void configurarResumenRol(
            String rol,
            Map<String, Object> resumen
    ) {

        switch (rol) {
            case "RECEPCIONISTA" -> {
                lblResumenClientes.setText("Registrar cliente y persona");
                lblResumenMembresias.setText("Crear o renovar membresía");
                lblResumenRutinas.setText("Registrar asistencia o reserva");
                lblResumenIngresos.setText("Registrar pago y comprobante");
                lblGraficoResumen.setText(
                        "Atención diaria basada en registros reales"
                );
            }
            case "ENTRENADOR" -> {
                lblResumenClientes.setText("Ver clientes asignados");
                lblResumenMembresias.setText("Asignar o revisar rutinas");
                lblResumenRutinas.setText("Registrar progreso físico");
                lblResumenIngresos.setText("Consultar evaluaciones");
                lblGraficoResumen.setText(
                        "Rutinas, progreso y evaluaciones de tus clientes"
                );
            }
            case "NUTRICIONISTA" -> {
                lblResumenClientes.setText("Crear plan nutricional");
                lblResumenMembresias.setText("Consultar alimentos");
                lblResumenRutinas.setText("Registrar recomendación");
                lblResumenIngresos.setText("Revisar indicadores de salud");
                lblGraficoResumen.setText(
                        "Planes y recomendaciones que requieren seguimiento"
                );
            }
            case "CLIENTE" -> {
                lblResumenClientes.setText(
                        "Reservas activas: "
                        + formatearNumero(resumen.get("reservas"))
                );
                lblResumenMembresias.setText(
                        "Asistencias del mes: "
                        + formatearNumero(resumen.get("asistencias"))
                );
                lblResumenRutinas.setText("Ver mi rutina y progreso");
                lblResumenIngresos.setText("Ver mi plan nutricional");
                lblGraficoResumen.setText(
                        "Tu información personal se actualiza con cada registro"
                );
            }
            default -> {
                lblResumenClientes.setText(
                        "Clientes activos: "
                        + formatearNumero(resumen.get("principal1"))
                );
                lblResumenMembresias.setText(
                        "Membresías activas: "
                        + formatearNumero(resumen.get("principal2"))
                );
                lblResumenRutinas.setText(
                        "Rutinas asignadas: "
                        + formatearNumero(resumen.get("principal4"))
                );
                lblResumenIngresos.setText(
                        "Ingresos del mes: "
                        + formatearMoneda(resumen.get("principal3"))
                );
                lblGraficoResumen.setText(
                        "Resumen consolidado de la operación del gimnasio"
                );
            }
        }

        lblDiasGrafico.setText(
                "Lun       Mar       Mié       Jue       Vie       Sáb       Dom"
        );
    }

    private void cargarActividadRol(
            String rol,
            Long idPersona
    ) {

        DefaultTableModel modelo
                = (DefaultTableModel) tblUltimosClientes.getModel();
        modelo.setRowCount(0);

        List<Map<String, Object>> actividad
                = dashboardControlador.listarActividadPorRol(
                        rol,
                        idPersona,
                        6
                );

        int numero = 1;
        for (Map<String, Object> fila : actividad) {
            modelo.addRow(new Object[]{
                numero++,
                valor(fila.get("nombre")),
                valor(fila.get("detalle")),
                valor(fila.get("referencia")),
                valor(fila.get("fecha")),
                valor(fila.get("estado"))
            });
        }

        lblVerClientes.setText(
                actividad.isEmpty()
                        ? "No existen registros para mostrar"
                        : "Mostrando " + actividad.size() + " registros recientes"
        );
    }

    private String normalizarRol(String nombreRol) {
        return nombreRol == null
                ? ""
                : nombreRol.trim().toUpperCase(Locale.ROOT);
    }

    private String formatearNumero(Object valor) {
        return valor instanceof Number
                ? NumberFormat.getIntegerInstance().format(valor)
                : "0";
    }

    private String formatearDecimal(Object valor) {
        return valor instanceof BigDecimal decimal
                ? decimal.stripTrailingZeros().toPlainString()
                : valor instanceof Number numero
                        ? String.valueOf(numero.doubleValue())
                        : "0";
    }

    private String formatearMoneda(Object valor) {
        BigDecimal monto = valor instanceof BigDecimal decimal
                ? decimal
                : BigDecimal.ZERO;
        return NumberFormat.getCurrencyInstance(
                Locale.US
        ).format(monto);
    }

    private String valor(Object valor) {
        return valor == null ? "-" : valor.toString();
    }

    private void configurarTabla() {
        String[] columnas = {
            "#", "Nombre", "Detalle", "Referencia", "Fecha", "Estado"
        };

        tblUltimosClientes.setModel(new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        });

        tblUltimosClientes.setRowHeight(38);
        tblUltimosClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUltimosClientes.setFillsViewportHeight(true);
        tblUltimosClientes.setShowGrid(false);
        tblUltimosClientes.setBackground(new java.awt.Color(7, 21, 43));
        tblUltimosClientes.setForeground(new java.awt.Color(220, 235, 255));
        tblUltimosClientes.setSelectionBackground(new java.awt.Color(8, 124, 255));
        tblUltimosClientes.setSelectionForeground(java.awt.Color.WHITE);
        tblUltimosClientes.getTableHeader().setBackground(
                new java.awt.Color(5, 15, 32)
        );
        tblUltimosClientes.getTableHeader().setForeground(
                new java.awt.Color(220, 235, 255)
        );
        tblUltimosClientes.getTableHeader().setPreferredSize(
                new java.awt.Dimension(0, 40)
        );
        jScrollPaneUltimos.getViewport().setBackground(
                new java.awt.Color(7, 21, 43)
        );
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblBienvenida = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        pnlTarjetaClientes = new javax.swing.JPanel();
        lblIconoClientes = new javax.swing.JLabel();
        lblTituloClientes = new javax.swing.JLabel();
        lblClientesActivos = new javax.swing.JLabel();
        lblEstadoClientes = new javax.swing.JLabel();
        pnlTarjetaMembresias = new javax.swing.JPanel();
        lblIconoMembresias = new javax.swing.JLabel();
        lblTituloMembresias = new javax.swing.JLabel();
        lblMembresiasActivas = new javax.swing.JLabel();
        lblEstadoMembresias = new javax.swing.JLabel();
        pnlTarjetaPagos = new javax.swing.JPanel();
        lblIconoPagos = new javax.swing.JLabel();
        lblTituloPagos = new javax.swing.JLabel();
        lblPagosMes = new javax.swing.JLabel();
        lblEstadoPagos = new javax.swing.JLabel();
        pnlTarjetaRutinas = new javax.swing.JPanel();
        lblIconoRutinas = new javax.swing.JLabel();
        lblTituloRutinas = new javax.swing.JLabel();
        lblRutinasAsignadas = new javax.swing.JLabel();
        lblEstadoRutinas = new javax.swing.JLabel();
        pnlUltimosClientes = new javax.swing.JPanel();
        lblTituloUltimos = new javax.swing.JLabel();
        jScrollPaneUltimos = new javax.swing.JScrollPane();
        tblUltimosClientes = new javax.swing.JTable();
        lblVerClientes = new javax.swing.JLabel();
        pnlResumenRapido = new javax.swing.JPanel();
        lblTituloResumen = new javax.swing.JLabel();
        lblResumenClientes = new javax.swing.JLabel();
        lblResumenMembresias = new javax.swing.JLabel();
        lblResumenRutinas = new javax.swing.JLabel();
        lblResumenIngresos = new javax.swing.JLabel();
        pnlGraficoSemanal = new javax.swing.JPanel();
        lblTituloGrafico = new javax.swing.JLabel();
        lblGraficoResumen = new javax.swing.JLabel();
        lblDiasGrafico = new javax.swing.JLabel();

        setBackground(new java.awt.Color(3, 10, 24));
        setMinimumSize(new java.awt.Dimension(950, 600));
        setPreferredSize(new java.awt.Dimension(1280, 720));

        lblBienvenida.setFont(new java.awt.Font("SansSerif", 1, 28)); // NOI18N
        lblBienvenida.setForeground(new java.awt.Color(255, 255, 255));
        lblBienvenida.setText("¡Bienvenido a GYMNOVA!");

        lblSubtitulo.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        lblSubtitulo.setForeground(new java.awt.Color(169, 187, 211));
        lblSubtitulo.setText("Aqui tienes el resumen de tu gimnasio");

        configurarTarjeta(pnlTarjetaClientes);
        configurarIcono(lblIconoClientes, "●");
        configurarTitulo(lblTituloClientes, "Clientes activos");
        configurarValor(lblClientesActivos, "0");
        configurarVariacion(lblEstadoClientes, "↑ 12% vs. mes anterior");
        crearLayoutTarjeta(pnlTarjetaClientes, lblIconoClientes,
                lblTituloClientes, lblClientesActivos, lblEstadoClientes);

        configurarTarjeta(pnlTarjetaMembresias);
        configurarIcono(lblIconoMembresias, "▣");
        configurarTitulo(lblTituloMembresias, "Membresias activas");
        configurarValor(lblMembresiasActivas, "0");
        configurarVariacion(lblEstadoMembresias, "↑ 9% vs. mes anterior");
        crearLayoutTarjeta(pnlTarjetaMembresias, lblIconoMembresias,
                lblTituloMembresias, lblMembresiasActivas,
                lblEstadoMembresias);

        configurarTarjeta(pnlTarjetaPagos);
        configurarIcono(lblIconoPagos, "$");
        configurarTitulo(lblTituloPagos, "Pagos del mes");
        configurarValor(lblPagosMes, "$0.00");
        configurarVariacion(lblEstadoPagos, "↑ 15% vs. mes anterior");
        crearLayoutTarjeta(pnlTarjetaPagos, lblIconoPagos,
                lblTituloPagos, lblPagosMes, lblEstadoPagos);

        configurarTarjeta(pnlTarjetaRutinas);
        configurarIcono(lblIconoRutinas, "◆");
        configurarTitulo(lblTituloRutinas, "Rutinas asignadas");
        configurarValor(lblRutinasAsignadas, "0");
        configurarVariacion(lblEstadoRutinas, "↑ 8% vs. semana anterior");
        crearLayoutTarjeta(pnlTarjetaRutinas, lblIconoRutinas,
                lblTituloRutinas, lblRutinasAsignadas, lblEstadoRutinas);

        pnlUltimosClientes.setBackground(new java.awt.Color(7, 21, 43));
        pnlUltimosClientes.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(23, 52, 87)));

        lblTituloUltimos.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lblTituloUltimos.setForeground(new java.awt.Color(255, 255, 255));
        lblTituloUltimos.setText("Ultimos clientes registrados");

        jScrollPaneUltimos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(23, 52, 87)));
        tblUltimosClientes.setBackground(new java.awt.Color(7, 21, 43));
        tblUltimosClientes.setForeground(new java.awt.Color(220, 235, 255));
        tblUltimosClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"#", "Nombre", "Telefono", "Membresia", "Registro", "Estado"}
        ));
        jScrollPaneUltimos.setViewportView(tblUltimosClientes);

        lblVerClientes.setFont(new java.awt.Font("SansSerif", 1, 13)); // NOI18N
        lblVerClientes.setForeground(new java.awt.Color(8, 124, 255));
        lblVerClientes.setText("Ver todos los clientes                                      ›");

        javax.swing.GroupLayout pnlUltimosClientesLayout = new javax.swing.GroupLayout(pnlUltimosClientes);
        pnlUltimosClientes.setLayout(pnlUltimosClientesLayout);
        pnlUltimosClientesLayout.setHorizontalGroup(
            pnlUltimosClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUltimosClientesLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlUltimosClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloUltimos)
                    .addComponent(jScrollPaneUltimos, javax.swing.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE)
                    .addComponent(lblVerClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlUltimosClientesLayout.setVerticalGroup(
            pnlUltimosClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUltimosClientesLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lblTituloUltimos)
                .addGap(16, 16, 16)
                .addComponent(jScrollPaneUltimos, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addGap(14, 14, 14)
                .addComponent(lblVerClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );

        pnlResumenRapido.setBackground(new java.awt.Color(7, 21, 43));
        pnlResumenRapido.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(23, 52, 87)));
        lblTituloResumen.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lblTituloResumen.setForeground(java.awt.Color.WHITE);
        lblTituloResumen.setText("Resumen rapido");
        configurarResumen(lblResumenClientes, "Nuevos clientes (este mes)                         0");
        configurarResumen(lblResumenMembresias, "Membresias por vencer                                0");
        configurarResumen(lblResumenRutinas, "Rutinas pendientes                                      0");
        configurarResumen(lblResumenIngresos, "Ingresos pendientes                                  $0.00");

        javax.swing.GroupLayout pnlResumenRapidoLayout = new javax.swing.GroupLayout(pnlResumenRapido);
        pnlResumenRapido.setLayout(pnlResumenRapidoLayout);
        pnlResumenRapidoLayout.setHorizontalGroup(
            pnlResumenRapidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenRapidoLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlResumenRapidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloResumen)
                    .addComponent(lblResumenClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(lblResumenMembresias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblResumenRutinas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblResumenIngresos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlResumenRapidoLayout.setVerticalGroup(
            pnlResumenRapidoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlResumenRapidoLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblTituloResumen)
                .addGap(15, 15, 15)
                .addComponent(lblResumenClientes)
                .addGap(13, 13, 13)
                .addComponent(lblResumenMembresias)
                .addGap(13, 13, 13)
                .addComponent(lblResumenRutinas)
                .addGap(13, 13, 13)
                .addComponent(lblResumenIngresos)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        pnlGraficoSemanal.setBackground(new java.awt.Color(7, 21, 43));
        pnlGraficoSemanal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(23, 52, 87)));
        lblTituloGrafico.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        lblTituloGrafico.setForeground(java.awt.Color.WHITE);
        lblTituloGrafico.setText("Actividad semanal");
        lblGraficoResumen.setFont(new java.awt.Font("SansSerif", 1, 22)); // NOI18N
        lblGraficoResumen.setForeground(new java.awt.Color(8, 124, 255));
        lblGraficoResumen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblGraficoResumen.setText("●──●──●────●──●────●──●");
        lblDiasGrafico.setForeground(new java.awt.Color(169, 187, 211));
        lblDiasGrafico.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDiasGrafico.setText("Lun      Mar      Mie      Jue      Vie      Sab      Dom");

        javax.swing.GroupLayout pnlGraficoSemanalLayout = new javax.swing.GroupLayout(pnlGraficoSemanal);
        pnlGraficoSemanal.setLayout(pnlGraficoSemanalLayout);
        pnlGraficoSemanalLayout.setHorizontalGroup(
            pnlGraficoSemanalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGraficoSemanalLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlGraficoSemanalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloGrafico)
                    .addComponent(lblGraficoResumen, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(lblDiasGrafico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        pnlGraficoSemanalLayout.setVerticalGroup(
            pnlGraficoSemanalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlGraficoSemanalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblTituloGrafico)
                .addGap(22, 22, 22)
                .addComponent(lblGraficoResumen, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblDiasGrafico)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBienvenida)
                    .addComponent(lblSubtitulo)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlTarjetaClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                        .addGap(14, 14, 14)
                        .addComponent(pnlTarjetaMembresias, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                        .addGap(14, 14, 14)
                        .addComponent(pnlTarjetaPagos, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                        .addGap(14, 14, 14)
                        .addComponent(pnlTarjetaRutinas, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlUltimosClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlResumenRapido, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlGraficoSemanal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(20, 20, 20))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(lblBienvenida)
                .addGap(4, 4, 4)
                .addComponent(lblSubtitulo)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlTarjetaClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                    .addComponent(pnlTarjetaMembresias, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTarjetaPagos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlTarjetaRutinas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlUltimosClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlResumenRapido, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)
                        .addComponent(pnlGraficoSemanal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configurarTarjeta(javax.swing.JPanel panel) {
        panel.setBackground(new java.awt.Color(7, 21, 43));
        panel.setBorder(javax.swing.BorderFactory.createLineBorder(
                new java.awt.Color(23, 52, 87)
        ));
    }

    private void configurarIcono(javax.swing.JLabel etiqueta, String texto) {
        etiqueta.setBackground(new java.awt.Color(9, 38, 76));
        etiqueta.setFont(new java.awt.Font("SansSerif", 1, 26));
        etiqueta.setForeground(new java.awt.Color(8, 124, 255));
        etiqueta.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etiqueta.setOpaque(true);
        etiqueta.setText(texto);
        etiqueta.setBorder(javax.swing.BorderFactory.createLineBorder(
                new java.awt.Color(35, 76, 124)
        ));
        etiqueta.setPreferredSize(new java.awt.Dimension(62, 62));
    }

    private void configurarTitulo(javax.swing.JLabel etiqueta, String texto) {
        etiqueta.setFont(new java.awt.Font("SansSerif", 0, 14));
        etiqueta.setForeground(new java.awt.Color(205, 220, 239));
        etiqueta.setText(texto);
    }

    private void configurarValor(javax.swing.JLabel etiqueta, String texto) {
        etiqueta.setFont(new java.awt.Font("SansSerif", 1, 26));
        etiqueta.setForeground(java.awt.Color.WHITE);
        etiqueta.setText(texto);
    }

    private void configurarVariacion(javax.swing.JLabel etiqueta, String texto) {
        etiqueta.setFont(new java.awt.Font("SansSerif", 0, 11));
        etiqueta.setForeground(new java.awt.Color(0, 230, 118));
        etiqueta.setText(texto);
    }

    private void configurarResumen(javax.swing.JLabel etiqueta, String texto) {
        etiqueta.setFont(new java.awt.Font("SansSerif", 0, 12));
        etiqueta.setForeground(new java.awt.Color(220, 235, 255));
        etiqueta.setText(texto);
    }

    private void crearLayoutTarjeta(
            javax.swing.JPanel panel,
            javax.swing.JLabel icono,
            javax.swing.JLabel titulo,
            javax.swing.JLabel valor,
            javax.swing.JLabel variacion
    ) {
        javax.swing.GroupLayout tarjetaLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(tarjetaLayout);
        tarjetaLayout.setHorizontalGroup(
            tarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tarjetaLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(icono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addGroup(tarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(titulo)
                    .addComponent(valor)
                    .addComponent(variacion))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        tarjetaLayout.setVerticalGroup(
            tarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tarjetaLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(tarjetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(icono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(tarjetaLayout.createSequentialGroup()
                        .addComponent(titulo)
                        .addGap(6, 6, 6)
                        .addComponent(valor)))
                .addGap(15, 15, 15)
                .addComponent(variacion)
                .addContainerGap(16, Short.MAX_VALUE))
        );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPaneUltimos;
    private javax.swing.JLabel lblBienvenida;
    private javax.swing.JLabel lblClientesActivos;
    private javax.swing.JLabel lblDiasGrafico;
    private javax.swing.JLabel lblEstadoClientes;
    private javax.swing.JLabel lblEstadoMembresias;
    private javax.swing.JLabel lblEstadoPagos;
    private javax.swing.JLabel lblEstadoRutinas;
    private javax.swing.JLabel lblGraficoResumen;
    private javax.swing.JLabel lblIconoClientes;
    private javax.swing.JLabel lblIconoMembresias;
    private javax.swing.JLabel lblIconoPagos;
    private javax.swing.JLabel lblIconoRutinas;
    private javax.swing.JLabel lblMembresiasActivas;
    private javax.swing.JLabel lblPagosMes;
    private javax.swing.JLabel lblResumenClientes;
    private javax.swing.JLabel lblResumenIngresos;
    private javax.swing.JLabel lblResumenMembresias;
    private javax.swing.JLabel lblResumenRutinas;
    private javax.swing.JLabel lblRutinasAsignadas;
    private javax.swing.JLabel lblSubtitulo;
    private javax.swing.JLabel lblTituloClientes;
    private javax.swing.JLabel lblTituloGrafico;
    private javax.swing.JLabel lblTituloMembresias;
    private javax.swing.JLabel lblTituloPagos;
    private javax.swing.JLabel lblTituloResumen;
    private javax.swing.JLabel lblTituloRutinas;
    private javax.swing.JLabel lblTituloUltimos;
    private javax.swing.JLabel lblVerClientes;
    private javax.swing.JPanel pnlGraficoSemanal;
    private javax.swing.JPanel pnlResumenRapido;
    private javax.swing.JPanel pnlTarjetaClientes;
    private javax.swing.JPanel pnlTarjetaMembresias;
    private javax.swing.JPanel pnlTarjetaPagos;
    private javax.swing.JPanel pnlTarjetaRutinas;
    private javax.swing.JPanel pnlUltimosClientes;
    private javax.swing.JTable tblUltimosClientes;
    // End of variables declaration//GEN-END:variables
}
