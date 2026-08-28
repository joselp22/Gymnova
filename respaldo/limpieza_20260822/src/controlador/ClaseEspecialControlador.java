package controlador;

import dao.ClaseEspecialDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.ClaseEspecial;
import utilidades.SesionUsuario;

/**
 * Controlador para clase_especial.
 *
 * @author Usuario
 */
public class ClaseEspecialControlador {

    private final ClaseEspecialDAO claseEspecialDAO;
    private String mensaje;

    public ClaseEspecialControlador() {

        claseEspecialDAO = new ClaseEspecialDAO();
        mensaje = "";
    }


    public boolean registrar(
            ClaseEspecial claseEspecial
    ) {

        mensaje = "";

        try {
            validarClaseEspecial(
                    claseEspecial
            );

            boolean guardado = claseEspecialDAO.guardar(
                    claseEspecial
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
            ClaseEspecial claseEspecial
    ) {

        mensaje = "";

        try {

            validarClaseEspecial(
                    claseEspecial
            );

            ClaseEspecial guardado = claseEspecialDAO.buscar(
                    claseEspecial.getIdClaseEspecial()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = claseEspecialDAO.modificar(
                    claseEspecial
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
            Long idClaseEspecial
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idClaseEspecial
            );

            boolean desactivado = claseEspecialDAO.desactivar(
                    idClaseEspecial
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
            Long idClaseEspecial
    ) throws SQLException {

        validarClavePrimaria(
                idClaseEspecial
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = claseEspecialDAO.eliminarDefinitivamente(
                    idClaseEspecial
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
    public ClaseEspecial buscar(
            Long idClaseEspecial
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idClaseEspecial
            );

            return claseEspecialDAO.buscar(
                    idClaseEspecial
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
    public List<ClaseEspecial> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return claseEspecialDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ClaseEspecial> listarPorIdClase(
            Long idClase
    ) {

        mensaje = "";

        if (idClase == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return claseEspecialDAO.listarPorIdClase(
                    idClase
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ClaseEspecial> listarPorIdHorario(
            Long idHorario
    ) {

        mensaje = "";

        if (idHorario == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return claseEspecialDAO.listarPorIdHorario(
                    idHorario
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<ClaseEspecial> listarPorIdEntrenador(
            Long idEntrenador
    ) {

        mensaje = "";

        if (idEntrenador == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return claseEspecialDAO.listarPorIdEntrenador(
                    idEntrenador
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarClaseEspecial(
            ClaseEspecial claseEspecial
    ) {

        if (claseEspecial == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idClaseEspecial
    ) {

        if (idClaseEspecial == null) {
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
