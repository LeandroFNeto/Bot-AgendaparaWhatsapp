package com.example.demo.servico.observador;

import com.example.demo.evento.EventoReservaConfirmada;
import com.example.demo.servico.ServicoMensagem;
import com.example.demo.servico.ServicoReserva;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ObservadorGoogleAgenda {

    @Autowired
    private ServicoReserva servicoReserva;

    @Autowired
    private ServicoMensagem servicoMensagem;


    @Async
    @EventListener
    public void registarNoGoogle(EventoReservaConfirmada evento) {
        System.out.println("🎧 [OBSERVER] A escutar o evento... A agendar no Google para: " + evento.getNomeCliente());

        try {
            // 1. Chama o serviço do Google que já tínhamos criado
            servicoReserva.agendarNoGoogle(evento.getEmpresa(), evento.getDataReserva(), evento.getNomeCliente());

            // 2. Envia o comprovativo de sucesso ao cliente
            String mensagemSucesso = "✅ *Reserva Confirmada com Sucesso!*\n\n" +
                    "📅 Data: " + evento.getDataReserva() + "\n" +
                    "📍 O seu lugar está garantido na nossa agenda.\n\n" +
                    "Agradecemos a preferência!";

            servicoMensagem.enviarMensagemWPP(evento.getEmpresa(), evento.getNumeroWhatsapp(), mensagemSucesso);

        } catch (Exception e) {
            System.out.println("❌ Erro ao tentar salvar na agenda do Google: " + e.getMessage());
            servicoMensagem.enviarMensagemWPP(evento.getEmpresa(), evento.getNumeroWhatsapp(), "⚠️ Houve um pequeno erro ao registar na agenda digital, mas um assistente já vai verificar!");
        }
    }
}

