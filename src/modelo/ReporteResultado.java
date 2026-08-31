package modelo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Resultado genérico y de solo lectura de un reporte administrativo. */
public class ReporteResultado {
    private final String titulo;
    private final List<String> columnas;
    private final List<Object[]> filas;
    private final LinkedHashMap<String, String> resumen;

    public ReporteResultado(String titulo, List<String> columnas,
            List<Object[]> filas, Map<String, String> resumen) {
        this.titulo = titulo;
        this.columnas = new ArrayList<>(columnas);
        this.filas = new ArrayList<>(filas);
        this.resumen = new LinkedHashMap<>();
        if (resumen != null) this.resumen.putAll(resumen);
    }

    public String getTitulo() { return titulo; }
    public List<String> getColumnas() { return new ArrayList<>(columnas); }
    public List<Object[]> getFilas() { return new ArrayList<>(filas); }
    public Map<String, String> getResumen() { return new LinkedHashMap<>(resumen); }
}
