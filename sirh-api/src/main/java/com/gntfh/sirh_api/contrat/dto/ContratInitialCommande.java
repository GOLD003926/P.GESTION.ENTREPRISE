package com.gntfh.sirh_api.contrat.dto;

import com.gntfh.sirh_api.contrat.enums.TypeContrat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Commande interne pour créer le contrat initial depuis le module embauche.
 */
public record ContratInitialCommande(
        Long employeId,
        Long posteId,
        Long managerId,
        TypeContrat type,
        LocalDate dateDebut,
        Integer dureeMois,
        BigDecimal salaireBase,
        String devise,
        String horairesTravail,
        String lieuTravail
) {
    public ContratInitialCommande {
        if (type == null) {
            type = TypeContrat.ESSAI;
        }
    }
}
