package utilidades;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import modelo.ReporteResultado;

/** Generador PDF real y liviano para reportes tabulares de GYMNOVA. */
public final class GeneradorReportePDF {
    private GeneradorReportePDF() {}

    public static void generar(ReporteResultado reporte, String periodo,
            String filtros, Path destino) throws IOException {
        if (reporte == null) throw new IllegalArgumentException("No existe un reporte generado.");
        if (destino == null) throw new IllegalArgumentException("Destino inválido.");
        Path padre=destino.toAbsolutePath().getParent(); if(padre!=null) Files.createDirectories(padre);
        Files.write(destino, construirPdf(construirPaginas(reporte,periodo,filtros)));
    }

    private static List<String> construirPaginas(ReporteResultado r,String periodo,String filtros){
        int cols=Math.max(1,r.getColumnas().size());
        int porPagina=24;
        int total=Math.max(1,(r.getFilas().size()+porPagina-1)/porPagina);
        List<String> paginas=new ArrayList<>();
        for(int p=0;p<total;p++){
            int ini=p*porPagina, fin=Math.min(r.getFilas().size(),ini+porPagina);
            StringBuilder c=new StringBuilder();
            color(c,.02,.17,.35); rect(c,25,770,545,48,true); color(c,1,1,1);
            texto(c,"F2",20,40,793,"GYMNOVA"); texto(c,"F2",12,300,793,limitar(r.getTitulo(),42));
            texto(c,"F1",8,300,778,"Pagina "+(p+1)+" de "+total);
            color(c,.06,.12,.22); texto(c,"F1",8,35,750,"Generado: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            texto(c,"F1",8,35,737,"Periodo: "+nulo(periodo)); texto(c,"F1",8,300,737,"Filtros: "+limitar(nulo(filtros),48));
            int y=716;
            if(p==0){
                for(Map.Entry<String,String> e:r.getResumen().entrySet()){
                    texto(c,"F2",8,35,y,limitar(e.getKey()+":",25)); texto(c,"F1",8,180,y,limitar(e.getValue(),55)); y-=14;
                    if(y<650) break;
                }
                y-=6;
            }
            double ancho=535.0/cols;
            color(c,.03,.39,.85); rect(c,30,y-4,535,22,true); color(c,1,1,1);
            for(int i=0;i<cols;i++) texto(c,"F2",Math.min(8,cols>6?6:8),(int)(34+i*ancho),y+4,limitar(r.getColumnas().get(i),Math.max(7,(int)(ancho/5.3))));
            y-=23;
            List<Object[]> filas=r.getFilas().subList(ini,fin);
            if(filas.isEmpty()) { color(c,.1,.2,.3); texto(c,"F1",9,35,y,"Sin registros para los filtros seleccionados."); }
            for(Object[] fila:filas){
                color(c,.08,.16,.27);
                for(int i=0;i<cols;i++){
                    String v=i<fila.length&&fila[i]!=null?fila[i].toString():"-";
                    texto(c,"F1",cols>6?6:7,(int)(34+i*ancho),y,limitar(v,Math.max(7,(int)(ancho/4.8))));
                }
                color(c,.82,.87,.93); linea(c,30,y-6,565,y-6); y-=23;
            }
            color(c,.38,.46,.57); texto(c,"F1",7,35,28,"Reporte administrativo generado por GYMNOVA.");
            paginas.add(c.toString());
        }
        return paginas;
    }

    private static byte[] construirPdf(List<String> contenidos)throws IOException{
        List<byte[]> objs=new ArrayList<>(); int n=contenidos.size(); StringBuilder kids=new StringBuilder();
        for(int i=0;i<n;i++) kids.append(5+i*2).append(" 0 R ");
        objs.add(bytes("<< /Type /Catalog /Pages 2 0 R >>")); objs.add(bytes("<< /Type /Pages /Count "+n+" /Kids ["+kids+"] >>"));
        objs.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>")); objs.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>"));
        for(int i=0;i<n;i++){ int cid=6+i*2; objs.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents "+cid+" 0 R >>")); byte[] s=bytes(contenidos.get(i)); objs.add(bytes("<< /Length "+s.length+" >>\nstream\n"+contenidos.get(i)+"\nendstream")); }
        ByteArrayOutputStream out=new ByteArrayOutputStream(); out.write(bytes("%PDF-1.4\n%GYMNOVA\n")); long[] off=new long[objs.size()+1];
        for(int i=0;i<objs.size();i++){off[i+1]=out.size();out.write(bytes((i+1)+" 0 obj\n"));out.write(objs.get(i));out.write(bytes("\nendobj\n"));}
        long x=out.size();out.write(bytes("xref\n0 "+(objs.size()+1)+"\n0000000000 65535 f \n"));for(int i=1;i<off.length;i++)out.write(bytes(String.format("%010d 00000 n \n",off[i])));out.write(bytes("trailer\n<< /Size "+(objs.size()+1)+" /Root 1 0 R >>\nstartxref\n"+x+"\n%%EOF\n"));return out.toByteArray();
    }
    private static void texto(StringBuilder c,String f,int t,int x,int y,String s){c.append("BT /").append(f).append(' ').append(t).append(" Tf ").append(x).append(' ').append(y).append(" Td (").append(escape(ascii(s))).append(") Tj ET\n");}
    private static void color(StringBuilder c,double r,double g,double b){c.append(r).append(' ').append(g).append(' ').append(b).append(" rg\n");}
    private static void rect(StringBuilder c,int x,int y,int w,int h,boolean fill){c.append(x).append(' ').append(y).append(' ').append(w).append(' ').append(h).append(" re ").append(fill?"f":"S").append('\n');}
    private static void linea(StringBuilder c,int x1,int y1,int x2,int y2){c.append(x1).append(' ').append(y1).append(" m ").append(x2).append(' ').append(y2).append(" l S\n");}
    private static String ascii(String s){return Normalizer.normalize(nulo(s),Normalizer.Form.NFD).replaceAll("\\p{M}","").replaceAll("[^\\x20-\\x7E]","?");}
    private static String escape(String s){return s.replace("\\","\\\\").replace("(","\\(").replace(")","\\)");}
    private static String limitar(String s,int m){s=nulo(s);return s.length()<=m?s:s.substring(0,Math.max(1,m-3))+"...";}
    private static String nulo(String s){return s==null||s.isBlank()?"-":s;}
    private static byte[] bytes(String s){return s.getBytes(StandardCharsets.ISO_8859_1);}
}
