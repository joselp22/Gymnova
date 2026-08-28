package controlador;

import dao.IndicadorSaludDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.IndicadorSalud;
import utilidades.SesionUsuario;

/**
 * Controlador para indicador_salud.
 *
 * @author Usuario
 */
public class IndicadorSaludControlador {

    private final IndicadorSaludDAO indicadorSaludDAO;
    private String mensaje;

    public IndicadorSaludControlador() {

        indicadorSaludDAO = new IndicadorSaludDAO();
        mensaje = "";
    }


    public boolean registrar(
            IndicadorSalud indicadorSalud
    ) {

        mensaje = "";

        try {
            validarIndicadorSalud(
                    indicadorSalud
            );

            boolean guardado = indicadorSaludDAO.guardar(
                    indicadorSalud
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
            IndicadorSalud indicadorSalud
    ) {

        mensaje = "";

        try {

            validarIndicadorSalud(
                    indicadorSalud
            );

            IndicadorSalud guardado = indicadorSaludDAO.buscar(
                    indicadorSalud.getIdIndicador()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = indicadorSaludDAO.modificar(
                    indicadorSalud
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
            Long idIndicador
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idIndicador
            );

            boolean desactivado = indicadorSaludDAO.desactivar(
                    idIndicador
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
            Long idIndicador
    ) throws SQLException {

        validarClavePrimaria(
                idIndicador
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = indicadorSaludDAO.eliminarDefinitivamente(
                    idIndicador
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
    public IndicadorSalud buscar(
            Long idIndicador
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idIndicador
            );

            return indicadorSaludDAO.buscar(
                    idIndicador
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
    public List<IndicadorSalud> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return indicadorSaludDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarIndicadorSalud(
            IndicadorSalud indicadorSalud
    ) {

        if (indicadorSalud == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }
    }

    private void validarClavePrimaria(
            Long idIndicador
    ) {

        if (idIndicador == null) {
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
