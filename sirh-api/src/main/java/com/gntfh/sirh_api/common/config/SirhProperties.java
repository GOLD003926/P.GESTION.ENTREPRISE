package com.gntfh.sirh_api.common.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tous les paramètres "sirh.*" de application.properties, typés et validés au démarrage.
 * Si un paramètre est absent ou invalide, l'application refuse de démarrer (fail fast).
 */
@Validated
@ConfigurationProperties(prefix = "sirh")
public record SirhProperties(
        @Valid @NotNull Storage storage,
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Cors cors,
        @Valid @NotNull Admin admin,
        @Valid @NotNull Demande demande,
        @Valid @NotNull Proposition proposition,
        @Valid @NotNull Contrat contrat,
        @Valid @NotNull Candidat candidat,
        @Valid @NotNull Employe employe
) {

    public record Storage(@NotBlank String location) {
    }

    public record Jwt(
            @NotBlank @Size(min = 32, message = "Le secret JWT doit faire au moins 32 caractères") String secret,
            @Positive int expirationMinutes
    ) {
    }

    public record Cors(@NotEmpty List<String> allowedOrigins) {
    }

    /** Administrateur créé au premier démarrage si aucun admin n'existe. */
    public record Admin(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank String nom,
            @NotBlank String prenom
    ) {
    }

    public record Demande(
            @Positive int delaiMinEmbaucheJours,
            @Positive int dureeBudgetMois,
            @NotNull @DecimalMin("0.0") BigDecimal coefCharges
    ) {
    }

    public record Proposition(
            @Positive int delaiReponseJours,
            @Positive int dureeProlongationJours
    ) {
    }

    public record Contrat(
            @Valid @NotNull Essai essai,
            @Valid @NotNull Cdd cdd,
            @Positive int alerteFinJours
    ) {
        public record Essai(@Positive int dureeMaxMois, @Positive int cumulMaxMois) {
        }

        public record Cdd(@Positive int dureeMaxMois) {
        }
    }

    public record Candidat(@Positive int conservationMois) {
    }

    public record Employe(@NotBlank String matriculePrefixe) {
    }
}
