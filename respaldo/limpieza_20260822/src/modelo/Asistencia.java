package modelo;

import java.time.LocalDate;
import java.time.LocalTime;
/**
 * Modelo para la tabla asistencia.
 *
 * @author Usuario
 */
public class Asistencia {

    private Long idAsistencia;
    private LocalDate fechaAsistencia;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private String tipoAcceso;
    private String metodoRegistro;
    private String estadoAcceso;
    private String observaciones;
    private Long idCliente;

    public Asistencia() {
    }


    public Long getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(
            Long idAsistencia
    ) {
        this.idAsistencia = idAsistencia;
    }
    public LocalDate getFechaAsistencia() {
        return fechaAsistencia;
    }

    public void setFechaAsistencia(
            LocalDate fechaAsistencia
    ) {
        this.fechaAsistencia = fechaAsistencia;
    }
    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(
            LocalTime horaEntrada
    ) {
        this.horaEntrada = horaEntrada;
    }
    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(
            LocalTime horaSalida
    ) {
        this.horaSalida = horaSalida;
    }
    public String getTipoAcceso() {
        return tipoAcceso;
    }

    public void setTipoAcceso(
            String tipoAcceso
    ) {
        this.tipoAcceso = tipoAcceso;
    }
    public String getMetodoRegistro() {
        return metodoRegistro;
    }

    public void setMetodoRegistro(
            String metodoRegistro
    ) {
        this.metodoRegistro = metodoRegistro;
    }
    public String getEstadoAcceso() {
        return estadoAcceso;
    }

    public void setEstadoAcceso(
            String estadoAcceso
    ) {
        this.estadoAcceso = estadoAcceso;
    }
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(
            Long idCliente
    ) {
        this.idCliente = idCliente;
    }

    @Override
    public String toString() {
        return String.valueOf(
                tipoAcceso
        );
    }
}
