package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.RutinaDetalleAdministrador;
import modelo.RutinaResumenAdministrador;

/**
 * Consultas de solo lectura para la supervisión administrativa de rutinas.
 */
public class RutinaAdministracionDAO {

    public List<RutinaResumenAdministrador> listar(String criterio)
            throws SQLException {

        String filtro = "%" + (criterio == null ? "" : criterio.trim()) + "%";

        String sql = """
                SELECT
                    r.id_rutina,
                    r.nombre_rutina,
                    TRIM(COALESCE(p.nombres, '') || ' ' || COALESCE(p.apellidos, '')) AS entrenador,
                    r.nivel,
                    r.duracion_semanas,
                    r.fecha_creacion,
                    r.estado_rutina,
                    COUNT(ce.id_rutina_ejercicio) AS total_ejercicios
                FROM rutina r
                INNER JOIN entrenador en
                    ON en.id_persona = r.id_entrenador
                INNER JOIN persona p
                    ON p.id_persona = en.id_persona
                LEFT JOIN contiene_ejercicio ce
                    ON ce.id_rutina = r.id_rutina
                WHERE CAST(r.id_rutina AS TEXT) ILIKE ?
                   OR r.nombre_rutina ILIKE ?
                   OR p.nombres ILIKE ?
                   OR p.apellidos ILIKE ?
                   OR r.nivel ILIKE ?
                   OR r.estado_rutina ILIKE ?
                GROUP BY
                    r.id_rutina,
                    r.nombre_rutina,
                    p.nombres,
                    p.apellidos,
                    r.nivel,
                    r.duracion_semanas,
                    r.fecha_creacion,
                    r.estado_rutina
                ORDER BY r.id_rutina DESC
                """;

        List<RutinaResumenAdministrador> lista = new ArrayList<>();

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            for (int i = 1; i <= 6; i++) {
                sentencia.setString(i, filtro);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    RutinaResumenAdministrador item = new RutinaResumenAdministrador();
                    item.setIdRutina(resultado.getLong("id_rutina"));
                    item.setNombreRutina(resultado.getString("nombre_rutina"));
                    item.setNombreEntrenador(resultado.getString("entrenador"));
                    item.setNivel(resultado.getString("nivel"));
                    item.setDuracionSemanas(resultado.getInt("duracion_semanas"));
                    Date fecha = resultado.getDate("fecha_creacion");
                    item.setFechaCreacion(fecha == null ? null : fecha.toLocalDate());
                    item.setEstadoRutina(resultado.getString("estado_rutina"));
                    item.setTotalEjercicios(resultado.getInt("total_ejercicios"));
                    lista.add(item);
                }
            }
        }

        return lista;
    }

    public List<RutinaDetalleAdministrador> listarDetalle(Long idRutina)
            throws SQLException {

        String sql = """
                SELECT
                    ce.dia_semana,
                    ce.orden,
                    e.nombre_ejercicio,
                    e.tipo_ejercicio,
                    e.nivel_dificultad,
                    ce.series,
                    ce.repeticiones,
                    ce.peso_sugerido,
                    ce.duracion_minutos,
                    ce.descanso_segundos
                FROM contiene_ejercicio ce
                INNER JOIN ejercicio e
                    ON e.id_ejercicio = ce.id_ejercicio
                WHERE ce.id_rutina = ?
                ORDER BY
                    CASE UPPER(ce.dia_semana)
                        WHEN 'LUNES' THEN 1
                        WHEN 'MARTES' THEN 2
                        WHEN 'MIERCOLES' THEN 3
                        WHEN 'MIÉRCOLES' THEN 3
                        WHEN 'JUEVES' THEN 4
                        WHEN 'VIERNES' THEN 5
                        WHEN 'SABADO' THEN 6
                        WHEN 'SÁBADO' THEN 6
                        WHEN 'DOMINGO' THEN 7
                        ELSE 8
                    END,
                    ce.orden,
                    ce.id_rutina_ejercicio
                """;

        List<RutinaDetalleAdministrador> lista = new ArrayList<>();

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setLong(1, idRutina);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    RutinaDetalleAdministrador item = new RutinaDetalleAdministrador();
                    item.setDiaSemana(resultado.getString("dia_semana"));
                    item.setOrden(resultado.getInt("orden"));
                    item.setNombreEjercicio(resultado.getString("nombre_ejercicio"));
                    item.setTipoEjercicio(resultado.getString("tipo_ejercicio"));
                    item.setNivelDificultad(resultado.getString("nivel_dificultad"));
                    item.setSeries(resultado.getInt("series"));
                    item.setRepeticiones(resultado.getString("repeticiones"));
                    item.setPesoSugerido(resultado.getBigDecimal("peso_sugerido"));
                    item.setDuracionMinutos((Integer) resultado.getObject("duracion_minutos"));
                    item.setDescansoSegundos((Integer) resultado.getObject("descanso_segundos"));
                    lista.add(item);
                }
            }
        }

        return lista;
    }
}
