package controlador;

import conexion.ConexionPostgreSQL;
import dao.ProgresoEjercicioDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Logica de "Mi rutina" para el cliente y de las metricas de
 * cumplimiento para el dashboard del cliente y del entrenador.
 *
 * Un dia = un rango [hoy, hoy]. La semana en curso = lunes actual
 * hasta domingo actual. Los ejercicios se cuentan a partir de
 * contiene_ejercicio (los planificados) y progreso_rutina.detalle_ejercicios
 * (los que el cliente marco como realizados en cada fecha).
 */
public class RutinaProgresoControlador {

    /** Una linea del checklist diario del cliente. */
    public static final class LineaEjercicio {
        public final long idRutinaEjercicio;
        public final String diaSemana;
        public final int orden;
        public final String nombreEjercicio;
        public final int series;
        public final String repeticiones;
        public final String pesoSugerido;
        public final Integer duracionMinutos;
        public final Integer descansoSegundos;
        public final boolean hecho;

        public LineaEjercicio(long idRutinaEjercicio, String diaSemana,
                int orden, String nombreEjercicio, int series,
                String repeticiones, String pesoSugerido,
                Integer duracionMinutos, Integer descansoSegundos,
                boolean hecho) {
            this.idRutinaEjercicio = idRutinaEjercicio;
            this.diaSemana = diaSemana;
            this.orden = orden;
            this.nombreEjercicio = nombreEjercicio;
            this.series = series;
            this.repeticiones = repeticiones;
            this.pesoSugerido = pesoSugerido;
            this.duracionMinutos = duracionMinutos;
            this.descansoSegundos = descansoSegundos;
            this.hecho = hecho;
        }
    }

    /** Vista compacta de la rutina asignada + su checklist para hoy. */
    public static final class RutinaDelDia {
        public Long idAsignacion;
        public Long idRutina;
        public String estadoAsignacion;
        public boolean puedeRegistrar;
        public String nombreRutina;
        public String nivel;
        public String entrenador;
        public LocalDate fechaInicio;
        public LocalDate fechaFin;
        public LocalDate fechaConsultada;
        public List<String> diasPlanificados = new ArrayList<>();
        public List<LineaEjercicio> ejercicios = new ArrayList<>();
        public String advertenciaProgreso;
    }

    /** Opción de rutina activa que puede escoger el cliente. */
    public static final class RutinaAsignadaCliente {
        public final long idAsignacion;
        public final long idRutina;
        public final String nombreRutina;
        public final String nivel;
        public final String entrenador;
        public final String estadoAsignacion;
        public final LocalDate fechaInicio;
        public final LocalDate fechaFin;

        public RutinaAsignadaCliente(long idAsignacion, long idRutina,
                String nombreRutina, String nivel, String entrenador) {
            this(idAsignacion, idRutina, nombreRutina, nivel, entrenador,
                    "ACTIVA", null, null);
        }

        public RutinaAsignadaCliente(long idAsignacion, long idRutina,
                String nombreRutina, String nivel, String entrenador,
                String estadoAsignacion, LocalDate fechaInicio,
                LocalDate fechaFin) {
            this.idAsignacion = idAsignacion;
            this.idRutina = idRutina;
            this.nombreRutina = nombreRutina;
            this.nivel = nivel;
            this.entrenador = entrenador;
            this.estadoAsignacion = estadoAsignacion;
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
        }

        @Override public String toString() {
            String base = nombreRutina
                    + (nivel == null || nivel.isBlank() ? "" : " · " + nivel);
            if (estadoAsignacion != null
                    && !"ACTIVA".equals(estadoAsignacion)) {
                base += " (" + estadoAsignacion + ")";
            }
            return base;
        }
    }

    private static final String[] DIAS_SEMANA_ES = {
        "LUNES", "MARTES", "MIERCOLES", "JUEVES",
        "VIERNES", "SABADO", "DOMINGO"
    };

    private final ProgresoEjercicioDAO progresoDao
            = new ProgresoEjercicioDAO();
    private String mensaje = "";

    public String getMensaje() {
        return mensaje;
    }

    /**
     * Rutina activa del cliente hoy (o null si no tiene) con la lista
     * de ejercicios planificados para hoy y su estado hecho/no hecho.
     */
    public RutinaDelDia obtenerRutinaHoy(long idCliente) {
        return obtenerRutinaEnFecha(idCliente, LocalDate.now());
    }

    /**
     * Devuelve la rutina que corresponde a una fecha real dentro del periodo
     * de asignacion del cliente. La plantilla semanal se resuelve contra el
     * dia de semana de esa fecha concreta.
     */
    public RutinaDelDia obtenerRutinaEnFecha(long idCliente, LocalDate fecha) {
        mensaje = "";
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        String dia = diaSemana(fecha);

        RutinaDelDia r = new RutinaDelDia();
        r.fechaConsultada = fecha;
        try (Connection c = ConexionPostgreSQL.getConexion()) {

            sincronizarEstadosAsignaciones(c);

            // 1) Resolvemos la asignación válida para la FECHA real. También
            // conservamos FINALIZADA/PAUSADA para que el cliente pueda consultar
            // su historial; solo ACTIVA permite registrar cumplimiento.
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT ar.id_asignacion, ar.estado_asignacion, "
                    + "       rt.id_rutina, rt.nombre_rutina, rt.nivel, "
                    + "       COALESCE(pe.nombres || ' ' || pe.apellidos, ''), "
                    + "       ar.fecha_inicio, ar.fecha_fin "
                    + "  FROM asignacion_rutina ar "
                    + "  JOIN rutina rt ON rt.id_rutina = ar.id_rutina "
                    + "  LEFT JOIN persona pe ON pe.id_persona = rt.id_entrenador "
                    + " WHERE ar.id_cliente = ? "
                    + "   AND ar.estado_asignacion IN "
                    + "       ('ACTIVA','PROGRAMADA','FINALIZADA','PAUSADA') "
                    + "   AND rt.estado_rutina = 'ACTIVA' "
                    + "   AND ar.fecha_inicio <= ? "
                    + "   AND (ar.fecha_fin IS NULL OR ar.fecha_fin >= ?) "
                    + " ORDER BY ar.fecha_inicio DESC, ar.id_asignacion DESC "
                    + " LIMIT 1")) {
                s.setLong(1, idCliente);
                s.setDate(2, Date.valueOf(fecha));
                s.setDate(3, Date.valueOf(fecha));
                try (ResultSet rs = s.executeQuery()) {
                    if (rs.next()) {
                        r.idAsignacion = rs.getLong(1);
                        r.estadoAsignacion = rs.getString(2);
                        r.idRutina = rs.getLong(3);
                        r.nombreRutina = rs.getString(4);
                        r.nivel = rs.getString(5);
                        r.entrenador = rs.getString(6);
                        Date ini = rs.getDate(7);
                        Date fin = rs.getDate(8);
                        r.fechaInicio = ini == null ? null : ini.toLocalDate();
                        r.fechaFin = fin == null ? null : fin.toLocalDate();
                        r.puedeRegistrar = "ACTIVA".equals(r.estadoAsignacion)
                                && !fecha.isAfter(LocalDate.now());
                    }
                }
            }
            if (r.idRutina == null) {
                return r;
            }

            // 2) Días configurados de la rutina. Se combina la definición
            // formal del entrenador (rutina_dia_entrenamiento) con los días
            // que realmente ya tienen actividades (contiene_ejercicio). Si
            // el entrenador configuró un día sin ejercicios, aún se muestra
            // en el resumen para que el cliente sepa que existe.
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT dia FROM ( "
                    + "  SELECT dia_entrenamiento AS dia "
                    + "    FROM rutina_dia_entrenamiento "
                    + "   WHERE id_rutina = ? "
                    + "  UNION "
                    + "  SELECT dia_semana AS dia "
                    + "    FROM contiene_ejercicio "
                    + "   WHERE id_rutina = ? "
                    + ") d "
                    + "ORDER BY CASE dia "
                    + " WHEN 'LUNES' THEN 1 WHEN 'MARTES' THEN 2 "
                    + " WHEN 'MIERCOLES' THEN 3 WHEN 'JUEVES' THEN 4 "
                    + " WHEN 'VIERNES' THEN 5 WHEN 'SABADO' THEN 6 "
                    + " WHEN 'DOMINGO' THEN 7 ELSE 8 END")) {
                s.setLong(1, r.idRutina);
                s.setLong(2, r.idRutina);
                try (ResultSet rs = s.executeQuery()) {
                    while (rs.next()) {
                        r.diasPlanificados.add(rs.getString(1));
                    }
                }
            }

            // 3) Las actividades se cargan ANTES del progreso. Así, aunque la
            // migración del registro de avance aún no se haya ejecutado, el
            // cliente SIEMPRE puede ver la rutina y sus ejercicios.
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT ce.id_rutina_ejercicio, ce.dia_semana, ce.orden, "
                    + "       e.nombre_ejercicio, ce.series, ce.repeticiones, "
                    + "       ce.peso_sugerido, ce.duracion_minutos, "
                    + "       ce.descanso_segundos "
                    + "  FROM contiene_ejercicio ce "
                    + "  JOIN ejercicio e ON e.id_ejercicio = ce.id_ejercicio "
                    + " WHERE ce.id_rutina = ? "
                    + "   AND UPPER(TRIM(ce.dia_semana)) = ? "
                    + " ORDER BY ce.orden, ce.id_rutina_ejercicio")) {
                s.setLong(1, r.idRutina);
                s.setString(2, dia);
                try (ResultSet rs = s.executeQuery()) {
                    while (rs.next()) {
                        java.math.BigDecimal peso = rs.getBigDecimal(7);
                        Integer dur = (Integer) rs.getObject(8);
                        Integer desc = (Integer) rs.getObject(9);
                        r.ejercicios.add(new LineaEjercicio(
                                rs.getLong(1),
                                rs.getString(2),
                                rs.getInt(3),
                                rs.getString(4),
                                rs.getInt(5),
                                rs.getString(6),
                                peso == null ? null : peso.toPlainString(),
                                dur, desc, false
                        ));
                    }
                }
            }

            // 4) Por último aplicamos las marcas de progreso. Si la estructura
            // de progreso todavía no está migrada, las tarjetas siguen visibles
            // como PENDIENTES en vez de desaparecer.
            if (!r.ejercicios.isEmpty()) {
                try {
                    Set<Long> hechos = progresoDao.listarHechosEnFecha(
                            idCliente, r.idRutina, fecha);
                    if (!hechos.isEmpty()) {
                        List<LineaEjercicio> actualizados = new ArrayList<>();
                        for (LineaEjercicio e : r.ejercicios) {
                            actualizados.add(new LineaEjercicio(
                                    e.idRutinaEjercicio, e.diaSemana, e.orden,
                                    e.nombreEjercicio, e.series, e.repeticiones,
                                    e.pesoSugerido, e.duracionMinutos,
                                    e.descansoSegundos,
                                    hechos.contains(e.idRutinaEjercicio)));
                        }
                        r.ejercicios = actualizados;
                    }
                } catch (SQLException ex) {
                    r.advertenciaProgreso =
                            "Las actividades se cargaron, pero el registro de progreso "
                            + "requiere ejecutar la migración de progreso_rutina.";
                }
            }

        } catch (SQLException ex) {
            mensaje = "No se pudo cargar la rutina: " + ex.getMessage();
        }
        return r;
    }

    /**
     * Lista rutinas del cliente que puede consultar en "Mi rutina":
     * ACTIVA, PROGRAMADA (aún no ha iniciado) y PAUSADA. Antes de leer
     * sincroniza los estados por vencimiento/inicio para que una rutina
     * cuya fecha de inicio ya llegó pase de PROGRAMADA a ACTIVA y aparezca
     * disponible sin que el cliente tenga que refrescar.
     */
    public List<RutinaAsignadaCliente> listarRutinasActivasCliente(long idCliente) {
        List<RutinaAsignadaCliente> lista = new ArrayList<>();
        String sql = "SELECT ar.id_asignacion, r.id_rutina, r.nombre_rutina, r.nivel, "
                + "COALESCE(TRIM(p.nombres || ' ' || p.apellidos), '') entrenador, "
                + "ar.estado_asignacion, ar.fecha_inicio, ar.fecha_fin "
                + "FROM asignacion_rutina ar JOIN rutina r ON r.id_rutina=ar.id_rutina "
                + "LEFT JOIN persona p ON p.id_persona=r.id_entrenador "
                + "WHERE ar.id_cliente=? "
                + "AND ar.estado_asignacion IN ('ACTIVA','PROGRAMADA','PAUSADA') "
                + "AND r.estado_rutina='ACTIVA' "
                + "AND (ar.fecha_fin IS NULL OR ar.fecha_fin >= CURRENT_DATE) "
                + "ORDER BY CASE ar.estado_asignacion "
                + " WHEN 'ACTIVA' THEN 1 WHEN 'PROGRAMADA' THEN 2 "
                + " WHEN 'PAUSADA' THEN 3 ELSE 4 END, "
                + "ar.fecha_inicio DESC, ar.id_asignacion DESC";
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            sincronizarEstadosAsignaciones(c);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setLong(1, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Date ini = rs.getDate(7);
                        Date fin = rs.getDate(8);
                        lista.add(new RutinaAsignadaCliente(
                                rs.getLong(1), rs.getLong(2),
                                rs.getString(3), rs.getString(4),
                                rs.getString(5), rs.getString(6),
                                ini == null ? null : ini.toLocalDate(),
                                fin == null ? null : fin.toLocalDate()));
                    }
                }
            }
        } catch (SQLException ex) {
            mensaje = "No se pudieron cargar las rutinas del cliente: " + ex.getMessage();
        }
        return lista;
    }

    /**
     * Carga una asignación elegida explícitamente por el cliente. La fecha ya
     * no decide qué rutina usar; únicamente decide qué día de la plantilla
     * semanal se muestra.
     */
    public RutinaDelDia obtenerRutinaAsignadaEnFecha(long idCliente,
            long idAsignacion, LocalDate fecha) {
        mensaje = "";
        if (fecha == null) fecha = LocalDate.now();
        String dia = diaSemana(fecha);
        RutinaDelDia r = new RutinaDelDia();
        r.fechaConsultada = fecha;
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            sincronizarEstadosAsignaciones(c);
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT ar.id_asignacion, ar.estado_asignacion, r.id_rutina, "
                    + "r.nombre_rutina, r.nivel, "
                    + "COALESCE(TRIM(p.nombres || ' ' || p.apellidos), ''), "
                    + "ar.fecha_inicio, ar.fecha_fin "
                    + "FROM asignacion_rutina ar JOIN rutina r ON r.id_rutina=ar.id_rutina "
                    + "LEFT JOIN persona p ON p.id_persona=r.id_entrenador "
                    + "WHERE ar.id_asignacion=? AND ar.id_cliente=? "
                    + "AND ar.estado_asignacion IN "
                    + "    ('ACTIVA','PROGRAMADA','PAUSADA','FINALIZADA') "
                    + "AND r.estado_rutina='ACTIVA'")) {
                ps.setLong(1, idAsignacion);
                ps.setLong(2, idCliente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        r.idAsignacion = rs.getLong(1);
                        r.estadoAsignacion = rs.getString(2);
                        r.idRutina = rs.getLong(3);
                        r.nombreRutina = rs.getString(4);
                        r.nivel = rs.getString(5);
                        r.entrenador = rs.getString(6);
                        Date ini = rs.getDate(7);
                        Date fin = rs.getDate(8);
                        r.fechaInicio = ini == null ? null : ini.toLocalDate();
                        r.fechaFin = fin == null ? null : fin.toLocalDate();
                        boolean fechaVigente =
                                (r.fechaInicio == null
                                        || !fecha.isBefore(r.fechaInicio))
                                && (r.fechaFin == null
                                        || !fecha.isAfter(r.fechaFin));
                        r.puedeRegistrar =
                                "ACTIVA".equals(r.estadoAsignacion)
                                && fechaVigente
                                && !fecha.isAfter(LocalDate.now());
                    }
                }
            }
            if (r.idRutina == null) return r;

            /*
             * "Días planificados" = union de la configuración del entrenador
             * (rutina_dia_entrenamiento) y de los días con ejercicios reales
             * (contiene_ejercicio). Así, si el entrenador configuró un día
             * pero aún no le agregó ejercicios, el cliente aún ve el día
             * en el resumen (con el estado "sin actividades").
             */
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT dia FROM ( "
                    + "  SELECT dia_entrenamiento AS dia "
                    + "    FROM rutina_dia_entrenamiento "
                    + "   WHERE id_rutina = ? "
                    + "  UNION "
                    + "  SELECT dia_semana AS dia "
                    + "    FROM contiene_ejercicio "
                    + "   WHERE id_rutina = ? "
                    + ") d "
                    + "ORDER BY CASE dia "
                    + " WHEN 'LUNES' THEN 1 WHEN 'MARTES' THEN 2 "
                    + " WHEN 'MIERCOLES' THEN 3 WHEN 'JUEVES' THEN 4 "
                    + " WHEN 'VIERNES' THEN 5 WHEN 'SABADO' THEN 6 "
                    + " WHEN 'DOMINGO' THEN 7 ELSE 8 END")) {
                ps.setLong(1, r.idRutina);
                ps.setLong(2, r.idRutina);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) r.diasPlanificados.add(rs.getString(1));
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT ce.id_rutina_ejercicio, ce.dia_semana, ce.orden, e.nombre_ejercicio, "
                    + "ce.series, ce.repeticiones, ce.peso_sugerido, ce.duracion_minutos, ce.descanso_segundos "
                    + "FROM contiene_ejercicio ce JOIN ejercicio e ON e.id_ejercicio=ce.id_ejercicio "
                    + "WHERE ce.id_rutina=? AND UPPER(TRIM(ce.dia_semana))=? "
                    + "ORDER BY ce.orden, ce.id_rutina_ejercicio")) {
                ps.setLong(1, r.idRutina);
                ps.setString(2, dia);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        java.math.BigDecimal peso = rs.getBigDecimal(7);
                        r.ejercicios.add(new LineaEjercicio(rs.getLong(1), rs.getString(2),
                                rs.getInt(3), rs.getString(4), rs.getInt(5), rs.getString(6),
                                peso == null ? null : peso.toPlainString(),
                                (Integer) rs.getObject(8), (Integer) rs.getObject(9), false));
                    }
                }
            }
            if (!r.ejercicios.isEmpty()) {
                try {
                    Set<Long> hechos = progresoDao.listarHechosEnFecha(idCliente, r.idRutina, fecha);
                    List<LineaEjercicio> act = new ArrayList<>();
                    for (LineaEjercicio e : r.ejercicios) {
                        act.add(new LineaEjercicio(e.idRutinaEjercicio, e.diaSemana, e.orden,
                                e.nombreEjercicio, e.series, e.repeticiones, e.pesoSugerido,
                                e.duracionMinutos, e.descansoSegundos, hechos.contains(e.idRutinaEjercicio)));
                    }
                    r.ejercicios = act;
                } catch (SQLException ex) {
                    r.advertenciaProgreso = "Las actividades están disponibles, pero no se pudo leer el progreso.";
                }
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo cargar la rutina seleccionada: " + ex.getMessage();
        }
        return r;
    }

    public LocalDate buscarProximaFechaConActividad(long idCliente,
            long idAsignacion, LocalDate desde) {
        LocalDate base = desde == null ? LocalDate.now() : desde;
        for (int i = 1; i <= 14; i++) {
            LocalDate candidata = base.plusDays(i);
            RutinaDelDia rutina = obtenerRutinaAsignadaEnFecha(idCliente, idAsignacion, candidata);
            if (rutina.idRutina != null && !rutina.ejercicios.isEmpty()) return candidata;
        }
        return null;
    }

    public boolean marcarHecho(long idRutinaEjercicio, long idCliente,
            long idAsignacion, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();
        if (fecha.isAfter(LocalDate.now())) { mensaje = "No se puede marcar una actividad futura."; return false; }
        RutinaDelDia rutina = obtenerRutinaAsignadaEnFecha(idCliente, idAsignacion, fecha);
        if (rutina.idRutina == null || !rutina.puedeRegistrar) {
            mensaje = "La rutina seleccionada no está activa para el cliente."; return false;
        }
        boolean pertenece = rutina.ejercicios.stream()
                .anyMatch(e -> e.idRutinaEjercicio == idRutinaEjercicio);
        if (!pertenece) { mensaje = "La actividad no pertenece a la rutina seleccionada para ese día."; return false; }
        try {
            boolean ok = progresoDao.marcarHecho(idRutinaEjercicio, idCliente, fecha);
            mensaje = ok ? "Actividad marcada como hecha." : "No se pudo registrar la actividad.";
            return ok;
        } catch (SQLException ex) { mensaje = "No se pudo registrar la actividad: " + ex.getMessage(); return false; }
    }

    public boolean desmarcarHecho(long idRutinaEjercicio, long idCliente,
            long idAsignacion, LocalDate fecha) {
        if (fecha == null) fecha = LocalDate.now();
        RutinaDelDia rutina = obtenerRutinaAsignadaEnFecha(idCliente, idAsignacion, fecha);
        if (rutina.idRutina == null || !rutina.puedeRegistrar) {
            mensaje = "La rutina seleccionada no está activa para el cliente."; return false;
        }
        try {
            boolean ok = progresoDao.desmarcarHecho(idRutinaEjercicio, idCliente, fecha);
            mensaje = ok ? "Actividad nuevamente pendiente." : "La actividad no estaba marcada.";
            return ok;
        } catch (SQLException ex) { mensaje = "No se pudo corregir la actividad: " + ex.getMessage(); return false; }
    }

    /**
     * Busca la siguiente fecha (máximo una semana) que tenga actividades de la
     * rutina activa del cliente. Sirve para saltar desde un día de descanso.
     */
    public LocalDate buscarProximaFechaConActividad(long idCliente, LocalDate desde) {
        LocalDate base = desde == null ? LocalDate.now() : desde;
        for (int i = 1; i <= 60; i++) {
            LocalDate candidata = base.plusDays(i);
            RutinaDelDia rutina = obtenerRutinaEnFecha(idCliente, candidata);
            if (rutina.idRutina != null && !rutina.ejercicios.isEmpty()) {
                return candidata;
            }
        }
        return null;
    }

    /** Marca una actividad de hoy como realizada. */
    public boolean marcarHecho(long idRutinaEjercicio, long idCliente) {
        return marcarHecho(idRutinaEjercicio, idCliente, LocalDate.now());
    }

    /** Marca una actividad correspondiente a una fecha concreta. */
    public boolean marcarHecho(long idRutinaEjercicio, long idCliente,
            LocalDate fecha) {
        mensaje = "";
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        if (fecha.isAfter(LocalDate.now())) {
            mensaje = "No se puede marcar como realizada una actividad futura.";
            return false;
        }
        RutinaDelDia rutina = obtenerRutinaEnFecha(idCliente, fecha);
        if (!rutina.puedeRegistrar) {
            mensaje = rutina.idRutina == null
                    ? "No existe una rutina asignada para esa fecha."
                    : "La asignación está " + rutina.estadoAsignacion
                    + " y no admite registrar actividades en esa fecha.";
            return false;
        }
        boolean pertenece = false;
        for (LineaEjercicio ejercicio : rutina.ejercicios) {
            if (ejercicio.idRutinaEjercicio == idRutinaEjercicio) {
                pertenece = true;
                break;
            }
        }
        if (!pertenece) {
            mensaje = "La actividad seleccionada no corresponde a la rutina de esa fecha.";
            return false;
        }
        try {
            boolean registrado = progresoDao.marcarHecho(
                    idRutinaEjercicio, idCliente, fecha);
            mensaje = registrado
                    ? "Actividad marcada como realizada para " + fecha + "."
                    : "No fue posible registrar la actividad.";
            return registrado;
        } catch (SQLException ex) {
            mensaje = "No se pudo registrar la actividad: " + ex.getMessage();
            return false;
        }
    }

    public boolean desmarcarHecho(long idRutinaEjercicio, long idCliente) {
        return desmarcarHecho(idRutinaEjercicio, idCliente, LocalDate.now());
    }

    /** Corrige una marcacion de una fecha concreta. */
    public boolean desmarcarHecho(long idRutinaEjercicio, long idCliente,
            LocalDate fecha) {
        mensaje = "";
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        if (fecha.isAfter(LocalDate.now())) {
            mensaje = "No existe progreso futuro que corregir.";
            return false;
        }
        RutinaDelDia rutina = obtenerRutinaEnFecha(idCliente, fecha);
        if (!rutina.puedeRegistrar) {
            mensaje = rutina.idRutina == null
                    ? "No existe una rutina asignada para esa fecha."
                    : "La asignación está " + rutina.estadoAsignacion
                    + " y no admite corregir actividades en esa fecha.";
            return false;
        }
        boolean pertenece = false;
        for (LineaEjercicio ejercicio : rutina.ejercicios) {
            if (ejercicio.idRutinaEjercicio == idRutinaEjercicio) {
                pertenece = true;
                break;
            }
        }
        if (!pertenece) {
            mensaje = "La actividad seleccionada no corresponde a la rutina de esa fecha.";
            return false;
        }
        try {
            boolean actualizado = progresoDao.desmarcarHecho(
                    idRutinaEjercicio, idCliente, fecha);
            mensaje = actualizado
                    ? "Se retiro la marca de actividad realizada."
                    : "La actividad no estaba marcada como realizada.";
            return actualizado;
        } catch (SQLException ex) {
            mensaje = "No se pudo actualizar la actividad: " + ex.getMessage();
            return false;
        }
    }

    private boolean perteneceARutinaEnFecha(long idRutinaEjercicio,
            long idCliente, LocalDate fecha) {
        RutinaDelDia rutina = obtenerRutinaEnFecha(idCliente, fecha);
        if (!rutina.puedeRegistrar) {
            return false;
        }
        for (LineaEjercicio ejercicio : rutina.ejercicios) {
            if (ejercicio.idRutinaEjercicio == idRutinaEjercicio) {
                return true;
            }
        }
        return false;
    }

    /**
     * Porcentaje de la rutina del dia completado por el cliente:
     * ejercicios marcados hoy / ejercicios planificados hoy * 100.
     * Devuelve 0 si no hay ejercicios planificados.
     */
    public int porcentajeHoy(long idCliente) {
        return porcentajeFecha(idCliente, LocalDate.now());
    }

    public int porcentajeFecha(long idCliente, LocalDate fecha) {
        RutinaDelDia r = obtenerRutinaEnFecha(idCliente, fecha);
        int total = r.ejercicios.size();
        if (total == 0) {
            return 0;
        }
        int hechos = 0;
        for (LineaEjercicio le : r.ejercicios) {
            if (le.hecho) {
                hechos++;
            }
        }
        return (int) Math.round(100.0 * hechos / total);
    }

    /**
     * Porcentaje semanal del cliente: ejercicios marcados desde el
     * lunes al domingo actual / total planificado en esos siete dias.
     */
    public int porcentajeSemana(long idCliente) {
        mensaje = "";
        LocalDate fecha = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate domingo = fecha.plusDays(6);
        int planificados = 0;
        int hechos = 0;

        while (!fecha.isAfter(domingo)) {
            RutinaDelDia dia = obtenerRutinaEnFecha(idCliente, fecha);
            planificados += dia.ejercicios.size();
            for (LineaEjercicio e : dia.ejercicios) {
                if (e.hecho) {
                    hechos++;
                }
            }
            fecha = fecha.plusDays(1);
        }
        if (planificados == 0) {
            return 0;
        }
        return (int) Math.round(100.0 * hechos / planificados);
    }

    /**
     * Promedio del % semanal de todos los clientes activos asignados a
     * un entrenador. Se usa en el dashboard del entrenador.
     */
    public int porcentajeSemanaEntrenador(long idEntrenador) {
        mensaje = "";
        List<Long> clientes = new ArrayList<>();
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            sincronizarEstadosAsignaciones(c);
        } catch (SQLException ex) {
            mensaje = "No se pudo sincronizar el estado de las asignaciones: "
                    + ex.getMessage();
        }
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(
                     "SELECT DISTINCT ar.id_cliente "
                     + "  FROM asignacion_rutina ar "
                     + "  JOIN rutina rt ON rt.id_rutina = ar.id_rutina "
                     + " WHERE rt.id_entrenador = ? "
                     + "   AND ar.estado_asignacion = 'ACTIVA'")) {
            s.setLong(1, idEntrenador);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    clientes.add(r.getLong(1));
                }
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo consultar clientes del entrenador: "
                    + ex.getMessage();
            return 0;
        }
        if (clientes.isEmpty()) {
            return 0;
        }
        int suma = 0;
        for (Long idCliente : clientes) {
            suma += porcentajeSemana(idCliente);
        }
        return suma / clientes.size();
    }

    private void sincronizarEstadosAsignaciones(Connection c)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "UPDATE asignacion_rutina SET estado_asignacion='FINALIZADA', "
                + "motivo_finalizacion=COALESCE(motivo_finalizacion, "
                + "'Finalizada por vencimiento') "
                + "WHERE estado_asignacion IN ('ACTIVA','PAUSADA') "
                + "AND fecha_fin IS NOT NULL AND fecha_fin < CURRENT_DATE")) {
            s.executeUpdate();
        }
        try (PreparedStatement s = c.prepareStatement(
                "UPDATE asignacion_rutina SET estado_asignacion='ACTIVA' "
                + "WHERE estado_asignacion='PROGRAMADA' "
                + "AND fecha_inicio <= CURRENT_DATE "
                + "AND (fecha_fin IS NULL OR fecha_fin >= CURRENT_DATE)")) {
            s.executeUpdate();
        }
        try (PreparedStatement s = c.prepareStatement(
                "UPDATE asignacion_rutina SET estado_asignacion='FINALIZADA', "
                + "motivo_finalizacion=COALESCE(motivo_finalizacion, "
                + "'Finalizada por vencimiento') "
                + "WHERE estado_asignacion='PROGRAMADA' "
                + "AND fecha_fin IS NOT NULL AND fecha_fin < CURRENT_DATE")) {
            s.executeUpdate();
        }
    }

    // ------- helpers -------

    /**
     * Cuenta cuantos "slots" de ejercicio hay planificados entre desde
     * y hasta: por cada dia del rango se suman los ejercicios cuyo
     * dia_semana coincide con ese dia.
     */
    private int contarPlanificadosEnSemana(long idCliente, LocalDate desde,
            LocalDate hasta) throws SQLException {
        AsignacionPeriodo asignacion = obtenerAsignacionEnRango(
                idCliente, desde, hasta);
        if (asignacion == null) {
            return 0;
        }

        LocalDate inicioReal = asignacion.fechaInicio.isAfter(desde)
                ? asignacion.fechaInicio : desde;
        LocalDate finReal = asignacion.fechaFin != null
                && asignacion.fechaFin.isBefore(hasta)
                ? asignacion.fechaFin : hasta;
        if (finReal.isBefore(inicioReal)) {
            return 0;
        }

        int total = 0;
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(
                     "SELECT dia_semana, COUNT(*) FROM contiene_ejercicio "
                     + "WHERE id_rutina = ? GROUP BY dia_semana")) {
            s.setLong(1, asignacion.idRutina);
            try (ResultSet r = s.executeQuery()) {
                java.util.Map<String, Integer> porDia = new java.util.HashMap<>();
                while (r.next()) {
                    porDia.put(r.getString(1).toUpperCase(), r.getInt(2));
                }
                LocalDate fecha = inicioReal;
                while (!fecha.isAfter(finReal)) {
                    total += porDia.getOrDefault(diaSemana(fecha), 0);
                    fecha = fecha.plusDays(1);
                }
            }
        }
        return total;
    }

    private AsignacionPeriodo obtenerAsignacionEnRango(long idCliente,
            LocalDate desde, LocalDate hasta) throws SQLException {
        try (Connection c = ConexionPostgreSQL.getConexion();
             PreparedStatement s = c.prepareStatement(
                     "SELECT id_rutina, fecha_inicio, fecha_fin "
                     + "FROM asignacion_rutina "
                     + "WHERE id_cliente = ? "
                     + "AND estado_asignacion = 'ACTIVA' "
                     + "AND fecha_inicio <= ? "
                     + "AND (fecha_fin IS NULL OR fecha_fin >= ?) "
                     + "ORDER BY fecha_asignacion DESC LIMIT 1")) {
            s.setLong(1, idCliente);
            s.setDate(2, Date.valueOf(hasta));
            s.setDate(3, Date.valueOf(desde));
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) {
                    return null;
                }
                Date ini = r.getDate(2);
                Date fin = r.getDate(3);
                return new AsignacionPeriodo(
                        r.getLong(1),
                        ini.toLocalDate(),
                        fin == null ? null : fin.toLocalDate());
            }
        }
    }

    private static final class AsignacionPeriodo {
        private final long idRutina;
        private final LocalDate fechaInicio;
        private final LocalDate fechaFin;

        private AsignacionPeriodo(long idRutina, LocalDate fechaInicio,
                LocalDate fechaFin) {
            this.idRutina = idRutina;
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
        }
    }

    private static String diaSemana(LocalDate fecha) {
        return DIAS_SEMANA_ES[fecha.getDayOfWeek().getValue() - 1];
    }
}
