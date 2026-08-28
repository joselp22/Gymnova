/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.EntrenadorCertificacionDAO;
import dao.EntrenadorDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.Entrenador;
import modelo.EntrenadorCertificacion;
import utilidades.SesionUsuario;
/**
 *
 * @author Usuario
 */

/**
 *
 * @author Usuario
 */
public class EntrenadorCertificacionControlador {

    private final EntrenadorCertificacionDAO certificacionDAO;
    private final EntrenadorDAO entrenadorDAO;
    private String mensaje;

    public EntrenadorCertificacionControlador() {

        certificacionDAO = new EntrenadorCertificacionDAO();
        entrenadorDAO = new EntrenadorDAO();
        mensaje = "";
    }

    public boolean registrar(
            EntrenadorCertificacion certificacion
    ) {

        mensaje = "";

        try {

            normalizarDatos(certificacion);
            validarCertificacion(certificacion);

            Entrenador entrenador
                    = entrenadorDAO.buscar(
                            certificacion.getIdEntrenador()
                    );

            if (entrenador == null) {

                mensaje
                        = "El entrenador seleccionado "
                        + "no existe.";

                return false;
            }

            if (!entrenador.isEstadoEntrenador()) {

                mensaje
                        = "El entrenador seleccionado "
                        + "se encuentra inactivo.";

                return false;
            }

            boolean existe
                    = certificacionDAO.existe(
                            certificacion.getIdEntrenador(),
                            certificacion.getCertificacion()
                    );

            if (existe) {

                mensaje
                        = "La certificacion ya esta registrada "
                        + "para este entrenador.";

                return false;
            }

            boolean guardado
                    = certificacionDAO.guardar(
                            certificacion
                    );

            if (guardado) {

                mensaje
                        = "Certificacion registrada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "No se pudo registrar "
                    + "la certificacion.";

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

    public List<EntrenadorCertificacion> listarPorEntrenador(
            Long idEntrenador
    ) {

        mensaje = "";

        if (idEntrenador == null) {

            mensaje
                    = "Seleccione un entrenador.";

            return new ArrayList<>();
        }

        try {

            return certificacionDAO
                    .listarPorEntrenador(
                            idEntrenador
                    );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "las certificaciones.";

            return new ArrayList<>();
        }
    }

    public boolean eliminar(
            Long idEntrenador,
            String certificacion
    ) {

        mensaje = "";

        try {

            if (!esAdministrador()) {

                mensaje
                        = "Solo el Administrador puede eliminar "
                        + "certificaciones definitivamente.";

                return false;
            }

            EntrenadorCertificacion datos
                    = new EntrenadorCertificacion();

            datos.setIdEntrenador(idEntrenador);
            datos.setCertificacion(certificacion);

            normalizarDatos(datos);
            validarCertificacion(datos);

            boolean eliminado
                    = certificacionDAO.eliminar(
                            datos.getIdEntrenador(),
                            datos.getCertificacion()
                    );

            if (eliminado) {

                mensaje
                        = "Certificacion eliminada "
                        + "correctamente.";

                return true;
            }

            mensaje
                    = "La certificacion seleccionada "
                    + "ya no existe.";

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

    private void validarCertificacion(
            EntrenadorCertificacion certificacion
    ) {

        if (certificacion == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "de la certificacion."
            );
        }

        if (certificacion.getIdEntrenador() == null) {

            throw new IllegalArgumentException(
                    "Seleccione un entrenador."
            );
        }

        if (certificacion.getCertificacion() == null
                || certificacion.getCertificacion().isBlank()) {

            throw new IllegalArgumentException(
                    "La certificacion es obligatoria."
            );
        }

        if (certificacion.getCertificacion().length() < 3
                || certificacion.getCertificacion().length() > 150) {

            throw new IllegalArgumentException(
                    "La certificacion debe contener "
                    + "entre 3 y 150 caracteres."
            );
        }
    }

    private void normalizarDatos(
            EntrenadorCertificacion certificacion
    ) {

        if (certificacion == null) {
            return;
        }

        String textoCertificacion
                = certificacion.getCertificacion();

        if (textoCertificacion != null) {

            certificacion.setCertificacion(
                    textoCertificacion.trim()
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

            return "La certificacion ya se encuentra "
                    + "registrada.";
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
