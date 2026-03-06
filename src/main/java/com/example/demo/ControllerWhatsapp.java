package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class ControllerWhatsapp {

    @Autowired
    private ControllerReserva controllerReserva;

    @Autowired
    private WppConnectService wppConnectService;

    @Autowired
    private GerenciadorSessao gerenciadorSessao;

    @PostMapping("/whatsapp")
    public ResponseEntity<Void> receberMensagem(@RequestBody Map<String, Object> payload) {
        try {
            String numeroCliente = (String) payload.get("from");
            String textoRecebido = (String) payload.get("body");
            String tipoMensagem = (String) payload.get("type");

            // 🛑 TRAVAS DE SEGURANÇA
            if (numeroCliente == null || textoRecebido == null ||
                    numeroCliente.contains("@g.us") ||
                    numeroCliente.contains("status") ||
                    numeroCliente.contains("@lid") ||
                    !"chat".equals(tipoMensagem)) {
                return ResponseEntity.ok().build();
            }

            // 🧠 CONSULTA A SESSÃO (Ele já calcula os 10 minutos sozinho)
            String estadoAtual = gerenciadorSessao.obterEstadoAtual(numeroCliente);
            String textoNormalizado = textoRecebido.trim().toLowerCase();

            // 🔄 ROTA DE FUGA MANUAL
            if (textoNormalizado.equals("0") || textoNormalizado.equals("menu") || textoNormalizado.equals("oi") || textoNormalizado.equals("olá")) {
                enviarMensagemWPP(numeroCliente, montarMenuPrincipal());
                gerenciadorSessao.atualizarEstado(numeroCliente, "MENU_ENVIADO");

            } else if (textoRecebido.equals("1")) {
                List<DiaReservaDTO> lista = controllerReserva.listarReservas();
                enviarMensagemWPP(numeroCliente, formatarLista(lista));
                gerenciadorSessao.atualizarEstado(numeroCliente, "INICIO");

            } else if (textoRecebido.equals("2")) {
                String msgData = "📅 *Consulta de Data Específica*\n\n" +
                        "Por favor, digite a data exata que deseja consultar.\n\n" +
                        "👉 *Formato obrigatório:* DD-MM-AAAA\n" +
                        "💡 *Exemplo:* Se você quer o dia 25 de dezembro, digite: *25-12-2026*\n\n" +
                        "_(Ou digite *0* para cancelar e voltar)_";
                enviarMensagemWPP(numeroCliente, msgData);
                gerenciadorSessao.atualizarEstado(numeroCliente, "AGUARDANDO_DATA");

            } else if (estadoAtual.equals("AGUARDANDO_DATA")) {
                try {
                    DiaReservaDTO dia = controllerReserva.pesquisarData(textoRecebido);
                    enviarMensagemWPP(numeroCliente, formatarDia(dia));
                    gerenciadorSessao.atualizarEstado(numeroCliente, "INICIO");

                } catch (IllegalArgumentException e) {
                    // 🛑 O cliente caiu na sua trava de datas passadas!
                    enviarMensagemWPP(numeroCliente, "⏳ *" + e.getMessage() + "*\nPor favor, digite uma data de hoje em diante ou digite *0* para voltar ao menu.");

                } catch (Exception e) {
                    // ❌ O cliente digitou algo maluco ou formato errado
                    enviarMensagemWPP(numeroCliente, "❌ Formato inválido. Digite a data como *DD-MM-AAAA* ou digite *0* para voltar ao menu.");
                }

            } else {
                if (!estadoAtual.equals("MENU_ENVIADO")) {
                    enviarMensagemWPP(numeroCliente, montarMenuPrincipal());
                    gerenciadorSessao.atualizarEstado(numeroCliente, "MENU_ENVIADO");
                }
            }

        } catch (Exception e) {
            System.out.println("⚠️ Erro no fluxo: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    private String montarMenuPrincipal() {
        return "👋 Bem-vindo ao *Recanto Vista Alegre*!\n\n" +
                "Como posso ajudar hoje?\n" +
                "1️⃣ - Ver agenda dos próximos 30 dias\n" +
                "2️⃣ - Consultar uma data específica\n\n" +
                "Digite o número da opção desejada.";
    }

    private String formatarLista(List<DiaReservaDTO> lista) {
        if (lista == null || lista.isEmpty()) return "Nenhuma reserva encontrada.";
        StringBuilder sb = new StringBuilder("🗓️ *Disponibilidade (30 dias)*:\n\n");

        for (DiaReservaDTO dia : lista) {
            String icone = dia.status().equals("Disponível") ? "🟢" : "🔴";

            // Remove a palavra "-feira" (e "-Feira" por segurança) para ficar só "Segunda", "Terça", etc.
            String diaCurto = dia.diaDaSemana().replace("-feira", "").replace("-Feira", "");

            // Monta a linha: 🟢 25-12-2025 (Segunda) - Disponível
            sb.append(icone).append(" ").append(dia.data())
                    .append(" (").append(diaCurto).append(") - ")
                    .append(dia.status()).append("\n");
        }
        return sb.toString();
    }

    private String formatarDia(DiaReservaDTO dia) {
        String icone = dia.status().equals("Disponível") ? "🟢" : "🔴";

        // Remove o "-feira" igual fizemos no outro método
        String diaCurto = dia.diaDaSemana().replace("-feira", "").replace("-Feira", "");

        return return """
       🔍 *Resultado da busca*:
       
       %s *%s (%s)*
       📌 Status: *%s*
       
       ⚠️ *AVISO IMPORTANTE*
       • Horário de retirada da chave a partir das *08h às 20h* saída.
       • Aluguel somente para responsáveis *maiores de 18 anos*.
       
       Digite *0* para voltar ao Menu Principal.\
       """.formatted(icone, dia.data(), diaCurto, dia.status());
    }

    private void enviarMensagemWPP(String numero, String texto) {
        System.out.println("🚀 Enviando resposta para " + numero);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://wppconnect:21465/api/RecantoBot/send-message";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Usando o novo serviço para pegar o token
            headers.set("Authorization", "Bearer " + wppConnectService.obterToken());

            Map<String, Object> body = new HashMap<>();
            body.put("phone", numero);
            body.put("message", texto);
            body.put("isGroup", numero.contains("@g.us"));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);

        } catch (Exception e) {
            System.out.println("❌ Falha ao enviar mensagem para o WhatsApp: " + e.getMessage());
            if (e.getMessage().contains("401")) {
                // Se der erro de autorização, manda a classe limpar o token velho
                wppConnectService.invalidarToken();
            }
        }
    }
}