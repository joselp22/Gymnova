package modelo;

import java.util.ArrayList;
import java.util.List;

/** Datos completos necesarios para representar una factura fuera de la BD. */
public class FacturaDocumento {

    private Factura factura;
    private Persona cliente;
    private List<DetalleFactura> detalles = new ArrayList<>();
    private List<Pago> pagos = new ArrayList<>();

    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }
    public Persona getCliente() { return cliente; }
    public void setCliente(Persona cliente) { this.cliente = cliente; }
    public List<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFactura> detalles) {
        this.detalles = detalles == null ? new ArrayList<>() : detalles;
    }
    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) {
        this.pagos = pagos == null ? new ArrayList<>() : pagos;
    }
}
