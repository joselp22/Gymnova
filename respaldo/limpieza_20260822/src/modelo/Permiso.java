/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Usuario
 */
public class Permiso {

    private Integer idPermiso;
    private String nombrePermiso;
    private String descripcion;
    private String modulo;
    private String accion;
    private boolean estadoPermiso;

    public Permiso() {
        estadoPermiso = true;
    }

    public Integer getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(
            Integer idPermiso
    ) {
        this.idPermiso = idPermiso;
    }

    public String getNombrePermiso() {
        return nombrePermiso;
    }

    public void setNombrePermiso(
            String nombrePermiso
    ) {
        this.nombrePermiso = nombrePermiso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(
            String modulo
    ) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(
            String accion
    ) {
        this.accion = accion;
    }

    public boolean isEstadoPermiso() {
        return estadoPermiso;
    }

    public void setEstadoPermiso(
            boolean estadoPermiso
    ) {
        this.estadoPermiso = estadoPermiso;
    }

    @Override
    public String toString() {
        return nombrePermiso;
    }
}
