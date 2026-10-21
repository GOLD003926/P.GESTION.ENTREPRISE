package com.gntfh.sirh_api.onboarding.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.onboarding.dto.OnboardingRequest;
import com.gntfh.sirh_api.onboarding.dto.OnboardingResponse;
import com.gntfh.sirh_api.onboarding.entity.Onboarding;
import com.gntfh.sirh_api.onboarding.enums.StatutOnboarding;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingServiceImpl implements OnboardingService {

    private UnsupportedOperationException todo(String codeRegle) {
        return new UnsupportedOperationException("TODO " + codeRegle);
    }

    /** RG-42, RG-43 : instancie les tâches comme copies indépendantes du modèle choisi. */
    @Override
    @Transactional
    public Onboarding creerPourContrat(Long contratId, Long employeId, String typeContrat,
                                      LocalDate dateDebut, String auteur) {
        throw todo("RG-42");
    }

    /** ONB-09 : clôture automatiquement la tâche de signature. */
    @Override
    @Transactional
    public void marquerContratSigne(Long contratId, String auteur) {
        throw todo("ONB-09");
    }

    /** ONB-10 : annule l'onboarding associé au contrat annulé. */
    @Override
    @Transactional
    public void annulerPourContrat(Long contratId, String auteur) {
        throw todo("ONB-10");
    }

    /** ONB-08 : refuse de révéler un onboarding hors du périmètre de l'acteur. */
    @Override
    public OnboardingResponse obtenir(Long id, CurrentUser currentUser) {
        throw todo("ONB-08");
    }

    /** ONB-08 : filtre par employé et rôle puis applique la liste blanche du tri. */
    @Override
    public PageResponse<OnboardingResponse> rechercher(Long employeId, StatutOnboarding statut,
                                                        Pageable pageable, CurrentUser currentUser) {
        throw todo("ONB-08");
    }

    /** ONB-08 : donne au salarié son onboarding le plus récent en lecture seule. */
    @Override
    public OnboardingResponse monOnboarding(CurrentUser currentUser) {
        throw todo("ONB-08");
    }

    /** ONB-08 : désigne le responsable RH après contrôle du périmètre. */
    @Override
    @Transactional
    public OnboardingResponse modifier(Long id, OnboardingRequest request, CurrentUser currentUser) {
        throw todo("ONB-08");
    }

    /** ONB-06, RG-44 : seules les tâches obligatoires encore non FAIT empêchent la clôture. */
    @Override
    @Transactional
    public void verifierCloture(Onboarding onboarding, String auteur) {
        throw todo("ONB-06");
    }

    /** ONB-08 : retourne l'historique après contrôle de visibilité. */
    @Override
    public List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser) {
        throw todo("ONB-08");
    }
}
