import controlador.FacturaControlador;
import controlador.FacturaDocumentoControlador;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import modelo.Factura;
import modelo.FacturaDocumento;

/** Prueba integrada de lectura PostgreSQL y generacion de factura PDF. */
public class PruebaFacturaPDF {

    public static void main(String[] args) throws Exception {
        FacturaControlador facturas = new FacturaControlador();
        List<Factura> disponibles = facturas.listar("");
        if (disponibles.isEmpty()) {
            throw new IllegalStateException("No existen facturas para probar.");
        }

        FacturaDocumentoControlador controlador =
                new FacturaDocumentoControlador();
        Factura factura = disponibles.get(0);
        FacturaDocumento documento = controlador.cargar(factura.getIdFactura());
        if (documento == null || documento.getDetalles().isEmpty()) {
            throw new IllegalStateException(controlador.getMensaje());
        }

        Path destino = Path.of("output", "pdf",
                "factura_prueba_GYMNOVA.pdf").toAbsolutePath();
        Files.createDirectories(destino.getParent());
        if (controlador.exportar(factura.getIdFactura(), destino) == null) {
            throw new IllegalStateException(controlador.getMensaje());
        }
        byte[] contenido = Files.readAllBytes(destino);
        if (contenido.length < 1000
                || !new String(contenido, 0, 5).equals("%PDF-")) {
            throw new IllegalStateException("El archivo generado no es un PDF valido.");
        }

        System.out.println("BD_CONECTADA=SI");
        System.out.println("FACTURA=" + factura.getNumeroFactura());
        System.out.println("CLIENTE=" + documento.getCliente().getNombreCompleto());
        System.out.println("DETALLES=" + documento.getDetalles().size());
        System.out.println("TOTAL=" + controlador.calcularTotal(documento));
        System.out.println("PDF=" + destino);
        System.out.println("BYTES=" + contenido.length);
    }
}
