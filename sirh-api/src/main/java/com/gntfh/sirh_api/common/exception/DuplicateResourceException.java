package com.gntfh.sirh_api.common.exception;

/** Doublon sur une valeur qui doit être unique (email, matricule…). Réponse HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException of(String ressource, String champ, Object valeur) {
        return new DuplicateResourceException("%s existe déjà avec %s « %s ».".formatted(ressource, champ, valeur));
    }
}
