package com.gntfh.sirh_api.onboarding.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.onboarding.dto.OnboardingTacheRequest;
import com.gntfh.sirh_api.onboarding.dto.OnboardingTacheResponse;
import com.gntfh.sirh_api.onboarding.enums.StatutTache;
import org.springframework.data.domain.Pageable;

public interface OnboardingTacheService {

    /** ONB-04 : RH voit toutes les tâches ouvertes ; les autres rôles voient les leurs. */
    PageResponse<OnboardingTacheResponse> mesTaches(StatutTache statut, Pageable pageable, CurrentUser currentUser);

    /** ONB-04 : seul le rôle responsable (ou RH) peut démarrer la tâche. */
    OnboardingTacheResponse demarrer(Long id, OnboardingTacheRequest request, CurrentUser currentUser);

    /** ONB-04, ONB-05 : vérifie le rôle et empêche toute modification d'une tâche FAIT. */
    OnboardingTacheResponse terminer(Long id, OnboardingTacheRequest request, CurrentUser currentUser);

    /** ONB-07 : réserve NON_APPLICABLE aux tâches facultatives et à la RH. */
    OnboardingTacheResponse marquerNonApplicable(Long id, OnboardingTacheRequest request, CurrentUser currentUser);

    /** ONB-04 : l'affectation est réservée à la RH et au rôle responsable de la tâche. */
    OnboardingTacheResponse assigner(Long id, OnboardingTacheRequest request, CurrentUser currentUser);
}
