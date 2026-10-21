package com.gntfh.sirh_api.contrat.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RenouvellementRequest(
        @NotNull(message = "La durée en mois est obligatoire")
        @Min(value = 1, message = "La durée en mois doit être strictement positive")
        Integer dureeMois,
        LocalDate dateDebut,
        @Positive(message = "Le salaire de base doit être strictement positif")
        BigDecimal salaireBase,
        @Size(max = 255, message = "Les horaires de travail ne doivent pas dépasser 255 caractères")
        String horairesTravail,
        @Size(max = 255, message = "Le lieu de travail ne doit pas dépasser 255 caractères")
        String lieuTravail
) {
}
