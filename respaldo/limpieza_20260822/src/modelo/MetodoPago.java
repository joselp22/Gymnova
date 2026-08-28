package modelo;

import java.math.BigDecimal;
/**
 * Modelo para la tabla metodo_pago.
 *
 * @author Usuario
 */
public class MetodoPago {

    private Long idMetodoPago;
    private String nombreMetodo;
    private String descripcion;
    private boolean requiereReferencia;
    private boolean permiteCuotas;
    private BigDecimal porcentajeComision;
    private boolean estadoMetodo;

    public MetodoPago() {
    }


    public Long getIdMetodoPago() {
        return idMetodoPago;
    }

    public void setIdMetodoPago(
            Long idMetodoPago
    ) {
        this.idMetodoPago = idMetodoPago;
    }
    public String getNombreMetodo() {
        return nombreMetodo;
    }

    public void setNombreMetodo(
            String nombreMetodo
    ) {
        this.nombreMetodo = nombreMetodo;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public boolean isRequiereReferencia() {
        return requiereReferencia;
    }

    public void setRequiereReferencia(
            boolean requiereReferencia
    ) {
        this.requiereReferencia = requiereReferencia;
    }
    public boolean isPermiteCuotas() {
        return permiteCuotas;
    }

    public void setPermiteCuotas(
            boolean permiteCuotas
    ) {
        this.permiteCuotas = permiteCuotas;
    }
    public BigDecimal getPorcentajeComision() {
        return porcentajeComision;
    }

    public void setPorcentajeComision(
            BigDecimal porcentajeComision
    ) {
        this.porcentajeComision = porcentajeComision;
    }
    public boolean isEstadoMetodo() {
        return estadoMetodo;
    }

    public void setEstadoMetodo(
            boolean estadoMetodo
    ) {
        this.estadoMetodo = estadoMetodo;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombreMetodo
        );
    }
}
