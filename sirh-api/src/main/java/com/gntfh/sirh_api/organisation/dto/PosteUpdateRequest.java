package com.gntfh.sirh_api.organisation.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * DTO de modification d'un poste (sans le code qui est immuable).
 */
@Builder
public record PosteUpdateRequest(

        @NotBlank(message = "L'intitulé est obligatoire")
        @Size(max = 150, message = "L'intitulé ne peut pas dépasser 150 caractères")
        String intitule,

        @Size(max = 4000, message = "La description ne peut pas dépasser 4000 caractères")
        String description,

        @Size(max = 4000, message = "Les compétences requises ne peuvent pas dépasser 4000 caractères")
        String competencesRequises,

        @NotNull(message = "Le département est obligatoire")
        Long departementId,

        @NotNull(message = "La grille salariale est obligatoire")
        Long grilleSalarialeId,

        @NotNull(message = "L'effectif autorisé est obligatoire")
        @Min(value = 1, message = "L'effectif autorisé doit être d'au moins 1")
        @Max(value = 1000, message = "L'effectif autorisé ne peut pas dépasser 1000")
        Integer effectifAutorise
) {
}
