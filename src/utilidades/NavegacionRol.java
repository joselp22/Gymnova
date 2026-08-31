package utilidades;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Fuente unica de verdad para la navegacion por rol.
 * Impide que una ventana abierta para un rol navegue a experiencias de otro.
 */
public final class NavegacionRol {

    public static final String INICIO = "INICIO";
    public static final String PERSONAS = "PERSONAS";
    public static final String CLIENTES = "CLIENTES";
    public static final String PERSONAL = "PERSONAL";
    public static final String SEGURIDAD = "SEGURIDAD";
    public static final String MEMBRESIAS = "MEMBRESIAS";
    public static final String ACCESO = "ACCESO";
    public static final String RUTINAS = "RUTINAS";
    public static final String SALUD = "SALUD";
    public static final String NUTRICION = "NUTRICION";
    public static final String FINANZAS = "FINANZAS";
    public static final String REPORTES = "REPORTES";
    public static final String CONFIGURACION = "CONFIGURACION";

    private static final Map<String, Set<String>> MODULOS = Map.of(
            "ADMINISTRADOR", conjunto(INICIO, PERSONAS, CLIENTES, MEMBRESIAS,
                    ACCESO, RUTINAS, SALUD, NUTRICION, FINANZAS, REPORTES,
                    CONFIGURACION),
            "RECEPCIONISTA", conjunto(INICIO, PERSONAS, CLIENTES, PERSONAL,
                    SEGURIDAD, MEMBRESIAS, ACCESO, FINANZAS, REPORTES,
                    CONFIGURACION),
            "ENTRENADOR", conjunto(INICIO, CLIENTES, ACCESO, RUTINAS, SALUD,
                    REPORTES, CONFIGURACION),
            "NUTRICIONISTA", conjunto(INICIO, CLIENTES, SALUD, NUTRICION,
                    REPORTES, CONFIGURACION),
            "CLIENTE", conjunto(INICIO, CLIENTES, MEMBRESIAS, ACCESO,
                    RUTINAS, SALUD, NUTRICION, REPORTES, CONFIGURACION)
    );

    private NavegacionRol() {
    }

    public static String normalizarRol(String rol) {
        if (rol == null) {
            return "";
        }
        return rol.trim().toUpperCase(Locale.ROOT);
    }

    public static String normalizarModulo(String modulo) {
        if (modulo == null) {
            return "";
        }
        return modulo.trim().toUpperCase(Locale.ROOT);
    }

    public static Set<String> modulosPermitidos(String rol) {
        Set<String> permitidos = MODULOS.get(normalizarRol(rol));
        return permitidos == null ? Set.of() : permitidos;
    }

    public static boolean puedeAbrir(String rol, String modulo) {
        return modulosPermitidos(rol).contains(normalizarModulo(modulo));
    }

    /** El administrador entra directamente a las pantallas de gestion CRUD. */
    public static boolean usaGestionAdministrativa(String rol) {
        return "ADMINISTRADOR".equals(normalizarRol(rol));
    }

    private static Set<String> conjunto(String... modulos) {
        return Set.copyOf(new LinkedHashSet<>(java.util.Arrays.asList(modulos)));
    }
}
