package modelo;

/**
 * Modelo para la tabla ejercicio.
 *
 * @author Usuario
 */
public class Ejercicio {

    private Long idEjercicio;
    private String nombreEjercicio;
    private String descripcion;
    private String tipoEjercicio;
    private String nivelDificultad;
    private String instrucciones;
    private Long idEntrenador;

    public Ejercicio() {
    }


    public Long getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(
            Long idEjercicio
    ) {
        this.idEjercicio = idEjercicio;
    }
    public String getNombreEjercicio() {
        return nombreEjercicio;
    }

    public void setNombreEjercicio(
            String nombreEjercicio
    ) {
        this.nombreEjercicio = nombreEjercicio;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public String getTipoEjercicio() {
        return tipoEjercicio;
    }

    public void setTipoEjercicio(
            String tipoEjercicio
    ) {
        this.tipoEjercicio = tipoEjercicio;
    }
    public String getNivelDificultad() {
        return nivelDificultad;
    }

    public void setNivelDificultad(
            String nivelDificultad
    ) {
        this.nivelDificultad = nivelDificultad;
    }
    public String getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(
            String instrucciones
    ) {
        this.instrucciones = instrucciones;
    }


    public Long getIdEntrenador() {
        return idEntrenador;
    }

    public void setIdEntrenador(
            Long idEntrenador
    ) {
        this.idEntrenador = idEntrenador;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombreEjercicio
        );
    }
}
