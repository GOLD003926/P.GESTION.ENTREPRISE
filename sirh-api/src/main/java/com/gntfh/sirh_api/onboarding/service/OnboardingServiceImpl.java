package com.gntfh.sirh_api.onboarding.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.InvalidStateTransitionException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.auth.entity.Utilisateur;
import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.employe.repository.AffectationRepository;
import com.gntfh.sirh_api.employe.repository.EmployeRepository;
import com.gntfh.sirh_api.onboarding.dto.OnboardingRequest;
import com.gntfh.sirh_api.onboarding.dto.OnboardingResponse;
import com.gntfh.sirh_api.onboarding.entity.Onboarding;
import com.gntfh.sirh_api.onboarding.entity.OnboardingTache;
import com.gntfh.sirh_api.onboarding.entity.ModeleTache;
import com.gntfh.sirh_api.onboarding.enums.StatutOnboarding;
import com.gntfh.sirh_api.onboarding.enums.StatutTache;
import com.gntfh.sirh_api.onboarding.mapper.OnboardingMapper;
import com.gntfh.sirh_api.onboarding.repository.OnboardingRepository;
import com.gntfh.sirh_api.onboarding.repository.OnboardingTacheRepository;
import com.gntfh.sirh_api.onboarding.repository.ModeleTacheRepository;
import com.gntfh.sirh_api.common.notification.enums.TypeNotification;
import com.gntfh.sirh_api.common.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OnboardingServiceImpl implements OnboardingService {

    private final OnboardingRepository repository;
    private final OnboardingTacheRepository tacheRepository;
    private final ModeleTacheRepository modeleTacheRepository;
    private final ModeleOnboardingService modeleService;
    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AffectationRepository affectationRepository;
    private final OnboardingMapper mapper;
    private final HistoriqueStatutService historique;
    private final NotificationService notificationService;

    /** RG-42, RG-43 : instancie les tâches comme copies indépendantes du modèle choisi. */
    @Override
    @Transactional
    public Onboarding creerPourContrat(Long contratId, Long employeId, String typeContrat,
                                      LocalDate dateDebut, String auteur) {
        if (repository.findByContratId(contratId).isPresent()) {
            throw new BusinessRuleException("Un onboarding existe déjà pour ce contrat.");
        }
        Employe employe = employeRepository.findById(employeId)
                .orElseThrow(() -> ResourceNotFoundException.of("Employe", employeId));
        var modele = modeleService.choisirPour(typeContrat);
        List<ModeleTache> modeles = modeleServiceTaches(modele.getId());
        LocalDate dateCible = modeles.stream()
                .map(t -> dateDebut.plusDays(t.getEcheanceJours()))
                .max(LocalDate::compareTo).orElse(dateDebut);
        Onboarding onboarding = repository.save(Onboarding.builder()
                .employe(employe).contratId(contratId).modele(modele)
                .dateDebut(dateDebut).dateCible(dateCible).build());
        for (ModeleTache modeleTache : modeles) {
            OnboardingTache tache = tacheRepository.save(OnboardingTache.builder()
                    .onboarding(onboarding).modeleTache(modeleTache)
                    .code(modeleTache.getCode()).libelle(modeleTache.getLibelle())
                    .obligatoire(modeleTache.getObligatoire()).roleResponsable(modeleTache.getRoleResponsable())
                    .echeance(dateDebut.plusDays(modeleTache.getEcheanceJours())).build());
            notificationService.notifierParRole(modeleTache.getRoleResponsable(),
                    TypeNotification.ONBOARDING_TACHE_ASSIGNEE, "Nouvelle tâche d'onboarding",
                    tache.getLibelle(), TypeEntite.ONBOARDING_TACHE, tache.getId());
        }
        historique.enregistrer(TypeEntite.ONBOARDING, onboarding.getId(), null,
                onboarding.getStatut().name(), auteur, "Création de l'onboarding");
        return onboarding;
    }

    /** ONB-09 : clôture automatiquement la tâche de signature. */
    @Override
    @Transactional
    public void marquerContratSigne(Long contratId, String auteur) {
        repository.findByContratId(contratId).ifPresent(onboarding -> {
            tacheRepository.findByOnboardingIdAndCode(onboarding.getId(), "CONTRAT_SIGNE")
                    .stream().findFirst().ifPresent(tache -> {
                        if (tache.getStatut().peutPasserA(StatutTache.FAIT)) {
                            tache.setStatut(StatutTache.FAIT);
                            tache.setDateRealisation(java.time.Instant.now());
                        }
                    });
            verifierCloture(onboarding, auteur);
        });
    }

    /** ONB-10 : annule l'onboarding associé au contrat annulé. */
    @Override
    @Transactional
    public void annulerPourContrat(Long contratId, String auteur) {
        repository.findByContratId(contratId).ifPresent(onboarding -> {
            if (!onboarding.getStatut().estTerminal()) {
                transitionner(onboarding, StatutOnboarding.ANNULE, auteur);
            }
        });
    }

    /** ONB-08 : refuse de révéler un onboarding hors du périmètre de l'acteur. */
    @Override
    public OnboardingResponse obtenir(Long id, CurrentUser currentUser) {
        Onboarding onboarding = visible(id, currentUser);
        return mapper.toResponse(onboarding, tacheRepository.findByOnboardingIdOrderByEcheanceAscIdAsc(id));
    }

    /** ONB-08 : filtre par employé et rôle puis applique la liste blanche du tri. */
    @Override
    public PageResponse<OnboardingResponse> rechercher(Long employeId, StatutOnboarding statut,
                                                        Pageable pageable, CurrentUser currentUser) {
        verifierTri(pageable);
        Specification<Onboarding> spec = (root, query, cb) -> {
            var ps = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (employeId != null) ps.add(cb.equal(root.get("employe").get("id"), employeId));
            if (statut != null) ps.add(cb.equal(root.get("statut"), statut));
            if (currentUser.role() == Role.MANAGER) {
                Long managerId = utilisateur(currentUser).getEmployeId();
                var ids = managerId == null ? List.<Long>of() : affectationRepository.findActivesByManagerId(managerId, LocalDate.now())
                        .stream().map(a -> a.getEmploye().getId()).toList();
                ps.add(root.get("employe").get("id").in(ids));
            }
            return cb.and(ps.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Page<Onboarding> page = repository.findAll(spec, pageable);
        return PageResponse.from(page, o -> mapper.toResponse(o,
                tacheRepository.findByOnboardingIdOrderByEcheanceAscIdAsc(o.getId())));
    }

    /** ONB-08 : donne au salarié son onboarding le plus récent en lecture seule. */
    @Override
    public OnboardingResponse monOnboarding(CurrentUser currentUser) {
        Utilisateur utilisateur = utilisateur(currentUser);
        if (utilisateur.getEmployeId() == null) throw new BusinessRuleException("Aucun employé lié à ce compte.");
        return obtenir(repository.findFirstByEmployeIdOrderByCreatedAtDesc(utilisateur.getEmployeId())
                .orElseThrow(() -> ResourceNotFoundException.of("Onboarding", utilisateur.getEmployeId())).getId(), currentUser);
    }

    /** ONB-08 : désigne le responsable RH après contrôle du périmètre. */
    @Override
    @Transactional
    public OnboardingResponse modifier(Long id, OnboardingRequest request, CurrentUser currentUser) {
        if (currentUser.role() != Role.RH) throw new BusinessRuleException("Action réservée à la RH.");
        Onboarding onboarding = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Onboarding", id));
        if (request.responsableRhId() == null) onboarding.setResponsableRh(null);
        else {
            Utilisateur u = utilisateurRepository.findById(request.responsableRhId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", request.responsableRhId()));
            if (!u.isActif() || u.getRole() != Role.RH) throw new BusinessRuleException("Le responsable doit être une RH active.");
            onboarding.setResponsableRh(u);
        }
        return mapper.toResponse(onboarding, tacheRepository.findByOnboardingIdOrderByEcheanceAscIdAsc(id));
    }

    /** ONB-06, RG-44 : seules les tâches obligatoires encore non FAIT empêchent la clôture. */
    @Override
    @Transactional
    public void verifierCloture(Onboarding onboarding, String auteur) {
        if (onboarding.getStatut().estTerminal()
                || tacheRepository.existsByOnboardingIdAndObligatoireTrueAndStatutNot(onboarding.getId(), StatutTache.FAIT)) return;
        transitionner(onboarding, StatutOnboarding.TERMINE, auteur);
        onboarding.setDateCloture(java.time.Instant.now());
    }

    /** ONB-08 : retourne l'historique après contrôle de visibilité. */
    @Override
    public List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser) {
        visible(id, currentUser);
        return historique.lister(TypeEntite.ONBOARDING, id);
    }

    private List<ModeleTache> modeleServiceTaches(Long id) {
        return modeleTacheRepository.findByModeleIdOrderByOrdreAsc(id);
    }

    private Utilisateur utilisateur(CurrentUser currentUser) {
        return utilisateurRepository.findById(currentUser.id())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", currentUser.id()));
    }

    private Onboarding visible(Long id, CurrentUser user) {
        Onboarding onboarding = repository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Onboarding", id));
        if (user.role() == Role.RH || user.role() == Role.ADMIN) return onboarding;
        if (user.role() == Role.MANAGER) {
            Long managerId = utilisateur(user).getEmployeId();
            boolean visible = managerId != null && affectationRepository.findActivesByManagerId(managerId, LocalDate.now()).stream()
                    .anyMatch(a -> a.getEmploye().getId().equals(onboarding.getEmploye().getId()));
            if (visible) return onboarding;
        }
        if (user.role() == Role.SALARIE && utilisateur(user).getEmployeId() != null
                && utilisateur(user).getEmployeId().equals(onboarding.getEmploye().getId())) return onboarding;
        throw new ResourceNotFoundException("Onboarding introuvable.");
    }

    private void transitionner(Onboarding onboarding, StatutOnboarding cible, String auteur) {
        StatutOnboarding ancien = onboarding.getStatut();
        if (!ancien.peutPasserA(cible)) throw new InvalidStateTransitionException("Onboarding", ancien, cible);
        onboarding.setStatut(cible);
        historique.enregistrer(TypeEntite.ONBOARDING, onboarding.getId(), ancien.name(), cible.name(), auteur, null);
    }

    private void verifierTri(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!Set.of("id", "dateDebut", "dateCible", "statut").contains(order.getProperty())) {
                throw new BusinessRuleException("Tri non autorisé.");
            }
        });
    }
}
