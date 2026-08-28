package modelo;

import java.time.LocalDate;
/**
 * Modelo para la tabla recomendacion.
 *
 * @author Usuario
 */
public class Recomendacion {

    private Long idRecomendacion;
    private LocalDate fechaRecomendacion;
    private String tipoRecomendacion;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estadoRecomendacion;
    private Long idEvaluacion;

    public Recomendacion() {
    }


    public Long getIdRecomendacion() {
        return idRecomendacion;
    }

    public void setIdRecomendacion(
            Long idRecomendacion
    ) {
        this.idRecomendacion = idRecomendacion;
    }
    public LocalDate getFechaRecomendacion() {
        return fechaRecomendacion;
    }

    public void setFechaRecomendacion(
            LocalDate fechaRecomendacion
    ) {
        this.fechaRecomendacion = fechaRecomendacion;
    }
    public String getTipoRecomendacion() {
        return tipoRecomendacion;
    }

    public void setTipoRecomendacion(
            String tipoRecomendacion
    ) {
        this.tipoRecomendacion = tipoRecomendacion;
    }
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(
            String titulo
    ) {
        this.titulo = titulo;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(
            String prioridad
    ) {
        this.prioridad = prioridad;
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
    public String getEstadoRecomendacion() {
        return estadoRecomendacion;
    }

    public void setEstadoRecomendacion(
            String estadoRecomendacion
    ) {
        this.estadoRecomendacion = estadoRecomendacion;
    }
    public Long getIdEvaluacion() {
        return idEvaluacion;
    }

    public void setIdEvaluacion(
            Long idEvaluacion
    ) {
        this.idEvaluacion = idEvaluacion;
    }

    @Override
    public String toString() {
        return String.valueOf(
                titulo
        );
    }
}
