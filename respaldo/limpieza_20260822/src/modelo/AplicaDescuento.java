package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla aplica_descuento.
 *
 * @author Usuario
 */
public class AplicaDescuento {

    private LocalDate fechaAplicacion;
    private BigDecimal baseCalculo;
    private String motivoAplicacion;
    private String estadoAplicacion;
    private Long idFactura;
    private Long idDescuento;

    public AplicaDescuento() {
    }


    public LocalDate getFechaAplicacion() {
        return fechaAplicacion;
    }

    public void setFechaAplicacion(
            LocalDate fechaAplicacion
    ) {
        this.fechaAplicacion = fechaAplicacion;
    }
    public BigDecimal getBaseCalculo() {
        return baseCalculo;
    }

    public void setBaseCalculo(
            BigDecimal baseCalculo
    ) {
        this.baseCalculo = baseCalculo;
    }
    public String getMotivoAplicacion() {
        return motivoAplicacion;
    }

    public void setMotivoAplicacion(
            String motivoAplicacion
    ) {
        this.motivoAplicacion = motivoAplicacion;
    }
    public String getEstadoAplicacion() {
        return estadoAplicacion;
    }

    public void setEstadoAplicacion(
            String estadoAplicacion
    ) {
        this.estadoAplicacion = estadoAplicacion;
    }
    public Long getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(
            Long idFactura
    ) {
        this.idFactura = idFactura;
    }
    public Long getIdDescuento() {
        return idDescuento;
    }

    public void setIdDescuento(
            Long idDescuento
    ) {
        this.idDescuento = idDescuento;
    }

    @Override
    public String toString() {
        return String.valueOf(
                motivoAplicacion
        );
    }
}
