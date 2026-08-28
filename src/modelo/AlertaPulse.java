package modelo;

import java.util.ArrayList;
import java.util.List;

/** Resultado explicable del indicador preventivo GYMNOVA Pulse. */
public class AlertaPulse {

    private Long idCliente;
    private String codigoCliente;
    private String nombreCliente;
    private int puntuacion;
    private String nivel;
    private final List<String> razones = new ArrayList<>();

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public List<String> getRazones() { return razones; }

    public String getExplicacion() {
        return razones.isEmpty()
                ? "Actividad, membresia y progreso sin alertas."
                : String.join(" ", razones);
    }

    @Override
    public String toString() {
        return nivel + " " + puntuacion + "/100";
    }
}
