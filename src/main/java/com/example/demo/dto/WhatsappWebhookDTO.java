package com.example.demo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Payload recebido do WPPConnect via Webhook")
public record WhatsappWebhookDTO(
        @Schema(example = "sessao_cliente_01")
        String session,

        @Schema(example = "chat", description = "Tipo da mensagem (chat, image, etc)")
        String type,

        @Schema(description = "Indica se a mensagem foi enviada pelo dono do bot", example = "false")
        boolean fromMe,

        @Schema(description = "Conteúdo completo da mensagem", example = "{\"body\": \"Olá, gostaria de reservar\"}")
        Map<String, Object> content,

        @Schema(description = "Dados do remetente", example = "5511999999999@c.us")
        String remoteJid
) {}