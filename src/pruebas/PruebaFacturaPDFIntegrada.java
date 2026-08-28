package pruebas;

import controlador.FacturaControlador;
import controlador.FacturaDocumentoControlador;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import modelo.Factura;
import modelo.FacturaDocumento;

/** Verifica lectura, calculo y exportacion PDF de una factura real. */
public class PruebaFacturaPDFIntegrada {
    public static void main(String[] args) throws Exception {
        List<Factura> facturas=new FacturaControlador().listar("");
        if(facturas.isEmpty()) throw new IllegalStateException("No existen facturas para probar.");
        FacturaDocumentoControlador c=new FacturaDocumentoControlador();
        Factura factura=facturas.get(0);
        FacturaDocumento documento=c.cargar(factura.getIdFactura());
        if(documento==null||documento.getDetalles().isEmpty()) throw new IllegalStateException(c.getMensaje());
        Path destino=Path.of("output","pdf","factura_prueba_GYMNOVA.pdf").toAbsolutePath();
        Files.createDirectories(destino.getParent());
        if(c.exportar(factura.getIdFactura(),destino)==null) throw new IllegalStateException(c.getMensaje());
        byte[] contenido=Files.readAllBytes(destino);
        if(contenido.length<1000||contenido[0]!='%'||contenido[1]!='P'||contenido[2]!='D'||contenido[3]!='F')
            throw new IllegalStateException("El archivo generado no es un PDF valido.");
        System.out.println("FACTURA_PDF_OK="+destino+" BYTES="+contenido.length+" TOTAL="+c.calcularTotal(documento));
    }
}
