package com.gntfh.sirh_api.onboarding.mapper;

import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.onboarding.dto.OnboardingTacheResponse;
import com.gntfh.sirh_api.onboarding.entity.OnboardingTache;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class OnboardingTacheMapper {

    public OnboardingTacheResponse toResponse(OnboardingTache tache) {
        return new OnboardingTacheResponse(
                tache.getId(),
                tache.getOnboarding().getId(),
                tache.getOnboarding().getEmploye().getId(),
                tache.getOnboarding().getEmploye().getNom() + " " + tache.getOnboarding().getEmploye().getPrenom(),
                tache.getOnboarding().getEmploye().getMatricule(),
                tache.getCode(),
                tache.getLibelle(),
                Boolean.TRUE.equals(tache.getObligatoire()),
                tache.getRoleResponsable(),
                tache.getAssigneA() == null ? null : tache.getAssigneA().getId(),
                tache.getEcheance(),
                tache.getStatut(),
                tache.getDateRealisation(),
                tache.getPreuveDocument() == null ? null : tache.getPreuveDocument().getId(),
                tache.getCommentaire(),
                tache.getEcheance().isBefore(LocalDate.now()) && !tache.getStatut().estTerminal()
        );
    }

    public void updateEntity(OnboardingTache tache, String commentaire, Document preuveDocument) {
        tache.setCommentaire(commentaire);
        if (preuveDocument != null) {
            tache.setPreuveDocument(preuveDocument);
        }
    }
}
