package modelo;

import java.time.LocalDate;

/**
 * Proyeccion de solo lectura para la vista administrativa de rutinas.
 */
public class RutinaResumenAdministrador {

    private Long idRutina;
    private String nombreRutina;
    private String nombreEntrenador;
    private String nivel;
    private Integer duracionSemanas;
    private String estadoRutina;
    private LocalDate fechaCreacion;
    private Integer totalEjercicios;

    public Long getIdRutina() {
        return idRutina;
    }

    public void setIdRutina(Long idRutina) {
        this.idRutina = idRutina;
    }

    public String getNombreRutina() {
        return nombreRutina;
    }

    public void setNombreRutina(String nombreRutina) {
        this.nombreRutina = nombreRutina;
    }

    public String getNombreEntrenador() {
        return nombreEntrenador;
    }

    public void setNombreEntrenador(String nombreEntrenador) {
        this.nombreEntrenador = nombreEntrenador;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public Integer getDuracionSemanas() {
        return duracionSemanas;
    }

    public void setDuracionSemanas(Integer duracionSemanas) {
        this.duracionSemanas = duracionSemanas;
    }

    public String getEstadoRutina() {
        return estadoRutina;
    }

    public void setEstadoRutina(String estadoRutina) {
        this.estadoRutina = estadoRutina;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getTotalEjercicios() {
        return totalEjercicios;
    }

    public void setTotalEjercicios(Integer totalEjercicios) {
        this.totalEjercicios = totalEjercicios;
    }
}
