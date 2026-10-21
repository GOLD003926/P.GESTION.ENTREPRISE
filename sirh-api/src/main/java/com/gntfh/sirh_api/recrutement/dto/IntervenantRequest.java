package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour un intervenant lors d'un entretien.
 */
public record IntervenantRequest(
        @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
        Long utilisateurId,

        @Size(max = 50, message = "Le rôle d'intervenant ne peut pas dépasser 50 caractères")
        String roleIntervenant
) {
}
