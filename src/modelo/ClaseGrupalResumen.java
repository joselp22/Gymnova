package modelo;

import java.time.LocalDateTime;

/**
 * Resumen de una clase grupal para las vistas de administrador, entrenador y cliente.
 */
public class ClaseGrupalResumen {

    private Long idClase;
    private String nombreClase;
    private String descripcion;
    private String nivel;
    private String intensidad;
    private Integer duracionMinutos;
    private Long idEntrenador;
    private String nombreEntrenador;
    private Integer cupoMaximo;
    private Integer reservados;
    private LocalDateTime fechaHora;
    private boolean estadoClase;
    private Long idReservaCliente;
    private String estadoReservaCliente;

    public Long getIdClase() { return idClase; }
    public void setIdClase(Long idClase) { this.idClase = idClase; }
    public String getNombreClase() { return nombreClase; }
    public void setNombreClase(String nombreClase) { this.nombreClase = nombreClase; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
    public String getIntensidad() { return intensidad; }
    public void setIntensidad(String intensidad) { this.intensidad = intensidad; }
    public Integer getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(Integer duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public Long getIdEntrenador() { return idEntrenador; }
    public void setIdEntrenador(Long idEntrenador) { this.idEntrenador = idEntrenador; }
    public String getNombreEntrenador() { return nombreEntrenador; }
    public void setNombreEntrenador(String nombreEntrenador) { this.nombreEntrenador = nombreEntrenador; }
    public Integer getCupoMaximo() { return cupoMaximo; }
    public void setCupoMaximo(Integer cupoMaximo) { this.cupoMaximo = cupoMaximo; }
    public Integer getReservados() { return reservados; }
    public void setReservados(Integer reservados) { this.reservados = reservados; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public boolean isEstadoClase() { return estadoClase; }
    public void setEstadoClase(boolean estadoClase) { this.estadoClase = estadoClase; }
    public Long getIdReservaCliente() { return idReservaCliente; }
    public void setIdReservaCliente(Long idReservaCliente) { this.idReservaCliente = idReservaCliente; }
    public String getEstadoReservaCliente() { return estadoReservaCliente; }
    public void setEstadoReservaCliente(String estadoReservaCliente) { this.estadoReservaCliente = estadoReservaCliente; }

    public int getDisponibles() {
        int maximo = cupoMaximo == null ? 0 : cupoMaximo;
        int ocupados = reservados == null ? 0 : reservados;
        return Math.max(0, maximo - ocupados);
    }

    public boolean isClienteInscrito() {
        return idReservaCliente != null && "ACTIVA".equalsIgnoreCase(estadoReservaCliente);
    }
}
