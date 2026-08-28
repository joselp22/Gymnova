package controlador;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import utilidades.Auditoria;

/**
 * Cierre transaccional del flujo del Nutricionista:
 *   ver clientes -> seleccionar -> crear plan -> agregar alimentos ->
 *   registrar indicadores -> recomendar.
 * Impide multiples planes ACTIVOS por cliente. Trabaja con BigDecimal.
 */
public class FlujoNutricionistaControlador {

    private String mensaje = "";

    public String getMensaje() {
        return mensaje;
    }

    /**
     * Crea un plan nutricional para un cliente. Si el cliente tiene otro
     * plan ACTIVO, lo finaliza automaticamente.
     */
    public Long crearPlanNutricional(Long idCliente, Long idNutricionista,
            String nombrePlan, LocalDate fechaInicio, LocalDate fechaFin,
            Integer caloriasObjetivo, BigDecimal proteinasG,
            BigDecimal carbohidratosG, String restricciones) {
        mensaje = "";
        if (idCliente == null || idNutricionista == null
                || nombrePlan == null || nombrePlan.isBlank()
                || fechaInicio == null) {
            mensaje = "Datos incompletos para el plan.";
            return null;
        }
        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            mensaje = "La fecha fin no puede ser anterior al inicio.";
            return null;
        }
        try (Connection c = ConexionPostgreSQL.getConexion()) {
            c.setAutoCommit(false);
            try {
                // El nutricionista debe existir como subtipo de empleado.
                // Si falta la fila en `nutricionista` pero SÍ existe la de
                // `empleado`, la creamos aquí mismo con datos por defecto
                // para no romper el flujo. Solo bloqueamos si tampoco hay
                // empleado (ahí el Administrador debe darlo de alta).
                if (!existePerfilNutricionista(c, idNutricionista)) {
                    if (!existeEmpleado(c, idNutricionista)) {
                        c.rollback();
                        mensaje = "Tu usuario tiene rol NUTRICIONISTA pero "
                                + "no está registrado como empleado en la BD. "
                                + "Pide al Administrador que te dé de alta "
                                + "desde el módulo Personal.";
                        return null;
                    }
                    try (PreparedStatement s = c.prepareStatement(
                            "INSERT INTO nutricionista ("
                            + "id_persona, numero_licencia, "
                            + "fecha_inicio_profesion, estado_licencia) "
                            + "VALUES (?, ?, CURRENT_DATE, 'ACTIVA')")) {
                        s.setLong(1, idNutricionista);
                        s.setString(2, "LIC-"
                                + String.format("%06d", idNutricionista));
                        s.executeUpdate();
                    }
                    Auditoria.exito("NUTRICION", "AUTO_ALTA_NUTRICIONISTA",
                            "Se creó automáticamente el subtipo nutricionista "
                            + "para persona " + idNutricionista + ".");
                }
                // Finaliza planes ACTIVOS previos del cliente
                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE plan_nutricional SET estado_plan = 'FINALIZADO', "
                        + "fecha_fin = COALESCE(fecha_fin, CURRENT_DATE) "
                        + "WHERE id_cliente = ? AND estado_plan = 'ACTIVO'")) {
                    s.setLong(1, idCliente);
                    s.executeUpdate();
                }
                String codigo = siguienteCodigoPN(c);
                Long idPlan;
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO plan_nutricional (codigo_plan, "
                        + "nombre_plan, fecha_creacion, fecha_inicio, "
                        + "fecha_fin, calorias_objetivo, proteinas_objetivo_g, "
                        + "carbohidratos_objetivo_g, restricciones_generales, "
                        + "estado_plan, id_cliente, id_nutricionista) "
                        + "VALUES (?, ?, CURRENT_DATE, ?, ?, ?, ?, ?, ?, "
                        + "'ACTIVO', ?, ?) "
                        + "RETURNING id_plan_nutricional")) {
                    s.setString(1, codigo);
                    s.setString(2, nombrePlan);
                    s.setDate(3, Date.valueOf(fechaInicio));
                    if (fechaFin != null) {
                        s.setDate(4, Date.valueOf(fechaFin));
                    } else {
                        s.setNull(4, java.sql.Types.DATE);
                    }
                    if (caloriasObjetivo != null) {
                        s.setInt(5, caloriasObjetivo);
                    } else {
                        s.setNull(5, java.sql.Types.INTEGER);
                    }
                    setDec(s, 6, proteinasG);
                    setDec(s, 7, carbohidratosG);
                    s.setString(8, restricciones);
                    s.setLong(9, idCliente);
                    s.setLong(10, idNutricionista);
                    try (ResultSet r = s.executeQuery()) {
                        r.next();
                        idPlan = r.getLong(1);
                    }
                }
                c.commit();
                Auditoria.exito("NUTRICION", "CREAR_PLAN",
                        "Plan " + codigo + " (id " + idPlan + ") creado "
                        + "por nutricionista " + idNutricionista
                        + " para cliente " + idCliente + ".");
                return idPlan;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo crear el plan: " + ex.getMessage();
            Auditoria.fallo("NUTRICION", "CREAR_PLAN", mensaje,
                    ex.getMessage());
            return null;
        }
    }

    /** Agrega una comida al plan (dia + tipo + alimento + cantidad). */
    public Long agregarComidaAlPlan(Long idPlan, String diaSemana,
            String tipoComida, LocalTime horaConsumo, BigDecimal cantidad,
            String unidadMedida, Integer ordenComida, Integer idAlimento,
            String indicaciones) {
        mensaje = "";
        if (idPlan == null || diaSemana == null || tipoComida == null
                || cantidad == null || unidadMedida == null
                || idAlimento == null || ordenComida == null) {
            mensaje = "Datos incompletos para la comida.";
            return null;
        }
        if (cantidad.signum() <= 0) {
            mensaje = "La cantidad debe ser mayor que cero.";
            return null;
        }
        try (Connection c = ConexionPostgreSQL.getConexion();
                PreparedStatement s = c.prepareStatement(
                "INSERT INTO incluye_alimento (dia_semana, tipo_comida, "
                + "hora_consumo, cantidad, unidad_medida, orden_comida, "
                + "indicaciones, estado_detalle, id_plan_nutricional, "
                + "id_alimento) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVO', ?, ?) "
                + "RETURNING id_detalle_plan")) {
            s.setString(1, diaSemana.toUpperCase());
            s.setString(2, tipoComida.toUpperCase());
            if (horaConsumo != null) {
                s.setTime(3, Time.valueOf(horaConsumo));
            } else {
                s.setNull(3, java.sql.Types.TIME);
            }
            s.setBigDecimal(4, cantidad);
            s.setString(5, unidadMedida);
            s.setInt(6, ordenComida);
            s.setString(7, indicaciones);
            s.setLong(8, idPlan);
            s.setInt(9, idAlimento);
            try (ResultSet r = s.executeQuery()) {
                r.next();
                Long id = r.getLong(1);
                Auditoria.exito("NUTRICION", "AGREGAR_ALIMENTO",
                        "Alimento " + idAlimento + " agregado al plan "
                        + idPlan + " (" + cantidad + " " + unidadMedida
                        + ") el " + diaSemana + " en " + tipoComida);
                return id;
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo agregar la comida: " + ex.getMessage();
            Auditoria.fallo("NUTRICION", "AGREGAR_ALIMENTO", mensaje,
                    ex.getMessage());
            return null;
        }
    }

    /** Finaliza un plan nutricional activo. */
    public boolean finalizarPlan(Long idPlan) {
        mensaje = "";
        try (Connection c = ConexionPostgreSQL.getConexion();
                PreparedStatement s = c.prepareStatement(
                "UPDATE plan_nutricional SET estado_plan = 'FINALIZADO', "
                + "fecha_fin = COALESCE(fecha_fin, CURRENT_DATE) "
                + "WHERE id_plan_nutricional = ?")) {
            s.setLong(1, idPlan);
            boolean ok = s.executeUpdate() > 0;
            if (ok) {
                Auditoria.exito("NUTRICION", "FINALIZAR_PLAN",
                        "Plan " + idPlan + " finalizado.");
            }
            return ok;
        } catch (SQLException ex) {
            mensaje = "No se pudo finalizar: " + ex.getMessage();
            Auditoria.fallo("NUTRICION", "FINALIZAR_PLAN", mensaje,
                    ex.getMessage());
            return false;
        }
    }

    /** Registra un resultado de indicador y calcula automaticamente si esta fuera de rango. */
    public Long registrarResultadoIndicador(Long idEvaluacion,
            Integer idIndicador, BigDecimal valor, String clasificacion,
            boolean fueraDeRangoIgnorado, String observaciones) {
        mensaje = "";
        if (idEvaluacion == null || idIndicador == null || valor == null) {
            mensaje = "Datos incompletos.";
            return null;
        }

        try (Connection c = ConexionPostgreSQL.getConexion()) {

            BigDecimal minimo = null;
            BigDecimal maximo = null;

            try (PreparedStatement s = c.prepareStatement(
                    "SELECT valor_minimo_referencia, valor_maximo_referencia "
                    + "FROM indicador_salud "
                    + "WHERE id_indicador = ? AND estado_indicador = TRUE")) {
                s.setInt(1, idIndicador);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) {
                        mensaje = "El indicador no existe o esta inactivo.";
                        return null;
                    }
                    minimo = r.getBigDecimal("valor_minimo_referencia");
                    maximo = r.getBigDecimal("valor_maximo_referencia");
                }
            }

            boolean fueraDeRango =
                    (minimo != null && valor.compareTo(minimo) < 0)
                    || (maximo != null && valor.compareTo(maximo) > 0);

            try (PreparedStatement s = c.prepareStatement(
                    "INSERT INTO resultado_indicador (valor_obtenido, "
                    + "clasificacion, fuera_de_rango, observaciones, "
                    + "fecha_registro, id_evaluacion, id_indicador) "
                    + "VALUES (?, ?, ?, ?, CURRENT_DATE, ?, ?) "
                    + "RETURNING id_resultado")) {
                s.setBigDecimal(1, valor);
                s.setString(2, clasificacion);
                s.setBoolean(3, fueraDeRango);
                s.setString(4, observaciones);
                s.setLong(5, idEvaluacion);
                s.setInt(6, idIndicador);

                try (ResultSet r = s.executeQuery()) {
                    r.next();
                    Long id = r.getLong(1);
                    Auditoria.exito("SALUD", "RESULTADO_INDICADOR",
                            "Resultado " + id + " (valor " + valor
                            + ", fuera de rango=" + fueraDeRango
                            + ") registrado sobre evaluacion " + idEvaluacion);
                    return id;
                }
            }
        } catch (SQLException ex) {
            mensaje = "No se pudo registrar el resultado: " + ex.getMessage();
            Auditoria.fallo("SALUD", "RESULTADO_INDICADOR", mensaje,
                    ex.getMessage());
            return null;
        }
    }

    private boolean existePerfilNutricionista(Connection c, long idPersona)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT 1 FROM nutricionista WHERE id_persona = ?")) {
            s.setLong(1, idPersona);
            try (ResultSet r = s.executeQuery()) {
                return r.next();
            }
        }
    }

    private boolean existeEmpleado(Connection c, long idPersona)
            throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT 1 FROM empleado WHERE id_persona = ?")) {
            s.setLong(1, idPersona);
            try (ResultSet r = s.executeQuery()) {
                return r.next();
            }
        }
    }

    private String siguienteCodigoPN(Connection c) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT COALESCE(MAX(CAST(SUBSTRING(codigo_plan FROM 3) "
                + "AS INTEGER)), 0) + 1 FROM plan_nutricional")) {
            try (ResultSet r = s.executeQuery()) {
                r.next();
                return String.format("PN%05d", r.getInt(1));
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
