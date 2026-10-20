package com.gntfh.sirh_api.organisation.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de réponse pour un poste.
 */
public record PosteResponse(
        Long id,
        String code,
        String intitule,
        String description,
        String competencesRequises,
        Long departementId,
        String departementNom,
        Long grilleSalarialeId,
        String grilleSalarialeIntitule,
        BigDecimal salaireMin,
        BigDecimal salaireMax,
        Integer effectifAutorise,
        Integer occupationActuelle,
        Integer placesLibres,
        Boolean actif,
        Instant createdAt,
        Instant updatedAt
) {
}
