package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla suministra_producto.
 *
 * @author Usuario
 */
public class SuministraProducto {

    private String codigoProductoProveedor;
    private BigDecimal costoReferencia;
    private Integer tiempoEntregaDias;
    private Integer cantidadMinimaPedido;
    private LocalDate fechaActualizacion;
    private Long idProveedor;
    private Long idProducto;

    public SuministraProducto() {
    }


    public String getCodigoProductoProveedor() {
        return codigoProductoProveedor;
    }

    public void setCodigoProductoProveedor(
            String codigoProductoProveedor
    ) {
        this.codigoProductoProveedor = codigoProductoProveedor;
    }
    public BigDecimal getCostoReferencia() {
        return costoReferencia;
    }

    public void setCostoReferencia(
            BigDecimal costoReferencia
    ) {
        this.costoReferencia = costoReferencia;
    }
    public Integer getTiempoEntregaDias() {
        return tiempoEntregaDias;
    }

    public void setTiempoEntregaDias(
            Integer tiempoEntregaDias
    ) {
        this.tiempoEntregaDias = tiempoEntregaDias;
    }
    public Integer getCantidadMinimaPedido() {
        return cantidadMinimaPedido;
    }

    public void setCantidadMinimaPedido(
            Integer cantidadMinimaPedido
    ) {
        this.cantidadMinimaPedido = cantidadMinimaPedido;
    }
    public LocalDate getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            LocalDate fechaActualizacion
    ) {
        this.fechaActualizacion = fechaActualizacion;
    }
    public Long getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(
            Long idProveedor
    ) {
        this.idProveedor = idProveedor;
    }
    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(
            Long idProducto
    ) {
        this.idProducto = idProducto;
    }

    @Override
    public String toString() {
        return String.valueOf(
                codigoProductoProveedor
        );
    }
}
