package com.gntfh.sirh_api.embauche.service;

import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.document.service.DocumentService;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.InvalidStateTransitionException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.common.notification.service.NotificationService;
import com.gntfh.sirh_api.common.notification.enums.TypeNotification;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.common.config.SirhProperties;
import com.gntfh.sirh_api.demande.service.DemandeWorkflowService;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheResponse;
import com.gntfh.sirh_api.embauche.dto.ReponsePropositionRequest;
import com.gntfh.sirh_api.embauche.dto.ReponsePropositionResultat;
import com.gntfh.sirh_api.embauche.entity.PropositionEmbauche;
import com.gntfh.sirh_api.embauche.mapper.PropositionEmbaucheMapper;
import com.gntfh.sirh_api.embauche.repository.PropositionEmbaucheRepository;
import com.gntfh.sirh_api.employe.service.EmployeService;
import com.gntfh.sirh_api.employe.service.FicheSalariePdfService;
import com.gntfh.sirh_api.contrat.service.ContratService;
import com.gntfh.sirh_api.recrutement.service.CandidatureService;
import com.gntfh.sirh_api.recrutement.service.OffreEmploiService;
import com.gntfh.sirh_api.embauche.enums.ReponseCandidat;
import com.gntfh.sirh_api.embauche.enums.StatutProposition;
import com.gntfh.sirh_api.contrat.dto.ContratInitialCommande;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmbaucheServiceImpl implements EmbaucheService {

    private final PropositionEmbaucheRepository propositionRepository;
    private final PropositionEmbaucheMapper mapper;
    private final CandidatureService candidatureService;
    private final EmployeService employeService;
    private final FicheSalariePdfService ficheSalariePdfService;
    private final DocumentService documentService;
    private final ContratService contratService;
    private final DemandeWorkflowService demandeWorkflowService;
    private final OffreEmploiService offreEmploiService;
    private final HistoriqueStatutService historiqueStatutService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final SirhProperties sirhProperties;

    private UnsupportedOperationException todo(String codeRegle) {
        return new UnsupportedOperationException("TODO " + codeRegle);
    }

    /**
     * EMB-06, RG-28, RG-30 : la RH enregistre une réponse sur une proposition ENVOYEE ou DELAI_ACCORDE,
     * puis la dirige vers l'acceptation, le refus ou la demande de délai.
     *
     * @throws ResourceNotFoundException si la proposition n'existe pas
     * @throws InvalidStateTransitionException si la proposition n'attend plus de réponse
     */
    @Override
    @Transactional
    public ReponsePropositionResultat repondre(
            Long propositionId,
            ReponsePropositionRequest request,
            CurrentUser currentUser
    ) {
        var proposition = charger(propositionId);
        if (!proposition.getStatut().estActive() || proposition.getStatut() == StatutProposition.BROUILLON) {
            throw new InvalidStateTransitionException("PropositionEmbauche", proposition.getStatut(),
                    "réponse");
        }
        return switch (request.reponse()) {
            case ACCEPTER -> accepter(propositionId, currentUser.email());
            case REFUSER -> new ReponsePropositionResultat(
                    refuser(propositionId, request.motifRefus(), currentUser.email()), null, null, null);
            case DEMANDER_DELAI -> new ReponsePropositionResultat(
                    demanderDelai(propositionId, currentUser.email()), null, null, null);
        };
    }

    /**
     * RG-31, RG-32, RG-33, EMB-07 : réalise l'acceptation atomique en transaction unique.
     * Toute erreur annule l'ensemble.
     *
     * <p>Algorithme :
     * 1. Passe la proposition à ACCEPTEE et renseigne sa date de réponse.
     * 2. Appelle l'API interne {@code CandidatureService.marquerEmbauchee}.
     * 3. Appelle l'API interne {@code EmployeService.creerDepuisCandidature} pour créer le salarié PRE_EMBAUCHE
     *    avec un matricule unique, sans modifier le candidat ni la candidature.
     * 4. Génère la fiche PDF et l'archive via {@code DocumentService.enregistrer}.
     * 5. Appelle l'API interne {@code ContratService.creerContratInitial} avec le type proposé (ESSAI par défaut).
     * 6. Rattache le salarié à la proposition.
     * 7. Appelle {@code DemandeWorkflowService.enregistrerEmbauche}, puis
     *    {@code OffreEmploiService.cloturerSiEffectifAtteint}.
     * 8. Enregistre historique, audit et notification RH avec le matricule.
     *
     * <p>Seules les API internes documentées sont appelées ; aucun repository d'un autre module n'est utilisé.
     *
     * @throws ResourceNotFoundException si la proposition ou une ressource liée est absente
     * @throws BusinessRuleException si une étape métier de l'embauche échoue
     * @throws InvalidStateTransitionException si la proposition ne peut pas être acceptée
     */
    @Override
    @Transactional
    public ReponsePropositionResultat accepter(Long propositionId, String auteur) {
        var proposition = charger(propositionId);
        if (!proposition.getStatut().peutPasserA(StatutProposition.ACCEPTEE)) {
            throw new InvalidStateTransitionException("PropositionEmbauche", proposition.getStatut(),
                    StatutProposition.ACCEPTEE);
        }
        var auteurDate = java.time.Instant.now();
        proposition.setDateReponse(auteurDate);
        changerStatut(proposition, StatutProposition.ACCEPTEE, auteur, null);
        candidatureService.marquerEmbauchee(proposition.getCandidature().getId(), auteur);
        var employe = employeService.creerDepuisCandidature(proposition.getCandidature().getId(),
                proposition.getDateDebutPrevue(), auteur);
        try {
            var fiche = ficheSalariePdfService.generer(employe, java.util.Optional.empty());
            var document = documentService.enregistrer(fiche, "fiche-salarie-" + employe.getId() + ".pdf",
                    "application/pdf", TypeDocument.FICHE_SALARIE, TypeEntite.EMPLOYE, employe.getId());
            var contrat = contratService.creerContratInitial(new ContratInitialCommande(
                    employe.getId(), proposition.getPoste().getId(), null, proposition.getTypeContratPropose(),
                    proposition.getDateDebutPrevue(), proposition.getDureeMois(), proposition.getSalairePropose(),
                    proposition.getDevise(), proposition.getHorairesTravail(), proposition.getLieuTravail()), auteur);
            proposition.setEmploye(employe);
            var demande = proposition.getCandidature().getOffre().getDemande();
            demandeWorkflowService.enregistrerEmbauche(demande.getId(), auteur);
            offreEmploiService.cloturerSiEffectifAtteint(demande.getId(), auteur);
            var response = mapper.toResponse(proposition);
            auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.PROPOSITION_EMBAUCHE,
                    propositionId, null, response, auteur);
            notificationService.notifierParRole(com.gntfh.sirh_api.auth.enums.Role.RH,
                    TypeNotification.PROPOSITION_REPONDUE, "Proposition acceptée",
                    "La proposition a été acceptée pour le matricule " + employe.getMatricule() + ".",
                    TypeEntite.PROPOSITION_EMBAUCHE, propositionId);
            return new ReponsePropositionResultat(response, employe.getId(), employe.getMatricule(), contrat.getId());
        } catch (java.io.IOException exception) {
            throw new BusinessRuleException("Impossible d'archiver la fiche salarié.");
        }
    }

    /**
     * RG-30, EMB-08 : passe la proposition à REFUSEE, conserve le motif, met à jour la candidature
     * et notifie la RH pour permettre le traitement du candidat suivant.
     *
     * @throws BusinessRuleException si le motif est absent
     * @throws InvalidStateTransitionException si la proposition ne peut pas être refusée
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse refuser(Long propositionId, String motif, String auteur) {
        if (motif == null || motif.isBlank()) {
            throw new BusinessRuleException("Le motif de refus est obligatoire (EMB-08).");
        }
        var proposition = charger(propositionId);
        changerStatut(proposition, StatutProposition.REFUSEE, auteur, motif);
        proposition.setDateReponse(java.time.Instant.now());
        proposition.setMotifRefus(motif);
        candidatureService.marquerNonRetenueApresProposition(
                proposition.getCandidature().getId(), auteur, motif);
        var response = mapper.toResponse(proposition);
        notificationService.notifierParRole(com.gntfh.sirh_api.auth.enums.Role.RH,
                TypeNotification.PROPOSITION_REPONDUE, "Proposition refusée",
                "La proposition d'embauche a été refusée.", TypeEntite.PROPOSITION_EMBAUCHE, propositionId);
        return response;
    }

    /**
     * RG-28 : accorde une seule prolongation et ajoute le délai configuré à la date limite de réponse.
     *
     * @throws BusinessRuleException si une prolongation a déjà été accordée
     * @throws InvalidStateTransitionException si la proposition n'attend pas de réponse
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse demanderDelai(Long propositionId, String auteur) {
        var proposition = charger(propositionId);
        if (!proposition.getStatut().peutPasserA(StatutProposition.DELAI_ACCORDE)
                || proposition.getNbProlongationsDelai() >= 1) {
            throw new BusinessRuleException("Une seule prolongation de délai est autorisée (RG-28).");
        }
        proposition.setDateLimiteReponse(proposition.getDateLimiteReponse()
                .plusDays(sirhProperties.proposition().dureeProlongationJours()));
        proposition.setNbProlongationsDelai(1);
        changerStatut(proposition, StatutProposition.DELAI_ACCORDE, auteur, null);
        return mapper.toResponse(proposition);
    }

    /**
     * RG-29, RG-30 : expire les propositions ENVOYEE ou DELAI_ACCORDE dont la date limite est antérieure
     * au jour de référence, met leur candidature à NON_RETENUE, notifie la RH et retourne le nombre traité.
     */
    @Override
    @Transactional
    public int expirerPropositionsEchues(LocalDate aujourdhui) {
        int traitees = 0;
        for (var proposition : propositionRepository.findEchues(aujourdhui)) {
            changerStatut(proposition, StatutProposition.EXPIREE, "SYSTEME", "Délai de réponse expiré");
            candidatureService.marquerNonRetenueApresProposition(
                    proposition.getCandidature().getId(), "SYSTEME", "Proposition expirée");
            notificationService.notifierParRole(com.gntfh.sirh_api.auth.enums.Role.RH,
                    TypeNotification.PROPOSITION_EXPIREE, "Proposition expirée",
                    "Une proposition d'embauche est arrivée à échéance.",
                    TypeEntite.PROPOSITION_EMBAUCHE, proposition.getId());
            traitees++;
        }
        return traitees;
    }

    private PropositionEmbauche charger(Long id) {
        return propositionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("PropositionEmbauche", id));
    }

    private void changerStatut(PropositionEmbauche proposition, StatutProposition cible,
                               String auteur, String commentaire) {
        var ancien = proposition.getStatut();
        if (!ancien.peutPasserA(cible)) {
            throw new InvalidStateTransitionException("PropositionEmbauche", ancien, cible);
        }
        proposition.setStatut(cible);
        historiqueStatutService.enregistrer(TypeEntite.PROPOSITION_EMBAUCHE, proposition.getId(),
                ancien.name(), cible.name(), auteur, commentaire);
    }
}
