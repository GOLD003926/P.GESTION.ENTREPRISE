package com.gntfh.sirh_api.recrutement.mapper;

import com.gntfh.sirh_api.recrutement.dto.EntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienResponse;
import com.gntfh.sirh_api.recrutement.dto.IntervenantResponse;
import com.gntfh.sirh_api.recrutement.entity.Entretien;
import com.gntfh.sirh_api.recrutement.entity.EntretienIntervenant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Conversion entité ↔ DTO pour Entretien.
 * Aucune règle métier ici.
 */
@Component
public class EntretienMapper {

    public EntretienResponse toResponse(Entretien entretien, List<EntretienIntervenant> intervenants, String avisNiveau) {
        List<IntervenantResponse> intervenantResponses = intervenants.stream()
                .map(ei -> new IntervenantResponse(
                        ei.getUtilisateur() != null ? ei.getUtilisateur().getId() : null,
                        ei.getUtilisateur() != null
                                ? ei.getUtilisateur().getNom() + " " + ei.getUtilisateur().getPrenom()
                                : null,
                        ei.getRoleIntervenant()
                ))
                .collect(Collectors.toList());

        return new EntretienResponse(
                entretien.getId(),
                entretien.getCandidature() != null ? entretien.getCandidature().getId() : null,
                entretien.getCandidature() != null && entretien.getCandidature().getCandidat() != null
                        ? entretien.getCandidature().getCandidat().getNom() : null,
                entretien.getNiveau(),
                entretien.getType() != null ? entretien.getType().name() : null,
                entretien.getDateHeure(),
                entretien.getDureeMinutes(),
                entretien.getLieuOuLien(),
                entretien.getStatut() != null ? entretien.getStatut().name() : null,
                entretien.getMotifAnnulation(),
                intervenantResponses,
                avisNiveau
        );
    }

    public Entretien toEntity(EntretienRequest request, com.gntfh.sirh_api.recrutement.entity.Candidature candidature) {
        return Entretien.builder()
                .candidature(candidature)
                .niveau(request.niveau())
                .type(com.gntfh.sirh_api.recrutement.enums.TypeEntretien.pourNiveau(request.niveau()))
                .dateHeure(request.dateHeure())
                .dureeMinutes(request.dureeMinutes())
                .lieuOuLien(request.lieuOuLien())
                .statut(com.gntfh.sirh_api.recrutement.enums.StatutEntretien.PLANIFIE)
                .motifAnnulation(null)
                .build();
    }

    public void updateEntity(com.gntfh.sirh_api.recrutement.dto.EntretienUpdateRequest request, Entretien entretien) {
        if (request.dateHeure() != null) {
            entretien.setDateHeure(request.dateHeure());
        }
        if (request.dureeMinutes() != null) {
            entretien.setDureeMinutes(request.dureeMinutes());
        }
        if (request.lieuOuLien() != null) {
            entretien.setLieuOuLien(request.lieuOuLien());
        }
    }
}
