package com.gntfh.sirh_api.organisation.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
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

    @Transactional(readOnly = true)
    public PageResponse<DepartementResponse> rechercher(String q, Boolean actif, Long parentId, Pageable pageable) {
        throw new UnsupportedOperationException("TODO ORG-08");
    }

    @Transactional(readOnly = true)
    public DepartementResponse obtenir(Long id) {
        throw new UnsupportedOperationException("TODO ORG-08");
    }

    @Transactional
    public DepartementResponse creer(DepartementRequest request) {
        if (request.responsableId() != null && !employeRepository.existsById(request.responsableId())) {
            throw ResourceNotFoundException.of("Employe", request.responsableId());
        }
        throw new UnsupportedOperationException("TODO ORG-01 ORG-03 ORG-07 ORG-08");
    }

    @Transactional
    public DepartementResponse modifier(Long id, DepartementUpdateRequest request) {
        if (request.responsableId() != null && !employeRepository.existsById(request.responsableId())) {
            throw ResourceNotFoundException.of("Employe", request.responsableId());
        }
        throw new UnsupportedOperationException("TODO ORG-03 ORG-07 ORG-08");
    }

    @Transactional
    public DepartementResponse activer(Long id) {
        throw new UnsupportedOperationException("TODO ORG-04 ORG-08");
    }

    @Transactional
    public DepartementResponse desactiver(Long id) {
        throw new UnsupportedOperationException("TODO ORG-04 ORG-08");
    }
}
