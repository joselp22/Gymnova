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
import modelo.ResultadoIndicador;

/**
 * DAO para la tabla resultado_indicador.
 *
 * @author Usuario
 */
public class ResultadoIndicadorDAO {

    public boolean guardar(
            ResultadoIndicador resultadoIndicador
    ) throws SQLException {

        String sql = "INSERT INTO resultado_indicador (valor_obtenido, clasificacion, fuera_de_rango, observaciones, fecha_registro, id_evaluacion, id_indicador) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setBigDecimal(
                    1,
                    resultadoIndicador.getValorObtenido()
            );

            sentencia.setString(
                    2,
                    resultadoIndicador.getClasificacion()
            );

            sentencia.setBoolean(
                    3,
                    resultadoIndicador.isFueraDeRango()
            );

            sentencia.setString(
                    4,
                    resultadoIndicador.getObservaciones()
            );

            if (resultadoIndicador.getFechaRegistro() == null) {
            sentencia.setDate(
                    5,
                    null
            );
        } else {
            sentencia.setDate(
                    5,
                    Date.valueOf(
                            resultadoIndicador.getFechaRegistro()
                    )
            );
        }

            sentencia.setLong(
                    6,
                    resultadoIndicador.getIdEvaluacion()
            );

            sentencia.setLong(
                    7,
                    resultadoIndicador.getIdIndicador()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            ResultadoIndicador resultadoIndicador
    ) throws SQLException {

        String sql = "UPDATE resultado_indicador SET valor_obtenido = ?, clasificacion = ?, fuera_de_rango = ?, observaciones = ?, fecha_registro = ?, id_evaluacion = ?, id_indicador = ? WHERE id_resultado = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setBigDecimal(
                    1,
                    resultadoIndicador.getValorObtenido()
            );

            sentencia.setString(
                    2,
                    resultadoIndicador.getClasificacion()
            );

            sentencia.setBoolean(
                    3,
                    resultadoIndicador.isFueraDeRango()
            );

            sentencia.setString(
                    4,
                    resultadoIndicador.getObservaciones()
            );

            if (resultadoIndicador.getFechaRegistro() == null) {
            sentencia.setDate(
                    5,
                    null
            );
        } else {
            sentencia.setDate(
                    5,
                    Date.valueOf(
                            resultadoIndicador.getFechaRegistro()
                    )
            );
        }

            sentencia.setLong(
                    6,
                    resultadoIndicador.getIdEvaluacion()
            );

            sentencia.setLong(
                    7,
                    resultadoIndicador.getIdIndicador()
            );

            sentencia.setLong(
                    8,
                    resultadoIndicador.getIdResultado()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public ResultadoIndicador buscar(
            Long idResultado
    ) throws SQLException {

        String sql = "SELECT id_resultado, valor_obtenido, clasificacion, fuera_de_rango, observaciones, fecha_registro, id_evaluacion, id_indicador FROM resultado_indicador WHERE id_resultado = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idResultado);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirResultadoIndicador(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<ResultadoIndicador> listar(
            String criterio
    ) throws SQLException {

        List<ResultadoIndicador> lista = new ArrayList<>();
        String sql = "SELECT id_resultado, valor_obtenido, clasificacion, fuera_de_rango, observaciones, fecha_registro, id_evaluacion, id_indicador FROM resultado_indicador WHERE clasificacion ILIKE ? OR observaciones ILIKE ? ORDER BY id_resultado";

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
                            convertirResultadoIndicador(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idResultado
    ) throws SQLException {

        String sql = "DELETE FROM resultado_indicador WHERE id_resultado = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idResultado);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<ResultadoIndicador> listarPorIdEvaluacion(
            Long idEvaluacion
    ) throws SQLException {

        List<ResultadoIndicador> lista = new ArrayList<>();
        String sql = "SELECT id_resultado, valor_obtenido, clasificacion, fuera_de_rango, observaciones, fecha_registro, id_evaluacion, id_indicador FROM resultado_indicador WHERE id_evaluacion = ? ORDER BY id_resultado";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEvaluacion
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirResultadoIndicador(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<ResultadoIndicador> listarPorIdIndicador(
            Long idIndicador
    ) throws SQLException {

        List<ResultadoIndicador> lista = new ArrayList<>();
        String sql = "SELECT id_resultado, valor_obtenido, clasificacion, fuera_de_rango, observaciones, fecha_registro, id_evaluacion, id_indicador FROM resultado_indicador WHERE id_indicador = ? ORDER BY id_resultado";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idIndicador
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirResultadoIndicador(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private ResultadoIndicador convertirResultadoIndicador(
            ResultSet resultado
    ) throws SQLException {

        ResultadoIndicador resultadoIndicador = new ResultadoIndicador();

        resultadoIndicador.setIdResultado(
                resultado.getLong("id_resultado")
        );

        resultadoIndicador.setValorObtenido(
                resultado.getBigDecimal("valor_obtenido")
        );

        resultadoIndicador.setClasificacion(
                resultado.getString("clasificacion")
        );

        resultadoIndicador.setFueraDeRango(
                resultado.getBoolean("fuera_de_rango")
        );

        resultadoIndicador.setObservaciones(
                resultado.getString("observaciones")
        );

        resultadoIndicador.setFechaRegistro(
                resultado.getDate("fecha_registro") == null
                ? null
                : resultado.getDate("fecha_registro").toLocalDate()
        );

        resultadoIndicador.setIdEvaluacion(
                resultado.getLong("id_evaluacion")
        );

        resultadoIndicador.setIdIndicador(
                resultado.getLong("id_indicador")
        );

        return resultadoIndicador;
    }
}
