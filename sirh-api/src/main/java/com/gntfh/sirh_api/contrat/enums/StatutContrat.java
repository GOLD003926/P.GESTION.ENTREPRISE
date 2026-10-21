package com.gntfh.sirh_api.contrat.enums;

import java.util.Set;

/**
 * Statuts du cycle de vie d'un contrat.
 */
public enum StatutContrat {
    BROUILLON,
    A_SIGNER,
    ACTIF,
    TERMINE,
    ANNULE;

    /**
     * Transitions autorisées depuis le statut courant.
     */
    public Set<StatutContrat> transitionsAutorisees() {
        return switch (this) {
            case BROUILLON -> Set.of(A_SIGNER, ANNULE);
            case A_SIGNER -> Set.of(ACTIF, BROUILLON, ANNULE);
            case ACTIF -> Set.of(TERMINE);
            case TERMINE, ANNULE -> Set.of();
        };
    }

    /**
     * Vérifie si une transition est autorisée.
     */
    public boolean peutPasserA(StatutContrat cible) {
        return transitionsAutorisees().contains(cible);
    }

    /**
     * Vérifie si le statut est terminal.
     */
    public boolean estTerminal() {
        return this == TERMINE || this == ANNULE;
    }
}
