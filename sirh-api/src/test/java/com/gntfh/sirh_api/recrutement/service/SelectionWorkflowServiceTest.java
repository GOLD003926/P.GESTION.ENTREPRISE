package com.gntfh.sirh_api.recrutement.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests pour SelectionWorkflowService.
 */
@Disabled("TODO")
class SelectionWorkflowServiceTest {

    @Test
    void niveau2_refuse_si_niveau1_non_favorable() {
        // TODO REC-06 : avis agrégé
    }

    @Test
    void niveau1_refuse_sans_preselection() {
        // TODO RG-19 : niveau 1 exige PRESELECTIONNEE
    }

    @Test
    void avis_agrege_trois_cas() {
        // TODO REC-06 : FAVORABLE, RESERVE, DEFAVORABLE
    }

    @Test
    void decision_finale_refusee_si_niveaux_incomplets() {
        // TODO RG-22 : tous les niveaux requis réalisés
    }

    @Test
    void mise_en_liste_attente_si_effectif_atteint() {
        // TODO REC-07 : LISTE_ATTENTE quand effectif atteint
    }
}
