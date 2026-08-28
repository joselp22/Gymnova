package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.ClaseEspecial;

/**
 * DAO para la tabla clase_especial.
 *
 * @author Usuario
 */
public class ClaseEspecialDAO {

    public boolean guardar(
            ClaseEspecial claseEspecial
    ) throws SQLException {

        String sql = "INSERT INTO clase_especial (fecha_clase_especial, estado_programacion, observaciones, id_clase, id_horario, id_entrenador) VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (claseEspecial.getFechaClaseEspecial() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            claseEspecial.getFechaClaseEspecial()
                    )
            );
        }

            sentencia.setString(
                    2,
                    claseEspecial.getEstadoProgramacion()
            );

            sentencia.setString(
                    3,
                    claseEspecial.getObservaciones()
            );

            sentencia.setLong(
                    4,
                    claseEspecial.getIdClase()
            );

            sentencia.setLong(
                    5,
                    claseEspecial.getIdHorario()
            );

            sentencia.setLong(
                    6,
                    claseEspecial.getIdEntrenador()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            ClaseEspecial claseEspecial
    ) throws SQLException {

        String sql = "UPDATE clase_especial SET fecha_clase_especial = ?, estado_programacion = ?, observaciones = ?, id_clase = ?, id_horario = ?, id_entrenador = ? WHERE id_clase_especial = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (claseEspecial.getFechaClaseEspecial() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            claseEspecial.getFechaClaseEspecial()
                    )
            );
        }

            sentencia.setString(
                    2,
                    claseEspecial.getEstadoProgramacion()
            );

            sentencia.setString(
                    3,
                    claseEspecial.getObservaciones()
            );

            sentencia.setLong(
                    4,
                    claseEspecial.getIdClase()
            );

            sentencia.setLong(
                    5,
                    claseEspecial.getIdHorario()
            );

            sentencia.setLong(
                    6,
                    claseEspecial.getIdEntrenador()
            );

            sentencia.setLong(
                    7,
                    claseEspecial.getIdClaseEspecial()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public ClaseEspecial buscar(
            Long idClaseEspecial
    ) throws SQLException {

        String sql = "SELECT id_clase_especial, fecha_clase_especial, estado_programacion, observaciones, id_clase, id_horario, id_entrenador FROM clase_especial WHERE id_clase_especial = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idClaseEspecial);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirClaseEspecial(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<ClaseEspecial> listar(
            String criterio
    ) throws SQLException {

        List<ClaseEspecial> lista = new ArrayList<>();
        String sql = "SELECT id_clase_especial, fecha_clase_especial, estado_programacion, observaciones, id_clase, id_horario, id_entrenador FROM clase_especial WHERE estado_programacion ILIKE ? OR observaciones ILIKE ? ORDER BY id_clase_especial";

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

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirClaseEspecial(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idClaseEspecial
    ) throws SQLException {

        String sql = "UPDATE clase_especial SET estado_programacion = 'CANCELADA' WHERE id_clase_especial = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idClaseEspecial);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idClaseEspecial
    ) throws SQLException {

        String sql = "DELETE FROM clase_especial WHERE id_clase_especial = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idClaseEspecial);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<ClaseEspecial> listarPorIdClase(
            Long idClase
    ) throws SQLException {

        List<ClaseEspecial> lista = new ArrayList<>();
        String sql = "SELECT id_clase_especial, fecha_clase_especial, estado_programacion, observaciones, id_clase, id_horario, id_entrenador FROM clase_especial WHERE id_clase = ? ORDER BY id_clase_especial";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idClase
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirClaseEspecial(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<ClaseEspecial> listarPorIdHorario(
            Long idHorario
    ) throws SQLException {

        List<ClaseEspecial> lista = new ArrayList<>();
        String sql = "SELECT id_clase_especial, fecha_clase_especial, estado_programacion, observaciones, id_clase, id_horario, id_entrenador FROM clase_especial WHERE id_horario = ? ORDER BY id_clase_especial";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idHorario
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirClaseEspecial(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<ClaseEspecial> listarPorIdEntrenador(
            Long idEntrenador
    ) throws SQLException {

        List<ClaseEspecial> lista = new ArrayList<>();
        String sql = "SELECT id_clase_especial, fecha_clase_especial, estado_programacion, observaciones, id_clase, id_horario, id_entrenador FROM clase_especial WHERE id_entrenador = ? ORDER BY id_clase_especial";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEntrenador
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirClaseEspecial(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private ClaseEspecial convertirClaseEspecial(
            ResultSet resultado
    ) throws SQLException {

        ClaseEspecial claseEspecial = new ClaseEspecial();

        claseEspecial.setIdClaseEspecial(
                resultado.getLong("id_clase_especial")
        );

        claseEspecial.setFechaClaseEspecial(
                resultado.getDate("fecha_clase_especial") == null
                ? null
                : resultado.getDate("fecha_clase_especial").toLocalDate()
        );

        claseEspecial.setEstadoProgramacion(
                resultado.getString("estado_programacion")
        );

        claseEspecial.setObservaciones(
                resultado.getString("observaciones")
        );

        claseEspecial.setIdClase(
                resultado.getLong("id_clase")
        );

        claseEspecial.setIdHorario(
                resultado.getLong("id_horario")
        );

        claseEspecial.setIdEntrenador(
                resultado.getLong("id_entrenador")
        );

        return claseEspecial;
    }
}
