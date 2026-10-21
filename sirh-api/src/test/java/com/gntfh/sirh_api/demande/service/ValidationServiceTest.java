package com.gntfh.sirh_api.demande.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test de ValidationService.
 */
@Disabled("TODO : implémenter le service")
class ValidationServiceTest {

    @Test
    void enAttentePour_demandesEtapeCourante() {
        // TODO : demandes à l'étape courante du rôle de l'utilisateur
    }

    @Test
    void decider_approuvee_transitionStatutSuivant() {
        // TODO : APPROUVEE → transition au statut suivant
    }

    @Test
    void decider_rejetee_statutRejetee() {
        // TODO : REJETEE → statut REJETEE
    }

    @Test
    void decider_rejeteeSansCommentaire_refusee() {
        // TODO : RG-10 : commentaire obligatoire pour REJETEE
    }

    @Test
    void decider_retournee_statutRetournee() {
        // TODO : RETOURNEE → statut RETOURNEE
    }

    @Test
    void decider_retourneeSansCommentaire_refusee() {
        // TODO : RG-10 : commentaire obligatoire pour RETOURNEE
    }

    @Test
    void decider_validateurDemandeur_refusee() {
        // TODO : RG-09 : validateur ne peut pas être le demandeur
    }

    @Test
    void decider_nouveauCycle_apresRetour() {
        // TODO : RG-12 : retour crée un nouveau cycle
    }

    @Test
    void historiqueValidations_tousCyclesParOrdre() {
        // TODO : historique par cycle puis ordre
    }
}
