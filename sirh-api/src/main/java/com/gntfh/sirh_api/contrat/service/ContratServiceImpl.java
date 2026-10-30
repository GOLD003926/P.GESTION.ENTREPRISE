package com.gntfh.sirh_api.contrat.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.config.SirhProperties;
import com.gntfh.sirh_api.common.document.enums.TypeDocument;
import com.gntfh.sirh_api.common.document.service.DocumentService;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.InvalidStateTransitionException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.common.numerotation.NumeroService;
import com.gntfh.sirh_api.common.numerotation.SequenceNumero;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.contrat.dto.ContratInitialCommande;
import com.gntfh.sirh_api.contrat.dto.ContratRequest;
import com.gntfh.sirh_api.contrat.dto.ContratResponse;
import com.gntfh.sirh_api.contrat.dto.ContratUpdateRequest;
import com.gntfh.sirh_api.contrat.dto.FinContratRequest;
import com.gntfh.sirh_api.contrat.dto.RenouvellementRequest;
import com.gntfh.sirh_api.contrat.dto.TransformationRequest;
import com.gntfh.sirh_api.contrat.entity.Contrat;
import com.gntfh.sirh_api.contrat.enums.StatutContrat;
import com.gntfh.sirh_api.contrat.enums.TypeContrat;
import com.gntfh.sirh_api.contrat.event.ContratAnnuleEvent;
import com.gntfh.sirh_api.contrat.event.ContratCreeEvent;
import com.gntfh.sirh_api.contrat.event.ContratSigneEvent;
import com.gntfh.sirh_api.contrat.mapper.ContratMapper;
import com.gntfh.sirh_api.contrat.repository.ContratRepository;
import com.gntfh.sirh_api.contrat.rule.ContratRuleValidator;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.employe.repository.AffectationRepository;
import com.gntfh.sirh_api.employe.repository.EmployeRepository;
import com.gntfh.sirh_api.employe.service.AffectationService;
import com.gntfh.sirh_api.employe.service.EmployeService;
import com.gntfh.sirh_api.organisation.entity.Poste;
import com.gntfh.sirh_api.organisation.repository.PosteRepository;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.notification.enums.TypeNotification;
import com.gntfh.sirh_api.common.notification.service.NotificationService;
import com.gntfh.sirh_api.auth.enums.Role;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Squelette de service du module contrat.
 * Les règles détaillées restent à implémenter par l'équipe métier.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContratServiceImpl implements ContratService {

    private final ContratRepository contratRepository;
    private final ContratMapper contratMapper;
    private final ContratRuleValidator ruleValidator;
    private final EmployeRepository employeRepository;
    private final PosteRepository posteRepository;
    private final AffectationRepository affectationRepository;
    private final EmployeService employeService;
    private final AffectationService affectationService;
    private final NumeroService numeroService;
    private final DocumentService documentService;
    private final HistoriqueStatutService historiqueStatutService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final SirhProperties sirhProperties;

    /**
     * RG-33 à RG-38, CON-01 à CON-03, CON-05, CON-14.
     */
    @Override
    @Transactional
    public ContratResponse creer(ContratRequest request, CurrentUser currentUser) {
        Contrat contrat = creerEntite(request.employeId(), request.posteId(), request.managerId(),
                request.type(), request.dateDebut(), request.dureeMois(), request.salaireBase(),
                request.devise(), request.horairesTravail(), request.lieuTravail(), null, 0);
        Contrat saved = contratRepository.save(contrat);
        historiqueStatutService.enregistrer(TypeEntite.CONTRAT, saved.getId(), null,
                saved.getStatut().name(), currentUser.email(), null);
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.CONTRAT, saved.getId(), null,
                contratMapper.toResponse(saved), currentUser.email());
        eventPublisher.publishEvent(new ContratCreeEvent(saved.getId(), saved.getEmploye().getId(),
                saved.getType(), saved.getDateDebut()));
        return contratMapper.toResponse(saved);
    }

    /**
     * CON-09, CON-14.
     */
    @Override
    @Transactional
    public ContratResponse modifier(Long id, ContratUpdateRequest request, CurrentUser currentUser) {
        Contrat contrat = trouver(id);
        verifierVisible(contrat, currentUser);
        if (contrat.getStatut() != StatutContrat.BROUILLON) {
            throw new BusinessRuleException("Seul un contrat brouillon peut être modifié.");
        }
        ContratResponse avant = contratMapper.toResponse(contrat);
        ruleValidator.verifierCreation(request.type(), request.dateDebut(), request.dureeMois());
        Poste poste = poste(request.posteId());
        Employe manager = manager(request.managerId());
        contratMapper.updateEntity(contrat, request, poste, manager,
                request.type().aDateFin() ? ruleValidator.calculerFinPrevue(request.dateDebut(), request.dureeMois()) : null);
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.CONTRAT, id, avant,
                contratMapper.toResponse(contrat), currentUser.email());
        return contratMapper.toResponse(contrat);
    }

    /**
     * CON-12 : contrôle la visibilité du contrat selon le rôle et le périmètre.
     */
    @Override
    public ContratResponse obtenir(Long id, CurrentUser currentUser) {
        Contrat contrat = trouver(id);
        verifierVisible(contrat, currentUser);
        return contratMapper.toResponse(contrat);
    }

    /**
     * CON-12 : contrôle la visibilité, les filtres et la liste blanche de tri
     * (id, numero, dateDebut, dateFinPrevue, statut).
     */
    @Override
    public PageResponse<ContratResponse> rechercher(
            Long employeId,
            TypeContrat type,
            StatutContrat statut,
            LocalDate finAvant,
            Pageable pageable,
            CurrentUser currentUser
    ) {
        Specification<Contrat> specification = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (employeId != null) predicates.add(cb.equal(root.get("employe").get("id"), employeId));
            if (type != null) predicates.add(cb.equal(root.get("type"), type));
            if (statut != null) predicates.add(cb.equal(root.get("statut"), statut));
            if (finAvant != null) predicates.add(cb.lessThanOrEqualTo(root.get("dateFinPrevue"), finAvant));
            if (currentUser.role() == Role.MANAGER) {
                predicates.add(root.get("employe").get("id").in(
                        affectationRepository.findActivesByManagerId(employeConnecte(currentUser).getId(), LocalDate.now()).stream()
                                .map(a -> a.getEmploye().getId()).toList()));
            } else if (currentUser.role() == Role.SALARIE) {
                predicates.add(cb.equal(root.get("employe").get("emailPro"), currentUser.email()));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        verifierTri(pageable);
        return PageResponse.from(contratRepository.findAll(specification, pageable), contratMapper::toResponse);
    }

    /**
     * CON-12.
     */
    @Override
    public PageResponse<ContratResponse> mesContrats(CurrentUser currentUser) {
        Employe employe = employeRepository.findByEmailProIgnoreCase(currentUser.email())
                .orElseThrow(() -> ResourceNotFoundException.of("Employé", currentUser.email()));
        return rechercher(employe.getId(), null, null, null,
                Pageable.ofSize(20), currentUser);
    }

    /**
     * RG-39, CON-10 : enregistre le document signé puis tente l'activation.
     */
    @Override
    @Transactional
    public void envoyerPourSignature(Long id, CurrentUser currentUser) {
        Contrat contrat = trouver(id);
        verifierVisible(contrat, currentUser);
        transitionner(contrat, StatutContrat.A_SIGNER, currentUser.email(), null);
    }

    /**
     * CON-10.
     */
    @Override
    @Transactional
    public void enregistrerSignature(Long id, MultipartFile fichier, LocalDate dateSignature, CurrentUser currentUser) {
        Contrat contrat = trouver(id);
        verifierVisible(contrat, currentUser);
        if (contrat.getStatut() != StatutContrat.A_SIGNER) {
            throw new BusinessRuleException("Le contrat doit être à signer.");
        }
        try {
            var document = documentService.enregistrer(fichier, TypeDocument.CONTRAT_SIGNE,
                    TypeEntite.CONTRAT, id);
            contrat.setDocumentSigne(document);
            contrat.setDateSignature(dateSignature);
        } catch (java.io.IOException e) {
            throw new BusinessRuleException("Le document signé ne peut pas être enregistré.");
        }
        eventPublisher.publishEvent(new ContratSigneEvent(id));
        activerSiPret(id, currentUser.email());
    }

    /**
     * RG-37, CON-04 à CON-06, CON-08.
     */
    @Override
    @Transactional
    public ContratResponse renouveler(Long id, RenouvellementRequest request, CurrentUser currentUser) {
        Contrat precedent = trouver(id);
        verifierVisible(precedent, currentUser);
        LocalDate debut = request.dateDebut() == null
                ? precedent.getDateFinPrevue().plusDays(1) : request.dateDebut();
        ruleValidator.verifierRenouvellement(precedent, request.dureeMois(), debut);
        Contrat nouveau = creerEntite(precedent.getEmploye().getId(), precedent.getPoste().getId(),
                precedent.getManager() == null ? null : precedent.getManager().getId(), precedent.getType(),
                debut, request.dureeMois(), valeur(request.salaireBase(), precedent.getSalaireBase()),
                precedent.getDevise(), valeur(request.horairesTravail(), precedent.getHorairesTravail()),
                valeur(request.lieuTravail(), precedent.getLieuTravail()), precedent,
                precedent.getNumeroRenouvellement() + 1);
        Contrat saved = contratRepository.save(nouveau);
        eventPublisher.publishEvent(new ContratCreeEvent(saved.getId(), saved.getEmploye().getId(), saved.getType(), saved.getDateDebut()));
        return contratMapper.toResponse(saved);
    }

    /**
     * RG-37, CON-07, CON-08.
     */
    @Override
    @Transactional
    public ContratResponse transformer(Long id, TransformationRequest request, CurrentUser currentUser) {
        Contrat precedent = trouver(id);
        verifierVisible(precedent, currentUser);
        LocalDate debut = request.dateDebut() == null
                ? precedent.getDateFinPrevue() == null ? precedent.getDateDebut() : precedent.getDateFinPrevue().plusDays(1)
                : request.dateDebut();
        ruleValidator.verifierTransformation(precedent, request.typeCible(), request.dureeMois(), debut);
        Contrat nouveau = creerEntite(precedent.getEmploye().getId(), precedent.getPoste().getId(),
                precedent.getManager() == null ? null : precedent.getManager().getId(), request.typeCible(),
                debut, request.dureeMois(), valeur(request.salaireBase(), precedent.getSalaireBase()),
                precedent.getDevise(), valeur(request.horairesTravail(), precedent.getHorairesTravail()),
                valeur(request.lieuTravail(), precedent.getLieuTravail()), precedent,
                precedent.getNumeroRenouvellement() + 1);
        Contrat saved = contratRepository.save(nouveau);
        eventPublisher.publishEvent(new ContratCreeEvent(saved.getId(), saved.getEmploye().getId(), saved.getType(), saved.getDateDebut()));
        return contratMapper.toResponse(saved);
    }

    /**
     * RG-39, CON-11, CON-14.
     */
    @Override
    @Transactional
    public void terminer(Long id, FinContratRequest request, CurrentUser currentUser) {
        Contrat contrat = trouver(id);
        verifierVisible(contrat, currentUser);
        if (contrat.getStatut() != StatutContrat.ACTIF) throw new BusinessRuleException("Le contrat doit être actif.");
        ContratResponse avant = contratMapper.toResponse(contrat);
        transitionner(contrat, StatutContrat.TERMINE, currentUser.email(), request.commentaire());
        contrat.setDateFinReelle(request.dateFin());
        contrat.setMotifFin(request.motifFin());
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.CONTRAT, id, avant,
                contratMapper.toResponse(contrat), currentUser.email());
    }

    /**
     * CON-08 : annulation réservée aux contrats BROUILLON ou A_SIGNER.
     */
    @Override
    @Transactional
    public void annuler(Long id, CurrentUser currentUser) {
        Contrat contrat = trouver(id);
        verifierVisible(contrat, currentUser);
        if (contrat.getStatut() != StatutContrat.BROUILLON && contrat.getStatut() != StatutContrat.A_SIGNER) {
            throw new BusinessRuleException("Seul un contrat brouillon ou à signer peut être annulé.");
        }
        transitionner(contrat, StatutContrat.ANNULE, currentUser.email(), null);
        eventPublisher.publishEvent(new ContratAnnuleEvent(id));
    }

    /**
     * CON-12 : retourne l'historique seulement si l'utilisateur peut voir le contrat.
     */
    @Override
    public List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser) {
        Contrat contrat = trouver(id);
        verifierVisible(contrat, currentUser);
        return historiqueStatutService.lister(TypeEntite.CONTRAT, id);
    }

    /**
     * RG-33.
     */
    @Override
    @Transactional
    public Contrat creerContratInitial(ContratInitialCommande commande, String auteur) {
        Contrat contrat = creerEntite(commande.employeId(), commande.posteId(), commande.managerId(),
                commande.type(), commande.dateDebut(), commande.dureeMois(), commande.salaireBase(),
                commande.devise(), commande.horairesTravail(), commande.lieuTravail(), null, 0);
        Contrat saved = contratRepository.save(contrat);
        historiqueStatutService.enregistrer(TypeEntite.CONTRAT, saved.getId(), null, saved.getStatut().name(), auteur, null);
        eventPublisher.publishEvent(new ContratCreeEvent(saved.getId(), saved.getEmploye().getId(), saved.getType(), saved.getDateDebut()));
        return saved;
    }

    /**
     * RG-39, RG-41, CON-05, CON-10, CON-11.
     *
     * Étapes : charger le contrat ; vérifier A_SIGNER, document signé et date de début atteinte ;
     * terminer le précédent actif avec le motif RENOUVELLEMENT ou TRANSFORMATION et une date de fin
     * égale à la veille du nouveau contrat ; activer le nouveau contrat ; activer l'employé ; créer
     * l'affectation initiale si aucune affectation active n'existe ; enregistrer les historiques
     * des transitions. Toute erreur annule l'ensemble de la transaction.
     */
    @Override
    @Transactional
    public void activerSiPret(Long contratId, String auteur) {
        Contrat contrat = trouver(contratId);
        if (contrat.getStatut() != StatutContrat.A_SIGNER || contrat.getDocumentSigne() == null
                || contrat.getDateDebut().isAfter(LocalDate.now())) return;
        contratRepository.findByEmployeIdAndStatut(contrat.getEmploye().getId(), StatutContrat.ACTIF)
                .filter(precedent -> !precedent.getId().equals(contrat.getId()))
                .ifPresent(precedent -> {
                    transitionner(precedent, StatutContrat.TERMINE, auteur, null);
                    precedent.setDateFinReelle(contrat.getDateDebut().minusDays(1));
                    precedent.setMotifFin(precedent.getType() == contrat.getType()
                            ? com.gntfh.sirh_api.contrat.enums.MotifFinContrat.RENOUVELLEMENT
                            : com.gntfh.sirh_api.contrat.enums.MotifFinContrat.TRANSFORMATION);
                });
        transitionner(contrat, StatutContrat.ACTIF, auteur, null);
        employeService.activer(contrat.getEmploye().getId(), contrat.getDateDebut(), auteur);
        if (affectationRepository.findActivesByEmployeId(contrat.getEmploye().getId(), contrat.getDateDebut()).isEmpty()) {
            affectationService.creerInitiale(contrat.getEmploye(), contrat.getPoste(),
                    contrat.getId(), contrat.getManager() == null ? null : contrat.getManager().getId(),
                    contrat.getDateDebut(), auteur);
        }
    }

    /**
     * CON-10.
     */
    @Override
    @Transactional
    public int activerContratsDus(LocalDate aujourdhui, String auteur) {
        int count = 0;
        for (Contrat contrat : contratRepository.findContratsASignerActivables(aujourdhui)) {
            activerSiPret(contrat.getId(), auteur);
            if (contrat.getStatut() == StatutContrat.ACTIF) count++;
        }
        return count;
    }

    /**
     * RG-40, CON-13 : alerte la RH une seule fois pour chaque fin proche d'ESSAI ou de CDD.
     */
    @Override
    @Transactional
    public int alerterFinsProches(LocalDate aujourdhui) {
        LocalDate limite = aujourdhui.plusDays(sirhProperties.contrat().alerteFinJours());
        List<Contrat> contrats = contratRepository.findContratsActifsAvecFinAvantEtAlerteNonEnvoyee(limite);
        for (Contrat contrat : contrats) {
            notificationService.notifierParRole(Role.RH, TypeNotification.CONTRAT_ECHEANCE,
                    "Échéance de contrat proche", "Le contrat " + contrat.getNumero() + " arrive à échéance.",
                    TypeEntite.CONTRAT, contrat.getId());
            contrat.setAlerteFinEnvoyee(true);
        }
        return contrats.size();
    }

    private Contrat trouver(Long id) {
        return contratRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Contrat", id));
    }

    private Employe employe(Long id) {
        return employeRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Employé", id));
    }

    private Poste poste(Long id) {
        return posteRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Poste", id));
    }

    private Employe manager(Long id) {
        return id == null ? null : employe(id);
    }

    private Contrat creerEntite(Long employeId, Long posteId, Long managerId, TypeContrat type,
                                LocalDate debut, Integer duree, java.math.BigDecimal salaire, String devise,
                                String horaires, String lieu, Contrat precedent, int renouvellement) {
        ruleValidator.verifierCreation(type, debut, duree);
        return Contrat.builder().numero(numeroService.prochain(SequenceNumero.CONTRAT))
                .employe(employe(employeId)).poste(poste(posteId)).manager(manager(managerId))
                .type(type).numeroRenouvellement(renouvellement).contratPrecedent(precedent)
                .dateDebut(debut).dateFinPrevue(type.aDateFin() ? ruleValidator.calculerFinPrevue(debut, duree) : null)
                .salaireBase(salaire).devise(devise).horairesTravail(horaires).lieuTravail(lieu)
                .statut(StatutContrat.BROUILLON).build();
    }

    private void transitionner(Contrat contrat, StatutContrat cible, String auteur, String commentaire) {
        if (!contrat.getStatut().peutPasserA(cible)) {
            throw new InvalidStateTransitionException("Contrat", contrat.getStatut(), cible);
        }
        String ancien = contrat.getStatut().name();
        contrat.setStatut(cible);
        historiqueStatutService.enregistrer(TypeEntite.CONTRAT, contrat.getId(), ancien, cible.name(), auteur, commentaire);
    }

    private void verifierVisible(Contrat contrat, CurrentUser user) {
        if (user.role() == Role.RH || user.role() == Role.PAIE || user.role() == Role.ADMIN) return;
        if (user.role() == Role.SALARIE && contrat.getEmploye().getEmailPro().equalsIgnoreCase(user.email())) return;
        if (user.role() == Role.MANAGER && affectationRepository.findActivesByManagerId(employeConnecte(user).getId(), LocalDate.now())
                .stream().anyMatch(a -> a.getEmploye().getId().equals(contrat.getEmploye().getId()))) return;
        throw ResourceNotFoundException.of("Contrat", contrat.getId());
    }

    private void verifierTri(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!List.of("id", "numero", "dateDebut", "dateFinPrevue", "statut").contains(order.getProperty())) {
                throw new BusinessRuleException("Tri non autorisé.");
            }
        });
    }

    private Employe employeConnecte(CurrentUser user) {
        return employeRepository.findByEmailProIgnoreCase(user.email())
                .orElseThrow(() -> ResourceNotFoundException.of("Employé", user.email()));
    }

    private <T> T valeur(T valeur, T defaut) { return valeur == null ? defaut : valeur; }
}
