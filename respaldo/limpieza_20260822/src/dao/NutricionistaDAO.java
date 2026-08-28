/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

import modelo.Nutricionista;
import modelo.Empleado;
/**
 *
 * @author Usuario
 */
public class NutricionistaDAO {

    public boolean guardar(
            Nutricionista nutricionista
    ) throws SQLException {

        String sql
                = "INSERT INTO nutricionista ("
                + "id_persona, "
                + "numero_licencia, "
                + "fecha_inicio_profesion, "
                + "estado_licencia"
                + ") VALUES (?, ?, ?, ?)";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    nutricionista.getIdPersona()
            );

            sentencia.setString(
                    2,
                    nutricionista.getNumeroLicencia()
            );

            colocarFechaOpcional(
                    sentencia,
                    3,
                    nutricionista.getFechaInicioProfesion()
            );

            sentencia.setString(
                    4,
                    nutricionista.getEstadoLicencia()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Nutricionista nutricionista
    ) throws SQLException {

        String sql
                = "UPDATE nutricionista SET "
                + "numero_licencia = ?, "
                + "fecha_inicio_profesion = ?, "
                + "estado_licencia = ? "
                + "WHERE id_persona = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    nutricionista.getNumeroLicencia()
            );

            colocarFechaOpcional(
                    sentencia,
                    2,
                    nutricionista.getFechaInicioProfesion()
            );

            sentencia.setString(
                    3,
                    nutricionista.getEstadoLicencia()
            );

            sentencia.setLong(
                    4,
                    nutricionista.getIdPersona()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean desactivar(
            Long idNutricionista
    ) throws SQLException {

        String sql
                = "UPDATE nutricionista "
                + "SET estado_licencia = 'INACTIVA' "
                + "WHERE id_persona = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idNutricionista
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idNutricionista
    ) throws SQLException {

        String sql
                = "DELETE FROM nutricionista "
                + "WHERE id_persona = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idNutricionista
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Nutricionista buscar(
            Long idNutricionista
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
                + "e.estado_empleado, "
                + "n.numero_licencia, "
                + "n.fecha_inicio_profesion, "
                + "n.estado_licencia "
                + "FROM nutricionista n "
                + "INNER JOIN empleado e "
                + "ON n.id_persona = e.id_persona "
                + "INNER JOIN persona p "
                + "ON e.id_persona = p.id_persona "
                + "WHERE n.id_persona = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idNutricionista
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return mapearNutricionista(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Nutricionista> listar(
            String criterio
    ) throws SQLException {

        List<Nutricionista> nutricionistas
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
                + "e.estado_empleado, "
                + "n.numero_licencia, "
                + "n.fecha_inicio_profesion, "
                + "n.estado_licencia "
                + "FROM nutricionista n "
                + "INNER JOIN empleado e "
                + "ON n.id_persona = e.id_persona "
                + "INNER JOIN persona p "
                + "ON e.id_persona = p.id_persona "
                + "WHERE p.cedula ILIKE ? "
                + "OR p.nombres ILIKE ? "
                + "OR p.apellidos ILIKE ? "
                + "OR e.codigo_empleado ILIKE ? "
                + "OR n.numero_licencia ILIKE ? "
                + "ORDER BY n.estado_licencia, "
                + "p.apellidos, "
                + "p.nombres";

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

                    nutricionistas.add(
                            mapearNutricionista(
                                    resultado
                            )
                    );
                }
            }
        }

        return nutricionistas;
    }

    public List<Empleado> listarEmpleadosDisponibles()
            throws SQLException {

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
                + "ON e.id_persona = p.id_persona "
                + "WHERE p.estado = TRUE "
                + "AND e.estado_empleado = TRUE "
                + "AND NOT EXISTS ("
                + "    SELECT 1 "
                + "    FROM nutricionista n "
                + "    WHERE n.id_persona = e.id_persona"
                + ") "
                + "ORDER BY p.apellidos, "
                + "p.nombres";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql);

            ResultSet resultado
                    = sentencia.executeQuery()
        ) {

            while (resultado.next()) {

                empleados.add(
                        mapearEmpleado(
                                resultado
                        )
                );
            }
        }

        return empleados;
    }

    private Nutricionista mapearNutricionista(
            ResultSet resultado
    ) throws SQLException {

        Nutricionista nutricionista
                = new Nutricionista();

        nutricionista.setIdPersona(
                resultado.getLong(
                        "id_persona"
                )
        );

        nutricionista.setCedula(
                resultado.getString(
                        "cedula"
                )
        );

        nutricionista.setNombres(
                resultado.getString(
                        "nombres"
                )
        );

        nutricionista.setApellidos(
                resultado.getString(
                        "apellidos"
                )
        );

        Date fechaNacimiento
                = resultado.getDate(
                        "fecha_nacimiento"
                );

        if (fechaNacimiento != null) {

            nutricionista.setFechaNacimiento(
                    fechaNacimiento.toLocalDate()
            );
        }

        nutricionista.setSexo(
                resultado.getString(
                        "sexo"
                )
        );

        nutricionista.setTelefono(
                resultado.getString(
                        "telefono"
                )
        );

        nutricionista.setCorreo(
                resultado.getString(
                        "correo"
                )
        );

        nutricionista.setFotoPerfil(
                resultado.getBytes(
                        "foto_perfil"
                )
        );

        nutricionista.setEstado(
                resultado.getBoolean(
                        "estado"
                )
        );

        nutricionista.setCodigoEmpleado(
                resultado.getString(
                        "codigo_empleado"
                )
        );

        Date fechaIngreso
                = resultado.getDate(
                        "fecha_ingreso"
                );

        if (fechaIngreso != null) {

            nutricionista.setFechaIngreso(
                    fechaIngreso.toLocalDate()
            );
        }

        nutricionista.setTipoContrato(
                resultado.getString(
                        "tipo_contrato"
                )
        );

        nutricionista.setSalario(
                resultado.getBigDecimal(
                        "salario"
                )
        );

        if (resultado.getTime(
                "hora_inicio"
        ) != null) {

            nutricionista.setHoraInicio(
                    resultado.getTime(
                            "hora_inicio"
                    ).toLocalTime()
            );
        }

        if (resultado.getTime(
                "hora_fin"
        ) != null) {

            nutricionista.setHoraFin(
                    resultado.getTime(
                            "hora_fin"
                    ).toLocalTime()
            );
        }

        nutricionista.setEstadoEmpleado(
                resultado.getBoolean(
                        "estado_empleado"
                )
        );

        nutricionista.setNumeroLicencia(
                resultado.getString(
                        "numero_licencia"
                )
        );

        Date fechaInicioProfesion
                = resultado.getDate(
                        "fecha_inicio_profesion"
                );

        if (fechaInicioProfesion != null) {

            nutricionista.setFechaInicioProfesion(
                    fechaInicioProfesion.toLocalDate()
            );
        }

        nutricionista.setEstadoLicencia(
                resultado.getString(
                        "estado_licencia"
                )
        );

        return nutricionista;
    }

    private Empleado mapearEmpleado(
            ResultSet resultado
    ) throws SQLException {

        Empleado empleado
                = new Empleado();

        empleado.setIdPersona(
                resultado.getLong(
                        "id_persona"
                )
        );

        empleado.setCedula(
                resultado.getString(
                        "cedula"
                )
        );

        empleado.setNombres(
                resultado.getString(
                        "nombres"
                )
        );

        empleado.setApellidos(
                resultado.getString(
                        "apellidos"
                )
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
                resultado.getString(
                        "sexo"
                )
        );

        empleado.setTelefono(
                resultado.getString(
                        "telefono"
                )
        );

        empleado.setCorreo(
                resultado.getString(
                        "correo"
                )
        );

        empleado.setFotoPerfil(
                resultado.getBytes(
                        "foto_perfil"
                )
        );

        empleado.setEstado(
                resultado.getBoolean(
                        "estado"
                )
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
                resultado.getBigDecimal(
                        "salario"
                )
        );

        if (resultado.getTime(
                "hora_inicio"
        ) != null) {

            empleado.setHoraInicio(
                    resultado.getTime(
                            "hora_inicio"
                    ).toLocalTime()
            );
        }

        if (resultado.getTime(
                "hora_fin"
        ) != null) {

            empleado.setHoraFin(
                    resultado.getTime(
                            "hora_fin"
                    ).toLocalTime()
            );
        }

        empleado.setEstadoEmpleado(
                resultado.getBoolean(
                        "estado_empleado"
                )
        );

        return empleado;
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
                    Date.valueOf(fecha)
            );
        }
    }
}