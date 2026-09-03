package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mensagem de WhatsApp enviada ou recebida pelo bot")
public record MensagemWhatsappDTO(
        @Schema(description = "Número do contato no formato internacional, sem máscara", example = "5511999999999")
        String numero,

        @Schema(description = "Nome de exibição do contato", example = "Maria Silva")
        String nome,

        @Schema(description = "Texto da mensagem", example = "Olá! Gostaria de reservar o espaço para sábado.")
        String texto
) {
}
