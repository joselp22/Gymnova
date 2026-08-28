package modelo;

import java.time.LocalDateTime;
/**
 * Modelo para la tabla reserva.
 *
 * @author Usuario
 */
public class Reserva {

    private Long idReserva;
    private String codigoReserva;
    private LocalDateTime fechaHoraReserva;
    private String estadoReserva;
    private LocalDateTime fechaHoraCancelacion;
    private String motivoCancelacion;
    private boolean asistenciaConfirmada;
    private String observaciones;
    private Long idCliente;
    private Long idHorario;

    public Reserva() {
    }


    public Long getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(
            Long idReserva
    ) {
        this.idReserva = idReserva;
    }
    public String getCodigoReserva() {
        return codigoReserva;
    }

    public void setCodigoReserva(
            String codigoReserva
    ) {
        this.codigoReserva = codigoReserva;
    }
    public LocalDateTime getFechaHoraReserva() {
        return fechaHoraReserva;
    }

    public void setFechaHoraReserva(
            LocalDateTime fechaHoraReserva
    ) {
        this.fechaHoraReserva = fechaHoraReserva;
    }
    public String getEstadoReserva() {
        return estadoReserva;
    }

    public void setEstadoReserva(
            String estadoReserva
    ) {
        this.estadoReserva = estadoReserva;
    }
    public LocalDateTime getFechaHoraCancelacion() {
        return fechaHoraCancelacion;
    }

    public void setFechaHoraCancelacion(
            LocalDateTime fechaHoraCancelacion
    ) {
        this.fechaHoraCancelacion = fechaHoraCancelacion;
    }
    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(
            String motivoCancelacion
    ) {
        this.motivoCancelacion = motivoCancelacion;
    }
    public boolean isAsistenciaConfirmada() {
        return asistenciaConfirmada;
    }

    public void setAsistenciaConfirmada(
            boolean asistenciaConfirmada
    ) {
        this.asistenciaConfirmada = asistenciaConfirmada;
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
    public Long getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(
            Long idHorario
    ) {
        this.idHorario = idHorario;
    }

    @Override
    public String toString() {
        return String.valueOf(
                codigoReserva
        );
    }
}
