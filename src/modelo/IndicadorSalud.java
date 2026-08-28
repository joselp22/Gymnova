package modelo;

import java.math.BigDecimal;
/**
 * Modelo para la tabla indicador_salud.
 *
 * @author Usuario
 */
public class IndicadorSalud {

    private Long idIndicador;
    private String descripcion;
    private String unidadMedida;
    private BigDecimal valorMinimoReferencia;
    private BigDecimal valorMaximoReferencia;
    private String categoria;
    private boolean estadoIndicador;
    private String nombreIndicador;

    public IndicadorSalud() {
    }


    public Long getIdIndicador() {
        return idIndicador;
    }

    public void setIdIndicador(
            Long idIndicador
    ) {
        this.idIndicador = idIndicador;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(
            String unidadMedida
    ) {
        this.unidadMedida = unidadMedida;
    }
    public BigDecimal getValorMinimoReferencia() {
        return valorMinimoReferencia;
    }

    public void setValorMinimoReferencia(
            BigDecimal valorMinimoReferencia
    ) {
        this.valorMinimoReferencia = valorMinimoReferencia;
    }
    public BigDecimal getValorMaximoReferencia() {
        return valorMaximoReferencia;
    }

    public void setValorMaximoReferencia(
            BigDecimal valorMaximoReferencia
    ) {
        this.valorMaximoReferencia = valorMaximoReferencia;
    }
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(
            String categoria
    ) {
        this.categoria = categoria;
    }
    public boolean isEstadoIndicador() {
        return estadoIndicador;
    }

    public void setEstadoIndicador(
            boolean estadoIndicador
    ) {
        this.estadoIndicador = estadoIndicador;
    }
    public String getNombreIndicador() {
        return nombreIndicador;
    }

    public void setNombreIndicador(
            String nombreIndicador
    ) {
        this.nombreIndicador = nombreIndicador;
    }

    @Override
    public String toString() {
        return String.valueOf(
                descripcion
        );
    }
}
