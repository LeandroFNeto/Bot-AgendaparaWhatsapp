package com.example.demo;

import jakarta.persistence.*;

@Entity
@Table(name = "horarios_reserva")
public class HorarioReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao; // Ex: "08:00 às 18:00 (Day Use)"
    private String status; // "Disponível" ou "Alugado"
    private String valor; // Ex: "R$ 400,00"

    // O Horário pertence a um Dia
    @ManyToOne
    @JoinColumn(name = "dia_id")
    private DiaReserva dia;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DiaReserva getDia() {
        return dia;
    }

    public void setDia(DiaReserva dia) {
        this.dia = dia;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}