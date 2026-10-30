package com.gntfh.sirh_api.demande.service;

import com.gntfh.sirh_api.common.exception.InvalidStateTransitionException;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.demande.entity.DemandeRecrutement;
import com.gntfh.sirh_api.demande.entity.Validation;
import com.gntfh.sirh_api.demande.enums.DecisionValidation;
import com.gntfh.sirh_api.demande.enums.RoleValidateur;
import com.gntfh.sirh_api.demande.enums.StatutDemande;
import com.gntfh.sirh_api.demande.repository.DemandeRecrutementRepository;
import com.gntfh.sirh_api.demande.repository.ValidationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de gestion du workflow des demandes de recrutement (machine à états).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemandeWorkflowService {

    private final DemandeRecrutementRepository demandeRecrutementRepository;
    private final ValidationRepository validationRepository;
    private final HistoriqueStatutService historiqueStatutService;

    /**
     * Transitionne une demande vers un nouveau statut si la transition est autorisée.
     * Règle : vérification de la transition autorisée par la machine à états.
     *
     * @param demande la demande à transitionner
     * @param cible le statut cible
     * @param auteur l'auteur de la transition
     * @param commentaire le commentaire optionnel
     * @throws InvalidStateTransitionException si la transition n'est pas autorisée
     */
    @Transactional
    public void transitionner(DemandeRecrutement demande, StatutDemande cible, String auteur, String commentaire) {
        StatutDemande ancien = demande.getStatut();
        if (!ancien.peutPasserA(cible)) {
            throw new InvalidStateTransitionException("DemandeRecrutement", ancien, cible);
        }
        demande.setStatut(cible);
        historiqueStatutService.enregistrer(
                TypeEntite.DEMANDE_RECRUTEMENT,
                demande.getId(),
                ancien.name(),
                cible.name(),
                auteur,
                commentaire
        );
    }

    /**
     * Crée les validations du cycle pour une demande soumise.
     * Incrémente cycleCourant et crée les trois lignes Validation (ordre 1, 2, 3) à EN_ATTENTE.
     */
    @Transactional
    public void creerValidationsDuCycle(DemandeRecrutement demande) {
        int cycle = demande.getCycleCourant() + 1;
        demande.setCycleCourant(cycle);
        for (RoleValidateur role : RoleValidateur.values()) {
            validationRepository.save(Validation.builder()
                    .demande(demande)
                    .cycle(cycle)
                    .ordre(role.ordre())
                    .roleValidateur(role)
                    .decision(DecisionValidation.EN_ATTENTE)
                    .build());
        }
    }

    /**
     * Marque une demande comme étant en recrutement.
     * API interne : appelée par le module recrutement à la publication d'une offre.
     * Transition : VALIDEE → EN_RECRUTEMENT. Sans effet si déjà EN_RECRUTEMENT.
     *
     * @param demandeId l'identifiant de la demande
     * @param auteur l'auteur de l'action
     */
    @Transactional
    public void marquerEnRecrutement(Long demandeId, String auteur) {
        // API interne
        DemandeRecrutement demande = demandeRecrutementRepository.findById(demandeId)
                .orElseThrow(() -> com.gntfh.sirh_api.common.exception.ResourceNotFoundException.of("DemandeRecrutement", demandeId));
        if (demande.getStatut() == StatutDemande.EN_RECRUTEMENT) {
            return;
        }
        transitionner(demande, StatutDemande.EN_RECRUTEMENT, auteur, null);
    }

    /**
     * Enregistre une embauche pour une demande.
     * API interne : appelée par le module embauche.
     * Incrémente effectifPourvu. Si effectifPourvu == effectifDemande : EN_RECRUTEMENT → POURVUE (RG-24).
     *
     * @param demandeId l'identifiant de la demande
     * @param auteur l'auteur de l'action
     */
    @Transactional
    public void enregistrerEmbauche(Long demandeId, String auteur) {
        // API interne
        DemandeRecrutement demande = demandeRecrutementRepository.findById(demandeId)
                .orElseThrow(() -> com.gntfh.sirh_api.common.exception.ResourceNotFoundException.of("DemandeRecrutement", demandeId));
        int pourvu = demande.getEffectifPourvu() + 1;
        if (pourvu > demande.getEffectifDemande()) {
            throw new com.gntfh.sirh_api.common.exception.BusinessRuleException("L'effectif pourvu dépasse l'effectif demandé.");
        }
        demande.setEffectifPourvu(pourvu);
        if (pourvu == demande.getEffectifDemande()) {
            transitionner(demande, StatutDemande.POURVUE, auteur, null);
        }
    }

    /**
     * Calcule l'effectif restant à pourvoir pour une demande.
     * API interne.
     *
     * @param demandeId l'identifiant de la demande
     * @return effectifDemande - effectifPourvu
     */
    @Transactional(readOnly = true)
    public int effectifRestantAPourvoir(Long demandeId) {
        // API interne
        DemandeRecrutement demande = demandeRecrutementRepository.findById(demandeId)
                .orElseThrow(() -> com.gntfh.sirh_api.common.exception.ResourceNotFoundException.of("DemandeRecrutement", demandeId));
        return demande.getEffectifDemande() - demande.getEffectifPourvu();
    }
}
