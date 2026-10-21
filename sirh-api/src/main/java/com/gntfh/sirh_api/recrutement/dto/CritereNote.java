package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour un critère et sa note dans la grille d'évaluation.
 */
public record CritereNote(
        @NotBlank(message = "Le critère est obligatoire")
        @Size(max = 100, message = "Le critère ne peut pas dépasser 100 caractères")
        String critere,

        @NotNull(message = "La note est obligatoire")
        @Min(value = 0, message = "La note doit être entre 0 et 20")
        @Max(value = 20, message = "La note doit être entre 0 et 20")
        Double note
) {
}
