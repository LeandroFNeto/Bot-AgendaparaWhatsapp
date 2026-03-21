package com.example.demo.util;

import java.util.Map;

public class WebhookHandler {

    private final Map<String, Object> payload;

    public WebhookHandler(Map<String, Object> payload) {
        this.payload = payload;
    }

    // Método exclusivo para pegar o número correto (JID)
    public String getRemoteJid() {
        try {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> key = (Map<String, Object>) data.get("key");
            String remoteJid = (String) key.get("remoteJid");

            if (remoteJid != null) {
                // Se for @lid, tentamos limpar ou buscar o sender como backup
                if (remoteJid.contains("@lid")) {
                    String sender = (String) payload.get("sender");
                    return (sender != null) ? sender : remoteJid.split("@")[0] + "@s.whatsapp.net";
                }
                return remoteJid;
            }
        } catch (Exception e) {
            System.out.println("Erro ao extrair JID: " + e.getMessage());
        }
        return null;
    }

    // Método exclusivo para pegar o texto
    public String getTextoMensagem() {
        try {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> message = (Map<String, Object>) data.get("message");
            return (String) message.get("conversation");
        } catch (Exception e) {
            return null;
        }
    }
}
