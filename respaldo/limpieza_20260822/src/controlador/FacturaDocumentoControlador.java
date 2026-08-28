package controlador;

import dao.DetalleFacturaDAO;
import dao.FacturaDAO;
import dao.PagoDAO;
import dao.PersonaDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.sql.SQLException;
import modelo.Factura;
import modelo.FacturaDocumento;
import modelo.Persona;
import utilidades.DocumentoFacturaPDF;

/** Prepara, valida y exporta una factura consultada desde PostgreSQL. */
public class FacturaDocumentoControlador {

    private final FacturaDAO facturaDAO = new FacturaDAO();
    private final DetalleFacturaDAO detalleDAO = new DetalleFacturaDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final PersonaDAO personaDAO = new PersonaDAO();
    private String mensaje = "";

    public FacturaDocumento cargar(Long idFactura) {
        mensaje = "";
        if (idFactura == null) {
            mensaje = "Seleccione una factura, detalle, pago o comprobante.";
            return null;
        }
        try {
            Factura factura = facturaDAO.buscar(idFactura);
            if (factura == null) {
                mensaje = "La factura seleccionada ya no existe.";
                return null;
            }
            Persona cliente = personaDAO.buscarPorId(factura.getIdCliente())
                    .orElse(null);
            if (cliente == null) {
                mensaje = "La factura no posee un cliente valido.";
                return null;
            }
            FacturaDocumento documento = new FacturaDocumento();
            documento.setFactura(factura);
            documento.setCliente(cliente);
            documento.setDetalles(detalleDAO.listarPorIdFactura(idFactura));
            documento.setPagos(pagoDAO.listarPorIdFactura(idFactura));
            return documento;
        } catch (SQLException ex) {
            mensaje = "No fue posible consultar la factura: " + ex.getMessage();
            return null;
        }
    }

    public Path exportar(Long idFactura, Path destino) {
        FacturaDocumento documento = cargar(idFactura);
        if (documento == null) return null;
        try {
            DocumentoFacturaPDF.generar(documento, destino);
            mensaje = "Factura PDF generada correctamente.";
            return destino;
        } catch (Exception ex) {
            mensaje = "No fue posible generar el PDF: " + ex.getMessage();
            return null;
        }
    }

    public BigDecimal calcularTotal(FacturaDocumento documento) {
        BigDecimal subtotal = documento.getDetalles().stream()
                .map(d -> d.getPrecioUnitario().multiply(
                        BigDecimal.valueOf(d.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuento = documento.getFactura().getTotalDescuento() == null
                ? BigDecimal.ZERO : documento.getFactura().getTotalDescuento();
        BigDecimal impuesto = documento.getFactura().getImpuesto() == null
                ? BigDecimal.ZERO : documento.getFactura().getImpuesto();
        return subtotal.subtract(descuento).add(impuesto)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public String getMensaje() { return mensaje; }
}
