package com.gntfh.sirh_api.common.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;
import java.util.List;

/**
 * Format unique de toutes les erreurs renvoyées par l'API.
 *
 * @param code identifiant stable lisible par le front (ex. VALIDATION_ERROR, BAD_CREDENTIALS)
 * @param message texte lisible par l'utilisateur, en français
 * @param fieldErrors détail champ par champ (vide hors erreurs de validation)
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldError> fieldErrors
) {

    public record FieldError(String field, String message) {
    }

    public static ApiError of(HttpStatusCode status, String code, String message, String path) {
        return of(status, code, message, path, List.of());
    }

    public static ApiError of(HttpStatusCode status, String code, String message, String path,
                              List<FieldError> fieldErrors) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        String error = resolved != null ? resolved.getReasonPhrase() : "Erreur";
        return new ApiError(Instant.now(), status.value(), error, code, message, path, fieldErrors);
    }
}
