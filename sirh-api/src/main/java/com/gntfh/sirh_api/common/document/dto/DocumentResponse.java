package com.gntfh.sirh_api.common.document.dto;

import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        TypeDocument type,
        String nomFichier,
        String typeMime,
        Long tailleOctets,
        Integer numeroVersion,
        TypeEntite entiteType,
        Long entiteId,
        boolean archive,
        Instant createdAt,
        String createdBy
) {
}
