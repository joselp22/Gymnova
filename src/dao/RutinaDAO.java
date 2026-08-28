package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Rutina;

/**
 * DAO para la tabla rutina.
 *
 * @author Usuario
 */
public class RutinaDAO {

    public boolean guardar(
            Rutina rutina
    ) throws SQLException {

        String sql = "INSERT INTO rutina (nombre_rutina, descripcion, nivel, duracion_semanas, fecha_creacion, estado_rutina, id_entrenador) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    rutina.getNombreRutina()
            );

            sentencia.setString(
                    2,
                    rutina.getDescripcion()
            );

            sentencia.setString(
                    3,
                    rutina.getNivel()
            );

            sentencia.setInt(
                    4,
                    rutina.getDuracionSemanas()
            );

            if (rutina.getFechaCreacion() == null) {
            sentencia.setDate(
                    5,
                    null
            );
        } else {
            sentencia.setDate(
                    5,
                    Date.valueOf(
                            rutina.getFechaCreacion()
                    )
            );
        }

            sentencia.setString(
                    6,
                    rutina.getEstadoRutina()
            );

            sentencia.setLong(
                    7,
                    rutina.getIdEntrenador()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Rutina rutina
    ) throws SQLException {

        String sql = "UPDATE rutina SET nombre_rutina = ?, descripcion = ?, nivel = ?, duracion_semanas = ?, fecha_creacion = ?, estado_rutina = ?, id_entrenador = ? WHERE id_rutina = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    rutina.getNombreRutina()
            );

            sentencia.setString(
                    2,
                    rutina.getDescripcion()
            );

            sentencia.setString(
                    3,
                    rutina.getNivel()
            );

            sentencia.setInt(
                    4,
                    rutina.getDuracionSemanas()
            );

            if (rutina.getFechaCreacion() == null) {
            sentencia.setDate(
                    5,
                    null
            );
        } else {
            sentencia.setDate(
                    5,
                    Date.valueOf(
                            rutina.getFechaCreacion()
                    )
            );
        }

            sentencia.setString(
                    6,
                    rutina.getEstadoRutina()
            );

            sentencia.setLong(
                    7,
                    rutina.getIdEntrenador()
            );

            sentencia.setLong(
                    8,
                    rutina.getIdRutina()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Rutina buscar(
            Long idRutina
    ) throws SQLException {

        String sql = "SELECT id_rutina, nombre_rutina, descripcion, nivel, duracion_semanas, fecha_creacion, estado_rutina, id_entrenador FROM rutina WHERE id_rutina = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idRutina);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirRutina(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Rutina> listar(
            String criterio
    ) throws SQLException {

        List<Rutina> lista = new ArrayList<>();
        String sql = "SELECT id_rutina, nombre_rutina, descripcion, nivel, duracion_semanas, fecha_creacion, estado_rutina, id_entrenador FROM rutina WHERE nombre_rutina ILIKE ? OR descripcion ILIKE ? OR nivel ILIKE ? OR estado_rutina ILIKE ? ORDER BY id_rutina";

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
                            convertirRutina(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idRutina
    ) throws SQLException {

        String sql = "UPDATE rutina SET estado_rutina = 'INACTIVA' WHERE id_rutina = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idRutina);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idRutina
    ) throws SQLException {

        String sql = "DELETE FROM rutina WHERE id_rutina = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idRutina);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Rutina> listarPorIdEntrenador(
            Long idEntrenador
    ) throws SQLException {

        List<Rutina> lista = new ArrayList<>();
        String sql = "SELECT id_rutina, nombre_rutina, descripcion, nivel, duracion_semanas, fecha_creacion, estado_rutina, id_entrenador FROM rutina WHERE id_entrenador = ? ORDER BY id_rutina";

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
                            convertirRutina(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    private Rutina convertirRutina(
            ResultSet resultado
    ) throws SQLException {

        Rutina rutina = new Rutina();

        rutina.setIdRutina(
                resultado.getLong("id_rutina")
        );

        rutina.setNombreRutina(
                resultado.getString("nombre_rutina")
        );

        rutina.setDescripcion(
                resultado.getString("descripcion")
        );

        rutina.setNivel(
                resultado.getString("nivel")
        );

        rutina.setDuracionSemanas(
                resultado.getInt("duracion_semanas")
        );

        rutina.setFechaCreacion(
                resultado.getDate("fecha_creacion") == null
                ? null
                : resultado.getDate("fecha_creacion").toLocalDate()
        );

        rutina.setEstadoRutina(
                resultado.getString("estado_rutina")
        );

        rutina.setIdEntrenador(
                resultado.getLong("id_entrenador")
        );

        return rutina;
    }
}
