package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.AplicaDescuento;

/**
 * DAO para la tabla aplica_descuento.
 *
 * @author Usuario
 */
public class AplicaDescuentoDAO {

    public boolean guardar(
            AplicaDescuento aplicaDescuento
    ) throws SQLException {

        String sql = "INSERT INTO aplica_descuento (fecha_aplicacion, base_calculo, motivo_aplicacion, estado_aplicacion, id_factura, id_descuento) VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (aplicaDescuento.getFechaAplicacion() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            aplicaDescuento.getFechaAplicacion()
                    )
            );
        }

            sentencia.setBigDecimal(
                    2,
                    aplicaDescuento.getBaseCalculo()
            );

            sentencia.setString(
                    3,
                    aplicaDescuento.getMotivoAplicacion()
            );

            sentencia.setString(
                    4,
                    aplicaDescuento.getEstadoAplicacion()
            );

            sentencia.setLong(
                    5,
                    aplicaDescuento.getIdFactura()
            );

            sentencia.setLong(
                    6,
                    aplicaDescuento.getIdDescuento()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            AplicaDescuento aplicaDescuento
    ) throws SQLException {

        String sql = "UPDATE aplica_descuento SET fecha_aplicacion = ?, base_calculo = ?, motivo_aplicacion = ?, estado_aplicacion = ? WHERE id_factura = ? AND id_descuento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (aplicaDescuento.getFechaAplicacion() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            aplicaDescuento.getFechaAplicacion()
                    )
            );
        }

            sentencia.setBigDecimal(
                    2,
                    aplicaDescuento.getBaseCalculo()
            );

            sentencia.setString(
                    3,
                    aplicaDescuento.getMotivoAplicacion()
            );

            sentencia.setString(
                    4,
                    aplicaDescuento.getEstadoAplicacion()
            );

            sentencia.setLong(
                    5,
                    aplicaDescuento.getIdFactura()
            );

            sentencia.setLong(
                    6,
                    aplicaDescuento.getIdDescuento()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public AplicaDescuento buscar(
            Long idFactura,
            Long idDescuento
    ) throws SQLException {

        String sql = "SELECT fecha_aplicacion, base_calculo, motivo_aplicacion, estado_aplicacion, id_factura, id_descuento FROM aplica_descuento WHERE id_factura = ? AND id_descuento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idFactura);
            sentencia.setLong(2, idDescuento);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirAplicaDescuento(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<AplicaDescuento> listar(
            String criterio
    ) throws SQLException {

        List<AplicaDescuento> lista = new ArrayList<>();
        String sql = "SELECT fecha_aplicacion, base_calculo, motivo_aplicacion, estado_aplicacion, id_factura, id_descuento FROM aplica_descuento WHERE motivo_aplicacion ILIKE ? OR estado_aplicacion ILIKE ? ORDER BY id_factura, id_descuento";

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
                            convertirAplicaDescuento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idFactura,
            Long idDescuento
    ) throws SQLException {

        String sql = "UPDATE aplica_descuento SET estado_aplicacion = 'ANULADO' WHERE id_factura = ? AND id_descuento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idFactura);
            sentencia.setLong(2, idDescuento);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idFactura,
            Long idDescuento
    ) throws SQLException {

        String sql = "DELETE FROM aplica_descuento WHERE id_factura = ? AND id_descuento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idFactura);
            sentencia.setLong(2, idDescuento);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<AplicaDescuento> listarPorIdFactura(
            Long idFactura
    ) throws SQLException {

        List<AplicaDescuento> lista = new ArrayList<>();
        String sql = "SELECT fecha_aplicacion, base_calculo, motivo_aplicacion, estado_aplicacion, id_factura, id_descuento FROM aplica_descuento WHERE id_factura = ? ORDER BY id_factura, id_descuento";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idFactura
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirAplicaDescuento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<AplicaDescuento> listarPorIdDescuento(
            Long idDescuento
    ) throws SQLException {

        List<AplicaDescuento> lista = new ArrayList<>();
        String sql = "SELECT fecha_aplicacion, base_calculo, motivo_aplicacion, estado_aplicacion, id_factura, id_descuento FROM aplica_descuento WHERE id_descuento = ? ORDER BY id_factura, id_descuento";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idDescuento
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirAplicaDescuento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private AplicaDescuento convertirAplicaDescuento(
            ResultSet resultado
    ) throws SQLException {

        AplicaDescuento aplicaDescuento = new AplicaDescuento();

        aplicaDescuento.setFechaAplicacion(
                resultado.getDate("fecha_aplicacion") == null
                ? null
                : resultado.getDate("fecha_aplicacion").toLocalDate()
        );

        aplicaDescuento.setBaseCalculo(
                resultado.getBigDecimal("base_calculo")
        );

        aplicaDescuento.setMotivoAplicacion(
                resultado.getString("motivo_aplicacion")
        );

        aplicaDescuento.setEstadoAplicacion(
                resultado.getString("estado_aplicacion")
        );

        aplicaDescuento.setIdFactura(
                resultado.getLong("id_factura")
        );

        aplicaDescuento.setIdDescuento(
                resultado.getLong("id_descuento")
        );

        return aplicaDescuento;
    }
}
