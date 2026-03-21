package com.example.demo;

import com.example.demo.model.Empresa;
import com.example.demo.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class EmpresaRepositoryTest {

    @Autowired
    private EmpresaRepository repository;

    public void deveSalvarEBuscarEmpresaComSucesso() {
        // 1. PREPARAÇÃO (Arrange): Criamos uma empresa falsa na memória
        Empresa novaEmpresa = new Empresa();
        novaEmpresa.setNome("Cliente Teste SaaS");
        novaEmpresa.setSessaoWhatsapp("ClienteBot");
        novaEmpresa.setMensagemSaudacao("Bem-vindo ao Cliente Teste!");
        novaEmpresa.setRamoDeAtuacao("LOCACAO");

        // 2. AÇÃO (Act): Mandamos o Java salvar no PostgreSQL e depois buscar de volta
        repository.save(novaEmpresa);
        Empresa empresaEncontrada = repository.findBySessaoWhatsapp("ClienteBot");

        // 3. VERIFICAÇÃO (Assert): É aqui que o teste passa ou falha!
        // Verificamos se ele realmente achou algo (não é nulo) e se o nome bate com o que salvamos.
        assertNotNull(empresaEncontrada, "A empresa deveria ter sido encontrada no banco!");
        assertEquals("Cliente Teste SaaS", empresaEncontrada.getNome(), "O nome salvo está incorreto!");

        System.out.println("✅ Teste passou! A empresa " + empresaEncontrada.getNome() + " foi salva e lida no PostgreSQL.");
    }

}
