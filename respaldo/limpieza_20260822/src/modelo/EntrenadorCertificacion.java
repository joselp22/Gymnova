/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Usuario
 */

public class EntrenadorCertificacion {

    private Long idEntrenador;
    private String certificacion;

    public EntrenadorCertificacion() {
    }

    public Long getIdEntrenador() {
        return idEntrenador;
    }

    public void setIdEntrenador(
            Long idEntrenador
    ) {
        this.idEntrenador = idEntrenador;
    }

    public String getCertificacion() {
        return certificacion;
    }

    public void setCertificacion(
            String certificacion
    ) {
        this.certificacion = certificacion;
    }

    @Override
    public String toString() {
        return certificacion;
    }
}
