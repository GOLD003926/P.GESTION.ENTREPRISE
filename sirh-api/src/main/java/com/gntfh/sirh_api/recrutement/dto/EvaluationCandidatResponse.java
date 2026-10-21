package com.gntfh.sirh_api.recrutement.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO de réponse pour une évaluation de candidat.
 */
public record EvaluationCandidatResponse(
        Long id,
        Long entretienId,
        Long evaluateurId,
        String evaluateurNom,
        BigDecimal noteGlobale,
        List<CritereNote> criteres,
        String avis,
        String commentaire,
        Instant dateEvaluation,
        Boolean verrouillee,
        Boolean active,
        Long evaluationPrecedenteId
) {
}
