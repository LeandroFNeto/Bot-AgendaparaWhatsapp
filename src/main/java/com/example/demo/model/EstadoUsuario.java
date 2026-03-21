package com.example.demo.model;

public class EstadoUsuario {

    private String fase;
    private long ultimaInteracao;

    public EstadoUsuario(String fase, long ultimaInteracao) {
        this.fase = fase;
        this.ultimaInteracao = ultimaInteracao;
    }

    public String getFase() { return fase; }
    public long getUltimaInteracao() { return ultimaInteracao; }
}
