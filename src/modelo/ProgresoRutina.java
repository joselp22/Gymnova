package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla progreso_rutina.
 *
 * @author Usuario
 */
public class ProgresoRutina {

    private Long idProgreso;
    private LocalDate fechaRegistro;
    private Integer sesionesPlanificadas;
    private Integer sesionesCompletadas;
    private BigDecimal pesoCorporal;
    private String nivelEsfuerzo;
    private String estadoProgreso;
    private Long idCliente;
    private Long idRutina;

    public ProgresoRutina() {
    }


    public Long getIdProgreso() {
        return idProgreso;
    }

    public void setIdProgreso(
            Long idProgreso
    ) {
        this.idProgreso = idProgreso;
    }
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            LocalDate fechaRegistro
    ) {
        this.fechaRegistro = fechaRegistro;
    }
    public Integer getSesionesPlanificadas() {
        return sesionesPlanificadas;
    }

    public void setSesionesPlanificadas(
            Integer sesionesPlanificadas
    ) {
        this.sesionesPlanificadas = sesionesPlanificadas;
    }
    public Integer getSesionesCompletadas() {
        return sesionesCompletadas;
    }

    public void setSesionesCompletadas(
            Integer sesionesCompletadas
    ) {
        this.sesionesCompletadas = sesionesCompletadas;
    }
    public BigDecimal getPesoCorporal() {
        return pesoCorporal;
    }

    public void setPesoCorporal(
            BigDecimal pesoCorporal
    ) {
        this.pesoCorporal = pesoCorporal;
    }
    public String getNivelEsfuerzo() {
        return nivelEsfuerzo;
    }

    public void setNivelEsfuerzo(
            String nivelEsfuerzo
    ) {
        this.nivelEsfuerzo = nivelEsfuerzo;
    }
    public String getEstadoProgreso() {
        return estadoProgreso;
    }

    public void setEstadoProgreso(
            String estadoProgreso
    ) {
        this.estadoProgreso = estadoProgreso;
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
                nivelEsfuerzo
        );
    }
}
