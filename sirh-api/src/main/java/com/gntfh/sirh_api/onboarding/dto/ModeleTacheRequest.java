package com.gntfh.sirh_api.onboarding.dto;

import com.gntfh.sirh_api.auth.enums.Role;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

public record ModeleTacheRequest(
        @NotNull(message = "L'ordre de la tâche est obligatoire")
        @Min(value = 1, message = "L'ordre de la tâche doit être supérieur ou égal à 1")
        Integer ordre,
        @NotBlank(message = "Le code de la tâche est obligatoire")
        @Size(max = 40, message = "Le code ne doit pas dépasser 40 caractères")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "Le code doit contenir uniquement des majuscules, chiffres ou traits de soulignement")
        String code,
        @NotBlank(message = "Le libellé de la tâche est obligatoire")
        @Size(max = 200, message = "Le libellé ne doit pas dépasser 200 caractères")
        String libelle,
        @NotNull(message = "Le caractère obligatoire de la tâche doit être indiqué")
        Boolean obligatoire,
        @NotNull(message = "Le rôle responsable est obligatoire")
        Role roleResponsable,
        @NotNull(message = "L'échéance en jours est obligatoire")
        @Min(value = -60, message = "L'échéance doit être comprise entre -60 et 365 jours")
        @Max(value = 365, message = "L'échéance doit être comprise entre -60 et 365 jours")
        Integer echeanceJours
) {

    @AssertTrue(message = "Le rôle responsable doit être RH, IT_SERVICES, PAIE ou MANAGER")
    public boolean roleResponsableAutorise() {
        return roleResponsable == null
                || roleResponsable == Role.RH
                || roleResponsable == Role.IT_SERVICES
                || roleResponsable == Role.PAIE
                || roleResponsable == Role.MANAGER;
    }
}
