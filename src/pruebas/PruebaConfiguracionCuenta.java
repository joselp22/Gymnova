package pruebas;

import controlador.LoginControlador;
import controlador.PersonaControlador;
import controlador.UsuarioControlador;
import java.io.InputStream;
import java.util.Arrays;
import modelo.Persona;
import modelo.Usuario;
import utilidades.SesionUsuario;

/** Prueba cambio de alias, clave, contacto y avatar, restaurando al finalizar. */
public final class PruebaConfiguracionCuenta {

    private static final String USUARIO = "cliente";
    private static final String CLAVE = "Gymnova123*";
    private static final String CLAVE_TEMPORAL = "Temporal2026*";

    private PruebaConfiguracionCuenta() {
    }

    public static void main(String[] args) throws Exception {
        exigirBaseDeAuditoria();
        LoginControlador login = new LoginControlador();
        UsuarioControlador usuarios = new UsuarioControlador();
        PersonaControlador personas = new PersonaControlador();

        Usuario sesion = login.iniciarSesion(USUARIO, CLAVE.toCharArray());
        Long idUsuario = sesion.getIdUsuario();
        Long idPersona = sesion.getIdPersona();
        Usuario originalUsuario = usuarios.buscar(idUsuario);
        Persona originalPersona = personas.buscarPorId(idPersona).orElseThrow();

        String aliasOriginal = originalUsuario.getNombreUsuario();
        String correoOriginal = originalPersona.getCorreo();
        String telefonoOriginal = originalPersona.getTelefono();
        byte[] avatarOriginal = originalPersona.getFotoPerfil() == null
                ? null : originalPersona.getFotoPerfil().clone();
        String aliasTemporal = "cliente_auditoria";
        boolean aliasCambiado = false;
        boolean claveCambiada = false;

        try {
            originalUsuario.setNombreUsuario(aliasTemporal);
            exigir(usuarios.modificar(originalUsuario), usuarios.getMensaje());
            aliasCambiado = true;

            SesionUsuario.cerrarSesion();
            sesion = login.iniciarSesion(aliasTemporal, CLAVE.toCharArray());
            exigir(sesion != null, "No fue posible entrar con el nuevo alias.");

            exigir(usuarios.cambiarClave(idUsuario,
                    CLAVE_TEMPORAL.toCharArray()), usuarios.getMensaje());
            claveCambiada = true;
            SesionUsuario.cerrarSesion();
            exigir(login.iniciarSesion(aliasTemporal,
                    CLAVE_TEMPORAL.toCharArray()) != null,
                    "No fue posible entrar con la nueva contraseña.");

            Persona editada = personas.buscarPorId(idPersona).orElseThrow();
            editada.setCorreo("cliente.auditoria@gymnova.test");
            editada.setTelefono("0991234567");
            try (InputStream recurso = PruebaConfiguracionCuenta.class
                    .getResourceAsStream("/recursos/avatar_neutro.png")) {
                exigir(recurso != null, "No existe el avatar neutro.");
                editada.setFotoPerfil(recurso.readAllBytes());
            }
            personas.modificar(editada);

            Persona comprobada = personas.buscarPorId(idPersona).orElseThrow();
            exigir("cliente.auditoria@gymnova.test".equals(
                    comprobada.getCorreo()), "El correo no se actualizo.");
            exigir("0991234567".equals(comprobada.getTelefono()),
                    "El telefono no se actualizo.");
            exigir(comprobada.getFotoPerfil() != null
                    && comprobada.getFotoPerfil().length > 0,
                    "El avatar no se almaceno.");
        } finally {
            Persona restaurada = personas.buscarPorId(idPersona).orElse(null);
            if (restaurada != null) {
                restaurada.setCorreo(correoOriginal);
                restaurada.setTelefono(telefonoOriginal);
                restaurada.setFotoPerfil(avatarOriginal);
                personas.modificar(restaurada);
            }
            if (claveCambiada) {
                usuarios.cambiarClave(idUsuario, CLAVE.toCharArray());
            }
            if (aliasCambiado) {
                Usuario restaurarUsuario = usuarios.buscar(idUsuario);
                restaurarUsuario.setNombreUsuario(aliasOriginal);
                exigir(usuarios.modificar(restaurarUsuario),
                        usuarios.getMensaje());
            }
            SesionUsuario.cerrarSesion();
        }

        Usuario finalSesion = login.iniciarSesion(USUARIO, CLAVE.toCharArray());
        exigir(finalSesion != null, "No se restauraron las credenciales originales.");
        Persona finalPersona = personas.buscarPorId(idPersona).orElseThrow();
        exigir(correoOriginal.equals(finalPersona.getCorreo())
                && telefonoOriginal.equals(finalPersona.getTelefono())
                && Arrays.equals(avatarOriginal, finalPersona.getFotoPerfil()),
                "No se restauro el perfil despues de la prueba.");
        SesionUsuario.cerrarSesion();
        System.out.println(
                "CONFIGURACION_CUENTA_OK=ALIAS+CLAVE+CONTACTO+AVATAR+RESTAURACION");
    }

    private static void exigirBaseDeAuditoria() {
        String url = System.getProperty("gymnova.db.url", "");
        if (!url.toLowerCase().contains("auditoria")) {
            throw new IllegalStateException(
                    "Esta prueba solo puede ejecutarse contra una base de auditoria.");
        }
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
