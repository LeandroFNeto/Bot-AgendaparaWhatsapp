package com.example.demo;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.format.TextStyle;
import java.util.Locale;

@RestController
public class ControllerReserva {

    @Autowired
    private ControllerGoogleagenda agendaService;

    @Value("${google.calendar.id}")
    private String idDaAgenda;

    @GetMapping("/reservas")
    public List<DiaReservaDTO> listarReservas(){
        try{
            com.google.api.services.calendar.Calendar servicoGoogle = agendaService.conectarAgenda();
            DateTime agora = new DateTime(System.currentTimeMillis());

            Events eventos = servicoGoogle.events().list(idDaAgenda)
                    .setMaxResults(100)
                    .setTimeMin(agora)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            List<Event> itens = eventos.getItems();

            if(itens.isEmpty()){
                return null;
            }

            List<DiaReservaDTO> listaDias = new ArrayList<>();
            Set<LocalDate> diasAlugados = new HashSet<>();

            for (Event evento : itens){
                var inicio = evento.getStart();

                if (inicio.getDateTime() != null){
                    ZonedDateTime dataComHora = ZonedDateTime.parse(inicio.getDateTime().toString());
                    diasAlugados.add(dataComHora.toLocalDate());
                } else if (inicio.getDate() != null) {
                    LocalDate dataSemHora = LocalDate.parse(inicio.getDate().toString());
                    diasAlugados.add(dataSemHora);
                }
            }

            DateTimeFormatter formatadorDia = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate hoje = LocalDate.now();
            Locale idiomaBrasil = new Locale("pt", "BR");

            for (int i = 0; i < 30; i++){
                LocalDate diaAtual = hoje.plusDays(i);
                String dataFormatada = diaAtual.format(formatadorDia);
                String nomeDiaSemana = diaAtual.getDayOfWeek().getDisplayName(TextStyle.FULL, idiomaBrasil);

                if (diasAlugados.contains(diaAtual)) {
                    listaDias.add(new DiaReservaDTO(dataFormatada, nomeDiaSemana, "Alugado"));
                } else {
                    listaDias.add(new DiaReservaDTO(dataFormatada, nomeDiaSemana, "Disponível"));
                }
            }

            return listaDias;

        } catch (Exception e) {
            System.out.println("⚠️ ERRO CRÍTICO NO GOOGLE CALENDAR (Listar): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/reservas/{dataRecebida}")
    public DiaReservaDTO pesquisarData(@PathVariable String dataRecebida) {
        try {
            DateTimeFormatter formatadorEntrada = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate dataAlvo = LocalDate.parse(dataRecebida, formatadorEntrada);

            // 🛑 A TRAVA: Se a data digitada for antes do dia de hoje (no passado)
            if (dataAlvo.isBefore(LocalDate.now())) {
                // Lança a "batata quente" para o ControllerWhatsapp pegar
                throw new IllegalArgumentException("Não é possível consultar datas que já passaram.");
            }

            ZoneId fusoBrasilia = ZoneId.of("America/Sao_Paulo");
            Locale idiomaBrasil = new Locale("pt", "BR");
            String nomeDiaSemana = dataAlvo.getDayOfWeek().getDisplayName(TextStyle.FULL, idiomaBrasil);

            ZonedDateTime inicioDoDia = dataAlvo.atStartOfDay(fusoBrasilia);
            ZonedDateTime fimDoDia = dataAlvo.atTime(23, 59, 59).atZone(fusoBrasilia);

            DateTime tempoMinimo = new DateTime(inicioDoDia.toInstant().toEpochMilli());
            DateTime tempoMaximo = new DateTime(fimDoDia.toInstant().toEpochMilli());

            com.google.api.services.calendar.Calendar servicoGoogle = agendaService.conectarAgenda();

            Events eventos = servicoGoogle.events().list(idDaAgenda)
                    .setTimeMin(tempoMinimo)
                    .setTimeMax(tempoMaximo)
                    .setSingleEvents(true)
                    .execute();

            List<Event> itens = eventos.getItems();

            if (itens.isEmpty()) {
                return new DiaReservaDTO(dataRecebida, nomeDiaSemana, "Disponível");
            } else {
                return new DiaReservaDTO(dataRecebida, nomeDiaSemana, "Alugado");
            }

        } catch (IllegalArgumentException e) {
            // Se o erro foi a data no passado, apenas joga para o método que chamou
            throw e;
        } catch (Exception e) {
            System.out.println("⚠️ ERRO CRÍTICO NO GOOGLE CALENDAR (Pesquisar): " + e.getMessage());
            e.printStackTrace();
            // Joga um erro genérico para o WhatsApp avisar que o formato foi inválido
            throw new RuntimeException("Erro ao buscar data: " + e.getMessage());
        }
    }
}