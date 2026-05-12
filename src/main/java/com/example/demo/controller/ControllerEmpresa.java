package com.example.demo.controller;

import com.example.demo.dto.empresa.*;
import com.example.demo.model.Empresa;
import com.example.demo.repository.EmpresaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/empresas")
@Tag(name = "Gerenciamento de Empresas", description = "Endpoints para Criação, Clientes e Administradores do SaaS")
public class ControllerEmpresa {

    @Autowired private EmpresaRepository empresaRepository;
    @Value("${admin.api.key}") private String adminApiKey;

    private boolean isAcessoNegado(String token) {
        return token == null || !token.equals(adminApiKey);
    }

    // ========================================================================
    // 🚪 PORTA 0: CADASTRAR NOVA EMPRESA (POST)
    // ========================================================================
    @PostMapping
    @Operation(summary = "Criar novo cliente SaaS", description = "Cadastra uma empresa e define sua sessão única de WhatsApp.")
    @ApiResponse(responseCode = "201", description = "Empresa criada com sucesso")
    public ResponseEntity<?> cadastrar(@RequestHeader(value = "x-admin-token", required = false) String token,
                                       @Valid @RequestBody EmpresaCreateDTO dto) {

        if (isAcessoNegado(token)) return ResponseEntity.status(401).body("Token Inválido");

        if (empresaRepository.findBySessaoWhatsapp(dto.sessaoWhatsapp()) != null) {
            return ResponseEntity.badRequest().body("Sessão já ocupada");
        }

        // Converte DTO para Entity
        Empresa empresa = new Empresa();
        empresa.setNome(dto.nome());
        empresa.setSessaoWhatsapp(dto.sessaoWhatsapp());
        empresa.setRamoDeAtuacao(dto.ramoDeAtuacao());

        Empresa salva = empresaRepository.save(empresa);

        return ResponseEntity.status(HttpStatus.CREATED).body(new EmpresaResponseDTO(
                salva.getId(), salva.getNome(), salva.getSessaoWhatsapp(), "DISCONNECTED", null, java.time.LocalDateTime.now()
        ));
    }

    // ========================================================================
    // 🚪 PORTA 1: PAINEL DO CLIENTE (PUT) - O dono do Recanto usa essa
    // ========================================================================
    @PutMapping("/{sessao}/cliente")
    @Operation(summary = "Atualização pelo Cliente", description = "Altera textos, links e aparências. Não altera cobrança.")
    public ResponseEntity<String> atualizarPeloCliente(
            @RequestHeader(value = "x-cliente-token", required = false) String token,
            @PathVariable String sessao,
            @RequestBody EmpresaUpdateClienteDTO dto) {

        Empresa empresa = empresaRepository.findBySessaoWhatsapp(sessao);
        if (empresa == null) return ResponseEntity.notFound().build();

        // Atualiza apenas os campos estéticos/básicos
        if (dto.nome() != null) empresa.setNome(dto.nome());
        if (dto.mensagemSaudacao() != null) empresa.setMensagemSaudacao(dto.mensagemSaudacao());
        if (dto.tabelaDePrecos() != null) empresa.setTabelaDePrecos(dto.tabelaDePrecos());
        if (dto.linkGoogleMaps() != null) empresa.setLinkGoogleMaps(dto.linkGoogleMaps());
        if (dto.linkFotoPrincipal() != null) empresa.setLinkFotoPrincipal(dto.linkFotoPrincipal());
        if (dto.linkGaleria() != null) empresa.setLinkGaleria(dto.linkGaleria());

        empresaRepository.save(empresa);
        return ResponseEntity.ok("✅ Perfil do cliente atualizado com sucesso!");
    }

    // ========================================================================
    // 🚪 PORTA 2: PAINEL DO ADMIN (PUT) - Você, o dono do SaaS, usa essa
    // ========================================================================
    @PutMapping("/{sessao}/admin")
    @Operation(summary = "Atualização pelo Admin (SaaS)", description = "Altera infraestrutura, IA e módulos de pagamento.")
    public ResponseEntity<String> atualizarPeloAdmin(
            @RequestHeader(value = "x-admin-token", required = false) String token,
            @PathVariable String sessao,
            @RequestBody EmpresaUpdateAdminDTO dto) {

        if (isAcessoNegado(token)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ Acesso negado");

        Empresa empresa = empresaRepository.findBySessaoWhatsapp(sessao);
        if (empresa == null) return ResponseEntity.notFound().build();

        // Atualiza campos sensíveis de negócio
        if (dto.sessaoWhatsapp() != null) empresa.setSessaoWhatsapp(dto.sessaoWhatsapp());
        if (dto.locacaoPorHora() != null) empresa.setLocacaoPorHora(dto.locacaoPorHora());

        empresaRepository.save(empresa);
        return ResponseEntity.ok("✅ Infraestrutura da empresa atualizada com sucesso pelo Admin!");
    }
}