package modelo;

import java.time.LocalDate;
/**
 * Modelo para la tabla asignacion_rutina.
 *
 * @author Usuario
 */
public class AsignacionRutina {

    private Long idAsignacion;
    private LocalDate fechaAsignacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estadoAsignacion;
    private String motivoFinalizacion;
    private String observaciones;
    private Long idCliente;
    private Long idRutina;

    public AsignacionRutina() {
    }


    public Long getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(
            Long idAsignacion
    ) {
        this.idAsignacion = idAsignacion;
    }
    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(
            LocalDate fechaAsignacion
    ) {
        this.fechaAsignacion = fechaAsignacion;
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
    public String getEstadoAsignacion() {
        return estadoAsignacion;
    }

    public void setEstadoAsignacion(
            String estadoAsignacion
    ) {
        this.estadoAsignacion = estadoAsignacion;
    }
    public String getMotivoFinalizacion() {
        return motivoFinalizacion;
    }

    public void setMotivoFinalizacion(
            String motivoFinalizacion
    ) {
        this.motivoFinalizacion = motivoFinalizacion;
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
    public Long getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(
            Long idRutina
    ) {
        this.idRutina = idRutina;
    }

    @Override
    public String toString() {
        return String.valueOf(
                estadoAsignacion
        );
    }
}
