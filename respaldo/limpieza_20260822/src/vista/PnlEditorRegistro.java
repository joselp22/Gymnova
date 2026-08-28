package vista;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import modelo.OpcionRelacion;

/**
 * Formulario reutilizable, creado en NetBeans Design, para editar los campos
 * de los catalogos y movimientos administrativos.
 */
public class PnlEditorRegistro extends javax.swing.JPanel {

    private final Map<Integer, JComboBox<OpcionRelacion>> relaciones
            = new HashMap<>();

    public PnlEditorRegistro() {
        initComponents();
        utilidades.AyudasContextuales.aplicar(this);
    }

    public List<JLabel> getEtiquetas() {
        return Arrays.asList(lblCampo1, lblCampo2, lblCampo3, lblCampo4,
                lblCampo5, lblCampo6, lblCampo7, lblCampo8, lblCampo9,
                lblCampo10, lblCampo11, lblCampo12, lblCampo13, lblCampo14,
                lblCampo15);
    }

    public List<JTextField> getCampos() {
        return Arrays.asList(txtCampo1, txtCampo2, txtCampo3, txtCampo4,
                txtCampo5, txtCampo6, txtCampo7, txtCampo8, txtCampo9,
                txtCampo10, txtCampo11, txtCampo12, txtCampo13, txtCampo14,
                txtCampo15);
    }

    public void configurarTitulo(String titulo) {
        lblTitulo.setText(titulo);
    }

    public void configurarRelacion(
            int indice,
            List<OpcionRelacion> opciones,
            Object valorSeleccionado
    ) {
        List<JTextField> textos = getCampos();
        JTextField texto = textos.get(indice);
        int posicion = indice * 2 + 1;
        JComboBox<OpcionRelacion> combo = new JComboBox<>();
        combo.setToolTipText(
                "Seleccione el registro relacionado por su nombre; el ID se asigna internamente."
        );
        combo.addItem(null);
        for (OpcionRelacion opcion : opciones) {
            combo.addItem(opcion);
            if (valorSeleccionado != null
                    && String.valueOf(valorSeleccionado).equals(
                            String.valueOf(opcion.getId()))) {
                combo.setSelectedItem(opcion);
            }
        }
        pnlCampos.remove(texto);
        pnlCampos.add(combo, posicion);
        relaciones.put(indice, combo);
        pnlCampos.revalidate();
        pnlCampos.repaint();
    }

    public Object getValorCampo(int indice) {
        JComboBox<OpcionRelacion> combo = relaciones.get(indice);
        if (combo != null) {
            OpcionRelacion opcion = (OpcionRelacion) combo.getSelectedItem();
            return opcion == null ? null : opcion.getId();
        }
        return getCampos().get(indice).getText();
    }

    public boolean usaRelacion(int indice) {
        return relaciones.containsKey(indice);
    }

    public void configurarAyuda(String texto) {
        lblAyuda.setText(texto);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitulo = new javax.swing.JLabel();
        pnlCampos = new javax.swing.JPanel();
        lblCampo1 = new javax.swing.JLabel();
        txtCampo1 = new javax.swing.JTextField();
        lblCampo2 = new javax.swing.JLabel();
        txtCampo2 = new javax.swing.JTextField();
        lblCampo3 = new javax.swing.JLabel();
        txtCampo3 = new javax.swing.JTextField();
        lblCampo4 = new javax.swing.JLabel();
        txtCampo4 = new javax.swing.JTextField();
        lblCampo5 = new javax.swing.JLabel();
        txtCampo5 = new javax.swing.JTextField();
        lblCampo6 = new javax.swing.JLabel();
        txtCampo6 = new javax.swing.JTextField();
        lblCampo7 = new javax.swing.JLabel();
        txtCampo7 = new javax.swing.JTextField();
        lblCampo8 = new javax.swing.JLabel();
        txtCampo8 = new javax.swing.JTextField();
        lblCampo9 = new javax.swing.JLabel();
        txtCampo9 = new javax.swing.JTextField();
        lblCampo10 = new javax.swing.JLabel();
        txtCampo10 = new javax.swing.JTextField();
        lblCampo11 = new javax.swing.JLabel();
        txtCampo11 = new javax.swing.JTextField();
        lblCampo12 = new javax.swing.JLabel();
        txtCampo12 = new javax.swing.JTextField();
        lblCampo13 = new javax.swing.JLabel();
        txtCampo13 = new javax.swing.JTextField();
        lblCampo14 = new javax.swing.JLabel();
        txtCampo14 = new javax.swing.JTextField();
        lblCampo15 = new javax.swing.JLabel();
        txtCampo15 = new javax.swing.JTextField();
        lblAyuda = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16));
        setLayout(new java.awt.BorderLayout(0, 12));

        lblTitulo.setFont(new java.awt.Font("SansSerif", 1, 19)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(23, 42, 67));
        lblTitulo.setText("Datos del registro");
        add(lblTitulo, java.awt.BorderLayout.NORTH);

        pnlCampos.setBackground(new java.awt.Color(255, 255, 255));
        pnlCampos.setLayout(new java.awt.GridLayout(15, 2, 12, 8));

        lblCampo1.setText("Campo 1");
        pnlCampos.add(lblCampo1);
        pnlCampos.add(txtCampo1);
        lblCampo2.setText("Campo 2");
        pnlCampos.add(lblCampo2);
        pnlCampos.add(txtCampo2);
        lblCampo3.setText("Campo 3");
        pnlCampos.add(lblCampo3);
        pnlCampos.add(txtCampo3);
        lblCampo4.setText("Campo 4");
        pnlCampos.add(lblCampo4);
        pnlCampos.add(txtCampo4);
        lblCampo5.setText("Campo 5");
        pnlCampos.add(lblCampo5);
        pnlCampos.add(txtCampo5);
        lblCampo6.setText("Campo 6");
        pnlCampos.add(lblCampo6);
        pnlCampos.add(txtCampo6);
        lblCampo7.setText("Campo 7");
        pnlCampos.add(lblCampo7);
        pnlCampos.add(txtCampo7);
        lblCampo8.setText("Campo 8");
        pnlCampos.add(lblCampo8);
        pnlCampos.add(txtCampo8);
        lblCampo9.setText("Campo 9");
        pnlCampos.add(lblCampo9);
        pnlCampos.add(txtCampo9);
        lblCampo10.setText("Campo 10");
        pnlCampos.add(lblCampo10);
        pnlCampos.add(txtCampo10);
        lblCampo11.setText("Campo 11");
        pnlCampos.add(lblCampo11);
        pnlCampos.add(txtCampo11);
        lblCampo12.setText("Campo 12");
        pnlCampos.add(lblCampo12);
        pnlCampos.add(txtCampo12);
        lblCampo13.setText("Campo 13");
        pnlCampos.add(lblCampo13);
        pnlCampos.add(txtCampo13);
        lblCampo14.setText("Campo 14");
        pnlCampos.add(lblCampo14);
        pnlCampos.add(txtCampo14);
        lblCampo15.setText("Campo 15");
        pnlCampos.add(lblCampo15);
        pnlCampos.add(txtCampo15);

        add(pnlCampos, java.awt.BorderLayout.CENTER);

        lblAyuda.setForeground(new java.awt.Color(107, 127, 153));
        lblAyuda.setText("Fechas: AAAA-MM-DD | Relaciones: seleccione por nombre");
        add(lblAyuda, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblAyuda;
    private javax.swing.JLabel lblCampo1;
    private javax.swing.JLabel lblCampo10;
    private javax.swing.JLabel lblCampo11;
    private javax.swing.JLabel lblCampo12;
    private javax.swing.JLabel lblCampo13;
    private javax.swing.JLabel lblCampo14;
    private javax.swing.JLabel lblCampo15;
    private javax.swing.JLabel lblCampo2;
    private javax.swing.JLabel lblCampo3;
    private javax.swing.JLabel lblCampo4;
    private javax.swing.JLabel lblCampo5;
    private javax.swing.JLabel lblCampo6;
    private javax.swing.JLabel lblCampo7;
    private javax.swing.JLabel lblCampo8;
    private javax.swing.JLabel lblCampo9;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JPanel pnlCampos;
    private javax.swing.JTextField txtCampo1;
    private javax.swing.JTextField txtCampo10;
    private javax.swing.JTextField txtCampo11;
    private javax.swing.JTextField txtCampo12;
    private javax.swing.JTextField txtCampo13;
    private javax.swing.JTextField txtCampo14;
    private javax.swing.JTextField txtCampo15;
    private javax.swing.JTextField txtCampo2;
    private javax.swing.JTextField txtCampo3;
    private javax.swing.JTextField txtCampo4;
    private javax.swing.JTextField txtCampo5;
    private javax.swing.JTextField txtCampo6;
    private javax.swing.JTextField txtCampo7;
    private javax.swing.JTextField txtCampo8;
    private javax.swing.JTextField txtCampo9;
    // End of variables declaration//GEN-END:variables
}
