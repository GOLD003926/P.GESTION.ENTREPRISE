package com.gntfh.sirh_api.organisation.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

/**
 * DTO de modification d'un département (sans le code qui est immuable).
 */
@Builder
public record DepartementUpdateRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères")
        String nom,

        @Positive(message = "L'identifiant du responsable doit être positif")
        Long responsableId
) {
}
