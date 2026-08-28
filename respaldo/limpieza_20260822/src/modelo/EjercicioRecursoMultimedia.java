package modelo;

/**
 * Modelo para la tabla ejercicio_recurso_multimedia.
 *
 * @author Usuario
 */
public class EjercicioRecursoMultimedia {

    private String recursoMultimedia;
    private Long idEjercicio;

    public EjercicioRecursoMultimedia() {
    }


    public String getRecursoMultimedia() {
        return recursoMultimedia;
    }

    public void setRecursoMultimedia(
            String recursoMultimedia
    ) {
        this.recursoMultimedia = recursoMultimedia;
    }
    public Long getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(
            Long idEjercicio
    ) {
        this.idEjercicio = idEjercicio;
    }

    @Override
    public String toString() {
        return String.valueOf(
                recursoMultimedia
        );
    }
}
