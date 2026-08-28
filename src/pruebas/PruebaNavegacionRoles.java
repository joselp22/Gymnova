package pruebas;

import java.util.Set;
import utilidades.NavegacionRol;

/** Verifica que ningun rol reciba modulos pertenecientes a otro rol. */
public final class PruebaNavegacionRoles {

    private PruebaNavegacionRoles() {
    }

    public static void main(String[] args) {
        verificar("ADMINISTRADOR", Set.of("INICIO", "PERSONAS", "CLIENTES",
                "PERSONAL", "SEGURIDAD", "MEMBRESIAS", "ACCESO", "RUTINAS",
                "SALUD", "NUTRICION", "FINANZAS", "REPORTES",
                "CONFIGURACION"));
        verificar("RECEPCIONISTA", Set.of("INICIO", "PERSONAS", "CLIENTES",
                "MEMBRESIAS", "ACCESO", "FINANZAS", "REPORTES",
                "CONFIGURACION"));
        verificar("ENTRENADOR", Set.of("INICIO", "CLIENTES", "RUTINAS",
                "SALUD", "REPORTES", "CONFIGURACION"));
        verificar("NUTRICIONISTA", Set.of("INICIO", "CLIENTES", "SALUD",
                "NUTRICION", "REPORTES", "CONFIGURACION"));
        verificar("CLIENTE", Set.of("INICIO", "CLIENTES", "MEMBRESIAS",
                "ACCESO", "RUTINAS", "SALUD", "NUTRICION", "REPORTES",
                "CONFIGURACION"));

        if (!NavegacionRol.usaGestionAdministrativa("Administrador")
                || NavegacionRol.usaGestionAdministrativa("Cliente")) {
            throw new AssertionError("La gestion CRUD no esta aislada al administrador");
        }
        if (NavegacionRol.puedeAbrir("CLIENTE", "SEGURIDAD")
                || NavegacionRol.puedeAbrir("ENTRENADOR", "FINANZAS")
                || NavegacionRol.puedeAbrir("NUTRICIONISTA", "RUTINAS")
                || NavegacionRol.puedeAbrir("RECEPCIONISTA", "SALUD")) {
            throw new AssertionError("Se detecto un cruce de navegacion entre roles");
        }
        System.out.println("NAVEGACION_ROLES_OK=5");
    }

    private static void verificar(String rol, Set<String> esperados) {
        Set<String> actuales = NavegacionRol.modulosPermitidos(rol);
        if (!actuales.equals(esperados)) {
            throw new AssertionError(rol + " esperaba " + esperados
                    + " pero obtuvo " + actuales);
        }
    }
}
