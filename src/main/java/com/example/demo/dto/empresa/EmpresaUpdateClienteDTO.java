package com.example.demo.dto.empresa;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Contrato restrito: Dados que o cliente (dono do estabelecimento) pode alterar no seu próprio painel.")
public record EmpresaUpdateClienteDTO(

        @Schema(description = "Nome fantasia", example = "Recanto Vista Alegre")
        String nome,

        @Schema(description = "Mensagem que o bot envia ao iniciar conversa", example = "Olá! Como posso ajudar?")
        String mensagemSaudacao,

        @Schema(description = "Preços em formato texto para o bot ler", example = "Diária padrão: R$ 500,00")
        String tabelaDePrecos,

        @Schema(description = "Link do Google Maps", example = "https://maps.app.goo.gl/...")
        String linkGoogleMaps,

        @Schema(description = "URL da foto principal do painel", example = "https://meusite.com/foto.jpg")
        String linkFotoPrincipal,

        @Schema(description = "Link do Instagram ou portfólio", example = "https://instagram.com/recanto")
        String linkGaleria
) {}
