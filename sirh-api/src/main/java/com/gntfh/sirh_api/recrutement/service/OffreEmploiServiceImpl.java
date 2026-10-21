package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiRequest;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiResponse;
import com.gntfh.sirh_api.recrutement.dto.OffreEmploiUpdateRequest;
import com.gntfh.sirh_api.recrutement.enums.StatutOffre;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du service OffreEmploiService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OffreEmploiServiceImpl implements OffreEmploiService {

    @Override
    @Transactional
    public OffreEmploiResponse creer(OffreEmploiRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-14, REC-01");
    }

    @Override
    @Transactional
    public OffreEmploiResponse modifier(Long id, OffreEmploiUpdateRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO REC-02");
    }

    @Override
    public OffreEmploiResponse obtenir(Long id) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public PageResponse<OffreEmploiResponse> rechercher(String q, StatutOffre statut, Long demandeId, Pageable pageable) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public OffreEmploiResponse publier(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-15");
    }

    @Override
    @Transactional
    public OffreEmploiResponse cloturer(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public OffreEmploiResponse annuler(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public void cloturerSiEffectifAtteint(Long demandeId, String auteur) {
        // API interne
        throw new UnsupportedOperationException("TODO RG-24");
    }

    @Override
    @Transactional
    public int cloturerOffresEchues(java.time.LocalDate aujourdhui) {
        throw new UnsupportedOperationException("TODO");
    }
}
