package com.example.demo.servico.observador;

import org.springframework.stereotype.Component;

@Component
public class Observadorwhatsapp {

    public void logSucesso(String sessao, String jid, String msg) {
        System.out.println(String.format("✅ [%s] Mensagem de %s: %s", sessao, jid, msg));
    }

    public void logFiltro(String sessao, String jid, String motivo) {
        // Isso aqui é ouro para o seu suporte:
        System.out.println(String.format("🛑 [%s] IGNORADA -> JID: %s | Motivo: %s", sessao, jid, motivo));
    }

    public void logErro(String sessao, String jid, String local, Exception e) {
        System.err.println(String.format("⚠️ [%s] ERRO em %s para %s: %s", sessao, local, jid, e.getMessage()));
    }
}
