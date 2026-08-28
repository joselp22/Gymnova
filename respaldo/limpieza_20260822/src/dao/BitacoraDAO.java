/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.Bitacora;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class BitacoraDAO {

    public boolean registrar(
            Bitacora bitacora
    ) throws SQLException {

        String sql = """
                INSERT INTO bitacora (
                    fecha_hora,
                    accion_realizada,
                    modulo,
                    direccion_ip,
                    descripcion,
                    resultado,
                    id_usuario
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id_bitacora
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setTimestamp(
                    1,
                    Timestamp.valueOf(
                            bitacora.getFechaHora()
                    )
            );

            sentencia.setString(
                    2,
                    bitacora.getAccionRealizada()
            );

            sentencia.setString(
                    3,
                    bitacora.getModulo()
            );

            colocarTextoOpcional(
                    sentencia,
                    4,
                    bitacora.getDireccionIp()
            );

            colocarTextoOpcional(
                    sentencia,
                    5,
                    bitacora.getDescripcion()
            );

            colocarTextoOpcional(
                    sentencia,
                    6,
                    bitacora.getResultado()
            );

            sentencia.setLong(
                    7,
                    bitacora.getIdUsuario()
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    bitacora.setIdBitacora(
                            resultado.getLong(
                                    "id_bitacora"
                            )
                    );

                    return true;
                }
            }
        }

        return false;
    }

    public Bitacora buscar(
            Long idBitacora
    ) throws SQLException {

        String sql = """
                SELECT
                    b.id_bitacora,
                    b.fecha_hora,
                    b.accion_realizada,
                    b.modulo,
                    b.direccion_ip,
                    b.descripcion,
                    b.resultado,
                    b.id_usuario,
                    u.nombre_usuario
                FROM bitacora b
                INNER JOIN usuario u
                    ON u.id_usuario = b.id_usuario
                WHERE b.id_bitacora = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    idBitacora
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return convertirBitacora(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Bitacora> listar(
            String criterio
    ) throws SQLException {

        List<Bitacora> registros
                = new ArrayList<>();

        String sql = """
                SELECT
                    b.id_bitacora,
                    b.fecha_hora,
                    b.accion_realizada,
                    b.modulo,
                    b.direccion_ip,
                    b.descripcion,
                    b.resultado,
                    b.id_usuario,
                    u.nombre_usuario
                FROM bitacora b
                INNER JOIN usuario u
                    ON u.id_usuario = b.id_usuario
                WHERE b.accion_realizada ILIKE ?
                   OR b.modulo ILIKE ?
                   OR b.direccion_ip ILIKE ?
                   OR b.descripcion ILIKE ?
                   OR b.resultado ILIKE ?
                   OR u.nombre_usuario ILIKE ?
                ORDER BY b.fecha_hora DESC,
                         b.id_bitacora DESC
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

                    registros.add(
                            convertirBitacora(
                                    resultado
                            )
                    );
                }
            }
        }

        return registros;
    }

    public List<Bitacora> listarPorUsuario(
            Long idUsuario
    ) throws SQLException {

        List<Bitacora> registros
                = new ArrayList<>();

        String sql = """
                SELECT
                    b.id_bitacora,
                    b.fecha_hora,
                    b.accion_realizada,
                    b.modulo,
                    b.direccion_ip,
                    b.descripcion,
                    b.resultado,
                    b.id_usuario,
                    u.nombre_usuario
                FROM bitacora b
                INNER JOIN usuario u
                    ON u.id_usuario = b.id_usuario
                WHERE b.id_usuario = ?
                ORDER BY b.fecha_hora DESC,
                         b.id_bitacora DESC
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

                while (resultado.next()) {

                    registros.add(
                            convertirBitacora(
                                    resultado
                            )
                    );
                }
            }
        }

        return registros;
    }

    public List<Bitacora> listarPorModulo(
            String modulo
    ) throws SQLException {

        List<Bitacora> registros
                = new ArrayList<>();

        String sql = """
                SELECT
                    b.id_bitacora,
                    b.fecha_hora,
                    b.accion_realizada,
                    b.modulo,
                    b.direccion_ip,
                    b.descripcion,
                    b.resultado,
                    b.id_usuario,
                    u.nombre_usuario
                FROM bitacora b
                INNER JOIN usuario u
                    ON u.id_usuario = b.id_usuario
                WHERE LOWER(b.modulo) = LOWER(?)
                ORDER BY b.fecha_hora DESC,
                         b.id_bitacora DESC
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

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    registros.add(
                            convertirBitacora(
                                    resultado
                            )
                    );
                }
            }
        }

        return registros;
    }

    public List<Bitacora> listarPorFechas(
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    ) throws SQLException {

        List<Bitacora> registros
                = new ArrayList<>();

        String sql = """
                SELECT
                    b.id_bitacora,
                    b.fecha_hora,
                    b.accion_realizada,
                    b.modulo,
                    b.direccion_ip,
                    b.descripcion,
                    b.resultado,
                    b.id_usuario,
                    u.nombre_usuario
                FROM bitacora b
                INNER JOIN usuario u
                    ON u.id_usuario = b.id_usuario
                WHERE b.fecha_hora >= ?
                  AND b.fecha_hora <= ?
                ORDER BY b.fecha_hora DESC,
                         b.id_bitacora DESC
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setTimestamp(
                    1,
                    Timestamp.valueOf(
                            fechaInicio
                    )
            );

            sentencia.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            fechaFin
                    )
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    registros.add(
                            convertirBitacora(
                                    resultado
                            )
                    );
                }
            }
        }

        return registros;
    }

    private Bitacora convertirBitacora(
            ResultSet resultado
    ) throws SQLException {

        Bitacora bitacora
                = new Bitacora();

        bitacora.setIdBitacora(
                resultado.getLong(
                        "id_bitacora"
                )
        );

        bitacora.setFechaHora(
                resultado.getObject(
                        "fecha_hora",
                        LocalDateTime.class
                )
        );

        bitacora.setAccionRealizada(
                resultado.getString(
                        "accion_realizada"
                )
        );

        bitacora.setModulo(
                resultado.getString(
                        "modulo"
                )
        );

        bitacora.setDireccionIp(
                resultado.getString(
                        "direccion_ip"
                )
        );

        bitacora.setDescripcion(
                resultado.getString(
                        "descripcion"
                )
        );

        bitacora.setResultado(
                resultado.getString(
                        "resultado"
                )
        );

        bitacora.setIdUsuario(
                resultado.getLong(
                        "id_usuario"
                )
        );

        bitacora.setNombreUsuario(
                resultado.getString(
                        "nombre_usuario"
                )
        );

        return bitacora;
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
