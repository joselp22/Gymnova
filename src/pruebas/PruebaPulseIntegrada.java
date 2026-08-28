package pruebas;

import controlador.PulseControlador;
import java.util.List;
import modelo.AlertaPulse;

/** Verifica que el indicador innovador use datos reales y valores validos. */
public class PruebaPulseIntegrada {
    public static void main(String[] args){
        PulseControlador c=new PulseControlador();
        List<AlertaPulse> alertas=c.listar();
        if(alertas.isEmpty()) throw new IllegalStateException(c.getMensaje());
        for(AlertaPulse a:alertas) if(a.getPuntuacion()<0||a.getPuntuacion()>100||a.getNivel()==null||a.getExplicacion().isBlank())
            throw new IllegalStateException("Alerta Pulse invalida para "+a.getCodigoCliente());
        System.out.println("PULSE_OK_CLIENTES="+alertas.size());
    }
}
