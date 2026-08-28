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

public class Entrenador extends Empleado {

    private LocalDate fechaInicioProfesion;
    private String nivelEntrenador;
    private boolean estadoEntrenador;

    public Entrenador() {
        super();
        estadoEntrenador = true;
    }

    public LocalDate getFechaInicioProfesion() {
        return fechaInicioProfesion;
    }

    public void setFechaInicioProfesion(
            LocalDate fechaInicioProfesion
    ) {
        this.fechaInicioProfesion = fechaInicioProfesion;
    }

    public String getNivelEntrenador() {
        return nivelEntrenador;
    }

    public void setNivelEntrenador(
            String nivelEntrenador
    ) {
        this.nivelEntrenador = nivelEntrenador;
    }

    public boolean isEstadoEntrenador() {
        return estadoEntrenador;
    }

    public void setEstadoEntrenador(
            boolean estadoEntrenador
    ) {
        this.estadoEntrenador = estadoEntrenador;
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
                + nivelEntrenador;
    }
}