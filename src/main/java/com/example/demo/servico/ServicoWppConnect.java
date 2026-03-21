package com.example.demo;


import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class WppConnectService {

    // Guarda o token na memória desta classe
    private String tokenAutenticacao = null;

    public String obterToken() {
        // Se já temos o token, não pede de novo
        if (tokenAutenticacao != null) {
            return tokenAutenticacao;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://wppconnect:21465/api/RecantoBot/THISISMYSECURETOKEN/generate-token";
            Map resposta = restTemplate.postForObject(url, null, Map.class);

            if (resposta != null && resposta.containsKey("token")) {
                tokenAutenticacao = (String) resposta.get("token");
                System.out.println("✅ Novo Token WPPConnect gerado com sucesso!");
                return tokenAutenticacao;
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erro ao obter token: " + e.getMessage());
        }
        return "";
    }

    // Método para apagar o token caso ele expire (Erro 401)
    public void invalidarToken() {
        this.tokenAutenticacao = null;
        System.out.println("🔄 Token WPPConnect invalidado. Um novo será gerado no próximo envio.");
    }
}
