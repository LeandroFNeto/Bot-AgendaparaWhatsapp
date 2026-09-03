package com.example.demo.controller;

import com.example.demo.model.Empresa;
import com.example.demo.repository.EmpresaRepository;
import com.example.demo.servico.GerenciadorSessao;
import com.example.demo.servico.ServicoGoogleagenda;
import com.example.demo.servico.ServicoIA;
import com.example.demo.servico.ServicoMensagem;
import com.example.demo.servico.ServicoMenu;
import com.example.demo.servico.ServicoReserva;
import com.example.demo.servico.observador.Observadorwhatsapp;
import com.example.demo.strategy.FactoryModulo;
import com.example.demo.strategy.LocacaoStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ControllerWhatsapp.class)
@Import(LocacaoStrategy.class)
class ControllerWhatsappTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocacaoStrategy locacaoStrategy;

    // @WebMvcTest sobe só a fatia web: os colaboradores viram dublês para
    // testar o comportamento do webhook sem banco, Google, IA ou WPPConnect.
    @MockitoBean private GerenciadorSessao gerenciadorSessao;
    @MockitoBean private EmpresaRepository empresaRepository;
    @MockitoBean private FactoryModulo moduloFactory;
    @MockitoBean private Observadorwhatsapp observador;
    // Evita a necessidade do arquivo crendecial.json neste slice de MVC.
    @MockitoBean private ServicoGoogleagenda servicoGoogleagenda;
    @MockitoBean private ServicoIA servicoIA;
    @MockitoBean private ServicoMensagem servicoMensagem;
    @MockitoBean private ServicoMenu servicoMenu;
    @MockitoBean private ServicoReserva servicoReserva;

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

        verify(gerenciadorSessao).obterEstadoAtual("551188887777");
    }

    @Test
    @DisplayName("Deve processar mensagem do cliente e acionar a IA")
    void deveProcessarMensagemEAcionarIA() throws Exception {
        Empresa empresa = new Empresa();
        empresa.setSessaoWhatsapp("teste-sessao");
        empresa.setRamoDeAtuacao("LOCACAO");
        empresa.setUsaIA(true);
        empresa.setNome("Recanto Teste");

        when(empresaRepository.findBySessaoWhatsapp("teste-sessao")).thenReturn(empresa);
        when(gerenciadorSessao.obterEstadoAtual(anyString())).thenReturn("INICIO");
        when(moduloFactory.obterEstrategia("LOCACAO")).thenReturn(locacaoStrategy);
        when(servicoMenu.descobrirAcao(any(), anyString())).thenReturn("OPCAO_INVALIDA");
        when(servicoIA.gerarRespostaHumanizada(anyString())).thenReturn("Olá! Como posso ajudar?");

        String payload = """
            {
                "session": "teste-sessao",
                "from": "5511988887777@c.us",
                "body": "Oi",
                "type": "chat",
                "fromMe": false
            }
        """;

        mockMvc.perform(post("/webhook/whatsapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verify(servicoIA, times(1)).gerarRespostaHumanizada(contains("Oi"));
    }
}
