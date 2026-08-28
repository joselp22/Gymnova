package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import modelo.Descuento;
import utilidades.GeneradorCodigos;

/**
 * DAO para la tabla descuento.
 *
 * @author Usuario
 */
public class DescuentoDAO {

    private static final String PREFIJO_CODIGO
            = GeneradorCodigos.PREFIJO_DESCUENTO;
    private static final long CLAVE_BLOQUEO_CODIGO
            = GeneradorCodigos.crearClaveBloqueoAdvisory(
                    "descuento.codigo_descuento"
            );

    public boolean guardar(
            Descuento descuento
    ) throws SQLException {

        Connection conexion = null;
        boolean autoCommitOriginal = true;

        try {
            conexion = ConexionPostgreSQL.getConexion();
            autoCommitOriginal = conexion.getAutoCommit();
            conexion.setAutoCommit(false);

            bloquearGeneracionCodigo(
                    conexion
            );

            descuento.setCodigoDescuento(
                    generarSiguienteCodigo(
                            conexion
                    )
            );

            boolean guardado = insertar(
                    conexion,
                    descuento
            );

            conexion.commit();
            return guardado;

        } catch (SQLException | RuntimeException e) {

            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }

            throw e;

        } finally {

            if (conexion != null) {
                try {
                    conexion.setAutoCommit(
                            autoCommitOriginal
                    );
                } finally {
                    conexion.close();
                }
            }
        }
    }

    private boolean insertar(
            Connection conexion,
            Descuento descuento
    ) throws SQLException {

        String sql = "INSERT INTO descuento (codigo_descuento, nombre_descuento, descripcion, tipo_descuento, valor_descuento, fecha_inicio, fecha_fin) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    descuento.getCodigoDescuento()
            );

            sentencia.setString(
                    2,
                    descuento.getNombreDescuento()
            );

            sentencia.setString(
                    3,
                    descuento.getDescripcion()
            );

            sentencia.setString(
                    4,
                    descuento.getTipoDescuento()
            );

            sentencia.setBigDecimal(
                    5,
                    descuento.getValorDescuento()
            );

            colocarFechaOpcional(
                    sentencia,
                    6,
                    descuento.getFechaInicio()
            );

            colocarFechaOpcional(
                    sentencia,
                    7,
                    descuento.getFechaFin()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Descuento descuento
    ) throws SQLException {

        String sql = "UPDATE descuento SET nombre_descuento = ?, descripcion = ?, tipo_descuento = ?, valor_descuento = ?, fecha_inicio = ?, fecha_fin = ? WHERE id_descuento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    descuento.getNombreDescuento()
            );

            sentencia.setString(
                    2,
                    descuento.getDescripcion()
            );

            sentencia.setString(
                    3,
                    descuento.getTipoDescuento()
            );

            sentencia.setBigDecimal(
                    4,
                    descuento.getValorDescuento()
            );

            colocarFechaOpcional(
                    sentencia,
                    5,
                    descuento.getFechaInicio()
            );

            colocarFechaOpcional(
                    sentencia,
                    6,
                    descuento.getFechaFin()
            );

            sentencia.setLong(
                    7,
                    descuento.getIdDescuento()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean finalizarVigencia(
            Long idDescuento,
            java.time.LocalDate fechaFin
    ) throws SQLException {

        String sql = "UPDATE descuento SET fecha_fin = ? WHERE id_descuento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            colocarFechaOpcional(
                    sentencia,
                    1,
                    fechaFin
            );

            sentencia.setLong(
                    2,
                    idDescuento
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Descuento buscar(
            Long idDescuento
    ) throws SQLException {

        String sql = "SELECT id_descuento, codigo_descuento, nombre_descuento, descripcion, tipo_descuento, valor_descuento, fecha_inicio, fecha_fin FROM descuento WHERE id_descuento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idDescuento);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirDescuento(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Descuento> listar(
            String criterio
    ) throws SQLException {

        List<Descuento> lista = new ArrayList<>();
        String sql = "SELECT id_descuento, codigo_descuento, nombre_descuento, descripcion, tipo_descuento, valor_descuento, fecha_inicio, fecha_fin FROM descuento WHERE codigo_descuento ILIKE ? OR nombre_descuento ILIKE ? OR descripcion ILIKE ? OR tipo_descuento ILIKE ? ORDER BY id_descuento";

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
                            convertirDescuento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idDescuento
    ) throws SQLException {

        String sql = "DELETE FROM descuento WHERE id_descuento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idDescuento);

            return sentencia.executeUpdate() > 0;
        }
    }

    private void bloquearGeneracionCodigo(
            Connection conexion
    ) throws SQLException {

        String sql = "SELECT pg_advisory_xact_lock(?)";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    CLAVE_BLOQUEO_CODIGO
            );

            sentencia.execute();
        }
    }

    private String generarSiguienteCodigo(
            Connection conexion
    ) throws SQLException {

        String sql = "SELECT codigo_descuento FROM descuento "
                + "WHERE codigo_descuento ~ ? "
                + "ORDER BY CAST(SUBSTRING(codigo_descuento FROM 3) AS INTEGER) DESC "
                + "LIMIT 1";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    GeneradorCodigos.crearPatronPostgreSQL(
                            PREFIJO_CODIGO
                    )
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return GeneradorCodigos.generarSiguienteCodigo(
                            PREFIJO_CODIGO,
                            resultado.getString(
                                    "codigo_descuento"
                            )
                    );
                }
            }
        }

        return GeneradorCodigos.construirCodigo(
                PREFIJO_CODIGO,
                GeneradorCodigos.VALOR_INICIAL
        );
    }

    private void colocarFechaOpcional(
            PreparedStatement sentencia,
            int posicion,
            java.time.LocalDate fecha
    ) throws SQLException {

        if (fecha == null) {
            sentencia.setNull(
                    posicion,
                    Types.DATE
            );
        } else {
            sentencia.setDate(
                    posicion,
                    Date.valueOf(
                            fecha
                    )
            );
        }
    }

    private Descuento convertirDescuento(
            ResultSet resultado
    ) throws SQLException {

        Descuento descuento = new Descuento();

        descuento.setIdDescuento(
                resultado.getLong("id_descuento")
        );

        descuento.setCodigoDescuento(
                resultado.getString("codigo_descuento")
        );

        descuento.setNombreDescuento(
                resultado.getString("nombre_descuento")
        );

        descuento.setDescripcion(
                resultado.getString("descripcion")
        );

        descuento.setTipoDescuento(
                resultado.getString("tipo_descuento")
        );

        descuento.setValorDescuento(
                resultado.getBigDecimal("valor_descuento")
        );

        descuento.setFechaInicio(
                resultado.getDate("fecha_inicio") == null
                ? null
                : resultado.getDate("fecha_inicio").toLocalDate()
        );

        descuento.setFechaFin(
                resultado.getDate("fecha_fin") == null
                ? null
                : resultado.getDate("fecha_fin").toLocalDate()
        );

        return descuento;
    }
}
