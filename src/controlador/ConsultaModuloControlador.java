package controlador;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adaptador de consulta para los submódulos administrativos. Mantiene la
 * reflexión fuera de las vistas y siempre delega las operaciones en los
 * controladores CRUD existentes.
 */
public class ConsultaModuloControlador {

    private String mensaje = "";
    private final AlcanceRolControlador alcanceRolControlador
            = new AlcanceRolControlador();

    public List<?> listar(
            String claseControlador,
            String criterio
    ) {

        mensaje = "";

        try {
            Object controlador = crearControlador(claseControlador);
            Method metodo = Arrays.stream(controlador.getClass().getMethods())
                    .filter(actual -> "listar".equals(actual.getName()))
                    .filter(actual -> actual.getParameterCount() == 1)
                    .filter(actual -> actual.getParameterTypes()[0]
                    .equals(String.class))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                    "El submódulo requiere seleccionar primero un registro relacionado."
            ));

            Object resultado = metodo.invoke(
                    controlador,
                    criterio == null ? "" : criterio.trim()
            );

            return resultado instanceof List<?> lista
                    ? lista
                    : new ArrayList<>();
        } catch (ReflectiveOperationException | RuntimeException ex) {
            mensaje = mensajeExcepcion(ex);
            return new ArrayList<>();
        }
    }

    public List<?> listarParaSesion(
            String claseControlador,
            String criterio,
            String nombreRol,
            Long idPersona
    ) {
        String rol = nombreRol == null ? "" : nombreRol.trim().toUpperCase();
        List<?> lista = listar(claseControlador, criterio);
        if (rol.isBlank()) {
            mensaje = "El rol de la sesión no es válido.";
            return new ArrayList<>();
        }
        if ("ADMINISTRADOR".equals(rol)
                || "RECEPCIONISTA".equals(rol)) {
            return lista;
        }
        if (!java.util.Set.of("CLIENTE", "ENTRENADOR", "NUTRICIONISTA")
                .contains(rol)) {
            mensaje = "Rol desconocido: acceso denegado.";
            return new ArrayList<>();
        }
        if (idPersona == null) {
            mensaje = "El usuario no esta vinculado con una persona.";
            return new ArrayList<>();
        }
        return alcanceRolControlador.filtrar(lista, rol, idPersona);
    }

    public boolean desactivar(
            String claseControlador,
            Object registro
    ) {
        return ejecutarAccion(
                claseControlador,
                registro,
                "desactivar"
        );
    }

    public boolean eliminarDefinitivamente(
            String claseControlador,
            Object registro
    ) {
        return ejecutarAccion(
                claseControlador,
                registro,
                "eliminarDefinitivamente"
        );
    }

    public String[] obtenerColumnas(Object registro) {
        if (registro == null) {
            return new String[]{"Sin registros"};
        }

        return camposVisibles(registro.getClass()).stream()
                .map(Field::getName)
                .map(this::separarNombre)
                .toArray(String[]::new);
    }

    public Object[] obtenerFila(Object registro) {
        List<Field> campos = camposVisibles(registro.getClass());
        Object[] fila = new Object[campos.size()];

        for (int i = 0; i < campos.size(); i++) {
            try {
                Field campo = campos.get(i);
                String prefijo = campo.getType().equals(boolean.class)
                        || campo.getType().equals(Boolean.class)
                        ? "is"
                        : "get";
                Method getter = registro.getClass().getMethod(
                        prefijo + Character.toUpperCase(campo.getName().charAt(0))
                        + campo.getName().substring(1)
                );
                Object valor = getter.invoke(registro);
                fila[i] = valor instanceof Boolean booleano
                        ? booleano ? "ACTIVO" : "INACTIVO"
                        : valor;
            } catch (ReflectiveOperationException ex) {
                fila[i] = "-";
            }
        }

        return fila;
    }

    private boolean ejecutarAccion(
            String claseControlador,
            Object registro,
            String accion
    ) {

        mensaje = "";

        if (registro == null) {
            mensaje = "Seleccione un registro de la tabla.";
            return false;
        }

        try {
            Object controlador = crearControlador(claseControlador);
            Method metodo = Arrays.stream(controlador.getClass().getMethods())
                    .filter(actual -> accion.equals(actual.getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                    "La operación no está disponible para este submódulo."
            ));

            List<Object> identificadores = obtenerIdentificadores(registro);
            if (identificadores.size() < metodo.getParameterCount()) {
                throw new IllegalStateException(
                        "No se pudieron determinar las llaves del registro."
                );
            }

            Object[] argumentos = identificadores.stream()
                    .limit(metodo.getParameterCount())
                    .toArray();
            Object resultado = metodo.invoke(controlador, argumentos);

            if (metodo.getReturnType().equals(void.class)) {
                return true;
            }

            boolean correcto = Boolean.TRUE.equals(resultado);
            if (!correcto) {
                mensaje = obtenerMensajeControlador(controlador);
            }
            return correcto;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            mensaje = mensajeExcepcion(ex);
            return false;
        }
    }

    private Object crearControlador(
            String claseControlador
    ) throws ReflectiveOperationException {
        return Class.forName("controlador." + claseControlador)
                .getDeclaredConstructor()
                .newInstance();
    }

    private List<Field> camposVisibles(Class<?> tipo) {
        return Arrays.stream(tipo.getDeclaredFields())
                .filter(campo -> !Modifier.isStatic(campo.getModifiers()))
                .filter(campo -> !campo.getType().isArray())
                .filter(campo -> !campo.getName().toLowerCase().contains("clave"))
                .filter(campo -> !campo.getName().toLowerCase().contains("foto"))
                .filter(campo -> !campo.getName().toLowerCase().contains("imagen"))
                .limit(10)
                .toList();
    }

    private List<Object> obtenerIdentificadores(
            Object registro
    ) throws ReflectiveOperationException {

        List<Object> valores = new ArrayList<>();

        for (Field campo : registro.getClass().getDeclaredFields()) {
            if (!campo.getName().startsWith("id")) {
                continue;
            }

            Method getter = registro.getClass().getMethod(
                    "get" + Character.toUpperCase(campo.getName().charAt(0))
                    + campo.getName().substring(1)
            );
            Object valor = getter.invoke(registro);
            if (valor != null) {
                valores.add(valor);
            }
        }

        return valores;
    }

    private String obtenerMensajeControlador(Object controlador) {
        try {
            Object valor = controlador.getClass()
                    .getMethod("getMensaje")
                    .invoke(controlador);
            return valor == null ? "" : valor.toString();
        } catch (ReflectiveOperationException ex) {
            return "";
        }
    }

    private String mensajeExcepcion(Throwable error) {
        Throwable causa = error;
        if (error instanceof InvocationTargetException invocacion
                && invocacion.getCause() != null) {
            causa = invocacion.getCause();
        }
        String detalle = causa.getMessage();
        return detalle == null || detalle.isBlank()
                ? "No fue posible completar la operación."
                : detalle;
    }

    private String separarNombre(String nombre) {
        String separado = nombre.replaceAll("([a-z])([A-Z])", "$1 $2");
        return Character.toUpperCase(separado.charAt(0))
                + separado.substring(1);
    }

    private List<?> filtrar(List<?> lista, String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return lista;
        }
        String buscado = criterio.trim().toLowerCase();
        return lista.stream()
                .filter(registro -> Arrays.stream(obtenerFila(registro))
                .anyMatch(valor -> valor != null
                && valor.toString().toLowerCase().contains(buscado)))
                .toList();
    }

    public String getMensaje() {
        return mensaje;
    }
}
