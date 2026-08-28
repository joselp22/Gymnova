package dao;

import conexion.ConexionPostgreSQL;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.IndicadorSalud;

/**
 * DAO para la tabla indicador_salud.
 *
 * @author Usuario
 */
public class IndicadorSaludDAO {

    public boolean guardar(
            IndicadorSalud indicadorSalud
    ) throws SQLException {

        String sql = "INSERT INTO indicador_salud (descripcion, unidad_medida, valor_minimo_referencia, categoria, estado_indicador, nombre_indicador) VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    indicadorSalud.getDescripcion()
            );

            sentencia.setString(
                    2,
                    indicadorSalud.getUnidadMedida()
            );

            sentencia.setBigDecimal(
                    3,
                    indicadorSalud.getValorMinimoReferencia()
            );

            sentencia.setString(
                    4,
                    indicadorSalud.getCategoria()
            );

            sentencia.setBoolean(
                    5,
                    indicadorSalud.isEstadoIndicador()
            );

            sentencia.setString(
                    6,
                    indicadorSalud.getNombreIndicador()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            IndicadorSalud indicadorSalud
    ) throws SQLException {

        String sql = "UPDATE indicador_salud SET descripcion = ?, unidad_medida = ?, valor_minimo_referencia = ?, categoria = ?, estado_indicador = ?, nombre_indicador = ? WHERE id_indicador = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    indicadorSalud.getDescripcion()
            );

            sentencia.setString(
                    2,
                    indicadorSalud.getUnidadMedida()
            );

            sentencia.setBigDecimal(
                    3,
                    indicadorSalud.getValorMinimoReferencia()
            );

            sentencia.setString(
                    4,
                    indicadorSalud.getCategoria()
            );

            sentencia.setBoolean(
                    5,
                    indicadorSalud.isEstadoIndicador()
            );

            sentencia.setString(
                    6,
                    indicadorSalud.getNombreIndicador()
            );

            sentencia.setLong(
                    7,
                    indicadorSalud.getIdIndicador()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public IndicadorSalud buscar(
            Long idIndicador
    ) throws SQLException {

        String sql = "SELECT id_indicador, descripcion, unidad_medida, valor_minimo_referencia, categoria, estado_indicador, nombre_indicador FROM indicador_salud WHERE id_indicador = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idIndicador);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirIndicadorSalud(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<IndicadorSalud> listar(
            String criterio
    ) throws SQLException {

        List<IndicadorSalud> lista = new ArrayList<>();
        String sql = "SELECT id_indicador, descripcion, unidad_medida, valor_minimo_referencia, categoria, estado_indicador, nombre_indicador FROM indicador_salud WHERE descripcion ILIKE ? OR unidad_medida ILIKE ? OR categoria ILIKE ? OR nombre_indicador ILIKE ? ORDER BY id_indicador";

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
                            convertirIndicadorSalud(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idIndicador
    ) throws SQLException {

        String sql = "UPDATE indicador_salud SET estado_indicador = FALSE WHERE id_indicador = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idIndicador);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idIndicador
    ) throws SQLException {

        String sql = "DELETE FROM indicador_salud WHERE id_indicador = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idIndicador);

            return sentencia.executeUpdate() > 0;
        }
    }



    private IndicadorSalud convertirIndicadorSalud(
            ResultSet resultado
    ) throws SQLException {

        IndicadorSalud indicadorSalud = new IndicadorSalud();

        indicadorSalud.setIdIndicador(
                resultado.getLong("id_indicador")
        );

        indicadorSalud.setDescripcion(
                resultado.getString("descripcion")
        );

        indicadorSalud.setUnidadMedida(
                resultado.getString("unidad_medida")
        );

        indicadorSalud.setValorMinimoReferencia(
                resultado.getBigDecimal("valor_minimo_referencia")
        );

        indicadorSalud.setCategoria(
                resultado.getString("categoria")
        );

        indicadorSalud.setEstadoIndicador(
                resultado.getBoolean("estado_indicador")
        );

        indicadorSalud.setNombreIndicador(
                resultado.getString("nombre_indicador")
        );

        return indicadorSalud;
    }
}
