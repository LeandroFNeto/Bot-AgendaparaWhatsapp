package com.example.demo.servico;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;
import com.google.api.services.calendar.CalendarScopes;
import java.io.InputStream;
import com.google.api.services.calendar.Calendar;
import java.util.Collections;

@Service
public class ServicoGoogleagenda {

    public Calendar conectarAgenda() throws  Exception {
        InputStream in = ServicoGoogleagenda.class.getResourceAsStream ("/crendecial.json");

    if(in==null){
        throw new RuntimeException("arquivo crendetial.json não encontredo na pasta resources.");
    }

    GoogleCredential credential = GoogleCredential.fromStream(in).
            createScoped(Collections.singleton(CalendarScopes.CALENDAR));

    return new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),GsonFactory.getDefaultInstance(),
                credential).setApplicationName("Bot Reservas Area de Lazer").build();
    }
}

