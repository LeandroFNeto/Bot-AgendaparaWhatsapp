package com.example.demo.util;

import org.springframework.stereotype.Component;

@Component
public class WhatsappUtil {
    /**
     * Limpa o JID e retorna apenas os números.
     * Trata JIDs como '5511988887777@c.us' ou '551188887777@s.whatsapp.net'
     */
    public static String extrairApenasNumeros(String jid) {
        if (jid == null) return "";
        // Pega tudo antes do @ e remove o que não for número
        return jid.split("@")[0].replaceAll("\\D", "");
    }

    /**
     * Normaliza o número para o Banco de Dados/Cache de Estado.
     * Regra: No Brasil (55), se tiver 11 dígitos após o 55 (DDI+DDD+9+8),
     * removemos o '9' para garantir que o estado seja o mesmo se o número oscilar.
     */
    public static String normalizarParaChaveEstado(String jid) {
        String numeros = extrairApenasNumeros(jid);

        // Se for Brasil (55) e tiver o formato com 9 dígitos (13 caracteres total)
        if (numeros.startsWith("55") && numeros.length() == 13) {
            // Remove o '9' (índice 4: 5511[9]...)
            return numeros.substring(0, 4) + numeros.substring(5);
        }
        return numeros;
    }

    /**
     * Verifica se o JID é um identificador de privacidade (LID).
     * Útil para logar avisos específicos, pois @lid costuma dar erro ao tentar responder.
     */
    public static boolean isLid(String jid) {
        return jid != null && jid.contains("@lid");
    }
}
