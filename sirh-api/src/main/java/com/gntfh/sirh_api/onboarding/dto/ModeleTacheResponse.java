package com.gntfh.sirh_api.onboarding.dto;

import com.gntfh.sirh_api.auth.enums.Role;

public record ModeleTacheResponse(
        Long id,
        Integer ordre,
        String code,
        String libelle,
        boolean obligatoire,
        Role roleResponsable,
        Integer echeanceJours
) {
}
