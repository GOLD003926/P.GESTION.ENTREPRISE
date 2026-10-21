package com.gntfh.sirh_api.recrutement.dto;

import java.time.Instant;

/**
 * DTO de réponse pour un candidat.
 */
public record CandidatResponse(
        Long id,
        String nom,
        String prenom,
        String email,
        String telephone,
        String dateNaissance,
        String adresse,
        String nationalite,
        Boolean consentementDonnees,
        Instant createdAt
) {
}
