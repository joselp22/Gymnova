package modelo;

import java.math.BigDecimal;
/**
 * Modelo para la tabla medicion_corporal.
 *
 * @author Usuario
 */
public class MedicionCorporal {

    private Long idMedicion;
    private BigDecimal pesoKg;
    private BigDecimal alturaM;
    private BigDecimal porcentajeGrasa;
    private BigDecimal cinturaCm;
    private BigDecimal pechoCm;
    private BigDecimal brazoCm;
    private BigDecimal musloCm;
    private String observaciones;
    private Long idEvaluacion;

    public MedicionCorporal() {
    }


    public Long getIdMedicion() {
        return idMedicion;
    }

    public void setIdMedicion(
            Long idMedicion
    ) {
        this.idMedicion = idMedicion;
    }
    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(
            BigDecimal pesoKg
    ) {
        this.pesoKg = pesoKg;
    }
    public BigDecimal getAlturaM() {
        return alturaM;
    }

    public void setAlturaM(
            BigDecimal alturaM
    ) {
        this.alturaM = alturaM;
    }
    public BigDecimal getPorcentajeGrasa() {
        return porcentajeGrasa;
    }

    public void setPorcentajeGrasa(
            BigDecimal porcentajeGrasa
    ) {
        this.porcentajeGrasa = porcentajeGrasa;
    }
    public BigDecimal getCinturaCm() {
        return cinturaCm;
    }

    public void setCinturaCm(
            BigDecimal cinturaCm
    ) {
        this.cinturaCm = cinturaCm;
    }
    public BigDecimal getPechoCm() {
        return pechoCm;
    }

    public void setPechoCm(
            BigDecimal pechoCm
    ) {
        this.pechoCm = pechoCm;
    }
    public BigDecimal getBrazoCm() {
        return brazoCm;
    }

    public void setBrazoCm(
            BigDecimal brazoCm
    ) {
        this.brazoCm = brazoCm;
    }
    public BigDecimal getMusloCm() {
        return musloCm;
    }

    public void setMusloCm(
            BigDecimal musloCm
    ) {
        this.musloCm = musloCm;
    }
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
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
                observaciones
        );
    }
}
