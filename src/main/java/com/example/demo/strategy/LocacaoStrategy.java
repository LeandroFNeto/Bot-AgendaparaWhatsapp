package com.example.demo.strategy; // Confirme se este é o seu pacote

import com.example.demo.servico.ServicoMenu;
import com.example.demo.servico.ServicoReserva;
import com.example.demo.dto.DiaReservaDTO;
import com.example.demo.model.DiaReserva;
import com.example.demo.model.Empresa;
import com.example.demo.model.HorarioReserva;
import com.example.demo.servico.GerenciadorSessao;
import com.example.demo.servico.ServicoMensagem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.example.demo.evento.EventoReservaConfirmada;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class LocacaoStrategy implements ModuloAtendimentoStrategy {

    // --- DEPENDÊNCIAS INJETADAS ---
    @Autowired
    private ServicoMensagem servicoMensagem;

    @Autowired
    private GerenciadorSessao gerenciadorSessao;

    @Autowired
    private ServicoReserva servicoReserva;

    @Autowired
    private ApplicationEventPublisher publicadorDeEventos;

    @Autowired
    private ServicoMenu servicoMenu;

    @Override
    public String getRamoDeAtuacao() {
        return "LOCACAO"; // Exatamente como está escrito no banco de dados
    }

    @Override
    public void processarMensagem(Empresa empresa, String numeroCliente, String textoRecebido, String estadoAtual) {

        String textoNormalizado = textoRecebido.trim().toLowerCase();

        // Se não é um fluxo de reserva/agenda rodando, e o cliente enviou qualquer coisa solta:
        if (estadoAtual.equals("INICIO") || estadoAtual.equals("MENU_ENVIADO")) {

            String acao = servicoMenu.descobrirAcao(empresa, textoNormalizado);

            // Se a ação retornou OPCAO_INVALIDA (ou seja, ele não digitou 1, 2, 3...),
            // significa que ele disse "bom dia", "oi", "quero alugar", ou errou o número.
            if (acao.equals("OPCAO_INVALIDA")) {
                servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, servicoMenu.montarMenuPrincipal(empresa));
                gerenciadorSessao.atualizarEstado(numeroCliente, "MENU_ENVIADO");
                return;
            }

            // SE ELE DIGITOU UM NÚMERO, DESCOBRIMOS QUAL É A AÇÃO DELE BASEADO NA EMPRESA
            if (estadoAtual.equals("MENU_ENVIADO")) {

                String acaoDesejada = servicoMenu.descobrirAcao(empresa, textoNormalizado);

                switch (acaoDesejada) {
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
                    case "OPCAO_INVALIDA":
                    default:
                        servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, "❌ Opção inválida. " + servicoMenu.montarMenuPrincipal(empresa));
                        break;
                }
            }
        }
    }
}