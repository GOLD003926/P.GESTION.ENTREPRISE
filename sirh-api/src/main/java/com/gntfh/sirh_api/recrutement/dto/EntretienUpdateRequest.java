package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * DTO de modification d'un entretien (reprogrammation).
 */
public record EntretienUpdateRequest(
        @Future(message = "La date et l'heure doivent être dans le futur")
        Instant dateHeure,

        @Min(value = 15, message = "La durée doit être au moins 15 minutes")
        @Max(value = 480, message = "La durée ne peut pas dépasser 480 minutes")
        Integer dureeMinutes,

        @Size(max = 255, message = "Le lieu ou le lien ne peut pas dépasser 255 caractères")
        String lieuOuLien,

        @Valid
        List<IntervenantRequest> intervenants
) {
}
