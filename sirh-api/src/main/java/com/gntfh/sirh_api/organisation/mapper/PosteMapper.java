package com.gntfh.sirh_api.organisation.mapper;

import com.gntfh.sirh_api.organisation.dto.PosteRequest;
import com.gntfh.sirh_api.organisation.dto.PosteResponse;
import com.gntfh.sirh_api.organisation.dto.PosteUpdateRequest;
import com.gntfh.sirh_api.organisation.entity.Poste;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mapper pour l'entité Poste.
 * Note : les relations departement et grilleSalariale sont gérées par le service.
 */
@Component
public class PosteMapper {

    public Poste toEntity(PosteRequest request) {
        return Poste.builder()
                .code(request.code().toUpperCase())
                .intitule(request.intitule())
                .description(request.description())
                .competencesRequises(request.competencesRequises())
                .effectifAutorise(request.effectifAutorise())
                .actif(true)
                .build();
    }

    public PosteResponse toResponse(Poste entity, Integer occupationActuelle) {
        String departementNom = entity.getDepartement() != null ? entity.getDepartement().getNom() : null;
        Long departementId = entity.getDepartement() != null ? entity.getDepartement().getId() : null;
        String grilleIntitule = entity.getGrilleSalariale() != null ? entity.getGrilleSalariale().getIntitule() : null;
        Long grilleId = entity.getGrilleSalariale() != null ? entity.getGrilleSalariale().getId() : null;
        BigDecimal salaireMin = entity.getGrilleSalariale() != null ? entity.getGrilleSalariale().getSalaireMin() : null;
        BigDecimal salaireMax = entity.getGrilleSalariale() != null ? entity.getGrilleSalariale().getSalaireMax() : null;
        Integer placesLibres = entity.getEffectifAutorise() - (occupationActuelle != null ? occupationActuelle : 0);

        return new PosteResponse(
                entity.getId(),
                entity.getCode(),
                entity.getIntitule(),
                entity.getDescription(),
                entity.getCompetencesRequises(),
                departementId,
                departementNom,
                grilleId,
                grilleIntitule,
                salaireMin,
                salaireMax,
                entity.getEffectifAutorise(),
                occupationActuelle,
                placesLibres,
                entity.getActif(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public void updateEntity(PosteUpdateRequest request, Poste entity) {
        entity.setIntitule(request.intitule());
        entity.setDescription(request.description());
        entity.setCompetencesRequises(request.competencesRequises());
        entity.setEffectifAutorise(request.effectifAutorise());
    }
}
