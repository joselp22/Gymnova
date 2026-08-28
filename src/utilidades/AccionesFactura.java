package utilidades;

import controlador.FacturaDocumentoControlador;
import java.awt.Desktop;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.DetalleFactura;
import modelo.FacturaDocumento;

/** Acciones visuales para vista previa, PDF e impresion de facturas. */
public final class AccionesFactura {

    private AccionesFactura() { }

    public static Long resolverIdFactura(Object registro) {
        if (registro == null) return null;
        try {
            Object valor = registro.getClass().getMethod("getIdFactura").invoke(registro);
            return valor instanceof Number numero ? numero.longValue() : null;
        } catch (ReflectiveOperationException ex) {
            try {
                Object idPago = registro.getClass().getMethod("getIdPago").invoke(registro);
                if (!(idPago instanceof Number numero)) return null;
                modelo.Pago pago = new controlador.PagoControlador().buscar(numero.longValue());
                return pago == null ? null : pago.getIdFactura();
            } catch (ReflectiveOperationException ignored) {
                return null;
            }
        }
    }

    public static void vistaPrevia(JPanel padre, Long idFactura) {
        FacturaDocumentoControlador controlador = new FacturaDocumentoControlador();
        FacturaDocumento doc = controlador.cargar(idFactura);
        if (doc == null) {
            advertir(padre, controlador.getMensaje());
            return;
        }
        JTable tabla = tablaDetalles(doc);
        JPanel panel = new JPanel(new java.awt.BorderLayout(0, 10));
        panel.setPreferredSize(new java.awt.Dimension(720, 430));
        javax.swing.JLabel cabecera = new javax.swing.JLabel(
                "<html><b>GYMNOVA - " + doc.getFactura().getNumeroFactura()
                + "</b><br>Cliente: " + doc.getCliente().getNombreCompleto()
                + " | Cedula: " + doc.getCliente().getCedula()
                + "<br>Fecha: " + doc.getFactura().getFechaEmision()
                + " | Estado: " + doc.getFactura().getEstadoFactura()
                + " | Total: $ " + controlador.calcularTotal(doc) + "</html>");
        cabecera.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(cabecera, java.awt.BorderLayout.NORTH);
        panel.add(new javax.swing.JScrollPane(tabla), java.awt.BorderLayout.CENTER);
        JOptionPane.showMessageDialog(padre, panel, "Vista previa de factura",
                JOptionPane.PLAIN_MESSAGE);
    }

    public static Path exportar(JPanel padre, Long idFactura) {
        FacturaDocumentoControlador controlador = new FacturaDocumentoControlador();
        FacturaDocumento doc = controlador.cargar(idFactura);
        if (doc == null) {
            advertir(padre, controlador.getMensaje());
            return null;
        }
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(new File(doc.getFactura().getNumeroFactura() + ".pdf"));
        if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) return null;
        Path destino = selector.getSelectedFile().toPath();
        if (!destino.getFileName().toString().toLowerCase().endsWith(".pdf"))
            destino = destino.resolveSibling(destino.getFileName() + ".pdf");
        if (Files.exists(destino) && JOptionPane.showConfirmDialog(padre,
                "El archivo ya existe. Desea reemplazarlo?", "Confirmar",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return null;
        Path generado = controlador.exportar(idFactura, destino);
        if (generado == null) {
            advertir(padre, controlador.getMensaje());
            return null;
        }
        JOptionPane.showMessageDialog(padre, "Factura guardada en:\n" + generado,
                "GYMNOVA", JOptionPane.INFORMATION_MESSAGE);
        return generado;
    }

    public static void abrir(JPanel padre, Long idFactura) {
        try {
            Path temporal = Files.createTempFile("gymnova_factura_", ".pdf");
            temporal.toFile().deleteOnExit();
            Path generado = new FacturaDocumentoControlador().exportar(idFactura, temporal);
            if (generado == null) {
                advertir(padre, "No fue posible preparar el comprobante.");
            } else if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(generado.toFile());
            } else {
                advertir(padre, "El sistema no dispone de un visor PDF predeterminado.");
            }
        } catch (IOException ex) {
            advertir(padre, "No fue posible abrir el PDF: " + ex.getMessage());
        }
    }

    public static void imprimir(JPanel padre, Long idFactura) {
        FacturaDocumento doc = new FacturaDocumentoControlador().cargar(idFactura);
        if (doc == null) {
            advertir(padre, "Seleccione una factura valida.");
            return;
        }
        JTable tabla = tablaDetalles(doc);
        try {
            tabla.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat("GYMNOVA - "
                            + doc.getFactura().getNumeroFactura()),
                    new java.text.MessageFormat("Pagina {0}"));
        } catch (PrinterException ex) {
            advertir(padre, "No fue posible imprimir: " + ex.getMessage());
        }
    }

    private static JTable tablaDetalles(FacturaDocumento doc) {
        DefaultTableModel modelo = new DefaultTableModel(
                new String[]{"Concepto", "Referencia", "Cantidad", "Precio", "Impuesto"}, 0) {
            @Override public boolean isCellEditable(int f, int c) { return false; }
        };
        for (DetalleFactura d : doc.getDetalles()) {
            modelo.addRow(new Object[]{d.getTipoConcepto(), d.getCodigoReferencia(),
                d.getCantidad(), d.getPrecioUnitario(), d.getPorcentajeImpuesto()});
        }
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        return tabla;
    }

    private static void advertir(JPanel padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Atencion",
                JOptionPane.WARNING_MESSAGE);
    }
}
