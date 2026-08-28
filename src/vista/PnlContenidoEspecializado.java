package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/** Contenido visual diferente para cada dominio funcional del sistema. */
public class PnlContenidoEspecializado extends JPanel {

    private static final Color FONDO = new Color(236, 244, 253);
    private static final Color AZUL_OSCURO = new Color(8, 31, 61);
    private static final Color AZUL = new Color(8, 124, 255);
    private static final Color CIAN = new Color(49, 197, 244);
    private static final Color TEXTO = new Color(13, 31, 60);
    private static final Color TARJETA = new Color(8, 31, 61);
    private static final Color TARJETA_CLARA = new Color(13, 47, 86);
    private final String modulo;
    private final String rol;
    private final Runnable accionPrincipal;
    private final controlador.ResumenModuloControlador resumen
            = new controlador.ResumenModuloControlador();

    public PnlContenidoEspecializado(String modulo, String rol) {
        this(modulo, rol, null);
    }

    public PnlContenidoEspecializado(
            String modulo, String rol, Runnable accionPrincipal) {
        this.modulo = normalizar(modulo);
        this.rol = normalizar(rol);
        this.accionPrincipal = accionPrincipal;
        setOpaque(false);
        setLayout(new BorderLayout());
        construir();
        aplicarEstiloInteractivo(this);
        conectarAccionesLocales(this);
    }

    private void construir() {
        switch (modulo) {
            case "RUTINAS" -> construirRutinas();
            case "SALUD" -> construirSalud();
            case "NUTRICION" -> construirNutricion();
            case "ACCESO" -> construirAcceso();
            case "MEMBRESIAS" -> construirMembresias();
            case "CLIENTES" -> construirClientes();
            case "FINANZAS" -> construirFinanzas();
            case "REPORTES" -> construirReportes();
            case "CONFIGURACION" -> construirConfiguracion();
            default -> construirResumen();
        }
    }

    private void construirRutinas() {
        if (rol.equals("CLIENTE")) {
            construirCalendarioRutinaCliente();
            return;
        }
        Map<String,Object> rutina=resumen.rutina(rol,idPersonaSesion());
        Long idRutina=rutina.get("id_rutina") instanceof Number n?n.longValue():null;
        List<Map<String,Object>> datosEjercicios=resumen.ejercicios(idRutina,8);
        JPanel base = espacioVertical(10);
        JPanel semana = tarjeta(new GridLayout(1, 7, 7, 0));
        String[] dias = {"LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO"};
        for (int i = 0; i < dias.length; i++) {
            String enfoque="Descanso / sin ejercicios";
            for(Map<String,Object> fila:datosEjercicios) if(dias[i].equalsIgnoreCase(texto(fila,"dia_semana",""))){enfoque=texto(fila,"nombre_ejercicio","Ejercicio");break;}
            JLabel dia = etiquetaCentrada("<html><center>" + dias[i].substring(0,3) + "<br>"+enfoque+"</center></html>");
            dia.setOpaque(true);
            dia.setBackground(i == 3 ? AZUL : TARJETA_CLARA);
            dia.setForeground(Color.WHITE);
            dia.setBorder(BorderFactory.createEmptyBorder(8, 3, 8, 3));
            semana.add(dia);
        }
        JPanel ejercicios = tarjeta(new GridLayout(Math.max(1,datosEjercicios.size()), 1, 0, 5));
        if(datosEjercicios.isEmpty()) ejercicios.add(new JLabel("No hay ejercicios registrados en la rutina."));
        else for(Map<String,Object> fila:datosEjercicios) ejercicios.add(filaEjercicio(
                texto(fila,"nombre_ejercicio","Ejercicio"),texto(fila,"series","-")+" series",
                texto(fila,"repeticiones","-")+" rep.",0));
        base.add(seccion("PLAN SEMANAL · "+texto(rutina,"nombre_rutina","SIN RUTINA"), semana));
        base.add(seccion(rol.equals("ENTRENADOR") ? "CONSTRUCTOR DE EJERCICIOS" : "EJERCICIOS DE HOY", ejercicios));
        add(base);
    }

    private void construirCalendarioRutinaCliente() {
        JPanel base = new JPanel(new BorderLayout(0, 10));
        base.setOpaque(false);
        JPanel semana = tarjeta(new GridLayout(1, 7, 7, 0));
        JPanel ejercicios = tarjeta(new BorderLayout(0, 8));
        JLabel tituloDia = titulo("EJERCICIOS DEL DIA");
        JPanel listado = new JPanel();
        listado.setOpaque(false);
        listado.setLayout(new BoxLayout(listado, BoxLayout.Y_AXIS));
        ejercicios.add(tituloDia, BorderLayout.NORTH);
        ejercicios.add(listado, BorderLayout.CENTER);

        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        DateTimeFormatter numero = DateTimeFormatter.ofPattern("dd MMM",
                new Locale("es", "EC"));
        String[] nombres = {"LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM"};

        for (int i = 0; i < 7; i++) {
            LocalDate fecha = lunes.plusDays(i);
            JButton boton = new JButton("<html><center>" + nombres[i]
                    + "<br>" + fecha.format(numero).toUpperCase() + "</center></html>");
            boton.putClientProperty("fechaRutina", fecha);
            boton.setBackground(fecha.equals(hoy) ? AZUL : TARJETA_CLARA);
            boton.setForeground(Color.WHITE);
            boton.setFocusPainted(false);
            boton.addActionListener(evento -> {
                for (java.awt.Component componente : semana.getComponents()) {
                    componente.setBackground(TARJETA_CLARA);
                }
                boton.setBackground(AZUL);
                cargarEjerciciosFecha(fecha, tituloDia, listado);
            });
            semana.add(boton);
        }

        base.add(seccion("PLAN SEMANAL · " + lunes + " AL "
                + lunes.plusDays(6), semana), BorderLayout.NORTH);
        base.add(seccion("RUTINA ASIGNADA", ejercicios), BorderLayout.CENTER);
        add(base);
        cargarEjerciciosFecha(hoy, tituloDia, listado);
    }

    private void cargarEjerciciosFecha(
            LocalDate fecha,
            JLabel tituloDia,
            JPanel listado
    ) {
        listado.removeAll();
        Long idCliente = utilidades.SesionUsuario.haySesionActiva()
                ? utilidades.SesionUsuario.getUsuarioActual().getIdPersona()
                : null;
        controlador.RutinaClienteControlador controlador
                = new controlador.RutinaClienteControlador();
        List<Map<String, Object>> filas
                = controlador.listarEjercicios(idCliente, fecha);
        tituloDia.setText("EJERCICIOS · " + fecha);

        if (filas.isEmpty()) {
            JLabel vacio = new JLabel(controlador.getMensaje().isBlank()
                    ? "No hay entrenamiento asignado para este dia."
                    : controlador.getMensaje());
            vacio.setForeground(Color.WHITE);
            vacio.setBorder(BorderFactory.createEmptyBorder(22, 10, 22, 10));
            listado.add(vacio);
        } else {
            for (Map<String, Object> fila : filas) {
                String nombre = String.valueOf(fila.get("nombre_ejercicio"));
                String series = fila.get("series") + " series";
                String repeticiones = fila.get("repeticiones") + " rep.";
                JPanel renglon = filaEjercicio(nombre, series, repeticiones, 0);
                renglon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
                listado.add(renglon);
            }
        }
        listado.revalidate();
        listado.repaint();
    }

    private void construirSalud() {
        Map<String, Object> clienteContexto = clienteAsignadoInicial();
        Long idCliente = idClienteContexto(clienteContexto);
        Map<String, Object> dato = resumen.salud(idCliente);
        String peso = texto(dato, "peso_kg", "Sin datos");
        String grasa = texto(dato, "porcentaje_grasa", "Sin datos");
        String imc = texto(dato, "imc", "Sin datos");
        JPanel base = new JPanel(new BorderLayout(10, 10));
        base.setOpaque(false);
        JPanel medidas = new JPanel(new GridLayout(1, 4, 10, 0));
        medidas.setOpaque(false);
        medidas.add(indicador("PESO ACTUAL", unidad(peso, " kg"),
                nombreClienteContexto(clienteContexto)));
        medidas.add(indicador("GRASA CORPORAL", unidad(grasa, " %"), "Medicion registrada"));
        medidas.add(indicador("IMC", imc, texto(dato, "condicion_general", "Sin evaluacion")));
        Map<String,Object> perfil = resumen.perfil(idCliente);
        medidas.add(indicador("PESO META", unidad(texto(perfil,"peso_meta","Sin datos")," kg"), "Objetivo personal"));
        JPanel cuerpo = new JPanel(new GridLayout(1, 2, 10, 0));
        cuerpo.setOpaque(false);
        JPanel evolucion = tarjeta(new GridLayout(4, 1, 0, 7));
        evolucion.add(titulo("EVOLUCION DE MEDICIONES"));
        int avance = porcentajePeso(perfil, dato);
        evolucion.add(barraConTexto("Avance hacia peso meta", avance));
        evolucion.add(new JLabel("Fecha: " + texto(dato,"fecha_evaluacion","Sin evaluacion")));
        evolucion.add(new JLabel("Proxima: " + texto(dato,"proxima_evaluacion","Sin programar")));
        List<Map<String,Object>> historial = resumen.historialSalud(idCliente,3);
        JPanel citas = tarjeta(new GridLayout(Math.max(2,historial.size()+1), 1, 0, 7));
        citas.add(titulo(rol.equals("ENTRENADOR") ? "PROXIMAS EVALUACIONES" : "HISTORIAL Y RECOMENDACIONES"));
        if (historial.isEmpty()) citas.add(new JLabel("No existen evaluaciones registradas."));
        else for (Map<String,Object> fila : historial)
            citas.add(filaSimple(texto(fila,"fecha_evaluacion","-"), texto(fila,"tipo_evaluacion","Evaluacion"), unidad(texto(fila,"peso_kg","-")," kg")));
        cuerpo.add(evolucion);
        cuerpo.add(citas);
        base.add(medidas, BorderLayout.NORTH);
        base.add(cuerpo, BorderLayout.CENTER);
        add(base);
    }

    private void construirNutricion() {
        Map<String, Object> clienteContexto = clienteAsignadoInicial();
        Long idCliente = idClienteContexto(clienteContexto);
        Map<String,Object> plan = resumen.plan(idCliente);
        JPanel base = new JPanel(new BorderLayout(10, 10));
        base.setOpaque(false);
        JPanel macros = new JPanel(new GridLayout(1, 4, 10, 0));
        macros.setOpaque(false);
        macros.add(indicador("CALORIAS", texto(plan,"calorias_objetivo","Sin datos"), "kcal objetivo"));
        macros.add(indicador("PROTEINA", unidad(texto(plan,"proteinas_objetivo_g","Sin datos")," g"), "Objetivo diario"));
        macros.add(indicador("CARBOHIDRATOS", unidad(texto(plan,"carbohidratos_objetivo_g","Sin datos")," g"), "Objetivo diario"));
        macros.add(indicador("PLAN", texto(plan,"nombre_plan","Sin plan"),
                nombreClienteContexto(clienteContexto) + " · "
                + texto(plan,"estado_plan","Sin datos")));
        List<Map<String,Object>> datosComidas = resumen.comidas(idCliente,4);
        JPanel comidas = new JPanel(new GridLayout(1, Math.max(1,datosComidas.size()), 10, 0));
        comidas.setOpaque(false);
        if (datosComidas.isEmpty()) comidas.add(indicador("PLAN ALIMENTICIO", "Sin alimentos", "El nutricionista aun no registra porciones"));
        else for (Map<String,Object> fila: datosComidas)
            comidas.add(comida(texto(fila,"hora_consumo","-"), texto(fila,"tipo_comida","COMIDA"),
                    texto(fila,"nombre_alimento","Alimento"), texto(fila,"cantidad","-")+" "+texto(fila,"unidad_medida","")));
        base.add(macros, BorderLayout.NORTH);
        base.add(seccion(rol.equals("NUTRICIONISTA") ? "PLANIFICADOR DE COMIDAS Y PORCIONES" : "MI PLAN ALIMENTICIO DE HOY", comidas), BorderLayout.CENTER);
        add(base);
    }

    private void construirAcceso() {
        if (rol.equals("CLIENTE")) {
            construirEventosAccesoCliente();
            return;
        }
        Long idCliente = idPersonaSesion();
        boolean recepcion = rol.equals("RECEPCIONISTA");
        List<Map<String,Object>> reservas = recepcion
                ? resumen.reservasOperacion(6) : resumen.reservas(idCliente,4);
        Map<String,Object> datoAcceso = resumen.acceso(idCliente);
        JPanel columnas = new JPanel(new GridLayout(1, 3, 10, 0));
        columnas.setOpaque(false);
        JPanel agenda = tarjeta(new GridLayout(Math.max(2,reservas.size()+1), 1, 0, 7));
        agenda.add(titulo(recepcion ? "RESERVAS RECIENTES" : "AGENDA DE HOY"));
        if (reservas.isEmpty()) agenda.add(new JLabel("No hay reservas registradas."));
        else for (Map<String,Object> fila : reservas)
            agenda.add(filaSimple(texto(fila,"hora_inicio","-"),
                    recepcion ? texto(fila,"cliente","Cliente")
                    : texto(fila,"nombre_clase","Clase programada"),
                    texto(fila,"estado_reserva","-")));
        JPanel proxima = tarjeta(new GridLayout(5, 1, 0, 7));
        proxima.add(titulo(rol.equals("CLIENTE") ? "PROXIMA CLASE RESERVADA" : "CONTROL DE RESERVAS"));
        Map<String,Object> proximaReserva = reservas.isEmpty() ? java.util.Collections.emptyMap() : reservas.get(0);
        proxima.add(etiquetaGrande(recepcion
                ? texto(proximaReserva,"cliente","Sin reserva")
                : texto(proximaReserva,"nombre_clase","Sin reserva")));
        proxima.add(new JLabel("Fecha: " + texto(proximaReserva,"fecha_hora_reserva","Sin programar")));
        proxima.add(new JLabel("Horario: " + texto(proximaReserva,"hora_inicio","-") + " - " + texto(proximaReserva,"hora_fin","-")));
        proxima.add(estado(texto(proximaReserva,"estado_reserva","SIN RESERVA")));
        JPanel acceso = tarjeta(new GridLayout(5, 1, 0, 7));
        acceso.add(titulo(recepcion ? "ATENCION EN RECEPCION" : "ACCESO RAPIDO"));
        acceso.add(etiquetaGrande(recepcion
                ? texto(proximaReserva,"codigo_cliente","SIN CLIENTE")
                : rol.equals("CLIENTE") ? "CODIGO / "+texto(datoAcceso,"codigo_cliente","Sin codigo")
                : "VALIDAR MEMBRESIA"));
        acceso.add(new JLabel(recepcion ? "Reserva: "
                + texto(proximaReserva,"nombre_clase","Sin clase")
                : "Estado de membresia: " + texto(datoAcceso,"estado_membresia","SIN MEMBRESIA")));
        acceso.add(new JLabel(recepcion ? "Fecha: "
                + texto(proximaReserva,"fecha_hora_reserva","Sin reservas")
                : "Ultimo ingreso: " + texto(datoAcceso,"ultimo_ingreso","Sin ingresos")));
        boolean habilitado = recepcion ? !reservas.isEmpty()
                : "ACTIVA".equalsIgnoreCase(texto(datoAcceso,"estado_membresia",""));
        acceso.add(estado(recepcion ? (habilitado ? "RESERVA LOCALIZADA" : "SIN RESERVAS")
                : habilitado ? "ACCESO HABILITADO" : "ACCESO NO HABILITADO"));
        columnas.add(agenda); columnas.add(proxima); columnas.add(acceso);
        add(columnas);
    }

    /** Portal de eventos para el cliente: clases futuras con cupo en tiempo real. */
    private void construirEventosAccesoCliente() {
        Long idCliente = idPersonaSesion();
        List<Map<String, Object>> eventos = resumen.clasesDisponibles(idCliente, 30);
        List<Map<String, Object>> misReservas = resumen.reservas(idCliente, 8);

        JPanel base = new JPanel(new BorderLayout(0, 12));
        base.setOpaque(false);

        JPanel cabecera = new JPanel(new BorderLayout(10, 0));
        cabecera.setOpaque(false);
        JLabel texto = new JLabel(
                "<html><b>CLASES GRUPALES DISPONIBLES</b><br>"
                + "Reserva Yoga, Bailoterapia, Spinning u otras clases hasta completar el cupo.</html>");
        texto.setForeground(TEXTO);
        JButton actualizar = new JButton("Actualizar eventos");
        actualizar.addActionListener(e -> refrescarContenido());
        cabecera.add(texto, BorderLayout.CENTER);
        cabecera.add(actualizar, BorderLayout.EAST);

        JPanel listado = new JPanel();
        listado.setOpaque(false);
        listado.setLayout(new BoxLayout(listado, BoxLayout.Y_AXIS));

        if (eventos.isEmpty()) {
            JPanel vacio = tarjeta(new BorderLayout());
            JLabel mensaje = new JLabel(
                    "No existen clases grupales futuras disponibles en este momento.");
            mensaje.setBorder(BorderFactory.createEmptyBorder(24, 14, 24, 14));
            vacio.add(mensaje, BorderLayout.CENTER);
            listado.add(vacio);
        } else {
            for (Map<String, Object> evento : eventos) {
                listado.add(tarjetaEventoClase(evento, idCliente));
                listado.add(javax.swing.Box.createVerticalStrut(8));
            }
        }

        JScrollPane scrollEventos = new JScrollPane(listado);
        scrollEventos.setBorder(BorderFactory.createTitledBorder("Próximos eventos"));
        scrollEventos.getVerticalScrollBar().setUnitIncrement(18);

        JPanel reservas = tarjeta(new GridLayout(Math.max(2, misReservas.size() + 1), 1, 0, 6));
        reservas.add(titulo("MIS RESERVAS"));
        if (misReservas.isEmpty()) {
            reservas.add(new JLabel("Todavía no tienes reservas registradas."));
        } else {
            for (Map<String, Object> reserva : misReservas) {
                reservas.add(filaSimple(
                        texto(reserva, "fecha_clase", "-"),
                        texto(reserva, "nombre_clase", "Clase"),
                        texto(reserva, "estado_reserva", "-")
                ));
            }
        }

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(scrollEventos, BorderLayout.CENTER);
        centro.add(reservas, BorderLayout.SOUTH);

        base.add(cabecera, BorderLayout.NORTH);
        base.add(centro, BorderLayout.CENTER);
        add(base);
    }

    private JPanel tarjetaEventoClase(Map<String, Object> evento, Long idCliente) {
        JPanel panel = tarjeta(new BorderLayout(14, 6));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        panel.setPreferredSize(new Dimension(900, 105));

        String nombre = texto(evento, "nombre_clase", "Clase grupal");
        String fecha = texto(evento, "fecha_hora", "Sin fecha");
        String entrenador = texto(evento, "entrenador", "Sin entrenador");
        String duracion = texto(evento, "duracion_base_minutos", "-");
        int cupo = entero(evento, "cupo_maximo", 0);
        int ocupados = entero(evento, "reservados", 0);
        int disponibles = entero(evento, "cupos_disponibles", 0);
        String descripcion = texto(evento, "descripcion", "");
        String estadoCupo = texto(evento, "estado_cupo", "DISPONIBLE");

        JPanel informacion = new JPanel(new GridLayout(4, 1, 0, 3));
        informacion.setOpaque(false);
        JLabel tituloEvento = etiquetaGrande(nombre);
        tituloEvento.setForeground(CIAN);
        informacion.add(tituloEvento);
        informacion.add(new JLabel("Fecha y hora: " + fecha + " · Duración: " + duracion + " min"));
        informacion.add(new JLabel("Entrenador: " + entrenador + " · Cupos: "
                + ocupados + "/" + cupo + " · Disponibles: " + disponibles));
        informacion.add(new JLabel(descripcion.isBlank() ? "Evento de clase grupal" : descripcion));

        JPanel accion = new JPanel(new GridLayout(2, 1, 0, 6));
        accion.setOpaque(false);
        JLabel estado = estado(estadoCupo);
        accion.add(estado);

        Number reservaNumero = evento.get("id_reserva_cliente") instanceof Number n ? n : null;
        JButton boton = new JButton(reservaNumero != null ? "Cancelar reserva" : "Reservar cupo");
        boolean lleno = "LLENA".equalsIgnoreCase(estadoCupo);
        boton.setEnabled(reservaNumero != null || !lleno);
        boton.addActionListener(e -> {
            controlador.FlujoRecepcionControlador flujo = new controlador.FlujoRecepcionControlador();
            if (reservaNumero != null) {
                boolean ok = flujo.cancelarReserva(reservaNumero.longValue(),
                        "Cancelada por el cliente desde Mis reservas");
                JOptionPane.showMessageDialog(this,
                        ok ? "Reserva cancelada correctamente." : flujo.getMensaje(),
                        "GYMNOVA", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
                if (ok) refrescarContenido();
                return;
            }

            Object id = evento.get("id_clase");
            if (!(id instanceof Number numeroClase)) {
                JOptionPane.showMessageDialog(this, "No se pudo identificar la clase.",
                        "GYMNOVA", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Long creada = flujo.registrarReserva(idCliente, numeroClase.intValue(),
                    LocalDateTime.now(), "Reserva realizada por el cliente");
            JOptionPane.showMessageDialog(this,
                    creada != null ? "Cupo reservado correctamente." : flujo.getMensaje(),
                    "GYMNOVA", creada != null ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            if (creada != null) refrescarContenido();
        });
        accion.add(boton);

        panel.add(informacion, BorderLayout.CENTER);
        panel.add(accion, BorderLayout.EAST);
        return panel;
    }

    private void construirMembresias() {
        if (rol.equals("RECEPCIONISTA")) {
            construirMembresiasRecepcion();
            return;
        }
        Long idCliente = idPersonaSesion();
        Map<String,Object> dato = resumen.membresia(idCliente);
        List<Map<String,Object>> datosBeneficios = resumen.beneficios(idCliente);
        JPanel columnas = new JPanel(new GridLayout(1, 3, 10, 0));
        columnas.setOpaque(false);
        JPanel plan = tarjeta(new GridLayout(6, 1, 0, 6));
        plan.add(titulo("PLAN ACTUAL"));
        plan.add(etiquetaGrande(texto(dato,"plan","SIN PLAN")));
        plan.add(new JLabel(texto(dato,"descripcion","Sin descripcion")));
        plan.add(new JLabel("Inicio: " + texto(dato,"fecha_inicio","Sin fecha")));
        plan.add(new JLabel("Vence: " + texto(dato,"fecha_fin","Sin fecha")));
        plan.add(estado(texto(dato,"estado_membresia","SIN MEMBRESIA")));
        JPanel beneficios = tarjeta(new GridLayout(6, 1, 0, 6));
        beneficios.add(titulo("BENEFICIOS INCLUIDOS"));
        for (int i=0;i<5;i++) beneficios.add(new JLabel(i<datosBeneficios.size()
                ? "- "+texto(datosBeneficios.get(i),"beneficio","Beneficio")
                : i==0 && datosBeneficios.isEmpty() ? "Sin beneficios registrados" : ""));
        JPanel vigencia = tarjeta(new GridLayout(6, 1, 0, 6));
        vigencia.add(titulo(rol.equals("RECEPCIONISTA") ? "RENOVACIONES PENDIENTES" : "VIGENCIA Y PAGOS"));
        int restantes = entero(dato,"dias_restantes",0);
        int duracion = Math.max(1,entero(dato,"duracion_dias",30));
        vigencia.add(barraConTexto("Vigencia utilizada", Math.max(0,Math.min(100,100-(restantes*100/duracion)))));
        vigencia.add(new JLabel("Dias restantes: " + restantes));
        vigencia.add(new JLabel("Precio base: $ " + texto(dato,"precio_base","0")));
        vigencia.add(new JLabel("Numero: " + texto(dato,"numero_membresia","Sin asignar")));
        vigencia.add(estado(texto(dato,"estado_membresia","SIN MEMBRESIA")));
        columnas.add(plan); columnas.add(beneficios); columnas.add(vigencia);
        add(columnas);
    }

    private void construirMembresiasRecepcion() {
        List<Map<String, Object>> membresias
                = resumen.membresiasOperacion(6);
        JPanel base = new JPanel(new BorderLayout(0, 10));
        base.setOpaque(false);
        JPanel indicadores = new JPanel(new GridLayout(1, 3, 10, 0));
        indicadores.setOpaque(false);
        long activas = membresias.stream().filter(fila -> "ACTIVA".equalsIgnoreCase(
                texto(fila, "estado_membresia", ""))).count();
        long porVencer = membresias.stream().filter(fila ->
                entero(fila, "dias_restantes", 999) <= 7).count();
        indicadores.add(indicador("MEMBRESIAS CONSULTADAS",
                String.valueOf(membresias.size()), "Registros recientes"));
        indicadores.add(indicador("ACTIVAS", String.valueOf(activas),
                "Disponibles para acceso"));
        indicadores.add(indicador("POR VENCER", String.valueOf(porVencer),
                "Siguientes 7 dias"));

        JPanel listado = tarjeta(new GridLayout(
                Math.max(2, membresias.size() + 1), 1, 0, 6));
        listado.add(titulo("RENOVACIONES Y MEMBRESIAS RECIENTES"));
        if (membresias.isEmpty()) {
            listado.add(new JLabel("No existen membresias registradas."));
        } else {
            for (Map<String, Object> fila : membresias) {
                listado.add(filaSimple(
                        texto(fila, "codigo_cliente", "-"),
                        texto(fila, "cliente", "Cliente") + " · "
                        + texto(fila, "plan", "Sin plan"),
                        texto(fila, "estado_membresia", "-") + " · vence "
                        + texto(fila, "fecha_fin", "-")
                ));
            }
        }
        base.add(indicadores, BorderLayout.NORTH);
        base.add(listado, BorderLayout.CENTER);
        add(base);
    }

    private void construirClientes() {
        if (rol.equals("CLIENTE")) {
            construirPerfilCliente();
            return;
        }
        JPanel base = new JPanel(new BorderLayout(10, 10));
        base.setOpaque(false);
        JPanel filtros = tarjeta(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JTextField buscar = new JTextField(28);
        buscar.setToolTipText("Buscar por código, nombre, detalle o estado.");
        JComboBox<String> estadoFiltro = new JComboBox<>(new String[]{
            "Todos", "Con seguimiento", "Pendientes"
        });
        JButton botonBuscar = new JButton("BUSCAR");
        filtros.add(buscar);
        filtros.add(estadoFiltro);
        filtros.add(botonBuscar);
        List<Map<String,Object>> datosClientes
                = resumen.clientes(rol,idPersonaSesion(),30);
        JPanel fichas = new JPanel();
        fichas.setOpaque(false);
        Runnable aplicarFiltro = () -> mostrarClientesFiltrados(
                fichas, datosClientes, buscar.getText(),
                String.valueOf(estadoFiltro.getSelectedItem())
        );
        botonBuscar.addActionListener(evento -> aplicarFiltro.run());
        buscar.addActionListener(evento -> aplicarFiltro.run());
        estadoFiltro.addActionListener(evento -> aplicarFiltro.run());
        aplicarFiltro.run();
        base.add(filtros, BorderLayout.NORTH);
        base.add(fichas, BorderLayout.CENTER);
        add(base);
    }

    private void construirPerfilCliente() {
        Long idCliente=idPersonaSesion();
        Map<String,Object> dato=resumen.perfil(idCliente);
        Map<String,Object> membresia=resumen.membresia(idCliente);
        Map<String,Object> plan=resumen.plan(idCliente);
        JPanel columnas = new JPanel(new GridLayout(1, 3, 10, 0));
        columnas.setOpaque(false);
        JPanel perfil = tarjeta(new GridLayout(6, 1, 0, 7));
        perfil.add(titulo("MI PERFIL"));
        perfil.add(etiquetaGrande(texto(dato,"nombres","Sin perfil")+" "+texto(dato,"apellidos","")));
        perfil.add(new JLabel("Codigo: "+texto(dato,"codigo_cliente","Sin codigo")));
        perfil.add(new JLabel("Cedula: "+texto(dato,"cedula","Sin cedula")));
        perfil.add(new JLabel(texto(dato,"correo","Sin correo")+" / "+texto(dato,"telefono","Sin telefono")));
        perfil.add(estado(booleano(dato,"estado_cliente") ? "PERFIL ACTIVO" : "PERFIL INACTIVO"));
        JPanel objetivo = tarjeta(new GridLayout(5, 1, 0, 8));
        objetivo.add(titulo("OBJETIVO FITNESS"));
        objetivo.add(etiquetaGrande("PESO META"));
        objetivo.add(barraConTexto("Avance actual", porcentajePeso(dato,resumen.salud(idCliente))));
        objetivo.add(new JLabel("Peso inicial: "+unidad(texto(dato,"peso_inicial","Sin datos")," kg")));
        objetivo.add(new JLabel("Peso meta: "+unidad(texto(dato,"peso_meta","Sin datos")," kg")));
        JPanel resumen = tarjeta(new GridLayout(6, 1, 0, 7));
        resumen.add(titulo("RESUMEN PERSONAL"));
        Map<String,Object> tablero=new controlador.DashboardControlador().obtenerResumenPorRol("CLIENTE",idCliente);
        resumen.add(new JLabel("Rutinas activas: "+texto(tablero,"principal2","0")));
        resumen.add(new JLabel("Membresia: "+texto(membresia,"plan","Sin plan")));
        resumen.add(new JLabel("Reservas activas: "+texto(tablero,"reservas","0")));
        resumen.add(new JLabel("Plan nutricional: "+texto(plan,"estado_plan","Sin plan")));
        resumen.add(new JLabel("Asistencias este mes: "+texto(tablero,"asistencias","0")));
        columnas.add(perfil); columnas.add(objetivo); columnas.add(resumen);
        add(columnas);
    }

    private void construirFinanzas() {
        Map<String,Object> dato=resumen.finanzas();
        List<Map<String,Object>> datosMovimientos=resumen.movimientos(4);
        JPanel base = new JPanel(new BorderLayout(10, 10));
        base.setOpaque(false);
        JPanel indicadores = new JPanel(new GridLayout(1, 4, 10, 0));
        indicadores.setOpaque(false);
        indicadores.add(indicador("CAJA DE HOY", "$ "+texto(dato,"caja_hoy","0"), "Pagos confirmados"));
        indicadores.add(indicador("COBROS HOY", texto(dato,"cobros_hoy","0"), "Confirmados"));
        indicadores.add(indicador("PENDIENTES", texto(dato,"pendientes","0"), "Por confirmar"));
        indicadores.add(indicador("COMPROBANTES", texto(dato,"comprobantes","0"), "Generados"));
        JPanel movimientos = tarjeta(new GridLayout(5, 1, 0, 5));
        movimientos.add(titulo("MOVIMIENTOS RECIENTES"));
        for(int i=0;i<4;i++) movimientos.add(i<datosMovimientos.size()
                ? filaSimple(texto(datosMovimientos.get(i),"fecha_hora_pago","-"),
                        texto(datosMovimientos.get(i),"referencia","Sin factura"),
                        "$ "+texto(datosMovimientos.get(i),"monto_pago","0"))
                : new JLabel(i==0 && datosMovimientos.isEmpty()?"No existen movimientos registrados.":""));
        base.add(indicadores, BorderLayout.NORTH);
        base.add(movimientos, BorderLayout.CENTER);
        add(base);
    }

    private void construirReportes() {
        List<Map<String,Object>> actividad=resumen.actividad(rol,idPersonaSesion());
        JPanel base = new JPanel(new BorderLayout(10, 10));
        base.setOpaque(false);
        JPanel filtros = tarjeta(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtros.add(new JLabel(
                "Resumen de los ultimos 7 dias consultado desde PostgreSQL."
        ));
        JButton actualizar = new JButton("ACTUALIZAR RESUMEN");
        actualizar.setToolTipText(
                "Vuelve a consultar PostgreSQL para actualizar este resumen."
        );
        actualizar.addActionListener(evento -> refrescarContenido());
        filtros.add(actualizar);
        if (accionPrincipal != null) {
            JButton abrirReporte = new JButton("ABRIR REPORTE FILTRABLE");
            abrirReporte.setToolTipText(
                    "Abre filtros reales por entidad y fechas, vista previa, CSV e impresion."
            );
            abrirReporte.addActionListener(evento -> accionPrincipal.run());
            filtros.add(abrirReporte);
        }
        JPanel graficas = new JPanel(new GridLayout(1, 2, 10, 0));
        graficas.setOpaque(false);
        JPanel semanal = tarjeta(new GridLayout(8, 1, 0, 4));
        semanal.add(titulo(rol.equals("CLIENTE") ? "MI ACTIVIDAD SEMANAL" : "ACTIVIDAD SEMANAL"));
        String[] dias={"Lunes","Martes","Miercoles","Jueves","Viernes","Sabado","Domingo"};
        int maximo=1,total=0;
        for(Map<String,Object> fila:actividad){ int valor=entero(fila,"total",0); total+=valor; maximo=Math.max(maximo,valor); }
        for(int i=0;i<7;i++){ int valor=i<actividad.size()?entero(actividad.get(i),"total",0):0;
            semanal.add(barraConTexto(dias[i]+" ("+valor+")",valor*100/maximo)); }
        JPanel resumen = tarjeta(new GridLayout(6, 1, 0, 7));
        resumen.add(titulo("RESUMEN DEL PERIODO"));
        resumen.add(indicadorLinea("Registros del periodo", String.valueOf(total)));
        resumen.add(indicadorLinea("Dias con actividad", String.valueOf(actividad.stream().filter(f->entero(f,"total",0)>0).count())));
        resumen.add(indicadorLinea("Dias sin actividad", String.valueOf(actividad.stream().filter(f->entero(f,"total",0)==0).count())));
        resumen.add(indicadorLinea("Origen", "PostgreSQL"));
        resumen.add(estado("REPORTE ACTUALIZADO"));
        graficas.add(semanal); graficas.add(resumen);
        base.add(filtros, BorderLayout.NORTH);
        base.add(graficas, BorderLayout.CENTER);
        add(base);
    }

    private void construirConfiguracion() {
        modelo.Usuario usuario=utilidades.SesionUsuario.haySesionActiva()
                ? utilidades.SesionUsuario.getUsuarioActual():new modelo.Usuario();
        Map<String,Object> dato=resumen.perfil(usuario.getIdPersona());
        JPanel columnas = new JPanel(new GridLayout(1, 3, 10, 0));
        columnas.setOpaque(false);
        JPanel perfil = tarjeta(new GridLayout(6, 1, 0, 7));
        perfil.add(titulo("PERFIL DE USUARIO"));
        perfil.add(etiquetaGrande(rol));
        perfil.add(new JLabel("Alias: "+(usuario.getNombreUsuario()==null?"Sin alias":usuario.getNombreUsuario())));
        perfil.add(new JLabel(texto(dato,"correo","Sin correo")+" / "+texto(dato,"telefono","Sin telefono")));
        perfil.add(new JButton("CAMBIAR ALIAS O AVATAR"));
        perfil.add(estado(usuario.isEstadoUsuario()?"CUENTA ACTIVA":"CUENTA INACTIVA"));
        JPanel preferencias = tarjeta(new GridLayout(6, 1, 0, 7));
        preferencias.add(titulo("PREFERENCIAS"));
        preferencias.add(new JLabel("El usuario puede cambiar su alias."));
        preferencias.add(new JLabel("Puede elegir un avatar PNG o JPG."));
        preferencias.add(new JLabel("La cedula y el rol estan protegidos."));
        preferencias.add(new JLabel("Los cambios se guardan en PostgreSQL."));
        preferencias.add(new JButton("ADMINISTRAR MI CUENTA"));
        JPanel seguridad = tarjeta(new GridLayout(6, 1, 0, 7));
        seguridad.add(titulo("SEGURIDAD"));
        seguridad.add(new JLabel("Contrasena protegida"));
        seguridad.add(new JLabel("Ultimo acceso: "+(usuario.getUltimoAcceso()==null?"Sin registro":usuario.getUltimoAcceso())));
        seguridad.add(new JLabel("Intentos fallidos: "+usuario.getIntentosFallidos()));
        seguridad.add(new JButton("CAMBIAR CONTRASENA"));
        seguridad.add(estado("SESION SEGURA"));
        columnas.add(perfil); columnas.add(preferencias); columnas.add(seguridad);
        add(columnas);
    }

    private void construirResumen() {
        add(seccion("RESUMEN DEL MODULO", indicador("ESTADO", "ACTIVO", "Informacion disponible")));
    }

    private JPanel espacioVertical(int separacion) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JPanel seccion(String texto, JPanel contenido) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(titulo(texto), BorderLayout.NORTH);
        panel.add(contenido, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        return panel;
    }

    private JPanel tarjeta(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(8, 124, 255)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        return panel;
    }

    private JPanel indicador(String nombre, String valor, String detalle) {
        JPanel panel = tarjeta(new GridLayout(3, 1, 0, 3));
        JLabel n = new JLabel(nombre); n.setForeground(new Color(178, 211, 245));
        JLabel v = etiquetaGrande(valor); v.setForeground(CIAN);
        JLabel d = new JLabel(detalle); d.setForeground(CIAN);
        panel.add(n); panel.add(v); panel.add(d);
        return panel;
    }

    private JPanel comida(String hora, String nombre, String detalle, String kcal) {
        JPanel panel = tarjeta(new GridLayout(5, 1, 0, 5));
        JLabel h = new JLabel(hora); h.setForeground(AZUL);
        panel.add(h); panel.add(titulo(nombre)); panel.add(new JLabel(detalle));
        panel.add(new JLabel(kcal)); panel.add(estado("PLANIFICADA"));
        return panel;
    }

    private JPanel fichaCliente(String codigo, String nombre, String objetivo,
            String estado, int progreso) {
        JPanel panel = tarjeta(new GridLayout(6, 1, 0, 5));
        JLabel c = new JLabel(codigo); c.setForeground(AZUL);
        panel.add(c); panel.add(etiquetaGrande(nombre)); panel.add(new JLabel(objetivo));
        panel.add(new JLabel(estado)); panel.add(barra(progreso));
        if (accionPrincipal != null) {
            JButton seguimiento = new JButton("VER SEGUIMIENTO");
            seguimiento.addActionListener(evento -> accionPrincipal.run());
            panel.add(seguimiento);
        } else {
            panel.add(new JLabel(
                    "Seguimiento disponible en Rutinas, Salud o Nutrición."
            ));
        }
        return panel;
    }

    private void mostrarClientesFiltrados(
            JPanel fichas,
            List<Map<String, Object>> datos,
            String criterio,
            String filtro
    ) {
        String buscado = normalizar(criterio);
        String estado = normalizar(filtro);
        List<Map<String, Object>> visibles = datos.stream()
                .filter(fila -> buscado.isBlank()
                || (texto(fila, "codigo_cliente", "") + " "
                + texto(fila, "nombre", "") + " "
                + texto(fila, "detalle", "") + " "
                + texto(fila, "estado", "")).toUpperCase()
                        .contains(buscado))
                .filter(fila -> "TODOS".equals(estado)
                || ("CON SEGUIMIENTO".equals(estado)
                && entero(fila, "progreso", 0) > 0)
                || ("PENDIENTES".equals(estado)
                && entero(fila, "progreso", 0) == 0))
                .toList();

        fichas.removeAll();
        fichas.setLayout(new GridLayout(
                1, Math.max(1, Math.min(4, visibles.size())), 10, 0
        ));
        if (visibles.isEmpty()) {
            fichas.add(indicador(
                    "CLIENTES", "Sin resultados",
                    "No existen clientes para el filtro seleccionado"
            ));
        } else {
            visibles.stream().limit(4).forEach(fila -> fichas.add(
                    fichaCliente(
                            texto(fila, "codigo_cliente", "-"),
                            texto(fila, "nombre", "Cliente"),
                            texto(fila, "detalle", "Sin detalle"),
                            texto(fila, "estado", "-"),
                            entero(fila, "progreso", 0)
                    )
            ));
        }
        fichas.revalidate();
        fichas.repaint();
    }

    private void refrescarContenido() {
        removeAll();
        construir();
        aplicarEstiloInteractivo(this);
        conectarAccionesLocales(this);
        revalidate();
        repaint();
    }

    private JPanel filaEjercicio(String ejercicio, String series, String repeticiones, int avance) {
        JPanel fila = new JPanel(new GridLayout(1, 4, 8, 0));
        fila.setBackground(TARJETA);
        fila.add(new JLabel(ejercicio)); fila.add(new JLabel(series));
        fila.add(new JLabel(repeticiones)); fila.add(barra(avance));
        return fila;
    }

    private JPanel filaSimple(String primero, String segundo, String tercero) {
        JPanel fila = new JPanel(new GridLayout(1, 3, 8, 0));
        fila.setBackground(TARJETA);
        JLabel uno = new JLabel(primero); uno.setForeground(AZUL);
        fila.add(uno); fila.add(new JLabel(segundo)); fila.add(new JLabel(tercero));
        return fila;
    }

    private JPanel barraConTexto(String texto, int avance) {
        JPanel fila = new JPanel(new BorderLayout(8, 0));
        fila.setBackground(TARJETA);
        fila.add(new JLabel(texto), BorderLayout.WEST);
        fila.add(barra(avance), BorderLayout.CENTER);
        return fila;
    }

    private JPanel indicadorLinea(String nombre, String valor) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setBackground(TARJETA);
        fila.add(new JLabel(nombre), BorderLayout.WEST);
        JLabel v = new JLabel(valor); v.setFont(new Font("SansSerif", Font.BOLD, 16));
        v.setForeground(AZUL); fila.add(v, BorderLayout.EAST);
        return fila;
    }

    private JProgressBar barra(int valor) {
        JProgressBar barra = new JProgressBar(0, 100);
        barra.setValue(valor); barra.setForeground(AZUL);
        barra.setBackground(new Color(20, 48, 82));
        barra.setStringPainted(true);
        return barra;
    }

    private JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 14));
        etiqueta.setForeground(Color.WHITE);
        return etiqueta;
    }

    private JLabel etiquetaGrande(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 18));
        etiqueta.setForeground(Color.WHITE);
        return etiqueta;
    }

    private JLabel etiquetaCentrada(String texto) {
        return new JLabel(texto, SwingConstants.CENTER);
    }

    private JLabel estado(String texto) {
        JLabel etiqueta = new JLabel(texto, SwingConstants.CENTER);
        etiqueta.setOpaque(true); etiqueta.setBackground(AZUL);
        etiqueta.setForeground(Color.WHITE);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(4, 7, 4, 7));
        return etiqueta;
    }

    private void aplicarEstiloInteractivo(java.awt.Container contenedor) {
        for (java.awt.Component componente : contenedor.getComponents()) {
            if (componente instanceof JButton boton) {
                boton.setBackground(AZUL);
                boton.setForeground(Color.WHITE);
                boton.setFont(new Font("SansSerif", Font.BOLD, 12));
                boton.setFocusPainted(false);
                boton.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            } else if (componente instanceof JCheckBox casilla) {
                casilla.setBackground(TARJETA_CLARA);
                casilla.setForeground(Color.WHITE);
            } else if (componente instanceof JLabel etiqueta
                    && !etiqueta.isOpaque()) {
                Color actual = etiqueta.getForeground();
                if (Color.BLACK.equals(actual) || TEXTO.equals(actual)
                        || actual.getRed() < 120 && actual.getGreen() < 130
                        && actual.getBlue() < 150) {
                    etiqueta.setForeground(new Color(229, 239, 251));
                }
            } else if (componente instanceof JTextField campo) {
                campo.setBackground(Color.WHITE);
                campo.setForeground(TEXTO);
            } else if (componente instanceof JComboBox<?> combo) {
                combo.setBackground(Color.WHITE);
                combo.setForeground(TEXTO);
            }
            if (componente instanceof java.awt.Container hijo) {
                aplicarEstiloInteractivo(hijo);
            }
        }
    }

    /** Conecta ayuda contextual sin convertir botones informativos en CRUD. */
    private void conectarAccionesLocales(java.awt.Container contenedor) {
        for (java.awt.Component componente : contenedor.getComponents()) {
            if (componente instanceof JButton boton
                    && boton.getActionListeners().length == 0) {
                if (accionPrincipal == null) {
                    boton.setEnabled(false);
                    boton.setToolTipText(
                            "Esta vista es de consulta para el rol actual."
                    );
                } else {
                    boton.addActionListener(
                            evento -> ejecutarAccionLocal(boton)
                    );
                }
            }
            if (componente instanceof java.awt.Container hijo) {
                conectarAccionesLocales(hijo);
            }
        }
    }

    /** Toda acción visible abre una función real o queda claramente deshabilitada. */
    private void ejecutarAccionLocal(JButton boton) {
        if (accionPrincipal != null) {
            accionPrincipal.run();
            return;
        }
        boton.setEnabled(false);
        boton.setToolTipText(
                "Esta vista es de consulta; el rol no tiene una operación autorizada."
        );
    }

    private Long idPersonaSesion() {
        return utilidades.SesionUsuario.haySesionActiva()
                ? utilidades.SesionUsuario.getUsuarioActual().getIdPersona() : null;
    }

    private Map<String, Object> clienteAsignadoInicial() {
        if (rol.equals("CLIENTE")) {
            return resumen.perfil(idPersonaSesion());
        }
        if (rol.equals("ENTRENADOR") || rol.equals("NUTRICIONISTA")) {
            List<Map<String, Object>> clientes
                    = resumen.clientes(rol, idPersonaSesion(), 1);
            return clientes.isEmpty()
                    ? java.util.Collections.emptyMap() : clientes.get(0);
        }
        return java.util.Collections.emptyMap();
    }

    private Long idClienteContexto(Map<String, Object> cliente) {
        if (rol.equals("CLIENTE")) {
            return idPersonaSesion();
        }
        Object valor = cliente == null ? null : cliente.get("id_cliente");
        return valor instanceof Number numero ? numero.longValue() : null;
    }

    private String nombreClienteContexto(Map<String, Object> cliente) {
        if (rol.equals("CLIENTE")) {
            return "Mi ultima evaluacion";
        }
        return "Cliente: " + texto(cliente, "nombre", "sin asignacion");
    }

    private String texto(Map<String,Object> fila,String clave,String defecto) {
        if(fila==null || fila.get(clave)==null) return defecto;
        String valor=String.valueOf(fila.get(clave)).trim();
        return valor.isEmpty() ? defecto : valor;
    }

    private int entero(Map<String,Object> fila,String clave,int defecto) {
        if(fila==null || fila.get(clave)==null) return defecto;
        Object valor=fila.get(clave);
        if(valor instanceof Number numero) return numero.intValue();
        try{return Integer.parseInt(String.valueOf(valor));}catch(NumberFormatException ex){return defecto;}
    }

    private boolean booleano(Map<String,Object> fila,String clave) {
        Object valor=fila==null?null:fila.get(clave);
        return valor instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(valor));
    }

    private String unidad(String valor,String sufijo) {
        return valor==null || valor.startsWith("Sin") || "-".equals(valor) ? valor : valor+sufijo;
    }

    private int porcentajePeso(Map<String,Object> perfil,Map<String,Object> salud) {
        try {
            double inicial=Double.parseDouble(texto(perfil,"peso_inicial","0"));
            double meta=Double.parseDouble(texto(perfil,"peso_meta","0"));
            double actual=Double.parseDouble(texto(salud,"peso_kg",String.valueOf(inicial)));
            if(inicial==meta) return actual==meta?100:0;
            return Math.max(0,Math.min(100,(int)Math.round((actual-inicial)*100.0/(meta-inicial))));
        } catch(NumberFormatException ex) { return 0; }
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toUpperCase();
    }
}
