package com.gntfh.sirh_api.demande.dto;

import com.gntfh.sirh_api.demande.enums.MotifDemande;
import com.gntfh.sirh_api.demande.enums.RoleValidateur;
import com.gntfh.sirh_api.demande.enums.StatutDemande;
import com.gntfh.sirh_api.demande.enums.TypeDemande;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DemandeRecrutementResponse(
        Long id,
        String reference,
        Long posteId,
        String posteIntitule,
        Long departementId,
        String departementNom,
        Long demandeurId,
        String demandeurNom,
        TypeDemande typeDemande,
        Long employeRemplaceId,
        Integer effectifDemande,
        Integer effectifPourvu,
        MotifDemande motif,
        String justification,
        BigDecimal budgetEstime,
        LocalDate dateSouhaitee,
        StatutDemande statut,
        Integer cycleCourant,
        RoleValidateur etapeCourante,
        Instant createdAt,
        Instant updatedAt
) {
}
