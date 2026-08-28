package controlador;

import dao.ResumenModuloDAO;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Protege a la vista de errores SQL y centraliza sus consultas de resumen. */
public class ResumenModuloControlador {
    private final ResumenModuloDAO dao = new ResumenModuloDAO();
    private String mensaje = "";

    @FunctionalInterface private interface Consulta<T> { T ejecutar() throws SQLException; }
    private <T> T ejecutar(Consulta<T> consulta, T vacio) {
        mensaje = "";
        try { return consulta.ejecutar(); }
        catch (SQLException ex) { mensaje = "No fue posible consultar PostgreSQL: " + ex.getMessage(); return vacio; }
    }

    public Map<String,Object> perfil(Long id){ return ejecutar(() -> dao.perfilCliente(id), Collections.emptyMap()); }
    public Map<String,Object> salud(Long id){ return ejecutar(() -> dao.saludCliente(id), Collections.emptyMap()); }
    public List<Map<String,Object>> historialSalud(Long id,int n){ return ejecutar(() -> dao.historialSalud(id,n), Collections.emptyList()); }
    public Map<String,Object> plan(Long id){ return ejecutar(() -> dao.planNutricional(id), Collections.emptyMap()); }
    public List<Map<String,Object>> comidas(Long id,int n){ return ejecutar(() -> dao.comidasCliente(id,n), Collections.emptyList()); }
    public Map<String,Object> membresia(Long id){ return ejecutar(() -> dao.membresiaCliente(id), Collections.emptyMap()); }
    public List<Map<String,Object>> beneficios(Long id){ return ejecutar(() -> dao.beneficiosMembresia(id), Collections.emptyList()); }
    public List<Map<String,Object>> reservas(Long id,int n){ return ejecutar(() -> dao.reservasCliente(id,n), Collections.emptyList()); }
    public Map<String,Object> acceso(Long id){ return ejecutar(() -> dao.accesoCliente(id), Collections.emptyMap()); }
    public List<Map<String,Object>> clientes(String rol,Long id,int n){ return ejecutar(() -> dao.clientesPorRol(rol,id,n), Collections.emptyList()); }
    public List<Map<String,Object>> reservasOperacion(int n){ return ejecutar(() -> dao.reservasOperacion(n), Collections.emptyList()); }
    public List<Map<String,Object>> membresiasOperacion(int n){ return ejecutar(() -> dao.membresiasOperacion(n), Collections.emptyList()); }
    public Map<String,Object> rutina(String rol,Long id){ return ejecutar(() -> dao.rutinaVisible(rol,id), Collections.emptyMap()); }
    public List<Map<String,Object>> ejercicios(Long id,int n){ return ejecutar(() -> dao.ejerciciosRutina(id,n), Collections.emptyList()); }
    public Map<String,Object> finanzas(){ return ejecutar(dao::resumenFinanciero, Collections.emptyMap()); }
    public List<Map<String,Object>> movimientos(int n){ return ejecutar(() -> dao.movimientosFinancieros(n), Collections.emptyList()); }
    public List<Map<String,Object>> actividad(String rol,Long id){ return ejecutar(() -> dao.actividadSemanal(rol,id), Collections.emptyList()); }
    public String getMensaje(){ return mensaje; }
}
