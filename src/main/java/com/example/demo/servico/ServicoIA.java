package  com.example.demo.servico ;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Classe responsável por atuar como "Ponte de Dados" entre o sistema SaaS e a IA do Google (Gemini).
 * * ARQUITETURA:
 * Esta classe NÃO executa regras de negócio (como salvar no banco ou validar datas no Google Calendar).
 * Ela apenas recebe um "Contexto/Prompt" gerado pelo Java, envia para a IA e retorna a resposta
 * humanizada para ser enviada ao cliente via WhatsApp.
 */
@Service
public class ServicoIA {

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;

    // Injeção de dependência via construtor (Melhor prática do Spring)
    public ServicoIA(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Envia um prompt estruturado para o Gemini e extrai a resposta em texto.
     * * @param prompt O texto de instrução + dados contextuais (ex: horários disponíveis).
     * @return String contendo a resposta gerada pela IA, pronta para o WhatsApp.
     */
    public String gerarRespostaHumanizada(String prompt) {
        try {
            // 1. Monta a URL completa com a chave de autenticação
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            // 2. Configura os Headers para avisar que estamos mandando JSON
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 3. Monta o corpo da requisição (Payload) exigido pela documentação do Gemini.
            // Para não criar classes DTOs complexas só para isso (evitando a "bazuca"),
            // formatamos o JSON diretamente, escapando as aspas duplas do prompt.
            String jsonBody = "{\n" +
                    "  \"contents\": [{\n" +
                    "    \"parts\":[{\"text\": \"" + prompt.replace("\"", "\\\"").replace("\n", " ") + "\"}]\n" +
                    "    }]\n" +
                    "}";

            // 4. Empacota tudo para envio
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            // 5. Dispara a requisição HTTP POST para a API do Google
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            // 6. Processa o retorno.
            // O retorno real é um JSON complexo. Aqui, delegamos para um método auxiliar extrair só o texto.
            return extrairTextoDaResposta(response.getBody());

        } catch (Exception e) {
            // Em caso de falha (queda de internet, limite de cota da API, etc),
            // temos um "Fallback" (Plano B) seguro para não deixar o cliente sem resposta.
            System.err.println("Erro ao comunicar com a IA: " + e.getMessage());
            return "Desculpe, nosso assistente inteligente está indisponível no momento. " +
                    "Por favor, aguarde um instante ou tente novamente.";
        }
    }

    /**
     * Método auxiliar para "garimpar" apenas o texto útil de dentro do JSON gigante retornado pelo Google.
     * Usamos manipulação básica de string para manter o serviço extremamente rápido e sem dependências extras.
     */
    private String extrairTextoDaResposta(String jsonResposta) {
        if (jsonResposta == null || !jsonResposta.contains("\"text\":")) {
            return "Erro ao processar a resposta da IA.";
        }

        // Uma forma simples (e rápida) de extrair o texto do JSON sem criar DTOs gigantes
        // Pega o que está depois de "text": "
        String[] partes = jsonResposta.split("\"text\": \"");
        if (partes.length > 1) {
            // Pega até o próximo fechamento de aspas duplas
            String textoBruto = partes[1].split("\"")[0];
            // Remove escapes de quebra de linha do JSON (\n) para ficar bonito no WhatsApp
            return textoBruto.replace("\\n", "\n").trim();
        }
        return "Não consegui formular uma resposta agora.";
    }
}