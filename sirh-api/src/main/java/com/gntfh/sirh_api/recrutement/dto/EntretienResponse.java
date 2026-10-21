package com.gntfh.sirh_api.recrutement.dto;

import java.time.Instant;
import java.util.List;

/**
 * DTO de réponse pour un entretien.
 */
public record EntretienResponse(
        Long id,
        Long candidatureId,
        String candidatNom,
        Integer niveau,
        String type,
        Instant dateHeure,
        Integer dureeMinutes,
        String lieuOuLien,
        String statut,
        String motifAnnulation,
        List<IntervenantResponse> intervenants,
        String avisNiveau
) {
}
