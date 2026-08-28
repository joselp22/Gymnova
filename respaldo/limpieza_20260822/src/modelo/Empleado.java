/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
/**
 *
 * @author Usuario
 */
public class Empleado extends Persona {

    private String codigoEmpleado;
    private LocalDate fechaIngreso;
    private String tipoContrato;
    private BigDecimal salario;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean estadoEmpleado;

    public Empleado() {
        super();

        this.fechaIngreso = LocalDate.now();
        this.estadoEmpleado = true;
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(
            String codigoEmpleado
    ) {
        this.codigoEmpleado = codigoEmpleado;
    }

    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(
            LocalDate fechaIngreso
    ) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(
            String tipoContrato
    ) {
        this.tipoContrato = tipoContrato;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(
            BigDecimal salario
    ) {
        this.salario = salario;
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

    public boolean isEstadoEmpleado() {
        return estadoEmpleado;
    }

    public void setEstadoEmpleado(
            boolean estadoEmpleado
    ) {
        this.estadoEmpleado = estadoEmpleado;
    }

    public int getAntiguedadLaboral() {

        if (fechaIngreso == null) {
            return 0;
        }

        return Period.between(
                fechaIngreso,
                LocalDate.now()
        ).getYears();
    }

    public String getTurno() {

        if (horaInicio == null || horaFin == null) {
            return "";
        }

        return horaInicio + " - " + horaFin;
    }

    @Override
    public String toString() {
        return codigoEmpleado
                + " - "
                + getNombreCompleto();
    }
}
