package controlador;

import dao.EquipoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Equipo;
import utilidades.SesionUsuario;

/**
 * Controlador para equipo.
 *
 * @author Usuario
 */
public class EquipoControlador {

    private final EquipoDAO equipoDAO;
    private String mensaje;

    public EquipoControlador() {

        equipoDAO = new EquipoDAO();
        mensaje = "";
    }


    public boolean registrar(
            Equipo equipo
    ) {

        mensaje = "";

        try {
            if (equipo != null) {
                equipo.setCodigoInterno(
                        null
                );
            }

            validarEquipo(
                    equipo
            );

            boolean guardado = equipoDAO.guardar(
                    equipo
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
            Equipo equipo
    ) {

        mensaje = "";

        try {

            validarEquipo(
                    equipo
            );

            Equipo guardado = equipoDAO.buscar(
                    equipo.getIdEquipo()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            equipo.setCodigoInterno(guardado.getCodigoInterno());
            boolean modificado = equipoDAO.modificar(
                    equipo
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
            Long idEquipo
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idEquipo
            );

            boolean desactivado = equipoDAO.desactivar(
                    idEquipo
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
            Long idEquipo
    ) throws SQLException {

        validarClavePrimaria(
                idEquipo
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = equipoDAO.eliminarDefinitivamente(
                    idEquipo
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
    public Equipo buscar(
            Long idEquipo
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idEquipo
            );

            return equipoDAO.buscar(
                    idEquipo
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
    public List<Equipo> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return equipoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Equipo> listarPorIdTipoEquipo(
            Long idTipoEquipo
    ) {

        mensaje = "";

        if (idTipoEquipo == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return equipoDAO.listarPorIdTipoEquipo(
                    idTipoEquipo
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarEquipo(
            Equipo equipo
    ) {

        if (equipo == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idEquipo
    ) {

        if (idEquipo == null) {
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
