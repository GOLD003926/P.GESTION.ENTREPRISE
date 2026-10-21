package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de création d'une candidature.
 * Exactement un de candidatId ou candidat doit être fourni.
 */
public record CandidatureRequest(
        @NotNull(message = "L'identifiant de l'offre est obligatoire")
        Long offreId,

        Long candidatId,

        @Valid
        CandidatRequest candidat,

        String lettreMotivation
) {
    /**
     * Vérifie que exactement un de candidatId ou candidat est fourni.
     * Cette validation est appliquée dans le service (RG-16).
     */
    public CandidatureRequest {
        // La validation est déléguée au service pour lever une BusinessRuleException
    }
}
