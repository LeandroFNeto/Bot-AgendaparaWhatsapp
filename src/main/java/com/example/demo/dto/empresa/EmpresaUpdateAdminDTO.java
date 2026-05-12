package com.example.demo.dto.empresa;


import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Contrato Master: Dados críticos de cobrança e infraestrutura que só o Admin do SaaS altera.")
public record EmpresaUpdateAdminDTO(

        @Schema(description = "ID da sessão que conecta com o WPPConnect", example = "sessao_recanto_01")
        String sessaoWhatsapp,

        @Schema(description = "Define se o local é alugado por hora (true) ou diária (false)", example = "false")
        Boolean locacaoPorHora,

        @Schema(description = "Lista de módulos liberados após pagamento", example = "[\"IA_GEMINI\", \"GOOGLE_CALENDAR\"]")
        List<String> modulosAtivos
) {}