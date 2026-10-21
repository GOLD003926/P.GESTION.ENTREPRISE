package com.gntfh.sirh_api.embauche.dto;

import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PropositionEmbaucheUpdateRequest(
        @NotNull(message = "Le type de contrat proposé est obligatoire")
        TypeContrat typeContratPropose,
        @NotNull(message = "Le salaire proposé est obligatoire")
        @DecimalMin(value = "0.0", inclusive = false, message = "Le salaire proposé doit être supérieur à zéro")
        BigDecimal salairePropose,
        @NotBlank(message = "La devise est obligatoire")
        @Pattern(regexp = "^[A-Z]{3}$", message = "La devise doit comporter trois lettres majuscules")
        String devise,
        @NotBlank(message = "Les horaires de travail sont obligatoires")
        @Size(max = 255, message = "Les horaires de travail ne doivent pas dépasser 255 caractères")
        String horairesTravail,
        @NotBlank(message = "Le lieu de travail est obligatoire")
        @Size(max = 255, message = "Le lieu de travail ne doit pas dépasser 255 caractères")
        String lieuTravail,
        @NotNull(message = "La date de début prévue est obligatoire")
        @FutureOrPresent(message = "La date de début prévue doit être aujourd'hui ou dans le futur")
        LocalDate dateDebutPrevue,
        @Min(value = 1, message = "La durée doit être comprise entre 1 et 60 mois")
        @Max(value = 60, message = "La durée doit être comprise entre 1 et 60 mois")
        Integer dureeMois,
        @Future(message = "La date limite de réponse doit être dans le futur")
        LocalDate dateLimiteReponse
) {
    public PropositionEmbaucheUpdateRequest {
        if (typeContratPropose == null) {
            typeContratPropose = TypeContrat.ESSAI;
        }
        if (devise == null) {
            devise = "MGA";
        }
    }

    @AssertTrue(message = "La durée est obligatoire pour un contrat d'essai ou un CDD, et interdite pour un CDI")
    public boolean dureeMoisCoherente() {
        return typeContratPropose == null
                || (typeContratPropose == TypeContrat.CDI
                ? dureeMois == null
                : dureeMois != null && dureeMois >= 1 && dureeMois <= 60);
    }
}
