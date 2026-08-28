package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import modelo.ClaseGrupal;

/**
 * DAO para la tabla clase_grupal.
 */
public class ClaseGrupalDAO {

    private static final String CAMPOS =
            "id_clase, nombre_clase, descripcion, nivel, "
            + "duracion_base_minutos, intensidad, id_entrenador, "
            + "cupo_maximo, fecha_hora, estado_clase";

    public boolean guardar(ClaseGrupal claseGrupal) throws SQLException {

        // Devolvemos el id generado por la BD y lo asignamos al modelo
        // para que quien llame pueda encadenar operaciones (por ejemplo,
        // registrar la reserva individual de la clase recién creada).
        String sql = "INSERT INTO clase_grupal "
                + "(nombre_clase, descripcion, nivel, duracion_base_minutos, "
                + "intensidad, id_entrenador, cupo_maximo, fecha_hora, estado_clase) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_clase";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, claseGrupal.getNombreClase());
            sentencia.setString(2, claseGrupal.getDescripcion());
            sentencia.setString(3, claseGrupal.getNivel());
            sentencia.setInt(4, claseGrupal.getDuracionBaseMinutos());
            sentencia.setString(5, claseGrupal.getIntensidad());
            sentencia.setLong(6, claseGrupal.getIdEntrenador());
            sentencia.setInt(7, claseGrupal.getCupoMaximo());
            sentencia.setTimestamp(8, Timestamp.valueOf(claseGrupal.getFechaHora()));
            sentencia.setBoolean(9, claseGrupal.isEstadoClase());

            try (ResultSet r = sentencia.executeQuery()) {
                if (r.next()) {
                    claseGrupal.setIdClase(r.getLong(1));
                    return true;
                }
                return false;
            }
        }
    }

    public boolean modificar(ClaseGrupal claseGrupal) throws SQLException {

        String sql = "UPDATE clase_grupal SET nombre_clase = ?, descripcion = ?, "
                + "nivel = ?, duracion_base_minutos = ?, intensidad = ?, "
                + "id_entrenador = ?, cupo_maximo = ?, fecha_hora = ?, "
                + "estado_clase = ? WHERE id_clase = ?";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, claseGrupal.getNombreClase());
            sentencia.setString(2, claseGrupal.getDescripcion());
            sentencia.setString(3, claseGrupal.getNivel());
            sentencia.setInt(4, claseGrupal.getDuracionBaseMinutos());
            sentencia.setString(5, claseGrupal.getIntensidad());
            sentencia.setLong(6, claseGrupal.getIdEntrenador());
            sentencia.setInt(7, claseGrupal.getCupoMaximo());
            sentencia.setTimestamp(8, Timestamp.valueOf(claseGrupal.getFechaHora()));
            sentencia.setBoolean(9, claseGrupal.isEstadoClase());
            sentencia.setLong(10, claseGrupal.getIdClase());

            return sentencia.executeUpdate() > 0;
        }
    }

    public ClaseGrupal buscar(Long idClase) throws SQLException {

        String sql = "SELECT " + CAMPOS + " FROM clase_grupal WHERE id_clase = ?";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setLong(1, idClase);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return convertirClaseGrupal(resultado);
                }
            }
        }
        return null;
    }

    public List<ClaseGrupal> listar(String criterio) throws SQLException {

        List<ClaseGrupal> lista = new ArrayList<>();
        String sql = "SELECT " + CAMPOS + " FROM clase_grupal "
                + "WHERE nombre_clase ILIKE ? OR descripcion ILIKE ? "
                + "OR nivel ILIKE ? OR intensidad ILIKE ? "
                + "ORDER BY fecha_hora, id_clase";

        if (criterio == null) {
            criterio = "";
        }

        String filtro = "%" + criterio.trim() + "%";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, filtro);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);
            sentencia.setString(4, filtro);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    lista.add(convertirClaseGrupal(resultado));
                }
            }
        }
        return lista;
    }

    public boolean desactivar(Long idClase) throws SQLException {
        String sql = "UPDATE clase_grupal SET estado_clase = FALSE WHERE id_clase = ?";
        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idClase);
            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(Long idClase) throws SQLException {
        String sql = "DELETE FROM clase_grupal WHERE id_clase = ?";
        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idClase);
            return sentencia.executeUpdate() > 0;
        }
    }

    private ClaseGrupal convertirClaseGrupal(ResultSet resultado) throws SQLException {

        ClaseGrupal claseGrupal = new ClaseGrupal();

        claseGrupal.setIdClase(resultado.getLong("id_clase"));
        claseGrupal.setNombreClase(resultado.getString("nombre_clase"));
        claseGrupal.setDescripcion(resultado.getString("descripcion"));
        claseGrupal.setNivel(resultado.getString("nivel"));
        claseGrupal.setDuracionBaseMinutos(resultado.getInt("duracion_base_minutos"));
        claseGrupal.setIntensidad(resultado.getString("intensidad"));

        long idEntrenador = resultado.getLong("id_entrenador");
        claseGrupal.setIdEntrenador(resultado.wasNull() ? null : idEntrenador);

        int cupo = resultado.getInt("cupo_maximo");
        claseGrupal.setCupoMaximo(resultado.wasNull() ? null : cupo);

        Timestamp fechaHora = resultado.getTimestamp("fecha_hora");
        claseGrupal.setFechaHora(fechaHora == null ? null : fechaHora.toLocalDateTime());

        claseGrupal.setEstadoClase(resultado.getBoolean("estado_clase"));

        return claseGrupal;
    }
}
