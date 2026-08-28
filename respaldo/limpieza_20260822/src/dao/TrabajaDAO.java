package dao;

import conexion.ConexionPostgreSQL;
import modelo.Trabaja;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla trabaja.
 *
 * @author Usuario
 */
public class TrabajaDAO {

    public boolean guardar(
            Trabaja trabaja
    ) throws SQLException {

        String sql
                = "INSERT INTO trabaja ("
                + "id_ejercicio, "
                + "id_grupo_muscular, "
                + "tipo_participacion, "
                + "porcentaje_estimulacion, "
                + "observaciones"
                + ") VALUES (?, ?, ?, ?, ?)";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    trabaja.getIdEjercicio()
            );

            sentencia.setLong(
                    2,
                    trabaja.getIdGrupoMuscular()
            );

            sentencia.setString(
                    3,
                    trabaja.getTipoParticipacion()
            );

            sentencia.setBigDecimal(
                    4,
                    trabaja.getPorcentajeEstimulacion()
            );

            colocarObservaciones(
                    sentencia,
                    5,
                    trabaja.getObservaciones()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Trabaja trabaja
    ) throws SQLException {

        String sql
                = "UPDATE trabaja SET "
                + "tipo_participacion = ?, "
                + "porcentaje_estimulacion = ?, "
                + "observaciones = ? "
                + "WHERE id_ejercicio = ? "
                + "AND id_grupo_muscular = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    trabaja.getTipoParticipacion()
            );

            sentencia.setBigDecimal(
                    2,
                    trabaja.getPorcentajeEstimulacion()
            );

            colocarObservaciones(
                    sentencia,
                    3,
                    trabaja.getObservaciones()
            );

            sentencia.setLong(
                    4,
                    trabaja.getIdEjercicio()
            );

            sentencia.setLong(
                    5,
                    trabaja.getIdGrupoMuscular()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idEjercicio,
            Long idGrupoMuscular
    ) throws SQLException {

        String sql
                = "DELETE FROM trabaja "
                + "WHERE id_ejercicio = ? "
                + "AND id_grupo_muscular = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEjercicio
            );

            sentencia.setLong(
                    2,
                    idGrupoMuscular
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Trabaja buscar(
            Long idEjercicio,
            Long idGrupoMuscular
    ) throws SQLException {

        String sql
                = "SELECT "
                + "id_ejercicio, "
                + "id_grupo_muscular, "
                + "tipo_participacion, "
                + "porcentaje_estimulacion, "
                + "observaciones "
                + "FROM trabaja "
                + "WHERE id_ejercicio = ? "
                + "AND id_grupo_muscular = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEjercicio
            );

            sentencia.setLong(
                    2,
                    idGrupoMuscular
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return convertirTrabaja(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public boolean existe(
            Long idEjercicio,
            Long idGrupoMuscular
    ) throws SQLException {

        String sql
                = "SELECT 1 "
                + "FROM trabaja "
                + "WHERE id_ejercicio = ? "
                + "AND id_grupo_muscular = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEjercicio
            );

            sentencia.setLong(
                    2,
                    idGrupoMuscular
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    public List<Trabaja> listar(
            String criterio
    ) throws SQLException {

        List<Trabaja> relaciones
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "id_ejercicio, "
                + "id_grupo_muscular, "
                + "tipo_participacion, "
                + "porcentaje_estimulacion, "
                + "observaciones "
                + "FROM trabaja "
                + "WHERE CAST(id_ejercicio AS TEXT) ILIKE ? "
                + "OR CAST(id_grupo_muscular AS TEXT) ILIKE ? "
                + "OR tipo_participacion ILIKE ? "
                + "OR observaciones ILIKE ? "
                + "ORDER BY id_ejercicio, id_grupo_muscular";

        if (criterio == null) {
            criterio = "";
        }

        String filtro
                = "%"
                + criterio.trim()
                + "%";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, filtro);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);
            sentencia.setString(4, filtro);

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    relaciones.add(
                            convertirTrabaja(
                                    resultado
                            )
                    );
                }
            }
        }

        return relaciones;
    }

    public List<Trabaja> listarPorEjercicio(
            Long idEjercicio
    ) throws SQLException {

        List<Trabaja> relaciones
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "id_ejercicio, "
                + "id_grupo_muscular, "
                + "tipo_participacion, "
                + "porcentaje_estimulacion, "
                + "observaciones "
                + "FROM trabaja "
                + "WHERE id_ejercicio = ? "
                + "ORDER BY id_grupo_muscular";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEjercicio
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    relaciones.add(
                            convertirTrabaja(
                                    resultado
                            )
                    );
                }
            }
        }

        return relaciones;
    }

    public List<Trabaja> listarPorGrupoMuscular(
            Long idGrupoMuscular
    ) throws SQLException {

        List<Trabaja> relaciones
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "id_ejercicio, "
                + "id_grupo_muscular, "
                + "tipo_participacion, "
                + "porcentaje_estimulacion, "
                + "observaciones "
                + "FROM trabaja "
                + "WHERE id_grupo_muscular = ? "
                + "ORDER BY id_ejercicio";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idGrupoMuscular
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    relaciones.add(
                            convertirTrabaja(
                                    resultado
                            )
                    );
                }
            }
        }

        return relaciones;
    }

    private Trabaja convertirTrabaja(
            ResultSet resultado
    ) throws SQLException {

        Trabaja trabaja
                = new Trabaja();

        trabaja.setIdEjercicio(
                resultado.getLong(
                        "id_ejercicio"
                )
        );

        trabaja.setIdGrupoMuscular(
                resultado.getLong(
                        "id_grupo_muscular"
                )
        );

        trabaja.setTipoParticipacion(
                resultado.getString(
                        "tipo_participacion"
                )
        );

        trabaja.setPorcentajeEstimulacion(
                resultado.getBigDecimal(
                        "porcentaje_estimulacion"
                )
        );

        trabaja.setObservaciones(
                resultado.getString(
                        "observaciones"
                )
        );

        return trabaja;
    }

    private void colocarObservaciones(
            PreparedStatement sentencia,
            int posicion,
            String observaciones
    ) throws SQLException {

        if (observaciones == null
                || observaciones.trim().isEmpty()) {

            sentencia.setNull(
                    posicion,
                    Types.VARCHAR
            );

        } else {

            sentencia.setString(
                    posicion,
                    observaciones.trim()
            );
        }
    }
}
