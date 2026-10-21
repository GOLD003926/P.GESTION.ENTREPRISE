package com.gntfh.sirh_api.onboarding.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.onboarding.dto.OnboardingRequest;
import com.gntfh.sirh_api.onboarding.dto.OnboardingResponse;
import com.gntfh.sirh_api.onboarding.entity.Onboarding;
import com.gntfh.sirh_api.onboarding.enums.StatutOnboarding;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface OnboardingService {

    /**
     * API interne : RG-42, RG-43 — crée l'onboarding et copie les tâches du modèle actif,
     * calcule les échéances, notifie les rôles responsables et trace la création.
     */
    Onboarding creerPourContrat(Long contratId, Long employeId, String typeContrat, LocalDate dateDebut, String auteur);

    /** API interne : ONB-09 — passe automatiquement la tâche CONTRAT_SIGNE à FAIT. */
    void marquerContratSigne(Long contratId, String auteur);

    /** API interne : ONB-10 — annule l'onboarding du contrat annulé. */
    void annulerPourContrat(Long contratId, String auteur);

    /** ONB-08 : retourne l'onboarding uniquement dans le périmètre de l'utilisateur. */
    OnboardingResponse obtenir(Long id, CurrentUser currentUser);

    /** ONB-08 : RH voit tout ; le manager voit les onboardings de ses employés. */
    PageResponse<OnboardingResponse> rechercher(Long employeId, StatutOnboarding statut,
                                                Pageable pageable, CurrentUser currentUser);

    /** ONB-08 : retourne l'onboarding le plus récent de l'employé lié au salarié connecté. */
    OnboardingResponse monOnboarding(CurrentUser currentUser);

    /** Désigne le responsable RH d'un onboarding. */
    OnboardingResponse modifier(Long id, OnboardingRequest request, CurrentUser currentUser);

    /** API interne : ONB-06, RG-44 — clôture lorsque toutes les tâches obligatoires sont FAIT. */
    void verifierCloture(Onboarding onboarding, String auteur);

    /** ONB-08 : retourne l'historique après contrôle du périmètre de visibilité. */
    List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser);
}
