package com.example.demo.model;


import jakarta.persistence.*;
        import java.util.List;

@Entity
@Table(name = "dias_reserva")

public class DiaReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String data; // Ex: 25-12-2026

    // O Dia pertence a uma Empresa
    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    // O Dia tem vários horários
    @OneToMany(mappedBy = "dia", cascade = CascadeType.ALL)
    private List<HorarioReserva> horariosDisponiveis;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public List<HorarioReserva> getHorariosDisponiveis() {
        return horariosDisponiveis;
    }

    public void setHorariosDisponiveis(List<HorarioReserva> horariosDisponiveis) {
        this.horariosDisponiveis = horariosDisponiveis;
    }
}