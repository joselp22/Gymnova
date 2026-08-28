/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.Especialidad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Usuario
 */

public class EspecialidadDAO {

    public boolean guardar(
            Especialidad especialidad
    ) throws SQLException {

        String sql
                = "INSERT INTO especialidad ("
                + "nombre_especialidad, "
                + "descripcion, "
                + "categoria, "
                + "estado_especialidad"
                + ") VALUES (?, ?, ?, ?)";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(
                            sql,
                            Statement.RETURN_GENERATED_KEYS
                    )
        ) {

            sentencia.setString(
                    1,
                    especialidad.getNombreEspecialidad()
            );

            colocarTextoOpcional(
                    sentencia,
                    2,
                    especialidad.getDescripcion()
            );

            colocarTextoOpcional(
                    sentencia,
                    3,
                    especialidad.getCategoria()
            );

            sentencia.setBoolean(
                    4,
                    especialidad.isEstadoEspecialidad()
            );

            int filasAfectadas
                    = sentencia.executeUpdate();

            if (filasAfectadas > 0) {

                try (
                    ResultSet clavesGeneradas
                            = sentencia.getGeneratedKeys()
                ) {

                    if (clavesGeneradas.next()) {

                        especialidad.setIdEspecialidad(
                                clavesGeneradas.getInt(1)
                        );
                    }
                }

                return true;
            }

            return false;
        }
    }

    public boolean modificar(
            Especialidad especialidad
    ) throws SQLException {

        String sql
                = "UPDATE especialidad SET "
                + "nombre_especialidad = ?, "
                + "descripcion = ?, "
                + "categoria = ?, "
                + "estado_especialidad = ? "
                + "WHERE id_especialidad = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    especialidad.getNombreEspecialidad()
            );

            colocarTextoOpcional(
                    sentencia,
                    2,
                    especialidad.getDescripcion()
            );

            colocarTextoOpcional(
                    sentencia,
                    3,
                    especialidad.getCategoria()
            );

            sentencia.setBoolean(
                    4,
                    especialidad.isEstadoEspecialidad()
            );

            sentencia.setInt(
                    5,
                    especialidad.getIdEspecialidad()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean desactivar(
            Integer idEspecialidad
    ) throws SQLException {

        String sql
                = "UPDATE especialidad "
                + "SET estado_especialidad = FALSE "
                + "WHERE id_especialidad = ?";

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

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Integer idEspecialidad
    ) throws SQLException {

        String sql
                = "DELETE FROM especialidad "
                + "WHERE id_especialidad = ?";

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

            return sentencia.executeUpdate() > 0;
        }
    }

    public Especialidad buscar(
            Integer idEspecialidad
    ) throws SQLException {

        String sql
                = "SELECT "
                + "id_especialidad, "
                + "nombre_especialidad, "
                + "descripcion, "
                + "categoria, "
                + "estado_especialidad "
                + "FROM especialidad "
                + "WHERE id_especialidad = ?";

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

                if (resultado.next()) {

                    return mapearEspecialidad(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Especialidad> listar(
            String criterio
    ) throws SQLException {

        List<Especialidad> especialidades
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "id_especialidad, "
                + "nombre_especialidad, "
                + "descripcion, "
                + "categoria, "
                + "estado_especialidad "
                + "FROM especialidad "
                + "WHERE nombre_especialidad ILIKE ? "
                + "OR descripcion ILIKE ? "
                + "OR categoria ILIKE ? "
                + "ORDER BY estado_especialidad DESC, "
                + "nombre_especialidad";

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

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    especialidades.add(
                            mapearEspecialidad(
                                    resultado
                            )
                    );
                }
            }
        }

        return especialidades;
    }

    public List<Especialidad> listarActivas()
            throws SQLException {

        List<Especialidad> especialidades
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "id_especialidad, "
                + "nombre_especialidad, "
                + "descripcion, "
                + "categoria, "
                + "estado_especialidad "
                + "FROM especialidad "
                + "WHERE estado_especialidad = TRUE "
                + "ORDER BY nombre_especialidad";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql);

            ResultSet resultado
                    = sentencia.executeQuery()
        ) {

            while (resultado.next()) {

                especialidades.add(
                        mapearEspecialidad(
                                resultado
                        )
                );
            }
        }

        return especialidades;
    }

    public boolean existeNombre(
            String nombreEspecialidad
    ) throws SQLException {

        String sql
                = "SELECT 1 "
                + "FROM especialidad "
                + "WHERE LOWER(nombre_especialidad) = LOWER(?)";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    nombreEspecialidad
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    public boolean existeNombreEnOtroRegistro(
            Integer idEspecialidad,
            String nombreEspecialidad
    ) throws SQLException {

        String sql
                = "SELECT 1 "
                + "FROM especialidad "
                + "WHERE LOWER(nombre_especialidad) = LOWER(?) "
                + "AND id_especialidad <> ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    nombreEspecialidad
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

    private Especialidad mapearEspecialidad(
            ResultSet resultado
    ) throws SQLException {

        Especialidad especialidad
                = new Especialidad();

        especialidad.setIdEspecialidad(
                resultado.getInt(
                        "id_especialidad"
                )
        );

        especialidad.setNombreEspecialidad(
                resultado.getString(
                        "nombre_especialidad"
                )
        );

        especialidad.setDescripcion(
                resultado.getString(
                        "descripcion"
                )
        );

        especialidad.setCategoria(
                resultado.getString(
                        "categoria"
                )
        );

        especialidad.setEstadoEspecialidad(
                resultado.getBoolean(
                        "estado_especialidad"
                )
        );

        return especialidad;
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
