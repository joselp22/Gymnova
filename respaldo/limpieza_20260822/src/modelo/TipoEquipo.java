package modelo;

/**
 * Modelo para la tabla tipo_equipo.
 *
 * @author Usuario
 */
public class TipoEquipo {

    private Long idTipoEquipo;
    private String nombreTipo;
    private boolean requiereElectricidad;
    private Integer periodicidadBaseDias;

    public TipoEquipo() {
    }


    public Long getIdTipoEquipo() {
        return idTipoEquipo;
    }

    public void setIdTipoEquipo(
            Long idTipoEquipo
    ) {
        this.idTipoEquipo = idTipoEquipo;
    }
    public String getNombreTipo() {
        return nombreTipo;
    }

    public void setNombreTipo(
            String nombreTipo
    ) {
        this.nombreTipo = nombreTipo;
    }
    public boolean isRequiereElectricidad() {
        return requiereElectricidad;
    }

    public void setRequiereElectricidad(
            boolean requiereElectricidad
    ) {
        this.requiereElectricidad = requiereElectricidad;
    }
    public Integer getPeriodicidadBaseDias() {
        return periodicidadBaseDias;
    }

    public void setPeriodicidadBaseDias(
            Integer periodicidadBaseDias
    ) {
        this.periodicidadBaseDias = periodicidadBaseDias;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombreTipo
        );
    }
}
