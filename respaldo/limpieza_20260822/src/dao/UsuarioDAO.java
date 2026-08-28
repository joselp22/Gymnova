/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Usuario
 */
public class UsuarioDAO {

    public boolean guardar(
            Usuario usuario
    ) throws SQLException {

        String sql = """
                INSERT INTO usuario (
                    id_persona,
                    id_rol,
                    nombre_usuario,
                    clave_hash,
                    bloqueado,
                    intentos_fallidos,
                    estado_usuario,
                    fecha_creacion,
                    ultimo_acceso
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, NULL)
                RETURNING id_usuario,
                          fecha_creacion
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            colocarIdPersona(
                    sentencia,
                    1,
                    usuario.getIdPersona()
            );

            sentencia.setInt(
                    2,
                    usuario.getIdRol()
            );

            sentencia.setString(
                    3,
                    usuario.getNombreUsuario()
            );

            sentencia.setString(
                    4,
                    usuario.getClaveHash()
            );

            sentencia.setBoolean(
                    5,
                    usuario.isBloqueado()
            );

            sentencia.setInt(
                    6,
                    usuario.getIntentosFallidos()
            );

            sentencia.setBoolean(
                    7,
                    usuario.isEstadoUsuario()
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    usuario.setIdUsuario(
                            resultado.getLong(
                                    "id_usuario"
                            )
                    );

                    usuario.setFechaCreacion(
                            resultado.getObject(
                                    "fecha_creacion",
                                    LocalDateTime.class
                            )
                    );

                    return true;
                }
            }
        }

        return false;
    }

    public boolean modificar(
            Usuario usuario
    ) throws SQLException {

        String sql = """
                UPDATE usuario
                SET id_persona = ?,
                    id_rol = ?,
                    nombre_usuario = ?,
                    bloqueado = ?,
                    intentos_fallidos = ?,
                    estado_usuario = ?
                WHERE id_usuario = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            colocarIdPersona(
                    sentencia,
                    1,
                    usuario.getIdPersona()
            );

            sentencia.setInt(
                    2,
                    usuario.getIdRol()
            );

            sentencia.setString(
                    3,
                    usuario.getNombreUsuario()
            );

            sentencia.setBoolean(
                    4,
                    usuario.isBloqueado()
            );

            sentencia.setInt(
                    5,
                    usuario.getIntentosFallidos()
            );

            sentencia.setBoolean(
                    6,
                    usuario.isEstadoUsuario()
            );

            sentencia.setLong(
                    7,
                    usuario.getIdUsuario()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean actualizarClave(
            Long idUsuario,
            String claveHash
    ) throws SQLException {

        String sql = """
                UPDATE usuario
                SET clave_hash = ?,
                    intentos_fallidos = 0,
                    bloqueado = FALSE
                WHERE id_usuario = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    claveHash
            );

            sentencia.setLong(
                    2,
                    idUsuario
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean desactivar(
            Long idUsuario
    ) throws SQLException {

        String sql = """
                UPDATE usuario
                SET estado_usuario = FALSE
                WHERE id_usuario = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idUsuario
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean activar(
            Long idUsuario
    ) throws SQLException {

        String sql = """
                UPDATE usuario
                SET estado_usuario = TRUE
                WHERE id_usuario = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idUsuario
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean bloquear(
            Long idUsuario
    ) throws SQLException {

        String sql = """
                UPDATE usuario
                SET bloqueado = TRUE
                WHERE id_usuario = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idUsuario
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean desbloquear(
            Long idUsuario
    ) throws SQLException {

        String sql = """
                UPDATE usuario
                SET bloqueado = FALSE,
                    intentos_fallidos = 0
                WHERE id_usuario = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idUsuario
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean eliminarDefinitivamente(
            Long idUsuario
    ) throws SQLException {

        String sql = """
                DELETE FROM usuario
                WHERE id_usuario = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idUsuario
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Usuario buscar(
            Long idUsuario
    ) throws SQLException {

        String sql = sqlBaseConsulta()
                + """
                WHERE u.id_usuario = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idUsuario
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return convertirUsuario(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public Optional<Usuario> buscarPorNombre(
            String nombreUsuario
    ) throws SQLException {

        if (nombreUsuario == null
                || nombreUsuario.isBlank()) {

            return Optional.empty();
        }

        String sql = sqlBaseConsulta()
                + """
                WHERE LOWER(u.nombre_usuario) = LOWER(?)
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    nombreUsuario.trim()
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    Usuario usuario
                            = convertirUsuario(
                                    resultado
                            );

                    return Optional.of(
                            usuario
                    );
                }
            }
        }

        return Optional.empty();
    }

    public List<Usuario> listar(
            String criterio
    ) throws SQLException {

        List<Usuario> usuarios
                = new ArrayList<>();

        String sql = sqlBaseConsulta()
                + """
                WHERE u.nombre_usuario ILIKE ?
                   OR r.nombre_rol ILIKE ?
                   OR p.cedula ILIKE ?
                   OR p.nombres ILIKE ?
                   OR p.apellidos ILIKE ?
                   OR p.correo ILIKE ?
                ORDER BY u.estado_usuario DESC,
                         u.bloqueado,
                         u.nombre_usuario
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

            for (int i = 1; i <= 6; i++) {

                sentencia.setString(
                        i,
                        filtro
                );
            }

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    usuarios.add(
                            convertirUsuario(
                                    resultado
                            )
                    );
                }
            }
        }

        return usuarios;
    }

    public boolean existeNombre(
            String nombreUsuario
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM usuario
                WHERE LOWER(nombre_usuario) = LOWER(?)
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    nombreUsuario
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
            Long idUsuario,
            String nombreUsuario
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM usuario
                WHERE LOWER(nombre_usuario) = LOWER(?)
                  AND id_usuario <> ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    nombreUsuario
            );

            sentencia.setLong(
                    2,
                    idUsuario
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    public boolean personaTieneUsuario(
            Long idPersona
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM usuario
                WHERE id_persona = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idPersona
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    public boolean personaTieneUsuarioEnOtroRegistro(
            Long idUsuario,
            Long idPersona
    ) throws SQLException {

        String sql = """
                SELECT 1
                FROM usuario
                WHERE id_persona = ?
                  AND id_usuario <> ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idPersona
            );

            sentencia.setLong(
                    2,
                    idUsuario
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    public void registrarIntentoFallido(
            Long idUsuario
    ) throws SQLException {

        if (idUsuario == null) {

            throw new IllegalArgumentException(
                    "No se recibio el usuario."
            );
        }

        String sql = """
                UPDATE usuario
                SET intentos_fallidos = intentos_fallidos + 1,
                    bloqueado = CASE
                        WHEN intentos_fallidos + 1 >= 3
                        THEN TRUE
                        ELSE bloqueado
                    END
                WHERE id_usuario = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idUsuario
            );

            sentencia.executeUpdate();
        }
    }

    public void registrarAccesoCorrecto(
            Long idUsuario
    ) throws SQLException {

        if (idUsuario == null) {

            throw new IllegalArgumentException(
                    "No se recibio el usuario."
            );
        }

        String sql = """
                UPDATE usuario
                SET intentos_fallidos = 0,
                    ultimo_acceso = CURRENT_TIMESTAMP
                WHERE id_usuario = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idUsuario
            );

            sentencia.executeUpdate();
        }
    }

    private String sqlBaseConsulta() {

        return """
                SELECT
                    u.id_usuario,
                    u.id_persona,
                    u.id_rol,
                    u.nombre_usuario,
                    u.clave_hash,
                    u.bloqueado,
                    u.intentos_fallidos,
                    u.estado_usuario,
                    u.fecha_creacion,
                    u.ultimo_acceso,
                    r.nombre_rol,
                    r.estado_rol
                FROM usuario u
                INNER JOIN rol r
                    ON r.id_rol = u.id_rol
                LEFT JOIN persona p
                    ON p.id_persona = u.id_persona
                """;
    }

    private Usuario convertirUsuario(
            ResultSet resultado
    ) throws SQLException {

        Usuario usuario
                = new Usuario();

        usuario.setIdUsuario(
                resultado.getLong(
                        "id_usuario"
                )
        );

        Object idPersona
                = resultado.getObject(
                        "id_persona"
                );

        if (idPersona != null) {

            usuario.setIdPersona(
                    ((Number) idPersona).longValue()
            );
        }

        usuario.setIdRol(
                resultado.getInt(
                        "id_rol"
                )
        );

        usuario.setNombreUsuario(
                resultado.getString(
                        "nombre_usuario"
                )
        );

        usuario.setClaveHash(
                resultado.getString(
                        "clave_hash"
                )
        );

        usuario.setBloqueado(
                resultado.getBoolean(
                        "bloqueado"
                )
        );

        usuario.setIntentosFallidos(
                resultado.getInt(
                        "intentos_fallidos"
                )
        );

        boolean usuarioActivo
                = resultado.getBoolean(
                        "estado_usuario"
                );

        boolean rolActivo
                = resultado.getBoolean(
                        "estado_rol"
                );

        usuario.setEstadoUsuario(
                usuarioActivo && rolActivo
        );

        usuario.setFechaCreacion(
                resultado.getObject(
                        "fecha_creacion",
                        LocalDateTime.class
                )
        );

        usuario.setUltimoAcceso(
                resultado.getObject(
                        "ultimo_acceso",
                        LocalDateTime.class
                )
        );

        usuario.setNombreRol(
                resultado.getString(
                        "nombre_rol"
                )
        );

        return usuario;
    }

    private void colocarIdPersona(
            PreparedStatement sentencia,
            int posicion,
            Long idPersona
    ) throws SQLException {

        if (idPersona == null) {

            sentencia.setNull(
                    posicion,
                    Types.BIGINT
            );

        } else {

            sentencia.setLong(
                    posicion,
                    idPersona
            );
        }
    }
}
