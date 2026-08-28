package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla descuento.
 *
 * @author Usuario
 */
public class Descuento {

    private Long idDescuento;
    private String codigoDescuento;
    private String nombreDescuento;
    private String descripcion;
    private String tipoDescuento;
    private BigDecimal valorDescuento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public Descuento() {
    }


    public Long getIdDescuento() {
        return idDescuento;
    }

    public void setIdDescuento(
            Long idDescuento
    ) {
        this.idDescuento = idDescuento;
    }
    public String getCodigoDescuento() {
        return codigoDescuento;
    }

    public void setCodigoDescuento(
            String codigoDescuento
    ) {
        this.codigoDescuento = codigoDescuento;
    }
    public String getNombreDescuento() {
        return nombreDescuento;
    }

    public void setNombreDescuento(
            String nombreDescuento
    ) {
        this.nombreDescuento = nombreDescuento;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public String getTipoDescuento() {
        return tipoDescuento;
    }

    public void setTipoDescuento(
            String tipoDescuento
    ) {
        this.tipoDescuento = tipoDescuento;
    }
    public BigDecimal getValorDescuento() {
        return valorDescuento;
    }

    public void setValorDescuento(
            BigDecimal valorDescuento
    ) {
        this.valorDescuento = valorDescuento;
    }
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(
            LocalDate fechaInicio
    ) {
        this.fechaInicio = fechaInicio;
    }
    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(
            LocalDate fechaFin
    ) {
        this.fechaFin = fechaFin;
    }

    @Override
    public String toString() {
        return String.valueOf(
                codigoDescuento
        );
    }
}
