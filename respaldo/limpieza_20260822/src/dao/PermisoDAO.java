/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.Permiso;

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
public class PermisoDAO {

    public boolean guardar(
            Permiso permiso
    ) throws SQLException {

        String sql = """
                INSERT INTO permiso (
                    nombre_permiso,
                    descripcion,
                    modulo,
                    accion,
                    estado_permiso
                )
                VALUES (?, ?, ?, ?, ?)
                RETURNING id_permiso
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    permiso.getNombrePermiso()
            );

            colocarTextoOpcional(
                    sentencia,
                    2,
                    permiso.getDescripcion()
            );

            sentencia.setString(
                    3,
                    permiso.getModulo()
            );

            sentencia.setString(
                    4,
                    permiso.getAccion()
            );

            sentencia.setBoolean(
                    5,
                    permiso.isEstadoPermiso()
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    permiso.setIdPermiso(
                            resultado.getInt(
                                    "id_permiso"
                            )
                    );

                    return true;
                }
            }
        }

        return false;
    }

    public boolean modificar(
            Permiso permiso
    ) throws SQLException {

        String sql = """
                UPDATE permiso
                SET nombre_permiso = ?,
                    descripcion = ?,
                    modulo = ?,
                    accion = ?,
                    estado_permiso = ?
                WHERE id_permiso = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    permiso.getNombrePermiso()
            );

            colocarTextoOpcional(
                    sentencia,
                    2,
                    permiso.getDescripcion()
            );

            sentencia.setString(
                    3,
                    permiso.getModulo()
            );

            sentencia.setString(
                    4,
                    permiso.getAccion()
            );

            sentencia.setBoolean(
                    5,
                    permiso.isEstadoPermiso()
            );

            sentencia.setInt(
                    6,
                    permiso.getIdPermiso()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean desactivar(
            Integer idPermiso
    ) throws SQLException {

        String sql = """
                UPDATE permiso
                SET estado_permiso = FALSE
                WHERE id_permiso = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    idPermiso
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Integer idPermiso
    ) throws SQLException {

        String sql = """
                DELETE FROM permiso
                WHERE id_permiso = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    idPermiso
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Permiso buscar(
            Integer idPermiso
    ) throws SQLException {

        String sql = """
                SELECT
                    id_permiso,
                    nombre_permiso,
                    descripcion,
                    modulo,
                    accion,
                    estado_permiso
                FROM permiso
                WHERE id_permiso = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    idPermiso
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return convertirPermiso(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Permiso> listar(
            String criterio
    ) throws SQLException {

        List<Permiso> permisos
                = new ArrayList<>();

        String sql = """
                SELECT
                    id_permiso,
                    nombre_permiso,
                    descripcion,
                    modulo,
                    accion,
                    estado_permiso
                FROM permiso
                WHERE nombre_permiso ILIKE ?
                   OR descripcion ILIKE ?
                   OR modulo ILIKE ?
                   OR accion ILIKE ?
                ORDER BY estado_permiso DESC,
                         modulo,
                         accion,
                         nombre_permiso
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

            sentencia.setString(
                    3,
                    filtro
            );

            sentencia.setString(
                    4,
                    filtro
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    permisos.add(
                            convertirPermiso(
                                    resultado
                            )
                    );
                }
            }
        }

        return permisos;
    }

    public List<Permiso> listarActivos()
            throws SQLException {

        List<Permiso> permisos
                = new ArrayList<>();

        String sql = """
                SELECT
                    id_permiso,
                    nombre_permiso,
                    descripcion,
                    modulo,
                    accion,
                    estado_permiso
                FROM permiso
                WHERE estado_permiso = TRUE
                ORDER BY modulo,
                         accion,
                         nombre_permiso
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

                permisos.add(
                        convertirPermiso(
                                resultado
                        )
                );
            }
        }

        return permisos;
    }

    public boolean existeNombre(
            String nombrePermiso
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM permiso
                WHERE LOWER(nombre_permiso) = LOWER(?)
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    nombrePermiso
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
            Integer idPermiso,
            String nombrePermiso
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM permiso
                WHERE LOWER(nombre_permiso) = LOWER(?)
                  AND id_permiso <> ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    nombrePermiso
            );

            sentencia.setInt(
                    2,
                    idPermiso
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    public boolean existeModuloAccion(
            String modulo,
            String accion
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM permiso
                WHERE LOWER(modulo) = LOWER(?)
                  AND LOWER(accion) = LOWER(?)
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    modulo
            );

            sentencia.setString(
                    2,
                    accion
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    public boolean existeModuloAccionEnOtroRegistro(
            Integer idPermiso,
            String modulo,
            String accion
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM permiso
                WHERE LOWER(modulo) = LOWER(?)
                  AND LOWER(accion) = LOWER(?)
                  AND id_permiso <> ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    modulo
            );

            sentencia.setString(
                    2,
                    accion
            );

            sentencia.setInt(
                    3,
                    idPermiso
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    private Permiso convertirPermiso(
            ResultSet resultado
    ) throws SQLException {

        Permiso permiso
                = new Permiso();

        permiso.setIdPermiso(
                resultado.getInt(
                        "id_permiso"
                )
        );

        permiso.setNombrePermiso(
                resultado.getString(
                        "nombre_permiso"
                )
        );

        permiso.setDescripcion(
                resultado.getString(
                        "descripcion"
                )
        );

        permiso.setModulo(
                resultado.getString(
                        "modulo"
                )
        );

        permiso.setAccion(
                resultado.getString(
                        "accion"
                )
        );

        permiso.setEstadoPermiso(
                resultado.getBoolean(
                        "estado_permiso"
                )
        );

        return permiso;
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
