package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.TipoMembresia;

/**
 * DAO para la tabla tipo_membresia.
 *
 * @author Usuario
 */
public class TipoMembresiaDAO {

    public boolean guardar(
            TipoMembresia tipoMembresia
    ) throws SQLException {

        String sql = "INSERT INTO tipo_membresia (nombre, descripcion, duracion_dias, precio_base, limite_accesos, acceso_ilimitado, estado_tipo) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    tipoMembresia.getNombre()
            );

            sentencia.setString(
                    2,
                    tipoMembresia.getDescripcion()
            );

            sentencia.setInt(
                    3,
                    tipoMembresia.getDuracionDias()
            );

            sentencia.setBigDecimal(
                    4,
                    tipoMembresia.getPrecioBase()
            );

            sentencia.setInt(
                    5,
                    tipoMembresia.getLimiteAccesos()
            );

            sentencia.setBoolean(
                    6,
                    tipoMembresia.isAccesoIlimitado()
            );

            sentencia.setBoolean(
                    7,
                    tipoMembresia.isEstadoTipo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            TipoMembresia tipoMembresia
    ) throws SQLException {

        String sql = "UPDATE tipo_membresia SET nombre = ?, descripcion = ?, duracion_dias = ?, precio_base = ?, limite_accesos = ?, acceso_ilimitado = ?, estado_tipo = ? WHERE id_tipo_membresia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    tipoMembresia.getNombre()
            );

            sentencia.setString(
                    2,
                    tipoMembresia.getDescripcion()
            );

            sentencia.setInt(
                    3,
                    tipoMembresia.getDuracionDias()
            );

            sentencia.setBigDecimal(
                    4,
                    tipoMembresia.getPrecioBase()
            );

            sentencia.setInt(
                    5,
                    tipoMembresia.getLimiteAccesos()
            );

            sentencia.setBoolean(
                    6,
                    tipoMembresia.isAccesoIlimitado()
            );

            sentencia.setBoolean(
                    7,
                    tipoMembresia.isEstadoTipo()
            );

            sentencia.setLong(
                    8,
                    tipoMembresia.getIdTipoMembresia()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public TipoMembresia buscar(
            Long idTipoMembresia
    ) throws SQLException {

        String sql = "SELECT id_tipo_membresia, nombre, descripcion, duracion_dias, precio_base, limite_accesos, acceso_ilimitado, estado_tipo FROM tipo_membresia WHERE id_tipo_membresia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idTipoMembresia);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirTipoMembresia(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<TipoMembresia> listar(
            String criterio
    ) throws SQLException {

        List<TipoMembresia> lista = new ArrayList<>();
        String sql = "SELECT id_tipo_membresia, nombre, descripcion, duracion_dias, precio_base, limite_accesos, acceso_ilimitado, estado_tipo FROM tipo_membresia WHERE nombre ILIKE ? OR descripcion ILIKE ? ORDER BY id_tipo_membresia";

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
                            convertirTipoMembresia(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idTipoMembresia
    ) throws SQLException {

        String sql = "UPDATE tipo_membresia SET estado_tipo = FALSE WHERE id_tipo_membresia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idTipoMembresia);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idTipoMembresia
    ) throws SQLException {

        String sql = "DELETE FROM tipo_membresia WHERE id_tipo_membresia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idTipoMembresia);

            return sentencia.executeUpdate() > 0;
        }
    }



    private TipoMembresia convertirTipoMembresia(
            ResultSet resultado
    ) throws SQLException {

        TipoMembresia tipoMembresia = new TipoMembresia();

        tipoMembresia.setIdTipoMembresia(
                resultado.getLong("id_tipo_membresia")
        );

        tipoMembresia.setNombre(
                resultado.getString("nombre")
        );

        tipoMembresia.setDescripcion(
                resultado.getString("descripcion")
        );

        tipoMembresia.setDuracionDias(
                resultado.getInt("duracion_dias")
        );

        tipoMembresia.setPrecioBase(
                resultado.getBigDecimal("precio_base")
        );

        tipoMembresia.setLimiteAccesos(
                resultado.getInt("limite_accesos")
        );

        tipoMembresia.setAccesoIlimitado(
                resultado.getBoolean("acceso_ilimitado")
        );

        tipoMembresia.setEstadoTipo(
                resultado.getBoolean("estado_tipo")
        );

        return tipoMembresia;
    }
}
