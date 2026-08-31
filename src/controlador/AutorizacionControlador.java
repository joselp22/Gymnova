/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlador;

import dao.RolPermisoDAO;
import dao.RolDAO;

import java.sql.SQLException;

import modelo.Usuario;
import modelo.Rol;
import utilidades.SesionUsuario;

/**
 *
 * @author Usuario
 */
public class AutorizacionControlador {

    private final RolPermisoDAO rolPermisoDAO;
    private final RolDAO rolDAO;
    private String mensaje;

    public AutorizacionControlador() {

        rolPermisoDAO = new RolPermisoDAO();
        rolDAO = new RolDAO();
        mensaje = "";
    }

    public boolean tienePermiso(
            String modulo,
            String accion
    ) {

        mensaje = "";

        try {

            validarDatos(
                    modulo,
                    accion
            );

            if (!SesionUsuario.haySesionActiva()) {

                mensaje
                        = "No existe una sesion activa.";

                return false;
            }

            Usuario usuarioActual
                    = SesionUsuario.getUsuarioActual();

            if (usuarioActual.getIdRol() == null) {

                mensaje
                        = "El usuario actual no tiene "
                        + "un rol asignado.";

                return false;
            }

            if (esAdministradorVerificado(usuarioActual)) {
                return true;
            }

            // Recepción administra el alta operativa del personal y las cuentas
            // de acceso. No se le conceden operaciones críticas sobre permisos
            // del sistema ni eliminación definitiva.
            if (esRecepcionistaVerificado(usuarioActual)
                    && permisoOperativoRecepcion(modulo, accion)) {
                return true;
            }

            boolean autorizado
                    = rolPermisoDAO.tienePermiso(
                            usuarioActual.getIdRol(),
                            modulo.trim().toUpperCase(),
                            accion.trim().toUpperCase()
                    );

            if (!autorizado) {

                mensaje
                        = "No tiene permiso para realizar "
                        + "esta operacion.";
            }

            return autorizado;

        } catch (IllegalArgumentException e) {

            mensaje = e.getMessage();
            return false;

        } catch (SQLException e) {

            mensaje
                    = "No se pudo verificar "
                    + "el permiso del usuario.";

            return false;
        }
    }

    public void exigirPermiso(
            String modulo,
            String accion
    ) {

        boolean autorizado
                = tienePermiso(
                        modulo,
                        accion
                );

        if (!autorizado) {

            throw new SecurityException(
                    mensaje
            );
        }
    }

    private void validarDatos(
            String modulo,
            String accion
    ) {

        if (modulo == null
                || modulo.isBlank()) {

            throw new IllegalArgumentException(
                    "No se recibio el modulo."
            );
        }

        if (accion == null
                || accion.isBlank()) {

            throw new IllegalArgumentException(
                    "No se recibio la accion."
            );
        }
    }

    private boolean esAdministradorVerificado(Usuario usuario)
            throws SQLException {
        if (usuario.getIdRol() == null || usuario.getNombreRol() == null) {
            return false;
        }
        Rol rolPersistido = rolDAO.buscar(usuario.getIdRol());
        return rolPersistido != null
                && rolPersistido.isEstadoRol()
                && "ADMINISTRADOR".equalsIgnoreCase(
                        rolPersistido.getNombreRol())
                && "ADMINISTRADOR".equalsIgnoreCase(
                        usuario.getNombreRol());
    }

    private boolean esRecepcionistaVerificado(Usuario usuario)
            throws SQLException {
        if (usuario.getIdRol() == null || usuario.getNombreRol() == null) {
            return false;
        }
        Rol rolPersistido = rolDAO.buscar(usuario.getIdRol());
        return rolPersistido != null
                && rolPersistido.isEstadoRol()
                && "RECEPCIONISTA".equalsIgnoreCase(rolPersistido.getNombreRol())
                && "RECEPCIONISTA".equalsIgnoreCase(usuario.getNombreRol());
    }

    private boolean permisoOperativoRecepcion(String modulo, String accion) {
        String m = modulo == null ? "" : modulo.trim().toUpperCase();
        String a = accion == null ? "" : accion.trim().toUpperCase();

        if (java.util.Set.of("PERSONAL", "EMPLEADOS", "ENTRENADORES",
                "NUTRICIONISTAS").contains(m)) {
            return java.util.Set.of("VER", "CREAR", "MODIFICAR", "DESACTIVAR")
                    .contains(a);
        }
        if ("USUARIOS".equals(m)) {
            return java.util.Set.of("VER", "CREAR", "MODIFICAR", "DESACTIVAR")
                    .contains(a);
        }
        if ("ROLES".equals(m)) {
            return "VER".equals(a);
        }
        if ("BITACORA".equals(m)) {
            return "VER".equals(a);
        }
        return false;
    }

    public String getMensaje() {
        return mensaje;
    }
}
