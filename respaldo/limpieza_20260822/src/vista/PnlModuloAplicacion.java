package vista;

import controlador.DashboardControlador;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import modelo.Usuario;
import utilidades.SesionUsuario;

/**
 * Portada operativa de cada modulo. Los componentes visuales se administran
 * desde NetBeans Design; el codigo solo selecciona contenido y datos del rol.
 */
public class PnlModuloAplicacion extends javax.swing.JPanel {

    private final String modulo;
    private final Runnable accionPrincipal;
    private final DashboardControlador dashboardControlador;
    private java.awt.Image imagenFondo;
    private PnlContenidoEspecializado contenidoEspecializado;

    public PnlModuloAplicacion(String modulo, Runnable accionPrincipal) {
        this.modulo = normalizar(modulo);
        this.accionPrincipal = accionPrincipal;
        initComponents();
        dashboardControlador = new DashboardControlador();
        configurarExperiencia();
        configurarImagenHero();
        configurarContenidoEspecializado();
        cargarDatos();
        btnAccionPrincipal.setVisible(accionPrincipal != null);
        btnAccionPrincipal.setToolTipText(accionPrincipal == null
                ? "Este modulo es de consulta para el rol actual."
                : "Abre las operaciones permitidas para el rol actual.");
        if (accionPrincipal != null) {
            btnAccionPrincipal.addActionListener(evento -> ejecutarAccion());
        }
    }

    public final void refrescarDatos() {
        cargarDatos();
        configurarContenidoEspecializado();
    }

    private void configurarExperiencia() {
        String rol = rolActual();
        String clave = rol + "|" + modulo;

        switch (clave) {
            case "ADMINISTRADOR|CLIENTES" -> configurar(
                    "Clientes", "Altas, estado y seguimiento general",
                    "Activos", "Nuevos", "En seguimiento", "Alertas",
                    "Resumen de clientes", "Acciones administrativas",
                    "Clientes registrados recientemente", "Perfiles pendientes de completar",
                    "Clientes activos e inactivos", "Alertas GYMNOVA Pulse",
                    "Asignaciones de entrenador", "Planes nutricionales relacionados",
                    "Informacion actualizada", "Consulta primero; modifica solo si corresponde",
                    "Los historiales se conservan", "Administrar registros");
            case "ADMINISTRADOR|MEMBRESIAS" -> configurar(
                    "Membresias", "Planes, vigencias, promociones y congelaciones",
                    "Activas", "Por vencer", "Promociones", "Ingresos",
                    "Estado de las membresias", "Catalogo comercial",
                    "Plan Basica", "Plan Premium", "Plan Elite",
                    "Renovaciones y vencimientos", "Congelaciones solicitadas",
                    "Descuentos y promociones separados del plan",
                    "Tres planes disponibles", "No crees planes duplicados",
                    "Los precios y beneficios se administran por plan", "Administrar membresias");
            case "ADMINISTRADOR|ACCESO" -> configurar(
                    "Acceso", "Clases, reservas y asistencias del gimnasio",
                    "Ingresos hoy", "Reservas", "Clases", "Incidencias",
                    "Operacion de acceso", "Control diario",
                    "Reservas confirmadas", "Asistencias registradas",
                    "Cupos de clases grupales", "Horarios disponibles",
                    "Accesos rechazados", "Historial de ingreso",
                    "Control activo", "Verifica membresia y reserva",
                    "La asistencia es un registro historico", "Administrar acceso");
            case "ADMINISTRADOR|RUTINAS" -> configurar(
                    "Rutinas", "Plantillas, asignaciones y progreso",
                    "Plantillas", "Asignadas", "Ejercicios", "Progreso",
                    "Planificacion de entrenamiento", "Flujo correcto",
                    "Crear una plantilla de rutina", "Agregar ejercicios a la plantilla",
                    "Asignar la rutina a un cliente", "Organizar calendario semanal",
                    "Registrar progreso sin cambiar el peso inicial", "Consultar recursos y equipos",
                    "Gestion separada", "Usa el progreso para mediciones actuales",
                    "El historial de sesiones no se reemplaza", "Administrar rutinas");
            case "ADMINISTRADOR|SALUD" -> configurar(
                    "Salud", "Evaluaciones, mediciones y objetivos",
                    "Evaluaciones", "Mediciones", "Objetivos", "Alertas",
                    "Seguimiento fisico", "Flujo profesional",
                    "Evaluar al cliente", "Registrar peso y medidas actuales",
                    "Comparar con evaluaciones anteriores", "Asignar objetivos fitness",
                    "Registrar recomendaciones", "Consultar indicadores de salud",
                    "Historial protegido", "No modifiques el peso inicial del cliente",
                    "Cada medicion nueva construye la evolucion", "Administrar salud");
            case "ADMINISTRADOR|NUTRICION" -> configurar(
                    "Nutricion", "Planes, comidas, alimentos y alertas",
                    "Planes", "Clientes", "Alimentos", "Revisiones",
                    "Seguimiento nutricional", "Flujo profesional",
                    "Asignar un plan al cliente", "Definir comidas y porciones",
                    "Consultar alimentos", "Controlar alergenos",
                    "Registrar revisiones", "Conservar planes anteriores",
                    "Gestion disponible", "Un plan pertenece a un cliente y nutricionista",
                    "Las alergias deben revisarse antes de asignar alimentos", "Administrar nutricion");
            case "ADMINISTRADOR|FINANZAS" -> configurar(
                    "Finanzas", "Facturas, pagos, comprobantes y promociones",
                    "Facturado", "Cobrado", "Pendiente", "Comprobantes",
                    "Resumen financiero", "Flujo de cobro",
                    "Crear factura con sus conceptos", "Registrar pago de la factura",
                    "Generar comprobante", "Aplicar descuento autorizado",
                    "Exportar factura a PDF", "Consultar historial de pagos",
                    "Datos contables relacionados", "No registres pagos sin factura",
                    "Los documentos emitidos se conservan", "Administrar finanzas");
            case "ADMINISTRADOR|REPORTES" -> configurar(
                    "Reportes", "Indicadores reales consultados desde PostgreSQL",
                    "Clientes", "Ingresos", "Asistencias", "Periodo",
                    "Reportes disponibles", "Uso de resultados",
                    "Seleccionar entidad y periodo", "Generar vista previa",
                    "Exportar el reporte", "Imprimir el documento",
                    "Comparar indicadores", "Conservar evidencia de la consulta",
                    "Informacion consolidada", "Los resultados cambian con los datos",
                    "Selecciona filtros antes de generar", "Abrir generador de reportes");
            case "ADMINISTRADOR|CONFIGURACION" -> configurar(
                    "Mi cuenta", "Alias, avatar y seguridad de acceso",
                    "Alias", "Avatar", "Contrasena", "Rol",
                    "Configuracion personal", "Opciones permitidas",
                    "Cambiar alias de acceso", "Elegir avatar PNG o JPG",
                    "Cambiar contrasena segura", "Consultar rol asignado",
                    "Mantener la cuenta activa", "Cerrar sesiones de forma segura",
                    "Cuenta protegida", "El rol no se cambia desde esta pantalla",
                    "Los cambios se aplican al usuario autenticado", "Administrar mi cuenta");
            case "CLIENTE|CLIENTES" -> configurar(
                    "Mi perfil", "Datos personales y estado de mi cuenta",
                    "Datos personales", "Estado", "Actualizaciones", "Cuenta",
                    "Mi informacion", "Resumen de mi perfil",
                    "Cedula y nombres registrados", "Correo y telefono de contacto",
                    "Objetivo fitness actual", "Fotografia y datos personales",
                    "Estado de cliente", "Preferencias de comunicacion",
                    "Perfil activo", "Puedes actualizar tus datos de contacto",
                    "La cedula permanece protegida", "Editar mi perfil");
            case "CLIENTE|MEMBRESIAS" -> configurar(
                    "Mi membresia", "Plan contratado, vigencia y beneficios",
                    "Planes activos", "Dias restantes", "Beneficios", "Estado",
                    "Estado de membresia", "Beneficios incluidos",
                    "Plan y precio contratado", "Fecha de inicio y vencimiento",
                    "Accesos disponibles", "Congelaciones solicitadas",
                    "Clases grupales incluidas", "Descuentos del plan",
                    "Membresia verificada", "Renueva antes del vencimiento",
                    "Consulta pagos y comprobantes", "Ver detalle completo");
            case "CLIENTE|ACCESO" -> configurar(
                    "Mis reservas", "Agenda de clases y accesos al gimnasio",
                    "Reservas activas", "Clases proximas", "Asistencias", "Acceso",
                    "Proximas clases reservadas", "Calendario y acceso rapido",
                    "Hoy - Acceso general al gimnasio", "Proxima clase grupal reservada",
                    "Horario y sala asignada", "Estado de confirmacion",
                    "Historial de asistencias", "Codigo de acceso personal",
                    "Agenda sincronizada", "Llega 10 minutos antes de la clase",
                    "Puedes cancelar segun las reglas del plan", "Gestionar reservas");
            case "CLIENTE|RUTINAS" -> configurar(
                    "Mi rutina", "Plan semanal, ejercicios y avance de hoy",
                    "Rutinas activas", "Ejercicios hoy", "Completados", "Progreso",
                    "Ejercicios de hoy", "Plan semanal y rendimiento",
                    "Press militar - 4 series - 8 a 10 repeticiones",
                    "Elevaciones laterales - 3 series - 12 repeticiones",
                    "Plancha abdominal - 3 series - 45 segundos",
                    "Cardio final - 15 minutos", "Descanso sugerido: 60 segundos",
                    "Peso y repeticiones se registran al finalizar",
                    "Rutina preparada", "Completa cada ejercicio en orden",
                    "Tu entrenador revisara el progreso", "Abrir rutina completa");
            case "CLIENTE|SALUD" -> configurar(
                    "Mi progreso", "Evolucion fisica, peso y objetivos personales",
                    "Evaluaciones", "Peso actual", "Meta", "Avance",
                    "Evolucion de mis mediciones", "Objetivos y recomendaciones",
                    "Ultima evaluacion fisica registrada", "Peso corporal y porcentaje de grasa",
                    "Medidas corporales comparadas", "Indicadores de salud",
                    "Objetivo principal del cliente", "Recomendaciones profesionales",
                    "Progreso actualizado", "Mantener constancia semanal",
                    "Los resultados dependen de evaluaciones reales", "Ver todo mi progreso");
            case "CLIENTE|NUTRICION" -> configurar(
                    "Mi nutricion", "Plan alimenticio, comidas y recomendaciones",
                    "Planes activos", "Comidas", "Calorias", "Cumplimiento",
                    "Plan alimenticio de hoy", "Macronutrientes y recomendaciones",
                    "Desayuno - alimentos y porciones", "Almuerzo - proteina y acompanamiento",
                    "Merienda - opcion saludable", "Cena - alimentos planificados",
                    "Agua y suplementacion recomendada", "Alergenos registrados",
                    "Plan nutricional activo", "Respeta las porciones indicadas",
                    "Consulta cambios con tu nutricionista", "Abrir plan alimenticio");
            case "CLIENTE|REPORTES" -> configurar(
                    "Mi historial", "Actividad, asistencias y resultados acumulados",
                    "Asistencias", "Rutinas", "Evaluaciones", "Periodo",
                    "Historial reciente", "Resumen de rendimiento",
                    "Asistencias registradas este mes", "Rutinas completadas",
                    "Cambios de peso y mediciones", "Reservas y clases tomadas",
                    "Pagos y comprobantes", "Planes nutricionales anteriores",
                    "Informacion consolidada", "Selecciona el periodo que deseas revisar",
                    "Los registros historicos no se eliminan", "Consultar historial");
            case "ENTRENADOR|CLIENTES" -> configurar(
                    "Mis clientes", "Personas asignadas y objetivos de entrenamiento",
                    "Asignados", "Con rutina", "Pendientes", "Seguimiento",
                    "Clientes que requieren atencion", "Resumen de objetivos",
                    "Clientes con sesion programada hoy", "Rutinas proximas a finalizar",
                    "Evaluaciones pendientes", "Progresos sin actualizar",
                    "Objetivos de fuerza y masa muscular", "Objetivos de perdida de grasa",
                    "Seguimiento activo", "Revisa cada cliente antes de entrenar",
                    "Solo aparecen clientes asignados", "Ver clientes asignados");
            case "ENTRENADOR|RUTINAS" -> configurar(
                    "Planificador de rutinas", "Crea semanas, dias y ejercicios",
                    "Rutinas activas", "Ejercicios", "Asignaciones", "Semana",
                    "Agenda de entrenamiento", "Constructor de rutina",
                    "Lunes - tren superior", "Martes - piernas y gluteos",
                    "Miercoles - recuperacion activa", "Jueves - fuerza y abdomen",
                    "Viernes - cardio y resistencia", "Fin de semana - descanso",
                    "Planificacion disponible", "Define series, repeticiones y descanso",
                    "Asigna la rutina al cliente correcto", "Administrar rutinas");
            case "ENTRENADOR|SALUD" -> configurar(
                    "Evaluaciones y progreso", "Mediciones, indicadores y recomendaciones",
                    "Evaluaciones", "Pendientes", "Mediciones", "Alertas",
                    "Proximas evaluaciones", "Progreso de clientes",
                    "Evaluacion fisica general", "Composicion corporal",
                    "Medicion de fuerza y resistencia", "Comparacion con evaluacion anterior",
                    "Indicadores fuera del objetivo", "Recomendaciones registradas",
                    "Seguimiento profesional", "Registra mediciones verificadas",
                    "No alteres evaluaciones historicas", "Abrir evaluaciones");
            case "NUTRICIONISTA|CLIENTES" -> configurar(
                    "Clientes nutricionales", "Planes asignados, objetivos y alertas",
                    "Asignados", "Plan activo", "Revisiones", "Alertas",
                    "Clientes que requieren revision", "Objetivos nutricionales",
                    "Planes proximos a finalizar", "Clientes sin seguimiento reciente",
                    "Objetivos de definicion muscular", "Objetivos de perdida de grasa",
                    "Alergias e intolerancias registradas", "Indicadores de salud relevantes",
                    "Seguimiento activo", "Revisa alertas antes de modificar un plan",
                    "Solo aparecen tus clientes asignados", "Ver mis clientes");
            case "NUTRICIONISTA|NUTRICION" -> configurar(
                    "Planes nutricionales", "Constructor de comidas, alimentos y porciones",
                    "Planes activos", "Clientes", "Revisiones", "Alertas",
                    "Planes recientes", "Composicion y seguridad alimentaria",
                    "Plan hipercalorico - alta proteina", "Plan hipocalorico - definicion",
                    "Plan balanceado de mantenimiento", "Alimentos y porciones incluidos",
                    "Alergenos e intolerancias", "Calorias y macronutrientes diarios",
                    "Planificador disponible", "Verifica fechas y objetivo del cliente",
                    "No incluyas alimentos con alergias activas", "Administrar planes");
            case "NUTRICIONISTA|SALUD" -> configurar(
                    "Seguimiento de salud", "Indicadores que afectan el plan nutricional",
                    "Indicadores", "Alertas", "Revisiones", "Prioridad",
                    "Alertas nutricionales", "Mediciones relevantes",
                    "Nivel de grasa corporal", "Peso y objetivo del cliente",
                    "Indicadores metabolicos registrados", "Recomendaciones activas",
                    "Alergias e intolerancias", "Fecha de la proxima revision",
                    "Revision requerida", "Coordina con entrenador cuando corresponda",
                    "Registra recomendaciones verificables", "Abrir seguimiento");
            case "RECEPCIONISTA|ACCESO" -> configurar(
                    "Control de acceso", "Reservas, asistencias y clases del dia",
                    "Reservas hoy", "Ingresos", "Clases", "Incidencias",
                    "Agenda de accesos", "Estado operativo",
                    "Validar membresia del cliente", "Confirmar reserva activa",
                    "Registrar hora de ingreso", "Controlar cupo de clase",
                    "Registrar salida o novedad", "Consultar historial de asistencia",
                    "Control habilitado", "Verifica identidad antes del acceso",
                    "No permitas membresias vencidas", "Abrir control de acceso");
            case "RECEPCIONISTA|MEMBRESIAS" -> configurar(
                    "Membresias y renovaciones", "Planes, vencimientos y congelaciones",
                    "Activas", "Por vencer", "Solicitudes", "Renovaciones",
                    "Membresias que requieren atencion", "Planes disponibles",
                    "Vencimientos de los proximos dias", "Renovaciones pendientes",
                    "Solicitudes de congelacion", "Cambios de tipo de membresia",
                    "Beneficios incluidos por plan", "Estado de pago relacionado",
                    "Gestion disponible", "Confirma fechas antes de renovar",
                    "Conserva el historial del cliente", "Administrar membresias");
            case "RECEPCIONISTA|FINANZAS" -> configurar(
                    "Caja y cobros", "Facturas, pagos y comprobantes de hoy",
                    "Cobrado hoy", "Pendiente", "Facturas", "Caja",
                    "Movimientos recientes", "Cierre operativo",
                    "Cobro de membresia", "Pago confirmado y metodo utilizado",
                    "Factura pendiente de pago", "Comprobante generado",
                    "Descuento autorizado", "Ingreso que requiere verificacion",
                    "Caja abierta", "Comprueba monto y referencia bancaria",
                    "Anulaciones requieren autorizacion", "Abrir caja y facturas");
            case "RECEPCIONISTA|CLIENTES" -> configurar(
                    "Atencion de clientes", "Registro, consulta y solicitudes en recepcion",
                    "Atendidos hoy", "Nuevos", "Solicitudes", "Turno",
                    "Cola de atencion", "Ficha rapida del cliente",
                    "Registro de una nueva persona", "Activacion del perfil de cliente",
                    "Consulta de membresia y vencimiento", "Actualizacion de contacto",
                    "Reserva solicitada en recepcion", "Entrega de comprobante",
                    "Recepcion disponible", "Verifica cedula y datos de contacto",
                    "Deriva solicitudes al area responsable", "Abrir ficha de clientes");
            case "RECEPCIONISTA|REPORTES" -> configurar(
                    "Resumen de recepcion", "Accesos, reservas y cobros del turno",
                    "Ingresos", "Reservas", "Cobros", "Incidencias",
                    "Actividad del turno", "Cierre y novedades",
                    "Ingresos registrados por hora", "Reservas confirmadas y canceladas",
                    "Membresias renovadas", "Pagos recibidos en caja",
                    "Incidencias de acceso", "Pendientes para el siguiente turno",
                    "Reporte preparado", "Revisa las novedades antes del cierre",
                    "La informacion historica permanece protegida", "Generar reporte del turno");
            case "ENTRENADOR|REPORTES" -> configurar(
                    "Rendimiento de mis clientes", "Cumplimiento, progreso y evaluaciones",
                    "Clientes", "Rutinas", "Completadas", "Promedio",
                    "Rendimiento semanal", "Indicadores de entrenamiento",
                    "Rutinas completadas esta semana", "Ejercicios con mayor avance",
                    "Clientes que requieren seguimiento", "Cambios de peso y medidas",
                    "Sesiones realizadas", "Evaluaciones pendientes",
                    "Reporte actualizado", "Compara con la semana anterior",
                    "Utiliza resultados reales de las sesiones", "Ver reporte de rendimiento");
            case "NUTRICIONISTA|REPORTES" -> configurar(
                    "Resultados nutricionales", "Cumplimiento, revisiones y alertas",
                    "Clientes", "Planes", "Revisiones", "Cumplimiento",
                    "Evolucion nutricional", "Indicadores del periodo",
                    "Planes activos por objetivo", "Revisiones realizadas",
                    "Clientes con mejora registrada", "Alertas atendidas",
                    "Cambios de peso relacionados", "Planes proximos a finalizar",
                    "Reporte actualizado", "Filtra por objetivo y periodo",
                    "Respeta la confidencialidad del cliente", "Ver reporte nutricional");
            case "CLIENTE|CONFIGURACION" -> configurar(
                    "Mi cuenta", "Preferencias, seguridad y datos de acceso",
                    "Perfil", "Sesion", "Avisos", "Seguridad",
                    "Preferencias de mi cuenta", "Proteccion de datos",
                    "Cambiar datos de contacto", "Actualizar fotografia de perfil",
                    "Configurar notificaciones", "Revisar sesiones recientes",
                    "Cambiar contrasena de acceso", "Consultar privacidad de datos",
                    "Cuenta protegida", "No compartas tus credenciales",
                    "La cedula y el historial no se modifican", "Administrar mi cuenta");
            case "ENTRENADOR|CONFIGURACION", "NUTRICIONISTA|CONFIGURACION",
                    "RECEPCIONISTA|CONFIGURACION" -> configurar(
                    "Mi cuenta profesional", "Perfil, notificaciones y seguridad",
                    "Perfil", "Turno", "Avisos", "Seguridad",
                    "Preferencias profesionales", "Cuenta y acceso",
                    "Datos de contacto registrados", "Notificaciones de tareas",
                    "Vista predeterminada del panel", "Sesiones recientes",
                    "Cambio seguro de contrasena", "Estado de la cuenta",
                    "Cuenta protegida", "No compartas tus credenciales",
                    "Los permisos dependen del rol asignado", "Administrar mi cuenta");
            default -> configurar(
                    tituloModulo(), "Panel de trabajo adaptado al rol " + rolActual(),
                    "Principal", "Pendientes", "Actividad", "Estado",
                    "Actividad reciente", "Resumen del modulo",
                    "Registros recientes del modulo", "Tareas que requieren atencion",
                    "Acciones disponibles para el rol", "Informacion relacionada",
                    "Seguimiento del proceso", "Historial conservado",
                    "Modulo disponible", "Utiliza las acciones segun tus permisos",
                    "Los datos se consultan desde PostgreSQL", "Abrir gestion");
        }
    }

    private void configurar(
            String titulo, String subtitulo,
            String m1, String m2, String m3, String m4,
            String tituloPrincipal, String tituloLateral,
            String f1, String f2, String f3, String f4, String f5, String f6,
            String d1, String d2, String d3, String accion
    ) {
        lblTitulo.setText(titulo);
        lblSubtitulo.setText(subtitulo);
        lblMetricaTitulo1.setText(m1);
        lblMetricaTitulo2.setText(m2);
        lblMetricaTitulo3.setText(m3);
        lblMetricaTitulo4.setText(m4);
        lblTituloPrincipal.setText(tituloPrincipal);
        lblTituloLateral.setText(tituloLateral);
        String usuario = SesionUsuario.haySesionActiva()
                ? SesionUsuario.getUsuarioActual().getNombreUsuario() : "GYMNOVA";
        lblHeroTitulo.setText("Hola, " + usuario);
        lblHeroFrase.setText(d2);
        lblFila1.setText("●  " + f1);
        lblFila2.setText("●  " + f2);
        lblFila3.setText("●  " + f3);
        lblFila4.setText("●  " + f4);
        lblFila5.setText("●  " + f5);
        lblFila6.setText("●  " + f6);
        lblDetalle1.setText(d1);
        lblDetalle2.setText(d2);
        lblDetalle3.setText(d3);
        btnAccionPrincipal.setText(accion);
    }

    private void cargarDatos() {
        if (!SesionUsuario.haySesionActiva()) {
            return;
        }
        Usuario usuario = SesionUsuario.getUsuarioActual();
        Map<String, Object> resumen = dashboardControlador.obtenerResumenPorRol(
                usuario.getNombreRol(), usuario.getIdPersona());
        lblMetricaValor1.setText(valor(resumen.get("principal1")));
        lblMetricaValor2.setText(valor(resumen.get("principal2")));
        lblMetricaValor3.setText(valor(resumen.get("principal3")));
        lblMetricaValor4.setText(valor(resumen.get("principal4")));

        List<Map<String, Object>> actividad = dashboardControlador.listarActividadPorRol(
                usuario.getNombreRol(), usuario.getIdPersona(), 6);
        if ("CLIENTES".equals(modulo) || "REPORTES".equals(modulo)) {
            javax.swing.JLabel[] filas = {lblFila1, lblFila2, lblFila3,
                lblFila4, lblFila5, lblFila6};
            for (int i = 0; i < actividad.size() && i < filas.length; i++) {
                Map<String, Object> dato = actividad.get(i);
                filas[i].setText("●  " + valor(dato.get("nombre")) + "  ·  "
                        + valor(dato.get("detalle")) + "  ·  "
                        + valor(dato.get("estado")));
            }
        }
        int progreso = Math.min(100, 20 + actividad.size() * 12);
        prgPrincipal.setValue(progreso);
    }

    private void ejecutarAccion() {
        if (accionPrincipal != null) {
            accionPrincipal.run();
        }
    }

    private void configurarImagenHero() {
        String ruta;
        if ("NUTRICION".equals(modulo)
                || "NUTRICIONISTA".equals(rolActual())) {
            ruta = "/recursos/gymnova_hero_nutricion.png";
        } else if ("RECEPCIONISTA".equals(rolActual())) {
            ruta = "/recursos/gymnova_hero_recepcion.png";
        } else {
            ruta = "/recursos/gymnova_hero_entrenamiento.png";
        }
        java.net.URL recurso = getClass().getResource(
                ruta);
        if (recurso == null) {
            return;
        }
        imagenFondo = new javax.swing.ImageIcon(recurso).getImage();
        lblHeroImagen.setVisible(false);
        pnlHero.setOpaque(false);
    }

    private void configurarContenidoEspecializado() {
        pnlCentro.remove(pnlTrabajo);
        if (contenidoEspecializado != null) {
            pnlCentro.remove(contenidoEspecializado);
        }
        contenidoEspecializado = new PnlContenidoEspecializado(
                modulo, rolActual(), accionPrincipal);
        pnlCentro.add(contenidoEspecializado, java.awt.BorderLayout.CENTER);
        pnlCentro.revalidate();
        pnlCentro.repaint();
    }

    @Override
    protected void paintComponent(java.awt.Graphics graphics) {
        super.paintComponent(graphics);
        if (imagenFondo == null) {
            return;
        }
        java.awt.Graphics2D g2 = (java.awt.Graphics2D) graphics.create();
        g2.setRenderingHint(
                java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
        g2.setColor(new java.awt.Color(2, 13, 31, 188));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    private String tituloModulo() {
        if (modulo.isBlank()) {
            return "Modulo";
        }
        return modulo.substring(0, 1) + modulo.substring(1).toLowerCase(Locale.ROOT);
    }

    private String rolActual() {
        return SesionUsuario.haySesionActiva()
                ? normalizar(SesionUsuario.getUsuarioActual().getNombreRol()) : "";
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toUpperCase(Locale.ROOT);
    }

    private static String valor(Object dato) {
        return dato == null ? "0" : dato.toString();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        pnlEncabezado = new javax.swing.JPanel();
        pnlTitulos = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblSubtitulo = new javax.swing.JLabel();
        btnAccionPrincipal = new javax.swing.JButton();
        pnlCentro = new javax.swing.JPanel();
        pnlHero = new javax.swing.JPanel();
        pnlHeroTexto = new javax.swing.JPanel();
        lblHeroTitulo = new javax.swing.JLabel();
        lblHeroFrase = new javax.swing.JLabel();
        lblHeroImagen = new javax.swing.JLabel();
        pnlTrabajo = new javax.swing.JPanel();
        pnlMetricas = new javax.swing.JPanel();
        pnlMetrica1 = crearTarjetaMetrica();
        lblMetricaTitulo1 = new javax.swing.JLabel("Indicador principal");
        lblMetricaValor1 = crearValor("0");
        pnlMetrica2 = crearTarjetaMetrica();
        lblMetricaTitulo2 = new javax.swing.JLabel("Pendientes");
        lblMetricaValor2 = crearValor("0");
        pnlMetrica3 = crearTarjetaMetrica();
        lblMetricaTitulo3 = new javax.swing.JLabel("Actividad");
        lblMetricaValor3 = crearValor("0");
        pnlMetrica4 = crearTarjetaMetrica();
        lblMetricaTitulo4 = new javax.swing.JLabel("Estado");
        lblMetricaValor4 = crearValor("Activo");
        pnlCuerpo = new javax.swing.JPanel();
        pnlPrincipal = crearTarjetaCuerpo();
        lblTituloPrincipal = crearTituloSeccion("Actividad principal");
        pnlLista = new javax.swing.JPanel(new java.awt.GridLayout(6, 1, 0, 9));
        lblFila1 = new javax.swing.JLabel("● Primer elemento de trabajo");
        lblFila2 = new javax.swing.JLabel("● Segundo elemento de trabajo");
        lblFila3 = new javax.swing.JLabel("● Tercer elemento de trabajo");
        lblFila4 = new javax.swing.JLabel("● Cuarto elemento de trabajo");
        lblFila5 = new javax.swing.JLabel("● Quinto elemento de trabajo");
        lblFila6 = new javax.swing.JLabel("● Sin novedades adicionales");
        pnlLateral = crearTarjetaCuerpo();
        lblTituloLateral = crearTituloSeccion("Resumen y progreso");
        pnlResumen = new javax.swing.JPanel(new java.awt.GridLayout(7, 1, 0, 9));
        lblDetalle1 = new javax.swing.JLabel("Estado general: correcto");
        prgPrincipal = new javax.swing.JProgressBar();
        lblDetalle2 = new javax.swing.JLabel("Proxima actividad programada");
        lblDetalle3 = new javax.swing.JLabel("Recomendacion personalizada");
        separador = new javax.swing.JSeparator();
        lblDetalle4 = new javax.swing.JLabel("Acceso rapido disponible");
        lblDetalle5 = new javax.swing.JLabel("Informacion actualizada desde PostgreSQL");

        setBackground(new java.awt.Color(3, 15, 34));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 22, 20, 22));
        setLayout(new java.awt.BorderLayout(0, 16));
        pnlEncabezado.setBackground(new java.awt.Color(6, 23, 47));
        pnlEncabezado.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 18, 14, 18));
        pnlEncabezado.setLayout(new java.awt.BorderLayout(15, 0));
        pnlTitulos.setOpaque(false);
        pnlTitulos.setLayout(new java.awt.GridLayout(2, 1, 0, 3));
        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 28));
        lblTitulo.setForeground(java.awt.Color.WHITE);
        lblTitulo.setText("Modulo operativo");
        lblSubtitulo.setForeground(new java.awt.Color(162, 199, 241));
        lblSubtitulo.setText("Vista de trabajo del usuario autenticado");
        pnlTitulos.add(lblTitulo);
        pnlTitulos.add(lblSubtitulo);
        pnlEncabezado.add(pnlTitulos, java.awt.BorderLayout.CENTER);
        btnAccionPrincipal.setBackground(new java.awt.Color(0, 174, 255));
        btnAccionPrincipal.setFont(new java.awt.Font("SansSerif", 1, 13));
        btnAccionPrincipal.setForeground(java.awt.Color.WHITE);
        btnAccionPrincipal.setText("Abrir gestion");
        btnAccionPrincipal.setPreferredSize(new java.awt.Dimension(180, 42));
        pnlEncabezado.add(btnAccionPrincipal, java.awt.BorderLayout.EAST);
        add(pnlEncabezado, java.awt.BorderLayout.NORTH);

        pnlCentro.setOpaque(false);
        pnlCentro.setLayout(new java.awt.BorderLayout(0, 14));

        pnlHero.setBackground(new java.awt.Color(6, 23, 47));
        pnlHero.setBorder(javax.swing.BorderFactory.createLineBorder(
                new java.awt.Color(8, 124, 255), 2));
        pnlHero.setPreferredSize(new java.awt.Dimension(900, 154));
        pnlHero.setLayout(new java.awt.BorderLayout(18, 0));
        pnlHeroTexto.setOpaque(false);
        pnlHeroTexto.setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 26, 24, 16));
        pnlHeroTexto.setLayout(new java.awt.GridLayout(2, 1, 0, 8));
        lblHeroTitulo.setFont(new java.awt.Font("SansSerif", 1, 24));
        lblHeroTitulo.setForeground(java.awt.Color.WHITE);
        lblHeroTitulo.setText("Tu espacio GYMNOVA");
        lblHeroFrase.setFont(new java.awt.Font("SansSerif", 0, 14));
        lblHeroFrase.setForeground(new java.awt.Color(49, 197, 244));
        lblHeroFrase.setText("Informacion preparada para tu rol");
        pnlHeroTexto.add(lblHeroTitulo);
        pnlHeroTexto.add(lblHeroFrase);
        pnlHero.add(pnlHeroTexto, java.awt.BorderLayout.CENTER);
        lblHeroImagen.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblHeroImagen.setPreferredSize(new java.awt.Dimension(500, 150));
        pnlHero.add(lblHeroImagen, java.awt.BorderLayout.EAST);
        pnlCentro.add(pnlHero, java.awt.BorderLayout.NORTH);

        pnlTrabajo.setOpaque(false);
        pnlTrabajo.setLayout(new java.awt.BorderLayout(0, 14));
        pnlMetricas.setOpaque(false);
        pnlMetricas.setLayout(new java.awt.GridLayout(1, 4, 12, 0));
        agregarMetrica(pnlMetrica1, lblMetricaTitulo1, lblMetricaValor1);
        agregarMetrica(pnlMetrica2, lblMetricaTitulo2, lblMetricaValor2);
        agregarMetrica(pnlMetrica3, lblMetricaTitulo3, lblMetricaValor3);
        agregarMetrica(pnlMetrica4, lblMetricaTitulo4, lblMetricaValor4);
        pnlTrabajo.add(pnlMetricas, java.awt.BorderLayout.NORTH);

        pnlCuerpo.setOpaque(false);
        pnlCuerpo.setLayout(new java.awt.GridLayout(1, 2, 14, 0));
        pnlPrincipal.setLayout(new java.awt.BorderLayout(0, 14));
        pnlPrincipal.add(lblTituloPrincipal, java.awt.BorderLayout.NORTH);
        pnlLista.setBackground(java.awt.Color.WHITE);
        for (javax.swing.JLabel fila : new javax.swing.JLabel[]{lblFila1, lblFila2,
                lblFila3, lblFila4, lblFila5, lblFila6}) pnlLista.add(fila);
        pnlPrincipal.add(pnlLista, java.awt.BorderLayout.CENTER);
        pnlCuerpo.add(pnlPrincipal);
        pnlLateral.setLayout(new java.awt.BorderLayout(0, 14));
        pnlLateral.add(lblTituloLateral, java.awt.BorderLayout.NORTH);
        pnlResumen.setBackground(java.awt.Color.WHITE);
        prgPrincipal.setValue(72);
        prgPrincipal.setStringPainted(true);
        lblDetalle4.setFont(new java.awt.Font("SansSerif", 1, 13));
        lblDetalle4.setForeground(new java.awt.Color(8, 124, 255));
        pnlResumen.add(lblDetalle1); pnlResumen.add(prgPrincipal);
        pnlResumen.add(lblDetalle2); pnlResumen.add(lblDetalle3);
        pnlResumen.add(separador); pnlResumen.add(lblDetalle4); pnlResumen.add(lblDetalle5);
        pnlLateral.add(pnlResumen, java.awt.BorderLayout.CENTER);
        pnlCuerpo.add(pnlLateral);
        pnlTrabajo.add(pnlCuerpo, java.awt.BorderLayout.CENTER);
        pnlCentro.add(pnlTrabajo, java.awt.BorderLayout.CENTER);
        add(pnlCentro, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private javax.swing.JPanel crearTarjetaMetrica() {
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(2, 1));
        panel.setBackground(new java.awt.Color(8, 31, 61));
        panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(8, 124, 255)),
                javax.swing.BorderFactory.createEmptyBorder(15, 16, 15, 16)));
        return panel;
    }

    private javax.swing.JPanel crearTarjetaCuerpo() {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(java.awt.Color.WHITE);
        panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(210, 222, 239)),
                javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return panel;
    }

    private javax.swing.JLabel crearValor(String texto) {
        javax.swing.JLabel etiqueta = new javax.swing.JLabel(texto);
        etiqueta.setFont(new java.awt.Font("SansSerif", 1, 24));
        etiqueta.setForeground(new java.awt.Color(8, 124, 255));
        return etiqueta;
    }

    private javax.swing.JLabel crearTituloSeccion(String texto) {
        javax.swing.JLabel etiqueta = new javax.swing.JLabel(texto);
        etiqueta.setFont(new java.awt.Font("SansSerif", 1, 17));
        return etiqueta;
    }

    private void agregarMetrica(javax.swing.JPanel panel,
            javax.swing.JLabel titulo, javax.swing.JLabel valor) {
        titulo.setForeground(new java.awt.Color(194, 217, 244));
        titulo.setFont(new java.awt.Font("SansSerif", 0, 13));
        valor.setForeground(new java.awt.Color(49, 197, 244));
        panel.add(titulo); panel.add(valor); pnlMetricas.add(panel);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAccionPrincipal;
    private javax.swing.JLabel lblDetalle1, lblDetalle2, lblDetalle3, lblDetalle4, lblDetalle5;
    private javax.swing.JLabel lblFila1, lblFila2, lblFila3, lblFila4, lblFila5, lblFila6;
    private javax.swing.JLabel lblMetricaTitulo1, lblMetricaTitulo2, lblMetricaTitulo3, lblMetricaTitulo4;
    private javax.swing.JLabel lblMetricaValor1, lblMetricaValor2, lblMetricaValor3, lblMetricaValor4;
    private javax.swing.JLabel lblSubtitulo, lblTitulo, lblTituloLateral, lblTituloPrincipal;
    private javax.swing.JLabel lblHeroFrase, lblHeroImagen, lblHeroTitulo;
    private javax.swing.JPanel pnlCentro, pnlCuerpo, pnlEncabezado, pnlLateral, pnlLista;
    private javax.swing.JPanel pnlHero, pnlHeroTexto, pnlTrabajo;
    private javax.swing.JPanel pnlMetrica1, pnlMetrica2, pnlMetrica3, pnlMetrica4, pnlMetricas;
    private javax.swing.JPanel pnlPrincipal, pnlResumen, pnlTitulos;
    private javax.swing.JProgressBar prgPrincipal;
    private javax.swing.JSeparator separador;
    // End of variables declaration//GEN-END:variables
}
