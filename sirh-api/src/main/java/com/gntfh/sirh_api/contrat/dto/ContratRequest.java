package com.gntfh.sirh_api.contrat.dto;

import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ContratRequest(
        @NotNull(message = "L'identifiant de l'employé est obligatoire")
        Long employeId,
        @NotNull(message = "Le type de contrat est obligatoire")
        TypeContrat type,
        @NotNull(message = "L'identifiant du poste est obligatoire")
        Long posteId,
        Long managerId,
        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,
        @Min(value = 1, message = "La durée en mois doit être comprise entre 1 et 60")
        @Max(value = 60, message = "La durée en mois doit être comprise entre 1 et 60")
        Integer dureeMois,
        @NotNull(message = "Le salaire de base est obligatoire")
        @Positive(message = "Le salaire de base doit être strictement positif")
        BigDecimal salaireBase,
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "La devise doit contenir exactement 3 lettres")
        String devise,
        @NotBlank(message = "Les horaires de travail sont obligatoires")
        @Size(max = 255, message = "Les horaires de travail ne doivent pas dépasser 255 caractères")
        String horairesTravail,
        @NotBlank(message = "Le lieu de travail est obligatoire")
        @Size(max = 255, message = "Le lieu de travail ne doit pas dépasser 255 caractères")
        String lieuTravail
) {
    public ContratRequest {
        if (type == null) {
            type = TypeContrat.ESSAI;
        }
    }
}
