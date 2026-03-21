package com.example.demo.controller;

import com.example.demo.model.Empresa;
import com.example.demo.repository.EmpresaRepository;
import com.example.demo.servico.GerenciadorSessao;
import com.example.demo.strategy.FactoryModulo;
import com.example.demo.servico.ServicoWppConnect;
import com.example.demo.strategy.ModuloAtendimentoStrategy;
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

    // A Rececionista só precisa de 4 coisas agora:
    @Autowired
    private ServicoWppConnect wppConnectService;

    @Autowired
    private GerenciadorSessao gerenciadorSessao;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private FactoryModulo moduloFactory; // 🔥 Trocamos o ServicoLocacao pela FACTORY!

    @PostMapping("/whatsapp")
    public ResponseEntity<Void> receberMensagem(@RequestBody Map<String, Object> payload) {
        try {

            String textoRecebido = (String) payload.get("body");
            String tipoMensagem = (String) payload.get("type");
            String sessaoWhatsapp = (String) payload.get("session");
            String numeroCliente = (String) payload.get("from");

            Empresa empresa = empresaRepository.findBySessaoWhatsapp(sessaoWhatsapp);

            if (empresa == null) {
                System.out.println("❌ Cliente não encontrado no Banco: " + sessaoWhatsapp);
                return ResponseEntity.ok().build();
            }

            // 🛑 LÓGICA ANTI-X9 (A Mágica para não responder o dono do celular)
            Boolean enviadaPorMim = false;

            // O WPPConnect pode mandar o "fromMe" solto na raiz do JSON...
            if (payload.containsKey("fromMe")) {
                enviadaPorMim = (Boolean) payload.get("fromMe");
            }
            // ...ou pode mandar escondido dentro do objeto "id"
            else if (payload.containsKey("id")) {
                Map<String, Object> idObj = (Map<String, Object>) payload.get("id");
                if (idObj != null && idObj.containsKey("fromMe")) {
                    enviadaPorMim = (Boolean) idObj.get("fromMe");
                }
            }

            // 🕵️‍♂️ X9 LOG 2: Revela quem é o autor da mensagem
            System.out.println("🕵️‍♂️ [X9] A mensagem '" + textoRecebido + "' foi enviada pelo dono do celular? -> " + enviadaPorMim);

            // Se for true, a gente mata o processo aqui mesmo com um return.
            if (Boolean.TRUE.equals(enviadaPorMim)) {
                System.out.println("🤫 [X9] O dono do celular digitou. Cruzando os braços, não vou responder!");
                return ResponseEntity.ok().build();
            }

            // 🛑 TRAVAS DE SEGURANÇA GERAIS (Para clientes)
            if (numeroCliente == null || textoRecebido == null ||
                    numeroCliente.contains("@g.us") || // Bloqueia mensagens de grupos
                    numeroCliente.contains("status") || // Bloqueia respostas a status
                    numeroCliente.contains("@lid") ||
                    !"chat".equals(tipoMensagem)) {
                System.out.println("🛑 [X9] Mensagem ignorada pelas travas de segurança (Grupo, Status, etc).");
                return ResponseEntity.ok().build();
            }

            String estadoAtual = gerenciadorSessao.obterEstadoAtual(numeroCliente);

            // 🔀 O ROTEADOR MÁGICO DO SaaS!
            try {
                // 1. Pede para a fábrica a estratégia correta baseada no Ramo
                ModuloAtendimentoStrategy estrategia = moduloFactory.obterEstrategia(empresa.getRamoDeAtuacao());

                // 2. Manda processar sem se importar se é Locação, Barbearia ou Padaria!
                estrategia.processarMensagem(empresa, numeroCliente, textoRecebido, estadoAtual);

            } catch (IllegalArgumentException e) {
                System.out.println("⚠️ " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erro no fluxo: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}