package com.gntfh.sirh_api.onboarding.dto;

import jakarta.validation.constraints.Size;

public record OnboardingTacheRequest(
        @Size(max = 2000, message = "Le commentaire ne doit pas dépasser 2 000 caractères")
        String commentaire,
        Long preuveDocumentId,
        Long assigneAId
) {
}
