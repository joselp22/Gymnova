package modelo;

/**
 * Modelo para la tabla rutina_dia_entrenamiento.
 *
 * @author Usuario
 */
public class RutinaDiaEntrenamiento {

    private String diaEntrenamiento;
    private Long idRutina;

    public RutinaDiaEntrenamiento() {
    }


    public String getDiaEntrenamiento() {
        return diaEntrenamiento;
    }

    public void setDiaEntrenamiento(
            String diaEntrenamiento
    ) {
        this.diaEntrenamiento = diaEntrenamiento;
    }
    public Long getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(
            Long idRutina
    ) {
        this.idRutina = idRutina;
    }

    @Override
    public String toString() {
        return String.valueOf(
                diaEntrenamiento
        );
    }
}
