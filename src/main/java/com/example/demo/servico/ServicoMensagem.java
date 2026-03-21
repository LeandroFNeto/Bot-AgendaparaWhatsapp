package com.example.demo.servico;

import com.example.demo.model.Empresa;
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

    // Qualquer outro ficheiro do seu projeto agora pode chamar este método para mandar mensagem!
    public void enviarMensagemWPP(Empresa empresa, String numero, String texto) {
        System.out.println("🚀 [CARTEIRO] Enviando resposta para " + numero + " (Sessão: " + empresa.getSessaoWhatsapp() + ")");

        try {
            RestTemplate restTemplate = new RestTemplate();

            // A URL dinâmica baseada na sessão do cliente
            String url = "http://wppconnect:21465/api/" + empresa.getSessaoWhatsapp() + "/send-message";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + wppConnectService.obterToken());

            Map<String, Object> body = new HashMap<>();
            body.put("phone", numero);
            body.put("message", texto);
            body.put("isGroup", numero.contains("@g.us"));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, request, String.class);

        } catch (Exception e) {
            System.out.println("❌ [CARTEIRO] Falha ao enviar mensagem: " + e.getMessage());
            if (e.getMessage().contains("401")) {
                wppConnectService.invalidarToken();
            }
        }
    }

    public void enviarImagemWPP(String telefone, String sessao, String urlImagem, String nomeArquivo, String legenda) {

        // A rota específica do WPPConnect para imagens
        String url = "http://localhost:21465/api/" + sessao + "/send-image";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Aqui usamos o mesmo token de segurança que você já configurou para o texto
        headers.setBearerAuth(wppConnectService.obterToken());

        // Montando o pacote que o WPPConnect exige
        Map<String, Object> body = new HashMap<>();
        body.put("phone", telefone);
        body.put("path", urlImagem); // Pode ser um link do Google Drive, Imgur, ou do seu próprio site
        body.put("filename", nomeArquivo);
        body.put("caption", legenda); // O texto que vai embaixo da foto

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.postForEntity(url, request, String.class);
            System.out.println("📸 [CARTEIRO] Imagem enviada com sucesso para " + telefone);
        } catch (Exception e) {
            System.out.println("❌ [CARTEIRO] Falha ao enviar imagem: " + e.getMessage());
        }
    }
}
