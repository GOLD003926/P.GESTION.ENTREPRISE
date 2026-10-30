package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.CandidatRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatResponse;
import com.gntfh.sirh_api.recrutement.entity.Candidat;
import com.gntfh.sirh_api.recrutement.mapper.CandidatMapper;
import com.gntfh.sirh_api.recrutement.repository.CandidatRepository;
import com.gntfh.sirh_api.common.exception.DuplicateResourceException;
import com.gntfh.sirh_api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service CandidatService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidatServiceImpl implements CandidatService {
    private final CandidatRepository repository;
    private final CandidatMapper mapper;

    @Override
    @Transactional
    public CandidatResponse creer(CandidatRequest request) {
        String email = request.email().trim().toLowerCase(java.util.Locale.ROOT);
        if (repository.existsByEmailIgnoreCase(email)) throw DuplicateResourceException.of("Candidat", "email", email);
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    @Transactional
    public CandidatResponse modifier(Long id, CandidatRequest request, CurrentUser currentUser) {
        Candidat c = repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Candidat", id));
        if (!c.getEmail().equalsIgnoreCase(request.email()) && repository.existsByEmailIgnoreCase(request.email()))
            throw DuplicateResourceException.of("Candidat", "email", request.email());
        mapper.updateEntity(request, c);
        return mapper.toResponse(c);
    }

    @Override
    public CandidatResponse obtenir(Long id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Candidat", id)));
    }

    @Override
    public PageResponse<CandidatResponse> rechercher(String q, Pageable pageable) {
        Specification<Candidat> spec = (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String like = "%" + q.toLowerCase(java.util.Locale.ROOT) + "%";
            return cb.or(cb.like(cb.lower(root.get("nom")), like), cb.like(cb.lower(root.get("prenom")), like),
                    cb.like(cb.lower(root.get("email")), like));
        };
        return PageResponse.from(repository.findAll(spec, pageable), mapper::toResponse);
    }

    @Override
    @Transactional
    public Candidat trouverOuCreer(CandidatRequest request) {
        // API interne
        return repository.findByEmailIgnoreCase(request.email().trim()).orElseGet(() -> repository.save(mapper.toEntity(request)));
    }
}
