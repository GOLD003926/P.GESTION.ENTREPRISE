package com.gntfh.sirh_api.auth.dto;

/** Politique de mot de passe, définie une seule fois et réutilisée par tous les DTO concernés. */
public final class PasswordRules {

    /** 8 à 72 caractères (limite de BCrypt), au moins une minuscule, une majuscule et un chiffre. */
    public static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,72}$";

    public static final String MESSAGE =
            "Le mot de passe doit faire de 8 à 72 caractères avec au moins une majuscule, une minuscule et un chiffre";

    private PasswordRules() {
    }
}
