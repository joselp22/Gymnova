package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.ContieneEjercicio;

/**
 * DAO para la tabla contiene_ejercicio.
 *
 * @author Usuario
 */
public class ContieneEjercicioDAO {

    public boolean guardar(
            ContieneEjercicio contieneEjercicio
    ) throws SQLException {

        String sql = "INSERT INTO contiene_ejercicio (dia_semana, orden, repeticiones, series, peso_sugerido, duracion_minutos, descanso_segundos, id_rutina, id_ejercicio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    contieneEjercicio.getDiaSemana()
            );

            sentencia.setInt(
                    2,
                    contieneEjercicio.getOrden()
            );

            sentencia.setString(
                    3,
                    contieneEjercicio.getRepeticiones()
            );

            sentencia.setInt(
                    4,
                    contieneEjercicio.getSeries()
            );

            sentencia.setBigDecimal(
                    5,
                    contieneEjercicio.getPesoSugerido()
            );

            sentencia.setInt(
                    6,
                    contieneEjercicio.getDuracionMinutos()
            );

            sentencia.setInt(
                    7,
                    contieneEjercicio.getDescansoSegundos()
            );

            sentencia.setLong(
                    8,
                    contieneEjercicio.getIdRutina()
            );

            sentencia.setLong(
                    9,
                    contieneEjercicio.getIdEjercicio()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            ContieneEjercicio contieneEjercicio
    ) throws SQLException {

        String sql = "UPDATE contiene_ejercicio SET dia_semana = ?, orden = ?, repeticiones = ?, series = ?, peso_sugerido = ?, duracion_minutos = ?, descanso_segundos = ?, id_rutina = ?, id_ejercicio = ? WHERE id_rutina_ejercicio = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    contieneEjercicio.getDiaSemana()
            );

            sentencia.setInt(
                    2,
                    contieneEjercicio.getOrden()
            );

            sentencia.setString(
                    3,
                    contieneEjercicio.getRepeticiones()
            );

            sentencia.setInt(
                    4,
                    contieneEjercicio.getSeries()
            );

            sentencia.setBigDecimal(
                    5,
                    contieneEjercicio.getPesoSugerido()
            );

            sentencia.setInt(
                    6,
                    contieneEjercicio.getDuracionMinutos()
            );

            sentencia.setInt(
                    7,
                    contieneEjercicio.getDescansoSegundos()
            );

            sentencia.setLong(
                    8,
                    contieneEjercicio.getIdRutina()
            );

            sentencia.setLong(
                    9,
                    contieneEjercicio.getIdEjercicio()
            );

            sentencia.setLong(
                    10,
                    contieneEjercicio.getIdRutinaEjercicio()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public ContieneEjercicio buscar(
            Long idRutinaEjercicio
    ) throws SQLException {

        String sql = "SELECT id_rutina_ejercicio, dia_semana, orden, repeticiones, series, peso_sugerido, duracion_minutos, descanso_segundos, id_rutina, id_ejercicio FROM contiene_ejercicio WHERE id_rutina_ejercicio = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idRutinaEjercicio);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirContieneEjercicio(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<ContieneEjercicio> listar(
            String criterio
    ) throws SQLException {

        List<ContieneEjercicio> lista = new ArrayList<>();
        String sql = "SELECT id_rutina_ejercicio, dia_semana, orden, repeticiones, series, peso_sugerido, duracion_minutos, descanso_segundos, id_rutina, id_ejercicio FROM contiene_ejercicio WHERE dia_semana ILIKE ? OR repeticiones ILIKE ? OR CAST(series AS TEXT) ILIKE ? ORDER BY id_rutina_ejercicio";

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

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirContieneEjercicio(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idRutinaEjercicio
    ) throws SQLException {

        String sql = "DELETE FROM contiene_ejercicio WHERE id_rutina_ejercicio = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idRutinaEjercicio);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<ContieneEjercicio> listarPorIdRutina(
            Long idRutina
    ) throws SQLException {

        List<ContieneEjercicio> lista = new ArrayList<>();
        String sql = "SELECT id_rutina_ejercicio, dia_semana, orden, repeticiones, series, peso_sugerido, duracion_minutos, descanso_segundos, id_rutina, id_ejercicio FROM contiene_ejercicio WHERE id_rutina = ? ORDER BY id_rutina_ejercicio";

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
                            convertirContieneEjercicio(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<ContieneEjercicio> listarPorIdEjercicio(
            Long idEjercicio
    ) throws SQLException {

        List<ContieneEjercicio> lista = new ArrayList<>();
        String sql = "SELECT id_rutina_ejercicio, dia_semana, orden, repeticiones, series, peso_sugerido, duracion_minutos, descanso_segundos, id_rutina, id_ejercicio FROM contiene_ejercicio WHERE id_ejercicio = ? ORDER BY id_rutina_ejercicio";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEjercicio
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirContieneEjercicio(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private ContieneEjercicio convertirContieneEjercicio(
            ResultSet resultado
    ) throws SQLException {

        ContieneEjercicio contieneEjercicio = new ContieneEjercicio();

        contieneEjercicio.setIdRutinaEjercicio(
                resultado.getLong("id_rutina_ejercicio")
        );

        contieneEjercicio.setDiaSemana(
                resultado.getString("dia_semana")
        );

        contieneEjercicio.setOrden(
                resultado.getInt("orden")
        );

        contieneEjercicio.setRepeticiones(
                resultado.getString("repeticiones")
        );

        contieneEjercicio.setSeries(
                resultado.getInt("series")
        );

        contieneEjercicio.setPesoSugerido(
                resultado.getBigDecimal("peso_sugerido")
        );

        contieneEjercicio.setDuracionMinutos(
                resultado.getInt("duracion_minutos")
        );

        contieneEjercicio.setDescansoSegundos(
                resultado.getInt("descanso_segundos")
        );

        contieneEjercicio.setIdRutina(
                resultado.getLong("id_rutina")
        );

        contieneEjercicio.setIdEjercicio(
                resultado.getLong("id_ejercicio")
        );

        return contieneEjercicio;
    }
}
