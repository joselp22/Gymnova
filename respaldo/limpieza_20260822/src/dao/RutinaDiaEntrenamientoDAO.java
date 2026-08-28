package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.RutinaDiaEntrenamiento;

/**
 * DAO para la tabla rutina_dia_entrenamiento.
 *
 * @author Usuario
 */
public class RutinaDiaEntrenamientoDAO {

    public boolean guardar(
            RutinaDiaEntrenamiento rutinaDiaEntrenamiento
    ) throws SQLException {

        String sql = "INSERT INTO rutina_dia_entrenamiento (dia_entrenamiento, id_rutina) VALUES (?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    rutinaDiaEntrenamiento.getDiaEntrenamiento()
            );

            sentencia.setLong(
                    2,
                    rutinaDiaEntrenamiento.getIdRutina()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public RutinaDiaEntrenamiento buscar(
            String diaEntrenamiento,
            Long idRutina
    ) throws SQLException {

        String sql = "SELECT dia_entrenamiento, id_rutina FROM rutina_dia_entrenamiento WHERE dia_entrenamiento = ? AND id_rutina = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, diaEntrenamiento);
            sentencia.setLong(2, idRutina);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirRutinaDiaEntrenamiento(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<RutinaDiaEntrenamiento> listar(
            String criterio
    ) throws SQLException {

        List<RutinaDiaEntrenamiento> lista = new ArrayList<>();
        String sql = "SELECT dia_entrenamiento, id_rutina FROM rutina_dia_entrenamiento WHERE dia_entrenamiento ILIKE ? ORDER BY dia_entrenamiento, id_rutina";

        if (criterio == null) {
            criterio = "";
        }

        String filtro = "%" + criterio.trim() + "%";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirRutinaDiaEntrenamiento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            String diaEntrenamiento,
            Long idRutina
    ) throws SQLException {

        String sql = "DELETE FROM rutina_dia_entrenamiento WHERE dia_entrenamiento = ? AND id_rutina = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, diaEntrenamiento);
            sentencia.setLong(2, idRutina);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<RutinaDiaEntrenamiento> listarPorIdRutina(
            Long idRutina
    ) throws SQLException {

        List<RutinaDiaEntrenamiento> lista = new ArrayList<>();
        String sql = "SELECT dia_entrenamiento, id_rutina FROM rutina_dia_entrenamiento WHERE id_rutina = ? ORDER BY dia_entrenamiento, id_rutina";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idRutina
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirRutinaDiaEntrenamiento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }


    public boolean modificar(
            RutinaDiaEntrenamiento rutinaDiaEntrenamiento
    ) throws SQLException {

        return false;
    }

    private RutinaDiaEntrenamiento convertirRutinaDiaEntrenamiento(
            ResultSet resultado
    ) throws SQLException {

        RutinaDiaEntrenamiento rutinaDiaEntrenamiento = new RutinaDiaEntrenamiento();

        rutinaDiaEntrenamiento.setDiaEntrenamiento(
                resultado.getString("dia_entrenamiento")
        );

        rutinaDiaEntrenamiento.setIdRutina(
                resultado.getLong("id_rutina")
        );

        return rutinaDiaEntrenamiento;
    }
}
