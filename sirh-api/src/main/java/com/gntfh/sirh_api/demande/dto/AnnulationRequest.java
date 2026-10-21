package com.gntfh.sirh_api.demande.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnulationRequest(
        @NotBlank(message = "Le motif d'annulation est obligatoire")
        @Size(max = 500, message = "Le motif d'annulation ne peut pas dépasser 500 caractères")
        String motif
) {
}
