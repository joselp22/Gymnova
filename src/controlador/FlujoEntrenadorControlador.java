package controlador;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import utilidades.Auditoria;
import utilidades.SesionUsuario;

/**
 * Cierre transaccional del flujo del Entrenador:
 *   ver clientes -> seleccionar -> crear rutina -> asignar -> evaluar.
 * Todo se ejecuta en una unica transaccion por operacion y con auditoria.
 */
public class FlujoEntrenadorControlador {

    private static final java.util.Set<String> DIAS_VALIDOS
            = java.util.Set.of("LUNES", "MARTES", "MIERCOLES", "JUEVES",
                    "VIERNES", "SABADO", "DOMINGO");
    private static final java.util.Set<String> NIVELES_VALIDOS
            = java.util.Set.of("PRINCIPIANTE", "INTERMEDIO", "AVANZADO");
    private static final BigDecimal PESO_MAXIMO = new BigDecimal("9999.99");
    private static final int MAX_NOMBRE_RUTINA = 150;
    private static final int MAX_REPETICIONES = 50;

    /** Ejercicio de un dia dentro de la rutina. */
    public static class EjercicioDia {

        public String diaSemana;
        public int orden;
        public int series;
        public String repeticiones;
        public BigDecimal pesoSugerido;
        public Long idRutinaEjercicio;
        public Integer duracionMinutos;
        public Integer descansoSegundos;
        public int idEjercicio;

        public EjercicioDia(String diaSemana, int orden, int series,
                String repeticiones, Integer descansoSegundos, int idEjercicio) {
            this.diaSemana = diaSemana;
            this.orden = orden;
            this.series = series;
            this.repeticiones = repeticiones;
            this.descansoSegundos = descansoSegundos;
            this.idEjercicio = idEjercicio;
        }
    }

    private String mensaje = "";

    public String getMensaje() {
        return mensaje;
    }

    /**
     * Crea una rutina completa (rutina + dias + ejercicios) en una unica
     * transaccion. Devuelve el id_rutina.
     */
    public Long crearRutinaCompleta(Long idEntrenador, String nombre,
            String descripcion, String nivel, int duracionSemanas,
            List<EjercicioDia> ejercicios) {
        mensaje = "";
        if (!validarCabeceraRutina(idEntrenador, nombre, nivel, duracionSemanas)
                || !validarDetalleRutina(ejercicios)) {
            return null;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                Long idRutina;
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO rutina (nombre_rutina, descripcion, "
                        + "nivel, duracion_semanas, fecha_creacion, "
                        + "estado_rutina, id_entrenador) "
                        + "VALUES (?, ?, ?, ?, CURRENT_DATE, 'ACTIVA', ?) "
                        + "RETURNING id_rutina")) {
                    s.setString(1, nombre);
                    s.setString(2, descripcion);
                    s.setString(3, nivel.toUpperCase());
                    s.setInt(4, duracionSemanas);
                    s.setLong(5, idEntrenador);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        idRutina = r.getLong(1);
                    }
                }
                sincronizarDetalleRutina(c, idEntrenador, idRutina, ejercicios);
                java.util.Set<String> dias = diasDetalle(ejercicios);
                c.commit();
                Auditoria.exito("RUTINAS", "CREAR",
                        "Rutina '" + nombre + "' (id " + idRutina
                        + ") creada por entrenador " + idEntrenador
                        + " con " + ejercicios.size() + " ejercicios en "
                        + dias.size() + " dias.");
                return idRutina;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = traducirErrorRutina("crear", ex);
            Auditoria.fallo("RUTINAS", "CREAR", mensaje, ex.getMessage());
            return null;
        }
    }

    /**
     * Reemplaza de forma transaccional el detalle de una rutina propia.
     * Sincroniza rutina_dia_entrenamiento y contiene_ejercicio.
     */
    public boolean actualizarDetalleRutina(Long idEntrenador, Long idRutina,
            List<EjercicioDia> ejercicios) {
        mensaje = "";

        if (idEntrenador == null || idRutina == null) {
            mensaje = "No se pudo identificar la rutina o el entrenador.";
            return false;
        }
        if (!validarDetalleRutina(ejercicios)) {
            return false;
        }

        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                if (!bloquearRutinaPropia(c, idEntrenador, idRutina)) {
                    c.rollback();
                    mensaje = "Solo puedes modificar el detalle de rutinas creadas por ti.";
                    return false;
                }

                sincronizarDetalleRutina(c, idEntrenador, idRutina, ejercicios);
                java.util.Set<String> dias = diasDetalle(ejercicios);

                c.commit();
                mensaje = "Detalle de rutina actualizado correctamente.";
                Auditoria.exito("RUTINAS", "MODIFICAR_DETALLE",
                        "Rutina " + idRutina + " actualizada por entrenador "
                        + idEntrenador + " con " + ejercicios.size()
                        + " actividades en " + dias.size() + " días.");
                return true;

            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = traducirErrorRutina("actualizar el detalle de", ex);
            Auditoria.fallo("RUTINAS", "MODIFICAR_DETALLE",
                    mensaje, detalleSQLException(ex));
            return false;
        }
    }

    /**
     * Actualiza en una sola transaccion la cabecera y el detalle de una
     * rutina creada por el entrenador autenticado.
     */
    public boolean actualizarRutinaCompleta(Long idEntrenador, Long idRutina,
            String nombre, String descripcion, String nivel,
            int duracionSemanas, List<EjercicioDia> ejercicios) {
        mensaje = "";

        if (idRutina == null) {
            mensaje = "No se pudo identificar la rutina.";
            return false;
        }
        if (!validarCabeceraRutina(idEntrenador, nombre, nivel, duracionSemanas)
                || !validarDetalleRutina(ejercicios)) {
            return false;
        }

        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                if (!bloquearRutinaPropia(c, idEntrenador, idRutina)) {
                    c.rollback();
                    mensaje = "Solo puedes modificar rutinas creadas por ti.";
                    return false;
                }

                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE rutina SET nombre_rutina = ?, descripcion = ?, "
                        + "nivel = ?, duracion_semanas = ? "
                        + "WHERE id_rutina = ? AND id_entrenador = ?")) {
                    s.setString(1, nombre.trim());
                    s.setString(2, descripcion);
                    s.setString(3, nivel.trim().toUpperCase());
                    s.setInt(4, duracionSemanas);
                    s.setLong(5, idRutina);
                    s.setLong(6, idEntrenador);
                    s.executeUpdate();
                }

                /*
                 * IMPORTANTE: no borramos todo el detalle y lo reinsertamos.
                 * Los id_rutina_ejercicio son las claves usadas por el progreso
                 * del cliente. Mantenerlos estables evita que una edición de
                 * rutina haga desaparecer actividades ya marcadas.
                 */
                sincronizarDetalleRutina(c, idEntrenador, idRutina, ejercicios);

                c.commit();
                mensaje = "Rutina actualizada correctamente.";
                Auditoria.exito("RUTINAS", "MODIFICAR",
                        "Rutina " + idRutina + " actualizada por entrenador "
                        + idEntrenador + " con " + ejercicios.size()
                        + " actividades.");
                return true;

            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = traducirErrorRutina("actualizar", ex);
            Auditoria.fallo("RUTINAS", "MODIFICAR", mensaje,
                    detalleSQLException(ex));
            return false;
        }
    }


    private boolean bloquearRutinaPropia(Connection c, Long idEntrenador,
            Long idRutina) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT 1 FROM rutina WHERE id_rutina = ? "
                + "AND id_entrenador = ? FOR UPDATE")) {
            s.setLong(1, idRutina);
            s.setLong(2, idEntrenador);
            try (ResultSet r = s.executeQuery()) {
                return r.next();
            }
        }
    }

    private java.util.Set<String> diasDetalle(List<EjercicioDia> ejercicios) {
        java.util.Set<String> dias = new java.util.LinkedHashSet<>();
        for (EjercicioDia ej : ejercicios) {
            dias.add(ej.diaSemana.trim().toUpperCase(java.util.Locale.ROOT));
        }
        return dias;
    }

    /**
     * Sincroniza la plantilla semanal conservando los identificadores de los
     * detalles que ya existían. Esto es esencial porque el progreso diario
     * guarda el id_rutina_ejercicio que el cliente marcó como realizado.
     */
    private void sincronizarDetalleRutina(Connection c, Long idEntrenador,
            Long idRutina, List<EjercicioDia> ejercicios) throws SQLException {

        // 1. Cada ejercicio debe existir y ser global o propio del entrenador.
        try (PreparedStatement s = c.prepareStatement(
                "SELECT 1 FROM ejercicio WHERE id_ejercicio = ? "
                + "AND (id_entrenador IS NULL OR id_entrenador = ?)")) {
            for (EjercicioDia ej : ejercicios) {
                s.setInt(1, ej.idEjercicio);
                s.setLong(2, idEntrenador);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) {
                        throw new SQLException(
                                "La actividad " + ej.idEjercicio
                                + " no está disponible para este entrenador.",
                                "23503");
                    }
                }
            }
        }

        java.util.Set<Long> idsExistentes = new java.util.LinkedHashSet<>();
        try (PreparedStatement s = c.prepareStatement(
                "SELECT id_rutina_ejercicio FROM contiene_ejercicio "
                + "WHERE id_rutina = ? FOR UPDATE")) {
            s.setLong(1, idRutina);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    idsExistentes.add(r.getLong(1));
                }
            }
        }

        java.util.Set<Long> idsRecibidos = new java.util.LinkedHashSet<>();
        for (EjercicioDia ej : ejercicios) {
            if (ej.idRutinaEjercicio != null) {
                if (!idsExistentes.contains(ej.idRutinaEjercicio)) {
                    throw new SQLException(
                            "El detalle " + ej.idRutinaEjercicio
                            + " no pertenece a la rutina " + idRutina + ".",
                            "23503");
                }
                if (!idsRecibidos.add(ej.idRutinaEjercicio)) {
                    throw new SQLException(
                            "El detalle " + ej.idRutinaEjercicio
                            + " está repetido en la actualización.", "23505");
                }
            }
        }

        java.util.Set<String> dias = diasDetalle(ejercicios);
        try (PreparedStatement s = c.prepareStatement(
                "INSERT INTO rutina_dia_entrenamiento "
                + "(id_rutina, dia_entrenamiento) VALUES (?, ?) "
                + "ON CONFLICT DO NOTHING")) {
            for (String dia : dias) {
                s.setLong(1, idRutina);
                s.setString(2, dia);
                s.addBatch();
            }
            s.executeBatch();
        }

        /*
         * Apartamos temporalmente los órdenes existentes para poder intercambiar
         * orden 1 <-> 2 sin chocar con el UNIQUE (rutina,día,orden).
         */
        if (!idsExistentes.isEmpty()) {
            try (PreparedStatement s = c.prepareStatement(
                    "UPDATE contiene_ejercicio SET orden = orden + 1000000 "
                    + "WHERE id_rutina = ?")) {
                s.setLong(1, idRutina);
                s.executeUpdate();
            }
        }

        String sqlUpdate = "UPDATE contiene_ejercicio SET dia_semana=?, "
                + "orden=?, series=?, repeticiones=?, peso_sugerido=?, "
                + "duracion_minutos=?, descanso_segundos=?, id_ejercicio=? "
                + "WHERE id_rutina_ejercicio=? AND id_rutina=?";
        String sqlInsert = "INSERT INTO contiene_ejercicio "
                + "(dia_semana, orden, series, repeticiones, peso_sugerido, "
                + "duracion_minutos, descanso_segundos, id_rutina, id_ejercicio) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement actualizar = c.prepareStatement(sqlUpdate);
             PreparedStatement insertar = c.prepareStatement(sqlInsert)) {
            for (EjercicioDia ej : ejercicios) {
                String dia = ej.diaSemana.trim().toUpperCase(java.util.Locale.ROOT);
                Integer duracion = normalizarDuracion(ej.duracionMinutos);

                if (ej.idRutinaEjercicio != null) {
                    actualizar.setString(1, dia);
                    actualizar.setInt(2, ej.orden);
                    actualizar.setInt(3, ej.series);
                    actualizar.setString(4, ej.repeticiones.trim());
                    setNullableBigDecimal(actualizar, 5, ej.pesoSugerido);
                    setNullableInteger(actualizar, 6, duracion);
                    setNullableInteger(actualizar, 7, ej.descansoSegundos);
                    actualizar.setInt(8, ej.idEjercicio);
                    actualizar.setLong(9, ej.idRutinaEjercicio);
                    actualizar.setLong(10, idRutina);
                    if (actualizar.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo actualizar el detalle "
                                + ej.idRutinaEjercicio + ".");
                    }
                } else {
                    insertar.setString(1, dia);
                    insertar.setInt(2, ej.orden);
                    insertar.setInt(3, ej.series);
                    insertar.setString(4, ej.repeticiones.trim());
                    setNullableBigDecimal(insertar, 5, ej.pesoSugerido);
                    setNullableInteger(insertar, 6, duracion);
                    setNullableInteger(insertar, 7, ej.descansoSegundos);
                    insertar.setLong(8, idRutina);
                    insertar.setInt(9, ej.idEjercicio);
                    insertar.executeUpdate();
                }
            }
        }

        // 4. Eliminamos únicamente los detalles que el entrenador quitó.
        java.util.Set<Long> eliminados = new java.util.LinkedHashSet<>(idsExistentes);
        eliminados.removeAll(idsRecibidos);
        if (!eliminados.isEmpty()) {
            try (PreparedStatement s = c.prepareStatement(
                    "DELETE FROM contiene_ejercicio "
                    + "WHERE id_rutina_ejercicio = ? AND id_rutina = ?")) {
                for (Long id : eliminados) {
                    s.setLong(1, id);
                    s.setLong(2, idRutina);
                    s.addBatch();
                }
                s.executeBatch();
            }
        }

        // 5. Quitamos días que ya no tienen ninguna actividad.
        try (PreparedStatement s = c.prepareStatement(
                "DELETE FROM rutina_dia_entrenamiento rd "
                + "WHERE rd.id_rutina = ? "
                + "AND NOT EXISTS (SELECT 1 FROM contiene_ejercicio ce "
                + "WHERE ce.id_rutina = rd.id_rutina "
                + "AND ce.dia_semana = rd.dia_entrenamiento)")) {
            s.setLong(1, idRutina);
            s.executeUpdate();
        }
    }

    private void setNullableInteger(PreparedStatement s, int indice,
            Integer valor) throws SQLException {
        if (valor == null) {
            s.setNull(indice, java.sql.Types.INTEGER);
        } else {
            s.setInt(indice, valor);
        }
    }

    private void setNullableBigDecimal(PreparedStatement s, int indice,
            BigDecimal valor) throws SQLException {
        if (valor == null) {
            s.setNull(indice, java.sql.Types.NUMERIC);
        } else {
            s.setBigDecimal(indice, valor);
        }
    }

    private boolean validarCabeceraRutina(Long idEntrenador, String nombre,
            String nivel, int duracionSemanas) {
        if (idEntrenador == null) {
            mensaje = "No se pudo identificar al entrenador.";
            return false;
        }
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (nombreLimpio.isEmpty()) {
            mensaje = "Ingresa el nombre de la rutina.";
            return false;
        }
        if (nombreLimpio.length() > MAX_NOMBRE_RUTINA) {
            mensaje = "El nombre de la rutina no puede superar "
                    + MAX_NOMBRE_RUTINA + " caracteres.";
            return false;
        }
        String nivelNormalizado = nivel == null
                ? "" : nivel.trim().toUpperCase(java.util.Locale.ROOT);
        if (!NIVELES_VALIDOS.contains(nivelNormalizado)) {
            mensaje = "Selecciona un nivel valido: PRINCIPIANTE, INTERMEDIO o AVANZADO.";
            return false;
        }
        if (duracionSemanas <= 0) {
            mensaje = "La duracion de la rutina debe ser mayor a cero semanas.";
            return false;
        }
        return true;
    }

    /**
     * Replica en Java las reglas que PostgreSQL aplica sobre
     * contiene_ejercicio. Asi una edicion invalida se rechaza antes de tocar
     * el detalle almacenado.
     */
    private boolean validarDetalleRutina(List<EjercicioDia> ejercicios) {
        if (ejercicios == null || ejercicios.isEmpty()) {
            mensaje = "La rutina debe contener al menos una actividad.";
            return false;
        }

        java.util.Set<String> posiciones = new java.util.HashSet<>();
        int fila = 0;
        for (EjercicioDia ej : ejercicios) {
            fila++;

            if (ej == null) {
                mensaje = "La actividad N° " + fila + " no contiene datos.";
                return false;
            }

            String dia = ej.diaSemana == null
                    ? "" : ej.diaSemana.trim().toUpperCase(java.util.Locale.ROOT);
            if (!DIAS_VALIDOS.contains(dia)) {
                mensaje = "La actividad N° " + fila
                        + " tiene un dia de entrenamiento invalido.";
                return false;
            }
            if (ej.orden <= 0) {
                mensaje = "El orden de la actividad N° " + fila
                        + " debe ser mayor a cero.";
                return false;
            }
            if (ej.series <= 0) {
                mensaje = "Las series de la actividad N° " + fila
                        + " deben ser mayores a cero.";
                return false;
            }

            String repeticiones = ej.repeticiones == null
                    ? "" : ej.repeticiones.trim();
            if (repeticiones.isEmpty()) {
                mensaje = "Ingresa las repeticiones de la actividad N° " + fila + ".";
                return false;
            }
            if (repeticiones.length() > MAX_REPETICIONES) {
                mensaje = "Las repeticiones de la actividad N° " + fila
                        + " no pueden superar " + MAX_REPETICIONES + " caracteres.";
                return false;
            }

            if (ej.pesoSugerido != null) {
                if (ej.pesoSugerido.signum() < 0) {
                    mensaje = "El peso sugerido de la actividad N° " + fila
                            + " no puede ser negativo.";
                    return false;
                }
                if (ej.pesoSugerido.scale() > 2) {
                    mensaje = "El peso sugerido de la actividad N° " + fila
                            + " admite maximo 2 decimales.";
                    return false;
                }
                if (ej.pesoSugerido.compareTo(PESO_MAXIMO) > 0) {
                    mensaje = "El peso sugerido de la actividad N° " + fila
                            + " no puede superar " + PESO_MAXIMO.toPlainString() + ".";
                    return false;
                }
            }

            if (ej.duracionMinutos != null && ej.duracionMinutos < 0) {
                mensaje = "La duracion de la actividad N° " + fila
                        + " debe quedar vacia o ser mayor a cero.";
                return false;
            }
            // Por compatibilidad con datos/formularios anteriores, 0 = no aplica.
            ej.duracionMinutos = normalizarDuracion(ej.duracionMinutos);

            if (ej.descansoSegundos != null && ej.descansoSegundos < 0) {
                mensaje = "El descanso de la actividad N° " + fila
                        + " no puede ser negativo.";
                return false;
            }
            if (ej.idEjercicio <= 0) {
                mensaje = "Selecciona una actividad valida en la fila N° " + fila + ".";
                return false;
            }

            String clave = dia + "#" + ej.orden;
            if (!posiciones.add(clave)) {
                mensaje = "No puede repetirse el orden " + ej.orden
                        + " dentro de " + dia + ".";
                return false;
            }

            ej.diaSemana = dia;
            ej.repeticiones = repeticiones;
        }
        return true;
    }

    private Integer normalizarDuracion(Integer valor) {
        return valor == null || valor <= 0 ? null : valor;
    }

    private String traducirErrorRutina(String operacion, SQLException ex) {
        String detalle = detalleSQLException(ex);

        if (detalle.contains("ck_contiene_ejercicio_duracion")) {
            return "No se pudo " + operacion + " la rutina: la duracion de una "
                    + "actividad debe quedar vacia o ser mayor a cero.";
        }
        if (detalle.contains("ck_contiene_ejercicio_orden")) {
            return "No se pudo " + operacion + " la rutina: el orden de las "
                    + "actividades debe ser mayor a cero.";
        }
        if (detalle.contains("ck_contiene_ejercicio_series")) {
            return "No se pudo " + operacion + " la rutina: las series deben "
                    + "ser mayores a cero.";
        }
        if (detalle.contains("ck_contiene_ejercicio_descanso")) {
            return "No se pudo " + operacion + " la rutina: el descanso no "
                    + "puede ser negativo.";
        }
        if (detalle.contains("ck_contiene_ejercicio_peso_sugerido")) {
            return "No se pudo " + operacion + " la rutina: el peso sugerido "
                    + "no puede ser negativo.";
        }
        if (detalle.contains("ck_contiene_ejercicio_repeticiones")) {
            return "No se pudo " + operacion + " la rutina: las repeticiones "
                    + "no pueden quedar vacias.";
        }
        if (detalle.contains("uq_contiene_ejercicio_rutina_dia_orden")) {
            return "No se pudo " + operacion + " la rutina: existe mas de una "
                    + "actividad con el mismo orden dentro del mismo dia.";
        }
        if (detalle.contains("fk_contiene_ejercicio_ejercicio")) {
            return "No se pudo " + operacion + " la rutina: una de las "
                    + "actividades seleccionadas ya no existe.";
        }
        if (detalle.contains("ck_rutina_nivel")) {
            return "No se pudo " + operacion + " la rutina: el nivel no es valido.";
        }
        if (detalle.contains("ck_rutina_duracion")) {
            return "No se pudo " + operacion + " la rutina: la duracion en "
                    + "semanas debe ser mayor a cero.";
        }
        if ("22003".equals(ex.getSQLState())
                || detalle.toLowerCase(java.util.Locale.ROOT)
                        .contains("numeric field overflow")) {
            return "No se pudo " + operacion + " la rutina: uno de los valores "
                    + "numericos supera el limite permitido por la base de datos.";
        }
        if ("22001".equals(ex.getSQLState())) {
            return "No se pudo " + operacion + " la rutina: uno de los textos "
                    + "supera la longitud permitida.";
        }
        return "No se pudo " + operacion + " la rutina: " + detalle;
    }

    /**
     * En operaciones batch PostgreSQL suele dejar el error real en
     * getNextException(). Lo recorremos para mostrar la restriccion exacta.
     */
    private String detalleSQLException(SQLException ex) {
        StringBuilder sb = new StringBuilder();
        SQLException actual = ex;
        int limite = 0;
        while (actual != null && limite++ < 8) {
            if (actual.getMessage() != null && !actual.getMessage().isBlank()) {
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append(actual.getMessage());
            }
            actual = actual.getNextException();
        }
        return sb.toString();
    }

    /** Vista de una asignación para la tabla del entrenador. */
    public static final class AsignacionRutinaVista {
        public final long idAsignacion;
        public final long idCliente;
        public final String cliente;
        public final long idRutina;
        public final String rutina;
        public final String estado;
        public final String dias;

        public AsignacionRutinaVista(long idAsignacion, long idCliente,
                String cliente, long idRutina, String rutina, String estado,
                String dias) {
            this.idAsignacion = idAsignacion;
            this.idCliente = idCliente;
            this.cliente = cliente;
            this.idRutina = idRutina;
            this.rutina = rutina;
            this.estado = estado;
            this.dias = dias;
        }
    }

    /**
     * Asignación directa Cliente <-> Rutina. No usa fechas de vigencia y no
     * reemplaza otras rutinas activas: un cliente puede seguir varias.
     */
    public Long asignarRutina(Long idCliente, Long idRutina, String observaciones) {
        mensaje = "";
        if (idCliente == null || idRutina == null) {
            mensaje = "Selecciona un cliente y una rutina.";
            return null;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT 1 FROM cliente c JOIN persona p ON p.id_persona=c.id_persona "
                        + "WHERE c.id_persona=? AND c.estado_cliente AND p.estado FOR UPDATE")) {
                    s.setLong(1, idCliente);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            c.rollback();
                            mensaje = "El cliente no existe o está inactivo.";
                            return null;
                        }
                    }
                }
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT 1 FROM rutina WHERE id_rutina=? AND id_entrenador=? "
                        + "AND estado_rutina='ACTIVA' FOR UPDATE")) {
                    s.setLong(1, idRutina);
                    s.setLong(2, SesionUsuario.getUsuarioActual().getIdPersona());
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            c.rollback();
                            mensaje = "La rutina no existe, está inactiva o no pertenece al entrenador.";
                            return null;
                        }
                    }
                }
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT COUNT(*) FROM contiene_ejercicio WHERE id_rutina=?")) {
                    s.setLong(1, idRutina);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        if (r.getInt(1) == 0) {
                            c.rollback();
                            mensaje = "La rutina no tiene actividades para asignar.";
                            return null;
                        }
                    }
                }
                // Evita duplicar la misma rutina si ya está activa para el cliente.
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT id_asignacion FROM asignacion_rutina "
                        + "WHERE id_cliente=? AND id_rutina=? AND estado_asignacion='ACTIVA' "
                        + "ORDER BY id_asignacion DESC LIMIT 1")) {
                    s.setLong(1, idCliente);
                    s.setLong(2, idRutina);
                    try (ResultSet r = s.executeQuery()) {
                        if (r.next()) {
                            long existente = r.getLong(1);
                            c.rollback();
                            mensaje = "Esta rutina ya está asignada y activa para el cliente.";
                            return existente;
                        }
                    }
                }
                // Si la misma rutina fue probada/cancelada hoy, reutilizamos la fila
                // para no chocar con UNIQUE(cliente,rutina,fecha_inicio).
                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE asignacion_rutina SET estado_asignacion='ACTIVA', "
                        + "fecha_fin=NULL, motivo_finalizacion=NULL, observaciones=? "
                        + "WHERE id_asignacion=(SELECT id_asignacion FROM asignacion_rutina "
                        + "WHERE id_cliente=? AND id_rutina=? AND fecha_inicio=CURRENT_DATE "
                        + "ORDER BY id_asignacion DESC LIMIT 1) RETURNING id_asignacion")) {
                    s.setString(1, observaciones);
                    s.setLong(2, idCliente);
                    s.setLong(3, idRutina);
                    try (ResultSet r = s.executeQuery()) {
                        if (r.next()) {
                            long reutilizada = r.getLong(1);
                            c.commit();
                            mensaje = "Rutina reactivada correctamente para el cliente.";
                            return reutilizada;
                        }
                    }
                }
                Long idAsignacion;
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO asignacion_rutina (fecha_asignacion, fecha_inicio, fecha_fin, "
                        + "estado_asignacion, observaciones, id_cliente, id_rutina) "
                        + "VALUES (CURRENT_DATE, CURRENT_DATE, NULL, 'ACTIVA', ?, ?, ?) "
                        + "RETURNING id_asignacion")) {
                    s.setString(1, observaciones);
                    s.setLong(2, idCliente);
                    s.setLong(3, idRutina);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        idAsignacion = r.getLong(1);
                    }
                }
                c.commit();
                mensaje = "Rutina asignada correctamente. El cliente ya puede seleccionarla en Mi rutina.";
                Auditoria.exito("RUTINAS", "ASIGNAR",
                        "Asignación directa " + idAsignacion + " rutina " + idRutina
                        + " cliente " + idCliente + ".");
                return idAsignacion;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo asignar la rutina: " + detalleSQLException(ex);
            return null;
        }
    }

    /** Lista las asignaciones de las rutinas creadas por el entrenador. */
    public java.util.List<AsignacionRutinaVista> listarAsignacionesEntrenador(Long idEntrenador) {
        java.util.List<AsignacionRutinaVista> lista = new java.util.ArrayList<>();
        if (idEntrenador == null) return lista;
        String sql = "SELECT ar.id_asignacion, ar.id_cliente, "
                + "TRIM(COALESCE(p.nombres,'') || ' ' || COALESCE(p.apellidos,'')) cliente, "
                + "ar.id_rutina, r.nombre_rutina, ar.estado_asignacion, "
                + "COALESCE(string_agg(DISTINCT ce.dia_semana, ', ' ORDER BY ce.dia_semana), '') dias "
                + "FROM asignacion_rutina ar JOIN rutina r ON r.id_rutina=ar.id_rutina "
                + "JOIN persona p ON p.id_persona=ar.id_cliente "
                + "LEFT JOIN contiene_ejercicio ce ON ce.id_rutina=r.id_rutina "
                + "WHERE r.id_entrenador=? AND ar.estado_asignacion <> 'CANCELADA' "
                + "GROUP BY ar.id_asignacion, ar.id_cliente, p.nombres, p.apellidos, "
                + "ar.id_rutina, r.nombre_rutina, ar.estado_asignacion "
                + "ORDER BY ar.id_asignacion DESC";
        try (Connection c = ConexionPostgreSQL.getConexion();
                PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, idEntrenador);
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    lista.add(new AsignacionRutinaVista(r.getLong(1), r.getLong(2),
                            r.getString(3), r.getLong(4), r.getString(5),
                            r.getString(6), r.getString(7)));
                }
            }
        } catch (SQLException ex) {
            mensaje = "No se pudieron cargar las asignaciones: " + ex.getMessage();
        }
        return lista;
    }

    /**
     * Asigna una rutina a un cliente conservando correctamente el historial.
     *
     * Una asignación futura queda PROGRAMADA. Si existe una asignación que
     * se superpone, se recorta hasta el día anterior a la nueva; una
     * asignación futura reemplazada se CANCELA. No se destruye la rutina
     * anterior ni su progreso.
     */
    public Long asignarRutina(Long idCliente, Long idRutina,
            LocalDate fechaInicio, LocalDate fechaFin, String observaciones) {
        mensaje = "";
        LocalDate hoy = LocalDate.now();

        if (idCliente == null || idRutina == null || fechaInicio == null) {
            mensaje = "Datos incompletos para la asignación.";
            return null;
        }
        if (fechaInicio.isBefore(hoy)) {
            mensaje = "Una nueva asignación debe iniciar hoy o en una fecha futura.";
            return null;
        }

        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                sincronizarEstadosAsignaciones(c);

                int semanas;
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT r.duracion_semanas "
                        + "FROM rutina r "
                        + "WHERE r.id_rutina = ? "
                        + "AND r.estado_rutina = 'ACTIVA' FOR UPDATE")) {
                    s.setLong(1, idRutina);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            c.rollback();
                            mensaje = "La rutina no existe o ya no está activa.";
                            return null;
                        }
                        semanas = r.getInt(1);
                    }
                }

                // Debe existir el cliente y encontrarse activo.
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT 1 FROM cliente c JOIN persona p "
                        + "ON p.id_persona=c.id_persona "
                        + "WHERE c.id_persona=? AND c.estado_cliente "
                        + "AND p.estado FOR UPDATE")) {
                    s.setLong(1, idCliente);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            c.rollback();
                            mensaje = "El cliente no existe o se encuentra inactivo.";
                            return null;
                        }
                    }
                }

                // No se asignan rutinas vacías.
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT COUNT(*) FROM contiene_ejercicio "
                        + "WHERE id_rutina=?")) {
                    s.setLong(1, idRutina);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        if (r.getInt(1) == 0) {
                            c.rollback();
                            mensaje = "La rutina no tiene actividades para asignar.";
                            return null;
                        }
                    }
                }

                LocalDate finReal = fechaFin;
                if (finReal == null && semanas > 0) {
                    // 4 semanas = 28 días incluyendo el día inicial.
                    finReal = fechaInicio.plusWeeks(semanas).minusDays(1);
                }
                if (finReal != null && finReal.isBefore(fechaInicio)) {
                    c.rollback();
                    mensaje = "La fecha fin no puede ser anterior al inicio.";
                    return null;
                }

                /*
                 * Bloqueamos asignaciones vigentes/programadas del cliente que
                 * se superponen. La nueva planificación reemplaza únicamente
                 * el tramo solapado, no todo el historial.
                 */
                String sqlSolapadas =
                        "SELECT id_asignacion, fecha_inicio, fecha_fin, "
                        + "estado_asignacion FROM asignacion_rutina "
                        + "WHERE id_cliente=? "
                        + "AND estado_asignacion IN ('ACTIVA','PROGRAMADA','PAUSADA') "
                        + "AND fecha_inicio <= COALESCE(?, DATE '9999-12-31') "
                        + "AND COALESCE(fecha_fin, DATE '9999-12-31') >= ? "
                        + "ORDER BY fecha_inicio FOR UPDATE";
                try (PreparedStatement s = c.prepareStatement(sqlSolapadas)) {
                    s.setLong(1, idCliente);
                    if (finReal == null) {
                        s.setNull(2, java.sql.Types.DATE);
                    } else {
                        s.setDate(2, Date.valueOf(finReal));
                    }
                    s.setDate(3, Date.valueOf(fechaInicio));
                    try (ResultSet r = s.executeQuery()) {
                        while (r.next()) {
                            long idAnterior = r.getLong(1);
                            LocalDate inicioAnterior = r.getDate(2).toLocalDate();

                            if (inicioAnterior.isBefore(fechaInicio)) {
                                LocalDate nuevoFinAnterior = fechaInicio.minusDays(1);
                                String estadoAnterior = r.getString(4);
                                String nuevoEstado = nuevoFinAnterior.isBefore(hoy)
                                        ? "FINALIZADA" : estadoAnterior;
                                String motivo = "FINALIZADA".equals(nuevoEstado)
                                        ? "Reemplazada por nueva asignación" : null;
                                try (PreparedStatement u = c.prepareStatement(
                                        "UPDATE asignacion_rutina SET fecha_fin=?, "
                                        + "estado_asignacion=?, "
                                        + "motivo_finalizacion=COALESCE(?, motivo_finalizacion) "
                                        + "WHERE id_asignacion=?")) {
                                    u.setDate(1, Date.valueOf(nuevoFinAnterior));
                                    u.setString(2, nuevoEstado);
                                    u.setString(3, motivo);
                                    u.setLong(4, idAnterior);
                                    u.executeUpdate();
                                }
                            } else {
                                try (PreparedStatement u = c.prepareStatement(
                                        "UPDATE asignacion_rutina SET "
                                        + "estado_asignacion='CANCELADA', "
                                        + "motivo_finalizacion='Reemplazada por nueva asignación' "
                                        + "WHERE id_asignacion=?")) {
                                    u.setLong(1, idAnterior);
                                    u.executeUpdate();
                                }
                            }
                        }
                    }
                }

                String estadoNuevo = fechaInicio.isAfter(hoy)
                        ? "PROGRAMADA" : "ACTIVA";

                Long idAsignacion;
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO asignacion_rutina (fecha_asignacion, "
                        + "fecha_inicio, fecha_fin, estado_asignacion, "
                        + "observaciones, id_cliente, id_rutina) "
                        + "VALUES (CURRENT_DATE, ?, ?, ?, ?, ?, ?) "
                        + "RETURNING id_asignacion")) {
                    s.setDate(1, Date.valueOf(fechaInicio));
                    if (finReal != null) {
                        s.setDate(2, Date.valueOf(finReal));
                    } else {
                        s.setNull(2, java.sql.Types.DATE);
                    }
                    s.setString(3, estadoNuevo);
                    s.setString(4, observaciones);
                    s.setLong(5, idCliente);
                    s.setLong(6, idRutina);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        idAsignacion = r.getLong(1);
                    }
                }

                c.commit();
                mensaje = "ACTIVA".equals(estadoNuevo)
                        ? "Rutina asignada y activa desde hoy."
                        : "Rutina programada desde " + fechaInicio + ".";
                Auditoria.exito("RUTINAS", "ASIGNAR",
                        "Asignación " + idAsignacion + " de rutina "
                        + idRutina + " al cliente " + idCliente
                        + " estado=" + estadoNuevo + ".");
                return idAsignacion;

            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            String detalle = detalleSQLException(ex);
            if ("23505".equals(ex.getSQLState())
                    || detalle.contains("uq_asignacion_rutina_cliente_rutina_inicio")) {
                mensaje = "Ya existe una asignación de esta rutina para el cliente "
                        + "con la misma fecha de inicio.";
            } else {
                mensaje = "No se pudo asignar la rutina: " + detalle;
            }
            Auditoria.fallo("RUTINAS", "ASIGNAR", mensaje, detalle);
            return null;
        }
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

    public boolean pausarAsignacion(Long idAsignacion) {
        return cambiarEstadoAsignacion(idAsignacion, "PAUSADA", null,
                "PAUSAR", "Asignacion pausada");
    }

    public boolean reactivarAsignacion(Long idAsignacion) {
        return cambiarEstadoAsignacion(idAsignacion, "ACTIVA", null,
                "REACTIVAR", "Asignacion reactivada");
    }

    public boolean finalizarAsignacion(Long idAsignacion, String motivo) {
        return cambiarEstadoAsignacion(idAsignacion, "FINALIZADA",
                motivo == null || motivo.isBlank()
                ? "Finalizada por entrenador" : motivo,
                "FINALIZAR", "Asignacion finalizada");
    }

    private boolean cambiarEstadoAsignacion(Long idAsignacion,
            String estado, String motivoFinalizacion, String accion,
            String descripcion) {
        mensaje = "";
        if (idAsignacion == null) {
            mensaje = "Asignacion no especificada.";
            return false;
        }
        try (Connection c = ConexionPostgreSQL.getConexion();
                PreparedStatement s = c.prepareStatement(
                "UPDATE asignacion_rutina SET estado_asignacion = ?, "
                + "motivo_finalizacion = COALESCE(?, motivo_finalizacion) "
                + "WHERE id_asignacion = ?")) {
            s.setString(1, estado);
            s.setString(2, motivoFinalizacion);
            s.setLong(3, idAsignacion);
            boolean ok = s.executeUpdate() > 0;
            if (ok) {
                Auditoria.exito("RUTINAS", accion,
                        descripcion + " id=" + idAsignacion);
            }
            return ok;
        } catch (SQLException ex) {
            mensaje = "No se pudo cambiar el estado: " + ex.getMessage();
            Auditoria.fallo("RUTINAS", accion, mensaje, ex.getMessage());
            return false;
        }
    }

    /**
     * Registra una evaluacion fisica y opcionalmente una medicion corporal
     * asociada, en una unica transaccion.
     */
    public Long registrarEvaluacion(Long idCliente, Long idEntrenador,
            String tipoEvaluacion, String motivo, String condicionGeneral,
            String nivelRiesgo, LocalDate proximaEvaluacion,
            BigDecimal pesoKg, BigDecimal alturaM, BigDecimal porcentajeGrasa,
            BigDecimal cinturaCm, BigDecimal pechoCm, BigDecimal brazoCm,
            BigDecimal musloCm, String observacionesMedicion) {
        mensaje = "";
        if (idCliente == null || idEntrenador == null
                || tipoEvaluacion == null || tipoEvaluacion.isBlank()) {
            mensaje = "Faltan datos para la evaluacion.";
            return null;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                String codigo = siguienteCodigoEV(c);
                Long idEvaluacion;
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO evaluacion_fisica (codigo_evaluacion, "
                        + "fecha_evaluacion, tipo_evaluacion, motivo, "
                        + "condicion_general, nivel_riesgo, proxima_evaluacion, "
                        + "estado_evaluacion, id_cliente, id_entrenador) "
                        + "VALUES (?, CURRENT_DATE, ?, ?, ?, ?, ?, "
                        + "'REGISTRADA', ?, ?) "
                        + "RETURNING id_evaluacion")) {
                    s.setString(1, codigo);
                    s.setString(2, tipoEvaluacion.toUpperCase());
                    s.setString(3, motivo);
                    s.setString(4, condicionGeneral);
                    s.setString(5, nivelRiesgo);
                    if (proximaEvaluacion != null) {
                        s.setDate(6, Date.valueOf(proximaEvaluacion));
                    } else {
                        s.setNull(6, java.sql.Types.DATE);
                    }
                    s.setLong(7, idCliente);
                    s.setLong(8, idEntrenador);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        idEvaluacion = r.getLong(1);
                    }
                }
                // Medicion asociada (opcional pero recomendada)
                if (pesoKg != null || alturaM != null || porcentajeGrasa != null
                        || cinturaCm != null || pechoCm != null
                        || brazoCm != null || musloCm != null) {
                    try (PreparedStatement s = c.prepareStatement(
                            "INSERT INTO medicion_corporal (peso_kg, altura_m, "
                            + "porcentaje_grasa, cintura_cm, pecho_cm, "
                            + "brazo_cm, muslo_cm, observaciones, id_evaluacion) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        setDec(s, 1, pesoKg);
                        setDec(s, 2, alturaM);
                        setDec(s, 3, porcentajeGrasa);
                        setDec(s, 4, cinturaCm);
                        setDec(s, 5, pechoCm);
                        setDec(s, 6, brazoCm);
                        setDec(s, 7, musloCm);
                        s.setString(8, observacionesMedicion);
                        s.setLong(9, idEvaluacion);
                        s.executeUpdate();
                    }
                }
                c.commit();
                Auditoria.exito("SALUD", "EVALUACION",
                        "Evaluacion " + codigo + " (id " + idEvaluacion
                        + ") registrada por entrenador " + idEntrenador
                        + " para cliente " + idCliente + ".");
                return idEvaluacion;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo registrar la evaluacion: " + ex.getMessage();
            Auditoria.fallo("SALUD", "EVALUACION", mensaje, ex.getMessage());
            return null;
        }
    }

    /** Agrega una recomendacion asociada a una evaluacion existente. */
    public Long registrarRecomendacion(Long idEvaluacion, String tipo,
            String titulo, String descripcion, String prioridad,
            LocalDate fechaInicio, LocalDate fechaFin) {
        mensaje = "";
        if (idEvaluacion == null || tipo == null || titulo == null
                || descripcion == null || prioridad == null) {
            mensaje = "Datos incompletos para la recomendacion.";
            return null;
        }
        try (Connection c = ConexionPostgreSQL.getConexion();
                PreparedStatement s = c.prepareStatement(
                "INSERT INTO recomendacion (fecha_recomendacion, "
                + "tipo_recomendacion, titulo, descripcion, prioridad, "
                + "fecha_inicio, fecha_fin, estado_recomendacion, id_evaluacion) "
                + "VALUES (CURRENT_DATE, ?, ?, ?, ?, ?, ?, 'ACTIVA', ?) "
                + "RETURNING id_recomendacion")) {
            s.setString(1, tipo);
            s.setString(2, titulo);
            s.setString(3, descripcion);
            s.setString(4, prioridad.toUpperCase());
            if (fechaInicio != null) {
                s.setDate(5, Date.valueOf(fechaInicio));
            } else {
                s.setNull(5, java.sql.Types.DATE);
            }
            if (fechaFin != null) {
                s.setDate(6, Date.valueOf(fechaFin));
            } else {
                s.setNull(6, java.sql.Types.DATE);
            }
            s.setLong(7, idEvaluacion);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                Long id = r.getLong(1);
                Auditoria.exito("SALUD", "RECOMENDACION",
                        "Recomendacion " + id + " ('" + titulo
                        + "') registrada sobre evaluacion " + idEvaluacion);
                return id;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo registrar la recomendacion: " + ex.getMessage();
            Auditoria.fallo("SALUD", "RECOMENDACION", mensaje, ex.getMessage());
            return null;
        }
    }

    private String siguienteCodigoEV(Connection c) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT COALESCE(MAX(CAST(SUBSTRING(codigo_evaluacion FROM 3) "
                + "AS INTEGER)), 0) + 1 FROM evaluacion_fisica")) {
            try (ResultSet r = s.executeQuery()) {
                r.next();
                return String.format("EV%05d", r.getInt(1));
            }
        }
    }

    private void setDec(PreparedStatement s, int idx, BigDecimal v)
            throws SQLException {
        if (v == null) {
            s.setNull(idx, java.sql.Types.NUMERIC);
        } else {
            s.setBigDecimal(idx, v);
        }
    }
}
