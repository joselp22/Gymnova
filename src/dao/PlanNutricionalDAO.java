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
import modelo.PlanNutricional;
import utilidades.GeneradorCodigos;

/**
 * DAO para la tabla plan_nutricional.
 *
 * @author Usuario
 */
public class PlanNutricionalDAO {

    private static final String PREFIJO_CODIGO
            = GeneradorCodigos.PREFIJO_PLAN_NUTRICIONAL;
    private static final long CLAVE_BLOQUEO_CODIGO
            = GeneradorCodigos.crearClaveBloqueoAdvisory(
                    "plan_nutricional.codigo_plan"
            );

    public boolean guardar(
            PlanNutricional planNutricional
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

            planNutricional.setCodigoPlan(
                    generarSiguienteCodigo(
                            conexion
                    )
            );

            boolean guardado = insertar(
                    conexion,
                    planNutricional
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
            PlanNutricional planNutricional
    ) throws SQLException {

        String sql = "INSERT INTO plan_nutricional (codigo_plan, nombre_plan, fecha_creacion, fecha_fin, calorias_objetivo, proteinas_objetivo_g, carbohidratos_objetivo_g, restricciones_generales, estado_plan, fecha_inicio, id_cliente, id_nutricionista) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    planNutricional.getCodigoPlan()
            );

            sentencia.setString(
                    2,
                    planNutricional.getNombrePlan()
            );

            if (planNutricional.getFechaCreacion() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            planNutricional.getFechaCreacion()
                    )
            );
        }

            if (planNutricional.getFechaFin() == null) {
            sentencia.setDate(
                    4,
                    null
            );
        } else {
            sentencia.setDate(
                    4,
                    Date.valueOf(
                            planNutricional.getFechaFin()
                    )
            );
        }

            sentencia.setInt(
                    5,
                    planNutricional.getCaloriasObjetivo()
            );

            sentencia.setBigDecimal(
                    6,
                    planNutricional.getProteinasObjetivoG()
            );

            sentencia.setBigDecimal(
                    7,
                    planNutricional.getCarbohidratosObjetivoG()
            );

            sentencia.setString(
                    8,
                    planNutricional.getRestriccionesGenerales()
            );

            sentencia.setString(
                    9,
                    planNutricional.getEstadoPlan()
            );

            if (planNutricional.getFechaInicio() == null) {
            sentencia.setDate(
                    10,
                    null
            );
        } else {
            sentencia.setDate(
                    10,
                    Date.valueOf(
                            planNutricional.getFechaInicio()
                    )
            );
        }

            sentencia.setLong(
                    11,
                    planNutricional.getIdCliente()
            );

            sentencia.setLong(
                    12,
                    planNutricional.getIdNutricionista()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            PlanNutricional planNutricional
    ) throws SQLException {

        String sql = "UPDATE plan_nutricional SET nombre_plan = ?, fecha_creacion = ?, fecha_fin = ?, calorias_objetivo = ?, proteinas_objetivo_g = ?, carbohidratos_objetivo_g = ?, restricciones_generales = ?, estado_plan = ?, fecha_inicio = ?, id_cliente = ?, id_nutricionista = ? WHERE id_plan_nutricional = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    planNutricional.getNombrePlan()
            );

            if (planNutricional.getFechaCreacion() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            planNutricional.getFechaCreacion()
                    )
            );
        }

            if (planNutricional.getFechaFin() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            planNutricional.getFechaFin()
                    )
            );
        }

            sentencia.setInt(
                    4,
                    planNutricional.getCaloriasObjetivo()
            );

            sentencia.setBigDecimal(
                    5,
                    planNutricional.getProteinasObjetivoG()
            );

            sentencia.setBigDecimal(
                    6,
                    planNutricional.getCarbohidratosObjetivoG()
            );

            sentencia.setString(
                    7,
                    planNutricional.getRestriccionesGenerales()
            );

            sentencia.setString(
                    8,
                    planNutricional.getEstadoPlan()
            );

            if (planNutricional.getFechaInicio() == null) {
            sentencia.setDate(
                    9,
                    null
            );
        } else {
            sentencia.setDate(
                    9,
                    Date.valueOf(
                            planNutricional.getFechaInicio()
                    )
            );
        }

            sentencia.setLong(
                    10,
                    planNutricional.getIdCliente()
            );

            sentencia.setLong(
                    11,
                    planNutricional.getIdNutricionista()
            );

            sentencia.setLong(
                    12,
                    planNutricional.getIdPlanNutricional()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public PlanNutricional buscar(
            Long idPlanNutricional
    ) throws SQLException {

        String sql = "SELECT id_plan_nutricional, codigo_plan, nombre_plan, fecha_creacion, fecha_fin, calorias_objetivo, proteinas_objetivo_g, carbohidratos_objetivo_g, restricciones_generales, estado_plan, fecha_inicio, id_cliente, id_nutricionista FROM plan_nutricional WHERE id_plan_nutricional = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idPlanNutricional);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirPlanNutricional(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<PlanNutricional> listar(
            String criterio
    ) throws SQLException {

        List<PlanNutricional> lista = new ArrayList<>();
        String sql = "SELECT id_plan_nutricional, codigo_plan, nombre_plan, fecha_creacion, fecha_fin, calorias_objetivo, proteinas_objetivo_g, carbohidratos_objetivo_g, restricciones_generales, estado_plan, fecha_inicio, id_cliente, id_nutricionista FROM plan_nutricional WHERE codigo_plan ILIKE ? OR nombre_plan ILIKE ? OR restricciones_generales ILIKE ? OR estado_plan ILIKE ? ORDER BY id_plan_nutricional";

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
                            convertirPlanNutricional(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idPlanNutricional
    ) throws SQLException {

        String sql = "UPDATE plan_nutricional SET estado_plan = 'SUSPENDIDO' WHERE id_plan_nutricional = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idPlanNutricional);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idPlanNutricional
    ) throws SQLException {

        String sql = "DELETE FROM plan_nutricional WHERE id_plan_nutricional = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idPlanNutricional);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<PlanNutricional> listarPorIdCliente(
            Long idCliente
    ) throws SQLException {

        List<PlanNutricional> lista = new ArrayList<>();
        String sql = "SELECT id_plan_nutricional, codigo_plan, nombre_plan, fecha_creacion, fecha_fin, calorias_objetivo, proteinas_objetivo_g, carbohidratos_objetivo_g, restricciones_generales, estado_plan, fecha_inicio, id_cliente, id_nutricionista FROM plan_nutricional WHERE id_cliente = ? ORDER BY id_plan_nutricional";

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
                            convertirPlanNutricional(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<PlanNutricional> listarPorIdNutricionista(
            Long idNutricionista
    ) throws SQLException {

        List<PlanNutricional> lista = new ArrayList<>();
        String sql = "SELECT id_plan_nutricional, codigo_plan, nombre_plan, fecha_creacion, fecha_fin, calorias_objetivo, proteinas_objetivo_g, carbohidratos_objetivo_g, restricciones_generales, estado_plan, fecha_inicio, id_cliente, id_nutricionista FROM plan_nutricional WHERE id_nutricionista = ? ORDER BY id_plan_nutricional";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idNutricionista
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirPlanNutricional(
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

        String sql = "SELECT codigo_plan FROM plan_nutricional "
                + "WHERE codigo_plan ~ ? "
                + "ORDER BY CAST(SUBSTRING(codigo_plan FROM 3) AS INTEGER) DESC "
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
                                    "codigo_plan"
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

    private PlanNutricional convertirPlanNutricional(
            ResultSet resultado
    ) throws SQLException {

        PlanNutricional planNutricional = new PlanNutricional();

        planNutricional.setIdPlanNutricional(
                resultado.getLong("id_plan_nutricional")
        );

        planNutricional.setCodigoPlan(
                resultado.getString("codigo_plan")
        );

        planNutricional.setNombrePlan(
                resultado.getString("nombre_plan")
        );

        planNutricional.setFechaCreacion(
                resultado.getDate("fecha_creacion") == null
                ? null
                : resultado.getDate("fecha_creacion").toLocalDate()
        );

        planNutricional.setFechaFin(
                resultado.getDate("fecha_fin") == null
                ? null
                : resultado.getDate("fecha_fin").toLocalDate()
        );

        planNutricional.setCaloriasObjetivo(
                resultado.getInt("calorias_objetivo")
        );

        planNutricional.setProteinasObjetivoG(
                resultado.getBigDecimal("proteinas_objetivo_g")
        );

        planNutricional.setCarbohidratosObjetivoG(
                resultado.getBigDecimal("carbohidratos_objetivo_g")
        );

        planNutricional.setRestriccionesGenerales(
                resultado.getString("restricciones_generales")
        );

        planNutricional.setEstadoPlan(
                resultado.getString("estado_plan")
        );

        planNutricional.setFechaInicio(
                resultado.getDate("fecha_inicio") == null
                ? null
                : resultado.getDate("fecha_inicio").toLocalDate()
        );

        planNutricional.setIdCliente(
                resultado.getLong("id_cliente")
        );

        planNutricional.setIdNutricionista(
                resultado.getLong("id_nutricionista")
        );

        return planNutricional;
    }
}
