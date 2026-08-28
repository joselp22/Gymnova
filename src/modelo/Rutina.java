package modelo;

import java.time.LocalDate;
/**
 * Modelo para la tabla rutina.
 *
 * @author Usuario
 */
public class Rutina {

    private Long idRutina;
    private String nombreRutina;
    private String descripcion;
    private String nivel;
    private Integer duracionSemanas;
    private LocalDate fechaCreacion;
    private String estadoRutina;
    private Long idEntrenador;

    public Rutina() {
    }


    public Long getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(
            Long idRutina
    ) {
        this.idRutina = idRutina;
    }
    public String getNombreRutina() {
        return nombreRutina;
    }

    public void setNombreRutina(
            String nombreRutina
    ) {
        this.nombreRutina = nombreRutina;
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
    public Integer getDuracionSemanas() {
        return duracionSemanas;
    }

    public void setDuracionSemanas(
            Integer duracionSemanas
    ) {
        this.duracionSemanas = duracionSemanas;
    }
    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(
            LocalDate fechaCreacion
    ) {
        this.fechaCreacion = fechaCreacion;
    }
    public String getEstadoRutina() {
        return estadoRutina;
    }

    public void setEstadoRutina(
            String estadoRutina
    ) {
        this.estadoRutina = estadoRutina;
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
                nombreRutina
        );
    }
}
