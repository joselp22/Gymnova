package modelo;

import java.math.BigDecimal;

/**
 * Modelo para la tabla trabaja.
 *
 * @author Usuario
 */
public class Trabaja {

    private Long idEjercicio;
    private Long idGrupoMuscular;
    private String tipoParticipacion;
    private BigDecimal porcentajeEstimulacion;
    private String observaciones;

    public Trabaja() {
    }

    public Long getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(
            Long idEjercicio
    ) {
        this.idEjercicio = idEjercicio;
    }

    public Long getIdGrupoMuscular() {
        return idGrupoMuscular;
    }

    public void setIdGrupoMuscular(
            Long idGrupoMuscular
    ) {
        this.idGrupoMuscular = idGrupoMuscular;
    }

    public String getTipoParticipacion() {
        return tipoParticipacion;
    }

    public void setTipoParticipacion(
            String tipoParticipacion
    ) {
        this.tipoParticipacion = tipoParticipacion;
    }

    public BigDecimal getPorcentajeEstimulacion() {
        return porcentajeEstimulacion;
    }

    public void setPorcentajeEstimulacion(
            BigDecimal porcentajeEstimulacion
    ) {
        this.porcentajeEstimulacion = porcentajeEstimulacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "Ejercicio: "
                + idEjercicio
                + " - Grupo muscular: "
                + idGrupoMuscular;
    }
}
