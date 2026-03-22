package com.example.demo.controller;

import com.example.demo.model.Empresa;
import com.example.demo.repository.EmpresaRepository;
import com.example.demo.servico.GerenciadorSessao;
import com.example.demo.servico.observador.Observadorwhatsapp;
import com.example.demo.strategy.FactoryModulo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // 👈 O novo pacote aqui!
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ControllerWhatsapp.class)
class ControllerWhatsappTest {

    @Autowired
    private MockMvc mockMvc;

    // Trocamos MockBean por MockitoBean
    @MockitoBean private GerenciadorSessao gerenciadorSessao;
    @MockitoBean private EmpresaRepository empresaRepository;
    @MockitoBean private FactoryModulo moduloFactory;
    @MockitoBean private Observadorwhatsapp observador;

    @Test
    @DisplayName("Deve ignorar mensagem quando for enviada pelo dono (Anti-X9)")
    void deveIgnorarMensagemDono() throws Exception {
        String payload = """
            {
                "session": "teste-sessao",
                "from": "5511988887777@c.us",
                "body": "Teste do dono",
                "type": "chat",
                "fromMe": true
            }
        """;

        mockMvc.perform(post("/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verify(observador).logFiltro(eq("teste-sessao"), anyString(), contains("Anti-X9"));
        verifyNoInteractions(moduloFactory);
    }

    @Test
    @DisplayName("Deve normalizar número ignorando o 9º dígito extra")
    void deveNormalizarNumero() throws Exception {
        Empresa emp = new Empresa();
        emp.setSessaoWhatsapp("teste-sessao");
        when(empresaRepository.findBySessaoWhatsapp("teste-sessao")).thenReturn(emp);

        String payload = """
            {
                "session": "teste-sessao",
                "from": "5511988887777@c.us",
                "body": "Quero alugar",
                "type": "chat",
                "fromMe": false
            }
        """;

        mockMvc.perform(post("/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // Verifica se a busca no Redis/Cache foi feita com o número normalizado pela WhatsappUtil
        verify(gerenciadorSessao).obterEstadoAtual("551188887777");
    }
}