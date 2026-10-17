package com.gntfh.sirh_api.auth.dto;

import java.time.Instant;

/**
 * Réponse de connexion : le jeton, son expiration, et l'utilisateur
 * (évite au front un second appel juste pour connaître le rôle).
 */
public record TokenResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UtilisateurResponse utilisateur
) {
}
