/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDate;
/**
 *
 * @author Usuario
 */

public class EntrenadorEspecialidad {

    private Long idEntrenador;
    private Integer idEspecialidad;
    private LocalDate fechaAsignacion;
    private boolean esPrincipal;
    private String nivelDominio;
    private String estadoAsignacion;

    public EntrenadorEspecialidad() {
        fechaAsignacion = LocalDate.now();
        esPrincipal = false;
        estadoAsignacion = "ACTIVA";
    }

    public Long getIdEntrenador() {
        return idEntrenador;
    }

    public void setIdEntrenador(
            Long idEntrenador
    ) {
        this.idEntrenador = idEntrenador;
    }

    public Integer getIdEspecialidad() {
        return idEspecialidad;
    }

    public void setIdEspecialidad(
            Integer idEspecialidad
    ) {
        this.idEspecialidad = idEspecialidad;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(
            LocalDate fechaAsignacion
    ) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public boolean isEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(
            boolean esPrincipal
    ) {
        this.esPrincipal = esPrincipal;
    }

    public String getNivelDominio() {
        return nivelDominio;
    }

    public void setNivelDominio(
            String nivelDominio
    ) {
        this.nivelDominio = nivelDominio;
    }

    public String getEstadoAsignacion() {
        return estadoAsignacion;
    }

    public void setEstadoAsignacion(
            String estadoAsignacion
    ) {
        this.estadoAsignacion = estadoAsignacion;
    }

    @Override
    public String toString() {
        return "Entrenador: "
                + idEntrenador
                + " - Especialidad: "
                + idEspecialidad;
    }
}
