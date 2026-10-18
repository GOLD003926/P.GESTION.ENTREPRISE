package com.gntfh.sirh_api.auth.dto;

import com.gntfh.sirh_api.auth.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Création d'un utilisateur par l'administrateur. */
public record UtilisateurRequest(

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
        String email,

        @NotNull(message = "Le mot de passe est obligatoire")
        @Pattern(regexp = PasswordRules.REGEX, message = PasswordRules.MESSAGE)
        String motDePasse,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
        String prenom,

        @NotNull(message = "Le rôle est obligatoire")
        Role role,

        /** Facultatif : employé lié à ce compte. */
        @Positive(message = "L'identifiant de l'employé doit être positif")
        Long employeId
) {
}
