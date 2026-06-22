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
 *    <prefijo>_PE <N>_<NNNNN>...
 * Extrae N (renglon 1) y NNNNN (renglon 2) y los escribe en columnas
 * consecutivas dentro de un archivo .xlsx.
 */
public class Main {

    // Acepta cualquier prefijo (digitos, letras o mezcla), seguido de "_PE ",
    // luego el numero "N", luego "_", luego el numero "NNNNN".
    // Ejemplo que matchea: "210_PE 1_47800.pdf", "ABC_PE 12_89000 (copia).pdf"
    private static final Pattern PATRON =
            Pattern.compile("^[^_]+_PE\\s+(\\d+)_(\\d+)", Pattern.CASE_INSENSITIVE);

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

        List<String> renglon1 = new ArrayList<>();
        List<String> renglon2 = new ArrayList<>();
        List<String> ignorados = new ArrayList<>();

        for (File f : archivos) {
            String nombre = f.getName();
            // quitar extension para que el patron matchee limpio
            String base = nombre.substring(0, nombre.length() - 4);
            Matcher m = PATRON.matcher(base);
            if (m.find()) {
                renglon1.add(m.group(1));
                renglon2.add(m.group(2));
            } else {
                ignorados.add(nombre);
            }
        }

        if (renglon1.isEmpty()) {
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
            Row r1 = hoja.createRow(0);
            Row r2 = hoja.createRow(1);

            for (int i = 0; i < renglon1.size(); i++) {
                Cell c1 = r1.createCell(i);
                Cell c2 = r2.createCell(i);
                try {
                    c1.setCellValue(Double.parseDouble(renglon1.get(i)));
                } catch (NumberFormatException e) {
                    c1.setCellValue(renglon1.get(i));
                }
                try {
                    c2.setCellValue(Double.parseDouble(renglon2.get(i)));
                } catch (NumberFormatException e) {
                    c2.setCellValue(renglon2.get(i));
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
        msg.append("PDFs procesados: ").append(renglon1.size()).append('\n');
        if (!ignorados.isEmpty()) {
            msg.append("Ignorados (no cumplen el patron): ").append(ignorados.size()).append('\n');
            int n = Math.min(5, ignorados.size());
            for (int i = 0; i < n; i++) msg.append("  - ").append(ignorados.get(i)).append('\n');
            if (ignorados.size() > n) msg.append("  ... y ").append(ignorados.size() - n).append(" mas\n");
        }
        JOptionPane.showMessageDialog(null, msg.toString(), "Listo", JOptionPane.INFORMATION_MESSAGE);
    }
}
