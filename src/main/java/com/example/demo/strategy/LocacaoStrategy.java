package com.example.demo.strategy;

import com.example.demo.servico.ServicoReserva;
import com.example.demo.model.Empresa;
import com.example.demo.servico.GerenciadorSessao;
import com.example.demo.servico.ServicoMensagem;
import com.example.demo.servico.ServicoMenu;
import com.example.demo.servico.ServicoIA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocacaoStrategy implements ModuloAtendimentoStrategy {

    @Autowired private ServicoMensagem servicoMensagem;
    @Autowired private GerenciadorSessao gerenciadorSessao;
    @Autowired private ServicoReserva servicoReserva;
    @Autowired private ServicoMenu servicoMenu;

    // Injetando a Inteligência Artificial
    @Autowired private ServicoIA servicoIA;

    @Override
    public String getRamoDeAtuacao() {
        return "LOCACAO";
    }

    @Override
    public void processarMensagem(Empresa empresa, String numeroCliente, String textoRecebido, String estadoAtual) {

        String textoNormalizado = textoRecebido.trim().toLowerCase();

        // Lógica inicial para quem acabou de chegar ou está no menu principal
        if (estadoAtual.equals("INICIO") || estadoAtual.equals("MENU_ENVIADO")) {

            // Tenta descobrir se o cliente digitou um número do menu (ex: 1, 2, 3)
            String acao = servicoMenu.descobrirAcao(empresa, textoNormalizado);

            // ==========================================
            // 🤖 O CRUZAMENTO: MENU TRADICIONAL VS IA
            // ==========================================
            // Se o cliente digitou um texto solto (ex: "tem vaga amanhã?"), o menu não entende, e cai aqui:
            if (acao.equals("OPCAO_INVALIDA")) {

                // Verifica se a empresa contratou o módulo de IA
                // (Nota: Confirme se você tem esse 'getUsaIA()' na sua classe Empresa)
                if (Boolean.TRUE.equals(empresa.getUsaIA())) {

                    // 1. MONTANDO O PROMPT: Damos a personalidade e o texto do cliente para a IA
                    String prompt = "Você é o assistente virtual da empresa " + empresa.getNome() + ". " +
                            "Seu objetivo é ser educado, prestativo e direto ao ponto. " +
                            "O cliente enviou a seguinte mensagem: '" + textoRecebido + "'. " +
                            "Responda ao cliente de forma amigável e natural.";

                    // 2. ENVIAMOS PARA A NOSSA PONTE
                    String respostaDaIA = servicoIA.gerarRespostaHumanizada(prompt);

                    // 3. DEVOLVEMOS A RESPOSTA PRO WHATSAPP
                    servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, respostaDaIA);

                } else {
                    // Fluxo sem IA: Retorna o Menu "engessado"
                    servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, servicoMenu.montarMenuPrincipal(empresa));
                    gerenciadorSessao.atualizarEstado(numeroCliente, "MENU_ENVIADO");
                }

                // Retorna para interromper a execução, já que o cliente já foi respondido
                return;
            }

            // ==========================================
            // ⚙️ FLUXO NORMAL DO MENU (Opções engessadas)
            // ==========================================
            switch (acao) {
                case "VER_AGENDA_30_DIAS":
                    // Exemplo: fluxoAgenda.listar30Dias(...);
                    servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, "Buscando nossa agenda dos próximos 30 dias...");
                    break;
                case "PESQUISAR_DATA":
                    // Exemplo: fluxoAgenda.iniciarPesquisa(...);
                    servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, "Por favor, me diga qual data você deseja verificar.");
                    break;
                case "VER_FOTOS":
                    // Exemplo: fluxoInstitucional.enviarGaleria(...);
                    servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, "Preparando nossa galeria de fotos para você...");
                    break;
                case "VER_VALORES":
                    // Exemplo: fluxoInstitucional.enviarPrecos(...);
                    servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, "Consultando nossa tabela de preços...");
                    break;
                case "VER_LOCALIZACAO":
                    // Exemplo: fluxoInstitucional.enviarLocalizacao(...);
                    servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, "Vou te enviar a nossa localização no mapa.");
                    break;
                default:
                    // Fallback de segurança para o menu
                    servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, "Opção não reconhecida.");
                    break;
            }
        }
    }
}