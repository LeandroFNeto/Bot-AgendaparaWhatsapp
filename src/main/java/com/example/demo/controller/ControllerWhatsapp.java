package com.example.demo;

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
    private WppConnectService wppConnectService;

    @Autowired
    private GerenciadorSessao gerenciadorSessao;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ServicoLocacao servicoLocacao; // 🔥 O Especialista!

    @PostMapping("/whatsapp")
    public ResponseEntity<Void> receberMensagem(@RequestBody Map<String, Object> payload) {
        try {
            // 🕵️‍♂️ X9 LOG 1: Mostra tudo que chegou do WhatsApp.
            // Isso vai te ajudar a ver o formato real do JSON no console.
            // System.out.println("🕵️‍♂️ [X9] Payload completo recebido: " + payload);
            // (Descomente a linha acima se quiser ver o JSON inteiro, mas é bem grande!)

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

            // 🧠 CONSULTA A SESSÃO
            String estadoAtual = gerenciadorSessao.obterEstadoAtual(numeroCliente);

            // 🔀 O ROTEADOR MÁGICO DO SaaS!
            if ("LOCACAO".equals(empresa.getRamoDeAtuacao())) {
                System.out.println("🔀 [X9] Cliente aprovado! Redirecionando para o Módulo de Locação...");
                servicoLocacao.processarMensagem(empresa, numeroCliente, textoRecebido, estadoAtual);
            }
            else if ("BARBEARIA".equals(empresa.getRamoDeAtuacao())) {
                System.out.println("🔀 Módulo de Barbearia ainda em construção...");
            }
            else {
                System.out.println("⚠️ Ramo de atuação desconhecido: " + empresa.getRamoDeAtuacao());
            }

        } catch (Exception e) {
            System.out.println("⚠️ Erro no fluxo: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}