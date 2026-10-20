package com.gntfh.sirh_api.organisation.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests pour PosteService.
 */
@Disabled("TODO")
class PosteServiceTest {

    @Test
    void creer_codeDuplique_refuse() {
        // TODO ORG-01 : code déjà existant
    }

    @Test
    void modifier_effectifSousOccupation_refuse() {
        // TODO ORG-05 : effectifAutorise < occupationActuelle
    }

    @Test
    void desactiver_posteOccupe_refuse() {
        // TODO ORG-04 : occupationActuelle > 0
    }

    @Test
    void creer_posteSurGrilleInactive_refuse() {
        // TODO ORG-06 : grille salariale inactive
    }
}
