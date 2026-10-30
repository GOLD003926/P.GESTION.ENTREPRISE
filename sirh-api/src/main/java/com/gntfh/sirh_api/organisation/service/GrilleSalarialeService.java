package com.gntfh.sirh_api.organisation.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.DuplicateResourceException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.organisation.dto.GrilleSalarialeRequest;
import com.gntfh.sirh_api.organisation.dto.GrilleSalarialeResponse;
import com.gntfh.sirh_api.organisation.dto.GrilleSalarialeUpdateRequest;
import com.gntfh.sirh_api.organisation.entity.GrilleSalariale;
import com.gntfh.sirh_api.organisation.mapper.GrilleSalarialeMapper;
import com.gntfh.sirh_api.organisation.repository.GrilleSalarialeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Service pour la gestion des grilles salariales.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GrilleSalarialeService {

    private final GrilleSalarialeRepository repository;
    private final GrilleSalarialeMapper mapper;
    private final AuditService auditService;

    private static final Set<String> TRIS_AUTORISES =
            Set.of("id", "code", "intitule", "niveau", "salaireMin", "salaireMax", "actif");

    /**
     * Recherche des grilles salariales avec filtres.
     * Règle ORG-08 : pas de journal d'audit pour la lecture.
     *
     * @param q       recherche sur code et intitulé
     * @param actif   filtre sur l'état actif
     * @param pageable pagination
     * @return page de grilles salariales
     */
    @Transactional(readOnly = true)
    public PageResponse<GrilleSalarialeResponse> rechercher(String q, Boolean actif, Pageable pageable) {
        verifierTri(pageable);
        Specification<GrilleSalariale> spec = (root, query, cb) -> cb.conjunction();
        if (q != null && !q.isBlank()) {
            String motif = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), motif),
                    cb.like(cb.lower(root.get("intitule")), motif)));
        }
        if (actif != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("actif"), actif));
        }
        return PageResponse.from(repository.findAll(spec, pageable), mapper::toResponse);
    }

    /**
     * Obtient une grille salariale par son identifiant.
     *
     * @param id identifiant de la grille
     * @return grille salariale
     */
    @Transactional(readOnly = true)
    public GrilleSalarialeResponse obtenir(Long id) {
        return mapper.toResponse(trouver(id));
    }

    /**
     * Crée une nouvelle grille salariale.
     * Règle ORG-01 : code unique sans tenir compte de la casse.
     * Règle ORG-02 : salaireMax >= salaireMin.
     * Règle ORG-08 : enregistrement du journal d'audit.
     *
     * @param request données de création
     * @return grille salariale créée
     */
    @Transactional
    public GrilleSalarialeResponse creer(GrilleSalarialeRequest request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (repository.existsByCodeIgnoreCase(code)) {
            throw DuplicateResourceException.of("Une grille salariale", "le code", code);
        }
        verifierBornes(request.salaireMin(), request.salaireMax());
        GrilleSalariale grille = mapper.toEntity(request);
        grille.setCode(code);
        GrilleSalarialeResponse response = mapper.toResponse(repository.save(grille));
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.GRILLE_SALARIALE,
                response.id(), null, response, "system");
        return response;
    }

    /**
     * Modifie une grille salariale existante.
     * Règle ORG-02 : salaireMax >= salaireMin.
     * Règle ORG-05 : les bornes ne peuvent pas exclure le salaire d'un contrat actif (non vérifié dans le MVP).
     * Règle ORG-08 : enregistrement du journal d'audit.
     *
     * @param id      identifiant de la grille
     * @param request données de modification
     * @return grille salariale modifiée
     */
    @Transactional
    public GrilleSalarialeResponse modifier(Long id, GrilleSalarialeUpdateRequest request) {
        GrilleSalariale grille = trouver(id);
        verifierBornes(request.salaireMin(), request.salaireMax());
        GrilleSalarialeResponse avant = mapper.toResponse(grille);
        mapper.updateEntity(request, grille);
        GrilleSalarialeResponse apres = mapper.toResponse(grille);
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.GRILLE_SALARIALE,
                id, avant, apres, "system");
        return apres;
    }

    /**
     * Active une grille salariale.
     * Règle ORG-04 : désactivation interdite si des postes actifs l'utilisent.
     * Règle ORG-08 : enregistrement du journal d'audit.
     *
     * @param id identifiant de la grille
     * @return grille salariale activée
     */
    @Transactional
    public GrilleSalarialeResponse activer(Long id) {
        GrilleSalariale grille = trouver(id);
        grille.setActif(true);
        return mapper.toResponse(grille);
    }

    /**
     * Désactive une grille salariale.
     * Règle ORG-04 : désactivation interdite si des postes actifs l'utilisent.
     * Règle ORG-08 : enregistrement du journal d'audit.
     *
     * @param id identifiant de la grille
     * @return grille salariale désactivée
     */
    @Transactional
    public GrilleSalarialeResponse desactiver(Long id) {
        GrilleSalariale grille = trouver(id);
        if (repository.countPostesActifsByGrilleSalarialeId(id) > 0) {
            throw new BusinessRuleException("La grille salariale ne peut pas être désactivée car elle est utilisée par des postes actifs.");
        }
        GrilleSalarialeResponse avant = mapper.toResponse(grille);
        grille.setActif(false);
        GrilleSalarialeResponse apres = mapper.toResponse(grille);
        auditService.journaliser(ActionAudit.SUPPRESSION_LOGIQUE, TypeEntite.GRILLE_SALARIALE,
                id, avant, apres, "system");
        return apres;
    }

    private GrilleSalariale trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Grille salariale", id));
    }

    private void verifierBornes(java.math.BigDecimal min, java.math.BigDecimal max) {
        if (max.compareTo(min) < 0) {
            throw new BusinessRuleException("Le salaire maximum doit être supérieur ou égal au salaire minimum.");
        }
    }

    private void verifierTri(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!TRIS_AUTORISES.contains(order.getProperty())) {
                throw new BusinessRuleException("Tri non autorisé sur le champ « %s ».".formatted(order.getProperty()));
            }
        });
    }
}
