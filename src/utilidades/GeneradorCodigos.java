package utilidades;

/**
 * Generador centralizado para codigos internos de GYMNOVA.
 *
 * @author Usuario
 */
public class GeneradorCodigos {

    public static final String PREFIJO_CLIENTE = "CL";
    public static final String PREFIJO_EMPLEADO = "EM";
    public static final String PREFIJO_DESCUENTO = "DS";
    public static final String PREFIJO_EQUIPO = "EQ";
    public static final String PREFIJO_EVALUACION_FISICA = "EV";
    public static final String PREFIJO_PAGO = "PG";
    public static final String PREFIJO_PLAN_NUTRICIONAL = "PN";
    public static final String PREFIJO_PRODUCTO = "PR";
    public static final String PREFIJO_RESERVA = "RS";

    public static final int LONGITUD_NUMERICA = 5;
    public static final int VALOR_INICIAL = 1;
    public static final int VALOR_MAXIMO = 99999;

    public static String generarSiguienteCodigo(
            String prefijo,
            String ultimoCodigo
    ) {

        String prefijoNormalizado
                = normalizarPrefijo(
                        prefijo
                );

        int siguienteNumero
                = VALOR_INICIAL;

        if (ultimoCodigo != null
                && !ultimoCodigo.isBlank()) {

            siguienteNumero
                    = obtenerSufijoNumerico(
                            ultimoCodigo,
                            prefijoNormalizado
                    ) + 1;
        }

        return construirCodigo(
                prefijoNormalizado,
                siguienteNumero
        );
    }

    public static String construirCodigo(
            String prefijo,
            int numero
    ) {

        String prefijoNormalizado
                = normalizarPrefijo(
                        prefijo
                );

        if (numero < VALOR_INICIAL
                || numero > VALOR_MAXIMO) {

            throw new IllegalArgumentException(
                    "El consecutivo del codigo debe estar "
                    + "entre 00001 y 99999."
            );
        }

        return prefijoNormalizado
                + String.format(
                        "%05d",
                        numero
                );
    }

    public static int obtenerSufijoNumerico(
            String codigo,
            String prefijo
    ) {

        String prefijoNormalizado
                = normalizarPrefijo(
                        prefijo
                );

        if (!esCodigoValido(
                codigo,
                prefijoNormalizado
        )) {

            throw new IllegalArgumentException(
                    "El codigo no cumple el formato requerido."
            );
        }

        return Integer.parseInt(
                codigo.substring(
                        prefijoNormalizado.length()
                )
        );
    }

    public static boolean esCodigoValido(
            String codigo,
            String prefijo
    ) {

        if (codigo == null) {
            return false;
        }

        String prefijoNormalizado
                = normalizarPrefijo(
                        prefijo
                );

        String codigoNormalizado
                = codigo.trim().toUpperCase();

        return codigoNormalizado.matches(
                "^"
                + prefijoNormalizado
                + "[0-9]{"
                + LONGITUD_NUMERICA
                + "}$"
        );
    }

    public static String crearPatronPostgreSQL(
            String prefijo
    ) {

        String prefijoNormalizado
                = normalizarPrefijo(
                        prefijo
                );

        return "^"
                + prefijoNormalizado
                + "[0-9]{"
                + LONGITUD_NUMERICA
                + "}$";
    }

    public static long crearClaveBloqueoAdvisory(
            String texto
    ) {

        if (texto == null
                || texto.isBlank()) {

            throw new IllegalArgumentException(
                    "El texto para generar la clave "
                    + "de bloqueo es obligatorio."
            );
        }

        long clave = 1125899906842597L;

        String textoNormalizado
                = texto.trim().toUpperCase();

        for (int i = 0; i < textoNormalizado.length(); i++) {

            clave = 31 * clave
                    + textoNormalizado.charAt(i);
        }

        return clave;
    }

    private static String normalizarPrefijo(
            String prefijo
    ) {

        if (prefijo == null
                || prefijo.isBlank()) {

            throw new IllegalArgumentException(
                    "El prefijo del codigo es obligatorio."
            );
        }

        String prefijoNormalizado
                = prefijo.trim().toUpperCase();

        if (!prefijoNormalizado.matches(
                "[A-Z]{2}"
        )) {

            throw new IllegalArgumentException(
                    "El prefijo debe tener exactamente "
                    + "dos letras mayusculas."
            );
        }

        return prefijoNormalizado;
    }

    private GeneradorCodigos() {
    }
}
