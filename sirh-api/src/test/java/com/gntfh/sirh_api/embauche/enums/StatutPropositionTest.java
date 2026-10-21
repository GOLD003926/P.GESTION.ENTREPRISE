package com.gntfh.sirh_api.embauche.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatutPropositionTest {

    @Test
    void transitionsAutorisees_respectentLeCycleDeVie() {
        assertTrue(StatutProposition.BROUILLON.peutPasserA(StatutProposition.ENVOYEE));
        assertTrue(StatutProposition.ENVOYEE.peutPasserA(StatutProposition.DELAI_ACCORDE));
        assertTrue(StatutProposition.DELAI_ACCORDE.peutPasserA(StatutProposition.ACCEPTEE));
        assertFalse(StatutProposition.ACCEPTEE.peutPasserA(StatutProposition.RETIREE));
        assertFalse(StatutProposition.BROUILLON.estTerminal());
        assertTrue(StatutProposition.EXPIREE.estTerminal());
    }

    @Test
    void estActive_identifieUniquementLesStatutsNonTerminaux() {
        assertTrue(StatutProposition.BROUILLON.estActive());
        assertTrue(StatutProposition.ENVOYEE.estActive());
        assertTrue(StatutProposition.DELAI_ACCORDE.estActive());
        assertFalse(StatutProposition.RETIREE.estActive());
    }
}
