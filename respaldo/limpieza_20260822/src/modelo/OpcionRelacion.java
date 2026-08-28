package modelo;

/** Elemento legible de un selector de llave foranea. */
public class OpcionRelacion {

    private final Object id;
    private final String descripcion;

    public OpcionRelacion(Object id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public Object getId() {
        return id;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
