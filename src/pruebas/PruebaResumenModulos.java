package pruebas;

import controlador.ResumenModuloControlador;

/** Prueba integral de solo lectura para las tarjetas conectadas a PostgreSQL. */
public class PruebaResumenModulos {
    public static void main(String[] args) {
        ResumenModuloControlador c=new ResumenModuloControlador();
        verificar("Perfil cliente",c.perfil(2L),c.getMensaje());
        verificar("Salud cliente",c.salud(2L),c.getMensaje());
        verificar("Historial salud",c.historialSalud(2L,3),c.getMensaje());
        verificar("Plan nutricional",c.plan(2L),c.getMensaje());
        verificar("Comidas",c.comidas(2L,4),c.getMensaje());
        verificar("Membresia",c.membresia(2L),c.getMensaje());
        verificar("Beneficios",c.beneficios(2L),c.getMensaje());
        verificar("Reservas",c.reservas(2L,4),c.getMensaje());
        verificar("Acceso",c.acceso(2L),c.getMensaje());
        verificar("Clientes admin",c.clientes("Administrador",null,3),c.getMensaje());
        verificar("Clientes entrenador",c.clientes("Entrenador",4L,3),c.getMensaje());
        verificar("Clientes nutricionista",c.clientes("Nutricionista",6L,3),c.getMensaje());
        verificar("Reservas de recepcion",c.reservasOperacion(6),c.getMensaje());
        verificar("Membresias de recepcion",c.membresiasOperacion(6),c.getMensaje());
        java.util.Map<String,Object> rutina=c.rutina("Entrenador",4L);
        verificar("Rutina entrenador",rutina,c.getMensaje());
        verificar("Ejercicios rutina",c.ejercicios(((Number)rutina.get("id_rutina")).longValue(),8),c.getMensaje());
        verificar("Finanzas",c.finanzas(),c.getMensaje());
        verificar("Movimientos",c.movimientos(4),c.getMensaje());
        for(String rol:new String[]{"Administrador","Recepcionista","Entrenador","Nutricionista","Cliente"})
            verificar("Actividad "+rol,c.actividad(rol,id(rol)),c.getMensaje());
        System.out.println("RESUMENES_MODULOS_OK");
    }
    private static Long id(String rol){return switch(rol){case "Recepcionista"->1L;case "Entrenador"->4L;case "Nutricionista"->6L;case "Cliente"->2L;default->null;};}
    private static void verificar(String nombre,Object resultado,String mensaje){
        if(mensaje!=null&&!mensaje.isBlank()) throw new IllegalStateException(nombre+": "+mensaje);
        System.out.println("OK "+nombre+" -> "+resultado);
    }
}
