package controlador;

import dao.MantenimientoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Mantenimiento;
import utilidades.SesionUsuario;

/**
 * Controlador para mantenimiento.
 *
 * @author Usuario
 */
public class MantenimientoControlador {

    private final MantenimientoDAO mantenimientoDAO;
    private String mensaje;

    public MantenimientoControlador() {

        mantenimientoDAO = new MantenimientoDAO();
        mensaje = "";
    }


    public boolean registrar(
            Mantenimiento mantenimiento
    ) {

        mensaje = "";

        try {
            if (mantenimiento != null) {
                mantenimiento.setNumeroMantenimiento(null);
            }
            validarMantenimiento(
                    mantenimiento
            );

            boolean guardado = mantenimientoDAO.guardar(
                    mantenimiento
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
            Mantenimiento mantenimiento
    ) {

        mensaje = "";

        try {

            validarMantenimiento(
                    mantenimiento
            );

            Mantenimiento guardado = mantenimientoDAO.buscar(
                    mantenimiento.getIdMantenimiento()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            if (!java.util.Objects.equals(
                    guardado.getNumeroMantenimiento(),
                    mantenimiento.getNumeroMantenimiento())) {
                mensaje = "El numero de mantenimiento no se puede modificar.";
                return false;
            }
            mantenimiento.setNumeroMantenimiento(
                    guardado.getNumeroMantenimiento());

            boolean modificado = mantenimientoDAO.modificar(
                    mantenimiento
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
            Long idMantenimiento
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idMantenimiento
            );

            boolean desactivado = mantenimientoDAO.desactivar(
                    idMantenimiento
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
            Long idMantenimiento
    ) throws SQLException {

        validarClavePrimaria(
                idMantenimiento
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = mantenimientoDAO.eliminarDefinitivamente(
                    idMantenimiento
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
    public Mantenimiento buscar(
            Long idMantenimiento
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idMantenimiento
            );

            return mantenimientoDAO.buscar(
                    idMantenimiento
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
    public List<Mantenimiento> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return mantenimientoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Mantenimiento> listarPorIdEquipo(
            Long idEquipo
    ) {

        mensaje = "";

        if (idEquipo == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return mantenimientoDAO.listarPorIdEquipo(
                    idEquipo
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Mantenimiento> listarPorIdTipoMantenimiento(
            Long idTipoMantenimiento
    ) {

        mensaje = "";

        if (idTipoMantenimiento == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return mantenimientoDAO.listarPorIdTipoMantenimiento(
                    idTipoMantenimiento
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Mantenimiento> listarPorIdEmpleado(
            Long idEmpleado
    ) {

        mensaje = "";

        if (idEmpleado == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return mantenimientoDAO.listarPorIdEmpleado(
                    idEmpleado
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarMantenimiento(
            Mantenimiento mantenimiento
    ) {

        if (mantenimiento == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idMantenimiento
    ) {

        if (idMantenimiento == null) {
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
