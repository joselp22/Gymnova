package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.TipoEquipo;

/**
 * DAO para la tabla tipo_equipo.
 *
 * @author Usuario
 */
public class TipoEquipoDAO {

    public boolean guardar(
            TipoEquipo tipoEquipo
    ) throws SQLException {

        String sql = "INSERT INTO tipo_equipo (nombre_tipo, requiere_electricidad, periodicidad_base_dias) VALUES (?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    tipoEquipo.getNombreTipo()
            );

            sentencia.setBoolean(
                    2,
                    tipoEquipo.isRequiereElectricidad()
            );

            sentencia.setInt(
                    3,
                    tipoEquipo.getPeriodicidadBaseDias()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            TipoEquipo tipoEquipo
    ) throws SQLException {

        String sql = "UPDATE tipo_equipo SET nombre_tipo = ?, requiere_electricidad = ?, periodicidad_base_dias = ? WHERE id_tipo_equipo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    tipoEquipo.getNombreTipo()
            );

            sentencia.setBoolean(
                    2,
                    tipoEquipo.isRequiereElectricidad()
            );

            sentencia.setInt(
                    3,
                    tipoEquipo.getPeriodicidadBaseDias()
            );

            sentencia.setLong(
                    4,
                    tipoEquipo.getIdTipoEquipo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public TipoEquipo buscar(
            Long idTipoEquipo
    ) throws SQLException {

        String sql = "SELECT id_tipo_equipo, nombre_tipo, requiere_electricidad, periodicidad_base_dias FROM tipo_equipo WHERE id_tipo_equipo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idTipoEquipo);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirTipoEquipo(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<TipoEquipo> listar(
            String criterio
    ) throws SQLException {

        List<TipoEquipo> lista = new ArrayList<>();
        String sql = "SELECT id_tipo_equipo, nombre_tipo, requiere_electricidad, periodicidad_base_dias FROM tipo_equipo WHERE nombre_tipo ILIKE ? OR CAST(requiere_electricidad AS TEXT) ILIKE ? OR CAST(periodicidad_base_dias AS TEXT) ILIKE ? ORDER BY id_tipo_equipo";

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
                            convertirTipoEquipo(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idTipoEquipo
    ) throws SQLException {

        String sql = "DELETE FROM tipo_equipo WHERE id_tipo_equipo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idTipoEquipo);

            return sentencia.executeUpdate() > 0;
        }
    }

    private TipoEquipo convertirTipoEquipo(
            ResultSet resultado
    ) throws SQLException {

        TipoEquipo tipoEquipo = new TipoEquipo();

        tipoEquipo.setIdTipoEquipo(
                resultado.getLong("id_tipo_equipo")
        );

        tipoEquipo.setNombreTipo(
                resultado.getString("nombre_tipo")
        );

        tipoEquipo.setRequiereElectricidad(
                resultado.getBoolean("requiere_electricidad")
        );

        tipoEquipo.setPeriodicidadBaseDias(
                resultado.getInt("periodicidad_base_dias")
        );

        return tipoEquipo;
    }
}
