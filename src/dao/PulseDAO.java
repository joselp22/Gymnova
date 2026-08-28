package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.AlertaPulse;

/** Consultas reales necesarias para calcular GYMNOVA Pulse. */
public class PulseDAO {

    public List<AlertaPulse> listar() throws SQLException {
        String sql = "SELECT c.id_persona, c.codigo_cliente, "
                + "p.nombres || ' ' || p.apellidos nombre, "
                + "COALESCE(CURRENT_DATE - MAX(a.fecha_asistencia), 999) dias_sin_asistir, "
                + "COUNT(a.id_asistencia) FILTER (WHERE a.fecha_asistencia >= CURRENT_DATE - 30) asistencias_30, "
                + "COALESCE(m.estado_membresia, 'SIN_MEMBRESIA') estado_membresia, "
                + "COALESCE(m.fecha_fin - CURRENT_DATE, -999) dias_membresia, "
                + "COALESCE(pr.planificadas, 0) planificadas, "
                + "COALESCE(pr.completadas, 0) completadas, "
                + "COALESCE(ar.rutinas_activas, 0) rutinas_activas, "
                + "COALESCE(rv.cancelaciones, 0) cancelaciones "
                + "FROM cliente c JOIN persona p ON p.id_persona=c.id_persona "
                + "LEFT JOIN asistencia a ON a.id_cliente=c.id_persona "
                + "LEFT JOIN LATERAL (SELECT estado_membresia, fecha_fin FROM membresia "
                + " WHERE id_cliente=c.id_persona ORDER BY fecha_fin DESC LIMIT 1) m ON TRUE "
                + "LEFT JOIN LATERAL (SELECT SUM(sesiones_planificadas) planificadas, "
                + " SUM(sesiones_completadas) completadas FROM progreso_rutina "
                + " WHERE id_cliente=c.id_persona AND fecha_registro >= CURRENT_DATE - 30) pr ON TRUE "
                + "LEFT JOIN LATERAL (SELECT COUNT(*) rutinas_activas FROM asignacion_rutina "
                + " WHERE id_cliente=c.id_persona AND estado_asignacion='ACTIVA') ar ON TRUE "
                + "LEFT JOIN LATERAL (SELECT COUNT(*) cancelaciones FROM reserva "
                + " WHERE id_cliente=c.id_persona AND estado_reserva='CANCELADA' "
                + " AND fecha_hora_reserva >= CURRENT_DATE - INTERVAL '30 days') rv ON TRUE "
                + "WHERE c.estado_cliente=TRUE GROUP BY c.id_persona, c.codigo_cliente, "
                + "p.nombres, p.apellidos, m.estado_membresia, m.fecha_fin, "
                + "pr.planificadas, pr.completadas, ar.rutinas_activas, rv.cancelaciones "
                + "ORDER BY c.id_persona";

        List<AlertaPulse> resultado = new ArrayList<>();
        try (Connection cn = ConexionPostgreSQL.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(calcular(rs));
            }
        }
        return resultado;
    }

    private AlertaPulse calcular(ResultSet rs) throws SQLException {
        AlertaPulse alerta = new AlertaPulse();
        alerta.setIdCliente(rs.getLong("id_persona"));
        alerta.setCodigoCliente(rs.getString("codigo_cliente"));
        alerta.setNombreCliente(rs.getString("nombre"));
        int puntos = 0;
        int dias = rs.getInt("dias_sin_asistir");
        if (dias >= 14) {
            puntos += 35;
            alerta.getRazones().add(dias >= 999
                    ? "No registra asistencias." : "Lleva " + dias + " dias sin asistir.");
        } else if (dias >= 7) {
            puntos += 20;
            alerta.getRazones().add("Lleva " + dias + " dias sin asistir.");
        }

        String estado = rs.getString("estado_membresia");
        int diasMembresia = rs.getInt("dias_membresia");
        if (!"ACTIVA".equalsIgnoreCase(estado) || diasMembresia < 0) {
            puntos += 25;
            alerta.getRazones().add("No posee una membresia activa.");
        } else if (diasMembresia <= 7) {
            puntos += 15;
            alerta.getRazones().add("Su membresia vence en " + diasMembresia + " dias.");
        }

        int planificadas = rs.getInt("planificadas");
        int completadas = rs.getInt("completadas");
        if (planificadas > 0) {
            int adherencia = Math.min(100,
                    (int) Math.round(completadas * 100.0 / planificadas));
            if (adherencia < 40) {
                puntos += 25;
                alerta.getRazones().add("Completo solo el " + adherencia + "% de sus sesiones.");
            } else if (adherencia < 70) {
                puntos += 12;
                alerta.getRazones().add("Su adherencia a la rutina es " + adherencia + "%.");
            }
        } else {
            puntos += 15;
            alerta.getRazones().add("No tiene progreso registrado este mes.");
        }

        if (rs.getInt("rutinas_activas") == 0) {
            puntos += 15;
            alerta.getRazones().add("No tiene una rutina activa.");
        }
        int cancelaciones = rs.getInt("cancelaciones");
        if (cancelaciones >= 2) {
            puntos += 10;
            alerta.getRazones().add("Cancelo " + cancelaciones + " reservas este mes.");
        }

        puntos = Math.min(100, puntos);
        alerta.setPuntuacion(puntos);
        alerta.setNivel(puntos >= 70 ? "CRITICO"
                : puntos >= 40 ? "ATENCION" : "ESTABLE");
        return alerta;
    }
}
