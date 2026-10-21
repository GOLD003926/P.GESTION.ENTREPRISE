package com.gntfh.sirh_api.onboarding.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatutTacheTest {

    @Test
    void transitionsRespectentLaSpecification() {
        assertTrue(StatutTache.A_FAIRE.peutPasserA(StatutTache.EN_COURS));
        assertTrue(StatutTache.A_FAIRE.peutPasserA(StatutTache.FAIT));
        assertTrue(StatutTache.A_FAIRE.peutPasserA(StatutTache.NON_APPLICABLE));
        assertTrue(StatutTache.EN_COURS.peutPasserA(StatutTache.FAIT));
        assertTrue(StatutTache.EN_COURS.peutPasserA(StatutTache.NON_APPLICABLE));
        assertFalse(StatutTache.FAIT.peutPasserA(StatutTache.EN_COURS));
        assertFalse(StatutTache.NON_APPLICABLE.peutPasserA(StatutTache.A_FAIRE));
    }
}
