package modelo;

/**
 * Modelo para la tabla categoria_producto.
 *
 * @author Usuario
 */
public class CategoriaProducto {

    private Long idCategoriaProducto;
    private String nombreCategoria;
    private String descripcion;
    private boolean requiereCaducidad;
    private boolean estadoCategoria;

    public CategoriaProducto() {
    }


    public Long getIdCategoriaProducto() {
        return idCategoriaProducto;
    }

    public void setIdCategoriaProducto(
            Long idCategoriaProducto
    ) {
        this.idCategoriaProducto = idCategoriaProducto;
    }
    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(
            String nombreCategoria
    ) {
        this.nombreCategoria = nombreCategoria;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public boolean isRequiereCaducidad() {
        return requiereCaducidad;
    }

    public void setRequiereCaducidad(
            boolean requiereCaducidad
    ) {
        this.requiereCaducidad = requiereCaducidad;
    }
    public boolean isEstadoCategoria() {
        return estadoCategoria;
    }

    public void setEstadoCategoria(
            boolean estadoCategoria
    ) {
        this.estadoCategoria = estadoCategoria;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombreCategoria
        );
    }
}
