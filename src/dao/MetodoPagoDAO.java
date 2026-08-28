package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.MetodoPago;

/**
 * DAO para la tabla metodo_pago.
 *
 * @author Usuario
 */
public class MetodoPagoDAO {

    public boolean guardar(
            MetodoPago metodoPago
    ) throws SQLException {

        String sql = "INSERT INTO metodo_pago (nombre_metodo, descripcion, requiere_referencia, permite_cuotas, porcentaje_comision, estado_metodo) VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    metodoPago.getNombreMetodo()
            );

            sentencia.setString(
                    2,
                    metodoPago.getDescripcion()
            );

            sentencia.setBoolean(
                    3,
                    metodoPago.isRequiereReferencia()
            );

            sentencia.setBoolean(
                    4,
                    metodoPago.isPermiteCuotas()
            );

            sentencia.setBigDecimal(
                    5,
                    metodoPago.getPorcentajeComision()
            );

            sentencia.setBoolean(
                    6,
                    metodoPago.isEstadoMetodo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            MetodoPago metodoPago
    ) throws SQLException {

        String sql = "UPDATE metodo_pago SET nombre_metodo = ?, descripcion = ?, requiere_referencia = ?, permite_cuotas = ?, porcentaje_comision = ?, estado_metodo = ? WHERE id_metodo_pago = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    metodoPago.getNombreMetodo()
            );

            sentencia.setString(
                    2,
                    metodoPago.getDescripcion()
            );

            sentencia.setBoolean(
                    3,
                    metodoPago.isRequiereReferencia()
            );

            sentencia.setBoolean(
                    4,
                    metodoPago.isPermiteCuotas()
            );

            sentencia.setBigDecimal(
                    5,
                    metodoPago.getPorcentajeComision()
            );

            sentencia.setBoolean(
                    6,
                    metodoPago.isEstadoMetodo()
            );

            sentencia.setLong(
                    7,
                    metodoPago.getIdMetodoPago()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public MetodoPago buscar(
            Long idMetodoPago
    ) throws SQLException {

        String sql = "SELECT id_metodo_pago, nombre_metodo, descripcion, requiere_referencia, permite_cuotas, porcentaje_comision, estado_metodo FROM metodo_pago WHERE id_metodo_pago = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMetodoPago);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirMetodoPago(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<MetodoPago> listar(
            String criterio
    ) throws SQLException {

        List<MetodoPago> lista = new ArrayList<>();
        String sql = "SELECT id_metodo_pago, nombre_metodo, descripcion, requiere_referencia, permite_cuotas, porcentaje_comision, estado_metodo FROM metodo_pago WHERE nombre_metodo ILIKE ? OR descripcion ILIKE ? ORDER BY id_metodo_pago";

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
                            convertirMetodoPago(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idMetodoPago
    ) throws SQLException {

        String sql = "UPDATE metodo_pago SET estado_metodo = FALSE WHERE id_metodo_pago = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMetodoPago);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idMetodoPago
    ) throws SQLException {

        String sql = "DELETE FROM metodo_pago WHERE id_metodo_pago = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMetodoPago);

            return sentencia.executeUpdate() > 0;
        }
    }



    private MetodoPago convertirMetodoPago(
            ResultSet resultado
    ) throws SQLException {

        MetodoPago metodoPago = new MetodoPago();

        metodoPago.setIdMetodoPago(
                resultado.getLong("id_metodo_pago")
        );

        metodoPago.setNombreMetodo(
                resultado.getString("nombre_metodo")
        );

        metodoPago.setDescripcion(
                resultado.getString("descripcion")
        );

        metodoPago.setRequiereReferencia(
                resultado.getBoolean("requiere_referencia")
        );

        metodoPago.setPermiteCuotas(
                resultado.getBoolean("permite_cuotas")
        );

        metodoPago.setPorcentajeComision(
                resultado.getBigDecimal("porcentaje_comision")
        );

        metodoPago.setEstadoMetodo(
                resultado.getBoolean("estado_metodo")
        );

        return metodoPago;
    }
}
