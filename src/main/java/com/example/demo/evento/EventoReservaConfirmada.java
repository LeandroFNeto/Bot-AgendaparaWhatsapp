package com.example.demo.evento;

import com.example.demo.model.Empresa;

public class EventoReservaConfirmada {

    private final Empresa empresa;
    private final String dataReserva;
    private final String nomeCliente;
    private final String numeroWhatsapp;

    public EventoReservaConfirmada(Empresa empresa, String dataReserva, String nomeCliente, String numeroWhatsapp) {
        this.empresa = empresa;
        this.dataReserva = dataReserva;
        this.nomeCliente = nomeCliente;
        this.numeroWhatsapp = numeroWhatsapp;
    }

    // Apenas Getters
    public Empresa getEmpresa() { return empresa; }
    public String getDataReserva() { return dataReserva; }
    public String getNomeCliente() { return nomeCliente; }
    public String getNumeroWhatsapp() { return numeroWhatsapp; }
}
