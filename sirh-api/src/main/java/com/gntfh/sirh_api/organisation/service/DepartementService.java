package com.gntfh.sirh_api.organisation.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.DuplicateResourceException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.organisation.dto.DepartementRequest;
import com.gntfh.sirh_api.organisation.dto.DepartementResponse;
import com.gntfh.sirh_api.organisation.dto.DepartementUpdateRequest;
import com.gntfh.sirh_api.organisation.entity.Departement;
import com.gntfh.sirh_api.organisation.mapper.DepartementMapper;
import com.gntfh.sirh_api.organisation.repository.DepartementRepository;
import com.gntfh.sirh_api.employe.repository.EmployeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Set;

/**
 * Service pour la gestion des départements.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartementService {

    private final DepartementRepository repository;
    private final DepartementMapper mapper;
    private final EmployeRepository employeRepository;
    private final AuditService auditService;
    private static final Set<String> TRIS_AUTORISES = Set.of("id", "code", "nom", "actif");

    @Transactional(readOnly = true)
    public PageResponse<DepartementResponse> rechercher(String q, Boolean actif, Long parentId, Pageable pageable) {
        verifierTri(pageable);
        Specification<Departement> spec = (root, query, cb) -> cb.conjunction();
        if (q != null && !q.isBlank()) {
            String motif = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(cb.like(cb.lower(root.get("code")), motif),
                    cb.like(cb.lower(root.get("nom")), motif)));
        }
        if (actif != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("actif"), actif));
        if (parentId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("departementParent").get("id"), parentId));
        return PageResponse.from(repository.findAll(spec, pageable), mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DepartementResponse obtenir(Long id) {
        return mapper.toResponse(trouver(id));
    }

    @Transactional
    public DepartementResponse creer(DepartementRequest request) {
        if (request.responsableId() != null && !employeRepository.existsById(request.responsableId())) {
            throw ResourceNotFoundException.of("Employe", request.responsableId());
        }
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (repository.existsByCodeIgnoreCase(code)) throw DuplicateResourceException.of("Un département", "le code", code);
        Departement parent = chargerParent(request.departementParentId());
        Departement departement = mapper.toEntity(request);
        departement.setCode(code);
        departement.setDepartementParent(parent);
        DepartementResponse response = mapper.toResponse(repository.save(departement));
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.DEPARTEMENT, response.id(), null, response, "system");
        return response;
    }

    @Transactional
    public DepartementResponse modifier(Long id, DepartementUpdateRequest request) {
        if (request.responsableId() != null && !employeRepository.existsById(request.responsableId())) {
            throw ResourceNotFoundException.of("Employe", request.responsableId());
        }
        Departement departement = trouver(id);
        Departement parent = chargerParent(request.departementParentId());
        verifierCycle(departement, parent);
        DepartementResponse avant = mapper.toResponse(departement);
        mapper.updateEntity(request, departement);
        departement.setDepartementParent(parent);
        DepartementResponse apres = mapper.toResponse(departement);
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.DEPARTEMENT, id, avant, apres, "system");
        return apres;
    }

    @Transactional
    public DepartementResponse activer(Long id) {
        Departement departement = trouver(id);
        departement.setActif(true);
        return mapper.toResponse(departement);
    }

    @Transactional
    public DepartementResponse desactiver(Long id) {
        Departement departement = trouver(id);
        if (repository.countPostesActifsByDepartementId(id) > 0 ||
                repository.existsSousDepartementsActifsByParentId(id)) {
            throw new BusinessRuleException("Le département ne peut pas être désactivé car il possède des postes ou sous-départements actifs.");
        }
        DepartementResponse avant = mapper.toResponse(departement);
        departement.setActif(false);
        DepartementResponse apres = mapper.toResponse(departement);
        auditService.journaliser(ActionAudit.SUPPRESSION_LOGIQUE, TypeEntite.DEPARTEMENT, id, avant, apres, "system");
        return apres;
    }

    private Departement trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Département", id));
    }

    private Departement chargerParent(Long parentId) {
        if (parentId == null) return null;
        Departement parent = trouver(parentId);
        if (!Boolean.TRUE.equals(parent.getActif())) throw new BusinessRuleException("Le département parent doit être actif.");
        return parent;
    }

    private void verifierCycle(Departement departement, Departement parent) {
        for (Departement courant = parent; courant != null; courant = courant.getDepartementParent()) {
            if (courant.getId().equals(departement.getId())) throw new BusinessRuleException("La hiérarchie des départements créerait un cycle.");
        }
    }

    private void verifierTri(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!TRIS_AUTORISES.contains(order.getProperty())) throw new BusinessRuleException("Tri non autorisé sur le champ « %s ».".formatted(order.getProperty()));
        });
    }
}
