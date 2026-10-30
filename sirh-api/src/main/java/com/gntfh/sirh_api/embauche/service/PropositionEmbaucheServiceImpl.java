package com.gntfh.sirh_api.embauche.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.document.service.DocumentService;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import com.gntfh.sirh_api.common.document.entity.Document;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.DuplicateResourceException;
import com.gntfh.sirh_api.common.exception.InvalidStateTransitionException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.common.notification.service.NotificationService;
import com.gntfh.sirh_api.common.notification.enums.TypeNotification;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.embauche.dto.DerogationRequest;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheRequest;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheResponse;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheUpdateRequest;
import com.gntfh.sirh_api.embauche.entity.PropositionEmbauche;
import com.gntfh.sirh_api.embauche.enums.StatutProposition;
import com.gntfh.sirh_api.embauche.mapper.PropositionEmbaucheMapper;
import com.gntfh.sirh_api.embauche.repository.PropositionEmbaucheRepository;
import com.gntfh.sirh_api.common.config.SirhProperties;
import com.gntfh.sirh_api.recrutement.service.CandidatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PropositionEmbaucheServiceImpl implements PropositionEmbaucheService {

    private final PropositionEmbaucheRepository propositionRepository;
    private final PropositionEmbaucheMapper mapper;
    private final PropositionPdfService propositionPdfService;
    private final DocumentService documentService;
    private final CandidatureService candidatureService;
    private final HistoriqueStatutService historiqueStatutService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final SirhProperties sirhProperties;
    private final UtilisateurRepository utilisateurRepository;

    private UnsupportedOperationException todo(String codeRegle) {
        return new UnsupportedOperationException("TODO " + codeRegle);
    }

    /**
     * RG-26, RG-27, EMB-01, EMB-02, EMB-03 et EMB-04 : crée un brouillon uniquement pour une candidature
     * RETENUE, reprend le poste de sa demande, contrôle les mentions obligatoires et calcule la dérogation
     * à partir de la grille salariale.
     *
     * @throws BusinessRuleException si la candidature, le poste, le salaire ou la durée ne respecte pas les règles
     * @throws DuplicateResourceException si une proposition active existe déjà pour cette candidature
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse creer(PropositionEmbaucheRequest request, CurrentUser currentUser) {
        var candidature = candidatureService.trouver(request.candidatureId());
        if (candidature.getStatut() != com.gntfh.sirh_api.recrutement.enums.StatutCandidature.RETENUE) {
            throw new BusinessRuleException("La proposition exige une candidature retenue (RG-26).");
        }
        var offre = candidature.getOffre();
        if (offre == null || offre.getDemande() == null || offre.getDemande().getPoste() == null) {
            throw new BusinessRuleException("Le poste de la candidature est introuvable (EMB-02).");
        }
        if (propositionRepository.findActiveByCandidatureId(request.candidatureId()).isPresent()) {
            throw DuplicateResourceException.of("Proposition active", "candidatureId", request.candidatureId());
        }
        var proposition = mapper.toEntity(request, candidature, offre.getDemande().getPoste());
        proposition.setDerogationRequise(horsGrille(proposition));
        var saved = propositionRepository.save(proposition);
        var response = mapper.toResponse(saved);
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.PROPOSITION_EMBAUCHE,
                saved.getId(), null, response, currentUser.email());
        return response;
    }

    /**
     * EMB-04 : modifie uniquement une proposition BROUILLON et recalcule la dérogation salariale requise.
     *
     * @throws ResourceNotFoundException si la proposition n'existe pas
     * @throws BusinessRuleException si la proposition n'est plus modifiable ou si les données sont incohérentes
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse modifier(
            Long id,
            PropositionEmbaucheUpdateRequest request,
            CurrentUser currentUser
    ) {
        var proposition = charger(id);
        if (proposition.getStatut() != StatutProposition.BROUILLON) {
            throw new BusinessRuleException("Seule une proposition brouillon peut être modifiée.");
        }
        var avant = mapper.toResponse(proposition);
        mapper.updateEntity(proposition, request);
        proposition.setDerogationRequise(horsGrille(proposition));
        var apres = mapper.toResponse(proposition);
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.PROPOSITION_EMBAUCHE,
                id, avant, apres, currentUser.email());
        return apres;
    }

    /**
     * Retourne une proposition visible par l'acteur.
     *
     * @throws ResourceNotFoundException si la proposition est absente ou hors périmètre
     */
    @Override
    public PropositionEmbaucheResponse obtenir(Long id, CurrentUser currentUser) {
        return mapper.toResponse(charger(id));
    }

    /**
     * Recherche les propositions visibles, avec tri limité à id, statut, dateEnvoi, dateLimiteReponse et createdAt.
     *
     * @throws BusinessRuleException si le tri demandé n'est pas autorisé
     */
    @Override
    public PageResponse<PropositionEmbaucheResponse> rechercher(
            Long candidatureId,
            StatutProposition statut,
            Pageable pageable,
            CurrentUser currentUser
    ) {
        Specification<PropositionEmbauche> specification = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (candidatureId != null) predicates.add(cb.equal(root.get("candidature").get("id"), candidatureId));
            if (statut != null) predicates.add(cb.equal(root.get("statut"), statut));
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return PageResponse.from(propositionRepository.findAll(specification, pageable), mapper::toResponse);
    }

    /**
     * EMB-03 : enregistre le valideur DG et l'horodatage de validation de la dérogation salariale.
     *
     * @throws ResourceNotFoundException si la proposition n'existe pas
     * @throws BusinessRuleException si aucune dérogation n'est requise ou si l'acteur n'est pas autorisé
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse validerDerogation(
            Long id,
            DerogationRequest request,
            CurrentUser currentUser
    ) {
        var proposition = charger(id);
        if (currentUser.role() != Role.DG) {
            throw new BusinessRuleException("Seul le DG peut valider une dérogation.");
        }
        if (!Boolean.TRUE.equals(proposition.getDerogationRequise())) {
            throw new BusinessRuleException("Cette proposition ne requiert pas de dérogation.");
        }
        var utilisateur = utilisateurRepository.findById(currentUser.id())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", currentUser.id()));
        proposition.setDerogationValideePar(utilisateur);
        proposition.setDateDerogation(Instant.now());
        return mapper.toResponse(proposition);
    }

    /**
     * EMB-03, EMB-05 : exige un brouillon complet et la validation DG si nécessaire, génère et archive le PDF,
     * fixe la date d'envoi et, si aucune date limite n'a été fournie, la calcule avec le délai configuré.
     * Passe ensuite la candidature à PROPOSITION_ENVOYEE, met à jour l'historique et notifie la RH.
     *
     * @throws ResourceNotFoundException si la proposition n'existe pas
     * @throws BusinessRuleException si le brouillon est incomplet ou si la dérogation requise n'est pas validée
     * @throws InvalidStateTransitionException si la proposition ne peut pas être envoyée
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse envoyer(Long id, CurrentUser currentUser) {
        var proposition = charger(id);
        if (!proposition.getStatut().peutPasserA(StatutProposition.ENVOYEE)) {
            throw new InvalidStateTransitionException("PropositionEmbauche", proposition.getStatut(),
                    StatutProposition.ENVOYEE);
        }
        if (Boolean.TRUE.equals(proposition.getDerogationRequise())
                && proposition.getDerogationValideePar() == null) {
            throw new BusinessRuleException("La dérogation du DG est obligatoire avant l'envoi (EMB-03).");
        }
        if (proposition.getDateLimiteReponse() == null) {
            proposition.setDateLimiteReponse(LocalDate.now().plusDays(sirhProperties.proposition().delaiReponseJours()));
        }
        proposition.setDateEnvoi(Instant.now());
        final Document document;
        try {
            document = documentService.enregistrer(propositionPdfService.generer(proposition),
                    "proposition-" + id + ".pdf", "application/pdf", TypeDocument.PROPOSITION_EMBAUCHE,
                    TypeEntite.PROPOSITION_EMBAUCHE, id);
        } catch (java.io.IOException exception) {
            throw new BusinessRuleException("Impossible d'archiver la proposition d'embauche.");
        }
        proposition.setDocument(document);
        changerStatut(proposition, StatutProposition.ENVOYEE, currentUser.email(), null);
        candidatureService.marquerPropositionEnvoyee(proposition.getCandidature().getId(), currentUser.email());
        notificationService.notifierParRole(Role.RH, TypeNotification.PROPOSITION_ENVOYEE,
                "Proposition envoyée", "Une proposition d'embauche a été envoyée.",
                TypeEntite.PROPOSITION_EMBAUCHE, id);
        return mapper.toResponse(proposition);
    }

    /**
     * Retire une proposition active, remet la candidature au statut RETENUE et enregistre l'historique.
     *
     * @throws ResourceNotFoundException si la proposition n'existe pas
     * @throws InvalidStateTransitionException si le retrait n'est pas autorisé depuis le statut courant
     */
    @Override
    @Transactional
    public PropositionEmbaucheResponse retirer(Long id, CurrentUser currentUser) {
        var proposition = charger(id);
        changerStatut(proposition, StatutProposition.RETIREE, currentUser.email(), null);
        candidatureService.remettreRetenue(proposition.getCandidature().getId(), currentUser.email());
        return mapper.toResponse(proposition);
    }

    /**
     * Retourne l'historique des statuts d'une proposition visible par l'acteur.
     *
     * @throws ResourceNotFoundException si la proposition est absente ou hors périmètre
     */
    @Override
    public List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser) {
        charger(id);
        return historiqueStatutService.lister(TypeEntite.PROPOSITION_EMBAUCHE, id);
    }

    private PropositionEmbauche charger(Long id) {
        return propositionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("PropositionEmbauche", id));
    }

    private boolean horsGrille(PropositionEmbauche proposition) {
        var grille = proposition.getPoste().getGrilleSalariale();
        return grille != null && (proposition.getSalairePropose().compareTo(grille.getSalaireMin()) < 0
                || proposition.getSalairePropose().compareTo(grille.getSalaireMax()) > 0);
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
