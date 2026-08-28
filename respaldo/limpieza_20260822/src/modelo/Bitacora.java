/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.time.LocalDateTime;

/**
 *
 * @author Usuario
 */
public class Bitacora {

    private Long idBitacora;
    private LocalDateTime fechaHora;
    private String accionRealizada;
    private String modulo;
    private String direccionIp;
    private String descripcion;
    private String resultado;
    private Long idUsuario;
    private String nombreUsuario;

    public Bitacora() {
        fechaHora = LocalDateTime.now();
    }

    public Long getIdBitacora() {
        return idBitacora;
    }

    public void setIdBitacora(
            Long idBitacora
    ) {
        this.idBitacora = idBitacora;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(
            LocalDateTime fechaHora
    ) {
        this.fechaHora = fechaHora;
    }

    public String getAccionRealizada() {
        return accionRealizada;
    }

    public void setAccionRealizada(
            String accionRealizada
    ) {
        this.accionRealizada = accionRealizada;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(
            String modulo
    ) {
        this.modulo = modulo;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(
            String direccionIp
    ) {
        this.direccionIp = direccionIp;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(
            String resultado
    ) {
        this.resultado = resultado;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(
            Long idUsuario
    ) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(
            String nombreUsuario
    ) {
        this.nombreUsuario = nombreUsuario;
    }

    @Override
    public String toString() {
        return fechaHora
                + " - "
                + modulo
                + " - "
                + accionRealizada;
    }
}
