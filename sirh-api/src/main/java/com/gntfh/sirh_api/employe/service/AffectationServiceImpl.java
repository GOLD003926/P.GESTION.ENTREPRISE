package com.gntfh.sirh_api.employe.service;

import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.employe.dto.AffectationClotureRequest;
import com.gntfh.sirh_api.employe.dto.AffectationRequest;
import com.gntfh.sirh_api.employe.dto.AffectationResponse;
import com.gntfh.sirh_api.employe.entity.Affectation;
import com.gntfh.sirh_api.employe.entity.Employe;
import com.gntfh.sirh_api.employe.enums.MotifAffectation;
import com.gntfh.sirh_api.employe.mapper.AffectationMapper;
import com.gntfh.sirh_api.employe.repository.AffectationRepository;
import com.gntfh.sirh_api.employe.repository.EmployeRepository;
import com.gntfh.sirh_api.organisation.entity.Poste;
import com.gntfh.sirh_api.organisation.repository.PosteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

/**
 * Implémentation du service Affectation.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AffectationServiceImpl implements AffectationService {

    private final AffectationRepository affectationRepository;
    private final AffectationMapper affectationMapper;
    private final EmployeRepository employeRepository;
    private final PosteRepository posteRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public AffectationResponse creer(AffectationRequest request, CurrentUser currentUser) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> ResourceNotFoundException.of("Employé", request.employeId()));
        Poste poste = posteRepository.findById(request.posteId())
                .orElseThrow(() -> ResourceNotFoundException.of("Poste", request.posteId()));
        verifierPosteActif(poste);
        Employe manager = trouverManager(request.managerId());
        LocalDate dateDebut = request.dateDebut();
        BigDecimal taux = taux(request);
        List<Affectation> chevauchements = affectationRepository.findOverlappingByEmployeId(
                employe.getId(), dateDebut, LocalDate.of(9999, 12, 31));
        if (Boolean.TRUE.equals(request.cloturerActives())) {
            for (Affectation existante : affectationRepository.findActivesByEmployeId(employe.getId(), dateDebut)) {
                if (existante.getDateDebut().isAfter(dateDebut)) {
                    throw new BusinessRuleException("La date de début ne peut pas précéder une affectation existante.");
                }
                existante.setDateFin(dateDebut.minusDays(1));
            }
            chevauchements = affectationRepository.findOverlappingByEmployeId(
                    employe.getId(), dateDebut, LocalDate.of(9999, 12, 31));
        }
        if (chevauchements.stream().anyMatch(a -> !a.getDateDebut().isAfter(dateDebut))) {
            BigDecimal total = chevauchements.stream()
                    .map(Affectation::getTauxOccupation)
                    .reduce(taux, BigDecimal::add);
            if (total.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BusinessRuleException("Le cumul des taux d'occupation ne peut pas dépasser 100.");
            }
        }
        if (occupationAu(poste.getId(), dateDebut) >= poste.getEffectifAutorise()) {
            throw new BusinessRuleException("L'effectif autorisé du poste est atteint.");
        }
        Affectation affectation = affectationMapper.toEntity(request);
        affectation.setEmploye(employe);
        affectation.setPoste(poste);
        affectation.setManager(manager);
        affectation.setDateDebut(dateDebut);
        Affectation saved = affectationRepository.save(affectation);
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.AFFECTATION, saved.getId(),
                null, affectationMapper.toResponse(saved), currentUser.email());
        return affectationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void cloturer(Long id, AffectationClotureRequest request, CurrentUser currentUser) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Affectation", id));
        if (request.dateFin().isBefore(affectation.getDateDebut())) {
            throw new BusinessRuleException("La date de fin doit être postérieure ou égale à la date de début.");
        }
        AffectationResponse avant = affectationMapper.toResponse(affectation);
        affectation.setDateFin(request.dateFin());
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.AFFECTATION, id,
                avant, affectationMapper.toResponse(affectation), currentUser.email());
    }

    @Override
    public PageResponse<AffectationResponse> lister(Long employeId, Long posteId, Boolean active, Pageable pageable) {
        Specification<Affectation> specification = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (employeId != null) {
                predicates.add(cb.equal(root.get("employe").get("id"), employeId));
            }
            if (posteId != null) {
                predicates.add(cb.equal(root.get("poste").get("id"), posteId));
            }
            if (active != null) {
                if (active) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("dateDebut"), LocalDate.now()));
                    predicates.add(cb.or(cb.isNull(root.get("dateFin")),
                            cb.greaterThanOrEqualTo(root.get("dateFin"), LocalDate.now())));
                } else {
                    predicates.add(cb.or(cb.greaterThan(root.get("dateDebut"), LocalDate.now()),
                            cb.lessThan(root.get("dateFin"), LocalDate.now())));
                }
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return PageResponse.from(affectationRepository.findAll(specification, pageable), affectationMapper::toResponse);
    }

    @Override
    @Transactional
    public Affectation creerInitiale(Employe employe, Poste poste, Long contratId, Long managerId, LocalDate dateDebut, String auteur) {
        verifierPosteActif(poste);
        Employe manager = trouverManager(managerId);
        if (occupationAu(poste.getId(), dateDebut) >= poste.getEffectifAutorise()) {
            throw new BusinessRuleException("L'effectif autorisé du poste est atteint.");
        }
        Affectation affectation = Affectation.builder()
                .employe(employe)
                .poste(poste)
                .contratId(contratId)
                .manager(manager)
                .dateDebut(dateDebut)
                .tauxOccupation(BigDecimal.valueOf(100))
                .motif(MotifAffectation.EMBAUCHE)
                .build();
        Affectation saved = affectationRepository.save(affectation);
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.AFFECTATION, saved.getId(),
                null, affectationMapper.toResponse(saved), auteur);
        return saved;
    }

    @Override
    public int occupationActuelle(Long posteId) {
        return affectationRepository.countActivesByPosteId(posteId, LocalDate.now());
    }

    private BigDecimal taux(AffectationRequest request) {
        return request.tauxOccupation() == null ? BigDecimal.valueOf(100) : request.tauxOccupation();
    }

    private int occupationAu(Long posteId, LocalDate date) {
        return affectationRepository.findActivesByPosteId(posteId, date).size();
    }

    private Employe trouverManager(Long managerId) {
        if (managerId == null) {
            return null;
        }
        Employe manager = employeRepository.findById(managerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Manager", managerId));
        if (manager.getStatut() != com.gntfh.sirh_api.employe.enums.StatutEmploye.ACTIF) {
            throw new BusinessRuleException("Le manager désigné doit être actif.");
        }
        return manager;
    }

    private void verifierPosteActif(Poste poste) {
        if (!Boolean.TRUE.equals(poste.getActif())) {
            throw new BusinessRuleException("Le poste doit être actif.");
        }
    }
}
