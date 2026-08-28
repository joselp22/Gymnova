package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
/**
 * Modelo para la tabla equipo.
 *
 * @author Usuario
 */
public class Equipo {

    private Long idEquipo;
    private String codigoInterno;
    private String nombreEquipo;
    private String marca;
    private String modelo;
    private LocalDate fechaAdquisicion;
    private BigDecimal costoAdquisicion;
    private Integer vidaUtilAnios;
    private String ubicacion;
    private boolean estadoEquipo;
    private Long idTipoEquipo;

    public Equipo() {
    }


    public Long getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(
            Long idEquipo
    ) {
        this.idEquipo = idEquipo;
    }
    public String getCodigoInterno() {
        return codigoInterno;
    }

    public void setCodigoInterno(
            String codigoInterno
    ) {
        this.codigoInterno = codigoInterno;
    }
    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public void setNombreEquipo(
            String nombreEquipo
    ) {
        this.nombreEquipo = nombreEquipo;
    }
    public String getMarca() {
        return marca;
    }

    public void setMarca(
            String marca
    ) {
        this.marca = marca;
    }
    public String getModelo() {
        return modelo;
    }

    public void setModelo(
            String modelo
    ) {
        this.modelo = modelo;
    }
    public LocalDate getFechaAdquisicion() {
        return fechaAdquisicion;
    }

    public void setFechaAdquisicion(
            LocalDate fechaAdquisicion
    ) {
        this.fechaAdquisicion = fechaAdquisicion;
    }
    public BigDecimal getCostoAdquisicion() {
        return costoAdquisicion;
    }

    public void setCostoAdquisicion(
            BigDecimal costoAdquisicion
    ) {
        this.costoAdquisicion = costoAdquisicion;
    }
    public Integer getVidaUtilAnios() {
        return vidaUtilAnios;
    }

    public void setVidaUtilAnios(
            Integer vidaUtilAnios
    ) {
        this.vidaUtilAnios = vidaUtilAnios;
    }
    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(
            String ubicacion
    ) {
        this.ubicacion = ubicacion;
    }
    public boolean isEstadoEquipo() {
        return estadoEquipo;
    }

    public void setEstadoEquipo(
            boolean estadoEquipo
    ) {
        this.estadoEquipo = estadoEquipo;
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
                codigoInterno
        );
    }
}
