package com.example.demo.servico;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class ServicoWppConnect {

    // Lê do application.properties
    @Value("${wppconnect.url}")
    private String urlBase;

    @Value("${wppconnect.secret-key}")
    private String secretKey;

    private String tokenAutenticacao = null;

    // Agora passamos a sessão (empresa) como parâmetro!
    public String obterToken(String sessao) {
        if (tokenAutenticacao != null) {
            return tokenAutenticacao;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            // Monta a URL dinamicamente para QUALQUER empresa!
            String url = urlBase + "/api/" + sessao + "/" + secretKey + "/generate-token";

            Map resposta = restTemplate.postForObject(url, null, Map.class);

            if (resposta != null && resposta.containsKey("token")) {
                tokenAutenticacao = (String) resposta.get("token");
                System.out.println("✅ Novo Token WPPConnect gerado com sucesso para a sessão: " + sessao);
                return tokenAutenticacao;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erro ao obter token para a sessão " + sessao + ": " + e.getMessage());
        }
        return "";
    }

    public void invalidarToken() {
        this.tokenAutenticacao = null;
        System.out.println("🔄 Token WPPConnect invalidado. Um novo será gerado na próxima requisição.");
    }
}