package com.gntfh.sirh_api.onboarding.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatutOnboardingTest {

    @Test
    void transitionsRespectentLaSpecification() {
        assertTrue(StatutOnboarding.A_DEMARRER.peutPasserA(StatutOnboarding.EN_COURS));
        assertTrue(StatutOnboarding.A_DEMARRER.peutPasserA(StatutOnboarding.ANNULE));
        assertTrue(StatutOnboarding.EN_COURS.peutPasserA(StatutOnboarding.TERMINE));
        assertTrue(StatutOnboarding.EN_COURS.peutPasserA(StatutOnboarding.ANNULE));
        assertFalse(StatutOnboarding.TERMINE.peutPasserA(StatutOnboarding.EN_COURS));
        assertFalse(StatutOnboarding.ANNULE.peutPasserA(StatutOnboarding.EN_COURS));
    }
}
