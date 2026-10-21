package com.gntfh.sirh_api.employe.dto;

import com.gntfh.sirh_api.employe.enums.MotifAffectation;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour la réponse d'une affectation.
 */
public record AffectationResponse(
        Long id,
        Long employeId,
        String employeNom,
        String matricule,
        Long posteId,
        String posteIntitule,
        String departementNom,
        Long contratId,
        Long managerId,
        String managerNom,
        LocalDate dateDebut,
        LocalDate dateFin,
        BigDecimal tauxOccupation,
        MotifAffectation motif,
        Boolean active
) {
}
