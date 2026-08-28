package pruebas;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Arrays;

/** Ejecuta listar("") sobre cada controlador CRUD sin modificar datos. */
public class PruebaControladoresLectura {

    public static void main(String[] args) throws URISyntaxException {
        File raiz = new File(PruebaControladoresLectura.class
                .getProtectionDomain().getCodeSource().getLocation().toURI());
        File carpeta = new File(raiz, "controlador");
        int correctos = 0;
        int errores = 0;
        for (File archivo : carpeta.listFiles((dir, nombre)
                -> nombre.endsWith("Controlador.class")
                && !nombre.contains("$")
                && !nombre.startsWith("Consulta")
                && !nombre.startsWith("Editor")
                && !nombre.startsWith("Catalogo")
                && !nombre.startsWith("Alcance"))) {
            String clase = "controlador." + archivo.getName().replace(".class", "");
            try {
                Class<?> tipo = Class.forName(clase);
                Method listar = Arrays.stream(tipo.getMethods())
                        .filter(m -> m.getName().equals("listar"))
                        .filter(m -> m.getParameterCount() == 1)
                        .filter(m -> m.getParameterTypes()[0].equals(String.class))
                        .findFirst().orElse(null);
                if (listar == null) continue;
                Object instancia = tipo.getDeclaredConstructor().newInstance();
                Object resultado = listar.invoke(instancia, "");
                int cantidad = resultado instanceof java.util.List<?> lista
                        ? lista.size() : -1;
                System.out.printf("OK   %-46s %d%n", tipo.getSimpleName(), cantidad);
                correctos++;
            } catch (ReflectiveOperationException | RuntimeException ex) {
                Throwable causa = ex instanceof InvocationTargetException inv
                        && inv.getCause() != null ? inv.getCause() : ex;
                System.out.printf("ERROR %-46s %s%n", clase, causa.getMessage());
                errores++;
            }
        }
        System.out.println("TOTAL correctos=" + correctos + " errores=" + errores);
        if (errores > 0) System.exit(1);
    }
}
