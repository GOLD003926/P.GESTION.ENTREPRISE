package com.gntfh.sirh_api.demande.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.common.numerotation.NumeroService;
import com.gntfh.sirh_api.common.numerotation.SequenceNumero;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.demande.dto.AnnulationRequest;
import com.gntfh.sirh_api.demande.dto.DemandeRecrutementRequest;
import com.gntfh.sirh_api.demande.dto.DemandeRecrutementResponse;
import com.gntfh.sirh_api.demande.entity.DemandeRecrutement;
import com.gntfh.sirh_api.demande.enums.StatutDemande;
import com.gntfh.sirh_api.demande.repository.DemandeRecrutementRepository;
import com.gntfh.sirh_api.demande.mapper.DemandeRecrutementMapper;
import com.gntfh.sirh_api.organisation.entity.Poste;
import com.gntfh.sirh_api.organisation.repository.PosteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final DemandeRecrutementMapper mapper;
    private final PosteRepository posteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final NumeroService numeroService;
    private final HistoriqueStatutService historiqueStatutService;

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
        Poste poste = posteRepository.findById(request.posteId())
                .orElseThrow(() -> ResourceNotFoundException.of("Poste", request.posteId()));
        if (!Boolean.TRUE.equals(poste.getActif())) {
            throw new BusinessRuleException("Le poste sélectionné est inactif.");
        }
        Utilisateur demandeur = utilisateurRepository.findById(currentUser.id())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", currentUser.id()));
        DemandeRecrutement demande = mapper.toEntity(request);
        demande.setReference(numeroService.prochain(SequenceNumero.DEMANDE));
        demande.setPoste(poste);
        demande.setDepartement(poste.getDepartement());
        demande.setDemandeur(demandeur);
        demande.setBudgetEstime(budgetService.calculer(poste, request.effectifDemande()));
        return mapper.toResponse(demandeRecrutementRepository.save(demande));
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
        DemandeRecrutement demande = charger(id);
        if (demande.getDemandeur() == null || !demande.getDemandeur().getId().equals(currentUser.id())) {
            throw new BusinessRuleException("Seul le demandeur peut modifier cette demande.");
        }
        if (demande.getStatut() != StatutDemande.BROUILLON && demande.getStatut() != StatutDemande.RETOURNEE) {
            throw new BusinessRuleException("La demande n'est modifiable qu'à l'état brouillon ou retournée.");
        }
        Poste poste = posteRepository.findById(request.posteId())
                .orElseThrow(() -> ResourceNotFoundException.of("Poste", request.posteId()));
        if (!Boolean.TRUE.equals(poste.getActif())) {
            throw new BusinessRuleException("Le poste sélectionné est inactif.");
        }
        mapper.updateEntity(request, demande);
        demande.setPoste(poste);
        demande.setDepartement(poste.getDepartement());
        demande.setBudgetEstime(budgetService.calculer(poste, request.effectifDemande()));
        return mapper.toResponse(demande);
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
        DemandeRecrutement demande = charger(id);
        verifierVisibilite(demande, currentUser);
        return mapper.toResponse(demande);
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
        Specification<DemandeRecrutement> specification = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (q != null && !q.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("reference")), "%" + q.toLowerCase() + "%"));
            }
            if (statut != null) predicates.add(cb.equal(root.get("statut"), statut));
            if (posteId != null) predicates.add(cb.equal(root.get("poste").get("id"), posteId));
            if (departementId != null) predicates.add(cb.equal(root.get("departement").get("id"), departementId));
            if (currentUser.role() == com.gntfh.sirh_api.auth.enums.Role.MANAGER) {
                predicates.add(cb.equal(root.get("demandeur").get("id"), currentUser.id()));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return PageResponse.from(demandeRecrutementRepository.findAll(specification, pageable), mapper::toResponse);
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
        DemandeRecrutement demande = charger(id);
        if (!demande.getDemandeur().getId().equals(currentUser.id())) {
            throw new BusinessRuleException("Seul le demandeur peut soumettre cette demande.");
        }
        if (demande.getDateSouhaitee().isBefore(java.time.LocalDate.now())) {
            throw new BusinessRuleException("La date souhaitée doit être aujourd'hui ou dans le futur.");
        }
        if (demande.getStatut() != StatutDemande.BROUILLON && demande.getStatut() != StatutDemande.RETOURNEE) {
            throw new BusinessRuleException("La demande ne peut pas être soumise dans son état actuel.");
        }
        demandeWorkflowService.creerValidationsDuCycle(demande);
        demandeWorkflowService.transitionner(demande, StatutDemande.EN_VALIDATION_RH, currentUser.email(), null);
        return mapper.toResponse(demande);
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
        DemandeRecrutement demande = charger(id);
        if (request.motif() == null || request.motif().isBlank()) {
            throw new BusinessRuleException("Le motif d'annulation est obligatoire.");
        }
        boolean autorise = currentUser.role() == com.gntfh.sirh_api.auth.enums.Role.RH
                || currentUser.role() == com.gntfh.sirh_api.auth.enums.Role.ADMIN
                || demande.getDemandeur().getId().equals(currentUser.id());
        if (!autorise || (demande.getStatut() != StatutDemande.BROUILLON
                && demande.getStatut() != StatutDemande.RETOURNEE
                && demande.getStatut() != StatutDemande.EN_RECRUTEMENT)) {
            throw new BusinessRuleException("La demande ne peut pas être annulée dans son état actuel.");
        }
        demandeWorkflowService.transitionner(demande, StatutDemande.ANNULEE, currentUser.email(), request.motif());
        return mapper.toResponse(demande);
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
        DemandeRecrutement demande = charger(id);
        verifierVisibilite(demande, currentUser);
        return historiqueStatutService.lister(TypeEntite.DEMANDE_RECRUTEMENT, id)
                .stream().map(element -> (Object) element).toList();
    }

    private DemandeRecrutement charger(Long id) {
        return demandeRecrutementRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("DemandeRecrutement", id));
    }

    private void verifierVisibilite(DemandeRecrutement demande, CurrentUser currentUser) {
        if (currentUser.role() == com.gntfh.sirh_api.auth.enums.Role.MANAGER
                && !demande.getDemandeur().getId().equals(currentUser.id())) {
            throw ResourceNotFoundException.of("DemandeRecrutement", demande.getId());
        }
    }
}
