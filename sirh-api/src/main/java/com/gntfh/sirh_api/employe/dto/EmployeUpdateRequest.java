package com.gntfh.sirh_api.employe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO pour la modification d'un employé.
 */
public record EmployeUpdateRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
        String prenom,

        @Email(message = "L'email professionnel doit être valide")
        String emailPro,

        @Email(message = "L'email personnel doit être valide")
        String emailPerso,

        @Size(max = 30, message = "Le téléphone ne peut pas dépasser 30 caractères")
        String telephone,

        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateNaissance,

        @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
        String adresse,

        @Size(max = 30, message = "Le numéro de sécurité sociale ne peut pas dépasser 30 caractères")
        String numeroSecuriteSociale
) {
}
