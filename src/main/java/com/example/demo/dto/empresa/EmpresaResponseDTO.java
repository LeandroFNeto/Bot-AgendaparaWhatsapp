package com.example.demo.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record EmpresaResponseDTO(
        @Schema(description = "ID único", example = "1")
        Long id,

        @Schema(description = "Nome fantasia", example = "Recanto Vista Alegre")
        String nome,

        @Schema(description = "ID da sessão no WPPConnect (Vital para o Front)", example = "sessao_recanto_01")
        String sessaoWhatsapp,

        @Schema(description = "Status atual da conexão", example = "CONNECTED")
        String statusSessao,

        @Schema(description = "Link configurado para o Google Maps")
        String linkGoogleMaps,

        @Schema(description = "Data da última modificação")
        LocalDateTime atualizadoEm
) {}