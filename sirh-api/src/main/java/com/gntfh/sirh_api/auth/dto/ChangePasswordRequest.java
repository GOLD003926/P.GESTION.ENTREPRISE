package com.gntfh.sirh_api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Changement de son propre mot de passe. */
public record ChangePasswordRequest(

        @NotBlank(message = "Le mot de passe actuel est obligatoire")
        String motDePasseActuel,

        @NotNull(message = "Le nouveau mot de passe est obligatoire")
        @Pattern(regexp = PasswordRules.REGEX, message = PasswordRules.MESSAGE)
        String nouveauMotDePasse
) {
}
