package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "modulos_empresa")
public class ModuloEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    // Ex: "VER_FOTOS", "PESQUISAR_DATA", "MENU_CARDAPIO"
    private String codigoAcao;

    // Ex: "📸 Ver fotos do espaço"
    private String textoMenu;

    // Ordem em que aparece no menu (1, 2, 3...)
    private Integer ordemExibicao;

    // Se o cliente parou de pagar esse módulo, você muda para false
    private Boolean ativo = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public String getCodigoAcao() {
        return codigoAcao;
    }

    public void setCodigoAcao(String codigoAcao) {
        this.codigoAcao = codigoAcao;
    }

    public String getTextoMenu() {
        return textoMenu;
    }

    public void setTextoMenu(String textoMenu) {
        this.textoMenu = textoMenu;
    }

    public Integer getOrdemExibicao() {
        return ordemExibicao;
    }

    public void setOrdemExibicao(Integer ordemExibicao) {
        this.ordemExibicao = ordemExibicao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
