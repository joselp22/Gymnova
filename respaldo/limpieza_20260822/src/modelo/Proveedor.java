package modelo;

import java.time.LocalDate;
/**
 * Modelo para la tabla proveedor.
 *
 * @author Usuario
 */
public class Proveedor {

    private Long idProveedor;
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String correo;
    private String direccionProveedor;
    private String nombreContacto;
    private LocalDate fechaRegistro;

    public Proveedor() {
    }


    public Long getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(
            Long idProveedor
    ) {
        this.idProveedor = idProveedor;
    }
    public String getRuc() {
        return ruc;
    }

    public void setRuc(
            String ruc
    ) {
        this.ruc = ruc;
    }
    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(
            String razonSocial
    ) {
        this.razonSocial = razonSocial;
    }
    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(
            String nombreComercial
    ) {
        this.nombreComercial = nombreComercial;
    }
    public String getCorreo() {
        return correo;
    }

    public void setCorreo(
            String correo
    ) {
        this.correo = correo;
    }
    public String getDireccionProveedor() {
        return direccionProveedor;
    }

    public void setDireccionProveedor(
            String direccionProveedor
    ) {
        this.direccionProveedor = direccionProveedor;
    }
    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(
            String nombreContacto
    ) {
        this.nombreContacto = nombreContacto;
    }
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            LocalDate fechaRegistro
    ) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return String.valueOf(
                razonSocial
        );
    }
}
