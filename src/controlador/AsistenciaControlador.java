package controlador;

import dao.AsistenciaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Asistencia;
import utilidades.SesionUsuario;

/**
 * Controlador para asistencia.
 *
 * @author Usuario
 */
public class AsistenciaControlador {

    private final AsistenciaDAO asistenciaDAO;
    private String mensaje;

    public AsistenciaControlador() {

        asistenciaDAO = new AsistenciaDAO();
        mensaje = "";
    }


    public boolean registrar(
            Asistencia asistencia
    ) {

        mensaje = "";

        try {
            validarAsistencia(
                    asistencia
            );

            boolean guardado = asistenciaDAO.guardar(
                    asistencia
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
            Asistencia asistencia
    ) {

        mensaje = "";

        try {

            validarAsistencia(
                    asistencia
            );

            Asistencia guardado = asistenciaDAO.buscar(
                    asistencia.getIdAsistencia()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = asistenciaDAO.modificar(
                    asistencia
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
            Long idAsistencia
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idAsistencia
            );

            boolean desactivado = asistenciaDAO.desactivar(
                    idAsistencia
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
            Long idAsistencia
    ) throws SQLException {

        validarClavePrimaria(
                idAsistencia
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = asistenciaDAO.eliminarDefinitivamente(
                    idAsistencia
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
    public Asistencia buscar(
            Long idAsistencia
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idAsistencia
            );

            return asistenciaDAO.buscar(
                    idAsistencia
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
    public List<Asistencia> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return asistenciaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Asistencia> listarPorIdCliente(
            Long idCliente
    ) {

        mensaje = "";

        if (idCliente == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return asistenciaDAO.listarPorIdCliente(
                    idCliente
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarAsistencia(
            Asistencia asistencia
    ) {

        if (asistencia == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idAsistencia
    ) {

        if (idAsistencia == null) {
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
