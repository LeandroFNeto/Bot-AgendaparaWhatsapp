package com.example.demo.strategy;

import com.example.demo.servico.ServicoReserva;
import com.example.demo.model.Empresa;
import com.example.demo.servico.GerenciadorSessao;
import com.example.demo.servico.ServicoMensagem;
import com.example.demo.servico.ServicoMenu;
import com.example.demo.servico.ServicoIA; // 👈 IMPORTANTE!
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocacaoStrategy implements ModuloAtendimentoStrategy {

    @Autowired private ServicoMensagem servicoMensagem;
    @Autowired private GerenciadorSessao gerenciadorSessao;
    @Autowired private ServicoReserva servicoReserva;
    @Autowired private ServicoMenu servicoMenu;

    @Autowired private ServicoIA servicoIA; // 👈 Injetando a Inteligência Artificial!

    @Override
    public String getRamoDeAtuacao() { return "LOCACAO"; }

    @Override
    public void processarMensagem(Empresa empresa, String numeroCliente, String textoRecebido, String estadoAtual) {

        String textoNormalizado = textoRecebido.trim().toLowerCase();

        if (estadoAtual.equals("INICIO") || estadoAtual.equals("MENU_ENVIADO")) {

            String acao = servicoMenu.descobrirAcao(empresa, textoNormalizado);

            // ⚠️ AQUI ACONTECE A MÁGICA DA IA vs MENU!
            if (acao.equals("OPCAO_INVALIDA")) {

                // Se a empresa pagou pelo módulo de IA (Assumindo que você criou a variável usaIA na Empresa)
                if (Boolean.TRUE.equals(empresa.getUsaIA())) {

                    // Manda o texto do cliente para o ChatGPT ler e responder!
                    String respostaDaIA = servicoIA.gerarRespostaInteligente(empresa, textoRecebido);
                    servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, respostaDaIA);

                } else {

                    // Se não tem IA, manda o Menu Tradicional "burro"
                    servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, servicoMenu.montarMenuPrincipal(empresa));
                    gerenciadorSessao.atualizarEstado(numeroCliente, "MENU_ENVIADO");
                }
                return;
            }

            // SE ELE DIGITOU UM NÚMERO VÁLIDO (Ex: 1, 2, 3), O FLUXO CONTINUA NORMAL, MESMO SE ELE TIVER IA
            switch (acao) {
                case "VER_AGENDA_30_DIAS":
                    // fluxoAgenda.listar30Dias(...);
                    break;
                case "PESQUISAR_DATA":
                    // fluxoAgenda.iniciarPesquisa(...);
                    break;
                case "VER_FOTOS":
                    // fluxoInstitucional.enviarGaleria(...);
                    break;
                case "VER_VALORES":
                    // fluxoInstitucional.enviarPrecos(...);
                    break;
                case "VER_LOCALIZACAO":
                    // fluxoInstitucional.enviarLocalizacao(...);
                    break;
            }
        }
    }
}