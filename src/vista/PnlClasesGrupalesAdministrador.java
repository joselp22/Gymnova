package vista;

import controlador.ClaseGrupalGestionControlador;
import dao.CatalogoRelacionDAO;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import modelo.ClaseGrupal;
import modelo.ClaseGrupalResumen;
import modelo.OpcionRelacion;
import utilidades.CalendarioSelector;

/** Vista exclusiva del Administrador para crear y gestionar clases grupales. */
public class PnlClasesGrupalesAdministrador extends JPanel {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ClaseGrupalGestionControlador controlador =
            new ClaseGrupalGestionControlador();
    private final CatalogoRelacionDAO catalogos = new CatalogoRelacionDAO();

    private final JTextField txtNombre = new JTextField(20);
    private final JComboBox<OpcionRelacion> cboEntrenador = new JComboBox<>();
    private final JTextField txtFechaHora = new JTextField(16);
    private final JTextField txtCupo = new JTextField("20", 8);
    private final JTextField txtDuracion = new JTextField("60", 8);
    private final JComboBox<String> cboNivel = new JComboBox<>(
            new String[]{"PRINCIPIANTE", "INTERMEDIO", "AVANZADO", "TODOS"});
    private final JComboBox<String> cboIntensidad = new JComboBox<>(
            new String[]{"BAJA", "MEDIA", "ALTA"});
    private final JTextArea txtDescripcion = new JTextArea(3, 28);
    private final JCheckBox chkActiva = new JCheckBox("Clase activa", true);

    private final JButton btnCrear = new JButton("Crear clase");
    private final JButton btnActualizar = new JButton("Actualizar clase");
    private final JButton btnDesactivar = new JButton("Desactivar clase");
    private final JButton btnLimpiar = new JButton("Nueva / Limpiar");
    private final JButton btnRefrescar = new JButton("Actualizar lista");

    // Modo "clase para una sola persona": al marcar el checkbox se
    // habilita el combo con los clientes activos y se fuerza cupo=1.
    // Al crear la clase se registra también su reserva en un solo paso.
    private final JCheckBox chkParaUnCliente =
            new JCheckBox("Clase para una sola persona");
    private final JComboBox<OpcionRelacion> cboClienteAsignar = new JComboBox<>();
    private final JTextField txtBuscar = new JTextField();
    private final JTable tblClases = new JTable();
    private final JLabel lblCantidad = new JLabel("0 clases");

    private Long idClaseSeleccionada;
    private List<ClaseGrupalResumen> clases = new ArrayList<>();

    public PnlClasesGrupalesAdministrador() {
        CalendarioSelector.vincularFechaHora(txtFechaHora);
        construirVista();
        configurarEventos();
        cargarEntrenadores();
        limpiarFormulario();
        refrescarDatos();
    }

    public final void refrescarDatos() {
        cargarClases();
    }

    private void construirVista() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new java.awt.Color(234, 242, 251));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        add(encabezado(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(12, 12));
        centro.setOpaque(false);
        centro.add(formulario(), BorderLayout.NORTH);
        centro.add(tabla(), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
    }

    private JPanel encabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(212,225,239)),
                BorderFactory.createEmptyBorder(14,18,14,18)));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new javax.swing.BoxLayout(textos, javax.swing.BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Clases");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        titulo.setForeground(new java.awt.Color(23,42,67));
        JLabel sub = new JLabel("Crea Yoga, Bailoterapia, Spinning u otras clases, asigna entrenador, fecha y cupo. También puedes asignar la clase a un cliente en particular.");
        sub.setForeground(new java.awt.Color(90,110,135));
        textos.add(titulo); textos.add(javax.swing.Box.createVerticalStrut(4)); textos.add(sub);
        JLabel mod = new JLabel("CLASES", SwingConstants.CENTER);
        mod.setForeground(new java.awt.Color(8,124,255));
        mod.setBorder(BorderFactory.createLineBorder(new java.awt.Color(8,124,255)));
        mod.setPreferredSize(new Dimension(105,45));
        p.add(textos, BorderLayout.CENTER); p.add(mod, BorderLayout.EAST);
        return p;
    }

    private JPanel formulario() {
        JPanel tarjeta = panelBlanco(new BorderLayout(8,8));
        JLabel titulo = new JLabel("Programar clase grupal");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        tarjeta.add(titulo, BorderLayout.NORTH);

        JPanel f = new JPanel(new GridBagLayout());
        f.setOpaque(false);
        int y=0;
        campo(f,y++,"Tipo / nombre de clase *",txtNombre,"Entrenador responsable *",cboEntrenador);
        campo(f,y++,"Fecha y hora * (AAAA-MM-DD HH:mm)",txtFechaHora,"Cupo máximo *",txtCupo);
        campo(f,y++,"Duración (minutos) *",txtDuracion,"Nivel",cboNivel);
        campo(f,y++,"Intensidad",cboIntensidad,"Descripción",new JScrollPane(txtDescripcion));

        // Fila con el switch para clase individual + su combo de cliente.
        chkParaUnCliente.setOpaque(false);
        chkParaUnCliente.setToolTipText(
                "Convierte la clase en una sesión personal para el cliente elegido. "
                + "El cupo pasa a 1 y se genera su reserva al guardar.");
        cboClienteAsignar.setEnabled(false);
        cboClienteAsignar.setPreferredSize(new Dimension(260, 26));
        campo(f, y++, "", chkParaUnCliente, "Cliente asignado", cboClienteAsignar);

        GridBagConstraints c=gbc(0,y); c.gridwidth=4; c.fill=GridBagConstraints.HORIZONTAL;
        f.add(chkActiva,c);
        tarjeta.add(f,BorderLayout.CENTER);

        JPanel acciones=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        acciones.setOpaque(false);
        estiloPrimario(btnCrear);
        estiloSecundario(btnActualizar);
        estiloAdvertencia(btnDesactivar);
        estiloClaro(btnLimpiar);
        acciones.add(btnCrear); acciones.add(btnActualizar); acciones.add(btnDesactivar); acciones.add(btnLimpiar);
        tarjeta.add(acciones,BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel tabla() {
        JPanel p=panelBlanco(new BorderLayout(8,8));
        JPanel barra=new JPanel(new BorderLayout(10,0)); barra.setOpaque(false);
        txtBuscar.setToolTipText("Buscar por clase, entrenador, nivel o intensidad");
        utilidades.EstilosComponentes.aplicarCampoSimple(txtBuscar);
        JPanel acciones=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); acciones.setOpaque(false);
        estiloClaro(btnRefrescar); acciones.add(btnRefrescar); acciones.add(lblCantidad);
        barra.add(txtBuscar,BorderLayout.CENTER); barra.add(acciones,BorderLayout.EAST);
        p.add(barra,BorderLayout.NORTH);

        DefaultTableModel m=new DefaultTableModel(new Object[]{
            "ID","Clase","Entrenador","Fecha / hora","Duración","Cupo","Inscritos","Disponibles","Estado"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        tblClases.setModel(m); estilizarTabla(tblClases);
        tblClases.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        p.add(new JScrollPane(tblClases),BorderLayout.CENTER);
        return p;
    }

    private void configurarEventos() {
        btnCrear.addActionListener(e->crear());
        btnActualizar.addActionListener(e->actualizar());
        btnDesactivar.addActionListener(e->desactivar());
        btnLimpiar.addActionListener(e->limpiarFormulario());
        btnRefrescar.addActionListener(e->cargarClases());
        txtBuscar.addActionListener(e->cargarClases());
        // Al activar el modo individual: se habilita el combo de cliente,
        // el cupo se fija en 1 y el campo se bloquea para dejar clara la
        // intención de la clase. Al desactivarlo se restauran los valores.
        chkParaUnCliente.addActionListener(e -> aplicarModoIndividual());
        tblClases.getSelectionModel().addListSelectionListener(e->{
            if(!e.getValueIsAdjusting()) cargarSeleccion();
        });
    }

    private void aplicarModoIndividual() {
        boolean individual = chkParaUnCliente.isSelected();
        cboClienteAsignar.setEnabled(individual);
        if (individual) {
            if (cboClienteAsignar.getItemCount() == 0) {
                cargarClientes();
            }
            txtCupo.setText("1");
            txtCupo.setEditable(false);
            txtCupo.setToolTipText(
                    "El cupo queda fijado en 1 porque la clase es para un solo cliente.");
        } else {
            txtCupo.setEditable(true);
            txtCupo.setToolTipText(null);
            if ("1".equals(txtCupo.getText().trim())) {
                txtCupo.setText("20");
            }
        }
    }

    private void cargarClientes() {
        try {
            DefaultComboBoxModel<OpcionRelacion> m = new DefaultComboBoxModel<>();
            for (OpcionRelacion o : catalogos.listar("idCliente")) {
                m.addElement(o);
            }
            cboClienteAsignar.setModel(m);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudieron cargar los clientes: " + ex.getMessage(),
                    "GYMNOVA", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cargarEntrenadores() {
        try {
            DefaultComboBoxModel<OpcionRelacion> m=new DefaultComboBoxModel<>();
            for(OpcionRelacion o:catalogos.listar("idEntrenador")) m.addElement(o);
            cboEntrenador.setModel(m);
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this,"No se pudieron cargar entrenadores: "+ex.getMessage(),"GYMNOVA",JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cargarClases() {
        clases=controlador.listarAdministracion(txtBuscar.getText().trim());
        DefaultTableModel m=(DefaultTableModel)tblClases.getModel(); m.setRowCount(0);
        for(ClaseGrupalResumen c:clases){
            m.addRow(new Object[]{c.getIdClase(),c.getNombreClase(),c.getNombreEntrenador(),
                fecha(c.getFechaHora()),c.getDuracionMinutos()+" min",
                c.getCupoMaximo(),c.getReservados(),c.getDisponibles(),
                c.isEstadoClase()?"ACTIVA":"INACTIVA"});
        }
        lblCantidad.setText(clases.size()+" clase(s)");
    }

    private void cargarSeleccion() {
        int fila=tblClases.getSelectedRow(); if(fila<0)return;
        Long id=((Number)tblClases.getValueAt(fila,0)).longValue();
        ClaseGrupalResumen c=clases.stream().filter(x->x.getIdClase().equals(id)).findFirst().orElse(null);
        if(c==null)return;
        idClaseSeleccionada=c.getIdClase();
        txtNombre.setText(c.getNombreClase());
        txtFechaHora.setText(fecha(c.getFechaHora()));
        txtCupo.setText(String.valueOf(c.getCupoMaximo()));
        txtDuracion.setText(String.valueOf(c.getDuracionMinutos()));
        txtDescripcion.setText(c.getDescripcion()==null?"":c.getDescripcion());
        seleccionar(cboNivel,c.getNivel()); seleccionar(cboIntensidad,c.getIntensidad());
        seleccionarEntrenador(c.getIdEntrenador()); chkActiva.setSelected(c.isEstadoClase());
    }

    private void crear() {
        ClaseGrupal c = leerFormulario(null);
        if (c == null) return;

        // En modo individual necesitamos un cliente seleccionado ANTES de
        // llegar a la BD, si no la clase se crearía sin reserva asociada.
        Long idClienteAsignar = null;
        if (chkParaUnCliente.isSelected()) {
            OpcionRelacion sel =
                    (OpcionRelacion) cboClienteAsignar.getSelectedItem();
            if (sel == null || !(sel.getId() instanceof Number n)) {
                error("Selecciona el cliente para la clase individual.");
                return;
            }
            idClienteAsignar = n.longValue();
            c.setCupoMaximo(1);
        }

        if (!controlador.crearClase(c)) {
            error(controlador.getMensaje());
            return;
        }

        if (idClienteAsignar != null && c.getIdClase() != null) {
            Long idReserva = controlador.reservarParaCliente(
                    idClienteAsignar, c.getIdClase().intValue(),
                    "Clase individual creada desde gestión de clases");
            if (idReserva == null) {
                error("Clase creada, pero no se pudo reservar al cliente: "
                        + controlador.getMensaje());
                limpiarFormulario();
                cargarClases();
                return;
            }
            mensaje("Clase individual creada y asignada al cliente. Reserva #"
                    + idReserva + ".");
        } else {
            mensaje("Clase grupal creada correctamente.");
        }
        limpiarFormulario();
        cargarClases();
    }

    private void actualizar() {
        if(idClaseSeleccionada==null){error("Seleccione una clase de la tabla.");return;}
        ClaseGrupal c=leerFormulario(idClaseSeleccionada); if(c==null)return;
        if(controlador.actualizarClase(c)){
            mensaje("Clase grupal actualizada correctamente."); limpiarFormulario(); cargarClases();
        } else error(controlador.getMensaje());
    }

    private void desactivar() {
        if(idClaseSeleccionada==null){error("Seleccione una clase de la tabla.");return;}
        if(JOptionPane.showConfirmDialog(this,"¿Desactivar la clase seleccionada?","GYMNOVA",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        if(controlador.desactivarClase(idClaseSeleccionada)){
            mensaje("Clase desactivada."); limpiarFormulario(); cargarClases();
        } else error(controlador.getMensaje());
    }

    private ClaseGrupal leerFormulario(Long id) {
        try {
            String nombre=txtNombre.getText().trim();
            if(nombre.isBlank()) throw new IllegalArgumentException("Ingrese el tipo o nombre de la clase.");
            OpcionRelacion entrenador=(OpcionRelacion)cboEntrenador.getSelectedItem();
            if(entrenador==null) throw new IllegalArgumentException("Seleccione un entrenador.");
            int cupo=Integer.parseInt(txtCupo.getText().trim());
            int duracion=Integer.parseInt(txtDuracion.getText().trim());
            LocalDateTime fecha=LocalDateTime.parse(txtFechaHora.getText().trim(),FORMATO);
            if(id==null && !fecha.isAfter(LocalDateTime.now())) throw new IllegalArgumentException("La fecha de una nueva clase debe ser futura.");
            ClaseGrupal c=new ClaseGrupal(); c.setIdClase(id); c.setNombreClase(nombre);
            c.setIdEntrenador(((Number)entrenador.getId()).longValue()); c.setCupoMaximo(cupo);
            c.setDuracionBaseMinutos(duracion); c.setFechaHora(fecha);
            c.setNivel(String.valueOf(cboNivel.getSelectedItem())); c.setIntensidad(String.valueOf(cboIntensidad.getSelectedItem()));
            c.setDescripcion(txtDescripcion.getText().trim()); c.setEstadoClase(chkActiva.isSelected());
            return c;
        } catch(NumberFormatException ex){error("Cupo y duración deben ser números enteros.");}
          catch(DateTimeParseException ex){error("Fecha/hora inválida. Use el formato AAAA-MM-DD HH:mm, por ejemplo 2026-09-02 18:30.");}
          catch(IllegalArgumentException ex){error(ex.getMessage());}
        return null;
    }

    private void limpiarFormulario(){
        idClaseSeleccionada=null; tblClases.clearSelection(); txtNombre.setText("");
        chkParaUnCliente.setSelected(false);
        cboClienteAsignar.setEnabled(false);
        txtCupo.setEditable(true);
        txtCupo.setToolTipText(null);
        txtCupo.setText("20");
        txtDuracion.setText("60"); txtFechaHora.setText(LocalDateTime.now().plusDays(1).withSecond(0).withNano(0).format(FORMATO));
        txtDescripcion.setText(""); cboNivel.setSelectedIndex(0); cboIntensidad.setSelectedIndex(1); chkActiva.setSelected(true);
        if(cboEntrenador.getItemCount()>0)cboEntrenador.setSelectedIndex(0);
    }

    private void seleccionarEntrenador(Long id){
        for(int i=0;i<cboEntrenador.getItemCount();i++){
            OpcionRelacion o=cboEntrenador.getItemAt(i);
            if(o!=null && o.getId() instanceof Number n && n.longValue()==id){cboEntrenador.setSelectedIndex(i);return;}
        }
    }
    private void seleccionar(JComboBox<String> c,String valor){if(valor==null)return; for(int i=0;i<c.getItemCount();i++)if(valor.equalsIgnoreCase(c.getItemAt(i))){c.setSelectedIndex(i);return;}}
    private String fecha(LocalDateTime f){return f==null?"-":f.format(FORMATO);}

    private void campo(JPanel p,int y,String l1,java.awt.Component c1,String l2,java.awt.Component c2){
        GridBagConstraints a=gbc(0,y); a.weightx=0; a.fill=GridBagConstraints.NONE; p.add(new JLabel(l1),a);
        a=gbc(1,y); a.weightx=1; a.fill=GridBagConstraints.HORIZONTAL; p.add(c1,a);
        a=gbc(2,y); a.weightx=0; a.fill=GridBagConstraints.NONE; p.add(new JLabel(l2),a);
        a=gbc(3,y); a.weightx=1; a.fill=GridBagConstraints.HORIZONTAL; p.add(c2,a);
    }
    private GridBagConstraints gbc(int x,int y){GridBagConstraints c=new GridBagConstraints();c.gridx=x;c.gridy=y;c.insets=new Insets(5,7,5,7);c.anchor=GridBagConstraints.WEST;return c;}
    private JPanel panelBlanco(java.awt.LayoutManager l){JPanel p=new JPanel(l);p.setBackground(java.awt.Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new java.awt.Color(212,225,239)),BorderFactory.createEmptyBorder(12,12,12,12)));return p;}
    private void estilizarTabla(JTable t){t.setRowHeight(36);t.setShowGrid(false);t.setFillsViewportHeight(true);t.setSelectionBackground(new java.awt.Color(220,238,255));t.setSelectionForeground(new java.awt.Color(23,42,67));t.getTableHeader().setPreferredSize(new Dimension(0,40));DefaultTableCellRenderer h=new DefaultTableCellRenderer();h.setOpaque(true);h.setBackground(new java.awt.Color(10,58,108));h.setForeground(java.awt.Color.WHITE);h.setHorizontalAlignment(SwingConstants.CENTER);t.getTableHeader().setDefaultRenderer(h);}
    private void estiloPrimario(JButton b){utilidades.EstilosComponentes.aplicarBotonPremium(b,new java.awt.Color(8,124,255),new java.awt.Color(54,207,255),java.awt.Color.WHITE);}
    private void estiloSecundario(JButton b){utilidades.EstilosComponentes.aplicarBotonPremium(b,new java.awt.Color(109,40,217),new java.awt.Color(168,85,247),java.awt.Color.WHITE);}
    private void estiloAdvertencia(JButton b){utilidades.EstilosComponentes.aplicarBotonPremium(b,new java.awt.Color(255,214,0),new java.awt.Color(255,232,82),new java.awt.Color(41,31,0));}
    private void estiloClaro(JButton b){utilidades.EstilosComponentes.aplicarBotonPremium(b,new java.awt.Color(241,245,249),new java.awt.Color(226,232,240),new java.awt.Color(52,74,100));}
    private void mensaje(String m){JOptionPane.showMessageDialog(this,m,"GYMNOVA",JOptionPane.INFORMATION_MESSAGE);}
    private void error(String m){JOptionPane.showMessageDialog(this,m==null||m.isBlank()?"No se pudo completar la operación.":m,"GYMNOVA",JOptionPane.WARNING_MESSAGE);}
}
