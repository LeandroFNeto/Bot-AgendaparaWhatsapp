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
            String textoRecebido = (String) payload.get("body");
            String tipoMensagem = (String) payload.get("type");
            String sessaoWhatsapp = (String) payload.get("session");

            Empresa empresa = empresaRepository.findBySessaoWhatsapp(sessaoWhatsapp);

            if (empresa == null) {
                System.out.println("❌ Cliente não encontrado no Banco: " + sessaoWhatsapp);
                return ResponseEntity.ok().build();
            }

            String numeroCliente = (String) payload.get("from");

            // 🛑 TRAVAS DE SEGURANÇA
            if (numeroCliente == null || textoRecebido == null ||
                    numeroCliente.contains("@g.us") ||
                    numeroCliente.contains("status") ||
                    numeroCliente.contains("@lid") ||
                    !"chat".equals(tipoMensagem)) {
                return ResponseEntity.ok().build();
            }

            // 🧠 CONSULTA A SESSÃO
            String estadoAtual = gerenciadorSessao.obterEstadoAtual(numeroCliente);

            // 🔀 O ROTEADOR MÁGICO DO SaaS!
            if ("LOCACAO".equals(empresa.getRamoDeAtuacao())) {
                System.out.println("🔀 Redirecionando para o Módulo de Locação...");

                // Passamos a bola (o bastão) para o especialista fazer o trabalho dele!
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