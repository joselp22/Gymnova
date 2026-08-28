package controlador;

import dao.TrabajaDAO;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Trabaja;
import utilidades.SesionUsuario;

/**
 * Controlador para la tabla trabaja.
 *
 * @author Usuario
 */
public class TrabajaControlador {

    private final TrabajaDAO trabajaDAO;
    private String mensaje;

    public TrabajaControlador() {

        trabajaDAO = new TrabajaDAO();
        mensaje = "";
    }

    public boolean registrar(
            Trabaja trabaja
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    trabaja
            );

            validarTrabaja(
                    trabaja
            );

            boolean existe
                    = trabajaDAO.existe(
                            trabaja.getIdEjercicio(),
                            trabaja.getIdGrupoMuscular()
                    );

            if (existe) {

                mensaje
                        = "El ejercicio ya esta relacionado "
                        + "con ese grupo muscular.";

                return false;
            }

            boolean guardado
                    = trabajaDAO.guardar(
                            trabaja
                    );

            if (guardado) {

                mensaje
                        = "Relacion registrada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo registrar "
                    + "la relacion.";

            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    public boolean modificar(
            Trabaja trabaja
    ) {

        mensaje = "";

        try {

            normalizarDatos(
                    trabaja
            );

            validarTrabaja(
                    trabaja
            );

            Trabaja guardado
                    = trabajaDAO.buscar(
                            trabaja.getIdEjercicio(),
                            trabaja.getIdGrupoMuscular()
                    );

            if (guardado == null) {

                mensaje
                        = "Seleccione una relacion "
                        + "registrada para modificar.";

                return false;
            }

            boolean modificado
                    = trabajaDAO.modificar(
                            trabaja
                    );

            if (modificado) {

                mensaje
                        = "Relacion modificada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo modificar "
                    + "la relacion.";

            return false;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = traducirErrorBaseDatos(
                            e
                    );

            return false;
        }
    }

    public void eliminarDefinitivamente(
            Long idEjercicio,
            Long idGrupoMuscular
    ) throws SQLException {

        validarIdentificador(
                idEjercicio,
                "Seleccione un ejercicio."
        );

        validarIdentificador(
                idGrupoMuscular,
                "Seleccione un grupo muscular."
        );

        if (!esAdministrador()) {

            throw new SecurityException(
                    "Solo el Administrador puede eliminar "
                    + "relaciones definitivamente."
            );
        }

        try {

            boolean eliminado
                    = trabajaDAO.eliminarDefinitivamente(
                            idEjercicio,
                            idGrupoMuscular
                    );

            if (!eliminado) {

                throw new IllegalArgumentException(
                        "La relacion ya no existe."
                );
            }

        } catch (SQLException e) {

            if (("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState()))) {

                throw new IllegalStateException(
                        "No se puede eliminar definitivamente "
                        + "porque existen registros relacionados.",
                        e
                );
            }

            throw e;
        }
    }

    public Trabaja buscar(
            Long idEjercicio,
            Long idGrupoMuscular
    ) {

        mensaje = "";

        if (idEjercicio == null
                || idGrupoMuscular == null) {

            return null;
        }

        try {

            return trabajaDAO.buscar(
                    idEjercicio,
                    idGrupoMuscular
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudo buscar "
                    + "la relacion.";

            return null;
        }
    }

    public List<Trabaja> listar(
            String criterio
    ) {

        mensaje = "";

        try {

            return trabajaDAO.listar(
                    criterio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "las relaciones.";

            return new ArrayList<>();
        }
    }

    public List<Trabaja> listarPorEjercicio(
            Long idEjercicio
    ) {

        mensaje = "";

        if (idEjercicio == null) {

            mensaje
                    = "Seleccione un ejercicio.";

            return new ArrayList<>();
        }

        try {

            return trabajaDAO.listarPorEjercicio(
                    idEjercicio
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los grupos musculares del ejercicio.";

            return new ArrayList<>();
        }
    }

    public List<Trabaja> listarPorGrupoMuscular(
            Long idGrupoMuscular
    ) {

        mensaje = "";

        if (idGrupoMuscular == null) {

            mensaje
                    = "Seleccione un grupo muscular.";

            return new ArrayList<>();
        }

        try {

            return trabajaDAO.listarPorGrupoMuscular(
                    idGrupoMuscular
            );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "los ejercicios del grupo muscular.";

            return new ArrayList<>();
        }
    }

    private void validarTrabaja(
            Trabaja trabaja
    ) {

        if (trabaja == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "de la relacion."
            );
        }

        validarIdentificador(
                trabaja.getIdEjercicio(),
                "Seleccione un ejercicio."
        );

        validarIdentificador(
                trabaja.getIdGrupoMuscular(),
                "Seleccione un grupo muscular."
        );

        validarTipoParticipacion(
                trabaja.getTipoParticipacion()
        );

        validarPorcentajeEstimulacion(
                trabaja.getPorcentajeEstimulacion()
        );

        validarObservaciones(
                trabaja.getObservaciones()
        );
    }

    private void validarIdentificador(
            Long identificador,
            String mensajeError
    ) {

        if (identificador == null
                || identificador <= 0) {

            throw new IllegalArgumentException(
                    mensajeError
            );
        }
    }

    private void validarTipoParticipacion(
            String tipoParticipacion
    ) {

        if (tipoParticipacion == null
                || tipoParticipacion.isBlank()) {

            throw new IllegalArgumentException(
                    "El tipo de participacion "
                    + "es obligatorio."
            );
        }

        if (tipoParticipacion.length() < 3
                || tipoParticipacion.length() > 150) {

            throw new IllegalArgumentException(
                    "El tipo de participacion debe tener "
                    + "entre 3 y 150 caracteres."
            );
        }
    }

    private void validarPorcentajeEstimulacion(
            BigDecimal porcentajeEstimulacion
    ) {

        if (porcentajeEstimulacion == null) {

            throw new IllegalArgumentException(
                    "El porcentaje de estimulacion "
                    + "es obligatorio."
            );
        }

        if (porcentajeEstimulacion.compareTo(
                BigDecimal.ZERO) < 0
                || porcentajeEstimulacion.compareTo(
                        new BigDecimal("100.00")) > 0) {

            throw new IllegalArgumentException(
                    "El porcentaje de estimulacion debe "
                    + "estar entre 0 y 100."
            );
        }

        if (porcentajeEstimulacion.scale() > 2) {

            throw new IllegalArgumentException(
                    "El porcentaje de estimulacion solamente "
                    + "puede tener dos decimales."
            );
        }
    }

    private void validarObservaciones(
            String observaciones
    ) {

        if (observaciones != null
                && observaciones.length() > 500) {

            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar "
                    + "500 caracteres."
            );
        }
    }

    private void normalizarDatos(
            Trabaja trabaja
    ) {

        if (trabaja == null) {
            return;
        }

        if (trabaja.getTipoParticipacion() != null) {

            trabaja.setTipoParticipacion(
                    trabaja.getTipoParticipacion()
                            .trim()
            );
        }

        if (trabaja.getObservaciones() != null) {

            String observaciones
                    = trabaja.getObservaciones()
                            .trim();

            if (observaciones.isEmpty()) {

                observaciones = null;
            }

            trabaja.setObservaciones(
                    observaciones
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

            return "No se puede realizar la operacion "
                    + "porque existen registros relacionados.";
        }

        if ("23505".equals(e.getSQLState())) {

            return "El ejercicio ya esta relacionado "
                    + "con ese grupo muscular.";
        }

        if ("23514".equals(e.getSQLState())) {

            return "Los datos no cumplen las reglas "
                    + "de validacion de la base de datos.";
        }

        return "Error de base de datos: "
                + e.getMessage();
    }

    public String getMensaje() {
        return mensaje;
    }
}
