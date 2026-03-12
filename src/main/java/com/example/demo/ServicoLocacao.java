package com.example.demo; // Confirme se este é o seu pacote

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServicoLocacao {

    // --- DEPENDÊNCIAS INJETADAS ---
    @Autowired
    private ServicoMensagem servicoMensagem;

    @Autowired
    private GerenciadorSessao gerenciadorSessao;

    @Autowired
    private ControllerReserva controllerReserva;


    // --- O CÉREBRO PRINCIPAL ---
    public void processarMensagem(Empresa empresa, String numeroCliente, String textoRecebido, String estadoAtual) {

        String textoNormalizado = textoRecebido.trim().toLowerCase();

        if (textoNormalizado.equals("0") || textoNormalizado.equals("menu") || textoNormalizado.equals("oi") || textoNormalizado.equals("olá")) {
            servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, montarMenuPrincipal(empresa));
            gerenciadorSessao.atualizarEstado(numeroCliente, "MENU_ENVIADO");

        } else if (textoRecebido.equals("1")) {
            List<DiaReservaDTO> lista = controllerReserva.listarReservas(empresa);
            servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, formatarLista(lista));
            gerenciadorSessao.atualizarEstado(numeroCliente, "INICIO");

        } else if (textoRecebido.equals("2")) {
            String msgData = """
                    📅 *Consulta de Data Específica*
                    
                    Por favor, digite a data exata que deseja consultar.
                    
                    👉 *Formato obrigatório:* DD-MM-AAAA
                    💡 *Exemplo:* Se você quer o dia 25 de dezembro, digite: *25-12-2026*
                    
                    _(Ou digite *0* para cancelar e voltar)_
                    """;
            servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, msgData);
            gerenciadorSessao.atualizarEstado(numeroCliente, "AGUARDANDO_DATA");

        } else if (textoRecebido.equals("3")) {

            // AÇÃO 1: Manda a foto destaque direta (Isca)
            if (empresa.getLinkFotoPrincipal() != null && !empresa.getLinkFotoPrincipal().isEmpty()) {
                servicoMensagem.enviarImagemWPP(numeroCliente, empresa.getSessaoWhatsapp(), empresa.getLinkFotoPrincipal(), "destaque.jpg", "📸 Um pedacinho do nosso espaço para você!");
            }

            // AÇÃO 2: Puxa o link da pasta do Drive (Galeria) do banco de dados
            String linkDoAlbum = (empresa.getLinkGaleria() != null && !empresa.getLinkGaleria().isEmpty())
                    ? empresa.getLinkGaleria()
                    : "⏳ (Galeria de fotos em atualização. Fale com um atendente!)";

            String msgGaleria = """
                Quer ver mais detalhes? 😍
                
                Temos uma galeria completa com fotos de todos os nossos ambientes.
                
                👉 *Clique no link abaixo para ver todas as fotos:*
                %s
                
                Digite *0* para voltar ao Menu Principal.
                """.formatted(linkDoAlbum);

            servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, msgGaleria);
            gerenciadorSessao.atualizarEstado(numeroCliente, "INICIO");

        } else if (textoRecebido.equals("4")) {
            // Opção 4: Tabela de Preços dinâmica do Banco de Dados
            servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, formatarTabelaPrecos(empresa));
            gerenciadorSessao.atualizarEstado(numeroCliente, "INICIO");

        }else if (textoRecebido.equals("5")) {

            // Puxa o link do banco. Se a empresa esquecer de cadastrar, manda um aviso elegante.
            String linkMap = (empresa.getLinkGoogleMaps() != null && !empresa.getLinkGoogleMaps().isEmpty())
                    ? empresa.getLinkGoogleMaps()
                    : "⏳ (Link do mapa indisponível no momento. Peça ao atendente!)";

            String msgLocalizacao = """
                📍 *Como Chegar ao %s*
                
                Estamos localizados a aproximadamente 1.5h de Londrina. O acesso é super tranquilo!
                
                Clique no link abaixo para abrir a rota direto no seu GPS:
                👉 %s
                
                Digite *0* para voltar ao Menu Principal.
                """.formatted(empresa.getNome(), linkMap);

            servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, msgLocalizacao);
            gerenciadorSessao.atualizarEstado(numeroCliente, "INICIO");
        }else if (estadoAtual.equals("AGUARDANDO_DATA")) {
            try {
                DiaReservaDTO dia = controllerReserva.pesquisarData(empresa,textoRecebido);
                servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, formatarDia(dia));
                gerenciadorSessao.atualizarEstado(numeroCliente, "INICIO");

            } catch (IllegalArgumentException e) {
                servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, "⏳ *" + e.getMessage() + "*\nPor favor, digite uma data válida ou digite *0* para voltar ao menu.");

            } catch (Exception e) {
                servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, "❌ Formato inválido. Digite a data como *DD-MM-AAAA* ou digite *0* para voltar ao menu.");
            }

        } else {
            if (!estadoAtual.equals("MENU_ENVIADO")) {
                servicoMensagem.enviarMensagemWPP(empresa, numeroCliente, montarMenuPrincipal(empresa));
                gerenciadorSessao.atualizarEstado(numeroCliente, "MENU_ENVIADO");
            }
        }
    }


    // --- MÉTODOS DE FORMATAÇÃO (O PADRÃO OURO) ---

    private String montarMenuPrincipal(Empresa empresa) {
        return """
           %s
           
           1️⃣ - Ver agenda dos próximos 30 dias
           2️⃣ - Consultar uma data específica
           3️⃣ - 📸 Ver fotos do espaço (Piscina)
           4️⃣ - 💰 Valores 
           5️⃣ - 📍 Localização e Como Chegar
           
           Digite o número da opção desejada.\
           """.formatted(empresa.getMensagemSaudacao());
    }

    private String formatarLista(List<DiaReservaDTO> lista) {
        if (lista == null || lista.isEmpty()) return "Nenhuma reserva encontrada.";
        StringBuilder sb = new StringBuilder("🗓️ *Disponibilidade (30 dias)*:\n\n");
        for (DiaReservaDTO dia : lista) {
            String icone = dia.status().equals("Disponível") ? "🟢" : "🔴";
            String diaCurto = dia.diaDaSemana().replace("-feira", "").replace("-Feira", "");
            sb.append(icone).append(" ").append(dia.data())
                    .append(" (").append(diaCurto).append(") - ")
                    .append(dia.status()).append("\n");
        }
        return sb.toString();
    }

    private String formatarDia(DiaReservaDTO dia) {
        String icone = dia.status().equals("Disponível") ? "🟢" : "🔴";
        String diaCurto = dia.diaDaSemana().replace("-feira", "").replace("-Feira", "");
        return """
               🔍 *Resultado da busca*:
               
               %s *%s (%s)*
               📌 Status: *%s*
               
               ⚠️ *AVISO IMPORTANTE*
               • Horário de retirada da chave a partir das *08h às 20h* saída.
               • Aluguel somente para responsáveis *maiores de 18 anos*.
               
               Digite *0* para voltar ao Menu Principal.\
               """.formatted(icone, dia.data(), diaCurto, dia.status());
    }

    private String formatarTabelaPrecos(Empresa empresa) {
        // 1. Criamos um construtor de texto
        StringBuilder sb = new StringBuilder();
        sb.append("💰 *Valores e Disponibilidade - ").append(empresa.getNome()).append("*\n\n");

        // 2. Verificamos se a empresa tem dias cadastrados
        // Importante: Certifique-se que na sua classe Empresa existe o List<DiaReserva> diasDeReserva;
        if (empresa.getDiasDeReserva() != null && !empresa.getDiasDeReserva().isEmpty()) {

            for (DiaReserva dia : empresa.getDiasDeReserva()) {
                sb.append("📅 *Data: ").append(dia.getData()).append("*\n");

                // 3. Percorremos os horários de cada dia
                if (dia.getHorariosDisponiveis() != null && !dia.getHorariosDisponiveis().isEmpty()) {
                    for (HorarioReserva horario : dia.getHorariosDisponiveis()) {
                        // Define o ícone baseado no status
                        String iconeStatus = "Disponível".equalsIgnoreCase(horario.getStatus()) ? "✅" : "❌";

                        sb.append(iconeStatus).append(" ")
                                .append(horario.getDescricao()).append(" - ")
                                .append("*").append(horario.getValor()).append("*\n");
                    }
                } else {
                    sb.append("   (Sem horários definidos para este dia)\n");
                }
                sb.append("\n"); // Pula linha entre os dias
            }

            sb.append("👉 Digite *0* para voltar ao Menu Principal.");

        } else {
            // Fallback: Se não houver dias no banco, tenta mostrar o texto antigo ou aviso
            return "⏳ No momento não temos datas e valores cadastrados no sistema. Fale com um atendente!";
        }

        return sb.toString();
    }
}