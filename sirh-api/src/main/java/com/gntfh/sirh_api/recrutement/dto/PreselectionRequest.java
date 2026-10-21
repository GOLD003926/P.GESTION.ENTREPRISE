package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la présélection sur dossier d'une candidature.
 */
public record PreselectionRequest(
        @NotNull(message = "Le champ accepter est obligatoire")
        Boolean accepter,

        @Min(value = 0, message = "Le score doit être entre 0 et 100")
        @Max(value = 100, message = "Le score doit être entre 0 et 100")
        Integer score,

        @Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères")
        String motif
) {
}
