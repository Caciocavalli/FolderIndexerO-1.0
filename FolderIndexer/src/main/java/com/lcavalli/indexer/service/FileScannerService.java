package com.lcavalli.indexer.service;

import com.lcavalli.indexer.model.FileInfo;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileScannerService {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    public List<FileInfo> scanDirectory(Path startPath) throws IOException {
        List<FileInfo> fileList = new ArrayList<>();

        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                // MODIFICA QUI: Non aggiungiamo più la cartella a 'fileList'.
                // Ritorniamo solo CONTINUE così Java entra dentro la cartella a cercare i file.
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // Qui troviamo i file veri e propri e li aggiungiamo
                fileList.add(createFileInfo(file, attrs));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

        return fileList;
    }

    // Abbiamo rimosso il boolean 'isDirectory' perché ora questo metodo elabora SOLO file
    private FileInfo createFileInfo(Path path, BasicFileAttributes attrs) {
        String nome = path.getFileName().toString();
        String ultimaModifica = dateFormatter.format(attrs.lastModifiedTime().toInstant());
        String tipo = getFileType(nome);
        
        // Calcolo della dimensione (solo per file)
        long bytes = attrs.size();
        long kb = (long) Math.ceil(bytes / 1024.0);
        String dimensione = String.format("%,d KB", kb);

        String percorso = path.toAbsolutePath().toString();
        return new FileInfo(nome, ultimaModifica, tipo, dimensione, percorso);
    }

    private String getFileType(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            String ext = fileName.substring(lastDot + 1).toUpperCase();
            return "File " + ext;
        }
        return "File";
    }
}