package com.gntfh.sirh_api.onboarding.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModeleOnboardingTest {

    @Test
    void builder_sansValeurActif_defautVrai() {
        assertTrue(ModeleOnboarding.builder().actif(null).build().getActif());
    }
}
