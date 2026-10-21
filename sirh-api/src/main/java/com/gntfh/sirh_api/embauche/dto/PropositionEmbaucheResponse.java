package com.gntfh.sirh_api.embauche.dto;

import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import com.gntfh.sirh_api.embauche.enums.StatutProposition;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PropositionEmbaucheResponse(
        Long id,
        Long candidatureId,
        String candidatNom,
        Long posteId,
        String posteIntitule,
        TypeContrat typeContratPropose,
        BigDecimal salairePropose,
        String devise,
        BigDecimal salaireMinGrille,
        BigDecimal salaireMaxGrille,
        String horairesTravail,
        String lieuTravail,
        LocalDate dateDebutPrevue,
        Integer dureeMois,
        LocalDate dateLimiteReponse,
        Integer nbProlongationsDelai,
        StatutProposition statut,
        Instant dateEnvoi,
        Instant dateReponse,
        String motifRefus,
        Boolean derogationRequise,
        Boolean derogationValidee,
        Long documentId,
        Long employeId,
        Instant createdAt
) {
}
