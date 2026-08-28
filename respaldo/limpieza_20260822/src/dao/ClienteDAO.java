/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.Cliente;
import utilidades.GeneradorCodigos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
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

public class ClienteDAO {

    private static final String PREFIJO_CODIGO_CLIENTE
            = GeneradorCodigos.PREFIJO_CLIENTE;
    private static final long CLAVE_BLOQUEO_CODIGO_CLIENTE
            = GeneradorCodigos.crearClaveBloqueoAdvisory(
                    "cliente.codigo_cliente"
            );

    // GUARDAR
    public boolean guardar(
            Cliente cliente
    ) throws SQLException {

        Connection conexion
                = null;

        boolean autoCommitOriginal
                = true;

        try {

            conexion
                    = ConexionPostgreSQL.getConexion();

            autoCommitOriginal
                    = conexion.getAutoCommit();

            conexion.setAutoCommit(false);

            bloquearGeneracionCodigo(
                    conexion
            );

            String codigoGenerado
                    = generarSiguienteCodigo(
                            conexion
                    );

            cliente.setCodigoCliente(
                    codigoGenerado
            );

            boolean guardado
                    = insertarCliente(
                            conexion,
                            cliente
                    );

            conexion.commit();

            return guardado;

        } catch (SQLException | RuntimeException e) {

            if (conexion != null) {

                try {

                    conexion.rollback();

                } catch (SQLException ex) {

                    e.addSuppressed(
                            ex
                    );
                }
            }

            throw e;

        } finally {

            if (conexion != null) {

                try {

                    conexion.setAutoCommit(
                            autoCommitOriginal
                    );

                } finally {

                    conexion.close();
                }
            }
        }
    }

    // LISTAR
    public List<Cliente> listar(
            String criterio
    ) throws SQLException {

        List<Cliente> clientes
                = new ArrayList<>();

        String sql
                = "SELECT "
                + "p.id_persona, "
                + "p.cedula, "
                + "p.nombres, "
                + "p.apellidos, "
                + "p.fecha_nacimiento, "
                + "p.sexo, "
                + "p.telefono, "
                + "p.correo, "
                + "p.foto_perfil, "
                + "p.estado, "
                + "c.codigo_cliente, "
                + "c.fecha_registro, "
                + "c.peso_inicial, "
                + "c.peso_meta, "
                + "c.observaciones, "
                + "c.estado_cliente "
                + "FROM cliente c "
                + "INNER JOIN persona p "
                + "ON p.id_persona = c.id_persona "
                + "WHERE c.codigo_cliente ILIKE ? "
                + "OR p.cedula ILIKE ? "
                + "OR p.nombres ILIKE ? "
                + "OR p.apellidos ILIKE ? "
                + "OR p.correo ILIKE ? "
                + "ORDER BY c.estado_cliente DESC, "
                + "p.apellidos, p.nombres";

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

            sentencia.setString(1, filtro);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);
            sentencia.setString(4, filtro);
            sentencia.setString(5, filtro);

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                while (resultado.next()) {

                    clientes.add(
                            convertirCliente(
                                    resultado
                            )
                    );
                }
            }
        }

        return clientes;
    }

    public List<Cliente> listarPorEntrenador(
            Long idEntrenador,
            String criterio
    ) throws SQLException {
        String relacion = "EXISTS (SELECT 1 FROM asignacion_rutina ar "
                + "INNER JOIN rutina r ON r.id_rutina = ar.id_rutina "
                + "WHERE ar.id_cliente = c.id_persona "
                + "AND r.id_entrenador = ?)";
        return listarPorRelacion(idEntrenador, criterio, relacion);
    }

    public List<Cliente> listarPorNutricionista(
            Long idNutricionista,
            String criterio
    ) throws SQLException {
        String relacion = "EXISTS (SELECT 1 FROM plan_nutricional pn "
                + "WHERE pn.id_cliente = c.id_persona "
                + "AND pn.id_nutricionista = ?)";
        return listarPorRelacion(idNutricionista, criterio, relacion);
    }

    private List<Cliente> listarPorRelacion(
            Long idProfesional,
            String criterio,
            String relacion
    ) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT p.id_persona, p.cedula, p.nombres, "
                + "p.apellidos, p.fecha_nacimiento, p.sexo, p.telefono, "
                + "p.correo, p.foto_perfil, p.estado, c.codigo_cliente, "
                + "c.fecha_registro, c.peso_inicial, c.peso_meta, "
                + "c.observaciones, c.estado_cliente FROM cliente c "
                + "INNER JOIN persona p ON p.id_persona = c.id_persona "
                + "WHERE " + relacion + " AND (c.codigo_cliente ILIKE ? "
                + "OR p.cedula ILIKE ? OR p.nombres ILIKE ? "
                + "OR p.apellidos ILIKE ? OR p.correo ILIKE ?) "
                + "ORDER BY c.estado_cliente DESC, p.apellidos, p.nombres";
        String filtro = "%" + (criterio == null ? "" : criterio.trim()) + "%";

        try (Connection conexion = ConexionPostgreSQL.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, idProfesional);
            for (int i = 2; i <= 6; i++) {
                sentencia.setString(i, filtro);
            }
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    clientes.add(convertirCliente(resultado));
                }
            }
        }
        return clientes;
    }

    // BUSCAR POR ID DE PERSONA
    public Cliente buscar(
            Long idPersona
    ) throws SQLException {

        String sql
                = "SELECT "
                + "p.id_persona, "
                + "p.cedula, "
                + "p.nombres, "
                + "p.apellidos, "
                + "p.fecha_nacimiento, "
                + "p.sexo, "
                + "p.telefono, "
                + "p.correo, "
                + "p.foto_perfil, "
                + "p.estado, "
                + "c.codigo_cliente, "
                + "c.fecha_registro, "
                + "c.peso_inicial, "
                + "c.peso_meta, "
                + "c.observaciones, "
                + "c.estado_cliente "
                + "FROM cliente c "
                + "INNER JOIN persona p "
                + "ON p.id_persona = c.id_persona "
                + "WHERE c.id_persona = ?";

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

                if (resultado.next()) {

                    return convertirCliente(
                            resultado
                    );
                }
            }
        }

        return null;
    }

    // MODIFICAR
    public boolean modificar(
            Cliente cliente
    ) throws SQLException {

        String sql
                = "UPDATE cliente SET "
                + "fecha_registro = ?, "
                + "peso_inicial = ?, "
                + "peso_meta = ?, "
                + "observaciones = ?, "
                + "estado_cliente = ? "
                + "WHERE id_persona = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setDate(
                    1,
                    Date.valueOf(
                            cliente.getFechaRegistro()
                    )
            );

            sentencia.setBigDecimal(
                    2,
                    cliente.getPesoInicial()
            );

            sentencia.setBigDecimal(
                    3,
                    cliente.getPesoMeta()
            );

            colocarObservaciones(
                    sentencia,
                    4,
                    cliente.getObservaciones()
            );

            sentencia.setBoolean(
                    5,
                    cliente.isEstadoCliente()
            );

            sentencia.setLong(
                    6,
                    cliente.getIdPersona()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    // DESACTIVAR
    public boolean desactivar(
            Long idPersona
    ) throws SQLException {

        String sql
                = "UPDATE cliente "
                + "SET estado_cliente = FALSE "
                + "WHERE id_persona = ?";

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

            return sentencia.executeUpdate() > 0;
        }
    }

    // ELIMINAR DEFINITIVAMENTE
    public boolean eliminarDefinitivamente(
            Long idPersona
    ) throws SQLException {

        String sql
                = "DELETE FROM cliente "
                + "WHERE id_persona = ?";

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

            return sentencia.executeUpdate() > 0;
        }
    }

    // COMPROBAR CÓDIGO REPETIDO
    public boolean existeCodigo(
            String codigoCliente,
            Long idExcluir
    ) throws SQLException {

        String sql;

        if (idExcluir == null) {

            sql = "SELECT COUNT(*) "
                    + "FROM cliente "
                    + "WHERE codigo_cliente = ?";

        } else {

            sql = "SELECT COUNT(*) "
                    + "FROM cliente "
                    + "WHERE codigo_cliente = ? "
                    + "AND id_persona <> ?";
        }

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    codigoCliente.trim()
            );

            if (idExcluir != null) {

                sentencia.setLong(
                        2,
                        idExcluir
                );
            }

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    return resultado.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    private void bloquearGeneracionCodigo(
            Connection conexion
    ) throws SQLException {

        String sql
                = "SELECT pg_advisory_xact_lock(?)";

        try (
            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    CLAVE_BLOQUEO_CODIGO_CLIENTE
            );

            sentencia.execute();
        }
    }

    private String generarSiguienteCodigo(
            Connection conexion
    ) throws SQLException {

        String sql
                = "SELECT codigo_cliente "
                + "FROM cliente "
                + "WHERE codigo_cliente ~ ? "
                + "ORDER BY CAST(SUBSTRING(codigo_cliente FROM 3) "
                + "AS INTEGER) DESC "
                + "LIMIT 1";

        String ultimoCodigo
                = null;

        try (
            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                    1,
                    GeneradorCodigos.crearPatronPostgreSQL(
                            PREFIJO_CODIGO_CLIENTE
                    )
            );

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    ultimoCodigo
                            = resultado.getString(
                                    "codigo_cliente"
                            );
                }
            }
        }

        return GeneradorCodigos.generarSiguienteCodigo(
                PREFIJO_CODIGO_CLIENTE,
                ultimoCodigo
        );
    }

    private boolean insertarCliente(
            Connection conexion,
            Cliente cliente
    ) throws SQLException {

        String sql
                = "INSERT INTO cliente ("
                + "id_persona, "
                + "codigo_cliente, "
                + "fecha_registro, "
                + "peso_inicial, "
                + "peso_meta, "
                + "observaciones, "
                + "estado_cliente"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    cliente.getIdPersona()
            );

            sentencia.setString(
                    2,
                    cliente.getCodigoCliente()
            );

            sentencia.setDate(
                    3,
                    Date.valueOf(
                            cliente.getFechaRegistro()
                    )
            );

            sentencia.setBigDecimal(
                    4,
                    cliente.getPesoInicial()
            );

            sentencia.setBigDecimal(
                    5,
                    cliente.getPesoMeta()
            );

            colocarObservaciones(
                    sentencia,
                    6,
                    cliente.getObservaciones()
            );

            sentencia.setBoolean(
                    7,
                    cliente.isEstadoCliente()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    // CONVERTIR RESULTADO EN OBJETO CLIENTE
    private Cliente convertirCliente(
            ResultSet resultado
    ) throws SQLException {

        Cliente cliente
                = new Cliente();

        cliente.setIdPersona(
                resultado.getLong(
                        "id_persona"
                )
        );

        cliente.setCedula(
                resultado.getString(
                        "cedula"
                )
        );

        cliente.setNombres(
                resultado.getString(
                        "nombres"
                )
        );

        cliente.setApellidos(
                resultado.getString(
                        "apellidos"
                )
        );

        Date fechaNacimiento
                = resultado.getDate(
                        "fecha_nacimiento"
                );

        if (fechaNacimiento != null) {

            cliente.setFechaNacimiento(
                    fechaNacimiento.toLocalDate()
            );
        }

        cliente.setSexo(
                resultado.getString(
                        "sexo"
                )
        );

        cliente.setTelefono(
                resultado.getString(
                        "telefono"
                )
        );

        cliente.setCorreo(
                resultado.getString(
                        "correo"
                )
        );

        cliente.setFotoPerfil(
                resultado.getBytes(
                        "foto_perfil"
                )
        );

        cliente.setEstado(
                resultado.getBoolean(
                        "estado"
                )
        );

        cliente.setCodigoCliente(
                resultado.getString(
                        "codigo_cliente"
                )
        );

        Date fechaRegistro
                = resultado.getDate(
                        "fecha_registro"
                );

        if (fechaRegistro != null) {

            cliente.setFechaRegistro(
                    fechaRegistro.toLocalDate()
            );
        }

        BigDecimal pesoInicial
                = resultado.getBigDecimal(
                        "peso_inicial"
                );

        cliente.setPesoInicial(
                pesoInicial
        );

        BigDecimal pesoMeta
                = resultado.getBigDecimal(
                        "peso_meta"
                );

        cliente.setPesoMeta(
                pesoMeta
        );

        cliente.setObservaciones(
                resultado.getString(
                        "observaciones"
                )
        );

        cliente.setEstadoCliente(
                resultado.getBoolean(
                        "estado_cliente"
                )
        );

        return cliente;
    }

    // COLOCAR OBSERVACIONES O NULL
    private void colocarObservaciones(
            PreparedStatement sentencia,
            int posicion,
            String observaciones
    ) throws SQLException {

        if (observaciones == null
                || observaciones
                        .trim()
                        .isEmpty()) {

            sentencia.setNull(
                    posicion,
                    Types.VARCHAR
            );

        } else {

            sentencia.setString(
                    posicion,
                    observaciones.trim()
            );
        }
    }
}
