package modelo;

import java.time.LocalDate;
/**
 * Modelo para la tabla evaluacion_fisica.
 *
 * @author Usuario
 */
public class EvaluacionFisica {

    private String codigoEvaluacion;
    private LocalDate fechaEvaluacion;
    private String tipoEvaluacion;
    private String motivo;
    private String condicionGeneral;
    private String nivelRiesgo;
    private LocalDate proximaEvaluacion;
    private String estadoEvaluacion;
    private Long idEvaluacion;
    private Long idCliente;
    private Long idEntrenador;

    public EvaluacionFisica() {
    }


    public String getCodigoEvaluacion() {
        return codigoEvaluacion;
    }

    public void setCodigoEvaluacion(
            String codigoEvaluacion
    ) {
        this.codigoEvaluacion = codigoEvaluacion;
    }
    public LocalDate getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(
            LocalDate fechaEvaluacion
    ) {
        this.fechaEvaluacion = fechaEvaluacion;
    }
    public String getTipoEvaluacion() {
        return tipoEvaluacion;
    }

    public void setTipoEvaluacion(
            String tipoEvaluacion
    ) {
        this.tipoEvaluacion = tipoEvaluacion;
    }
    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(
            String motivo
    ) {
        this.motivo = motivo;
    }
    public String getCondicionGeneral() {
        return condicionGeneral;
    }

    public void setCondicionGeneral(
            String condicionGeneral
    ) {
        this.condicionGeneral = condicionGeneral;
    }
    public String getNivelRiesgo() {
        return nivelRiesgo;
    }

    public void setNivelRiesgo(
            String nivelRiesgo
    ) {
        this.nivelRiesgo = nivelRiesgo;
    }
    public LocalDate getProximaEvaluacion() {
        return proximaEvaluacion;
    }

    public void setProximaEvaluacion(
            LocalDate proximaEvaluacion
    ) {
        this.proximaEvaluacion = proximaEvaluacion;
    }
    public String getEstadoEvaluacion() {
        return estadoEvaluacion;
    }

    public void setEstadoEvaluacion(
            String estadoEvaluacion
    ) {
        this.estadoEvaluacion = estadoEvaluacion;
    }
    public Long getIdEvaluacion() {
        return idEvaluacion;
    }

    public void setIdEvaluacion(
            Long idEvaluacion
    ) {
        this.idEvaluacion = idEvaluacion;
    }
    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(
            Long idCliente
    ) {
        this.idCliente = idCliente;
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
                codigoEvaluacion
        );
    }
}
