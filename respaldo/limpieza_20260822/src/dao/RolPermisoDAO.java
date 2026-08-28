/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.RolPermiso;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class RolPermisoDAO {

    public boolean guardar(
            RolPermiso rolPermiso
    ) throws SQLException {

        String sql = """
                INSERT INTO rol_permiso (
                    id_rol,
                    id_permiso,
                    fecha_asignacion,
                    estado_asignacion
                )
                VALUES (?, ?, ?, ?)
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setInt(
                    1,
                    rolPermiso.getIdRol()
            );

            sentencia.setInt(
                    2,
                    rolPermiso.getIdPermiso()
            );

            sentencia.setDate(
                    3,
                    Date.valueOf(
                            rolPermiso.getFechaAsignacion()
                    )
            );

            sentencia.setString(
                    4,
                    rolPermiso.getEstadoAsignacion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            RolPermiso rolPermiso
    ) throws SQLException {

        String sql = """
                UPDATE rol_permiso
                SET fecha_asignacion = ?,
                    estado_asignacion = ?
                WHERE id_rol = ?
                  AND id_permiso = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setDate(
                    1,
                    Date.valueOf(
                            rolPermiso.getFechaAsignacion()
                    )
            );

            sentencia.setString(
                    2,
                    rolPermiso.getEstadoAsignacion()
            );

            sentencia.setInt(
                    3,
                    rolPermiso.getIdRol()
            );

            sentencia.setInt(
                    4,
                    rolPermiso.getIdPermiso()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean desactivar(
            Integer idRol,
            Integer idPermiso
    ) throws SQLException {

        String sql = """
                UPDATE rol_permiso
                SET estado_asignacion = 'INACTIVA'
                WHERE id_rol = ?
                  AND id_permiso = ?
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

            sentencia.setInt(
                    2,
                    idPermiso
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Integer idRol,
            Integer idPermiso
    ) throws SQLException {

        String sql = """
                DELETE FROM rol_permiso
                WHERE id_rol = ?
                  AND id_permiso = ?
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

            sentencia.setInt(
                    2,
                    idPermiso
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public RolPermiso buscar(
            Integer idRol,
            Integer idPermiso
    ) throws SQLException {

        String sql = """
                SELECT
                    id_rol,
                    id_permiso,
                    fecha_asignacion,
                    estado_asignacion
                FROM rol_permiso
                WHERE id_rol = ?
                  AND id_permiso = ?
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

            sentencia.setInt(
                    2,
                    idPermiso
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return convertirRolPermiso(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public boolean existe(
            Integer idRol,
            Integer idPermiso
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM rol_permiso
                WHERE id_rol = ?
                  AND id_permiso = ?
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

    public List<RolPermiso> listarPorRol(
            Integer idRol
    ) throws SQLException {

        List<RolPermiso> permisos
                = new ArrayList<>();

        String sql = """
                SELECT
                    id_rol,
                    id_permiso,
                    fecha_asignacion,
                    estado_asignacion
                FROM rol_permiso
                WHERE id_rol = ?
                ORDER BY estado_asignacion,
                         id_permiso
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

                while (resultado.next()) {

                    permisos.add(
                            convertirRolPermiso(
                                    resultado
                            )
                    );
                }
            }
        }

        return permisos;
    }

    public List<RolPermiso> listarPorPermiso(
            Integer idPermiso
    ) throws SQLException {

        List<RolPermiso> roles
                = new ArrayList<>();

        String sql = """
                SELECT
                    id_rol,
                    id_permiso,
                    fecha_asignacion,
                    estado_asignacion
                FROM rol_permiso
                WHERE id_permiso = ?
                ORDER BY estado_asignacion,
                         id_rol
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

                while (resultado.next()) {

                    roles.add(
                            convertirRolPermiso(
                                    resultado
                            )
                    );
                }
            }
        }

        return roles;
    }

    public List<RolPermiso> listarActivosPorRol(
            Integer idRol
    ) throws SQLException {

        List<RolPermiso> permisos
                = new ArrayList<>();

        String sql = """
                SELECT
                    id_rol,
                    id_permiso,
                    fecha_asignacion,
                    estado_asignacion
                FROM rol_permiso
                WHERE id_rol = ?
                  AND estado_asignacion = 'ACTIVA'
                ORDER BY id_permiso
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

                while (resultado.next()) {

                    permisos.add(
                            convertirRolPermiso(
                                    resultado
                            )
                    );
                }
            }
        }

        return permisos;
    }
    
    public boolean tienePermiso(
        Integer idRol,
        String modulo,
        String accion
) throws SQLException {

    String sql = """
            SELECT 1
            FROM rol_permiso rp
            INNER JOIN rol r
                ON r.id_rol = rp.id_rol
            INNER JOIN permiso p
                ON p.id_permiso = rp.id_permiso
            WHERE rp.id_rol = ?
              AND LOWER(p.modulo) = LOWER(?)
              AND LOWER(p.accion) = LOWER(?)
              AND rp.estado_asignacion = 'ACTIVA'
              AND r.estado_rol = TRUE
              AND p.estado_permiso = TRUE
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

        sentencia.setString(
                2,
                modulo
        );

        sentencia.setString(
                3,
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

    private RolPermiso convertirRolPermiso(
            ResultSet resultado
    ) throws SQLException {

        RolPermiso rolPermiso
                = new RolPermiso();

        rolPermiso.setIdRol(
                resultado.getInt(
                        "id_rol"
                )
        );

        rolPermiso.setIdPermiso(
                resultado.getInt(
                        "id_permiso"
                )
        );

        Date fechaAsignacion
                = resultado.getDate(
                        "fecha_asignacion"
                );

        if (fechaAsignacion != null) {

            rolPermiso.setFechaAsignacion(
                    fechaAsignacion.toLocalDate()
            );
        }

        rolPermiso.setEstadoAsignacion(
                resultado.getString(
                        "estado_asignacion"
                )
        );

        return rolPermiso;
    }
}
