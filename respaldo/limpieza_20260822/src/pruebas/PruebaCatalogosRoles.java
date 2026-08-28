package pruebas;

import dao.CatalogoRelacionDAO;

/** Comprueba que los combos de FK presentan opciones validas para cada rol. */
public class PruebaCatalogosRoles {

    public static void main(String[] args) throws Exception {
        CatalogoRelacionDAO dao = new CatalogoRelacionDAO();
        probar(dao, "CLIENTE", 2L,
                new String[]{"idCliente", "idRutina", "idEvaluacion",
                    "idPlanNutricional", "idMembresia", "idFactura"});
        probar(dao, "ENTRENADOR", 4L,
                new String[]{"idEntrenador", "idCliente", "idRutina",
                    "idEvaluacion", "idEjercicio"});
        probar(dao, "NUTRICIONISTA", 6L,
                new String[]{"idNutricionista", "idCliente",
                    "idPlanNutricional", "idEvaluacion", "idAlimento"});
    }

    private static void probar(CatalogoRelacionDAO dao, String rol,
            Long persona, String[] campos) throws Exception {
        System.out.println("\nCOMBOS " + rol);
        for (String campo : campos) {
            int cantidad = dao.listarParaSesion(campo, rol, persona).size();
            System.out.printf("  %-22s %d opciones%n", campo, cantidad);
            if (cantidad == 0) {
                throw new IllegalStateException(
                        "El selector " + campo + " quedo sin opciones para " + rol);
            }
        }
    }
}
