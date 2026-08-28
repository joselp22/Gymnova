package controlador;

import dao.GrupoMuscularDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.GrupoMuscular;
import utilidades.SesionUsuario;

/**
 * Controlador para grupo_muscular.
 *
 * @author Usuario
 */
public class GrupoMuscularControlador {

    private final GrupoMuscularDAO grupoMuscularDAO;
    private String mensaje;

    public GrupoMuscularControlador() {

        grupoMuscularDAO = new GrupoMuscularDAO();
        mensaje = "";
    }


    public boolean registrar(
            GrupoMuscular grupoMuscular
    ) {

        mensaje = "";

        try {
            validarGrupoMuscular(
                    grupoMuscular
            );

            boolean guardado = grupoMuscularDAO.guardar(
                    grupoMuscular
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
            GrupoMuscular grupoMuscular
    ) {

        mensaje = "";

        try {

            validarGrupoMuscular(
                    grupoMuscular
            );

            GrupoMuscular guardado = grupoMuscularDAO.buscar(
                    grupoMuscular.getIdGrupoMuscular()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = grupoMuscularDAO.modificar(
                    grupoMuscular
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
            Long idGrupoMuscular
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idGrupoMuscular
            );

            boolean desactivado = grupoMuscularDAO.desactivar(
                    idGrupoMuscular
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
            Long idGrupoMuscular
    ) throws SQLException {

        validarClavePrimaria(
                idGrupoMuscular
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = grupoMuscularDAO.eliminarDefinitivamente(
                    idGrupoMuscular
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
    public GrupoMuscular buscar(
            Long idGrupoMuscular
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idGrupoMuscular
            );

            return grupoMuscularDAO.buscar(
                    idGrupoMuscular
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
    public List<GrupoMuscular> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return grupoMuscularDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarGrupoMuscular(
            GrupoMuscular grupoMuscular
    ) {

        if (grupoMuscular == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idGrupoMuscular
    ) {

        if (idGrupoMuscular == null) {
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
