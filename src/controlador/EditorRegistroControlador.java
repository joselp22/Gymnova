package controlador;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextField;
import vista.PnlEditorRegistro;
import utilidades.SesionUsuario;

/**
 * Enlaza el formulario Design reutilizable con los modelos y controladores
 * CRUD. La vista no accede directamente a los DAO ni a JDBC.
 */
public class EditorRegistroControlador {

    private String mensaje = "";
    private final List<Field> campos = new ArrayList<>();
    private Object controlador;
    private Object registro;
    private Method operacion;
    private final CatalogoRelacionControlador catalogoRelacionControlador
            = new CatalogoRelacionControlador();
    private final AlcanceRolControlador alcanceRolControlador
            = new AlcanceRolControlador();

    public boolean preparar(
            String nombreControlador,
            Object seleccionado,
            boolean modificacion,
            PnlEditorRegistro vista
    ) {
        mensaje = "";
        campos.clear();

        try {
            controlador = Class.forName("controlador." + nombreControlador)
                    .getDeclaredConstructor().newInstance();
            operacion = buscarOperacion(controlador, modificacion
                    ? "modificar" : "registrar");
            Class<?> modelo = operacion.getParameterTypes()[0];

            registro = modificacion
                    ? seleccionado
                    : modelo.getDeclaredConstructor().newInstance();

            if (registro == null || !modelo.isInstance(registro)) {
                mensaje = "Seleccione primero un registro valido de la tabla.";
                return false;
            }

            campos.addAll(camposEditables(modelo));
            configurarVista(vista, modificacion);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            mensaje = mensajeExcepcion(ex);
            return false;
        }
    }

    public boolean guardar(PnlEditorRegistro vista) {
        mensaje = "";

        try {
            List<JTextField> entradas = vista.getCampos();
            for (int i = 0; i < campos.size(); i++) {
                Field campo = campos.get(i);
                JTextField entrada = entradas.get(i);

                if (!entrada.isEditable() && !vista.usaRelacion(i)) {
                    continue;
                }

                Object dato = vista.getValorCampo(i);
                Object valor = convertir(
                        dato == null ? "" : dato.toString(), campo.getType());
                Method setter = registro.getClass().getMethod(
                        "set" + mayusculaInicial(campo.getName()),
                        campo.getType()
                );
                setter.invoke(registro, valor);
            }

            if (SesionUsuario.haySesionActiva()
                    && !alcanceRolControlador.puedeModificar(
                            registro,
                            SesionUsuario.getUsuarioActual().getNombreRol(),
                            SesionUsuario.getUsuarioActual().getIdPersona())) {
                mensaje = alcanceRolControlador.getMensaje();
                return false;
            }

            Object resultado = operacion.invoke(controlador, registro);
            boolean correcto = operacion.getReturnType().equals(void.class)
                    || Boolean.TRUE.equals(resultado);
            mensaje = obtenerMensajeControlador(controlador);
            if (correcto && mensaje.isBlank()) {
                mensaje = "Registro guardado correctamente.";
            }
            return correcto;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            mensaje = mensajeExcepcion(ex);
            return false;
        }
    }

    public String getMensaje() {
        return mensaje;
    }

    private void configurarVista(
            PnlEditorRegistro vista,
            boolean modificacion
    ) throws ReflectiveOperationException {
        vista.configurarTitulo((modificacion ? "Modificar " : "Nuevo ")
                + separarNombre(registro.getClass().getSimpleName()));

        List<JLabel> etiquetas = vista.getEtiquetas();
        List<JTextField> entradas = vista.getCampos();
        for (int i = 0; i < etiquetas.size(); i++) {
            boolean visible = i < campos.size();
            etiquetas.get(i).setVisible(visible);
            entradas.get(i).setVisible(visible);
            if (!visible) {
                continue;
            }

            Field campo = campos.get(i);
            etiquetas.get(i).setText(separarNombre(campo.getName()) + " *");
            String ayudaCampo = ayudaCampo(campo, registro.getClass());
            etiquetas.get(i).setToolTipText(ayudaCampo);
            entradas.get(i).setToolTipText(ayudaCampo);
            Object valor = leerValor(registro, campo);
            entradas.get(i).setText(valor == null ? "" : valor.toString());

            boolean llavePrimaria = esLlavePrimaria(campo, registro.getClass());
            boolean identificadorHeredado
                    = esIdentificadorHeredado(campo, registro.getClass());
            boolean codigoAutomatico
                    = esCodigoAutomatico(campo, registro.getClass());
            boolean bloquearLlave = llavePrimaria
                    && (modificacion
                    || !catalogoRelacionControlador.esRelacion(campo.getName()));
            entradas.get(i).setEditable(!bloquearLlave && !codigoAutomatico);
            if (!bloquearLlave
                    && catalogoRelacionControlador.esRelacion(campo.getName())) {
                vista.configurarRelacion(
                        i,
                        catalogoRelacionControlador.listarParaSesion(campo.getName()),
                        valor
                );
            }
            if (codigoAutomatico && !modificacion) {
                entradas.get(i).setText("");
                entradas.get(i).setToolTipText(
                        "Este codigo se asigna automaticamente al guardar."
                );
            }
            if ((campo.getType().equals(boolean.class)
                    || campo.getType().equals(Boolean.class))
                    && valor == null) {
                entradas.get(i).setText("true");
            }
        }
        vista.configurarAyuda(
                "Seleccione las relaciones por nombre. Fechas: AAAA-MM-DD | "
                + "Fecha y hora: AAAA-MM-DDTHH:MM"
        );
    }

    private Method buscarOperacion(Object objeto, String nombre) {
        return Arrays.stream(objeto.getClass().getMethods())
                .filter(metodo -> nombre.equals(metodo.getName()))
                .filter(metodo -> metodo.getParameterCount() == 1)
                .filter(metodo -> metodo.getParameterTypes()[0]
                .getPackageName().equals("modelo"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                "Este submodulo no dispone de la operacion " + nombre + "."));
    }

    private List<Field> camposEditables(Class<?> modelo) {
        List<Field> resultado = new ArrayList<>();
        Class<?> superior = modelo.getSuperclass();
        while (superior != null && !superior.equals(Object.class)) {
            Arrays.stream(superior.getDeclaredFields())
                    .filter(campo -> campo.getName().equals("idPersona"))
                    .findFirst().ifPresent(resultado::add);
            superior = superior.getSuperclass();
        }
        Arrays.stream(modelo.getDeclaredFields())
                .filter(campo -> !Modifier.isStatic(campo.getModifiers()))
                .filter(campo -> !campo.getType().isArray())
                .filter(campo -> !campo.getName().toLowerCase().contains("clave"))
                .filter(campo -> !campo.getName().toLowerCase().contains("foto"))
                .filter(campo -> !campo.getName().toLowerCase().contains("imagen"))
                .limit(15 - resultado.size())
                .forEach(resultado::add);
        return resultado;
    }

    private Object leerValor(Object objeto, Field campo)
            throws ReflectiveOperationException {
        String prefijo = campo.getType().equals(boolean.class)
                || campo.getType().equals(Boolean.class) ? "is" : "get";
        return objeto.getClass().getMethod(
                prefijo + mayusculaInicial(campo.getName()))
                .invoke(objeto);
    }

    private Object convertir(String texto, Class<?> tipo) {
        String valor = texto == null ? "" : texto.trim();
        if (tipo.equals(String.class)) {
            return valor.isBlank() ? null : valor;
        }
        if (valor.isBlank()) {
            return tipo.isPrimitive() ? false : null;
        }
        if (tipo.equals(Long.class)) {
            return Long.valueOf(valor);
        }
        if (tipo.equals(Integer.class) || tipo.equals(int.class)) {
            return Integer.valueOf(valor);
        }
        if (tipo.equals(BigDecimal.class)) {
            return new BigDecimal(valor.replace(',', '.'));
        }
        if (tipo.equals(LocalDate.class)) {
            return LocalDate.parse(valor);
        }
        if (tipo.equals(LocalDateTime.class)) {
            return LocalDateTime.parse(valor.replace(' ', 'T'));
        }
        if (tipo.equals(LocalTime.class)) {
            return LocalTime.parse(valor);
        }
        if (tipo.equals(Boolean.class) || tipo.equals(boolean.class)) {
            return "true".equalsIgnoreCase(valor)
                    || "si".equalsIgnoreCase(valor)
                    || "activo".equalsIgnoreCase(valor)
                    || "1".equals(valor);
        }
        throw new IllegalArgumentException(
                "Tipo de campo no compatible: " + tipo.getSimpleName());
    }

    private boolean esLlavePrimaria(Field campo, Class<?> modelo) {
        if (esIdentificadorHeredado(campo, modelo)
                || campo.getName().equalsIgnoreCase(
                        "id" + modelo.getSimpleName())) {
            return true;
        }
        // En el modelo GYMNOVA la llave sustituta se declara como el primer
        // atributo id... de la clase (idRutinaEjercicio, idClienteObjetivo...).
        return Arrays.stream(modelo.getDeclaredFields())
                .filter(actual -> !Modifier.isStatic(actual.getModifiers()))
                .filter(actual -> actual.getName().startsWith("id"))
                .findFirst()
                .map(actual -> actual.getName().equals(campo.getName()))
                .orElse(false);
    }

    private boolean esIdentificadorHeredado(Field campo, Class<?> modelo) {
        return campo.getName().equals("idPersona")
                && !modelo.getSimpleName().equals("Persona");
    }

    private boolean esCodigoAutomatico(Field campo, Class<?> modelo) {
        String nombre = campo.getName();
        String clase = modelo.getSimpleName();
        return (nombre.equals("codigoCliente") && clase.equals("Cliente"))
                || (nombre.equals("codigoEmpleado") && clase.equals("Empleado"))
                || (nombre.equals("codigoDescuento") && clase.equals("Descuento"))
                || (nombre.equals("codigoInterno") && clase.equals("Equipo"))
                || (nombre.equals("codigoEvaluacion") && clase.equals("EvaluacionFisica"))
                || (nombre.equals("codigoPago") && clase.equals("Pago"))
                || (nombre.equals("codigoPlan") && clase.equals("PlanNutricional"))
                || (nombre.equals("codigoProducto") && clase.equals("Producto"))
                || (nombre.equals("codigoReserva") && clase.equals("Reserva"))
                || (nombre.equals("numeroMembresia") && clase.equals("Membresia"))
                || (nombre.equals("numeroCompra") && clase.equals("Compra"))
                || (nombre.equals("numeroMantenimiento") && clase.equals("Mantenimiento"))
                || (nombre.equals("numeroComprobante") && clase.equals("Comprobante"))
                || (nombre.equals("numeroFactura") && clase.equals("Factura"));
    }

    private String obtenerMensajeControlador(Object objeto) {
        try {
            Object valor = objeto.getClass().getMethod("getMensaje").invoke(objeto);
            return valor == null ? "" : valor.toString();
        } catch (ReflectiveOperationException ex) {
            return "";
        }
    }

    private String mensajeExcepcion(Throwable ex) {
        Throwable causa = ex;
        while (causa instanceof InvocationTargetException
                && ((InvocationTargetException) causa).getCause() != null) {
            causa = ((InvocationTargetException) causa).getCause();
        }
        if (causa instanceof NumberFormatException) {
            return "Revise los campos numericos y los identificadores relacionados.";
        }
        return causa.getMessage() == null
                ? "No fue posible completar la operacion."
                : causa.getMessage();
    }

    private String separarNombre(String texto) {
        if (texto == null || texto.isBlank()) {
            return "Registro";
        }
        String separado = texto.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
        return Character.toUpperCase(separado.charAt(0)) + separado.substring(1);
    }

    private String ayudaCampo(Field campo, Class<?> modelo) {
        String nombre = campo.getName();
        if (esCodigoAutomatico(campo, modelo)) {
            return "Codigo automatico y protegido; no debe escribirlo.";
        }
        if (esLlavePrimaria(campo, modelo)) {
            return "Identificador protegido; no puede modificarse.";
        }
        if (catalogoRelacionControlador.esRelacion(nombre)) {
            return "Seleccione un registro por su nombre; no necesita memorizar IDs.";
        }
        if (nombre.toLowerCase().contains("fecha")) {
            return "Ingrese la fecha con formato AAAA-MM-DD.";
        }
        if (nombre.toLowerCase().contains("estado")) {
            return "Indica si el registro esta activo, pendiente, completado o inactivo.";
        }
        return "Ingrese " + separarNombre(nombre).toLowerCase() + ".";
    }

    private String mayusculaInicial(String texto) {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
