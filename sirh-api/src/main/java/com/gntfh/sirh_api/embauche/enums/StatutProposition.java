package com.gntfh.sirh_api.embauche.enums;

import java.util.Set;

public enum StatutProposition {
    BROUILLON,
    ENVOYEE,
    DELAI_ACCORDE,
    ACCEPTEE,
    REFUSEE,
    EXPIREE,
    RETIREE;

    public Set<StatutProposition> transitionsAutorisees() {
        return switch (this) {
            case BROUILLON -> Set.of(ENVOYEE, RETIREE);
            case ENVOYEE -> Set.of(ACCEPTEE, REFUSEE, DELAI_ACCORDE, EXPIREE, RETIREE);
            case DELAI_ACCORDE -> Set.of(ACCEPTEE, REFUSEE, EXPIREE, RETIREE);
            case ACCEPTEE, REFUSEE, EXPIREE, RETIREE -> Set.of();
        };
    }

    public boolean peutPasserA(StatutProposition cible) {
        return transitionsAutorisees().contains(cible);
    }

    public boolean estTerminal() {
        return this == ACCEPTEE || this == REFUSEE || this == EXPIREE || this == RETIREE;
    }

    public boolean estActive() {
        return this == BROUILLON || this == ENVOYEE || this == DELAI_ACCORDE;
    }
}
