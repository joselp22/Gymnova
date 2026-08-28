package controlador;

import dao.AlimentoDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Alimento;
import utilidades.SesionUsuario;

/**
 * Controlador para el catalogo de alimentos.
 *
 * El catalogo es administrado por el rol Administrador. El nutricionista
 * utiliza los alimentos existentes al construir los planes nutricionales,
 * pero no modifica este catalogo desde el modulo de Nutricion.
 */
public class AlimentoControlador {

    private final AlimentoDAO alimentoDAO;
    private String mensaje;

    public AlimentoControlador() {
        alimentoDAO = new AlimentoDAO();
        mensaje = "";
    }

    public boolean registrar(Alimento alimento) {
        mensaje = "";

        try {
            validarAdministradorCatalogo();
            validarAlimento(alimento);

            boolean guardado = alimentoDAO.guardar(alimento);

            if (guardado) {
                mensaje = "Alimento guardado correctamente.";
                return true;
            }

            mensaje = "No se pudo guardar el alimento.";
            return false;

        } catch (IllegalArgumentException e) {
            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return false;
        }
    }

    public boolean modificar(Alimento alimento) {
        mensaje = "";

        try {
            validarAdministradorCatalogo();
            validarAlimento(alimento);
            validarClavePrimaria(alimento.getIdAlimento());

            Alimento guardado = alimentoDAO.buscar(alimento.getIdAlimento());

            if (guardado == null) {
                mensaje = "Seleccione un alimento existente.";
                return false;
            }

            boolean modificado = alimentoDAO.modificar(alimento);

            if (modificado) {
                mensaje = "Alimento modificado correctamente.";
                return true;
            }

            mensaje = "No se pudo modificar el alimento.";
            return false;

        } catch (IllegalArgumentException e) {
            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return false;
        }
    }

    public void eliminarDefinitivamente(Long idAlimento) throws SQLException {
        validarClavePrimaria(idAlimento);

        if (!esAdministrador()) {
            throw new SecurityException(
                    "Solo el Administrador puede eliminar definitivamente un alimento."
            );
        }

        try {
            boolean eliminado = alimentoDAO.eliminarDefinitivamente(idAlimento);

            if (!eliminado) {
                throw new IllegalArgumentException(
                        "El alimento ya no existe."
                );
            }

        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())
                    || "23001".equals(e.getSQLState())) {
                throw new IllegalStateException(
                        "No se puede eliminar el alimento porque esta siendo utilizado en uno o mas planes nutricionales.",
                        e
                );
            }

            throw e;
        }
    }

    public Alimento buscar(Long idAlimento) {
        mensaje = "";

        try {
            validarClavePrimaria(idAlimento);
            return alimentoDAO.buscar(idAlimento);

        } catch (IllegalArgumentException e) {
            mensaje = e.getMessage();
            return null;

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return null;
        }
    }

    public List<Alimento> listar(String criterio) {
        mensaje = "";

        try {
            return alimentoDAO.listar(criterio);

        } catch (SQLException e) {
            mensaje = traducirErrorBaseDatos(e);
            return new ArrayList<>();
        }
    }

    private void validarAlimento(Alimento alimento) {
        if (alimento == null) {
            throw new IllegalArgumentException(
                    "No se recibieron los datos del alimento."
            );
        }

        if (alimento.getNombreAlimento() == null
                || alimento.getNombreAlimento().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del alimento es obligatorio."
            );
        }

        alimento.setNombreAlimento(alimento.getNombreAlimento().trim());
        alimento.setCategoria(limpiarTexto(alimento.getCategoria()));
        alimento.setDescripcion(limpiarTexto(alimento.getDescripcion()));

        validarMayorQueCero(
                alimento.getPorcionReferenciaG(),
                "La porcion de referencia"
        );

        validarNoNegativo(
                alimento.getProteinasG(),
                "Las proteinas"
        );

        validarNoNegativo(
                alimento.getCarbohidratosG(),
                "Los carbohidratos"
        );

        validarNoNegativo(
                alimento.getFibraG(),
                "La fibra"
        );
    }

    private void validarMayorQueCero(BigDecimal valor, String campo) {
        if (valor != null && valor.signum() <= 0) {
            throw new IllegalArgumentException(
                    campo + " debe ser mayor que cero."
            );
        }
    }

    private void validarNoNegativo(BigDecimal valor, String campo) {
        if (valor != null && valor.signum() < 0) {
            throw new IllegalArgumentException(
                    campo + " no puede ser negativo."
            );
        }
    }

    private String limpiarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        String valor = texto.trim();
        return valor.isBlank() ? null : valor;
    }

    private void validarClavePrimaria(Long idAlimento) {
        if (idAlimento == null || idAlimento <= 0) {
            throw new IllegalArgumentException(
                    "Seleccione un alimento."
            );
        }
    }

    private void validarAdministradorCatalogo() {
        if (!esAdministrador()) {
            throw new IllegalArgumentException(
                    "Solo el Administrador puede modificar el catalogo de alimentos."
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
            return "No se puede realizar la operacion porque el alimento tiene registros relacionados.";
        }

        if ("23505".equals(e.getSQLState())) {
            return "Ya existe un alimento con esos datos unicos.";
        }

        if ("23514".equals(e.getSQLState())) {
            return "Los datos del alimento no cumplen las reglas de validacion de la base de datos.";
        }

        if ("23502".equals(e.getSQLState())) {
            return "Falta completar un dato obligatorio del alimento.";
        }

        return "Error de base de datos: " + e.getMessage();
    }

    public String getMensaje() {
        return mensaje;
    }
}
