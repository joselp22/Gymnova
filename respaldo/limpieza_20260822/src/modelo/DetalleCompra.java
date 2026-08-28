package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla detalle_compra.
 *
 * @author Usuario
 */
public class DetalleCompra {

    private Long idDetalleCompra;
    private Integer cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal porcentajeDescuento;
    private BigDecimal porcentajeImpuesto;
    private LocalDate fechaElaboracion;
    private LocalDate fechaVencimiento;
    private Long idCompra;
    private Long idProducto;

    public DetalleCompra() {
    }


    public Long getIdDetalleCompra() {
        return idDetalleCompra;
    }

    public void setIdDetalleCompra(
            Long idDetalleCompra
    ) {
        this.idDetalleCompra = idDetalleCompra;
    }
    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(
            Integer cantidad
    ) {
        this.cantidad = cantidad;
    }
    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(
            BigDecimal costoUnitario
    ) {
        this.costoUnitario = costoUnitario;
    }
    public BigDecimal getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(
            BigDecimal porcentajeDescuento
    ) {
        this.porcentajeDescuento = porcentajeDescuento;
    }
    public BigDecimal getPorcentajeImpuesto() {
        return porcentajeImpuesto;
    }

    public void setPorcentajeImpuesto(
            BigDecimal porcentajeImpuesto
    ) {
        this.porcentajeImpuesto = porcentajeImpuesto;
    }
    public LocalDate getFechaElaboracion() {
        return fechaElaboracion;
    }

    public void setFechaElaboracion(
            LocalDate fechaElaboracion
    ) {
        this.fechaElaboracion = fechaElaboracion;
    }
    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(
            LocalDate fechaVencimiento
    ) {
        this.fechaVencimiento = fechaVencimiento;
    }
    public Long getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(
            Long idCompra
    ) {
        this.idCompra = idCompra;
    }
    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(
            Long idProducto
    ) {
        this.idProducto = idProducto;
    }

    @Override
    public String toString() {
        return String.valueOf(
                idDetalleCompra
        );
    }
}
