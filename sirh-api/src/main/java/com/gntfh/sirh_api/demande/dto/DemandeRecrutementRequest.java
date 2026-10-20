package com.gntfh.sirh_api.demande.dto;

import com.gntfh.sirh_api.demande.enums.MotifDemande;
import com.gntfh.sirh_api.demande.enums.TypeDemande;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record DemandeRecrutementRequest(
        @NotNull(message = "Le poste est obligatoire")
        Long posteId,

        @NotNull(message = "Le type de demande est obligatoire")
        TypeDemande typeDemande,

        Long employeRemplaceId,

        @NotNull(message = "L'effectif demandé est obligatoire")
        @Min(value = 1, message = "L'effectif demandé doit être au moins 1")
        @Max(value = 50, message = "L'effectif demandé ne peut pas dépasser 50")
        Integer effectifDemande,

        @NotNull(message = "Le motif est obligatoire")
        MotifDemande motif,

        @Size(max = 2000, message = "La justification ne peut pas dépasser 2000 caractères")
        String justification,

        @NotNull(message = "La date souhaitée est obligatoire")
        @FutureOrPresent(message = "La date souhaitée doit être aujourd'hui ou dans le futur")
        LocalDate dateSouhaitee
) {
}
