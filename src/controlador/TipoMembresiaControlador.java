package controlador;

import dao.TipoMembresiaDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.TipoMembresia;
import utilidades.SesionUsuario;

/**
 * Controlador para tipo_membresia.
 *
 * @author Usuario
 */
public class TipoMembresiaControlador {

    private final TipoMembresiaDAO tipoMembresiaDAO;
    private String mensaje;

    public TipoMembresiaControlador() {

        tipoMembresiaDAO = new TipoMembresiaDAO();
        mensaje = "";
    }


    public boolean registrar(
            TipoMembresia tipoMembresia
    ) {

        mensaje = "";

        try {
            validarTipoMembresia(
                    tipoMembresia
            );

            boolean yaExiste = tipoMembresiaDAO.listar("").stream()
                    .anyMatch(plan -> plan.getNombre().equalsIgnoreCase(
                    tipoMembresia.getNombre()));
            if (yaExiste) {
                mensaje = "El plan ya existe. Seleccionelo para actualizar precio, duracion o beneficios.";
                return false;
            }

            boolean guardado = tipoMembresiaDAO.guardar(
                    tipoMembresia
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
            TipoMembresia tipoMembresia
    ) {

        mensaje = "";

        try {

            validarTipoMembresia(
                    tipoMembresia
            );

            TipoMembresia guardado = tipoMembresiaDAO.buscar(
                    tipoMembresia.getIdTipoMembresia()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            if (!guardado.getNombre().equalsIgnoreCase(
                    tipoMembresia.getNombre())) {
                mensaje = "El nombre del plan es fijo. Puede modificar precio, duracion, accesos y beneficios.";
                return false;
            }

            boolean modificado = tipoMembresiaDAO.modificar(
                    tipoMembresia
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
            Long idTipoMembresia
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idTipoMembresia
            );

            boolean desactivado = tipoMembresiaDAO.desactivar(
                    idTipoMembresia
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
            Long idTipoMembresia
    ) throws SQLException {

        validarClavePrimaria(
                idTipoMembresia
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = tipoMembresiaDAO.eliminarDefinitivamente(
                    idTipoMembresia
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
    public TipoMembresia buscar(
            Long idTipoMembresia
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idTipoMembresia
            );

            return tipoMembresiaDAO.buscar(
                    idTipoMembresia
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
    public List<TipoMembresia> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return tipoMembresiaDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarTipoMembresia(
            TipoMembresia tipoMembresia
    ) {

        if (tipoMembresia == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }

        String nombre = tipoMembresia.getNombre() == null
                ? "" : tipoMembresia.getNombre().trim();
        if (!java.util.Set.of("BASICA", "PREMIUM", "ELITE")
                .contains(nombre.toUpperCase(java.util.Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "GYMNOVA utiliza solamente los planes Basica, Premium y Elite."
            );
        }
        if (tipoMembresia.getDuracionDias() == null
                || tipoMembresia.getDuracionDias() <= 0) {
            throw new IllegalArgumentException(
                    "La duracion del plan debe ser mayor que cero dias."
            );
        }
        if (tipoMembresia.getPrecioBase() == null
                || tipoMembresia.getPrecioBase().signum() < 0) {
            throw new IllegalArgumentException(
                    "El precio base debe ser un valor valido mayor o igual a cero."
            );
        }
        if (!tipoMembresia.isAccesoIlimitado()
                && (tipoMembresia.getLimiteAccesos() == null
                || tipoMembresia.getLimiteAccesos() <= 0)) {
            throw new IllegalArgumentException(
                    "Indique el limite de accesos del plan cuando no es ilimitado."
            );
        }
    }

    private void validarClavePrimaria(
            Long idTipoMembresia
    ) {

        if (idTipoMembresia == null) {
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
