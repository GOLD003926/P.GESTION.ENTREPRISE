package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour l'annulation d'un entretien.
 */
public record AnnulationEntretienRequest(
        @NotBlank(message = "Le motif d'annulation est obligatoire")
        @Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères")
        String motif
) {
}
