package modelo;

/**
 * Modelo para la tabla requiere_equipo.
 *
 * @author Usuario
 */
public class RequiereEquipo {

    private Integer cantidadRequerida;
    private boolean esObligatorio;
    private String alternativaSinEquipo;
    private String observaciones;
    private Long idEjercicio;
    private Long idTipoEquipo;

    public RequiereEquipo() {
    }


    public Integer getCantidadRequerida() {
        return cantidadRequerida;
    }

    public void setCantidadRequerida(
            Integer cantidadRequerida
    ) {
        this.cantidadRequerida = cantidadRequerida;
    }
    public boolean isEsObligatorio() {
        return esObligatorio;
    }

    public void setEsObligatorio(
            boolean esObligatorio
    ) {
        this.esObligatorio = esObligatorio;
    }
    public String getAlternativaSinEquipo() {
        return alternativaSinEquipo;
    }

    public void setAlternativaSinEquipo(
            String alternativaSinEquipo
    ) {
        this.alternativaSinEquipo = alternativaSinEquipo;
    }
    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }
    public Long getIdEjercicio() {
        return idEjercicio;
    }

    public void setIdEjercicio(
            Long idEjercicio
    ) {
        this.idEjercicio = idEjercicio;
    }
    public Long getIdTipoEquipo() {
        return idTipoEquipo;
    }

    public void setIdTipoEquipo(
            Long idTipoEquipo
    ) {
        this.idTipoEquipo = idTipoEquipo;
    }

    @Override
    public String toString() {
        return String.valueOf(
                alternativaSinEquipo
        );
    }
}
