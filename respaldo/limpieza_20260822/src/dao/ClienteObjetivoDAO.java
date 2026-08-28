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
import modelo.ClienteObjetivo;

/**
 * DAO para la tabla cliente_objetivo.
 *
 * @author Usuario
 */
public class ClienteObjetivoDAO {

    public boolean guardar(
            ClienteObjetivo clienteObjetivo
    ) throws SQLException {

        String sql = "INSERT INTO cliente_objetivo (fecha_inicio, fecha_meta, prioridad, estado_cliente_objetivo, observaciones, peso_actual, id_cliente, id_objetivo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (clienteObjetivo.getFechaInicio() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            clienteObjetivo.getFechaInicio()
                    )
            );
        }

            if (clienteObjetivo.getFechaMeta() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            clienteObjetivo.getFechaMeta()
                    )
            );
        }

            sentencia.setString(
                    3,
                    clienteObjetivo.getPrioridad()
            );

            sentencia.setString(
                    4,
                    clienteObjetivo.getEstadoClienteObjetivo()
            );

            sentencia.setString(
                    5,
                    clienteObjetivo.getObservaciones()
            );

            sentencia.setBigDecimal(
                    6,
                    clienteObjetivo.getPesoActual()
            );

            sentencia.setLong(
                    7,
                    clienteObjetivo.getIdCliente()
            );

            sentencia.setLong(
                    8,
                    clienteObjetivo.getIdObjetivo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            ClienteObjetivo clienteObjetivo
    ) throws SQLException {

        String sql = "UPDATE cliente_objetivo SET fecha_inicio = ?, fecha_meta = ?, prioridad = ?, estado_cliente_objetivo = ?, observaciones = ?, peso_actual = ?, id_cliente = ?, id_objetivo = ? WHERE id_cliente_objetivo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (clienteObjetivo.getFechaInicio() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            clienteObjetivo.getFechaInicio()
                    )
            );
        }

            if (clienteObjetivo.getFechaMeta() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            clienteObjetivo.getFechaMeta()
                    )
            );
        }

            sentencia.setString(
                    3,
                    clienteObjetivo.getPrioridad()
            );

            sentencia.setString(
                    4,
                    clienteObjetivo.getEstadoClienteObjetivo()
            );

            sentencia.setString(
                    5,
                    clienteObjetivo.getObservaciones()
            );

            sentencia.setBigDecimal(
                    6,
                    clienteObjetivo.getPesoActual()
            );

            sentencia.setLong(
                    7,
                    clienteObjetivo.getIdCliente()
            );

            sentencia.setLong(
                    8,
                    clienteObjetivo.getIdObjetivo()
            );

            sentencia.setLong(
                    9,
                    clienteObjetivo.getIdClienteObjetivo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public ClienteObjetivo buscar(
            Long idClienteObjetivo
    ) throws SQLException {

        String sql = "SELECT id_cliente_objetivo, fecha_inicio, fecha_meta, prioridad, estado_cliente_objetivo, observaciones, peso_actual, id_cliente, id_objetivo FROM cliente_objetivo WHERE id_cliente_objetivo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idClienteObjetivo);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirClienteObjetivo(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<ClienteObjetivo> listar(
            String criterio
    ) throws SQLException {

        List<ClienteObjetivo> lista = new ArrayList<>();
        String sql = "SELECT id_cliente_objetivo, fecha_inicio, fecha_meta, prioridad, estado_cliente_objetivo, observaciones, peso_actual, id_cliente, id_objetivo FROM cliente_objetivo WHERE prioridad ILIKE ? OR estado_cliente_objetivo ILIKE ? OR observaciones ILIKE ? ORDER BY id_cliente_objetivo";

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
                            convertirClienteObjetivo(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idClienteObjetivo
    ) throws SQLException {

        String sql = "UPDATE cliente_objetivo SET estado_cliente_objetivo = 'CANCELADO' WHERE id_cliente_objetivo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idClienteObjetivo);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idClienteObjetivo
    ) throws SQLException {

        String sql = "DELETE FROM cliente_objetivo WHERE id_cliente_objetivo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idClienteObjetivo);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<ClienteObjetivo> listarPorIdCliente(
            Long idCliente
    ) throws SQLException {

        List<ClienteObjetivo> lista = new ArrayList<>();
        String sql = "SELECT id_cliente_objetivo, fecha_inicio, fecha_meta, prioridad, estado_cliente_objetivo, observaciones, peso_actual, id_cliente, id_objetivo FROM cliente_objetivo WHERE id_cliente = ? ORDER BY id_cliente_objetivo";

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
                            convertirClienteObjetivo(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<ClienteObjetivo> listarPorIdObjetivo(
            Long idObjetivo
    ) throws SQLException {

        List<ClienteObjetivo> lista = new ArrayList<>();
        String sql = "SELECT id_cliente_objetivo, fecha_inicio, fecha_meta, prioridad, estado_cliente_objetivo, observaciones, peso_actual, id_cliente, id_objetivo FROM cliente_objetivo WHERE id_objetivo = ? ORDER BY id_cliente_objetivo";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idObjetivo
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirClienteObjetivo(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private ClienteObjetivo convertirClienteObjetivo(
            ResultSet resultado
    ) throws SQLException {

        ClienteObjetivo clienteObjetivo = new ClienteObjetivo();

        clienteObjetivo.setIdClienteObjetivo(
                resultado.getLong("id_cliente_objetivo")
        );

        clienteObjetivo.setFechaInicio(
                resultado.getDate("fecha_inicio") == null
                ? null
                : resultado.getDate("fecha_inicio").toLocalDate()
        );

        clienteObjetivo.setFechaMeta(
                resultado.getDate("fecha_meta") == null
                ? null
                : resultado.getDate("fecha_meta").toLocalDate()
        );

        clienteObjetivo.setPrioridad(
                resultado.getString("prioridad")
        );

        clienteObjetivo.setEstadoClienteObjetivo(
                resultado.getString("estado_cliente_objetivo")
        );

        clienteObjetivo.setObservaciones(
                resultado.getString("observaciones")
        );

        clienteObjetivo.setPesoActual(
                resultado.getBigDecimal("peso_actual")
        );

        clienteObjetivo.setIdCliente(
                resultado.getLong("id_cliente")
        );

        clienteObjetivo.setIdObjetivo(
                resultado.getLong("id_objetivo")
        );

        return clienteObjetivo;
    }
}
