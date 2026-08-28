package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Verifica pertenencia de registros sin exponer JDBC a las vistas. */
public class AlcanceRolDAO {

    public boolean existe(String sql, Object... parametros) throws SQLException {
        try (Connection conexion = ConexionPostgreSQL.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                sentencia.setObject(i + 1, parametros[i]);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next() && resultado.getBoolean(1);
            }
        }
    }
}
