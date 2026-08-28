import controlador.FacturaControlador;
import dao.FacturaDAO;
import dao.ClienteDAO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import modelo.Cliente;
import modelo.Factura;

/** Prueba transitoria del numero automatico de factura; limpia lo creado. */
public class PruebaNumeroFactura {
    public static void main(String[] args) throws Exception {
        List<Cliente> clientes = new ClienteDAO().listar("");
        if (clientes.isEmpty()) throw new IllegalStateException("Sin clientes");
        Factura factura = new Factura();
        factura.setFechaEmision(LocalDate.now());
        factura.setTotalDescuento(BigDecimal.ZERO);
        factura.setImpuesto(BigDecimal.ZERO);
        factura.setEstadoFactura("EMITIDA");
        factura.setIdCliente(clientes.get(0).getIdPersona());

        FacturaControlador controlador = new FacturaControlador();
        if (!controlador.registrar(factura)) {
            throw new IllegalStateException(controlador.getMensaje());
        }
        if (!factura.getNumeroFactura().matches("FAC-[0-9]{5}")) {
            throw new IllegalStateException("Numero invalido: "
                    + factura.getNumeroFactura());
        }
        Factura creada = new FacturaDAO().listar(factura.getNumeroFactura())
                .stream().filter(f -> factura.getNumeroFactura()
                .equals(f.getNumeroFactura())).findFirst().orElseThrow();
        if (!new FacturaDAO().eliminarDefinitivamente(creada.getIdFactura())) {
            throw new IllegalStateException("No se limpio la factura transitoria");
        }
        System.out.println("NUMERO_AUTOMATICO=" + factura.getNumeroFactura());
        System.out.println("LIMPIEZA=SI");
    }
}
