package modelo;

import java.time.LocalTime;
/**
 * Modelo para la tabla horario.
 *
 * @author Usuario
 */
public class Horario {

    private Long idHorario;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionProgramada;
    private Integer cupoMaximo;
    private boolean estadoHorario;
    private String observaciones;

    public Horario() {
        estadoHorario = true;
    }


    public Long getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(
            Long idHorario
    ) {
        this.idHorario = idHorario;
    }
    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(
            String diaSemana
    ) {
        this.diaSemana = diaSemana;
    }
    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(
            LocalTime horaInicio
    ) {
        this.horaInicio = horaInicio;
    }
    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(
            LocalTime horaFin
    ) {
        this.horaFin = horaFin;
    }
    public Integer getDuracionProgramada() {
        return duracionProgramada;
    }

    public void setDuracionProgramada(
            Integer duracionProgramada
    ) {
        this.duracionProgramada = duracionProgramada;
    }
    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(
            Integer cupoMaximo
    ) {
        this.cupoMaximo = cupoMaximo;
    }
    public boolean isEstadoHorario() {
        return estadoHorario;
    }

    public void setEstadoHorario(
            boolean estadoHorario
    ) {
        this.estadoHorario = estadoHorario;
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
        return String.valueOf(
                diaSemana
        );
    }
}
