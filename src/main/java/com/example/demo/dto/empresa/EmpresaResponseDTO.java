package com.example.demo.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Contrato de saída da API de empresas. Não expõe a entidade JPA nem campos internos de infraestrutura.")
public record EmpresaResponseDTO(
        @Schema(description = "Identificador único da empresa", example = "1")
        Long id,

        @Schema(description = "Nome fantasia", example = "Recanto Vista Alegre")
        String nome,

        @Schema(description = "Identificador da sessão no WPPConnect, usado pelo front-end para acompanhar a conexão", example = "sessao_recanto_01")
        String sessaoWhatsapp,

        @Schema(description = "Status atual da sessão WhatsApp", example = "DISCONNECTED")
        String statusSessao,

        @Schema(description = "Link configurado para o Google Maps", example = "https://maps.app.goo.gl/exemplo")
        String linkGoogleMaps,

        @Schema(description = "Data e hora da última modificação retornada pela API", example = "2026-09-03T14:30:00")
        LocalDateTime atualizadoEm
) {
}
