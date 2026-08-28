/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.Persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Usuario
 */

public class PersonaDAO {

    public Persona guardar(
            Persona persona
    ) throws SQLException {

        String sql = """
                INSERT INTO persona (
                    cedula,
                    nombres,
                    apellidos,
                    fecha_nacimiento,
                    sexo,
                    telefono,
                    correo,
                    foto_perfil,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id_persona
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {
            colocarParametros(sentencia, persona);

            try (ResultSet resultado
                    = sentencia.executeQuery()) {

                if (resultado.next()) {
                    persona.setIdPersona(
                            resultado.getLong("id_persona")
                    );
                }
            }
        }

        return persona;
    }

    public void modificar(
        Persona persona
) throws SQLException {

    String sql = """
            UPDATE persona
            SET nombres = ?,
                apellidos = ?,
                fecha_nacimiento = ?,
                sexo = ?,
                telefono = ?,
                correo = ?,
                foto_perfil = ?,
                estado = ?
            WHERE id_persona = ?
            """;

    try (
        Connection conexion
                = ConexionPostgreSQL.getConexion();

        PreparedStatement sentencia
                = conexion.prepareStatement(sql)
    ) {

        sentencia.setString(
                1,
                persona.getNombres().trim()
        );

        sentencia.setString(
                2,
                persona.getApellidos().trim()
        );

        sentencia.setObject(
                3,
                persona.getFechaNacimiento()
        );

        sentencia.setString(
                4,
                persona.getSexo()
        );

        sentencia.setString(
                5,
                persona.getTelefono().trim()
        );

        sentencia.setString(
                6,
                persona.getCorreo()
                        .trim()
                        .toLowerCase()
        );

        if (persona.getFotoPerfil() == null) {

            sentencia.setNull(
                    7,
                    Types.BINARY
            );

        } else {

            sentencia.setBytes(
                    7,
                    persona.getFotoPerfil()
            );
        }

        sentencia.setBoolean(
                8,
                persona.isEstado()
        );

        sentencia.setLong(
                9,
                persona.getIdPersona()
        );

        sentencia.executeUpdate();
    }
}

    public List<Persona> listar(
            String criterio
    ) throws SQLException {

        String sql = """
                SELECT
                    id_persona,
                    cedula,
                    nombres,
                    apellidos,
                    fecha_nacimiento,
                    sexo,
                    telefono,
                    correo,
                    foto_perfil,
                    estado
                FROM persona
                WHERE cedula ILIKE ?
                   OR nombres ILIKE ?
                   OR apellidos ILIKE ?
                   OR correo ILIKE ?
                ORDER BY estado DESC,
                         apellidos,
                         nombres
                """;

        List<Persona> personas = new ArrayList<>();

        String filtro = "%"
                + (criterio == null
                    ? ""
                    : criterio.trim())
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

            try (ResultSet resultado
                    = sentencia.executeQuery()) {

                while (resultado.next()) {
                    personas.add(
                            convertirPersona(resultado)
                    );
                }
            }
        }

        return personas;
    }

    public Optional<Persona> buscarPorId(
            Long idPersona
    ) throws SQLException {

        String sql = """
                SELECT
                    id_persona,
                    cedula,
                    nombres,
                    apellidos,
                    fecha_nacimiento,
                    sexo,
                    telefono,
                    correo,
                    foto_perfil,
                    estado
                FROM persona
                WHERE id_persona = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {
            sentencia.setLong(1, idPersona);

            try (ResultSet resultado
                    = sentencia.executeQuery()) {

                if (resultado.next()) {
                    return Optional.of(
                            convertirPersona(resultado)
                    );
                }
            }
        }

        return Optional.empty();
    }

    public boolean existeCedula(
            String cedula,
            Long idExcluir
    ) throws SQLException {

        String sql;

        if (idExcluir == null) {
            sql = """
                    SELECT COUNT(*)
                    FROM persona
                    WHERE cedula = ?
                    """;
        } else {
            sql = """
                    SELECT COUNT(*)
                    FROM persona
                    WHERE cedula = ?
                      AND id_persona <> ?
                    """;
        }

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, cedula);

            if (idExcluir != null) {
                sentencia.setLong(2, idExcluir);
            }

            try (ResultSet resultado
                    = sentencia.executeQuery()) {

                resultado.next();
                return resultado.getInt(1) > 0;
            }
        }
    }

    public void desactivar(
            Long idPersona
    ) throws SQLException {

        String sql = """
                UPDATE persona
                SET estado = FALSE
                WHERE id_persona = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {
            sentencia.setLong(1, idPersona);
            sentencia.executeUpdate();
        }
    }
    
        public boolean eliminarDefinitivamente(
            Long idPersona
    ) throws SQLException {

        String sql = """
                DELETE FROM persona
                WHERE id_persona = ?
                """;

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {
            sentencia.setLong(1, idPersona);

            return sentencia.executeUpdate() > 0;
        }
    }

        public List<Persona> listarDisponiblesParaCliente()
        throws SQLException {

    // Devuelve TODAS las personas activas. El módulo Clientes bloquea la
    // duplicación con un mensaje amistoso cuando ya existe un cliente
    // para esa persona, en vez de ocultarla del combo.
    String sql = """
            SELECT
                p.id_persona,
                p.cedula,
                p.nombres,
                p.apellidos,
                p.fecha_nacimiento,
                p.sexo,
                p.telefono,
                p.correo,
                p.foto_perfil,
                p.estado
            FROM persona p
            WHERE p.estado = TRUE
            ORDER BY p.apellidos,
                     p.nombres
            """;

    List<Persona> personas
            = new ArrayList<>();

    try (
        Connection conexion
                = ConexionPostgreSQL.getConexion();

        PreparedStatement sentencia
                = conexion.prepareStatement(sql);

        ResultSet resultado
                = sentencia.executeQuery()
    ) {

        while (resultado.next()) {

            personas.add(
                    convertirPersona(
                            resultado
                    )
            );
        }
    }

    return personas;
}

        public List<Persona> listarDisponiblesParaEmpleado()
        throws SQLException {

    // Devuelve TODAS las personas activas para que el selector del panel
    // Personal muestre siempre el universo completo. La restricción "no
    // duplicar empleado" se aplica en EmpleadoControlador.registrar
    // (mensaje amistoso) y como red de seguridad en el PK del empleado.
    String sql = """
            SELECT
                p.id_persona,
                p.cedula,
                p.nombres,
                p.apellidos,
                p.fecha_nacimiento,
                p.sexo,
                p.telefono,
                p.correo,
                p.foto_perfil,
                p.estado
            FROM persona p
            WHERE p.estado = TRUE
            ORDER BY p.apellidos,
                     p.nombres
            """;

    List<Persona> personas
            = new ArrayList<>();

    try (
        Connection conexion
                = ConexionPostgreSQL.getConexion();

        PreparedStatement sentencia
                = conexion.prepareStatement(sql);

        ResultSet resultado
                = sentencia.executeQuery()
    ) {

        while (resultado.next()) {

            personas.add(
                    convertirPersona(
                            resultado
                    )
            );
        }
    }

    return personas;
}

    private void colocarParametros(
            PreparedStatement sentencia,
            Persona persona
    ) throws SQLException {

        sentencia.setString(
                1,
                persona.getCedula().trim()
        );

        sentencia.setString(
                2,
                persona.getNombres().trim()
        );

        sentencia.setString(
                3,
                persona.getApellidos().trim()
        );

        sentencia.setObject(
                4,
                persona.getFechaNacimiento()
        );

        sentencia.setString(
                5,
                persona.getSexo()
        );

        sentencia.setString(
                6,
                persona.getTelefono().trim()
        );

        sentencia.setString(
                7,
                persona.getCorreo()
                        .trim()
                        .toLowerCase()
        );

        if (persona.getFotoPerfil() == null) {
            sentencia.setNull(8, Types.BINARY);
        } else {
            sentencia.setBytes(
                    8,
                    persona.getFotoPerfil()
            );
        }

        sentencia.setBoolean(
                9,
                persona.isEstado()
        );
    }

    private Persona convertirPersona(
            ResultSet resultado
    ) throws SQLException {

        Persona persona = new Persona();

        persona.setIdPersona(
                resultado.getLong("id_persona")
        );

        persona.setCedula(
                resultado.getString("cedula")
        );

        persona.setNombres(
                resultado.getString("nombres")
        );

        persona.setApellidos(
                resultado.getString("apellidos")
        );

        persona.setFechaNacimiento(
                resultado.getObject(
                        "fecha_nacimiento",
                        LocalDate.class
                )
        );

        persona.setSexo(
                resultado.getString("sexo")
        );

        persona.setTelefono(
                resultado.getString("telefono")
        );

        persona.setCorreo(
                resultado.getString("correo")
        );

        persona.setFotoPerfil(
                resultado.getBytes("foto_perfil")
        );

        persona.setEstado(
                resultado.getBoolean("estado")
        );

        return persona;
    }
}
