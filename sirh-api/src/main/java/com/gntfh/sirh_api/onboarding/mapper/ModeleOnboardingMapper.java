package com.gntfh.sirh_api.onboarding.mapper;

import com.gntfh.sirh_api.onboarding.dto.ModeleOnboardingRequest;
import com.gntfh.sirh_api.onboarding.dto.ModeleOnboardingResponse;
import com.gntfh.sirh_api.onboarding.dto.ModeleTacheResponse;
import com.gntfh.sirh_api.onboarding.entity.ModeleOnboarding;
import com.gntfh.sirh_api.onboarding.entity.ModeleTache;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ModeleOnboardingMapper {

    public ModeleOnboardingResponse toResponse(ModeleOnboarding modele, List<ModeleTache> taches) {
        List<ModeleTacheResponse> reponses = taches.stream()
                .map(tache -> new ModeleTacheResponse(
                        tache.getId(),
                        tache.getOrdre(),
                        tache.getCode(),
                        tache.getLibelle(),
                        Boolean.TRUE.equals(tache.getObligatoire()),
                        tache.getRoleResponsable(),
                        tache.getEcheanceJours()
                ))
                .toList();

        return new ModeleOnboardingResponse(
                modele.getId(),
                modele.getNom(),
                modele.getTypeContrat(),
                modele.getActif(),
                reponses,
                modele.getCreatedAt()
        );
    }

    public ModeleOnboarding toEntity(ModeleOnboardingRequest request) {
        return ModeleOnboarding.builder()
                .nom(request.nom())
                .typeContrat(request.typeContrat())
                .actif(true)
                .build();
    }

    public ModeleTache toTache(com.gntfh.sirh_api.onboarding.dto.ModeleTacheRequest request,
                                ModeleOnboarding modele) {
        return ModeleTache.builder()
                .modele(modele)
                .ordre(request.ordre())
                .code(request.code())
                .libelle(request.libelle())
                .obligatoire(request.obligatoire())
                .roleResponsable(request.roleResponsable())
                .echeanceJours(request.echeanceJours())
                .build();
    }

    public void updateEntity(ModeleOnboarding modele, ModeleOnboardingRequest request) {
        modele.setNom(request.nom());
        modele.setTypeContrat(request.typeContrat());
    }
}
