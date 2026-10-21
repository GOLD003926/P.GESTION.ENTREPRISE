package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatRequest;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatResponse;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatUpdateRequest;
import org.springframework.data.domain.Pageable;

/**
 * Service pour la gestion des évaluations de candidats.
 */
public interface EvaluationCandidatService {

    /**
     * Crée une nouvelle évaluation de candidat.
     * Règle : RG-20.
     * L'acteur doit être intervenant de l'entretien (REC-10).
     * L'entretien doit être PLANIFIE ou REALISE.
     * Pas d'évaluation active existante du même évaluateur.
     *
     * @param request     La demande de création
     * @param currentUser L'utilisateur connecté
     * @return L'évaluation créée
     */
    EvaluationCandidatResponse creer(EvaluationCandidatRequest request, CurrentUser currentUser);

    /**
     * Modifie une évaluation existante.
     * Auteur uniquement, évaluation non verrouillée (RG-21).
     *
     * @param id          L'identifiant de l'évaluation
     * @param request     Les modifications
     * @param currentUser L'utilisateur connecté
     * @return L'évaluation modifiée
     */
    EvaluationCandidatResponse modifier(Long id, EvaluationCandidatUpdateRequest request, CurrentUser currentUser);

    /**
     * Verrouille une évaluation.
     * Auteur ou RH ; irréversible.
     *
     * @param id          L'identifiant de l'évaluation
     * @param currentUser L'utilisateur connecté
     * @return L'évaluation verrouillée
     */
    EvaluationCandidatResponse verrouiller(Long id, CurrentUser currentUser);

    /**
     * Corrige une évaluation (crée une nouvelle version).
     * Règle : RG-21.
     * Réservé à la RH.
     * Crée une nouvelle évaluation liée à la précédente (evaluationPrecedente).
     * Passe l'ancienne à active = false.
     * Enregistre l'historique.
     *
     * @param id          L'identifiant de l'évaluation à corriger
     * @param request     Les corrections
     * @param currentUser L'utilisateur connecté
     * @return La nouvelle évaluation
     */
    EvaluationCandidatResponse corriger(Long id, EvaluationCandidatUpdateRequest request, CurrentUser currentUser);

    /**
     * Liste les évaluations d'un entretien.
     * Par défaut, retourne les évaluations actives.
     *
     * @param entretienId  L'identifiant de l'entretien
     * @param pageable     La pagination
     * @param currentUser  L'utilisateur connecté
     * @return La page des évaluations
     */
    PageResponse<EvaluationCandidatResponse> lister(Long entretienId, Pageable pageable, CurrentUser currentUser);
}
