package com.example.demo.util;

import com.example.demo.dto.WhatsappWebhookDTO;

public class WebhookHandler {

    private final WhatsappWebhookDTO payload;

    public WebhookHandler(WhatsappWebhookDTO payload) {
        this.payload = payload;
    }

    public String getRemoteJid() {
        return payload.resolverRemoteJid();
    }

    public String getTextoMensagem() {
        return payload.resolverTexto();
    }

    public boolean isFromMe() {
        return payload.enviadaPorMim();
    }
}
