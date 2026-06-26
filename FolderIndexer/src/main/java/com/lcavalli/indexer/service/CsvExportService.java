package com.lcavalli.indexer.service;

import com.lcavalli.indexer.model.FileInfo;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CsvExportService {

    public void exportToCsv(Path outputPath, List<FileInfo> files) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            // 1. Scrittura Intestazioni (Standard italiano con punto e virgola)
            writer.write("Nome;Ultima modifica;Tipo;Dimensione;Percorso File;Link Cartella");
            writer.newLine();

            // 2. Scrittura dei dati
            for (FileInfo file : files) {
                String nome = escapeCsv(file.getNome());
                String modifica = escapeCsv(file.getUltimaModifica());
                String tipo = escapeCsv(file.getTipo());
                String dimensione = escapeCsv(file.getDimensione());
                String percorso = escapeCsv(file.getPercorso());
                
                String linkCartella = "";
                Path filePath = Paths.get(file.getPercorso());
                Path parentPath = filePath.getParent();
                
                if (parentPath != null) {
                    String parentStr = parentPath.toAbsolutePath().toString();
                    // Finezza tecnica: Nei CSV aperti in Italia, i parametri della formula HYPERLINK 
                    // vogliono il punto e virgola ";" al posto della virgola ","
                    linkCartella = "=HYPERLINK(\"" + parentStr + "\";\"Vedi in explorer\")";
                    linkCartella = escapeCsv(linkCartella);
                }

                // Scrive la riga nel file
                writer.write(String.format("%s;%s;%s;%s;%s;%s", nome, modifica, tipo, dimensione, percorso, linkCartella));
                writer.newLine();
            }
        }
    }

    /**
     * Gestisce l'escape dei caratteri speciali nel CSV (es. se un nome file contiene un punto e virgola)
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = value.replace("\"", "\"\""); // Raddoppia le virgolette interne secondo lo standard RFC 4180
            return "\"" + value + "\"";
        }
        return value;
    }
}