package com.gntfh.sirh_api.employe.service;

import com.gntfh.sirh_api.auth.enums.Role;
import com.gntfh.sirh_api.auth.repository.UtilisateurRepository;
import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.audit.service.HistoriqueStatutService;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.InvalidStateTransitionException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.employe.dto.EmployeRequest;
import com.gntfh.sirh_api.employe.dto.EmployeResponse;
import com.gntfh.sirh_api.employe.dto.EmployeResumeResponse;
import com.gntfh.sirh_api.employe.dto.EmployeUpdateRequest;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.employe.enums.StatutEmploye;
import com.gntfh.sirh_api.employe.mapper.EmployeMapper;
import com.gntfh.sirh_api.employe.entity.Affectation;
import com.gntfh.sirh_api.employe.repository.AffectationRepository;
import com.gntfh.sirh_api.employe.repository.EmployeRepository;
import com.gntfh.sirh_api.recrutement.entity.Candidature;
import com.gntfh.sirh_api.recrutement.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Implémentation du service Employe.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeServiceImpl implements EmployeService {

    private final EmployeRepository employeRepository;
    private final EmployeMapper employeMapper;
    private final MatriculeService matriculeService;
    private final FicheSalariePdfService ficheSalariePdfService;
    private final AffectationRepository affectationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CandidatureRepository candidatureRepository;
    private final AuditService auditService;
    private final HistoriqueStatutService historiqueStatutService;

    @Override
    @Transactional
    public EmployeResponse creer(EmployeRequest request, CurrentUser currentUser) {
        Employe employe = employeMapper.toEntity(request);
        employe.setMatricule(matriculeService.generer());
        Employe saved = employeRepository.save(employe);
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.EMPLOYE, saved.getId(),
                null, employeMapper.toResponse(saved), auteur(currentUser));
        historiqueStatutService.enregistrer(TypeEntite.EMPLOYE, saved.getId(), null,
                saved.getStatut().name(), auteur(currentUser), null);
        return responseDetail(saved);
    }

    @Override
    @Transactional
    public EmployeResponse modifier(Long id, EmployeUpdateRequest request, CurrentUser currentUser) {
        Employe employe = trouver(id);
        EmployeResponse avant = responseDetail(employe);
        employeMapper.updateEntity(request, employe);
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.EMPLOYE, id,
                avant, responseDetail(employe), auteur(currentUser));
        return responseDetail(employe);
    }

    @Override
    public Object obtenir(Long id, CurrentUser currentUser) {
        Employe employe = trouver(id);
        if (currentUser.role() == Role.RH) {
            return responseDetail(employe);
        }
        if (currentUser.role() == Role.PAIE) {
            return responseResume(employe);
        }
        if (currentUser.role() == Role.MANAGER
                && affectationRepository.findActivesByManagerId(currentEmployeeId(currentUser), LocalDate.now())
                .stream().anyMatch(a -> a.getEmploye().getId().equals(id))) {
            return responseResume(employe);
        }
        throw ResourceNotFoundException.of("Employé", id);
    }

    @Override
    public EmployeResponse moi(CurrentUser currentUser) {
        Long employeId = currentEmployeeId(currentUser);
        return responseDetail(trouver(employeId));
    }

    @Override
    public PageResponse<?> rechercher(String q, StatutEmploye statut, Long departementId, Long posteId, Pageable pageable, CurrentUser currentUser) {
        Specification<Employe> specification = (root, query, criteriaBuilder) -> {
            query.distinct(true);
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("matricule")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("prenom")), pattern)));
            }
            if (statut != null) {
                predicates.add(criteriaBuilder.equal(root.get("statut"), statut));
            }
            if (departementId != null || posteId != null || currentUser.role() == Role.MANAGER) {
                var subquery = query.subquery(Long.class);
                var affectation = subquery.from(Affectation.class);
                var affectationPredicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
                affectationPredicates.add(criteriaBuilder.equal(
                        affectation.get("employe").get("id"), root.get("id")));
                if (departementId != null) {
                    affectationPredicates.add(criteriaBuilder.equal(
                            affectation.get("poste").get("departement").get("id"), departementId));
                }
                if (posteId != null) {
                    affectationPredicates.add(criteriaBuilder.equal(
                            affectation.get("poste").get("id"), posteId));
                }
                affectationPredicates.add(criteriaBuilder.lessThanOrEqualTo(
                        affectation.get("dateDebut"), LocalDate.now()));
                affectationPredicates.add(criteriaBuilder.or(
                        criteriaBuilder.isNull(affectation.get("dateFin")),
                        criteriaBuilder.greaterThanOrEqualTo(
                                affectation.get("dateFin"), LocalDate.now())));
                if (currentUser.role() == Role.MANAGER) {
                    affectationPredicates.add(criteriaBuilder.equal(
                            affectation.get("manager").get("id"),
                            currentEmployeeId(currentUser)));
                }
                subquery.select(affectation.get("employe").get("id"))
                        .where(affectationPredicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
                predicates.add(criteriaBuilder.exists(subquery));
            } else if (currentUser.role() != Role.RH && currentUser.role() != Role.PAIE) {
                predicates.add(criteriaBuilder.disjunction());
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Page<Employe> page = employeRepository.findAll(specification, pageable);
        if (currentUser.role() == Role.RH) {
            return PageResponse.from(page, this::responseDetail);
        }
        return PageResponse.from(page, this::responseResume);
    }

    @Override
    @Transactional
    public void suspendre(Long id, CurrentUser currentUser) {
        changerStatut(id, StatutEmploye.SUSPENDU, currentUser);
    }

    @Override
    @Transactional
    public void reprendre(Long id, CurrentUser currentUser) {
        Employe employe = trouver(id);
        if (employe.getStatut() != StatutEmploye.SUSPENDU) {
            throw new InvalidStateTransitionException("Employé", employe.getStatut(), StatutEmploye.ACTIF);
        }
        changerStatut(id, StatutEmploye.ACTIF, currentUser);
    }

    @Override
    public byte[] ficheSalarie(Long id, CurrentUser currentUser) {
        Employe employe = trouver(id);
        Affectation affectation = affectationRepository.findActivesByEmployeId(id, LocalDate.now()).stream()
                .max(java.util.Comparator.comparing(Affectation::getDateDebut)).orElse(null);
        return ficheSalariePdfService.generer(employe, Optional.ofNullable(affectation));
    }

    @Override
    public List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser) {
        trouver(id);
        return historiqueStatutService.lister(TypeEntite.EMPLOYE, id);
    }

    @Override
    @Transactional
    public Employe creerDepuisCandidature(Long candidatureId, LocalDate dateEmbauche, String auteur) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> ResourceNotFoundException.of("Candidature", candidatureId));
        if (employeRepository.findByCandidatureOrigineId(candidatureId).isPresent()) {
            throw new BusinessRuleException("Cette candidature a déjà donné naissance à un employé.");
        }
        var candidat = candidature.getCandidat();
        Employe employe = Employe.builder()
                .matricule(matriculeService.generer())
                .nom(candidat.getNom())
                .prenom(candidat.getPrenom())
                .emailPerso(candidat.getEmail())
                .telephone(candidat.getTelephone())
                .dateNaissance(candidat.getDateNaissance())
                .adresse(candidat.getAdresse())
                .dateEmbauche(dateEmbauche)
                .statut(StatutEmploye.PRE_EMBAUCHE)
                .candidatureOrigine(candidature)
                .build();
        Employe saved = employeRepository.save(employe);
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.EMPLOYE, saved.getId(),
                null, employeMapper.toResponse(saved), auteur);
        historiqueStatutService.enregistrer(TypeEntite.EMPLOYE, saved.getId(), null,
                saved.getStatut().name(), auteur, null);
        return saved;
    }

    @Override
    @Transactional
    public void activer(Long employeId, LocalDate dateDebut, String auteur) {
        Employe employe = trouver(employeId);
        if (!employe.getStatut().peutPasserA(StatutEmploye.ACTIF)) {
            throw new InvalidStateTransitionException("Employé", employe.getStatut(), StatutEmploye.ACTIF);
        }
        StatutEmploye ancien = employe.getStatut();
        employe.setStatut(StatutEmploye.ACTIF);
        if (employe.getDateEmbauche() == null) {
            employe.setDateEmbauche(dateDebut);
        }
        historiqueStatutService.enregistrer(TypeEntite.EMPLOYE, employeId,
                ancien.name(), employe.getStatut().name(), auteur, null);
    }

    @Override
    public Employe trouver(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Employé", id));
    }

    private void changerStatut(Long id, StatutEmploye cible, CurrentUser currentUser) {
        Employe employe = trouver(id);
        StatutEmploye ancien = employe.getStatut();
        if (!ancien.peutPasserA(cible)) {
            throw new InvalidStateTransitionException("Employé", ancien, cible);
        }
        employe.setStatut(cible);
        historiqueStatutService.enregistrer(TypeEntite.EMPLOYE, id, ancien.name(), cible.name(),
                auteur(currentUser), null);
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.EMPLOYE, id,
                ancien.name(), cible.name(), auteur(currentUser));
    }

    private EmployeResponse responseDetail(Employe employe) {
        EmployeResponse base = employeMapper.toResponse(employe);
        Affectation affectation = affectationRepository.findActivesByEmployeId(employe.getId(), LocalDate.now())
                .stream().max(java.util.Comparator.comparing(Affectation::getDateDebut)).orElse(null);
        return new EmployeResponse(base.id(), base.matricule(), base.nom(), base.prenom(), base.emailPro(),
                base.emailPerso(), base.telephone(), base.dateNaissance(), base.adresse(),
                base.numeroSecuriteSociale(), base.dateEmbauche(), base.statut(), base.candidatureOrigineId(),
                affectation == null ? null : affectation.getPoste().getId(),
                affectation == null ? null : affectation.getPoste().getIntitule(),
                affectation == null ? null : affectation.getPoste().getDepartement().getNom(),
                affectation == null || affectation.getManager() == null ? null : affectation.getManager().getId(),
                affectation == null || affectation.getManager() == null ? null : affectation.getManager().getNom(),
                base.createdAt());
    }

    private EmployeResumeResponse responseResume(Employe employe) {
        EmployeResumeResponse base = employeMapper.toResumeResponse(employe);
        Affectation affectation = affectationRepository.findActivesByEmployeId(employe.getId(), LocalDate.now())
                .stream().max(java.util.Comparator.comparing(Affectation::getDateDebut)).orElse(null);
        return new EmployeResumeResponse(base.id(), base.matricule(), base.nom(), base.prenom(),
                base.statut(), base.dateEmbauche(),
                affectation == null ? null : affectation.getPoste().getIntitule());
    }

    private Long currentEmployeeId(CurrentUser currentUser) {
        if (currentUser.id() == null) {
            throw ResourceNotFoundException.of("Utilisateur", currentUser.email());
        }
        return utilisateurRepository.findById(currentUser.id())
                .map(u -> u.getEmployeId())
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", currentUser.id()));
    }

    private String auteur(CurrentUser currentUser) {
        return currentUser.email();
    }
}
