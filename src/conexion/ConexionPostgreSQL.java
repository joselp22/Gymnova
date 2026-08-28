/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author Usuario
 */

public final class ConexionPostgreSQL {

    private static final String URL_PREDETERMINADA
            = "jdbc:postgresql://dpg-da8eru3tqb8s73a0r44g-a.virginia-postgres.render.com:5432/gymnova_db?sslmode=require";

    private static final String USUARIO_PREDETERMINADO = "gymnova_db_user";

    private static final String CONTRASENA_PREDETERMINADA = "n14bKerizAJvXaPaowFGK9iWhePtTWxO";
    
    private ConexionPostgreSQL() {
        // Evita crear objetos de esta clase.
    }

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(
                valorConfigurado(
                        "gymnova.db.url",
                        URL_PREDETERMINADA
                ),
                valorConfigurado(
                        "gymnova.db.usuario",
                        USUARIO_PREDETERMINADO
                ),
                valorConfigurado(
                        "gymnova.db.contrasena",
                        CONTRASENA_PREDETERMINADA
                )
        );
    }

    private static String valorConfigurado(
            String propiedad,
            String valorPredeterminado
    ) {

        String valor = System.getProperty(propiedad);

        if (valor == null || valor.isBlank()) {
            return valorPredeterminado;
        }

        return valor.trim();
    }
}
