package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.CandidatureRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatureResponse;
import com.gntfh.sirh_api.recrutement.enums.StatutCandidature;
import com.gntfh.sirh_api.recrutement.entity.Candidature;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service pour la gestion des candidatures.
 */
public interface CandidatureService {

    Candidature trouver(Long id);

    /**
     * Crée une nouvelle candidature.
     * Règles : RG-16 (une candidature par candidat et par offre), RG-17 (offre PUBLIEE et date de clôture non dépassée).
     * Exactement un de candidatId ou candidat doit être fourni.
     * Le statut est RECUE.
     *
     * @param request     La demande de création
     * @param currentUser L'utilisateur connecté
     * @return La candidature créée
     */
    CandidatureResponse creer(CandidatureRequest request, CurrentUser currentUser);

    /**
     * Dépose le CV d'une candidature.
     * Règle : REC-03.
     * Types acceptés : PDF, DOC, DOCX.
     * Un nouveau dépôt crée une nouvelle version via DocumentService.
     *
     * @param id          L'identifiant de la candidature
     * @param fichier     Le fichier CV
     * @param currentUser L'utilisateur connecté
     * @return La candidature mise à jour
     */
    CandidatureResponse televerserCv(Long id, MultipartFile fichier, CurrentUser currentUser);

    /**
     * Obtient une candidature par son identifiant.
     * Règle : REC-09 (visibilité).
     *
     * @param id          L'identifiant de la candidature
     * @param currentUser L'utilisateur connecté
     * @return La candidature
     */
    CandidatureResponse obtenir(Long id, CurrentUser currentUser);

    /**
     * Recherche des candidatures avec filtres facultatifs.
     * Règle : REC-09 (visibilité).
     * Tri autorisé : id, dateDepot, statut, scorePreselection.
     *
     * @param offreId     L'identifiant de l'offre filtré (facultatif)
     * @param statut      Le statut filtré (facultatif)
     * @param candidatId  L'identifiant du candidat filtré (facultatif)
     * @param pageable    La pagination et le tri
     * @param currentUser L'utilisateur connecté
     * @return La page des candidatures
     */
    PageResponse<CandidatureResponse> rechercher(Long offreId, StatutCandidature statut, Long candidatId,
                                                  Pageable pageable, CurrentUser currentUser);

    /**
     * Enregistre le désistement d'un candidat.
     * Transition vers DESISTEE.
     *
     * @param id          L'identifiant de la candidature
     * @param currentUser L'utilisateur connecté
     * @return La candidature mise à jour
     */
    CandidatureResponse desister(Long id, CurrentUser currentUser);

    /**
     * Promeut une candidature de la liste d'attente à retenue.
     * Règle : REC-08.
     * Transition LISTE_ATTENTE → RETENUE si l'effectif restant de la demande est > 0.
     *
     * @param id          L'identifiant de la candidature
     * @param currentUser L'utilisateur connecté
     * @return La candidature mise à jour
     */
    CandidatureResponse promouvoirListeAttente(Long id, CurrentUser currentUser);

    /**
     * Obtient l'historique des statuts d'une candidature.
     *
     * @param id          L'identifiant de la candidature
     * @param currentUser L'utilisateur connecté
     * @return L'historique des statuts
     */
    PageResponse<com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser);

    // ==================== API interne (appelée par embauche) ====================

    /**
     * Marque une candidature comme proposition envoyée.
     * API interne.
     * Transition RETENUE → PROPOSITION_ENVOYEE.
     *
     * @param id     L'identifiant de la candidature
     * @param auteur L'auteur de l'action
     */
    void marquerPropositionEnvoyee(Long id, String auteur);

    /**
     * Marque une candidature comme embauchée.
     * API interne.
     * Transition PROPOSITION_ENVOYEE → EMBAUCHEE.
     *
     * @param id     L'identifiant de la candidature
     * @param auteur L'auteur de l'action
     */
    void marquerEmbauchee(Long id, String auteur);

    /**
     * Marque une candidature comme non retenue après proposition.
     * API interne.
     * Transition PROPOSITION_ENVOYEE → NON_RETENUE.
     *
     * @param id     L'identifiant de la candidature
     * @param auteur L'auteur de l'action
     * @param motif  Le motif du refus
     */
    void marquerNonRetenueApresProposition(Long id, String auteur, String motif);

    /**
     * Remet une candidature en statut RETENUE (proposition retirée).
     * API interne.
     * Transition PROPOSITION_ENVOYEE → RETENUE.
     *
     * @param id     L'identifiant de la candidature
     * @param auteur L'auteur de l'action
     */
    void remettreRetenue(Long id, String auteur);
}
