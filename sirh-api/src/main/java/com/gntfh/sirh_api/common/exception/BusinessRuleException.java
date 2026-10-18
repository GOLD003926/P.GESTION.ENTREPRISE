package com.gntfh.sirh_api.common.exception;

/**
 * Violation d'une règle de gestion (RG-xx du document de conception).
 * Réponse HTTP 422. Le message doit être compréhensible par l'utilisateur final.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
