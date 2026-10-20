package com.gntfh.sirh_api.organisation.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO de modification d'une grille salariale (sans le code qui est immuable).
 */
@Builder
public record GrilleSalarialeUpdateRequest(

        @NotBlank(message = "L'intitulé est obligatoire")
        @Size(max = 100, message = "L'intitulé ne peut pas dépasser 100 caractères")
        String intitule,

        @NotBlank(message = "Le niveau est obligatoire")
        @Size(max = 30, message = "Le niveau ne peut pas dépasser 30 caractères")
        String niveau,

        @NotNull(message = "Le salaire minimum est obligatoire")
        @DecimalMin(value = "0.01", message = "Le salaire minimum doit être supérieur à 0")
        BigDecimal salaireMin,

        @NotNull(message = "Le salaire maximum est obligatoire")
        @DecimalMin(value = "0.01", message = "Le salaire maximum doit être supérieur à 0")
        BigDecimal salaireMax,

        @Size(max = 3, message = "La devise ne peut pas dépasser 3 caractères")
        String devise
) {
}
