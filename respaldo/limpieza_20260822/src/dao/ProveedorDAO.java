package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import modelo.Proveedor;

/**
 * DAO para la tabla proveedor.
 *
 * @author Usuario
 */
public class ProveedorDAO {

    public boolean guardar(
            Proveedor proveedor
    ) throws SQLException {

        String sql = "INSERT INTO proveedor (ruc, razon_social, nombre_comercial, correo, direccion_proveedor, nombre_contacto, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    proveedor.getRuc()
            );

            sentencia.setString(
                    2,
                    proveedor.getRazonSocial()
            );

            sentencia.setString(
                    3,
                    proveedor.getNombreComercial()
            );

            sentencia.setString(
                    4,
                    proveedor.getCorreo()
            );

            sentencia.setString(
                    5,
                    proveedor.getDireccionProveedor()
            );

            sentencia.setString(
                    6,
                    proveedor.getNombreContacto()
            );

            colocarFechaOpcional(
                    sentencia,
                    7,
                    proveedor.getFechaRegistro()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean modificar(
            Proveedor proveedor
    ) throws SQLException {

        String sql = "UPDATE proveedor SET ruc = ?, razon_social = ?, nombre_comercial = ?, correo = ?, direccion_proveedor = ?, nombre_contacto = ?, fecha_registro = ? WHERE id_proveedor = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    proveedor.getRuc()
            );

            sentencia.setString(
                    2,
                    proveedor.getRazonSocial()
            );

            sentencia.setString(
                    3,
                    proveedor.getNombreComercial()
            );

            sentencia.setString(
                    4,
                    proveedor.getCorreo()
            );

            sentencia.setString(
                    5,
                    proveedor.getDireccionProveedor()
            );

            sentencia.setString(
                    6,
                    proveedor.getNombreContacto()
            );

            colocarFechaOpcional(
                    sentencia,
                    7,
                    proveedor.getFechaRegistro()
            );

            sentencia.setLong(
                    8,
                    proveedor.getIdProveedor()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public Proveedor buscar(
            Long idProveedor
    ) throws SQLException {

        String sql = "SELECT id_proveedor, ruc, razon_social, nombre_comercial, correo, direccion_proveedor, nombre_contacto, fecha_registro FROM proveedor WHERE id_proveedor = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idProveedor);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                if (resultado.next()) {
                    return convertirProveedor(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    public List<Proveedor> listar(
            String criterio
    ) throws SQLException {

        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT id_proveedor, ruc, razon_social, nombre_comercial, correo, direccion_proveedor, nombre_contacto, fecha_registro FROM proveedor WHERE ruc ILIKE ? OR razon_social ILIKE ? OR nombre_comercial ILIKE ? OR correo ILIKE ? OR direccion_proveedor ILIKE ? OR nombre_contacto ILIKE ? ORDER BY id_proveedor";

        if (criterio == null) {
            criterio = "";
        }

        String filtro = "%" + criterio.trim() + "%";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, filtro);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);
            sentencia.setString(4, filtro);
            sentencia.setString(5, filtro);
            sentencia.setString(6, filtro);

            try (
                ResultSet resultado = sentencia.executeQuery()
            ) {

                while (resultado.next()) {
                    lista.add(
                            convertirProveedor(
                                    resultado
                            )
                    );
                }
            }
        }

        return lista;
    }

    public boolean eliminarDefinitivamente(
            Long idProveedor
    ) throws SQLException {

        String sql = "DELETE FROM proveedor WHERE id_proveedor = ?";

        try (
            Connection conexion = ConexionPostgreSQL.getConexion();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idProveedor);

            return sentencia.executeUpdate() > 0;
        }
    }

    private void colocarFechaOpcional(
            PreparedStatement sentencia,
            int posicion,
            java.time.LocalDate fecha
    ) throws SQLException {

        if (fecha == null) {
            sentencia.setNull(
                    posicion,
                    Types.DATE
            );
        } else {
            sentencia.setDate(
                    posicion,
                    Date.valueOf(
                            fecha
                    )
            );
        }
    }

    private Proveedor convertirProveedor(
            ResultSet resultado
    ) throws SQLException {

        Proveedor proveedor = new Proveedor();

        proveedor.setIdProveedor(
                resultado.getLong("id_proveedor")
        );

        proveedor.setRuc(
                resultado.getString("ruc")
        );

        proveedor.setRazonSocial(
                resultado.getString("razon_social")
        );

        proveedor.setNombreComercial(
                resultado.getString("nombre_comercial")
        );

        proveedor.setCorreo(
                resultado.getString("correo")
        );

        proveedor.setDireccionProveedor(
                resultado.getString("direccion_proveedor")
        );

        proveedor.setNombreContacto(
                resultado.getString("nombre_contacto")
        );

        proveedor.setFechaRegistro(
                resultado.getDate("fecha_registro") == null
                ? null
                : resultado.getDate("fecha_registro").toLocalDate()
        );

        return proveedor;
    }
}
