package com.example.demo;

import com.example.demo.servico.ServicoGoogleagenda;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
// Injeta credenciais falsas em memória para o Spring Boot subir o contexto
// sem depender do .env nem de variáveis reais de ambiente (CI/local).
@TestPropertySource(properties = {
		"ADMIN_API_KEY=mock-admin-key",
		"GEMINI_API_KEY=mock-gemini-key",
		"WPPCONNECT_SECRET_KEY=mock-wpp-key",
		"WPPCONNECT_URL=http://localhost:21465",
		"gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent",
		"gemini.api.key=mock-gemini-key"
})
class DemoApplicationTests {

	// Evita a necessidade do arquivo crendecial.json: o bean real tenta
	// autenticar no Google no @PostConstruct e derrubaria o contextLoads.
	@MockitoBean
	ServicoGoogleagenda servicoGoogleagenda;

	@Test
	void contextLoads() {
	}

}
