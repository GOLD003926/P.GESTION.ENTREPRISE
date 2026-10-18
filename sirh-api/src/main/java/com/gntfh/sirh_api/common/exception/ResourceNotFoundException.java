package com.gntfh.sirh_api.common.exception;

/** Ressource introuvable. Réponse HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String ressource, Object identifiant) {
        return new ResourceNotFoundException("%s introuvable (identifiant : %s).".formatted(ressource, identifiant));
    }
}
