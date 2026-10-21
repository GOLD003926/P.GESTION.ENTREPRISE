package com.gntfh.sirh_api.employe.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO pour la clôture d'une affectation.
 */
public record AffectationClotureRequest(
        @NotNull(message = "La date de fin est obligatoire")
        LocalDate dateFin
) {
}
