package com.renombrador;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renombrador a Excel:
 * Escanea PDFs en una carpeta cuyo nombre coincide con
 *    <prefijo>_PE <algo>_<NNNNN>...
 * Lo que importa es el SEGUNDO sufijo (el numero despues del segundo "_").
 * Renglon 1: contador secuencial 1, 2, 3, ...
 * Renglon 2: el segundo sufijo extraido del nombre.
 *
 * Ejemplos:
 *    210_PE 1_47000.pdf   -> col1: 1   /   row1=1, row2=47000
 *    210_PE 15_73.pdf     -> col2: 2   /   row1=2, row2=73
 */
public class Main {

    // Solo nos interesa el numero despues del SEGUNDO "_".
    // Captura cualquier numero al final de la zona "_NNNNN".
    // Acepta espacios, letras, cualquier cosa antes del segundo "_".
    private static final Pattern PATRON =
            Pattern.compile("^[^_]+_[^_]+_(\\d+)", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        // 1) Elegir carpeta raiz
        JFileChooser carpetaChooser = new JFileChooser();
        carpetaChooser.setDialogTitle("Selecciona la carpeta con los PDFs");
        carpetaChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (carpetaChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File carpeta = carpetaChooser.getSelectedFile();

        // 2) Listar PDFs y extraer datos
        File[] archivos = carpeta.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (archivos == null || archivos.length == 0) {
            JOptionPane.showMessageDialog(null,
                    "No se encontraron archivos .pdf en:\n" + carpeta.getAbsolutePath(),
                    "Sin PDFs", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Arrays.sort(archivos, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        List<String> segundosSufijos = new ArrayList<>();
        List<String> ignorados = new ArrayList<>();

        for (File f : archivos) {
            String nombre = f.getName();
            // quitar extension para que el patron matchee limpio
            String base = nombre.substring(0, nombre.length() - 4);
            Matcher m = PATRON.matcher(base);
            if (m.find()) {
                segundosSufijos.add(m.group(1));
            } else {
                ignorados.add(nombre);
            }
        }

        if (segundosSufijos.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Ningun PDF coincide con el patron 'XXX_PE N_NNNNN'.",
                    "Sin coincidencias", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3) Elegir destino del Excel
        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setDialogTitle("Guardar Excel como");
        saveChooser.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        saveChooser.setSelectedFile(new File(carpeta, "resultado.xlsx"));
        if (saveChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File destino = saveChooser.getSelectedFile();
        if (!destino.getName().toLowerCase().endsWith(".xlsx")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".xlsx");
        }

        // 4) Escribir xlsx con Apache POI: 2 renglones, una columna por archivo
        try (XSSFWorkbook wb = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(destino)) {

            Sheet hoja = wb.createSheet("Datos");
            Row r1 = hoja.createRow(0); // contador 1, 2, 3, ...
            Row r2 = hoja.createRow(1); // segundo sufijo

            for (int i = 0; i < segundosSufijos.size(); i++) {
                r1.createCell(i).setCellValue(i + 1);
                Cell c2 = r2.createCell(i);
                try {
                    c2.setCellValue(Double.parseDouble(segundosSufijos.get(i)));
                } catch (NumberFormatException e) {
                    c2.setCellValue(segundosSufijos.get(i));
                }
            }
            wb.write(out);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Error escribiendo el Excel:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 5) Reporte final
        StringBuilder msg = new StringBuilder();
        msg.append("Excel generado: ").append(destino.getAbsolutePath()).append('\n');
        msg.append("PDFs procesados: ").append(segundosSufijos.size()).append('\n');
        if (!ignorados.isEmpty()) {
            msg.append("Ignorados (no cumplen el patron): ").append(ignorados.size()).append('\n');
            int n = Math.min(5, ignorados.size());
            for (int i = 0; i < n; i++) msg.append("  - ").append(ignorados.get(i)).append('\n');
            if (ignorados.size() > n) msg.append("  ... y ").append(ignorados.size() - n).append(" mas\n");
        }
        JOptionPane.showMessageDialog(null, msg.toString(), "Listo", JOptionPane.INFORMATION_MESSAGE);
    }
}
