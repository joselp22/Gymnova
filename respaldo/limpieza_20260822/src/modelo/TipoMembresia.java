package modelo;

import java.math.BigDecimal;
/**
 * Modelo para la tabla tipo_membresia.
 *
 * @author Usuario
 */
public class TipoMembresia {

    private Long idTipoMembresia;
    private String nombre;
    private String descripcion;
    private Integer duracionDias;
    private BigDecimal precioBase;
    private Integer limiteAccesos;
    private boolean accesoIlimitado;
    private boolean estadoTipo;

    public TipoMembresia() {
    }


    public Long getIdTipoMembresia() {
        return idTipoMembresia;
    }

    public void setIdTipoMembresia(
            Long idTipoMembresia
    ) {
        this.idTipoMembresia = idTipoMembresia;
    }
    public String getNombre() {
        return nombre;
    }

    public void setNombre(
            String nombre
    ) {
        this.nombre = nombre;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public Integer getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(
            Integer duracionDias
    ) {
        this.duracionDias = duracionDias;
    }
    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(
            BigDecimal precioBase
    ) {
        this.precioBase = precioBase;
    }
    public Integer getLimiteAccesos() {
        return limiteAccesos;
    }

    public void setLimiteAccesos(
            Integer limiteAccesos
    ) {
        this.limiteAccesos = limiteAccesos;
    }
    public boolean isAccesoIlimitado() {
        return accesoIlimitado;
    }

    public void setAccesoIlimitado(
            boolean accesoIlimitado
    ) {
        this.accesoIlimitado = accesoIlimitado;
    }
    public boolean isEstadoTipo() {
        return estadoTipo;
    }

    public void setEstadoTipo(
            boolean estadoTipo
    ) {
        this.estadoTipo = estadoTipo;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombre
        );
    }
}
