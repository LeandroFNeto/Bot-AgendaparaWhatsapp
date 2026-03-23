package com.example.demo.controller;

import com.example.demo.model.Empresa;
import com.example.demo.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/empresas")
public class ControllerEmpresa {

    @Autowired
    private EmpresaRepository empresaRepository;

    // Trazemos a senha do painel de admin lá do application.properties / .env
    @Value("${admin.api.key}")
    private String adminApiKey;

    // Método auxiliar (O Segurança da Porta)
    private boolean isAcessoNegado(String tokenRecebido) {
        return tokenRecebido == null || !tokenRecebido.equals(adminApiKey);
    }

    // 🔥 ROTA EXCLUSIVA PARA CADASTRAR (POST)
    @PostMapping
    public ResponseEntity<String> cadastrarEmpresa(
            @RequestHeader(value = "x-admin-token", required = false) String token,
            @RequestBody Empresa novaEmpresa) {

        // 1. Barrar quem não tem a chave
        if (isAcessoNegado(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ Acesso negado: Token de administrador inválido.");
        }

        // 2. Validação básica
        if (novaEmpresa.getSessaoWhatsapp() == null || novaEmpresa.getNome() == null) {
            return ResponseEntity.badRequest().body("❌ Erro: Nome e Sessão são obrigatórios para novos cadastros!");
        }

        // 3. Garantir que não existe
        Empresa empresaExistente = empresaRepository.findBySessaoWhatsapp(novaEmpresa.getSessaoWhatsapp());
        if (empresaExistente != null) {
            return ResponseEntity.badRequest().body("❌ Erro: Já existe uma empresa com esta sessão. Use o método PUT para atualizar.");
        }

        empresaRepository.save(novaEmpresa);
        return ResponseEntity.status(HttpStatus.CREATED).body("✅ Nova empresa '" + novaEmpresa.getNome() + "' cadastrada e pronta para uso!");
    }

    // 🔥 ROTA EXCLUSIVA PARA ATUALIZAR (PUT)
    @PutMapping
    public ResponseEntity<String> atualizarEmpresa(
            @RequestHeader(value = "x-admin-token", required = false) String token,
            @RequestBody Empresa dadosAtualizados) {

        // 1. Barrar quem não tem a chave
        if (isAcessoNegado(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ Acesso negado: Token de administrador inválido.");
        }

        if (dadosAtualizados.getSessaoWhatsapp() == null) {
            return ResponseEntity.badRequest().body("❌ Erro: É necessário informar a sessão do WhatsApp para atualizar a empresa.");
        }

        // 2. Buscar no banco
        Empresa empresaExistente = empresaRepository.findBySessaoWhatsapp(dadosAtualizados.getSessaoWhatsapp());
        if (empresaExistente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ Erro: Empresa não encontrada para a sessão informada.");
        }

        // 3. Atualização segura (só altera o que foi enviado no JSON)
        if (dadosAtualizados.getNome() != null) empresaExistente.setNome(dadosAtualizados.getNome());
        if (dadosAtualizados.getMensagemSaudacao() != null) empresaExistente.setMensagemSaudacao(dadosAtualizados.getMensagemSaudacao());
        if (dadosAtualizados.getRamoDeAtuacao() != null) empresaExistente.setRamoDeAtuacao(dadosAtualizados.getRamoDeAtuacao());
        if (dadosAtualizados.getTabelaDePrecos() != null) empresaExistente.setTabelaDePrecos(dadosAtualizados.getTabelaDePrecos());
        if (dadosAtualizados.getLinkGoogleMaps() != null) empresaExistente.setLinkGoogleMaps(dadosAtualizados.getLinkGoogleMaps());
        if (dadosAtualizados.getLinkFotoPrincipal() != null) empresaExistente.setLinkFotoPrincipal(dadosAtualizados.getLinkFotoPrincipal());
        if (dadosAtualizados.getLinkGaleria()!= null) empresaExistente.setLinkGaleria(dadosAtualizados.getLinkGaleria());
        if (dadosAtualizados.getGoogleCalendarId() != null) empresaExistente.setGoogleCalendarId(dadosAtualizados.getGoogleCalendarId());
        // Se a empresa mudou a regra de locação, atualiza também
        if (dadosAtualizados.getLocacaoPorHora() != null) empresaExistente.setLocacaoPorHora(dadosAtualizados.getLocacaoPorHora());

        empresaRepository.save(empresaExistente);
        return ResponseEntity.ok("✅ Configurações de '" + empresaExistente.getNome() + "' atualizadas com sucesso!");
    }
}