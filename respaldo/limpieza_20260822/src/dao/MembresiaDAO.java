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
import modelo.Membresia;

/**
 * DAO para la tabla membresia.
 *
 * @author Usuario
 */
public class MembresiaDAO {

    public boolean guardar(
            Membresia membresia
    ) throws SQLException {

        String sql = "INSERT INTO membresia (numero_membresia, fecha_inicio, fecha_fin, costo_final, estado_membresia, observaciones, id_cliente, id_tipo_membresia) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    membresia.getNumeroMembresia()
            );

            if (membresia.getFechaInicio() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            membresia.getFechaInicio()
                    )
            );
        }

            if (membresia.getFechaFin() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            membresia.getFechaFin()
                    )
            );
        }

            sentencia.setBigDecimal(
                    4,
                    membresia.getCostoFinal()
            );

            sentencia.setString(
                    5,
                    membresia.getEstadoMembresia()
            );

            sentencia.setString(
                    6,
                    membresia.getObservaciones()
            );

            sentencia.setLong(
                    7,
                    membresia.getIdCliente()
            );

            sentencia.setLong(
                    8,
                    membresia.getIdTipoMembresia()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Membresia membresia
    ) throws SQLException {

        String sql = "UPDATE membresia SET numero_membresia = ?, fecha_inicio = ?, fecha_fin = ?, costo_final = ?, estado_membresia = ?, observaciones = ?, id_cliente = ?, id_tipo_membresia = ? WHERE id_membresia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    membresia.getNumeroMembresia()
            );

            if (membresia.getFechaInicio() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            membresia.getFechaInicio()
                    )
            );
        }

            if (membresia.getFechaFin() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            membresia.getFechaFin()
                    )
            );
        }

            sentencia.setBigDecimal(
                    4,
                    membresia.getCostoFinal()
            );

            sentencia.setString(
                    5,
                    membresia.getEstadoMembresia()
            );

            sentencia.setString(
                    6,
                    membresia.getObservaciones()
            );

            sentencia.setLong(
                    7,
                    membresia.getIdCliente()
            );

            sentencia.setLong(
                    8,
                    membresia.getIdTipoMembresia()
            );

            sentencia.setLong(
                    9,
                    membresia.getIdMembresia()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Membresia buscar(
            Long idMembresia
    ) throws SQLException {

        String sql = "SELECT id_membresia, numero_membresia, fecha_inicio, fecha_fin, costo_final, estado_membresia, observaciones, id_cliente, id_tipo_membresia FROM membresia WHERE id_membresia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMembresia);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirMembresia(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Membresia> listar(
            String criterio
    ) throws SQLException {

        List<Membresia> lista = new ArrayList<>();
        String sql = "SELECT id_membresia, numero_membresia, fecha_inicio, fecha_fin, costo_final, estado_membresia, observaciones, id_cliente, id_tipo_membresia FROM membresia WHERE numero_membresia ILIKE ? OR estado_membresia ILIKE ? OR observaciones ILIKE ? ORDER BY id_membresia";

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
                            convertirMembresia(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idMembresia
    ) throws SQLException {

        String sql = "UPDATE membresia SET estado_membresia = 'CANCELADA' WHERE id_membresia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMembresia);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idMembresia
    ) throws SQLException {

        String sql = "DELETE FROM membresia WHERE id_membresia = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMembresia);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Membresia> listarPorIdCliente(
            Long idCliente
    ) throws SQLException {

        List<Membresia> lista = new ArrayList<>();
        String sql = "SELECT id_membresia, numero_membresia, fecha_inicio, fecha_fin, costo_final, estado_membresia, observaciones, id_cliente, id_tipo_membresia FROM membresia WHERE id_cliente = ? ORDER BY id_membresia";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idCliente
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirMembresia(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<Membresia> listarPorIdTipoMembresia(
            Long idTipoMembresia
    ) throws SQLException {

        List<Membresia> lista = new ArrayList<>();
        String sql = "SELECT id_membresia, numero_membresia, fecha_inicio, fecha_fin, costo_final, estado_membresia, observaciones, id_cliente, id_tipo_membresia FROM membresia WHERE id_tipo_membresia = ? ORDER BY id_membresia";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idTipoMembresia
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirMembresia(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private Membresia convertirMembresia(
            ResultSet resultado
    ) throws SQLException {

        Membresia membresia = new Membresia();

        membresia.setIdMembresia(
                resultado.getLong("id_membresia")
        );

        membresia.setNumeroMembresia(
                resultado.getString("numero_membresia")
        );

        membresia.setFechaInicio(
                resultado.getDate("fecha_inicio") == null
                ? null
                : resultado.getDate("fecha_inicio").toLocalDate()
        );

        membresia.setFechaFin(
                resultado.getDate("fecha_fin") == null
                ? null
                : resultado.getDate("fecha_fin").toLocalDate()
        );

        membresia.setCostoFinal(
                resultado.getBigDecimal("costo_final")
        );

        membresia.setEstadoMembresia(
                resultado.getString("estado_membresia")
        );

        membresia.setObservaciones(
                resultado.getString("observaciones")
        );

        membresia.setIdCliente(
                resultado.getLong("id_cliente")
        );

        membresia.setIdTipoMembresia(
                resultado.getLong("id_tipo_membresia")
        );

        return membresia;
    }
}
