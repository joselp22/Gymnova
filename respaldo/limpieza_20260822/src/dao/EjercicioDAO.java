package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Ejercicio;

/**
 * DAO para la tabla ejercicio.
 *
 * @author Usuario
 */
public class EjercicioDAO {

    public boolean guardar(
            Ejercicio ejercicio
    ) throws SQLException {

        String sql = "INSERT INTO ejercicio (nombre_ejercicio, descripcion, tipo_ejercicio, nivel_dificultad, instrucciones) VALUES (?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    ejercicio.getNombreEjercicio()
            );

            sentencia.setString(
                    2,
                    ejercicio.getDescripcion()
            );

            sentencia.setString(
                    3,
                    ejercicio.getTipoEjercicio()
            );

            sentencia.setString(
                    4,
                    ejercicio.getNivelDificultad()
            );

            sentencia.setString(
                    5,
                    ejercicio.getInstrucciones()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Ejercicio ejercicio
    ) throws SQLException {

        String sql = "UPDATE ejercicio SET nombre_ejercicio = ?, descripcion = ?, tipo_ejercicio = ?, nivel_dificultad = ?, instrucciones = ? WHERE id_ejercicio = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    ejercicio.getNombreEjercicio()
            );

            sentencia.setString(
                    2,
                    ejercicio.getDescripcion()
            );

            sentencia.setString(
                    3,
                    ejercicio.getTipoEjercicio()
            );

            sentencia.setString(
                    4,
                    ejercicio.getNivelDificultad()
            );

            sentencia.setString(
                    5,
                    ejercicio.getInstrucciones()
            );

            sentencia.setLong(
                    6,
                    ejercicio.getIdEjercicio()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Ejercicio buscar(
            Long idEjercicio
    ) throws SQLException {

        String sql = "SELECT id_ejercicio, nombre_ejercicio, descripcion, tipo_ejercicio, nivel_dificultad, instrucciones FROM ejercicio WHERE id_ejercicio = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idEjercicio);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirEjercicio(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Ejercicio> listar(
            String criterio
    ) throws SQLException {

        List<Ejercicio> lista = new ArrayList<>();
        String sql = "SELECT id_ejercicio, nombre_ejercicio, descripcion, tipo_ejercicio, nivel_dificultad, instrucciones FROM ejercicio WHERE nombre_ejercicio ILIKE ? OR descripcion ILIKE ? OR tipo_ejercicio ILIKE ? OR nivel_dificultad ILIKE ? OR instrucciones ILIKE ? ORDER BY id_ejercicio";

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
            sentencia.setString(5, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirEjercicio(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idEjercicio
    ) throws SQLException {

        String sql = "DELETE FROM ejercicio WHERE id_ejercicio = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idEjercicio);

            return sentencia.executeUpdate() > 0;
        }
    }



    private Ejercicio convertirEjercicio(
            ResultSet resultado
    ) throws SQLException {

        Ejercicio ejercicio = new Ejercicio();

        ejercicio.setIdEjercicio(
                resultado.getLong("id_ejercicio")
        );

        ejercicio.setNombreEjercicio(
                resultado.getString("nombre_ejercicio")
        );

        ejercicio.setDescripcion(
                resultado.getString("descripcion")
        );

        ejercicio.setTipoEjercicio(
                resultado.getString("tipo_ejercicio")
        );

        ejercicio.setNivelDificultad(
                resultado.getString("nivel_dificultad")
        );

        ejercicio.setInstrucciones(
                resultado.getString("instrucciones")
        );

        return ejercicio;
    }
}
