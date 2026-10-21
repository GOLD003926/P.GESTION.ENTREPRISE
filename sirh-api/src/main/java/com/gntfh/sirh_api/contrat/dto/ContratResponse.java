package com.gntfh.sirh_api.contrat.dto;

import com.gntfh.sirh_api.contrat.enums.MotifFinContrat;
import com.gntfh.sirh_api.contrat.enums.StatutContrat;
import com.gntfh.sirh_api.contrat.enums.TypeContrat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ContratResponse(
        Long id,
        String numero,
        Long employeId,
        String employeNom,
        String matricule,
        TypeContrat type,
        Integer numeroRenouvellement,
        Long contratPrecedentId,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        LocalDate dateFinReelle,
        BigDecimal salaireBase,
        String devise,
        String horairesTravail,
        String lieuTravail,
        Long posteId,
        String posteIntitule,
        Long managerId,
        StatutContrat statut,
        MotifFinContrat motifFin,
        Long documentSigneId,
        LocalDate dateSignature,
        Integer joursRestants,
        Instant createdAt
) {
}
