package modelo;

import java.time.LocalDate;
/**
 * Modelo para la tabla clase_especial.
 *
 * @author Usuario
 */
public class ClaseEspecial {

    private Long idClaseEspecial;
    private LocalDate fechaClaseEspecial;
    private String estadoProgramacion;
    private String observaciones;
    private Long idClase;
    private Long idHorario;
    private Long idEntrenador;

    public ClaseEspecial() {
    }


    public Long getIdClaseEspecial() {
        return idClaseEspecial;
    }

    public void setIdClaseEspecial(
            Long idClaseEspecial
    ) {
        this.idClaseEspecial = idClaseEspecial;
    }
    public LocalDate getFechaClaseEspecial() {
        return fechaClaseEspecial;
    }

    public void setFechaClaseEspecial(
            LocalDate fechaClaseEspecial
    ) {
        this.fechaClaseEspecial = fechaClaseEspecial;
    }
    public String getEstadoProgramacion() {
        return estadoProgramacion;
    }

    public void setEstadoProgramacion(
            String estadoProgramacion
    ) {
        this.estadoProgramacion = estadoProgramacion;
    }
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }
    public Long getIdClase() {
        return idClase;
    }

    public void setIdClase(
            Long idClase
    ) {
        this.idClase = idClase;
    }
    public Long getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(
            Long idHorario
    ) {
        this.idHorario = idHorario;
    }
    public Long getIdEntrenador() {
        return idEntrenador;
    }

    public void setIdEntrenador(
            Long idEntrenador
    ) {
        this.idEntrenador = idEntrenador;
    }

    @Override
    public String toString() {
        return String.valueOf(
                estadoProgramacion
        );
    }
}
