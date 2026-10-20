package com.gntfh.sirh_api.common.document.dto;

import org.springframework.core.io.Resource;

public record ContenuDocument(
        Resource contenu,
        String nomFichier,
        String typeMime
) {
}
