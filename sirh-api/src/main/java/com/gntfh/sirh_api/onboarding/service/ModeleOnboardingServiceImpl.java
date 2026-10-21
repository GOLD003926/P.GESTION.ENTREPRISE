package com.gntfh.sirh_api.onboarding.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.onboarding.dto.ModeleOnboardingRequest;
import com.gntfh.sirh_api.onboarding.dto.ModeleOnboardingResponse;
import com.gntfh.sirh_api.onboarding.entity.ModeleOnboarding;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModeleOnboardingServiceImpl implements ModeleOnboardingService {

    private UnsupportedOperationException todo(String codeRegle) {
        return new UnsupportedOperationException("TODO " + codeRegle);
    }

    /** ONB-01, ONB-02. */
    @Override
    @Transactional
    public ModeleOnboardingResponse creer(ModeleOnboardingRequest request, CurrentUser currentUser) {
        throw todo("ONB-01");
    }

    /** ONB-03 : les tâches déjà copiées restent indépendantes du modèle modifié. */
    @Override
    @Transactional
    public ModeleOnboardingResponse modifier(Long id, ModeleOnboardingRequest request, CurrentUser currentUser) {
        throw todo("ONB-03");
    }

    @Override
    public ModeleOnboardingResponse obtenir(Long id) {
        throw todo("ONB-01");
    }

    /** ONB-01 : tri autorisé uniquement sur id, nom et actif. */
    @Override
    public PageResponse<ModeleOnboardingResponse> rechercher(String q, Boolean actif, Pageable pageable) {
        throw todo("ONB-01");
    }

    /** ONB-01 : rend le modèle actif pour les prochains onboardings. */
    @Override
    @Transactional
    public void activer(Long id) {
        throw todo("ONB-01");
    }

    /** ONB-01 : désactive le modèle sans supprimer les copies de tâches existantes. */
    @Override
    @Transactional
    public void desactiver(Long id) {
        throw todo("ONB-01");
    }

    /** API interne : sélection du type exact, repli sur le modèle sans type. */
    @Override
    public ModeleOnboarding choisirPour(String typeContrat) {
        throw todo("RG-42");
    }
}
