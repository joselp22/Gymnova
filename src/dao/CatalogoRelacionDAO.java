package dao;

import conexion.ConexionPostgreSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.OpcionRelacion;

/** Carga nombres legibles para las llaves foraneas del editor comun. */
public class CatalogoRelacionDAO {

    private static final Map<String, String> CONSULTAS = new LinkedHashMap<>();

    static {
        CONSULTAS.put("idPersona", "SELECT id_persona, cedula||' - '||nombres||' '||apellidos FROM persona WHERE estado ORDER BY nombres,apellidos");
        CONSULTAS.put("idCliente", "SELECT c.id_persona, c.codigo_cliente||' - '||p.nombres||' '||p.apellidos FROM cliente c JOIN persona p ON p.id_persona=c.id_persona WHERE c.estado_cliente ORDER BY p.nombres,p.apellidos");
        CONSULTAS.put("idEmpleado", "SELECT e.id_persona, e.codigo_empleado||' - '||p.nombres||' '||p.apellidos FROM empleado e JOIN persona p ON p.id_persona=e.id_persona WHERE e.estado_empleado ORDER BY p.nombres,p.apellidos");
        CONSULTAS.put("idEntrenador", "SELECT e.id_persona, p.nombres||' '||p.apellidos||' - Entrenador' FROM entrenador e JOIN persona p ON p.id_persona=e.id_persona WHERE e.estado_entrenador ORDER BY p.nombres,p.apellidos");
        CONSULTAS.put("idNutricionista", "SELECT n.id_persona, p.nombres||' '||p.apellidos||' - '||n.numero_licencia FROM nutricionista n JOIN persona p ON p.id_persona=n.id_persona WHERE n.estado_licencia='ACTIVA' ORDER BY p.nombres,p.apellidos");
        CONSULTAS.put("idRol", "SELECT id_rol,nombre_rol FROM rol WHERE estado_rol ORDER BY nombre_rol");
        CONSULTAS.put("idPermiso", "SELECT id_permiso,modulo||' - '||accion||' - '||nombre_permiso FROM permiso WHERE estado_permiso ORDER BY modulo,accion");
        // idEspecialidad retirado con el catalogo especialidad.
        CONSULTAS.put("idTipoMembresia", "SELECT id_tipo_membresia,nombre||' - $'||precio_base FROM tipo_membresia WHERE estado_tipo ORDER BY nombre");
        CONSULTAS.put("idMembresia", "SELECT m.id_membresia,m.numero_membresia||' - '||p.nombres||' '||p.apellidos FROM membresia m JOIN cliente c ON c.id_persona=m.id_cliente JOIN persona p ON p.id_persona=c.id_persona ORDER BY m.numero_membresia");
        CONSULTAS.put("idClase", "SELECT id_clase,nombre_clase FROM clase_grupal WHERE estado_clase ORDER BY nombre_clase");
        CONSULTAS.put("idRutina", "SELECT id_rutina,nombre_rutina||' - '||nivel FROM rutina WHERE estado_rutina='ACTIVA' ORDER BY nombre_rutina");
        CONSULTAS.put("idEjercicio", "SELECT id_ejercicio,nombre_ejercicio FROM ejercicio ORDER BY nombre_ejercicio");
        CONSULTAS.put("idEvaluacion", "SELECT e.id_evaluacion,e.codigo_evaluacion||' - '||p.nombres||' '||p.apellidos FROM evaluacion_fisica e JOIN persona p ON p.id_persona=e.id_cliente WHERE e.estado_evaluacion<>'ANULADA' ORDER BY e.fecha_evaluacion DESC");
        CONSULTAS.put("idIndicador", "SELECT id_indicador,nombre_indicador||COALESCE(' ('||unidad_medida||')','') FROM indicador_salud WHERE estado_indicador ORDER BY nombre_indicador");
        CONSULTAS.put("idPlanNutricional", "SELECT pn.id_plan_nutricional,pn.codigo_plan||' - '||p.nombres||' '||p.apellidos FROM plan_nutricional pn JOIN persona p ON p.id_persona=pn.id_cliente WHERE pn.estado_plan='ACTIVO' ORDER BY pn.fecha_creacion DESC");
        CONSULTAS.put("idAlimento", "SELECT id_alimento,nombre_alimento FROM alimento ORDER BY nombre_alimento");
        CONSULTAS.put("idMetodoPago", "SELECT id_metodo_pago,nombre_metodo FROM metodo_pago WHERE estado_metodo ORDER BY nombre_metodo");
        CONSULTAS.put("idFactura", "SELECT f.id_factura,f.numero_factura||' - '||p.nombres||' '||p.apellidos FROM factura f JOIN persona p ON p.id_persona=f.id_cliente WHERE f.estado_factura<>'ANULADA' ORDER BY f.fecha_emision DESC");
        CONSULTAS.put("idPago", "SELECT id_pago,codigo_pago||' - $'||monto_pago FROM pago WHERE estado_pago<>'ANULADO' ORDER BY fecha_hora_pago DESC");
        // Inventario y descuentos retirados del alcance.
        // idTipoMantenimiento retirado.
    }

    public boolean esRelacion(String campo) {
        return CONSULTAS.containsKey(campo);
    }

    public List<OpcionRelacion> listar(String campo) throws SQLException {
        String sql = CONSULTAS.get(campo);
        List<OpcionRelacion> opciones = new ArrayList<>();
        if (sql == null) {
            return opciones;
        }
        try (Connection conexion = ConexionPostgreSQL.getConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                opciones.add(new OpcionRelacion(
                        resultado.getObject(1), resultado.getString(2)));
            }
        }
        return opciones;
    }

    public List<OpcionRelacion> listarParaSesion(
            String campo, String nombreRol, Long idPersona) throws SQLException {
        String rol = nombreRol == null ? "" : nombreRol.trim().toUpperCase();
        if (idPersona == null || "ADMINISTRADOR".equals(rol)
                || "RECEPCIONISTA".equals(rol) || rol.isBlank()) {
            return listar(campo);
        }
        String sql = consultaRestringida(campo, rol);
        if (sql == null) {
            return listar(campo);
        }
        List<OpcionRelacion> opciones = new ArrayList<>();
        try (Connection conexion = ConexionPostgreSQL.getConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setObject(1, idPersona);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    opciones.add(new OpcionRelacion(
                            resultado.getObject(1), resultado.getString(2)));
                }
            }
        }
        return opciones;
    }

    private String consultaRestringida(String campo, String rol) {
        if ("CLIENTE".equals(rol)) {
            return switch (campo) {
                case "idCliente" -> "SELECT c.id_persona,c.codigo_cliente||' - '||p.nombres||' '||p.apellidos FROM cliente c JOIN persona p ON p.id_persona=c.id_persona WHERE c.id_persona=?";
                case "idRutina" -> "SELECT DISTINCT r.id_rutina,r.nombre_rutina||' - '||r.nivel FROM rutina r JOIN asignacion_rutina a ON a.id_rutina=r.id_rutina WHERE a.id_cliente=?";
                case "idEvaluacion" -> "SELECT e.id_evaluacion,e.codigo_evaluacion||' - '||p.nombres||' '||p.apellidos FROM evaluacion_fisica e JOIN persona p ON p.id_persona=e.id_cliente WHERE e.id_cliente=?";
                case "idPlanNutricional" -> "SELECT pn.id_plan_nutricional,pn.codigo_plan||' - '||p.nombres||' '||p.apellidos FROM plan_nutricional pn JOIN persona p ON p.id_persona=pn.id_cliente WHERE pn.id_cliente=?";
                case "idMembresia" -> "SELECT m.id_membresia,m.numero_membresia FROM membresia m WHERE m.id_cliente=?";
                case "idFactura" -> "SELECT f.id_factura,f.numero_factura FROM factura f WHERE f.id_cliente=?";
                case "idPago" -> "SELECT p.id_pago,p.codigo_pago||' - $'||p.monto_pago FROM pago p JOIN factura f ON f.id_factura=p.id_factura WHERE f.id_cliente=?";
                default -> null;
            };
        }
        if ("ENTRENADOR".equals(rol)) {
            return switch (campo) {
                case "idEntrenador" -> "SELECT e.id_persona,p.nombres||' '||p.apellidos||' - Entrenador' FROM entrenador e JOIN persona p ON p.id_persona=e.id_persona WHERE e.id_persona=?";
                case "idCliente" -> "SELECT DISTINCT c.id_persona,c.codigo_cliente||' - '||p.nombres||' '||p.apellidos FROM cliente c JOIN persona p ON p.id_persona=c.id_persona WHERE c.estado_cliente AND (EXISTS (SELECT 1 FROM asignacion_rutina ar JOIN rutina r ON r.id_rutina=ar.id_rutina WHERE ar.id_cliente=c.id_persona AND r.id_entrenador=? AND ar.estado_asignacion IN ('ACTIVA','PROGRAMADA','PAUSADA')) OR NOT EXISTS (SELECT 1 FROM asignacion_rutina ar0 WHERE ar0.id_cliente=c.id_persona AND ar0.estado_asignacion IN ('ACTIVA','PROGRAMADA','PAUSADA'))) ORDER BY 2";
                case "idRutina" -> "SELECT id_rutina,nombre_rutina||' - '||nivel FROM rutina WHERE id_entrenador=?";
                case "idEvaluacion" -> "SELECT e.id_evaluacion,e.codigo_evaluacion||' - '||p.nombres||' '||p.apellidos FROM evaluacion_fisica e JOIN persona p ON p.id_persona=e.id_cliente WHERE e.id_entrenador=?";
                default -> null;
            };
        }
        if ("NUTRICIONISTA".equals(rol)) {
            return switch (campo) {
                case "idNutricionista" -> "SELECT n.id_persona,p.nombres||' '||p.apellidos||' - '||n.numero_licencia FROM nutricionista n JOIN persona p ON p.id_persona=n.id_persona WHERE n.id_persona=?";
                case "idCliente" -> "SELECT DISTINCT c.id_persona,c.codigo_cliente||' - '||p.nombres||' '||p.apellidos FROM cliente c JOIN persona p ON p.id_persona=c.id_persona WHERE c.estado_cliente AND (EXISTS (SELECT 1 FROM plan_nutricional pn WHERE pn.id_cliente=c.id_persona AND pn.id_nutricionista=? AND pn.estado_plan='ACTIVO') OR NOT EXISTS (SELECT 1 FROM plan_nutricional pn0 WHERE pn0.id_cliente=c.id_persona AND pn0.estado_plan='ACTIVO')) ORDER BY 2";
                case "idPlanNutricional" -> "SELECT pn.id_plan_nutricional,pn.codigo_plan||' - '||p.nombres||' '||p.apellidos FROM plan_nutricional pn JOIN persona p ON p.id_persona=pn.id_cliente WHERE pn.id_nutricionista=?";
                case "idEvaluacion" -> "SELECT DISTINCT e.id_evaluacion,e.codigo_evaluacion||' - '||p.nombres||' '||p.apellidos FROM evaluacion_fisica e JOIN persona p ON p.id_persona=e.id_cliente JOIN plan_nutricional pn ON pn.id_cliente=e.id_cliente WHERE pn.id_nutricionista=?";
                default -> null;
            };
        }
        return null;
    }
}
