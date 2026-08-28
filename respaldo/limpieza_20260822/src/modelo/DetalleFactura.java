package modelo;

import java.math.BigDecimal;
/**
 * Modelo para la tabla detalle_factura.
 *
 * @author Usuario
 */
public class DetalleFactura {

    private Long idDetalleFactura;
    private String tipoConcepto;
    private String codigoReferencia;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal porcentajeImpuesto;
    private Long idFactura;

    public DetalleFactura() {
    }


    public Long getIdDetalleFactura() {
        return idDetalleFactura;
    }

    public void setIdDetalleFactura(
            Long idDetalleFactura
    ) {
        this.idDetalleFactura = idDetalleFactura;
    }
    public String getTipoConcepto() {
        return tipoConcepto;
    }

    public void setTipoConcepto(
            String tipoConcepto
    ) {
        this.tipoConcepto = tipoConcepto;
    }
    public String getCodigoReferencia() {
        return codigoReferencia;
    }

    public void setCodigoReferencia(
            String codigoReferencia
    ) {
        this.codigoReferencia = codigoReferencia;
    }
    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(
            Integer cantidad
    ) {
        this.cantidad = cantidad;
    }
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(
            BigDecimal precioUnitario
    ) {
        this.precioUnitario = precioUnitario;
    }
    public BigDecimal getPorcentajeImpuesto() {
        return porcentajeImpuesto;
    }

    public void setPorcentajeImpuesto(
            BigDecimal porcentajeImpuesto
    ) {
        this.porcentajeImpuesto = porcentajeImpuesto;
    }
    public Long getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(
            Long idFactura
    ) {
        this.idFactura = idFactura;
    }

    @Override
    public String toString() {
        return String.valueOf(
                tipoConcepto
        );
    }
}
