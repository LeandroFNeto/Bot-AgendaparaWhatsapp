package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Disponibilidade de um dia no calendário de reservas da empresa")
public record DiaReservaDTO(
        @Schema(description = "Data no formato brasileiro", example = "25-12-2026")
        String data,

        @Schema(description = "Nome do dia da semana", example = "Sexta-feira")
        String diaDaSemana,

        @Schema(description = "Situação da data no calendário", example = "Disponível", allowableValues = {"Disponível", "Alugado"})
        String status
) {
}
