package com.gntfh.sirh_api.recrutement.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests pour EvaluationCandidatService.
 */
@Disabled("TODO")
class EvaluationCandidatServiceTest {

    @Test
    void non_intervenant_refuse() {
        // TODO REC-10 : seul un intervenant désigné peut évaluer
    }

    @Test
    void modification_apres_verrouillage_refusee() {
        // TODO RG-21 : évaluation verrouillée non modifiable
    }

    @Test
    void correction_cree_une_version() {
        // TODO RG-21 : correction crée une nouvelle évaluation tracée
    }
}
