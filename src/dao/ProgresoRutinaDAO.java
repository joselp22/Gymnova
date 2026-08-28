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
import modelo.ProgresoRutina;

/**
 * DAO para la tabla progreso_rutina.
 *
 * @author Usuario
 */
public class ProgresoRutinaDAO {

    public boolean guardar(
            ProgresoRutina progresoRutina
    ) throws SQLException {

        String sql = "INSERT INTO progreso_rutina (fecha_registro, sesiones_planificadas, sesiones_completadas, peso_corporal, nivel_esfuerzo, estado_progreso, id_cliente, id_rutina) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (progresoRutina.getFechaRegistro() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            progresoRutina.getFechaRegistro()
                    )
            );
        }

            sentencia.setInt(
                    2,
                    progresoRutina.getSesionesPlanificadas()
            );

            sentencia.setInt(
                    3,
                    progresoRutina.getSesionesCompletadas()
            );

            sentencia.setBigDecimal(
                    4,
                    progresoRutina.getPesoCorporal()
            );

            sentencia.setString(
                    5,
                    progresoRutina.getNivelEsfuerzo()
            );

            sentencia.setString(
                    6,
                    progresoRutina.getEstadoProgreso()
            );

            sentencia.setLong(
                    7,
                    progresoRutina.getIdCliente()
            );

            sentencia.setLong(
                    8,
                    progresoRutina.getIdRutina()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            ProgresoRutina progresoRutina
    ) throws SQLException {

        String sql = "UPDATE progreso_rutina SET fecha_registro = ?, sesiones_planificadas = ?, sesiones_completadas = ?, peso_corporal = ?, nivel_esfuerzo = ?, estado_progreso = ?, id_cliente = ?, id_rutina = ? WHERE id_progreso = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            if (progresoRutina.getFechaRegistro() == null) {
            sentencia.setDate(
                    1,
                    null
            );
        } else {
            sentencia.setDate(
                    1,
                    Date.valueOf(
                            progresoRutina.getFechaRegistro()
                    )
            );
        }

            sentencia.setInt(
                    2,
                    progresoRutina.getSesionesPlanificadas()
            );

            sentencia.setInt(
                    3,
                    progresoRutina.getSesionesCompletadas()
            );

            sentencia.setBigDecimal(
                    4,
                    progresoRutina.getPesoCorporal()
            );

            sentencia.setString(
                    5,
                    progresoRutina.getNivelEsfuerzo()
            );

            sentencia.setString(
                    6,
                    progresoRutina.getEstadoProgreso()
            );

            sentencia.setLong(
                    7,
                    progresoRutina.getIdCliente()
            );

            sentencia.setLong(
                    8,
                    progresoRutina.getIdRutina()
            );

            sentencia.setLong(
                    9,
                    progresoRutina.getIdProgreso()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public ProgresoRutina buscar(
            Long idProgreso
    ) throws SQLException {

        String sql = "SELECT id_progreso, fecha_registro, sesiones_planificadas, sesiones_completadas, peso_corporal, nivel_esfuerzo, estado_progreso, id_cliente, id_rutina FROM progreso_rutina WHERE id_progreso = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idProgreso);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirProgresoRutina(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<ProgresoRutina> listar(
            String criterio
    ) throws SQLException {

        List<ProgresoRutina> lista = new ArrayList<>();
        String sql = "SELECT id_progreso, fecha_registro, sesiones_planificadas, sesiones_completadas, peso_corporal, nivel_esfuerzo, estado_progreso, id_cliente, id_rutina FROM progreso_rutina WHERE nivel_esfuerzo ILIKE ? OR estado_progreso ILIKE ? ORDER BY id_progreso";

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
                            convertirProgresoRutina(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idProgreso
    ) throws SQLException {

        String sql = "UPDATE progreso_rutina SET estado_progreso = 'ANULADO' WHERE id_progreso = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idProgreso);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idProgreso
    ) throws SQLException {

        String sql = "DELETE FROM progreso_rutina WHERE id_progreso = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idProgreso);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<ProgresoRutina> listarPorIdCliente(
            Long idCliente
    ) throws SQLException {

        List<ProgresoRutina> lista = new ArrayList<>();
        String sql = "SELECT id_progreso, fecha_registro, sesiones_planificadas, sesiones_completadas, peso_corporal, nivel_esfuerzo, estado_progreso, id_cliente, id_rutina FROM progreso_rutina WHERE id_cliente = ? ORDER BY id_progreso";

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
                            convertirProgresoRutina(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<ProgresoRutina> listarPorIdRutina(
            Long idRutina
    ) throws SQLException {

        List<ProgresoRutina> lista = new ArrayList<>();
        String sql = "SELECT id_progreso, fecha_registro, sesiones_planificadas, sesiones_completadas, peso_corporal, nivel_esfuerzo, estado_progreso, id_cliente, id_rutina FROM progreso_rutina WHERE id_rutina = ? ORDER BY id_progreso";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idRutina
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirProgresoRutina(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private ProgresoRutina convertirProgresoRutina(
            ResultSet resultado
    ) throws SQLException {

        ProgresoRutina progresoRutina = new ProgresoRutina();

        progresoRutina.setIdProgreso(
                resultado.getLong("id_progreso")
        );

        progresoRutina.setFechaRegistro(
                resultado.getDate("fecha_registro") == null
                ? null
                : resultado.getDate("fecha_registro").toLocalDate()
        );

        progresoRutina.setSesionesPlanificadas(
                resultado.getInt("sesiones_planificadas")
        );

        progresoRutina.setSesionesCompletadas(
                resultado.getInt("sesiones_completadas")
        );

        progresoRutina.setPesoCorporal(
                resultado.getBigDecimal("peso_corporal")
        );

        progresoRutina.setNivelEsfuerzo(
                resultado.getString("nivel_esfuerzo")
        );

        progresoRutina.setEstadoProgreso(
                resultado.getString("estado_progreso")
        );

        progresoRutina.setIdCliente(
                resultado.getLong("id_cliente")
        );

        progresoRutina.setIdRutina(
                resultado.getLong("id_rutina")
        );

        return progresoRutina;
    }
}
