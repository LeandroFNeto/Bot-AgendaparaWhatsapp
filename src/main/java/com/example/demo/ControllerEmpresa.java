package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/empresas")
public class ControllerEmpresa {

    @Autowired
    private EmpresaRepository empresaRepository;

    // 🔥 ROTA PARA SALVAR EMPRESA PELO POSTMAN (Fim do Hardcode!)
    @PostMapping
    public ResponseEntity<String> cadastrarEmpresa(@RequestBody Empresa novaEmpresa) {
        // 1. Procura se já existe a empresa pelo "DNA" (Sessão do WhatsApp)
        Empresa empresaExistente = empresaRepository.findBySessaoWhatsapp(novaEmpresa.getSessaoWhatsapp());

        if (empresaExistente != null) {
            // --- ATUALIZAÇÃO SEGURA ---

            // Só atualiza se o que veio no Postman não estiver vazio/nulo
            if (novaEmpresa.getNome() != null) empresaExistente.setNome(novaEmpresa.getNome());
            if (novaEmpresa.getMensagemSaudacao() != null) empresaExistente.setMensagemSaudacao(novaEmpresa.getMensagemSaudacao());
            if (novaEmpresa.getRamoDeAtuacao() != null) empresaExistente.setRamoDeAtuacao(novaEmpresa.getRamoDeAtuacao());
            if (novaEmpresa.getTabelaDePrecos() != null) empresaExistente.setTabelaDePrecos(novaEmpresa.getTabelaDePrecos());
            if (novaEmpresa.getLinkGoogleMaps() != null) empresaExistente.setLinkGoogleMaps(novaEmpresa.getLinkGoogleMaps());
            if (novaEmpresa.getLinkFotoPrincipal() != null) empresaExistente.setLinkFotoPrincipal(novaEmpresa.getLinkFotoPrincipal());
            if (novaEmpresa.getLinkGaleria()!= null) empresaExistente.setLinkGaleria(novaEmpresa.getLinkGaleria());

            // 🛑 A SESSÃO NÃO É ATUALIZADA AQUI!
            // Ela é a chave de ligação, então "travamos" ela para evitar que o bot perca a conexão.

            empresaRepository.save(empresaExistente);
            return ResponseEntity.ok("✅ Configurações de '" + empresaExistente.getNome() + "' atualizadas com sucesso!");
        }

        // --- NOVO CADASTRO ---

        // Validação básica: Não deixa criar empresa sem o básico
        if (novaEmpresa.getSessaoWhatsapp() == null || novaEmpresa.getNome() == null) {
            return ResponseEntity.badRequest().body("❌ Erro: Nome e Sessão são obrigatórios para novos cadastros!");
        }

        empresaRepository.save(novaEmpresa);
        return ResponseEntity.ok("✅ Nova empresa '" + novaEmpresa.getNome() + "' cadastrada e pronta para uso!");
    }
}
