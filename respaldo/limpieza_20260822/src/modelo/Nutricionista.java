/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;
import java.time.Period;
/**
 *
 * @author Usuario
 */

public class Nutricionista extends Empleado {

    private String numeroLicencia;
    private LocalDate fechaInicioProfesion;
    private String estadoLicencia;

    public Nutricionista() {
        super();
        estadoLicencia = "ACTIVA";
    }

    public String getNumeroLicencia() {
        return numeroLicencia;
    }

    public void setNumeroLicencia(
            String numeroLicencia
    ) {
        this.numeroLicencia = numeroLicencia;
    }

    public LocalDate getFechaInicioProfesion() {
        return fechaInicioProfesion;
    }

    public void setFechaInicioProfesion(
            LocalDate fechaInicioProfesion
    ) {
        this.fechaInicioProfesion = fechaInicioProfesion;
    }

    public String getEstadoLicencia() {
        return estadoLicencia;
    }

    public void setEstadoLicencia(
            String estadoLicencia
    ) {
        this.estadoLicencia = estadoLicencia;
    }

    public int getAniosExperiencia() {

        if (fechaInicioProfesion == null) {
            return 0;
        }

        return Period.between(
                fechaInicioProfesion,
                LocalDate.now()
        ).getYears();
    }

    @Override
    public String toString() {
        return super.toString()
                + " - "
                + numeroLicencia;
    }
}