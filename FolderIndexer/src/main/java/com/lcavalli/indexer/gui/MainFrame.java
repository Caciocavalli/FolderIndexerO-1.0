package com.lcavalli.indexer.gui;

import com.lcavalli.indexer.model.FileInfo;
import com.lcavalli.indexer.service.ExcelExportService;
import com.lcavalli.indexer.service.CsvExportService;
import com.lcavalli.indexer.service.FileScannerService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MainFrame extends JFrame {
    private final JTextField txtScanDir;
    private final JTextField txtExportFile;
    private final JCheckBox chkExcel;
    private final JCheckBox chkCsv;
    
    private final FileScannerService scannerService;
    private final ExcelExportService excelService;
    private final CsvExportService csvService;

    public MainFrame() {
        this.scannerService = new FileScannerService();
        this.excelService = new ExcelExportService();
        this.csvService = new CsvExportService();

        setTitle("Folder to Excel/CSV Indexer 📊");
        setSize(600, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Sezione 1: Cartella Sorgente
        JPanel scanPanel = new JPanel(new BorderLayout(5, 5));
        txtScanDir = new JTextField("Seleziona la cartella da indicizzare...");
        txtScanDir.setEditable(false);
        JButton btnBrowseScan = new JButton("Sfoglia...");
        btnBrowseScan.addActionListener(e -> chooseScanDirectory());
        scanPanel.add(txtScanDir, BorderLayout.CENTER);
        scanPanel.add(btnBrowseScan, BorderLayout.EAST);
        scanPanel.setBorder(BorderFactory.createTitledBorder("1. Cartella da Scansionare"));

        // Sezione 2: Nome base del file di destinazione
        JPanel exportPanel = new JPanel(new BorderLayout(5, 5));
        txtExportFile = new JTextField("Seleziona dove salvare il report...");
        txtExportFile.setEditable(false);
        JButton btnBrowseExport = new JButton("Salva come...");
        btnBrowseExport.addActionListener(e -> chooseExportFile());
        exportPanel.add(txtExportFile, BorderLayout.CENTER);
        exportPanel.add(btnBrowseExport, BorderLayout.EAST);
        exportPanel.setBorder(BorderFactory.createTitledBorder("2. Percorso e Nome Base del Report (Senza Estensione)"));

        // Sezione 3: Caselline Flaggabili per il Formato
        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        chkExcel = new JCheckBox("Salva come file Excel (.xlsx)", true);
        chkCsv = new JCheckBox("Salva come file CSV (.csv)", false);
        chkExcel.setFont(new Font("Arial", Font.PLAIN, 12));
        chkCsv.setFont(new Font("Arial", Font.PLAIN, 12));
        formatPanel.add(chkExcel);
        formatPanel.add(chkCsv);
        formatPanel.setBorder(BorderFactory.createTitledBorder("3. Formati di Esportazione (Selezionabili anche entrambi)"));

        centerPanel.add(scanPanel);
        centerPanel.add(exportPanel);
        centerPanel.add(formatPanel);

        JButton btnExecute = new JButton("Genera Report 🚀");
        btnExecute.setFont(new Font("Arial", Font.BOLD, 14));
        btnExecute.addActionListener(e -> handleExport());

        add(centerPanel, BorderLayout.CENTER);
        add(btnExecute, BorderLayout.SOUTH);
    }

    private void chooseScanDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            txtScanDir.setText(selectedDir.getAbsolutePath());
            
            String defaultFileName = selectedDir.getName() + "_report";
            txtExportFile.setText(new File(selectedDir.getParent(), defaultFileName).getAbsolutePath());
        }
    }

    private void chooseExportFile() {
        JFileChooser chooser = new JFileChooser();
        String currentPath = txtExportFile.getText();
        
        if (!currentPath.equals("Seleziona dove salvare il report...")) {
            chooser.setSelectedFile(new File(currentPath));
        } else {
            chooser.setSelectedFile(new File("report_cartella"));
        }
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String path = file.getAbsolutePath();
            
            if (path.toLowerCase().endsWith(".xlsx")) path = path.substring(0, path.length() - 5);
            if (path.toLowerCase().endsWith(".csv")) path = path.substring(0, path.length() - 4);
            
            txtExportFile.setText(path);
        }
    }

    private void handleExport() {
        String scanPathText = txtScanDir.getText();
        String exportPathText = txtExportFile.getText();

        if (scanPathText.equals("Seleziona la cartella da indicizzare...")) {
            JOptionPane.showMessageDialog(this, "Seleziona prima una cartella da scansionare!", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (exportPathText.equals("Seleziona dove salvare il report...")) {
            JOptionPane.showMessageDialog(this, "Scegli un percorso e un nome base per il report!", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!chkExcel.isSelected() && !chkCsv.isSelected()) {
            JOptionPane.showMessageDialog(this, "Seleziona almeno un formato di salvataggio (Excel o CSV)!", "Attenzione", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pulizia del percorso base (definito qui per usarlo sia nel controllo che nel thread)
        String baseOutputPath = exportPathText;
        if (baseOutputPath.toLowerCase().endsWith(".xlsx")) baseOutputPath = baseOutputPath.substring(0, baseOutputPath.length() - 5);
        if (baseOutputPath.toLowerCase().endsWith(".csv")) baseOutputPath = baseOutputPath.substring(0, baseOutputPath.length() - 4);

        // 🛡️ CONTROLLO PRECAUZIONALE DI SOVRASCRITTURA
        boolean fileGiaEsistente = false;
        StringBuilder msgEsistenti = new StringBuilder("Attenzione! Nella cartella esistono già i seguenti file:\n");

        if (chkExcel.isSelected() && Files.exists(Paths.get(baseOutputPath + ".xlsx"))) {
            fileGiaEsistente = true;
            msgEsistenti.append("- ").append(Paths.get(baseOutputPath + ".xlsx").getFileName()).append("\n");
        }
        if (chkCsv.isSelected() && Files.exists(Paths.get(baseOutputPath + ".csv"))) {
            fileGiaEsistente = true;
            msgEsistenti.append("- ").append(Paths.get(baseOutputPath + ".csv").getFileName()).append("\n");
        }

        if (fileGiaEsistente) {
            msgEsistenti.append("\nVuoi sovrascreverli e procedere con il nuovo report?");
            int scelta = JOptionPane.showConfirmDialog(this, 
                    msgEsistenti.toString(), 
                    "Conferma Sovrascrittura", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
            
            // Se l'utente clicca su "No" o chiude la finestra, interrompiamo l'esecuzione immediatamente
            if (scelta != JOptionPane.YES_OPTION) {
                return; 
            }
        }

        // Cursore di caricamento ed esecuzione asincrona
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        final String finalBasePath = baseOutputPath; // Variabile finale per il thread
        new Thread(() -> {
            try {
                Path scanPath = Paths.get(scanPathText);
                List<FileInfo> files = scannerService.scanDirectory(scanPath);
                
                StringBuilder successMessage = new StringBuilder("Indicizzazione completata con successo!\n");
                successMessage.append("Elementi trovati: ").append(files.size()).append("\n\nFile generati:\n");

                if (chkExcel.isSelected()) {
                    Path excelPath = Paths.get(finalBasePath + ".xlsx");
                    excelService.exportToExcel(excelPath, files);
                    successMessage.append("- ").append(excelPath.getFileName()).append("\n");
                }
                
                if (chkCsv.isSelected()) {
                    Path csvPath = Paths.get(finalBasePath + ".csv");
                    csvService.exportToCsv(csvPath, files);
                    successMessage.append("- ").append(csvPath.getFileName()).append("\n");
                }

                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(this, successMessage.toString(), "Successo", JOptionPane.INFORMATION_MESSAGE);
                });

            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    JOptionPane.showMessageDialog(this, "Errore durante l'elaborazione: " + ex.getMessage(), "Errore I/O", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}