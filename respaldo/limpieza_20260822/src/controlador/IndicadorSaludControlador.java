package controlador;

import dao.IndicadorSaludDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.IndicadorSalud;
import utilidades.SesionUsuario;

/**
 * Controlador para el catalogo de indicadores de salud.
 *
 * El Administrador configura el indicador, su unidad, categoria y rango de
 * referencia. Los profesionales utilizan este catalogo para registrar los
 * resultados de los clientes.
 */
public class IndicadorSaludControlador {

    private final IndicadorSaludDAO indicadorSaludDAO;
    private String mensaje;

    public IndicadorSaludControlador() {
        indicadorSaludDAO = new IndicadorSaludDAO();
        mensaje = "";
    }

    public boolean registrar(IndicadorSalud indicadorSalud) {
        mensaje = "";

        try {
            validarIndicadorSalud(indicadorSalud);

            boolean guardado = indicadorSaludDAO.guardar(indicadorSalud);

            if (guardado) {
                mensaje = "Indicador de salud guardado correctamente.";
                return true;
            }

            mensaje = "No se pudo guardar el indicador de salud.";
            return false;

        } catch (IllegalArgumentException e) {
            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return false;
        }
    }

    public boolean modificar(IndicadorSalud indicadorSalud) {
        mensaje = "";

        try {
            validarIndicadorSalud(indicadorSalud);
            validarClavePrimaria(indicadorSalud.getIdIndicador());

            IndicadorSalud guardado = indicadorSaludDAO.buscar(
                    indicadorSalud.getIdIndicador()
            );

            if (guardado == null) {
                mensaje = "Seleccione un indicador existente.";
                return false;
            }

            boolean modificado = indicadorSaludDAO.modificar(indicadorSalud);

            if (modificado) {
                mensaje = "Indicador de salud modificado correctamente.";
                return true;
            }

            mensaje = "No se pudo modificar el indicador de salud.";
            return false;

        } catch (IllegalArgumentException e) {
            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return false;
        }
    }

    public boolean desactivar(Long idIndicador) {
        mensaje = "";

        try {
            validarClavePrimaria(idIndicador);

            if (!esAdministrador()) {
                mensaje = "Solo el Administrador puede desactivar indicadores del catalogo.";
                return false;
            }

            boolean desactivado = indicadorSaludDAO.desactivar(idIndicador);

            if (desactivado) {
                mensaje = "Indicador de salud desactivado correctamente.";
                return true;
            }

            mensaje = "No se pudo desactivar el indicador de salud.";
            return false;

        } catch (IllegalArgumentException e) {
            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return false;
        }
    }

    public void eliminarDefinitivamente(Long idIndicador) throws SQLException {
        validarClavePrimaria(idIndicador);

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente un indicador."
            );
        }

        try {
            boolean eliminado = indicadorSaludDAO.eliminarDefinitivamente(idIndicador);

            if (!eliminado) {
                throw new IllegalArgumentException(
                        "El indicador ya no existe."
                );
            }

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState())) {
                throw new IllegalStateException(
                        "No se puede eliminar el indicador porque existen resultados relacionados. Puede desactivarlo en su lugar.",
                        e
                );
            }

            throw e;
        }
    }

    public IndicadorSalud buscar(Long idIndicador) {
        mensaje = "";

        try {
            validarClavePrimaria(idIndicador);
            return indicadorSaludDAO.buscar(idIndicador);

        } catch (IllegalArgumentException e) {
            mensaje = e.getMessage();
            return null;

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return null;
        }
    }

    public List<IndicadorSalud> listar(String criterio) {
        mensaje = "";

        try {
            return indicadorSaludDAO.listar(criterio);

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    private void validarIndicadorSalud(IndicadorSalud indicadorSalud) {
        if (indicadorSalud == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del indicador de salud."
            );
        }

        if (indicadorSalud.getNombreIndicador() == null
                || indicadorSalud.getNombreIndicador().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del indicador es obligatorio."
            );
        }

        if (indicadorSalud.getUnidadMedida() == null
                || indicadorSalud.getUnidadMedida().isBlank()) {
            throw new IllegalArgumentException(
                    "La unidad de medida es obligatoria."
            );
        }

        if (indicadorSalud.getValorMinimoReferencia() == null) {
            throw new IllegalArgumentException(
                    "El valor minimo de referencia es obligatorio."
            );
        }

        if (indicadorSalud.getValorMaximoReferencia() == null) {
            throw new IllegalArgumentException(
                    "El valor maximo de referencia es obligatorio."
            );
        }

        if (indicadorSalud.getValorMinimoReferencia().compareTo(
                indicadorSalud.getValorMaximoReferencia()
        ) > 0) {
            throw new IllegalArgumentException(
                    "El valor minimo no puede ser mayor que el valor maximo."
            );
        }

        indicadorSalud.setNombreIndicador(
                indicadorSalud.getNombreIndicador().trim()
        );
        indicadorSalud.setUnidadMedida(
                indicadorSalud.getUnidadMedida().trim()
        );
        indicadorSalud.setCategoria(
                limpiarTexto(indicadorSalud.getCategoria())
        );
        indicadorSalud.setDescripcion(
                limpiarTexto(indicadorSalud.getDescripcion())
        );
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        String valor = texto.trim();
        return valor.isBlank() ? null : valor;
    }

    private void validarClavePrimaria(Long idIndicador) {
        if (idIndicador == null || idIndicador <= 0) {
            throw new IllegalArgumentException(
                    "Seleccione un indicador de salud."
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

    private String traducirErrorBaseDatos(SQLException e) {
        if ("23503".equals(e.getSQLState())
                || "23001".equals(e.getSQLState())) {
            return "No se puede realizar la operacion porque el indicador tiene registros relacionados.";
        }

        if ("23505".equals(e.getSQLState())) {
            return "Ya existe un indicador con esos datos unicos.";
        }

        if ("23514".equals(e.getSQLState())) {
            return "Los datos del indicador no cumplen las reglas de validacion de la base de datos.";
        }

        if ("23502".equals(e.getSQLState())) {
            return "Falta completar un dato obligatorio del indicador de salud.";
        }

        return "Error de base de datos: " + e.getMessage();
    }

    public String getMensaje() {
        return mensaje;
    }
}
