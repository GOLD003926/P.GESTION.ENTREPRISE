package com.gntfh.sirh_api.demande.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test de DemandeWorkflowService.
 */
@Disabled("TODO : implémenter le service")
class DemandeWorkflowServiceTest {

    @Test
    void transitionner_transitionAutorisee_acceptee() {
        // TODO : transition BROUILLON → EN_VALIDATION_RH
    }

    @Test
    void transitionner_transitionInterdite_refusee() {
        // TODO : transition non autorisée → InvalidStateTransitionException
    }

    @Test
    void creerValidationsDuCycle_troisValidationsCreees() {
        // TODO : création des validations RH, Finance, DG
    }

    @Test
    void marquerEnRecrutement_valideeVersEnRecrutement() {
        // TODO : VALIDEE → EN_RECRUTEMENT
    }

    @Test
    void enregistrerEmbauche_incrementeEffectifPourvu() {
        // TODO : incrémentation de effectifPourvu
    }

    @Test
    void enregistrerEmbauche_complet_pourvue() {
        // TODO : effectifPourvu == effectifDemande → POURVUE (RG-24)
    }

    @Test
    void effectifRestantAPourvoir_calculCorrect() {
        // TODO : retourner effectifDemande - effectifPourvu
    }
}
