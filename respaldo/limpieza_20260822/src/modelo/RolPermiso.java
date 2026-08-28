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
public class RolPermiso {

    private Integer idRol;
    private Integer idPermiso;
    private LocalDate fechaAsignacion;
    private String estadoAsignacion;

    public RolPermiso() {
        fechaAsignacion = LocalDate.now();
        estadoAsignacion = "ACTIVA";
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(
            Integer idRol
    ) {
        this.idRol = idRol;
    }

    public Integer getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(
            Integer idPermiso
    ) {
        this.idPermiso = idPermiso;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(
            LocalDate fechaAsignacion
    ) {
        this.fechaAsignacion = fechaAsignacion;
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
        return "Rol: "
                + idRol
                + " - Permiso: "
                + idPermiso;
    }
}
