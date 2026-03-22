package com.example.demo.servico;

import com.example.demo.model.Empresa;
import com.example.demo.model.ModuloEmpresa;
import org.springframework.stereotype.Service;

@Service
public class ServicoMenu {

    // 1. MONTA O MENU LENDO DIRETO DO BANCO DE DADOS
    public String montarMenuPrincipal(Empresa empresa) {
        StringBuilder menu = new StringBuilder();
        menu.append(empresa.getMensagemSaudacao()).append("\n\n");

        int numeroOpcao = 1;
        for (ModuloEmpresa modulo : empresa.getModulosAtivos()) {
            if (modulo.getAtivo()) {
                menu.append(numeroOpcao).append("️⃣ - ").append(modulo.getTextoMenu()).append("\n");
                numeroOpcao++;
            }
        }

        menu.append("\n👉 Digite o número da opção desejada.");
        return menu.toString();
    }

    // 2. DESCOBRE O QUE O CLIENTE QUER LENDO A MESMA ORDEM DO BANCO
    public String descobrirAcao(Empresa empresa, String numeroDigitado) {
        try {
            int numero = Integer.parseInt(numeroDigitado);
            int contador = 1;

            // Percorre os módulos na mesmíssima ordem que o menu foi desenhado
            for (ModuloEmpresa modulo : empresa.getModulosAtivos()) {
                if (modulo.getAtivo()) {
                    // Se o contador bateu com o número que o cliente digitou, achamos a ação!
                    if (contador == numero) {
                        return modulo.getCodigoAcao(); // Ex: Retorna "PESQUISAR_DATA" ou "VER_FOTOS"
                    }
                    contador++;
                }
            }
            return "OPCAO_INVALIDA"; // Digitou um número maior do que as opções disponíveis

        } catch (NumberFormatException e) {
            return "OPCAO_INVALIDA"; // Digitou uma letra em vez de número
        }
    }
}