package com.gntfh.sirh_api.onboarding.dto;

import java.time.Instant;
import java.util.List;

public record ModeleOnboardingResponse(
        Long id,
        String nom,
        String typeContrat,
        boolean actif,
        List<ModeleTacheResponse> taches,
        Instant createdAt
) {
}
