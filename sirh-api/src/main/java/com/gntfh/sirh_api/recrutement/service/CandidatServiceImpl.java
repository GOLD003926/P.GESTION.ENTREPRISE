package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.CandidatRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatResponse;
import com.gntfh.sirh_api.recrutement.entity.Candidat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service CandidatService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidatServiceImpl implements CandidatService {

    @Override
    @Transactional
    public CandidatResponse creer(CandidatRequest request) {
        throw new UnsupportedOperationException("TODO RG-16");
    }

    @Override
    @Transactional
    public CandidatResponse modifier(Long id, CandidatRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public CandidatResponse obtenir(Long id) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PageResponse<CandidatResponse> rechercher(String q, Pageable pageable) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public Candidat trouverOuCreer(CandidatRequest request) {
        // API interne
        throw new UnsupportedOperationException("TODO");
    }
}
