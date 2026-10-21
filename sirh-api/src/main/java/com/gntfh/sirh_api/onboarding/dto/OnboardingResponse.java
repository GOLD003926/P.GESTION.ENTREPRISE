package com.gntfh.sirh_api.onboarding.dto;

import com.gntfh.sirh_api.onboarding.enums.StatutOnboarding;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record OnboardingResponse(
        Long id,
        Long employeId,
        String employeNom,
        String matricule,
        Long contratId,
        Long modeleId,
        Long responsableRhId,
        LocalDate dateDebut,
        LocalDate dateCible,
        StatutOnboarding statut,
        Instant dateCloture,
        int tachesObligatoiresFaites,
        int tachesObligatoiresTotal,
        List<OnboardingTacheResponse> taches
) {
}
