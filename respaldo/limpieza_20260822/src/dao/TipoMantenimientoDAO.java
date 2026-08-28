package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.TipoMantenimiento;

/**
 * DAO para la tabla tipo_mantenimiento.
 *
 * @author Usuario
 */
public class TipoMantenimientoDAO {

    public boolean guardar(
            TipoMantenimiento tipoMantenimiento
    ) throws SQLException {

        String sql = "INSERT INTO tipo_mantenimiento (nombre_tipo, requiere_repuestos, requiere_detener_equipo, periodicidad_recomendada_dias) VALUES (?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    tipoMantenimiento.getNombreTipo()
            );

            sentencia.setBoolean(
                    2,
                    tipoMantenimiento.isRequiereRepuestos()
            );

            sentencia.setBoolean(
                    3,
                    tipoMantenimiento.isRequiereDetenerEquipo()
            );

            sentencia.setInt(
                    4,
                    tipoMantenimiento.getPeriodicidadRecomendadaDias()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            TipoMantenimiento tipoMantenimiento
    ) throws SQLException {

        String sql = "UPDATE tipo_mantenimiento SET nombre_tipo = ?, requiere_repuestos = ?, requiere_detener_equipo = ?, periodicidad_recomendada_dias = ? WHERE id_tipo_mantenimiento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    tipoMantenimiento.getNombreTipo()
            );

            sentencia.setBoolean(
                    2,
                    tipoMantenimiento.isRequiereRepuestos()
            );

            sentencia.setBoolean(
                    3,
                    tipoMantenimiento.isRequiereDetenerEquipo()
            );

            sentencia.setInt(
                    4,
                    tipoMantenimiento.getPeriodicidadRecomendadaDias()
            );

            sentencia.setLong(
                    5,
                    tipoMantenimiento.getIdTipoMantenimiento()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public TipoMantenimiento buscar(
            Long idTipoMantenimiento
    ) throws SQLException {

        String sql = "SELECT id_tipo_mantenimiento, nombre_tipo, requiere_repuestos, requiere_detener_equipo, periodicidad_recomendada_dias FROM tipo_mantenimiento WHERE id_tipo_mantenimiento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idTipoMantenimiento);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirTipoMantenimiento(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<TipoMantenimiento> listar(
            String criterio
    ) throws SQLException {

        List<TipoMantenimiento> lista = new ArrayList<>();
        String sql = "SELECT id_tipo_mantenimiento, nombre_tipo, requiere_repuestos, requiere_detener_equipo, periodicidad_recomendada_dias FROM tipo_mantenimiento WHERE nombre_tipo ILIKE ? OR CAST(requiere_repuestos AS TEXT) ILIKE ? OR CAST(requiere_detener_equipo AS TEXT) ILIKE ? OR CAST(periodicidad_recomendada_dias AS TEXT) ILIKE ? ORDER BY id_tipo_mantenimiento";

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
                            convertirTipoMantenimiento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idTipoMantenimiento
    ) throws SQLException {

        String sql = "DELETE FROM tipo_mantenimiento WHERE id_tipo_mantenimiento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idTipoMantenimiento);

            return sentencia.executeUpdate() > 0;
        }
    }

    private TipoMantenimiento convertirTipoMantenimiento(
            ResultSet resultado
    ) throws SQLException {

        TipoMantenimiento tipoMantenimiento = new TipoMantenimiento();

        tipoMantenimiento.setIdTipoMantenimiento(
                resultado.getLong("id_tipo_mantenimiento")
        );

        tipoMantenimiento.setNombreTipo(
                resultado.getString("nombre_tipo")
        );

        tipoMantenimiento.setRequiereRepuestos(
                resultado.getBoolean("requiere_repuestos")
        );

        tipoMantenimiento.setRequiereDetenerEquipo(
                resultado.getBoolean("requiere_detener_equipo")
        );

        tipoMantenimiento.setPeriodicidadRecomendadaDias(
                resultado.getInt("periodicidad_recomendada_dias")
        );

        return tipoMantenimiento;
    }
}
