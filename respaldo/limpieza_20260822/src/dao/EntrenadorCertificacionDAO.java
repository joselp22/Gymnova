/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.EntrenadorCertificacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Usuario
 */

public class EntrenadorCertificacionDAO {

    public boolean guardar(
            EntrenadorCertificacion entrenadorCertificacion
    ) throws SQLException {

        String sql
                = "INSERT INTO entrenador_certificacion ("
                + "id_entrenador, "
                + "certificacion"
                + ") VALUES (?, ?)";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    entrenadorCertificacion.getIdEntrenador()
            );

            sentencia.setString(
                    2,
                    entrenadorCertificacion.getCertificacion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<EntrenadorCertificacion> listarPorEntrenador(
            Long idEntrenador
    ) throws SQLException {

        List<EntrenadorCertificacion> certificaciones
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "id_entrenador, "
                + "certificacion "
                + "FROM entrenador_certificacion "
                + "WHERE id_entrenador = ? "
                + "ORDER BY certificacion";

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

                    certificaciones.add(
                            mapearCertificacion(resultado)
                    );
                }
            }
        }

        return certificaciones;
    }

    public boolean existe(
            Long idEntrenador,
            String certificacion
    ) throws SQLException {

        String sql
                = "SELECT 1 "
                + "FROM entrenador_certificacion "
                + "WHERE id_entrenador = ? "
                + "AND certificacion = ?";

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

            sentencia.setString(
                    2,
                    certificacion
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                return resultado.next();
            }
        }
    }

    public boolean eliminar(
            Long idEntrenador,
            String certificacion
    ) throws SQLException {

        String sql
                = "DELETE FROM entrenador_certificacion "
                + "WHERE id_entrenador = ? "
                + "AND certificacion = ?";

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

            sentencia.setString(
                    2,
                    certificacion
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    private EntrenadorCertificacion mapearCertificacion(
            ResultSet resultado
    ) throws SQLException {

        EntrenadorCertificacion certificacion
                = new EntrenadorCertificacion();

        certificacion.setIdEntrenador(
                resultado.getLong(
                        "id_entrenador"
                )
        );

        certificacion.setCertificacion(
                resultado.getString(
                        "certificacion"
                )
        );

        return certificacion;
    }
}
