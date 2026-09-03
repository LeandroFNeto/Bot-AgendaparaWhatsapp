package com.example.demo.controller;

import com.example.demo.dto.empresa.EmpresaCreateDTO;
import com.example.demo.dto.empresa.EmpresaResponseDTO;
import com.example.demo.dto.empresa.EmpresaUpdateAdminDTO;
import com.example.demo.dto.empresa.EmpresaUpdateDTO;
import com.example.demo.model.Empresa;
import com.example.demo.repository.EmpresaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/empresas")
@Tag(name = "Gerenciamento de Empresas", description = "Cadastro e atualização de empresas do SaaS, isolados por intenção (criação, cliente e admin)")
public class ControllerEmpresa {

    @Autowired private EmpresaRepository empresaRepository;
    @Value("${admin.api.key}") private String adminApiKey;

    private boolean isAcessoNegado(String token) {
        return token == null || !token.equals(adminApiKey);
    }

    @PostMapping
    @Operation(summary = "Criar novo cliente SaaS", description = "Cadastra uma empresa a partir de EmpresaCreateDTO e devolve EmpresaResponseDTO, sem expor a entidade JPA.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empresa criada com sucesso",
                    content = @Content(schema = @Schema(implementation = EmpresaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Sessão WhatsApp já ocupada"),
            @ApiResponse(responseCode = "401", description = "Token administrativo inválido")
    })
    public ResponseEntity<EmpresaResponseDTO> cadastrar(
            @Parameter(description = "Token administrativo do SaaS", example = "sua-chave-admin")
            @RequestHeader(value = "x-admin-token", required = false) String token,
            @Valid @RequestBody EmpresaCreateDTO dto) {

        if (isAcessoNegado(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Inválido");
        }

        if (empresaRepository.findBySessaoWhatsapp(dto.sessaoWhatsapp()) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sessão já ocupada");
        }

        Empresa empresa = new Empresa();
        empresa.setNome(dto.nome());
        empresa.setSessaoWhatsapp(dto.sessaoWhatsapp());
        empresa.setRamoDeAtuacao(dto.ramoDeAtuacao());

        Empresa salva = empresaRepository.save(empresa);
        return ResponseEntity.status(HttpStatus.CREATED).body(paraResposta(salva));
    }

    @PutMapping("/{sessao}/cliente")
    @Operation(summary = "Atualizar perfil pelo cliente", description = "Aplica EmpresaUpdateDTO: textos, links e aparência. Não altera cobrança nem infraestrutura.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil do cliente atualizado",
                    content = @Content(schema = @Schema(implementation = EmpresaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada para a sessão informada")
    })
    public ResponseEntity<EmpresaResponseDTO> atualizarPeloCliente(
            @Parameter(description = "Token do painel do cliente")
            @RequestHeader(value = "x-cliente-token", required = false) String token,
            @Parameter(description = "Identificador da sessão WhatsApp da empresa", example = "sessao_recanto_01")
            @PathVariable String sessao,
            @Valid @RequestBody EmpresaUpdateDTO dto) {

        Empresa empresa = empresaRepository.findBySessaoWhatsapp(sessao);
        if (empresa == null) {
            return ResponseEntity.notFound().build();
        }

        if (dto.nome() != null) empresa.setNome(dto.nome());
        if (dto.mensagemSaudacao() != null) empresa.setMensagemSaudacao(dto.mensagemSaudacao());
        if (dto.tabelaDePrecos() != null) empresa.setTabelaDePrecos(dto.tabelaDePrecos());
        if (dto.linkGoogleMaps() != null) empresa.setLinkGoogleMaps(dto.linkGoogleMaps());
        if (dto.linkFotoPrincipal() != null) empresa.setLinkFotoPrincipal(dto.linkFotoPrincipal());
        if (dto.linkGaleria() != null) empresa.setLinkGaleria(dto.linkGaleria());

        return ResponseEntity.ok(paraResposta(empresaRepository.save(empresa)));
    }

    @PutMapping("/{sessao}/admin")
    @Operation(summary = "Atualizar infraestrutura pelo admin", description = "Aplica EmpresaUpdateAdminDTO: sessão, tipo de locação e módulos. Uso restrito ao administrador do SaaS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Infraestrutura atualizada",
                    content = @Content(schema = @Schema(implementation = EmpresaResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Token administrativo inválido"),
            @ApiResponse(responseCode = "404", description = "Empresa não encontrada para a sessão informada")
    })
    public ResponseEntity<EmpresaResponseDTO> atualizarPeloAdmin(
            @Parameter(description = "Token administrativo do SaaS", example = "sua-chave-admin")
            @RequestHeader(value = "x-admin-token", required = false) String token,
            @Parameter(description = "Identificador da sessão WhatsApp da empresa", example = "sessao_recanto_01")
            @PathVariable String sessao,
            @Valid @RequestBody EmpresaUpdateAdminDTO dto) {

        if (isAcessoNegado(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Acesso negado");
        }

        Empresa empresa = empresaRepository.findBySessaoWhatsapp(sessao);
        if (empresa == null) {
            return ResponseEntity.notFound().build();
        }

        if (dto.sessaoWhatsapp() != null) empresa.setSessaoWhatsapp(dto.sessaoWhatsapp());
        if (dto.locacaoPorHora() != null) empresa.setLocacaoPorHora(dto.locacaoPorHora());

        return ResponseEntity.ok(paraResposta(empresaRepository.save(empresa)));
    }

    private EmpresaResponseDTO paraResposta(Empresa empresa) {
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getNome(),
                empresa.getSessaoWhatsapp(),
                "DISCONNECTED",
                empresa.getLinkGoogleMaps(),
                LocalDateTime.now()
        );
    }
}
