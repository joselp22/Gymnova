/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.Rol;

import java.sql.Connection;
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
public class RolDAO {

    public boolean guardar(
            Rol rol
    ) throws SQLException {

        String sql = """
                INSERT INTO rol (
                    nombre_rol,
                    descripcion,
                    estado_rol
                )
                VALUES (?, ?, ?)
                RETURNING id_rol
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    rol.getNombreRol()
            );

            colocarTextoOpcional(
                    sentencia,
                    2,
                    rol.getDescripcion()
            );

            sentencia.setBoolean(
                    3,
                    rol.isEstadoRol()
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    rol.setIdRol(
                            resultado.getInt(
                                    "id_rol"
                            )
                    );

                    return true;
                }
            }
        }

        return false;
    }

    public boolean modificar(
            Rol rol
    ) throws SQLException {

        String sql = """
                UPDATE rol
                SET nombre_rol = ?,
                    descripcion = ?,
                    estado_rol = ?
                WHERE id_rol = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    rol.getNombreRol()
            );

            colocarTextoOpcional(
                    sentencia,
                    2,
                    rol.getDescripcion()
            );

            sentencia.setBoolean(
                    3,
                    rol.isEstadoRol()
            );

            sentencia.setInt(
                    4,
                    rol.getIdRol()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean desactivar(
            Integer idRol
    ) throws SQLException {

        String sql = """
                UPDATE rol
                SET estado_rol = FALSE
                WHERE id_rol = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    idRol
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Integer idRol
    ) throws SQLException {

        String sql = """
                DELETE FROM rol
                WHERE id_rol = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    idRol
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Rol buscar(
            Integer idRol
    ) throws SQLException {

        String sql = """
                SELECT
                    id_rol,
                    nombre_rol,
                    descripcion,
                    estado_rol
                FROM rol
                WHERE id_rol = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    idRol
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return convertirRol(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Rol> listar(
            String criterio
    ) throws SQLException {

        List<Rol> roles
                = new ArrayList<>();

        String sql = """
                SELECT
                    id_rol,
                    nombre_rol,
                    descripcion,
                    estado_rol
                FROM rol
                WHERE nombre_rol ILIKE ?
                   OR descripcion ILIKE ?
                ORDER BY estado_rol DESC,
                         nombre_rol
                """;

        if (criterio == null) {
            criterio = "";
        }

        String filtro
                = "%"
                + criterio.trim()
                + "%";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    filtro
            );

            sentencia.setString(
                    2,
                    filtro
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    roles.add(
                            convertirRol(
                                    resultado
                            )
                    );
                }
            }
        }

        return roles;
    }

    public List<Rol> listarActivos()
            throws SQLException {

        List<Rol> roles
                = new ArrayList<>();

        String sql = """
                SELECT
                    id_rol,
                    nombre_rol,
                    descripcion,
                    estado_rol
                FROM rol
                WHERE estado_rol = TRUE
                ORDER BY nombre_rol
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql);

            ResultSet resultado
                    = sentencia.executeQuery()
        ) {

            while (resultado.next()) {

                roles.add(
                        convertirRol(
                                resultado
                        )
                );
            }
        }

        return roles;
    }

    public boolean existeNombre(
            String nombreRol
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM rol
                WHERE LOWER(nombre_rol) = LOWER(?)
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    nombreRol
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
            Integer idRol,
            String nombreRol
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM rol
                WHERE LOWER(nombre_rol) = LOWER(?)
                  AND id_rol <> ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    nombreRol
            );

            sentencia.setInt(
                    2,
                    idRol
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    private Rol convertirRol(
            ResultSet resultado
    ) throws SQLException {

        Rol rol
                = new Rol();

        rol.setIdRol(
                resultado.getInt(
                        "id_rol"
                )
        );

        rol.setNombreRol(
                resultado.getString(
                        "nombre_rol"
                )
        );

        rol.setDescripcion(
                resultado.getString(
                        "descripcion"
                )
        );

        rol.setEstadoRol(
                resultado.getBoolean(
                        "estado_rol"
                )
        );

        return rol;
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
                    valor.trim()
            );
        }
    }
}
