package modelo;

/**
 * Modelo para la tabla objetivo_fitness.
 *
 * @author Usuario
 */
public class ObjetivoFitness {

    private Long idObjetivo;
    private String nombreObjetivo;
    private String descripcion;
    private String categoria;
    private boolean estadoObjetivo;

    public ObjetivoFitness() {
    }


    public Long getIdObjetivo() {
        return idObjetivo;
    }

    public void setIdObjetivo(
            Long idObjetivo
    ) {
        this.idObjetivo = idObjetivo;
    }
    public String getNombreObjetivo() {
        return nombreObjetivo;
    }

    public void setNombreObjetivo(
            String nombreObjetivo
    ) {
        this.nombreObjetivo = nombreObjetivo;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(
            String categoria
    ) {
        this.categoria = categoria;
    }
    public boolean isEstadoObjetivo() {
        return estadoObjetivo;
    }

    public void setEstadoObjetivo(
            boolean estadoObjetivo
    ) {
        this.estadoObjetivo = estadoObjetivo;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombreObjetivo
        );
    }
}
