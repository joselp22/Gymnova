import controlador.PulseControlador;
import java.util.List;
import modelo.AlertaPulse;

/** Prueba de calculo y explicabilidad de GYMNOVA Pulse. */
public class PruebaPulse {
    public static void main(String[] args) {
        PulseControlador controlador = new PulseControlador();
        List<AlertaPulse> alertas = controlador.listar();
        if (alertas.isEmpty()) {
            throw new IllegalStateException(controlador.getMensaje());
        }
        for (AlertaPulse alerta : alertas) {
            if (alerta.getPuntuacion() < 0 || alerta.getPuntuacion() > 100) {
                throw new IllegalStateException("Pulse fuera de rango");
            }
            if (alerta.getNivel() == null || alerta.getExplicacion().isBlank()) {
                throw new IllegalStateException("Pulse sin explicacion");
            }
            System.out.println(alerta.getCodigoCliente() + "|"
                    + alerta.getNombreCliente() + "|" + alerta + "|"
                    + alerta.getExplicacion());
        }
        System.out.println("CLIENTES_EVALUADOS=" + alertas.size());
        System.out.println("PULSE_VALIDO=SI");
    }
}
