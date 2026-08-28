package modelo;

/**
 * Modelo para la tabla tipo_membresia_beneficio.
 *
 * @author Usuario
 */
public class TipoMembresiaBeneficio {

    private String beneficio;
    private Long idTipoMembresia;

    public TipoMembresiaBeneficio() {
    }


    public String getBeneficio() {
        return beneficio;
    }

    public void setBeneficio(
            String beneficio
    ) {
        this.beneficio = beneficio;
    }
    public Long getIdTipoMembresia() {
        return idTipoMembresia;
    }

    public void setIdTipoMembresia(
            Long idTipoMembresia
    ) {
        this.idTipoMembresia = idTipoMembresia;
    }

    @Override
    public String toString() {
        return String.valueOf(
                beneficio
        );
    }
}
