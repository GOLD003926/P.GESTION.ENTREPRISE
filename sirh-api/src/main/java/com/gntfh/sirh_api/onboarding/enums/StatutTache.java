package com.gntfh.sirh_api.onboarding.enums;

import java.util.Set;

public enum StatutTache {
    A_FAIRE,
    EN_COURS,
    FAIT,
    NON_APPLICABLE;

    public Set<StatutTache> transitionsAutorisees() {
        return switch (this) {
            case A_FAIRE -> Set.of(EN_COURS, FAIT, NON_APPLICABLE);
            case EN_COURS -> Set.of(FAIT, NON_APPLICABLE);
            case FAIT, NON_APPLICABLE -> Set.of();
        };
    }

    public boolean peutPasserA(StatutTache cible) {
        return transitionsAutorisees().contains(cible);
    }

    public boolean estTerminal() {
        return this == FAIT || this == NON_APPLICABLE;
    }
}
