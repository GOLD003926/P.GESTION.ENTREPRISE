package com.gntfh.sirh_api.onboarding.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ModeleOnboardingRequest(
        @NotBlank(message = "Le nom du modèle est obligatoire")
        @Size(max = 100, message = "Le nom du modèle ne doit pas dépasser 100 caractères")
        String nom,
        @Pattern(regexp = "^(ESSAI|CDD|CDI)$", message = "Le type de contrat doit être ESSAI, CDD ou CDI")
        String typeContrat,
        @NotEmpty(message = "Un modèle doit contenir au moins une tâche")
        List<@Valid ModeleTacheRequest> taches
) {
}
