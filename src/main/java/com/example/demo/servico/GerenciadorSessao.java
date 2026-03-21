package com.example.demo.servico;
import com.example.demo.model.EstadoUsuario;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GerenciadorSessao {
    // Guarda as sessões na memória de forma segura
    private final Map<String, EstadoUsuario> sessoes = new ConcurrentHashMap<>();

    // 10 minutos em milissegundos
    private static final long TEMPO_EXPIRACAO_MS = 10 * 60 * 1000;

    public String obterEstadoAtual(String numeroCliente) {
        long agora = System.currentTimeMillis();
        EstadoUsuario sessao = sessoes.get(numeroCliente);

        // Se a sessão existe mas já passou de 10 minutos
        if (sessao != null && (agora - sessao.getUltimaInteracao() > TEMPO_EXPIRACAO_MS)) {
            System.out.println("⏳ Sessão de " + numeroCliente + " expirada por tempo. Resetando para INICIO.");
            sessoes.remove(numeroCliente);
            return "INICIO";
        }

        // Retorna a fase atual ou INICIO se for cliente novo
        return sessao != null ? sessao.getFase() : "INICIO";
    }

    public void atualizarEstado(String numeroCliente, String novaFase) {
        // Grava a nova fase e carimba a hora exata de AGORA
        sessoes.put(numeroCliente, new EstadoUsuario(novaFase, System.currentTimeMillis()));
    }
}
