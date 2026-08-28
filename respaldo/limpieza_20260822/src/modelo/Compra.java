package modelo;

import java.time.LocalDate;
/**
 * Modelo para la tabla compra.
 *
 * @author Usuario
 */
public class Compra {

    private Long idCompra;
    private String numeroCompra;
    private String numeroFacturaProveedor;
    private LocalDate fechaCompra;
    private LocalDate fechaRecepcion;
    private String tipoPago;
    private String estadoCompra;
    private Long idProveedor;
    private Long idEmpleado;

    public Compra() {
    }


    public Long getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(
            Long idCompra
    ) {
        this.idCompra = idCompra;
    }
    public String getNumeroCompra() {
        return numeroCompra;
    }

    public void setNumeroCompra(
            String numeroCompra
    ) {
        this.numeroCompra = numeroCompra;
    }
    public String getNumeroFacturaProveedor() {
        return numeroFacturaProveedor;
    }

    public void setNumeroFacturaProveedor(
            String numeroFacturaProveedor
    ) {
        this.numeroFacturaProveedor = numeroFacturaProveedor;
    }
    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(
            LocalDate fechaCompra
    ) {
        this.fechaCompra = fechaCompra;
    }
    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(
            LocalDate fechaRecepcion
    ) {
        this.fechaRecepcion = fechaRecepcion;
    }
    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(
            String tipoPago
    ) {
        this.tipoPago = tipoPago;
    }
    public String getEstadoCompra() {
        return estadoCompra;
    }

    public void setEstadoCompra(
            String estadoCompra
    ) {
        this.estadoCompra = estadoCompra;
    }
    public Long getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(
            Long idProveedor
    ) {
        this.idProveedor = idProveedor;
    }
    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(
            Long idEmpleado
    ) {
        this.idEmpleado = idEmpleado;
    }

    @Override
    public String toString() {
        return String.valueOf(
                numeroCompra
        );
    }
}
