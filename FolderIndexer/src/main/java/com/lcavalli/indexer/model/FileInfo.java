package com.lcavalli.indexer.model;

public class FileInfo {
    private final String nome;
    private final String ultimaModifica;
    private final String tipo;
    private final String dimensione;
    private final String percorso;

    public FileInfo(String nome, String ultimaModifica, String tipo, String dimensione, String percorso) {
        this.nome = nome;
        this.ultimaModifica = ultimaModifica;
        this.tipo = tipo;
        this.dimensione = dimensione;
        this.percorso = percorso;
    }

    public String getNome() { return nome; }
    public String getUltimaModifica() { return ultimaModifica; }
    public String getTipo() { return tipo; }
    public String getDimensione() { return dimensione; }
    public String getPercorso() { return percorso; }
}