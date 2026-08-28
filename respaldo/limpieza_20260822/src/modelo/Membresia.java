package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla membresia.
 *
 * @author Usuario
 */
public class Membresia {

    private Long idMembresia;
    private String numeroMembresia;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal costoFinal;
    private String estadoMembresia;
    private String observaciones;
    private Long idCliente;
    private Long idTipoMembresia;

    public Membresia() {
    }


    public Long getIdMembresia() {
        return idMembresia;
    }

    public void setIdMembresia(
            Long idMembresia
    ) {
        this.idMembresia = idMembresia;
    }
    public String getNumeroMembresia() {
        return numeroMembresia;
    }

    public void setNumeroMembresia(
            String numeroMembresia
    ) {
        this.numeroMembresia = numeroMembresia;
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
    public BigDecimal getCostoFinal() {
        return costoFinal;
    }

    public void setCostoFinal(
            BigDecimal costoFinal
    ) {
        this.costoFinal = costoFinal;
    }
    public String getEstadoMembresia() {
        return estadoMembresia;
    }

    public void setEstadoMembresia(
            String estadoMembresia
    ) {
        this.estadoMembresia = estadoMembresia;
    }
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(
            Long idCliente
    ) {
        this.idCliente = idCliente;
    }
    public Long getIdTipoMembresia() {
        return idTipoMembresia;
    }

    public void setIdTipoMembresia(
            Long idTipoMembresia
    ) {
        this.idTipoMembresia = idTipoMembresia;
    }

    @Override
    public String toString() {
        return String.valueOf(
                numeroMembresia
        );
    }
}
