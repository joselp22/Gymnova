package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import modelo.IncluyeAlimento;

/**
 * DAO para la tabla incluye_alimento.
 *
 * @author Usuario
 */
public class IncluyeAlimentoDAO {

    public boolean guardar(
            IncluyeAlimento incluyeAlimento
    ) throws SQLException {

        String sql = "INSERT INTO incluye_alimento (dia_semana, tipo_comida, hora_consumo, cantidad, unidad_medida, orden_comida, indicaciones, estado_detalle, id_plan_nutricional, id_alimento) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    incluyeAlimento.getDiaSemana()
            );

            sentencia.setString(
                    2,
                    incluyeAlimento.getTipoComida()
            );

            if (incluyeAlimento.getHoraConsumo() == null) {
            sentencia.setTime(
                    3,
                    null
            );
        } else {
            sentencia.setTime(
                    3,
                    Time.valueOf(
                            incluyeAlimento.getHoraConsumo()
                    )
            );
        }

            sentencia.setBigDecimal(
                    4,
                    incluyeAlimento.getCantidad()
            );

            sentencia.setString(
                    5,
                    incluyeAlimento.getUnidadMedida()
            );

            sentencia.setInt(
                    6,
                    incluyeAlimento.getOrdenComida()
            );

            sentencia.setString(
                    7,
                    incluyeAlimento.getIndicaciones()
            );

            sentencia.setString(
                    8,
                    incluyeAlimento.getEstadoDetalle()
            );

            sentencia.setLong(
                    9,
                    incluyeAlimento.getIdPlanNutricional()
            );

            sentencia.setLong(
                    10,
                    incluyeAlimento.getIdAlimento()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            IncluyeAlimento incluyeAlimento
    ) throws SQLException {

        String sql = "UPDATE incluye_alimento SET dia_semana = ?, tipo_comida = ?, hora_consumo = ?, cantidad = ?, unidad_medida = ?, orden_comida = ?, indicaciones = ?, estado_detalle = ?, id_plan_nutricional = ?, id_alimento = ? WHERE id_detalle_plan = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    incluyeAlimento.getDiaSemana()
            );

            sentencia.setString(
                    2,
                    incluyeAlimento.getTipoComida()
            );

            if (incluyeAlimento.getHoraConsumo() == null) {
            sentencia.setTime(
                    3,
                    null
            );
        } else {
            sentencia.setTime(
                    3,
                    Time.valueOf(
                            incluyeAlimento.getHoraConsumo()
                    )
            );
        }

            sentencia.setBigDecimal(
                    4,
                    incluyeAlimento.getCantidad()
            );

            sentencia.setString(
                    5,
                    incluyeAlimento.getUnidadMedida()
            );

            sentencia.setInt(
                    6,
                    incluyeAlimento.getOrdenComida()
            );

            sentencia.setString(
                    7,
                    incluyeAlimento.getIndicaciones()
            );

            sentencia.setString(
                    8,
                    incluyeAlimento.getEstadoDetalle()
            );

            sentencia.setLong(
                    9,
                    incluyeAlimento.getIdPlanNutricional()
            );

            sentencia.setLong(
                    10,
                    incluyeAlimento.getIdAlimento()
            );

            sentencia.setLong(
                    11,
                    incluyeAlimento.getIdDetallePlan()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public IncluyeAlimento buscar(
            Long idDetallePlan
    ) throws SQLException {

        String sql = "SELECT id_detalle_plan, dia_semana, tipo_comida, hora_consumo, cantidad, unidad_medida, orden_comida, indicaciones, estado_detalle, id_plan_nutricional, id_alimento FROM incluye_alimento WHERE id_detalle_plan = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idDetallePlan);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirIncluyeAlimento(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<IncluyeAlimento> listar(
            String criterio
    ) throws SQLException {

        List<IncluyeAlimento> lista = new ArrayList<>();
        String sql = "SELECT id_detalle_plan, dia_semana, tipo_comida, hora_consumo, cantidad, unidad_medida, orden_comida, indicaciones, estado_detalle, id_plan_nutricional, id_alimento FROM incluye_alimento WHERE dia_semana ILIKE ? OR tipo_comida ILIKE ? OR unidad_medida ILIKE ? OR indicaciones ILIKE ? OR estado_detalle ILIKE ? ORDER BY id_detalle_plan";

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
                            convertirIncluyeAlimento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idDetallePlan
    ) throws SQLException {

        String sql = "UPDATE incluye_alimento SET estado_detalle = 'CANCELADO' WHERE id_detalle_plan = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idDetallePlan);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idDetallePlan
    ) throws SQLException {

        String sql = "DELETE FROM incluye_alimento WHERE id_detalle_plan = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idDetallePlan);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<IncluyeAlimento> listarPorIdPlanNutricional(
            Long idPlanNutricional
    ) throws SQLException {

        List<IncluyeAlimento> lista = new ArrayList<>();
        String sql = "SELECT id_detalle_plan, dia_semana, tipo_comida, hora_consumo, cantidad, unidad_medida, orden_comida, indicaciones, estado_detalle, id_plan_nutricional, id_alimento FROM incluye_alimento WHERE id_plan_nutricional = ? ORDER BY id_detalle_plan";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idPlanNutricional
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirIncluyeAlimento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<IncluyeAlimento> listarPorIdAlimento(
            Long idAlimento
    ) throws SQLException {

        List<IncluyeAlimento> lista = new ArrayList<>();
        String sql = "SELECT id_detalle_plan, dia_semana, tipo_comida, hora_consumo, cantidad, unidad_medida, orden_comida, indicaciones, estado_detalle, id_plan_nutricional, id_alimento FROM incluye_alimento WHERE id_alimento = ? ORDER BY id_detalle_plan";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idAlimento
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirIncluyeAlimento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private IncluyeAlimento convertirIncluyeAlimento(
            ResultSet resultado
    ) throws SQLException {

        IncluyeAlimento incluyeAlimento = new IncluyeAlimento();

        incluyeAlimento.setIdDetallePlan(
                resultado.getLong("id_detalle_plan")
        );

        incluyeAlimento.setDiaSemana(
                resultado.getString("dia_semana")
        );

        incluyeAlimento.setTipoComida(
                resultado.getString("tipo_comida")
        );

        incluyeAlimento.setHoraConsumo(
                resultado.getTime("hora_consumo") == null
                ? null
                : resultado.getTime("hora_consumo").toLocalTime()
        );

        incluyeAlimento.setCantidad(
                resultado.getBigDecimal("cantidad")
        );

        incluyeAlimento.setUnidadMedida(
                resultado.getString("unidad_medida")
        );

        incluyeAlimento.setOrdenComida(
                resultado.getInt("orden_comida")
        );

        incluyeAlimento.setIndicaciones(
                resultado.getString("indicaciones")
        );

        incluyeAlimento.setEstadoDetalle(
                resultado.getString("estado_detalle")
        );

        incluyeAlimento.setIdPlanNutricional(
                resultado.getLong("id_plan_nutricional")
        );

        incluyeAlimento.setIdAlimento(
                resultado.getLong("id_alimento")
        );

        return incluyeAlimento;
    }
}
