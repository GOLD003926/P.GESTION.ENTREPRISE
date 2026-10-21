package com.gntfh.sirh_api.demande.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.demande.dto.AnnulationRequest;
import com.gntfh.sirh_api.demande.dto.DemandeRecrutementRequest;
import com.gntfh.sirh_api.demande.dto.DemandeRecrutementResponse;
import com.gntfh.sirh_api.demande.entity.DemandeRecrutement;
import com.gntfh.sirh_api.demande.enums.StatutDemande;
import com.gntfh.sirh_api.demande.repository.DemandeRecrutementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des demandes de recrutement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemandeRecrutementService {

    private final DemandeRecrutementRepository demandeRecrutementRepository;
    private final BudgetService budgetService;
    private final DemandeWorkflowService demandeWorkflowService;
    private final ObjectProvider<EmployeLookup> employeLookupProvider;

    /**
     * Crée une nouvelle demande de recrutement.
     * Règles : DEM-01 à DEM-06, RG-03 à RG-07.
     * Génère la référence, calcule le budget, statut BROUILLON, historique, journal d'audit.
     *
     * @param request la demande
     * @param currentUser l'utilisateur actuel
     * @return la demande créée
     */
    @Transactional
    public DemandeRecrutementResponse creer(DemandeRecrutementRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO DEM-01, DEM-02, DEM-03, RG-03, RG-04, RG-05, RG-06, RG-07");
    }

    /**
     * Modifie une demande existante.
     * Règle RG-11 : uniquement BROUILLON ou RETOURNEE, uniquement par le demandeur.
     * Recalcul du budget.
     *
     * @param id l'identifiant de la demande
     * @param request les modifications
     * @param currentUser l'utilisateur actuel
     * @return la demande modifiée
     */
    @Transactional
    public DemandeRecrutementResponse modifier(Long id, DemandeRecrutementRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-11 : modification uniquement en BROUILLON ou RETOURNEE");
    }

    /**
     * Obtient le détail d'une demande.
     * Règle DEM-07 : visibilité selon le rôle et le périmètre.
     *
     * @param id l'identifiant de la demande
     * @param currentUser l'utilisateur actuel
     * @return la demande
     */
    @Transactional(readOnly = true)
    public DemandeRecrutementResponse obtenir(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO DEM-07 : visibilité selon le rôle et le périmètre");
    }

    /**
     * Recherche des demandes avec filtres.
     * Règle DEM-07 : visibilité. q sur la référence.
     * Tri autorisé : id, reference, statut, dateSouhaitee, budgetEstime, createdAt.
     *
     * @param q recherche sur la référence
     * @param statut filtre sur le statut
     * @param posteId filtre sur le poste
     * @param departementId filtre sur le département
     * @param pageable pagination
     * @param currentUser l'utilisateur actuel
     * @return les demandes paginées
     */
    @Transactional(readOnly = true)
    public PageResponse<DemandeRecrutementResponse> rechercher(String q, StatutDemande statut, Long posteId, Long departementId, Pageable pageable, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO DEM-07 : recherche avec filtres et visibilité");
    }

    /**
     * Soumet une demande à validation.
     * Règles RG-06 (date), RG-07, RG-08 : BROUILLON ou RETOURNEE → EN_VALIDATION_RH.
     * Crée les validations du cycle, notifie les utilisateurs RH.
     *
     * @param id l'identifiant de la demande
     * @param currentUser l'utilisateur actuel
     * @return la demande soumise
     */
    @Transactional
    public DemandeRecrutementResponse soumettre(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-06, RG-07, RG-08 : soumission à validation");
    }

    /**
     * Annule une demande.
     * Règle DEM-08 : motif obligatoire, statuts autorisés par la machine à états.
     * Depuis BROUILLON, RETOURNEE (par le demandeur ou la RH) ou EN_RECRUTEMENT (par la RH).
     *
     * @param id l'identifiant de la demande
     * @param request le motif d'annulation
     * @param currentUser l'utilisateur actuel
     * @return la demande annulée
     */
    @Transactional
    public DemandeRecrutementResponse annuler(Long id, AnnulationRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO DEM-08 : annulation selon statut et rôle");
    }

    /**
     * Récupère l'historique des statuts d'une demande.
     * Même visibilité que obtenir.
     *
     * @param id l'identifiant de la demande
     * @param currentUser l'utilisateur actuel
     * @return l'historique des statuts
     */
    @Transactional(readOnly = true)
    public List<Object> historique(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO : historique des statuts");
    }
}
