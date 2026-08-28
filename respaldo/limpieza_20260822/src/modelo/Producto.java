package modelo;

import java.math.BigDecimal;
/**
 * Modelo para la tabla producto.
 *
 * @author Usuario
 */
public class Producto {

    private String codigoProducto;
    private Long idProducto;
    private String codigoBarras;
    private String nombreProducto;
    private String marca;
    private String unidadMedida;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private BigDecimal margenGanancia;
    private byte[] imagenProducto;
    private Integer stockActual;
    private Integer stockMinimo;
    private boolean estadoProducto;
    private Long idCategoriaProducto;

    public Producto() {
    }


    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(
            String codigoProducto
    ) {
        this.codigoProducto = codigoProducto;
    }
    public Long getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(
            Long idProducto
    ) {
        this.idProducto = idProducto;
    }
    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(
            String codigoBarras
    ) {
        this.codigoBarras = codigoBarras;
    }
    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(
            String nombreProducto
    ) {
        this.nombreProducto = nombreProducto;
    }
    public String getMarca() {
        return marca;
    }

    public void setMarca(
            String marca
    ) {
        this.marca = marca;
    }
    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(
            String unidadMedida
    ) {
        this.unidadMedida = unidadMedida;
    }
    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(
            BigDecimal precioCompra
    ) {
        this.precioCompra = precioCompra;
    }
    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(
            BigDecimal precioVenta
    ) {
        this.precioVenta = precioVenta;
    }
    public BigDecimal getMargenGanancia() {
        return margenGanancia;
    }

    public void setMargenGanancia(
            BigDecimal margenGanancia
    ) {
        this.margenGanancia = margenGanancia;
    }
    public byte[] getImagenProducto() {
        return imagenProducto;
    }

    public void setImagenProducto(
            byte[] imagenProducto
    ) {
        this.imagenProducto = imagenProducto;
    }
    public Integer getStockActual() {
        return stockActual;
    }

    public void setStockActual(
            Integer stockActual
    ) {
        this.stockActual = stockActual;
    }
    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(
            Integer stockMinimo
    ) {
        this.stockMinimo = stockMinimo;
    }
    public boolean isEstadoProducto() {
        return estadoProducto;
    }

    public void setEstadoProducto(
            boolean estadoProducto
    ) {
        this.estadoProducto = estadoProducto;
    }
    public Long getIdCategoriaProducto() {
        return idCategoriaProducto;
    }

    public void setIdCategoriaProducto(
            Long idCategoriaProducto
    ) {
        this.idCategoriaProducto = idCategoriaProducto;
    }

    @Override
    public String toString() {
        return String.valueOf(
                codigoProducto
        );
    }
}
