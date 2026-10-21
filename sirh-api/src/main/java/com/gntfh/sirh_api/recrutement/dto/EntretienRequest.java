package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * DTO de création d'un entretien.
 */
public record EntretienRequest(
        @NotNull(message = "L'identifiant de la candidature est obligatoire")
        Long candidatureId,

        @NotNull(message = "Le niveau est obligatoire")
        @Min(value = 1, message = "Le niveau doit être entre 1 et 3")
        @Max(value = 3, message = "Le niveau doit être entre 1 et 3")
        Integer niveau,

        @NotNull(message = "La date et l'heure sont obligatoires")
        @Future(message = "La date et l'heure doivent être dans le futur")
        Instant dateHeure,

        @Min(value = 15, message = "La durée doit être au moins 15 minutes")
        @Max(value = 480, message = "La durée ne peut pas dépasser 480 minutes")
        Integer dureeMinutes,

        @Size(max = 255, message = "Le lieu ou le lien ne peut pas dépasser 255 caractères")
        String lieuOuLien,

        @Valid
        @NotNull(message = "Au moins un intervenant est obligatoire")
        List<IntervenantRequest> intervenants
) {
    public EntretienRequest {
        if (dureeMinutes == null) {
            dureeMinutes = 60;
        }
    }
}
