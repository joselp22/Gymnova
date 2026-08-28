package modelo;

import java.time.LocalDate;
/**
 * Modelo para la tabla congelacion.
 *
 * @author Usuario
 */
public class Congelacion {

    private Long idCongelacion;
    private LocalDate fechaSolicitud;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String motivo;
    private String observaciones;
    private String estadoCongelacion;
    private Long idMembresia;

    public Congelacion() {
    }


    public Long getIdCongelacion() {
        return idCongelacion;
    }

    public void setIdCongelacion(
            Long idCongelacion
    ) {
        this.idCongelacion = idCongelacion;
    }
    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(
            LocalDate fechaSolicitud
    ) {
        this.fechaSolicitud = fechaSolicitud;
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
    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(
            String motivo
    ) {
        this.motivo = motivo;
    }
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }
    public String getEstadoCongelacion() {
        return estadoCongelacion;
    }

    public void setEstadoCongelacion(
            String estadoCongelacion
    ) {
        this.estadoCongelacion = estadoCongelacion;
    }
    public Long getIdMembresia() {
        return idMembresia;
    }

    public void setIdMembresia(
            Long idMembresia
    ) {
        this.idMembresia = idMembresia;
    }

    @Override
    public String toString() {
        return String.valueOf(
                motivo
        );
    }
}
