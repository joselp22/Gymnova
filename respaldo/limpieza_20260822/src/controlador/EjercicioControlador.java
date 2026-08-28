package controlador;

import dao.EjercicioDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Ejercicio;
import utilidades.SesionUsuario;

/**
 * Controlador para ejercicio.
 *
 * @author Usuario
 */
public class EjercicioControlador {

    private final EjercicioDAO ejercicioDAO;
    private String mensaje;

    public EjercicioControlador() {

        ejercicioDAO = new EjercicioDAO();
        mensaje = "";
    }


    public boolean registrar(
            Ejercicio ejercicio
    ) {

        mensaje = "";

        try {
            validarEjercicio(
                    ejercicio
            );

            boolean guardado = ejercicioDAO.guardar(
                    ejercicio
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
            Ejercicio ejercicio
    ) {

        mensaje = "";

        try {

            validarEjercicio(
                    ejercicio
            );

            Ejercicio guardado = ejercicioDAO.buscar(
                    ejercicio.getIdEjercicio()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = ejercicioDAO.modificar(
                    ejercicio
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
    public void eliminarDefinitivamente(
            Long idEjercicio
    ) throws SQLException {

        validarClavePrimaria(
                idEjercicio
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = ejercicioDAO.eliminarDefinitivamente(
                    idEjercicio
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
    public Ejercicio buscar(
            Long idEjercicio
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idEjercicio
            );

            return ejercicioDAO.buscar(
                    idEjercicio
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
    public List<Ejercicio> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return ejercicioDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarEjercicio(
            Ejercicio ejercicio
    ) {

        if (ejercicio == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idEjercicio
    ) {

        if (idEjercicio == null) {
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
