package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fila de historial que une la membresia con su factura y pago asociado.
 */
public class MembresiaCobroResumen {

    private Long idMembresia;
    private String numeroMembresia;
    private String nombrePlan;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estadoMembresia;
    private BigDecimal costoFinal;
    private String numeroFactura;
    private String codigoPago;
    private LocalDateTime fechaHoraPago;
    private String metodoPago;
    private String referenciaTransaccion;
    private BigDecimal totalCobrado;

    public Long getIdMembresia() { return idMembresia; }
    public void setIdMembresia(Long idMembresia) { this.idMembresia = idMembresia; }
    public String getNumeroMembresia() { return numeroMembresia; }
    public void setNumeroMembresia(String numeroMembresia) { this.numeroMembresia = numeroMembresia; }
    public String getNombrePlan() { return nombrePlan; }
    public void setNombrePlan(String nombrePlan) { this.nombrePlan = nombrePlan; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getEstadoMembresia() { return estadoMembresia; }
    public void setEstadoMembresia(String estadoMembresia) { this.estadoMembresia = estadoMembresia; }
    public BigDecimal getCostoFinal() { return costoFinal; }
    public void setCostoFinal(BigDecimal costoFinal) { this.costoFinal = costoFinal; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public String getCodigoPago() { return codigoPago; }
    public void setCodigoPago(String codigoPago) { this.codigoPago = codigoPago; }
    public LocalDateTime getFechaHoraPago() { return fechaHoraPago; }
    public void setFechaHoraPago(LocalDateTime fechaHoraPago) { this.fechaHoraPago = fechaHoraPago; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getReferenciaTransaccion() { return referenciaTransaccion; }
    public void setReferenciaTransaccion(String referenciaTransaccion) { this.referenciaTransaccion = referenciaTransaccion; }
    public BigDecimal getTotalCobrado() { return totalCobrado; }
    public void setTotalCobrado(BigDecimal totalCobrado) { this.totalCobrado = totalCobrado; }
}
