package com.example.demo.dto.empresa;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record EmpresaCreateDTO(
        @NotBlank @Schema(example = "Gamb") String nome,
        @NotBlank @Schema(example = "Agorá") String sessaoWhatsapp,
        @Schema(example = "solucoes tecnologicas") String ramoDeAtuacao,
        @Schema(example = "150.00") Double precoBase,
        @Schema(description = "Lista de nomes dos módulos", example = "[\"IA_GEMINI\", \"CALENDAR\"]")
        List<String> modulosIniciais
) {}