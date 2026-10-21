package com.gntfh.sirh_api.recrutement.dto;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO de réponse pour une offre d'emploi.
 */
public record OffreEmploiResponse(
        Long id,
        String reference,
        Long demandeId,
        String demandeReference,
        String posteIntitule,
        String titre,
        String description,
        String profilRecherche,
        Integer nombrePostes,
        Integer nombreNiveauxSelection,
        String canalPublication,
        LocalDate datePublication,
        LocalDate dateCloture,
        String statut,
        Long nombreCandidatures,
        Instant createdAt
) {
}
