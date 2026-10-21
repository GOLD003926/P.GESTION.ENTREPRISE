package com.gntfh.sirh_api.onboarding.enums;

import java.util.Set;

public enum StatutOnboarding {
    A_DEMARRER,
    EN_COURS,
    TERMINE,
    ANNULE;

    public Set<StatutOnboarding> transitionsAutorisees() {
        return switch (this) {
            case A_DEMARRER -> Set.of(EN_COURS, ANNULE);
            case EN_COURS -> Set.of(TERMINE, ANNULE);
            case TERMINE, ANNULE -> Set.of();
        };
    }

    public boolean peutPasserA(StatutOnboarding cible) {
        return transitionsAutorisees().contains(cible);
    }

    public boolean estTerminal() {
        return this == TERMINE || this == ANNULE;
    }
}
