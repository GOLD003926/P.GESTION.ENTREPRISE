package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatRequest;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatResponse;
import com.gntfh.sirh_api.recrutement.dto.EvaluationCandidatUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service EvaluationCandidatService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluationCandidatServiceImpl implements EvaluationCandidatService {

    @Override
    @Transactional
    public EvaluationCandidatResponse creer(EvaluationCandidatRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-20, REC-10");
    }

    @Override
    @Transactional
    public EvaluationCandidatResponse modifier(Long id, EvaluationCandidatUpdateRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-21");
    }

    @Override
    @Transactional
    public EvaluationCandidatResponse verrouiller(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public EvaluationCandidatResponse corriger(Long id, EvaluationCandidatUpdateRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-21");
    }

    @Override
    public PageResponse<EvaluationCandidatResponse> lister(Long entretienId, Pageable pageable, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }
}
