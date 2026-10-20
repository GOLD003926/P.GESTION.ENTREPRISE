package com.gntfh.sirh_api.organisation.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * DTO de création d'un département.
 */
@Builder
public record DepartementRequest(

        @NotBlank(message = "Le code est obligatoire")
        @Pattern(regexp = "[A-Z0-9_-]+", message = "Le code doit contenir uniquement des lettres majuscules, chiffres, tirets et underscores")
        @Size(max = 30, message = "Le code ne peut pas dépasser 30 caractères")
        String code,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères")
        String nom,

        @Positive(message = "L'identifiant du département parent doit être positif")
        Long departementParentId,

        @Positive(message = "L'identifiant du responsable doit être positif")
        Long responsableId
) {
}
