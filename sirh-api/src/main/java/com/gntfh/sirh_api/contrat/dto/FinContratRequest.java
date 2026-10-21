package com.gntfh.sirh_api.contrat.dto;

import com.gntfh.sirh_api.contrat.enums.MotifFinContrat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FinContratRequest(
        @NotNull(message = "Le motif de fin est obligatoire")
        MotifFinContrat motifFin,
        @NotNull(message = "La date de fin est obligatoire")
        LocalDate dateFin,
        @Size(max = 500, message = "Le commentaire ne doit pas dépasser 500 caractères")
        String commentaire
) {
}
