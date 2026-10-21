package com.gntfh.sirh_api.employe.dto;

import com.gntfh.sirh_api.employe.enums.MotifAffectation;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour la création d'une affectation.
 */
public record AffectationRequest(
        @NotNull(message = "L'employé est obligatoire")
        Long employeId,

        @NotNull(message = "Le poste est obligatoire")
        Long posteId,

        Long managerId,

        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,

        @DecimalMin(value = "0.01", message = "Le taux d'occupation doit être supérieur à 0")
        @DecimalMax(value = "100.00", message = "Le taux d'occupation ne peut pas dépasser 100")
        BigDecimal tauxOccupation,

        @NotNull(message = "Le motif est obligatoire")
        MotifAffectation motif,

        Boolean cloturerActives
) {
}
