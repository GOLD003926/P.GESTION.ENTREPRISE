package com.gntfh.sirh_api.organisation.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.audit.enums.ActionAudit;
import com.gntfh.sirh_api.common.audit.enums.TypeEntite;
import com.gntfh.sirh_api.common.audit.service.AuditService;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.DuplicateResourceException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.organisation.dto.PosteRequest;
import com.gntfh.sirh_api.organisation.dto.PosteResponse;
import com.gntfh.sirh_api.organisation.dto.PosteUpdateRequest;
import com.gntfh.sirh_api.organisation.entity.Poste;
import com.gntfh.sirh_api.organisation.entity.Departement;
import com.gntfh.sirh_api.organisation.entity.GrilleSalariale;
import com.gntfh.sirh_api.organisation.mapper.PosteMapper;
import com.gntfh.sirh_api.organisation.port.PosteOccupationProvider;
import com.gntfh.sirh_api.organisation.repository.PosteRepository;
import com.gntfh.sirh_api.organisation.repository.DepartementRepository;
import com.gntfh.sirh_api.organisation.repository.GrilleSalarialeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Set;

/**
 * Service pour la gestion des postes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PosteService {

    private final PosteRepository repository;
    private final PosteMapper mapper;
    private final ObjectProvider<PosteOccupationProvider> occupationProvider;
    private final DepartementRepository departementRepository;
    private final GrilleSalarialeRepository grilleSalarialeRepository;
    private final AuditService auditService;
    private static final Set<String> TRIS_AUTORISES =
            Set.of("id", "code", "intitule", "effectifAutorise", "actif");

    @Transactional(readOnly = true)
    public PageResponse<PosteResponse> rechercher(String q, Long departementId, Long grilleSalarialeId, Boolean actif, Pageable pageable) {
        verifierTri(pageable);
        Specification<Poste> spec = (root, query, cb) -> cb.conjunction();
        if (q != null && !q.isBlank()) {
            String motif = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(cb.like(cb.lower(root.get("code")), motif),
                    cb.like(cb.lower(root.get("intitule")), motif)));
        }
        if (departementId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("departement").get("id"), departementId));
        if (grilleSalarialeId != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("grilleSalariale").get("id"), grilleSalarialeId));
        if (actif != null) spec = spec.and((root, query, cb) -> cb.equal(root.get("actif"), actif));
        PosteOccupationProvider provider = occupationProvider.getIfAvailable(() -> posteId -> 0);
        return PageResponse.from(repository.findAll(spec, pageable),
                poste -> mapper.toResponse(poste, provider.occupationActuelle(poste.getId())));
    }

    @Transactional(readOnly = true)
    public PosteResponse obtenir(Long id) {
        Poste poste = trouver(id);
        return mapper.toResponse(poste, occupation(id));
    }

    @Transactional
    public PosteResponse creer(PosteRequest request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (repository.existsByCodeIgnoreCase(code)) throw DuplicateResourceException.of("Un poste", "le code", code);
        Departement departement = chargerDepartementActif(request.departementId());
        GrilleSalariale grille = chargerGrilleActive(request.grilleSalarialeId());
        Poste poste = mapper.toEntity(request);
        poste.setCode(code);
        poste.setDepartement(departement);
        poste.setGrilleSalariale(grille);
        PosteResponse response = mapper.toResponse(repository.save(poste), 0);
        auditService.journaliser(ActionAudit.CREATION, TypeEntite.POSTE, response.id(), null, response, "system");
        return response;
    }

    @Transactional
    public PosteResponse modifier(Long id, PosteUpdateRequest request) {
        Poste poste = trouver(id);
        Departement departement = chargerDepartementActif(request.departementId());
        GrilleSalariale grille = chargerGrilleActive(request.grilleSalarialeId());
        int occupation = occupation(id);
        if (request.effectifAutorise() < occupation) {
            throw new BusinessRuleException("L'effectif autorisé ne peut pas être inférieur à l'occupation actuelle.");
        }
        PosteResponse avant = mapper.toResponse(poste, occupation);
        mapper.updateEntity(request, poste);
        poste.setDepartement(departement);
        poste.setGrilleSalariale(grille);
        PosteResponse apres = mapper.toResponse(poste, occupation);
        auditService.journaliser(ActionAudit.MODIFICATION, TypeEntite.POSTE, id, avant, apres, "system");
        return apres;
    }

    @Transactional
    public PosteResponse activer(Long id) {
        Poste poste = trouver(id);
        poste.setActif(true);
        return mapper.toResponse(poste, occupation(id));
    }

    @Transactional
    public PosteResponse desactiver(Long id) {
        Poste poste = trouver(id);
        int occupation = occupation(id);
        if (occupation > 0) throw new BusinessRuleException("Le poste ne peut pas être désactivé car il est occupé.");
        PosteResponse avant = mapper.toResponse(poste, occupation);
        poste.setActif(false);
        PosteResponse apres = mapper.toResponse(poste, occupation);
        auditService.journaliser(ActionAudit.SUPPRESSION_LOGIQUE, TypeEntite.POSTE, id, avant, apres, "system");
        return apres;
    }

    /**
     * API interne : retourne le nombre de places libres d'un poste.
     * Règle RG-04 : utilisé par le module demande.
     *
     * @param posteId identifiant du poste
     * @return nombre de places libres
     */
    @Transactional(readOnly = true)
    public int placesLibres(Long posteId) {
        Poste poste = trouver(posteId);
        return Math.max(0, poste.getEffectifAutorise() - occupation(posteId));
    }

    /**
     * API interne : trouve un poste actif par son identifiant.
     * Lève BusinessRuleException si le poste est inactif.
     *
     * @param posteId identifiant du poste
     * @return poste trouvé
     */
    @Transactional(readOnly = true)
    public Poste trouverActif(Long posteId) {
        Poste poste = trouver(posteId);
        if (!Boolean.TRUE.equals(poste.getActif())) throw new BusinessRuleException("Le poste est inactif.");
        return poste;
    }

    private Poste trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Poste", id));
    }

    private int occupation(Long id) {
        return occupationProvider.getIfAvailable(() -> posteId -> 0).occupationActuelle(id);
    }

    private Departement chargerDepartementActif(Long id) {
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Département", id));
        if (!Boolean.TRUE.equals(departement.getActif())) throw new BusinessRuleException("Le département doit être actif.");
        return departement;
    }

    private GrilleSalariale chargerGrilleActive(Long id) {
        GrilleSalariale grille = grilleSalarialeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Grille salariale", id));
        if (!Boolean.TRUE.equals(grille.getActif())) throw new BusinessRuleException("La grille salariale doit être active.");
        return grille;
    }

    private void verifierTri(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!TRIS_AUTORISES.contains(order.getProperty())) throw new BusinessRuleException("Tri non autorisé sur le champ « %s ».".formatted(order.getProperty()));
        });
    }
}
