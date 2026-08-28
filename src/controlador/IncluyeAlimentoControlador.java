package controlador;

import dao.IncluyeAlimentoDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.IncluyeAlimento;
import utilidades.SesionUsuario;

/**
 * Controlador para incluye_alimento.
 *
 * @author Usuario
 */
public class IncluyeAlimentoControlador {

    private final IncluyeAlimentoDAO incluyeAlimentoDAO;
    private String mensaje;

    public IncluyeAlimentoControlador() {

        incluyeAlimentoDAO = new IncluyeAlimentoDAO();
        mensaje = "";
    }


    public boolean registrar(
            IncluyeAlimento incluyeAlimento
    ) {

        mensaje = "";

        try {
            validarIncluyeAlimento(
                    incluyeAlimento
            );

            boolean guardado = incluyeAlimentoDAO.guardar(
                    incluyeAlimento
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
            IncluyeAlimento incluyeAlimento
    ) {

        mensaje = "";

        try {

            validarIncluyeAlimento(
                    incluyeAlimento
            );

            IncluyeAlimento guardado = incluyeAlimentoDAO.buscar(
                    incluyeAlimento.getIdDetallePlan()
            );

            if (guardado == null) {
                mensaje = "Seleccione un registro existente.";
                return false;
            }

            boolean modificado = incluyeAlimentoDAO.modificar(
                    incluyeAlimento
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
            Long idDetallePlan
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idDetallePlan
            );

            boolean desactivado = incluyeAlimentoDAO.desactivar(
                    idDetallePlan
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
            Long idDetallePlan
    ) throws SQLException {

        validarClavePrimaria(
                idDetallePlan
        );

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente."
            );
        }

        try {

            boolean eliminado = incluyeAlimentoDAO.eliminarDefinitivamente(
                    idDetallePlan
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
    public IncluyeAlimento buscar(
            Long idDetallePlan
    ) {

        mensaje = "";

        try {

            validarClavePrimaria(
                    idDetallePlan
            );

            return incluyeAlimentoDAO.buscar(
                    idDetallePlan
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
    public List<IncluyeAlimento> listar(
            String criterio
    ) {

        mensaje = "";

        try {
            return incluyeAlimentoDAO.listar(
                    criterio
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<IncluyeAlimento> listarPorIdPlanNutricional(
            Long idPlanNutricional
    ) {

        mensaje = "";

        if (idPlanNutricional == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return incluyeAlimentoDAO.listarPorIdPlanNutricional(
                    idPlanNutricional
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }
    public List<IncluyeAlimento> listarPorIdAlimento(
            Long idAlimento
    ) {

        mensaje = "";

        if (idAlimento == null) {
            mensaje = "Seleccione un registro relacionado.";
            return new ArrayList<>();
        }

        try {
            return incluyeAlimentoDAO.listarPorIdAlimento(
                    idAlimento
            );
        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(
                    e
            );
            return new ArrayList<>();
        }
    }

    private void validarIncluyeAlimento(
            IncluyeAlimento incluyeAlimento
    ) {

        if (incluyeAlimento == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del registro."
            );
        }

        if (!java.util.Set.of("LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO")
                .contains(texto(incluyeAlimento.getDiaSemana()))) {
            throw new IllegalArgumentException("Seleccione un dia de la semana valido.");
        }
        if (!java.util.Set.of("DESAYUNO", "MEDIA_MANANA", "ALMUERZO", "MERIENDA", "CENA", "SNACK")
                .contains(texto(incluyeAlimento.getTipoComida()))) {
            throw new IllegalArgumentException("Seleccione un tipo de comida valido.");
        }
        if (incluyeAlimento.getCantidad() == null
                || incluyeAlimento.getCantidad().signum() <= 0
                || incluyeAlimento.getOrdenComida() == null
                || incluyeAlimento.getOrdenComida() <= 0) {
            throw new IllegalArgumentException("La cantidad y el orden de comida deben ser mayores que cero.");
        }
        if (texto(incluyeAlimento.getUnidadMedida()).isEmpty()) {
            throw new IllegalArgumentException("Ingrese la unidad de medida.");
        }
        if (!java.util.Set.of("ACTIVO", "SUSPENDIDO", "REEMPLAZADO", "CANCELADO")
                .contains(texto(incluyeAlimento.getEstadoDetalle()))) {
            throw new IllegalArgumentException("Seleccione un estado de alimento valido.");
        }
        if (incluyeAlimento.getIdPlanNutricional() == null || incluyeAlimento.getIdAlimento() == null) {
            throw new IllegalArgumentException("Seleccione el plan nutricional y el alimento.");
        }
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void validarClavePrimaria(
            Long idDetallePlan
    ) {

        if (idDetallePlan == null) {
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
