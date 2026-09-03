# language: pt
# Arquitetura: EventoReservaConfirmada → ObservadorGoogleAgenda (@Async)
# Carga correspondente: k6/webhook-carga.js (HTTP não espera o Google)
Funcionalidade: Observer assíncrono da Google Agenda
  Como sistema de reservas
  Quero que a confirmação dispare o ObservadorGoogleAgenda sem bloquear o webhook
  Para a integração com a agenda ser resiliente sob tráfego

  Cenário: evento de reserva é consumido de forma assíncrona
    Dado uma empresa persistida com sessão "sessao_agenda" e ramo "LOCACAO"
    Quando o evento EventoReservaConfirmada é publicado para a data "25-12-2026"
    Então o ObservadorGoogleAgenda deve agendar no Google sem bloquear o publicador
