package modelo;

import java.math.BigDecimal;
import java.time.LocalTime;
/**
 * Modelo para la tabla incluye_alimento.
 *
 * @author Usuario
 */
public class IncluyeAlimento {

    private Long idDetallePlan;
    private String diaSemana;
    private String tipoComida;
    private LocalTime horaConsumo;
    private BigDecimal cantidad;
    private String unidadMedida;
    private Integer ordenComida;
    private String indicaciones;
    private String estadoDetalle;
    private Long idPlanNutricional;
    private Long idAlimento;

    public IncluyeAlimento() {
    }


    public Long getIdDetallePlan() {
        return idDetallePlan;
    }

    public void setIdDetallePlan(
            Long idDetallePlan
    ) {
        this.idDetallePlan = idDetallePlan;
    }
    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(
            String diaSemana
    ) {
        this.diaSemana = diaSemana;
    }
    public String getTipoComida() {
        return tipoComida;
    }

    public void setTipoComida(
            String tipoComida
    ) {
        this.tipoComida = tipoComida;
    }
    public LocalTime getHoraConsumo() {
        return horaConsumo;
    }

    public void setHoraConsumo(
            LocalTime horaConsumo
    ) {
        this.horaConsumo = horaConsumo;
    }
    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(
            BigDecimal cantidad
    ) {
        this.cantidad = cantidad;
    }
    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(
            String unidadMedida
    ) {
        this.unidadMedida = unidadMedida;
    }
    public Integer getOrdenComida() {
        return ordenComida;
    }

    public void setOrdenComida(
            Integer ordenComida
    ) {
        this.ordenComida = ordenComida;
    }
    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(
            String indicaciones
    ) {
        this.indicaciones = indicaciones;
    }
    public String getEstadoDetalle() {
        return estadoDetalle;
    }

    public void setEstadoDetalle(
            String estadoDetalle
    ) {
        this.estadoDetalle = estadoDetalle;
    }
    public Long getIdPlanNutricional() {
        return idPlanNutricional;
    }

    public void setIdPlanNutricional(
            Long idPlanNutricional
    ) {
        this.idPlanNutricional = idPlanNutricional;
    }
    public Long getIdAlimento() {
        return idAlimento;
    }

    public void setIdAlimento(
            Long idAlimento
    ) {
        this.idAlimento = idAlimento;
    }

    @Override
    public String toString() {
        return String.valueOf(
                diaSemana
        );
    }
}
