package controlador;

import dao.AlcanceRolDAO;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Limita cada sesion a sus clientes, planes, rutinas y seguimientos. */
public class AlcanceRolControlador {

    private final AlcanceRolDAO dao = new AlcanceRolDAO();
    private String mensaje = "";

    public List<?> filtrar(List<?> registros, String rol, Long idPersona) {
        List<Object> resultado = new ArrayList<>();
        for (Object registro : registros) {
            if (puedeAcceder(registro, rol, idPersona)) {
                resultado.add(registro);
            }
        }
        return resultado;
    }

    public boolean puedeModificar(Object registro, String rol, Long idPersona) {
        boolean permitido = puedeAcceder(registro, rol, idPersona);
        if (!permitido && mensaje.isBlank()) {
            mensaje = "El registro no pertenece al alcance del usuario actual.";
        }
        return permitido;
    }

    public boolean puedeAcceder(Object registro, String nombreRol, Long idPersona) {
        mensaje = "";
        if (registro == null) {
            mensaje = "Seleccione un registro.";
            return false;
        }
        String rol = nombreRol == null ? "" : nombreRol.trim().toUpperCase();
        if (rol.isBlank()) {
            mensaje = "El rol de la sesión no es válido.";
            return false;
        }
        if ("ADMINISTRADOR".equals(rol)
                || "RECEPCIONISTA".equals(rol)) {
            return true;
        }
        if (idPersona == null) {
            mensaje = "El usuario no esta vinculado con una persona.";
            return false;
        }
        try {
            return switch (rol) {
                case "CLIENTE" -> alcanceCliente(registro, idPersona);
                case "ENTRENADOR" -> alcanceEntrenador(registro, idPersona);
                case "NUTRICIONISTA" -> alcanceNutricionista(registro, idPersona);
                default -> {
                    mensaje = "Rol desconocido: acceso denegado.";
                    yield false;
                }
            };
        } catch (ReflectiveOperationException | SQLException ex) {
            mensaje = ex.getMessage() == null
                    ? "No fue posible validar el alcance del registro."
                    : ex.getMessage();
            return false;
        }
    }

    private boolean alcanceCliente(Object registro, Long persona)
            throws ReflectiveOperationException, SQLException {
        String tipo = registro.getClass().getSimpleName();
        boolean relacionComprobada = false;
        Long cliente = valor(registro, "getIdCliente");
        if (cliente != null) {
            if (!cliente.equals(persona)) return false;
            relacionComprobada = true;
        }
        if ("Cliente".equals(tipo) || "Persona".equals(tipo))
            return persona.equals(valor(registro, "getIdPersona"));
        Long rutina = valor(registro, "getIdRutina");
        if (rutina != null) {
            if (!existe("SELECT EXISTS (SELECT 1 FROM asignacion_rutina WHERE id_cliente=? AND id_rutina=?)", persona, rutina)) return false;
            relacionComprobada = true;
        }
        Long evaluacion = valor(registro, "getIdEvaluacion");
        if (evaluacion != null) {
            if (!existe("SELECT EXISTS (SELECT 1 FROM evaluacion_fisica WHERE id_evaluacion=? AND id_cliente=?)", evaluacion, persona)) return false;
            relacionComprobada = true;
        }
        Long plan = valor(registro, "getIdPlanNutricional");
        if (plan != null) {
            if (!existe("SELECT EXISTS (SELECT 1 FROM plan_nutricional WHERE id_plan_nutricional=? AND id_cliente=?)", plan, persona)) return false;
            relacionComprobada = true;
        }
        Long factura = valor(registro, "getIdFactura");
        if (factura != null) return existe("SELECT EXISTS (SELECT 1 FROM factura WHERE id_factura=? AND id_cliente=?)", factura, persona);
        Long pago = valor(registro, "getIdPago");
        if (pago != null) return existe("SELECT EXISTS (SELECT 1 FROM pago p JOIN factura f ON f.id_factura=p.id_factura WHERE p.id_pago=? AND f.id_cliente=?)", pago, persona);
        Long ejercicio = valor(registro, "getIdEjercicio");
        if (ejercicio != null) return existe("SELECT EXISTS (SELECT 1 FROM asignacion_rutina ar JOIN contiene_ejercicio ce ON ce.id_rutina=ar.id_rutina WHERE ar.id_cliente=? AND ce.id_ejercicio=?)", persona, ejercicio);
        if (relacionComprobada) return true;
        return java.util.Set.of("Alimento", "Ejercicio", "GrupoMuscular", "TipoMembresia", "ClaseGrupal", "Horario", "IndicadorSalud").contains(tipo);
    }

    private boolean alcanceEntrenador(Object registro, Long persona)
            throws ReflectiveOperationException, SQLException {
        String tipo = registro.getClass().getSimpleName();
        boolean relacionComprobada = false;
        Long entrenador = valor(registro, "getIdEntrenador");
        if (entrenador != null) {
            if (!entrenador.equals(persona)) return false;
            relacionComprobada = true;
        }
        Long rutina = valor(registro, "getIdRutina");
        if (rutina != null) {
            if (!existe("SELECT EXISTS (SELECT 1 FROM rutina WHERE id_rutina=? AND id_entrenador=?)", rutina, persona)) return false;
            relacionComprobada = true;
        }
        Long evaluacion = valor(registro, "getIdEvaluacion");
        if (evaluacion != null) {
            if (!existe("SELECT EXISTS (SELECT 1 FROM evaluacion_fisica WHERE id_evaluacion=? AND id_entrenador=?)", evaluacion, persona)) return false;
            relacionComprobada = true;
        }
        Long cliente = valor(registro, "getIdCliente");
        if (cliente == null && "Cliente".equals(tipo)) cliente = valor(registro, "getIdPersona");
        if (cliente != null && !"AsignacionRutina".equals(tipo)) {
            if (!existe("SELECT EXISTS (SELECT 1 FROM asignacion_rutina ar JOIN rutina r ON r.id_rutina=ar.id_rutina WHERE ar.id_cliente=? AND r.id_entrenador=?)", cliente, persona)) return false;
            relacionComprobada = true;
        }
        if (relacionComprobada) return true;
        if (valor(registro, "getIdEjercicio") != null) return true;
        return java.util.Set.of("Ejercicio", "GrupoMuscular", "TipoEquipo", "Equipo").contains(tipo);
    }

    private boolean alcanceNutricionista(Object registro, Long persona)
            throws ReflectiveOperationException, SQLException {
        String tipo = registro.getClass().getSimpleName();
        boolean relacionComprobada = false;
        Long nutricionista = valor(registro, "getIdNutricionista");
        if (nutricionista != null) {
            if (!nutricionista.equals(persona)) return false;
            relacionComprobada = true;
        }
        Long plan = valor(registro, "getIdPlanNutricional");
        if (plan != null) {
            if (!existe("SELECT EXISTS (SELECT 1 FROM plan_nutricional WHERE id_plan_nutricional=? AND id_nutricionista=?)", plan, persona)) return false;
            relacionComprobada = true;
        }
        Long cliente = valor(registro, "getIdCliente");
        if (cliente == null && "Cliente".equals(tipo)) cliente = valor(registro, "getIdPersona");
        if (cliente != null && !"PlanNutricional".equals(tipo)) {
            if (!existe("SELECT EXISTS (SELECT 1 FROM plan_nutricional WHERE id_cliente=? AND id_nutricionista=?)", cliente, persona)) return false;
            relacionComprobada = true;
        }
        Long evaluacion = valor(registro, "getIdEvaluacion");
        if (evaluacion != null) {
            if (!existe("SELECT EXISTS (SELECT 1 FROM evaluacion_fisica e JOIN plan_nutricional p ON p.id_cliente=e.id_cliente WHERE e.id_evaluacion=? AND p.id_nutricionista=?)", evaluacion, persona)) return false;
            relacionComprobada = true;
        }
        if (relacionComprobada) return true;
        return java.util.Set.of("Alimento", "Alergeno", "IndicadorSalud", "ObjetivoFitness").contains(tipo);
    }

    private boolean existe(String sql, Object... valores) throws SQLException {
        return dao.existe(sql, valores);
    }

    private Long valor(Object registro, String getter) throws ReflectiveOperationException {
        try {
            Method metodo = registro.getClass().getMethod(getter);
            Object resultado = metodo.invoke(registro);
            return resultado instanceof Number numero ? numero.longValue() : null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    public String getMensaje() { return mensaje; }
}
