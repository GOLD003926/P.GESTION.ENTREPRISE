package com.gntfh.sirh_api.embauche.service;

import com.gntfh.sirh_api.common.security.CurrentUser;
import com.gntfh.sirh_api.embauche.dto.PropositionEmbaucheResponse;
import com.gntfh.sirh_api.embauche.dto.ReponsePropositionRequest;
import com.gntfh.sirh_api.embauche.dto.ReponsePropositionResultat;

import java.time.LocalDate;

public interface EmbaucheService {

    ReponsePropositionResultat repondre(
            Long propositionId,
            ReponsePropositionRequest request,
            CurrentUser currentUser
    );

    ReponsePropositionResultat accepter(Long propositionId, String auteur);

    PropositionEmbaucheResponse refuser(Long propositionId, String motif, String auteur);

    PropositionEmbaucheResponse demanderDelai(Long propositionId, String auteur);

    int expirerPropositionsEchues(LocalDate aujourdhui);
}
