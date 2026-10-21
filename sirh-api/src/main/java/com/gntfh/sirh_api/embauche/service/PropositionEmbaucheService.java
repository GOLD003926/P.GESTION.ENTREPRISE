package com.gntfh.sirh_api.embauche.service;

import com.gntfh.sirh_api.common.audit.dto.HistoriqueStatutResponse;
import com.gntfh.sirh_api.common.dto.PageResponse;
import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.embauche.dto.DerogationRequest;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheRequest;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheResponse;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheUpdateRequest;
import com.gntfh.sirh_api.embauche.enums.StatutProposition;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PropositionEmbaucheService {

    PropositionEmbaucheResponse creer(PropositionEmbaucheRequest request, CurrentUser currentUser);

    PropositionEmbaucheResponse modifier(
            Long id,
            PropositionEmbaucheUpdateRequest request,
            CurrentUser currentUser
    );

    PropositionEmbaucheResponse obtenir(Long id, CurrentUser currentUser);

    PageResponse<PropositionEmbaucheResponse> rechercher(
            Long candidatureId,
            StatutProposition statut,
            Pageable pageable,
            CurrentUser currentUser
    );

    PropositionEmbaucheResponse validerDerogation(Long id, DerogationRequest request, CurrentUser currentUser);

    PropositionEmbaucheResponse envoyer(Long id, CurrentUser currentUser);

    PropositionEmbaucheResponse retirer(Long id, CurrentUser currentUser);

    List<HistoriqueStatutResponse> historique(Long id, CurrentUser currentUser);
}
