package com.example.demo.servico;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.Calendar;
import jakarta.annotation.PostConstruct; // Importante (Pode ser javax.annotation se for Spring Boot antigo)

import java.io.InputStream;
import java.util.Collections;

@Service
public class ServicoGoogleagenda {

    // Guardamos o serviço pronto aqui na memória!
    private Calendar servicoGoogleEmMemoria;

    // Essa anotação faz o método rodar sozinho assim que o projeto Spring levanta
    @PostConstruct
    public void inicializarAgenda() {
        try {
            System.out.println("⏳ Inicializando conexão com Google Agenda...");
            InputStream in = ServicoGoogleagenda.class.getResourceAsStream("/crendecial.json");

            if(in == null){
                throw new RuntimeException("Arquivo crendecial.json não encontrado na pasta resources.");
            }

            GoogleCredential credential = GoogleCredential.fromStream(in)
                    .createScoped(Collections.singleton(CalendarScopes.CALENDAR));

            this.servicoGoogleEmMemoria = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("Bot Reservas Area de Lazer")
                    .build();

            System.out.println("✅ Google Agenda inicializado e salvo em memória com sucesso!");

        } catch (Exception e) {
            System.err.println("❌ Erro Crítico ao conectar com Google Agenda: " + e.getMessage());
        }
    }

    // Agora, os outros serviços chamam este método e a resposta é imediata!
    public Calendar conectarAgenda() {
        if (this.servicoGoogleEmMemoria == null) {
            throw new RuntimeException("O serviço do Google Agenda não está inicializado.");
        }
        return this.servicoGoogleEmMemoria;
    }
}