package com.gntfh.sirh_api.auth.dto;

import com.gntfh.sirh_api.auth.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Modification d'un utilisateur. Ni l'email (immuable), ni le mot de passe, ni l'état actif
 * ne se modifient ici : chacun a son propre endpoint.
 */
public record UtilisateurUpdateRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,

        @NotBlank(message = "Le prénom est obligatoire")
        @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
        String prenom,

        @NotNull(message = "Le rôle est obligatoire")
        Role role,

        @Positive(message = "L'identifiant de l'employé doit être positif")
        Long employeId
) {
}
