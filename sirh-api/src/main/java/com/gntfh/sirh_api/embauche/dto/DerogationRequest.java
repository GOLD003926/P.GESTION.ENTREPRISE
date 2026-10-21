package com.gntfh.sirh_api.embauche.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DerogationRequest(
        @NotBlank(message = "Le commentaire de dérogation est obligatoire")
        @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
        String commentaire
) {
}
