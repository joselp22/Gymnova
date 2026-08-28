package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla cliente_objetivo.
 *
 * @author Usuario
 */
public class ClienteObjetivo {

    private Long idClienteObjetivo;
    private LocalDate fechaInicio;
    private LocalDate fechaMeta;
    private String prioridad;
    private String estadoClienteObjetivo;
    private String observaciones;
    private BigDecimal pesoActual;
    private Long idCliente;
    private Long idObjetivo;

    public ClienteObjetivo() {
    }


    public Long getIdClienteObjetivo() {
        return idClienteObjetivo;
    }

    public void setIdClienteObjetivo(
            Long idClienteObjetivo
    ) {
        this.idClienteObjetivo = idClienteObjetivo;
    }
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(
            LocalDate fechaInicio
    ) {
        this.fechaInicio = fechaInicio;
    }
    public LocalDate getFechaMeta() {
        return fechaMeta;
    }

    public void setFechaMeta(
            LocalDate fechaMeta
    ) {
        this.fechaMeta = fechaMeta;
    }
    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(
            String prioridad
    ) {
        this.prioridad = prioridad;
    }
    public String getEstadoClienteObjetivo() {
        return estadoClienteObjetivo;
    }

    public void setEstadoClienteObjetivo(
            String estadoClienteObjetivo
    ) {
        this.estadoClienteObjetivo = estadoClienteObjetivo;
    }
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }
    public BigDecimal getPesoActual() {
        return pesoActual;
    }

    public void setPesoActual(
            BigDecimal pesoActual
    ) {
        this.pesoActual = pesoActual;
    }
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(
            Long idCliente
    ) {
        this.idCliente = idCliente;
    }
    public Long getIdObjetivo() {
        return idObjetivo;
    }

    public void setIdObjetivo(
            Long idObjetivo
    ) {
        this.idObjetivo = idObjetivo;
    }

    @Override
    public String toString() {
        return String.valueOf(
                prioridad
        );
    }
}
