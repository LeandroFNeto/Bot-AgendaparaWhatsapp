package com.example.demo.servico;

import com.example.demo.model.Empresa;
import com.example.demo.servico.observador.Observadorwhatsapp;
import com.example.demo.util.WhatsappUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ServicoMensagem {

    @Autowired
    private ServicoWppConnect wppConnectService;

    @Autowired
    private Observadorwhatsapp observador; // 👈 Adicionado para monitorar o envio

    @Autowired
    private WhatsappUtil whatsappUtil;

    public void enviarMensagemWPP(Empresa empresa, String numeroDestino, String texto) {
        String sessao = empresa.getSessaoWhatsapp();

        // Limpamos o número para garantir que o WPPConnect receba apenas os dígitos
        // ou o JID correto. O método 'extrairApenasNumeros' é perfeito aqui.
        String numeroLimpo = WhatsappUtil.extrairApenasNumeros(numeroDestino);

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://wppconnect:21465/api/" + sessao + "/send-message";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + wppConnectService.obterToken());

            Map<String, Object> body = new HashMap<>();
            body.put("phone", numeroLimpo); // 👈 Enviamos o número limpo
            body.put("message", texto);
            body.put("isGroup", false); // Como o controller já filtra grupos, aqui é quase sempre false

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);

            // Usamos o observador aqui também!
            observador.logSucesso(sessao, numeroDestino, "[RESPOSTA ENVIADA]: " + texto);

        } catch (Exception e) {
            observador.logErro(sessao, numeroDestino, "ServicoMensagem (Envio)", e);
            if (e.getMessage().contains("401")) {
                wppConnectService.invalidarToken();
            }
        }
    }
}
