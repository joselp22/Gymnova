package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * Modelo para la tabla pago.
 *
 * @author Usuario
 */
public class Pago {

    private Long idPago;
    private BigDecimal montoRecibido;
    private BigDecimal montoPago;
    private String codigoPago;
    private LocalDateTime fechaHoraPago;
    private String estadoPago;
    private String referenciaTransaccion;
    private Long idFactura;
    private Long idMetodoPago;

    public Pago() {
    }


    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(
            Long idPago
    ) {
        this.idPago = idPago;
    }
    public BigDecimal getMontoRecibido() {
        return montoRecibido;
    }

    public void setMontoRecibido(
            BigDecimal montoRecibido
    ) {
        this.montoRecibido = montoRecibido;
    }
    public BigDecimal getMontoPago() {
        return montoPago;
    }

    public void setMontoPago(
            BigDecimal montoPago
    ) {
        this.montoPago = montoPago;
    }
    public String getCodigoPago() {
        return codigoPago;
    }

    public void setCodigoPago(
            String codigoPago
    ) {
        this.codigoPago = codigoPago;
    }
    public LocalDateTime getFechaHoraPago() {
        return fechaHoraPago;
    }

    public void setFechaHoraPago(
            LocalDateTime fechaHoraPago
    ) {
        this.fechaHoraPago = fechaHoraPago;
    }
    public String getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(
            String estadoPago
    ) {
        this.estadoPago = estadoPago;
    }
    public String getReferenciaTransaccion() {
        return referenciaTransaccion;
    }

    public void setReferenciaTransaccion(
            String referenciaTransaccion
    ) {
        this.referenciaTransaccion = referenciaTransaccion;
    }
    public Long getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(
            Long idFactura
    ) {
        this.idFactura = idFactura;
    }
    public Long getIdMetodoPago() {
        return idMetodoPago;
    }

    public void setIdMetodoPago(
            Long idMetodoPago
    ) {
        this.idMetodoPago = idMetodoPago;
    }

    @Override
    public String toString() {
        return String.valueOf(
                codigoPago
        );
    }
}
