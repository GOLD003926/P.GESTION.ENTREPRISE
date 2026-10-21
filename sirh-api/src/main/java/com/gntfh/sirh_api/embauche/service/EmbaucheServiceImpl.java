package com.gntfh.sirh_api.embauche.service;

import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.document.service.DocumentService;
import com.gntfh.sirh_api.common.notification.service.NotificationService;
import com.gntfh.sirh_api.common.security.CurrentUser;
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
        throw todo("EMB-06");
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
        throw todo("EMB-07");
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
        throw todo("EMB-08");
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
        throw todo("RG-28");
    }

    /**
     * RG-29, RG-30 : expire les propositions ENVOYEE ou DELAI_ACCORDE dont la date limite est antérieure
     * au jour de référence, met leur candidature à NON_RETENUE, notifie la RH et retourne le nombre traité.
     */
    @Override
    @Transactional
    public int expirerPropositionsEchues(LocalDate aujourdhui) {
        throw todo("RG-29");
    }
}
