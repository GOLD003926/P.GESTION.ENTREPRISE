package com.gntfh.sirh_api.demande.service;

import com.gntfh.sirh_api.common.exception.InvalidStateTransitionException;
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
        throw new UnsupportedOperationException("TODO : vérifier peutPasserA et appliquer la transition");
    }

    /**
     * Crée les validations du cycle pour une demande soumise.
     * Incrémente cycleCourant et crée les trois lignes Validation (ordre 1, 2, 3) à EN_ATTENTE.
     */
    @Transactional
    public void creerValidationsDuCycle(DemandeRecrutement demande) {
        throw new UnsupportedOperationException("TODO : incrémenter cycleCourant et créer les validations RH, Finance, DG");
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
        throw new UnsupportedOperationException("TODO DEM-13 : VALIDEE → EN_RECRUTEMENT");
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
        throw new UnsupportedOperationException("TODO RG-24 : incrémenter effectifPourvu et passer à POURVUE si complet");
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
        throw new UnsupportedOperationException("TODO : retourner effectifDemande - effectifPourvu");
    }
}
