/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Usuario
 */
public class Rol {

    private Integer idRol;
    private String nombreRol;
    private String descripcion;
    private boolean estadoRol;

    public Rol() {
        estadoRol = true;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(
            Integer idRol
    ) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(
            String nombreRol
    ) {
        this.nombreRol = nombreRol;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }

    public boolean isEstadoRol() {
        return estadoRol;
    }

    public void setEstadoRol(
            boolean estadoRol
    ) {
        this.estadoRol = estadoRol;
    }

    @Override
    public String toString() {
        return nombreRol;
    }
}
