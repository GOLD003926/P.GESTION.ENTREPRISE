package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.AnnulationEntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatureResponse;
import com.gntfh.sirh_api.recrutement.dto.DecisionFinaleRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienResponse;
import com.gntfh.sirh_api.recrutement.dto.EntretienUpdateRequest;
import com.gntfh.sirh_api.recrutement.dto.PreselectionRequest;
import com.gntfh.sirh_api.recrutement.enums.Avis;

/**
 * Service pour le workflow de sélection (présélection, entretiens, décision finale).
 */
public interface SelectionWorkflowService {

    /**
     * Présélectionne une candidature sur dossier.
     * Règle : RG-18.
     * Transition RECUE → PRESELECTIONNEE (accepté) ou REFUSEE_DOSSIER (motif obligatoire).
     *
     * @param candidatureId L'identifiant de la candidature
     * @param request       La demande de présélection
     * @param currentUser   L'utilisateur connecté
     * @return La candidature mise à jour
     */
    CandidatureResponse preselectionner(Long candidatureId, PreselectionRequest request, CurrentUser currentUser);

    /**
     * Planifie un entretien pour une candidature.
     * Règles : RG-19, REC-04, REC-05.
     * Le niveau n+1 n'est planifiable que si le niveau n est REALISE avec avis FAVORABLE.
     * Le niveau 1 exige la candidature PRESELECTIONNEE.
     * Le niveau maximal est offre.nombreNiveauxSelection.
     * Le type de l'entretien est dérivé du niveau (TypeEntretien.pourNiveau).
     *
     * @param request     La demande de planification
     * @param currentUser L'utilisateur connecté
     * @return L'entretien créé
     */
    EntretienResponse planifierEntretien(EntretienRequest request, CurrentUser currentUser);

    /**
     * Reprogramme un entretien existant.
     * Transition PLANIFIE → REPORTE → PLANIFIE.
     *
     * @param entretienId  L'identifiant de l'entretien
     * @param request      Les modifications
     * @param currentUser  L'utilisateur connecté
     * @return L'entretien reprogrammé
     */
    EntretienResponse reprogrammer(Long entretienId, EntretienUpdateRequest request, CurrentUser currentUser);

    /**
     * Clôture un entretien.
     * Règle : RG-20.
     * Au moins une évaluation active et verrouillée est requise.
     * Transition → REALISE.
     * Calcule l'avis du niveau (REC-06).
     * Si avis DEFAVORABLE → candidature NON_RETENUE.
     * Si avis FAVORABLE → ouvre le niveau suivant.
     * Si avis RESERVE → laisse la décision à la RH.
     *
     * @param entretienId  L'identifiant de l'entretien
     * @param currentUser  L'utilisateur connecté
     * @return L'entretien clôturé
     */
    EntretienResponse cloturerEntretien(Long entretienId, CurrentUser currentUser);

    /**
     * Annule un entretien.
     * Transition → ANNULE.
     *
     * @param id          L'identifiant de l'entretien
     * @param request     Le motif d'annulation
     * @param currentUser L'utilisateur connecté
     * @return L'entretien annulé
     */
    EntretienResponse annulerEntretien(Long id, AnnulationEntretienRequest request, CurrentUser currentUser);

    /**
     * Marque un candidat absent à un entretien.
     * Transition → CANDIDAT_ABSENT.
     *
     * @param id          L'identifiant de l'entretien
     * @param currentUser L'utilisateur connecté
     * @return L'entretien mis à jour
     */
    EntretienResponse marquerAbsent(Long id, CurrentUser currentUser);

    /**
     * Enregistre la décision finale sur une candidature.
     * Règles : RG-22, REC-07.
     * Seulement après tous les niveaux requis réalisés et favorables.
     * Décision datée, signée (décideur) et motivée.
     *
     * @param candidatureId L'identifiant de la candidature
     * @param request       La demande de décision
     * @param currentUser   L'utilisateur connecté
     * @return La candidature mise à jour
     */
    CandidatureResponse decisionFinale(Long candidatureId, DecisionFinaleRequest request, CurrentUser currentUser);

    /**
     * Calcule l'avis agrégé d'un niveau pour une candidature.
     * Règle : REC-06.
     * L'avis est calculé à partir des évaluations actives du niveau.
     *
     * @param candidatureId L'identifiant de la candidature
     * @param niveau        Le niveau (1 à 3)
     * @return L'avis agrégé (FAVORABLE, RESERVE, DEFAVORABLE)
     */
    Avis avisDuNiveau(Long candidatureId, int niveau);
}
