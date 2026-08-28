/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.EntrenadorEspecialidad;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class EntrenadorEspecialidadDAO {

    public boolean guardar(
            EntrenadorEspecialidad entrenadorEspecialidad
    ) throws SQLException {

        String sql
                = "INSERT INTO entrenador_especialidad ("
                + "id_entrenador, "
                + "id_especialidad, "
                + "fecha_asignacion, "
                + "es_principal, "
                + "nivel_dominio, "
                + "estado_asignacion"
                + ") VALUES (?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    entrenadorEspecialidad.getIdEntrenador()
            );

            sentencia.setInt(
                    2,
                    entrenadorEspecialidad.getIdEspecialidad()
            );

            colocarFechaOpcional(
                    sentencia,
                    3,
                    entrenadorEspecialidad.getFechaAsignacion()
            );

            sentencia.setBoolean(
                    4,
                    entrenadorEspecialidad.isEsPrincipal()
            );

            colocarTextoOpcional(
                    sentencia,
                    5,
                    entrenadorEspecialidad.getNivelDominio()
            );

            sentencia.setString(
                    6,
                    entrenadorEspecialidad.getEstadoAsignacion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            EntrenadorEspecialidad entrenadorEspecialidad
    ) throws SQLException {

        String sql
                = "UPDATE entrenador_especialidad SET "
                + "fecha_asignacion = ?, "
                + "es_principal = ?, "
                + "nivel_dominio = ?, "
                + "estado_asignacion = ? "
                + "WHERE id_entrenador = ? "
                + "AND id_especialidad = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            colocarFechaOpcional(
                    sentencia,
                    1,
                    entrenadorEspecialidad.getFechaAsignacion()
            );

            sentencia.setBoolean(
                    2,
                    entrenadorEspecialidad.isEsPrincipal()
            );

            colocarTextoOpcional(
                    sentencia,
                    3,
                    entrenadorEspecialidad.getNivelDominio()
            );

            sentencia.setString(
                    4,
                    entrenadorEspecialidad.getEstadoAsignacion()
            );

            sentencia.setLong(
                    5,
                    entrenadorEspecialidad.getIdEntrenador()
            );

            sentencia.setInt(
                    6,
                    entrenadorEspecialidad.getIdEspecialidad()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean desactivar(
            Long idEntrenador,
            Integer idEspecialidad
    ) throws SQLException {

        String sql
                = "UPDATE entrenador_especialidad "
                + "SET estado_asignacion = 'INACTIVA' "
                + "WHERE id_entrenador = ? "
                + "AND id_especialidad = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEntrenador
            );

            sentencia.setInt(
                    2,
                    idEspecialidad
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idEntrenador,
            Integer idEspecialidad
    ) throws SQLException {

        String sql
                = "DELETE FROM entrenador_especialidad "
                + "WHERE id_entrenador = ? "
                + "AND id_especialidad = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEntrenador
            );

            sentencia.setInt(
                    2,
                    idEspecialidad
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public EntrenadorEspecialidad buscar(
            Long idEntrenador,
            Integer idEspecialidad
    ) throws SQLException {

        String sql
                = "SELECT "
                + "id_entrenador, "
                + "id_especialidad, "
                + "fecha_asignacion, "
                + "es_principal, "
                + "nivel_dominio, "
                + "estado_asignacion "
                + "FROM entrenador_especialidad "
                + "WHERE id_entrenador = ? "
                + "AND id_especialidad = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEntrenador
            );

            sentencia.setInt(
                    2,
                    idEspecialidad
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return mapearEntrenadorEspecialidad(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public boolean existe(
            Long idEntrenador,
            Integer idEspecialidad
    ) throws SQLException {

        String sql
                = "SELECT 1 "
                + "FROM entrenador_especialidad "
                + "WHERE id_entrenador = ? "
                + "AND id_especialidad = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEntrenador
            );

            sentencia.setInt(
                    2,
                    idEspecialidad
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    public List<EntrenadorEspecialidad> listarPorEntrenador(
            Long idEntrenador
    ) throws SQLException {

        List<EntrenadorEspecialidad> especialidades
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "id_entrenador, "
                + "id_especialidad, "
                + "fecha_asignacion, "
                + "es_principal, "
                + "nivel_dominio, "
                + "estado_asignacion "
                + "FROM entrenador_especialidad "
                + "WHERE id_entrenador = ? "
                + "ORDER BY estado_asignacion, "
                + "es_principal DESC, "
                + "id_especialidad";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEntrenador
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    especialidades.add(
                            mapearEntrenadorEspecialidad(
                                    resultado
                            )
                    );
                }
            }
        }

        return especialidades;
    }

    public List<EntrenadorEspecialidad> listarPorEspecialidad(
            Integer idEspecialidad
    ) throws SQLException {

        List<EntrenadorEspecialidad> entrenadores
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "id_entrenador, "
                + "id_especialidad, "
                + "fecha_asignacion, "
                + "es_principal, "
                + "nivel_dominio, "
                + "estado_asignacion "
                + "FROM entrenador_especialidad "
                + "WHERE id_especialidad = ? "
                + "ORDER BY estado_asignacion, "
                + "es_principal DESC, "
                + "id_entrenador";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    idEspecialidad
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    entrenadores.add(
                            mapearEntrenadorEspecialidad(
                                    resultado
                            )
                    );
                }
            }
        }

        return entrenadores;
    }

    public List<EntrenadorEspecialidad> listarActivasPorEntrenador(
            Long idEntrenador
    ) throws SQLException {

        List<EntrenadorEspecialidad> especialidades
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "id_entrenador, "
                + "id_especialidad, "
                + "fecha_asignacion, "
                + "es_principal, "
                + "nivel_dominio, "
                + "estado_asignacion "
                + "FROM entrenador_especialidad "
                + "WHERE id_entrenador = ? "
                + "AND estado_asignacion = 'ACTIVA' "
                + "ORDER BY es_principal DESC, "
                + "id_especialidad";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idEntrenador
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    especialidades.add(
                            mapearEntrenadorEspecialidad(
                                    resultado
                            )
                    );
                }
            }
        }

        return especialidades;
    }

    private EntrenadorEspecialidad mapearEntrenadorEspecialidad(
            ResultSet resultado
    ) throws SQLException {

        EntrenadorEspecialidad entrenadorEspecialidad
                = new EntrenadorEspecialidad();

        entrenadorEspecialidad.setIdEntrenador(
                resultado.getLong(
                        "id_entrenador"
                )
        );

        entrenadorEspecialidad.setIdEspecialidad(
                resultado.getInt(
                        "id_especialidad"
                )
        );

        Date fechaAsignacion
                = resultado.getDate(
                        "fecha_asignacion"
                );

        if (fechaAsignacion != null) {

            entrenadorEspecialidad.setFechaAsignacion(
                    fechaAsignacion.toLocalDate()
            );
        }

        entrenadorEspecialidad.setEsPrincipal(
                resultado.getBoolean(
                        "es_principal"
                )
        );

        entrenadorEspecialidad.setNivelDominio(
                resultado.getString(
                        "nivel_dominio"
                )
        );

        entrenadorEspecialidad.setEstadoAsignacion(
                resultado.getString(
                        "estado_asignacion"
                )
        );

        return entrenadorEspecialidad;
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

    private void colocarTextoOpcional(
            PreparedStatement sentencia,
            int posicion,
            String valor
    ) throws SQLException {

        if (valor == null
                || valor.isBlank()) {

            sentencia.setNull(
                    posicion,
                    Types.VARCHAR
            );

        } else {

            sentencia.setString(
                    posicion,
                    valor
            );
        }
    }
}
