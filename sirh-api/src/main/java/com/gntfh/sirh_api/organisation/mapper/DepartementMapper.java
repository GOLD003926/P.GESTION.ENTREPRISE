package com.gntfh.sirh_api.organisation.mapper;

import com.gntfh.sirh_api.organisation.dto.DepartementRequest;
import com.gntfh.sirh_api.organisation.dto.DepartementResponse;
import com.gntfh.sirh_api.organisation.dto.DepartementUpdateRequest;
import com.gntfh.sirh_api.organisation.entity.Departement;
import org.springframework.stereotype.Component;

/**
 * Mapper pour l'entité Departement.
 */
@Component
public class DepartementMapper {

    public Departement toEntity(DepartementRequest request) {
        return Departement.builder()
                .code(request.code().toUpperCase())
                .nom(request.nom())
                .responsableId(request.responsableId())
                .actif(true)
                .build();
    }

    public DepartementResponse toResponse(Departement entity) {
        String parentNom = entity.getDepartementParent() != null ? entity.getDepartementParent().getNom() : null;
        Long parentId = entity.getDepartementParent() != null ? entity.getDepartementParent().getId() : null;
        return new DepartementResponse(
                entity.getId(),
                entity.getCode(),
                entity.getNom(),
                parentId,
                parentNom,
                entity.getResponsableId(),
                entity.getActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateEntity(DepartementUpdateRequest request, Departement entity) {
        entity.setNom(request.nom());
        entity.setResponsableId(request.responsableId());
    }
}
