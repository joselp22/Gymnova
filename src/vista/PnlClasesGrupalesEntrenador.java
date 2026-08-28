package vista;

import controlador.ClaseGrupalGestionControlador;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.ClaseGrupalResumen;
import modelo.ParticipanteClaseGrupal;

/** Vista del entrenador: clases asignadas y participantes inscritos. */
public class PnlClasesGrupalesEntrenador extends JPanel {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final ClaseGrupalGestionControlador controlador = new ClaseGrupalGestionControlador();
    private final JTable tblClases = new JTable();
    private final JTable tblParticipantes = new JTable();
    private final JLabel lblClase = new JLabel("Selecciona una clase para ver sus participantes.");
    private final JLabel lblCantidad = new JLabel("0 clases asignadas");
    private List<ClaseGrupalResumen> clases = new ArrayList<>();

    public PnlClasesGrupalesEntrenador() {
        construirVista();
        configurarEventos();
        refrescarDatos();
    }

    public final void refrescarDatos() {
        cargarClases();
    }

    private void construirVista() {
        setLayout(new BorderLayout(12,12));
        setBackground(new java.awt.Color(234,242,251));
        setBorder(BorderFactory.createEmptyBorder(14,14,14,14));
        add(encabezado(), BorderLayout.NORTH);

        configurarTablaClases();
        configurarTablaParticipantes();

        JPanel superior = panelBlanco(new BorderLayout(8,8));
        JPanel tit = new JPanel(new BorderLayout()); tit.setOpaque(false);
        JLabel t = new JLabel("Mis clases grupales asignadas"); t.setFont(new Font("SansSerif",Font.BOLD,17));
        tit.add(t,BorderLayout.WEST); tit.add(lblCantidad,BorderLayout.EAST);
        superior.add(tit,BorderLayout.NORTH); superior.add(new JScrollPane(tblClases),BorderLayout.CENTER);

        JPanel inferior = panelBlanco(new BorderLayout(8,8));
        lblClase.setFont(new Font("SansSerif",Font.BOLD,15));
        inferior.add(lblClase,BorderLayout.NORTH); inferior.add(new JScrollPane(tblParticipantes),BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,superior,inferior);
        split.setResizeWeight(0.52); split.setDividerLocation(330); split.setBorder(null);
        add(split,BorderLayout.CENTER);
    }

    private JPanel encabezado(){
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(java.awt.Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new java.awt.Color(212,225,239)),BorderFactory.createEmptyBorder(14,18,14,18)));
        JPanel tx=new JPanel();tx.setOpaque(false);tx.setLayout(new javax.swing.BoxLayout(tx,javax.swing.BoxLayout.Y_AXIS));
        JLabel t=new JLabel("Mis clases grupales");t.setFont(new Font("SansSerif",Font.BOLD,24));t.setForeground(new java.awt.Color(23,42,67));
        JLabel s=new JLabel("Consulta las clases que te asignó el Administrador y las personas inscritas en cada una.");s.setForeground(new java.awt.Color(90,110,135));
        tx.add(t);tx.add(javax.swing.Box.createVerticalStrut(4));tx.add(s);
        JLabel mod=new JLabel("MIS CLASES",SwingConstants.CENTER);mod.setForeground(new java.awt.Color(8,124,255));mod.setBorder(BorderFactory.createLineBorder(new java.awt.Color(8,124,255)));mod.setPreferredSize(new Dimension(120,45));
        p.add(tx,BorderLayout.CENTER);p.add(mod,BorderLayout.EAST);return p;
    }

    private void configurarTablaClases(){
        tblClases.setModel(new DefaultTableModel(new Object[]{"ID","Clase","Fecha / hora","Duración","Cupo","Inscritos","Disponibles","Estado"},0){@Override public boolean isCellEditable(int r,int c){return false;}});
        estilizar(tblClases); tblClases.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    private void configurarTablaParticipantes(){
        tblParticipantes.setModel(new DefaultTableModel(new Object[]{"Reserva","Código cliente","Cliente","Fecha de inscripción","Estado"},0){@Override public boolean isCellEditable(int r,int c){return false;}});
        estilizar(tblParticipantes);
    }
    private void configurarEventos(){
        tblClases.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting())cargarParticipantesSeleccionados();});
    }
    private void cargarClases(){
        clases=controlador.listarMisClasesEntrenador();
        DefaultTableModel m=(DefaultTableModel)tblClases.getModel();m.setRowCount(0);
        for(ClaseGrupalResumen c:clases)m.addRow(new Object[]{c.getIdClase(),c.getNombreClase(),fecha(c.getFechaHora()),c.getDuracionMinutos()+" min",c.getCupoMaximo(),c.getReservados(),c.getDisponibles(),estado(c)});
        lblCantidad.setText(clases.size()+" clase(s) asignada(s)");
        ((DefaultTableModel)tblParticipantes.getModel()).setRowCount(0);lblClase.setText("Selecciona una clase para ver sus participantes.");
        if(clases.isEmpty()&&!controlador.getMensaje().isBlank())avisar(controlador.getMensaje());
    }
    private void cargarParticipantesSeleccionados(){
        int fila=tblClases.getSelectedRow();if(fila<0)return;
        Long id=((Number)tblClases.getValueAt(fila,0)).longValue();
        ClaseGrupalResumen clase=clases.stream().filter(c->c.getIdClase().equals(id)).findFirst().orElse(null);if(clase==null)return;
        List<ParticipanteClaseGrupal> participantes=controlador.listarParticipantesEntrenador(id);
        DefaultTableModel m=(DefaultTableModel)tblParticipantes.getModel();m.setRowCount(0);
        for(ParticipanteClaseGrupal p:participantes)m.addRow(new Object[]{p.getIdReserva(),p.getCodigoCliente(),p.getNombreCompleto(),fecha(p.getFechaReserva()),p.getEstadoReserva()});
        lblClase.setText(clase.getNombreClase()+"  •  "+fecha(clase.getFechaHora())+"  •  "+participantes.size()+" participante(s)");
        if(!controlador.getMensaje().isBlank())avisar(controlador.getMensaje());
    }
    private String estado(ClaseGrupalResumen c){if(!c.isEstadoClase())return "INACTIVA";if(c.getFechaHora()!=null&&c.getFechaHora().isBefore(LocalDateTime.now()))return "FINALIZADA";return c.getDisponibles()<=0?"LLENA":"PROGRAMADA";}
    private String fecha(LocalDateTime f){return f==null?"-":f.format(FORMATO);}
    private JPanel panelBlanco(java.awt.LayoutManager l){JPanel p=new JPanel(l);p.setBackground(java.awt.Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new java.awt.Color(212,225,239)),BorderFactory.createEmptyBorder(12,12,12,12)));return p;}
    private void estilizar(JTable t){t.setRowHeight(36);t.setShowGrid(false);t.setFillsViewportHeight(true);t.setSelectionBackground(new java.awt.Color(220,238,255));t.setSelectionForeground(new java.awt.Color(23,42,67));t.getTableHeader().setPreferredSize(new Dimension(0,40));DefaultTableCellRenderer h=new DefaultTableCellRenderer();h.setOpaque(true);h.setBackground(new java.awt.Color(10,58,108));h.setForeground(java.awt.Color.WHITE);h.setHorizontalAlignment(SwingConstants.CENTER);t.getTableHeader().setDefaultRenderer(h);}
    private void avisar(String m){JOptionPane.showMessageDialog(this,m,"GYMNOVA",JOptionPane.WARNING_MESSAGE);}
}
