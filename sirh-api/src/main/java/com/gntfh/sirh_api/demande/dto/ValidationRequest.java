package com.gntfh.sirh_api.demande.dto;

import com.gntfh.sirh_api.demande.enums.DecisionValidation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ValidationRequest(
        @NotNull(message = "La décision est obligatoire")
        DecisionValidation decision,

        @Size(max = 2000, message = "Le commentaire ne peut pas dépasser 2000 caractères")
        String commentaire
) {
}
