package com.gntfh.sirh_api.organisation.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de réponse pour une grille salariale.
 */
public record GrilleSalarialeResponse(
        Long id,
        String code,
        String intitule,
        String niveau,
        BigDecimal salaireMin,
        BigDecimal salaireMax,
        String devise,
        Boolean actif,
        Instant createdAt,
        Instant updatedAt
) {
}
