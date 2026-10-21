package com.gntfh.sirh_api.recrutement.enums;

import java.util.Set;

public enum StatutOffre {
    BROUILLON,
    PUBLIEE,
    CLOTUREE,
    ANNULEE;

    public Set<StatutOffre> transitionsAutorisees() {
        return switch (this) {
            case BROUILLON -> Set.of(PUBLIEE, ANNULEE);
            case PUBLIEE -> Set.of(CLOTUREE, ANNULEE);
            case CLOTUREE, ANNULEE -> Set.of();
        };
    }

    public boolean peutPasserA(StatutOffre cible) {
        return transitionsAutorisees().contains(cible);
    }

    public boolean estTerminal() {
        return this == CLOTUREE || this == ANNULEE;
    }
}
