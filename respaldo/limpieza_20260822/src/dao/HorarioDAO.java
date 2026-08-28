package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import modelo.Horario;

/**
 * DAO para la tabla horario.
 *
 * @author Usuario
 */
public class HorarioDAO {

    public boolean guardar(
            Horario horario
    ) throws SQLException {

        String sql = "INSERT INTO horario (dia_semana, hora_inicio, hora_fin, duracion_programada, cupo_maximo, estado_horario, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    horario.getDiaSemana()
            );

            sentencia.setTime(
                    2,
                    Time.valueOf(
                            horario.getHoraInicio()
                    )
            );

            sentencia.setTime(
                    3,
                    Time.valueOf(
                            horario.getHoraFin()
                    )
            );

            sentencia.setInt(
                    4,
                    horario.getDuracionProgramada()
            );

            sentencia.setInt(
                    5,
                    horario.getCupoMaximo()
            );

            sentencia.setBoolean(
                    6,
                    horario.isEstadoHorario()
            );

            sentencia.setString(
                    7,
                    horario.getObservaciones()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Horario horario
    ) throws SQLException {

        String sql = "UPDATE horario SET dia_semana = ?, hora_inicio = ?, hora_fin = ?, duracion_programada = ?, cupo_maximo = ?, estado_horario = ?, observaciones = ? WHERE id_horario = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    horario.getDiaSemana()
            );

            sentencia.setTime(
                    2,
                    Time.valueOf(
                            horario.getHoraInicio()
                    )
            );

            sentencia.setTime(
                    3,
                    Time.valueOf(
                            horario.getHoraFin()
                    )
            );

            sentencia.setInt(
                    4,
                    horario.getDuracionProgramada()
            );

            sentencia.setInt(
                    5,
                    horario.getCupoMaximo()
            );

            sentencia.setBoolean(
                    6,
                    horario.isEstadoHorario()
            );

            sentencia.setString(
                    7,
                    horario.getObservaciones()
            );

            sentencia.setLong(
                    8,
                    horario.getIdHorario()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Horario buscar(
            Long idHorario
    ) throws SQLException {

        String sql = "SELECT id_horario, dia_semana, hora_inicio, hora_fin, duracion_programada, cupo_maximo, estado_horario, observaciones FROM horario WHERE id_horario = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idHorario);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirHorario(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Horario> listar(
            String criterio
    ) throws SQLException {

        List<Horario> lista = new ArrayList<>();
        String sql = "SELECT id_horario, dia_semana, hora_inicio, hora_fin, duracion_programada, cupo_maximo, estado_horario, observaciones FROM horario WHERE dia_semana ILIKE ? OR observaciones ILIKE ? OR CAST(estado_horario AS TEXT) ILIKE ? OR CAST(duracion_programada AS TEXT) ILIKE ? ORDER BY id_horario";

        if (criterio == null) {
            criterio = "";
        }

        String filtro = "%" + criterio.trim() + "%";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, filtro);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);
            sentencia.setString(4, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirHorario(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idHorario
    ) throws SQLException {

        String sql = "UPDATE horario SET estado_horario = FALSE WHERE id_horario = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idHorario);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idHorario
    ) throws SQLException {

        String sql = "DELETE FROM horario WHERE id_horario = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idHorario);

            return sentencia.executeUpdate() > 0;
        }
    }

    private Horario convertirHorario(
            ResultSet resultado
    ) throws SQLException {

        Horario horario = new Horario();

        horario.setIdHorario(
                resultado.getLong("id_horario")
        );

        horario.setDiaSemana(
                resultado.getString("dia_semana")
        );

        if (resultado.getTime("hora_inicio") != null) {
            horario.setHoraInicio(
                    resultado.getTime("hora_inicio").toLocalTime()
            );
        }

        if (resultado.getTime("hora_fin") != null) {
            horario.setHoraFin(
                    resultado.getTime("hora_fin").toLocalTime()
            );
        }

        horario.setDuracionProgramada(
                resultado.getInt("duracion_programada")
        );

        horario.setCupoMaximo(
                resultado.getInt("cupo_maximo")
        );

        horario.setEstadoHorario(
                resultado.getBoolean("estado_horario")
        );

        horario.setObservaciones(
                resultado.getString("observaciones")
        );

        return horario;
    }
}
