package com.gntfh.sirh_api.demande.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.demande.enums.DecisionValidation;
import com.gntfh.sirh_api.demande.enums.RoleValidateur;
import com.gntfh.sirh_api.demande.enums.StatutDemande;
import com.gntfh.sirh_api.demande.mapper.DemandeRecrutementMapper;
import com.gntfh.sirh_api.demande.mapper.ValidationMapper;
import com.gntfh.sirh_api.demande.repository.ValidationRepository;
import com.gntfh.sirh_api.demande.dto.DemandeRecrutementResponse;
import com.gntfh.sirh_api.demande.dto.ValidationRequest;
import com.gntfh.sirh_api.demande.dto.ValidationResponse;
import com.gntfh.sirh_api.demande.entity.DemandeRecrutement;
import com.gntfh.sirh_api.demande.entity.Validation;
import com.gntfh.sirh_api.demande.repository.DemandeRecrutementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Instant;
import org.springframework.data.domain.PageImpl;

/**
 * Service de gestion des validations de demandes de recrutement.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final DemandeRecrutementRepository demandeRecrutementRepository;
    private final DemandeWorkflowService demandeWorkflowService;
    private final ObjectProvider<EmployeLookup> employeLookupProvider;
    private final ValidationRepository validationRepository;
    private final ValidationMapper validationMapper;
    private final DemandeRecrutementMapper demandeMapper;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Récupère les demandes en attente de validation pour l'utilisateur actuel.
     * Boîte de validation : demandes dont l'étape courante correspond au rôle de l'utilisateur.
     *
     * @param currentUser l'utilisateur actuel
     * @param pageable pagination
     * @return les demandes en attente
     */
    @Transactional(readOnly = true)
    public PageResponse<DemandeRecrutementResponse> enAttentePour(CurrentUser currentUser, Pageable pageable) {
        RoleValidateur role = switch (currentUser.role()) {
            case RH -> RoleValidateur.RH;
            case FINANCE -> RoleValidateur.FINANCE;
            case DG -> RoleValidateur.DG;
            default -> throw new BusinessRuleException("Votre rôle ne possède pas de boîte de validation.");
        };
        StatutDemandeEtape etape = StatutDemandeEtape.of(role);
        List<DemandeRecrutement> demandes = demandeRecrutementRepository
                .findByEtapeCourante(etape.statut, role);
        int start = Math.min((int) pageable.getOffset(), demandes.size());
        int end = Math.min(start + pageable.getPageSize(), demandes.size());
        Page<DemandeRecrutement> page = new PageImpl<>(demandes.subList(start, end), pageable, demandes.size());
        return PageResponse.from(page, demandeMapper::toResponse);
    }

    /**
     * Prend une décision sur une demande en attente.
     * Règles RG-08 à RG-13.
     * Algorithme : charger la validation EN_ATTENTE du cycle courant, vérifier le rôle,
     * vérifier RG-09 (acteur != demandeur) et RG-10 (commentaire obligatoire pour REJETEE/RETOURNEE).
     * Selon la décision : APPROUVEE → transition au statut suivant, REJETEE → REJETEE,
     * RETOURNEE → RETOURNEE. Notifications.
     *
     * @param demandeId l'identifiant de la demande
     * @param request la décision et le commentaire
     * @param currentUser l'utilisateur actuel
     * @return la demande après décision
     */
    @Transactional
    public DemandeRecrutementResponse decider(Long demandeId, ValidationRequest request, CurrentUser currentUser) {
        DemandeRecrutement demande = demandeRecrutementRepository.findById(demandeId)
                .orElseThrow(() -> ResourceNotFoundException.of("DemandeRecrutement", demandeId));
        RoleValidateur role = switch (currentUser.role()) {
            case RH -> RoleValidateur.RH;
            case FINANCE -> RoleValidateur.FINANCE;
            case DG -> RoleValidateur.DG;
            default -> throw new BusinessRuleException("Votre rôle ne peut pas valider cette demande.");
        };
        if (demande.getDemandeur().getId().equals(currentUser.id())) {
            throw new BusinessRuleException("Le demandeur ne peut pas valider sa propre demande.");
        }
        Validation validation = validationRepository
                .findEnAttentePourCycleEtRole(demandeId, demande.getCycleCourant(), role)
                .orElseThrow(() -> new BusinessRuleException("Aucune validation en attente pour votre rôle."));
        if ((request.decision() == DecisionValidation.REJETEE || request.decision() == DecisionValidation.RETOURNEE)
                && (request.commentaire() == null || request.commentaire().isBlank())) {
            throw new BusinessRuleException("Un commentaire est obligatoire pour un rejet ou un retour.");
        }
        Utilisateur validateur = utilisateurRepository.findById(currentUser.id())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", currentUser.id()));
        validation.setValidateur(validateur);
        validation.setDecision(request.decision());
        validation.setCommentaire(request.commentaire());
        validation.setDateDecision(Instant.now());
        if (request.decision() == DecisionValidation.REJETEE) {
            demandeWorkflowService.transitionner(demande, StatutDemande.REJETEE, currentUser.email(), request.commentaire());
        } else if (request.decision() == DecisionValidation.RETOURNEE) {
            demandeWorkflowService.transitionner(demande, StatutDemande.RETOURNEE, currentUser.email(), request.commentaire());
        } else {
            StatutDemande cible = switch (role) {
                case RH -> StatutDemande.EN_VALIDATION_FINANCE;
                case FINANCE -> StatutDemande.EN_VALIDATION_DG;
                case DG -> StatutDemande.VALIDEE;
            };
            demandeWorkflowService.transitionner(demande, cible, currentUser.email(), request.commentaire());
        }
        return demandeMapper.toResponse(demande);
    }

    /**
     * Récupère l'historique des validations d'une demande.
     * Tous les cycles, par cycle puis ordre.
     *
     * @param demandeId l'identifiant de la demande
     * @param currentUser l'utilisateur actuel
     * @return l'historique des validations
     */
    @Transactional(readOnly = true)
    public List<ValidationResponse> historiqueValidations(Long demandeId, CurrentUser currentUser) {
        DemandeRecrutement demande = demandeRecrutementRepository.findById(demandeId)
                .orElseThrow(() -> ResourceNotFoundException.of("DemandeRecrutement", demandeId));
        if (currentUser.role() == com.gntfh.sirh_api.auth.enums.Role.MANAGER
                && !demande.getDemandeur().getId().equals(currentUser.id())) {
            throw ResourceNotFoundException.of("DemandeRecrutement", demandeId);
        }
        return validationRepository.findByDemandeIdOrderByCycleDescOrdreAsc(demandeId).stream()
                .map(validationMapper::toResponse).toList();
    }

    private record StatutDemandeEtape(com.gntfh.sirh_api.demande.enums.StatutDemande statut) {
        private static StatutDemandeEtape of(RoleValidateur role) {
            return new StatutDemandeEtape(role.statutEnValidation());
        }
    }
}
