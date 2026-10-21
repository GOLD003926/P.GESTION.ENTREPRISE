package com.gntfh.sirh_api.onboarding.mapper;

import com.gntfh.sirh_api.onboarding.dto.OnboardingResponse;
import com.gntfh.sirh_api.onboarding.entity.Onboarding;
import com.gntfh.sirh_api.onboarding.entity.OnboardingTache;
import com.gntfh.sirh_api.onboarding.enums.StatutTache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OnboardingMapper {

    private final OnboardingTacheMapper tacheMapper;

    public OnboardingResponse toResponse(Onboarding onboarding, List<OnboardingTache> taches) {
        List<OnboardingTache> obligatoires = taches.stream()
                .filter(tache -> Boolean.TRUE.equals(tache.getObligatoire()))
                .toList();
        int faites = (int) obligatoires.stream()
                .filter(tache -> tache.getStatut() == StatutTache.FAIT)
                .count();

        return new OnboardingResponse(
                onboarding.getId(),
                onboarding.getEmploye().getId(),
                onboarding.getEmploye().getNom() + " " + onboarding.getEmploye().getPrenom(),
                onboarding.getEmploye().getMatricule(),
                onboarding.getContratId(),
                onboarding.getModele().getId(),
                onboarding.getResponsableRh() == null ? null : onboarding.getResponsableRh().getId(),
                onboarding.getDateDebut(),
                onboarding.getDateCible(),
                onboarding.getStatut(),
                onboarding.getDateCloture(),
                faites,
                obligatoires.size(),
                taches.stream().map(tacheMapper::toResponse).toList()
        );
    }
}
