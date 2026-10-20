package com.gntfh.sirh_api.organisation.mapper;

import com.gntfh.sirh_api.organisation.dto.GrilleSalarialeRequest;
import com.gntfh.sirh_api.organisation.dto.GrilleSalarialeResponse;
import com.gntfh.sirh_api.organisation.dto.GrilleSalarialeUpdateRequest;
import com.gntfh.sirh_api.organisation.entity.GrilleSalariale;
import org.springframework.stereotype.Component;

/**
 * Mapper pour l'entité GrilleSalariale.
 */
@Component
public class GrilleSalarialeMapper {

    public GrilleSalariale toEntity(GrilleSalarialeRequest request) {
        return GrilleSalariale.builder()
                .code(request.code().toUpperCase())
                .intitule(request.intitule())
                .niveau(request.niveau())
                .salaireMin(request.salaireMin())
                .salaireMax(request.salaireMax())
                .devise(request.devise() != null ? request.devise() : "MGA")
                .actif(true)
                .build();
    }

    public GrilleSalarialeResponse toResponse(GrilleSalariale entity) {
        return new GrilleSalarialeResponse(
                entity.getId(),
                entity.getCode(),
                entity.getIntitule(),
                entity.getNiveau(),
                entity.getSalaireMin(),
                entity.getSalaireMax(),
                entity.getDevise(),
                entity.getActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateEntity(GrilleSalarialeUpdateRequest request, GrilleSalariale entity) {
        entity.setIntitule(request.intitule());
        entity.setNiveau(request.niveau());
        entity.setSalaireMin(request.salaireMin());
        entity.setSalaireMax(request.salaireMax());
        if (request.devise() != null) {
            entity.setDevise(request.devise());
        }
    }
}
