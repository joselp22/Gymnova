/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.NutricionistaCertificacion;

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
public class NutricionistaCertificacionDAO {

    public boolean guardar(
            NutricionistaCertificacion nutricionistaCertificacion
    ) throws SQLException {

        String sql
                = "INSERT INTO nutricionista_certificacion ("
                + "id_nutricionista, "
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
                    nutricionistaCertificacion.getIdNutricionista()
            );

            sentencia.setString(
                    2,
                    nutricionistaCertificacion.getCertificacion()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<NutricionistaCertificacion> listarPorNutricionista(
            Long idNutricionista
    ) throws SQLException {

        List<NutricionistaCertificacion> certificaciones
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "id_nutricionista, "
                + "certificacion "
                + "FROM nutricionista_certificacion "
                + "WHERE id_nutricionista = ? "
                + "ORDER BY certificacion";

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
            Long idNutricionista,
            String certificacion
    ) throws SQLException {

        String sql
                = "SELECT 1 "
                + "FROM nutricionista_certificacion "
                + "WHERE id_nutricionista = ? "
                + "AND certificacion = ?";

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
            Long idNutricionista,
            String certificacion
    ) throws SQLException {

        String sql
                = "DELETE FROM nutricionista_certificacion "
                + "WHERE id_nutricionista = ? "
                + "AND certificacion = ?";

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

            sentencia.setString(
                    2,
                    certificacion
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    private NutricionistaCertificacion mapearCertificacion(
            ResultSet resultado
    ) throws SQLException {

        NutricionistaCertificacion certificacion
                = new NutricionistaCertificacion();

        certificacion.setIdNutricionista(
                resultado.getLong(
                        "id_nutricionista"
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
