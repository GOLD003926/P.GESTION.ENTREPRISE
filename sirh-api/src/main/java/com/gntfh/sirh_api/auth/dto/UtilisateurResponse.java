package com.gntfh.sirh_api.auth.dto;

import com.gntfh.sirh_api.auth.enums.Role;

import java.time.Instant;

/** Ce que l'API renvoie d'un utilisateur. Jamais le mot de passe, même haché. */
public record UtilisateurResponse(
        Long id,
        String email,
        String nom,
        String prenom,
        Role role,
        boolean actif,
        Long employeId,
        Instant createdAt
) {
}
