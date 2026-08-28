package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla mantenimiento.
 *
 * @author Usuario
 */
public class Mantenimiento {

    private Long idMantenimiento;
    private String numeroMantenimiento;
    private LocalDate fechaSolicitud;
    private LocalDate fechaProgramada;
    private LocalDate fechaRealizacion;
    private BigDecimal costoRepuestos;
    private BigDecimal costoManoObra;
    private String resultadoMantenimiento;
    private String descripcionFalla;
    private String trabajoRealizado;
    private String estadoMantenimiento;
    private String observaciones;
    private Long idEquipo;
    private Long idTipoMantenimiento;
    private Long idEmpleado;

    public Mantenimiento() {
    }


    public Long getIdMantenimiento() {
        return idMantenimiento;
    }

    public void setIdMantenimiento(
            Long idMantenimiento
    ) {
        this.idMantenimiento = idMantenimiento;
    }
    public String getNumeroMantenimiento() {
        return numeroMantenimiento;
    }

    public void setNumeroMantenimiento(
            String numeroMantenimiento
    ) {
        this.numeroMantenimiento = numeroMantenimiento;
    }
    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(
            LocalDate fechaSolicitud
    ) {
        this.fechaSolicitud = fechaSolicitud;
    }
    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(
            LocalDate fechaProgramada
    ) {
        this.fechaProgramada = fechaProgramada;
    }
    public LocalDate getFechaRealizacion() {
        return fechaRealizacion;
    }

    public void setFechaRealizacion(
            LocalDate fechaRealizacion
    ) {
        this.fechaRealizacion = fechaRealizacion;
    }
    public BigDecimal getCostoRepuestos() {
        return costoRepuestos;
    }

    public void setCostoRepuestos(
            BigDecimal costoRepuestos
    ) {
        this.costoRepuestos = costoRepuestos;
    }
    public BigDecimal getCostoManoObra() {
        return costoManoObra;
    }

    public void setCostoManoObra(
            BigDecimal costoManoObra
    ) {
        this.costoManoObra = costoManoObra;
    }
    public String getResultadoMantenimiento() {
        return resultadoMantenimiento;
    }

    public void setResultadoMantenimiento(
            String resultadoMantenimiento
    ) {
        this.resultadoMantenimiento = resultadoMantenimiento;
    }
    public String getDescripcionFalla() {
        return descripcionFalla;
    }

    public void setDescripcionFalla(
            String descripcionFalla
    ) {
        this.descripcionFalla = descripcionFalla;
    }
    public String getTrabajoRealizado() {
        return trabajoRealizado;
    }

    public void setTrabajoRealizado(
            String trabajoRealizado
    ) {
        this.trabajoRealizado = trabajoRealizado;
    }
    public String getEstadoMantenimiento() {
        return estadoMantenimiento;
    }

    public void setEstadoMantenimiento(
            String estadoMantenimiento
    ) {
        this.estadoMantenimiento = estadoMantenimiento;
    }
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }
    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(
            Long idEquipo
    ) {
        this.idEquipo = idEquipo;
    }
    public Long getIdTipoMantenimiento() {
        return idTipoMantenimiento;
    }

    public void setIdTipoMantenimiento(
            Long idTipoMantenimiento
    ) {
        this.idTipoMantenimiento = idTipoMantenimiento;
    }
    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(
            Long idEmpleado
    ) {
        this.idEmpleado = idEmpleado;
    }

    @Override
    public String toString() {
        return String.valueOf(
                numeroMantenimiento
        );
    }
}
