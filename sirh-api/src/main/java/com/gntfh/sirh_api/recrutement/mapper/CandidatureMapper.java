package com.gntfh.sirh_api.recrutement.mapper;

import com.gntfh.sirh_api.recrutement.dto.CandidatureRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatureResponse;
import com.gntfh.sirh_api.recrutement.entity.Candidature;
import org.springframework.stereotype.Component;

/**
 * Conversion entité ↔ DTO pour Candidature.
 * Aucune règle métier ici.
 */
@Component
public class CandidatureMapper {

    public CandidatureResponse toResponse(Candidature candidature) {
        return new CandidatureResponse(
                candidature.getId(),
                candidature.getCandidat() != null ? candidature.getCandidat().getId() : null,
                candidature.getCandidat() != null ? candidature.getCandidat().getNom() : null,
                candidature.getCandidat() != null ? candidature.getCandidat().getEmail() : null,
                candidature.getOffre() != null ? candidature.getOffre().getId() : null,
                candidature.getOffre() != null ? candidature.getOffre().getReference() : null,
                candidature.getOffre() != null ? candidature.getOffre().getTitre() : null,
                candidature.getCvDocument() != null ? candidature.getCvDocument().getId() : null,
                candidature.getLettreMotivation(),
                candidature.getDateDepot(),
                candidature.getStatut() != null ? candidature.getStatut().name() : null,
                candidature.getScorePreselection(),
                candidature.getMotifRefus(),
                candidature.getDecisionFinale() != null ? candidature.getDecisionFinale().name() : null,
                candidature.getRangListeAttente(),
                candidature.getDecideur() != null ? candidature.getDecideur().getId() : null,
                candidature.getDateDecision(),
                candidature.getMotifDecision(),
                null // prochainNiveau : calculé par le service
        );
    }

    public Candidature toEntity(CandidatureRequest request, com.gntfh.sirh_api.recrutement.entity.Candidat candidat,
                                 com.gntfh.sirh_api.recrutement.entity.OffreEmploi offre) {
        return Candidature.builder()
                .candidat(candidat)
                .offre(offre)
                .cvDocument(null)
                .lettreMotivation(request.lettreMotivation())
                .dateDepot(java.time.Instant.now())
                .statut(com.gntfh.sirh_api.recrutement.enums.StatutCandidature.RECUE)
                .scorePreselection(null)
                .motifRefus(null)
                .decisionFinale(null)
                .rangListeAttente(null)
                .decideur(null)
                .dateDecision(null)
                .motifDecision(null)
                .build();
    }
}
