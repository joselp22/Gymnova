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
import modelo.Equipo;
import utilidades.GeneradorCodigos;

/**
 * DAO para la tabla equipo.
 *
 * @author Usuario
 */
public class EquipoDAO {

    private static final String PREFIJO_CODIGO
            = GeneradorCodigos.PREFIJO_EQUIPO;
    private static final long CLAVE_BLOQUEO_CODIGO
            = GeneradorCodigos.crearClaveBloqueoAdvisory(
                    "equipo.codigo_interno"
            );

    public boolean guardar(
            Equipo equipo
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

            equipo.setCodigoInterno(
                    generarSiguienteCodigo(
                            conexion
                    )
            );

            boolean guardado = insertar(
                    conexion,
                    equipo
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
            Equipo equipo
    ) throws SQLException {

        String sql = "INSERT INTO equipo (codigo_interno, nombre_equipo, marca, modelo, fecha_adquisicion, costo_adquisicion, vida_util_anios, ubicacion, estado_equipo, id_tipo_equipo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    equipo.getCodigoInterno()
            );

            sentencia.setString(
                    2,
                    equipo.getNombreEquipo()
            );

            sentencia.setString(
                    3,
                    equipo.getMarca()
            );

            sentencia.setString(
                    4,
                    equipo.getModelo()
            );

            if (equipo.getFechaAdquisicion() == null) {
            sentencia.setDate(
                    5,
                    null
            );
        } else {
            sentencia.setDate(
                    5,
                    Date.valueOf(
                            equipo.getFechaAdquisicion()
                    )
            );
        }

            sentencia.setBigDecimal(
                    6,
                    equipo.getCostoAdquisicion()
            );

            sentencia.setInt(
                    7,
                    equipo.getVidaUtilAnios()
            );

            sentencia.setString(
                    8,
                    equipo.getUbicacion()
            );

            sentencia.setBoolean(
                    9,
                    equipo.isEstadoEquipo()
            );

            sentencia.setLong(
                    10,
                    equipo.getIdTipoEquipo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Equipo equipo
    ) throws SQLException {

        String sql = "UPDATE equipo SET nombre_equipo = ?, marca = ?, modelo = ?, fecha_adquisicion = ?, costo_adquisicion = ?, vida_util_anios = ?, ubicacion = ?, estado_equipo = ?, id_tipo_equipo = ? WHERE id_equipo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    equipo.getNombreEquipo()
            );

            sentencia.setString(
                    2,
                    equipo.getMarca()
            );

            sentencia.setString(
                    3,
                    equipo.getModelo()
            );

            if (equipo.getFechaAdquisicion() == null) {
            sentencia.setDate(
                    4,
                    null
            );
        } else {
            sentencia.setDate(
                    4,
                    Date.valueOf(
                            equipo.getFechaAdquisicion()
                    )
            );
        }

            sentencia.setBigDecimal(
                    5,
                    equipo.getCostoAdquisicion()
            );

            sentencia.setInt(
                    6,
                    equipo.getVidaUtilAnios()
            );

            sentencia.setString(
                    7,
                    equipo.getUbicacion()
            );

            sentencia.setBoolean(
                    8,
                    equipo.isEstadoEquipo()
            );

            sentencia.setLong(
                    9,
                    equipo.getIdTipoEquipo()
            );

            sentencia.setLong(
                    10,
                    equipo.getIdEquipo()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Equipo buscar(
            Long idEquipo
    ) throws SQLException {

        String sql = "SELECT id_equipo, codigo_interno, nombre_equipo, marca, modelo, fecha_adquisicion, costo_adquisicion, vida_util_anios, ubicacion, estado_equipo, id_tipo_equipo FROM equipo WHERE id_equipo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idEquipo);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirEquipo(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Equipo> listar(
            String criterio
    ) throws SQLException {

        List<Equipo> lista = new ArrayList<>();
        String sql = "SELECT id_equipo, codigo_interno, nombre_equipo, marca, modelo, fecha_adquisicion, costo_adquisicion, vida_util_anios, ubicacion, estado_equipo, id_tipo_equipo FROM equipo WHERE codigo_interno ILIKE ? OR nombre_equipo ILIKE ? OR marca ILIKE ? OR modelo ILIKE ? OR ubicacion ILIKE ? ORDER BY id_equipo";

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

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirEquipo(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idEquipo
    ) throws SQLException {

        String sql = "UPDATE equipo SET estado_equipo = FALSE WHERE id_equipo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idEquipo);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idEquipo
    ) throws SQLException {

        String sql = "DELETE FROM equipo WHERE id_equipo = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idEquipo);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Equipo> listarPorIdTipoEquipo(
            Long idTipoEquipo
    ) throws SQLException {

        List<Equipo> lista = new ArrayList<>();
        String sql = "SELECT id_equipo, codigo_interno, nombre_equipo, marca, modelo, fecha_adquisicion, costo_adquisicion, vida_util_anios, ubicacion, estado_equipo, id_tipo_equipo FROM equipo WHERE id_tipo_equipo = ? ORDER BY id_equipo";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idTipoEquipo
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirEquipo(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
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

        String sql = "SELECT codigo_interno FROM equipo "
                + "WHERE codigo_interno ~ ? "
                + "ORDER BY CAST(SUBSTRING(codigo_interno FROM 3) AS INTEGER) DESC "
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
                                    "codigo_interno"
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

    private Equipo convertirEquipo(
            ResultSet resultado
    ) throws SQLException {

        Equipo equipo = new Equipo();

        equipo.setIdEquipo(
                resultado.getLong("id_equipo")
        );

        equipo.setCodigoInterno(
                resultado.getString("codigo_interno")
        );

        equipo.setNombreEquipo(
                resultado.getString("nombre_equipo")
        );

        equipo.setMarca(
                resultado.getString("marca")
        );

        equipo.setModelo(
                resultado.getString("modelo")
        );

        equipo.setFechaAdquisicion(
                resultado.getDate("fecha_adquisicion") == null
                ? null
                : resultado.getDate("fecha_adquisicion").toLocalDate()
        );

        equipo.setCostoAdquisicion(
                resultado.getBigDecimal("costo_adquisicion")
        );

        equipo.setVidaUtilAnios(
                resultado.getInt("vida_util_anios")
        );

        equipo.setUbicacion(
                resultado.getString("ubicacion")
        );

        equipo.setEstadoEquipo(
                resultado.getBoolean("estado_equipo")
        );

        equipo.setIdTipoEquipo(
                resultado.getLong("id_tipo_equipo")
        );

        return equipo;
    }
}
