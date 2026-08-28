package utilidades;

import dao.MembresiaDAO;
import java.sql.SQLException;

/**
 * Vigila la caducidad de las membresias. Cada vez que se ejecuta, marca
 * como VENCIDA cualquier membresia todavia en estado ACTIVA cuya
 * fecha_fin ya expiro segun la duracion de su plan (30 dias para la
 * Mensual, 90 para la Trimestral, 365 para la Anual). No borra nada:
 * la fila queda con estado 'VENCIDA' para poder auditar.
 *
 * Se llama:
 *   - Al arrancar la aplicacion (GymnovaApp.main).
 *   - Antes de validar un login de cliente (LoginControlador).
 *
 * Fallar silenciosamente esta bien: si la BD no responde, el resto del
 * sistema decide que hacer y el filtro de acceso sigue funcionando
 * porque tambien compara fecha_fin en tiempo real.
 */
public final class VerificadorMembresias {

    private static final MembresiaDAO MEMBRESIA_DAO = new MembresiaDAO();

    private VerificadorMembresias() {
    }

    /**
     * Ejecuta la verificacion. Devuelve la cantidad de membresias que
     * pasaron de ACTIVA a VENCIDA en esta pasada, o 0 si algo fallo.
     */
    public static int ejecutar() {
        try {
            int vencidas = MEMBRESIA_DAO.marcarVencidas();
            int activadas = MEMBRESIA_DAO.activarPendientesVigentes();
            if (vencidas > 0) {
                Auditoria.exito("MEMBRESIAS", "VENCIMIENTO",
                        "Se marcaron " + vencidas + " membresias como VENCIDA "
                        + "por expiracion de fecha_fin.");
            }
            if (activadas > 0) {
                Auditoria.exito("MEMBRESIAS", "ACTIVAR_RENOVACIONES",
                        "Se activaron " + activadas
                        + " renovaciones cuya fecha de inicio ya llego.");
            }
            return vencidas + activadas;
        } catch (SQLException ex) {
            Auditoria.fallo("MEMBRESIAS", "VENCIMIENTO",
                    "No se pudieron marcar las membresias vencidas.",
                    ex.getMessage());
            return 0;
        }
    }
}
