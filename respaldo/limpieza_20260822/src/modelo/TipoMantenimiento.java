package modelo;

/**
 * Modelo para la tabla tipo_mantenimiento.
 *
 * @author Usuario
 */
public class TipoMantenimiento {

    private Long idTipoMantenimiento;
    private String nombreTipo;
    private boolean requiereRepuestos;
    private boolean requiereDetenerEquipo;
    private Integer periodicidadRecomendadaDias;

    public TipoMantenimiento() {
    }


    public Long getIdTipoMantenimiento() {
        return idTipoMantenimiento;
    }

    public void setIdTipoMantenimiento(
            Long idTipoMantenimiento
    ) {
        this.idTipoMantenimiento = idTipoMantenimiento;
    }
    public String getNombreTipo() {
        return nombreTipo;
    }

    public void setNombreTipo(
            String nombreTipo
    ) {
        this.nombreTipo = nombreTipo;
    }
    public boolean isRequiereRepuestos() {
        return requiereRepuestos;
    }

    public void setRequiereRepuestos(
            boolean requiereRepuestos
    ) {
        this.requiereRepuestos = requiereRepuestos;
    }
    public boolean isRequiereDetenerEquipo() {
        return requiereDetenerEquipo;
    }

    public void setRequiereDetenerEquipo(
            boolean requiereDetenerEquipo
    ) {
        this.requiereDetenerEquipo = requiereDetenerEquipo;
    }
    public Integer getPeriodicidadRecomendadaDias() {
        return periodicidadRecomendadaDias;
    }

    public void setPeriodicidadRecomendadaDias(
            Integer periodicidadRecomendadaDias
    ) {
        this.periodicidadRecomendadaDias = periodicidadRecomendadaDias;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombreTipo
        );
    }
}
