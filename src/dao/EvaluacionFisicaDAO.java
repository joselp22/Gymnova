package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.EvaluacionFisica;
import utilidades.GeneradorCodigos;

/**
 * DAO para la tabla evaluacion_fisica.
 *
 * @author Usuario
 */
public class EvaluacionFisicaDAO {

    private static final String PREFIJO_CODIGO
            = GeneradorCodigos.PREFIJO_EVALUACION_FISICA;
    private static final long CLAVE_BLOQUEO_CODIGO
            = GeneradorCodigos.crearClaveBloqueoAdvisory(
                    "evaluacion_fisica.codigo_evaluacion"
            );

    public boolean guardar(
            EvaluacionFisica evaluacionFisica
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

            evaluacionFisica.setCodigoEvaluacion(
                    generarSiguienteCodigo(
                            conexion
                    )
            );

            boolean guardado = insertar(
                    conexion,
                    evaluacionFisica
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
            EvaluacionFisica evaluacionFisica
    ) throws SQLException {

        String sql = "INSERT INTO evaluacion_fisica (codigo_evaluacion, fecha_evaluacion, tipo_evaluacion, motivo, condicion_general, nivel_riesgo, proxima_evaluacion, estado_evaluacion, id_cliente, id_entrenador) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    evaluacionFisica.getCodigoEvaluacion()
            );

            if (evaluacionFisica.getFechaEvaluacion() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            evaluacionFisica.getFechaEvaluacion()
                    )
            );
        }

            sentencia.setString(
                    3,
                    evaluacionFisica.getTipoEvaluacion()
            );

            sentencia.setString(
                    4,
                    evaluacionFisica.getMotivo()
            );

            sentencia.setString(
                    5,
                    evaluacionFisica.getCondicionGeneral()
            );

            sentencia.setString(
                    6,
                    evaluacionFisica.getNivelRiesgo()
            );

            if (evaluacionFisica.getProximaEvaluacion() == null) {
                sentencia.setDate(7, null);
            } else {
                sentencia.setDate(
                        7,
                        Date.valueOf(evaluacionFisica.getProximaEvaluacion())
                );
            }

            sentencia.setString(
                    8,
                    evaluacionFisica.getEstadoEvaluacion()
            );

            sentencia.setLong(
                    9,
                    evaluacionFisica.getIdCliente()
            );

            sentencia.setLong(
                    10,
                    evaluacionFisica.getIdEntrenador()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            EvaluacionFisica evaluacionFisica
    ) throws SQLException {

        String sql = "UPDATE evaluacion_fisica SET fecha_evaluacion = ?, tipo_evaluacion = ?, motivo = ?, condicion_general = ?, nivel_riesgo = ?, proxima_evaluacion = ?, estado_evaluacion = ?, id_cliente = ?, id_entrenador = ? WHERE id_evaluacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (evaluacionFisica.getFechaEvaluacion() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            evaluacionFisica.getFechaEvaluacion()
                    )
            );
        }

            sentencia.setString(
                    2,
                    evaluacionFisica.getTipoEvaluacion()
            );

            sentencia.setString(
                    3,
                    evaluacionFisica.getMotivo()
            );

            sentencia.setString(
                    4,
                    evaluacionFisica.getCondicionGeneral()
            );

            sentencia.setString(
                    5,
                    evaluacionFisica.getNivelRiesgo()
            );

            if (evaluacionFisica.getProximaEvaluacion() == null) {
                sentencia.setDate(6, null);
            } else {
                sentencia.setDate(
                        6,
                        Date.valueOf(evaluacionFisica.getProximaEvaluacion())
                );
            }

            sentencia.setString(
                    7,
                    evaluacionFisica.getEstadoEvaluacion()
            );

            sentencia.setLong(
                    8,
                    evaluacionFisica.getIdCliente()
            );

            sentencia.setLong(
                    9,
                    evaluacionFisica.getIdEntrenador()
            );

            sentencia.setLong(
                    10,
                    evaluacionFisica.getIdEvaluacion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public EvaluacionFisica buscar(
            Long idEvaluacion
    ) throws SQLException {

        String sql = "SELECT codigo_evaluacion, fecha_evaluacion, tipo_evaluacion, motivo, condicion_general, nivel_riesgo, proxima_evaluacion, estado_evaluacion, id_evaluacion, id_cliente, id_entrenador FROM evaluacion_fisica WHERE id_evaluacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idEvaluacion);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirEvaluacionFisica(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<EvaluacionFisica> listar(
            String criterio
    ) throws SQLException {

        List<EvaluacionFisica> lista = new ArrayList<>();
        String sql = "SELECT codigo_evaluacion, fecha_evaluacion, tipo_evaluacion, motivo, condicion_general, nivel_riesgo, proxima_evaluacion, estado_evaluacion, id_evaluacion, id_cliente, id_entrenador FROM evaluacion_fisica WHERE codigo_evaluacion ILIKE ? OR tipo_evaluacion ILIKE ? OR motivo ILIKE ? OR condicion_general ILIKE ? OR nivel_riesgo ILIKE ? OR CAST(proxima_evaluacion AS TEXT) ILIKE ? OR estado_evaluacion ILIKE ? ORDER BY id_evaluacion";

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
            sentencia.setString(7, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirEvaluacionFisica(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idEvaluacion
    ) throws SQLException {

        String sql = "UPDATE evaluacion_fisica SET estado_evaluacion = 'ANULADA' WHERE id_evaluacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idEvaluacion);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idEvaluacion
    ) throws SQLException {

        String sql = "DELETE FROM evaluacion_fisica WHERE id_evaluacion = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idEvaluacion);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<EvaluacionFisica> listarPorIdCliente(
            Long idCliente
    ) throws SQLException {

        List<EvaluacionFisica> lista = new ArrayList<>();
        String sql = "SELECT codigo_evaluacion, fecha_evaluacion, tipo_evaluacion, motivo, condicion_general, nivel_riesgo, proxima_evaluacion, estado_evaluacion, id_evaluacion, id_cliente, id_entrenador FROM evaluacion_fisica WHERE id_cliente = ? ORDER BY id_evaluacion";

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
                            convertirEvaluacionFisica(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<EvaluacionFisica> listarPorIdEntrenador(
            Long idEntrenador
    ) throws SQLException {

        List<EvaluacionFisica> lista = new ArrayList<>();
        String sql = "SELECT codigo_evaluacion, fecha_evaluacion, tipo_evaluacion, motivo, condicion_general, nivel_riesgo, proxima_evaluacion, estado_evaluacion, id_evaluacion, id_cliente, id_entrenador FROM evaluacion_fisica WHERE id_entrenador = ? ORDER BY id_evaluacion";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEntrenador
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirEvaluacionFisica(
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

        String sql = "SELECT codigo_evaluacion FROM evaluacion_fisica "
                + "WHERE codigo_evaluacion ~ ? "
                + "ORDER BY CAST(SUBSTRING(codigo_evaluacion FROM 3) AS INTEGER) DESC "
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
                                    "codigo_evaluacion"
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

    private EvaluacionFisica convertirEvaluacionFisica(
            ResultSet resultado
    ) throws SQLException {

        EvaluacionFisica evaluacionFisica = new EvaluacionFisica();

        evaluacionFisica.setCodigoEvaluacion(
                resultado.getString("codigo_evaluacion")
        );

        evaluacionFisica.setFechaEvaluacion(
                resultado.getDate("fecha_evaluacion") == null
                ? null
                : resultado.getDate("fecha_evaluacion").toLocalDate()
        );

        evaluacionFisica.setTipoEvaluacion(
                resultado.getString("tipo_evaluacion")
        );

        evaluacionFisica.setMotivo(
                resultado.getString("motivo")
        );

        evaluacionFisica.setCondicionGeneral(
                resultado.getString("condicion_general")
        );

        evaluacionFisica.setNivelRiesgo(
                resultado.getString("nivel_riesgo")
        );

        evaluacionFisica.setProximaEvaluacion(
                resultado.getDate("proxima_evaluacion") == null
                ? null
                : resultado.getDate("proxima_evaluacion").toLocalDate()
        );

        evaluacionFisica.setEstadoEvaluacion(
                resultado.getString("estado_evaluacion")
        );

        evaluacionFisica.setIdEvaluacion(
                resultado.getLong("id_evaluacion")
        );

        evaluacionFisica.setIdCliente(
                resultado.getLong("id_cliente")
        );

        evaluacionFisica.setIdEntrenador(
                resultado.getLong("id_entrenador")
        );

        return evaluacionFisica;
    }
}
