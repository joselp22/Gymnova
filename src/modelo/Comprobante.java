package modelo;

import java.time.LocalDate;
/**
 * Modelo para la tabla comprobante.
 *
 * @author Usuario
 */
public class Comprobante {

    private Long idComprobante;
    private String numeroComprobante;
    private String tipoComprobante;
    private LocalDate fechaEmision;
    private String formatoArchivo;
    private String rutaArchivo;
    private String correoEnvio;
    private LocalDate fechaEnvio;
    private String estadoComprobante;
    private Long idPago;

    public Comprobante() {
    }


    public Long getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(
            Long idComprobante
    ) {
        this.idComprobante = idComprobante;
    }
    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(
            String numeroComprobante
    ) {
        this.numeroComprobante = numeroComprobante;
    }
    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(
            String tipoComprobante
    ) {
        this.tipoComprobante = tipoComprobante;
    }
    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(
            LocalDate fechaEmision
    ) {
        this.fechaEmision = fechaEmision;
    }
    public String getFormatoArchivo() {
        return formatoArchivo;
    }

    public void setFormatoArchivo(
            String formatoArchivo
    ) {
        this.formatoArchivo = formatoArchivo;
    }
    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(
            String rutaArchivo
    ) {
        this.rutaArchivo = rutaArchivo;
    }
    public String getCorreoEnvio() {
        return correoEnvio;
    }

    public void setCorreoEnvio(
            String correoEnvio
    ) {
        this.correoEnvio = correoEnvio;
    }
    public LocalDate getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(
            LocalDate fechaEnvio
    ) {
        this.fechaEnvio = fechaEnvio;
    }
    public String getEstadoComprobante() {
        return estadoComprobante;
    }

    public void setEstadoComprobante(
            String estadoComprobante
    ) {
        this.estadoComprobante = estadoComprobante;
    }
    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(
            Long idPago
    ) {
        this.idPago = idPago;
    }

    @Override
    public String toString() {
        return String.valueOf(
                numeroComprobante
        );
    }
}
