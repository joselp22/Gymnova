package modelo;

import java.math.BigDecimal;
/**
 * Modelo para la tabla alimento.
 *
 * @author Usuario
 */
public class Alimento {

    private Long idAlimento;
    private String nombreAlimento;
    private String categoria;
    private String descripcion;
    private BigDecimal porcionReferenciaG;
    private BigDecimal proteinasG;
    private BigDecimal carbohidratosG;
    private BigDecimal fibraG;

    public Alimento() {
    }


    public Long getIdAlimento() {
        return idAlimento;
    }

    public void setIdAlimento(
            Long idAlimento
    ) {
        this.idAlimento = idAlimento;
    }
    public String getNombreAlimento() {
        return nombreAlimento;
    }

    public void setNombreAlimento(
            String nombreAlimento
    ) {
        this.nombreAlimento = nombreAlimento;
    }
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(
            String categoria
    ) {
        this.categoria = categoria;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public BigDecimal getPorcionReferenciaG() {
        return porcionReferenciaG;
    }

    public void setPorcionReferenciaG(
            BigDecimal porcionReferenciaG
    ) {
        this.porcionReferenciaG = porcionReferenciaG;
    }
    public BigDecimal getProteinasG() {
        return proteinasG;
    }

    public void setProteinasG(
            BigDecimal proteinasG
    ) {
        this.proteinasG = proteinasG;
    }
    public BigDecimal getCarbohidratosG() {
        return carbohidratosG;
    }

    public void setCarbohidratosG(
            BigDecimal carbohidratosG
    ) {
        this.carbohidratosG = carbohidratosG;
    }
    public BigDecimal getFibraG() {
        return fibraG;
    }

    public void setFibraG(
            BigDecimal fibraG
    ) {
        this.fibraG = fibraG;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombreAlimento
        );
    }
}
