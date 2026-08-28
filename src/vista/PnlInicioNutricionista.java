package vista;

import controlador.NutricionistaWorkspaceControlador;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.Cliente;
import modelo.PlanNutricional;
import utilidades.ClienteEnAtencion;

/** Dashboard exclusivo del rol Nutricionista. */
public class PnlInicioNutricionista extends JPanel {

    private final NutricionistaWorkspaceControlador controlador
            = new NutricionistaWorkspaceControlador();

    private final JLabel lblClientes = valorGrande();
    private final JLabel lblPlanes = valorGrande();
    private final JLabel lblFinalizados = valorGrande();
    private final JLabel lblResultados = valorGrande();
    private final JLabel lblClienteActual = new JLabel();
    private final JLabel lblPlanActual = new JLabel();
    private final JTable tblPlanes = new JTable();

    public PnlInicioNutricionista() {
        construir();
        refrescarDatos();
    }

    private void construir() {
        setLayout(new BorderLayout());
        setBackground(NutricionistaUI.FONDO);

        JPanel pagina = NutricionistaUI.pagina();
        pagina.add(NutricionistaUI.encabezado(
                "Inicio nutricional",
                "Resumen de sus clientes, planes y actividad profesional."),
                BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);

        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 12, 0));
        tarjetas.setOpaque(false);
        tarjetas.add(tarjetaResumen("Mis clientes", lblClientes));
        tarjetas.add(tarjetaResumen("Planes activos", lblPlanes));
        tarjetas.add(tarjetaResumen("Planes finalizados", lblFinalizados));
        tarjetas.add(tarjetaResumen("Resultados registrados", lblResultados));
        centro.add(tarjetas, BorderLayout.NORTH);

        JPanel contenido = new JPanel(new GridLayout(1, 2, 14, 0));
        contenido.setOpaque(false);
        contenido.add(crearClienteActual());
        contenido.add(crearPlanesRecientes());
        centro.add(contenido, BorderLayout.CENTER);

        pagina.add(centro, BorderLayout.CENTER);
        add(pagina, BorderLayout.CENTER);
    }

    private JPanel tarjetaResumen(String titulo, JLabel valor) {
        JPanel p = NutricionistaUI.tarjeta();
        p.setLayout(new BorderLayout(0, 8));
        JLabel t = NutricionistaUI.etiqueta(titulo);
        t.setHorizontalAlignment(JLabel.CENTER);
        valor.setHorizontalAlignment(JLabel.CENTER);
        p.add(valor, BorderLayout.CENTER);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearClienteActual() {
        JPanel p = NutricionistaUI.tarjeta();
        p.setLayout(new BorderLayout(0, 12));
        p.add(NutricionistaUI.tituloSeccion("Cliente en atención"), BorderLayout.NORTH);

        JPanel datos = new JPanel();
        datos.setOpaque(false);
        datos.setLayout(new javax.swing.BoxLayout(datos, javax.swing.BoxLayout.Y_AXIS));
        lblClienteActual.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 17));
        lblClienteActual.setForeground(NutricionistaUI.TEXTO);
        lblPlanActual.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        datos.add(lblClienteActual);
        datos.add(javax.swing.Box.createVerticalStrut(10));
        datos.add(lblPlanActual);
        datos.add(javax.swing.Box.createVerticalStrut(14));
        JLabel ayuda = new JLabel("Seleccione un cliente desde Mis clientes para continuar su atención.");
        ayuda.setForeground(NutricionistaUI.TEXTO_SECUNDARIO);
        datos.add(ayuda);
        p.add(datos, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearPlanesRecientes() {
        JPanel p = NutricionistaUI.tarjeta();
        p.setLayout(new BorderLayout(0, 10));
        p.add(NutricionistaUI.tituloSeccion("Planes recientes"), BorderLayout.NORTH);
        NutricionistaUI.tabla(tblPlanes);
        p.add(NutricionistaUI.scrollTabla(tblPlanes), BorderLayout.CENTER);
        return p;
    }

    public void refrescarDatos() {
        try {
            NutricionistaWorkspaceControlador.ResumenDashboard r
                    = controlador.resumenDashboard();
            lblClientes.setText(String.valueOf(r.clientes()));
            lblPlanes.setText(String.valueOf(r.planesActivos()));
            lblFinalizados.setText(String.valueOf(r.planesFinalizados()));
            lblResultados.setText(String.valueOf(r.resultados()));

            Cliente cliente = ClienteEnAtencion.actual();
            if (cliente == null) {
                lblClienteActual.setText("Ningún cliente seleccionado");
                lblPlanActual.setText("Plan activo: —");
            } else {
                lblClienteActual.setText(cliente.getNombreCompleto()
                        + " · " + cliente.getCodigoCliente());
                PlanNutricional plan = controlador.planActivo(cliente.getIdPersona());
                lblPlanActual.setText(plan == null
                        ? "Plan activo: ninguno"
                        : "Plan activo: " + plan.getNombrePlan()
                        + " · " + plan.getCaloriasObjetivo() + " kcal");
            }

            List<PlanNutricional> planes = controlador.listarPlanesPropios();
            DefaultTableModel m = modelo(new String[]{
                "Código", "Cliente", "Plan", "Inicio", "Estado"
            });
            planes.stream().limit(8).forEach(plan -> m.addRow(new Object[]{
                plan.getCodigoPlan(),
                controlador.nombreCliente(plan.getIdCliente()),
                plan.getNombrePlan(),
                plan.getFechaInicio(),
                plan.getEstadoPlan()
            }));
            tblPlanes.setModel(m);
        } catch (RuntimeException ex) {
            lblClienteActual.setText("No se pudo cargar el dashboard");
            lblPlanActual.setText(ex.getMessage());
        }
    }

    private static JLabel valorGrande() {
        JLabel l = new JLabel("0");
        l.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 28));
        l.setForeground(NutricionistaUI.AZUL_OSCURO);
        return l;
    }

    private DefaultTableModel modelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }
}
