package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla factura.
 *
 * @author Usuario
 */
public class Factura {

    private Long idFactura;
    private String numeroFactura;
    private LocalDate fechaEmision;
    private BigDecimal totalDescuento;
    private BigDecimal impuesto;
    private String estadoFactura;
    private Long idCliente;

    public Factura() {
    }


    public Long getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(
            Long idFactura
    ) {
        this.idFactura = idFactura;
    }
    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(
            String numeroFactura
    ) {
        this.numeroFactura = numeroFactura;
    }
    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(
            LocalDate fechaEmision
    ) {
        this.fechaEmision = fechaEmision;
    }
    public BigDecimal getTotalDescuento() {
        return totalDescuento;
    }

    public void setTotalDescuento(
            BigDecimal totalDescuento
    ) {
        this.totalDescuento = totalDescuento;
    }
    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(
            BigDecimal impuesto
    ) {
        this.impuesto = impuesto;
    }
    public String getEstadoFactura() {
        return estadoFactura;
    }

    public void setEstadoFactura(
            String estadoFactura
    ) {
        this.estadoFactura = estadoFactura;
    }
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(
            Long idCliente
    ) {
        this.idCliente = idCliente;
    }

    @Override
    public String toString() {
        return String.valueOf(
                numeroFactura
        );
    }
}
