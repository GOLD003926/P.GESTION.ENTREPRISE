package com.gntfh.sirh_api.onboarding.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.onboarding.dto.OnboardingTacheRequest;
import com.gntfh.sirh_api.onboarding.dto.OnboardingTacheResponse;
import com.gntfh.sirh_api.onboarding.enums.StatutTache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingTacheServiceImpl implements OnboardingTacheService {

    private UnsupportedOperationException todo(String codeRegle) {
        return new UnsupportedOperationException("TODO " + codeRegle);
    }

    /** ONB-04 : RH voit tout ; les autres acteurs voient les tâches correspondant à leur rôle. */
    @Override
    public PageResponse<OnboardingTacheResponse> mesTaches(StatutTache statut, Pageable pageable,
                                                            CurrentUser currentUser) {
        throw todo("ONB-04");
    }

    /** ONB-04 : contrôle le rôle responsable avant A_FAIRE → EN_COURS. */
    @Override
    @Transactional
    public OnboardingTacheResponse demarrer(Long id, OnboardingTacheRequest request, CurrentUser currentUser) {
        throw todo("ONB-04");
    }

    /** ONB-04, ONB-05 : réserve le traitement au responsable et refuse une tâche déjà FAIT. */
    @Override
    @Transactional
    public OnboardingTacheResponse terminer(Long id, OnboardingTacheRequest request, CurrentUser currentUser) {
        throw todo("ONB-04");
    }

    /** ONB-07 : la RH seule peut marquer NON_APPLICABLE une tâche facultative. */
    @Override
    @Transactional
    public OnboardingTacheResponse marquerNonApplicable(Long id, OnboardingTacheRequest request,
                                                         CurrentUser currentUser) {
        throw todo("ONB-07");
    }

    /** ONB-04 : vérifie que l'utilisateur assigné est actif et possède le rôle responsable. */
    @Override
    @Transactional
    public OnboardingTacheResponse assigner(Long id, OnboardingTacheRequest request, CurrentUser currentUser) {
        throw todo("ONB-04");
    }
}
