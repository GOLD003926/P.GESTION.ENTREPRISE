package com.gntfh.sirh_api.employe.enums;

import java.util.Set;

/**
 * Statut d'un employé.
 */
public enum StatutEmploye {
    PRE_EMBAUCHE,
    ACTIF,
    SUSPENDU,
    SORTI;

    /**
     * Retourne les statuts vers lesquels une transition est autorisée.
     */
    public Set<StatutEmploye> transitionsAutorisees() {
        return switch (this) {
            case PRE_EMBAUCHE -> Set.of(ACTIF, SORTI);
            case ACTIF -> Set.of(SUSPENDU, SORTI);
            case SUSPENDU -> Set.of(ACTIF, SORTI);
            case SORTI -> Set.of();
        };
    }

    /**
     * Vérifie si la transition vers le statut cible est autorisée.
     */
    public boolean peutPasserA(StatutEmploye cible) {
        return transitionsAutorisees().contains(cible);
    }

    /**
     * Indique si ce statut est terminal (aucune transition possible).
     */
    public boolean estTerminal() {
        return this == SORTI;
    }
}
