package com.gntfh.sirh_api.common.audit.dto;

import java.time.Instant;

public record JournalAuditResponse(
        Long id,
        String entiteType,
        Long entiteId,
        String action,
        String valeursAvant,
        String valeursApres,
        String auteur,
        Instant dateAction
) {
}
