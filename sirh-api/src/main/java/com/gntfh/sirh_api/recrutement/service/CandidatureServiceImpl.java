package com.gntfh.sirh_api.recrutement.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.recrutement.dto.CandidatureRequest;
import com.gntfh.sirh_api.recrutement.dto.CandidatureResponse;
import com.gntfh.sirh_api.recrutement.enums.StatutCandidature;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implémentation du service CandidatureService.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CandidatureServiceImpl implements CandidatureService {

    @Override
    @Transactional
    public CandidatureResponse creer(CandidatureRequest request, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO RG-16, RG-17");
    }

    @Override
    @Transactional
    public CandidatureResponse televerserCv(Long id, MultipartFile fichier, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO REC-03");
    }

    @Override
    public CandidatureResponse obtenir(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO REC-09");
    }

    @Override
    public PageResponse<CandidatureResponse> rechercher(Long offreId, StatutCandidature statut, Long candidatId,
                                                        Pageable pageable, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO REC-09");
    }

    @Override
    @Transactional
    public CandidatureResponse desister(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public CandidatureResponse promouvoirListeAttente(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO REC-08");
    }

    @Override
    public PageResponse<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser) {
        throw new UnsupportedOperationException("TODO");
    }

    // ==================== API interne ====================

    @Override
    @Transactional
    public void marquerPropositionEnvoyee(Long id, String auteur) {
        // API interne
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public void marquerEmbauchee(Long id, String auteur) {
        // API interne
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public void marquerNonRetenueApresProposition(Long id, String auteur, String motif) {
        // API interne
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    @Transactional
    public void remettreRetenue(Long id, String auteur) {
        // API interne
        throw new UnsupportedOperationException("TODO");
    }
}
