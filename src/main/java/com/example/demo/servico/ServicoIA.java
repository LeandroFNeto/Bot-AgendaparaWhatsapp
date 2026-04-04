package com.example.demo.servico;

import com.example.demo.model.Empresa;

public class ServicoIA {
    // No futuro, você vai injetar aqui o RestTemplate para chamar a API do ChatGPT/Gemini

    public String gerarRespostaInteligente(Empresa empresa, String textoCliente) {

        System.out.println("🤖 [IA] Processando a dúvida do cliente: " + textoCliente);

        // AQUI ENTRARÁ A COMUNICAÇÃO REAL COM A IA.
        // Por enquanto, vamos retornar uma resposta simulada para você testar o fluxo:

        return "🤖 *Assistente Virtual do " + empresa.getNome() + "*\n\n" +
                "Eu entendi que você está perguntando sobre: _\"" + textoCliente + "\"_.\n" +
                "Ainda estou em fase de treinamento, mas em breve poderei tirar todas as suas dúvidas automaticamente!";
    }
}
