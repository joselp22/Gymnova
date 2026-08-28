package controlador;

import dao.HorarioDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Horario;
import utilidades.SesionUsuario;

/**
 * Controlador para horario.
 *
 * @author Usuario
 */
public class HorarioControlador {

    private final HorarioDAO horarioDAO;
    private String mensaje;

    public HorarioControlador() {

        horarioDAO = new HorarioDAO();
        mensaje = "";
    }


    public boolean registrar(
            Horario horario
    ) {

        mensaje = "";

        try {
            validarHorario(
                    horario
            );

            boolean guardado = horarioDAO.guardar(
                    horario
            );

            if (guardado) {
                mensaje = "Registro guardado correctamente.";
                return true;
            }

            mensaje = "No se pudo guardar el registro.";
            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje = traducirErrorBaseDatos(
                    e
            );
            return false;
        }
    }
    public boolean modificar(
            Horario horario
    ) {

        mensaje = "";

        try {

            validarHorario(
                    horario
            );

            Horario guardado = horarioDAO.buscar(
                    horario.getIdHorario()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = horarioDAO.modificar(
                    horario
            );

            if (modificado) {
                mensaje = "Registro modificado correctamente.";
                return true;
            }

            mensaje = "No se pudo modificar el registro.";
            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje = traducirErrorBaseDatos(
                    e
            );
            return false;
        }
    }
    public boolean desactivar(
            Long idHorario
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idHorario
            );

            boolean desactivado = horarioDAO.desactivar(
                    idHorario
            );

            if (desactivado) {
                mensaje = "Registro desactivado correctamente.";
                return true;
            }

            mensaje = "No se pudo desactivar el registro.";
            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje = traducirErrorBaseDatos(
                    e
            );
            return false;
        }
    }
    public void eliminarDefinitivamente(
            Long idHorario
    ) throws SQLException {

        validarClavePrimaria(
                idHorario
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = horarioDAO.eliminarDefinitivamente(
                    idHorario
            );

            if (!eliminado) {
                throw new IllegalArgumentException(
                        "El registro ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {
                throw new IllegalStateException(
                        "No se puede eliminar porque existen registros relacionados. Puede desactivar el registro.",
                        e
                );
            }

            throw e;
        }
    }
    public Horario buscar(
            Long idHorario
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idHorario
            );

            return horarioDAO.buscar(
                    idHorario
            );

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return null;

        } catch (SQLException e) {

            mensaje = traducirErrorBaseDatos(
                    e
            );
            return null;
        }
    }
    public List<Horario> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return horarioDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarHorario(
            Horario horario
    ) {

        if (horario == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idHorario
    ) {

        if (idHorario == null) {
            throw new IllegalArgumentException(
                    "Seleccione un registro."
            );
        }
    }

    private boolean esAdministrador() {

        return SesionUsuario.haySesionActiva()
                && "Administrador".equalsIgnoreCase(
                        SesionUsuario
                                .getUsuarioActual()
                                .getNombreRol()
                );
    }

    private String traducirErrorBaseDatos(
            SQLException e
    ) {

        if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {
            return "No se puede realizar la operacion porque existen registros relacionados.";
        }

        if ("23505".equals(e.getSQLState())) {
            return "Ya existe un registro con esos datos unicos.";
        }

        if ("23514".equals(e.getSQLState())) {
            return "Los datos no cumplen las reglas de validacion de la base de datos.";
        }

        return "Error de base de datos: " + e.getMessage();
    }

    public String getMensaje() {
        return mensaje;
    }
}
