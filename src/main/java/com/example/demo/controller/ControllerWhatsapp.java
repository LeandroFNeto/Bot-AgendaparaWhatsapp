package com.example.demo.controller;

import com.example.demo.dto.WhatsappWebhookDTO;
import com.example.demo.model.Empresa;
import com.example.demo.repository.EmpresaRepository;
import com.example.demo.servico.GerenciadorSessao;
import com.example.demo.servico.observador.Observadorwhatsapp;
import com.example.demo.strategy.FactoryModulo;
import com.example.demo.strategy.ModuloAtendimentoStrategy;
import com.example.demo.util.WhatsappUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@Tag(name = "Webhook WhatsApp", description = "Recepção tipada de eventos do WPPConnect")
public class ControllerWhatsapp {

    @Autowired private GerenciadorSessao gerenciadorSessao;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private FactoryModulo moduloFactory;
    @Autowired private Observadorwhatsapp observador;

    @PostMapping("/whatsapp")
    @Operation(
            summary = "Receber mensagem do WhatsApp",
            description = "Consome WhatsappWebhookDTO (sem Map genérico). Aplica travas Anti-X9, ignora grupos/status e encaminha o texto ao módulo da empresa."
    )
    @ApiResponse(responseCode = "200", description = "Evento aceito. Mensagens filtradas também retornam 200 para o WPPConnect não reenviar.",
            content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Void> receberMensagem(@RequestBody WhatsappWebhookDTO payload) {

        String sessao = payload.session();
        String tipoMensagem = payload.type();
        String rawFrom = payload.resolverRemoteJid();
        String textoRecebido = payload.resolverTexto();

        try {
            if (payload.enviadaPorMim()) {
                observador.logFiltro(sessao, rawFrom, "Dono do celular digitou (Anti-X9)");
                return ResponseEntity.ok().build();
            }

            if (rawFrom == null || rawFrom.contains("@g.us") || rawFrom.contains("status") ||
                    rawFrom.contains("@lid") || !"chat".equals(tipoMensagem)) {
                observador.logFiltro(sessao, rawFrom, "Mensagem ignorada (Grupo, Status ou Tipo Inválido)");
                return ResponseEntity.ok().build();
            }

            String chaveEstado = WhatsappUtil.normalizarParaChaveEstado(rawFrom);
            Empresa empresa = empresaRepository.findBySessaoWhatsapp(sessao);

            if (empresa == null) {
                observador.logFiltro(sessao, rawFrom, "Empresa não encontrada para esta sessão");
                return ResponseEntity.ok().build();
            }

            String estadoAtual = gerenciadorSessao.obterEstadoAtual(chaveEstado);

            ModuloAtendimentoStrategy estrategia = moduloFactory.obterEstrategia(empresa.getRamoDeAtuacao());
            estrategia.processarMensagem(empresa, rawFrom, textoRecebido, estadoAtual);

            observador.logSucesso(sessao, rawFrom, textoRecebido);

        } catch (Exception e) {
            observador.logErro(sessao, rawFrom, "ControllerWhatsapp", e);
        }
        return ResponseEntity.ok().build();
    }
}
