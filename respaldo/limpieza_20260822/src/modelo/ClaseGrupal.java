package modelo;

/**
 * Modelo para la tabla clase_grupal.
 *
 * @author Usuario
 */
public class ClaseGrupal {

    private Long idClase;
    private String nombreClase;
    private String descripcion;
    private String nivel;
    private Integer duracionBaseMinutos;
    private String intensidad;
    private boolean estadoClase;

    public ClaseGrupal() {
    }


    public Long getIdClase() {
        return idClase;
    }

    public void setIdClase(
            Long idClase
    ) {
        this.idClase = idClase;
    }
    public String getNombreClase() {
        return nombreClase;
    }

    public void setNombreClase(
            String nombreClase
    ) {
        this.nombreClase = nombreClase;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public String getNivel() {
        return nivel;
    }

    public void setNivel(
            String nivel
    ) {
        this.nivel = nivel;
    }
    public Integer getDuracionBaseMinutos() {
        return duracionBaseMinutos;
    }

    public void setDuracionBaseMinutos(
            Integer duracionBaseMinutos
    ) {
        this.duracionBaseMinutos = duracionBaseMinutos;
    }
    public String getIntensidad() {
        return intensidad;
    }

    public void setIntensidad(
            String intensidad
    ) {
        this.intensidad = intensidad;
    }
    public boolean isEstadoClase() {
        return estadoClase;
    }

    public void setEstadoClase(
            boolean estadoClase
    ) {
        this.estadoClase = estadoClase;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombreClase
        );
    }
}
