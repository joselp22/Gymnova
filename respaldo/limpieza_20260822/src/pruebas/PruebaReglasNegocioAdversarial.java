package pruebas;

import controlador.AlcanceRolControlador;
import controlador.ClienteControlador;
import controlador.LoginControlador;
import controlador.MembresiaControlador;
import controlador.PersonaControlador;
import java.math.BigDecimal;
import java.time.LocalDate;
import modelo.Cliente;
import modelo.Membresia;
import modelo.Persona;
import utilidades.SesionUsuario;

/**
 * Pruebas negativas que protegen identidad, historial, alcance y estados.
 * Debe ejecutarse solamente contra la base clonada de auditoria.
 */
public final class PruebaReglasNegocioAdversarial {

    private static final String CLAVE = "Gymnova2026*";

    private PruebaReglasNegocioAdversarial() {
    }

    public static void main(String[] args) throws Exception {
        exigirBaseDeAuditoria();

        iniciarSesion("cliente", "Cliente");
        Long personaCliente = SesionUsuario.getUsuarioActual().getIdPersona();
        probarAlcanceDelCliente(personaCliente);
        probarEliminacionSinPermiso(personaCliente);

        iniciarSesion("admin", "Administrador");
        probarCedulaInmutable(personaCliente);
        probarProteccionDelHistorial(personaCliente);
        probarMembresiaAutomaticaInmutableYCancelacion(personaCliente);

        SesionUsuario.cerrarSesion();
        System.out.println(
                "REGLAS_ADVERSARIALES_OK="
                + "ALCANCE+SOLO_ADMIN+CEDULA_INMUTABLE+HISTORIAL_NO_ACTION+"
                + "CODIGO_AUTOMATICO+CODIGO_INMUTABLE+ESTADO_VALIDO");
    }

    private static void probarAlcanceDelCliente(Long personaCliente) {
        ClienteControlador clientes = new ClienteControlador();
        Cliente propio = null;
        Cliente ajeno = null;
        for (Cliente cliente : clientes.listar("")) {
            if (personaCliente.equals(cliente.getIdPersona())) {
                propio = cliente;
            } else if (ajeno == null) {
                ajeno = cliente;
            }
        }
        exigir(propio != null && ajeno != null,
                "La auditoria necesita al menos dos clientes.");

        AlcanceRolControlador alcance = new AlcanceRolControlador();
        exigir(alcance.puedeAcceder(propio, "Cliente", personaCliente),
                "El cliente no puede consultar su propio perfil.");
        exigir(!alcance.puedeAcceder(ajeno, "Cliente", personaCliente),
                "El cliente pudo consultar datos de otra persona.");
    }

    private static void probarEliminacionSinPermiso(Long personaCliente)
            throws Exception {
        boolean bloqueado = false;
        try {
            new PersonaControlador().eliminarDefinitivamente(personaCliente);
        } catch (SecurityException ex) {
            bloqueado = true;
        }
        exigir(bloqueado,
                "Un cliente pudo eliminar definitivamente una persona.");
    }

    private static void probarCedulaInmutable(Long personaCliente)
            throws Exception {
        PersonaControlador controlador = new PersonaControlador();
        Persona persona = controlador.buscarPorId(personaCliente).orElseThrow();
        String original = persona.getCedula();
        persona.setCedula("0999999999");

        boolean bloqueado = false;
        try {
            controlador.modificar(persona);
        } catch (IllegalArgumentException ex) {
            bloqueado = ex.getMessage().toLowerCase().contains("no se puede");
        }
        exigir(bloqueado, "La cedula pudo modificarse.");
        exigir(original.equals(controlador.buscarPorId(personaCliente)
                .orElseThrow().getCedula()),
                "La cedula almacenada cambio durante la prueba.");
    }

    private static void probarProteccionDelHistorial(Long personaCliente)
            throws Exception {
        boolean bloqueado = false;
        try {
            new PersonaControlador().eliminarDefinitivamente(personaCliente);
        } catch (IllegalStateException ex) {
            bloqueado = true;
        }
        exigir(bloqueado,
                "Se elimino una persona que conserva historial relacionado.");
    }

    private static void probarMembresiaAutomaticaInmutableYCancelacion(
            Long personaCliente) {
        MembresiaControlador controlador = new MembresiaControlador();
        Membresia membresia = new Membresia();
        membresia.setNumeroMembresia("CODIGO-MANUAL-PROHIBIDO");
        membresia.setFechaInicio(LocalDate.now());
        membresia.setFechaFin(LocalDate.now().plusDays(30));
        membresia.setCostoFinal(new BigDecimal("30.00"));
        membresia.setEstadoMembresia("ACTIVA");
        membresia.setObservaciones("Registro descartable de auditoria adversarial");
        membresia.setIdCliente(personaCliente);
        membresia.setIdTipoMembresia(1L);

        exigir(controlador.registrar(membresia), controlador.getMensaje());
        Membresia creada = controlador.listar("Registro descartable").stream()
                .filter(item -> personaCliente.equals(item.getIdCliente()))
                .reduce((primera, ultima) -> ultima)
                .orElseThrow(() -> new AssertionError(
                "No se encontro la membresia creada."));

        String codigoOriginal = creada.getNumeroMembresia();
        exigir(codigoOriginal != null && codigoOriginal.matches("MB\\d{5}"),
                "El codigo no fue generado con la regla MB00000: "
                + codigoOriginal);
        exigir(!"CODIGO-MANUAL-PROHIBIDO".equals(codigoOriginal),
                "Se acepto un codigo escrito manualmente.");

        creada.setNumeroMembresia("MB99999");
        exigir(!controlador.modificar(creada)
                && controlador.getMensaje().toLowerCase().contains("no se puede"),
                "Se permitio modificar el codigo automatico.");

        Membresia persistida = controlador.buscar(creada.getIdMembresia());
        exigir(codigoOriginal.equals(persistida.getNumeroMembresia()),
                "El codigo persistido fue alterado.");

        persistida.setEstadoMembresia("ESTADO_INVENTADO");
        exigir(!controlador.modificar(persistida),
                "Se acepto un estado inexistente.");

        exigir(controlador.desactivar(creada.getIdMembresia()),
                controlador.getMensaje());
        exigir("CANCELADA".equals(controlador.buscar(
                creada.getIdMembresia()).getEstadoMembresia()),
                "Desactivar no uso el estado CANCELADA permitido por PostgreSQL.");
    }

    private static void iniciarSesion(String usuario, String rol)
            throws Exception {
        SesionUsuario.cerrarSesion();
        new LoginControlador().iniciarSesion(usuario, CLAVE.toCharArray());
        exigir(rol.equalsIgnoreCase(
                SesionUsuario.getUsuarioActual().getNombreRol()),
                "El rol de la sesion no coincide para " + usuario + ".");
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
