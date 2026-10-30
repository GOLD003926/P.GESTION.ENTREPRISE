package com.gntfh.sirh_api.onboarding.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.common.exception.BusinessRuleException;
import com.gntfh.sirh_api.common.exception.DuplicateResourceException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import com.gntfh.sirh_api.onboarding.dto.ModeleOnboardingRequest;
import com.gntfh.sirh_api.onboarding.dto.ModeleOnboardingResponse;
import com.gntfh.sirh_api.onboarding.entity.ModeleOnboarding;
import com.gntfh.sirh_api.onboarding.entity.ModeleTache;
import com.gntfh.sirh_api.onboarding.mapper.ModeleOnboardingMapper;
import com.gntfh.sirh_api.onboarding.repository.ModeleOnboardingRepository;
import com.gntfh.sirh_api.onboarding.repository.ModeleTacheRepository;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModeleOnboardingServiceImpl implements ModeleOnboardingService {
    private final ModeleOnboardingRepository repository;
    private final ModeleTacheRepository tacheRepository;
    private final ModeleOnboardingMapper mapper;

    /** ONB-01, ONB-02. */
    @Override
    @Transactional
    public ModeleOnboardingResponse creer(ModeleOnboardingRequest request, CurrentUser currentUser) {
        verifierTaches(request);
        if (repository.existsByNomIgnoreCase(request.nom())) {
            throw DuplicateResourceException.of("ModeleOnboarding", "nom", request.nom());
        }
        ModeleOnboarding modele = repository.save(mapper.toEntity(request));
        request.taches().forEach(t -> tacheRepository.save(mapper.toTache(t, modele)));
        return response(modele);
    }

    /** ONB-03 : les tâches déjà copiées restent indépendantes du modèle modifié. */
    @Override
    @Transactional
    public ModeleOnboardingResponse modifier(Long id, ModeleOnboardingRequest request, CurrentUser currentUser) {
        verifierTaches(request);
        ModeleOnboarding modele = trouver(id);
        if (repository.existsByNomIgnoreCaseAndIdNot(request.nom(), id)) {
            throw DuplicateResourceException.of("ModeleOnboarding", "nom", request.nom());
        }
        mapper.updateEntity(modele, request);
        tacheRepository.deleteByModeleId(id);
        request.taches().forEach(t -> tacheRepository.save(mapper.toTache(t, modele)));
        return response(modele);
    }

    @Override
    public ModeleOnboardingResponse obtenir(Long id) {
        return response(trouver(id));
    }

    /** ONB-01 : tri autorisé uniquement sur id, nom et actif. */
    @Override
    public PageResponse<ModeleOnboardingResponse> rechercher(String q, Boolean actif, Pageable pageable) {
        verifierTri(pageable);
        Specification<ModeleOnboarding> specification = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (q != null && !q.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("nom")), "%" + q.trim().toLowerCase() + "%"));
            }
            if (actif != null) predicates.add(cb.equal(root.get("actif"), actif));
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Page<ModeleOnboarding> page = repository.findAll(specification, pageable);
        return PageResponse.from(page, this::response);
    }

    /** ONB-01 : rend le modèle actif pour les prochains onboardings. */
    @Override
    @Transactional
    public void activer(Long id) {
        trouver(id).setActif(true);
    }

    /** ONB-01 : désactive le modèle sans supprimer les copies de tâches existantes. */
    @Override
    @Transactional
    public void desactiver(Long id) {
        trouver(id).setActif(false);
    }

    /** API interne : sélection du type exact, repli sur le modèle sans type. */
    @Override
    public ModeleOnboarding choisirPour(String typeContrat) {
        return (typeContrat == null ? java.util.Optional.<ModeleOnboarding>empty()
                : repository.findFirstByTypeContratAndActifTrue(typeContrat))
                .or(() -> repository.findFirstByTypeContratIsNullAndActifTrue())
                .orElseThrow(() -> new BusinessRuleException("Aucun modèle d'onboarding actif n'est disponible."));
    }

    private ModeleOnboarding trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("ModeleOnboarding", id));
    }

    private ModeleOnboardingResponse response(ModeleOnboarding modele) {
        return mapper.toResponse(modele, tacheRepository.findByModeleIdOrderByOrdreAsc(modele.getId()));
    }

    private void verifierTaches(ModeleOnboardingRequest request) {
        var ordres = request.taches().stream().map(t -> t.ordre()).toList();
        var codes = request.taches().stream().map(t -> t.code()).toList();
        if (ordres.size() != new java.util.HashSet<>(ordres).size()
                || codes.size() != new java.util.HashSet<>(codes).size()) {
            throw new BusinessRuleException("Les ordres et les codes des tâches doivent être uniques.");
        }
    }

    private void verifierTri(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!Set.of("id", "nom", "actif").contains(order.getProperty())) {
                throw new BusinessRuleException("Tri non autorisé.");
            }
        });
    }
}
