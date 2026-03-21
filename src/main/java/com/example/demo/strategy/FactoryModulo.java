package com.example.demo.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FactoryModulo {

    // Um mapa (Dicionário) que guarda: "NOME_DO_RAMO" -> Classe Strategy correspondente
    private final Map<String, ModuloAtendimentoStrategy> estrategias;

    @Autowired
    public FactoryModulo(List<ModuloAtendimentoStrategy> listaDeEstrategias) {
        // O Spring acha sozinho todas as classes que implementam a interface e joga nessa lista.
        // Aqui nós transformamos a lista num Mapa para buscar super rápido.
        this.estrategias = listaDeEstrategias.stream()
                .collect(Collectors.toMap(ModuloAtendimentoStrategy::getRamoDeAtuacao, estrategia -> estrategia));
    }

    // O Webhook vai chamar esse método
    public ModuloAtendimentoStrategy obterEstrategia(String ramo) {
        ModuloAtendimentoStrategy estrategia = estrategias.get(ramo);

        if (estrategia == null) {
            throw new IllegalArgumentException("Ramo de atuação não suportado ou em construção: " + ramo);
        }

        return estrategia;
    }

}
