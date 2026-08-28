package utilidades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import modelo.DetalleFactura;
import modelo.FacturaDocumento;
import modelo.Pago;

/** Generador PDF liviano, sin dependencias externas, para facturas GYMNOVA. */
public final class DocumentoFacturaPDF {

    private DocumentoFacturaPDF() { }

    public static void generar(FacturaDocumento documento, Path destino)
            throws IOException {
        if (documento == null || documento.getFactura() == null
                || documento.getCliente() == null) {
            throw new IllegalArgumentException("La factura esta incompleta.");
        }
        if (destino == null) throw new IllegalArgumentException("Destino invalido.");
        Path padre = destino.toAbsolutePath().getParent();
        if (padre != null) Files.createDirectories(padre);

        List<String> paginas = construirPaginas(documento);
        Files.write(destino, construirPdf(paginas));
    }

    private static List<String> construirPaginas(FacturaDocumento doc) {
        List<DetalleFactura> detalles = doc.getDetalles();
        int porPagina = 16;
        int totalPaginas = Math.max(1, (detalles.size() + porPagina - 1) / porPagina);
        List<String> paginas = new ArrayList<>();
        for (int pagina = 0; pagina < totalPaginas; pagina++) {
            int desde = pagina * porPagina;
            int hasta = Math.min(detalles.size(), desde + porPagina);
            paginas.add(construirPagina(doc, detalles.subList(desde, hasta),
                    pagina + 1, totalPaginas, pagina == totalPaginas - 1));
        }
        return paginas;
    }

    private static String construirPagina(FacturaDocumento doc,
            List<DetalleFactura> detalles, int pagina, int paginas,
            boolean ultima) {
        StringBuilder c = new StringBuilder();
        color(c, 0.02, 0.17, 0.35);
        rect(c, 35, 755, 525, 55, true);
        color(c, 1, 1, 1);
        texto(c, "F2", 22, 52, 785, "GYMNOVA");
        texto(c, "F1", 9, 52, 768, "Sistema integrado de gimnasio");
        texto(c, "F2", 15, 390, 785, "FACTURA");
        texto(c, "F1", 9, 390, 770, doc.getFactura().getNumeroFactura());

        color(c, 0.05, 0.12, 0.23);
        texto(c, "F2", 10, 42, 730, "DATOS DEL CLIENTE");
        texto(c, "F1", 9, 42, 712, "Cliente: " + doc.getCliente().getNombreCompleto());
        texto(c, "F1", 9, 42, 697, "Cedula: " + doc.getCliente().getCedula());
        texto(c, "F1", 9, 42, 682, "Correo: " + nulo(doc.getCliente().getCorreo()));
        texto(c, "F1", 9, 330, 712, "Fecha: " + doc.getFactura().getFechaEmision());
        texto(c, "F1", 9, 330, 697, "Estado: " + doc.getFactura().getEstadoFactura());
        texto(c, "F1", 9, 330, 682, "Pagina: " + pagina + " de " + paginas);

        color(c, 0.03, 0.39, 0.85);
        rect(c, 35, 645, 525, 24, true);
        color(c, 1, 1, 1);
        texto(c, "F2", 8, 43, 653, "CONCEPTO");
        texto(c, "F2", 8, 235, 653, "REFERENCIA");
        texto(c, "F2", 8, 345, 653, "CANT.");
        texto(c, "F2", 8, 405, 653, "PRECIO");
        texto(c, "F2", 8, 490, 653, "IMP.");

        int y = 625;
        for (DetalleFactura d : detalles) {
            color(c, 0.08, 0.16, 0.27);
            texto(c, "F1", 8, 43, y, limitar(d.getTipoConcepto(), 29));
            texto(c, "F1", 8, 235, y, limitar(nulo(d.getCodigoReferencia()), 17));
            texto(c, "F1", 8, 355, y, String.valueOf(d.getCantidad()));
            texto(c, "F1", 8, 405, y, dinero(d.getPrecioUnitario()));
            texto(c, "F1", 8, 500, y, dinero(d.getPorcentajeImpuesto()) + "%");
            color(c, 0.82, 0.87, 0.93);
            linea(c, 35, y - 7, 560, y - 7);
            y -= 27;
        }
        if (detalles.isEmpty()) {
            texto(c, "F1", 9, 43, y, "Sin detalles registrados.");
        }

        if (ultima) {
            BigDecimal subtotal = doc.getDetalles().stream()
                    .map(d -> d.getPrecioUnitario().multiply(
                            BigDecimal.valueOf(d.getCantidad())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal descuento = valor(doc.getFactura().getTotalDescuento());
            BigDecimal impuesto = valor(doc.getFactura().getImpuesto());
            BigDecimal total = subtotal.subtract(descuento).add(impuesto);
            int ty = Math.min(y - 10, 180);
            color(c, 0.05, 0.12, 0.23);
            texto(c, "F1", 10, 385, ty + 54, "Subtotal: " + dinero(subtotal));
            texto(c, "F1", 10, 385, ty + 36, "Descuento: " + dinero(descuento));
            texto(c, "F1", 10, 385, ty + 18, "Impuesto: " + dinero(impuesto));
            texto(c, "F2", 13, 385, ty - 3, "TOTAL: " + dinero(total));

            BigDecimal pagado = doc.getPagos().stream()
                    .filter(p -> "CONFIRMADO".equalsIgnoreCase(p.getEstadoPago()))
                    .map(Pago::getMontoPago).reduce(BigDecimal.ZERO, BigDecimal::add);
            texto(c, "F1", 9, 42, ty + 36, "Pagado: " + dinero(pagado));
            texto(c, "F1", 9, 42, ty + 18, "Saldo: " + dinero(total.subtract(pagado).max(BigDecimal.ZERO)));
        }

        color(c, 0.38, 0.46, 0.57);
        texto(c, "F1", 8, 42, 42,
                "Documento academico/no tributario - No sustituye comprobante electronico autorizado por el SRI.");
        texto(c, "F1", 8, 42, 28, "Generado por GYMNOVA desde datos almacenados en PostgreSQL.");
        return c.toString();
    }

    private static byte[] construirPdf(List<String> contenidos) throws IOException {
        List<byte[]> objetos = new ArrayList<>();
        int paginas = contenidos.size();
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < paginas; i++) kids.append(5 + i * 2).append(" 0 R ");
        objetos.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        objetos.add(bytes("<< /Type /Pages /Count " + paginas + " /Kids [" + kids + "] >>"));
        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"));
        objetos.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>"));
        for (int i = 0; i < paginas; i++) {
            int contenidoId = 6 + i * 2;
            objetos.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> "
                    + "/Contents " + contenidoId + " 0 R >>"));
            byte[] stream = bytes(contenidos.get(i));
            objetos.add(bytes("<< /Length " + stream.length + " >>\nstream\n"
                    + contenidos.get(i) + "\nendstream"));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(bytes("%PDF-1.4\n%GYMNOVA\n"));
        long[] offsets = new long[objetos.size() + 1];
        for (int i = 0; i < objetos.size(); i++) {
            offsets[i + 1] = out.size();
            out.write(bytes((i + 1) + " 0 obj\n"));
            out.write(objetos.get(i));
            out.write(bytes("\nendobj\n"));
        }
        long xref = out.size();
        out.write(bytes("xref\n0 " + (objetos.size() + 1) + "\n"));
        out.write(bytes("0000000000 65535 f \n"));
        for (int i = 1; i < offsets.length; i++)
            out.write(bytes(String.format("%010d 00000 n \n", offsets[i])));
        out.write(bytes("trailer\n<< /Size " + (objetos.size() + 1)
                + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n"));
        return out.toByteArray();
    }

    private static void texto(StringBuilder c, String fuente, int tamano,
            int x, int y, String texto) {
        c.append("BT /").append(fuente).append(' ').append(tamano)
                .append(" Tf ").append(x).append(' ').append(y)
                .append(" Td (").append(escape(ascii(texto))).append(") Tj ET\n");
    }
    private static void color(StringBuilder c, double r, double g, double b) {
        c.append(r).append(' ').append(g).append(' ').append(b).append(" rg\n");
    }
    private static void rect(StringBuilder c, int x, int y, int w, int h, boolean fill) {
        c.append(x).append(' ').append(y).append(' ').append(w).append(' ')
                .append(h).append(" re ").append(fill ? "f" : "S").append('\n');
    }
    private static void linea(StringBuilder c, int x1, int y1, int x2, int y2) {
        c.append(x1).append(' ').append(y1).append(" m ")
                .append(x2).append(' ').append(y2).append(" l S\n");
    }
    private static String dinero(BigDecimal valor) {
        return "$ " + valor(valor).setScale(2, RoundingMode.HALF_UP);
    }
    private static BigDecimal valor(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
    private static String nulo(String valor) { return valor == null ? "-" : valor; }
    private static String limitar(String texto, int max) {
        String valor = nulo(texto);
        return valor.length() <= max ? valor : valor.substring(0, max - 3) + "...";
    }
    private static String ascii(String texto) {
        return Normalizer.normalize(nulo(texto), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^\\x20-\\x7E]", "?");
    }
    private static String escape(String texto) {
        return texto.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
    private static byte[] bytes(String texto) {
        return texto.getBytes(StandardCharsets.ISO_8859_1);
    }
}
