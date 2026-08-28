/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.Empleado;
import utilidades.GeneradorCodigos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Usuario
 */
public class EmpleadoDAO {

    private static final String PREFIJO_CODIGO_EMPLEADO
            = GeneradorCodigos.PREFIJO_EMPLEADO;
    private static final long CLAVE_BLOQUEO_CODIGO_EMPLEADO
            = GeneradorCodigos.crearClaveBloqueoAdvisory(
                    "empleado.codigo_empleado"
            );

    public boolean guardar(
            Empleado empleado
    ) throws SQLException {

        Connection conexion
                = null;

        boolean autoCommitOriginal
                = true;

        try {

            conexion
                    = ConexionPostgreSQL.getConexion();

            autoCommitOriginal
                    = conexion.getAutoCommit();

            conexion.setAutoCommit(false);

            bloquearGeneracionCodigo(
                    conexion
            );

            String codigoGenerado
                    = generarSiguienteCodigo(
                            conexion
                    );

            empleado.setCodigoEmpleado(
                    codigoGenerado
            );

            boolean guardado
                    = insertarEmpleado(
                            conexion,
                            empleado
                    );

            conexion.commit();

            return guardado;

        } catch (SQLException | RuntimeException e) {

            if (conexion != null) {

                try {

                    conexion.rollback();

                } catch (SQLException ex) {

                    e.addSuppressed(
                            ex
                    );
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

    public List<Empleado> listar(
            String criterio
    ) throws SQLException {

        List<Empleado> empleados
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "p.id_persona, "
                + "p.cedula, "
                + "p.nombres, "
                + "p.apellidos, "
                + "p.fecha_nacimiento, "
                + "p.sexo, "
                + "p.telefono, "
                + "p.correo, "
                + "p.foto_perfil, "
                + "p.estado, "
                + "e.codigo_empleado, "
                + "e.fecha_ingreso, "
                + "e.tipo_contrato, "
                + "e.salario, "
                + "e.hora_inicio, "
                + "e.hora_fin, "
                + "e.estado_empleado "
                + "FROM empleado e "
                + "INNER JOIN persona p "
                + "ON p.id_persona = e.id_persona "
                + "WHERE e.codigo_empleado ILIKE ? "
                + "OR p.cedula ILIKE ? "
                + "OR p.nombres ILIKE ? "
                + "OR p.apellidos ILIKE ? "
                + "OR e.tipo_contrato ILIKE ? "
                + "ORDER BY e.estado_empleado DESC, "
                + "p.apellidos, p.nombres";

        if (criterio == null) {
            criterio = "";
        }

        String filtro
                = "%" + criterio.trim() + "%";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, filtro);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);
            sentencia.setString(4, filtro);
            sentencia.setString(5, filtro);

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    empleados.add(
                            convertirEmpleado(resultado)
                    );
                }
            }
        }

        return empleados;
    }

    public Empleado buscar(
            Long idPersona
    ) throws SQLException {

        String sql
                = "SELECT "
                + "p.id_persona, "
                + "p.cedula, "
                + "p.nombres, "
                + "p.apellidos, "
                + "p.fecha_nacimiento, "
                + "p.sexo, "
                + "p.telefono, "
                + "p.correo, "
                + "p.foto_perfil, "
                + "p.estado, "
                + "e.codigo_empleado, "
                + "e.fecha_ingreso, "
                + "e.tipo_contrato, "
                + "e.salario, "
                + "e.hora_inicio, "
                + "e.hora_fin, "
                + "e.estado_empleado "
                + "FROM empleado e "
                + "INNER JOIN persona p "
                + "ON p.id_persona = e.id_persona "
                + "WHERE e.id_persona = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idPersona);

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return convertirEmpleado(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public boolean modificar(
        Empleado empleado
) throws SQLException {

    String sql
            = "UPDATE empleado SET "
            + "fecha_ingreso = ?, "
            + "tipo_contrato = ?, "
            + "salario = ?, "
            + "hora_inicio = ?, "
            + "hora_fin = ?, "
            + "estado_empleado = ? "
            + "WHERE id_persona = ?";

    try (
        Connection conexion
                = ConexionPostgreSQL.getConexion();

        PreparedStatement sentencia
                = conexion.prepareStatement(sql)
    ) {

        sentencia.setDate(
                1,
                Date.valueOf(
                        empleado.getFechaIngreso()
                )
        );

        sentencia.setString(
                2,
                empleado.getTipoContrato()
        );

        sentencia.setBigDecimal(
                3,
                empleado.getSalario()
        );

        sentencia.setTime(
                4,
                Time.valueOf(
                        empleado.getHoraInicio()
                )
        );

        sentencia.setTime(
                5,
                Time.valueOf(
                        empleado.getHoraFin()
                )
        );

        sentencia.setBoolean(
                6,
                empleado.isEstadoEmpleado()
        );

        sentencia.setLong(
                7,
                empleado.getIdPersona()
        );

        return sentencia.executeUpdate() > 0;
    }
}

    public boolean desactivar(
            Long idPersona
    ) throws SQLException {

        String sql
                = "UPDATE empleado "
                + "SET estado_empleado = FALSE "
                + "WHERE id_persona = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idPersona);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idPersona
    ) throws SQLException {

        String sql
                = "DELETE FROM empleado "
                + "WHERE id_persona = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idPersona);

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean existeCodigo(
        String codigoEmpleado,
        Long idExcluir
) throws SQLException {

    String sql;

    if (idExcluir == null) {

        sql = "SELECT COUNT(*) "
                + "FROM empleado "
                + "WHERE codigo_empleado = ?";

    } else {

        sql = "SELECT COUNT(*) "
                + "FROM empleado "
                + "WHERE codigo_empleado = ? "
                + "AND id_persona <> ?";
    }

    try (
        Connection conexion
                = ConexionPostgreSQL.getConexion();

        PreparedStatement sentencia
                = conexion.prepareStatement(sql)
    ) {

        sentencia.setString(
                1,
                codigoEmpleado.trim().toUpperCase()
        );

        if (idExcluir != null) {

            sentencia.setLong(
                    2,
                    idExcluir
            );
        }

        try (
            ResultSet resultado
                    = sentencia.executeQuery()
        ) {

            if (resultado.next()) {

                return resultado.getInt(1) > 0;
            }
        }
    }

    return false;
}

    private void bloquearGeneracionCodigo(
            Connection conexion
    ) throws SQLException {

        String sql
                = "SELECT pg_advisory_xact_lock(?)";

        try (
            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    CLAVE_BLOQUEO_CODIGO_EMPLEADO
            );

            sentencia.execute();
        }
    }

    private String generarSiguienteCodigo(
            Connection conexion
    ) throws SQLException {

        String sql
                = "SELECT codigo_empleado "
                + "FROM empleado "
                + "WHERE codigo_empleado ~ ? "
                + "ORDER BY CAST(SUBSTRING(codigo_empleado FROM 3) "
                + "AS INTEGER) DESC "
                + "LIMIT 1";

        String ultimoCodigo
                = null;

        try (
            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    GeneradorCodigos.crearPatronPostgreSQL(
                            PREFIJO_CODIGO_EMPLEADO
                    )
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    ultimoCodigo
                            = resultado.getString(
                                    "codigo_empleado"
                            );
                }
            }
        }

        return GeneradorCodigos.generarSiguienteCodigo(
                PREFIJO_CODIGO_EMPLEADO,
                ultimoCodigo
        );
    }

    private boolean insertarEmpleado(
            Connection conexion,
            Empleado empleado
    ) throws SQLException {

        String sql
                = "INSERT INTO empleado ("
                + "id_persona, "
                + "codigo_empleado, "
                + "fecha_ingreso, "
                + "tipo_contrato, "
                + "salario, "
                + "hora_inicio, "
                + "hora_fin, "
                + "estado_empleado"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    empleado.getIdPersona()
            );

            sentencia.setString(
                    2,
                    empleado.getCodigoEmpleado()
            );

            sentencia.setDate(
                    3,
                    Date.valueOf(
                            empleado.getFechaIngreso()
                    )
            );

            sentencia.setString(
                    4,
                    empleado.getTipoContrato()
            );

            sentencia.setBigDecimal(
                    5,
                    empleado.getSalario()
            );

            sentencia.setTime(
                    6,
                    Time.valueOf(
                            empleado.getHoraInicio()
                    )
            );

            sentencia.setTime(
                    7,
                    Time.valueOf(
                            empleado.getHoraFin()
                    )
            );

            sentencia.setBoolean(
                    8,
                    empleado.isEstadoEmpleado()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    private Empleado convertirEmpleado(
            ResultSet resultado
    ) throws SQLException {

        Empleado empleado = new Empleado();

        empleado.setIdPersona(
                resultado.getLong("id_persona")
        );

        empleado.setCedula(
                resultado.getString("cedula")
        );

        empleado.setNombres(
                resultado.getString("nombres")
        );

        empleado.setApellidos(
                resultado.getString("apellidos")
        );

        Date fechaNacimiento
                = resultado.getDate(
                        "fecha_nacimiento"
                );

        if (fechaNacimiento != null) {

            empleado.setFechaNacimiento(
                    fechaNacimiento.toLocalDate()
            );
        }

        empleado.setSexo(
                resultado.getString("sexo")
        );

        empleado.setTelefono(
                resultado.getString("telefono")
        );

        empleado.setCorreo(
                resultado.getString("correo")
        );

        empleado.setFotoPerfil(
                resultado.getBytes("foto_perfil")
        );

        empleado.setEstado(
                resultado.getBoolean("estado")
        );

        empleado.setCodigoEmpleado(
                resultado.getString(
                        "codigo_empleado"
                )
        );

        Date fechaIngreso
                = resultado.getDate(
                        "fecha_ingreso"
                );

        if (fechaIngreso != null) {

            empleado.setFechaIngreso(
                    fechaIngreso.toLocalDate()
            );
        }

        empleado.setTipoContrato(
                resultado.getString(
                        "tipo_contrato"
                )
        );

        empleado.setSalario(
                resultado.getBigDecimal("salario")
        );

        Time horaInicio
                = resultado.getTime("hora_inicio");

        if (horaInicio != null) {

            empleado.setHoraInicio(
                    horaInicio.toLocalTime()
            );
        }

        Time horaFin
                = resultado.getTime("hora_fin");

        if (horaFin != null) {

            empleado.setHoraFin(
                    horaFin.toLocalTime()
            );
        }

        empleado.setEstadoEmpleado(
                resultado.getBoolean(
                        "estado_empleado"
                )
        );

        return empleado;
    }
}
