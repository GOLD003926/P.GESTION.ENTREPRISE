package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.CandidatRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatResponse;
import com.gntfh.sirh_api.recrutement.entity.Candidat;
import org.springframework.data.domain.Pageable;

/**
 * Service pour la gestion des candidats.
 */
public interface CandidatService {

    /**
     * Crée un nouveau candidat.
     * Règle : RG-16.
     * L'email doit être unique (409 sinon).
     *
     * @param request La demande de création
     * @return Le candidat créé
     */
    CandidatResponse creer(CandidatRequest request);

    /**
     * Modifie un candidat existant.
     *
     * @param id      L'identifiant du candidat
     * @param request Les modifications
     * @param currentUser L'utilisateur connecté
     * @return Le candidat modifié
     */
    CandidatResponse modifier(Long id, CandidatRequest request, CurrentUser currentUser);

    /**
     * Obtient un candidat par son identifiant.
     *
     * @param id L'identifiant du candidat
     * @return Le candidat
     */
    CandidatResponse obtenir(Long id);

    /**
     * Recherche des candidats avec filtre facultatif.
     * Le terme q recherche sur nom, prénom, email.
     * Tri autorisé : id, nom, prenom, email, createdAt.
     *
     * @param q        Le terme de recherche (facultatif)
     * @param pageable La pagination et le tri
     * @return La page des candidats
     */
    PageResponse<CandidatResponse> rechercher(String q, Pageable pageable);

    /**
     * Trouve un candidat existant par email ou en crée un nouveau.
     * API interne.
     * Retourne le candidat existant (même email) sans le modifier,
     * sinon le crée.
     *
     * @param request Les données du candidat
     * @return Le candidat trouvé ou créé
     */
    Candidat trouverOuCreer(CandidatRequest request);
}
