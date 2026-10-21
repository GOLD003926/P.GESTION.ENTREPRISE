package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO de modification d'une offre d'emploi (uniquement en statut BROUILLON).
 */
public record OffreEmploiUpdateRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 150, message = "Le titre ne peut pas dépasser 150 caractères")
        String titre,

        @NotBlank(message = "La description est obligatoire")
        String description,

        String profilRecherche,

        @Min(value = 1, message = "Le nombre de postes doit être au moins 1")
        Integer nombrePostes,

        @Min(value = 1, message = "Le nombre de niveaux de sélection doit être au moins 1")
        @Max(value = 3, message = "Le nombre de niveaux de sélection ne peut pas dépasser 3")
        Integer nombreNiveauxSelection,

        @Size(max = 100, message = "Le canal de publication ne peut pas dépasser 100 caractères")
        String canalPublication,

        @Future(message = "La date de clôture doit être dans le futur")
        LocalDate dateCloture
) {
}
