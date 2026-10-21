package com.gntfh.sirh_api.onboarding.dto;

import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.onboarding.enums.StatutTache;

import java.time.Instant;
import java.time.LocalDate;

public record OnboardingTacheResponse(
        Long id,
        Long onboardingId,
        Long employeId,
        String employeNom,
        String matricule,
        String code,
        String libelle,
        boolean obligatoire,
        Role roleResponsable,
        Long assigneAId,
        LocalDate echeance,
        StatutTache statut,
        Instant dateRealisation,
        Long preuveDocumentId,
        String commentaire,
        boolean enRetard
) {
}
