package pruebas;

import controlador.ConsultaModuloControlador;

/** Prueba de lectura real por rol, sin modificar datos de PostgreSQL. */
public class PruebaAlcanceRoles {

    public static void main(String[] args) {
        probar("CLIENTE", 2L, new String[]{
            "MembresiaControlador", "ReservaControlador",
            "AsignacionRutinaControlador", "ProgresoRutinaControlador",
            "EvaluacionFisicaControlador", "PlanNutricionalControlador"
        });
        probar("ENTRENADOR", 4L, new String[]{
            "ClienteControlador", "RutinaControlador",
            "AsignacionRutinaControlador", "ContieneEjercicioControlador",
            "ProgresoRutinaControlador", "EvaluacionFisicaControlador",
            "MedicionCorporalControlador"
        });
        probar("NUTRICIONISTA", 6L, new String[]{
            "ClienteControlador", "PlanNutricionalControlador",
            "IncluyeAlimentoControlador", "AlimentoControlador",
            "RecomendacionControlador"
        });
    }

    private static void probar(String rol, Long idPersona, String[] controladores) {
        ConsultaModuloControlador consulta = new ConsultaModuloControlador();
        System.out.println("\n" + rol + " (persona " + idPersona + ")");
        for (String controlador : controladores) {
            int cantidad = consulta.listarParaSesion(
                    controlador, "", rol, idPersona).size();
            System.out.printf("  %-38s %d%n", controlador, cantidad);
            if (!consulta.getMensaje().isBlank()) {
                System.out.println("    mensaje: " + consulta.getMensaje());
            }
        }
    }
}
