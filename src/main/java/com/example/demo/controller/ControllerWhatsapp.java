package com.example.demo.controller;

import com.example.demo.model.Empresa;
import com.example.demo.repository.EmpresaRepository;
import com.example.demo.servico.GerenciadorSessao;
import com.example.demo.servico.observador.Observadorwhatsapp;
import com.example.demo.strategy.FactoryModulo;
import com.example.demo.servico.ServicoWppConnect;
import com.example.demo.strategy.ModuloAtendimentoStrategy;
import com.example.demo.util.WhatsappUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class ControllerWhatsapp {

    @Autowired private GerenciadorSessao gerenciadorSessao;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private FactoryModulo moduloFactory;
    @Autowired private Observadorwhatsapp observador;

    @PostMapping("/whatsapp")
    public ResponseEntity<Void> receberMensagem(@RequestBody Map<String, Object> payload) {
        String sessao = (String) payload.get("session");
        String rawFrom = (String) payload.get("from");
        String tipoMensagem = (String) payload.get("type");
        String textoRecebido = (String) payload.get("body");

        try {
            // 🛑 TRAVA ANTI-X9 (Recuperada e Protegida)
            boolean enviadaPorMim = false;
            if (payload.containsKey("fromMe")) {
                enviadaPorMim = (Boolean) payload.get("fromMe");
            } else if (payload.containsKey("id") && payload.get("id") instanceof Map) {
                Map<String, Object> idObj = (Map<String, Object>) payload.get("id");
                enviadaPorMim = Boolean.TRUE.equals(idObj.get("fromMe"));
            }

            if (enviadaPorMim) {
                observador.logFiltro(sessao, rawFrom, "Dono do celular digitou (Anti-X9)");
                return ResponseEntity.ok().build();
            }

            // 🛑 TRAVAS DE SEGURANÇA GERAIS
            if (rawFrom == null || rawFrom.contains("@g.us") || rawFrom.contains("status") ||
                    rawFrom.contains("@lid") || !"chat".equals(tipoMensagem)) {
                observador.logFiltro(sessao, rawFrom, "Mensagem ignorada (Grupo, Status ou Tipo Inválido)");
                return ResponseEntity.ok().build();
            }

            // 🔍 NORMALIZAÇÃO E ESTADO
            String chaveEstado = WhatsappUtil.normalizarParaChaveEstado(rawFrom);
            Empresa empresa = empresaRepository.findBySessaoWhatsapp(sessao);

            if (empresa == null) {
                observador.logFiltro(sessao, rawFrom, "Empresa não encontrada para esta sessão");
                return ResponseEntity.ok().build();
            }

            String estadoAtual = gerenciadorSessao.obterEstadoAtual(chaveEstado);

            // 🔀 PROCESSAMENTO
            ModuloAtendimentoStrategy estrategia = moduloFactory.obterEstrategia(empresa.getRamoDeAtuacao());
            estrategia.processarMensagem(empresa, rawFrom, textoRecebido, estadoAtual);

            observador.logSucesso(sessao, rawFrom, textoRecebido);

        } catch (Exception e) {
            observador.logErro(sessao, rawFrom, "ControllerWhatsapp", e);
        }
        return ResponseEntity.ok().build();
    }
}