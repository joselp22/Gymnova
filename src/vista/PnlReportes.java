package vista;

import controlador.ReportesControlador;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.ReporteResultado;
import utilidades.GeneradorReportePDF;

/** Dashboard administrativo de reportes de GYMNOVA. */
public class PnlReportes extends JPanel {
    private static final Color AZUL = new Color(10,58,108);
    private static final Color FONDO = new Color(244,248,252);
    private final ReportesControlador controlador = new ReportesControlador();
    private final JComboBox<String> cboTipo = new JComboBox<>(ReportesControlador.TIPOS);
    private final JTextField txtDesde = new JTextField(10);
    private final JTextField txtHasta = new JTextField(10);
    private final JTextField txtFiltro = new JTextField(22);
    private final JComboBox<Integer> cboDias = new JComboBox<>(new Integer[]{7,15,30});
    private final JButton btnGenerar = new JButton("Generar reporte");
    private final JButton btnPdf = new JButton("Guardar como PDF");
    private final JButton btnLimpiar = new JButton("Limpiar");
    private final JButton btnFactura = new JButton("Factura de membresía");
    private final JTable tabla = new JTable();
    private final JLabel lblEstado = new JLabel("Seleccione un reporte y presione Generar reporte.");
    private final JPanel pnlResumen = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
    private final Map<String,JLabel> kpis = new LinkedHashMap<>();
    private ReporteResultado reporteActual;

    public PnlReportes(){
        construirVista(); configurarEventos(); refrescarDatos();
    }

    private void construirVista(){
        setLayout(new BorderLayout(0,12)); setBackground(FONDO); setBorder(BorderFactory.createEmptyBorder(18,18,18,18));
        JPanel superior=new JPanel(); superior.setOpaque(false); superior.setLayout(new BoxLayout(superior,BoxLayout.Y_AXIS));
        JLabel titulo=new JLabel("Reportes administrativos"); titulo.setFont(new Font("SansSerif",Font.BOLD,24)); titulo.setForeground(new Color(23,42,67));
        JLabel sub=new JLabel("Consulta indicadores, analiza resultados y guarda el reporte como PDF."); sub.setForeground(new Color(90,110,135));
        superior.add(titulo); superior.add(sub); superior.add(crearKpis()); superior.add(crearFiltros());
        add(superior,BorderLayout.NORTH);

        JPanel centro=new JPanel(new BorderLayout(0,8)); centro.setBackground(Color.WHITE); centro.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(212,225,239)),BorderFactory.createEmptyBorder(12,12,12,12)));
        pnlResumen.setOpaque(false); centro.add(pnlResumen,BorderLayout.NORTH);
        configurarTabla(); centro.add(new JScrollPane(tabla),BorderLayout.CENTER);
        lblEstado.setForeground(new Color(90,110,135)); centro.add(lblEstado,BorderLayout.SOUTH); add(centro,BorderLayout.CENTER);
    }

    private JPanel crearKpis(){
        JPanel p=new JPanel(new java.awt.GridLayout(1,5,10,0)); p.setOpaque(false); p.setBorder(BorderFactory.createEmptyBorder(14,0,14,0));
        for(String nombre:new String[]{"Clientes activos","Membresías activas","Ingresos del mes","Asistencias del mes","Rutinas activas"}){
            JPanel card=new JPanel(new BorderLayout()); card.setBackground(Color.WHITE); card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(214,226,239)),BorderFactory.createEmptyBorder(10,12,10,12)));
            JLabel n=new JLabel(nombre); n.setForeground(new Color(90,110,135)); n.setFont(new Font("SansSerif",Font.PLAIN,11));
            JLabel v=new JLabel("—"); v.setFont(new Font("SansSerif",Font.BOLD,20)); v.setForeground(AZUL); card.add(n,BorderLayout.NORTH); card.add(v,BorderLayout.CENTER); kpis.put(nombre,v); p.add(card);
        }
        return p;
    }

    private JPanel crearFiltros(){
        JPanel box=new JPanel(new BorderLayout(0,10)); box.setBackground(Color.WHITE); box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(212,225,239)),BorderFactory.createEmptyBorder(12,14,12,14)));
        JPanel fila1=new JPanel(new FlowLayout(FlowLayout.LEFT,10,2)); fila1.setOpaque(false);
        fila1.add(new JLabel("Tipo de reporte:")); cboTipo.setPreferredSize(new Dimension(240,36)); fila1.add(cboTipo);
        fila1.add(new JLabel("Desde:")); txtDesde.setPreferredSize(new Dimension(105,36)); fila1.add(txtDesde);
        fila1.add(new JLabel("Hasta:")); txtHasta.setPreferredSize(new Dimension(105,36)); fila1.add(txtHasta);
        fila1.add(new JLabel("Vence en:")); cboDias.setPreferredSize(new Dimension(80,36)); fila1.add(cboDias); fila1.add(new JLabel("días"));
        JPanel fila2=new JPanel(new FlowLayout(FlowLayout.LEFT,10,2)); fila2.setOpaque(false);
        fila2.add(new JLabel("Buscar / filtro opcional:")); txtFiltro.setPreferredSize(new Dimension(360,36)); fila2.add(txtFiltro);
        estiloBoton(btnGenerar,new Color(8,124,255),Color.WHITE); estiloBoton(btnPdf,new Color(34,139,94),Color.WHITE); estiloBoton(btnLimpiar,new Color(235,240,246),new Color(45,65,90));
        estiloBoton(btnFactura,new Color(10,58,108),Color.WHITE);
        btnPdf.setEnabled(false); fila2.add(btnGenerar); fila2.add(btnPdf); fila2.add(btnFactura); fila2.add(btnLimpiar);
        box.add(fila1,BorderLayout.NORTH); box.add(fila2,BorderLayout.CENTER); return box;
    }

    private void configurarTabla(){
        tabla.setRowHeight(34); tabla.setFillsViewportHeight(true); tabla.setShowVerticalLines(false); tabla.setSelectionBackground(new Color(220,238,255)); tabla.setSelectionForeground(new Color(23,42,67)); tabla.getTableHeader().setPreferredSize(new Dimension(0,38)); tabla.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer h=new DefaultTableCellRenderer(); h.setOpaque(true); h.setBackground(AZUL); h.setForeground(Color.WHITE); h.setHorizontalAlignment(SwingConstants.CENTER); h.setFont(new Font("SansSerif",Font.BOLD,11)); tabla.getTableHeader().setDefaultRenderer(h);
        tabla.setModel(new DefaultTableModel(new Object[][]{},new String[]{"Sin datos"}){public boolean isCellEditable(int r,int c){return false;}});
    }

    private void configurarEventos(){
        txtDesde.setText(LocalDate.now().withDayOfMonth(1).toString()); txtHasta.setText(LocalDate.now().toString());
        utilidades.CalendarioSelector.vincularFecha(txtDesde); utilidades.CalendarioSelector.vincularFecha(txtHasta);
        cboTipo.addActionListener(e->actualizarFiltros()); btnGenerar.addActionListener(e->generar()); btnPdf.addActionListener(e->guardarPdf()); btnFactura.addActionListener(e->abrirFacturaMembresia()); btnLimpiar.addActionListener(e->limpiar()); txtFiltro.addActionListener(e->generar()); actualizarFiltros();
    }

    public void refrescarDatos(){
        new SwingWorker<Map<String,String>,Void>(){
            protected Map<String,String> doInBackground()throws Exception{return controlador.cargarKpis();}
            protected void done(){try{Map<String,String> m=get();m.forEach((k,v)->{if(kpis.containsKey(k))kpis.get(k).setText(v);});}catch(Exception ex){kpis.values().forEach(l->l.setText("N/D"));}}
        }.execute();
    }

    private void actualizarFiltros(){
        boolean venc="Membresías próximas a vencer".equals(cboTipo.getSelectedItem()); txtDesde.setEnabled(!venc); txtHasta.setEnabled(!venc); cboDias.setEnabled(venc);
        txtFiltro.setToolTipText(venc?"Cliente o número de membresía":"Cliente, estado, rutina, clase, método o referencia según el reporte");
    }

    private void generar(){
        final String tipo=(String)cboTipo.getSelectedItem(); final String filtro=txtFiltro.getText().trim(); final int dias=(Integer)cboDias.getSelectedItem();
        final LocalDate desde,hasta;
        try{desde=LocalDate.parse(txtDesde.getText().trim());hasta=LocalDate.parse(txtHasta.getText().trim());}catch(DateTimeParseException ex){JOptionPane.showMessageDialog(this,"Use fechas válidas con formato AAAA-MM-DD.","Reportes",JOptionPane.WARNING_MESSAGE);return;}
        btnGenerar.setEnabled(false);btnPdf.setEnabled(false);lblEstado.setText("Generando reporte...");
        new SwingWorker<ReporteResultado,Void>(){
            protected ReporteResultado doInBackground()throws Exception{return controlador.generar(tipo,desde,hasta,filtro,dias);}
            protected void done(){try{reporteActual=get();mostrar(reporteActual);btnPdf.setEnabled(!reporteActual.getFilas().isEmpty());lblEstado.setText(reporteActual.getFilas().size()+" registros encontrados.");}catch(Exception ex){reporteActual=null;mostrarError(causa(ex));}finally{btnGenerar.setEnabled(true);}}
        }.execute();
    }

    private void mostrar(ReporteResultado r){
        DefaultTableModel m=new DefaultTableModel(r.getColumnas().toArray(),0){public boolean isCellEditable(int f,int c){return false;}}; for(Object[] fila:r.getFilas())m.addRow(fila); tabla.setModel(m);
        pnlResumen.removeAll(); for(Map.Entry<String,String> e:r.getResumen().entrySet()){JLabel l=new JLabel("<html><b>"+html(e.getKey())+":</b> "+html(e.getValue())+"</html>");l.setBorder(BorderFactory.createEmptyBorder(5,8,5,8));pnlResumen.add(l);} pnlResumen.revalidate();pnlResumen.repaint();
    }

    private void guardarPdf(){
        if(reporteActual==null||reporteActual.getFilas().isEmpty()){JOptionPane.showMessageDialog(this,"Primero debe generar un reporte con resultados.","Reportes",JOptionPane.WARNING_MESSAGE);return;}
        JFileChooser ch=new JFileChooser(); ch.setDialogTitle("Guardar reporte como PDF"); ch.setFileFilter(new FileNameExtensionFilter("Documento PDF (*.pdf)","pdf"));
        String base=reporteActual.getTitulo().toLowerCase().replaceAll("[^a-záéíóúñ0-9]+","_").replaceAll("(^_|_$)",""); ch.setSelectedFile(new File("reporte_"+base+"_"+LocalDate.now().toString().replace("-","")+".pdf"));
        if(ch.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return; File f=ch.getSelectedFile(); if(!f.getName().toLowerCase().endsWith(".pdf"))f=new File(f.getParentFile(),f.getName()+".pdf");
        if(f.exists()&&JOptionPane.showConfirmDialog(this,"El archivo ya existe. ¿Desea sobrescribirlo?","Confirmar",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{String periodo="Membresías próximas a vencer".equals(cboTipo.getSelectedItem())?"Próximos "+cboDias.getSelectedItem()+" días":txtDesde.getText()+" a "+txtHasta.getText();GeneradorReportePDF.generar(reporteActual,periodo,txtFiltro.getText().trim().isEmpty()?"Sin filtro adicional":txtFiltro.getText().trim(),f.toPath());JOptionPane.showMessageDialog(this,"PDF guardado correctamente en:\n"+f.getAbsolutePath(),"Reportes",JOptionPane.INFORMATION_MESSAGE);}catch(Exception ex){mostrarError(ex.getMessage());}
    }


    private void abrirFacturaMembresia(){
        java.awt.Window ventana=javax.swing.SwingUtilities.getWindowAncestor(this);
        java.awt.Frame padre=ventana instanceof java.awt.Frame?(java.awt.Frame)ventana:null;
        new DlgFacturaMembresiaCliente(padre).setVisible(true);
    }

    private void limpiar(){txtDesde.setText(LocalDate.now().withDayOfMonth(1).toString());txtHasta.setText(LocalDate.now().toString());txtFiltro.setText("");cboDias.setSelectedItem(30);reporteActual=null;btnPdf.setEnabled(false);pnlResumen.removeAll();configurarTabla();lblEstado.setText("Seleccione un reporte y presione Generar reporte.");}
    private void estiloBoton(JButton b,Color fondo,Color texto){b.setBackground(fondo);b.setForeground(texto);b.setFocusPainted(false);b.setFont(new Font("SansSerif",Font.BOLD,12));b.setPreferredSize(new Dimension(155,36));}
    private void mostrarError(String m){lblEstado.setText("No fue posible generar el reporte.");JOptionPane.showMessageDialog(this,m==null?"Ocurrió un error al consultar la información.":m,"Reportes",JOptionPane.ERROR_MESSAGE);}
    private String causa(Exception ex){Throwable t=ex;while(t.getCause()!=null)t=t.getCause();return t.getMessage()==null?"Ocurrió un error al consultar la información.":t.getMessage();}
    private String html(String s){return s==null?"":s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
}
