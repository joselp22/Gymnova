/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 *
 * @author Usuario
 */

public class Cliente extends Persona {

    private String codigoCliente;
    private LocalDate fechaRegistro;
    private BigDecimal pesoInicial;
    private BigDecimal pesoMeta;
    private String observaciones;
    private boolean estadoCliente;

    public Cliente() {
        super();

        this.fechaRegistro = LocalDate.now();
        this.estadoCliente = true;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(
            String codigoCliente
    ) {
        this.codigoCliente = codigoCliente;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            LocalDate fechaRegistro
    ) {
        this.fechaRegistro = fechaRegistro;
    }

    public BigDecimal getPesoInicial() {
        return pesoInicial;
    }

    public void setPesoInicial(
            BigDecimal pesoInicial
    ) {
        this.pesoInicial = pesoInicial;
    }

    public BigDecimal getPesoMeta() {
        return pesoMeta;
    }

    public void setPesoMeta(
            BigDecimal pesoMeta
    ) {
        this.pesoMeta = pesoMeta;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }

    public boolean isEstadoCliente() {
        return estadoCliente;
    }

    public void setEstadoCliente(
            boolean estadoCliente
    ) {
        this.estadoCliente = estadoCliente;
    }
}
