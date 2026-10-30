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
import com.gntfh.sirh_api.recrutement.enums.DecisionFinale;
import com.gntfh.sirh_api.recrutement.enums.StatutCandidature;
import com.gntfh.sirh_api.recrutement.enums.StatutEntretien;
import com.gntfh.sirh_api.recrutement.entity.Candidature;
import com.gntfh.sirh_api.recrutement.entity.Entretien;
import com.gntfh.sirh_api.recrutement.mapper.CandidatureMapper;
import com.gntfh.sirh_api.recrutement.mapper.EntretienMapper;
import com.gntfh.sirh_api.recrutement.repository.CandidatureRepository;
import com.gntfh.sirh_api.recrutement.repository.EntretienIntervenantRepository;
import com.gntfh.sirh_api.recrutement.repository.EntretienRepository;
import com.gntfh.sirh_api.recrutement.repository.EvaluationCandidatRepository;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.InvalidStateTransitionException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service SelectionWorkflowService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SelectionWorkflowServiceImpl implements SelectionWorkflowService {
    private final CandidatureRepository candidatureRepository;
    private final EntretienRepository entretienRepository;
    private final EntretienIntervenantRepository intervenantRepository;
    private final EvaluationCandidatRepository evaluationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CandidatureMapper candidatureMapper;
    private final EntretienMapper entretienMapper;
    private final HistoriqueStatutService historiqueService;

    @Override
    @Transactional
    public CandidatureResponse preselectionner(Long candidatureId, PreselectionRequest request, CurrentUser currentUser) {
        Candidature candidature = candidature(candidatureId);
        if (candidature.getStatut() != StatutCandidature.RECUE) {
            throw new BusinessRuleException("La candidature doit être au statut RECUE.");
        }
        StatutCandidature cible = Boolean.TRUE.equals(request.accepter())
                ? StatutCandidature.PRESELECTIONNEE : StatutCandidature.REFUSEE_DOSSIER;
        if (!Boolean.TRUE.equals(request.accepter()) && (request.motif() == null || request.motif().isBlank())) {
            throw new BusinessRuleException("Le motif est obligatoire pour refuser une candidature.");
        }
        candidature.setScorePreselection(request.score());
        candidature.setMotifRefus(Boolean.TRUE.equals(request.accepter()) ? null : request.motif());
        transition(candidature, cible, currentUser.email(), request.motif());
        return candidatureMapper.toResponse(candidature);
    }

    @Override
    @Transactional
    public EntretienResponse planifierEntretien(EntretienRequest request, CurrentUser currentUser) {
        Candidature candidature = candidature(request.candidatureId());
        int niveau = request.niveau();
        int maximum = candidature.getOffre().getNombreNiveauxSelection();
        if (niveau > maximum) {
            throw new BusinessRuleException("Le niveau demandé dépasse le nombre de niveaux de l'offre.");
        }
        if (niveau == 1) {
            if (candidature.getStatut() != StatutCandidature.PRESELECTIONNEE) {
                throw new BusinessRuleException("Le niveau 1 exige une candidature présélectionnée.");
            }
        } else {
            Entretien precedent = entretienRepository
                    .findActifByCandidatureIdAndNiveau(candidature.getId(), niveau - 1)
                    .orElseThrow(() -> new BusinessRuleException("Le niveau précédent doit être réalisé."));
            if (precedent.getStatut() != StatutEntretien.REALISE
                    || avisDuNiveau(candidature.getId(), niveau - 1) != Avis.FAVORABLE) {
                throw new BusinessRuleException("Le niveau précédent doit être réalisé avec un avis favorable.");
            }
        }
        if (entretienRepository.countActifByCandidatureIdAndNiveau(candidature.getId(), niveau) > 0) {
            throw new BusinessRuleException("Un entretien non annulé existe déjà pour ce niveau.");
        }
        verifierIntervenants(request.intervenants());
        if (candidature.getStatut() == StatutCandidature.PRESELECTIONNEE) {
            transition(candidature, StatutCandidature.EN_ENTRETIEN, currentUser.email(), null);
        }
        Entretien entretien = entretienRepository.save(entretienMapper.toEntity(request, candidature));
        enregistrerIntervenants(entretien, request.intervenants());
        return entretienResponse(entretien);
    }

    @Override
    @Transactional
    public EntretienResponse reprogrammer(Long entretienId, EntretienUpdateRequest request, CurrentUser currentUser) {
        Entretien entretien = entretien(entretienId);
        if (!entretien.getStatut().peutPasserA(StatutEntretien.REPORTE)) {
            throw new InvalidStateTransitionException("Entretien", entretien.getStatut(), StatutEntretien.REPORTE);
        }
        entretien.setStatut(StatutEntretien.REPORTE);
        entretienMapper.updateEntity(request, entretien);
        if (request.intervenants() != null) {
            verifierIntervenants(request.intervenants());
            intervenantRepository.deleteAll(intervenantRepository.findByEntretienId(entretien.getId()));
            enregistrerIntervenants(entretien, request.intervenants());
        }
        entretien.setStatut(StatutEntretien.PLANIFIE);
        return entretienResponse(entretien);
    }

    @Override
    @Transactional
    public EntretienResponse cloturerEntretien(Long entretienId, CurrentUser currentUser) {
        Entretien entretien = entretien(entretienId);
        if (evaluationRepository.countActiveAndVerrouilleeByEntretienId(entretienId) == 0) {
            throw new BusinessRuleException("Au moins une évaluation active et verrouillée est requise.");
        }
        if (!entretien.getStatut().peutPasserA(StatutEntretien.REALISE)) {
            throw new InvalidStateTransitionException("Entretien", entretien.getStatut(), StatutEntretien.REALISE);
        }
        entretien.setStatut(StatutEntretien.REALISE);
        Avis avis = avisDuNiveau(entretien.getCandidature().getId(), entretien.getNiveau());
        if (avis == Avis.DEFAVORABLE) {
            Candidature candidature = entretien.getCandidature();
            transition(candidature, StatutCandidature.NON_RETENUE, currentUser.email(), "Avis défavorable au niveau " + entretien.getNiveau());
        }
        return entretienResponse(entretien);
    }

    @Override
    @Transactional
    public EntretienResponse annulerEntretien(Long id, AnnulationEntretienRequest request, CurrentUser currentUser) {
        Entretien entretien = entretien(id);
        if (!entretien.getStatut().peutPasserA(StatutEntretien.ANNULE)) {
            throw new InvalidStateTransitionException("Entretien", entretien.getStatut(), StatutEntretien.ANNULE);
        }
        entretien.setStatut(StatutEntretien.ANNULE);
        entretien.setMotifAnnulation(request.motif());
        return entretienResponse(entretien);
    }

    @Override
    @Transactional
    public EntretienResponse marquerAbsent(Long id, CurrentUser currentUser) {
        Entretien entretien = entretien(id);
        if (!entretien.getStatut().peutPasserA(StatutEntretien.CANDIDAT_ABSENT)) {
            throw new InvalidStateTransitionException("Entretien", entretien.getStatut(), StatutEntretien.CANDIDAT_ABSENT);
        }
        entretien.setStatut(StatutEntretien.CANDIDAT_ABSENT);
        return entretienResponse(entretien);
    }

    @Override
    @Transactional
    public CandidatureResponse decisionFinale(Long candidatureId, DecisionFinaleRequest request, CurrentUser currentUser) {
        Candidature candidature = candidature(candidatureId);
        DecisionFinale decision;
        try {
            decision = DecisionFinale.valueOf(request.decision());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("La décision finale est invalide.");
        }
        if (decision == DecisionFinale.NON_RETENU && (request.motif() == null || request.motif().isBlank())) {
            throw new BusinessRuleException("Le motif est obligatoire pour une décision non retenue.");
        }
        int niveaux = candidature.getOffre().getNombreNiveauxSelection();
        for (int niveau = 1; niveau <= niveaux; niveau++) {
            Entretien entretien = entretienRepository.findActifByCandidatureIdAndNiveau(candidatureId, niveau)
                    .orElseThrow(() -> new BusinessRuleException("Tous les niveaux requis doivent être réalisés."));
            if (entretien.getStatut() != StatutEntretien.REALISE || avisDuNiveau(candidatureId, niveau) != Avis.FAVORABLE) {
                throw new BusinessRuleException("Tous les niveaux requis doivent être réalisés avec un avis favorable.");
            }
        }
        StatutCandidature cible = decision == DecisionFinale.RETENU
                ? StatutCandidature.RETENUE
                : decision == DecisionFinale.NON_RETENU ? StatutCandidature.NON_RETENUE : StatutCandidature.LISTE_ATTENTE;
        if (cible == StatutCandidature.RETENUE
                && candidature.getOffre().getDemande().getEffectifDemande()
                - candidature.getOffre().getDemande().getEffectifPourvu() <= 0) {
            cible = StatutCandidature.LISTE_ATTENTE;
            candidature.setRangListeAttente(
                    candidatureRepository.maxRangListeAttenteByOffreId(candidature.getOffre().getId()) + 1);
            decision = DecisionFinale.LISTE_ATTENTE;
        }
        transition(candidature, cible, currentUser.email(), request.motif());
        candidature.setDecisionFinale(decision);
        candidature.setDecideur(utilisateurRepository.findById(currentUser.id())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", currentUser.id())));
        candidature.setDateDecision(java.time.Instant.now());
        candidature.setMotifDecision(request.motif());
        return candidatureMapper.toResponse(candidature);
    }

    @Override
    public Avis avisDuNiveau(Long candidatureId, int niveau) {
        if (niveau < 1 || niveau > 3) {
            throw new BusinessRuleException("Le niveau doit être entre 1 et 3.");
        }
        Entretien entretien = entretienRepository.findActifByCandidatureIdAndNiveau(candidatureId, niveau)
                .orElseThrow(() -> ResourceNotFoundException.of("Entretien", candidatureId));
        var evaluations = evaluationRepository.findActiveByEntretienId(entretien.getId());
        if (evaluations.isEmpty()) {
            throw new BusinessRuleException("Aucune évaluation active pour ce niveau.");
        }
        if (evaluations.stream().anyMatch(e -> e.getAvis() == Avis.DEFAVORABLE)) return Avis.DEFAVORABLE;
        if (evaluations.stream().allMatch(e -> e.getAvis() == Avis.FAVORABLE)) return Avis.FAVORABLE;
        return Avis.RESERVE;
    }

    private Candidature candidature(Long id) {
        return candidatureRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Candidature", id));
    }

    private Entretien entretien(Long id) {
        return entretienRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Entretien", id));
    }

    private EntretienResponse entretienResponse(Entretien entretien) {
        String avis = null;
        if (entretien.getStatut() == StatutEntretien.REALISE) {
            avis = avisDuNiveau(entretien.getCandidature().getId(), entretien.getNiveau()).name();
        }
        return entretienMapper.toResponse(entretien,
                intervenantRepository.findByEntretienId(entretien.getId()), avis);
    }

    private void verifierIntervenants(java.util.List<com.gntfh.sirh_api.recrutement.dto.IntervenantRequest> intervenants) {
        if (intervenants == null || intervenants.isEmpty()) {
            throw new BusinessRuleException("Au moins un intervenant est obligatoire.");
        }
        for (var request : intervenants) {
            var utilisateur = utilisateurRepository.findById(request.utilisateurId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", request.utilisateurId()));
            if (!utilisateur.isActif() || utilisateur.getRole() == Role.SALARIE) {
                throw new BusinessRuleException("Les intervenants doivent être des utilisateurs actifs non salariés.");
            }
        }
    }

    private void enregistrerIntervenants(Entretien entretien,
            java.util.List<com.gntfh.sirh_api.recrutement.dto.IntervenantRequest> intervenants) {
        for (var request : intervenants) {
            var utilisateur = utilisateurRepository.findById(request.utilisateurId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", request.utilisateurId()));
            intervenantRepository.save(com.gntfh.sirh_api.recrutement.entity.EntretienIntervenant.builder()
                    .entretien(entretien).utilisateur(utilisateur)
                    .roleIntervenant(request.roleIntervenant()).build());
        }
    }

    private void transition(Candidature candidature, StatutCandidature cible, String auteur, String commentaire) {
        StatutCandidature ancien = candidature.getStatut();
        if (!ancien.peutPasserA(cible)) {
            throw new InvalidStateTransitionException("Candidature", ancien, cible);
        }
        candidature.setStatut(cible);
        historiqueService.enregistrer(TypeEntite.CANDIDATURE, candidature.getId(),
                ancien.name(), cible.name(), auteur, commentaire);
    }
}
