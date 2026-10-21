package com.gntfh.sirh_api.embauche.entity;

import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import com.gntfh.sirh_api.embauche.enums.StatutProposition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PropositionEmbaucheTest {

    @Test
    void builder_valeursParDefaut_appliqueEssaiBrouillonEtDerogationFausse() {
        PropositionEmbauche proposition = PropositionEmbauche.builder().build();

        assertEquals(TypeContrat.ESSAI, proposition.getTypeContratPropose());
        assertEquals(StatutProposition.BROUILLON, proposition.getStatut());
        assertEquals(0, proposition.getNbProlongationsDelai());
        assertFalse(proposition.getDerogationRequise());
    }
}
