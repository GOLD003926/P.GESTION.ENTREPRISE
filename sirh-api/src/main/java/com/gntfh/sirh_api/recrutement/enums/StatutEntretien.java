package com.gntfh.sirh_api.recrutement.enums;

import java.util.Set;

public enum StatutEntretien {
    PLANIFIE,
    REPORTE,
    REALISE,
    ANNULE,
    CANDIDAT_ABSENT;

    public Set<StatutEntretien> transitionsAutorisees() {
        return switch (this) {
            case PLANIFIE -> Set.of(REPORTE, REALISE, ANNULE, CANDIDAT_ABSENT);
            case REPORTE -> Set.of(PLANIFIE, REALISE, ANNULE, CANDIDAT_ABSENT);
            case REALISE, ANNULE, CANDIDAT_ABSENT -> Set.of();
        };
    }

    public boolean peutPasserA(StatutEntretien cible) {
        return transitionsAutorisees().contains(cible);
    }

    public boolean estTerminal() {
        return this == REALISE || this == ANNULE || this == CANDIDAT_ABSENT;
    }
}
