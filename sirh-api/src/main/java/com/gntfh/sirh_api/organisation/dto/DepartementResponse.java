package com.gntfh.sirh_api.organisation.dto;

import java.time.Instant;

/**
 * DTO de réponse pour un département.
 */
public record DepartementResponse(
        Long id,
        String code,
        String nom,
        Long departementParentId,
        String departementParentNom,
        Long responsableId,
        Boolean actif,
        Instant createdAt,
        Instant updatedAt
) {
}
