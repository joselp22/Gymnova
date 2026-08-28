/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilidades;

import java.time.LocalDate;
import java.util.regex.Pattern;
/**
 *
 * @author Usuario
 */

public final class Validaciones {

    private static final Pattern PATRON_NOMBRE
            = Pattern.compile("^[\\p{L} ]+$");

    private static final Pattern PATRON_CORREO
            = Pattern.compile(
                    "^[A-Za-z0-9._%+-]+"
                    + "@[A-Za-z0-9.-]+"
                    + "\\.[A-Za-z]{2,}$"
            );

    private Validaciones() {
    }

    public static void validarTextoObligatorio(
            String valor,
            String nombreCampo
    ) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                    nombreCampo + " es obligatorio."
            );
        }
    }

    public static void validarCedulaEcuatoriana(
            String cedula
    ) {
        if (cedula == null
                || !cedula.matches("\\d{10}")) {

            throw new IllegalArgumentException(
                    "La cédula debe contener 10 dígitos."
            );
        }

        int provincia = Integer.parseInt(
                cedula.substring(0, 2)
        );

        if (provincia < 1 || provincia > 24) {
            throw new IllegalArgumentException(
                    "El código de provincia de la cédula "
                    + "no es válido."
            );
        }

        int tercerDigito
                = Character.getNumericValue(
                        cedula.charAt(2)
                );

        if (tercerDigito >= 6) {
            throw new IllegalArgumentException(
                    "La cédula no corresponde "
                    + "a una persona natural."
            );
        }

        int suma = 0;

        for (int i = 0; i < 9; i++) {

            int digito = Character.getNumericValue(
                    cedula.charAt(i)
            );

            if (i % 2 == 0) {
                digito *= 2;

                if (digito > 9) {
                    digito -= 9;
                }
            }

            suma += digito;
        }

        int digitoVerificador
                = (10 - (suma % 10)) % 10;

        int ultimoDigito
                = Character.getNumericValue(
                        cedula.charAt(9)
                );

        if (digitoVerificador != ultimoDigito) {
            throw new IllegalArgumentException(
                    "La cédula ecuatoriana no es válida."
            );
        }
    }

    public static void validarNombre(
            String valor,
            String nombreCampo
    ) {
        validarTextoObligatorio(
                valor,
                nombreCampo
        );

        String texto = valor.trim();

        if (texto.length() < 2
                || texto.length() > 80) {

            throw new IllegalArgumentException(
                    nombreCampo
                    + " debe contener entre 2 y 80 caracteres."
            );
        }

        if (!PATRON_NOMBRE.matcher(texto).matches()) {
            throw new IllegalArgumentException(
                    nombreCampo
                    + " solamente puede contener letras y espacios."
            );
        }
    }

    public static void validarTelefono(
            String telefono
    ) {
        if (telefono == null
                || !telefono.matches("\\d{10}")) {

            throw new IllegalArgumentException(
                    "El teléfono debe contener 10 dígitos."
            );
        }
    }

    public static void validarCorreo(
            String correo
    ) {
        validarTextoObligatorio(
                correo,
                "El correo"
        );

        if (correo.length() > 120
                || !PATRON_CORREO
                        .matcher(correo.trim())
                        .matches()) {

            throw new IllegalArgumentException(
                    "Ingrese un correo electrónico válido."
            );
        }
    }

    public static void validarFechaNacimiento(
            LocalDate fecha
    ) {
        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento es obligatoria."
            );
        }

        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento "
                    + "no puede ser futura."
            );
        }

        if (fecha.isBefore(
                LocalDate.now().minusYears(120)
        )) {
            throw new IllegalArgumentException(
                    "La fecha de nacimiento "
                    + "no parece válida."
            );
        }
    }

    public static void validarSexo(String sexo) {

        if (!"Masculino".equals(sexo)
                && !"Femenino".equals(sexo)
                && !"Otro".equals(sexo)) {

            throw new IllegalArgumentException(
                    "Seleccione un sexo válido."
            );
        }
    }
}
