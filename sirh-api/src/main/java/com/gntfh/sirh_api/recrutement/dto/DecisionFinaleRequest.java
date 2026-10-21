package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour la décision finale sur une candidature.
 */
public record DecisionFinaleRequest(
        @NotNull(message = "La décision est obligatoire")
        String decision,

        String motif
) {
}
