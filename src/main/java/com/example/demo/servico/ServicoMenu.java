package com.example.demo.servico;

import com.example.demo.model.Empresa;
import com.example.demo.model.ModuloEmpresa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 👈 Import necessário

@Service
public class ServicoMenu {

    // 🔥 Mantém a conexão aberta para o Java conseguir buscar os módulos "preguiçosos"
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true) // 🔥 Aqui também!
    public String descobrirAcao(Empresa empresa, String numeroDigitado) {
        try {
            int numero = Integer.parseInt(numeroDigitado);
            int contador = 1;

            for (ModuloEmpresa modulo : empresa.getModulosAtivos()) {
                if (modulo.getAtivo()) {
                    if (contador == numero) {
                        return modulo.getCodigoAcao();
                    }
                    contador++;
                }
            }
            return "OPCAO_INVALIDA";

        } catch (NumberFormatException e) {
            return "OPCAO_INVALIDA";
        }
    }
}