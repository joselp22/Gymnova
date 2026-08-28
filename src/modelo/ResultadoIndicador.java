package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla resultado_indicador.
 *
 * @author Usuario
 */
public class ResultadoIndicador {

    private Long idResultado;
    private BigDecimal valorObtenido;
    private String clasificacion;
    private boolean fueraDeRango;
    private String observaciones;
    private LocalDate fechaRegistro;
    private Long idEvaluacion;
    private Long idIndicador;

    public ResultadoIndicador() {
    }


    public Long getIdResultado() {
        return idResultado;
    }

    public void setIdResultado(
            Long idResultado
    ) {
        this.idResultado = idResultado;
    }
    public BigDecimal getValorObtenido() {
        return valorObtenido;
    }

    public void setValorObtenido(
            BigDecimal valorObtenido
    ) {
        this.valorObtenido = valorObtenido;
    }
    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(
            String clasificacion
    ) {
        this.clasificacion = clasificacion;
    }
    public boolean isFueraDeRango() {
        return fueraDeRango;
    }

    public void setFueraDeRango(
            boolean fueraDeRango
    ) {
        this.fueraDeRango = fueraDeRango;
    }
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            LocalDate fechaRegistro
    ) {
        this.fechaRegistro = fechaRegistro;
    }
    public Long getIdEvaluacion() {
        return idEvaluacion;
    }

    public void setIdEvaluacion(
            Long idEvaluacion
    ) {
        this.idEvaluacion = idEvaluacion;
    }
    public Long getIdIndicador() {
        return idIndicador;
    }

    public void setIdIndicador(
            Long idIndicador
    ) {
        this.idIndicador = idIndicador;
    }

    @Override
    public String toString() {
        return String.valueOf(
                clasificacion
        );
    }
}
