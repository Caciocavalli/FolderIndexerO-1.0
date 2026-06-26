package com.lcavalli.indexer.service;

import com.lcavalli.indexer.model.FileInfo;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ExcelExportService {

    public void exportToExcel(Path outputPath, List<FileInfo> files) throws IOException {
        try (OutputStream os = Files.newOutputStream(outputPath);
             Workbook wb = new Workbook(os, "FolderIndexer", "1.0")) {
            
            Worksheet ws = wb.newWorksheet("Contenuto Cartella");
            
            // 1. Scrittura Intestazioni (Riga 0)
            ws.value(0, 0, "Nome");
            ws.value(0, 1, "Ultima modifica");
            ws.value(0, 2, "Tipo");
            ws.value(0, 3, "Dimensione");
            ws.value(0, 4, "Percorso File");
            ws.value(0, 5, "Link Cartella"); 
            
            // Stile Intestazioni: Grassetto + Sfondo Azzurro Chiaro
            String headerBgColor = "DCE6F1";
            ws.style(0, 0).bold().fillColor(headerBgColor).set();
            ws.style(0, 1).bold().fillColor(headerBgColor).set();
            ws.style(0, 2).bold().fillColor(headerBgColor).set();
            ws.style(0, 3).bold().fillColor(headerBgColor).set();
            ws.style(0, 4).bold().fillColor(headerBgColor).set();
            ws.style(0, 5).bold().fillColor(headerBgColor).set();

            // 2. Scrittura dei dati dei file (Dalla riga 1 in poi)
            int row = 1;
            for (FileInfo file : files) {
                ws.value(row, 0, file.getNome());
                ws.value(row, 1, file.getUltimaModifica());
                ws.value(row, 2, file.getTipo());
                ws.value(row, 3, file.getDimensione());
                ws.value(row, 4, file.getPercorso());
                
                Path filePath = Paths.get(file.getPercorso());
                Path parentPath = filePath.getParent();
                
                if (parentPath != null) {
                    String parentStr = parentPath.toAbsolutePath().toString();
                    String formulaStr = "HYPERLINK(\"" + parentStr + "\", \"Vedi in explorer\")";
                    ws.formula(row, 5, formulaStr);
                }
                
                // 📊 GESTIONE SFONDO ALTERNATO (Zebra Striping)
                // Se la riga è pari (2, 4, 6...), applichiamo lo sfondo sabbia chiaro. Le righe dispari restano bianche.
                boolean isEvenRow = (row % 2 == 0);
                String rowBgColor = "F5F2EB"; // MODIFICATO: Tonalità sabbia chiaro/crema molto tenue e professionale
                
                // Applichiamo lo sfondo alle prime 5 colonne (da A a E) se la riga è pari
                for (int col = 0; col <= 4; col++) {
                    if (isEvenRow) {
                        ws.style(row, col).fillColor(rowBgColor).set();
                    }
                }
                
                // Gestione dello stile per la colonna del Link (Colonna 5 -> F)
                if (parentPath != null) {
                    // Il link deve essere sempre BLU e SOTTOLINEATO
                    var linkStyle = ws.style(row, 5).fontColor("0056B3").underlined();
                    
                    // Se la riga è pari, aggiungiamo anche lo sfondo sabbia
                    if (isEvenRow) {
                        linkStyle.fillColor(rowBgColor);
                    }
                    linkStyle.set();
                } else {
                    // Se non c'è il link, coloriamo solo lo sfondo se la riga è pari
                    if (isEvenRow) {
                        ws.style(row, 5).fillColor(rowBgColor).set();
                    }
                }
                
                row++;
            }
        }
    }
}