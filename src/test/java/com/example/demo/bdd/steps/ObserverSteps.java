package com.example.demo.bdd.steps;

import com.example.demo.bdd.BddContexto;
import com.example.demo.evento.EventoReservaConfirmada;
import com.example.demo.model.Empresa;
import com.example.demo.repository.EmpresaRepository;
import com.example.demo.servico.ServicoReserva;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.Duration;

public class ObserverSteps {

    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ServicoReserva servicoReserva;
    @Autowired private BddContexto ctx;

    @Quando("o evento EventoReservaConfirmada é publicado para a data {string}")
    public void publicaEvento(String data) {
        Empresa empresa = empresaRepository.findBySessaoWhatsapp(ctx.sessao);
        long inicio = System.nanoTime();
        publisher.publishEvent(new EventoReservaConfirmada(
                empresa, data, "Maria Silva", "5511999999999@c.us"));
        long bloqueioMs = Duration.ofNanos(System.nanoTime() - inicio).toMillis();
        ctx.status = (int) bloqueioMs;
    }

    @Então("o ObservadorGoogleAgenda deve agendar no Google sem bloquear o publicador")
    public void observerAgendou() throws Exception {
        assertPublicacaoNaoBloqueou();
        verify(servicoReserva, timeout(3000).atLeastOnce())
                .agendarNoGoogle(org.mockito.ArgumentMatchers.any(Empresa.class), eq("25-12-2026"), eq("Maria Silva"));
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() ->
                verify(servicoReserva, atLeastOnce())
                        .agendarNoGoogle(org.mockito.ArgumentMatchers.any(Empresa.class), eq("25-12-2026"), eq("Maria Silva")));
    }

    private void assertPublicacaoNaoBloqueou() {
        org.assertj.core.api.Assertions.assertThat(ctx.status)
                .as("publishEvent não deve esperar o @Async do Observer")
                .isLessThan(200);
    }
}
