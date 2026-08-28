package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import modelo.Ejercicio;

/**
 * DAO para la tabla ejercicio.
 *
 * Los ejercicios pueden ser globales (id_entrenador NULL) o propios de
 * un entrenador. La vista especializada del entrenador trabaja con ambos,
 * pero las actividades creadas por el entrenador quedan asociadas a él.
 */
public class EjercicioDAO {

    private static final String CAMPOS =
            "id_ejercicio, nombre_ejercicio, descripcion, tipo_ejercicio, "
            + "nivel_dificultad, instrucciones, id_entrenador";

    public boolean guardar(Ejercicio ejercicio) throws SQLException {
        String sql = "INSERT INTO ejercicio "
                + "(nombre_ejercicio, descripcion, tipo_ejercicio, "
                + "nivel_dificultad, instrucciones, id_entrenador) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, ejercicio.getNombreEjercicio());
            sentencia.setString(2, ejercicio.getDescripcion());
            sentencia.setString(3, ejercicio.getTipoEjercicio());
            sentencia.setString(4, ejercicio.getNivelDificultad());
            sentencia.setString(5, ejercicio.getInstrucciones());

            if (ejercicio.getIdEntrenador() == null) {
                sentencia.setNull(6, Types.BIGINT);
            } else {
                sentencia.setLong(6, ejercicio.getIdEntrenador());
            }

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(Ejercicio ejercicio) throws SQLException {
        String sql = "UPDATE ejercicio SET nombre_ejercicio = ?, "
                + "descripcion = ?, tipo_ejercicio = ?, nivel_dificultad = ?, "
                + "instrucciones = ?, id_entrenador = ? "
                + "WHERE id_ejercicio = ?";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, ejercicio.getNombreEjercicio());
            sentencia.setString(2, ejercicio.getDescripcion());
            sentencia.setString(3, ejercicio.getTipoEjercicio());
            sentencia.setString(4, ejercicio.getNivelDificultad());
            sentencia.setString(5, ejercicio.getInstrucciones());

            if (ejercicio.getIdEntrenador() == null) {
                sentencia.setNull(6, Types.BIGINT);
            } else {
                sentencia.setLong(6, ejercicio.getIdEntrenador());
            }

            sentencia.setLong(7, ejercicio.getIdEjercicio());
            return sentencia.executeUpdate() > 0;
        }
    }

    public Ejercicio buscar(Long idEjercicio) throws SQLException {
        String sql = "SELECT " + CAMPOS
                + " FROM ejercicio WHERE id_ejercicio = ?";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setLong(1, idEjercicio);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return convertirEjercicio(resultado);
                }
            }
        }

        return null;
    }

    public List<Ejercicio> listar(String criterio) throws SQLException {
        List<Ejercicio> lista = new ArrayList<>();
        String sql = "SELECT " + CAMPOS + " FROM ejercicio "
                + "WHERE nombre_ejercicio ILIKE ? "
                + "OR descripcion ILIKE ? "
                + "OR tipo_ejercicio ILIKE ? "
                + "OR nivel_dificultad ILIKE ? "
                + "OR instrucciones ILIKE ? "
                + "ORDER BY nombre_ejercicio";

        String filtro = "%" + (criterio == null ? "" : criterio.trim()) + "%";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            for (int i = 1; i <= 5; i++) {
                sentencia.setString(i, filtro);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    lista.add(convertirEjercicio(resultado));
                }
            }
        }

        return lista;
    }

    public List<Ejercicio> listarDisponiblesParaEntrenador(
            Long idEntrenador,
            String criterio
    ) throws SQLException {

        List<Ejercicio> lista = new ArrayList<>();
        String sql = "SELECT " + CAMPOS + " FROM ejercicio "
                + "WHERE (id_entrenador = ? OR id_entrenador IS NULL) "
                + "AND (nombre_ejercicio ILIKE ? "
                + "OR descripcion ILIKE ? "
                + "OR tipo_ejercicio ILIKE ? "
                + "OR nivel_dificultad ILIKE ?) "
                + "ORDER BY CASE WHEN id_entrenador = ? THEN 0 ELSE 1 END, "
                + "nombre_ejercicio";

        String filtro = "%" + (criterio == null ? "" : criterio.trim()) + "%";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setLong(1, idEntrenador);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);
            sentencia.setString(4, filtro);
            sentencia.setString(5, filtro);
            sentencia.setLong(6, idEntrenador);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    lista.add(convertirEjercicio(resultado));
                }
            }
        }

        return lista;
    }

    public List<Ejercicio> listarPropiosEntrenador(
            Long idEntrenador,
            String criterio
    ) throws SQLException {

        List<Ejercicio> lista = new ArrayList<>();
        String sql = "SELECT " + CAMPOS + " FROM ejercicio "
                + "WHERE id_entrenador = ? "
                + "AND (nombre_ejercicio ILIKE ? "
                + "OR descripcion ILIKE ? "
                + "OR tipo_ejercicio ILIKE ? "
                + "OR nivel_dificultad ILIKE ?) "
                + "ORDER BY nombre_ejercicio";

        String filtro = "%" + (criterio == null ? "" : criterio.trim()) + "%";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setLong(1, idEntrenador);
            for (int i = 2; i <= 5; i++) {
                sentencia.setString(i, filtro);
            }

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    lista.add(convertirEjercicio(resultado));
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(Long idEjercicio) throws SQLException {
        String sql = "DELETE FROM ejercicio WHERE id_ejercicio = ?";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setLong(1, idEjercicio);
            return sentencia.executeUpdate() > 0;
        }
    }

    private Ejercicio convertirEjercicio(ResultSet resultado)
            throws SQLException {

        Ejercicio ejercicio = new Ejercicio();

        ejercicio.setIdEjercicio(resultado.getLong("id_ejercicio"));
        ejercicio.setNombreEjercicio(resultado.getString("nombre_ejercicio"));
        ejercicio.setDescripcion(resultado.getString("descripcion"));
        ejercicio.setTipoEjercicio(resultado.getString("tipo_ejercicio"));
        ejercicio.setNivelDificultad(resultado.getString("nivel_dificultad"));
        ejercicio.setInstrucciones(resultado.getString("instrucciones"));

        long idEntrenador = resultado.getLong("id_entrenador");
        ejercicio.setIdEntrenador(resultado.wasNull() ? null : idEntrenador);

        return ejercicio;
    }
}
