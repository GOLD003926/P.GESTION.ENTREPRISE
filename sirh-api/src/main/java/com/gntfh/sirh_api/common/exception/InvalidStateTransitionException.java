package com.gntfh.sirh_api.common.exception;

/**
 * Transition de statut interdite par la machine à états
 * (ex. valider une demande déjà rejetée). Réponse HTTP 422.
 */
public class InvalidStateTransitionException extends BusinessRuleException {

    public InvalidStateTransitionException(String entite, Object statutActuel, Object statutCible) {
        super("Transition impossible pour %s : de %s vers %s.".formatted(entite, statutActuel, statutCible));
    }
}
