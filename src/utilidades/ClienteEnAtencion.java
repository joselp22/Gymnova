package utilidades;

import modelo.Cliente;

/**
 * Cliente actualmente en atencion durante la sesion de un profesional
 * (Recepcionista, Entrenador, Nutricionista). Mantiene el contexto
 * seleccionado entre paneles para evitar operaciones sobre un cliente
 * ambiguo. Se limpia automaticamente al cerrar sesion.
 */
public final class ClienteEnAtencion {

    private static Cliente cliente;

    private ClienteEnAtencion() {
    }

    public static void seleccionar(Cliente clienteSeleccionado) {
        cliente = clienteSeleccionado;
    }

    public static Cliente actual() {
        return cliente;
    }

    public static boolean hay() {
        return cliente != null;
    }

    public static Long idPersona() {
        return cliente == null ? null : cliente.getIdPersona();
    }

    public static String descripcion() {
        if (cliente == null) {
            return "Sin cliente en atencion";
        }
        return cliente.getNombres() + " " + cliente.getApellidos()
                + " (" + cliente.getCodigoCliente() + ")";
    }

    public static void limpiar() {
        cliente = null;
    }
}
