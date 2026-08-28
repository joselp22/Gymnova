package utilidades;

import java.awt.Component;
import java.awt.Container;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.text.JTextComponent;

/** Agrega explicaciones breves sin mezclar reglas de negocio con las vistas. */
public final class AyudasContextuales {

    public static void aplicar(Container raiz) {
        for (Component componente : raiz.getComponents()) {
            if (componente instanceof JComponent visual
                    && (visual.getToolTipText() == null
                    || visual.getToolTipText().isBlank())) {
                if (componente instanceof JTextComponent texto) {
                    visual.setToolTipText(texto.isEditable()
                            ? "Ingrese el dato solicitado en la etiqueta."
                            : "Dato automatico o protegido; no puede escribirse.");
                } else if (componente instanceof JComboBox<?>) {
                    visual.setToolTipText(
                            "Seleccione una opcion por su nombre; no necesita escribir IDs.");
                } else if (componente instanceof AbstractButton boton) {
                    visual.setToolTipText("Accion: " + boton.getText());
                } else if (componente instanceof JTable) {
                    visual.setToolTipText(
                            "Seleccione una fila solo cuando vaya a consultar o modificarla.");
                }
            }
            if (componente instanceof JTable tabla
                    && tabla.getClientProperty("gymnova.tooltip.celdas") == null) {
                tabla.putClientProperty("gymnova.tooltip.celdas", Boolean.TRUE);
                tabla.addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent evento) {
                        int fila = tabla.rowAtPoint(evento.getPoint());
                        int columna = tabla.columnAtPoint(evento.getPoint());
                        if (fila >= 0 && columna >= 0) {
                            Object valor = tabla.getValueAt(fila, columna);
                            tabla.setToolTipText(valor == null
                                    ? "Sin información"
                                    : valor.toString());
                        } else {
                            tabla.setToolTipText(
                                    "Seleccione una fila para consultar sus datos.");
                        }
                    }
                });
            }
            if (componente instanceof Container contenedor) {
                aplicar(contenedor);
            }
        }
    }

    private AyudasContextuales() {
    }
}
