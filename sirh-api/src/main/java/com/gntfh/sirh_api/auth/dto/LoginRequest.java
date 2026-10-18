package com.gntfh.sirh_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Ce que le client envoie pour se connecter. */
public record LoginRequest(

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email n'est pas valide")
        @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(max = 72, message = "Le mot de passe ne peut pas dépasser 72 caractères") // limite de BCrypt
        String motDePasse
) {
}
