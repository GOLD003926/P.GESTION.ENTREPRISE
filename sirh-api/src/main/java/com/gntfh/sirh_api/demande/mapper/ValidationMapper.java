package com.gntfh.sirh_api.demande.mapper;

import com.gntfh.sirh_api.demande.dto.ValidationResponse;
import com.gntfh.sirh_api.demande.entity.Validation;
import org.springframework.stereotype.Component;

@Component
public class ValidationMapper {

    public ValidationResponse toResponse(Validation entity) {
        return new ValidationResponse(
                entity.getId(),
                entity.getDemande() != null ? entity.getDemande().getId() : null,
                entity.getCycle(),
                entity.getOrdre(),
                entity.getRoleValidateur(),
                entity.getValidateur() != null ? entity.getValidateur().getId() : null,
                entity.getValidateur() != null ? entity.getValidateur().getNom() + " " + entity.getValidateur().getPrenom() : null,
                entity.getDecision(),
                entity.getCommentaire(),
                entity.getDateDecision()
        );
    }
}
