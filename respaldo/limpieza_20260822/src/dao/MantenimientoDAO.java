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
import modelo.Mantenimiento;

/**
 * DAO para la tabla mantenimiento.
 *
 * @author Usuario
 */
public class MantenimientoDAO {

    public boolean guardar(
            Mantenimiento mantenimiento
    ) throws SQLException {

        String sql = "INSERT INTO mantenimiento (numero_mantenimiento, fecha_solicitud, fecha_programada, fecha_realizacion, costo_repuestos, costo_mano_obra, resultado_mantenimiento, descripcion_falla, trabajo_realizado, estado_mantenimiento, observaciones, id_equipo, id_tipo_mantenimiento, id_empleado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    mantenimiento.getNumeroMantenimiento()
            );

            if (mantenimiento.getFechaSolicitud() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            mantenimiento.getFechaSolicitud()
                    )
            );
        }

            if (mantenimiento.getFechaProgramada() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            mantenimiento.getFechaProgramada()
                    )
            );
        }

            if (mantenimiento.getFechaRealizacion() == null) {
            sentencia.setDate(
                    4,
                    null
            );
        } else {
            sentencia.setDate(
                    4,
                    Date.valueOf(
                            mantenimiento.getFechaRealizacion()
                    )
            );
        }

            sentencia.setBigDecimal(
                    5,
                    mantenimiento.getCostoRepuestos()
            );

            sentencia.setBigDecimal(
                    6,
                    mantenimiento.getCostoManoObra()
            );

            sentencia.setString(
                    7,
                    mantenimiento.getResultadoMantenimiento()
            );

            sentencia.setString(
                    8,
                    mantenimiento.getDescripcionFalla()
            );

            sentencia.setString(
                    9,
                    mantenimiento.getTrabajoRealizado()
            );

            sentencia.setString(
                    10,
                    mantenimiento.getEstadoMantenimiento()
            );

            sentencia.setString(
                    11,
                    mantenimiento.getObservaciones()
            );

            sentencia.setLong(
                    12,
                    mantenimiento.getIdEquipo()
            );

            sentencia.setLong(
                    13,
                    mantenimiento.getIdTipoMantenimiento()
            );

            sentencia.setLong(
                    14,
                    mantenimiento.getIdEmpleado()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Mantenimiento mantenimiento
    ) throws SQLException {

        String sql = "UPDATE mantenimiento SET numero_mantenimiento = ?, fecha_solicitud = ?, fecha_programada = ?, fecha_realizacion = ?, costo_repuestos = ?, costo_mano_obra = ?, resultado_mantenimiento = ?, descripcion_falla = ?, trabajo_realizado = ?, estado_mantenimiento = ?, observaciones = ?, id_equipo = ?, id_tipo_mantenimiento = ?, id_empleado = ? WHERE id_mantenimiento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    mantenimiento.getNumeroMantenimiento()
            );

            if (mantenimiento.getFechaSolicitud() == null) {
            sentencia.setDate(
                    2,
                    null
            );
        } else {
            sentencia.setDate(
                    2,
                    Date.valueOf(
                            mantenimiento.getFechaSolicitud()
                    )
            );
        }

            if (mantenimiento.getFechaProgramada() == null) {
            sentencia.setDate(
                    3,
                    null
            );
        } else {
            sentencia.setDate(
                    3,
                    Date.valueOf(
                            mantenimiento.getFechaProgramada()
                    )
            );
        }

            if (mantenimiento.getFechaRealizacion() == null) {
            sentencia.setDate(
                    4,
                    null
            );
        } else {
            sentencia.setDate(
                    4,
                    Date.valueOf(
                            mantenimiento.getFechaRealizacion()
                    )
            );
        }

            sentencia.setBigDecimal(
                    5,
                    mantenimiento.getCostoRepuestos()
            );

            sentencia.setBigDecimal(
                    6,
                    mantenimiento.getCostoManoObra()
            );

            sentencia.setString(
                    7,
                    mantenimiento.getResultadoMantenimiento()
            );

            sentencia.setString(
                    8,
                    mantenimiento.getDescripcionFalla()
            );

            sentencia.setString(
                    9,
                    mantenimiento.getTrabajoRealizado()
            );

            sentencia.setString(
                    10,
                    mantenimiento.getEstadoMantenimiento()
            );

            sentencia.setString(
                    11,
                    mantenimiento.getObservaciones()
            );

            sentencia.setLong(
                    12,
                    mantenimiento.getIdEquipo()
            );

            sentencia.setLong(
                    13,
                    mantenimiento.getIdTipoMantenimiento()
            );

            sentencia.setLong(
                    14,
                    mantenimiento.getIdEmpleado()
            );

            sentencia.setLong(
                    15,
                    mantenimiento.getIdMantenimiento()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Mantenimiento buscar(
            Long idMantenimiento
    ) throws SQLException {

        String sql = "SELECT id_mantenimiento, numero_mantenimiento, fecha_solicitud, fecha_programada, fecha_realizacion, costo_repuestos, costo_mano_obra, resultado_mantenimiento, descripcion_falla, trabajo_realizado, estado_mantenimiento, observaciones, id_equipo, id_tipo_mantenimiento, id_empleado FROM mantenimiento WHERE id_mantenimiento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMantenimiento);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirMantenimiento(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Mantenimiento> listar(
            String criterio
    ) throws SQLException {

        List<Mantenimiento> lista = new ArrayList<>();
        String sql = "SELECT id_mantenimiento, numero_mantenimiento, fecha_solicitud, fecha_programada, fecha_realizacion, costo_repuestos, costo_mano_obra, resultado_mantenimiento, descripcion_falla, trabajo_realizado, estado_mantenimiento, observaciones, id_equipo, id_tipo_mantenimiento, id_empleado FROM mantenimiento WHERE numero_mantenimiento ILIKE ? OR resultado_mantenimiento ILIKE ? OR descripcion_falla ILIKE ? OR trabajo_realizado ILIKE ? OR estado_mantenimiento ILIKE ? OR observaciones ILIKE ? ORDER BY id_mantenimiento";

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
                            convertirMantenimiento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean desactivar(
            Long idMantenimiento
    ) throws SQLException {

        String sql = "UPDATE mantenimiento SET estado_mantenimiento = 'CANCELADO' WHERE id_mantenimiento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMantenimiento);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idMantenimiento
    ) throws SQLException {

        String sql = "DELETE FROM mantenimiento WHERE id_mantenimiento = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idMantenimiento);

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Mantenimiento> listarPorIdEquipo(
            Long idEquipo
    ) throws SQLException {

        List<Mantenimiento> lista = new ArrayList<>();
        String sql = "SELECT id_mantenimiento, numero_mantenimiento, fecha_solicitud, fecha_programada, fecha_realizacion, costo_repuestos, costo_mano_obra, resultado_mantenimiento, descripcion_falla, trabajo_realizado, estado_mantenimiento, observaciones, id_equipo, id_tipo_mantenimiento, id_empleado FROM mantenimiento WHERE id_equipo = ? ORDER BY id_mantenimiento";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEquipo
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirMantenimiento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<Mantenimiento> listarPorIdTipoMantenimiento(
            Long idTipoMantenimiento
    ) throws SQLException {

        List<Mantenimiento> lista = new ArrayList<>();
        String sql = "SELECT id_mantenimiento, numero_mantenimiento, fecha_solicitud, fecha_programada, fecha_realizacion, costo_repuestos, costo_mano_obra, resultado_mantenimiento, descripcion_falla, trabajo_realizado, estado_mantenimiento, observaciones, id_equipo, id_tipo_mantenimiento, id_empleado FROM mantenimiento WHERE id_tipo_mantenimiento = ? ORDER BY id_mantenimiento";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idTipoMantenimiento
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirMantenimiento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }
    public List<Mantenimiento> listarPorIdEmpleado(
            Long idEmpleado
    ) throws SQLException {

        List<Mantenimiento> lista = new ArrayList<>();
        String sql = "SELECT id_mantenimiento, numero_mantenimiento, fecha_solicitud, fecha_programada, fecha_realizacion, costo_repuestos, costo_mano_obra, resultado_mantenimiento, descripcion_falla, trabajo_realizado, estado_mantenimiento, observaciones, id_equipo, id_tipo_mantenimiento, id_empleado FROM mantenimiento WHERE id_empleado = ? ORDER BY id_mantenimiento";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEmpleado
            );

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirMantenimiento(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    private Mantenimiento convertirMantenimiento(
            ResultSet resultado
    ) throws SQLException {

        Mantenimiento mantenimiento = new Mantenimiento();

        mantenimiento.setIdMantenimiento(
                resultado.getLong("id_mantenimiento")
        );

        mantenimiento.setNumeroMantenimiento(
                resultado.getString("numero_mantenimiento")
        );

        mantenimiento.setFechaSolicitud(
                resultado.getDate("fecha_solicitud") == null
                ? null
                : resultado.getDate("fecha_solicitud").toLocalDate()
        );

        mantenimiento.setFechaProgramada(
                resultado.getDate("fecha_programada") == null
                ? null
                : resultado.getDate("fecha_programada").toLocalDate()
        );

        mantenimiento.setFechaRealizacion(
                resultado.getDate("fecha_realizacion") == null
                ? null
                : resultado.getDate("fecha_realizacion").toLocalDate()
        );

        mantenimiento.setCostoRepuestos(
                resultado.getBigDecimal("costo_repuestos")
        );

        mantenimiento.setCostoManoObra(
                resultado.getBigDecimal("costo_mano_obra")
        );

        mantenimiento.setResultadoMantenimiento(
                resultado.getString("resultado_mantenimiento")
        );

        mantenimiento.setDescripcionFalla(
                resultado.getString("descripcion_falla")
        );

        mantenimiento.setTrabajoRealizado(
                resultado.getString("trabajo_realizado")
        );

        mantenimiento.setEstadoMantenimiento(
                resultado.getString("estado_mantenimiento")
        );

        mantenimiento.setObservaciones(
                resultado.getString("observaciones")
        );

        mantenimiento.setIdEquipo(
                resultado.getLong("id_equipo")
        );

        mantenimiento.setIdTipoMantenimiento(
                resultado.getLong("id_tipo_mantenimiento")
        );

        mantenimiento.setIdEmpleado(
                resultado.getLong("id_empleado")
        );

        return mantenimiento;
    }
}
