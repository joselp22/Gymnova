/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Usuario
 */

public class NutricionistaCertificacion {

    private Long idNutricionista;
    private String certificacion;

    public NutricionistaCertificacion() {
    }

    public Long getIdNutricionista() {
        return idNutricionista;
    }

    public void setIdNutricionista(
            Long idNutricionista
    ) {
        this.idNutricionista = idNutricionista;
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
