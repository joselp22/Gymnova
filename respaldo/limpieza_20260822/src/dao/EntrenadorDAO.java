/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexionPostgreSQL;
import modelo.Empleado;
import modelo.Entrenador;

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

public class EntrenadorDAO {

    private final EmpleadoDAO empleadoDAO;

    public EntrenadorDAO() {
        empleadoDAO = new EmpleadoDAO();
    }

    public boolean guardar(
            Entrenador entrenador
    ) throws SQLException {

        String sql
                = "INSERT INTO entrenador ("
                + "id_persona, "
                + "fecha_inicio_profesion, "
                + "nivel_entrenador, "
                + "estado_entrenador"
                + ") VALUES (?, ?, ?, ?)";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(
                    1,
                    entrenador.getIdPersona()
            );

            if (entrenador.getFechaInicioProfesion() == null) {

                sentencia.setNull(
                        2,
                        Types.DATE
                );

            } else {

                sentencia.setDate(
                        2,
                        Date.valueOf(
                                entrenador.getFechaInicioProfesion()
                        )
                );
            }

            sentencia.setString(
                    3,
                    entrenador.getNivelEntrenador()
            );

            sentencia.setBoolean(
                    4,
                    entrenador.isEstadoEntrenador()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public List<Entrenador> listar(
            String criterio
    ) throws SQLException {

        List<Long> identificadores
                = new ArrayList<>();

        String sql
                = "SELECT t.id_persona "
                + "FROM entrenador t "
                + "INNER JOIN empleado e "
                + "ON e.id_persona = t.id_persona "
                + "INNER JOIN persona p "
                + "ON p.id_persona = e.id_persona "
                + "WHERE e.codigo_empleado ILIKE ? "
                + "OR p.cedula ILIKE ? "
                + "OR p.nombres ILIKE ? "
                + "OR p.apellidos ILIKE ? "
                + "OR t.nivel_entrenador ILIKE ? "
                + "ORDER BY t.estado_entrenador DESC, "
                + "p.apellidos, p.nombres";

        if (criterio == null) {
            criterio = "";
        }

        String filtro
                = "%" + criterio.trim() + "%";

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

                    identificadores.add(
                            resultado.getLong(
                                    "id_persona"
                            )
                    );
                }
            }
        }

        List<Entrenador> entrenadores
                = new ArrayList<>();

        for (Long idPersona : identificadores) {

            Entrenador entrenador
                    = buscar(idPersona);

            if (entrenador != null) {
                entrenadores.add(entrenador);
            }
        }

        return entrenadores;
    }

    public Entrenador buscar(
            Long idPersona
    ) throws SQLException {

        Empleado empleado
                = empleadoDAO.buscar(idPersona);

        if (empleado == null) {
            return null;
        }

        String sql
                = "SELECT "
                + "fecha_inicio_profesion, "
                + "nivel_entrenador, "
                + "estado_entrenador "
                + "FROM entrenador "
                + "WHERE id_persona = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            sentencia.setLong(1, idPersona);

            try (
                ResultSet resultado
                        = sentencia.executeQuery()
            ) {

                if (resultado.next()) {

                    Entrenador entrenador
                            = new Entrenador();

                    copiarDatosEmpleado(
                            empleado,
                            entrenador
                    );

                    Date fechaInicio
                            = resultado.getDate(
                                    "fecha_inicio_profesion"
                            );

                    if (fechaInicio != null) {

                        entrenador.setFechaInicioProfesion(
                                fechaInicio.toLocalDate()
                        );
                    }

                    entrenador.setNivelEntrenador(
                            resultado.getString(
                                    "nivel_entrenador"
                            )
                    );

                    entrenador.setEstadoEntrenador(
                            resultado.getBoolean(
                                    "estado_entrenador"
                            )
                    );

                    return entrenador;
                }
            }
        }

        return null;
    }

    public boolean modificar(
            Entrenador entrenador
    ) throws SQLException {

        String sql
                = "UPDATE entrenador SET "
                + "fecha_inicio_profesion = ?, "
                + "nivel_entrenador = ?, "
                + "estado_entrenador = ? "
                + "WHERE id_persona = ?";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql)
        ) {

            if (entrenador.getFechaInicioProfesion() == null) {

                sentencia.setNull(
                        1,
                        Types.DATE
                );

            } else {

                sentencia.setDate(
                        1,
                        Date.valueOf(
                                entrenador.getFechaInicioProfesion()
                        )
                );
            }

            sentencia.setString(
                    2,
                    entrenador.getNivelEntrenador()
            );

            sentencia.setBoolean(
                    3,
                    entrenador.isEstadoEntrenador()
            );

            sentencia.setLong(
                    4,
                    entrenador.getIdPersona()
            );

            return sentencia.executeUpdate() > 0;
        }
    }

    public boolean desactivar(
            Long idPersona
    ) throws SQLException {

        String sql
                = "UPDATE entrenador "
                + "SET estado_entrenador = FALSE "
                + "WHERE id_persona = ?";

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

    public boolean eliminarDefinitivamente(
            Long idPersona
    ) throws SQLException {

        String sql
                = "DELETE FROM entrenador "
                + "WHERE id_persona = ?";

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

    public List<Empleado>
            listarEmpleadosDisponibles()
            throws SQLException {

        List<Long> identificadores
                = new ArrayList<>();

        String sql
                = "SELECT e.id_persona "
                + "FROM empleado e "
                + "INNER JOIN persona p "
                + "ON p.id_persona = e.id_persona "
                + "WHERE e.estado_empleado = TRUE "
                + "AND p.estado = TRUE "
                + "AND NOT EXISTS ("
                + "SELECT 1 FROM entrenador t "
                + "WHERE t.id_persona = e.id_persona"
                + ") "
                + "ORDER BY p.apellidos, p.nombres";

        try (
            Connection conexion
                    = ConexionPostgreSQL.getConexion();

            PreparedStatement sentencia
                    = conexion.prepareStatement(sql);

            ResultSet resultado
                    = sentencia.executeQuery()
        ) {

            while (resultado.next()) {

                identificadores.add(
                        resultado.getLong(
                                "id_persona"
                        )
                );
            }
        }

        List<Empleado> empleados
                = new ArrayList<>();

        for (Long idPersona : identificadores) {

            Empleado empleado
                    = empleadoDAO.buscar(idPersona);

            if (empleado != null) {
                empleados.add(empleado);
            }
        }

        return empleados;
    }

    private void copiarDatosEmpleado(
            Empleado origen,
            Entrenador destino
    ) {

        destino.setIdPersona(
                origen.getIdPersona()
        );

        destino.setCedula(
                origen.getCedula()
        );

        destino.setNombres(
                origen.getNombres()
        );

        destino.setApellidos(
                origen.getApellidos()
        );

        destino.setFechaNacimiento(
                origen.getFechaNacimiento()
        );

        destino.setSexo(
                origen.getSexo()
        );

        destino.setTelefono(
                origen.getTelefono()
        );

        destino.setCorreo(
                origen.getCorreo()
        );

        destino.setFotoPerfil(
                origen.getFotoPerfil()
        );

        destino.setEstado(
                origen.isEstado()
        );

        destino.setCodigoEmpleado(
                origen.getCodigoEmpleado()
        );

        destino.setFechaIngreso(
                origen.getFechaIngreso()
        );

        destino.setTipoContrato(
                origen.getTipoContrato()
        );

        destino.setSalario(
                origen.getSalario()
        );

        destino.setHoraInicio(
                origen.getHoraInicio()
        );

        destino.setHoraFin(
                origen.getHoraFin()
        );

        destino.setEstadoEmpleado(
                origen.isEstadoEmpleado()
        );
    }
}
