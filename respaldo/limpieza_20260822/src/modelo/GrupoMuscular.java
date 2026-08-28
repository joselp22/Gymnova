package modelo;

/**
 * Modelo para la tabla grupo_muscular.
 *
 * @author Usuario
 */
public class GrupoMuscular {

    private Long idGrupoMuscular;
    private String nombreGrupo;
    private String zonaCorporal;
    private String descripcion;
    private boolean estadoGrupo;

    public GrupoMuscular() {
    }


    public Long getIdGrupoMuscular() {
        return idGrupoMuscular;
    }

    public void setIdGrupoMuscular(
            Long idGrupoMuscular
    ) {
        this.idGrupoMuscular = idGrupoMuscular;
    }
    public String getNombreGrupo() {
        return nombreGrupo;
    }

    public void setNombreGrupo(
            String nombreGrupo
    ) {
        this.nombreGrupo = nombreGrupo;
    }
    public String getZonaCorporal() {
        return zonaCorporal;
    }

    public void setZonaCorporal(
            String zonaCorporal
    ) {
        this.zonaCorporal = zonaCorporal;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(
            String descripcion
    ) {
        this.descripcion = descripcion;
    }
    public boolean isEstadoGrupo() {
        return estadoGrupo;
    }

    public void setEstadoGrupo(
            boolean estadoGrupo
    ) {
        this.estadoGrupo = estadoGrupo;
    }

    @Override
    public String toString() {
        return String.valueOf(
                nombreGrupo
        );
    }
}
