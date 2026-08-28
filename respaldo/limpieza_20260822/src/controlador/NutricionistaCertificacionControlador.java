/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.NutricionistaCertificacionDAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import modelo.NutricionistaCertificacion;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class NutricionistaCertificacionControlador {

    private final NutricionistaCertificacionDAO certificacionDAO;
    private String mensaje;

    public NutricionistaCertificacionControlador() {

        certificacionDAO = new NutricionistaCertificacionDAO();
        mensaje = "";
    }

    public boolean registrar(
            NutricionistaCertificacion certificacion
    ) {

        mensaje = "";

        try {

            normalizarDatos(certificacion);
            validarCertificacion(certificacion);

            boolean existe
                    = certificacionDAO.existe(
                            certificacion.getIdNutricionista(),
                            certificacion.getCertificacion()
                    );

            if (existe) {

                mensaje
                        = "La certificacion ya esta registrada "
                        + "para este nutricionista.";

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

    public List<NutricionistaCertificacion> listarPorNutricionista(
            Long idNutricionista
    ) {

        mensaje = "";

        if (idNutricionista == null) {

            mensaje
                    = "Seleccione un nutricionista.";

            return new ArrayList<>();
        }

        try {

            return certificacionDAO
                    .listarPorNutricionista(
                            idNutricionista
                    );

        } catch (SQLException e) {

            mensaje
                    = "No se pudieron cargar "
                    + "las certificaciones.";

            return new ArrayList<>();
        }
    }

    public boolean eliminar(
            Long idNutricionista,
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

            NutricionistaCertificacion datos
                    = new NutricionistaCertificacion();

            datos.setIdNutricionista(idNutricionista);
            datos.setCertificacion(certificacion);

            normalizarDatos(datos);
            validarCertificacion(datos);

            boolean eliminado
                    = certificacionDAO.eliminar(
                            datos.getIdNutricionista(),
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
            NutricionistaCertificacion certificacion
    ) {

        if (certificacion == null) {

            throw new IllegalArgumentException(
                    "No se recibieron los datos "
                    + "de la certificacion."
            );
        }

        if (certificacion.getIdNutricionista() == null) {

            throw new IllegalArgumentException(
                    "Seleccione un nutricionista."
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
            NutricionistaCertificacion certificacion
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

            return "No se puede registrar la certificacion "
                    + "porque el nutricionista seleccionado "
                    + "no existe.";
        }

        if ("23505".equals(e.getSQLState())) {

            return "La certificacion ya se encuentra "
                    + "registrada para este nutricionista.";
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
