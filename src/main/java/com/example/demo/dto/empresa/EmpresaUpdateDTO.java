package com.example.demo.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contrato de atualização do painel do cliente (PUT). Não inclui campos de cobrança ou infraestrutura.")
public record EmpresaUpdateDTO(
        @Schema(description = "Nome fantasia", example = "Recanto Vista Alegre")
        String nome,

        @Schema(description = "Mensagem que o bot envia ao iniciar a conversa", example = "Olá! Como posso ajudar?")
        String mensagemSaudacao,

        @Schema(description = "Tabela de preços em texto para o bot ler", example = "Diária padrão: R$ 500,00")
        String tabelaDePrecos,

        @Schema(description = "Link do Google Maps do estabelecimento", example = "https://maps.app.goo.gl/exemplo")
        String linkGoogleMaps,

        @Schema(description = "URL da foto principal exibida no painel", example = "https://meusite.com/foto.jpg")
        String linkFotoPrincipal,

        @Schema(description = "Link da galeria, Instagram ou portfólio", example = "https://instagram.com/recanto")
        String linkGaleria
) {
}
