package modelo;

/**
 * Modelo para la tabla alimento_alergeno.
 *
 * @author Usuario
 */
public class AlimentoAlergeno {

    private String alergeno;
    private Long idAlimento;

    public AlimentoAlergeno() {
    }


    public String getAlergeno() {
        return alergeno;
    }

    public void setAlergeno(
            String alergeno
    ) {
        this.alergeno = alergeno;
    }
    public Long getIdAlimento() {
        return idAlimento;
    }

    public void setIdAlimento(
            Long idAlimento
    ) {
        this.idAlimento = idAlimento;
    }

    @Override
    public String toString() {
        return String.valueOf(
                alergeno
        );
    }
}
