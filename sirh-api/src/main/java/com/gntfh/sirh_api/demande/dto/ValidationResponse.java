package com.gntfh.sirh_api.demande.dto;

import com.gntfh.sirh_api.demande.enums.DecisionValidation;
import com.gntfh.sirh_api.demande.enums.RoleValidateur;

import java.time.Instant;

public record ValidationResponse(
        Long id,
        Long demandeId,
        Integer cycle,
        Integer ordre,
        RoleValidateur roleValidateur,
        Long validateurId,
        String validateurNom,
        DecisionValidation decision,
        String commentaire,
        Instant dateDecision
) {
}
