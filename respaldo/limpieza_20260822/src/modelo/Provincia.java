package modelo;

/**
 * Modelo para la tabla provincia.
 *
 * @author Usuario
 */
public class Provincia {

    private Integer idProvincia;
    private String nombre;
    private boolean estado;

    public Provincia() {
        estado = true;
    }


    public Integer getIdProvincia() {
        return idProvincia;
    }

    public void setIdProvincia(
            Integer idProvincia
    ) {
        this.idProvincia = idProvincia;
    }
    public String getNombre() {
        return nombre;
    }

    public void setNombre(
            String nombre
    ) {
        this.nombre = nombre;
    }
    public boolean isEstado() {
        return estado;
    }

    public void setEstado(
            boolean estado
    ) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombre
        );
    }
}
