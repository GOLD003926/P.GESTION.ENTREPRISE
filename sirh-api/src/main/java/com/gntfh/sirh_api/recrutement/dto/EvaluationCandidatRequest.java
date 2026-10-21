package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de création d'une évaluation de candidat.
 */
public record EvaluationCandidatRequest(
        @NotNull(message = "L'identifiant de l'entretien est obligatoire")
        Long entretienId,

        @NotNull(message = "La note globale est obligatoire")
        @DecimalMin(value = "0.0", message = "La note globale doit être entre 0 et 20")
        @DecimalMax(value = "20.0", message = "La note globale doit être entre 0 et 20")
        BigDecimal noteGlobale,

        @Valid
        List<CritereNote> criteres,

        @NotNull(message = "L'avis est obligatoire")
        String avis,

        @Size(max = 4000, message = "Le commentaire ne peut pas dépasser 4000 caractères")
        String commentaire
) {
}
