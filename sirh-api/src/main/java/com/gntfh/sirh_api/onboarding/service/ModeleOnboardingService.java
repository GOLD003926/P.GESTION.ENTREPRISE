package com.gntfh.sirh_api.onboarding.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.onboarding.dto.ModeleOnboardingRequest;
import com.gntfh.sirh_api.onboarding.dto.ModeleOnboardingResponse;
import com.gntfh.sirh_api.onboarding.entity.ModeleOnboarding;
import org.springframework.data.domain.Pageable;

public interface ModeleOnboardingService {

    /** ONB-01, ONB-02 : vérifie l'unicité insensible à la casse et la composition du modèle. */
    ModeleOnboardingResponse creer(ModeleOnboardingRequest request, CurrentUser currentUser);

    /** ONB-03 : remplace la définition du modèle sans modifier les tâches déjà copiées. */
    ModeleOnboardingResponse modifier(Long id, ModeleOnboardingRequest request, CurrentUser currentUser);

    ModeleOnboardingResponse obtenir(Long id);

    /** ONB-01 : recherche paginée avec tri limité à id, nom et actif. */
    PageResponse<ModeleOnboardingResponse> rechercher(String q, Boolean actif, Pageable pageable);

    void activer(Long id);

    void desactiver(Long id);

    /** API interne : ONB-01, ONB-02 — sélectionne le modèle du type, puis le modèle sans type. */
    ModeleOnboarding choisirPour(String typeContrat);
}
