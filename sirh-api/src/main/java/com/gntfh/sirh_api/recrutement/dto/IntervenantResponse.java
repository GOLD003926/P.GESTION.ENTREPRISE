package com.gntfh.sirh_api.recrutement.dto;

/**
 * DTO de réponse pour un intervenant lors d'un entretien.
 */
public record IntervenantResponse(
        Long utilisateurId,
        String utilisateurNom,
        String roleIntervenant
) {
}
