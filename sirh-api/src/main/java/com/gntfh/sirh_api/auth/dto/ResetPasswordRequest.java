package com.gntfh.sirh_api.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Réinitialisation du mot de passe d'un utilisateur par l'administrateur. */
public record ResetPasswordRequest(

        @NotNull(message = "Le nouveau mot de passe est obligatoire")
        @Pattern(regexp = PasswordRules.REGEX, message = PasswordRules.MESSAGE)
        String nouveauMotDePasse
) {
}
