package modelo;

import java.math.BigDecimal;
/**
 * Modelo para la tabla contiene_ejercicio.
 *
 * @author Usuario
 */
public class ContieneEjercicio {

    private Long idRutinaEjercicio;
    private String diaSemana;
    private Integer orden;
    private String repeticiones;
    private Integer series;
    private BigDecimal pesoSugerido;
    private Integer duracionMinutos;
    private Integer descansoSegundos;
    private Long idRutina;
    private Long idEjercicio;

    public ContieneEjercicio() {
    }


    public Long getIdRutinaEjercicio() {
        return idRutinaEjercicio;
    }

    public void setIdRutinaEjercicio(
            Long idRutinaEjercicio
    ) {
        this.idRutinaEjercicio = idRutinaEjercicio;
    }
    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(
            String diaSemana
    ) {
        this.diaSemana = diaSemana;
    }
    public Integer getOrden() {
        return orden;
    }

    public void setOrden(
            Integer orden
    ) {
        this.orden = orden;
    }
    public String getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(
            String repeticiones
    ) {
        this.repeticiones = repeticiones;
    }
    public Integer getSeries() {
        return series;
    }

    public void setSeries(
            Integer series
    ) {
        this.series = series;
    }
    public BigDecimal getPesoSugerido() {
        return pesoSugerido;
    }

    public void setPesoSugerido(
            BigDecimal pesoSugerido
    ) {
        this.pesoSugerido = pesoSugerido;
    }
    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(
            Integer duracionMinutos
    ) {
        this.duracionMinutos = duracionMinutos;
    }
    public Integer getDescansoSegundos() {
        return descansoSegundos;
    }

    public void setDescansoSegundos(
            Integer descansoSegundos
    ) {
        this.descansoSegundos = descansoSegundos;
    }
    public Long getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(
            Long idRutina
    ) {
        this.idRutina = idRutina;
    }
    public Long getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(
            Long idEjercicio
    ) {
        this.idEjercicio = idEjercicio;
    }

    @Override
    public String toString() {
        return String.valueOf(
                diaSemana
        );
    }
}
