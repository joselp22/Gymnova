package vista;

import controlador.NutricionistaWorkspaceControlador;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.PlanNutricional;

/** Reportes limitados al alcance profesional del Nutricionista conectado. */
public class PnlReportesNutricionista extends JPanel {

    private final NutricionistaWorkspaceControlador controlador
            = new NutricionistaWorkspaceControlador();

    private final JLabel lblClientes = valorGrande();
    private final JLabel lblActivos = valorGrande();
    private final JLabel lblFinalizados = valorGrande();
    private final JLabel lblResultados = valorGrande();
    private final JLabel lblRecomendaciones = valorGrande();

    private final JComboBox<String> cboReporte = new JComboBox<>(new String[]{
        "Clientes", "Planes nutricionales", "Indicadores registrados", "Recomendaciones"
    });
    private final JTextField txtDesde = NutricionistaUI.campo();
    private final JTextField txtHasta = NutricionistaUI.campo();
    private final JTable tabla = new JTable();
    private final JLabel lblCantidad = new JLabel("0 registros");

    public PnlReportesNutricionista() {
        construir();
        eventos();
        refrescarDatos();
    }

    private void construir() {
        setLayout(new BorderLayout());
        setBackground(NutricionistaUI.FONDO);
        JPanel pagina = NutricionistaUI.pagina();
        pagina.add(NutricionistaUI.encabezado(
                "Mis reportes",
                "Consulte únicamente la información asociada a su atención nutricional."),
                BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);

        JPanel resumen = new JPanel(new GridLayout(1, 5, 10, 0));
        resumen.setOpaque(false);
        resumen.add(tarjeta("Clientes", lblClientes));
        resumen.add(tarjeta("Planes activos", lblActivos));
        resumen.add(tarjeta("Finalizados", lblFinalizados));
        resumen.add(tarjeta("Resultados", lblResultados));
        resumen.add(tarjeta("Recomendaciones", lblRecomendaciones));
        centro.add(resumen, BorderLayout.NORTH);

        JPanel reporte = NutricionistaUI.tarjeta();
        reporte.setLayout(new BorderLayout(0, 12));
        JPanel filtros = new JPanel(new GridLayout(1, 4, 10, 0));
        filtros.setOpaque(false);
        agregarCampo(filtros, "Reporte", cboReporte);
        agregarCampo(filtros, "Desde (AAAA-MM-DD)", txtDesde);
        agregarCampo(filtros, "Hasta (AAAA-MM-DD)", txtHasta);
        javax.swing.JButton btnGenerar = NutricionistaUI.boton("Generar");
        JPanel boton = new JPanel(new BorderLayout());
        boton.setOpaque(false);
        boton.add(new JLabel(" "), BorderLayout.NORTH);
        boton.add(btnGenerar, BorderLayout.CENTER);
        filtros.add(boton);
        reporte.add(filtros, BorderLayout.NORTH);

        NutricionistaUI.tabla(tabla);
        reporte.add(NutricionistaUI.scrollTabla(tabla), BorderLayout.CENTER);
        reporte.add(lblCantidad, BorderLayout.SOUTH);
        btnGenerar.addActionListener(e -> cargarReporte());
        centro.add(reporte, BorderLayout.CENTER);

        pagina.add(centro, BorderLayout.CENTER);
        add(pagina, BorderLayout.CENTER);
    }

    private void eventos() {
        cboReporte.addActionListener(e -> cargarReporte());
    }

    public void refrescarDatos() {
        Map<String, Integer> r = controlador.resumenReportes();
        lblClientes.setText(String.valueOf(r.getOrDefault("Clientes en alcance", 0)));
        lblActivos.setText(String.valueOf(r.getOrDefault("Planes activos", 0)));
        lblFinalizados.setText(String.valueOf(r.getOrDefault("Planes finalizados", 0)));
        lblResultados.setText(String.valueOf(r.getOrDefault("Resultados registrados", 0)));
        lblRecomendaciones.setText(String.valueOf(r.getOrDefault("Recomendaciones", 0)));
        cargarReporte();
    }

    private void cargarReporte() {
        try {
            LocalDate desde = fechaOpcional(txtDesde.getText());
            LocalDate hasta = fechaOpcional(txtHasta.getText());
            if (desde != null && hasta != null && hasta.isBefore(desde)) {
                throw new IllegalArgumentException("La fecha hasta no puede ser anterior a la fecha desde.");
            }
            String reporte = String.valueOf(cboReporte.getSelectedItem());
            switch (reporte) {
                case "Planes nutricionales" -> cargarPlanes(desde, hasta);
                case "Indicadores registrados" -> cargarResultados(desde, hasta);
                case "Recomendaciones" -> cargarRecomendaciones(desde, hasta);
                default -> cargarClientes();
            }
        } catch (IllegalArgumentException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "GYMNOVA", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cargarClientes() {
        List<Cliente> lista = controlador.listarMisClientes("");
        DefaultTableModel m = modelo(new String[]{
            "Código", "Cédula", "Cliente", "Teléfono", "Correo", "Peso inicial", "Peso meta", "Estado"
        });
        for (Cliente c : lista) m.addRow(new Object[]{
            c.getCodigoCliente(), c.getCedula(), c.getNombreCompleto(), c.getTelefono(),
            c.getCorreo(), c.getPesoInicial(), c.getPesoMeta(), c.isEstadoCliente() ? "ACTIVO" : "INACTIVO"
        });
        tabla.setModel(m);
        lblCantidad.setText(lista.size() + " registros");
    }

    private void cargarPlanes(LocalDate desde, LocalDate hasta) {
        List<PlanNutricional> lista = controlador.listarPlanesPropios().stream()
                .filter(p -> dentro(p.getFechaInicio(), desde, hasta)).toList();
        DefaultTableModel m = modelo(new String[]{
            "Código", "Cliente", "Plan", "Inicio", "Fin", "Calorías", "Estado"
        });
        for (PlanNutricional p : lista) m.addRow(new Object[]{
            p.getCodigoPlan(), controlador.nombreCliente(p.getIdCliente()), p.getNombrePlan(),
            p.getFechaInicio(), p.getFechaFin(), p.getCaloriasObjetivo(), p.getEstadoPlan()
        });
        tabla.setModel(m);
        lblCantidad.setText(lista.size() + " registros");
    }

    private void cargarResultados(LocalDate desde, LocalDate hasta) {
        List<NutricionistaWorkspaceControlador.ResultadoReporte> lista
                = controlador.listarResultadosPropios().stream()
                        .filter(r -> dentro(r.fecha(), desde, hasta)).toList();
        DefaultTableModel m = modelo(new String[]{
            "Código cliente", "Cliente", "Fecha", "Indicador", "Valor", "Clasificación", "Fuera de rango"
        });
        for (var r : lista) m.addRow(new Object[]{
            r.codigoCliente(), r.cliente(), r.fecha(), r.indicador(), r.valor(),
            r.clasificacion(), r.fueraRango() ? "SÍ" : "NO"
        });
        tabla.setModel(m);
        lblCantidad.setText(lista.size() + " registros");
    }

    private void cargarRecomendaciones(LocalDate desde, LocalDate hasta) {
        List<NutricionistaWorkspaceControlador.RecomendacionReporte> lista
                = controlador.listarRecomendacionesPropias().stream()
                        .filter(r -> dentro(r.fecha(), desde, hasta)).toList();
        DefaultTableModel m = modelo(new String[]{
            "Código cliente", "Cliente", "Fecha", "Tipo", "Título", "Prioridad", "Estado"
        });
        for (var r : lista) m.addRow(new Object[]{
            r.codigoCliente(), r.cliente(), r.fecha(), r.tipo(), r.titulo(), r.prioridad(), r.estado()
        });
        tabla.setModel(m);
        lblCantidad.setText(lista.size() + " registros");
    }

    private boolean dentro(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        if (fecha == null) return desde == null && hasta == null;
        return (desde == null || !fecha.isBefore(desde)) && (hasta == null || !fecha.isAfter(hasta));
    }

    private LocalDate fechaOpcional(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); }
        catch (java.time.format.DateTimeParseException ex) {
            throw new IllegalArgumentException("Las fechas deben tener formato AAAA-MM-DD.");
        }
    }

    private JPanel tarjeta(String titulo, JLabel valor) {
        JPanel p = NutricionistaUI.tarjeta();
        p.setLayout(new BorderLayout(0, 6));
        valor.setHorizontalAlignment(JLabel.CENTER);
        JLabel t = NutricionistaUI.etiqueta(titulo);
        t.setHorizontalAlignment(JLabel.CENTER);
        p.add(valor, BorderLayout.CENTER);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }

    private void agregarCampo(JPanel panel, String etiqueta, java.awt.Component campo) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(NutricionistaUI.etiqueta(etiqueta), BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);
        panel.add(p);
    }

    private static JLabel valorGrande() {
        JLabel l = new JLabel("0");
        l.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));
        l.setForeground(NutricionistaUI.AZUL_OSCURO);
        return l;
    }

    private DefaultTableModel modelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }
}
