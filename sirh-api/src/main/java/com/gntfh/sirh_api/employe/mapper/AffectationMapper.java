package com.gntfh.sirh_api.employe.mapper;

import com.gntfh.sirh_api.employe.dto.AffectationRequest;
import com.gntfh.sirh_api.employe.dto.AffectationResponse;
import com.gntfh.sirh_api.employe.entity.Affectation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mapper pour l'entité Affectation.
 */
@Component
public class AffectationMapper {

    public Affectation toEntity(AffectationRequest request) {
        return Affectation.builder()
                .tauxOccupation(request.tauxOccupation() != null ? request.tauxOccupation() : BigDecimal.valueOf(100))
                .motif(request.motif())
                .build();
    }

    public void updateEntity(AffectationRequest request, Affectation affectation) {
        affectation.setTauxOccupation(request.tauxOccupation() != null ? request.tauxOccupation() : BigDecimal.valueOf(100));
        affectation.setMotif(request.motif());
    }

    public AffectationResponse toResponse(Affectation affectation) {
        return new AffectationResponse(
                affectation.getId(),
                affectation.getEmploye() != null ? affectation.getEmploye().getId() : null,
                affectation.getEmploye() != null ? affectation.getEmploye().getNom() : null,
                affectation.getEmploye() != null ? affectation.getEmploye().getMatricule() : null,
                affectation.getPoste() != null ? affectation.getPoste().getId() : null,
                affectation.getPoste() != null ? affectation.getPoste().getIntitule() : null,
                affectation.getPoste() != null && affectation.getPoste().getDepartement() != null ? affectation.getPoste().getDepartement().getNom() : null,
                affectation.getContratId(),
                affectation.getManager() != null ? affectation.getManager().getId() : null,
                affectation.getManager() != null ? affectation.getManager().getNom() : null,
                affectation.getDateDebut(),
                affectation.getDateFin(),
                affectation.getTauxOccupation(),
                affectation.getMotif(),
                isActive(affectation)
        );
    }

    private boolean isActive(Affectation affectation) {
        LocalDate today = LocalDate.now();
        return !affectation.getDateDebut().isAfter(today) &&
                (affectation.getDateFin() == null || !affectation.getDateFin().isBefore(today));
    }
}
