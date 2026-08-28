package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consultas de solo lectura para el calendario real del cliente. */
public class RutinaClienteDAO {

    public List<Map<String, Object>> listarEjercicios(
            Long idCliente,
            LocalDate fecha
    ) throws SQLException {
        String dia = diaBaseDatos(fecha);
        String sql = "SELECT r.nombre_rutina, e.nombre_ejercicio, "
                + "ce.series, ce.repeticiones, ce.peso_sugerido, "
                + "ce.duracion_minutos, ce.descanso_segundos, ce.orden "
                + "FROM asignacion_rutina ar "
                + "JOIN rutina r ON r.id_rutina = ar.id_rutina "
                + "JOIN contiene_ejercicio ce ON ce.id_rutina = r.id_rutina "
                + "JOIN ejercicio e ON e.id_ejercicio = ce.id_ejercicio "
                + "WHERE ar.id_cliente = ? "
                + "AND ar.estado_asignacion = 'ACTIVA' "
                + "AND r.estado_rutina = 'ACTIVA' "
                + "AND ? BETWEEN ar.fecha_inicio AND COALESCE(ar.fecha_fin, ?) "
                + "AND ce.dia_semana = ? ORDER BY ce.orden";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setObject(1, idCliente);
            sentencia.setObject(2, fecha);
            sentencia.setObject(3, fecha);
            sentencia.setString(4, dia);
            try (ResultSet resultado = sentencia.executeQuery()) {
                List<Map<String, Object>> filas = new ArrayList<>();
                ResultSetMetaData meta = resultado.getMetaData();
                while (resultado.next()) {
                    Map<String, Object> fila = new LinkedHashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        fila.put(meta.getColumnLabel(i), resultado.getObject(i));
                    }
                    filas.add(fila);
                }
                return filas;
            }
        }
    }

    private String diaBaseDatos(LocalDate fecha) {
        return switch (fecha.getDayOfWeek()) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }
}
