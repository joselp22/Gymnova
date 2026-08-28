package vista;

import controlador.ClaseGrupalGestionControlador;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import modelo.ClaseGrupalResumen;

/** Portal del cliente para consultar, unirse o salir de clases grupales. */
public class PnlClasesGrupalesCliente extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final ClaseGrupalGestionControlador controlador = new ClaseGrupalGestionControlador();
    private final JTable tblClases = new JTable();
    private final JButton btnActualizar = new JButton("Actualizar clases");
    private final JLabel lblResumen = new JLabel("0 clases disponibles");
    private List<ClaseGrupalResumen> clases = new ArrayList<>();

    public PnlClasesGrupalesCliente(){
        construirVista();
        btnActualizar.addActionListener(e->refrescarDatos());
        refrescarDatos();
    }

    public final void refrescarDatos(){cargarClases();}

    private void construirVista(){
        setLayout(new BorderLayout(12,12));setBackground(new java.awt.Color(234,242,251));setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        add(encabezado(),BorderLayout.NORTH);
        JPanel centro=panelBlanco(new BorderLayout(8,8));
        JPanel barra=new JPanel(new BorderLayout());barra.setOpaque(false);
        JLabel ayuda=new JLabel("Elige una clase y toma un cupo. Puedes salir mientras la clase aún no haya iniciado.");ayuda.setForeground(new java.awt.Color(90,110,135));
        utilidades.EstilosComponentes.aplicarBotonPremium(btnActualizar,new java.awt.Color(241,245,249),new java.awt.Color(226,232,240),new java.awt.Color(52,74,100));
        JPanel derecha=new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT,8,0));derecha.setOpaque(false);derecha.add(lblResumen);derecha.add(btnActualizar);
        barra.add(ayuda,BorderLayout.CENTER);barra.add(derecha,BorderLayout.EAST);centro.add(barra,BorderLayout.NORTH);
        configurarTabla();centro.add(new JScrollPane(tblClases),BorderLayout.CENTER);add(centro,BorderLayout.CENTER);
    }
    private JPanel encabezado(){
        JPanel p=new JPanel(new BorderLayout());p.setBackground(java.awt.Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new java.awt.Color(212,225,239)),BorderFactory.createEmptyBorder(14,18,14,18)));
        JPanel tx=new JPanel();tx.setOpaque(false);tx.setLayout(new javax.swing.BoxLayout(tx,javax.swing.BoxLayout.Y_AXIS));
        JLabel t=new JLabel("Clases grupales");t.setFont(new Font("SansSerif",Font.BOLD,24));t.setForeground(new java.awt.Color(23,42,67));
        JLabel s=new JLabel("Consulta próximos eventos y decide en cuáles deseas participar.");s.setForeground(new java.awt.Color(90,110,135));tx.add(t);tx.add(javax.swing.Box.createVerticalStrut(4));tx.add(s);
        JLabel mod=new JLabel("EVENTOS",SwingConstants.CENTER);mod.setForeground(new java.awt.Color(8,124,255));mod.setBorder(BorderFactory.createLineBorder(new java.awt.Color(8,124,255)));mod.setPreferredSize(new Dimension(105,45));
        p.add(tx,BorderLayout.CENTER);p.add(mod,BorderLayout.EAST);return p;
    }
    private void configurarTabla(){
        DefaultTableModel m=new DefaultTableModel(new Object[]{"Clase","Entrenador","Fecha / hora","Duración","Cupo","Disponibles","Mi estado","Acción"},0){@Override public boolean isCellEditable(int r,int c){return c==7;}};
        tblClases.setModel(m);estilizar(tblClases);tblClases.getColumnModel().getColumn(7).setCellRenderer(new BotonRenderer());tblClases.getColumnModel().getColumn(7).setCellEditor(new BotonEditor());
    }
    private void cargarClases(){
        clases=controlador.listarClasesCliente();DefaultTableModel m=(DefaultTableModel)tblClases.getModel();m.setRowCount(0);
        for(ClaseGrupalResumen c:clases){
            boolean inscrito=c.isClienteInscrito();String estado=inscrito?"INSCRITO":c.getDisponibles()<=0?"LLENA":"DISPONIBLE";
            String accion=inscrito?"Salir de la clase":c.getDisponibles()<=0?"Sin cupo":"Unirme";
            m.addRow(new Object[]{c.getNombreClase(),c.getNombreEntrenador(),fecha(c.getFechaHora()),c.getDuracionMinutos()+" min",c.getReservados()+" / "+c.getCupoMaximo(),c.getDisponibles(),estado,accion});
        }
        lblResumen.setText(clases.size()+" clase(s) próxima(s)");
        if(clases.isEmpty()&&!controlador.getMensaje().isBlank())avisar(controlador.getMensaje());
    }
    private void ejecutarAccion(int filaVista){
        if(filaVista<0||filaVista>=clases.size())return;int fila=tblClases.convertRowIndexToModel(filaVista);if(fila<0||fila>=clases.size())return;
        ClaseGrupalResumen c=clases.get(fila);
        if(c.isClienteInscrito()){
            if(JOptionPane.showConfirmDialog(this,"¿Salir de "+c.getNombreClase()+"?","GYMNOVA",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            if(controlador.salirDeClase(c.getIdReservaCliente())){mensaje(controlador.getMensaje());cargarClases();}else avisar(controlador.getMensaje());
        }else{
            if(c.getDisponibles()<=0){avisar("La clase ya no tiene cupos disponibles.");return;}
            if(JOptionPane.showConfirmDialog(this,"¿Tomar un cupo en "+c.getNombreClase()+"?","GYMNOVA",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            if(controlador.unirseAClase(c.getIdClase())){mensaje(controlador.getMensaje());cargarClases();}else avisar(controlador.getMensaje());
        }
    }
    private String fecha(LocalDateTime f){return f==null?"-":f.format(FORMATO);}
    private JPanel panelBlanco(java.awt.LayoutManager l){JPanel p=new JPanel(l);p.setBackground(java.awt.Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new java.awt.Color(212,225,239)),BorderFactory.createEmptyBorder(12,12,12,12)));return p;}
    private void estilizar(JTable t){t.setRowHeight(38);t.setShowGrid(false);t.setFillsViewportHeight(true);t.setSelectionBackground(new java.awt.Color(220,238,255));t.setSelectionForeground(new java.awt.Color(23,42,67));t.getTableHeader().setPreferredSize(new Dimension(0,40));DefaultTableCellRenderer h=new DefaultTableCellRenderer();h.setOpaque(true);h.setBackground(new java.awt.Color(10,58,108));h.setForeground(java.awt.Color.WHITE);h.setHorizontalAlignment(SwingConstants.CENTER);t.getTableHeader().setDefaultRenderer(h);}
    private void mensaje(String m){JOptionPane.showMessageDialog(this,m,"GYMNOVA",JOptionPane.INFORMATION_MESSAGE);}
    private void avisar(String m){JOptionPane.showMessageDialog(this,m==null||m.isBlank()?"No se pudo completar la operación.":m,"GYMNOVA",JOptionPane.WARNING_MESSAGE);}

    private class BotonRenderer extends JButton implements TableCellRenderer{
        BotonRenderer(){setOpaque(true);}
        @Override public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column){setText(String.valueOf(value));boolean activo=!"Sin cupo".equals(String.valueOf(value));setEnabled(activo);return this;}
    }
    private class BotonEditor extends AbstractCellEditor implements TableCellEditor{
        private final JButton boton=new JButton();private int fila=-1;
        BotonEditor(){boton.addActionListener(this::accion);}
        private void accion(ActionEvent e){int actual=fila;fireEditingStopped();javax.swing.SwingUtilities.invokeLater(()->ejecutarAccion(actual));}
        @Override public Object getCellEditorValue(){return boton.getText();}
        @Override public Component getTableCellEditorComponent(JTable table,Object value,boolean isSelected,int row,int column){fila=row;boton.setText(String.valueOf(value));boton.setEnabled(!"Sin cupo".equals(String.valueOf(value)));return boton;}
    }
}
