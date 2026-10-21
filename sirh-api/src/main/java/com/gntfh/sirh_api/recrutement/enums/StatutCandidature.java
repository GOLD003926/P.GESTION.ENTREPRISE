package com.gntfh.sirh_api.recrutement.enums;

import java.util.Set;

public enum StatutCandidature {
    RECUE,
    PRESELECTIONNEE,
    REFUSEE_DOSSIER,
    EN_ENTRETIEN,
    NON_RETENUE,
    LISTE_ATTENTE,
    RETENUE,
    PROPOSITION_ENVOYEE,
    EMBAUCHEE,
    DESISTEE;

    public Set<StatutCandidature> transitionsAutorisees() {
        return switch (this) {
            case RECUE -> Set.of(PRESELECTIONNEE, REFUSEE_DOSSIER, DESISTEE);
            case PRESELECTIONNEE -> Set.of(EN_ENTRETIEN, DESISTEE);
            case EN_ENTRETIEN -> Set.of(EN_ENTRETIEN, NON_RETENUE, RETENUE, LISTE_ATTENTE, DESISTEE);
            case LISTE_ATTENTE -> Set.of(RETENUE, NON_RETENUE, DESISTEE);
            case RETENUE -> Set.of(PROPOSITION_ENVOYEE, NON_RETENUE);
            case PROPOSITION_ENVOYEE -> Set.of(EMBAUCHEE, NON_RETENUE, RETENUE);
            case REFUSEE_DOSSIER, NON_RETENUE, EMBAUCHEE, DESISTEE -> Set.of();
        };
    }

    public boolean peutPasserA(StatutCandidature cible) {
        return transitionsAutorisees().contains(cible);
    }

    public boolean estTerminal() {
        return this == REFUSEE_DOSSIER || this == NON_RETENUE || this == EMBAUCHEE || this == DESISTEE;
    }
}
