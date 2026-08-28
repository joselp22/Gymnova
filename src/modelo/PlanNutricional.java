package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla plan_nutricional.
 *
 * @author Usuario
 */
public class PlanNutricional {

    private Long idPlanNutricional;
    private String codigoPlan;
    private String nombrePlan;
    private LocalDate fechaCreacion;
    private LocalDate fechaFin;
    private Integer caloriasObjetivo;
    private BigDecimal proteinasObjetivoG;
    private BigDecimal carbohidratosObjetivoG;
    private String restriccionesGenerales;
    private String estadoPlan;
    private LocalDate fechaInicio;
    private Long idCliente;
    private Long idNutricionista;

    public PlanNutricional() {
    }


    public Long getIdPlanNutricional() {
        return idPlanNutricional;
    }

    public void setIdPlanNutricional(
            Long idPlanNutricional
    ) {
        this.idPlanNutricional = idPlanNutricional;
    }
    public String getCodigoPlan() {
        return codigoPlan;
    }

    public void setCodigoPlan(
            String codigoPlan
    ) {
        this.codigoPlan = codigoPlan;
    }
    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(
            String nombrePlan
    ) {
        this.nombrePlan = nombrePlan;
    }
    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(
            LocalDate fechaCreacion
    ) {
        this.fechaCreacion = fechaCreacion;
    }
    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(
            LocalDate fechaFin
    ) {
        this.fechaFin = fechaFin;
    }
    public Integer getCaloriasObjetivo() {
        return caloriasObjetivo;
    }

    public void setCaloriasObjetivo(
            Integer caloriasObjetivo
    ) {
        this.caloriasObjetivo = caloriasObjetivo;
    }
    public BigDecimal getProteinasObjetivoG() {
        return proteinasObjetivoG;
    }

    public void setProteinasObjetivoG(
            BigDecimal proteinasObjetivoG
    ) {
        this.proteinasObjetivoG = proteinasObjetivoG;
    }
    public BigDecimal getCarbohidratosObjetivoG() {
        return carbohidratosObjetivoG;
    }

    public void setCarbohidratosObjetivoG(
            BigDecimal carbohidratosObjetivoG
    ) {
        this.carbohidratosObjetivoG = carbohidratosObjetivoG;
    }
    public String getRestriccionesGenerales() {
        return restriccionesGenerales;
    }

    public void setRestriccionesGenerales(
            String restriccionesGenerales
    ) {
        this.restriccionesGenerales = restriccionesGenerales;
    }
    public String getEstadoPlan() {
        return estadoPlan;
    }

    public void setEstadoPlan(
            String estadoPlan
    ) {
        this.estadoPlan = estadoPlan;
    }
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(
            LocalDate fechaInicio
    ) {
        this.fechaInicio = fechaInicio;
    }
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(
            Long idCliente
    ) {
        this.idCliente = idCliente;
    }
    public Long getIdNutricionista() {
        return idNutricionista;
    }

    public void setIdNutricionista(
            Long idNutricionista
    ) {
        this.idNutricionista = idNutricionista;
    }

    @Override
    public String toString() {
        return String.valueOf(
                codigoPlan
        );
    }
}
