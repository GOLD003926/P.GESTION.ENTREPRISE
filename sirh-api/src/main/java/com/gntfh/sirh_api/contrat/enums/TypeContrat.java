package com.gntfh.sirh_api.contrat.enums;

/**
 * Types de contrat gérés par le SIRH.
 */
public enum TypeContrat {
    ESSAI,
    CDD,
    CDI;

    /**
     * Indique si le type de contrat impose une date de fin prévue.
     */
    public boolean aDateFin() {
        return this == ESSAI || this == CDD;
    }
}
