package com.example.demo.strategy;

import com.example.demo.model.Empresa;

public interface ModuloAtendimentoStrategy {

    // Todo módulo terá que saber processar uma mensagem
    void processarMensagem(Empresa empresa, String numeroCliente, String textoRecebido, String estadoAtual);

    // Todo módulo terá que dizer de qual ramo ele é (Ex: "LOCACAO")
    String getRamoDeAtuacao();
}
