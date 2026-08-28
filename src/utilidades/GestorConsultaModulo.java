package utilidades;

import controlador.AutorizacionControlador;
import controlador.AlcanceRolControlador;
import controlador.ConsultaModuloControlador;
import controlador.EditorRegistroControlador;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import vista.PnlEditorRegistro;

/**
 * Conecta la tabla común de los módulos con los controladores CRUD existentes.
 * No crea componentes visuales; todos se conservan en los formularios Design.
 */
public class GestorConsultaModulo {

    private final JPanel padre;
    private final JComboBox<String> selector;
    private final JTable tabla;
    private final JTextField buscador;
    private final JLabel cantidad;
    private final String permisoModulo;
    private final Map<String, String> controladores;
    private final ConsultaModuloControlador consultaControlador;
    private final AutorizacionControlador autorizacionControlador;
    private final AlcanceRolControlador alcanceRolControlador;
    private List<?> registros;

    public GestorConsultaModulo(
            JPanel padre,
            JComboBox<String> selector,
            JTable tabla,
            JTextField buscador,
            JLabel cantidad,
            String permisoModulo,
            Map<String, String> controladores
    ) {
        this.padre = padre;
        this.selector = selector;
        this.tabla = tabla;
        this.buscador = buscador;
        this.cantidad = cantidad;
        this.permisoModulo = permisoModulo;
        this.controladores = new LinkedHashMap<>(controladores);
        filtrarOpcionesPorRol();
        this.consultaControlador = new ConsultaModuloControlador();
        this.autorizacionControlador = new AutorizacionControlador();
        this.alcanceRolControlador = new AlcanceRolControlador();
        this.registros = new ArrayList<>();
        AyudasContextuales.aplicar(padre);
    }

    private void filtrarOpcionesPorRol() {
        if (!SesionUsuario.haySesionActiva()) {
            this.controladores.clear();
            selector.setModel(new javax.swing.DefaultComboBoxModel<>());
            return;
        }
        String rol = SesionUsuario.getUsuarioActual().getNombreRol();
        rol = rol == null ? "" : rol.trim().toUpperCase();
        if ("ADMINISTRADOR".equals(rol)) {
            return;
        }
        java.util.Set<String> permitidos = switch (rol + "|" + permisoModulo) {
            case "RECEPCIONISTA|MEMBRESIAS" -> java.util.Set.of(
                    "Membresias", "Congelaciones");
            case "RECEPCIONISTA|ACCESO" -> java.util.Set.of(
                    "Reservas", "Asistencias", "Clases grupales");
            case "RECEPCIONISTA|FINANZAS" -> java.util.Set.of(
                    "Pagos", "Facturas", "Detalle de factura",
                    "Comprobantes");
            case "RECEPCIONISTA|REPORTES" -> java.util.Set.of(
                    "Clientes", "Membresias", "Accesos", "Finanzas");
            case "ENTRENADOR|RUTINAS" -> java.util.Set.of(
                    "Plantillas de rutina", "Asignar rutina a cliente",
                    "Catalogo de ejercicios",
                    "Calendario semanal", "Registrar progreso del cliente",
                    "Ejercicios que componen la rutina");
            case "ENTRENADOR|SALUD" -> java.util.Set.of(
                    "Evaluar cliente", "Registrar peso y medidas",
                    "Resultados de indicadores", "Recomendaciones profesionales");
            case "ENTRENADOR|REPORTES" -> java.util.Set.of(
                    "Clientes", "Rutinas", "Salud");
            case "NUTRICIONISTA|NUTRICION" -> java.util.Set.of(
                    "Planes asignados a clientes", "Catalogo de alimentos",
                    "Comidas y porciones del plan");
            case "NUTRICIONISTA|SALUD" -> java.util.Set.of(
                    "Registrar peso y medidas", "Resultados de indicadores",
                    "Recomendaciones profesionales");
            case "NUTRICIONISTA|REPORTES" -> java.util.Set.of(
                    "Clientes", "Salud", "Nutricion");
            case "CLIENTE|ACCESO" -> java.util.Set.of("Reservas");
            case "CLIENTE|RUTINAS" -> java.util.Set.of(
                    "Registrar progreso del cliente");
            case "CLIENTE|REPORTES" -> java.util.Set.of(
                    "Membresias", "Accesos", "Rutinas", "Salud",
                    "Nutricion", "Finanzas");
            default -> java.util.Set.of();
        };
        this.controladores.entrySet().removeIf(
                entrada -> !permitidos.contains(entrada.getKey()));
        selector.setModel(new javax.swing.DefaultComboBoxModel<>(
                this.controladores.keySet().toArray(String[]::new)));
    }

    public static Map<String, String> controladoresPara(String modulo) {
        Map<String, String> mapa = new LinkedHashMap<>();

        switch (modulo.toUpperCase()) {
            case "PERSONAL" -> {
                mapa.put("Empleados", "EmpleadoControlador");
                mapa.put("Entrenadores", "EntrenadorControlador");
                mapa.put("Nutricionistas", "NutricionistaControlador");
            }
            case "MEMBRESIAS" -> {
                mapa.put("Membresias", "MembresiaControlador");
                mapa.put("Planes de membresia", "TipoMembresiaControlador");
                mapa.put("Congelaciones", "CongelacionControlador");
            }
            case "ACCESO" -> {
                mapa.put("Reservas", "ReservaControlador");
                mapa.put("Asistencias", "AsistenciaControlador");
                mapa.put("Clases grupales", "ClaseGrupalControlador");
            }
            case "RUTINAS" -> {
                mapa.put("Plantillas de rutina", "RutinaControlador");
                mapa.put("Asignar rutina a cliente", "AsignacionRutinaControlador");
                mapa.put("Catalogo de ejercicios", "EjercicioControlador");
                mapa.put("Calendario semanal", "RutinaDiaEntrenamientoControlador");
                mapa.put("Registrar progreso del cliente", "ProgresoRutinaControlador");
                mapa.put("Ejercicios que componen la rutina", "ContieneEjercicioControlador");
            }
            case "SALUD" -> {
                mapa.put("Evaluar cliente", "EvaluacionFisicaControlador");
                mapa.put("Registrar peso y medidas", "MedicionCorporalControlador");
                mapa.put("Catalogo de indicadores", "IndicadorSaludControlador");
                mapa.put("Resultados de indicadores", "ResultadoIndicadorControlador");
                mapa.put("Recomendaciones profesionales", "RecomendacionControlador");
            }
            case "NUTRICION" -> {
                mapa.put("Planes asignados a clientes", "PlanNutricionalControlador");
                mapa.put("Catalogo de alimentos", "AlimentoControlador");
                mapa.put("Comidas y porciones del plan", "IncluyeAlimentoControlador");
                mapa.put("Nutricionistas habilitados", "NutricionistaControlador");
            }
            case "FINANZAS" -> {
                mapa.put("Pagos", "PagoControlador");
                mapa.put("Facturas", "FacturaControlador");
                mapa.put("Detalle de factura", "DetalleFacturaControlador");
                mapa.put("Metodos de pago", "MetodoPagoControlador");
                mapa.put("Comprobantes", "ComprobanteControlador");
            }
            case "REPORTES" -> {
                mapa.put("Clientes", "ClienteControlador");
                mapa.put("Membresias", "MembresiaControlador");
                mapa.put("Accesos", "AsistenciaControlador");
                mapa.put("Rutinas", "AsignacionRutinaControlador");
                mapa.put("Salud", "EvaluacionFisicaControlador");
                mapa.put("Nutricion", "PlanNutricionalControlador");
                mapa.put("Finanzas", "PagoControlador");
                mapa.put("Personal", "EmpleadoControlador");
                mapa.put("Auditoria", "BitacoraControlador");
            }
            default -> {
                // El módulo no utiliza el gestor de consultas CRUD.
            }
        }

        return mapa;
    }

    public void cargar() {
        String controlador = controladorActual();
        if (controlador == null) {
            return;
        }

        String rol = SesionUsuario.haySesionActiva()
                ? SesionUsuario.getUsuarioActual().getNombreRol()
                : "";
        Long idPersona = SesionUsuario.haySesionActiva()
                ? SesionUsuario.getUsuarioActual().getIdPersona()
                : null;

        registros = consultaControlador.listarParaSesion(
                controlador,
                buscador.getText(),
                rol,
                idPersona
        );

        String[] columnas = registros.isEmpty()
                ? new String[]{"Información"}
                : consultaControlador.obtenerColumnas(registros.get(0));
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        for (Object registro : registros) {
            modelo.addRow(consultaControlador.obtenerFila(registro));
        }

        tabla.setModel(modelo);
        cantidad.setText(
                registros.size()
                + (registros.size() == 1
                        ? " registro encontrado"
                        : " registros encontrados")
        );

        if (!consultaControlador.getMensaje().isBlank()
                && registros.isEmpty()) {
            cantidad.setText(consultaControlador.getMensaje());
        }
    }

    public Object getSeleccionado() {
        int filaVista = tabla.getSelectedRow();
        if (filaVista < 0) {
            return null;
        }
        int filaModelo = tabla.convertRowIndexToModel(filaVista);
        return filaModelo < registros.size()
                ? registros.get(filaModelo)
                : null;
    }

    public String getControladorActual() {
        return controladorActual();
    }

    public boolean nuevo() {
        if (!verificarPermiso("CREAR")) {
            return false;
        }
        return abrirEditor(false);
    }

    public boolean modificar() {
        if (!verificarPermiso("MODIFICAR")) {
            return false;
        }
        if (getSeleccionado() == null) {
            mostrarAdvertencia("Seleccione un registro de la tabla.");
            return false;
        }
        if (!seleccionDentroDelAlcance()) {
            return false;
        }
        return abrirEditor(true);
    }

    public boolean desactivar() {
        if (!verificarPermiso("DESACTIVAR")) {
            return false;
        }
        if (!seleccionDentroDelAlcance()) {
            return false;
        }
        boolean correcto = consultaControlador.desactivar(
                controladorActual(),
                getSeleccionado()
        );
        informarResultado(correcto, "Registro desactivado correctamente.");
        return correcto;
    }

    public boolean eliminar() {
        if (!esAdministrador() || !verificarPermiso("ELIMINAR")) {
            mostrarAdvertencia(
                    "Solo el administrador puede eliminar definitivamente."
            );
            return false;
        }
        if (JOptionPane.showConfirmDialog(
                padre,
                "¿Eliminar definitivamente el registro seleccionado?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        ) != JOptionPane.YES_OPTION) {
            return false;
        }

        boolean correcto = consultaControlador.eliminarDefinitivamente(
                controladorActual(),
                getSeleccionado()
        );
        informarResultado(correcto, "Registro eliminado correctamente.");
        return correcto;
    }

    public boolean verificarPermiso(String accion) {
        if (!accionPermitidaPorRol(accion)) {
            mostrarAdvertencia(
                    "El rol actual no puede realizar esta operacion en este submodulo."
            );
            return false;
        }
        if (autorizacionControlador.tienePermiso(permisoModulo, accion)) {
            return true;
        }
        mostrarAdvertencia(autorizacionControlador.getMensaje());
        return false;
    }

    private boolean accionPermitidaPorRol(String accion) {
        if (!SesionUsuario.haySesionActiva()) {
            return false;
        }
        String rol = SesionUsuario.getUsuarioActual().getNombreRol();
        rol = rol == null ? "" : rol.trim().toUpperCase();
        String actual = controladorActual();

        if (!java.util.Set.of("ADMINISTRADOR", "RECEPCIONISTA",
                "ENTRENADOR", "NUTRICIONISTA", "CLIENTE").contains(rol)) {
            return false;
        }

        if ("ADMINISTRADOR".equals(rol) || "VER".equalsIgnoreCase(accion)) {
            return true;
        }
        if ("ELIMINAR".equalsIgnoreCase(accion)) {
            return false;
        }
        if ("CLIENTE".equals(rol)) {
            return java.util.Set.of(
                    "ReservaControlador", "ProgresoRutinaControlador"
            ).contains(actual);
        }
        if ("RECEPCIONISTA".equals(rol)) {
            return java.util.Set.of(
                    "MembresiaControlador", "CongelacionControlador",
                    "ReservaControlador", "AsistenciaControlador",
                    "PagoControlador", "FacturaControlador",
                    "DetalleFacturaControlador", "ComprobanteControlador",
                    "AplicaDescuentoControlador"
            ).contains(actual);
        }
        if ("ENTRENADOR".equals(rol)) {
            return java.util.Set.of(
                    "RutinaControlador", "AsignacionRutinaControlador",
                    "ContieneEjercicioControlador",
                    "RutinaDiaEntrenamientoControlador",
                    "EjercicioControlador", "GrupoMuscularControlador",
                    "TrabajaControlador", "RequiereEquipoControlador",
                    "EjercicioRecursoMultimediaControlador",
                    "ProgresoRutinaControlador",
                    "EvaluacionFisicaControlador",
                    "MedicionCorporalControlador",
                    "ResultadoIndicadorControlador",
                    "RecomendacionControlador"
            ).contains(actual);
        }
        if ("NUTRICIONISTA".equals(rol)) {
            return java.util.Set.of(
                    "PlanNutricionalControlador",
                    "IncluyeAlimentoControlador",
                    "AlimentoControlador",
                    "AlimentoAlergenoControlador",
                    "MedicionCorporalControlador",
                    "ResultadoIndicadorControlador",
                    "RecomendacionControlador"
            ).contains(actual);
        }
        return false;
    }

    public void limpiarSeleccion() {
        tabla.clearSelection();
    }

    private boolean seleccionDentroDelAlcance() {
        if (!SesionUsuario.haySesionActiva()) {
            mostrarAdvertencia("No existe una sesion activa.");
            return false;
        }
        boolean permitido = alcanceRolControlador.puedeModificar(
                getSeleccionado(),
                SesionUsuario.getUsuarioActual().getNombreRol(),
                SesionUsuario.getUsuarioActual().getIdPersona());
        if (!permitido) {
            mostrarAdvertencia(alcanceRolControlador.getMensaje());
        }
        return permitido;
    }

    private boolean abrirEditor(boolean modificacion) {
        EditorRegistroControlador editorControlador
                = new EditorRegistroControlador();
        PnlEditorRegistro editor = new PnlEditorRegistro();

        if (!editorControlador.preparar(
                controladorActual(),
                modificacion ? getSeleccionado() : null,
                modificacion,
                editor
        )) {
            mostrarAdvertencia(editorControlador.getMensaje());
            return false;
        }

        int opcion = JOptionPane.showConfirmDialog(
                padre,
                editor,
                modificacion ? "Modificar registro" : "Nuevo registro",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (opcion != JOptionPane.OK_OPTION) {
            return false;
        }

        boolean correcto = editorControlador.guardar(editor);
        if (correcto) {
            JOptionPane.showMessageDialog(
                    padre,
                    editorControlador.getMensaje(),
                    "GYMNOVA",
                    JOptionPane.INFORMATION_MESSAGE
            );
            cargar();
        } else {
            mostrarAdvertencia(editorControlador.getMensaje());
        }
        return correcto;
    }

    private String controladorActual() {
        Object item = selector.getSelectedItem();
        return item == null ? null : controladores.get(item.toString());
    }

    private void informarResultado(boolean correcto, String mensajeCorrecto) {
        if (correcto) {
            JOptionPane.showMessageDialog(
                    padre,
                    mensajeCorrecto,
                    "GYMNOVA",
                    JOptionPane.INFORMATION_MESSAGE
            );
            cargar();
        } else {
            mostrarAdvertencia(consultaControlador.getMensaje());
        }
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(
                padre,
                mensaje == null || mensaje.isBlank()
                        ? "No fue posible completar la operación."
                        : mensaje,
                "Atención",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private boolean esAdministrador() {
        return SesionUsuario.haySesionActiva()
                && "Administrador".equalsIgnoreCase(
                        SesionUsuario.getUsuarioActual().getNombreRol()
                );
    }
}
