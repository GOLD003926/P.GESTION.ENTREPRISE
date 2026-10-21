package com.gntfh.sirh_api.recrutement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO de création/modification d'un candidat.
 */
public record CandidatRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
        String prenom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        String email,

        @Size(max = 30, message = "Le téléphone ne peut pas dépasser 30 caractères")
        String telephone,

        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateNaissance,

        String adresse,

        @Size(max = 60, message = "La nationalité ne peut pas dépasser 60 caractères")
        String nationalite,

        Boolean consentementDonnees
) {
}
