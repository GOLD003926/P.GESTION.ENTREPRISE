package com.gntfh.sirh_api.common.audit.dto;

import java.time.Instant;

public record HistoriqueStatutResponse(
        Long id,
        String entiteType,
        Long entiteId,
        String ancienStatut,
        String nouveauStatut,
        String auteur,
        Instant dateChangement,
        String commentaire
) {
}
