package com.gntfh.sirh_api.demande.enums;

import java.util.Set;

public enum StatutDemande {
    BROUILLON,
    EN_VALIDATION_RH,
    EN_VALIDATION_FINANCE,
    EN_VALIDATION_DG,
    RETOURNEE,
    REJETEE,
    VALIDEE,
    EN_RECRUTEMENT,
    POURVUE,
    ANNULEE;

    public Set<StatutDemande> transitionsAutorisees() {
        return switch (this) {
            case BROUILLON -> Set.of(EN_VALIDATION_RH, ANNULEE);
            case EN_VALIDATION_RH -> Set.of(EN_VALIDATION_FINANCE, RETOURNEE, REJETEE);
            case EN_VALIDATION_FINANCE -> Set.of(EN_VALIDATION_DG, RETOURNEE, REJETEE);
            case EN_VALIDATION_DG -> Set.of(VALIDEE, RETOURNEE, REJETEE);
            case RETOURNEE -> Set.of(EN_VALIDATION_RH, ANNULEE);
            case VALIDEE -> Set.of(EN_RECRUTEMENT);
            case EN_RECRUTEMENT -> Set.of(POURVUE, ANNULEE);
            case REJETEE, POURVUE, ANNULEE -> Set.of();
        };
    }

    public boolean peutPasserA(StatutDemande cible) {
        return transitionsAutorisees().contains(cible);
    }

    public boolean estTerminal() {
        return this == REJETEE || this == POURVUE || this == ANNULEE;
    }

    public boolean estEnValidation() {
        return this == EN_VALIDATION_RH || this == EN_VALIDATION_FINANCE || this == EN_VALIDATION_DG;
    }
}
