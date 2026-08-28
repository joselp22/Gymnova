/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Usuario
 */

public class Especialidad {

    private Integer idEspecialidad;
    private String nombreEspecialidad;
    private String descripcion;
    private String categoria;
    private boolean estadoEspecialidad;

    public Especialidad() {
        estadoEspecialidad = true;
    }

    public Integer getIdEspecialidad() {
        return idEspecialidad;
    }

    public void setIdEspecialidad(
            Integer idEspecialidad
    ) {
        this.idEspecialidad = idEspecialidad;
    }

    public String getNombreEspecialidad() {
        return nombreEspecialidad;
    }

    public void setNombreEspecialidad(
            String nombreEspecialidad
    ) {
        this.nombreEspecialidad = nombreEspecialidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(
            String categoria
    ) {
        this.categoria = categoria;
    }

    public boolean isEstadoEspecialidad() {
        return estadoEspecialidad;
    }

    public void setEstadoEspecialidad(
            boolean estadoEspecialidad
    ) {
        this.estadoEspecialidad = estadoEspecialidad;
    }

    @Override
    public String toString() {
        return nombreEspecialidad;
    }
}
