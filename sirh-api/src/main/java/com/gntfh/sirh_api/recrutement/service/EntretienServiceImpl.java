package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.AnnulationEntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienRequest;
import com.gntfh.sirh_api.recrutement.dto.EntretienResponse;
import com.gntfh.sirh_api.recrutement.dto.EntretienUpdateRequest;
import com.gntfh.sirh_api.recrutement.enums.StatutEntretien;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Implémentation du service EntretienService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntretienServiceImpl implements EntretienService {

    @Override
    public EntretienResponse obtenir(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PageResponse<EntretienResponse> rechercher(Long candidatureId, StatutEntretien statut,
                                                     LocalDate du, LocalDate au, Pageable pageable, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public EntretienResponse creer(EntretienRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public EntretienResponse modifier(Long id, EntretienUpdateRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public EntretienResponse cloturer(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public EntretienResponse annuler(Long id, AnnulationEntretienRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public EntretienResponse marquerAbsent(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }
}
