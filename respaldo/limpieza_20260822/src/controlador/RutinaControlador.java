package controlador;

import dao.RutinaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Rutina;
import utilidades.SesionUsuario;

/**
 * Controlador para rutina.
 *
 * @author Usuario
 */
public class RutinaControlador {

    private final RutinaDAO rutinaDAO;
    private String mensaje;

    public RutinaControlador() {

        rutinaDAO = new RutinaDAO();
        mensaje = "";
    }


    public boolean registrar(
            Rutina rutina
    ) {

        mensaje = "";

        try {
            validarRutina(
                    rutina
            );

            boolean guardado = rutinaDAO.guardar(
                    rutina
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
            Rutina rutina
    ) {

        mensaje = "";

        try {

            validarRutina(
                    rutina
            );

            Rutina guardado = rutinaDAO.buscar(
                    rutina.getIdRutina()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = rutinaDAO.modificar(
                    rutina
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
            Long idRutina
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idRutina
            );

            boolean desactivado = rutinaDAO.desactivar(
                    idRutina
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
            Long idRutina
    ) throws SQLException {

        validarClavePrimaria(
                idRutina
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = rutinaDAO.eliminarDefinitivamente(
                    idRutina
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
    public Rutina buscar(
            Long idRutina
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idRutina
            );

            return rutinaDAO.buscar(
                    idRutina
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
    public List<Rutina> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return rutinaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Rutina> listarPorIdEntrenador(
            Long idEntrenador
    ) {

        mensaje = "";

        if (idEntrenador == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return rutinaDAO.listarPorIdEntrenador(
                    idEntrenador
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<Rutina> listarPorIdObjetivo(
            Long idObjetivo
    ) {

        mensaje = "";

        if (idObjetivo == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return rutinaDAO.listarPorIdObjetivo(
                    idObjetivo
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarRutina(
            Rutina rutina
    ) {

        if (rutina == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }

        if (texto(rutina.getNombreRutina()).isEmpty()) {
            throw new IllegalArgumentException("Ingrese el nombre de la rutina.");
        }
        if (!java.util.Set.of("PRINCIPIANTE", "INTERMEDIO", "AVANZADO")
                .contains(texto(rutina.getNivel()))) {
            throw new IllegalArgumentException("Seleccione un nivel valido.");
        }
        if (rutina.getDuracionSemanas() == null || rutina.getDuracionSemanas() <= 0) {
            throw new IllegalArgumentException("La duracion debe ser mayor que cero semanas.");
        }
        if (rutina.getFechaCreacion() == null
                || rutina.getFechaCreacion().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de creacion no puede estar vacia ni ser futura.");
        }
        if (!java.util.Set.of("ACTIVA", "INACTIVA", "ARCHIVADA")
                .contains(texto(rutina.getEstadoRutina()))) {
            throw new IllegalArgumentException("Seleccione un estado de rutina valido.");
        }
        if (rutina.getIdEntrenador() == null || rutina.getIdObjetivo() == null) {
            throw new IllegalArgumentException("Seleccione el entrenador y el objetivo.");
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void validarClavePrimaria(
            Long idRutina
    ) {

        if (idRutina == null) {
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
