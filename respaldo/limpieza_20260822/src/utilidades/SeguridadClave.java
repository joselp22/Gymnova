/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilidades;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
/**
 *
 * @author Usuario
 */

public final class SeguridadClave {

    private static final int ITERACIONES = 210000;
    private static final int LONGITUD_CLAVE = 256;
    private static final int LONGITUD_SAL = 16;

    private SeguridadClave() {
        // Evita crear objetos de esta clase.
    }

    public static String generarHash(char[] contrasena) {

        byte[] sal = new byte[LONGITUD_SAL];
        new SecureRandom().nextBytes(sal);

        byte[] hash = calcularHash(
                contrasena,
                sal,
                ITERACIONES
        );

        return ITERACIONES
                + ":"
                + HexFormat.of().formatHex(sal)
                + ":"
                + HexFormat.of().formatHex(hash);
    }

    public static boolean verificar(
            char[] contrasena,
            String hashGuardado
    ) {
        if (contrasena == null
                || hashGuardado == null
                || hashGuardado.isBlank()) {
            return false;
        }

        try {
            String[] partes = hashGuardado.split(":");

            if (partes.length != 3) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[0]);
            byte[] sal = HexFormat.of().parseHex(partes[1]);
            byte[] hashEsperado
                    = HexFormat.of().parseHex(partes[2]);

            byte[] hashCalculado = calcularHash(
                    contrasena,
                    sal,
                    iteraciones
            );

            return MessageDigest.isEqual(
                    hashEsperado,
                    hashCalculado
            );

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] calcularHash(
            char[] contrasena,
            byte[] sal,
            int iteraciones
    ) {
        PBEKeySpec especificacion = new PBEKeySpec(
                contrasena,
                sal,
                iteraciones,
                LONGITUD_CLAVE
        );

        try {
            SecretKeyFactory fabrica
                    = SecretKeyFactory.getInstance(
                            "PBKDF2WithHmacSHA256"
                    );

            return fabrica
                    .generateSecret(especificacion)
                    .getEncoded();

        } catch (NoSuchAlgorithmException
                | InvalidKeySpecException e) {

            throw new IllegalStateException(
                    "No se pudo proteger la contraseña.",
                    e
            );

        } finally {
            especificacion.clearPassword();
        }
    }
}
