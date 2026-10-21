package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.AnnulationEntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienResponse;
import com.gntfh.sirh_api.recrutement.dto.EntretienUpdateRequest;
import com.gntfh.sirh_api.recrutement.enums.StatutEntretien;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Service pour la gestion des entretiens.
 */
public interface EntretienService {

    /**
     * Obtient un entretien par son identifiant.
     *
     * @param id          L'identifiant de l'entretien
     * @param currentUser L'utilisateur connecté
     * @return L'entretien
     */
    EntretienResponse obtenir(Long id, CurrentUser currentUser);

    /**
     * Recherche des entretiens avec filtres facultatifs.
     * Tri autorisé : id, dateHeure, niveau, statut.
     *
     * @param candidatureId L'identifiant de la candidature filtré (facultatif)
     * @param statut        Le statut filtré (facultatif)
     * @param du            Date de début (facultatif)
     * @param au            Date de fin (facultatif)
     * @param pageable      La pagination et le tri
     * @param currentUser   L'utilisateur connecté
     * @return La page des entretiens
     */
    PageResponse<EntretienResponse> rechercher(Long candidatureId, StatutEntretien statut,
                                                LocalDate du, LocalDate au, Pageable pageable, CurrentUser currentUser);

    /**
     * Crée un entretien (planification).
     * Règles : RG-19, REC-04, REC-05.
     *
     * @param request     La demande de planification
     * @param currentUser L'utilisateur connecté
     * @return L'entretien créé
     */
    EntretienResponse creer(EntretienRequest request, CurrentUser currentUser);

    /**
     * Modifie un entretien (reprogrammation).
     *
     * @param id          L'identifiant de l'entretien
     * @param request     Les modifications
     * @param currentUser L'utilisateur connecté
     * @return L'entretien modifié
     */
    EntretienResponse modifier(Long id, EntretienUpdateRequest request, CurrentUser currentUser);

    /**
     * Clôture un entretien.
     * Règle : RG-20.
     *
     * @param id          L'identifiant de l'entretien
     * @param currentUser L'utilisateur connecté
     * @return L'entretien clôturé
     */
    EntretienResponse cloturer(Long id, CurrentUser currentUser);

    /**
     * Annule un entretien.
     *
     * @param id          L'identifiant de l'entretien
     * @param request     Le motif d'annulation
     * @param currentUser L'utilisateur connecté
     * @return L'entretien annulé
     */
    EntretienResponse annuler(Long id, AnnulationEntretienRequest request, CurrentUser currentUser);

    /**
     * Marque un candidat absent à un entretien.
     *
     * @param id          L'identifiant de l'entretien
     * @param currentUser L'utilisateur connecté
     * @return L'entretien mis à jour
     */
    EntretienResponse marquerAbsent(Long id, CurrentUser currentUser);
}
