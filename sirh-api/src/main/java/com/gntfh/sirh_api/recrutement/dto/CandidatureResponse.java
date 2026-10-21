package com.gntfh.sirh_api.recrutement.dto;

import java.time.Instant;

/**
 * DTO de réponse pour une candidature.
 */
public record CandidatureResponse(
        Long id,
        Long candidatId,
        String candidatNom,
        String candidatEmail,
        Long offreId,
        String offreReference,
        String offreTitre,
        Long cvDocumentId,
        String lettreMotivation,
        Instant dateDepot,
        String statut,
        Integer scorePreselection,
        String motifRefus,
        String decisionFinale,
        Integer rangListeAttente,
        Long decideurId,
        Instant dateDecision,
        String motifDecision,
        Integer prochainNiveau
) {
}
