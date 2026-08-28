package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Comprobante;

/**
 * DAO para la tabla comprobante.
 *
 * @author Usuario
 */
public class ComprobanteDAO {

    public boolean guardar(
            Comprobante comprobante
    ) throws SQLException {

        String sql = "INSERT INTO comprobante (numero_comprobante, tipo_comprobante, fecha_emision, formato_archivo, ruta_archivo, correo_envio, fecha_envio, estado_comprobante, id_pago) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    comprobante.getNumeroComprobante()
            );

            sentencia.setString(
                    2,
                    comprobante.getTipoComprobante()
            );

            if (comprobante.getFechaEmision() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            comprobante.getFechaEmision()
                    )
            );
        }

            sentencia.setString(
                    4,
                    comprobante.getFormatoArchivo()
            );

            sentencia.setString(
                    5,
                    comprobante.getRutaArchivo()
            );

            sentencia.setString(
                    6,
                    comprobante.getCorreoEnvio()
            );

            if (comprobante.getFechaEnvio() == null) {
            sentencia.setDate(
                    7,
                    null
            );
        } else {
            sentencia.setDate(
                    7,
                    Date.valueOf(
                            comprobante.getFechaEnvio()
                    )
            );
        }

            sentencia.setString(
                    8,
                    comprobante.getEstadoComprobante()
            );

            sentencia.setLong(
                    9,
                    comprobante.getIdPago()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Comprobante comprobante
    ) throws SQLException {

        String sql = "UPDATE comprobante SET numero_comprobante = ?, tipo_comprobante = ?, fecha_emision = ?, formato_archivo = ?, ruta_archivo = ?, correo_envio = ?, fecha_envio = ?, estado_comprobante = ?, id_pago = ? WHERE id_comprobante = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    comprobante.getNumeroComprobante()
            );

            sentencia.setString(
                    2,
                    comprobante.getTipoComprobante()
            );

            if (comprobante.getFechaEmision() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            comprobante.getFechaEmision()
                    )
            );
        }

            sentencia.setString(
                    4,
                    comprobante.getFormatoArchivo()
            );

            sentencia.setString(
                    5,
                    comprobante.getRutaArchivo()
            );

            sentencia.setString(
                    6,
                    comprobante.getCorreoEnvio()
            );

            if (comprobante.getFechaEnvio() == null) {
            sentencia.setDate(
                    7,
                    null
            );
        } else {
            sentencia.setDate(
                    7,
                    Date.valueOf(
                            comprobante.getFechaEnvio()
                    )
            );
        }

            sentencia.setString(
                    8,
                    comprobante.getEstadoComprobante()
            );

            sentencia.setLong(
                    9,
                    comprobante.getIdPago()
            );

            sentencia.setLong(
                    10,
                    comprobante.getIdComprobante()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Comprobante buscar(
            Long idComprobante
    ) throws SQLException {

        String sql = "SELECT id_comprobante, numero_comprobante, tipo_comprobante, fecha_emision, formato_archivo, ruta_archivo, correo_envio, fecha_envio, estado_comprobante, id_pago FROM comprobante WHERE id_comprobante = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idComprobante);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirComprobante(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Comprobante> listar(
            String criterio
    ) throws SQLException {

        List<Comprobante> lista = new ArrayList<>();
        String sql = "SELECT id_comprobante, numero_comprobante, tipo_comprobante, fecha_emision, formato_archivo, ruta_archivo, correo_envio, fecha_envio, estado_comprobante, id_pago FROM comprobante WHERE numero_comprobante ILIKE ? OR tipo_comprobante ILIKE ? OR formato_archivo ILIKE ? OR ruta_archivo ILIKE ? OR correo_envio ILIKE ? OR estado_comprobante ILIKE ? ORDER BY id_comprobante";

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
            sentencia.setString(6, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirComprobante(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idComprobante
    ) throws SQLException {

        String sql = "UPDATE comprobante SET estado_comprobante = 'ANULADO' WHERE id_comprobante = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idComprobante);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idComprobante
    ) throws SQLException {

        String sql = "DELETE FROM comprobante WHERE id_comprobante = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idComprobante);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Comprobante> listarPorIdPago(
            Long idPago
    ) throws SQLException {

        List<Comprobante> lista = new ArrayList<>();
        String sql = "SELECT id_comprobante, numero_comprobante, tipo_comprobante, fecha_emision, formato_archivo, ruta_archivo, correo_envio, fecha_envio, estado_comprobante, id_pago FROM comprobante WHERE id_pago = ? ORDER BY id_comprobante";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idPago
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirComprobante(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private Comprobante convertirComprobante(
            ResultSet resultado
    ) throws SQLException {

        Comprobante comprobante = new Comprobante();

        comprobante.setIdComprobante(
                resultado.getLong("id_comprobante")
        );

        comprobante.setNumeroComprobante(
                resultado.getString("numero_comprobante")
        );

        comprobante.setTipoComprobante(
                resultado.getString("tipo_comprobante")
        );

        comprobante.setFechaEmision(
                resultado.getDate("fecha_emision") == null
                ? null
                : resultado.getDate("fecha_emision").toLocalDate()
        );

        comprobante.setFormatoArchivo(
                resultado.getString("formato_archivo")
        );

        comprobante.setRutaArchivo(
                resultado.getString("ruta_archivo")
        );

        comprobante.setCorreoEnvio(
                resultado.getString("correo_envio")
        );

        comprobante.setFechaEnvio(
                resultado.getDate("fecha_envio") == null
                ? null
                : resultado.getDate("fecha_envio").toLocalDate()
        );

        comprobante.setEstadoComprobante(
                resultado.getString("estado_comprobante")
        );

        comprobante.setIdPago(
                resultado.getLong("id_pago")
        );

        return comprobante;
    }
}
