package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.AnnulationEntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatureResponse;
import com.gntfh.sirh_api.recrutement.dto.DecisionFinaleRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienResponse;
import com.gntfh.sirh_api.recrutement.dto.EntretienUpdateRequest;
import com.gntfh.sirh_api.recrutement.dto.PreselectionRequest;
import com.gntfh.sirh_api.recrutement.enums.Avis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service SelectionWorkflowService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SelectionWorkflowServiceImpl implements SelectionWorkflowService {

    @Override
    @Transactional
    public CandidatureResponse preselectionner(Long candidatureId, PreselectionRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-18");
    }

    @Override
    @Transactional
    public EntretienResponse planifierEntretien(EntretienRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-19, REC-04, REC-05");
    }

    @Override
    @Transactional
    public EntretienResponse reprogrammer(Long entretienId, EntretienUpdateRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public EntretienResponse cloturerEntretien(Long entretienId, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-20, REC-06");
    }

    @Override
    @Transactional
    public EntretienResponse annulerEntretien(Long id, AnnulationEntretienRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public EntretienResponse marquerAbsent(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public CandidatureResponse decisionFinale(Long candidatureId, DecisionFinaleRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-22, REC-07");
    }

    @Override
    public Avis avisDuNiveau(Long candidatureId, int niveau) {
        throw new UnsupportedOperationException("TODO REC-06");
    }
}
