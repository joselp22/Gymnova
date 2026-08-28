package pruebas;

import controlador.AutorizacionControlador;
import controlador.LoginControlador;
import utilidades.NavegacionRol;
import utilidades.SesionUsuario;

/** Verifica la matriz funcional y las denegaciones de los cinco roles. */
public final class PruebaPermisosCincoRoles {

    private static final String CLAVE = "Gymnova2026*";

    private PruebaPermisosCincoRoles() {
    }

    public static void main(String[] args) throws Exception {
        verificar("admin", "Administrador",
                new String[][]{{"SEGURIDAD", "ELIMINAR"},
                    {"INVENTARIO", "MODIFICAR"}},
                new String[][]{});
        verificar("RECEPCION", "Recepcionista",
                new String[][]{{"CLIENTES", "CREAR"},
                    {"MEMBRESIAS", "MODIFICAR"}, {"FINANZAS", "CREAR"}},
                new String[][]{{"SEGURIDAD", "VER"},
                    {"INVENTARIO", "MODIFICAR"}});
        verificar("ENTRENADOR", "Entrenador",
                new String[][]{{"RUTINAS", "MODIFICAR"},
                    {"SALUD", "CREAR"}, {"CLIENTES", "VER"}},
                new String[][]{{"FINANZAS", "VER"},
                    {"CLIENTES", "MODIFICAR"}});
        verificar("NUTRICION", "Nutricionista",
                new String[][]{{"NUTRICION", "MODIFICAR"},
                    {"SALUD", "CREAR"}, {"CLIENTES", "VER"}},
                new String[][]{{"RUTINAS", "MODIFICAR"},
                    {"CLIENTES", "MODIFICAR"}});
        verificar("cliente", "Cliente",
                new String[][]{{"CLIENTES", "VER"},
                    {"RUTINAS", "MODIFICAR"},
                    {"CONFIGURACION", "MODIFICAR"}},
                new String[][]{{"PERSONAS", "VER"},
                    {"FINANZAS", "CREAR"}, {"SEGURIDAD", "VER"}});

        SesionUsuario.cerrarSesion();
        System.out.println("PERMISOS_CINCO_ROLES_OK=ALLOW+DENY+NAVEGACION");
    }

    private static void verificar(String usuario, String rol,
            String[][] permitidos, String[][] denegados) throws Exception {
        SesionUsuario.cerrarSesion();
        new LoginControlador().iniciarSesion(usuario, CLAVE.toCharArray());
        exigir(rol.equalsIgnoreCase(
                SesionUsuario.getUsuarioActual().getNombreRol()),
                "Rol incorrecto para " + usuario);

        AutorizacionControlador autorizacion = new AutorizacionControlador();
        for (String[] permiso : permitidos) {
            exigir(autorizacion.tienePermiso(permiso[0], permiso[1]),
                    rol + " no obtuvo " + permiso[0] + "/" + permiso[1]);
        }
        for (String[] permiso : denegados) {
            exigir(!autorizacion.tienePermiso(permiso[0], permiso[1]),
                    rol + " obtuvo permiso indebido "
                    + permiso[0] + "/" + permiso[1]);
        }
        for (String modulo : NavegacionRol.modulosPermitidos(rol)) {
            exigir(NavegacionRol.puedeAbrir(rol, modulo),
                    rol + " no puede abrir su modulo " + modulo);
        }
        exigir(!NavegacionRol.puedeAbrir(rol, "MODULO_INVENTADO"),
                rol + " pudo abrir un modulo desconocido");
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
